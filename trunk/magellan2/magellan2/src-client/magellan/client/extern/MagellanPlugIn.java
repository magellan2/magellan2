// class magellan.client.extern.MagellanPlugIn
// created on 28.05.2007
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
package magellan.client.extern;

import java.util.List;
import java.util.Properties;

import javax.swing.JMenuItem;

import magellan.client.Client;
import magellan.library.GameData;

/**
 * This is the interface that a Magellan PlugIn has to
 * implement in order to be available in the Magellan
 * Client.
 *
 * @author Thoralf Rickert
 * @author Fiete
 * @version 1.0, 28.05.2007
 */
public interface MagellanPlugIn {
  /**
   * Returns the Name of the PlugIn. This name will
   * be presented to the user in the options panel.
   */
  public String getName();
  
  /**
   * This method is called during client start up
   * procedure. You can use this method to initialize
   * your PlugIn (load preferences and so on...)
   * 
   * @param client     the main application
   * @param properties the already loaded configuration
   */
  public void init(Client client, Properties properties);
  
  /**
   * This method is called everytime the user has load a
   * file into Magellan (open or add). You should use
   * this method to load report specific informations.
   * 
   * @param data the loaded and merged gamedata
   */
  public void init(GameData data);
  
  /**
   * Returns the menu items that should be added to the
   * Magellan PlugIn menu. You can return multiple menu
   * items for every kind of action that is available
   * in your PlugIn.
   */
  public List<JMenuItem> getMenuItems();
  
  /**
   * This method is called whenever the application
   * stops.
   */
  public void quit(boolean storeSettings);
}
