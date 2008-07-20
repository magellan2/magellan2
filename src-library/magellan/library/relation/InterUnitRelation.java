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
 * A (possibly abstract) relation indicating between a source and a target unit.
 */
public class InterUnitRelation extends UnitRelation {
	/** DOCUMENT-ME */
	public Unit target;

	/**
	 * Creates a new InterUnitRelation object.
	 *
	 * @param s The source unit
	 * @param t The target unit
	 * @param line The line in the source's orders
	 */
	public InterUnitRelation(Unit s, Unit t, int line) {
		super(s, line);
		this.target = t;
	}

	/**
	 * Creates a new InterUnitRelation object.
	 *
	 * @param s The source unit
	 * @param t The target unit
	 * @param line The line in the source's orders
	 * @param w <code>true</code> iff this relation causes a warning
	 */
	public InterUnitRelation(Unit s, Unit t, int line, boolean w) {
		super(s, line, w);
		this.target = t;
	}

	/* (non-Javadoc)
	 * @see com.eressea.relation.UnitRelation#toString()
	 */
	@Override
  public String toString() {
		return super.toString() + "@TARGET=" + target;
	}
}
