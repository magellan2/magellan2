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

import java.util.Map;

import magellan.library.utils.logging.Logger;

/**
 * DOCUMENT ME!
 * 
 * @author Sebastian
 * @version 1.0
 */
public final class Umlaut {
  private static final char UMLAUTS[] = { 'Ä', 'Ö', 'Ü', 'ä', 'ö', 'ü', 'ß' };
  private static final String EXPANSIONS[] = { "Ae", "Oe", "Ue", "ae", "oe", "ue", "ss" };
  private static final Map<String, String> recodedStrings = CollectionFactory.createSyncMap();
  private static final Map<String, String> normalizeds = CollectionFactory.createSyncMap();

  /**
   * Expand all umlauts in a string. Note that uppercase umlauts are converted to mixed case
   * expansions (Ä &rarr; Ae).
   */
  public static String convertUmlauts(String string) {
    return Umlaut.recode(string, Umlaut.UMLAUTS, Umlaut.EXPANSIONS);
  }

  /**
   * Search <tt>string</tt> for a character contained in <tt>keys[]</tt> and replace it with the
   * corresponding string in <tt>values[]</tt>.
   * 
   * @param string the string to recode.
   * @param keys an array of chars to be replaced in <tt>string</tt>.
   * @param values an array of the strings that are used as replacements for the corresponding char
   *          in <tt>keys</tt>.
   * @return a string with all occurrences of an element of the <tt>keys</tt> array replaced by the
   *         corresponding element in the <tt>values</tt> array.
   */
  public static String recode(String string, char keys[], String values[]) {
    // recoding is kind of expensive, so store recoded strings
    String s = Umlaut.recodedStrings.get(string);

    if (s == null) {
      s = Umlaut.recodeIt(string, Umlaut.UMLAUTS, Umlaut.EXPANSIONS);
      Umlaut.recodedStrings.put(StringFactory.getFactory().intern(string), s);
    }

    return s;
  }

  private static String recodeIt(String string, char keys[], String values[]) {
    char chars[] = string.toCharArray();
    StringBuffer sb = null;
    boolean foundUmlaut = false;

    for (int i = 0; i < chars.length; i++) {
      foundUmlaut = false;

      char c = chars[i];

      for (int j = 0; j < keys.length; j++) {
        if (c == keys[j]) {
          if (sb == null) {
            sb = new StringBuffer(string.length() + values[j].length());
            sb.insert(0, chars, 0, i);
          }

          sb.append(values[j]);
          foundUmlaut = true;
        }
      }

      if ((foundUmlaut == false) && (sb != null)) {
        sb.append(c);
      }
    }

    if (sb == null)
      return StringFactory.getFactory().intern(string);
    else
      return StringFactory.getFactory().intern(sb.toString());
  }

  /**
   * Replaces all occurences of <tt>from</tt> in <tt>str</tt> with <tt>to</tt>.
   */
  public static String replace(String str, String from, String to) {
    int startIndex = 0;
    int endIndex = 0;
    boolean delimiterFound = false;
    StringBuffer returnString = new StringBuffer();

    do {
      endIndex = str.indexOf(from, startIndex);

      if (endIndex < 0) {
        endIndex = str.length();
        delimiterFound = false;
      } else {
        delimiterFound = true;
      }

      if (startIndex < endIndex) {
        returnString.append(str.substring(startIndex, endIndex));
      }

      if (delimiterFound) {
        returnString.append(to);
        startIndex = endIndex + from.length();
      }
    } while (delimiterFound);

    return returnString.toString();
  }

  // for debugging/profiling
  private static int oldSize = 10;

  /**
   * Expand all umlauts in a string and convert it to uppercase.
   * 
   * @param str the string to be normalized.
   * @return the uppercase version of <tt>str</tt> with all umlauts expanded.
   */
  public static String normalize(String str) {
    if (str == null)
      return null;

    String normalized = normalizeds.get(str);
    if (normalized == null) {
      normalized =
          StringFactory.getFactory().intern(
              Umlaut.convertUmlauts(str).toUpperCase().replaceAll("~", " "));
      normalizeds.put(str, normalized);
      if (normalizeds.size() > oldSize) {
        oldSize *= 2;
        Logger.getInstance(Umlaut.class).finest("normalizeds " + normalizeds.size());
      }
    }
    return normalized;
  }
}
