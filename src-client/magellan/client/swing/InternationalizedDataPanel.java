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

import javax.swing.JPanel;

import magellan.client.MagellanContext;
import magellan.client.event.EventDispatcher;
import magellan.library.GameData;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;

/**
 * Common subclass for use as template for most magellan panel components.
 * 
 * @author $Author: $
 * @version $Revision: 242 $
 */
public abstract class InternationalizedDataPanel extends JPanel implements GameDataListener {
  private GameData data;
  protected Properties settings;
  protected EventDispatcher dispatcher;
  private MagellanContext context;

  /**
   * Creates a new InternationalizedDataPanel object. Adds this object as GameDataListener.
   */
  public InternationalizedDataPanel(EventDispatcher ed) {
    this(ed, new Properties());
  }

  /**
   * Creates a new InternationalizedDataPanel object. Adds this object as GameDataListener.
   */
  public InternationalizedDataPanel(EventDispatcher ed, Properties p) {
    this(ed, null, p);
  }

  /**
   * Creates a new InternationalizedDataPanel object. Adds this object as GameDataListener.
   */
  public InternationalizedDataPanel(EventDispatcher ed, GameData initData, Properties p) {
    // FIXME hmm...?!?
    this(ed.getMagellanContext());
  }

  /**
   * Creates a new InternationalizedDataPanel object. Adds this object as GameDataListener.
   */
  public InternationalizedDataPanel(MagellanContext context) {
    this.context = context;
    dispatcher = context.getEventDispatcher();

    if (dispatcher != null) {
      dispatcher.addGameDataListener(this);
    }

    setData(context.getGameData());
    settings = context.getProperties();
  }

  /**
   * @return the current MagellanContext
   */
  public MagellanContext getMagellanContext() {
    return context;
  }

  /**
   * Should be called if this dialog is no longer needed.
   */
  public void quit() {
    if (dispatcher != null) {
      dispatcher.removeGameDataListener(this);

      // remove stale listeners
      dispatcher.removeAllListeners(this);
    }
  }

  /**
   * @see magellan.library.event.GameDataListener#gameDataChanged(magellan.library.event.GameDataEvent)
   */
  public void gameDataChanged(GameDataEvent e) {
    setGameData(e.getGameData());
  }

  /**
   * Returns the value of data.
   * 
   * @return Returns data.
   */
  public GameData getGameData() {
    return getData();
  }

  /**
   * Sets the value of data.
   * 
   * @param data The value for data.
   */
  public void setGameData(GameData data) {
    this.setData(data);
  }

  /**
   * Returns the value of data.
   * 
   * @return Returns data.
   */
  protected GameData getData() {
    return data;
  }

  /**
   * Sets the value of data.
   *
   * @param data The value for data.
   */
  protected void setData(GameData data) {
    this.data = data;
  }

  // FIXME(stm) listeners almost never get properly removed. We could remove this from the
  // dispatcher
  // whenever this component is no longer used, but where?
  // @Override
  // public void removeNotify() {
  // super.removeNotify();
  // }
}
