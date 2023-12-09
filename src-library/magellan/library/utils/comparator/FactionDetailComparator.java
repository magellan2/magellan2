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

/**
 * A comparator imposing an ordering on <kbd>Faction</kbd> objects by comparing their <i>exact</i>
 * trust levels.
 * <p>
 * Note: this comparator imposes orderings that are inconsistent with equals.
 * </p>
 * <p>
 * In order to overcome the inconsistency with equals this comparator allows the introduction of a
 * sub-comparator which is applied in cases of equality. I.e. if the two compared factions belong to
 * the same trust level and they would be regarded as equal by this comparator, instead of 0 the
 * result of the sub-comparator's comparison is returned.
 * </p>
 * 
 * @author stm
 */
public class FactionDetailComparator implements Comparator<Faction> {
  protected Comparator<? super Faction> sameTrustSubCmp = null;

  /**
   * Creates a new <kbd>FactionDetailComparator</kbd> object.
   * 
   * @param sameFactionSubComparator if two factions with the same trust level are compared, this
   *          sub-comparator is applied if it is not <kbd>null</kbd>.
   */
  public FactionDetailComparator(Comparator<? super Faction> sameFactionSubComparator) {
    sameTrustSubCmp = sameFactionSubComparator;
  }

  /**
   * Compares the exact trust levels of the two factions.
   * 
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  public int compare(Faction o1, Faction o2) {
    Faction f1 = o1;
    Faction f2 = o2;
    int t1 = f1.getTrustLevel();
    int t2 = f2.getTrustLevel();
    return (t2 == t1 && sameTrustSubCmp != null) ? sameTrustSubCmp.compare(o1, o2) : (t2 - t1);
  }

}