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

/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version 1.0
 */
public class OperationMode extends EnvironmentPart {
	protected boolean nullEqualsZero = false;

	/**
	 * DOCUMENT-ME
	 */
	@Override
  public void reset() {
		nullEqualsZero = false;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean isNullEqualsZero() {
		return nullEqualsZero;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setNullEqualsZero(boolean bool) {
		nullEqualsZero = bool;
	}
}
