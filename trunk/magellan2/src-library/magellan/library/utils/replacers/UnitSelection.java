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

/*
 * FactionSelectableHelp.java
 *
 * Created on 30. Dezember 2001, 16:01
 */
package magellan.library.utils.replacers;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.utils.filters.UnitFilter;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version 1.0
 */
public class UnitSelection extends EnvironmentPart {
	protected List<UnitFilter> filters = new LinkedList<UnitFilter>();

	/**
	 * DOCUMENT-ME
	 */
	@Override
  public void reset() {
		filters.clear();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void addFilter(UnitFilter f) {
		filters.add(f);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void removeFilters(Class filterClass) {
		Iterator it = filters.iterator();

		while(it.hasNext()) {
			Class c = it.next().getClass();

			if(filterClass.equals(c)) {
				it.remove();
			}
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void removeFilter(UnitFilter f) {
		filters.remove(f);
	}

	/**
	 * DOCUMENT-ME
	 */
	public void removeAllFilters() {
		filters.clear();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public boolean belongsTo(Object o) {
		if(!(o instanceof Unit)) {
			return false;
		}

		if(filters.size() == 0) {
			return true;
		}

		Unit u = (Unit) o;
		Iterator it = filters.iterator();

		while(it.hasNext()) {
			UnitFilter filter = (UnitFilter) it.next();

			if(!filter.acceptUnit(u)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public Collection getUnits(Region r) {
		Collection<Unit> retList = new LinkedList<Unit>(r.units());
		Iterator<UnitFilter> it = filters.iterator();
		int i = 0;

		while(it.hasNext()) {
			retList = it.next().acceptUnits(retList, true);

			if(retList.size() == 0) {
				return retList;
			}

			i++;
		}

		return retList;
	}
}
