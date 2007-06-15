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
 * @version
 */
public class SkillCategory extends Category {
	/**
	 * Creates new ItemCategory
	 *
	 * 
	 */
	public SkillCategory(ID id) {
		super(id);
	}

	/**
	 * Creates a new SkillCategory object.
	 *
	 * 
	 * 
	 */
	public SkillCategory(ID id, Category parent) {
		super(id, parent);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public boolean isInstance(Object o) {
		if(o instanceof SkillType) {
			SkillType st = (SkillType) o;

			if(st.getCategory() != null) {
				return st.getCategory().isDescendant(this);
			}
		}

		return false;
	}
}
