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

/**
 * The listener interface for receiving temp unit events. A class interested in temp unit events
 * implements this interface and registers with an instance of the EventDispatcher class to
 * receive such events. Temp unit events are issued when a temporary unit is created or deleted.
 *
 * @see TempUnitEvent
 * @see EventDispatcher
 */
public interface TempUnitListener {
	/**
	 * Invoked when a temporary unit is created.
	 */
	public void tempUnitCreated(TempUnitEvent e);

	/**
	 * Invoked when a temporary unit is deleted.
	 */
	public void tempUnitDeleted(TempUnitEvent e);
}
