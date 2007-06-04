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
 * A template class for objects to be uniquely identifiable by other objects.
 */
public interface Identifiable extends Unique, Comparable, Cloneable {

	/**
	 * Returns the id uniquely identifying this object.
	 */
	public ID getID();

	/**
	 * Returns a copy of this object identified by a copy of the orignial's id. I.e., the following
	 * statement holds true: this.getID() != this.clone().getID()
	 *
	 * @throws CloneNotSupportedException DOCUMENT-ME
	 */
	public Object clone() throws CloneNotSupportedException;

	/**
	 * Indicates that this object is to be regarded as equal to some other object. Especially with
	 * implementing sub classes of Identifiable, equality will often be established through the
	 * equality of ids.
	 */
	public boolean equals(Object o);

	/**
	 * As we want to use the hashCode/equals contract we need to force the implementation of 
	 * hashCode.
	 *
	 * @return the hashCode of the current object
	 */
	public int hashCode();

	/**
	 * DOCUMENT-ME
	 */
	public int superHashCode() ;

	/**
	 * Imposes a natural ordering on Identifiable objects. Especially with implementing sub classes
	 * of Identifiable, such orderings will often be established by the natural order of ids.
	 */
	public int compareTo(Object o);
}
