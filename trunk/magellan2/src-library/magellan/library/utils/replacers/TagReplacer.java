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
 * @version 1.0
 */
public class TagReplacer extends AbstractParameterReplacer {
	protected boolean mode;

	/**
	 * Creates new TagReplacer
	 *
	 * 
	 * @param mode
	 */
	public TagReplacer(boolean mode) {
		super(1);
		this.mode = mode;
	}

	/**
	 * Returns the value of the tag given as parameter from the unit given as argument. If mode==true,
	 * an empty string is returned instead of null.
	 * 
	 * @see magellan.library.utils.replacers.Replacer#getReplacement(java.lang.Object)
	 */
	public Object getReplacement(Object unit) {
		if(unit instanceof Taggable) {
			Object obj = getParameter(0, unit);

			if(obj != null) {
				Taggable t = (Taggable) unit;
				String s = obj.toString();

				if(t.containsTag(s)) {
					return t.getTag(s);
				}

				if(mode) {
					return Replacer.EMPTY;
				}
			}
		}

		return null; // no parameter or not an object with tags is always error
	}

	/**
	 *
	 * 
   * @see magellan.library.utils.replacers.Replacer#getDescription()
   */
  public String getDescription() {
		return Resources.get("util.replacers.tagreplacer.description." + mode)+"\n\n"+super.getDescription();
	}
}
