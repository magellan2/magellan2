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
import magellan.library.rules.RegionType;
import magellan.library.utils.Resources;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version 1.0
 */
public class MaxWorkersReplacer extends AbstractRegionReplacer {
	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	@Override
  public Object getRegionReplacement(Region region) {
		if((region.getTrees() != -1) && (region.getSprouts() != -1) && (region.getType() != null)) {
			return new Integer(((RegionType) region.getType()).getInhabitants() -
							   (8 * region.getTrees()) - (4 * region.getSprouts()));
		}

		return null;
	}
  

  public String getDescription() {
    return Resources.get("util.replacers.maxworkersreplacer.description");
  }  

}
