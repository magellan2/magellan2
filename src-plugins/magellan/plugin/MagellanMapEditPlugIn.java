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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import magellan.client.Client;
import magellan.client.event.EventDispatcher;
import magellan.client.extern.MagellanPlugIn;
import magellan.client.swing.context.MapContextMenuProvider;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.client.utils.ErrorWindow;
import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.event.GameDataEvent;
import magellan.library.rules.ItemCategory;
import magellan.library.rules.ItemType;
import magellan.library.rules.RegionType;
import magellan.library.utils.MagellanFactory;
import magellan.library.utils.Resources;
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
  private JMenuItem delRegion = null;
  
  private JMenu setHerbs = null;
  
  private Region r = null;
  private CoordinateID c = null;
  
  private GameData data = null;
  private Client client = null;
  
  private final String regionTypeIdentifier = "regiontype.";
  private final String herbTypeIdentifier = "herbtype.";
  private final String unknownHerbType = "unknownHerb";
  private ArrayList<ItemType> herbTypes = new ArrayList<ItemType>();
  
  /**
   * Creates the Context-MenuItem (after Right-Click on Map)
   * 
   * @param dispatcher EventDispatcher
   * @param data GameData
   * 
   * @return The JMenuItem to show in the MapContextMenu
   */
  public JMenuItem createMapContextMenu(EventDispatcher dispatcher, GameData data) {
    rootTitle = new JMenu(Resources.get("mapedit.menu.title"));
    
    setName = new JMenuItem(Resources.get("mapedit.menu.setname"));
    setName.addActionListener(this);
    setName.setActionCommand("setName");
    rootTitle.add(setName);
    
    delName = new JMenuItem(Resources.get("mapedit.menu.delname"));
    delName.addActionListener(this);
    delName.setActionCommand("delName");
    rootTitle.add(delName);
    
    setTerrain = new JMenu(Resources.get("mapedit.menu.setterrain"));
    this.addTerrains(setTerrain);
    rootTitle.add(setTerrain);
    
    delRegion = new JMenuItem(Resources.get("mapedit.menu.delterrain"));
    delRegion.addActionListener(this);
    delRegion.setActionCommand("delRegion");
    rootTitle.add(delRegion);
    
    setHerbs = new JMenu(Resources.get("mapedit.menu.setherbs"));
    this.addHerbs(setHerbs);
    rootTitle.add(setHerbs);
    
    return rootTitle;
  }
  
  
  /**
   * adds all terrains to a JMenu
   * @param menu
   */
  private void addTerrains(JMenu menu){
    if (this.data==null || menu==null){
      return;
    }
    
    menu.removeAll();
    
    ArrayList<RegionType> types = new ArrayList<RegionType>();
    types.addAll(this.data.rules.getRegionTypes());
    
    Collections.sort(types,new RegionTypeComparator());
    
    for (Iterator iter = types.iterator();iter.hasNext();){
      RegionType rType = (RegionType)iter.next();
      JMenuItem toAdd = new JMenuItem(rType.getName());
      toAdd.setActionCommand(this.regionTypeIdentifier + rType.getID().toString());
      toAdd.addActionListener(this);
      toAdd.setIcon(client.getMagellanContext().getImageFactory().loadImageIcon(rType.getID().toString() + "-detail"));
      menu.add(toAdd);
    }
  }
  
  /**
   * adds all herbs to a JMenu
   * @param menu
   */
  private void addHerbs(JMenu menu){
    if (this.data==null || menu==null){
      return;
    }
    
    menu.removeAll();
    herbTypes.clear();
    
    // get the right ItemCat from Rules
    ItemCategory herbCat = this.data.rules.getItemCategory("herbs");
    ItemType kraeuterbeutelType = this.data.rules.getItemType("Kräuterbeutel"); 
    if (herbCat!=null){
       
       for (Iterator<ItemType> iter = this.data.rules.getItemTypeIterator();iter.hasNext();){
         ItemType type = iter.next();
         if (type.getCategory()!=null && type.getCategory().equals(herbCat)){
           if (kraeuterbeutelType!=null && !type.equals(kraeuterbeutelType)){
             herbTypes.add(type);
           }
         }
       }
       if (herbTypes.size()>0){
         Collections.sort(herbTypes,new HerbTypeComparator());
         for (ItemType type:herbTypes){
           JMenuItem toAdd = new JMenuItem(type.getName());
           toAdd.setActionCommand(this.herbTypeIdentifier + type.getID().toString());
           toAdd.addActionListener(this);
           toAdd.setIcon(client.getMagellanContext().getImageFactory().loadImageIcon("/items/" + type.getIconName()));
           menu.add(toAdd);  
         }
         menu.addSeparator();
         // unknwon
         JMenuItem toAdd = new JMenuItem(Resources.get("mapedit.menu." + this.unknownHerbType));
         toAdd.setActionCommand(this.herbTypeIdentifier + this.unknownHerbType);
         toAdd.addActionListener(this);
         toAdd.setIcon(client.getMagellanContext().getImageFactory().loadImageIcon("kraeuter.gif"));
         menu.add(toAdd);
       }
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
      this.setTerrain.setText(Resources.get("mapedit.menu.addterrain"));
      this.setTerrain.setEnabled(true);
      
      // Löschmöglichkeit
      this.delRegion.setEnabled(false);
      
      //  keine benannten Nixe
      this.setName.setEnabled(false);
      // nix nicht löschen können
      this.delName.setEnabled(false);
    } else {
      this.rootTitle.setText(Resources.get("mapedit.menu.title"));
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
      
      // if (r.getRegionType().getID().toString().equalsIgnoreCase("Ozean")){
      if  (r.getRegionType().isOcean()){
        // keine benannten Ozeane
        this.setName.setEnabled(false);
        // zombies löschen können
        // wenn name gesetzt
        if (r.getName()!=null && r.getName().length()>0){
          this.delName.setEnabled(true);
        } else {
          this.delName.setEnabled(false);
        }
        // definitiv keine Kräuter auf dem Ozean
        this.setHerbs.setEnabled(false);
        
      } else {
        // benannten Regionen
        this.setName.setEnabled(true);
        // keine Unbenannten LandRegionen
        this.delName.setEnabled(false);
        // Kräuter auch in Vulkanen...?!
        this.setHerbs.setEnabled(true);
      }
      
      // Terrainänderung
      this.setTerrain.setText(Resources.get("mapedit.menu.setterrain"));
      this.setTerrain.setEnabled(true);
      
      // Löschmöglichkeit
      this.delRegion.setEnabled(true);
      
    } else {
      this.rootTitle.setText(Resources.get("mapedit.menu.title"));
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
    return "MapEditPlugIn";
  }
  
  /**
   * Aktualisiert die lokale Referenz auf den Client
   * @see magellan.client.extern.MagellanPlugIn#init(magellan.client.Client, java.util.Properties)
   */
  public void init(Client client, Properties properties) {
    // init the plugin
    MagellanMapEditPlugIn.log = Logger.getInstance(MagellanMapEditPlugIn.class);
    Resources.getInstance().initialize(Client.getSettingsDirectory(),"mapedit_");
    MagellanMapEditPlugIn.log.info("MapEdit initialized...(client)");
    this.client = client;
    // System.err.println(Resources.get("mapedit.menu.setterrain"));
  }
  
  /**
   * aktualisiert die lokale referenz auf GameData
   * @see magellan.client.extern.MagellanPlugIn#init(magellan.library.GameData)
   */
  public void init(GameData data) {
    MagellanMapEditPlugIn.log.info("MapEdit initialized...(GameData)");
    this.data=data;
    this.addTerrains(this.setTerrain);
    this.addHerbs(this.setHerbs);
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#quit(boolean)
   */
  public void quit(boolean storeSettings) {
    // do nothing
  }

  /**
   * handels the event that one of our Items was selected
   * @param e the event
   */
  public void actionPerformed(ActionEvent e) {
    MagellanMapEditPlugIn.log.info(e.getActionCommand());
    if (e.getActionCommand().equalsIgnoreCase("delName")){
      this.runDelName();
    } else if (e.getActionCommand().equalsIgnoreCase("setName")){
      this.runSetName();
    } else if (e.getActionCommand().equalsIgnoreCase("delRegion")){
      this.runDelRegion();
    } else if (e.getActionCommand().startsWith(this.regionTypeIdentifier)){
      this.runSetTerrain(e.getActionCommand());
    } else if (e.getActionCommand().startsWith(this.herbTypeIdentifier)){
      this.runSetHerb(e.getActionCommand());
    }
      
  }
  
  /**
   * Name einer vorhandenen Region setzen
   *
   */
  private void runSetName(){
    if (this.r==null){
      MagellanMapEditPlugIn.log.error("MapEdit runSetName with no region");
      return;
    }
    String newName =  JOptionPane.showInputDialog(this.client, Resources.get("mapedit.input.newname"));
    if (newName!=null && newName.length()>0){
      // Nur bestimmte Zeichen zulassen (!?)
      if (Pattern.matches("[a-zA-Z0-9\\s´`']+", newName)){
        // alles fein
        r.setName(newName);
        this.updateClient();
      } else {
        // Passte wohl nicht
        ErrorWindow errorWindow = new ErrorWindow(
            Resources.get("mapedit.error.unwantedcharacter.message"),
              Resources.get("mapedit.error.unwantedcharacter.description"),null);
        errorWindow.setVisible(true);
      }
    } else {
      // keine Eingabe
    }
  }
  
  /**
   * Namen einer Region komplett löschen
   *
   */
  private void runDelName(){
    if (this.r==null){
      MagellanMapEditPlugIn.log.error("MapEdit runDelName with no region");
      return;
    }
    r.setName(null);
    this.updateClient();
  }
  
  /**
   * Eine Region aus GameData entfernen
   *
   */
  private void runDelRegion(){
    if (this.r==null){
      MagellanMapEditPlugIn.log.error("MapEdit runDelRegion with no region");
      return;
    }
    Region rGone = this.data.regions().remove(r.getID());
    if (rGone!=null){
      // ok..ist wech
      this.r = null;
      this.updateClient();
    } else {
      // konnte nicht entfernt werden
      MagellanMapEditPlugIn.log.error("MapEdit runDelRegion: removing region not successful");
    }
  }
  
  /**
   * Terrain einer vorhandenen Region ändern
   * bzw eine neue Region anlegen (mit gewähltem Terrain
   */
  private void runSetTerrain(String actionCommand){
    // zuerst RegionType organisieren und checken
    String regionTypeName = actionCommand.substring(this.regionTypeIdentifier.length());
    if (regionTypeName==null || regionTypeName.length()==0){
      MagellanMapEditPlugIn.log.error("MapEdit: runSetTerrain unknown Region Type Name");
      return;
    }
    
    // the Region Type to set
    RegionType setRegionType = null;
    
    ArrayList<RegionType> types = new ArrayList<RegionType>();
    types.addAll(this.data.rules.getRegionTypes());
    for (Iterator iter = types.iterator();iter.hasNext();){
      RegionType rType = (RegionType)iter.next();
      if (rType.getID().toString().equalsIgnoreCase(regionTypeName)){
        setRegionType = rType;
        break;
      }
    }
    
    if (setRegionType==null){
      MagellanMapEditPlugIn.log.error("MapEdit: RegionType not found:" + regionTypeName);
      return;
    }
    
    // the Region to work on
    Region workingRegion = r;
    if (workingRegion==null){
      // may be we have to create a new region here?
      if (this.c==null){
        // we can not create...
        MagellanMapEditPlugIn.log.error("MapEdit: can not create new Region!");
        return;
      } else {
        // alles fein, wir können loslegen
        // last check...kennen wir doch die region?
        workingRegion = this.data.getRegion(c);
        if (workingRegion==null){
          // ok, wirklich nicht da
          workingRegion = MagellanFactory.createRegion(c, this.data);
          // sortIndex setzen und erhöhen
          workingRegion.setSortIndex(this.data.getMaxSortIndex());
          // name setzen ?
          // hinzu!
          this.data.regions().put(workingRegion.getCoordinate(),workingRegion);
        }
      }
    }
    
    // the real terrain type setting
    workingRegion.setType(setRegionType);
    this.updateClient();
  }
  
  /**
   * Herb einer vorhandenen Region ändern
   * 
   */
  private void runSetHerb(String actionCommand){
    // zuerst RegionType organisieren und checken
    String herbName = actionCommand.substring(this.herbTypeIdentifier.length());
    if (herbName==null || herbName.length()==0){
      MagellanMapEditPlugIn.log.error("MapEdit: runSetherb unknown Herb Type Name");
      return;
    }
    
    // the Item Type to set
    ItemType setherbType = null;
    
    if (herbTypes!=null){
      for (ItemType type:herbTypes){
        if (type.getID().toString().equalsIgnoreCase(herbName)){
          setherbType = type;
          break;
        }
      }
    }
    if (setherbType==null && !herbName.equals(this.unknownHerbType)){
      MagellanMapEditPlugIn.log.error("MapEdit: herbType not found:" + herbName);
      return;
    }
    
    // the Region to work on
    Region workingRegion = r;
    if (workingRegion==null){
        MagellanMapEditPlugIn.log.error("MapEdit: can not work without region!");
        return;
    }
    
    if (herbName.equals(this.unknownHerbType)){
      workingRegion.setHerb(null);
    }
    if (setherbType!=null){
      // set the new (or old) herb
      workingRegion.setHerb(setherbType);
    }
    
    
    this.updateClient();
  }
  
  
  /**
   * fires the GameDataChanged Event to notify all Listeners
   * (refreshes map, details...all.)
   */
  private void updateClient(){
    if (this.client!=null){
      this.client.getDispatcher().fire(new GameDataEvent(this, this.data, true));
    }
  }
  
  
  
  // inner class //
  private class RegionTypeComparator implements Comparator<RegionType> {
    public int compare(RegionType arg0, RegionType arg1) {
      return arg0.getName().compareToIgnoreCase(arg1.getName());
    }
  }

  //inner class //
  private class HerbTypeComparator implements Comparator<ItemType> {
    public int compare(ItemType arg0, ItemType arg1) {
      return arg0.getName().compareToIgnoreCase(arg1.getName());
    }
  }
  
  
  /**
   * 
   */
  public PreferencesFactory getPreferencesProvider() {
    return null;
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#getDocks()
   */
  public Map<String, Component> getDocks() {
    return null;
  }
}
