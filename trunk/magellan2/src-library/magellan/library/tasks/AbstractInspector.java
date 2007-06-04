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

import java.util.ArrayList;
import java.util.List;

import magellan.library.Region;
import magellan.library.Unit;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 171 $
 */
public abstract class AbstractInspector implements Inspector {
	protected AbstractInspector() {
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public List<AbstractProblem> reviewUnit(Unit u) {
		List<AbstractProblem> problems = new ArrayList<AbstractProblem>(10);

		problems.addAll(reviewUnit(u, Problem.INFORMATION));
		problems.addAll(reviewUnit(u, Problem.WARNING));
		problems.addAll(reviewUnit(u, Problem.ERROR));

		return problems.isEmpty() ? new ArrayList<AbstractProblem>() : problems;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public List<AbstractProblem> reviewUnit(Unit u, int type) {
		return new ArrayList<AbstractProblem>();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public List<AbstractProblem> reviewRegion(Region r) {
		List<AbstractProblem> problems = new ArrayList<AbstractProblem>(2);
		problems.addAll(reviewRegion(r, Problem.INFORMATION));
		problems.addAll(reviewRegion(r, Problem.WARNING));
		problems.addAll(reviewRegion(r, Problem.ERROR));

		return problems.isEmpty() ? new ArrayList<AbstractProblem>() : problems;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public List<AbstractProblem> reviewRegion(Region r, int type) {
		return new ArrayList<AbstractProblem>();
	}
}
