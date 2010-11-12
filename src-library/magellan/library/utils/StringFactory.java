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

import java.util.HashMap;
import java.util.Map;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class StringFactory {
  private static final StringFactory sf = new StringFactory();

  private StringFactory() {
  }

  /**
   * DOCUMENT-ME
   */
  public static StringFactory getFactory() {
    return StringFactory.sf;
  }

  private Map<String, String> strings = new HashMap<String, String>();

  /**
   * DOCUMENT-ME
   */
  public String intern(String s) {
    String is = strings.get(s);

    if (is == null) {
      is = getOptimizedString(s);
      strings.put(is, is);
    }

    return is;
  }

  /**
   * DOCUMENT-ME
   */
  public String getOptimizedString(String s) {
    // FIXME(stm) understand and assess this
    // copy all strings into new char and recreate string with it.
    // Prevent inefficient use of char[]
    char allchars[] = new char[s.length()];
    s.getChars(0, s.length(), allchars, 0);

    return new String(allchars);
  }
}
