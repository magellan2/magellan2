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

package magellan.library.rules;

import magellan.library.ID;

/**
 * This class represents a Magellan Date.
 *
 * @author Sebastian
 */
public abstract class Date extends Object implements ID {
	protected int iDate = 0;

	/** Short format */
	public static final int TYPE_SHORT = 0;

	/** Long format */
	public static final int TYPE_LONG = 1;

	/** DOCUMENT-ME */
	public static final int TYPE_PHRASE = 2;

	/** DOCUMENT-ME */
	public static final int TYPE_PHRASE_AND_SEASON = 3;
	
  /** a constant representing the season "unknown" */
  public static final int UNKNOWN = 0;
  /** a constant representing the season spring */
  public static final int SPRING = 1;
  /** a constant representing the season summer */
	public static final int SUMMER = 2;
  /** a constant representing the season autumn */
	public static final int AUTUMN = 3;
  /** a constant representing the season winter */
	public static final int WINTER = 4;

	/**
	 * Creates new Date
	 *
	 * 
	 */
	public Date(int iInitDate) {
		iDate = iInitDate;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getDate() {
		return iDate;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setDate(int newDate) {
		iDate = newDate;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * @param o the date to compare this date to
	 *
	 * @return <tt>true</tt> if this date equals o, <tt> false </tt> otherwise
	 */
	public boolean equals(Object o) {
		try {
			return (this == o) || iDate == ((Date) o).iDate;
		} catch(ClassCastException e) {
			return false;
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int hashCode() {
		return iDate;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String toString() {
		return toString(TYPE_SHORT);
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
	 * Imposes a natural ordering on date objects based on the numeric ordering of the integer date
	 * value.
	 *
	 * @param o the date to compare this date to
	 *
	 * @return &gt; 0 if this date is greater than <tt>o</tt><br/>, 
	 *         &lt; 0 if this date is smaller, 
	 *         0 if the dates are equal
	 */
	public int compareTo(Object o) {
		Date d = (Date) o;

		return (iDate > d.iDate) ? 1 : ((iDate == d.iDate) ? 0 : (-1));
	}

	/**
	 * Creates a copy of this Date object.
	 *
	 * 
	 *
	 * @throws CloneNotSupportedException DOCUMENT-ME
	 */
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	/**
	 * Returns the season of this date.
	 * 
	 * @return {@link Date.SPRING}, {@link Date.SUMMER}, {@link Date.AUTUMN}, {@link Date.WINTER} or  {@link Date.UNKNOWN}
	 */
	public abstract int getSeason();

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public abstract String toString(int iDateType);
}
