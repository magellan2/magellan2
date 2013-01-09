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

import magellan.library.Region;

/**
 * A replacer action on a region argument.
 * 
 * @author unknown
 * @version 1.0
 */
public abstract class AbstractRegionReplacer implements Replacer {
  /**
   * @see magellan.library.utils.replacers.Replacer#getReplacement(java.lang.Object)
   */
  public Object getReplacement(Object r) {
    if ((r instanceof Region))
      return getRegionReplacement((Region) r);

    return null;
  }

  /**
   * Returns replacemenet for given region
   */
  public abstract Object getRegionReplacement(Region r);

}
