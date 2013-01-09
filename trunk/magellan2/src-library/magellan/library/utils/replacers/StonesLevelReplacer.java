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
import magellan.library.RegionResource;
import magellan.library.rules.ItemType;
import magellan.library.utils.Resources;

/**
 * Returns the skill level for stones of the region.
 * 
 * @author Fiete
 * @version 1.0
 */
public class StonesLevelReplacer extends AbstractRegionReplacer {
  private ItemType resourceType;

  /**
   * @param resourceType
   */
  public StonesLevelReplacer(ItemType resourceType) {
    this.resourceType = resourceType;
  }

  /**
   * Returns the skill level for stones of the region.
   * 
   * @see magellan.library.utils.replacers.AbstractRegionReplacer#getRegionReplacement(magellan.library.Region)
   */
  @Override
  public Object getRegionReplacement(Region region) {
    if (resourceType == null)
      return null;
    RegionResource ironResource = region.getResource(resourceType);
    if (ironResource == null)
      return null;
    return Integer.valueOf(ironResource.getSkillLevel());
  }

  public String getDescription() {
    return Resources.get("util.replacers.stoneslevelreplacer.description");
  }
}
