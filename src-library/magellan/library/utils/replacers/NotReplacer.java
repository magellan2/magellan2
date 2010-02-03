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
 * NotReplacer.java
 *
 * Created on 21. Mai 2002, 17:24
 */
package magellan.library.utils.replacers;

import magellan.library.utils.Resources;

/**
 * Negates the argument if it is TRUE or FALSE. Otherwise, the argument is not changed.
 *
 * @author Andreas
 * @version 1.0
 */
public class NotReplacer extends AbstractParameterReplacer {
	/**
	 * Creates new NotReplacer
	 */
	public NotReplacer() {
		super(1);
	}

	/**
	 * @see magellan.library.utils.replacers.Replacer#getReplacement(java.lang.Object)
	 */
	public Object getReplacement(Object o) {
		Object obj = getParameter(0, o);

		if(obj != null) {
			if(Replacer.TRUE.equals(obj.toString())) {
				return Replacer.FALSE;
			}

			if(Replacer.FALSE.equals(obj.toString())) {
				return Replacer.TRUE;
			}
		}

		return null;
	}

  @Override
  public String getDescription() {
    return Resources.get("util.replacers.not.description")+"\n\n"+super.getDescription();
  }
}
