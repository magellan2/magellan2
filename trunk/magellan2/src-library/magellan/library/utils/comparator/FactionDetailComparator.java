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
 * @author steffen TODO DOCUMENT ME!
 */
public class FactionDetailComparator implements Comparator<Faction> {
  protected Comparator<? super Faction> sameTrustSubCmp = null;

  /**
   * Creates a new <tt>FactionTrustComparator</tt> object.
   * 
   * @param sameFactionSubComparator if two factions with the same trust level are compared, this
   *          sub-comparator is applied if it is not <tt>null</tt>.
   */
  public FactionDetailComparator(Comparator<? super Faction> sameFactionSubComparator) {
    sameTrustSubCmp = sameFactionSubComparator;
  }

  /*
   * (non-Javadoc)
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