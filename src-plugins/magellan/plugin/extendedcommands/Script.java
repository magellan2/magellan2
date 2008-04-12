// class magellan.plugin.extendedcommands.Script
// created on 12.04.2008
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

import java.io.PrintWriter;

import magellan.library.utils.Utils;

import org.w3c.dom.Element;

/**
 * This is a container for a script. It contains some informations about
 * a beanshell script:
 * 
 * 1. of course the script text itself
 * 2. the unit or unitcontainer number
 * 3. the last known cursor position (or null)
 * 4. the priority of this script inside the group of scripts of the same type
 *
 * @author Thoralf Rickert
 * @version 1.0, 12.04.2008
 */
public class Script implements Cloneable {
  public static final int SCRIPTTYPE_UNKNOWN = 0;
  public static final int SCRIPTTYPE_LIBRARY = 1;
  public static final int SCRIPTTYPE_CONTAINER = 2;
  public static final int SCRIPTTYPE_UNIT = 4;
  
  private String script = null;
  private int cursor = 0;
  private Priority priority = Priority.NORMAL;
  private String containerId = null;
  private ContainerType type = ContainerType.UNKNOWN;
  private int scripttype = SCRIPTTYPE_UNKNOWN;
  
  public Script(String containerId, int scripttype, ContainerType type, String script) {
    this.containerId = containerId;
    this.scripttype = scripttype;
    this.type = type;
    this.script = script;
  }
  
  
  public Script(Element node) {
    if (node == null) return;
    if (node.getNodeName().equalsIgnoreCase("library")) {
      script = Utils.getCData(node);
      priority = Priority.NORMAL;
      cursor = getCursor(node);
      containerId = null;
      scripttype = SCRIPTTYPE_LIBRARY;
      
    } else if (node.getNodeName().equalsIgnoreCase("container")) {
      script = Utils.getCData(node);
      priority = getPriority(node);
      cursor = getCursor(node);
      containerId = getContainerId(node);
      type = getContainerType(node);
      scripttype = SCRIPTTYPE_CONTAINER;
      
    } else if (node.getNodeName().equalsIgnoreCase("unit")) {
      script = Utils.getCData(node);
      priority = getPriority(node);
      cursor = getCursor(node);
      containerId = getContainerId(node);
      scripttype = SCRIPTTYPE_UNIT;
      
    } else {
      throw new IllegalArgumentException("Unknown Script type");
    }
  }
  
  
  /**
   * Returns the priority of this script
   */
  private Priority getPriority(Element node) {
    Priority priority = Priority.NORMAL;
    if (!Utils.isEmpty(node.getAttribute("priority"))) {
      priority = Priority.getPriority(node.getAttribute("priority"));
    }
    return priority;
  }

  /**
   * Returns the container type of this script
   */
  private ContainerType getContainerType(Element node) {
    ContainerType containerType = ContainerType.UNKNOWN;
    if (!Utils.isEmpty(node.getAttribute("type"))) {
      containerType = ContainerType.getType(node.getAttribute("type"));
    }
    return containerType;
  }

  /**
   * Returns the container id of this script.
   */
  private String getContainerId(Element node) {
    return node.getAttribute("id");
  }
  
  /**
   * Returns the last known cursor position.
   */
  private int getCursor(Element node) {
    int cursor = 0;
    if (!Utils.isEmpty(node.getAttribute("cursor"))) {
      try {
        cursor = Integer.parseInt(node.getAttribute("cursor"));
      } catch (Exception e) {
        cursor = 0;
      }
      if (cursor<0) cursor = 0;
    }
    return cursor;
  }


  /**
   * Returns the value of script.
   * 
   * @return Returns script.
   */
  public String getScript() {
    return script;
  }


  /**
   * Sets the value of script.
   *
   * @param script The value for script.
   */
  public void setScript(String script) {
    this.script = script;
  }


  /**
   * Returns the value of cursor.
   * 
   * @return Returns cursor.
   */
  public int getCursor() {
    return cursor;
  }


  /**
   * Sets the value of cursor.
   *
   * @param cursor The value for cursor.
   */
  public void setCursor(int cursor) {
    this.cursor = cursor;
  }


  /**
   * Returns the value of priority.
   * 
   * @return Returns priority.
   */
  public Priority getPriority() {
    return priority;
  }


  /**
   * Sets the value of priority.
   *
   * @param priority The value for priority.
   */
  public void setPriority(Priority priority) {
    this.priority = priority;
  }


  /**
   * Returns the value of containerId.
   * 
   * @return Returns containerId.
   */
  public String getContainerId() {
    return containerId;
  }


  /**
   * Sets the value of containerId.
   *
   * @param containerId The value for containerId.
   */
  public void setContainerId(String containerId) {
    this.containerId = containerId;
  }


  /**
   * Returns the value of type.
   * 
   * @return Returns type.
   */
  public ContainerType getType() {
    return type;
  }


  /**
   * Sets the value of type.
   *
   * @param type The value for type.
   */
  public void setType(ContainerType type) {
    this.type = type;
  }
  
  public void toXML(PrintWriter writer) {
    switch (scripttype) {
      case SCRIPTTYPE_LIBRARY: {
        writer.print("<library cursor=\""+getCursor()+"\">");
        writer.print("<![CDATA[");
        writer.print(script);
        writer.println("]]></library>");
        break;
      }
      case SCRIPTTYPE_CONTAINER: {
        writer.print(" <container id=\""+containerId+"\" type=\""+type+"\" cursor=\""+getCursor()+"\" priority=\""+getPriority()+"\">");
        writer.println("<![CDATA["+script+"]]></container>");
        break;
      }
      case SCRIPTTYPE_UNIT: {
        writer.print(" <unit id=\""+containerId+"\" cursor=\""+getCursor()+"\" priority=\""+getPriority()+"\">");
        writer.println("<![CDATA["+script+"]]></unit>");
        break;
      }
    }
  }
  
  /**
   * @see java.lang.Object#clone()
   */
  @Override
  public Object clone() {
    Script script = new Script(this.containerId,this.scripttype,this.type,this.script);
    script.setCursor(this.cursor);
    script.setPriority(this.priority);
    return script;
  }
}
