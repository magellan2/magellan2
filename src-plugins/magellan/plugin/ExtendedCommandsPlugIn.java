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
package magellan.plugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import bsh.Interpreter;

import magellan.client.Client;
import magellan.client.event.EventDispatcher;
import magellan.client.extern.MagellanPlugIn;
import magellan.client.swing.context.UnitContainerContextMenuProvider;
import magellan.client.swing.context.UnitContextMenuProvider;
import magellan.client.utils.ErrorWindow;
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.GameData;
import magellan.library.Item;
import magellan.library.Skill;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.UnitID;
import magellan.library.rules.BuildingType;
import magellan.library.rules.Race;
import magellan.library.rules.RegionType;
import magellan.library.rules.ShipType;
import magellan.library.rules.UnitContainerType;
import magellan.library.utils.Encoding;
import magellan.library.utils.Resources;
import magellan.library.utils.Utils;
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
  private JMenuItem executeMenu = null;
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
   * An enum for all action types in this plugin.
   *
   * @author Thoralf Rickert
   * @version 1.0, 11.09.2007
   */
  private enum PlugInAction {
    EXECUTE_ALL("mainmenu.execute"),
    CONFIGURE_ALL("mainmenu.configure"),
    UNKNOWN("");
    
    private String id;
    
    private PlugInAction(String id) {
      this.id = id;
    }
    
    public String getID() {
      return id;
    }
    
    public static PlugInAction getAction(ActionEvent e) {
      if (e == null) return UNKNOWN;
      for (PlugInAction action : values()) {
        if (action.id.equalsIgnoreCase(e.getActionCommand())) return action;
      }
      return UNKNOWN;
    }
  }

  /**
   * An enum for all container types in this plugin.
   *
   * @author Thoralf Rickert
   * @version 1.0, 11.09.2007
   */
  private enum ContainerType {
    REGIONTYPE,
    RACE,
    BUILDINGTYPE,
    SHIPTYPE,
    UNKNOWN;
    
    public String toString() {
      return name().toLowerCase();
    }
    
    public static ContainerType getType(String name) {
      if (Utils.isEmpty(name)) return UNKNOWN;
      for (ContainerType type : values()) {
        if (type.toString().equalsIgnoreCase(name)) return type;
      }
      return UNKNOWN;
    }
    
    public static ContainerType getType(UnitContainerType uctype) {
      if (Utils.isEmpty(uctype)) return UNKNOWN;
      if (uctype instanceof RegionType) return REGIONTYPE;
      if (uctype instanceof Race) return RACE;
      if (uctype instanceof BuildingType) return BUILDINGTYPE;
      if (uctype instanceof ShipType) return SHIPTYPE;
      return UNKNOWN;
    }
    
    
  }
  

  /**
   * This class holds the commands for all units.
   *
   * @author Thoralf Rickert
   * @version 1.0, 11.09.2007
   */
  class ExtendedCommands {
    private Logger log = Logger.getInstance(ExtendedCommands.class);
    private File unitCommandsFile;
    private Client client;
    private Hashtable<String, String> unitCommands = new Hashtable<String, String>();
    
    private Hashtable<String, String> unitContainerCommands = new Hashtable<String, String>();
    private Hashtable<String, ContainerType> unitContainerTypes = new Hashtable<String, ContainerType>();
    
    
    public ExtendedCommands(Client client) {
      this.client = client;
      Properties properties = client.getProperties();
      String commandsFilename = properties.getProperty("extendedcommands.unitCommands");
      if (Utils.isEmpty(commandsFilename)) commandsFilename = "extendedcommands.xml";
      
      unitCommandsFile = new File(commandsFilename);
      if (!unitCommandsFile.exists()) {
        // file doesn't exist. create it with an empty set.
        write(unitCommandsFile);
      }
      
      read(unitCommandsFile);
    }
    
    /**
     * Returns true if there are any commands set.
     */
    public boolean hasCommands() {
      return unitCommands.size() > 0 || unitContainerCommands.size() > 0;
    }
    
    /**
     * Returns true if there is a command for the given unitcontainer.
     */
    public boolean hasCommands(UnitContainer container) {
      if (!hasCommands()) return false;
      if (container == null) return false;
      if (container.getID() == null) return false;
      if (!unitContainerCommands.containsKey(container.getID().toString())) return false;
      return true;
    }
    
    /**
     * Returns true if there is a command for the given unit.
     */
    public boolean hasCommands(Unit unit) {
      if (!hasCommands()) return false;
      if (unit == null) return false;
      if (unit instanceof TempUnit) return false;
      if (unit.getID() == null) return false;
      if (!unitCommands.containsKey(unit.getID().toString())) return false;
      return true;
    }
    
    /**
     * Returns the command script for the given unitcontainer.
     */
    public String getCommands(UnitContainer container) {
      if (!hasCommands(container)) return null;
      return unitContainerCommands.get(container.getID().toString());
    }
    
    /**
     * Returns the command script for the given unit.
     */
    public String getCommands(Unit unit) {
      if (!hasCommands(unit)) return null;
      return unitCommands.get(unit.getID().toString());
    }
    
    /**
     * Sets the commands for a given unit.
     */
    public void setCommands(Unit unit, String script) {
      if (unit == null) return;
      if (unit instanceof TempUnit) return;
      if (unit.getID() == null) return;
      
      if (Utils.isEmpty(script) && hasCommands(unit)) {
        unitCommands.remove(unit.getID().toString());
      } else {
        unitCommands.put(unit.getID().toString(), script);
      }
      
      executeMenu.setEnabled(hasCommands());
    }
    
    /**
     * Sets the commands for a given unit.
     */
    public void setCommands(UnitContainer container, String script) {
      if (container == null) return;
      if (container.getID() == null) return;
      
      if (Utils.isEmpty(script) && hasCommands(container)) {
        unitContainerCommands.remove(container.getID().toString());
      } else {
        unitContainerCommands.put(container.getID().toString(), script);
        unitContainerTypes.put(container.getID().toString(), ContainerType.getType(container.getType()));
      }
      
      executeMenu.setEnabled(hasCommands());
    }
    
    /**
     * Returns a list of all units with commands.
     */
    public List<Unit> getUnitsWithCommands() {
      List<Unit> units = new ArrayList<Unit>();
      GameData world = client.getData();
      
      for (String unitId : unitCommands.keySet()) {
        Unit unit = world.getUnit(UnitID.createUnitID(unitId,world.base));
        if (unit != null) units.add(unit);
      }
      
      return units;
    }
    
    /**
     * Returns a list of all unitcontainerss with commands.
     */
    public List<UnitContainer> getUnitContainersWithCommands() {
      List<UnitContainer> containers = new ArrayList<UnitContainer>();
      GameData world = client.getData();
      
      for (String unitContainerId : unitContainerCommands.keySet()) {
        switch (unitContainerTypes.get(unitContainerId)) {
          case REGIONTYPE: {
            UnitContainer container = world.getRegion(CoordinateID.parse(unitContainerId, ", "));
            if (container != null) containers.add(container);
            break;
          }
          case RACE: {
            UnitContainer container = world.getFaction(EntityID.createEntityID(unitContainerId,world.base));
            if (container != null) containers.add(container);
            break;
          }
          case BUILDINGTYPE: {
            UnitContainer container = world.getBuilding(EntityID.createEntityID(unitContainerId,world.base));
            if (container != null) containers.add(container);
            break;
          }
          case SHIPTYPE: {
            UnitContainer container = world.getShip(EntityID.createEntityID(unitContainerId,world.base));
            if (container != null) containers.add(container);
            break;
          }
        }
      }
      
      return containers;
    }
    
    
    /**
     * Executes the commands/script for a given unit.
     */
    public void execute(GameData world, Unit unit) {
      if (!hasCommands(unit)) return;
      
      try {
        Interpreter interpreter = new Interpreter();
        interpreter.set("world",world);
        interpreter.set("unit",unit);
        interpreter.set("helper", new ExtendedCommandHelper(world,unit));
        interpreter.eval(getCommands(unit));
        unit.setOrdersChanged(true);
      } catch (Throwable throwable) {
        log.error("",throwable);
        ErrorWindow errorWindow = new ErrorWindow(client,throwable.getMessage(),"Problems with unit "+unit.getName()+" ("+unit.getID()+")\n.See script:\n"+getCommands(unit),throwable);
        errorWindow.setShutdownOnCancel(false);
        errorWindow.setVisible(true);
      }
    }
    
    /**
     * Executes the commands/script for a given unitcontainer.
     */
    public void execute(GameData world, UnitContainer container) {
      if (!hasCommands(container)) return;
      
      try {
        Interpreter interpreter = new Interpreter();
        interpreter.set("world",world);
        interpreter.set("container",container);
        interpreter.set("helper", new ExtendedCommandHelper(world,container));
        interpreter.eval(getCommands(container));
      } catch (Throwable throwable) {
        log.error("",throwable);
        ErrorWindow errorWindow = new ErrorWindow(client,throwable.getMessage(),"Problems with unit "+container.getName()+" ("+container.getID()+")\n.See script:\n"+getCommands(container),throwable);
        errorWindow.setShutdownOnCancel(false);
        errorWindow.setVisible(true);
      }
    }
    
    /**
     * Reads a file with commands
     */
    protected void read(File file) {
      try {
        log.info("Reading XML "+file);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(false);
        DocumentBuilder builder  = factory.newDocumentBuilder();
        Document document = builder.parse(file);
        Element rootNode = document.getDocumentElement();
        if (!rootNode.getNodeName().equalsIgnoreCase("extended_commands")) {
          log.error("This is NOT an extended command configuration file");
          return;
        }
        NodeList nodes = rootNode.getElementsByTagName("container");
        log.info("Found "+nodes.getLength()+" unitcontainer commands");
        for( int i=0; i<nodes.getLength(); i++ ) {
          Element node = (Element)nodes.item(i);
          String id = node.getAttribute("id");
          String type = node.getAttribute("type");
          String command = node.getFirstChild().getNodeValue();
          unitContainerCommands.put(id,command);
          unitContainerTypes.put(id, ContainerType.getType(type));
        }
        
        nodes = rootNode.getElementsByTagName("unit");
        log.info("Found "+nodes.getLength()+" unit commands");
        for( int i=0; i<nodes.getLength(); i++ ) {
          Element node = (Element)nodes.item(i);
          String id = node.getAttribute("id");
          String command = node.getFirstChild().getNodeValue();
          unitCommands.put(id,command);
        }
        
      } catch (Throwable throwable) {
        log.error("",throwable);
        ErrorWindow errorWindow = new ErrorWindow(client,throwable.getMessage(),"",throwable);
        errorWindow.setShutdownOnCancel(false);
        errorWindow.setVisible(true);
      }
    }
    
    /**
     *
     */
    public void save() {
      write(unitCommandsFile);
    }
    
    /**
     * Writes the commands
     */
    protected void write(File file) {
      try {
        FileOutputStream fos = new FileOutputStream(file);
        OutputStreamWriter osw = new OutputStreamWriter(fos,Encoding.UTF8.toString());
        PrintWriter writer = new PrintWriter(osw);
        
        writer.println("<?xml version=\"1.0\" encoding=\""+Encoding.UTF8.toString()+"\"?>");
        writer.println("<extended_commands>");
        for (String unitContainerId : unitContainerCommands.keySet()) {
          writer.println(" <container id=\""+unitContainerId+"\" type=\""+unitContainerTypes.get(unitContainerId)+"\"><![CDATA["+unitContainerCommands.get(unitContainerId)+"]]></container>");
        }
        for (String unitId : unitCommands.keySet()) {
          writer.println(" <unit id=\""+unitId+"\"><![CDATA["+unitCommands.get(unitId)+"]]></unit>");
        }
        writer.println("</extended_commands>");
        
        writer.close();
        osw.close();
        fos.close();
      } catch (Throwable throwable) {
        log.error("",throwable);
        ErrorWindow errorWindow = new ErrorWindow(client,throwable.getMessage(),"",throwable);
        errorWindow.setShutdownOnCancel(false);
        errorWindow.setVisible(true);
      }
    }
  }


  /**
   * This is a dialog to edit the script/commands for a given unit.
   *
   * @author Thoralf Rickert
   * @version 1.0, 11.09.2007
   */
  class ExtendedCommandsDialog extends JDialog implements ActionListener {
    private JTextArea scriptingArea = null;
    private Unit unit = null;
    private UnitContainer container = null;
    private ExtendedCommands commands = null;
    
    public ExtendedCommandsDialog(Client client, GameData data, ExtendedCommands commands, Unit unit, String script) {
      super(client,true);
      
      this.commands = commands;
      this.unit = unit;
      
      init(script);
    }
    
    public ExtendedCommandsDialog(Client client, GameData data, ExtendedCommands commands, UnitContainer container, String script) {
      super(client,true);
      
      this.commands = commands;
      this.container = container;
      
      init(script);
    }
    
    protected void init(String script) {
      setLayout(new BorderLayout());
      setWindowSize(640, 480);
      
      JPanel center = new JPanel(new BorderLayout());
      center.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
      
      scriptingArea = new JTextArea(script);
      center.add(new JScrollPane(scriptingArea),BorderLayout.CENTER);
      add(center,BorderLayout.CENTER);
      
      JPanel buttonPanel = new JPanel();
      buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
      buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      buttonPanel.add(Box.createHorizontalGlue());
      JButton cancelButton = new JButton(Resources.get("button.cancel"));
      cancelButton.setRequestFocusEnabled(false);
      cancelButton.setActionCommand("button.cancel");
      cancelButton.addActionListener(this);
      cancelButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
      buttonPanel.add(cancelButton);
      buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
      JButton okButton = new JButton(Resources.get("button.ok"));
      okButton.setRequestFocusEnabled(false);
      okButton.setActionCommand("button.ok");
      okButton.addActionListener(this);
      okButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
      buttonPanel.add(okButton);
      buttonPanel.add(Box.createHorizontalGlue());
      
      add(buttonPanel,BorderLayout.SOUTH);
    }

    public void actionPerformed(ActionEvent e) {
      if (e.getActionCommand().equalsIgnoreCase("button.ok")) {
        setVisible(false);
        String script = scriptingArea.getText();
        if (unit != null) commands.setCommands(unit,script);
        if (container != null) commands.setCommands(container,script);
      } else if (e.getActionCommand().equalsIgnoreCase("button.cancel")) {
        setVisible(false);
      }
    }

    // **********************************************************************
    /**
     * This method sets the window dimension and positions the window to the
     * center of the screen.
     */

    public void setWindowSize(int xSize, int ySize) {
      if (xSize > 0 && ySize > 0) {
        int x = (int) getToolkit().getScreenSize().width;
        int y = (int) getToolkit().getScreenSize().height;
        setSize(xSize, ySize);
        setLocation(new Point((int) (x / 2 - xSize / 2), (int) (y / 2 - ySize / 2)));
      }
    }
  }
  
  /**
   * A Helper class to have some shortcuts
   *
   * @author Thoralf Rickert
   * @version 1.0, 11.09.2007
   */
  public class ExtendedCommandHelper {
    private GameData world = null;
    private Unit unit = null;
    private UnitContainer container = null;
    
    public ExtendedCommandHelper(GameData world, Unit unit) {
      this.world = world;
      this.unit = unit;
    }
    
    public ExtendedCommandHelper(GameData world, UnitContainer container) {
      this.world = world;
      this.container = container;
    }
    
    /**
     * Returns true, if the current unit is in the region with the
     * given name.
     */
    public boolean unitIsInRegion(String regionName) {
      return unit.getRegion().getName().equalsIgnoreCase(regionName);
    }
    
    /**
     * Returns the unit with the given Unit-ID in the current region.
     */
    public Unit getUnitInRegion(String unitId) {
      return unit.getRegion().getUnit(UnitID.createUnitID(unitId, world.base));
    }
    
    /**
     * Returns true, if the current unit sees another unit with
     * the given unit id.
     */
    public boolean unitSeesOtherUnit(String unitId) {
      Unit otherunit = getUnitInRegion(unitId);
      return otherunit != null;
    }
    
    /**
     * Returns the number of items of a unit with the given
     * item name. For example:
     * 
     *  int horses = getItemCount(unit,"Pferd")
     *  
     * returns the number of horses of the given unit.
     */
    public int getItemCount(Unit unit, String itemTypeName) {
      Collection<Item> items = unit.getItems();
      if (items != null) {
        for (Item item : items) {
          if (item.getItemType().getName().equalsIgnoreCase(itemTypeName)) {
            return item.getAmount();
          }
        }
      }
      return 0;
    }
    
    /**
     * This method returns the amount of silver of the given
     * unit. This is a shortcut for
     *  
     *  getItemCount(unit,"Silber")
     */
    public int getSilver(Unit unit) {
      return getItemCount(unit, "Silber");
    }
    
    /**
     * Returns the number of persons in this unit.
     */
    public int getPersons(Unit unit) {
      return unit.getPersons();
    }
    
    /**
     * Returns the skill level of a unit. For example
     *  getLevel(unit,"Unterhaltung")
     */
    public int getLevel(Unit unit, String skillName) {
      Collection<Skill> skills = unit.getSkills();
      if (skills != null) {
        for (Skill skill : skills) {
          if (skill.getSkillType().getName().equalsIgnoreCase(skillName)) {
            return skill.getLevel();
          }
        }
      }
      return 0;
    }
    
    /**
     * Adds an order to the current unit.
     */
    public void addOrder(String order) {
      unit.addOrder(order, false, 0);
    }
    
    /**
     * Sets the order of the current unit.
     */
    public void setOrder(String order) {
      List<String> orders = new ArrayList<String>();
      orders.add(order);
      unit.setOrders(orders);
    }
  }
}
