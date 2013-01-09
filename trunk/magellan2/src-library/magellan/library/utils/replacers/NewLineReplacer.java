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

package magellan.library.utils.replacers;

import magellan.library.utils.Resources;

/**
 * Evaluates to the newline character.
 * 
 * @author Andreas
 * @version 1.0
 */
public class NewLineReplacer implements Replacer {
  protected static final String NEWLINE = "\n";

  /**
   * Evaluates to the newline character.
   */
  public Object getReplacement(Object o) {
    return NewLineReplacer.NEWLINE;
  }

  /**
   * @see magellan.library.utils.replacers.Replacer#getDescription()
   */
  public String getDescription() {
    return Resources.get("util.replacers.newlinereplacer.description");
  }

}
