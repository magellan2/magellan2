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
import magellan.library.Named;

/**
 * A class representing a uniquely identifiable object with a modifiable name.
 */
public abstract class MagellanNamedImpl extends MagellanIdentifiableImpl implements Named {
	private String name = null;
	
	/**
	 * Constructs a new named object that is uniquely identifiable by the specified id.
	 *
	 * 
	 */
	public MagellanNamedImpl(ID id) {
		super(id);
	}

	/**
	 * Sets the name of this object.
	 *
	 * 
	 */
	public void setName(String name) {
		this.name = name;
	}

    /** 
     * @see magellan.library.Named#getName()
     */
    public String getName() {
        return name;
    }
	/**
	 * Returns the (possibly) modified name of this object.
	 *
	 * 
	 */
	public String getModifiedName() {
		return this.name;
	}

	/**
	 * Returns a String representation of this object.
	 *
	 * 
	 */
	@Override
  public String toString() {
		return this.name;
	}

	/**
	 * Returns a copy of this named object.
	 *
	 * 
	 *
	 * @throws CloneNotSupportedException DOCUMENT-ME
	 */
	@Override
  public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
