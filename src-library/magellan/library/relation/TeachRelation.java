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

package magellan.library.relation;

import magellan.library.Unit;

/**
 * A relation indicating that the source unit has a LEHREN order for the target unit.
 */
public class TeachRelation extends InterUnitRelation implements LongOrderRelation {
	/**
	 * Creates a new TeachRelation object.
	 *
	 * @param s The source unit
	 * @param t The target unit
	 * @param line The line in the source's orders
	 */
	public TeachRelation(Unit s, Unit t, int line) {
		super(s, t, line);
	}

	/**
	 * Creates a new TeachRelation object.
	 *
	 * @param s The source unit
	 * @param t The target unit
	 * @param line The line in the source's orders
	 * @param w <code>true</code> iff this relation causes a warning
	 */
	public TeachRelation(Unit s, Unit t, int line, boolean w) {
		super(s, t, line, w);
	}
}
