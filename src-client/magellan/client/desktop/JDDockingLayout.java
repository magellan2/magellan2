// class magellan.client.desktop.DockingLayout
// created on 17.11.2007
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
package magellan.client.desktop;

import java.awt.Point;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import magellan.client.desktop.JDMagellanDesktop.RootWindow;
import magellan.client.utils.ErrorWindow;
import magellan.library.utils.Utils;
import magellan.library.utils.logging.Logger;

import org.apache.tools.ant.filters.StringInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.javadocking.DockingExecutor;
import com.javadocking.dock.CompositeDock;
import com.javadocking.dock.CompositeTabDock;
import com.javadocking.dock.Dock;
import com.javadocking.dock.FloatDock;
import com.javadocking.dock.LeafDock;
import com.javadocking.dock.Position;
import com.javadocking.dock.SingleDock;
import com.javadocking.dock.SplitDock;
import com.javadocking.dock.TabDock;
import com.javadocking.dock.factory.SplitDockFactory;
import com.javadocking.dockable.Dockable;

/**
 * This class holds all information about a Docking Layout in the Sanaware Java Docking Framework.
 * 
 * @author Thoralf Rickert
 * @author stm
 * @version 1.0, 17.11.2007
 * @deprecated unfinished
 */
@Deprecated
public class JDDockingLayout {
  private static final Logger log = Logger.getInstance(JDDockingLayout.class);
  private String name = null;
  private Element root = null;
  private boolean isActive = false;
  private RootWindow window = null;
  private Map<String, Dockable> viewMap = null;
  private Map<String, Dockable> views = null;

  /**
   * Creates a new Docking Framework Layout Container.
   */
  public JDDockingLayout(String name, Element root, Map<String, Dockable> viewMap,
      Map<String, Dockable> views) {
    setName(name);
    setRoot(root);
    this.viewMap = viewMap;
    this.views = views;
  }

  /**
   * Loads a List of docking layouts from the given File.
   */
  public static List<JDDockingLayout> load(File file, Map<String, Dockable> viewMap,
      Map<String, Dockable> views) {
    List<JDDockingLayout> layouts = new ArrayList<JDDockingLayout>();
    try {
      DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = builder.parse(file);
      if (!document.getDocumentElement().getNodeName().equals("dock")) {
        JDDockingLayout.log.fatal("The file " + file
            + " does NOT contain Docking-layouts for Magellan. Missing XML root element 'dock'");
        return null;
      }

      JDDockingLayout.load(layouts, document.getDocumentElement(), viewMap, views);
    } catch (Exception exception) {
      JDDockingLayout.log.error(exception);
      ErrorWindow errorWindow = new ErrorWindow("Could not load docking layouts.", exception);
      errorWindow.open();
    }
    return layouts;
  }

  /**
   * Loads a Docking Layout from the XML file.
   */
  protected static synchronized void load(List<JDDockingLayout> layouts, Element root,
      Map<String, Dockable> viewMap, Map<String, Dockable> views) {
    if (root.getNodeName().equalsIgnoreCase("dock")) {
      List<Element> subnodes = Utils.getChildNodes(root);
      JDDockingLayout.log.info("Found " + subnodes.size() + " Docking layouts.");
      for (int i = 0; i < subnodes.size(); i++) {
        Element node = subnodes.get(i);
        JDDockingLayout.load(layouts, node, viewMap, views);
      }
    } else if (root.getNodeName().equalsIgnoreCase("rootwindow")) {
      String layoutName = root.getAttribute("name");
      if (Utils.isEmpty(layoutName)) {
        layoutName = "Standard";
      }
      boolean isActive = Utils.getBoolValue(root.getAttribute("isActive"), true);

      JDDockingLayout.log.warn("Lade Layout " + layoutName);
      JDDockingLayout layout = new JDDockingLayout(layoutName, root, viewMap, views);
      layout.setActive(isActive);
      layouts.add(layout);
    }
  }

  /**
   * Removes all views from the root window.
   */
  public void dispose() {
    if (window == null)
      return;
    JDDockingLayout.log.info("Dispose Docking Layout " + name);

    // save the current layout
    setActive(false);
    root = save();

    dispose(window);

    // if (views != null) {
    // for (Dockable view : views.values()) {
    // DockingLayout.log.info("Remove View " + view.getName());
    // DockingFrameworkBuilder.getInstance().setInActive(view);
    // window.removeView(view);
    // }
    // }
    window.updateUI();
  }

  protected void dispose(Dock window) {
    DockingExecutor de = new DockingExecutor();
    if (window instanceof LeafDock) {
      LeafDock leaf = ((LeafDock) window);
      for (int i = 0; i < leaf.getDockableCount(); i++) {
        Dockable child = leaf.getDockable(i);
        JDDockingLayout.log.info("Remove View " + child.getTitle());
        de.changeDocking(child, null, null);
      }
    } else if (window instanceof CompositeDock) {
      CompositeDock composite = (CompositeDock) window;
      for (int i = 0; i < composite.getChildDockCount(); i++) {
        dispose(composite.getChildDock(i));
      }
    }
  }

  /**
   * Creates the Docking Layout inside the RootWindow.
   */
  public void open(RootWindow window, Properties settings) {
    if (window == null) {
      JDDockingLayout.log.error("RootWindow is null");
      throw new NullPointerException("RootWindow is null");
    }
    setRootWindow(window);
    open(window, root);

    // RootWindowProperties prop = window.getRootWindowProperties();
    // if (PropertiesHelper.getBoolean(settings, PropertiesHelper.CLIENTPREFERENCES_DONT_SHOW_TABS,
    // false)) {
    // prop.getTabWindowProperties().getTabbedPanelProperties().getTabAreaProperties()
    // .setTabAreaVisiblePolicy(TabAreaVisiblePolicy.MORE_THAN_ONE_TAB);
    // } else {
    // prop.getTabWindowProperties().getTabbedPanelProperties().getTabAreaProperties()
    // .setTabAreaVisiblePolicy(TabAreaVisiblePolicy.ALWAYS);
    // }

  }

  /**
   * Creates the Docking Layout inside the rootwindow.
   */
  protected Dock open(RootWindow window, Element root) {
    if (root.getNodeName().equalsIgnoreCase("rootwindow")) {
      CompositeDock composite = window;
      Dock child = null;

      List<Element> subnodes = Utils.getChildNodes(root);
      for (int i = 0; i < subnodes.size(); i++) {
        Element node = subnodes.get(i);
        child = open(window, node);
        if (child == null) {
          continue;
        }
        // if (child instanceof FloatingWindow) {
        // continue;
        // }
        // if (child instanceof WindowBar) {
        // continue;
        // }
        composite.addChildDock(child, new Position(Position.CENTER));
      }
    }
    // } else if (root.getNodeName().equalsIgnoreCase("windowbar"))
    // return loadWindowBar(window, root);
    // else if (root.getNodeName().equalsIgnoreCase("floatingwindow"))
    // return loadFloatingWindow(window, root);
    else if (root.getNodeName().equalsIgnoreCase("splitwindow"))
      return loadSplitWindow(window, root);
    else if (root.getNodeName().equalsIgnoreCase("tabwindow"))
      return loadTabWindow(window, root);
    else if (root.getNodeName().equalsIgnoreCase("view"))
      return loadView(window, root);
    else {
      List<Element> subnodes = Utils.getChildNodes(root);
      if (subnodes.size() > 0)
        return open(window, subnodes.get(0));
    }
    return null;
  }

  /**
   * Creates a SplitWindow inside a Dock.
   */
  protected synchronized SplitDock loadSplitWindow(RootWindow window, Element root) {
    boolean isHorizontal = Boolean.valueOf(root.getAttribute("horizontal"));
    float divider = Float.valueOf(root.getAttribute("divider"));
    List<Element> nodes = Utils.getChildNodes(root, "split");

    SplitDock splitWindow = new SplitDock(getSplitDockFactory());
    if (nodes.size() == 2) {
      Dock left = open(window, Utils.getChildNode(nodes.get(0)));
      Dock right = open(window, Utils.getChildNode(nodes.get(1)));
      splitWindow.addChildDock(left, new Position(isHorizontal ? Position.LEFT : Position.TOP));
      splitWindow
          .addChildDock(right, new Position(isHorizontal ? Position.RIGHT : Position.BOTTOM));
    } else if (nodes.size() == 1) {
      Dock left = open(window, Utils.getChildNode(nodes.get(0)));
      splitWindow.addChildDock(left, new Position(isHorizontal ? Position.LEFT : Position.TOP));
    }
    splitWindow.setDividerLocation((int) (divider * 100));
    return splitWindow;
  }

  private SplitDockFactory getSplitDockFactory() {
    return new SplitDockFactory();
  }

  /**
   * Creates a TabWindow inside a dock
   */
  protected synchronized Dock loadTabWindow(RootWindow window, Element root) {
    List<Element> nodes = Utils.getChildNodes(root, "tab");

    Dock selectedTab = null;
    LinkedList<Dock> tabs = new LinkedList<Dock>();
    boolean allSimple = true;
    for (int i = 0; i < nodes.size(); i++) {
      Element e = nodes.get(i);

      try {
        Dock tab = open(window, Utils.getChildNode(e));
        if (tab == null) {
          continue;
        }
        boolean isActive = Boolean.valueOf(e.getAttribute("isActive"));
        if (isActive) {
          selectedTab = tab;
        }
        tabs.add(tab);
        if (!(tab instanceof SingleDock)) {
          allSimple = false;
        }
      } catch (Throwable t) {
        JDDockingLayout.log.error(t);
      }
    }

    Dock result;
    if (allSimple) {
      TabDock tabDock = new TabDock();
      for (Dock tab : tabs) {
        if (((SingleDock) tab).getDockableCount() > 0) {
          tabDock.addDockable(((SingleDock) tab).getDockable(0), new Point(), new Point());
        }
      }
      result = tabDock;
    } else {
      CompositeTabDock tabWindow = new CompositeTabDock();
      for (Dock tab : tabs) {
        if (tab instanceof SingleDock && ((SingleDock) tab).getDockableCount() > 0) {
          tabWindow.addDockable(((SingleDock) tab).getDockable(0), new Point(), new Point());
        } else {
          tabWindow.addChildDock(tab, new Position());
        }
      }
      if (selectedTab != null) {
        tabWindow.setSelectedDock(selectedTab);
      }
      result = tabWindow;
    }
    return result;
  }

  /**
   * Created a View inside a Dock.
   */
  protected synchronized Dock loadView(RootWindow window, Element root) {
    String key = root.getAttribute("title");
    Dockable view = null;
    try {
      view = views.get(key);
      JDDockingFrameworkBuilder.getInstance().setActive(view);
    } catch (Throwable t) {
      JDDockingLayout.log.error(t);
    }
    Dock dock = new SingleDock();
    dock.addDockable(view, new Point(), new Point());
    return dock;
  }

  /**
   * Creates a floatingwindow inside the rootwindow.
   */
  protected synchronized FloatDock loadFloatingWindow(RootWindow window, Element root) {
    Dock child = null;
    List<Element> subnodes = Utils.getChildNodes(root);
    for (int i = 0; i < subnodes.size(); i++) {
      Element element = subnodes.get(i);
      child = open(window, element);
      if (child != null) {
        break;
      }
    }
    if (child == null)
      return null;
    int x = Utils.getIntValue(root.getAttribute("x"));
    int y = Utils.getIntValue(root.getAttribute("y"));
    int width = Utils.getIntValue(root.getAttribute("width"));
    int height = Utils.getIntValue(root.getAttribute("height"));

    FloatDock floatWindow = new FloatDock(null);
    // TODO add dock
    // window.create
    // FloatingWindow(new Point(x, y), new Dimension(width, height), child);
    // floatWindow.getTopLevelAncestor().setVisible(true);
    return floatWindow;
  }

  // /**
  // * Creates a windowbar inside the rootwindow.
  // */
  // protected synchronized WindowBar loadWindowBar(RootWindow window, Element root) {
  // String directionName = root.getAttribute("direction");
  // WindowBar windowBar = null;
  //
  // if (directionName != null) {
  // if (directionName.equalsIgnoreCase("up")) {
  // windowBar = window.getWindowBar(Direction.UP);
  // } else if (directionName.equalsIgnoreCase("down")) {
  // windowBar = window.getWindowBar(Direction.DOWN);
  // } else if (directionName.equalsIgnoreCase("right")) {
  // windowBar = window.getWindowBar(Direction.RIGHT);
  // } else if (directionName.equalsIgnoreCase("left")) {
  // windowBar = window.getWindowBar(Direction.LEFT);
  // }
  // }
  //
  // if (windowBar != null) {
  // List<Element> subnodes = Utils.getChildNodes(root);
  // for (int i = 0; i < subnodes.size(); i++) {
  // Element element = subnodes.get(i);
  // DockingWindow child = open(window, element);
  // if (child != null) {
  // windowBar.addTab(child);
  // }
  // }
  // }
  //
  // return windowBar;
  // }

  /**
   * Saves this Docking Layout in a XML Element
   */
  public Element save() {
    try {
      StringBuffer buffer = new StringBuffer();
      save(buffer, window, " ");

      DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = builder.parse(new StringInputStream(buffer.toString()));
      return document.getDocumentElement();
    } catch (Exception exception) {
      JDDockingLayout.log.error(exception);
    }
    return null;
  }

  /**
   * Saves this Docking Layout. For this the method tries three methods. 1. Save it via the settings
   * inside the rootwindow (active layout) 2. Save it via the unchanged XML tree in the root element
   * 3. Save a default set.
   */
  public void save(StringBuffer buffer) {
    if (window != null && isActive) {
      // save the active layout
      save(buffer, window, " ");
    } else if (root != null) {
      // save the (hopefully) unchanged xml
      save(buffer, root, " ");
    } else {
      // save default.
    }
  }

  /**
   * Writes all informations about a DockingWindow into the StringBuffer as XML.
   */
  protected synchronized void save(StringBuffer buffer, Dock window, String offset) {
    if (window == null)
      return;
    if (window instanceof SplitDock) {
      save(buffer, (SplitDock) window, offset);
    } else if (window instanceof CompositeTabDock) {
      save(buffer, (CompositeTabDock) window, offset);
    } else if (window instanceof Dockable) {
      save(buffer, (Dockable) window, offset);
    } else if (window instanceof RootWindow) {
      save(buffer, (RootWindow) window, offset);
      // } else if (window instanceof FloatingWindow) {
      // save(buffer, (FloatingWindow) window, offset);
      // } else if (window instanceof WindowBar) {
      // save(buffer, (WindowBar) window, offset);
    } else {
      JDDockingLayout.log.warn("UNKNOWN DockingWindow Type");
      // DockingLayout.log.warn("Title:" + window.getTitle());
      JDDockingLayout.log.warn("Type.:" + window.getClass().getName());
      if (window instanceof CompositeDock) {
        CompositeDock comp = (CompositeDock) window;
        for (int i = 0; i < comp.getChildDockCount(); i++) {
          save(buffer, comp.getChildDock(i), offset + "  ");
        }
      } else if (window instanceof LeafDock) {
        LeafDock leaf = (LeafDock) window;
        for (int i = 0; i < leaf.getDockableCount(); i++) {
          save(buffer, leaf.getDockable(i), offset + "  ");
        }
      }
    }
  }

  /**
   * Writes all informations about the SplitWindow into the StringBuffer as XML.
   */
  protected synchronized void save(StringBuffer buffer, SplitDock window, String offset) {
    boolean isHorizontal = true;
    if (window.getChildDockCount() > 0) {
      isHorizontal =
          window.getChildDockPosition(window.getChildDock(0)).equals(new Position(Position.LEFT));
    }
    buffer.append(offset + "<splitwindow divider='" + window.getDividerLocation()
        + "' horizontal='" + isHorizontal + "'>\r\n");
    for (int i = 0; i < window.getChildDockCount(); i++) {
      buffer.append(offset + " <split>\r\n");
      save(buffer, window.getChildDock(i), offset + "  ");
      buffer.append(offset + " </split>\r\n");
    }
    buffer.append(offset + "</splitwindow>\r\n");
  }

  /**
   * Writes all informations about the RootWindow into the StringBuffer as XML.
   */
  protected synchronized void save(StringBuffer buffer, RootWindow window, String offset) {
    buffer.append(offset + "<rootwindow name='" + name + "' isActive='" + isActive + "'>\r\n");
    for (int i = 0; i < window.getChildDockCount(); i++) {
      save(buffer, window.getChildDock(i), offset + "  ");
    }
    buffer.append(offset + "</rootwindow>\r\n");
  }

  /**
   * Writes all informations about the TabWindow into the StringBuffer as XML.
   */
  protected synchronized void save(StringBuffer buffer, CompositeTabDock window, String offset) {
    buffer.append(offset + "<tabwindow>\r\n");
    for (int i = 0; i < window.getChildDockCount(); i++) {
      Dock tab = window.getChildDock(i);
      boolean active = false;
      if (window.getSelectedDock() != null && tab != null) {
        active = window.getSelectedDock().equals(tab);
      }
      buffer.append(offset + " <tab isActive='" + active + "'>\r\n");
      save(buffer, tab, offset + "  ");
      buffer.append(offset + " </tab>\r\n");
    }
    buffer.append(offset + "</tabwindow>\r\n");
  }

  /**
   * Writes all informations about the View into the StringBuffer as XML.
   */
  protected synchronized void save(StringBuffer buffer, Dockable window, String offset) {
    buffer.append(offset + "<view title='" + Utils.escapeXML(window.getID()) + "'>\r\n");
    // for (int i = 0; i < window.getChildWindowCount(); i++) {
    // save(buffer, window.getChildWindow(i), offset + "  ");
    // }
    buffer.append(offset + "</view>\r\n");
  }

  // /**
  // * Writes all informations about the WindowBar into the StringBuffer as XML.
  // */
  // protected synchronized void save(StringBuffer buffer, WindowBar window, String offset) {
  // if (window.isEnabled()) {
  // buffer.append(offset + "<windowbar direction='" + window.getDirection() + "'>\r\n");
  // for (int i = 0; i < window.getChildWindowCount(); i++) {
  // save(buffer, window.getChildWindow(i), offset + "  ");
  // }
  // buffer.append(offset + "</windowbar>\r\n");
  // }
  // }

  // /**
  // * Writes all informations about the FloatingWindow into the StringBuffer as XML.
  // */
  // protected synchronized void save(StringBuffer buffer, FloatDock window, String offset) {
  // Point location = window.getTopLevelAncestor().getLocation();
  // Dimension dimension = window.getTopLevelAncestor().getSize();
  // // Fiete 20090105: fix http://magellan.log-out.net/mantis/view.php?id=338
  // // (Floating Window changes Size between Saves and Loads)
  // java.awt.Insets insets = window.getTopLevelAncestor().getInsets();
  // int offsetHeight = insets.bottom + insets.top;
  // int offsetWidth = insets.left + insets.right;
  // boolean isVisible = window.getTopLevelAncestor().isVisible();
  // buffer.append(offset + "<floatingwindow x='" + ((int) location.getX()) + "' y='"
  // + ((int) location.getY()) + "' width='" + ((int) dimension.getWidth() - offsetWidth)
  // + "' height='" + ((int) dimension.getHeight() - offsetHeight) + "' isVisible='" + isVisible
  // + "'>\r\n");
  // for (int i = 0; i < window.getChildWindowCount(); i++) {
  // save(buffer, window.getChildWindow(i), offset + "  ");
  // }
  // buffer.append(offset + "</floatingwindow>\r\n");
  // }

  /**
   * Writes all informations inside the root Element into the StringBuffer.
   */
  protected void save(StringBuffer buffer, Element root, String offset) {
    try {
      StringWriter writer = new StringWriter();
      StreamResult result = new StreamResult(writer);
      DOMSource source = new DOMSource(root);
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.transform(source, result);
      writer.close();

      String output = writer.toString();
      output = output.replaceAll("<\\?xml version=\"1.0\" encoding=\".*\"\\?>", "");

      buffer.append("\r\n").append(offset).append(output).append("\r\n");
    } catch (Exception exception) {
      JDDockingLayout.log.error("error during dock layout save process.", exception);
    }
  }

  /**
   * Creates the default layout.
   */
  public static Element createDefaultLayout(String name, boolean isActive) {
    try {
      StringBuffer buffer = new StringBuffer();
      buffer.append("<rootwindow name='" + name + "' isActive='" + isActive + "'>\r\n");
      buffer.append(" <splitwindow divider='0.3' horizontal='true'>\r\n");
      buffer.append("  <split>\r\n");
      buffer.append("   <splitwindow divider='0.6' horizontal='false'>\r\n");
      buffer.append("    <split>\r\n");
      buffer.append("     <tabwindow>\r\n");
      buffer.append("      <tab isActive='true'>\r\n");
      buffer.append("       <view title='OVERVIEW'/>\r\n");
      buffer.append("      </tab>\r\n");
      buffer.append("     </tabwindow>\r\n");
      buffer.append("    </split>\r\n");
      buffer.append("    <split>\r\n");
      buffer.append("     <tabwindow>\r\n");
      buffer.append("      <tab isActive='true'>\r\n");
      buffer.append("       <view title='MINIMAP'/>\r\n");
      buffer.append("      </tab>\r\n");
      buffer.append("      <tab isActive='false'>\r\n");
      buffer.append("       <view title='HISTORY'/>\r\n");
      buffer.append("      </tab>\r\n");
      buffer.append("     </tabwindow>\r\n");
      buffer.append("    </split>\r\n");
      buffer.append("   </splitwindow>\r\n");
      buffer.append("  </split>\r\n");
      buffer.append("  <split>\r\n");
      buffer.append("   <splitwindow divider='0.5' horizontal='true'>\r\n");
      buffer.append("    <split>\r\n");
      buffer.append("     <splitwindow divider='0.6' horizontal='false'>\r\n");
      buffer.append("      <split>\r\n");
      buffer.append("       <tabwindow>\r\n");
      buffer.append("        <tab isActive='true'>\r\n");
      buffer.append("         <view title='MAP'/>\r\n");
      buffer.append("        </tab>\r\n");
      buffer.append("       </tabwindow>\r\n");
      buffer.append("      </split>\r\n");
      buffer.append("      <split>\r\n");
      buffer.append("       <tabwindow>\r\n");
      buffer.append("        <tab isActive='true'>\r\n");
      buffer.append("         <view title='MESSAGES'/>\r\n");
      buffer.append("        </tab>\r\n");
      buffer.append("        <tab isActive='false'>\r\n");
      buffer.append("         <view title='ECHECK'/>\r\n");
      buffer.append("        </tab>\r\n");
      buffer.append("        <tab isActive='false'>\r\n");
      buffer.append("         <view title='TASKS'/>\r\n");
      buffer.append("        </tab>\r\n");
      buffer.append("       </tabwindow>\r\n");
      buffer.append("      </split>\r\n");
      buffer.append("     </splitwindow>\r\n");
      buffer.append("    </split>\r\n");
      buffer.append("    <split>\r\n");
      buffer.append("     <splitwindow divider='0.5' horizontal='false'>\r\n");
      buffer.append("      <split>\r\n");
      buffer.append("       <splitwindow divider='0.5' horizontal='false'>\r\n");
      buffer.append("        <split>\r\n");
      buffer.append("         <tabwindow>\r\n");
      buffer.append("          <tab isActive='true'>\r\n");
      buffer.append("           <view title='NAME&amp;DESCRIPTION'/>\r\n");
      buffer.append("          </tab>\r\n");
      buffer.append("         </tabwindow>\r\n");
      buffer.append("        </split>\r\n");
      buffer.append("        <split>\r\n");
      buffer.append("         <tabwindow>\r\n");
      buffer.append("          <tab isActive='true'>\r\n");
      buffer.append("           <view title='DETAILS'/>\r\n");
      buffer.append("          </tab>\r\n");
      buffer.append("         </tabwindow>\r\n");
      buffer.append("        </split>\r\n");
      buffer.append("       </splitwindow>\r\n");
      buffer.append("      </split>\r\n");
      buffer.append("      <split>\r\n");
      buffer.append("       <tabwindow>\r\n");
      buffer.append("        <tab isActive='true'>\r\n");
      buffer.append("         <view title='ORDERS'/>\r\n");
      buffer.append("        </tab>\r\n");
      buffer.append("       </tabwindow>\r\n");
      buffer.append("      </split>\r\n");
      buffer.append("     </splitwindow>\r\n");
      buffer.append("    </split>\r\n");
      buffer.append("   </splitwindow>\r\n");
      buffer.append("  </split>\r\n");
      buffer.append(" </splitwindow>\r\n");
      buffer.append("</rootwindow>\r\n");

      DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = builder.parse(new StringInputStream(buffer.toString()));
      return document.getDocumentElement();
    } catch (Exception exception) {
      JDDockingLayout.log.error("Error during default layout creating", exception);
      return null;
    }
  }

  /**
   * Returns the value of name.
   * 
   * @return Returns name.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the value of name.
   * 
   * @param name The value for name.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the value of root.
   * 
   * @return Returns root.
   */
  public Element getRoot() {
    return root;
  }

  /**
   * Sets the value of root.
   * 
   * @param root The value for root.
   */
  public void setRoot(Element root) {
    this.root = root;
  }

  /**
   * Returns the value of isActive.
   * 
   * @return Returns isActive.
   */
  public boolean isActive() {
    return isActive;
  }

  /**
   * Sets the value of isActive.
   * 
   * @param isActive The value for isActive.
   */
  public void setActive(boolean isActive) {
    this.isActive = isActive;
  }

  /**
   * Returns the value of viewMap.
   * 
   * @return Returns viewMap.
   */
  public Map<String, Dockable> getViewMap() {
    return viewMap;
  }

  /**
   * Sets the value of viewMap.
   * 
   * @param viewMap The value for viewMap.
   */
  public void setViewMap(Map<String, Dockable> viewMap) {
    this.viewMap = viewMap;
  }

  /**
   * Returns the value of views.
   * 
   * @return Returns views.
   */
  public Map<String, Dockable> getViews() {
    return views;
  }

  /**
   * Sets the value of views.
   * 
   * @param views The value for views.
   */
  public void setViews(Map<String, Dockable> views) {
    this.views = views;
  }

  /**
   * Returns the value of window.
   * 
   * @return Returns window.
   */
  public RootWindow getRootWindow() {
    return window;
  }

  /**
   * Sets the value of window.
   * 
   * @param window The value for window.
   */
  public void setRootWindow(RootWindow window) {
    this.window = window;
  }

}
