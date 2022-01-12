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

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.tools.ant.filters.StringInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import magellan.client.utils.ErrorWindow;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Utils;
import magellan.library.utils.logging.Logger;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.FloatingWindow;
import net.infonode.docking.RootWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.docking.WindowBar;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.util.StringViewMap;
import net.infonode.tabbedpanel.TabAreaVisiblePolicy;
import net.infonode.util.Direction;

/**
 * This class holds all information about a Docking Layout.
 *
 * @author Thoralf Rickert
 * @version 1.0, 17.11.2007
 */
public class DockingLayout {
  private static final Logger log = Logger.getInstance(DockingLayout.class);
  private String name = null;
  private Element root = null;
  private boolean isActive = false;
  private RootWindow window = null;
  private StringViewMap viewMap = null;
  private Map<String, View> views = null;
  private DockingFrameworkBuilder dfBuilder;

  /**
   * Creates a new Docking Framework Layout Container.
   *
   * @param dfBuilder
   */
  public DockingLayout(String name, Element root, StringViewMap viewMap, Map<String, View> views,
      DockingFrameworkBuilder dfBuilder) {
    setName(name);
    setRoot(root);
    this.viewMap = viewMap;
    this.views = views;
    this.dfBuilder = dfBuilder;
  }

  /**
   * Loads a List of docking layouts from the given File.
   *
   * @param dfBuilder
   */
  public static List<DockingLayout> load(File file, StringViewMap viewMap,
      Map<String, View> views, DockingFrameworkBuilder dfBuilder) {
    List<DockingLayout> layouts = new ArrayList<DockingLayout>();
    try {
      DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = builder.parse(file);
      if (!document.getDocumentElement().getNodeName().equals("dock")) {
        DockingLayout.log.fatal("The file " + file
            + " does NOT contain Docking-layouts for Magellan. Missing XML root element 'dock'");
        return null;
      }

      DockingLayout.load(layouts, document.getDocumentElement(), viewMap, views, dfBuilder);
    } catch (Exception exception) {
      DockingLayout.log.error(exception);
      ErrorWindow errorWindow = new ErrorWindow("Could not load docking layouts.", exception);
      errorWindow.open();
    }
    return layouts;
  }

  /**
   * Loads a Docking Layout from the XML file.
   *
   * @param dfBuilder
   */
  protected static synchronized void load(List<DockingLayout> layouts, Element root,
      StringViewMap viewMap, Map<String, View> views, DockingFrameworkBuilder dfBuilder) {
    if (root.getNodeName().equalsIgnoreCase("dock")) {
      List<Element> subnodes = Utils.getChildNodes(root);
      DockingLayout.log.info("Found " + subnodes.size() + " Docking layouts.");
      for (Element node : subnodes) {
        DockingLayout.load(layouts, node, viewMap, views, dfBuilder);
      }
    } else if (root.getNodeName().equalsIgnoreCase("rootwindow")) {
      String layoutName = root.getAttribute("name");
      if (Utils.isEmpty(layoutName)) {
        layoutName = "Standard";
      }
      boolean isActive = Utils.getBoolValue(root.getAttribute("isActive"), true);

      DockingLayout.log.warn("Lade Layout " + layoutName);
      DockingLayout layout = new DockingLayout(layoutName, root, viewMap, views, dfBuilder);
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
    DockingLayout.log.info("Dispose Docking Layout " + name);

    // save the current layout
    setActive(false);
    root = save();

    dispose(window);

    if (views != null) {
      for (View view : views.values()) {
        DockingLayout.log.info("Remove View " + view.getName());
        dfBuilder.setInActive(view);
        window.removeView(view);
      }
    }
    window.updateUI();
  }

  protected void dispose(DockingWindow window) {
    for (int i = 0; i < window.getChildWindowCount(); i++) {
      DockingWindow child = window.getChildWindow(i);
      if (child == null)
        return;

      RootWindow root = child.getRootWindow();
      if (root == null) {
        root = this.window;
      }
      if (child instanceof View) {
        if (root == null) {
          continue;
        }
        DockingLayout.log.info("Remove View " + child.getName());
        root.removeView((View) child);
      } else {
        dispose(child);
        window.remove(child);
      }
    }
  }

  /**
   * Creates the Docking Layout inside the RootWindow.
   */
  public void open(RootWindow window, Properties settings) {
    if (window == null) {
      DockingLayout.log.error("RootWindow is null");
      throw new NullPointerException("RootWindow is null");
    }
    setRootWindow(window);
    open(window, root);

    RootWindowProperties prop = window.getRootWindowProperties();
    if (PropertiesHelper.getBoolean(settings, PropertiesHelper.CLIENTPREFERENCES_DONT_SHOW_TABS,
        false)) {
      prop.getTabWindowProperties().getTabbedPanelProperties().getTabAreaProperties()
          .setTabAreaVisiblePolicy(TabAreaVisiblePolicy.MORE_THAN_ONE_TAB);
    } else {
      prop.getTabWindowProperties().getTabbedPanelProperties().getTabAreaProperties()
          .setTabAreaVisiblePolicy(TabAreaVisiblePolicy.ALWAYS);
    }

  }

  /**
   * Creates the Docking Layout inside the rootwindow.
   */
  protected DockingWindow open(RootWindow window, Element root) {
    if (root.getNodeName().equalsIgnoreCase("rootwindow")) {
      DockingWindow child = null;

      List<Element> subnodes = Utils.getChildNodes(root);
      for (Element node : subnodes) {
        child = open(window, node);
        if (child == null) {
          continue;
        }
        if (child instanceof FloatingWindow) {
          continue;
        }
        if (child instanceof WindowBar) {
          continue;
        }
        window.setWindow(child);
      }
    } else if (root.getNodeName().equalsIgnoreCase("windowbar"))
      return loadWindowBar(window, root);
    else if (root.getNodeName().equalsIgnoreCase("floatingwindow"))
      return loadFloatingWindow(window, root);
    else if (root.getNodeName().equalsIgnoreCase("splitwindow"))
      return loadSplitWindow(window, root);
    else if (root.getNodeName().equalsIgnoreCase("tabwindow"))
      return loadTabWindow(window, root);
    else if (root.getNodeName().equalsIgnoreCase("view"))
      return loadView(window, root);
    else {
      List<Element> subnodes = Utils.getChildNodes(root);
      return open(window, subnodes.get(0));
    }
    return null;
  }

  /**
   * Creates a SplitWindow inside a Dock.
   */
  protected synchronized DockingWindow loadSplitWindow(RootWindow window, Element root) {
    boolean isHorizontal = Boolean.valueOf(root.getAttribute("horizontal"));
    float divider = Float.valueOf(root.getAttribute("divider"));
    List<Element> nodes = Utils.getChildNodes(root, "split");

    SplitWindow splitWindow = null;
    if (nodes.size() == 2) {
      DockingWindow left = open(window, Utils.getChildNode(nodes.get(0)));
      DockingWindow right = open(window, Utils.getChildNode(nodes.get(1)));
      splitWindow = new SplitWindow(isHorizontal, left, right);
    } else {
      splitWindow = new SplitWindow(isHorizontal);
    }
    splitWindow.setDividerLocation(divider);

    return splitWindow;
  }

  /**
   * Creates a TabWindow inside a dock
   */
  protected synchronized DockingWindow loadTabWindow(RootWindow window, Element root) {
    List<Element> nodes = Utils.getChildNodes(root, "tab");

    TabWindow tabWindow = new TabWindow();
    int selected = 0;

    for (int i = 0; i < nodes.size(); i++) {
      Element e = nodes.get(i);

      boolean isActive = Boolean.valueOf(e.getAttribute("isActive"));
      if (isActive) {
        selected = i;
      }

      try {
        DockingWindow tab = open(window, Utils.getChildNode(e));
        if (tab == null) {
          continue;
        }
        tabWindow.addTab(tab);
      } catch (Throwable t) {
        DockingLayout.log.error(t);
      }
    }

    tabWindow.setSelectedTab(selected);

    return tabWindow;
  }

  /**
   * Created a View inside a Dock.
   */
  protected synchronized DockingWindow loadView(RootWindow window, Element root) {
    String key = root.getAttribute("title");
    View view = null;
    try {
      view = views.get(key);
      dfBuilder.setActive(view);
    } catch (Throwable t) {
      DockingLayout.log.error(t);
    }
    return view;
  }

  /**
   * Creates a floatingwindow inside the rootwindow.
   */
  protected synchronized FloatingWindow loadFloatingWindow(RootWindow window, Element root) {
    DockingWindow child = null;
    List<Element> subnodes = Utils.getChildNodes(root);
    for (Element element : subnodes) {
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

    FloatingWindow floatWindow =
        window.createFloatingWindow(new Point(x, y), new Dimension(width, height), child);
    floatWindow.getTopLevelAncestor().setVisible(true);
    return floatWindow;
  }

  /**
   * Creates a windowbar inside the rootwindow.
   */
  protected synchronized WindowBar loadWindowBar(RootWindow window, Element root) {
    String directionName = root.getAttribute("direction");
    WindowBar windowBar = null;

    if (directionName != null) {
      if (directionName.equalsIgnoreCase("up")) {
        windowBar = window.getWindowBar(Direction.UP);
      } else if (directionName.equalsIgnoreCase("down")) {
        windowBar = window.getWindowBar(Direction.DOWN);
      } else if (directionName.equalsIgnoreCase("right")) {
        windowBar = window.getWindowBar(Direction.RIGHT);
      } else if (directionName.equalsIgnoreCase("left")) {
        windowBar = window.getWindowBar(Direction.LEFT);
      }
    }

    if (windowBar != null) {
      List<Element> subnodes = Utils.getChildNodes(root);
      for (Element element : subnodes) {
        DockingWindow child = open(window, element);
        if (child != null) {
          windowBar.addTab(child);
        }
      }
    }

    return windowBar;
  }

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
      DockingLayout.log.error(exception);
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
  protected synchronized void save(StringBuffer buffer, DockingWindow window, String offset) {
    if (window == null)
      return;
    if (window instanceof SplitWindow) {
      save(buffer, (SplitWindow) window, offset);
    } else if (window instanceof TabWindow) {
      save(buffer, (TabWindow) window, offset);
    } else if (window instanceof View) {
      save(buffer, (View) window, offset);
    } else if (window instanceof RootWindow) {
      save(buffer, (RootWindow) window, offset);
    } else if (window instanceof FloatingWindow) {
      save(buffer, (FloatingWindow) window, offset);
    } else if (window instanceof WindowBar) {
      save(buffer, (WindowBar) window, offset);
    } else {
      DockingLayout.log.warn("UNKNOWN DockingWindow Type");
      DockingLayout.log.warn("Title:" + window.getTitle());
      DockingLayout.log.warn("Type.:" + window.getClass().getName());
      for (int i = 0; i < window.getChildWindowCount(); i++) {
        save(buffer, window.getChildWindow(i), offset + "  ");
      }
    }
  }

  /**
   * Writes all informations about the SplitWindow into the StringBuffer as XML.
   */
  protected synchronized void save(StringBuffer buffer, SplitWindow window, String offset) {
    buffer.append(offset + "<splitwindow divider='" + window.getDividerLocation()
        + "' horizontal='" + window.isHorizontal() + "'>\r\n");
    for (int i = 0; i < window.getChildWindowCount(); i++) {
      buffer.append(offset + " <split>\r\n");
      save(buffer, window.getChildWindow(i), offset + "  ");
      buffer.append(offset + " </split>\r\n");
    }
    buffer.append(offset + "</splitwindow>\r\n");
  }

  /**
   * Writes all informations about the RootWindow into the StringBuffer as XML.
   */
  protected synchronized void save(StringBuffer buffer, RootWindow window, String offset) {
    buffer.append(offset + "<rootwindow name='" + name + "' isActive='" + isActive + "'>\r\n");
    for (int i = 0; i < window.getChildWindowCount(); i++) {
      save(buffer, window.getChildWindow(i), offset + "  ");
    }
    buffer.append(offset + "</rootwindow>\r\n");
  }

  /**
   * Writes all informations about the TabWindow into the StringBuffer as XML.
   */
  protected synchronized void save(StringBuffer buffer, TabWindow window, String offset) {
    buffer.append(offset + "<tabwindow>\r\n");
    for (int i = 0; i < window.getChildWindowCount(); i++) {
      DockingWindow tab = window.getChildWindow(i);
      boolean active = false;
      if (window.getSelectedWindow() != null && tab != null) {
        active = window.getSelectedWindow().equals(tab);
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
  protected synchronized void save(StringBuffer buffer, View window, String offset) {
    buffer.append(offset + "<view title='" + Utils.escapeXML(window.getName()) + "'>\r\n");
    for (int i = 0; i < window.getChildWindowCount(); i++) {
      save(buffer, window.getChildWindow(i), offset + "  ");
    }
    buffer.append(offset + "</view>\r\n");
  }

  /**
   * Writes all informations about the WindowBar into the StringBuffer as XML.
   */
  protected synchronized void save(StringBuffer buffer, WindowBar window, String offset) {
    if (window.isEnabled()) {
      buffer.append(offset + "<windowbar direction='" + window.getDirection() + "'>\r\n");
      for (int i = 0; i < window.getChildWindowCount(); i++) {
        save(buffer, window.getChildWindow(i), offset + "  ");
      }
      buffer.append(offset + "</windowbar>\r\n");
    }
  }

  /**
   * Writes all informations about the FloatingWindow into the StringBuffer as XML.
   */
  protected synchronized void save(StringBuffer buffer, FloatingWindow window, String offset) {
    Point location = window.getTopLevelAncestor().getLocation();
    Dimension dimension = window.getTopLevelAncestor().getSize();
    // Fiete 20090105: fix http://magellan.log-out.net/mantis/view.php?id=338
    // (Floating Window changes Size between Saves and Loads)
    java.awt.Insets insets = window.getTopLevelAncestor().getInsets();
    int offsetHeight = insets.bottom + insets.top;
    int offsetWidth = insets.left + insets.right;
    boolean isVisible = window.getTopLevelAncestor().isVisible();
    buffer.append(offset + "<floatingwindow x='" + ((int) location.getX()) + "' y='"
        + ((int) location.getY()) + "' width='" + ((int) dimension.getWidth() - offsetWidth)
        + "' height='" + ((int) dimension.getHeight() - offsetHeight) + "' isVisible='" + isVisible
        + "'>\r\n");
    for (int i = 0; i < window.getChildWindowCount(); i++) {
      save(buffer, window.getChildWindow(i), offset + "  ");
    }
    buffer.append(offset + "</floatingwindow>\r\n");
  }

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
      DockingLayout.log.error("error during dock layout save process.", exception);
    }
  }

  /**
   * Creates the default layout.
   *
   * @throws LayoutException on internal error
   */
  public static Element createDefaultLayout(String name, boolean isActive) throws LayoutException {
    StringBuffer buffer = new StringBuffer();
    buffer
        .append("<rootwindow name='" + name + "' isActive='" + isActive + "'>\r\n");
    buffer
        .append("   <windowbar direction='Down'>\r\n" +
            "   </windowbar>\r\n" +
            "   <splitwindow divider='0.3' horizontal='true'>\r\n" +
            "    <split>\r\n" +
            "     <splitwindow divider='0.6' horizontal='false'>\r\n" +
            "      <split>\r\n" +
            "       <tabwindow>\r\n" +
            "        <tab isActive='true'>\r\n" +
            "         <view title='")
        .append(toHTML(MagellanDesktop.OVERVIEW_IDENTIFIER)).append("'>\r\n" +
            "         </view>\r\n" +
            "        </tab>\r\n" +
            "       </tabwindow>\r\n" +
            "      </split>\r\n" +
            "      <split>\r\n" +
            "       <tabwindow>\r\n" +
            "        <tab isActive='true'>\r\n" +
            "         <view title='")
        .append(toHTML(MagellanDesktop.MAP_IDENTIFIER)).append("'>\r\n" +
            "         </view>\r\n" +
            "        </tab>\r\n" +
            "        <tab isActive='false'>\r\n" +
            "         <view title='")
        .append(toHTML(MagellanDesktop.MINIMAP_IDENTIFIER)).append("'>\r\n" +
            "         </view>\r\n" +
            "        </tab>\r\n" +
            "       </tabwindow>\r\n" +
            "      </split>\r\n" +
            "     </splitwindow>\r\n" +
            "    </split>\r\n" +
            "    <split>\r\n" +
            "     <splitwindow divider='0.67' horizontal='false'>\r\n" +
            "      <split>\r\n" +
            "       <splitwindow divider='0.5' horizontal='true'>\r\n" +
            "        <split>\r\n" +
            "         <tabwindow>\r\n" +
            "          <tab isActive='true'>\r\n" +
            "           <view title='")
        .append(toHTML(MagellanDesktop.DETAILS_IDENTIFIER)).append("'>\r\n" +
            "           </view>\r\n" +
            "          </tab>\r\n" +
            "         </tabwindow>\r\n" +
            "        </split>\r\n" +
            "        <split>\r\n" +
            "         <splitwindow divider='0.2' horizontal='false'>\r\n" +
            "          <split>\r\n" +
            "           <tabwindow>\r\n" +
            "            <tab isActive='true'>\r\n" +
            "             <view title='")
        .append(toHTML(MagellanDesktop.NAMEDESCRIPTION_IDENTIFIER)).append("'>\r\n" +
            "             </view>\r\n" +
            "            </tab>\r\n" +
            "           </tabwindow>\r\n" +
            "          </split>\r\n" +
            "          <split>\r\n" +
            "           <splitwindow divider='0.67' horizontal='false'>\r\n" +
            "            <split>\r\n" +
            "             <tabwindow>\r\n" +
            "              <tab isActive='true'>\r\n" +
            "               <view title='")
        .append(toHTML(MagellanDesktop.ORDERS_IDENTIFIER)).append("'>\r\n" +
            "               </view>\r\n" +
            "              </tab>\r\n" +
            "             </tabwindow>\r\n" +
            "            </split>\r\n" +
            "            <split>\r\n" +
            "             <tabwindow>\r\n" +
            "              <tab isActive='true'>\r\n" +
            "               <view title='")
        .append(toHTML(MagellanDesktop.MESSAGES_IDENTIFIER)).append("'>\r\n"
            +
            "               </view>\r\n" +
            "              </tab>\r\n" +
            "             </tabwindow>\r\n" +
            "            </split>\r\n" +
            "           </splitwindow>\r\n" +
            "          </split>\r\n" +
            "         </splitwindow>\r\n" +
            "        </split>\r\n" +
            "       </splitwindow>\r\n" +
            "      </split>\r\n" +
            "      <split>\r\n" +
            "       <splitwindow divider='0.67' horizontal='true'>\r\n"
            +
            "        <split>\r\n" +
            "         <tabwindow>\r\n" +
            "          <tab isActive='true'>\r\n" +
            "           <view title='")
        .append(toHTML(MagellanDesktop.TASKS_IDENTIFIER)).append("'>\r\n" +
            "           </view>\r\n" +
            "          </tab>\r\n" +
            "         </tabwindow>\r\n" +
            "        </split>\r\n" +
            "        <split>\r\n" +
            "         <splitwindow divider='0.5' horizontal='true'>\r\n"
            +
            "          <split>\r\n" +
            "           <tabwindow>\r\n" +
            "            <tab isActive='true'>\r\n" +
            "             <view title='")
        .append(toHTML(
            MagellanDesktop.HISTORY_IDENTIFIER)).append("'>\r\n" +
                "             </view>\r\n" +
                "            </tab>\r\n" +
                "           </tabwindow>\r\n" +
                "          </split>\r\n" +
                "          <split>\r\n" +
                "           <tabwindow>\r\n" +
                "            <tab isActive='true'>\r\n" +
                "             <view title='")
        .append(toHTML(MagellanDesktop.BOOKMARKS_IDENTIFIER)).append("'>\r\n" +
            "             </view>\r\n" +
            "            </tab>\r\n" +
            "           </tabwindow>\r\n" +
            "          </split>\r\n" +
            "         </splitwindow>\r\n" +
            "        </split>\r\n" +
            "       </splitwindow>\r\n" +
            "      </split>\r\n" +
            "     </splitwindow>\r\n" +
            "    </split>\r\n" +
            "   </splitwindow>\r\n" +
            " </rootwindow>\r\n");

    DocumentBuilder builder;
    try {
      builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = builder.parse(new StringInputStream(buffer.toString()));
      return document.getDocumentElement();
    } catch (ParserConfigurationException e) {
      throw new LayoutException("could not create default layout", e);
    } catch (SAXException e) {
      throw new LayoutException("could not create default layout", e);
    } catch (IOException e) {
      throw new LayoutException("could not create default layout", e);
    }
  }

  private static String toHTML(String str) {
    return str.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll(
        "\"", "&quot;").replaceAll("'", "&apos;");
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
  public StringViewMap getViewMap() {
    return viewMap;
  }

  /**
   * Sets the value of viewMap.
   *
   * @param viewMap The value for viewMap.
   */
  public void setViewMap(StringViewMap viewMap) {
    this.viewMap = viewMap;
  }

  /**
   * Returns the value of views.
   *
   * @return Returns views.
   */
  public Map<String, View> getViews() {
    return views;
  }

  /**
   * Sets the value of views.
   *
   * @param views The value for views.
   */
  public void setViews(Map<String, View> views) {
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

  /**
   * An exception during docking layout handling.
   */
  public static class LayoutException extends Exception {

    /**
     * @see Exception#Exception()
     */
    public LayoutException() {
      super();
    }

    /**
     * @see Exception#Exception(String)
     */
    public LayoutException(String message) {
      super(message);
    }

    /**
     * @see Exception#Exception(Throwable)
     */
    public LayoutException(Throwable cause) {
      super(cause);
    }

    /**
     * @see Exception#Exception(String, Throwable)
     */
    public LayoutException(String message, Throwable cause) {
      super(message, cause);
    }

  }

}
