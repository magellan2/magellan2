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

package magellan.library.utils.filters;

import magellan.library.Faction;
import magellan.library.Unit;

/**
 * DOCUMENT ME!
 * 
 * @author Andreas
 * @version 1.0
 */
public class UnitFactionFilter extends UnitFilter {
  protected String factionS;
  protected Faction faction;

  /**
   * Creates a new UnitFactionFilter object.
   */
  public UnitFactionFilter(String faction) {
    factionS = faction;
  }

  /**
   * Creates a new UnitFactionFilter object.
   */
  public UnitFactionFilter(Faction faction) {
    this.faction = faction;
  }

  /**
   * DOCUMENT-ME
   */
  @Override
  public boolean acceptUnit(Unit u) {
    Faction f = u.getFaction();

    return (f != null)
        && (((factionS != null) && (factionS.equalsIgnoreCase(f.getName()) || factionS
            .equalsIgnoreCase(f.getID().toString()))) || ((faction != null) && (faction.equals(f))));
  }

  /**
   * DOCUMENT-ME
   */
  public void setFaction(String faction) {
    factionS = faction;
  }

  /**
   * DOCUMENT-ME
   */
  public void setFaction(Faction faction) {
    this.faction = faction;
  }
}
