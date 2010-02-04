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

package magellan.library.utils;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import magellan.library.Skill;
import magellan.library.Unit;
import magellan.library.rules.SkillType;
import magellan.library.utils.comparator.SkillComparator;

/**
 * DOCUMENT ME!
 * 
 * @author Ulrich Küster a class for holding statistic information about units and their skills like
 *         number of persons with a specified skill or total skillpoints or things like that. Units
 *         can be added by a call to the addUnit-Method but not removed.
 */
public class SkillStats {
  /**
   * Default constructor
   */
  public SkillStats() {
  }

  /**
   * Constructor that initialize the internal data with the given units
   */
  public SkillStats(List<Unit> units) {
    for (Unit unit : units) {
      addUnit(unit);
    }
  }

  // maps skillTypes to SkillStorage-Objects
  private Map<SkillType, SkillStorage> skillData = new Hashtable<SkillType, SkillStorage>();

  /**
   * returns a List containing the units with the specified skill at the specified level
   */
  public List<Unit> getUnits(Skill skill) {
    SkillStorage skillStorage = skillData.get(skill.getSkillType());

    if (skillStorage == null)
      return Collections.emptyList();
    else {
      Map<Integer, UnitVector> levelTable = skillStorage.levelTable;
      UnitVector uv = levelTable.get(new Integer(skill.getLevel()));

      if (uv == null)
        return Collections.emptyList();
      else
        return uv.units;
    }
  }

  /**
   * returns the number of persons that master the specified skill at exact that level, specified in
   * the skill Object. That means, a call with a skill-Object containing let's say skilllevel 5,
   * will not consider persons, that master this skill at a higher level
   */
  public int getPersonNumber(Skill skill) {
    SkillStorage skillStorage = skillData.get(skill.getSkillType());

    if (skillStorage == null)
      return 0;
    else {
      Map<Integer, UnitVector> levelTable = skillStorage.levelTable;
      UnitVector uv = levelTable.get(new Integer(skill.getLevel()));

      if (uv == null)
        return 0;
      else
        return uv.personCounter;
    }
  }

  /**
   * returns the total number of days learned yet the specified SkillType
   */
  public int getSkillPointsNumber(SkillType skillType) {
    SkillStorage skillStorage = skillData.get(skillType);

    if (skillStorage == null)
      return 0;
    else
      return skillStorage.skillPointCounter;
  }

  /**
   * just like getSkillPointsNumber(SkillType) but limited to a single level
   */
  public int getSkillPointsNumber(Skill skill) {
    int retVal = 0;

    for (Unit u : getUnits(skill)) {
      retVal += u.getSkill(skill.getSkillType()).getPoints();
    }

    return retVal;
  }

  /**
   * returns the total number of skilllevel of the specified SkillType
   */
  public int getSkillLevelNumber(SkillType skillType) {
    SkillStorage skillStorage = skillData.get(skillType);

    if (skillStorage == null)
      return 0;
    else
      return skillStorage.skillLevelCounter;
  }

  /**
   * just like getSkillLevelNumber(SkillType) but limited to a level
   */
  public int getSkillLevelNumber(Skill skill) {
    int retVal = 0;

    for (Unit u : getUnits(skill)) {
      retVal += (u.getPersons() * skill.getLevel());
    }

    return retVal;
  }

  /**
   * returns the total number of persons that master the specified SkillType at any level (also
   * level 0, as long as they have more than zero skillpoints of this skillType)
   */
  public int getPersonNumber(SkillType skillType) {
    SkillStorage skillStorage = skillData.get(skillType);

    if (skillStorage == null)
      return 0;
    else
      return skillStorage.personCounter;
  }

  /**
   * returns a sorted Collection containing the existing entries (type Skill) for the specified
   * SkillType in the internal data. If type == null returns a Collection containing all existing
   * entries (for all skilltypes)
   */
  public List<Skill> getKnownSkills(SkillType type) {
    if (type == null) {
      List<Skill> v = new LinkedList<Skill>();

      for (Iterator<SkillType> iter = skillData.keySet().iterator(); iter.hasNext();) {
        type = iter.next();

        SkillStorage skillStorage = skillData.get(type);

        for (Integer level : skillStorage.levelTable.keySet()) {
          v.add(new Skill(type, 1, level.intValue(), 1, false));
        }
      }

      Collections.sort(v, new SkillComparator());

      return v;
    } else {
      SkillStorage skillStorage = skillData.get(type);

      if (skillStorage == null)
        return Collections.emptyList();
      else {
        Map<Integer, UnitVector> levelTable = skillStorage.levelTable;
        List<Skill> v = new LinkedList<Skill>();

        for (Integer integer : levelTable.keySet()) {
          int level = (integer).intValue();
          v.add(new Skill(type, Skill.getPointsAtLevel(level), level, 1, false));
        }

        Collections.sort(v, new SkillComparator());

        return v;
      }
    }
  }

  /**
   * returns an Collection containing the known SkillTypes in the internal data.
   */
  public List<SkillType> getKnownSkillTypes() {
    List<SkillType> v = new LinkedList<SkillType>();

    for (SkillType type : skillData.keySet()) {
      if (!v.contains(type)) {
        v.add(type);
      }
    }

    Collections.sort(v);

    return v;
  }

  /**
   * returns the lowest level of the specified skillType known in the internal data
   */
  public int getLowestKnownSkillLevel(SkillType type) {
    SkillStorage skillStorage = skillData.get(type);

    if (skillStorage == null)
      return 0;
    else {
      Map<Integer, UnitVector> levelTable = skillStorage.levelTable;
      int retVal = Integer.MAX_VALUE;

      for (Integer integer : levelTable.keySet()) {
        int i = (integer).intValue();

        if (i < retVal) {
          retVal = i;
        }
      }

      return retVal;
    }
  }

  /**
   * returns the highest level of the specified skillType known in the internal data
   */
  public int getHighestKnownSkillLevel(SkillType type) {
    SkillStorage skillStorage = skillData.get(type);

    if (skillStorage == null)
      return 0;
    else {
      Map<Integer, UnitVector> levelTable = skillStorage.levelTable;
      int retVal = Integer.MIN_VALUE;

      for (Integer integer : levelTable.keySet()) {
        int i = (integer).intValue();

        if (i > retVal) {
          retVal = i;
        }
      }

      return retVal;
    }
  }

  /**
   * adds a unit to the internal statistics
   */
  public void addUnit(Unit u) {
    for (Skill skill : u.getSkills()) {
      SkillStorage skillStorage = skillData.get(skill.getSkillType());

      if (skillStorage == null) {
        skillStorage = new SkillStorage();
        skillData.put(skill.getSkillType(), skillStorage);
      }

      Map<Integer, UnitVector> levelTable = skillStorage.levelTable;
      UnitVector uv = levelTable.get(new Integer(skill.getLevel()));

      if (uv == null) {
        uv = new UnitVector();
        levelTable.put(new Integer(skill.getLevel()), uv);
      }

      uv.units.add(u);
      uv.personCounter += u.getPersons();
      skillStorage.personCounter += u.getPersons();
      skillStorage.skillPointCounter += u.getSkill(skill.getSkillType()).getPoints();
      skillStorage.skillLevelCounter +=
          (u.getSkill(skill.getSkillType()).getLevel() * u.getPersons());
    }
  }

  // inner helper classes
  private class UnitVector {
    int personCounter = 0;
    List<Unit> units = new LinkedList<Unit>();
  }

  private class SkillStorage {
    // maps level (Integerobjects) to UnitVector-Objects
    Map<Integer, UnitVector> levelTable = new Hashtable<Integer, UnitVector>();
    int skillPointCounter = 0;
    int personCounter = 0;
    int skillLevelCounter = 0;
  }
}
