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

package magellan.library.impl;

import magellan.library.HotSpot;
import magellan.library.ID;

/**
 * A class encapsulating a hot spot, which represents a region of interest on the map.
 */
public class MagellanHotSpotImpl extends MagellanNamedImpl implements HotSpot {
	private ID center = null;

	/**
	 * Create a new HotSpot object with the specified unique id.
	 *
	 * 
	 */
	public MagellanHotSpotImpl(ID id) {
		super(id);
	}

	/**
	 * Returns the ID in the center of the region of interest this HotSpot points to.
	 *
	 * 
	 */
	public ID getCenter() {
		return center;
	}

	/**
	 * Set the ID the is at the center of the region of interest this HotSpot object should point
	 * to.
	 *
	 * 
	 */
	public void setCenter(ID center) {
		this.center = center;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String toString() {
		return getName();
	}
}
