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

package magellan.client.swing;

import java.awt.Frame;
import java.util.Properties;

import magellan.client.event.EventDispatcher;
import magellan.library.GameData;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;


/**
 * A dialog that is a GameDataListener.
 *
 */
public abstract class InternationalizedDataDialog extends InternationalizedDialog
	implements GameDataListener
{
	protected GameData data = null;
	protected Properties settings = null;
	protected EventDispatcher dispatcher = null;

	/**
	 * Creates a new InternationalizedDataDialog object. Adds this object as gameDataListener to the dispatcher.
	 *
	 * @param owner the <code>Frame</code> from which the dialog is displayed
	 * @param modal <code>true</code> for a modal dialog, false for one that allows others windows to be 
	 * active at the same time
	 * @param ed The event dispatcher that this dialog should use
	 * @param initData The corresponding GameData
	 * @param p The corresponding properties
	 */
	public InternationalizedDataDialog(Frame owner, boolean modal, EventDispatcher ed,
									   GameData initData, Properties p) {
		super(owner, modal);
		this.dispatcher = ed;

		if(this.dispatcher != null) {
			this.dispatcher.addGameDataListener(this);
		}

		this.data = initData;
		this.settings = p;
	}

	/**
	 * @see magellan.library.event.GameDataListener#gameDataChanged(magellan.library.event.GameDataEvent)
	 */
	public void gameDataChanged(GameDataEvent e) {
		this.data = e.getGameData();
	}

  /**
   * Removes this instance as listener from the dispatcher. 
   * 
   * @see java.awt.Window#dispose()
   */
  @Override
  public void dispose() {
    if(dispatcher != null) {
      // remove stale listeners
      dispatcher.removeAllListeners(this);
    }
    super.dispose();
  }
	
	/**
	 * Returns the current GameData this dialog works with.
	 * 
	 * @return the data
	 */
	protected GameData getData() {
		return data;
	}

	/**
	 * Returns the dispatcher
	 */
	protected EventDispatcher getDispatcher() {
		return dispatcher;
	}

	/**
	 * Returns the settings.
	 */
	protected Properties getSettings() {
		return settings;
	}
}
