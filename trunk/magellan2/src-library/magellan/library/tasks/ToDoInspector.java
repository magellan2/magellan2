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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import magellan.library.Unit;
import magellan.library.tasks.Problem.Severity;


/**
 * This Inspector inspects a unit's orders and reports an item for every comment
 * that starts with "TODO".
 */
public class ToDoInspector extends AbstractInspector {
	/** The singleton instance. */
	public static final ToDoInspector INSPECTOR = new ToDoInspector();

	private static final ProblemType TODOTYPE = new ProblemType("TODO", null, null, null, getInstance());

	/**
	 * Returns a (singleton) instance.
	 * 
   * @return An instance of this class
	 */
	public static ToDoInspector getInstance() {
		return ToDoInspector.INSPECTOR;
	}

	protected ToDoInspector() {
	}

	/**
	 * @see magellan.library.tasks.AbstractInspector#reviewUnit(magellan.library.Unit, int)
	 */
	@Override
  public List<Problem> reviewUnit(Unit u, Severity severity) {
		if((u == null) || u.ordersAreNull()) {
			return Collections.emptyList();
		}

		if(severity != Severity.WARNING) {
			return Collections.emptyList();
		}

		List<Problem> problems = new ArrayList<Problem>(2);

		int line = 0;

		for(Iterator iter = u.getOrders().iterator(); iter.hasNext();) {
      line++;
			String order = ((String) iter.next()).trim();

			if(order.startsWith("//")) {
				order = order.substring(2).trim();

				if(order.toLowerCase().startsWith("todo")) {
					problems.add(ProblemFactory.createProblem(Severity.INFORMATION, TODOTYPE, u, order, line)); 
				}
			} else 
				if(order.startsWith(";")) {
					order = order.substring(1).trim();

					if(order.toLowerCase().startsWith("todo")) {
	          problems.add(ProblemFactory.createProblem(Severity.INFORMATION, TODOTYPE, u, order, line)); 
					}
				
			}
		}

		if(problems.isEmpty()) {
			return Collections.emptyList();
		} else {
			return problems;
		}
	}

  public Collection<ProblemType> getTypes() {
    return Collections.singletonList(TODOTYPE);
  }

}