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
 * Sets the environment parameter "NullEqualsZero" to true.
 *
 * @author Andreas
 * @version 1.0
 */
public class OperationSwitch extends AbstractParameterReplacer implements EnvironmentDependent,
																		  SwitchOnly
{
	protected ReplacerEnvironment environment;

	/**
	 * Creates new FactionSwitch
	 */
	public OperationSwitch() {
		super(1);
	}


	/**
	 * @see magellan.library.utils.replacers.Replacer#getReplacement(java.lang.Object)
	 */
	public Object getReplacement(Object src) {
		try {
			String fName = getParameter(0, src).toString();
			((OperationMode) environment.getPart(ReplacerEnvironment.OPERATION_PART)).setNullEqualsZero(fName.equals("true"));
		} catch(NullPointerException npe) {
		}

		return Replacer.EMPTY;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * @see magellan.library.utils.replacers.EnvironmentDependent#setEnvironment(magellan.library.utils.replacers.ReplacerEnvironment)
	 */
	public void setEnvironment(ReplacerEnvironment env) {
		environment = env;
	}


  /**
   * @see magellan.library.utils.replacers.Replacer#getDescription()
   */
  public String getDescription() {
    return Resources.get("util.replacers.operationswitch.description")+"\n\n"+super.getDescription();
  }
}
