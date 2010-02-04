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
 * Returns the number of max workers per region. This depends on some details - region type - number
 * of trees in the region - number of sprouts in the region Because this is game specific we use the
 * game specific stuff in the future.
 * 
 * @author Andreas
 * @version 1.0
 */
public class MaxWorkersReplacer extends AbstractRegionReplacer {
  /**
   * Returns the number of max workers per region.
   */
  @Override
  public Object getRegionReplacement(Region region) {
    return region.getData().getGameSpecificStuff().getGameSpecificRules().getMaxWorkers(region);
  }

  public String getDescription() {
    return Resources.get("util.replacers.maxworkersreplacer.description");
  }

}
