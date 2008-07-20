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
 * ItemCategory.java
 *
 * Created on 9. März 2002, 20:39
 */
package magellan.library.rules;

import magellan.library.ID;

/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version 1.0
 */
public class ItemCategory extends Category {
	/**
	 * Creates new ItemCategory
	 *
	 * 
	 */
	public ItemCategory(ID id) {
		super(id);
	}

	/**
	 * Creates a new ItemCategory object.
	 *
	 * 
	 * 
	 */
	public ItemCategory(ID id, Category parent) {
		super(id, parent);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	@Override
  public boolean isInstance(Object o) {
		if(o instanceof ItemType) {
			ItemType it = (ItemType) o;

			if(it.getCategory() != null) {
				return it.getCategory().isDescendant(this);
			}
		}

		return false;
	}
}
