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
 * Returns trade volume.
 * 
 * @author unknown
 * @version 1.0
 */
public class MaxTradeReplacer extends AbstractRegionReplacer {
  protected static final Integer ZERO = Integer.valueOf(0);

  /**
   * Returns the luxury volume.
   * 
   * @see magellan.library.utils.replacers.AbstractRegionReplacer#getRegionReplacement(magellan.library.Region)
   */
  @Override
  public Object getRegionReplacement(Region r) {
    if (r.maxLuxuries() >= 0) {
      if (r.maxLuxuries() == 0)
        return MaxTradeReplacer.ZERO;

      return Integer.valueOf(r.maxLuxuries());
    }

    return null;
  }

  /**
   * @see magellan.library.utils.replacers.Replacer#getDescription()
   */
  public String getDescription() {
    return Resources.get("util.replacers.maxtradereplacer.description");
  }

}
