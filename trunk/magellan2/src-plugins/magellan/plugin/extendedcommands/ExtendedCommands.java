// class magellan.plugin.extendedcommands.ExtendedCommands
// created on 02.02.2008
//
// Copyright 2003-2008 by magellan project team
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import magellan.client.Client;
import magellan.client.swing.DebugDock;
import magellan.client.utils.ErrorWindow;
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.GameData;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.UnitID;
import magellan.library.utils.Encoding;
import magellan.library.utils.Utils;
import magellan.library.utils.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * This class holds the commands for all units.
 *
 * @author Thoralf Rickert
 * @version 1.0, 11.09.2007
 */
public class ExtendedCommands {

  private static final Logger log = Logger.getInstance(ExtendedCommands.class);
  private File unitCommandsFile;
  private Client client;
  private Hashtable<String,Script> unitCommands = new Hashtable<String,Script>();
  private Hashtable<String,Script> unitContainerCommands = new Hashtable<String,Script>();
  
  private Script library;
  
  public ExtendedCommands(Client client) {
    this.client = client;
    Properties properties = client.getProperties();
    String commandsFilename = properties.getProperty("extendedcommands.unitCommands");
    if (Utils.isEmpty(commandsFilename)) {
      commandsFilename = Client.getSettingsDirectory()+"/extendedcommands.xml";
    }
    
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
    if (!hasCommands()) {
      return false;
    }
    if (container == null) {
      return false;
    }
    if (container.getID() == null) {
      return false;
    }
    return unitContainerCommands.containsKey(container.getID().toString());
  }
  
  /**
   * Returns true if there is a command for the given unitcontainer.
   */
  public boolean hasAllCommands(GameData world, UnitContainer container) {
    if (!hasCommands()) {
      return false;
    }
    if (container == null) {
      return false;
    }
    if (container.getID() == null) {
      return false;
    }
    boolean ok = unitContainerCommands.containsKey(container.getID().toString());
    
    Collection<Unit> units = container.units();
    if (units != null && units.size()>0) {
      for (Unit unit : units) {
        ok = ok || hasCommands(unit);
        if (ok) {
          break;
        }
      }
    }
    
    return ok;
  }
  
  /**
   * Returns true if there is a command for the given unit.
   */
  public boolean hasCommands(Unit unit) {
    if (!hasCommands()) {
      return false;
    }
    if (unit == null) {
      return false;
    }
    if (unit instanceof TempUnit) {
      return false;
    }
    if (unit.getID() == null) {
      return false;
    }
    return unitCommands.containsKey(unit.getID().toString());
  }
  
  /**
   * Returns the command script for the given unitcontainer.
   */
  public Script getCommands(UnitContainer container) {
    if (!hasCommands(container)) {
      return null;
    }
    return unitContainerCommands.get(container.getID().toString());
  }
  
  /**
   * Returns the command script for the given unit.
   */
  public Script getCommands(Unit unit) {
    if (!hasCommands(unit)) {
      return null;
    }
    return unitCommands.get(unit.getID().toString());
  }
  
  /**
   * Sets the commands for a given unit.
   */
  public void setCommands(Unit unit, Script script) {
    if (unit == null) {
      return;
    }
    if (unit instanceof TempUnit) {
      return;
    }
    if (unit.getID() == null) {
      return;
    }
    
    if ((Utils.isEmpty(script) || Utils.isEmpty(script.getScript())) && hasCommands(unit)) {
      // script is empty, let's remove the script from the mapping
      unitCommands.remove(unit.getID().toString());
    } else {
      // replace the script in the mapping
      unitCommands.put(unit.getID().toString(), script);
    }
    
    ExtendedCommandsPlugIn.getExecuteMenu().setEnabled(hasCommands());
  }
  
  /**
   * Sets the commands for a given unit.
   */
  public void setCommands(UnitContainer container, Script script) {
    if (container == null) {
      return;
    }
    if (container.getID() == null) {
      return;
    }
    
    if ((Utils.isEmpty(script) || Utils.isEmpty(script.getScript())) && hasCommands(container)) {
      // script is empty, let's remove the script from the mapping
      unitContainerCommands.remove(container.getID().toString());
    } else {
      // replace the script in the mapping
      unitContainerCommands.put(container.getID().toString(), script);
    }
    
    ExtendedCommandsPlugIn.getExecuteMenu().setEnabled(hasCommands());
  }
  
  /**
   * Returns the value of library.
   * 
   * @return Returns library.
   */
  public Script getLibrary() {
    return library;
  }

  /**
   * Sets the value of library.
   *
   * @param library The value for library.
   */
  public void setLibrary(Script library) {
    this.library = library;
  }

  /**
   * Returns a list of all units with commands.
   */
  public List<Unit> getUnitsWithCommands() {
    List<Unit> units = new ArrayList<Unit>();
    GameData world = client.getData();
    
    for (String unitId : unitCommands.keySet()) {
      Unit unit = world.getUnit(UnitID.createUnitID(unitId,world.base));
      if (unit != null) {
        units.add(unit);
      }
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
      switch (unitContainerCommands.get(unitContainerId).getType()) {
        case REGIONTYPE: {
          UnitContainer container = world.getRegion(CoordinateID.parse(unitContainerId, ", "));
          if (container != null) {
            containers.add(container);
          }
          break;
        }
        case RACE: {
          UnitContainer container = world.getFaction(EntityID.createEntityID(unitContainerId,world.base));
          if (container != null) {
            containers.add(container);
          }
          break;
        }
        case BUILDINGTYPE: {
          UnitContainer container = world.getBuilding(EntityID.createEntityID(unitContainerId,world.base));
          if (container != null) {
            containers.add(container);
          }
          break;
        }
        case SHIPTYPE: {
          UnitContainer container = world.getShip(EntityID.createEntityID(unitContainerId,world.base));
          if (container != null) {
            containers.add(container);
          }
          break;
        }
      }
    }
    
    return containers;
  }
  

  /**
   * Clears the commands if there are now units associated with
   * this script
   */
  public int clearUnusedUnits() {
    GameData world = client.getData();
    List<String> keys = new ArrayList<String>();
    int counter = 0;
    
    for (String unitId : unitCommands.keySet()) {
      Unit unit = world.getUnit(UnitID.createUnitID(unitId,world.base));
      if (unit == null) {
        keys.add(unitId);
        counter++;
      }
    }
    
    for (String key : keys) {
      unitCommands.remove(key);
    }
    
    return counter;
  }
  
  /**
   * Clears the commands if there are now unitcontainers associated with
   * this script
   */
  public int clearUnusedContainers() {
    GameData world = client.getData();
    List<String> keys = new ArrayList<String>();
    int counter = 0;
    
    for (String unitContainerId : unitContainerCommands.keySet()) {
      switch (unitContainerCommands.get(unitContainerId).getType()) {
        case REGIONTYPE: {
          UnitContainer container = world.getRegion(CoordinateID.parse(unitContainerId, ", "));
          if (container == null) {
            keys.add(unitContainerId);
            counter++;
          }
          break;
        }
        case RACE: {
          UnitContainer container = world.getFaction(EntityID.createEntityID(unitContainerId,world.base));
          if (container == null) {
            keys.add(unitContainerId);
            counter++;
          }
          break;
        }
        case BUILDINGTYPE: {
          UnitContainer container = world.getBuilding(EntityID.createEntityID(unitContainerId,world.base));
          if (container == null) {
            keys.add(unitContainerId);
            counter++;
          }
          break;
        }
        case SHIPTYPE: {
          UnitContainer container = world.getShip(EntityID.createEntityID(unitContainerId,world.base));
          if (container == null) {
            keys.add(unitContainerId);
            counter++;
          }
          break;
        }
      }
    }
    
    for (String key : keys) {
      unitContainerCommands.remove(key);
    }

    
    return counter;
  }
  
  
  /**
   * Executes the commands/script for a given unit.
   */
  public void execute(GameData world, Unit unit) {
    if (!hasCommands(unit)) {
      return;
    }
    
    try {
      Interpreter interpreter = new Interpreter();
      interpreter.set("world",world);
      interpreter.set("unit",unit);
      interpreter.set("helper", new ExtendedCommandsHelper(world,unit));
      interpreter.set("log", DebugDock.getInstance());
      
      String script = "";
      if (getLibrary() != null) {
        script += getLibrary().getScript();
      }
      script += "\n";
      script += getCommands(unit).getScript();
      
      interpreter.eval(script);
      unit.setOrdersChanged(true);
    } catch (EvalError error) {
      String message = error.getMessage();
      if (message==null || message.length()==0) {
        message=error.getCause().getClass().getName();
      }
      try {
        message+="\r\n"+error.getErrorText();
      } catch (NullPointerException npe) {}
      ErrorWindow errorWindow = new ErrorWindow(client,message,"",error);
      errorWindow.setShutdownOnCancel(false);
      errorWindow.setVisible(true);
    } catch (Throwable throwable) {
      ExtendedCommands.log.info("",throwable);
      ErrorWindow errorWindow = new ErrorWindow(client,throwable.getMessage(),"",throwable);
      errorWindow.setShutdownOnCancel(false);
      errorWindow.setVisible(true);
    }
  }
  
  /**
   * Executes the commands/script for a given unitcontainer.
   */
  public void execute(GameData world, UnitContainer container) {
    if (!hasCommands(container)) {
      return;
    }
    
    try {
      Interpreter interpreter = new Interpreter();
      interpreter.set("world",world);
      interpreter.set("container",container);
      interpreter.set("helper", new ExtendedCommandsHelper(world));
      interpreter.set("log", DebugDock.getInstance());
      
      String script = "";
      if (getLibrary() != null) {
        script += getLibrary().getScript();
      }
      script += "\n";
      script += getCommands(container).getScript();
      
      interpreter.eval(script);
    } catch (EvalError error) {
      String message = error.getMessage();
      if (message==null || message.length()==0) {
        message=error.getCause().getClass().getName();
      }
      try {
        message+="\r\n"+error.getErrorText();
      } catch (NullPointerException npe) {}
      ErrorWindow errorWindow = new ErrorWindow(client,message,"",error);
      errorWindow.setShutdownOnCancel(false);
      errorWindow.setVisible(true);
    } catch (Throwable throwable) {
      ExtendedCommands.log.info("",throwable);
      ErrorWindow errorWindow = new ErrorWindow(client,throwable.getMessage(),"",throwable);
      errorWindow.setShutdownOnCancel(false);
      errorWindow.setVisible(true);
    }
  }
  /**
   * Executes the library commands/script.
   */
  public void execute(GameData world) {
    try {
      Interpreter interpreter = new Interpreter();
      interpreter.set("world",world);
      interpreter.set("helper", new ExtendedCommandsHelper(world));
      interpreter.set("log", DebugDock.getInstance());
      interpreter.eval(getLibrary().getScript());
    } catch (EvalError error) {
      String message = error.getMessage();
      if (message==null || message.length()==0) {
        message=error.getCause().getClass().getName();
      }
      try {
        message+="\r\n"+error.getErrorText();
      } catch (NullPointerException npe) {}
      ErrorWindow errorWindow = new ErrorWindow(client,message,"",error);
      errorWindow.setShutdownOnCancel(false);
      errorWindow.setVisible(true);
    } catch (Throwable throwable) {
      ExtendedCommands.log.info("",throwable);
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
      ExtendedCommands.log.info("Reading XML "+file);
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(false);
      factory.setValidating(false);
      DocumentBuilder builder  = factory.newDocumentBuilder();
      Document document = builder.parse(file);
      Element rootNode = document.getDocumentElement();
      if (!rootNode.getNodeName().equalsIgnoreCase("extended_commands")) {
        ExtendedCommands.log.error("This is NOT an extended command configuration file");
        return;
      }
      
      Element libraryNode = Utils.getChildNode(rootNode, "library");
      if (libraryNode != null) {
        library = new Script(libraryNode);
      }
      
      List<Element> nodes = Utils.getChildNodes(rootNode,"container");
      ExtendedCommands.log.info("Found "+nodes.size()+" unitcontainer commands");
      for( Element node : nodes ) {
        Script script = new Script(node);
        unitContainerCommands.put(script.getContainerId(),script);
      }
      
      nodes = Utils.getChildNodes(rootNode,"unit");
      ExtendedCommands.log.info("Found "+nodes.size()+" unit commands");
      for( Element node : nodes ) {
        Script script = new Script(node);
        unitCommands.put(script.getContainerId(),script);
      }
      
    } catch (Throwable throwable) {
      ExtendedCommands.log.error("",throwable);
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
      if (library != null) {
        library.toXML(writer);
      }
      for (Script script : unitContainerCommands.values()) {
        script.toXML(writer);
      }
      for (Script script : unitCommands.values()) {
        script.toXML(writer);
      }
      writer.println("</extended_commands>");
      
      writer.close();
      osw.close();
      fos.close();
    } catch (Throwable throwable) {
      ExtendedCommands.log.error("",throwable);
      ErrorWindow errorWindow = new ErrorWindow(client,throwable.getMessage(),"",throwable);
      errorWindow.setShutdownOnCancel(false);
      errorWindow.setVisible(true);
    }
  }
}
