// class magellan.library.event.EventDispatcherInterface
// created on Jun 7, 2012
//
// Copyright 2003-2012 by magellan project team
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
package magellan.library.utils.guiwrapper;

import java.util.EventObject;

import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;

/**
 * A class forwarding events from event sources to listeners.
 */
public interface EventDispatcherInterface {

  /**
   * Adds a listener for game data events.
   *
   * @param l the listener to add.
   * @see GameDataEvent
   */
  public abstract void addGameDataListener(GameDataListener l);

  /**
   * Adds the given game-data listener to the front of all registered listeners. Warning: The order
   * will change if another listener is added with priority.
   */
  public abstract void addPriorityGameDataListener(GameDataListener l);

  /**
   * Removes the specified listener for game data events.
   *
   * @param l the listener to remove.
   * @return true if this list contained the specified element.
   * @see GameDataEvent
   */
  public abstract boolean removeGameDataListener(GameDataListener l);

  /**
   * Removes the specified listener from all event queues
   *
   * @param o the listener to remove.
   * @return true if one of the list contained the specified element.
   */
  public abstract boolean removeAllListeners(Object o);

  /**
   * Forwards an event to all registered listeners for this event type.
   * <p>
   * If synchronous is false, the forwarding is done asynchronously in a separate dispatcher thread.
   * If the fire method is called before the dispatcher thread has finished the previous request, it
   * is stopped and starts forwarding the new event.
   * </p>
   */
  public abstract void fire(EventObject e, boolean synchronous);

  /**
   * Asynchronously forwards an event to all registered listeners for this event type.
   */
  public abstract void fire(EventObject e);

  /**
   * Returns the number of events that were passed to this dispatcher for forwarding.
   */
  public abstract int getEventsFired();

  /**
   * Returns the number of events that were actually forwarded to event listeners.
   */
  public abstract int getEventsDispatched();

}