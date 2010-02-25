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

import magellan.library.Region;
import magellan.library.Unit;

/**
 * A comparator imposing an ordering on Unit objects by comparing the regions they are in.
 * <p>
 * Note: this comparator can impose orderings that are inconsistent with equals.
 * </p>
 */
public class UnitRegionComparator implements Comparator<Unit> {
  protected Comparator<? super Region> regionSubCmp = null;
  protected Comparator<? super Unit> sameRegionSubCmp = null;

  /**
   * Creates a new UnitRegionComparator object.
   * 
   * @param regionSubComparator is used to compare the regions that two units to be compared belong
   *          to. If regionSubComparators regards the regions as equal, the comparator
   *          sameRegionSubComparator is applied.
   * @param sameRegionSubComparator is applied if the regionSubComparator regards two units equal
   *          according to the regions they are in.
   */
  public UnitRegionComparator(Comparator<? super Region> regionSubComparator,
      Comparator<? super Unit> sameRegionSubComparator) {
    regionSubCmp = regionSubComparator;
    sameRegionSubCmp = sameRegionSubComparator;
  }

  /**
   * Compares its two arguments for order according to the regions they belong to.
   * 
   * @return the result of the region sub-comparator applied to the regions of the the two units o1
   *         and o2 or the result of the sameRegion sub-comparator if the afore-said comparator
   *         regards the units' regions as equal. Unknown values are evaluated as
   *         <code>&gt; 0</code>.
   */
  public int compare(Unit o1, Unit o2) {
    int retVal = 0;

    Region r1 = o1.getRegion();
    Region r2 = o2.getRegion();

    if (r1 == null) {
      if (r2 == null) {
        retVal = 0;
      } else {
        retVal = Integer.MAX_VALUE;
      }
    } else {
      if (r2 == null) {
        retVal = Integer.MIN_VALUE;
      } else {
        retVal = regionSubCmp.compare(r1, r2);
      }
    }

    if (retVal == 0) {
      retVal = sameRegionSubCmp.compare(o1, o2);
    }

    return retVal;
  }
}
