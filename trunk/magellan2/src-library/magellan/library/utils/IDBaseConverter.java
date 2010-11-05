/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.utils;

import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;

/**
 * A class for handling the input and output of ids at certain bases.
 */
public class IDBaseConverter {
  /**
   * Parses a String and interprets it as a number in the base that is given.
   * 
   * @param str the string to parse.
   * @param base the radix used for parsing the string
   * @return a decimal integer representation of the string.
   * @throws NumberFormatException If str is null or of zero length or cannot be interpreted in the
   *           current base.
   */
  public static int parse(String str, int base) throws NumberFormatException {
    return Integer.parseInt(str, base);
  }

  /**
   * Returns a string representation of id in the requested base. For clarity lowercase 'l's are
   * converted to uppercase.
   * 
   * @param id the id to convert.
   * @param base the radix used for conversion
   * @return the String representation of id.
   */
  public static String toString(int id, int base) {
    return Integer.toString(id, base).replace('l', 'L');
  }

  /**
   * Returns the largest integer representing a valid id by the given base.
   * 
   * @param base
   */
  public static int getMaxId(int base) {
    // base36 is limited to 4 digits
    // Fiete 20080520: why? No limits!
    // because unit and building IDs have at most 4 digits in Eressea! Used by methods that create
    // IDs
    return (base == 10) ? Integer.MAX_VALUE : ((base * base * base * base) - 1);
  }

  /**
   * Field <code>listener</code>
   * 
   * @deprecated is not used any more
   */
  @Deprecated
  private static GameDataListener listener = null;

  /**
	 * 
	 */
  public static void init() {
    if (IDBaseConverter.listener == null) {
      IDBaseConverter.listener = new IDBaseConverterListener();
      // TODO!!!!!!! EventDispatcher.getDispatcher().addGameDataListener(listener);
    }
  }

  /**
   * @deprecated Does not do anything (any more?)
   */
  @Deprecated
  private static class IDBaseConverterListener implements GameDataListener {
    /**
     * Creates a new IDBaseConverterListener object.
     */
    public IDBaseConverterListener() {
    }

    /**
     * @see magellan.library.event.GameDataListener#gameDataChanged(magellan.library.event.GameDataEvent)
     */
    public void gameDataChanged(GameDataEvent e) {
      // IDBaseConverter.getBase();

      try {
        // IDBaseConverter.setBase(e.getGameData().base);
      } catch (IllegalArgumentException iae) {
      }
    }
  }
}
