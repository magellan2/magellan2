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
 * provides a MapContextMenu to edit the Map
 * 
 * @author Fiete
 * @version 1.0, 05.07.2007
 */
public class MagellanMapEditPlugIn implements MagellanPlugIn, MapContextMenuProvider,
    ActionListener {
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
   * @param data2 GameData
   * @return The JMenuItem to show in the MapContextMenu
   */
  @Override
  public JMenuItem createMapContextMenu(EventDispatcher dispatcher, GameData data2) {
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
    addTerrains(setTerrain);
    rootTitle.add(setTerrain);

    delRegion = new JMenuItem(Resources.get("mapedit.menu.delterrain"));
    delRegion.addActionListener(this);
    delRegion.setActionCommand("delRegion");
    rootTitle.add(delRegion);

    setHerbs = new JMenu(Resources.get("mapedit.menu.setherbs"));
    addHerbs(setHerbs);
    rootTitle.add(setHerbs);

    return rootTitle;
  }

  /**
   * adds all terrains to a JMenu
   * 
   * @param menu
   */
  private void addTerrains(JMenu menu) {
    if (data == null || menu == null)
      return;

    menu.removeAll();

    ArrayList<RegionType> types = new ArrayList<RegionType>();
    types.addAll(data.getRules().getRegionTypes());

    Collections.sort(types, new RegionTypeComparator());

    for (RegionType rType : types) {
      JMenuItem toAdd = new JMenuItem(rType.getName());
      toAdd.setActionCommand(regionTypeIdentifier + rType.getID().toString());
      toAdd.addActionListener(this);
      toAdd.setIcon(client.getMagellanContext().getImageFactory().loadImageIcon(
          rType.getIcon() + "-detail"));
      menu.add(toAdd);
    }
  }

  /**
   * adds all herbs to a JMenu
   * 
   * @param menu
   */
  private void addHerbs(JMenu menu) {
    if (data == null || menu == null)
      return;

    menu.removeAll();
    herbTypes.clear();

    // get the right ItemCat from Rules
    ItemCategory herbCat = data.getRules().getItemCategory("herbs");
    ItemType kraeuterbeutelType = data.getRules().getItemType("Kräuterbeutel");
    if (herbCat != null) {

      for (Iterator<ItemType> iter = data.getRules().getItemTypeIterator(); iter.hasNext();) {
        ItemType type = iter.next();
        if (type.getCategory() != null && type.getCategory().equals(herbCat)) {
          if (kraeuterbeutelType != null && !type.equals(kraeuterbeutelType)) {
            herbTypes.add(type);
          }
        }
      }
      if (herbTypes.size() > 0) {
        Collections.sort(herbTypes, new HerbTypeComparator());
        for (ItemType type : herbTypes) {
          JMenuItem toAdd = new JMenuItem(type.getName());
          toAdd.setActionCommand(herbTypeIdentifier + type.getID().toString());
          toAdd.addActionListener(this);
          toAdd.setIcon(client.getMagellanContext().getImageFactory().loadImageIcon(
              "/items/" + type.getIcon()));
          menu.add(toAdd);
        }
        menu.addSeparator();
        // unknwon
        JMenuItem toAdd = new JMenuItem(Resources.get("mapedit.menu." + unknownHerbType));
        toAdd.setActionCommand(herbTypeIdentifier + unknownHerbType);
        toAdd.addActionListener(this);
        toAdd.setIcon(client.getMagellanContext().getImageFactory().loadImageIcon("kraeuter.gif"));
        menu.add(toAdd);
      }
    }

  }

  /**
   * update the plugin menu and react to a click on an non-region area
   * 
   * @see magellan.client.swing.context.MapContextMenuProvider#updateUnknownRegion(magellan.library.CoordinateID)
   */
  @Override
  public void updateUnknownRegion(CoordinateID c2) {
    if (c2 != null) {
      c = c2;
      r = null;
      rootTitle.setText("MapEdit: NEW " + c2.toString());
      rootTitle.setEnabled(true);
      // Terrainänderung
      setTerrain.setText(Resources.get("mapedit.menu.addterrain"));
      setTerrain.setEnabled(true);

      // Löschmöglichkeit
      delRegion.setEnabled(false);

      // keine benannten Nixe
      setName.setEnabled(false);
      // nix nicht löschen können
      delName.setEnabled(false);
    } else {
      rootTitle.setText(Resources.get("mapedit.menu.title"));
      rootTitle.setEnabled(false);
    }
  }

  /**
   * update the PlugIn-Menu to the current region
   * 
   * @see magellan.client.swing.context.MapContextMenuProvider#update(magellan.library.Region)
   */
  @Override
  public void update(Region newRegion) {
    if (newRegion != null) {
      r = newRegion;
      rootTitle.setText("MapEdit: " + newRegion.toString());
      rootTitle.setEnabled(true);

      // if (r.getRegionType().getID().toString().equalsIgnoreCase("Ozean")){
      if (newRegion.getRegionType().isOcean()) {
        // keine benannten Ozeane
        setName.setEnabled(false);
        // zombies löschen können
        // wenn name gesetzt
        if (newRegion.getName() != null && newRegion.getName().length() > 0) {
          delName.setEnabled(true);
        } else {
          delName.setEnabled(false);
        }
        // definitiv keine Kräuter auf dem Ozean
        setHerbs.setEnabled(false);

      } else {
        // benannten Regionen
        setName.setEnabled(true);
        // keine Unbenannten LandRegionen
        delName.setEnabled(false);
        // Kräuter auch in Vulkanen...?!
        setHerbs.setEnabled(true);
      }

      // Terrainänderung
      setTerrain.setText(Resources.get("mapedit.menu.setterrain"));
      setTerrain.setEnabled(true);

      // Löschmöglichkeit
      delRegion.setEnabled(true);

    } else {
      rootTitle.setText(Resources.get("mapedit.menu.title"));
      rootTitle.setEnabled(false);
    }

  }

  /**
   * kein Eintrag in Plugins &rarr; no return here
   * 
   * @see magellan.client.extern.MagellanPlugIn#getMenuItems()
   */
  @Override
  public List<JMenuItem> getMenuItems() {
    return null;
  }

  /**
   * kein Eintrag &rarr; kein Name
   * 
   * @see magellan.client.extern.MagellanPlugIn#getName()
   */
  @Override
  public String getName() {
    return "MapEditPlugIn";
  }

  /**
   * Aktualisiert die lokale Referenz auf den Client
   * 
   * @see magellan.client.extern.MagellanPlugIn#init(magellan.client.Client, java.util.Properties)
   */
  @Override
  public void init(@SuppressWarnings("hiding") Client client, Properties properties) {
    // init the plugin
    MagellanMapEditPlugIn.log = Logger.getInstance(MagellanMapEditPlugIn.class);
    Resources.getInstance().initialize(Client.getResourceDirectory(), "mapedit_");
    MagellanMapEditPlugIn.log.fine("MapEdit initialized...(client)");
    this.client = client;
    // System.err.println(Resources.get("mapedit.menu.setterrain"));
  }

  /**
   * aktualisiert die lokale referenz auf GameData
   * 
   * @see magellan.client.extern.MagellanPlugIn#init(magellan.library.GameData)
   */
  @Override
  public void init(@SuppressWarnings("hiding") GameData data) {
    MagellanMapEditPlugIn.log.fine("MapEdit initialized...(GameData)");
    this.data = data;
    addTerrains(setTerrain);
    addHerbs(setHerbs);
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#quit(boolean)
   */
  @Override
  public void quit(boolean storeSettings) {
    // do nothing
  }

  /**
   * handels the event that one of our Items was selected
   * 
   * @param e the event
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    MagellanMapEditPlugIn.log.info(e.getActionCommand());
    if (e.getActionCommand().equalsIgnoreCase("delName")) {
      runDelName();
    } else if (e.getActionCommand().equalsIgnoreCase("setName")) {
      runSetName();
    } else if (e.getActionCommand().equalsIgnoreCase("delRegion")) {
      runDelRegion();
    } else if (e.getActionCommand().startsWith(regionTypeIdentifier)) {
      runSetTerrain(e.getActionCommand());
    } else if (e.getActionCommand().startsWith(herbTypeIdentifier)) {
      runSetHerb(e.getActionCommand());
    }

  }

  /**
   * Name einer vorhandenen Region setzen
   */
  private void runSetName() {
    if (r == null) {
      MagellanMapEditPlugIn.log.error("MapEdit runSetName with no region");
      return;
    }
    String newName = JOptionPane.showInputDialog(client, Resources.get("mapedit.input.newname"));
    if (newName != null && newName.length() > 0) {
      // Nur bestimmte Zeichen zulassen (!?)
      if (Pattern.matches("[a-zA-Z0-9\\s´`']+", newName)) {
        // alles fein
        r.setName(newName);
        updateClient();
      } else {
        // Passte wohl nicht
        ErrorWindow errorWindow =
            new ErrorWindow(Resources.get("mapedit.error.unwantedcharacter.message"), Resources
                .get("mapedit.error.unwantedcharacter.description"), null);
        errorWindow.setVisible(true);
      }
    } else {
      // keine Eingabe
    }
  }

  /**
   * Namen einer Region komplett löschen
   */
  private void runDelName() {
    if (r == null) {
      MagellanMapEditPlugIn.log.error("MapEdit runDelName with no region");
      return;
    }

    r.setName(null);
    updateClient();
  }

  /**
   * Eine Region aus GameData entfernen
   */
  private void runDelRegion() {
    if (r == null) {
      MagellanMapEditPlugIn.log.error("MapEdit runDelRegion with no region");
      return;
    }

    Map<CoordinateID, Region> selection = client.getSelectedRegions();
    if (selection.containsKey(r.getCoordinate())) {
      int result = JOptionPane.showConfirmDialog(client, "delete the selection?");
      if (result == JOptionPane.YES_OPTION) {
        for (Region r : selection.values()) {
          if (data.removeRegion(r) == null) {
            MagellanMapEditPlugIn.log.error("MapEdit runDelRegion: removing region not successful");
          }
        }

        updateClient();
      } else if (result == JOptionPane.NO_OPTION) {
        Region rGone = data.removeRegion(r);
        if (rGone != null) {
          // ok..ist wech
          r = null;
          updateClient();
        } else {
          // konnte nicht entfernt werden
          MagellanMapEditPlugIn.log.error("MapEdit runDelRegion: removing region not successful");
        }
      }
    } else {
      Region rGone = data.removeRegion(r);
      if (rGone != null) {
        // ok..ist wech
        r = null;
        updateClient();
      } else {
        // konnte nicht entfernt werden
        MagellanMapEditPlugIn.log.error("MapEdit runDelRegion: removing region not successful");
      }
    }
  }

  /**
   * Terrain einer vorhandenen Region ändern bzw eine neue Region anlegen (mit gewähltem Terrain
   */
  private void runSetTerrain(String actionCommand) {
    // zuerst RegionType organisieren und checken
    String regionTypeName = actionCommand.substring(regionTypeIdentifier.length());
    if (regionTypeName == null || regionTypeName.length() == 0) {
      MagellanMapEditPlugIn.log.error("MapEdit: runSetTerrain unknown Region Type Name");
      return;
    }

    // the Region Type to set
    RegionType setRegionType = null;

    ArrayList<RegionType> types = new ArrayList<RegionType>();
    types.addAll(data.getRules().getRegionTypes());
    for (RegionType rType : types) {
      if (rType.getID().toString().equalsIgnoreCase(regionTypeName)) {
        setRegionType = rType;
        break;
      }
    }

    if (setRegionType == null) {
      MagellanMapEditPlugIn.log.error("MapEdit: RegionType not found:" + regionTypeName);
      return;
    }

    // the Region to work on
    Region workingRegion = r;
    if (workingRegion == null) {
      // may be we have to create a new region here?
      if (c == null) {
        // we can not create...
        MagellanMapEditPlugIn.log.error("MapEdit: can not create new Region!");
        return;
      } else {
        // alles fein, wir können loslegen
        // last check...kennen wir doch die region?
        workingRegion = data.getRegion(c);
        if (workingRegion == null) {
          // ok, wirklich nicht da
          workingRegion = MagellanFactory.createRegion(c, data);
          // sortIndex setzen und erhöhen
          workingRegion.setSortIndex(data.getMaxSortIndex());
          // name setzen ?
          // hinzu!
          data.addRegion(workingRegion);
          // data.regions().put(workingRegion.getCoordinate(), workingRegion);
        }
      }
    }

    // the real terrain type setting
    workingRegion.setType(setRegionType);
    updateClient();
  }

  /**
   * Herb einer vorhandenen Region ändern
   */
  private void runSetHerb(String actionCommand) {
    // zuerst RegionType organisieren und checken
    String herbName = actionCommand.substring(herbTypeIdentifier.length());
    if (herbName == null || herbName.length() == 0) {
      MagellanMapEditPlugIn.log.error("MapEdit: runSetherb unknown Herb Type Name");
      return;
    }

    // the Item Type to set
    ItemType setherbType = null;

    if (herbTypes != null) {
      for (ItemType type : herbTypes) {
        if (type.getID().toString().equalsIgnoreCase(herbName)) {
          setherbType = type;
          break;
        }
      }
    }
    if (setherbType == null && !herbName.equals(unknownHerbType)) {
      MagellanMapEditPlugIn.log.error("MapEdit: herbType not found:" + herbName);
      return;
    }

    // the Region to work on
    Region workingRegion = r;
    if (workingRegion == null) {
      MagellanMapEditPlugIn.log.error("MapEdit: can not work without region!");
      return;
    }

    if (herbName.equals(unknownHerbType)) {
      workingRegion.setHerb(null);
      workingRegion.setHerbAmount(null);
    }
    if (setherbType != null) {
      // set the new (or old) herb
      workingRegion.setHerb(setherbType);
    }

    updateClient();
  }

  /**
   * fires the GameDataChanged Event to notify all Listeners (refreshes map, details...all.)
   */
  private void updateClient() {
    if (client != null) {
      client.getDispatcher().fire(new GameDataEvent(this, data, true));
    }
  }

  // inner class //
  private static class RegionTypeComparator implements Comparator<RegionType> {
    public int compare(RegionType arg0, RegionType arg1) {
      return arg0.getName().compareToIgnoreCase(arg1.getName());
    }
  }

  // inner class //
  private static class HerbTypeComparator implements Comparator<ItemType> {
    public int compare(ItemType arg0, ItemType arg1) {
      return arg0.getName().compareToIgnoreCase(arg1.getName());
    }
  }

  /**
   * Returns <code>null</code>
   * 
   * @see magellan.client.extern.MagellanPlugIn#getPreferencesProvider()
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
