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

import magellan.library.Unit;

/**
 * A comparator imposing an ordering on Unit objects by comparing their health status
 * <p>
 * Note: this comparator imposes orderings that are inconsistent with equals.
 * </p>
 * <p>
 * In order to overcome the inconsistency with equals this comparator allows the introduction of a
 * sub-comparator which is applied in cases of equality.
 * </p>
 * 
 * @author Ulrich Küster
 */
public class UnitHealthComparator implements Comparator<Unit> {
  protected Comparator<? super Unit> subCmp = null;

  /**
   * Creates a new UnitHealthComparator object.
   * 
   * @param subComparator if two units have the same health-status, this sub-comparator is applied
   *          if it is not <kbd>null</kbd>.
   */
  public UnitHealthComparator(Comparator<? super Unit> subComparator) {
    subCmp = subComparator;
  }

  /**
   * Compares its two arguments for order according to the health-status. Unlike other comparators
   * in this package, unknown values are evaluated as <code>&lt; 0</code>.
   */
  public int compare(Unit u1, Unit u2) {
    int retVal = 0;
    String health1 = u1.getHealth();
    String health2 = u2.getHealth();

    if (health1 == null) {
      if (health2 != null) {
        // we deviate from the common pattern of returning >0 for null values, because units with
        // full health have a null value, too.
        retVal = Integer.MIN_VALUE;
      } else
        return 0;
    } else if (health2 == null) {
      retVal = Integer.MAX_VALUE;
    } else {
      // the alphabetical sorting is not very pretty
      // this is a try to create a better order
      // (healthy, tired, wounded, heavily wounded)
      if (health1.equalsIgnoreCase("schwer verwundet")) {
        health1 = "z" + health1;
      }

      if (health2.equalsIgnoreCase("schwer verwundet")) {
        health2 = "z" + health2;
      }

      retVal = health1.compareToIgnoreCase(health2);
    }

    return ((retVal == 0) && (subCmp != null)) ? subCmp.compare(u1, u2) : retVal;
  }

}
