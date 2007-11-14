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
 * AbstractParameterSwitch.java
 *
 * Created on 20. Mai 2002, 14:47
 */
package magellan.library.utils.replacers;

/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version 1.0
 */
public abstract class AbstractParameterSwitch extends AbstractSwitch implements ParameterReplacer {
	protected Object parameters[];

	/**
	 * Creates new AbstractParameterSwitch
	 *
	 * 
	 */
	public AbstractParameterSwitch(int parameters) {
		this.parameters = new Object[parameters];
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getParameterCount() {
		return parameters.length;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 */
	public void setParameter(int index, Object obj) {
		parameters[index] = obj;
	}

	protected Object getParameter(int index, Object o) {
		if(parameters[index] != null) {
			if(parameters[index] instanceof Replacer) {
				return ((Replacer) parameters[index]).getReplacement(o);
			}
		}

		return parameters[index];
	}
}
