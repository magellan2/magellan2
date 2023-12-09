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

  /** Phrase format */
  public static final int TYPE_PHRASE = 2;

  /** Phrase and season format */
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
   */
  public Date(int iInitDate) {
    iDate = iInitDate;
  }

  /**
   * Returns the round.
   */
  public int getDate() {
    return iDate;
  }

  /**
   * Sets the round.
   */
  public void setDate(int newDate) {
    iDate = newDate;
  }

  /**
   * @param o the date to compare this date to
   * @return <kbd>true</kbd> if this date equals o, <kbd> false </kbd> otherwise
   */
  @Override
  public boolean equals(Object o) {
    if (o instanceof Date)
      return (this == o) || iDate == ((Date) o).iDate;
    return false;
  }

  /**
	 * 
	 */
  @Override
  public int hashCode() {
    return iDate;
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    return toString(Date.TYPE_SHORT);
  }

  /**
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
   * @return &gt; 0 if this date is greater than <kbd>o</kbd>,<br/>
   *         &lt; 0 if this date is smaller, 0 if the dates are equal
   */
  public int compareTo(Object o) {
    Date d = (Date) o;

    return (iDate > d.iDate) ? 1 : ((iDate == d.iDate) ? 0 : (-1));
  }

  /**
   * Creates a copy of this Date object.
   */
  @Override
  public Date clone() {
    try {
      return (Date) super.clone();
    } catch (CloneNotSupportedException e) {
      // won't happen
      e.printStackTrace();
      throw new AssertionError();
    }
  }

  /**
   * Returns the season of this date.
   * 
   * @return {@link #SPRING}, {@link #SUMMER}, {@link #AUTUMN}, {@link #WINTER} or {@link #UNKNOWN}
   */
  public abstract int getSeason();

  /**
	 * 
	 */
  public abstract String toString(int iDateType);
}
