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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import magellan.client.Client;
import magellan.client.event.EventDispatcher;
import magellan.client.extern.MagellanPlugIn;
import magellan.client.swing.context.MapContextMenuProvider;
import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.rules.RegionType;
import magellan.library.utils.logging.Logger;

/**
 * 
 * provides a MapContextMenu to edit the Map
 *
 * @author Fiete
 * @version 1.0, 05.07.2007
 */
public class MagellanMapEditPlugIn implements MagellanPlugIn,MapContextMenuProvider,ActionListener{
  private static Logger log = null;
  
  private JMenu rootTitle = null;
  private JMenuItem setName = null;
  private JMenuItem delName = null;
  private JMenu setTerrain = null;
  private JMenuItem delTerrain = null;
  
  private Region r = null;
  private CoordinateID c = null;
  
  private GameData data = null;
  private Client client = null;
  
  
  /**
   * Creates the Context-MenuItem (after Right-Click on Map)
   * @param dispatcher EventDispatcher
   * @param data GameData
   * @param argument some object - should be a (clicked) region or regionwrapper
   * @param selectedObjects Collection of objects
   * @return The JMenuItem to show in the MapContextMenu
   */
  public JMenuItem createContextMenu(EventDispatcher dispatcher, GameData data) {
    rootTitle = new JMenu("MapEdit present");
    
    setName = new JMenuItem("set name");
    rootTitle.add(setName);
    
    delName = new JMenuItem("del name");
    rootTitle.add(delName);
    
    setTerrain = new JMenu("set terrain");
    this.addTerrains(setTerrain);
    rootTitle.add(setTerrain);
    
    delTerrain = new JMenuItem("del terrain");
    rootTitle.add(delTerrain);
    return rootTitle;
  }
  
  
  /**
   * adds all terrains to a JMenu
   * @param menu
   */
  private void addTerrains(JMenu menu){
    if (this.data==null){
      return;
    }
    ArrayList<RegionType> types = new ArrayList<RegionType>();
    types.addAll(this.data.rules.getRegionTypes());
    
    Collections.sort(types,new RegionTypeComparator());
    
    for (Iterator iter = types.iterator();iter.hasNext();){
      RegionType rType = (RegionType)iter.next();
      JMenuItem toAdd = new JMenuItem(rType.getName());
      toAdd.setActionCommand("regiontype." + rType.getID().toString());
      toAdd.addActionListener(this);
      menu.add(toAdd);
    }
  }
  
  
  /**
   * update the plugin menu and react to a click on an 
   * non-region area
   * @see magellan.client.swing.context.MapContextMenuProvider#updateUnknownRegion(magellan.library.CoordinateID)
   */
  public void updateUnknownRegion(CoordinateID c) {
    if (c!=null){
      this.c = c;
      this.rootTitle.setText("MapEdit: NEW " + c.toString());
      this.rootTitle.setEnabled(true);
      // Terrainänderung
      this.setTerrain.setText("add terrain");
      this.setTerrain.setEnabled(true);
      
      // Löschmöglichkeit
      this.delTerrain.setEnabled(false);
      
      //  keine benannten Nixe
      this.setName.setEnabled(false);
      // nix nicht löschen können
      this.delName.setEnabled(false);
    } else {
      this.rootTitle.setText("MapEdit present");
      this.rootTitle.setEnabled(false);
    }
  }



  /**
   * update the PlugIn-Menu to the current region
   * @see magellan.client.swing.context.MapContextMenuProvider#update(magellan.library.Region)
   */
  public void update(Region r) {
    if (r!=null){
      this.r = r;
      this.rootTitle.setText("MapEdit: " + r.toString());
      this.rootTitle.setEnabled(true);
      
      if (r.getRegionType().getID().toString().equalsIgnoreCase("Ozean")){
        // keine benannten Ozeane
        this.setName.setEnabled(false);
        // zombies löschen können
        // wenn name gesetzt
        if (r.getName()!=null && r.getName().length()>0){
          this.delName.setEnabled(true);
        } else {
          this.delName.setEnabled(false);
        }
      } else {
        // benannten Regionen
        this.setName.setEnabled(true);
        // keine Unbenannten LandRegionen
        this.delName.setEnabled(false);
      }
      
      // Terrainänderung
      this.setTerrain.setText("set terrain");
      this.setTerrain.setEnabled(true);
      
      // Löschmöglichkeit
      this.delTerrain.setEnabled(true);
      
    } else {
      this.rootTitle.setText("MapEdit present");
      this.rootTitle.setEnabled(false);
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
   * Aktualisiert die lokale Referenz auf den Client
   * @see magellan.client.extern.MagellanPlugIn#init(magellan.client.Client, java.util.Properties)
   */
  public void init(Client client, Properties properties) {
//  init the plugin
    log = Logger.getInstance(MagellanMapEditPlugIn.class);
    log.info("MapEdit initialized...(client)");
    this.client = client;
  }
  
  /**
   * aktualisiert die lokale referenz auf GameData
   * @see magellan.client.extern.MagellanPlugIn#init(magellan.library.GameData)
   */
  public void init(GameData data) {
    log.info("MapEdit initialized...(GameData)");
    this.data=data;
    this.addTerrains(this.setTerrain);
  }

  
  /**
   * handels the event that one of our Items was selected
   * @param e the event
   */
  public void actionPerformed(ActionEvent e) {
    log.info(e.getActionCommand());
  }
  // inner class //
  private class RegionTypeComparator implements Comparator<RegionType> {
    public int compare(RegionType arg0, RegionType arg1) {
      return arg0.getName().compareToIgnoreCase(arg1.getName());
    }
    
  }
}
