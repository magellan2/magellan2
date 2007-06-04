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

import magellan.library.utils.Resources;
import magellan.library.utils.Taggable;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public class TagReplacer extends AbstractParameterReplacer {
	protected boolean mode;

	/**
	 * Creates new TagReplacer
	 *
	 * 
	 */
	public TagReplacer(boolean mode) {
		super(1);
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
		if(o instanceof Taggable) {
			Object obj = getParameter(0, o);

			if(obj != null) {
				Taggable t = (Taggable) o;
				String s = obj.toString();

				if(t.containsTag(s)) {
					return t.getTag(s);
				}

				if(mode) {
					return BLANK;
				}
			}
		}

		return null; // no parameter or not an object with tags is always error
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getDescription() {
		return Resources.get( "magellan.util.replacers.tagreplacer.description." + mode);
	}
}
