// class magellan.plugin.groupeditor.GroupEditorPlugIn
// created on 25.09.2008
//
// Copyright 2003-2008 by magellan project team
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
package magellan.plugin.groupeditor;

import java.awt.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JMenuItem;

import magellan.client.Client;
import magellan.client.extern.MagellanPlugIn;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.library.GameData;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * This plugin allows it to control the state of all faction to a group based
 * on a table layout. This plugin provides a dock which contains a table. The
 * column contains the groups. The rows are the factions. In the cells you can
 * find the help state of a group for a faction.
 *
 * @author Thoralf Rickert
 * @version 1.0, 25.09.2008
 */
public class GroupEditorPlugIn implements MagellanPlugIn {
  private static Logger log = null; 
  private GroupEditorDock dock = null;

  /**
   * @see magellan.client.extern.MagellanPlugIn#init(magellan.client.Client, java.util.Properties)
   */
  public void init(Client client, Properties properties) {
    GroupEditorPlugIn.log = Logger.getInstance(GroupEditorPlugIn.class);
    Resources.getInstance().initialize(Client.getSettingsDirectory(),"groupeditor_");
    this.dock = new GroupEditorDock(client);
    log.info(getName()+" initialized...(Client)");
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#init(magellan.library.GameData)
   */
  public void init(GameData data) {
    dock.setWorld(data);
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#getDocks()
   */
  public Map<String, Component> getDocks() {
    Map<String, Component> docks = new HashMap<String, Component>();
    docks.put(GroupEditorDock.IDENTIFIER, dock);
    return docks;
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#getMenuItems()
   */
  public List<JMenuItem> getMenuItems() {
    return null;
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#getName()
   */
  public String getName() {
    return "GroupEditor";
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#getPreferencesProvider()
   */
  public PreferencesFactory getPreferencesProvider() {
    return null;
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#quit(boolean)
   */
  public void quit(boolean storeSettings) {
    // is ignored
  }

}
