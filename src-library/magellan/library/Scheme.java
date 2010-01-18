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

package magellan.library;


/**
 * A class encapsulating a scheme object indicating the position of a region in the 'Astralraum'
 * relative to the standard Eressea map.
 */
public interface Scheme extends Named {
  /**
   * @see Object#toString()
   */
	public String toString();
	
	/**
   * Returns the coordinate of this region. This method is only a type-safe short cut for
   * retrieving and converting the ID object of this region.
   */
  public CoordinateID getCoordinate();
	
  /**
   * @see magellan.library.Identifiable#getID()
   */
  public CoordinateID getID();
}
