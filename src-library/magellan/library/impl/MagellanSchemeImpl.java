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

import java.util.ArrayList;
import java.util.List;

import magellan.library.CoordinateID;
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
	@Override
  public String toString() {
		return getName() + " (" + this.id.toString() + ")";
	}
	
	/**
   * Returns the coordinate of this region. This method is only a type-safe short cut for
   * retrieving and converting the ID object of this region.
   */
  public CoordinateID getCoordinate() {
          return (CoordinateID) this.getID();
  }

  /**
   * @see magellan.library.Addeable#addAttribute(java.lang.String, java.lang.String)
   */
  public void addAttribute(String key, String value) {
    throw new RuntimeException("this method is not implemented");
  }

  /**
   * @see magellan.library.Addeable#containsAttribute(java.lang.String)
   */
  public boolean containsAttribute(String key) {
    return false;
  }

  /**
   * @see magellan.library.Addeable#getAttribute(java.lang.String)
   */
  public String getAttribute(String key) {
    throw new RuntimeException("this method is not implemented");
  }

  /**
   * @see magellan.library.Addeable#getAttributeKeys()
   */
  public List<String> getAttributeKeys() {
    return new ArrayList<String>();
  }

  /**
   * @see magellan.library.Addeable#getAttributeSize()
   */
  public int getAttributeSize() {
    return 0;
  }
}
