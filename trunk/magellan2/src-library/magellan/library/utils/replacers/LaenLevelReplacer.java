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
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.ItemType;
import magellan.library.utils.Resources;

/**
 * DOCUMENT ME!
 * 
 * @author Fiete
 * @version 1.0
 */
public class LaenLevelReplacer extends AbstractRegionReplacer {
  /**
   * DOCUMENT-ME
   */
  @Override
  public Object getRegionReplacement(Region region) {
    ItemType ironType = region.getData().rules.getItemType(EresseaConstants.I_RLAEN);
    if (ironType == null)
      return null;
    RegionResource ironResource = region.getResource(ironType);
    if (ironResource == null)
      return null;
    return new Integer(ironResource.getSkillLevel());
  }

  public String getDescription() {
    return Resources.get("util.replacers.laenlevelreplacer.description");
  }
}
