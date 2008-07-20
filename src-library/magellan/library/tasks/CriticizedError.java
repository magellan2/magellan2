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

package magellan.library.tasks;

import magellan.library.HasRegion;

/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class CriticizedError extends AbstractProblem implements Problem {
	/**
	 * Creates a new CriticizedError object.
	 *
	 * 
	 * 
	 * 
	 * 
	 */
	public CriticizedError(Object s, HasRegion o, Inspector i, String m) {
		super(s, o, i, m);
	}

	/**
	 * Creates a new CriticizedError object.
	 *
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	public CriticizedError(Object s, HasRegion o, Inspector i, String m, int l) {
		super(s, o, i, m, l);
	}

	/**
	 * returns the type of the problem
	 *
	 * 
	 */
	@Override
  public int getType() {
		return Problem.ERROR;
	}
}
