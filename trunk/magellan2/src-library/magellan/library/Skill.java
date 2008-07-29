/*
 *  Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe,
 *                          Stefan Goetz, Sebastian Pappert,
 *                          Klaas Prause, Enno Rehling,
 *                          Sebastian Tusk, Ulrich Kuester,
 *                          Ilja Pavkovic
 *
 * This file is part of the Eressea Java Code Base, see the
 * file LICENSING for the licensing information applying to
 * this file.
 *
 */

package magellan.library;

import magellan.library.rules.Race;
import magellan.library.rules.RegionType;
import magellan.library.rules.SkillType;

/**
 * A class representing a certain skill level of a unit for some skill type. Since there is no
 * strict mapping of skill points to skill levels, this class allows to specify points and levels
 * independently of each other but also offers convenience functions for converting the values
 * between each other.
 */
public class Skill {
	private final SkillType type;
	private final boolean noSkillPoints;

	/**
	 * The total of points_per_person  persons as it is found in the report. Note, that the report
	 * can contain skill point values that are not dividable by the unit's number of persons (e.g.
	 * 65 skill point with a unit of 2 persons)
	 */
	private int points = 0;
	private int level = 0;

	/** The number of persons in the unit this skill belongs to. */
	private int persons = 0;

	/** The level of change. Only important in merged reports. */
	private int changeLevel = 0;

	/** Holds value of property levelChanged. */
	private boolean levelChanged;

	/**
	 * Creates a new Skill object.
	 *
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	public Skill(SkillType type, int points, int level, int persons, boolean noSkillPoints) {
		this.type = type;
		this.points = points;
		this.level = level;
		this.persons = persons;
		this.noSkillPoints = noSkillPoints;
	}

	/**
	 * Returns the skill points required to reach the specified level.
	 *
	 * 
	 *
	 * 
	 */
	public static final int getPointsAtLevel(int level) {
		return 30 * (((level + 1) * level) / 2);
	}

	/**
	 * Returns the skill level gained at the specified number of skill points.
	 *
	 * 
	 *
	 * 
	 */
	public static final int getLevelAtPoints(int points) {
		int i = 1;

		while(Skill.getPointsAtLevel(i) <= points) {
			++i;
		}

		return i - 1;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 * 
	 * 
	 * 
	 *
	 * 
	 */
	public static final int getLevel(int pointsPerPerson, int raceBonus, int terrainBonus,
									 int buildingBonus, boolean isStarving) {
		int baseLevel = Skill.getLevelAtPoints(pointsPerPerson);
		int level = 0;

		if(baseLevel > 0) {
			level = Math.max(baseLevel + raceBonus + terrainBonus, 0);
		}

		if(level > 0) {
			level = Math.max(level + buildingBonus, 0);
		}

		if(isStarving) {
			level /= 2;
		}

		return level;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public int getLevel(Unit unit, boolean includeBuilding) {
		if((unit != null) && (unit.getPersons() != 0)) {
			int raceBonus = 0;
			int terrainBonus = 0;
			int buildingBonus = 0;

			if(unit.getRace() != null) {
				raceBonus = unit.getRace().getSkillBonus(getSkillType());
			}

			if(unit.getRegion() != null) {
				terrainBonus = unit.getRace().getSkillBonus(getSkillType(), unit.getRegion().getRegionType());
			}

			if(includeBuilding && (unit.getBuilding() != null)) {
				buildingBonus = (unit.getBuilding().getBuildingType()).getSkillBonus(getSkillType());
			}

			return Skill.getLevel(getPoints() / unit.getPersons(), raceBonus, terrainBonus, buildingBonus,
							unit.isStarving());
		}

		return 0;
	}

	/**
	 * Calculates the skill level for the given modified number of persons in the unit and the
	 * skill points of this skill.
	 *
	 * 
	 * 
	 * @deprecated (stm) This is not used by anyone and I'm not sure if it's correct any more.
	 * 
	 */
	@Deprecated
  public int getModifiedLevel(Unit unit, boolean includeBuilding) {
		if((unit != null) && (unit.getModifiedPersons() != 0)) {
			int raceBonus = 0;
			int terrainBonus = 0;
			int buildingBonus = 0;

			if(unit.getRace() != null) {
				raceBonus = unit.getRace().getSkillBonus(getSkillType());
			}

			if(unit.getRegion() != null) {
				terrainBonus = unit.getRace().getSkillBonus(getSkillType(), unit.getRegion().getRegionType());
			}

			if(includeBuilding && (unit.getBuilding() != null)) {
				buildingBonus = unit.getBuilding().getBuildingType().getSkillBonus(getSkillType());
			}

			return Skill.getLevel(getPoints() / unit.getModifiedPersons(), raceBonus, terrainBonus,
							buildingBonus, unit.isStarving());
		}

		return 0;
	}

	/**
	 * Returns the modifier of the specified race has on the specified skill in the specified
	 * terrain.
	 *
	 * 
	 * 
	 * 
	 *
	 * 
	 */
	public static int getModifier(SkillType skillType, Race race, RegionType terrain) {
		int modifier = 0;

		if(race != null) {
			modifier += race.getSkillBonus(skillType);

			if(terrain != null) {
				modifier += race.getSkillBonus(skillType, terrain);
			}
		}

		return modifier;
	}

	/**
	 * Returns the modifier of the specified unit's race has on the specified skill in the terrain
	 * the specified unit resides in.
	 */
	public static int getModifier(SkillType skillType, Unit unit) {
		Race realRace = unit.getRace();
		RegionType terrain = (unit.getRegion() != null) ? unit.getRegion().getRegionType() : null;

		return Skill.getModifier(skillType, realRace, terrain);
	}

	/**
	 * Returns the modifier of the specified unit's race has on this skill in the terrain the
	 * specified unit resides in.
	 *
	 * 
	 *
	 * 
	 */
	public int getModifier(Unit unit) {
		return Skill.getModifier(this.type, unit);
	}

	/**
	 * Indicated whether the skill points value of this Skill object has relevance, i.e. was either
	 * read from a report or calculated as there can be reports with only skill levels and no
	 * points.
	 *
	 * 
	 */
	public boolean noSkillPoints() {
		return this.noSkillPoints;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public SkillType getSkillType() {
		return type;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getName() {
		return type.getName();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setLevel(int l) {
		level = l;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getLevel() {
		return Math.max(0, level);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getRealLevel() {
		return level;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setPoints(int d) {
		points = d;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getPoints() {
		return points;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setPersons(int p) {
		this.persons = p;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getPointsPerPerson() {
		if(persons != 0) {
			return this.getPoints() / persons;
		} else {
			return 0;
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getChangeLevel() {
		return changeLevel;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setChangeLevel(int change) {
		changeLevel = change;

		if(changeLevel != 0) {
			setLevelChanged(true);
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public String toString() {
		if(this.noSkillPoints()) {
			return getName() + " " + getLevel();
		} else {
			return getName() + " " + getLevel() + " [" + getPointsPerPerson() + "]";
		}
	}

	/**
	 * Getter for property levelChanged.
	 *
	 * @return Value of property levelChanged.
	 */
	public boolean isLevelChanged() {
		return levelChanged;
	}

	/**
	 * Setter for property levelChanged.
	 *
	 * @param levelChanged New value of property levelChanged.
	 */
	public void setLevelChanged(boolean levelChanged) {
		this.levelChanged = levelChanged;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean isLostSkill() {
		return level < 0;
	}
}
