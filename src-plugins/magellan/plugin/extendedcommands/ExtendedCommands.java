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
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import magellan.client.Client;
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
import org.w3c.dom.NodeList;

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
    
    ExtendedCommandsPlugIn.getExecuteMenu().setEnabled(hasCommands());
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
    
    ExtendedCommandsPlugIn.getExecuteMenu().setEnabled(hasCommands());
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
      interpreter.set("helper", new ExtendedCommandsHelper(world,unit));
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
   * Executes the commands/script for a given unitcontainer.
   */
  public void execute(GameData world, UnitContainer container) {
    if (!hasCommands(container)) return;
    
    try {
      Interpreter interpreter = new Interpreter();
      interpreter.set("world",world);
      interpreter.set("container",container);
      interpreter.set("helper", new ExtendedCommandsHelper(world,container));
      interpreter.eval(getCommands(container));
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
