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

import java.util.StringTokenizer;

/**
 * Represents a Versionnumber.
 * This is primary used for eCheck and maybe not compatible with
 * the magellan version number, because this version number contains
 * Strings (f.e. 2.0.alpha). In this case you have to call 
 * the second constructor that doesn't convert the version number
 * into integers.
 *
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class Version implements Comparable {
	private String major = "0";
	private String minor = "0";
	private String build = "0";
  
  private boolean isNumber = false;

  /**
   * Creates a new Version object.
   *
   * @throws NumberFormatException If the given String cannot be parsed into a Version-Number
   */
  public Version(String str, String delim) throws NumberFormatException {
    this(str,delim,true);
  }

  /**
   * Creates a new Version object.
   */
  public Version(String str, String delim, boolean checkInteger) throws NumberFormatException {
    StringTokenizer st = new StringTokenizer(str, delim);
    isNumber = checkInteger;

    if(st.countTokens() == 3) {
      if (isNumber) {
        major = Integer.toString(Integer.parseInt(st.nextToken()));
        minor = Integer.toString(Integer.parseInt(st.nextToken()));
        build = Integer.toString(Integer.parseInt(st.nextToken()));
      } else {
        major = st.nextToken();
        minor = st.nextToken();
        build = st.nextToken();
      }
    } else {
      throw new NumberFormatException("Unable to parse the specified version string \"" + str + "\" with the delimiter \"" + delim + "\"");
    }
  }

	/**
	 * Returns the Major Version Number
	 */
	public String getMajor() {
		return major;
	}

	/**
   * Returns the Minor Version Number
	 */
	public String getMinor() {
		return minor;
	}

	/**
   * Returns the Build Version Number
	 */
	public String getBuild() {
		return build;
	}

	/**
   * Returns the Versionnumber in the Form Major.Minor.Build
	 */
	public String toString() {
		return toString(".");
	}

	/**
   * Returns the Versionnumber in the Form Major.Minor.Build
	 */
	public String toString(String delim) {
		return major + delim + minor + delim + build;
	}

	/**
	 * Compares this object with the specified object for order.  Returns a negative integer, zero,
	 * or a positive integer as this object is less than, equal to, or greater than the specified
	 * object.
	 */
	public int compareTo(Object o) {
		Version v = (Version) o;

		if(this.getMajor().equalsIgnoreCase(v.getMajor())) {
			if(this.getMinor().equalsIgnoreCase(v.getMinor())) {
				return this.getBuild().compareTo(v.getBuild());
			} else {
				return this.getMinor().compareTo(v.getMinor());
			}
		} else {
			return this.getMajor().compareTo(v.getMajor());
		}
	}
  
  public boolean isNewer(Version aVersion) {
    return compareTo(aVersion)>0;
  }
}
