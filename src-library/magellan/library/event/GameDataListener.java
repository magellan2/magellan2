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

package magellan.library.event;

/**
 * The listener interface for receiving game data events. A class interested in game data events
 * implements this interface and registers with an instance of the EventDispatcher class to receive
 * game data events. Game data events are issued when the current game data object becomes invalid
 * e.g. after the user loads a report.
 * 
 * @see GameDataEvent
 * @see magellan.client.event.EventDispatcher
 */
public interface GameDataListener extends EventListener {
  /**
   * Invoked when the current game data object becomes invalid.
   */
  public void gameDataChanged(GameDataEvent e);
}
