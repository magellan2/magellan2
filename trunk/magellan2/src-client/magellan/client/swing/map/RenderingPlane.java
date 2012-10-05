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

package magellan.client.swing.map;

import magellan.library.utils.logging.Logger;

/**
 * A class representing a rendering plane in the mapper. A rendering plane forms a part of the map
 * that all information of one kind is drawn on completely and independantly of other planes. Planes
 * are drawn on top of each according to their index, i.e. plane 1 is painted on top of plane 0,
 * therefore higher indexed planes should contain less graphical content than lower ones to maintain
 * visibility of the lower planes.
 */
public class RenderingPlane {
  private static final Logger log = Logger.getInstance(RenderingPlane.class);

  /** If non of the following is set then the renderer should paint all regions. */
  /**
   * Indicates that all visible (on screen) regions should be painted. If set together with the
   * following only visible selected regions etc. should be painted.
   */
  public static final int VISIBLE_REGIONS = 1;

  /** Indicates that only selected regions should be painted. */
  public static final int SELECTED_REGIONS = 2;

  /** Indicates that the active region should be painted. */
  public static final int ACTIVE_REGION = 4;

  /** Indicates that both active and selected regions should be painted. */
  public static final int ACTIVE_OR_SELECTED = 8;

  /** Indicates that regions with tags should be painted. */
  public static final int TAGGED_REGIONS = 16;

  /** Indicates that the renderer will determine the regions itself. */
  public static final int ACTIVE_OBJECT = 32;

  /** Indicates that the renderer does not depend on visible objects and should always be called. */
  public static final int ALWAYS_ONCE = 1 << 15;

  private int index = -1;
  private String name = null;
  private MapCellRenderer renderer = null;
  protected int regionTypes = RenderingPlane.VISIBLE_REGIONS;

  /**
   * Creates a new RenderingPlane object representing the plane at the specified index and with the
   * specified name.
   */
  public RenderingPlane(int index, String name) {
    this.index = index;
    this.name = name;
  }

  /**
   * Creates a new RenderingPlane object.
   */
  public RenderingPlane(int index, String name, int regionTypes) {
    this(index, name);
    this.regionTypes = regionTypes;
  }

  /**
   * Retrieve the index of this rendering plane.
   */
  public int getIndex() {
    return index;
  }

  /**
   * Returns the name of this plane.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets a new name for this rendering plane.
   */
  public void setName(String newName) {
    name = newName;
  }

  /**
   * Returns the renderer that is currently used to draw on this rendering plane.
   */
  public MapCellRenderer getRenderer() {
    return renderer;
  }

  /**
   * Sets the renderer to be used to paint this rendering plane. Since renderers have a default
   * rendering plane, a warning is issued if the default rendering plane of the specified renderer
   * differs from the index of this rendering plane.
   */
  public void setRenderer(MapCellRenderer renderer) {
    this.renderer = renderer;

    if ((renderer != null) && (renderer.getPlaneIndex() != getIndex())) {
      RenderingPlane.log.warn("RenderingPlane.setRenderer: the non-conforming renderer " + renderer
          + " (" + renderer.getPlaneIndex() + ") has been set for rendering plane " + toString()
          + " (" + getIndex() + ")!");
    }
  }

  /**
   * Returns the region types this plane is interested in.
   */
  public int getRegionTypes() {
    return regionTypes;
  }

  /**
   * Sets the region types this plane is interested in.
   */
  public void setRegionTypes(int r) {
    regionTypes = r;
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public int hashCode() {
    return getIndex();
  }

}
