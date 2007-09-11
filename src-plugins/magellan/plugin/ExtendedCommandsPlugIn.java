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
import magellan.client.swing.context.ContextMenuProvider;
import magellan.client.utils.ErrorWindow;
import magellan.library.GameData;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.UnitID;
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
public class ExtendedCommandsPlugIn implements MagellanPlugIn, ContextMenuProvider, ActionListener {
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
    Resources.getInstance().initialize("extendedcommands_");
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
   * @see magellan.client.swing.context.ContextMenuProvider#createContextMenu(magellan.client.event.EventDispatcher, magellan.library.GameData, java.lang.Object, java.util.Collection)
   */
  public JMenuItem createContextMenu(final EventDispatcher dispatcher, final GameData data, Object argument, Collection selectedObjects) {
    if (!(argument instanceof Unit)) return null;
    
    final Unit unit = (Unit)argument;
    
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
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    log.info(e.getActionCommand());
    switch (PlugInAction.getAction(e)) {
      case EXECUTE_ALL: {
        log.info("Execute all");
        executeCommands(client.getData());
        break;
      }
      case CONFIGURE_ALL: {
        log.info("Execute all");
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
  }
  
  /**
   * Executes the commands for a given unit.
   */
  protected void executeCommands(GameData data, Unit unit) {
    log.info("Execute Command for Unit "+unit);
    
    // find the commands for this unit.
    commands.execute(data, unit);
    
    client.repaint();
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
   * This class holds the commands for all units.
   *
   * @author Thoralf Rickert
   * @version 1.0, 11.09.2007
   */
  class ExtendedCommands {
    private Logger log = Logger.getInstance(ExtendedCommands.class);
    private File commandsFile;
    private Client client;
    private Hashtable<String, String> commands = new Hashtable<String, String>();
    
    public ExtendedCommands(Client client) {
      this.client = client;
      Properties properties = client.getProperties();
      String commandsFilename = properties.getProperty("extendedcommands.commands");
      if (Utils.isEmpty(commandsFilename)) commandsFilename = "extendedcommands.xml";
      
      commandsFile = new File(commandsFilename);
      if (!commandsFile.exists()) {
        // file doesn't exist. create it with an empty set.
        write(commandsFile);
      }
      
      read(commandsFile);
    }
    
    /**
     * Returns true if there are any commands set.
     */
    public boolean hasCommands() {
      return commands.size() > 0;
    }
    
    /**
     * Returns true if there is a command for the given unit.
     */
    public boolean hasCommands(Unit unit) {
      if (!hasCommands()) return false;
      if (unit == null) return false;
      if (unit instanceof TempUnit) return false;
      if (unit.getID() == null) return false;
      if (!commands.containsKey(unit.getID().toString())) return false;
      return true;
    }
    
    /**
     * Returns the command script for the given unit.
     */
    public String getCommands(Unit unit) {
      if (!hasCommands(unit)) return null;
      return commands.get(unit.getID().toString());
    }
    
    /**
     * Sets the commands for a given unit.
     */
    public void setCommands(Unit unit, String script) {
      if (unit == null) return;
      if (unit instanceof TempUnit) return;
      if (unit.getID() == null) return;
      
      if (Utils.isEmpty(script) && hasCommands(unit)) {
        commands.remove(unit.getID().toString());
      } else {
        commands.put(unit.getID().toString(), script);
      }
      
      executeMenu.setEnabled(hasCommands());
    }
    
    /**
     * Returns a list of all units with commands.
     */
    public List<Unit> getUnitsWithCommands() {
      List<Unit> units = new ArrayList<Unit>();
      GameData world = client.getData();
      
      for (String unitId : commands.keySet()) {
        Unit unit = world.getUnit(UnitID.createUnitID(unitId,world.base));
        if (unit != null) units.add(unit);
      }
      
      return units;
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
        ErrorWindow errorWindow = new ErrorWindow(client,throwable.getMessage(),"",throwable);
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
        NodeList nodes = rootNode.getElementsByTagName("unit");
        log.info("Found "+nodes.getLength()+" unit commands");
        for( int i=0; i<nodes.getLength(); i++ ) {
          Element node = (Element)nodes.item(i);
          String id = node.getAttribute("id");
          String command = node.getFirstChild().getNodeValue();
          commands.put(id,command);
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
      write(commandsFile);
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
        for (String unitId : commands.keySet()) {
          writer.println(" <unit id=\""+unitId+"\"><![CDATA["+commands.get(unitId)+"]]></unit>");
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
    private ExtendedCommands commands = null;
    
    public ExtendedCommandsDialog(Client client, GameData data, ExtendedCommands commands, Unit unit, String script) {
      super(client,true);
      
      this.commands = commands;
      this.unit = unit;
      
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
        commands.setCommands(unit,script);
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
    
    public ExtendedCommandHelper(GameData world, Unit unit) {
      this.world = world;
      this.unit = unit;
    }
    
    public boolean isInRegion(String regionName) {
      return unit.getRegion().getName().equalsIgnoreCase(regionName);
    }
    
    public void addOrder(String order) {
      unit.addOrder(order, false, 0);
    }
  }
}
