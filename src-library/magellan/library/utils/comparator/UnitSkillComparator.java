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

package magellan.library.utils.comparator;

import java.util.Comparator;
import java.util.Map;

import magellan.library.ID;
import magellan.library.Skill;
import magellan.library.Unit;

/**
 * A comparator imposing an ordering on Unit objects by comparing their skills.
 * <p>
 * Note: this comparator imposes orderings that are inconsistent with equals.
 * </p>
 * <p>
 * In order to overcome the inconsistency with equals this comparator allows the introduction of a
 * sub-comparator which is applied in cases of equality.
 * </p>
 */
public class UnitSkillComparator implements Comparator<Unit> {
  private final Comparator<? super Map<? extends ID, Skill>> skillsCmp;
  private final Comparator<? super Unit> subCmp;

  /**
   * Creates a new UnitSkillComparator object.
   * 
   * @param skillsComparator used to compare the skills of two units
   * @param subComparator if two units do not possess skills or if the skills comparator regards
   *          them as equal, this sub-comparator is applied if it is not <kbd>null</kbd>.
   */
  public UnitSkillComparator(Comparator<? super Map<? extends ID, Skill>> skillsComparator,
      Comparator<? super Unit> subComparator) {
    skillsCmp = skillsComparator;
    subCmp = subComparator;
  }

  /**
   * Compares its two arguments for order according to their skills. The learning days of the best
   * skill of unit one is compared to those of the second unit.
   * 
   * @return a number &lt; 0 if o1's best skill is alphabetically less than o2's best skill. If both
   *         units have the same best skill these are compared using the standard skill comparator.
   *         If these two values are the same, the subcomparator is used to compare the two units.
   *         Unknown values are evaluated as <code>&gt; 0</code>.
   */
  public int compare(Unit o1, Unit o2) {
    int retVal = 0;
    final Map<? extends ID, Skill> s1 = o1.getSkillMap();
    final Map<? extends ID, Skill> s2 = o2.getSkillMap();

    if ((s1 == null) && (s2 != null)) {
      retVal = Integer.MAX_VALUE;
    } else if ((s1 != null) && (s2 == null)) {
      retVal = Integer.MIN_VALUE;
    } else if ((s1 == null) && (s2 == null)) {
      retVal = subCmp.compare(o1, o2);
    } else {
      retVal = skillsCmp.compare(s1, s2);

      if ((retVal == 0) && (subCmp != null)) {
        retVal = subCmp.compare(o1, o2);
      }
    }

    return retVal;
  }
}
