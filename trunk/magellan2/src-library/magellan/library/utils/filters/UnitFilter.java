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

package magellan.library.utils.filters;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import magellan.library.Unit;
import magellan.library.utils.Resources;


/**
 * The base class for filtering units. Designed after FileFilter and similar interfaces, but as an
 * abstract class to have a short-cut for Collections implemented. ,
 *
 * @author Andreas
 * @version
 */
public abstract class UnitFilter {
	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public abstract boolean acceptUnit(Unit u);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public Collection<Unit> acceptUnits(Collection<Unit> col) {
		return acceptUnits(col, false);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public Collection<Unit> acceptUnits(Collection<Unit> col, boolean useThis) {
		Collection<Unit> col2 = null;

		if(useThis) {
			col2 = col;
		} else {
			col2 = new LinkedList<Unit>(col);
		}

		Iterator<Unit> it = col2.iterator();

		while(it.hasNext()) {
			if(!acceptUnit(it.next())) {
				it.remove();
			}
		}

		return col2;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getName() {
		String ret = Resources.get("magellan.unitfilter."+getClass().getName());

		if(ret == null) {
			ret = "UnitFilter";
		}

		return ret;
	}
}
