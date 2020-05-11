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
  private String build = "0";
  private String devel = "0";

  private int iMajor;
  private int iMinor;
  private int iBuild;
  private int iDevel;

  private boolean isNumber = false;
  private String identifiers = "";
  private boolean error;

  private String delim;

  /**
   * Creates a new Version object with numeric parts.
   * 
   * @throws NumberFormatException If the given String cannot be parsed into a version number
   * @deprecated Use {@link #Version(String)} with semantic version.
   */
  @Deprecated
  public Version(String str, String delim) throws NumberFormatException {
    this(str, delim, true);
  }

  /**
   * If checkInteger is true, parses a simple version string like 1.2.3 with exactly three numeric parts separated by
   * dots. Otherwise a number with major, minor, and build version.
   * 
   * @throws NumberFormatException If the given String cannot be parsed into a version number
   * @deprecated use {@link #Version(String)} and semantic version.
   */
  @Deprecated
  public Version(String str, String delim, boolean checkInteger) throws NumberFormatException {
    this.delim = delim;
    StringTokenizer st = new StringTokenizer(str, delim);
    isNumber = checkInteger;

    if (st.countTokens() == 3) {
      if (isNumber) {
        major = Integer.toString(iMajor = Integer.parseInt(st.nextToken()));
        minor = Integer.toString(iMinor = Integer.parseInt(st.nextToken()));
        build = Integer.toString(iBuild = Integer.parseInt(st.nextToken()));
      } else {
        major = st.nextToken();
        minor = st.nextToken();
        build = st.nextToken();
        iMajor = Integer.parseInt(major);
        iMinor = Integer.parseInt(minor);
        try {
          iBuild = Integer.parseInt(build);
        } catch (NumberFormatException e) {
          iBuild = 0;
        }
        if (build.indexOf(" (build ") > 0) {
          devel = build.substring(build.indexOf(" (build ") + 8, build.length() - 1);
          build = build.substring(0, build.indexOf(" (")).trim();
        }
      }
    } else
      throw new NumberFormatException("Unable to parse the specified version string \"" + str
          + "\" with the delimiter \"" + delim + "\"");
  }

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
    isNumber = false;

    if (splitDot.length > 2) {
      iBuild = parseInt(splitDot[2]);
      if (iBuild < 0) {
        error(str, delim);
        identifiers = splitDot[2] + identifiers;
        iBuild = 0;
      }
      build = String.valueOf(iBuild);
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
      iDevel = parseInt(devel);
      if (iDevel < 0) {
        error(str, delim);
        iDevel = 0;
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

  private void error(String str, String delim) {
    if (!error) {
      log.warn("Unable to parse the specified version string \"" + str
          + "\" with the delimiter \"" + delim + "\"");
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
  public String getBuild() {
    return build;
  }

  /**
   * Returns the identifers, that follow after the "-" as string.
   */
  public String getIdentifiers() {
    return identifiers;
  }

  /**
   * Returns the development build.
   */
  public String getDevel() {
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
  public String toString(String delim) {
    return major + delim + minor + delim + build;
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
        if (getBuild().equalsIgnoreCase(v.getBuild())) {
          String a = devel;
          String b = v.devel;
          int cdev = compareS(a, b);
          if (cdev != 0)
            return cdev;
          return compareIdentifiers(getIdentifiers(), v.getIdentifiers());
        } else {
          // okay, this is a workaround for 2.0.rc1 > 2.0.0
          boolean a = isNumber(getBuild());
          boolean b = isNumber(v.getBuild());
          if ((a && b) || (!a && !b))
            return getBuild().compareTo(v.getBuild());
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
