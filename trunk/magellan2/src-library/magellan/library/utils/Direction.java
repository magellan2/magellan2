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

package magellan.library.utils;

import magellan.library.CoordinateID;
import magellan.library.Rules;
import magellan.library.StringID;

/**
 * A class that handles directions like in ships or borders. There are three direction formats and
 * the conversions between them supported: integer representation (0 = north west and clockwise up),
 * string representation (like 'NW' or 'Nordwesten') and relative coordinate representation
 * (coordinate with x = -1, y = 1).
 */
public class Direction {

  /** Code for the invalid direction that does not correspond to any direction on the map */
  public static final int DIR_INVALID = Integer.MIN_VALUE;
  /** An invalid direction that does not correspond to any direction on the map */
  public static final Direction INVALID = new Direction(DIR_INVALID, StringID
      .create("MAGELLAN_INVALID_DIRECTION"), CoordinateID.ZERO, null);

  private int dir;
  private StringID id;
  private String iconName;
  private CoordinateID coord;

  /**
   * Creates a new Direction object interpreting the specified integer as a direction according to
   * the direction constants of this class.
   * 
   * @param direction The internal direction code
   * @param id The id that identifies this direction in the {@link Rules}
   * @param coord A coordinate corresponding to the direction (i.e. the neighbor of (0,0,0) in this
   *          direction.
   * @param iconName A map icon for an arrow in this direction on the graphical map.
   */
  public Direction(int direction, StringID id, CoordinateID coord, String iconName) {
    dir = direction;
    this.id = id;
    this.iconName = iconName;
    this.coord = coord;
  }

  @Override
  public String toString() {
    return id + " (" + dir + "): " + coord;
  }

  /**
   * Returns the id that identifies this direction in the {@link Rules}
   */
  public StringID getId() {
    return id;
  }

  /**
   * Returns a map icon for an arrow in this direction on the graphical map.
   */
  public String getIcon() {
    return iconName;
  }

  /**
   * Returns the coordinate corresponding to the direction (i.e. the neighbor of (0,0,0) in this
   * direction).
   */
  public CoordinateID toCoordinate() {
    return coord;
  }

  /**
   * Returns the internal code of this direction (used in the CR, for example)
   */
  public int getDirCode() {
    return dir;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Direction)
      return id.equals(((Direction) obj).id);
    return false;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

}
