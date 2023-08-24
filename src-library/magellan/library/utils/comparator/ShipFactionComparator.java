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

import magellan.library.Faction;
import magellan.library.Ship;
import magellan.library.Unit;

/**
 * A comparator imposing an ordering on Ship objects by comparing their owner faction.
 * <p>
 * Note: this comparator imposes orderings that are inconsistent with equals.
 * </p>
 * <p>
 * In order to overcome the inconsistency with equals this comparator allows the introduction of a
 * sub-comparator which is applied in cases of equality. I.e. if the two compared ships have the
 * same type and they would be regarded as equal by this comparator, instead of 0 the result of the
 * sub-comparator's comparison is returned.
 * </p>
 */
public class ShipFactionComparator implements Comparator<Ship> {
  protected Comparator<? super Ship> subCmp = null;

  /**
   * Creates a new ShipFactionComparator object.
   * 
   * @param aSubCmp if two ships having the same owner faction are compared, this sub-comparator is
   *          applied if it is not<kbd>null</kbd>.
   */
  public ShipFactionComparator(Comparator<? super Ship> aSubCmp) {
    subCmp = aSubCmp;
  }

  /**
   * Compares its two arguments for order according to their types.
   * 
   * @return the natural ordering of <kbd>o1</kbd>'s and <kbd>o2</kbd>'s types as returned by
   *         BuildingType.compareTo(). If the types are equal and a sub-comparator was specified,
   *         the result of that sub-comparator's comparison is returned. Unlike other comparators in
   *         this package, undefined values (i.e., owners) are evaluated as <code>&lt; 0</code>.
   */
  public int compare(Ship o1, Ship o2) {
    Unit u1 = o1.getOwner();
    Unit u2 = o2.getOwner();
    Faction f1 = null;
    Faction f2 = null;

    int retVal = 0;
    if (u1 != null) {
      f1 = u1.getFaction();
    }
    if (u2 != null) {
      f2 = u2.getFaction();
    }

    if (f1 == null && f2 != null)
      return -1;
    else if (f1 != null && f2 == null)
      return 1;
    else if (f1 != null && f2 != null) {
      retVal = f1.compareTo(f2);
    } else {
      retVal = 0;
    }

    return ((retVal == 0) && (subCmp != null)) ? subCmp.compare(o1, o2) : retVal;
  }
}
