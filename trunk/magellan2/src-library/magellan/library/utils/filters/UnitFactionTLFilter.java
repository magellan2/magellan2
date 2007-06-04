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
 * @version
 */
public class UnitFactionTLFilter extends UnitFilter {
	protected int minTL;
	protected int maxTL;

	/**
	 * Creates a new UnitFactionTLFilter object.
	 *
	 * 
	 * 
	 */
	public UnitFactionTLFilter(int minTL, int maxTL) {
		this.minTL = minTL;
		this.maxTL = maxTL;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public boolean acceptUnit(Unit u) {
		Faction f = u.getFaction();

		return (f != null) && (minTL <= f.getTrustLevel()) && (f.getTrustLevel() <= maxTL);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getMinTL() {
		return minTL;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getMaxTL() {
		return maxTL;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setMinTL(int minTL) {
		this.minTL = minTL;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setMaxTL(int maxTL) {
		this.maxTL = maxTL;
	}
}
