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
 * A comparator sorting units according to whether they belong to a specified faction or not.
 * <p>
 * Note: this comparator imposes orderings that are inconsistent with equals
 * </p>
 * <p>
 * This class allows to introduce sub-comparators in order to create a sorting hierarchy.
 * </p>
 * <p>
 * This comparator does not impose a strict ordering on the compared units, since either two units
 * may be of the same faction or the may be of different factions, where neither of them belongs to
 * the specified faction. In these cases the units remain "unsorted".
 * </p>
 * <p>
 * In order to have these units sorted, one sub-comparator for each of the two cases above my be
 * introduced, that may or, again, may not impose a strict order on the compared elements.
 * </p>
 */
public class UnitPreferredFactionComparator implements Comparator<Unit> {
  protected Comparator<? super Unit> preferredFactionSubCmp = null;
  protected Comparator<? super Unit> otherFactionSubCmp = null;
  protected int factionID = 0;

  /**
   * Creates a new <kbd>UnitPreferredFactionComparator</kbd> object with the specified sub-comparators
   * and the specified faction id.
   * 
   * @param preferredFactionSubComparator a <kbd>Comparator</kbd> applied if both compared units are
   *          in the preferred faction.
   * @param otherFactionSubComparator a <kbd>Comparator</kbd> applied if both compared units are not
   *          in the preferred faction.
   */
  public UnitPreferredFactionComparator(Comparator<? super Unit> preferredFactionSubComparator,
      Comparator<? super Unit> otherFactionSubComparator, int fID) {
    preferredFactionSubCmp = preferredFactionSubComparator;
    otherFactionSubCmp = otherFactionSubComparator;
    factionID = fID;
  }

  /**
   * Compares two units, where unit one is regarded less than unit two if unit one belongs to the
   * preferred faction, whereas unit two does not. Cases of equality are handled by sub-comparators,
   * if specified, or returned as such.
   * 
   * @return -1 if o1 belongs to the preferred faction and o2 does not, 1 if o2 belongs to the
   *         preferred faction and o1 does not. The return value in the other cases (both or neither
   *         of them belong to the preferred faction) depends on the sub-comparators.
   */
  public int compare(Unit o1, Unit o2) {
    int retVal = 0;
    int id1 = (o1.getFaction().getID()).intValue();
    int id2 = (o2.getFaction().getID()).intValue();

    if (id1 == factionID) {
      if (id2 == factionID) {
        if (preferredFactionSubCmp != null) {
          retVal = preferredFactionSubCmp.compare(o1, o2);
        } else {
          retVal = 0;
        }
      } else {
        retVal = -1;
      }
    } else {
      if (id2 == factionID) {
        retVal = 1;
      } else {
        if (otherFactionSubCmp != null) {
          retVal = otherFactionSubCmp.compare(o1, o2);
        } else {
          retVal = 0;
        }
      }
    }

    return retVal;
  }
}
