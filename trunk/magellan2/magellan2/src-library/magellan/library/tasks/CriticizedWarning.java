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
 * A Problem of the WARNING type.
 *
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class CriticizedWarning extends AbstractProblem implements Problem {
	/**
	 * Creates a new CriticizedWarning object.
   *
   * @param s The origin of the problem
   * @param o The object that this problem critisizes
   * @param i The Inspector that reported this problem
   * @param m The message text of the problem
   *
   */
	public CriticizedWarning(Object s, HasRegion o, Inspector i, String m) {
		super(s, o, i, m);
	}

	/**
	 * Creates a new CriticizedWarning object.
	 *
   * @param s The origin of the problem
   * @param o The object that this problem critisizes
   * @param i The Inspector that reported this problem
   * @param m The message text of the problem
   * @param l The line number where the problem occurred
	 * 
	 */
	public CriticizedWarning(Object s, HasRegion o, Inspector i, String m, int l) {
		super(s, o, i, m, l);
	}

	/**
	 * returns the type of the problem
	 *
	 * 
	 */
	public int getType() {
		return WARNING;
	}
}
