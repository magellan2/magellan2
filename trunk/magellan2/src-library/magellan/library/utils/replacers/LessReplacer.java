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

import magellan.library.utils.Resources;

/**
 * A Switch that compares the following to elements by their String replacement. Possible replacers
 * are evaluated by forwarding the Switch object and iterator. If these two are not
 * evaluatable(list too short) or only on of them is <i>null</i> the Switch stays active. <i>Note
 * that if both are null the switch is inactive!</i>
 *
 * @author Andreas
 * @version
 */
public class LessReplacer extends AbstractParameterSwitch {
	/**
	 * Creates a new LessReplacer object.
	 */
	public LessReplacer() {
		super(2);
	}

	/**
	 * Checks the following two elements and evaluates their replacements. They are treated as
	 * Strings through <i>toString()</i> and compared for equality.
	 *
	 * 
	 *
	 * 
	 *
	 * @throws IllegalArgumentException DOCUMENT-ME
	 */
	public boolean isSwitchingObject(Object o) {
		Object o1 = getParameter(0, o);
		Object o2 = getParameter(1, o);

		if((o1 != null) && (o2 != null)) {
			// first check on numbers
			try {
				float f1 = 0;
				float f2 = 0;

				if(o1 instanceof Number) {
					f1 = ((Number) o1).floatValue();
				} else {
					f1 = Float.parseFloat(o1.toString());
				}

				if(o2 instanceof Number) {
					f2 = ((Number) o2).floatValue();
				} else {
					f2 = Float.parseFloat(o2.toString());
				}

				return f1 < f2;
			} catch(NumberFormatException nfe) {
			}

			return o1.toString().compareTo(o2.toString()) < 0;
		}

		throw new IllegalArgumentException("Not enough arguments.");
	}


  public String getDescription() {
    return Resources.get("magellan.util.replacers.lessreplacer.description");
  }  
}
