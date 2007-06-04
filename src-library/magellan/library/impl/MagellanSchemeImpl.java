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

import magellan.library.ID;
import magellan.library.Scheme;

/**
 * A class encapsulating a scheme object indicating the position of a region in the 'Astralraum'
 * relative to the standard Eressea map.
 */
public class MagellanSchemeImpl extends MagellanNamedImpl implements Scheme {
	/**
	 * Create a new Scheme object with the specified unique ID.
	 *
	 * 
	 */
	public MagellanSchemeImpl(ID id) {
		super(id);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String toString() {
		return getName() + " (" + this.id.toString() + ")";
	}
}
