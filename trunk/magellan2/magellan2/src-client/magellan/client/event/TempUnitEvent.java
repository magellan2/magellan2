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

import magellan.library.TempUnit;
import magellan.library.event.TimeStampedEvent;

/**
 * An event indicating that a temporary unit was created or deleted.
 *
 * @see TempUnitListener
 * @see EventDispatcher
 */
public class TempUnitEvent extends TimeStampedEvent {
	/** An event indicating that a temp unit was created. */
	public static final int CREATED = 1;

	/** An event indicating that a temp unit is about to be deleted. */
	public static final int DELETING = 2;
  
	private magellan.library.TempUnit tempUnit = null;
	private int eventType = 0;

	/**
	 * Creates an event object.
	 *
	 * @param source the object that originated the event.
	 * @param temp the temporary unit affected by this event.
	 * @param type specifies whether the temp unit was created or is being deleted.
	 */
	public TempUnitEvent(Object source, magellan.library.TempUnit temp, int type) {
		super(source);
		this.tempUnit = temp;
		this.eventType = type;
	}

	/**
	 * Returns the temporary unit affected.
	 *
	 * 
	 */
	public TempUnit getTempUnit() {
		return tempUnit;
	}

	/**
	 * Returns whether the temp unit was created or deleted.
	 *
	 * 
	 */
	public int getType() {
		return eventType;
	}
}
