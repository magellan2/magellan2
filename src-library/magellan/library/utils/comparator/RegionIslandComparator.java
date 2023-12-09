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

import magellan.library.Island;
import magellan.library.Region;

/**
 * A comparator imposing an ordering on Region objects by comparing the islands they belong to.
 * <p>
 * Note: this comparator imposes orderings that are inconsistent with equals.
 * </p>
 * <p>
 * In order to overcome the inconsistency with equals this comparator allows the introduction of a
 * sub-comparator which is applied in cases of equality. I.e. if the two compared regions belong to
 * the same island and they would be regarded as equal by this comparator, instead of 0 the result
 * of the sub-comparator's comparison is returned. Similarly, the no-island sub-comparator is
 * applied when the island members of both regions are null.
 * </p>
 */
public class RegionIslandComparator implements Comparator<Region> {
  protected Comparator<? super Island> islandCmp = null;
  protected Comparator<? super Region> sameIslandSubCmp = null;
  protected Comparator<? super Region> noIslandSubCmp = null;

  /**
   * Creates a new RegionIslandComparator object.
   * 
   * @param islandComparator determines how the islands are sorted. If null is specified the islands
   *          are sorted according to their natural order.
   * @param sameIslandSubComparator if two regions belonging to the same island are compared, this
   *          sub-comparator is applied if it is not <kbd>null</kbd>.
   * @param noIslandSubComparator if the compared regions both have island members that are null,
   *          this sub-comparator is applied if it is not <kbd>null</kbd>.
   */
  public RegionIslandComparator(Comparator<? super Island> islandComparator,
      Comparator<? super Region> sameIslandSubComparator,
      Comparator<? super Region> noIslandSubComparator) {
    islandCmp = islandComparator;
    sameIslandSubCmp = sameIslandSubComparator;
    noIslandSubCmp = noIslandSubComparator;
  }

  /**
   * Compares its two arguments for order according to the islands they belong to.
   * 
   * @return the result of comparing <kbd>o1</kbd>'s and <kbd>o2</kbd>'s islands with the specified
   *         island comparator. If both belong to the same island and a same-island sub-comparator
   *         was specified, the result of that sub-comparator's comparison is returned. If one of
   *         the islands is null, it is considered larger than the other one. If both islands are
   *         null the no-island sub-comparator is applied.
   */
  public int compare(Region o1, Region o2) {
    Region r1 = o1;
    Region r2 = o2;

    if (r1.getIsland() == null) {
      if (r2.getIsland() == null)
        // r1.getIsland == null, r2.getIsland == null
        return (noIslandSubCmp != null) ? noIslandSubCmp.compare(o1, o2) : 0;
      else
        // r1.getIsland == null, r2.getIsland != null
        return 1;
    } else {
      if (r2.getIsland() == null)
        // r1.getIsland != null, r2.getIsland == null
        return -1;
      else {
        // r1.getIsland != null, r2.getIsland != null
        int retVal = 0;

        if (islandCmp != null) {
          retVal = islandCmp.compare(r1.getIsland(), r2.getIsland());
        } else {
          retVal = r1.getIsland().compareTo(r2.getIsland());
        }

        if (retVal == 0) {
          if (sameIslandSubCmp != null) {
            retVal = sameIslandSubCmp.compare(o1, o2);
          }
        }

        return retVal;
      }
    }
  }
}
