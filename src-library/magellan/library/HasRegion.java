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
 * An interface for all objects belonging to a region.
 */
public interface HasRegion {
  /**
   * Puts this entity into a new region
   * 
   * @param region the new region, possibly <code>null</code>
   */
  public void setRegion(Region region);

  /**
   * Returns the region of this entity.
   * 
   * @return the region, possibly <code>null</code>
   */
  public Region getRegion();
}
