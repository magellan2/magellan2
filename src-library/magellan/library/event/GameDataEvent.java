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

import magellan.library.GameData;

/**
 * An event indicating that the previous game data object is no longer valid e.g. after the user
 * loaded a report.
 *
 * @see GameDataListener
 * @see magellan.client.event.EventDispatcher
 */
public class GameDataEvent extends TimeStampedEvent {
	private GameData data;
	private boolean isLoaded;

	/**
	 * Creates an GameDataEvent object.
	 *
	 * @param source the object that originated the event.
	 * @param data the new game data object.
	 */
	public GameDataEvent(Object source, GameData data) {
		this(source, data, false);
	}

	/**
	 * Creates an GameDataEvent object.
	 *
	 * @param source the object that originated the event.
	 * @param data the new game data object.
	 * @param isLoaded the new isLoaded state of the GameDataEvent.
	 */
	public GameDataEvent(Object source, GameData data, boolean isLoaded) {
		super(source);
		this.data = data;
		this.isLoaded = isLoaded;
	}

	/**
	 * Returns the new valid game data object.
	 *
	 * @return the new game data object
	 */
	public GameData getGameData() {
		return data;
	}

	/**
	 * Returns true if game data is freshly loaded (in contrast to  a GameDataEvent with a changed
	 * game data object that is only thrown if there are so many changes that single
	 * OrderConfirmEvent/ TempUnitEvent/UnitOrdersEvent) would bloat the event queue).
	 *
	 * 
	 */
	public boolean isLoaded() {
		return isLoaded;
	}
}
