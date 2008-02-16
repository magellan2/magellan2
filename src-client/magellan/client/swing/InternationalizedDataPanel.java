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

import java.util.Properties;

import magellan.client.MagellanContext;
import magellan.client.event.EventDispatcher;
import magellan.library.GameData;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 242 $
 */
public abstract class InternationalizedDataPanel extends InternationalizedPanel implements GameDataListener {
  protected GameData data = null;
  protected Properties settings = null;
  protected EventDispatcher dispatcher = null;
  private MagellanContext context;

  /**
   * Creates a new InternationalizedDataPanel object.
   */
  public InternationalizedDataPanel(EventDispatcher ed) {
    this(ed, new Properties());
  }

  /**
   * Creates a new InternationalizedDataPanel object.
   */
  public InternationalizedDataPanel(EventDispatcher ed, Properties p) {
    this(ed, null, p);
  }

  /**
   * Creates a new InternationalizedDataPanel object.
   */
  public InternationalizedDataPanel(EventDispatcher ed, GameData initData, Properties p) {
    this(ed.getMagellanContext());
  }

  public InternationalizedDataPanel(MagellanContext context) {
    this.context = context;
    this.dispatcher = context.getEventDispatcher();

    if (this.dispatcher != null) {
      this.dispatcher.addGameDataListener(this);
    }

    this.data = context.getGameData();
    this.settings = context.getProperties();
  }

  /**
   * @return the current MagellanContext
   */
  public MagellanContext getMagellanContext() {
    return context;
  }

  /**
   * DOCUMENT-ME
   */
  public void quit() {
    if (this.dispatcher != null) {
      dispatcher.removeGameDataListener(this);

      // remove stale listeners
      dispatcher.removeAllListeners(this);
    }
  }

  /**
   * @see magellan.library.event.GameDataListener#gameDataChanged(magellan.library.event.GameDataEvent)
   */
  public void gameDataChanged(GameDataEvent e) {
    this.data = e.getGameData();
  }

  /**
   * Returns the value of data.
   * 
   * @return Returns data.
   */
  public GameData getGameData() {
    return data;
  }

  /**
   * Sets the value of data.
   *
   * @param data The value for data.
   */
  public void setGameData(GameData data) {
    this.data = data;
  }
}
