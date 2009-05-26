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
 * A switch that reacts if the given region is a mallorn region.
 * 
 *
 * @author Fiete
 * @version 1.0
 */
public class MallornRegionSwitch extends AbstractRegionSwitch {
	


	/**
	 * Compares the region's type with the ID given in the constructor.
	 *
	 * 
	 *
	 * 
	 */
	@Override
  public boolean isSwitchingRegion(Region r) {
		return r.isMallorn();
	}

	/**
	 * @see magellan.library.utils.replacers.Replacer#getDescription()
	 */
	public String getDescription() {
    return Resources.get("util.replacers.mallornregion.description");
  }
}
