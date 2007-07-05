// class magellan.plugin.MagellanMapEditPlugIn
// created on 05.07.2007
//
// Copyright 2003-2007 by magellan project team
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
package magellan.plugin;

import java.util.List;
import java.util.Properties;

import javax.swing.JMenuItem;

import magellan.client.Client;
import magellan.client.event.EventDispatcher;
import magellan.client.extern.MagellanPlugIn;
import magellan.client.swing.context.MapContextMenuProvider;
import magellan.library.GameData;
import magellan.library.Region;

/**
 * 
 * provides a MapContextMenu to edit the Map
 *
 * @author Fiete
 * @version 1.0, 05.07.2007
 */
public class MagellanMapEditPlugIn implements MagellanPlugIn,MapContextMenuProvider{
  
  private JMenuItem testTitle = null;
  
  
  /**
   * Creates the Context-MenuItem (after Right-Click on Map)
   * @param dispatcher EventDispatcher
   * @param data GameData
   * @param argument some object - should be a (clicked) region or regionwrapper
   * @param selectedObjects Collection of objects
   * @return The JMenuItem to show in the MapContextMenu
   */
  public JMenuItem createContextMenu(EventDispatcher dispatcher, GameData data) {
    testTitle = new JMenuItem("MapEdit present");
    return testTitle;
  }

  
  
  /**
   * update the PlugIn-Menu to the current region
   * @see magellan.client.swing.context.MapContextMenuProvider#update(magellan.library.Region)
   */
  public void update(Region r) {
    if (r!=null){
      this.testTitle.setText("MapEdit: " + r.toString());
    } else {
      this.testTitle.setText("MapEdit present");
    }
    
  }




  /**
   * kein Eintrag in Plugins -> no return here
   * @see magellan.client.extern.MagellanPlugIn#getMenuItems()
   */
  public List<JMenuItem> getMenuItems() {
    return null;
  }
  
  /**
   * kein Eintrag -> kein Name
   * @see magellan.client.extern.MagellanPlugIn#getName()
   */
  public String getName() {
    return null;
  }
  
  /**
   * kein Bedarf dieses Inits
   * @see magellan.client.extern.MagellanPlugIn#init(magellan.client.Client, java.util.Properties)
   */
  public void init(Client client, Properties properties) {
    
  }
  
  /**
   * kein Bedarf dieses Inits
   * @see magellan.client.extern.MagellanPlugIn#init(magellan.library.GameData)
   */
  public void init(GameData data) {
    
  }
  

}
