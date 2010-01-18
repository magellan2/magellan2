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

import java.util.Collection;
import java.util.Map;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 305 $
 */
public interface Island extends Described,Addeable {
  /**
   * Returns a String representation of this Island object.
   */
  public String toString();

  /**
   * Returns an unmodifiable collection of all the regions in this container.
   */
  public Collection<Region> regions();

  /**
   * Retrieve a region in this container by id.
   */
  public Region getRegion(ID key);

  /**
   * Informs this container that a region was added or removed. It is mandatory
   * that this function is called every time a region is added or removed from
   * this container for keeping the objects returned by the getRegion() and
   * regions() methods consistent.
   */
  public void invalidateRegions();
  
  /**
   * Sets the Map of regions. This shall solely called by GameData.postProcess.
   */
  public void setRegions(Map<CoordinateID,Region> r);
  
  /**
   * @see magellan.library.Identifiable#getID()
   */
  public IntegerID getID();
}
