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
 * A class encapsulating a hot spot, now called bookmark, which represents a region of interest on the map.
 */
public interface HotSpot extends Named {
  /**
   * Returns the ID in the center of the region of interest this HotSpot points to.
   */
  public CoordinateID getCenter();

  /**
   * Set the ID the is at the center of the region of interest this HotSpot object should point to.
   */
  public void setCenter(CoordinateID center);

  /**
   * DOCUMENT-ME
   */
  public String toString();

  /**
   * @see magellan.library.Identifiable#getID()
   */
  public IntegerID getID();
}
