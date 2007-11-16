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

package magellan.library.rules;

import magellan.library.ID;
import magellan.library.impl.MagellanNamedImpl;

/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 203 $
 */
public abstract class ObjectType extends MagellanNamedImpl {
	/**
	 * Creates a new ObjectType object.
	 *
	 * 
	 */
	public ObjectType(ID id) {
		super(id);
	}
}
