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
public class UnitFactionTLFilter extends UnitFilter {
  protected int minTL;
  protected int maxTL;

  /**
   * Creates a new UnitFactionTLFilter object.
   */
  public UnitFactionTLFilter(int minTL, int maxTL) {
    this.minTL = minTL;
    this.maxTL = maxTL;
  }

  /**
   * Accepts units with trustlevel between (or including) specified min an max.
   * 
   * @see magellan.library.utils.filters.UnitFilter#acceptUnit(magellan.library.Unit)
   */
  @Override
  public boolean acceptUnit(Unit u) {
    Faction f = u.getFaction();

    return (f != null) && (minTL <= f.getTrustLevel()) && (f.getTrustLevel() <= maxTL);
  }

  /**
   * @return minimum trustlevel
   */
  public int getMinTL() {
    return minTL;
  }

  /**
   * @return maximum trustlevel
   */
  public int getMaxTL() {
    return maxTL;
  }

  /**
   * Set minimal accepted trustlevel.
   */
  public void setMinTL(int minTL) {
    this.minTL = minTL;
  }

  /**
   * Set maximal accepted trustlevel.
   */
  public void setMaxTL(int maxTL) {
    this.maxTL = maxTL;
  }
}
