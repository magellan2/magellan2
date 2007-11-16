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

package magellan.library.rules;

import magellan.library.ID;

/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 203 $
 */
public class SkillType extends ObjectType {
	protected SkillCategory category;

	/**
	 * Creates a new SkillType object.
	 *
	 * 
	 */
	public SkillType(ID id) {
		super(id);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public SkillCategory getCategory() {
		return category;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setCategory(SkillCategory sc) {
		category = sc;

		if(sc != null) {
			sc.addInstance(this);
		}
	}
}
