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

import java.util.Collection;
import java.util.Iterator;

import magellan.library.Item;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.rules.ItemType;


/**
 * Replaces a item name string with the number of that item in the given region.
 *
 * @author unknown
 * @version
 */
public class ItemTypeReplacer extends AbstractParameterReplacer implements EnvironmentDependent {
	private static final Integer ZERO = new Integer(0);
	protected ReplacerEnvironment environment;

	/**
	 * Creates a new ItemTypeReplacer object.
	 */
	public ItemTypeReplacer() {
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
			String items = getParameter(0, o).toString();
			int count = 0;
			Collection c = ((UnitSelection) environment.getPart(ReplacerEnvironment.UNITSELECTION_PART)).getUnits((Region) o);

			if(c == null) {
				return null;
			}

			Iterator it = c.iterator();

			while(it.hasNext()) {
				Unit u = (Unit) it.next();
				Iterator it2 = u.getItems().iterator();

				while(it2.hasNext()) {
					Item i = (Item) it2.next();
					ItemType ity = i.getItemType();

					if(ity.getName().equalsIgnoreCase(items) ||
						   ity.getID().toString().equalsIgnoreCase(items)) {
						count += i.getAmount();

						break;
					}
				}
			}

			if(count != 0) {
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
}
