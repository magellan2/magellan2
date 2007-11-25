/*
 *  Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe,
 *                          Stefan Goetz, Sebastian Pappert,
 *                          Klaas Prause, Enno Rehling,
 *                          Sebastian Tusk, Ulrich Kuester,
 *                          Ilja Pavkovic
 *
 * This file is part of the Eressea Java Code Base, see the
 * file LICENSING for the licensing information applying to
 * this file.
 *
 */

package magellan.client.desktop;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import magellan.client.Client;
import magellan.client.actions.desktop.LayoutCheckboxMenuItem;
import magellan.client.actions.desktop.LayoutDeleteAction;
import magellan.client.actions.desktop.LayoutExportAction;
import magellan.client.actions.desktop.LayoutImportAction;
import magellan.client.actions.desktop.LayoutNewAction;
import magellan.client.actions.desktop.LayoutSaveAction;
import magellan.client.utils.ErrorWindow;
import magellan.library.utils.Encoding;
import magellan.library.utils.Resources;
import magellan.library.utils.Utils;
import magellan.library.utils.logging.Logger;
import net.infonode.docking.RootWindow;
import net.infonode.docking.View;
import net.infonode.docking.theme.DockingWindowsTheme;
import net.infonode.docking.theme.ShapedGradientDockingTheme;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.StringViewMap;
import net.infonode.util.Direction;


/**
 *
 * @author Andreas
 * @author Thoralf
 * @version 1.0
 */
public class DockingFrameworkBuilder  {
  private static final Logger log = Logger.getInstance(DockingFrameworkBuilder.class);
  private static DockingFrameworkBuilder _instance = null;
	private List<Component> componentsUsed;
  private StringViewMap viewMap = null;
  private Map<String,View> views = null;

	/** Holds value of property screen. */
  
  private static JMenu layoutMenu = null;
  private static List<DockingLayout> layouts = new ArrayList<DockingLayout>();
  private static DockingLayout activeLayout = null;

	/**
	 * Creates new DockingFrameworkBuilder
	 */
	private DockingFrameworkBuilder() {
		componentsUsed = new LinkedList<Component>();
	}
  
  public static DockingFrameworkBuilder getInstance() {
    if (_instance == null) _instance = new DockingFrameworkBuilder();
    return _instance;
  }

	/**
	 * This method builds the desktop. This is the main component inside Magellan
   * It contains a IDF RootWindow with multiple Docks.
	 */
	public JComponent buildDesktop(FrameTreeNode root, Map<String,Component> components, File serializedView) {
		componentsUsed.clear();
    for (Component component : components.values()) {
      componentsUsed.add(component);
    }
    
    // we have a tree of settings and a list of components. Let's build a root window here...
    return createRootWindow(components,serializedView);
	}
  
  /**
   * This method tries to setup the infonode docking framework.
   */
  protected JComponent createRootWindow(Map<String,Component> components, File serializedViewData) {
    views = new HashMap<String,View>();
    viewMap = new StringViewMap();

    for (String key : components.keySet()) {
      if (key.equals("COMMANDS")) continue; // deprecated
      if (key.equals("NAME")) continue; // deprecated
      if (key.equals("DESCRIPTION")) continue; // deprecated
      if (key.equals("OVERVIEW&HISTORY")) continue; // deprecated
      
      Component component = components.get(key);
      
      View view = new View(Resources.get("dock."+key+".title"),null,component);
      view.setName(key);
      view.setToolTipText(Resources.get("dock."+key+".tooltip"));
      viewMap.addView(key,view);
      views.put(key,view);
    }
    
    RootWindow window = null;
    try {
      if (serializedViewData != null && serializedViewData.exists()) {
        window = read(viewMap,views,serializedViewData);
      } else {
        window = createDefault(viewMap,views);
      }
      if (window == null) {
        ErrorWindow errorWindow = new ErrorWindow("Could not load docking layouts.");
        errorWindow.open();
      }
    } catch (NullPointerException npe) {
      // okay, sometimes this happens without a reason (setToolTipText())...
      log.error("NPE",npe);
    } catch (Throwable t) {
      log.fatal(t.getMessage(),t);
      ErrorWindow errorWindow = new ErrorWindow(Client.INSTANCE,t.getMessage(),"",t);
      errorWindow.setVisible(true);
    }
    
    DockingWindowsTheme theme = new ShapedGradientDockingTheme();
    window.getWindowBar(Direction.DOWN).setEnabled(true);
    window.getRootWindowProperties().addSuperObject(theme.getRootWindowProperties());
    window.setPopupMenuFactory(new MagellanPopupMenuFactory(viewMap));
    
    window.getRootWindowProperties().getWindowAreaProperties().setBackgroundColor(null).setBorder(null);
    window.getRootWindowProperties().getWindowAreaShapedPanelProperties().setComponentPainter(null);
    window.getRootWindowProperties().getComponentProperties().setBackgroundColor(null);
    window.getRootWindowProperties().getShapedPanelProperties().setComponentPainter(null);

    return window;
  }
  
  public StringViewMap getViewMap() {
    return viewMap;
  }
  
  /**
   * This method writes a docking configuration to the given file.
   */
  public void write(File serializedViewData, RootWindow window) throws IOException {
    StringBuffer buffer = new StringBuffer();
    buffer.append("<?xml version='1.0' encoding='"+Encoding.DEFAULT.toString()+"'?>\r\n");
    buffer.append("<dock version='1.0'>\r\n");
    for (DockingLayout layout : layouts) {
      layout.save(buffer);
    }
    buffer.append("</dock>\r\n");
    
    //System.out.println(buffer);
    
    PrintWriter pw = new PrintWriter(serializedViewData,Encoding.DEFAULT.toString());
    pw.println(buffer.toString());
    pw.close();
  }
  
  
  /**
   * This method reads a docking configuration from the given file.
   */
  public synchronized RootWindow read(StringViewMap viewMap, Map<String,View> views, File serializedViewData) throws IOException {
    log.info("Loading Docking Layouts");
    
    RootWindow window = DockingUtil.createRootWindow(viewMap, true);
    
    try {
      DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = builder.parse(serializedViewData);
      if (!document.getDocumentElement().getNodeName().equals("dock")) {
        log.fatal("The file "+serializedViewData+" does NOT contain Docking-layouts for Magellan. Missing XML root element 'dock'");
        return null;
      }
      load(document.getDocumentElement(), viewMap, views);
      
      log.info("Loaded "+layouts.size()+" Docking layouts.");
      for (DockingLayout layout : layouts) {
        if (layout.isActive()) {
          activeLayout = layout;
          break;
        }
      }
      
      if (activeLayout == null && layouts.size()>0) activeLayout = layouts.get(0);
      
      activeLayout.open(window);
      
    } catch (Exception exception) {
      log.error(exception);
      ErrorWindow errorWindow = new ErrorWindow("Could not load docking layouts.",exception);
      errorWindow.open();
    }
    
    return window;
  }
  protected synchronized void load(Element root, StringViewMap viewMap, Map<String,View> views) {
    if (root.getNodeName().equalsIgnoreCase("dock")) {
      List<Element> subnodes = Utils.getChildNodes(root);
      log.info("Found "+subnodes.size()+" Docking layouts.");
      for (int i=0; i<subnodes.size(); i++) {
        Element node = subnodes.get(i);
        load(node, viewMap, views);
      }
    } else if (root.getNodeName().equalsIgnoreCase("rootwindow")) {
      String layoutName = root.getAttribute("name");
      if (Utils.isEmpty(layoutName)) layoutName = "Standard";
      boolean isActive = Utils.getBoolValue(root.getAttribute("isActive"),true);
      
      log.warn("Lade Layout "+layoutName);
      DockingLayout layout = new DockingLayout(layoutName,root, viewMap, views);
      layout.setActive(isActive);
      layouts.add(layout);
    }
  }

  
  protected synchronized RootWindow createDefault(StringViewMap viewMap, Map<String,View> views) {
    RootWindow window = DockingUtil.createRootWindow(viewMap, true);
    Element root = DockingLayout.createDefaultLayout("Standard", true);
    if (root == null) {
      ErrorWindow errorWindow = new ErrorWindow("Could not create default docking layout.");
      errorWindow.open();
    }
    DockingLayout defaultLayout = new DockingLayout("Standard", root, viewMap, views);
    defaultLayout.setActive(true);
    layouts.add(defaultLayout);
    
    activeLayout = defaultLayout;
    activeLayout.open(window);
    
    return window;
  }
  
  /**
   * This method creates the desktop menu. It contains the layout submenu and all
   * dock components. 
   */
  public JMenu createDesktopMenu(Map<String,Component> components, ActionListener listener) {
    JMenu desktopMenu = new JMenu(Resources.get("desktop.magellandesktop.menu.desktop.caption"));
    desktopMenu.setMnemonic(Resources.get("desktop.magellandesktop.menu.desktop.mnemonic").charAt(0));
    
    layoutMenu = new JMenu(Resources.get("desktop.magellandesktop.menu.desktop.layout.caption"));
    layoutMenu.setEnabled(false);
    
    ButtonGroup group = new ButtonGroup();
    for (DockingLayout layout : layouts) {
      LayoutCheckboxMenuItem item = new LayoutCheckboxMenuItem(layout);
      group.add(item);
      layoutMenu.add(item);
    }
    layoutMenu.addSeparator();
    layoutMenu.add(new LayoutExportAction());
    layoutMenu.add(new LayoutImportAction());
    layoutMenu.addSeparator();
    layoutMenu.add(new LayoutNewAction());
    layoutMenu.add(new LayoutSaveAction());
    layoutMenu.add(new LayoutDeleteAction());
    
    desktopMenu.add(layoutMenu);
    desktopMenu.addSeparator();
    
    if(components.size() > 0) {
      for (String key : components.keySet()) {
        if (key.equals("COMMANDS")) continue; // deprecated
        if (key.equals("NAME")) continue; // deprecated
        if (key.equals("DESCRIPTION")) continue; // deprecated
        if (key.equals("OVERVIEW&HISTORY")) continue; // deprecated
        
        Component component = components.get(key);
        JCheckBoxMenuItem item = new JCheckBoxMenuItem(Resources.get("dock."+key+".title"), false);
        item.setActionCommand("menu."+key);
        desktopMenu.add(item);
        item.addActionListener(listener);
      }
    }
    
    return desktopMenu;
  }
  
  /**
   * Updates all Docking Layouts in the Desktop>Layout menu by removing
   * them and recreate the list of available docking layouts.
   */
  public void updateLayoutMenu() {
    if (layoutMenu == null) return;
    
    // remove all layout items from the menu
    for (int i=0; i<layoutMenu.getItemCount(); i++) {
      if (layoutMenu.getItem(i) instanceof LayoutCheckboxMenuItem) layoutMenu.remove(i);
    }
    
    // add all available items.
    int i=0;
    ButtonGroup group = new ButtonGroup();
    for (DockingLayout layout : layouts) {
      LayoutCheckboxMenuItem item = new LayoutCheckboxMenuItem(layout);
      group.add(item);
      layoutMenu.insert(item,i++);
    }
  }
  
  public void createNewLayout(String name) {
    
    Element root = DockingLayout.createDefaultLayout(name, false);
    
    DockingLayout layout = new DockingLayout(name,root,viewMap,views);
    layouts.add(layout);
    
    updateLayoutMenu();
  }
  
  /**
   * Enabled the specified docking layout.
   */
  public void setActiveLayout(DockingLayout layout) {
    RootWindow window = null;
    if (activeLayout != null) {
      activeLayout.setActive(false);
      activeLayout.dispose();
      window = activeLayout.getRootWindow();
    }
    activeLayout = layout;
    activeLayout.setActive(true);
    activeLayout.open(window);
    
  }

	/**
	 * 
	 */
	public List getComponentsUsed() {
		return componentsUsed;
	}

}
