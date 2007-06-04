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
import java.util.Iterator;
import java.util.List;

import magellan.library.Unit;


/**
 * An Inspector inspects the given resource and returns a list of problems.
 */
public class ToDoInspector extends AbstractInspector implements Inspector {
	/** DOCUMENT-ME */
	public static final ToDoInspector INSPECTOR = new ToDoInspector();

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static ToDoInspector getInstance() {
		return INSPECTOR;
	}

	protected ToDoInspector() {
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
		if((u == null) || u.ordersAreNull()) {
			return new ArrayList<AbstractProblem>();
		}

		if(type != Problem.WARNING) {
			return new ArrayList<AbstractProblem>();
		}

		List<AbstractProblem> problems = new ArrayList<AbstractProblem>(2);

		int line = 0;

		for(Iterator iter = u.getOrders().iterator(); iter.hasNext();) {
			line++;

			String order = ((String) iter.next()).trim();

			if(order.startsWith("//")) {
				order = order.substring(2).trim();

				if(order.startsWith("TODO")) {
					problems.add(new CriticizedInformation(u, u, this, order, line));
				}
			} else {
				if(order.startsWith(";")) {
					order = order.substring(1).trim();

					if(order.startsWith("TODO")) {
						problems.add(new CriticizedInformation(u, u, this, order, line));
					}
				}
			}
		}

		if(problems.isEmpty()) {
			return new ArrayList<AbstractProblem>();
		} else {
			return problems;
		}
	}
}
