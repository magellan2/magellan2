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
 * UnitCountReplacer.java
 *
 * Created on 29. Dezember 2001, 15:47
 */
package magellan.library.utils.replacers;

import java.util.Collection;
import java.util.Iterator;

import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.utils.Resources;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public class UnitCountReplacer extends AbstractRegionReplacer implements EnvironmentDependent {
	private static final Integer ZERO = new Integer(0);
	protected ReplacerEnvironment environment;
	protected boolean countPersons;

	/**
	 * Creates a new UnitCountReplacer object.
	 */
	public UnitCountReplacer() {
		this(true);
	}

	/**
	 * Creates a new UnitCountReplacer object.
	 *
	 * 
	 */
	public UnitCountReplacer(boolean countPersons) {
		this.countPersons = countPersons;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public Object getRegionReplacement(Region r) {
		Collection c = ((UnitSelection) environment.getPart(ReplacerEnvironment.UNITSELECTION_PART)).getUnits(r);

		if(c != null) {
			int count = 0;

			if(countPersons) {
				Iterator it = c.iterator();

				while(it.hasNext()) {
					count += ((Unit) it.next()).getPersons();
				}
			} else {
				count = c.size();
			}

			if(count > 0) {
				return new Integer(count);
			}

			return ZERO;
		}

		return null;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setEnvironment(ReplacerEnvironment env) {
		environment = env;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getDescription() {
		return Resources.get("util.replacers.unitcountreplacer.description." + countPersons);
	}
}
