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
import magellan.library.utils.Resources;

/**
 * Returns region herb.
 * 
 * @author unknown
 * @version 1.0
 */
public class HerbReplacer extends AbstractRegionReplacer {
  /**
   * Returns region herb.
   * 
   * @see magellan.library.utils.replacers.AbstractRegionReplacer#getRegionReplacement(magellan.library.Region)
   */
  @Override
  public Object getRegionReplacement(Region r) {
    if (r.getHerb() != null)
      return r.getHerb().getName();
    else if ("keine".equals(r.getHerbAmount()))
      return "---";

    return null;
  }

  /**
   * @see magellan.library.utils.replacers.Replacer#getDescription()
   */
  public String getDescription() {
    return Resources.get("util.replacers.herbreplacer.description");
  }
}
