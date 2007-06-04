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
 * The listener interface for receiving events when the orders of one or more units are confirmed
 * or disconfirmed. A class interested in order confirm events implements this interface and
 * registers with an instance of the EventDispatcher class to receive order confirm events.
 *
 * @see OrderConfirmEvent
 * @see EventDispatcher
 */
public interface OrderConfirmListener {
	/**
	 * Invoked when the order confirmation status of one or more units changes.
	 */
	public void orderConfirmationChanged(OrderConfirmEvent e);
}
