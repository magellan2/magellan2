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
 * DOCUMENT ME!
 *
 * @author Fiete
 * @version 1.0
 */
public class LaenReplacer extends AbstractRegionReplacer {
	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	@Override
  public Object getRegionReplacement(Region region) {
		ItemType laenType = region.getData().rules.getItemType("Laen");
		if (laenType==null){
			return new Integer(0);
		}
		RegionResource laenResource = region.getResource(laenType);
		if (laenResource==null){
			return new Integer(0);
		}
		return new Integer(laenResource.getAmount());
	}

  public String getDescription() {
    return Resources.get("util.replacers.laenreplacer.description");
  }  
}
