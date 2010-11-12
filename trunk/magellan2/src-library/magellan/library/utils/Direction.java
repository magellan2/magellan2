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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import magellan.library.CoordinateID;
import magellan.library.Region;
import magellan.library.gamebinding.EresseaConstants;

/**
 * A class providing convience functions for handling directions like in ships or borders. There are
 * three direction formats and the coversions between them supported: integer representation (0 =
 * north west and clockwise up), string representation (like 'NW' or 'Nordwesten') and relative
 * coordinate representation (coordinate with x = -1, y = 1).
 */
public enum Direction {

  /**
   * north-west direction
   */
  NW(0), /**
   * north-east direction
   */
  NE(1), /**
   * east direction
   */
  E(2), /**
   * south-east direction
   */
  SE(3), /**
   * south-west direction
   */
  SW(4), /**
   * west direction
   */
  W(5), /**
   * Invalid/unknown direction
   */
  INVALID(-1);

  /** Invalid/unknown direction */
  public static final int DIR_INVALID = -1;

  /** north west direction */
  public static final int DIR_NW = 0;

  /** north east direction */
  public static final int DIR_NE = 1;

  /** east direction */
  public static final int DIR_E = 2;

  /** south east direction */
  public static final int DIR_SE = 3;

  /** south west direction */
  public static final int DIR_SW = 4;

  /** west direction */
  public static final int DIR_W = 5;

  protected static final int SHORT = 0;
  protected static final int LONG = 1;
  protected static final int NORMLONG = 2;

  private static ArrayList<Direction> tempDirections = new ArrayList<Direction>(7);
  static {
    tempDirections.add(NW);
    tempDirections.add(NE);
    tempDirections.add(E);
    tempDirections.add(SE);
    tempDirections.add(SW);
    tempDirections.add(W);
  }

  private static final List<Direction> directions = Collections.unmodifiableList(tempDirections);
  static {
    tempDirections = null;
  }

  private static String[] shortNames = new String[6];
  private static String[] longNames = new String[6];
  private static String[] normalizedLongNames = new String[6];

  private static Locale usedLocale = null;

  private int dir;

  /**
   * Creates a new Direction object interpreting the specified integer as a direction according to
   * the direction constants of this class.
   */
  private Direction(int direction) {
    if ((direction > -1) && (direction < 6)) {
      dir = direction;
    } else {
      dir = DIR_INVALID;
    }
  }

  /**
   * Creates a new Direction object interpreting the specified coordinate as a direction.
   * 
   * @param c a relative coordinate, e.g. (1, 0) for DIR_E. If c is null an IllegalArgumentException
   *          is thrown.
   * @throws NullPointerException if the c param is null.
   * @deprecated
   */
  @Deprecated
  private Direction(CoordinateID c) {
    if (c != null) {
      dir = Direction.toInt(c);
    } else
      throw new NullPointerException();
  }

  /**
   * Creates a new Direction object interpreting the specified String as a direction.
   * 
   * @param str a german name for a direction, e.g. "Osten" for DIR_E. If str is null an
   *          IllegalArgumentException is thrown.
   * @throws NullPointerException if the str param is null.
   * @deprecated
   */
  @Deprecated
  private Direction(String str) {
    if (str != null) {
      dir = Direction.toInt(str);
    } else
      throw new NullPointerException();
  }

  /**
   * Returns the actual direction of this object. Please prefer using the singletons
   * {@link #INVALID}, {@link #NE}, {@link #E}, {@link #SE}, {@link #SW}, {@link #W}, {@link #NW}.
   */
  public int getDir() {
    return dir;
  }

  /**
   * Returns a direction as distance from from to to. If they are not in the same z-layer,
   * {@link #INVALID} is returned.
   */
  public static Direction toDirection(CoordinateID from, CoordinateID to) {
    if (to.getZ() != from.getZ())
      return Direction.INVALID;
    return toDirection(to.getX() - from.getX(), to.getY() - from.getY());
  }

  /**
   * Returns a direction as distance from from to to regarding wraparound effects. If they are not
   * in the same z-layer, {@link #INVALID} is returned.
   */
  public static Direction toDirection(Region from, Region to) {
    Map<Direction, Region> neighbors = from.getNeighbors();
    for (Direction d : neighbors.keySet()) {
      if (neighbors.get(d) == to)
        return d;
    }
    return Direction.INVALID;
  }

  /**
   * Converts a relative coordinate to a direction.
   */
  public static Direction toDirection(CoordinateID c) {
    return toDirection(c.getX(), c.getY());
  }

  /**
   * Converts to coordinates to a direction.
   * 
   * @see #toDirection(CoordinateID)
   */
  public static Direction toDirection(int x, int y) {
    if (x == -1) {
      if (y == 0)
        return W;
      else if (y == 1)
        return NW;
    } else if (x == 0) {
      if (y == -1)
        return SW;
      else if (y == 1)
        return NE;
    } else if (x == 1) {
      if (y == -1)
        return SE;
      else if (y == 0)
        return E;
    }

    return INVALID;
  }

  /**
   * Converts a string (in the default order locale) to a direction.
   */
  public static Direction toDirection(String str) {
    return toDirection(str, null);
  }

  /**
   * Converts a string (in the specified locale) to a direction.
   */
  public static Direction toDirection(String str, Locale locale) {
    int dir = Direction.DIR_INVALID;
    String s = Umlaut.normalize(str).toLowerCase();

    initNames(locale);
    dir = Direction.find(s, SHORT);

    if (dir == Direction.DIR_INVALID) {
      dir = Direction.find(s, NORMLONG);
    }

    return toDirection(dir);
  }

  /**
   * Converts one of the <code>DIR_</code>constants to a direction.
   */
  public static Direction toDirection(int dir) {
    if (dir < 0 || dir > 5)
      return INVALID;
    return directions.get(dir);
  }

  /**
   * Returns a relative coordinate representing this direction. E.g. the direction DIR_W would
   * create the coordinate (-1, 0).
   */
  public CoordinateID toCoordinate() {
    int x = 0;
    int y = 0;

    switch (getDir()) {
    case DIR_NW:
      x = -1;
      y = 1;
      break;
    case DIR_NE:
      y = 1;
      break;
    case DIR_SE:
      y = -1;
      x = 1;
      break;
    case DIR_E:
      x = 1;
      break;
    case DIR_SW:
      y = -1;
      break;
    case DIR_W:
      x = -1;
      break;
    }

    return CoordinateID.create(x, y);
  }

  /**
   * Returns a relative coordinate representing the direction dir. E.g. the direction DIR_W would
   * create the coordinate (-1, 0).
   */
  public static CoordinateID toCoordinate(Direction dir) {
    return dir.toCoordinate();
  }

  /**
   * Returns a relative coordinate representing the direction dir. E.g. the direction DIR_W would
   * create the coordinate (-1, 0).
   * 
   * @deprecated Prefer using {@link #toCoordinate(Direction)}.
   */
  @Deprecated
  public static CoordinateID toCoordinate(int dir) {
    return toCoordinate(toDirection(dir));
  }

  /**
   * Returns a String representation for this direction.
   */
  @Override
  public String toString() {
    return Direction.toString(dir, false);
  }

  /**
   * @param shortForm
   * @return f true, a short form of the direction's string representation is returned.
   */
  public String toString(boolean shortForm) {
    if (shortForm)
      return Direction.getShortDirectionString(dir);
    else
      return Direction.getLongDirectionString(dir);
  }

  /**
   * Returns a String representation of the specified direction. <b>Note:</b> Please prefer using
   * {@link #toString()}.
   */
  public static String toString(int dir) {
    return Direction.toString(dir, false);
  }

  /**
   * Returns a String representation of the specified direction. <b>Note:</b> Please prefer using
   * {@link #toString(boolean)}.
   * 
   * @param dir if true, a short form of the direction's string representation is returned.
   */
  public static String toString(int dir, boolean shortForm) {
    if (shortForm)
      return Direction.getShortDirectionString(dir);
    else
      return Direction.getLongDirectionString(dir);
  }

  /**
   * Returns a String representation of the specified direction.
   */
  public static String toString(CoordinateID c) {
    return toDirection(c).toString();
  }

  private static String getLongDirectionString(int key) {
    switch (key) {
    case DIR_NW:
      return Resources.getOrderTranslation(EresseaConstants.O_NORTHWEST);

    case DIR_NE:
      return Resources.getOrderTranslation(EresseaConstants.O_NORTHEAST);

    case DIR_E:
      return Resources.getOrderTranslation(EresseaConstants.O_EAST);

    case DIR_SE:
      return Resources.getOrderTranslation(EresseaConstants.O_SOUTHEAST);

    case DIR_SW:
      return Resources.getOrderTranslation(EresseaConstants.O_SOUTHWEST);

    case DIR_W:
      return Resources.getOrderTranslation(EresseaConstants.O_WEST);
    }

    return Resources.get("util.direction.name.long.invalid");
  }

  private static String getShortDirectionString(int key) {
    switch (key) {
    case DIR_NW:
      return Resources.getOrderTranslation(EresseaConstants.O_NW);

    case DIR_NE:
      return Resources.getOrderTranslation(EresseaConstants.O_NE);

    case DIR_E:
      return Resources.getOrderTranslation(EresseaConstants.O_E);

    case DIR_SE:
      return Resources.getOrderTranslation(EresseaConstants.O_SE);

    case DIR_SW:
      return Resources.getOrderTranslation(EresseaConstants.O_SW);

    case DIR_W:
      return Resources.getOrderTranslation(EresseaConstants.O_W);
    }

    return Resources.get("util.direction.name.short.invalid");
  }

  /**
   * Converts a relative coordinate to an integer representation of the direction.
   * 
   * @deprecated Prefer using {@link #toDirection(CoordinateID)}.
   */
  @Deprecated
  public static int toInt(CoordinateID c) {
    return toDirection(c.getX(), c.getY()).getDir();
  }

  /**
   * Converts a string to an integer representation of the direction.
   * 
   * @deprecated Prefer using {@link #toDirection(String)}.
   */
  @Deprecated
  public static int toInt(String str) {
    return toDirection(str).getDir();
  }

  /**
   * Returns the names of all valid directions in an all-lowercase short form.
   */
  public static List<String> getShortNames() {
    return getShortNames(null);
  }

  /**
   * Returns the names of all valid directions in an all-lowercase short form.
   */
  public static List<String> getShortNames(Locale locale) {
    initNames(locale);

    return Arrays.asList(Direction.shortNames);
  }

  /**
   * Returns the names of all valid directions in an all-lowercase long form.
   */
  public static List<String> getLongNames() {
    return getLongNames(null);
  }

  /**
   * Returns the names of all valid directions in an all-lowercase long form.
   */
  public static List<String> getLongNames(Locale locale) {
    initNames(locale);

    return Arrays.asList(Direction.longNames);
  }

  /**
   * Returns the names of all valid directions in an all-lowercase long form. The names are also
   * normalized (umlauts converted etc.)
   */
  public static List<String> getNormalizedLongNames() {
    return getNormalizedLongNames(null);
  }

  /**
   * Returns the names of all valid directions in an all-lowercase long form. The names are also
   * normalized (umlauts converted etc.)
   */
  public static List<String> getNormalizedLongNames(Locale locale) {
    initNames(locale);

    return Arrays.asList(Direction.normalizedLongNames);
  }

  protected static void initNames(Locale locale) {
    if (locale == null) {
      locale = Locales.getOrderLocale();
    }
    if (locale.equals(Direction.usedLocale))
      return;
    Direction.usedLocale = locale;

    for (int i = 0; i < 6; i++) {
      Direction.shortNames[i] = Direction.getShortDirectionString(i).toLowerCase();
      Direction.longNames[i] = Direction.getLongDirectionString(i).toLowerCase();
      Direction.normalizedLongNames[i] =
          Umlaut.convertUmlauts(Direction.getLongDirectionString(i)).toLowerCase();
    }

  }

  /**
   * Finds pattern in the set of matches (case-sensitively) and returns the index of the hit.
   * Pattern may be an abbreviation of any of the matches. If pattern is ambiguous or cannot be
   * found among the matches, -1 is returned
   */
  private static int find(String pattern, int mode) {

    // Map<String, Integer> hits;
    // switch (mode) {
    // case SHORT:
    // hits = shortNames.searchPrefixMap(pattern, Integer.MAX_VALUE);
    // if (hits.size() == 1)
    // return hits.values().iterator().next();
    // break;
    //
    // case LONG:
    // hits = longNames.searchPrefixMap(pattern, Integer.MAX_VALUE);
    // if (hits.size() == 1)
    // return hits.values().iterator().next();
    // break;
    // case NORMLONG:
    // hits = normalizedLongNames.searchPrefixMap(pattern, Integer.MAX_VALUE);
    // if (hits.size() == 1)
    // return hits.values().iterator().next();
    // break;
    // default:
    // throw new IllegalStateException();
    // }
    // return -1;

    int hits = 0;
    int hitIndex = -1;
    String[] strings;
    switch (mode) {
    case SHORT:
      strings = shortNames;
      break;
    case LONG:
      strings = longNames;
      break;
    case NORMLONG:
      strings = normalizedLongNames;
      break;
    default:
      throw new IllegalStateException();
    }

    for (int i = 0; i < strings.length; ++i) {
      if (strings[i].startsWith(pattern)) {
        hits++;
        hitIndex = i;
      }
    }

    if (hits == 1)
      return hitIndex;
    else
      return -1;
  }

  /**
   * Returns the difference to the specified dir constant. E.g.,
   * <code>NE.getDifference(DIR_W) == -2</code>, <code>NE.getDifference(DIR_SE) == 2</code>,
   * <code>NE.getDifference(DIR_SW) == 3</code> Differences to {@link #INVALID} are always
   * {@link Integer#MAX_VALUE}.
   */
  public int getDifference(Direction dir) {
    return getDifference(dir.dir);
  }

  /**
   * Returns the difference to the specified direction. E.g., <code>NE.getDifference(W) == -2</code>
   * , <code>NE.getDifference(SE) == 2</code>, <code>NE.getDifference(SW) == 3</code>. Differences
   * to {@link #INVALID} are always {@link Integer#MAX_VALUE}.
   */
  public int getDifference(int dir) {
    if (toDirection(dir) == INVALID || this == INVALID)
      return Integer.MAX_VALUE;
    return (this.dir - dir + 3 >= 0 ? 3 - (this.dir - dir + 3) % 6 : (dir - this.dir + 3) % 6 - 3);
  }

  /**
   * Returns a list of all valid directions.
   */
  public static List<Direction> getDirections() {
    return directions;
  }

  /**
   * Returns a direction that is delta steps further (clockwise). E.g., <code>W.add(1)==NW</code>,
   * <code>W.add(-2)==SE</code>, <code>W.add(6)==W</code>.
   */
  public Direction add(int delta) {
    if (this == INVALID)
      return INVALID;
    return Direction.toDirection(((getDir() + delta) % 6 + 12) % 6);
  }

}
