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

import magellan.library.Group;
import magellan.library.Unit;

/**
 * A comparator imposing an ordering on Unit objects by comparing the groups they belong to.
 * <p>
 * Note: this comparator imposes orderings that are inconsistent with equals.
 * </p>
 * <p>
 * In order to overcome the inconsistency with equals this comparator allows the introduction of a
 * sub-comparator which is applied in cases of equality. I.e. if the two compared units belong to
 * the same group or they have no group set and they would be regarded as equal by this comparator,
 * instead of 0 the result of the sub-comparator's comparison is returned.
 * </p>
 */
public class UnitGroupComparator implements Comparator<Unit> {
  protected Comparator<? super Group> groupCmp = null;
  protected Comparator<? super Unit> sameGroupSubCmp = null;
  protected Comparator<? super Unit> noGroupSubCmp = null;

  /**
   * Creates a new UnitGroupComparator object.
   * 
   * @param groupComparator the comparator used to compare the units' groups.
   * @param sameGroupSubComparator if two units belonging to the same group are compared, this
   *          sub-comparator is applied if it is not <kbd>null</kbd>.
   * @param noGroupSubComparator if two units belonging to no group are compared, this
   *          sub-comparator is applied if it is not <kbd>null</kbd>.
   */
  public UnitGroupComparator(Comparator<? super Group> groupComparator,
      Comparator<? super Unit> sameGroupSubComparator, Comparator<? super Unit> noGroupSubComparator) {
    groupCmp = groupComparator;
    sameGroupSubCmp = sameGroupSubComparator;
    noGroupSubCmp = noGroupSubComparator;
  }

  /**
   * Compares its two arguments for order according to the groups they belong to.
   * 
   * @param o1 the 1st object to compare
   * @param o2 the 2nd object to compare
   * @return the difference of <kbd>o1</kbd>'s and <kbd>o2</kbd>'s group ids. If both belong to the same
   *         group and a sub-comparator was specified, the result that sub-comparator's comparison
   *         is returned. If both units do not belong to any group and a no-group sub-comparator was
   *         specified, the result of that sub-comparator's comparison is returned. Undefined values
   *         are evaluated as <code>&gt; 0</code>.
   */
  public int compare(Unit o1, Unit o2) {
    Group g1 = o1.getGroup();
    Group g2 = o2.getGroup();

    if (g1 == null) {
      if (g2 == null)
        return (noGroupSubCmp != null) ? noGroupSubCmp.compare(o1, o2) : 0;
      else
        // g2 != null
        return 1;
    } else {
      if (g2 == null)
        return -1;
      else {
        int retVal = groupCmp.compare(g1, g2);

        return ((retVal == 0) && (sameGroupSubCmp != null)) ? sameGroupSubCmp.compare(o1, o2)
            : retVal;
      }
    }
  }
}
