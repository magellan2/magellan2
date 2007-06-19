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
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
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
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getDescription() {
		return Resources.get("util.replacers.operationswitch.description");
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public Object getReplacement(Object src) {
		try {
			String fName = getParameter(0, src).toString();
			((OperationMode) environment.getPart(ReplacerEnvironment.OPERATION_PART)).setNullEqualsZero(fName.equals("true"));
		} catch(NullPointerException npe) {
		}

		return BLANK;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setEnvironment(ReplacerEnvironment env) {
		environment = env;
	}
}
