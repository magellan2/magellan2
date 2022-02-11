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
import magellan.library.HotSpot;
import magellan.library.IntegerID;

/**
 * A class encapsulating a hot spot (now calld bookmark), which represents a region of interest on the map.
 */
public class MagellanHotSpotImpl extends MagellanNamedImpl implements HotSpot {
  private CoordinateID center = null;

  /**
   * Create a new HotSpot object with the specified unique id.
   */
  public MagellanHotSpotImpl(IntegerID id) {
    super(id);
  }

  /**
   * Returns the ID in the center of the region of interest this HotSpot points to.
   */
  public CoordinateID getCenter() {
    return center;
  }

  /**
   * Set the ID the is at the center of the region of interest this HotSpot object should point to.
   */
  public void setCenter(CoordinateID center) {
    this.center = center;
  }

  /**
   * DOCUMENT-ME
   */
  @Override
  public String toString() {
    return getName();
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

  /**
   * @see magellan.library.Identifiable#getID()
   */
  @Override
  public IntegerID getID() {
    return (IntegerID) super.getID();
  }
}
