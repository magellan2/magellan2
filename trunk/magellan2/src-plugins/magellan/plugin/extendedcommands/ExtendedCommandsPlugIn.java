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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
package magellan.plugin.extendedcommands;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import magellan.client.Client;
import magellan.client.desktop.DesktopEnvironment;
import magellan.client.desktop.ShortcutListener;
import magellan.client.event.EventDispatcher;
import magellan.client.extern.MagellanPlugIn;
import magellan.client.swing.ProgressBarUI;
import magellan.client.swing.context.UnitContainerContextMenuProvider;
import magellan.client.swing.context.UnitContextMenuProvider;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.event.GameDataEvent;
import magellan.library.utils.Resources;
import magellan.library.utils.Utils;
import magellan.library.utils.logging.Logger;

/**
 * This is a magellan plugin. It makes it possible to save complex commands for single units and
 * load them every time a report is opened. For example: - UnitA creates Wood - UnitB needs Wood for
 * creating something else So, UnitA needs a GIVE command every week but the Vorlage doesn't want to
 * show this command...
 * 
 * @author Thoralf Rickert
 * @version 1.0, 28.05.2007
 */
public class ExtendedCommandsPlugIn implements MagellanPlugIn, UnitContextMenuProvider,
    UnitContainerContextMenuProvider, ActionListener, ShortcutListener {
  private static Logger log = null;
  private Client client = null;
  private static JMenuItem executeMenu = null;
  private ExtendedCommands commands = null;
  private ExtendedCommandsDock dock = null;
  private HelpDock help = null;

  private List<KeyStroke> shortcuts;

  /**
   * @see magellan.client.extern.MagellanPlugIn#init(magellan.client.Client, java.util.Properties)
   */
  public void init(Client client, Properties properties) {
    // init the plugin
    ExtendedCommandsPlugIn.log = Logger.getInstance(ExtendedCommandsPlugIn.class);
    Resources.getInstance().initialize(Client.getSettingsDirectory(), "extendedcommands_");
    this.client = client;
    commands = new ExtendedCommands(client);
    initCommands();
    dock = new ExtendedCommandsDock(commands);
    client.getDispatcher().addSelectionListener(dock);
    help = new HelpDock();
    ExtendedCommandsPlugIn.log.debug(getName() + " initialized...(Client)");

    // initialize shortcuts
    shortcuts = new ArrayList<KeyStroke>(2);
    // 0: Focus
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_MASK));
    // 1: open current
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_MASK
        | InputEvent.SHIFT_MASK));
    // register for shortcuts
    DesktopEnvironment.registerShortcutListener(this);
  }

  private void initCommands() {
    String exampleScript = "// example for beginners...\n";
    exampleScript += "// \n";
    exampleScript += "// import magellan.library.*;\n";
    exampleScript += "// \n";
    exampleScript += "// int getHorses(Region region) {\n";
    exampleScript += "// return region.getHorses();\n";
    exampleScript += "// }\n";
    exampleScript += "// check the Magellan web pages for more examples...";
    exampleScript += "//\n";
    commands.setDefaultLibrary(exampleScript);

    exampleScript = "// example for beginners...\n";
    exampleScript += "//\n";
    exampleScript += "//if (!unit.isOrdersConfirmed()) {\n";
    exampleScript += "//  unit.setOrdersConfirmed(true);\n";
    exampleScript += "//}\n";
    exampleScript += "// check the Magellan web pages for more examples...";
    commands.setDefaultUnitScript(exampleScript);

    exampleScript = "// example for beginners...\n";
    exampleScript += "//\n";
    exampleScript += "// for (Unit u : container.units()) {\n";
    exampleScript += "//   helper.addOrder(u, \"; gotcha\");\n";
    exampleScript += "//   helper.updateUnit(u);\n";
    exampleScript += "// }\n";
    exampleScript += "//\n";
    exampleScript += "// check the Magellan web pages for more examples...";
    commands.setDefaultContainerScript(exampleScript);
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#init(magellan.library.GameData)
   */
  public void init(GameData data) {
    // init the report
    ExtendedCommandsPlugIn.log.debug(getName() + " initialized...(GameData)");
    dock.setWorld(data);
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#getMenuItems()
   */
  public List<JMenuItem> getMenuItems() {
    List<JMenuItem> items = new ArrayList<JMenuItem>();

    JMenu menu = new JMenu(Resources.get("extended_commands.mainmenu.title"));
    items.add(menu);

    ExtendedCommandsPlugIn.executeMenu =
        new JMenuItem(Resources.get("extended_commands.mainmenu.execute.title"));
    ExtendedCommandsPlugIn.executeMenu.setActionCommand(PlugInAction.EXECUTE_ALL.getID());
    ExtendedCommandsPlugIn.executeMenu.addActionListener(this);
    ExtendedCommandsPlugIn.executeMenu.setEnabled(commands.hasCommands());
    menu.add(ExtendedCommandsPlugIn.executeMenu);

    JMenuItem libraryMenu =
        new JMenuItem(Resources.get("extended_commands.mainmenu.library.title"));
    libraryMenu.setActionCommand(PlugInAction.LIBRARY_EDIT.getID());
    libraryMenu.addActionListener(this);
    menu.add(libraryMenu);

    JMenuItem clearMenu = new JMenuItem(Resources.get("extended_commands.mainmenu.clear.title"));
    clearMenu.setActionCommand(PlugInAction.CLEAR.getID());
    clearMenu.addActionListener(this);
    menu.add(clearMenu);

    menu.addSeparator();

    JMenuItem saveMenu = new JMenuItem(Resources.get("extended_commands.mainmenu.save.title"));
    saveMenu.setActionCommand(PlugInAction.SAVE_ALL.getID());
    saveMenu.addActionListener(this);
    menu.add(saveMenu);

    JMenuItem exportMenu = new JMenuItem(Resources.get("extended_commands.mainmenu.export.title"));
    exportMenu.setActionCommand(PlugInAction.EXPORT.getID());
    exportMenu.addActionListener(this);
    exportMenu.setEnabled(false);
    menu.add(exportMenu);

    JMenuItem importMenu = new JMenuItem(Resources.get("extended_commands.mainmenu.import.title"));
    importMenu.setActionCommand(PlugInAction.IMPORT.getID());
    importMenu.addActionListener(this);
    importMenu.setEnabled(false);
    menu.add(importMenu);

    return items;
  }

  public static JMenuItem getExecuteMenu() {
    return ExtendedCommandsPlugIn.executeMenu;
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#getName()
   */
  public String getName() {
    return "ExtendedCommands";
  }

  /**
   * @see magellan.client.swing.context.UnitContextMenuProvider#createContextMenu(magellan.client.event.EventDispatcher,
   *      magellan.library.GameData, magellan.library.Unit, java.util.Collection)
   */
  public JMenuItem createContextMenu(final EventDispatcher dispatcher, final GameData data,
      final Unit unit, Collection<?> selectedObjects) {
    JMenu menu = new JMenu(Resources.get("extended_commands.popupmenu.title"));

    JMenuItem editMenu =
        new JMenuItem(Resources.get("extended_commands.popupmenu.edit.title", new Object[] {
            unit.getName(), unit.getID().toString(),
            KeyEvent.getKeyModifiersText(shortcuts.get(1).getModifiers()),
            KeyEvent.getKeyText(shortcuts.get(1).getKeyCode()) }));
    editMenu.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        editCommands(data, unit);
      }
    });
    menu.add(editMenu);

    JMenuItem executeMenu =
        new JMenuItem(Resources.get("extended_commands.popupmenu.execute.title", new Object[] {
            unit.getName(), unit.getID().toString() }));
    executeMenu.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        executeCommands(data, unit);
      }
    });
    executeMenu.setEnabled(commands.hasCommands(unit));
    menu.add(executeMenu);

    return menu;
  }

  /**
   * @see magellan.client.swing.context.UnitContainerContextMenuProvider#createContextMenu(EventDispatcher,
   *      GameData, UnitContainer, Collection)
   */
  public JMenuItem createContextMenu(EventDispatcher dispatcher, final GameData data,
      final UnitContainer container, final Collection<?> selectedObjects) {
    ContainerType type = ContainerType.getType(container.getType());
    if (type.equals(ContainerType.UNKNOWN)) {
      ExtendedCommandsPlugIn.log.error("Unknown containertype " + container.getType());
      return null;
    }

    JMenu menu = new JMenu(Resources.get("extended_commands.popupmenu.title"));

    JMenuItem editMenu =
        new JMenuItem(Resources.get("extended_commands.popupmenu.edit.title", new Object[] {
            container.getName(), container.getID().toString() }));
    editMenu.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        editCommands(data, container);
      }
    });
    menu.add(editMenu);

    JMenuItem executeMenu =
        new JMenuItem(Resources.get("extended_commands.popupmenu.execute.title", new Object[] {
            container.getName(), container.getID().toString() }));
    executeMenu.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        executeCommands(data, container);
      }
    });
    executeMenu.setEnabled(commands.hasCommands(container));
    menu.add(executeMenu);

    JMenuItem executeAllMenu =
        new JMenuItem(Resources.get("extended_commands.popupmenu.executeall.title", new Object[] {
            container.getName(), container.getID().toString() }));
    executeAllMenu.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        executeAllCommands(data, container);
      }
    });
    executeAllMenu.setEnabled(commands.hasAllCommands(data, container));
    menu.add(executeAllMenu);

    return menu;
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    ExtendedCommandsPlugIn.log.debug(e.getActionCommand());
    switch (PlugInAction.getAction(e)) {
    case EXECUTE_ALL: {
      ExtendedCommandsPlugIn.log.info("Execute all...");
      ProgressBarUI progress = new ProgressBarUI(Client.INSTANCE);
      ExecutionThread thread = new ExecutionThread(client, progress, commands);
      thread.start();
      break;
    }
    case LIBRARY_EDIT: {
      ExtendedCommandsPlugIn.log.debug("Edit library...");
      editLibrary(client.getData());
      break;
    }
    case CLEAR: {
      ExtendedCommandsPlugIn.log.info("Clear unused scripts...");
      clearCommands();
      break;
    }
    case SAVE_ALL: {
      ExtendedCommandsPlugIn.log.debug("Saving...");
      commands.save();
      break;
    }
    case EXPORT: {
      ExtendedCommandsPlugIn.log.debug("Exporting commands...");
      break;
    }
    case IMPORT: {
      ExtendedCommandsPlugIn.log.debug("import commands...");
      break;
    }
    case UNKNOWN:
      ExtendedCommandsPlugIn.log.debug("unknown command...");
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
    ExtendedCommandsPlugIn.log.debug("Edit Command for UnitContainer " + container);

    // find the commands for this container or set them to "".
    Script script = commands.getCommands(container);
    if (Utils.isEmpty(script)) {
      script = commands.createScript(container);
    }

    // open a dialog for the commands...
    // ExtendedCommandsDialog dialog = new
    // ExtendedCommandsDialog(client,data,commands,container,script);
    // dialog.setVisible(true);
    dock.setScript(null, container, script);

  }

  /**
   * Opens a Dialog for editing the commands for the given Unit.
   */
  protected void editCommands(GameData data, Unit unit) {
    ExtendedCommandsPlugIn.log.debug("Edit Command for Unit " + unit);

    // find the commands for this unit or set them to "".
    Script script = commands.getCommands(unit);
    if (Utils.isEmpty(script)) {
      script = commands.createScript(unit);
    }

    // open a dialog for the commands...
    // ExtendedCommandsDialog dialog = new ExtendedCommandsDialog(client,data,commands,unit,script);
    // dialog.setVisible(true);
    dock.setScript(unit, null, script);
  }

  /**
   * Opens a Dialog for editing the library.
   */
  protected void editLibrary(GameData data) {
    ExtendedCommandsPlugIn.log.debug("Edit library for all units and containers...");

    // find the commands for this unit or set them to "".
    Script script = commands.getLibrary();
    if (Utils.isEmpty(script)) {
      script = commands.createLibrary(data);
    }

    // open a dialog for the commands...
    // ExtendedCommandsDialog dialog = new ExtendedCommandsDialog(client,data,commands,script);
    // dialog.setVisible(true);
    dock.setScript(null, null, script);
  }

  protected void clearCommands() {
    if (JOptionPane.showConfirmDialog(client, Resources.get("extended_commands.cleanup.question"),
        Resources.get("extended_commands.cleanup.title"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
      return;
    int units = commands.clearUnusedUnits();
    int containers = commands.clearUnusedContainers();

    JOptionPane.showMessageDialog(client, Resources.get("extended_commands.cleanup.result", units,
        containers), Resources.get("extended_commands.cleanup.title"),
        JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Executes the commands for a given unitcontainer.
   */
  protected void executeCommands(GameData data, UnitContainer container) {
    ExtendedCommandsPlugIn.log.info("Execute Command for UnitContainer " + container);

    // execute the commands for this container.
    commands.execute(data, container);

    client.getDispatcher().fire(new GameDataEvent(this, data));
  }

  /**
   * Executes the commands for a given unitcontainer.
   */
  protected void executeAllCommands(GameData data, UnitContainer container) {
    ExtendedCommandsPlugIn.log.info("Execute Command for UnitContainer " + container);

    // execute the commands for this unit.
    if (commands.hasCommands(container)) {
      commands.execute(data, container);
    }

    Collection<Unit> units = container.units();
    if (units != null && units.size() > 0) {
      for (Unit unit : units) {
        if (commands.hasCommands(unit)) {
          // execute the commands for this unit.
          ExtendedCommandsPlugIn.log.info("Execute Command for Unit " + unit);
          commands.execute(data, unit);
        }
      }
    }

    // client notification is done from commands.execute
    // client.getDispatcher().fire(new GameDataEvent(this, data));
    // client.getDispatcher().fire(new UnitOrdersEvent(this, unit));
    // container.getCache().orderEditor.reloadOrders();

  }

  /**
   * Executes the commands for a given unit.
   */
  protected void executeCommands(GameData data, Unit unit) {
    ExtendedCommandsPlugIn.log.info("Execute Command for Unit " + unit);

    // execute the commands for this unit.
    commands.execute(data, unit);

    unit.getOrderEditor().reloadOrders();
    unit.getOrderEditor().fireOrdersChangedEvent();
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#getPreferencesProvider()
   */
  public PreferencesFactory getPreferencesProvider() {
    // later we need a dialog for the library files.
    return null;
  }

  /**
   * @see magellan.client.extern.MagellanPlugIn#getDocks()
   */
  public Map<String, Component> getDocks() {
    Map<String, Component> docks = new HashMap<String, Component>();
    docks.put(ExtendedCommandsDock.IDENTIFIER, dock);
    docks.put(HelpDock.IDENTIFIER, help);
    return docks;
  }

  /**
   * @see magellan.client.desktop.ShortcutListener#getListenerDescription()
   */
  public String getListenerDescription() {
    return Resources.get("extended_commands.shortcuts.title");
  }

  /**
   * @see magellan.client.desktop.ShortcutListener#getShortCuts()
   */
  public Iterator<KeyStroke> getShortCuts() {
    return shortcuts.iterator();
  }

  /**
   * @see magellan.client.desktop.ShortcutListener#shortCut(javax.swing.KeyStroke)
   */
  public void shortCut(javax.swing.KeyStroke shortcut) {
    int index = shortcuts.indexOf(shortcut);

    switch (index) {
    case 0:
      DesktopEnvironment.requestFocus(ExtendedCommandsDock.IDENTIFIER);
      break;
    case 1:
      dock.openCurrent();
      break;
    default:
      break;
    }
  }

  /**
   * @see magellan.client.desktop.ShortcutListener#getShortcutDescription(KeyStroke)
   */
  public String getShortcutDescription(KeyStroke stroke) {
    int index = shortcuts.indexOf(stroke);

    return Resources.get("extended_commands.shortcut.description." + String.valueOf(index));
  }

}
