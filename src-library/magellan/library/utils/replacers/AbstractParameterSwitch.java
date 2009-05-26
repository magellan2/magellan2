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

import magellan.library.utils.Resources;

/**
 * A default implementation for replacers implementing AbstractSwitch and ParameterReplacer.
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
	 * @see magellan.library.utils.replacers.ParameterReplacer#getParameterCount()
	 */
	public int getParameterCount() {
		return parameters.length;
	}

	/**
	 * @see magellan.library.utils.replacers.ParameterReplacer#setParameter(int, java.lang.Object)
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
	
	 /**
   * Returns a string describing the number of parameters.
   * 
   * @see magellan.library.utils.replacers.Replacer#getDescription()
   */
  public String getDescription(){
    return Resources.get("util.replacers.abstractparameter.description", new Object[] { getParameterCount() } );
  }

}
