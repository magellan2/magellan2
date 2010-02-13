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

import java.util.HashMap;
import java.util.Map;

/**
 * A class establishing the uniqueness property through a long. This class assumes the
 * representation of integers to be decimal in all cases.
 */
public class LongID implements ID {
  /** The Long object this id is based on. */

  // pavkovic 2003.09.18: changed to primitive type to avoid memory overhead
  protected final long id;

  /**
   * Constructs a new LongID object from the specified integer.
   */
  protected LongID(Long l) {
    this(l.longValue());
  }

  /**
   * Constructs a new LongID object based on an Long object created from the specified long.
   */
  protected LongID(long l) {
    id = l;
  }

  /**
   * Creates a new LongID object by parsing the specified string for a decimal integer.
   */
  protected LongID(String s) {
    this(Long.valueOf(s));
  }

  /** a static cache to use this class as flyweight factory */
  private static Map<Long, LongID> idMap = new HashMap<Long, LongID>();

  /**
   * Returns a (possibly) new StringID object.
   * 
   * @throws NullPointerException If <code>o==null</code>
   */
  public static LongID create(Long o) {
    if (o == null)
      throw new NullPointerException();

    LongID id = LongID.idMap.get(o);

    if (id == null) {
      id = new LongID(o);
      LongID.idMap.put(o, id);
    }

    return id;
  }

  /**
   * Creates an id with the integer represented by s.
   */
  public static LongID create(String s) {
    return LongID.create(Long.valueOf(s));
  }

  /**
   * Creates an ID with the specified value.
   */
  public static LongID create(int i) {
    return LongID.create(new Long(i));
  }

  /**
   * Creates an ID with the specified value.
   */
  public static LongID create(long l) {
    return LongID.create(new Long(l));
  }

  /**
   * Returns a string representation of the underlying integer.
   */
  @Override
  public String toString() {
    return Long.toString(id);
  }

  /**
   * Returns a string representation of the underlying integer.
   */
  public String toString(String delim) {
    return toString();
  }

  /**
   * Returns the value of this LongID as an int.
   */
  public long longValue() {
    return id;
  }

  /**
   * Indicates whether this LongID object is equal to some other object.
   * 
   * @return true, if o is an instance of class LongID and the numerical values of this and the
   *         specified object are equal.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o instanceof LongID)
      return id == ((LongID) o).id;
    return false;
  }

  /**
   * Imposes a natural ordering on LongID objects which is based on the natural ordering of the
   * underlying integers.
   */
  public int compareTo(Object o) {
    long anotherId = ((LongID) o).id;

    return (id < anotherId) ? (-1) : ((id == anotherId) ? 0 : 1);
  }

  /**
   * Returns a hash code for this object.
   * 
   * @return a hash code value based on the hash code returned by the underlying Long object.
   */
  @Override
  public int hashCode() {
    return (int) (id ^ (id >>> 32));
  }

  /**
   * Returns a copy of this LongID object.
   * 
   * @throws CloneNotSupportedException DOCUMENT-ME
   */
  @Override
  public Object clone() throws CloneNotSupportedException {
    // pavkovic 2003.07.08: we dont really clone this object as LongID is unchangeable after
    // creation
    return this;
  }
}
