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

/**
 * DOCUMENT ME!
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
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public Object getReplacement(Object o) {
		Object obj = getParameter(0, o);

		if(obj != null) {
			if(TRUE.equals(obj.toString())) {
				return FALSE;
			}

			if(FALSE.equals(obj.toString())) {
				return TRUE;
			}
		}

		return null;
	}
}
