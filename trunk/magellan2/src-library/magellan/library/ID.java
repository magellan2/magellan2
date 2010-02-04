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
 * A general interface to ID objects conveying "uniqueness".
 */
public interface ID extends Comparable<Object>, Cloneable {
  /**
   * DOCUMENT ME!
   * 
   * @return a String representation of the ID formatted in a user friendly manner.
   */
  public String toString();

  /**
   * Returns a String representation of the ID formatted in a user friendly manner with a given
   * seperator. Right now only Coordinate should implement this, all others should invoke
   * toString()!
   * 
   * @return a String representation of the ID formatted in a user friendly manner.
   */
  public String toString(String delim);

  /**
   * Compares this object to the specified object. The result is true if and only if the argument is
   * not null and is an object of the same class implementing this interface and contains the same
   * unique value.
   * 
   * @param obj the reference object with which to compare.
   * @return <code>true</code> if this object is the same as the obj argument; <code>false</code>
   *         otherwise.
   */
  public boolean equals(Object obj);

  /**
   * Returns a hashcode for this ID.
   * 
   * @return a hash code value for this object.
   */
  public int hashCode();

  /**
   * Imposes a natural ordering on ID objects.
   */
  public int compareTo(Object o);

  /**
   * Returns a copy of this object.
   */
  public Object clone() throws CloneNotSupportedException;
}
