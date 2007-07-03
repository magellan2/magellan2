// class magellan.plugin.FFplugin.FFplugin
// created on 04.07.2007
//
// Copyright 2003-2007 by magellan project team
//
// Author : $Fiete: $
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
package magellan.plugin.FFplugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JMenuItem;

import magellan.client.Client;
import magellan.client.extern.MagellanPlugIn;
import magellan.library.GameData;
import magellan.library.utils.logging.Logger;

public class FFplugin implements MagellanPlugIn{
  private static Logger log = null;
  
  private Client client=null;
  private Properties properties = null;
  private GameData gd = null;
  
  /**
   * @see magellan.client.extern.MagellanPlugIn#init(magellan.client.Client, java.util.Properties)
   */
  public void init(Client _client, Properties _properties) {
    // init the plugin
    this.client = _client;
    this.properties = _properties;
    
    log = Logger.getInstance(FFplugin.class);
    log.info(getName()+" initialized...");
    
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#init(magellan.library.GameData)
   */
  public void init(GameData data) {
    // init the report
    this.gd=data;
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#getMenuItems()
   */
  public List<JMenuItem> getMenuItems() {
    List <JMenuItem> erg = new ArrayList<JMenuItem>();
    erg.add(new FFpluginMenu());
    return erg;
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#getName()
   */
  public String getName() {
    return "FFplugin";
  }
}
