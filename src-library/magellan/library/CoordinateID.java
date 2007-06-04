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

import java.util.StringTokenizer;

/**
 * A CoordinateID uniquely identifies a location in a three dimensional space by x-, y- and z-axis
 * components.
 */
public class CoordinateID implements ID {
	/**
	 * The x-axis part of this CoordinateID. Modifying the x, y and z values changes the hash value
	 * of this CoordinateID!
	 */
	public int x;

	/**
	 * The y-axis part of this CoordinateID. Modifying the x, y and z values changes the hash value
	 * of this CoordinateID!
	 */
	public int y;

	/**
	 * The z-axis part of this CoordinateID. Modifying the x, y and z values changes the hash value
	 * of this CoordinateID!
	 */
	public int z;

	/**
	 * Create a new CoordinateID with a z-value of 0.
	 *
	 * 
	 * 
	 */
	public CoordinateID(int x, int y) {
		this.x = x;
		this.y = y;
		this.z = 0;
	}

	/**
	 * Creates a new CoordinateID object.
	 *
	 * 
	 * 
	 * 
	 */
	public CoordinateID(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Creates a new CoordinateID object.
	 *
	 * 
	 */
	public CoordinateID(CoordinateID c) {
		this.x = c.x;
		this.y = c.y;
		this.z = c.z;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public boolean equals(Object o) {
		try {
			CoordinateID c = (CoordinateID) o;
			
			return ((c == this) || ((x == c.x) && (y == c.y) && (z == c.z)));
		} catch(ClassCastException e) {
			return false;
		}
	}

	/**
	 * Returns a String representation of this corrdinate. The x, y and z components are seperated
	 * by semicolon with a blank and the z component is ommitted if it equals 0.
	 *
	 * 
	 */
	public String toString() {
		return toString(", ", false);
	}

	/**
	 * Returns a String representation of this CoordinateID consisting of the x, y and, if not 0, z
	 * coordinates delimited by delim.
	 *
	 * 
	 *
	 * 
	 */
	public String toString(String delim) {
		return toString(delim, false);
	}

	/**
	 * Returns a String representation of this CoordinateID. The x, y and z components are seperated
	 * by the specified string and the z component is ommitted if it equals 0 and forceZ is false.
	 *
	 * @param delim the string to delimit the x, y and z components.
	 * @param forceZ if true, the z component is only included if it is not 0, else the z component
	 * 		  is always included.
	 *
	 * 
	 */
	public String toString(String delim, boolean forceZ) {
		if(!forceZ && (z == 0)) {
			return x + delim + y;
		} else {
			return x + delim + y + delim + z;
		}
	}

	/**
	 * Returns a hash code value for this CoordinateID. The value depends on the x, y and z values,
	 * so be careful when modifying these values.
	 *
	 * 
	 */
	public int hashCode() {
		return (x << 12) ^ (y << 6) ^ z;
	}

	/**
	 * Creates a new <tt>CoordinateID</tt> object from a string containing the coordinates
	 * separated by delimiters.
	 * 
	 * The string can contain two resp. three integers separated by one resp. two delimiters. For
	 * example, <code>parse("12 4"," ")</code> returns the CoordinateID (12,4,0). Leading and
	 * trailing whitespace around numbers is ignored. For instance, <code>parse("13, 4, 1",",")</code>
	 * returns the CoordinateID (13,4,1), but the result of <code>parse("14  4  5
	 * 
	 * @param coords
	 *            A string which presumably contains a coordinate description
	 * @param delim
	 *            The delimiters of the coordinates. See java.util.StringTokenizer
	 * 
	 * @return The CoordinateID as read from coord; <code>null</code> if parsing failed
	 */
	public static CoordinateID parse(String coords, String delim) {
		CoordinateID c = null;

		if(coords != null) {
			StringTokenizer st = new StringTokenizer(coords, delim);

			if(st.countTokens() == 2) {
				try {
					c = new CoordinateID(Integer.parseInt(st.nextToken().trim()),
									   Integer.parseInt(st.nextToken().trim()));
				} catch(NumberFormatException e) {
					c = null;
				}
			} else if(st.countTokens() == 3) {
				try {
					c = new CoordinateID(Integer.parseInt(st.nextToken().trim()),
									   Integer.parseInt(st.nextToken().trim()),
									   Integer.parseInt(st.nextToken().trim()));
				} catch(NumberFormatException e) {
					c = null;
				}
			}
		}

		return c;
	}

	/**
	 * Translates this CoordinateID by c.x on the x-axis and c.y on the y-axis and c.z on the z-axis.
	 * Be careful when using this method on a coordinate used as a key in a hash map: modifying
	 * the x, y and z values changes the hash value.
	 *
	 * @param c the relative CoordinateID to translate the current one by.
	 *
	 * @return this.
	 */
	public CoordinateID translate(CoordinateID c) {
		x += c.x;
		y += c.y;
		z += c.z;

		return this;
	}


	/**
	 * Creates the distance coordinate from this coordinate to the given coordinate 
	 * 
	 */
	public CoordinateID createDistanceCoordinate(CoordinateID to) {
		return new CoordinateID(to.x - this.x, to.y - this.y, to.z - this.z);
	}

	/**
	 * Defines the natural ordering of coordinates which is: Iff the z coordinates differ their
	 * difference is returend. Iff the y coordinates differ their difference is returend. Else the
	 * difference of the x coordinates is returned.
	 *
	 * 
	 *
	 * 
	 */
	public int compareTo(Object o) {
		CoordinateID c = (CoordinateID) o;

		if(!this.equals(c)) {
			if(this.z != c.z) {
				return this.z - c.z;
			} else if(this.y != c.y) {
				return (c.y - this.y);
			} else {
				return (this.x - c.x);
			}
		} else {
			return 0;
		}
	}

	/**
	 * Returns a copy of this CoordinateID object.
	 *
	 * 
	 *
	 * @throws CloneNotSupportedException DOCUMENT-ME
	 */
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
