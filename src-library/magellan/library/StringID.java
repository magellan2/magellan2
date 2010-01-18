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

import magellan.library.utils.StringFactory;
import magellan.library.utils.Umlaut;


/**
 * An implementation of the ID interface providing uniqueness and identifiability through strings.
 * The strings used to establish the uniqueness of this id may differ from the strings specified
 * with the constructor of a StringID object. I.e. although with String s1 and s2 s1.equals(s2) is
 * false (new StringID(s1)).equals(new StringID(s2)) may be true. Two StringID objects are
 * regarded as equal when s1 differs from s2 only in case or the umlaut expansion of s1 equals s2
 * (or vice versa).
 */
public class StringID implements ID {
	/** The string used to establish the uniqueness of this ID. */
	protected final String id;

	/** The string specified at construction time of this object. */
	protected final String originalString;

	/**
	 * Creates a new StringID object. See the class description on how the specified string is used
	 * to establish the uniqueness of this id.
	 *
	 * 
	 *
	 * @throws IllegalArgumentException DOCUMENT-ME
	 */
	protected StringID(String i) {
		if((i == null) || i.equals("")) {
			throw new IllegalArgumentException("StringID: empty String specified as an ID");
		}

		this.originalString = StringFactory.getFactory().intern(i);
		this.id = StringFactory.getFactory().intern(Umlaut.normalize(i));
	}

	/** a static cache to use this class as flyweight factory */
	private static Map<String,StringID> idMap = new HashMap<String, StringID>();

	/**
	 * Returns a (possibly) new StringID object.
	 *
	 * 
	 *
	 * 
	 *
	 * @throws NullPointerException DOCUMENT-ME
	 */
	public static StringID create(String o) {
		if(o == null) {
			throw new NullPointerException();
		}
		
		StringID id = StringID.idMap.get(o);

		if(id == null) {
			id = new StringID(o);
			StringID.idMap.put(o, id);
		}

		return id;
	}

	/**
	 * Returns the string used to construct this StringID object.
	 *
	 * 
	 */
	@Override
  public String toString() {
		return originalString;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public String toString(String delim) {
		return toString();
	}

	/**
	 * Indicates whether this id is "equal to" some other object. For equality rules see the class
	 * description, of course o must be an instance of StringID.
	 *
	 * 
	 *
	 * 
	 */
	@Override
  public boolean equals(Object o) {
		try {
			return (this == o) || id.equals(((StringID) o).id);
		} catch(ClassCastException e) {
			return false;
		}
	}

	/**
	 * Returns a hash code based on the identifying string.
	 *
	 * 
	 */
	@Override
  public int hashCode() {
		return id.hashCode();
	}

	/**
	 * Performs a lexicographical comparision between this object and another instance of class
	 * StringID.
	 *
	 * 
	 *
	 * 
	 */
	public int compareTo(Object o) {
		return id.compareTo(((StringID) o).id);
	}

	/**
	 * Returns a copy of this string id object.
	 *
	 * 
	 *
	 * @throws CloneNotSupportedException DOCUMENT-ME
	 */
	@Override
  public StringID clone() throws CloneNotSupportedException {
		// pavkovic 2003.07.08: we dont really clone this object as StringID are immutable after creation
		return this;
	}
}
