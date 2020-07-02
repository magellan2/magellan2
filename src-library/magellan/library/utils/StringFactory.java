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

import magellan.library.utils.logging.Logger;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 171 $
 */
public final class StringFactory {
  private static final StringFactory sf = new StringFactory();

  private StringFactory() {
  }

  /**
   * DOCUMENT-ME
   */
  public static StringFactory getFactory() {
    return StringFactory.sf;
  }

  private Map<String, String> strings = new HashMap<String, String>(); // new WeakHashMap<String, String>();
  private int oldSize = 10;

  /**
   * DOCUMENT-ME
   */
  public String intern(String s) {
    // FIXME map only for s.length()<100??
    String is = strings.get(s);

    if (is == null) {
      is = getOptimizedString(s);
      strings.put(is, is);
      if (strings.size() > oldSize) {
        oldSize *= 2;
        Logger.getInstance(Umlaut.class).finest("strings " + strings.size());
      }
    }

    return is;
  }

  /**
   * DOCUMENT-ME
   */
  public String getOptimizedString(String s) {
    // copy all strings into new char and recreate string with it.
    // Prevent inefficient use of char[]
    char allchars[] = new char[s.length()];
    s.getChars(0, s.length(), allchars, 0);

    return new String(allchars);
  }
}
