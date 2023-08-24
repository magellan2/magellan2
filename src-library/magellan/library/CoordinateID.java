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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * A CoordinateID uniquely identifies a location in a three dimensional space by x-, y- and z-axis
 * components. This is an immutable object.
 */
public final class CoordinateID implements ID {
  /**
   * Convenience implementation of a mutable coordinate triplet.
   */
  public final static class Triplet {
    /**
     * x-coordinate
     */
    public int x;
    /**
     * y-coordinate
     */
    public int y;
    /**
     * z-coordinate
     */
    public int z;

    /**
     * Creates a new triplet.
     */
    public Triplet(int x, int y, int z) {
      this.x = x;
      this.y = y;
      this.z = z;
    }

    /**
     * Returns a CoordinateID with identical coordinates.
     */
    public CoordinateID toCoordinate() {
      return create(x, y, z);
    }

    @Override
    public String toString() {
      return "(" + x + "," + y + "," + z + ")";
    }
  }

  /** denotes an invalid coordinate */
  public static final CoordinateID INVALID = new CoordinateID(Integer.MIN_VALUE, Integer.MIN_VALUE,
      Integer.MIN_VALUE);

  private static final Map<Integer, Map<Integer, CoordinateID>> lookup0 =
      new LinkedHashMap<Integer, Map<Integer, CoordinateID>>();
  private static final Map<Integer, Map<Integer, Map<Integer, CoordinateID>>> lookup =
      new LinkedHashMap<Integer, Map<Integer, Map<Integer, CoordinateID>>>();

  /**
   * The coordinate (0,0,0).
   */
  public static final CoordinateID ZERO = CoordinateID.create(0, 0);

  /**
   * The x-axis part of this CoordinateID. Modifying the x, y and z values changes the hash value of
   * this CoordinateID!
   */
  private final int x;

  /**
   * The y-axis part of this CoordinateID. Modifying the x, y and z values changes the hash value of
   * this CoordinateID!
   */
  private final int y;

  /**
   * The z-axis part of this CoordinateID. Modifying the x, y and z values changes the hash value of
   * this CoordinateID!
   */
  private final int z;

  /**
   * Create a new CoordinateID with a z-value of 0.
   */
  private CoordinateID(int x, int y) {
    this.x = x;
    this.y = y;
    z = 0;
  }

  /**
   * Creates a new CoordinateID object.
   */
  private CoordinateID(int x, int y, int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /**
   * Creates a new CoordinateID object.
   */
  private CoordinateID(CoordinateID c) {
    x = c.x;
    y = c.y;
    z = c.z;
  }

  /**
   * Returns the value of x.
   * 
   * @return Returns x.
   */
  public int getX() {
    return x;
  }

  /**
   * Returns the value of y.
   * 
   * @return Returns y.
   */
  public int getY() {
    return y;
  }

  /**
   * Returns the value of z.
   * 
   * @return Returns z.
   */
  public int getZ() {
    return z;
  }

  /**
   * Returns a new Triplet with identical coordinates.
   */
  public Triplet toTriplet() {
    return new Triplet(x, y, z);
  }

  /**
   * Two instances are equal if their x,y,z values are equal.
   */
  @Override
  public boolean equals(Object o) {
    // return this == o;
    if (o == this)
      return true;
    if (this == INVALID || o == INVALID)
      return false;
    if (o instanceof CoordinateID) {
      CoordinateID c = (CoordinateID) o;

      return (x == c.x) && (y == c.y) && (z == c.z);
    }
    return false;
  }

  /**
   * Returns a String representation of this coordinate. The x, y and z components are seperated by
   * semicolon with a blank and the z component is ommitted if it equals 0.
   */
  @Override
  public String toString() {
    return toString(", ", false);
  }

  /**
   * Returns a String representation of this CoordinateID consisting of the x, y and, if not 0, z
   * coordinates delimited by delim.
   */
  public String toString(String delim) {
    return toString(delim, false);
  }

  /**
   * Returns a String representation of this CoordinateID. The x, y and z components are seperated
   * by the specified string and the z component is ommitted if it equals 0 and forceZ is false.
   * 
   * @param delim the string to delimit the x, y and z components.
   * @param forceZ if true, the z component is only included if it is not 0, else the z component is
   *          always included.
   */
  public String toString(String delim, boolean forceZ) {
    if (this == INVALID)
      return "---";
    if (!forceZ && (z == 0))
      return x + delim + y;
    else
      return x + delim + y + delim + z;
  }

  /**
   * Returns a hash code value for this CoordinateID. The value depends on the x, y and z values, so
   * be careful when modifying these values.
   */
  @Override
  public int hashCode() {
    // return (x << 12) ^ (y << 6) ^ z;
    int hash = 42;
    hash = hash * 31 + z;
    hash = 31 * hash + y;
    hash = 31 * hash + x;
    return hash;
  }

  /**
   * Creates a new <kbd>CoordinateID</kbd> object from a string containing the coordinates separated
   * by delimiters. The string can contain two resp. three integers separated by one resp. two
   * delimiters. For example, <code>parse("12 4"," ")</code> returns the CoordinateID (12,4,0).
   * Leading and trailing whitespace around numbers is ignored. For instance,
   * <code>parse("13, 4, 1",",")</code> returns the CoordinateID (13,4,1), but the result of
   * <code>parse("14  4  5",  ",")</code> is undefined.
   * 
   * @param coords A string which presumably contains a coordinate description
   * @param delim The delimiters of the coordinates. See java.util.StringTokenizer
   * @return The CoordinateID as read from coord; <code>null</code> if parsing failed
   */
  public static CoordinateID parse(String coords, String delim) {
    CoordinateID c = null;

    if (coords != null) {
      StringTokenizer st = new StringTokenizer(coords, delim);

      if (st.countTokens() == 2) {
        try {
          c =
              create(Integer.parseInt(st.nextToken().trim()), Integer.parseInt(st.nextToken()
                  .trim()));
        } catch (NumberFormatException e) {
          c = null;
        }
      } else if (st.countTokens() == 3) {
        try {
          c =
              create(Integer.parseInt(st.nextToken().trim()), Integer.parseInt(st.nextToken()
                  .trim()), Integer.parseInt(st.nextToken().trim()));
        } catch (NumberFormatException e) {
          c = null;
        }
      }
    }

    return c;
  }

  /**
   * Create a CoordinateID with a z-value of 0.
   */
  public static CoordinateID create(int x, int y) {
    return createCoordinate(x, y, 0);
  }

  /**
   * Creates a new CoordinateID object.
   */
  public static CoordinateID create(int x, int y, int z) {
    return createCoordinate(x, y, z);
  }

  /**
   * Creates a CoordinateID identical to c.
   */
  public static CoordinateID create(CoordinateID c) {
    return c;
  }

  /**
   * A synonym for {@link #translate(CoordinateID)}.
   */
  public CoordinateID add(CoordinateID c) {
    if (this == INVALID || c == INVALID)
      return INVALID;
    return create(x + c.x, y + c.y, z + c.z);
  }

  /**
   * Return a new CoordinateID that is this one modified by c.x on the x-axis and c.y on the y-axis
   * and c.z on the z-axis.
   * 
   * @param c the relative CoordinateID to translate the current one by.
   * @return A new CoordinateID
   */
  public CoordinateID translate(CoordinateID c) {
    if (this == INVALID || c == INVALID)
      return INVALID;
    return create(x + c.x, y + c.y, z + c.z);
  }

  /**
   * Return a new CoordinateID that is this one modified by c.x on the x-axis and c.y on the y-axis
   * <em>but has the same z-coordinate</em>.
   * 
   * @param c the relative CoordinateID to translate the current one by.
   * @return A new CoordinateID
   */
  public CoordinateID translateInLayer(CoordinateID c) {
    if (this == INVALID || c == INVALID)
      return INVALID;
    return create(x + c.x, y + c.y, z);
  }

  /**
   * Returns a new CoordinateID that is this one modified by -c.x, -c.y, -c.z.
   * 
   * @param c the relative CoordinateID to subtract from this coordinate.
   * @return A new CoordinateID
   */
  public CoordinateID subtract(CoordinateID c) {
    if (this == INVALID || c == INVALID)
      return INVALID;
    return create(x - c.x, y - c.y, z - c.z);
  }

  /**
   * Subtract c from this coordinate, but only the x and y coordinates.
   * 
   * @return The result as a new CoordinateID
   */
  public CoordinateID inverseTranslateInLayer(CoordinateID c) {
    if (this == INVALID || c == INVALID)
      return INVALID;
    return create(x - c.x, y - c.y, z);
  }

  /**
   * Creates the distance coordinate from this coordinate to the given coordinate
   */
  public CoordinateID createDistanceCoordinate(CoordinateID to) {
    if (this == INVALID || to == INVALID)
      return INVALID;
    return create(to.x - x, to.y - y, to.z - z);
  }

  /**
   * Defines the natural ordering of coordinates which is: Iff the z coordinates differ their
   * difference is returned. Iff the y coordinates differ their difference is returned. Else the
   * difference of the x coordinates is returned.
   */
  public int compareTo(Object o) {
    if (this == o)
      return 0;

    CoordinateID c = (CoordinateID) o;
    // if (!equals(c)) {
    if (z != c.z)
      return z - c.z;
    else if (y != c.y)
      return (c.y - y);
    else
      return (x - c.x);
    // } else
    // return 0;
  }

  /**
   * Returns a copy of this CoordinateID object.
   */
  @Override
  public CoordinateID clone() {
    return this;
  }

  /**
   * @param x
   * @param y
   * @param z
   */
  private static CoordinateID createCoordinate(int x, int y, int z) {
    if (x > 200 || y > 200 || x < -200 || y < -200)
      return new CoordinateID(x, y, z);

    // trying to improve performance -- does it work?
    // if Coordinate is not too big, take instance from cache
    Map<Integer, Map<Integer, CoordinateID>> mz;
    if (z == 0) {
      mz = lookup0;
    } else {
      mz = lookup.get(z);
      if (mz == null) {
        mz = new LinkedHashMap<Integer, Map<Integer, CoordinateID>>();
        lookup.put(z, mz);
      }
    }
    Map<Integer, CoordinateID> my = mz.get(y);

    if (my == null) {
      my = new LinkedHashMap<Integer, CoordinateID>();
      mz.put(y, my);
    }
    CoordinateID mx = my.get(x);
    if (mx == null) {
      mx = new CoordinateID(x, y, z);
      my.put(x, mx);
    }
    if (x != mx.x || y != mx.y || z != mx.z)
      throw new IllegalStateException();
    return mx;
  }

  /**
   * Returns a dummy coordinate that is not equal to any other CoordinateID.
   */
  public static CoordinateID getInvalid() {
    return INVALID;
  }

}
