// class magellan.plugin.ExtendedCommandsPlugIn
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
package magellan.plugin.extendedcommands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import magellan.client.Client;
import magellan.client.event.EventDispatcher;
import magellan.client.extern.MagellanPlugIn;
import magellan.client.swing.context.UnitContainerContextMenuProvider;
import magellan.client.swing.context.UnitContextMenuProvider;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * This is a magellan plugin. It makes it possible to save complex
 * commands for single units and load them everytime a report is
 * opened.
 * 
 * For example:
 *  - UnitA creates Wood
 *  - UnitB needs Wood for creating something else
 * 
 * So, UnitA needs a GIVE command every week but the Vorlage
 * doesn't want to show this command...
 *
 * @author Thoralf Rickert
 * @version 1.0, 28.05.2007
 */
public class ExtendedCommandsPlugIn implements MagellanPlugIn, UnitContextMenuProvider, UnitContainerContextMenuProvider, ActionListener {
  private static Logger log = null;
  private Client client = null;
  private static JMenuItem executeMenu = null;
  private ExtendedCommands commands = null;
  
  /**
   * @see magellan.client.extern.MagellanPlugIn#init(magellan.client.Client, java.util.Properties)
   */
  public void init(Client client, Properties properties) {
    // init the plugin
    log = Logger.getInstance(ExtendedCommandsPlugIn.class);
    Resources.getInstance().initialize(Client.getSettingsDirectory(),"extendedcommands_");
    this.client = client;
    this.commands = new ExtendedCommands(client);
    log.info(getName()+" initialized...(Client)");
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#init(magellan.library.GameData)
   */
  public void init(GameData data) {
    // init the report
    log.info(getName()+" initialized...(GameData)");
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#getMenuItems()
   */
  public List<JMenuItem> getMenuItems() {
    List<JMenuItem> items = new ArrayList<JMenuItem>();
    
    JMenu menu = new JMenu(Resources.get("mainmenu.title"));
    items.add(menu);
    
    executeMenu = new JMenuItem(Resources.get("mainmenu.execute.title"));
    executeMenu.setActionCommand(PlugInAction.EXECUTE_ALL.getID());
    executeMenu.addActionListener(this);
    executeMenu.setEnabled(commands.hasCommands());
    menu.add(executeMenu);    

    JMenuItem configureMenu = new JMenuItem(Resources.get("mainmenu.configure.title"));
    configureMenu.setActionCommand(PlugInAction.CONFIGURE_ALL.getID());
    configureMenu.addActionListener(this);
    configureMenu.setEnabled(false);
    menu.add(configureMenu);    

    return items;
  }
  
  public static JMenuItem getExecuteMenu() {
    return executeMenu;
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#getName()
   */
  public String getName() {
    return "ExtendedComands";
  }

  /**
   * @see magellan.client.swing.context.UnitContextMenuProvider#createContextMenu(magellan.client.event.EventDispatcher, magellan.library.GameData, java.lang.Object, java.util.Collection)
   */
  public JMenuItem createContextMenu(final EventDispatcher dispatcher, final GameData data, final Unit unit, Collection selectedObjects) {
    JMenu menu = new JMenu(Resources.get("popupmenu.title"));
    
    JMenuItem editMenu = new JMenuItem(Resources.get("popupmenu.edit.title", new Object[]{unit.getName(),unit.getID().toString()}));
    editMenu.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        editCommands(data,unit);
      }
    });
    menu.add(editMenu);
    
    JMenuItem executeMenu = new JMenuItem(Resources.get("popupmenu.execute.title", new Object[]{unit.getName(),unit.getID().toString()})); 
    executeMenu.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        executeCommands(data,unit);
      }
    });
    executeMenu.setEnabled(commands.hasCommands(unit));
    menu.add(executeMenu);
    
    return menu;
  }
  
  /**
   * @see magellan.client.swing.context.UnitContainerContextMenuProvider#createContextMenu(magellan.client.event.EventDispatcher, magellan.library.GameData, magellan.library.UnitContainer)
   */
  public JMenuItem createContextMenu(EventDispatcher dispatcher, final GameData data, final UnitContainer container) {
    ContainerType type = ContainerType.getType(container.getType());
    if (type.equals(ContainerType.UNKNOWN)) {
      log.error("Unknown containertype "+container.getType());
      return null;
    }
    
    JMenu menu = new JMenu(Resources.get("popupmenu.title"));
    
    JMenuItem editMenu = new JMenuItem(Resources.get("popupmenu.edit.title", new Object[]{container.getName(),container.getID().toString()}));
    editMenu.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        editCommands(data,container);
      }
    });
    menu.add(editMenu);
    
    JMenuItem executeMenu = new JMenuItem(Resources.get("popupmenu.execute.title", new Object[]{container.getName(),container.getID().toString()})); 
    executeMenu.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        executeCommands(data,container);
      }
    });
    executeMenu.setEnabled(commands.hasCommands(container));
    menu.add(executeMenu);
    
    return menu;
  }
  
  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    log.info(e.getActionCommand());
    switch (PlugInAction.getAction(e)) {
      case EXECUTE_ALL: {
        log.info("Execute all...");
        executeCommands(client.getData());
        break;
      }
      case CONFIGURE_ALL: {
        log.info("Configure...");
        // TODO must be implemented.... 
        break;
      }
    }
  }
  
  /**
   * @see magellan.client.extern.MagellanPlugIn#quit(boolean)
   */
  public void quit(boolean storeSettings) {
    commands.save();
  }

  /**
   * Opens a Dialog for editing the commands for the given Unitcontainer.
   */
  protected void editCommands(GameData data, UnitContainer container) {
    log.info("Edit Command for UnitContainer "+container);
    
    // find the commands for this unit or set them to "".
    String script = commands.getCommands(container);
    if (script == null) {
      // show some examples for beginners...
      script = "// example for beginners...\n";
      script+= "//\n";
      script+= "//...";
    }
    
    // open a dialog for the commands...
    ExtendedCommandsDialog dialog = new ExtendedCommandsDialog(client,data,commands,container,script);
    dialog.setVisible(true);
  }

  /**
   * Opens a Dialog for editing the commands for the given Unit.
   */
  protected void editCommands(GameData data, Unit unit) {
    log.info("Edit Command for Unit "+unit);
    
    // find the commands for this unit or set them to "".
    String script = commands.getCommands(unit);
    if (script == null) {
      // show some examples for beginners...
      script = "// example for beginners...\n";
      script+= "//\n";
      script+= "//if (!unit.isOrdersConfirmed()) {\n";
      script+= "//  unit.setOrdersConfirmed(true);\n";
      script+= "//}\n";
      
    }
    
    // open a dialog for the commands...
    ExtendedCommandsDialog dialog = new ExtendedCommandsDialog(client,data,commands,unit,script);
    dialog.setVisible(true);
  }
  
  protected void executeCommands(GameData data) {
    log.info("Executing commands for all configured units...");
    List<Unit> units = commands.getUnitsWithCommands();
    for (Unit unit : units) {
      commands.execute(data, unit);
    }
    List<UnitContainer> containers = commands.getUnitContainersWithCommands();
    for (UnitContainer container : containers) {
      commands.execute(data, container);
    }
  }

  /**
   * Executes the commands for a given unitcontainer.
   */
  protected void executeCommands(GameData data, UnitContainer container) {
    log.info("Execute Command for UnitContainer "+container);
    
    // find the commands for this unit.
    commands.execute(data, container);
    
    container.getCache().orderEditor.reloadOrders();
    
  }
  
  /**
   * Executes the commands for a given unit.
   */
  protected void executeCommands(GameData data, Unit unit) {
    log.info("Execute Command for Unit "+unit);
    
    // find the commands for this unit.
    commands.execute(data, unit);
    
    unit.getCache().orderEditor.reloadOrders();
    unit.getCache().orderEditor.fireOrdersChangedEvent();
  }
  
  /**
   * @see magellan.client.extern.MagellanPlugIn#getPreferencesProvider()
   */
  public PreferencesFactory getPreferencesProvider() {
    // later we need a dialog for the library files.
    return null;
  }

}
