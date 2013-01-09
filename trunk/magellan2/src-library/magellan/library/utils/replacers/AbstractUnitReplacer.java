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

import magellan.library.Unit;

/**
 * A replacer action on a region argument.
 * 
 * @author unknown
 * @version 1.0
 */
public abstract class AbstractUnitReplacer implements Replacer {
  /**
   * @see magellan.library.utils.replacers.Replacer#getReplacement(java.lang.Object)
   */
  public Object getReplacement(Object r) {
    if (r instanceof Unit)
      return getUnitReplacement((Unit) r);

    return null;
  }

  /**
   * Returns replacement for given unit
   */
  public abstract Object getUnitReplacement(Unit u);

}
