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
 * The listener interface for receiving selection events. A class interested in selection events
 * implements this interface and registers with an instance of the EventDispatcher class to
 * receive such events. Selection events are issued when the user selects or activates some
 * objects informing components to display detailed information about these objects.
 *
 * @see SelectionEvent
 * @see EventDispatcher
 */
public interface SelectionListener<T> {
	/**
	 * Invoked when different objects are activated or selected.
	 */
	public void selectionChanged(SelectionEvent<T> e);
}
