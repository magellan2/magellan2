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
import java.util.Collections;
import java.util.List;

import magellan.library.Region;
import magellan.library.Unit;


/**
 * This is an abstract implementation of an inspector. You can use this
 * as a base for your own implementation.
 *
 * @author $Author: $
 * @version $Revision: 171 $
 */
public abstract class AbstractInspector implements Inspector {
	protected AbstractInspector() {
	}

  /**
   * @see magellan.library.tasks.Inspector#reviewUnit(magellan.library.Unit)
   */
	public List<Problem> reviewUnit(Unit u) {
		List<Problem> problems = new ArrayList<Problem>(10);

		problems.addAll(reviewUnit(u, Problem.INFORMATION));
		problems.addAll(reviewUnit(u, Problem.WARNING));
		problems.addAll(reviewUnit(u, Problem.ERROR));

		return problems.isEmpty() ? new ArrayList<Problem>() : problems;
	}

  /**
   * Returns an empty list.
   * 
   * @see magellan.library.tasks.Inspector#reviewUnit(magellan.library.Unit, int)
   */
	public List<Problem> reviewUnit(Unit u, int type) {
		return Collections.emptyList();
	}

  /**
   * @see magellan.library.tasks.Inspector#reviewRegion(magellan.library.Region)
   */
	public List<Problem> reviewRegion(Region r) {
		List<Problem> problems = new ArrayList<Problem>(2);
		problems.addAll(reviewRegion(r, Problem.INFORMATION));
		problems.addAll(reviewRegion(r, Problem.WARNING));
		problems.addAll(reviewRegion(r, Problem.ERROR));

		return problems.isEmpty() ? new ArrayList<Problem>() : problems;
	}

  /**
   * Returns an empty list.
   * 
   * @see magellan.library.tasks.Inspector#reviewRegion(magellan.library.Region, int)
   */
	public List<Problem> reviewRegion(Region r, int type) {
		return Collections.emptyList();
	}
}
