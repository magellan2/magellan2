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

import java.util.List;

import magellan.library.CoordinateID;
import magellan.library.Unit;


/**
 * A relation indicating a movement of a unit.
 */
public class MovementRelation extends UnitRelation implements LongOrderRelation {
	/** This list consists of the reached coordinates (starting with the current region) */
	public final List<CoordinateID> movement;

	/**
	 * Creates a new MovementRelation object.
	 *
	 * @param s The source unit
	 * @param m The list of region coordinates
	 * @param line The line in the source's orders
	 */
	public MovementRelation(Unit s, List<CoordinateID> m, int line) {
		super(s, line);
		this.movement = m;
	}

	/**
	 * @see magellan.library.relation.UnitRelation#toString()
	 */
	public String toString() {
		return super.toString() + "@MOVEMENT=" + movement;
	}
}
