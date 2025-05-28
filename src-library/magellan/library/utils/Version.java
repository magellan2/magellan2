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

import magellan.library.utils.logging.Logger;

/**
 * Represents a version number. ECheck used numeric version numbers and Magellan has used non-standard version numbers
 * like 2.0.alpha in the past. The preferred form are now semantic version numbers (https://semver.org/).
 * 
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class Version implements Comparable<Object> {
  private static final Logger log = Logger.getInstance(Version.class);

  private String major = "0";
  private String minor = "0";
  private String revision = "0";
  private String devel = "0";

  private int iMajor;
  private int iMinor;
  private int iRevision;
  private int iBuild;

  private String identifiers = "";
  private boolean error;

  private String delim;

  /**
   * Parses a semantic version (or, more precisely, a subset of those) consisting of a major, minor, and build version,
   * optional identifier, and optional build number
   * suffix. For example, "1.2.3-rc1.5 (build 678)" has major 1, minor 2, build 3, identifiers rc1.5 and develop 678. If
   * the given string is not a valid semantic version number, {@link #isError()} will return true.
   * 
   * This constructor will try to "fix" some invalid version:
   * <ul>
   * <li>2.3.rc will be interpreted as 2.3.0-rc</li>
   * <li>1.2.3 (build 123) will be accepted.</li>
   * </ul>
   * 
   * @param str A string representation of the version number;
   */
  public Version(String str) {
    delim = ".";
    String[] splitMinus = str.split("-");
    String[] splitSpace = str.split(" ", 2);
    if (splitMinus.length < 1) {
      error(str, delim);
      // empty string is 0.0.0
    }

    String main = splitMinus[0].split(" ")[0];
    identifiers = splitMinus.length > 1 ? splitMinus[1].split(" ")[0] : "";
    String add = splitSpace.length > 1 ? splitSpace[1] : "";

    String[] splitDot = main.split("[.]");

    if (splitDot.length > 2) {
      iRevision = parseInt(splitDot[2]);
      if (iRevision < 0) {
        error(str, delim);
        identifiers = splitDot[2] + identifiers;
        iRevision = 0;
      }
      revision = String.valueOf(iRevision);
    } else {
      error(str, delim);
    }

    if (splitDot.length > 1) {
      iMinor = parseInt(splitDot[1]);
      if (iMinor < 0) {
        error(str, delim);
        identifiers = splitDot[1] + identifiers;
        iMinor = 0;
      }
      minor = String.valueOf(iMinor);
    } else {
      error(str, delim);
    }

    if (splitDot.length > 0) {
      iMajor = parseInt(splitDot[0]);
      if (iMajor < 0) {
        error(str, delim);
        identifiers = splitDot[0] + identifiers;
        iMajor = 0;
      }
      major = String.valueOf(iMajor);
    } else {
      error(str, delim);
    }
    if (splitDot.length > 3) {
      error(str, delim);
      for (int i = 3; i < splitDot.length; ++i) {
        identifiers = splitDot[i] + identifiers;
      }
    }

    if (add.indexOf("(build ") >= 0) {
      devel = add.substring(add.indexOf("(build ") + 7, add.length() - 1);
      iBuild = parseInt(devel);
      if (iBuild < 0) {
        error(str, delim);
        iBuild = 0;
      }
    } else if (add.trim().length() > 0) {
      error(str, delim);
    }
  }

  private int parseInt(String string) {
    if (isNumber(string))
      return Integer.parseInt(string);
    else
      return -1;
  }

  private void error(String str, String aDelim) {
    if (!error) {
      log.warn("Unable to parse the specified version string \"" + str
          + "\" with the delimiter \"" + aDelim + "\"");
    }
    error = true;
  }

  /**
   * Returns true if parsing detected a syntax error.
   */
  public boolean isError() {
    return error;
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
  public String getRevision() {
    return revision;
  }

  /**
   * Returns the identifiers, that follow after the "-" as string.
   */
  public String getIdentifiers() {
    return identifiers;
  }

  /**
   * Returns the development build.
   */
  public String getBuild() {
    return devel;
  }

  /**
   * Returns the version number in the Form Major.Minor.Build
   */
  @Override
  public String toString() {
    return toString(".");
  }

  /**
   * Returns the version number in the Form Major.Minor.Build
   */
  public String toString(String aDelim) {
    return major + aDelim + minor + aDelim + revision;
  }

  /**
   * Compares this object with the specified object for order. Returns a negative integer, zero, or
   * a positive integer as this object is less than, equal to, or greater than the specified object.
   * Compares by major, minor, build, devel in that order. x.y.z-abc is always smaller than x.y.z. x.y.z-abc compares to
   * x.y.z.defg alphabetically
   * 
   * @see Comparable#compareTo(Object)
   */
  public int compareTo(Object o) {
    Version v = (Version) o;

    if (getMajor().equalsIgnoreCase(v.getMajor())) {
      if (getMinor().equalsIgnoreCase(v.getMinor())) {
        if (getRevision().equalsIgnoreCase(v.getRevision())) {
          String a = devel;
          String b = v.devel;
          int cdev = compareS(a, b);
          if (cdev != 0)
            return cdev;
          return compareIdentifiers(getIdentifiers(), v.getIdentifiers());
        } else {
          // okay, this is a workaround for 2.0.rc1 > 2.0.0
          boolean a = isNumber(getRevision());
          boolean b = isNumber(v.getRevision());
          if ((a && b) || (!a && !b))
            return getRevision().compareTo(v.getRevision());
          else if (a)
            return 1;
          else
            return -1;
        }
      } else
        return compareS(getMinor(), v.getMinor());
    } else
      return compareS(getMajor(), v.getMajor());
  }

  private int compareIdentifiers(String id11, String id22) {
    String[] parts1 = id11.split("\\Q" + delim + "\\E"); // quoting delim, so "." isn't interpreted as regular
                                                         // expression
    String[] parts2 = id22.split("\\Q" + delim + "\\E");
    int i = 0;
    for (i = 0; i < parts1.length && i < parts2.length; ++i) {
      String id1 = parts1[i];
      String id2 = parts2[i];
      if (id1.isEmpty() && !id2.isEmpty())
        return 1;
      if (!id1.isEmpty() && id2.isEmpty())
        return -1;
      boolean n1 = isNumber(id1), n2 = isNumber(id2);
      if (n1 && !n2)
        return -1;
      if (n2 && !n1)
        return 1;
      int c;
      if (n1 && n2) {
        c = Integer.parseInt(id1) - Integer.parseInt(id2);
      } else {
        c = id1.compareTo(id2);
      }
      if (c != 0)
        return c;
    }
    return parts1.length - parts2.length;

  }

  private int compareS(String a, String b) {
    while (a.length() < b.length()) {
      a = "0" + a;
    }
    while (b.length() < a.length()) {
      b = "0" + b;
    }
    return a.compareTo(b);
  }

  /**
   * Returns true if this version is strictly greater the aVersion.
   */
  public boolean isNewer(Version aVersion) {
    return compareTo(aVersion) > 0;
  }

  private boolean isNumber(String s) {
    try {
      Integer.parseInt(s);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

}
