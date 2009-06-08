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
 * A (possibly abstract) relation originating from a source unit.
 */
public abstract class UnitRelation {
	/** the origin of this relation, normally the same as source */
	public Unit origin;

	/** the source of this relation */
	public final Unit source;

	/** The line in the source's orders that is the cause for this relation. The first line is 1. */
	public final int line;

	/** if somethin is amiss, this should be true */
	public boolean warning;
	
	/**
	 * Creates a new UnitRelation object.
	 *
	 * @param s The source unit
	 * @param line The line in the source's orders. The first line is 1.
	 * @param w <code>true</code> iff this relation causes a warning
	 */
	public UnitRelation(Unit s, int line, boolean w) {
		this.origin = s;
		this.source = s;
		this.line = line;
		this.warning = w;
	}

	/**
	 * Creates a new UnitRelation object.
	 *
	 * @param s The source unit
	 * @param line The line in the source's orders
	 */
	public UnitRelation(Unit s, int line) {
		this.origin = s;
		this.source = s;
		this.line = line;
		this.warning = false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
  public String toString() {
		return this.getClass().getName() + "@ORIGIN=" + origin + "@SOURCE=" + source + "@line=" +
			   line + "@WARNING=" + warning;
	}

	/**
	 * Returns true iff this relation is caused by a long order
	 *
	 * @return true iff this relation is caused by a long order
	 */
	public boolean isLongOrder() {
		return this instanceof LongOrderRelation;
	}
}
