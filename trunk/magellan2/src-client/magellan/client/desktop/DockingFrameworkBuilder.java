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
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

import magellan.client.Client;
import magellan.client.actions.desktop.LayoutCheckboxMenuItem;
import magellan.client.actions.desktop.LayoutDeleteAction;
import magellan.client.actions.desktop.LayoutExportAction;
import magellan.client.actions.desktop.LayoutImportAction;
import magellan.client.actions.desktop.LayoutNewAction;
import magellan.client.actions.desktop.LayoutSaveAction;
import magellan.client.desktop.DockingLayout.LayoutException;
import magellan.client.utils.ErrorWindow;
import magellan.library.utils.Encoding;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;
import net.infonode.docking.DockingWindowListener;
import net.infonode.docking.RootWindow;
import net.infonode.docking.View;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.theme.DockingWindowsTheme;
import net.infonode.docking.theme.ShapedGradientDockingTheme;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.StringViewMap;
import net.infonode.util.Direction;

import org.w3c.dom.Element;

/**
 * This is a factory for working with the infonode docking framework.
 * 
 * @author Thoralf
 * @version 1.0
 */
public class DockingFrameworkBuilder {
  private static final Logger log = Logger.getInstance(DockingFrameworkBuilder.class);
  private static DockingFrameworkBuilder _instance = null;
  private List<Component> componentsUsed;
  private StringViewMap viewMap = null;
  private Map<String, View> views = null;

  /** Holds value of property screen. */

  private static JMenu layoutMenu = null;
  private static JCheckBoxMenuItem hideTabs = null;
  private static LayoutDeleteAction deleteMenu = null;
  private static List<DockingLayout> layouts = new ArrayList<DockingLayout>();
  private static DockingLayout activeLayout = null;
  private Properties settings = null;

  /**
   * Creates new DockingFrameworkBuilder
   */
  private DockingFrameworkBuilder() {
    componentsUsed = new LinkedList<Component>();
  }

  public static DockingFrameworkBuilder getInstance() {
    if (DockingFrameworkBuilder._instance == null) {
      DockingFrameworkBuilder._instance = new DockingFrameworkBuilder();
    }
    return DockingFrameworkBuilder._instance;
  }

  /**
   * This method builds the desktop. This is the main component inside Magellan It contains a IDF
   * RootWindow with multiple Docks.
   */
  public RootWindow buildDesktop(Map<String, Component> components, File serializedView) {
    componentsUsed.clear();
    for (Component component : components.values()) {
      componentsUsed.add(component);
    }

    // we have a tree of settings and a list of components. Let's build a root window here...
    return createRootWindow(components, serializedView);
  }

  /**
   * This method tries to setup the infonode docking framework.
   */
  protected RootWindow createRootWindow(Map<String, Component> components, File serializedViewData) {
    views = new HashMap<String, View>();
    viewMap = new StringViewMap();

    for (String key : components.keySet()) {
      if (key.equals(MagellanDesktop.COMMANDS_IDENTIFIER)) {
        continue; // deprecated
      }
      if (key.equals(MagellanDesktop.NAME_IDENTIFIER)) {
        continue; // deprecated
      }
      if (key.equals(MagellanDesktop.DESCRIPTION_IDENTIFIER)) {
        continue; // deprecated
      }
      if (key.equals(MagellanDesktop.OVERVIEWHISTORY_IDENTIFIER)) {
        continue; // deprecated
      }

      Component component = components.get(key);

      View view = new View(Resources.get("dock." + key + ".title"), null, component);
      view.setName(key);
      view.setToolTipText(Resources.get("dock." + key + ".tooltip"));
      if (component instanceof DockingWindowListener) {
        view.addListener((DockingWindowListener) component);
      }
      viewMap.addView(key, view);
      views.put(key, view);
    }

    RootWindow window = null;
    try {
      if (serializedViewData != null && serializedViewData.exists()) {
        window = read(viewMap, views, serializedViewData);
      } else {
        window = createDefault(viewMap, views);
      }
      if (window == null) {
        ErrorWindow errorWindow = new ErrorWindow("Could not load docking layouts.");
        errorWindow.open();
      }
    } catch (NullPointerException npe) {
      // okay, sometimes this happens without a reason (setToolTipText())...
      DockingFrameworkBuilder.log.error("NPE", npe);
    } catch (Throwable t) {
      DockingFrameworkBuilder.log.fatal(t.getMessage(), t);
      ErrorWindow errorWindow = new ErrorWindow(Client.INSTANCE, t.getMessage(), "", t);
      errorWindow.setVisible(true);
    }

    DockingWindowsTheme theme = new ShapedGradientDockingTheme();
    window.getWindowBar(Direction.DOWN).setEnabled(true);
    window.setPopupMenuFactory(new MagellanPopupMenuFactory(viewMap));

    RootWindowProperties prop = window.getRootWindowProperties();
    prop.addSuperObject(theme.getRootWindowProperties());

    prop.getWindowAreaProperties().setBackgroundColor(null).setBorder(null);
    prop.getWindowAreaShapedPanelProperties().setComponentPainter(null);
    prop.getComponentProperties().setBackgroundColor(null);
    prop.getShapedPanelProperties().setComponentPainter(null);

    prop.getWindowAreaProperties().setInsets(new Insets(0, 0, 0, 0));
    prop.getWindowAreaProperties().setBorder(null);
    prop.getWindowBarProperties().getComponentProperties().setInsets(new Insets(3, 0, 3, 0));
    prop.getWindowBarProperties().getComponentProperties().setBorder(null);
    prop.getComponentProperties().setInsets(new Insets(3, 3, 0, 3));
    prop.getComponentProperties().setBorder(null);

    return window;
  }

  public StringViewMap getViewMap() {
    return viewMap;
  }

  /**
   * This method writes a docking configuration to the given file.
   */
  public void write(File serializedViewData) throws IOException {
    DockingFrameworkBuilder.log.info("Storing docking layout in " + serializedViewData);
    StringBuffer buffer = new StringBuffer();
    buffer.append("<?xml version='1.0' encoding='" + Encoding.DEFAULT.toString() + "'?>\r\n");
    buffer.append("<dock version='1.0'>\r\n");
    for (DockingLayout layout : DockingFrameworkBuilder.layouts) {
      layout.save(buffer);
    }
    buffer.append("</dock>\r\n");

    // System.out.println(buffer);

    PrintWriter pw = new PrintWriter(serializedViewData, Encoding.DEFAULT.toString());
    pw.println(buffer.toString());
    pw.close();
  }

  /**
   * This method reads a docking configuration from the given file.
   */
  public synchronized RootWindow read(StringViewMap viewMap, Map<String, View> views,
      File serializedViewData) throws IOException {
    DockingFrameworkBuilder.log.info("Loading Docking Layouts");

    this.viewMap = viewMap;
    this.views = views;

    RootWindow window = DockingUtil.createRootWindow(viewMap, true);

    try {
      DockingFrameworkBuilder.layouts = DockingLayout.load(serializedViewData, viewMap, views);

      DockingFrameworkBuilder.log.info("Loaded " + DockingFrameworkBuilder.layouts.size()
          + " Docking layouts.");
      for (DockingLayout layout : DockingFrameworkBuilder.layouts) {
        if (layout.isActive()) {
          DockingFrameworkBuilder.activeLayout = layout;
          break;
        }
      }

      if (DockingFrameworkBuilder.activeLayout == null
          && DockingFrameworkBuilder.layouts.size() > 0) {
        DockingFrameworkBuilder.activeLayout = DockingFrameworkBuilder.layouts.get(0);
      }
      DockingFrameworkBuilder.activeLayout.setActive(true);
      DockingFrameworkBuilder.activeLayout.open(window, settings);

    } catch (Exception exception) {
      DockingFrameworkBuilder.log.error(exception);
      ErrorWindow errorWindow = new ErrorWindow("Could not load docking layouts.", exception);
      errorWindow.open();
    }

    return window;
  }

  /**
   * Adds all docking layouts from the given file to the currently available layouts. all layouts
   * are disabled and layouts with the same name are renamed
   */
  public synchronized void addLayouts(File file) {
    List<DockingLayout> newLayouts = DockingLayout.load(file, viewMap, views);
    if (newLayouts != null) {
      for (DockingLayout layout : newLayouts) {
        layout.setActive(false);
        layout.setName(findNewName(DockingFrameworkBuilder.layouts, layout.getName(), layout
            .getName(), 0));
        DockingFrameworkBuilder.layouts.add(layout);
      }
      updateLayoutMenu();
    }
  }

  /**
   * Searches for a layout with the given name. If it is not found, then the name is returned. If it
   * is found, then it returns the name "name (x)".
   */
  private synchronized String findNewName(List<DockingLayout> layouts, String originalName,
      String name, int suffix) {
    for (DockingLayout layout : layouts) {
      if (layout.getName().equalsIgnoreCase(name)) {
        String newName = originalName + " (" + (++suffix) + ")";
        return findNewName(layouts, originalName, newName, suffix);
      }
    }
    return name;
  }

  /**
   * Creates a default Docking layout set.
   */
  protected synchronized RootWindow createDefault(StringViewMap viewMap, Map<String, View> views) {
    RootWindow window = DockingUtil.createRootWindow(viewMap, true);
    Element root;
    try {
      root = DockingLayout.createDefaultLayout("Standard", true);
    } catch (LayoutException e) {
      ErrorWindow errorWindow = new ErrorWindow("Could not create default docking layout.", e);
      errorWindow.open();
      throw new RuntimeException(e);
    }
    DockingLayout defaultLayout = new DockingLayout("Standard", root, viewMap, views);
    defaultLayout.setActive(true);
    DockingFrameworkBuilder.layouts.add(defaultLayout);

    DockingFrameworkBuilder.activeLayout = defaultLayout;
    DockingFrameworkBuilder.activeLayout.open(window, settings);

    return window;
  }

  /**
   * This method creates the desktop menu. It contains the layout submenu and all dock components.
   */
  public JMenu createDesktopMenu(Map<String, Component> components, Properties settings,
      ActionListener listener) {
    JMenu desktopMenu = new JMenu(Resources.get("desktop.magellandesktop.menu.desktop.caption"));
    desktopMenu.setMnemonic(Resources.get("desktop.magellandesktop.menu.desktop.mnemonic")
        .charAt(0));

    DockingFrameworkBuilder.layoutMenu =
        new JMenu(Resources.get("desktop.magellandesktop.menu.desktop.layout.caption"));

    ButtonGroup group = new ButtonGroup();
    for (DockingLayout layout : DockingFrameworkBuilder.layouts) {
      LayoutCheckboxMenuItem item = new LayoutCheckboxMenuItem(layout);
      group.add(item);
      DockingFrameworkBuilder.layoutMenu.add(item);
    }

    DockingFrameworkBuilder.deleteMenu = new LayoutDeleteAction();
    DockingFrameworkBuilder.deleteMenu.setEnabled(DockingFrameworkBuilder.layouts.size() > 1);

    DockingFrameworkBuilder.layoutMenu.addSeparator();
    DockingFrameworkBuilder.layoutMenu.add(new LayoutExportAction());
    DockingFrameworkBuilder.layoutMenu.add(new LayoutImportAction());
    DockingFrameworkBuilder.layoutMenu.addSeparator();
    DockingFrameworkBuilder.layoutMenu.add(new LayoutNewAction());
    DockingFrameworkBuilder.layoutMenu.add(new LayoutSaveAction());
    DockingFrameworkBuilder.layoutMenu.add(DockingFrameworkBuilder.deleteMenu);

    desktopMenu.add(DockingFrameworkBuilder.layoutMenu);

    DockingFrameworkBuilder.hideTabs =
        new JCheckBoxMenuItem(Resources
            .get("desktop.magellandesktop.menu.desktop.hidetabs.caption"), PropertiesHelper
            .getBoolean(settings, PropertiesHelper.CLIENTPREFERENCES_DONT_SHOW_TABS, false));
    DockingFrameworkBuilder.hideTabs.setActionCommand("hideTabs");
    desktopMenu.add(DockingFrameworkBuilder.hideTabs);
    DockingFrameworkBuilder.hideTabs.addActionListener(listener);

    desktopMenu.addSeparator();

    if (components.size() > 0) {
      for (String key : components.keySet()) {
        if (key.equals(MagellanDesktop.COMMANDS_IDENTIFIER)) {
          continue; // deprecated
        }
        if (key.equals(MagellanDesktop.NAME_IDENTIFIER)) {
          continue; // deprecated
        }
        if (key.equals(MagellanDesktop.DESCRIPTION_IDENTIFIER)) {
          continue; // deprecated
        }
        if (key.equals(MagellanDesktop.OVERVIEWHISTORY_IDENTIFIER)) {
          continue; // deprecated
        }

        JCheckBoxMenuItem item =
            new JCheckBoxMenuItem(Resources.get("dock." + key + ".title"), false);
        item.setActionCommand("menu." + key);
        desktopMenu.add(item);
        item.addActionListener(listener);
      }
    }

    return desktopMenu;
  }

  /**
   * Updates all Docking Layouts in the Desktop>Layout menu by removing them and recreate the list
   * of available docking layouts.
   */
  public void updateLayoutMenu() {
    if (DockingFrameworkBuilder.layoutMenu == null)
      return;

    // remove all layout items from the menu
    for (int i = 0; i < DockingFrameworkBuilder.layoutMenu.getItemCount(); i++) {
      if (DockingFrameworkBuilder.layoutMenu.getItem(i) instanceof LayoutCheckboxMenuItem) {
        DockingFrameworkBuilder.log.debug("Removing Layout Menu Entry "
            + DockingFrameworkBuilder.layoutMenu.getItem(i).getText());
        DockingFrameworkBuilder.layoutMenu.remove(i);
        i--;
      } else if (DockingFrameworkBuilder.layoutMenu.getItem(i) != null) {
        DockingFrameworkBuilder.log.debug("Don't remove Menu Entry "
            + DockingFrameworkBuilder.layoutMenu.getItem(i).getText() + " ("
            + DockingFrameworkBuilder.layoutMenu.getItem(i).getClass().getName() + ")");
      }
    }

    // add all available items.
    int i = 0;
    ButtonGroup group = new ButtonGroup();
    for (DockingLayout layout : DockingFrameworkBuilder.layouts) {
      LayoutCheckboxMenuItem item = new LayoutCheckboxMenuItem(layout);
      group.add(item);
      DockingFrameworkBuilder.log.debug("Add Layout Menu Entry (" + i + "): " + layout.getName());
      DockingFrameworkBuilder.layoutMenu.insert(item, i++);
    }

    DockingFrameworkBuilder.deleteMenu.setEnabled(DockingFrameworkBuilder.layouts.size() > 1);
  }

  /**
   * 
   */
  public void setTabVisibility(boolean showTabs) {
    if (DockingFrameworkBuilder.hideTabs != null) {
      DockingFrameworkBuilder.hideTabs.setSelected(!showTabs);
    }
  }

  /**
   * Returns the docking layout with the given name or null, if there is no layout with this name.
   */
  public DockingLayout getLayout(String name) {
    for (DockingLayout layout : DockingFrameworkBuilder.layouts) {
      if (layout.getName().equalsIgnoreCase(name))
        return layout;
    }
    return null;
  }

  /**
   * This method creates a new layout with the given name and the default settings.
   */
  public void createNewLayout(String name) {
    Element root;
    try {
      root = DockingLayout.createDefaultLayout(name, false);
    } catch (LayoutException e) {
      ErrorWindow errorWindow = new ErrorWindow("Could not create default docking layout.", e);
      errorWindow.open();
      throw new RuntimeException(e);
    }

    DockingLayout layout = new DockingLayout(name, root, viewMap, views);
    DockingFrameworkBuilder.layouts.add(layout);

    setActiveLayout(layout);

    updateLayoutMenu();
  }

  /**
   * Deletes the current layout and selects the first layout.
   */
  public void deleteCurrentLayout() {
    if (DockingFrameworkBuilder.layouts.size() <= 1)
      return; // nene...
    if (DockingFrameworkBuilder.activeLayout == null)
      return; // ham we nich.

    DockingLayout deletableLayout = DockingFrameworkBuilder.activeLayout;
    DockingFrameworkBuilder.log.debug("Remove docking layout '" + deletableLayout.getName() + "'");
    int index = DockingFrameworkBuilder.layouts.indexOf(DockingFrameworkBuilder.activeLayout);
    if (index == 0) {
      index = 1;
    } else {
      index = 0; // if the first layout is active, use the second layout.
    }
    DockingFrameworkBuilder.log.debug("index:" + index);
    setActiveLayout(DockingFrameworkBuilder.layouts.get(index));

    DockingFrameworkBuilder.layouts.remove(deletableLayout);

    updateLayoutMenu();
  }

  /**
   * Enabled the specified docking layout.
   */
  public void setActiveLayout(DockingLayout layout) {
    DockingFrameworkBuilder.log.info("Set docking layout '" + layout.getName() + "'");
    RootWindow window = null;
    if (DockingFrameworkBuilder.activeLayout != null) {
      DockingFrameworkBuilder.activeLayout.setActive(false);
      DockingFrameworkBuilder.activeLayout.dispose();
      window = DockingFrameworkBuilder.activeLayout.getRootWindow();
    }
    DockingFrameworkBuilder.activeLayout = layout;
    DockingFrameworkBuilder.activeLayout.setActive(true);
    DockingFrameworkBuilder.activeLayout.open(window, settings);
  }

  /**
   * Enables a desktop menu entry for the given View
   */
  public void setActive(View view) {
    if (view == null)
      return;
    MagellanDesktop.getInstance().setActive(view.getName());
  }

  /**
   * Disables a desktop menu entry for the given View
   */
  public void setInActive(View view) {
    if (view == null)
      return;
    MagellanDesktop.getInstance().setInActive(view.getName());
  }

  /**
   * Returns the list of used components
   */
  public List<Component> getComponentsUsed() {
    return componentsUsed;
  }

  /**
   * 
   */
  public void setProperties(Properties settings) {
    this.settings = settings;
  }

  /**
   * Opens or closes a specific dock.
   */
  public void setVisible(RootWindow window, String componentName, boolean setVisible) {
    if (views != null && views.containsKey(componentName)) {
      View view = views.get(componentName);
      if (view != null) {
        if (setVisible) {
          view.restore();
          view.makeVisible();
        } else {
          view.close();
        }
      }
    }
  }
}
