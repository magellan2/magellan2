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
 * @version
 */
public class MallornReplacer extends AbstractRegionReplacer {
	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public Object getRegionReplacement(Region region) {
		if (!region.isMallorn()){
			return new Integer(0);
		}
		ItemType mallornType = region.getData().rules.getItemType("Mallorn");
		if (mallornType==null){
			return new Integer(0);
		}
		RegionResource mallornResource = region.getResource(mallornType);
		if (mallornResource==null){
			return new Integer(0);
		}
		return new Integer(mallornResource.getAmount());
	}
  

  public String getDescription() {
    return Resources.get("util.replacers.mallornreplacer.description");
  }  

}
