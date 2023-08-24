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
 * A comparator imposing an ordering on Unit objects by comparing their combat status
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
public class UnitCombatStatusComparator implements Comparator<Unit> {
  protected Comparator<? super Unit> subCmp = null;

  /**
   * Creates a new UnitCombatStautsComparator object.
   * 
   * @param subComparator if two units have the same combat-status, this sub-comparator is applied
   *          if it is not <kbd>null</kbd>.
   */
  public UnitCombatStatusComparator(Comparator<? super Unit> subComparator) {
    subCmp = subComparator;
  }

  /**
   * Compares its two arguments for order according to the combat-status
   */
  public int compare(Unit o1, Unit o2) {
    int retVal = 0;
    Unit u1 = o1;
    Unit u2 = o2;
    retVal = u1.getCombatStatus() - u2.getCombatStatus();

    if ((retVal == 0) && (subCmp != null)) {
      retVal = subCmp.compare(o1, o2);
    }

    return retVal;
  }
}
