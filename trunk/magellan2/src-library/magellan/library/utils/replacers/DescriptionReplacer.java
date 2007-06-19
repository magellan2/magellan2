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
 * TagReplacer.java
 *
 * Created on 6. Juni 2002, 18:41
 */
package magellan.library.utils.replacers;

import magellan.library.Described;
import magellan.library.utils.Resources;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public class DescriptionReplacer implements Replacer {
	protected boolean mode;

	/**
	 * Creates a new DescriptionReplacer object.
	 */
	public DescriptionReplacer() {
		this(true);
	}

	/**
	 * Creates new TagReplacer
	 *
	 * 
	 */
	public DescriptionReplacer(boolean mode) {
		this.mode = mode;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public Object getReplacement(Object o) {
		if(o instanceof Described) {
			return ((Described) o).getDescription();
		}

		if(mode) {
			return BLANK;
		}

		return null;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getDescription() {
		return Resources.get("util.replacers.descriptionreplacer.description");
	}
}
