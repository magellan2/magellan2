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

package magellan.client.utils;

import java.awt.Color;

import magellan.library.utils.logging.Logger;

/**
 * A class to unify encoding and decoding of colors stored in string
 * representation (e.g. in magellan.ini).
 * 
 * @author Ilja Pavkovic
 */
public class Colors {
  private static final Logger log = Logger.getInstance(Colors.class);
  private static final String SEPARATOR = ",";

  /**
   * Decode a string with three integers separated by ";" to a Color.
   */
  public static Color decode(String txt) {
    int firstSeparatorPos = txt.indexOf(Colors.SEPARATOR);
    int secondSeparatorPos = txt.lastIndexOf(Colors.SEPARATOR);

    if ((firstSeparatorPos > -1) && (secondSeparatorPos > -1)) {
      try {
        int r = Integer.parseInt(txt.substring(0, firstSeparatorPos));
        int g = Integer.parseInt(txt.substring(firstSeparatorPos + 1, secondSeparatorPos));
        int b = Integer.parseInt(txt.substring(secondSeparatorPos + 1, txt.length()));

        return new Color(r, g, b);
      } catch (NumberFormatException e) {
        Colors.log.warn("Colors.decode(\"" + txt + "\") failed", e);
      }
    }

    return Color.black;
  }

  /**
   * Encode a color into a String with three integers separated by separator ";".
   */
  public static String encode(Color c) {
    if (c == null)
      return "";
    return c.getRed() + Colors.SEPARATOR + c.getGreen() + Colors.SEPARATOR + c.getBlue();
  }
}
