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

/*
 * UnitSkillCountReplacer.java
 *
 * Created on 30. Dezember 2001, 17:23
 */
package magellan.library.utils.replacers;

import java.util.Collection;
import java.util.Iterator;

import magellan.library.Region;
import magellan.library.Skill;
import magellan.library.Unit;
import magellan.library.rules.SkillType;
import magellan.library.utils.Resources;

/**
 * Counts skill points or persons with a skill.
 * 
 * @author Andreas
 * @version 1.0
 */
public class UnitSkillCountReplacer extends AbstractParameterReplacer implements
    EnvironmentDependent {
  /** count all units with the skill. */
  public static final int MODE_SKILL = 0;

  /** count all units with at least a certain skill level */
  public static final int MODE_SKILL_MIN = 1;

  /** count all skill points of persons with the skill */
  public static final int MODE_SKILL_SUM = 2;

  /** count all skill points of persons with at least a certain skill level. */
  public static final int MODE_SKILL_SUM_MIN = 3;
  protected int mode;
  private static final int MODE_LENGTHS[] = { 1, 2, 1, 2 };
  protected ReplacerEnvironment environment;

  /**
   * Creates new UnitSkillCountReplacer
   * 
   * @param mode {@link #MODE_SKILL} : counts all persons with the skill<br />
   *          {@link #MODE_SKILL_MIN} : count all persons with at least a certain skill level<br />
   *          {@link #MODE_SKILL_SUM}: count the skill points of units with the given skill.<br />
   *          {@link #MODE_SKILL_SUM_MIN}: count all skill points of units with at least a certain
   *          skill level.
   */
  public UnitSkillCountReplacer(int mode) {
    super(UnitSkillCountReplacer.MODE_LENGTHS[mode]);
    this.mode = mode;
  }

  /**
   * Counts all persons in the region. Can be restricted by unit filters. Can be restricted by unit
   * filters. The first parameter specifies the skill. If <code>mode=={@link #MODE_SKILL_MIN}</code>
   * or <code>mode=={@link #MODE_SKILL_SUM_MIN}</code>, only persons with a certain skill level are
   * counted.
   * 
   * @see magellan.library.utils.replacers.Replacer#getReplacement(java.lang.Object)
   */
  public Object getReplacement(Object region) {
    if (!(region instanceof Region))
      return null;

    boolean minMode =
        ((mode == UnitSkillCountReplacer.MODE_SKILL_MIN) || (mode == UnitSkillCountReplacer.MODE_SKILL_SUM_MIN));
    boolean sumMode =
        ((mode == UnitSkillCountReplacer.MODE_SKILL_SUM) || (mode == UnitSkillCountReplacer.MODE_SKILL_SUM_MIN));
    int min = 1;
    String skill = getParameter(0, region).toString();

    if (minMode) {
      Object obj = getParameter(1, region);

      if (obj instanceof Number) {
        min = ((Number) obj).intValue();
      } else {
        try {
          min = (int) Double.parseDouble(obj.toString());
        } catch (NumberFormatException nfe) {
          // default to 1 on error
        }
      }
    }

    Collection<Unit> units =
        ((UnitSelection) environment.getPart(ReplacerEnvironment.UNITSELECTION_PART))
            .getUnits((Region) region);

    if ((units == null) || (units.size() == 0))
      return Integer.valueOf(0);

    int count = 0;
    Iterator<Unit> it = units.iterator();

    while (it.hasNext()) {
      Unit u = it.next();
      Iterator<Skill> it2 = u.getSkills().iterator();

      while (it2.hasNext()) {
        Skill sk = it2.next();
        SkillType sty = sk.getSkillType();

        if (sty.getName().equals(skill) || sty.getID().toString().equals(skill)) {
          if (!minMode || (sk.getLevel() >= min)) {
            if (sumMode) {
              count += (u.getPersons() * sk.getLevel());
            } else {
              count += u.getPersons();
            }
          }

          break;
        }
      }
    }

    return Integer.valueOf(count);
  }

  /**
   * @see magellan.library.utils.replacers.EnvironmentDependent#setEnvironment(magellan.library.utils.replacers.ReplacerEnvironment)
   */
  public void setEnvironment(ReplacerEnvironment env) {
    environment = env;
  }

  /**
   * @see magellan.library.utils.replacers.Replacer#getDescription()
   */
  @Override
  public String getDescription() {
    return Resources.get("util.replacers.unitskillcountreplacer.description." + mode) + "\n\n"
        + super.getDescription();
  }

}
