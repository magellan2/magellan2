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

import java.util.Iterator;

import magellan.library.LuxuryPrice;
import magellan.library.Region;
import magellan.library.rules.ItemType;


/**
 * DOCUMENT ME!
 *
 * @author unknown
 * @version 1.0
 */
public class LuxuryPriceReplacer extends AbstractParameterReplacer {
	/**
	 * Creates a new LuxuryPriceReplacer object.
	 */
	public LuxuryPriceReplacer() {
		super(1);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public Object getReplacement(Object o) {
		if(o instanceof Region) {
			Region r = (Region) o;

			if(r.getPrices() == null) {
				return null;
			}

			String luxury = getParameter(0, o).toString();
			Iterator<LuxuryPrice> it = r.getPrices().values().iterator();

			while(it.hasNext()) {
				LuxuryPrice lp = it.next();
				ItemType ity = lp.getItemType();

				if(ity.getName().equals(luxury) || ity.getID().toString().equals(luxury)) {
					return new Integer(lp.getPrice());
				}
			}
		}

		return null;
	}
}
