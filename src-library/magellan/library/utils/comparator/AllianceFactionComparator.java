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

import magellan.library.Alliance;
import magellan.library.Faction;

/**
 * A comparator imposing an ordering on <kbd>Alliance</kbd> objects by comparing the factions they
 * contain.
 */
public class AllianceFactionComparator implements Comparator<Alliance> {
  protected Comparator<? super Faction> factionSubCmp = null;

  /**
   * Creates a new <kbd>AllianceFactionComparator</kbd> object.
   * 
   * @param factionSubComparator is used to compare the factions of two alliance objects.
   */
  public AllianceFactionComparator(Comparator<? super Faction> factionSubComparator) {
    factionSubCmp = factionSubComparator;
  }

  /**
   * Compares its two arguments for order with regard to their trust levels.
   * 
   * @return the result of the faction comparator applied to the factions of the alliances o1 and
   *         o2. Undefined values are evaluated as <code>&gt; 0</code>.
   */
  public int compare(Alliance a1, Alliance a2) {
    if (a1 == null)
      return (a2 == null) ? 0 : 1;
    else
      return (a2 == null) ? (-1) : factionSubCmp.compare(a1.getFaction(), a2.getFaction());
  }
}
