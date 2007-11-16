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
 * A class establishing the uniqueness property through an integer. This class assumes the
 * representation of integers to be decimal in all cases.
 */
public class IntegerID implements ID {
	/** The Integer object this id is based on. */

	// pavkovic 2003.09.18: Changed from Integer to int to reduce memory consumption
	protected final int id;

	/**
	 * Constructs a new IntegerID object from the specified integer.
	 *
	 * 
	 */
	protected IntegerID(Integer i) {
		this(i.intValue());
	}

	/**
	 * Constructs a new IntegerID object based on an Integer object created from the specified int.
	 *
	 * 
	 */
	protected IntegerID(int i) {
		id = i;
	}

	/**
	 * Creates a new IntegerID object by parsing the specified string for a decimal integer.
	 *
	 * 
	 */
	protected IntegerID(String s) {
		this(Integer.valueOf(s));
	}

	/** a static cache to use this class as flyweight factory */
	private static Map<Integer,IntegerID> idMap = new HashMap<Integer, IntegerID>();

	/**
	 * Returns a (possibly) new StringID object.
	 *
	 * 
	 *
	 * 
	 *
	 * @throws NullPointerException DOCUMENT-ME
	 */
	public static synchronized IntegerID create(Integer o) {
		if(o == null) {
			throw new NullPointerException();
		}

		IntegerID id = idMap.get(o);

		if(id == null) {
			id = new IntegerID(o);
			idMap.put(o, id);
		}

		return id;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public static IntegerID create(String str) {
		return create(Integer.valueOf(str));
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public static IntegerID create(int i) {
		return create(new Integer(i));
	}

	/**
	 * Returns a string representation of the underlying integer.
	 *
	 * 
	 */
	public String toString() {
		return Integer.toString(id);
	}

	/**
	 * Returns a string representation of the underlying integer.
	 *
	 * 
	 *
	 * 
	 */
	public String toString(String delim) {
		return toString();
	}

	/**
	 * Returns the value of this IntegerID as an int.
	 *
	 * 
	 */
	public int intValue() {
		return id;
	}

	/**
	 * Indicates whether this IntegerID object is equal to some other object.
	 *
	 * 
	 *
	 * @return true, if o is an instance of class IntegerID and the numerical values of this and
	 * 		   the specified object are equal.
	 */
	public boolean equals(Object o) {
		try {
			return this == o || id == ((IntegerID) o).id;
		} catch(ClassCastException e) {
			return false;
		}
	}

	/**
	 * Imposes a natural ordering on IntegerID objects which is based on the natural ordering of
	 * the underlying integers.
	 *
	 * 
	 *
	 * 
	 */
	public int compareTo(Object o) {
		int anotherId = ((IntegerID) o).id;

		return (id < anotherId) ? (-1) : ((id == anotherId) ? 0 : 1);
	}

	/**
	 * Returns a hash code for this object.
	 *
	 * @return a hash code value based on the hash code returned by the underlying Integer object.
	 */
	public int hashCode() {
		return id;
	}

	/**
	 * Returns a copy of this IntegerID object.
	 *
	 * 
	 *
	 * @throws CloneNotSupportedException DOCUMENT-ME
	 */
	public Object clone() throws CloneNotSupportedException {
		// pavkovic 2003.07.08: we dont really clone this object as IntegerID is unchangeable after creation
		return this;
	}
}
