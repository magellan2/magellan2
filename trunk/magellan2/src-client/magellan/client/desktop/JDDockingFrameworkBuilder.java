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
import java.awt.Window;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
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
import magellan.client.desktop.JDMagellanDesktop.RootWindow;
import magellan.client.utils.ErrorWindow;
import magellan.library.utils.Encoding;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

import org.w3c.dom.Element;

import com.javadocking.DockingManager;
import com.javadocking.dock.BorderDock;
import com.javadocking.dock.Dock;
import com.javadocking.dock.Position;
import com.javadocking.dock.SingleDock;
import com.javadocking.dock.docker.BorderDocker;
import com.javadocking.dock.factory.ToolBarDockFactory;
import com.javadocking.dockable.DefaultDockable;
import com.javadocking.dockable.Dockable;
import com.javadocking.dockable.DockableState;
import com.javadocking.dockable.StateActionDockable;
import com.javadocking.dockable.action.DefaultDockableStateActionFactory;
import com.javadocking.event.DockingListener;
import com.javadocking.model.FloatDockModel;
import com.javadocking.visualizer.DockingMinimizer;
import com.javadocking.visualizer.FloatExternalizer;
import com.javadocking.visualizer.SingleMaximizer;

/**
 * This is a factory for working with the Sanaware Java Docking Framework.
 * 
 * @author Thoralf
 * @author stm
 * @version 1.0
 * @deprecated unfinished
 */
@Deprecated
public class JDDockingFrameworkBuilder {
  private static final Logger log = Logger.getInstance(JDDockingFrameworkBuilder.class);
  private static JDDockingFrameworkBuilder _instance = null;
  private List<Component> componentsUsed;
  private Map<String, Dockable> viewMap = null;
  private Map<String, Dockable> views = null;

  /** Holds value of property screen. */

  private static JMenu layoutMenu = null;
  private static JCheckBoxMenuItem hideTabs = null;
  private static LayoutDeleteAction deleteMenu = null;
  private static List<JDDockingLayout> layouts = new ArrayList<JDDockingLayout>();
  private static JDDockingLayout activeLayout = null;
  private Properties settings = null;

  /**
   * Creates new DockingFrameworkBuilder
   */
  private JDDockingFrameworkBuilder() {
    componentsUsed = new LinkedList<Component>();
  }

  public static JDDockingFrameworkBuilder getInstance() {
    if (JDDockingFrameworkBuilder._instance == null) {
      JDDockingFrameworkBuilder._instance = new JDDockingFrameworkBuilder();
    }
    return JDDockingFrameworkBuilder._instance;
  }

  /**
   * This method builds the desktop. This is the main component inside Magellan It contains a IDF
   * RootWindow with multiple Docks.
   * 
   * @param ownerWindow
   */
  public RootWindow buildDesktop(Map<String, Component> components, File serializedView,
      Window ownerWindow) {
    componentsUsed.clear();
    for (Component component : components.values()) {
      componentsUsed.add(component);
    }

    // we have a tree of settings and a list of components. Let's build a root window here...
    return createRootWindow(components, serializedView, ownerWindow);
  }

  /**
   * This method tries to setup the infonode docking framework.
   * 
   * @param ownerWindow
   */
  protected RootWindow createRootWindow(Map<String, Component> components, File serializedViewData,
      Window ownerWindow) {
    views = new HashMap<String, Dockable>();
    viewMap = new HashMap<String, Dockable>();

    for (String key : components.keySet()) {
      if (key.equals("COMMANDS")) {
        continue; // deprecated
      }
      if (key.equals("NAME")) {
        continue; // deprecated
      }
      if (key.equals("DESCRIPTION")) {
        continue; // deprecated
      }
      if (key.equals("OVERVIEW&HISTORY")) {
        continue; // deprecated
      }

      Component component = components.get(key);

      Dockable view = new DefaultDockable(key, component, Resources.get("dock." + key + ".title"));
      view = addActions(view);
      // view.setName(key);
      // view.setToolTipText(Resources.get("dock." + key + ".tooltip"));
      if (component instanceof DockingListener) {
        view.addDockingListener((DockingListener) component);
      }
      viewMap.put(key, view);
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
        return null;
      }
      FloatDockModel dockModel = new FloatDockModel();
      dockModel.addOwner("client", ownerWindow);
      dockModel.addRootDock("rootwindow", window, ownerWindow);
      DockingManager.setDockModel(dockModel);

      // Create an externalizer.
      FloatExternalizer externalizer = new FloatExternalizer(ownerWindow);
      dockModel.addVisualizer("externalizer", externalizer, ownerWindow);

      // // Create a minimizer.
      // LineMinimizer minimizer = new LineMinimizer(window);
      // dockModel.addVisualizer("minimizer", minimizer, ownerWindow);

      // // Create a maximizer.
      // SingleMaximizer maximizer = new SingleMaximizer(minimizer);
      // dockModel.addVisualizer("maximizer", maximizer, ownerWindow);

      // Create a maximizer and add it to the dock model.
      SingleDock maxPanel = new SingleDock();
      window.addChildDock(maxPanel, new Position());
      SingleMaximizer maximizer = new SingleMaximizer(maxPanel);
      dockModel.addVisualizer("maximizer", maximizer, ownerWindow);

      // Create a docking minimizer.
      BorderDock borderDock = new BorderDock(new ToolBarDockFactory());
      borderDock.setMode(BorderDock.MODE_MINIMIZE_BAR);
      borderDock.setCenterComponent(maximizer);
      BorderDocker borderDocker = new BorderDocker();
      borderDocker.setBorderDock(borderDock);
      DockingMinimizer minimizer = new DockingMinimizer(borderDocker);

      // Add the minimizer to the dock model, add also the border dock used by the minimizer to the
      // dock model.
      dockModel.addVisualizer("minimizer", minimizer, ownerWindow);
      dockModel.addRootDock("minimizerBorderDock", borderDock, ownerWindow);

      // Add the border dock of the minimizer to this panel.
      window.addChildDock(borderDock, new Position(1));
      // ownerWindow.add(borderDock);

    } catch (NullPointerException npe) {
      // okay, sometimes this happens without a reason (setToolTipText())...
      JDDockingFrameworkBuilder.log.error("NPE", npe);
    } catch (Throwable t) {
      JDDockingFrameworkBuilder.log.fatal(t.getMessage(), t);
      ErrorWindow errorWindow = new ErrorWindow(Client.INSTANCE, t.getMessage(), "", t);
      errorWindow.setVisible(true);
    }

    // DockingWindowsTheme theme = new ShapedGradientDockingTheme();
    // // DockingWindowsTheme theme = new BlueHighlightDockingTheme();
    // // DockingWindowsTheme theme = new ClassicDockingTheme();
    // // DockingWindowsTheme theme = new DefaultDockingTheme();
    // // DockingWindowsTheme theme = new GradientDockingTheme();
    // // DockingWindowsTheme theme = new LookAndFeelDockingTheme();
    // // DockingWindowsTheme theme = new SlimFlatDockingTheme();
    // // DockingWindowsTheme theme = new SoftBlueIceDockingTheme();
    // window.getWindowBar(Direction.DOWN).setEnabled(true);
    // window.setPopupMenuFactory(new MagellanPopupMenuFactory(viewMap));
    //
    // RootWindowProperties prop = window.getRootWindowProperties();
    // prop.addSuperObject(theme.getRootWindowProperties());
    //
    // prop.getWindowAreaProperties().setBackgroundColor(null).setBorder(null);
    // prop.getWindowAreaShapedPanelProperties().setComponentPainter(null);
    // prop.getComponentProperties().setBackgroundColor(null);
    // prop.getShapedPanelProperties().setComponentPainter(null);
    //
    // prop.getWindowAreaProperties().setInsets(new Insets(0, 0, 0, 0));
    // prop.getWindowAreaProperties().setBorder(null);
    // prop.getWindowBarProperties().getComponentProperties().setInsets(new Insets(3, 0, 3, 0));
    // prop.getWindowBarProperties().getComponentProperties().setBorder(null);
    // prop.getComponentProperties().setInsets(new Insets(3, 3, 0, 3));
    // prop.getComponentProperties().setBorder(null);

    return window;
  }

  /**
   * Decorates the given dockable with a state actions.
   * 
   * @param dockable The dockable to decorate.
   * @return The wrapper around the given dockable, with actions.
   */
  private Dockable addActions(Dockable dockable) {

    Dockable wrapper =
        new StateActionDockable(dockable, new DefaultDockableStateActionFactory(), new int[0]);
    int[] states =
        { DockableState.NORMAL, DockableState.MINIMIZED, DockableState.MAXIMIZED,
            DockableState.EXTERNALIZED, DockableState.CLOSED };
    wrapper = new StateActionDockable(wrapper, new DefaultDockableStateActionFactory(), states);
    return wrapper;

  }

  public Map<String, Dockable> getViewMap() {
    return viewMap;
  }

  /**
   * This method writes a docking configuration to the given file.
   */
  public void write(File serializedViewData) throws IOException {
    JDDockingFrameworkBuilder.log.info("Storing docking layout in " + serializedViewData);
    StringBuffer buffer = new StringBuffer();
    buffer.append("<?xml version='1.0' encoding='" + Encoding.DEFAULT.toString() + "'?>\r\n");
    buffer.append("<dock version='1.0'>\r\n");
    for (JDDockingLayout layout : JDDockingFrameworkBuilder.layouts) {
      layout.save(buffer);
    }
    buffer.append("</dock>\r\n");

    log.fine(buffer);

    // PrintWriter pw = new PrintWriter(serializedViewData, Encoding.DEFAULT.toString());
    // pw.println(buffer.toString());
    // pw.close();
  }

  /**
   * This method reads a docking configuration from the given file.
   */
  public synchronized RootWindow read(Map<String, Dockable> viewMap, Map<String, Dockable> views,
      File serializedViewData) throws IOException {
    JDDockingFrameworkBuilder.log.info("Loading Docking Layouts");

    this.viewMap = viewMap;
    this.views = views;

    // DockingUtil.createRootWindow(new StringViewMap(), true);
    RootWindow window = createRootWindow(viewMap, true);

    try {
      JDDockingFrameworkBuilder.layouts = JDDockingLayout.load(serializedViewData, viewMap, views);

      JDDockingFrameworkBuilder.log.info("Loaded " + JDDockingFrameworkBuilder.layouts.size()
          + " Docking layouts.");
      for (JDDockingLayout layout : JDDockingFrameworkBuilder.layouts) {
        if (layout.isActive()) {
          JDDockingFrameworkBuilder.activeLayout = layout;
          break;
        }
      }

      if (JDDockingFrameworkBuilder.activeLayout == null
          && JDDockingFrameworkBuilder.layouts.size() > 0) {
        JDDockingFrameworkBuilder.activeLayout = JDDockingFrameworkBuilder.layouts.get(0);
      }
      JDDockingFrameworkBuilder.activeLayout.setActive(true);
      JDDockingFrameworkBuilder.activeLayout.open(window, settings);

    } catch (Exception exception) {
      JDDockingFrameworkBuilder.log.error(exception);
      ErrorWindow errorWindow = new ErrorWindow("Could not load docking layouts.", exception);
      errorWindow.open();
    }

    return window;
  }

  private RootWindow createRootWindow(Map<String, Dockable> viewMap2, boolean b) {
    RootWindow tabWindow = new RootWindow();

    // int i = 0;
    // for (Dockable view : viewMap2.values()) {
    // SingleDock dock = new SingleDock();
    // dock.addDockable(view, null);
    // tabWindow.addChildDock(dock, new Position(i++));
    // }

    // if (createWindowPopupMenu)
    // rootWindow.setPopupMenuFactory(WindowMenuUtil.createWindowMenuFactory(views, true));

    return tabWindow;
  }

  /**
   * Adds all docking layouts from the given file to the currently available layouts. all layouts
   * are disabled and layouts with the same name are renamed
   */
  public synchronized void addLayouts(File file) {
    List<JDDockingLayout> newLayouts = JDDockingLayout.load(file, viewMap, views);
    if (newLayouts != null) {
      for (JDDockingLayout layout : newLayouts) {
        layout.setActive(false);
        layout.setName(findNewName(JDDockingFrameworkBuilder.layouts, layout.getName(), layout
            .getName(), 0));
        JDDockingFrameworkBuilder.layouts.add(layout);
      }
      updateLayoutMenu();
    }
  }

  /**
   * Searches for a layout with the given name. If it is not found, then the name is returned. If it
   * is found, then it returns the name "name (x)".
   */
  private synchronized String findNewName(List<JDDockingLayout> layouts, String originalName,
      String name, int suffix) {
    for (JDDockingLayout layout : layouts) {
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
  protected synchronized RootWindow createDefault(Map<String, Dockable> viewMap,
      Map<String, Dockable> views) {
    RootWindow window = new RootWindow();
    // DockingUtil.createRootWindow(viewMap, true);
    Element root = JDDockingLayout.createDefaultLayout("Standard", true);
    if (root == null) {
      ErrorWindow errorWindow = new ErrorWindow("Could not create default docking layout.");
      errorWindow.open();
    }
    JDDockingLayout defaultLayout = new JDDockingLayout("Standard", root, viewMap, views);
    defaultLayout.setActive(true);
    JDDockingFrameworkBuilder.layouts.add(defaultLayout);

    JDDockingFrameworkBuilder.activeLayout = defaultLayout;
    JDDockingFrameworkBuilder.activeLayout.open(window, settings);

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

    JDDockingFrameworkBuilder.layoutMenu =
        new JMenu(Resources.get("desktop.magellandesktop.menu.desktop.layout.caption"));

    ButtonGroup group = new ButtonGroup();
    for (JDDockingLayout layout : JDDockingFrameworkBuilder.layouts) {
      // FIXME !!!!!
      // LayoutCheckboxMenuItem item = new LayoutCheckboxMenuItem(layout);
      LayoutCheckboxMenuItem item = null;
      group.add(item);
      JDDockingFrameworkBuilder.layoutMenu.add(item);
    }

    JDDockingFrameworkBuilder.deleteMenu = new LayoutDeleteAction();
    JDDockingFrameworkBuilder.deleteMenu.setEnabled(JDDockingFrameworkBuilder.layouts.size() > 1);

    JDDockingFrameworkBuilder.layoutMenu.addSeparator();
    JDDockingFrameworkBuilder.layoutMenu.add(new LayoutExportAction());
    JDDockingFrameworkBuilder.layoutMenu.add(new LayoutImportAction());
    JDDockingFrameworkBuilder.layoutMenu.addSeparator();
    JDDockingFrameworkBuilder.layoutMenu.add(new LayoutNewAction());
    JDDockingFrameworkBuilder.layoutMenu.add(new LayoutSaveAction());
    JDDockingFrameworkBuilder.layoutMenu.add(JDDockingFrameworkBuilder.deleteMenu);

    desktopMenu.add(JDDockingFrameworkBuilder.layoutMenu);

    JDDockingFrameworkBuilder.hideTabs =
        new JCheckBoxMenuItem(Resources
            .get("desktop.magellandesktop.menu.desktop.hidetabs.caption"), PropertiesHelper
            .getBoolean(settings, PropertiesHelper.CLIENTPREFERENCES_DONT_SHOW_TABS, false));
    JDDockingFrameworkBuilder.hideTabs.setActionCommand("hideTabs");
    desktopMenu.add(JDDockingFrameworkBuilder.hideTabs);
    JDDockingFrameworkBuilder.hideTabs.addActionListener(listener);

    desktopMenu.addSeparator();

    if (components.size() > 0) {
      for (String key : components.keySet()) {
        if (key.equals("COMMANDS")) {
          continue; // deprecated
        }
        if (key.equals("NAME")) {
          continue; // deprecated
        }
        if (key.equals("DESCRIPTION")) {
          continue; // deprecated
        }
        if (key.equals("OVERVIEW&HISTORY")) {
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
    if (JDDockingFrameworkBuilder.layoutMenu == null)
      return;

    // remove all layout items from the menu
    for (int i = 0; i < JDDockingFrameworkBuilder.layoutMenu.getItemCount(); i++) {
      if (JDDockingFrameworkBuilder.layoutMenu.getItem(i) instanceof LayoutCheckboxMenuItem) {
        JDDockingFrameworkBuilder.log.debug("Removing Layout Menu Entry "
            + JDDockingFrameworkBuilder.layoutMenu.getItem(i).getText());
        JDDockingFrameworkBuilder.layoutMenu.remove(i);
        i--;
      } else if (JDDockingFrameworkBuilder.layoutMenu.getItem(i) != null) {
        JDDockingFrameworkBuilder.log.debug("Don't remove Menu Entry "
            + JDDockingFrameworkBuilder.layoutMenu.getItem(i).getText() + " ("
            + JDDockingFrameworkBuilder.layoutMenu.getItem(i).getClass().getName() + ")");
      }
    }

    // add all available items.
    int i = 0;
    ButtonGroup group = new ButtonGroup();
    for (JDDockingLayout layout : JDDockingFrameworkBuilder.layouts) {
      // FIXME !!!!
      // LayoutCheckboxMenuItem item = new LayoutCheckboxMenuItem(layout);
      LayoutCheckboxMenuItem item = null;
      group.add(item);
      JDDockingFrameworkBuilder.log.debug("Add Layout Menu Entry (" + i + "): " + layout.getName());
      JDDockingFrameworkBuilder.layoutMenu.insert(item, i++);
    }

    JDDockingFrameworkBuilder.deleteMenu.setEnabled(JDDockingFrameworkBuilder.layouts.size() > 1);
  }

  /**
   * 
   */
  public void setTabVisibility(boolean showTabs) {
    if (JDDockingFrameworkBuilder.hideTabs != null) {
      JDDockingFrameworkBuilder.hideTabs.setSelected(!showTabs);
    }
  }

  /**
   * Returns the docking layout with the given name or null, if there is no layout with this name.
   */
  public JDDockingLayout getLayout(String name) {
    for (JDDockingLayout layout : JDDockingFrameworkBuilder.layouts) {
      if (layout.getName().equalsIgnoreCase(name))
        return layout;
    }
    return null;
  }

  /**
   * This method creates a new layout with the given name and the default settings.
   */
  public void createNewLayout(String name) {
    Element root = JDDockingLayout.createDefaultLayout(name, false);

    JDDockingLayout layout = new JDDockingLayout(name, root, viewMap, views);
    JDDockingFrameworkBuilder.layouts.add(layout);

    setActiveLayout(layout);

    updateLayoutMenu();
  }

  /**
   * Deletes the current layout and selects the first layout.
   */
  public void deleteCurrentLayout() {
    if (JDDockingFrameworkBuilder.layouts.size() <= 1)
      return; // nene...
    if (JDDockingFrameworkBuilder.activeLayout == null)
      return; // ham we nich.

    JDDockingLayout deletableLayout = JDDockingFrameworkBuilder.activeLayout;
    JDDockingFrameworkBuilder.log
        .debug("Remove docking layout '" + deletableLayout.getName() + "'");
    int index = JDDockingFrameworkBuilder.layouts.indexOf(JDDockingFrameworkBuilder.activeLayout);
    if (index == 0) {
      index = 1;
    } else {
      index = 0; // if the first layout is active, use the second layout.
    }
    JDDockingFrameworkBuilder.log.debug("index:" + index);
    setActiveLayout(JDDockingFrameworkBuilder.layouts.get(index));

    JDDockingFrameworkBuilder.layouts.remove(deletableLayout);

    updateLayoutMenu();
  }

  /**
   * Enabled the specified docking layout.
   */
  public void setActiveLayout(JDDockingLayout layout) {
    JDDockingFrameworkBuilder.log.info("Set docking layout '" + layout.getName() + "'");
    RootWindow window = null;
    if (JDDockingFrameworkBuilder.activeLayout != null) {
      JDDockingFrameworkBuilder.activeLayout.setActive(false);
      JDDockingFrameworkBuilder.activeLayout.dispose();
      window = JDDockingFrameworkBuilder.activeLayout.getRootWindow();
    }
    JDDockingFrameworkBuilder.activeLayout = layout;
    JDDockingFrameworkBuilder.activeLayout.setActive(true);
    JDDockingFrameworkBuilder.activeLayout.open(window, settings);
  }

  /**
   * Enables a desktop menu entry for the given View
   */
  public void setActive(Dockable view) {
    if (view == null)
      return;
    JDMagellanDesktop.getInstance().setActive(view.getID());
  }

  /**
   * Disables a desktop menu entry for the given View
   */
  public void setInActive(Dockable view) {
    if (view == null)
      return;
    JDMagellanDesktop.getInstance().setInActive(view.getID());
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
  public void setVisible(Dock window, String componentName, boolean setVisible) {
    if (views != null && views.containsKey(componentName)) {
      Dockable view = views.get(componentName);
      if (view != null) {
        if (setVisible) {
          view.setState(DockableState.NORMAL, null);
          // view.restore();
          // view.makeVisible();
        } else {
          view.setState(DockableState.NORMAL, window);
          // view.close();
        }
      }
    }
  }
}
