// class magellan.plugin.battleanalyzer.BattelAnalyzerPlugIn
// created on Feb 5, 2012
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
package magellan.plugin.battleanalyzer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import magellan.client.Client;
import magellan.client.extern.MagellanPlugIn;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.library.GameData;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * Analyzes battle reports.
 * 
 * @author stm
 */
public class BattleAnalyzerPlugIn implements MagellanPlugIn {

  /**
   * An enum for all action types in this plugin.
   * 
   * @author stm
   */
  public enum PlugInAction {
    /** show the dock */
    SHOW("mainmenu.show"),
    /** unknown action */
    UNKNOWN("");

    private String id;

    private PlugInAction(String id) {
      this.id = id;
    }

    /** Returns the action ID */
    public String getID() {
      return id;
    }

    /** finds the action matching the event's action command */
    public static PlugInAction getAction(ActionEvent e) {
      if (e == null)
        return UNKNOWN;
      for (PlugInAction action : values()) {
        if (action.id.equalsIgnoreCase(e.getActionCommand()))
          return action;
      }
      return UNKNOWN;
    }
  }

  private static Logger log = null;

  @SuppressWarnings("unused")
  private Properties properties;

  private JMenuItem showMenu;

  private BattleDock dock;

  private ActionListener listener = new BattleListener();

  /**
   * @see magellan.client.extern.MagellanPlugIn#init(magellan.client.Client, java.util.Properties)
   */
  public void init(Client _client, Properties _properties) {
    // init the plugin
    properties = _properties;
    Resources.getInstance().initialize(Client.getResourceDirectory(), "battleanalyzer_");

    initProperties();

    dock = new BattleDock(_client, _properties);

    log = Logger.getInstance(BattleAnalyzerPlugIn.class);
    log.info(getName() + " initialized...");
  }

  private void initProperties() {
    // no properties so far
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#init(magellan.library.GameData)
   */
  public void init(GameData data) {
    // init the report
    dock.setWorld(data);
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#getMenuItems()
   */
  public List<JMenuItem> getMenuItems() {
    List<JMenuItem> items = new ArrayList<JMenuItem>();

    JMenu menu = new JMenu(getString("plugin.battle.mainmenu.title"));
    items.add(menu);

    showMenu = new JMenuItem(getString("plugin.battle.mainmenu.show.title"));
    showMenu.setActionCommand(PlugInAction.SHOW.getID());
    showMenu.setEnabled(true);
    showMenu.addActionListener(listener);
    menu.add(showMenu);

    return items;
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#getName()
   */
  public String getName() {
    return getString("plugin.battle.name");
  }

  class BattleListener implements ActionListener {
    public void actionPerformed(final ActionEvent e) {
      if (PlugInAction.getAction(e).equals(PlugInAction.SHOW)) {
        dock.restoreView();
        log.fine(e.getActionCommand());
      } else {
        log.warn(e.getActionCommand());
      }
    }
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#quit(boolean)
   */
  public void quit(boolean storeSettings) {
    // no settings; do nothing
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#getDocks()
   */
  public Map<String, Component> getDocks() {
    Map<String, Component> docks = new HashMap<String, Component>();
    docks.put(BattleDock.IDENTIFIER, dock);
    return docks;
  }

  protected static String getString(String key, Object... args) {
    String value = getString(key);
    if (value != null) {
      value = new MessageFormat(value).format(args);
    }
    return value;
  }

  protected static String getString(String key) {
    return Resources.get(key);
  }

  public PreferencesFactory getPreferencesProvider() {
    return null;
  }

}
