/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
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
  /**
   * DOCUMENT-ME
   */
  public static final int SPECIAL_LEVEL = Integer.MIN_VALUE / 2;

  private final SkillType type;
  private final boolean noSkillPoints;

  /**
   * The total of points_per_person persons as it is found in the report. Note, that the report can
   * contain skill point values that are not dividable by the unit's number of persons (e.g. 65 skill
   * point with a unit of 2 persons)
   */
  private int points;
  private int level;

  /**
   * The number of persons in the unit this skill belongs to.
   *
   * @deprecated (stm) This seems to be obsolete.
   */
  @Deprecated
  private int persons;

  /** The level of change. Only important in merged reports. */
  private int changeLevel;

  // /** Holds value of property levelChanged. */
  // private boolean levelChanged;

  /**
   * Creates a new Skill object.
   *
   * @param type
   * @param points
   * @param level
   * @param persons
   * @param noSkillPoints Indicated whether the skill points value of this Skill object has relevance
   */
  public Skill(SkillType type, int points, int level, int persons, boolean noSkillPoints) {
    this.type = type;
    this.points = points;
    this.level = level;
    this.persons = persons;
    this.noSkillPoints = noSkillPoints;

    if (level < 0 && level != SPECIAL_LEVEL) {
      magellan.library.utils.logging.Logger.getInstance(this.getClass()).warnOnce(
          "negative skill level " + this + " " + level);
    }
  }

  /**
   * Returns the skill points required to reach the specified level. They are given by the formula
   * <code>pointsAtLevel(level) = 30 * (((level + 1) * level) / 2)</code>.
   */
  public static final int getPointsAtLevel(int level) {
    return 30 * (((level + 1) * level) / 2);
  }

  /**
   * Returns the skill level gained at the specified number of skill points according to
   * {@link #getPointsAtLevel(int)}.
   */
  public static final int getLevelAtPoints(int points) {
    int i = 1;

    while (Skill.getPointsAtLevel(i) <= points) {
      ++i;
    }

    return i - 1;
  }

  /**
   * Calculate the skill level from skill points according to the formula
   * {@link #getLevelAtPoints(int)} including given bonuses.
   *
   * @param pointsPerPerson
   * @param raceBonus
   * @param terrainBonus
   * @param buildingBonus
   * @param isStarving
   */
  public static final int getLevel(int pointsPerPerson, int raceBonus, int terrainBonus,
      int buildingBonus, boolean isStarving) {
    int baseLevel = Skill.getLevelAtPoints(pointsPerPerson);
    int level = 0;

    if (baseLevel > 0) {
      level = Math.max(baseLevel + raceBonus + terrainBonus, 0);
    }

    if (level > 0) {
      level = Math.max(level + buildingBonus, 0);
    }

    if (isStarving) {
      level /= 2;
    }

    return level;
  }

  /**
   * Re-calculate the skill level from the skillPoints including race bonus, terrain bonus and (if
   * includeBuilding==true) building bonus of the given unit. This skill is not changed by this
   * method.
   *
   * @param unit
   * @param includeBuilding
   * @return The changed skill level.
   */
  public int getLevel(Unit unit, boolean includeBuilding) {
    if ((unit != null) && (unit.getPersons() != 0)) {
      int raceBonus = 0;
      int terrainBonus = 0;
      int buildingBonus = 0;

      if (unit.getRace() != null) {
        raceBonus = unit.getRace().getSkillBonus(getSkillType());
      }

      if (unit.getRegion() != null) {
        terrainBonus =
            unit.getRace().getSkillBonus(getSkillType(), unit.getRegion().getRegionType());
      }

      if (includeBuilding && (unit.getBuilding() != null)) {
        buildingBonus = (unit.getBuilding().getBuildingType()).getSkillBonus(getSkillType());
      }

      return Skill.getLevel(getPoints() / unit.getPersons(), raceBonus, terrainBonus,
          buildingBonus, unit.isStarving());
    }

    return 0;
  }

  /**
   * Calculates the skill level for the given modified number of persons in the unit and the skill
   * points of this skill.
   *
   * @deprecated (stm) This is not used by anyone and I'm not sure if it's correct any more.
   */
  @Deprecated
  public int getModifiedLevel(Unit unit, boolean includeBuilding) {
    if ((unit != null) && (unit.getModifiedPersons() != 0)) {
      int raceBonus = 0;
      int terrainBonus = 0;
      int buildingBonus = 0;

      if (unit.getRace() != null) {
        raceBonus = unit.getRace().getSkillBonus(getSkillType());
      }

      if (unit.getRegion() != null) {
        terrainBonus =
            unit.getRace().getSkillBonus(getSkillType(), unit.getRegion().getRegionType());
      }

      if (includeBuilding && (unit.getBuilding() != null)) {
        buildingBonus = unit.getBuilding().getBuildingType().getSkillBonus(getSkillType());
      }

      return Skill.getLevel(getPoints() / unit.getModifiedPersons(), raceBonus, terrainBonus,
          buildingBonus, unit.isStarving());
    }

    return 0;
  }

  /**
   * Returns the modifier that the specified race has on the specified skill in the specified terrain.
   *
   * @param skillType
   * @param race
   * @param terrain
   */
  public static int getModifier(SkillType skillType, Race race, RegionType terrain) {
    int modifier = 0;

    if (race != null) {
      modifier += race.getSkillBonus(skillType);

      if (terrain != null) {
        modifier += race.getSkillBonus(skillType, terrain);
      }
    }

    return modifier;
  }

  /**
   * Returns the modifier that the specified unit's race has on the specified skill in the terrain the
   * specified unit resides in.
   *
   * @param skillType
   * @param unit
   */
  public static int getModifier(SkillType skillType, Unit unit) {
    Race realRace = unit.getRace();
    RegionType terrain = (unit.getRegion() != null) ? unit.getRegion().getRegionType() : null;

    return Skill.getModifier(skillType, realRace, terrain);
  }

  /**
   * Returns the modifier that the specified unit's race has on this skill in the terrain the
   * specified unit resides in.
   *
   * @param unit
   */
  public int getModifier(Unit unit) {
    return Skill.getModifier(type, unit);
  }

  /**
   * Indicated whether the skill points value of this Skill object has relevance, i.e. was either read
   * from a report or calculated as there can be reports with only skill levels and no points.
   */
  public boolean noSkillPoints() {
    return noSkillPoints;
  }

  /**
   * Return the skill type of this skill.
   */
  public SkillType getSkillType() {
    return type;
  }

  /**
   * Return the name of this skill (which is the name of its SkillType).
   */
  public String getName() {
    return type.getName();
  }

  /**
   * Sets the level of the skill to <code>l</code>. A level &lt; 0 indicates a skill that was present in
   * the last round but is lost now.
   *
   * @param l
   */
  public void setLevel(int l) {
    level = l;
    if (level < 0 && level != SPECIAL_LEVEL) {
      magellan.library.utils.logging.Logger.getInstance(this.getClass()).warnOnce(
          "negative skill level " + this + " " + l);
    }
  }

  /**
   * Indicates a lost skill. -{@link #getChangeLevel()} is the old level.
   *
   * @param oldLevel
   */
  public void setLostLevel(int oldLevel) {
    level = SPECIAL_LEVEL;
    changeLevel = -oldLevel;
  }

  /**
   * Returns the level of the skill. Returns 0 if level is less than 0.
   */
  public int getLevel() {
    return Math.max(0, level);
  }

  /**
   * Return the level of the skill, which may be negative (indicating a lost skill).
   */
  public int getRealLevel() {
    return level;
  }

  /**
   * Sets the number of skill points.
   */
  public void setPoints(int d) {
    points = d;
  }

  /**
   * Returns the number of skill points
   */
  public int getPoints() {
    return points;
  }

  /**
   * The number of persons in the unit this skill belongs to.
   *
   * @param p
   * @deprecated (stm) This information seems to be obsolete.
   */
  @Deprecated
  public void setPersons(int p) {
    persons = p;
  }

  /**
   * @deprecated This seems to be somewhat obsolete as skill points are not used consequently any
   *             more.
   */
  @Deprecated
  public int getPointsPerPerson() {
    if (persons != 0)
      return getPoints() / persons;
    else
      return 0;
  }

  /**
   * The level of change. Only important in merged reports (?).
   */
  public int getChangeLevel() {
    return changeLevel;
  }

  /**
   * Set the level of change. Only important in merged reports (?).
   */
  public void setChangeLevel(int change) {
    changeLevel = change;

    // if (changeLevel != 0) {
    // setLevelChanged(true);
    // }
  }

  /**
   * Get a string representation of this skill.
   */
  @Override
  public String toString() {
    if (noSkillPoints())
      return getName() + " " + getLevel();
    else
      return getName() + " " + getLevel() + " [" + getPointsPerPerson() + "]";
  }

  /**
   * Getter for property levelChanged.
   *
   * @return Value of property levelChanged.
   */
  public boolean isLevelChanged() {
    return changeLevel != 0;
  }

  /**
   * Setter for property levelChanged.
   *
   * @param levelChanged New value of property levelChanged.
   * @deprecated just use {@link #setChangeLevel(int)}
   */
  @Deprecated
  public void setLevelChanged(boolean levelChanged) {
    // this.levelChanged = levelChanged;
  }

  /**
   * Should return <code>true</code> if this skill was present in the previous round.
   */
  public boolean isLostSkill() {
    return level < 0;
  }

}
