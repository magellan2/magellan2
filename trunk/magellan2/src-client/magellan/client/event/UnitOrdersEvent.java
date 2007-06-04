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

package magellan.client.event;

import java.util.Collection;
import java.util.HashSet;

import magellan.library.Unit;
import magellan.library.event.TimeStampedEvent;


/**
 * An event indicating that the orders of a certain unit were modified.
 *
 * @see UnitOrdersListener
 * @see EventDispatcher
 */
public class UnitOrdersEvent extends TimeStampedEvent {
	private Unit unit;

	/** A collection of related units before the unit orders was changed */
	private Collection<Unit> relatedUnits;

	/**
	 * Creates an event object.
	 *
	 * @param source the object that originated the event.
	 * @param unit the unit which orders changed.
	 */
	public UnitOrdersEvent(Object source, Unit unit) {
		super(source);
		this.unit = unit;
		this.relatedUnits = new HashSet<Unit>();
		unit.getRelatedUnits(relatedUnits);
	}

	/**
	 * Returns the unit which orders changed.
	 *
	 * 
	 */
	public Unit getUnit() {
		return unit;
	}

	/**
	 * Returns the relates units
	 *
	 * 
	 */
	public Collection getRelatedUnits() {
		return relatedUnits;
	}
}
