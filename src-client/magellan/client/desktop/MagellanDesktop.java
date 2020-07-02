/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.client.desktop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import magellan.client.Client;
import magellan.client.MagellanContext;
import magellan.client.event.EventDispatcher;
import magellan.client.preferences.DesktopPreferences;
import magellan.client.swing.desktop.WorkSpace;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.client.utils.ErrorWindow;
import magellan.client.utils.SwingUtils;
import magellan.library.event.GameDataEvent;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.logging.Logger;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowListener;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.RootWindow;
import net.infonode.docking.View;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.tabbedpanel.TabAreaVisiblePolicy;

/**
 * This class represents the Magellan Desktop. It contains all visible components. We use this class
 * to load, set and save their positions.
 *
 * @author Roger Butenuth
 * @author Andreas Gampe
 * @author Thoralf Rickert
 */
public class MagellanDesktop extends JPanel implements WindowListener, ActionListener,
    PreferencesFactory, DockingWindowListener {
  private static final Logger log = Logger.getInstance(MagellanDesktop.class);

  private static MagellanDesktop _instance = null;

  /** the workSpace associated with this MagellanDesktop */
  private WorkSpace workSpace;

  /** The name of the docking layout file */
  public static final String DOCKING_LAYOUT_FILE = "dock-default.xml";

  /** Name of MAP component */
  public static final String MAP_IDENTIFIER = "MAP";
  /** Name of MINIMAP component */
  public static final String MINIMAP_IDENTIFIER = "MINIMAP";
  /** Name of ECheck component */
  public static final String ECHECK_IDENTIFIER = "ECHECK";
  /** Name of the MESSAGES component */
  public static final String MESSAGES_IDENTIFIER = "MESSAGES";
  /** Name of the NAME&amp;DESCRIPTION component */
  public static final String NAMEDESCRIPTION_IDENTIFIER = "NAME&DESCRIPTION";
  /** Name of the NAME component */
  public static final String NAME_IDENTIFIER = "NAME";
  /** Name of the DESCRIPTION component */
  public static final String DESCRIPTION_IDENTIFIER = "DESCRIPTION";
  /** Name of the DETAILS component */
  public static final String DETAILS_IDENTIFIER = "DETAILS";
  /** Name of the ORDERS component */
  public static final String ORDERS_IDENTIFIER = "ORDERS";
  /** Name of the COMMANDS component */
  public static final String COMMANDS_IDENTIFIER = "COMMANDS";
  /** Name of the OVERVIEW component */
  public static final String OVERVIEW_IDENTIFIER = "OVERVIEW";
  /** Name of the HISTORY component */
  public static final String HISTORY_IDENTIFIER = "HISTORY";
  /** Name of the OVERVIEW&amp;HISTORY component */
  public static final String OVERVIEWHISTORY_IDENTIFIER = "OVERVIEW&HISTORY";
  /** Name of the TASKS component */
  public static final String TASKS_IDENTIFIER = "TASKS";
  /** Name of the BOOKMARKS component */
  public static final String BOOKMARKS_IDENTIFIER = "BOOKMARKS";
  /** Name of the DEBUG component */
  public static final String DEBUG_IDENTIFIER = "DEBUG";

  /**
   * Holds all the components. The key is the global id like NAME or OVERVIEW, the value is the
   * component.
   */
  private Map<String, Component> components;

  /** Some shortcut things */
  private Map<KeyStroke, Object> shortCutListeners = new HashMap<KeyStroke, Object>();
  private Map<KeyStroke, KeyStroke> shortCutTranslations = new HashMap<KeyStroke, KeyStroke>();
  private KeyHandler keyHandler;

  /** Decides if all frames should be (de)iconified if the client frame is (de)iconified. */
  private boolean iconify;

  /** Holds the settings in case a mode-change occurs. */
  private Properties settings;

  /**
   * Stores the root component of the splitted desktop. So the desktop can be saved even if a
   * mode-changed occured.
   */
  private RootWindow splitRoot;

  /** Holds the client frame. This is for (de)iconification compares. */
  private Client client;

  /** Holds the frame rect for Magellan in Split-Mode. */
  private Rectangle splitRect;

  /**
   * Just a variable to suppress double activation events. If anyone can do it better, please DO IT.
   */
  private boolean inFront = false;
  private Timer timer;

  // Split mode objects
  private DockingFrameworkBuilder dockingFrameworkBuilder;

  // Desktop menu
  private JMenu desktopMenu;

  private File settingsDir = null;
  private int bgMode = -1;
  private Image bgImage = null;
  private Color bgColor = Color.red;

  /** the current context */
  MagellanContext context;

  /**
   * Creates new MagellanDesktop
   */
  private MagellanDesktop() {
    // do nothing
  }

  public static MagellanDesktop getInstance() {
    if (MagellanDesktop._instance == null) {
      MagellanDesktop._instance = new MagellanDesktop();
    }
    return MagellanDesktop._instance;
  }

  /**
   * Creates new MagellanDesktop
   */
  public void init(Client client, MagellanContext context, Properties settings,
      Map<String, Component> components, File settingsDir) {
    this.client = client;
    this.context = context;
    this.settings = settings;

    if (dockingFrameworkBuilder == null) {
      dockingFrameworkBuilder = new DFBInstance(this);
    }
    dockingFrameworkBuilder.setProperties(settings);

    this.settingsDir = settingsDir;
    timer = new Timer(1000, this);
    timer.start();
    timer.stop();
    keyHandler = new KeyHandler();
    client.addWindowListener(this);
    setManagedComponents(components);

    // init desktop menu
    initDesktopMenu();

    initWorkSpace();

    // init the desktop
    initSplitSet();

    validateDesktopMenu();

    loadTranslations();

    // register keystrokes
    registerKeyStrokes();

    DesktopEnvironment.init(this);
  }

  /**
   *
   */
  private void initWorkSpace() {
    if (workSpace == null) {
      workSpace = new WorkSpace();

      setLayout(new BorderLayout());
      removeAll();
      this.add(workSpace, BorderLayout.CENTER);
    }
    workSpace.setEnabledChooser(settings.getProperty(
        PropertiesHelper.DESKTOP_ENABLE_WORKSPACE_CHOOSER, Boolean.TRUE.toString()).equals(
            Boolean.TRUE.toString()));
  }

  /**
   * Returns the workspace of Magellan.
   */
  public WorkSpace getWorkSpace() {
    return workSpace;
  }

  /**
   *
   */
  public void setWorkSpaceChooser(boolean enabled) {
    settings
        .setProperty(PropertiesHelper.DESKTOP_ENABLE_WORKSPACE_CHOOSER, String.valueOf(enabled));
    initWorkSpace();
  }

  public Map<KeyStroke, Object> getShortCutListeners() {
    return shortCutListeners;
  }

  public Map<KeyStroke, KeyStroke> getShortCutTranslations() {
    return shortCutTranslations;
  }

  /**
   * Returns the home directory of Magellan.
   */
  public File getMagellanSettingsDir() {
    return settingsDir;
  }

  /*
   * ######################## # Property access code # ########################
   */
  /**
   * Returns the current desktop menu.
   *
   * @return The current desktop menu.
   */
  public JMenu getDesktopMenu() {
    return desktopMenu;
  }

  /**
   * Returns all the components available to this desktop. Keys are IDs, Values are components.
   */
  public Map<String, Component> getManagedComponents() {
    return components;
  }

  /**
   * Sets the components available for this desktop. This is only useful if called before a
   * mode-change.
   */
  public void setManagedComponents(Map<String, Component> components) {
    this.components = components;
  }

  /**
   * Returns the iconification mode. TRUE means, that all windows are (de)iconified if the main window
   * is (de)iconified.
   */
  public boolean isIconify() {
    return iconify;
  }

  /**
   * Sets the iconification mode. TRUE means, that all windows are (de)iconified if the main window is
   * (de)iconified.
   */
  public void setIconify(boolean iconify) {
    this.iconify = iconify;
  }

  /**
   * Creates the menu "Desktop" for Magellan. At first creates a sub-menu with all frames, then a
   * sub-menu for all available split sets and at last a sub-menu with all layouts.
   */
  protected void initDesktopMenu() {
    desktopMenu =
        dockingFrameworkBuilder.createDesktopMenu(components, settings, this);
  }

  /*
   * ######################## # Desktop init methods # ########################
   */

  /**
   * Sets the Magellan window bounds according to current mode.
   */
  protected void setClientBounds() {
    if (splitRect == null) { // not initialized before, load it
      SwingUtils.setBounds(client, settings, "Client", true);
      splitRect = new Rectangle(client.getBounds());
    }
  }

  /**
   * Checks if allo checkboxes in the menu are correctly set
   */
  protected void validateDesktopMenu() {
    if (splitRoot != null) {
      for (int i = 0; i < desktopMenu.getItemCount(); i++) {
        JMenuItem menuItem = desktopMenu.getItem(i);
        if (menuItem instanceof JCheckBoxMenuItem) {
          JCheckBoxMenuItem item = (JCheckBoxMenuItem) menuItem;
          String name = item.getActionCommand().substring(5);
          DockingWindow window = findDockingWindow(splitRoot, name);
          if (window != null) {
            item.setSelected(true);
          }
        }
      }

      dockingFrameworkBuilder.updateLayoutMenu();
    }
  }

  /**
   * Initializes the desktop using SplitPanes.
   */
  protected boolean initSplitSet() {
    setClientBounds();

    // get out area, (approximatly)
    Rectangle r = client.getBounds();
    r.x += 3;
    r.y += 20;
    r.width -= 6;
    r.height -= 23;

    splitRoot =
        dockingFrameworkBuilder.buildDesktop(components, new File(settingsDir,
            MagellanDesktop.DOCKING_LAYOUT_FILE));
    if (splitRoot != null) {
      splitRoot.addListener(this);
    }

    if (splitRoot == null)
      return false;

    workSpace.setContent(splitRoot);
    buildShortCutTable(dockingFrameworkBuilder.getComponentsUsed());

    return true;
  }

  /*
   * ###################### # Shortcut - Methods # ######################
   */

  /**
   * Based on the components used by the desktop this method builds the KeyStroke-HashMap. The key is
   * the KeyStroke-Object returned by ShortcutListener.getShortCuts(), the value is the listener
   * object.
   */
  protected void buildShortCutTable(Collection<Component> scomps) {
    for (Component o : scomps) {

      if (o instanceof ShortcutListener) {
        ShortcutListener sl = (ShortcutListener) o;
        Iterator<KeyStroke> it2 = sl.getShortCuts();

        while (it2.hasNext()) {
          KeyStroke stroke = it2.next();
          shortCutListeners.put(stroke, sl);
        }
      }
    }
  }

  /**
   * This method register all KeyStrokes in the KeyStroke-HashMap using registerListener().
   */
  protected void registerKeyStrokes() {
    registerListener();
  }

  /**
   * This registers the key handler at all known containers.
   */
  protected void registerListener() {
    // register at all frames
    keyHandler.disconnect();

    Collection<Component> desk = new LinkedList<Component>();
    desk.add(client);

    keyHandler.connect(desk);
  }

  /**
   *
   */
  protected void loadTranslations() {
    String s = settings.getProperty("Desktop.KeyTranslations");

    if (s != null) {
      StringTokenizer st = new StringTokenizer(s, ",");

      while (st.hasMoreTokens()) {
        try {
          KeyStroke newStroke =
              KeyStroke.getKeyStroke(Integer.parseInt(st.nextToken()), Integer.parseInt(st
                  .nextToken()));
          KeyStroke oldStroke =
              KeyStroke.getKeyStroke(Integer.parseInt(st.nextToken()), Integer.parseInt(st
                  .nextToken()));
          registerTranslation(newStroke, oldStroke);
        } catch (RuntimeException exc) {
        }
      }
    }
  }

  protected void saveTranslations() {
    if (shortCutTranslations.size() > 0) {
      StringBuffer buf = new StringBuffer();
      Iterator<Map.Entry<KeyStroke, KeyStroke>> it = shortCutTranslations.entrySet().iterator();

      while (it.hasNext()) {
        Map.Entry<KeyStroke, KeyStroke> e = it.next();
        KeyStroke stroke = e.getKey();
        buf.append(stroke.getKeyCode());
        buf.append(',');
        buf.append(stroke.getModifiers());
        buf.append(',');
        stroke = e.getValue();
        buf.append(stroke.getKeyCode());
        buf.append(',');
        buf.append(stroke.getModifiers());

        if (it.hasNext()) {
          buf.append(',');
        }
      }

      settings.setProperty("Desktop.KeyTranslations", buf.toString());
    } else {
      settings.remove("Desktop.KeyTranslations");
    }
  }

  /**
   * Registers a shortcut translation.
   */
  public void registerTranslation(KeyStroke newStroke, KeyStroke oldStroke) {
    if (newStroke.equals(oldStroke))
      return;

    if (!shortCutTranslations.containsKey(oldStroke)) {
      keyHandler.removeStroke(oldStroke);
    }

    keyHandler.addTranslationStroke(newStroke);
    shortCutTranslations.put(newStroke, oldStroke);
  }

  /**
   *
   */
  public void removeTranslation(KeyStroke stroke) {
    if (shortCutTranslations.containsKey(stroke)) {
      KeyStroke old = shortCutTranslations.get(stroke);
      keyHandler.removeStroke(stroke);
      keyHandler.install(old);
      shortCutTranslations.remove(stroke);
    }
  }

  /**
   *
   */
  public KeyStroke getTranslation(KeyStroke newStroke) {
    return shortCutTranslations.get(newStroke);
  }

  /**
   *
   */
  public KeyStroke findTranslation(KeyStroke oldStroke) {
    Iterator<KeyStroke> it = shortCutTranslations.keySet().iterator();

    while (it.hasNext()) {
      Object obj = it.next();

      if (shortCutTranslations.get(obj).equals(oldStroke))
        return (KeyStroke) obj;
    }

    return null;
  }

  /**
   * Registers a new KeyStroke/ShortcutListener pair at the desktop.
   */
  public void registerShortcut(KeyStroke stroke, ShortcutListener sl) {
    keyHandler.addStroke(stroke, sl);
  }

  /**
   * Register a ShortcutListener with all its shortcuts at the desktop.
   */
  public void registerShortcut(ShortcutListener sl) {
    keyHandler.addListener(sl);
  }

  /**
   * Registers a new KeyStroke/ActionListener pair at the desktop. If the given boolean is FALSE, the
   * listener will not be registered at the Client frame. This feature is for menu items.
   */
  public void registerShortcut(KeyStroke stroke, ActionListener al) {
    keyHandler.addStroke(stroke, al, true);
  }

  /*
   * ################################ # Frame management methods # # ONLY USED WHEN IN FRAME MODE #
   * ################################
   */

  /**
   * Just empty - do nothing if a frame is inactivated.
   */
  public void windowDeactivated(java.awt.event.WindowEvent p1) {
    // clear "input buffer"
    keyHandler.resetExtendedShortcutListener();
  }

  /**
   * Just empty - do nothing if a frame is closed.
   */
  public void windowClosed(java.awt.event.WindowEvent p1) {
  }

  /**
   * Check if this event comes from the client window and if all windows should be deiconified - if
   * so, deiconify all and put them to front.
   */
  public void windowDeiconified(java.awt.event.WindowEvent p1) {
    // clear "input buffer"
    keyHandler.resetExtendedShortcutListener();
  }

  /**
   * Just empty - do nothing if a frame is opened.
   */
  public void windowOpened(java.awt.event.WindowEvent p1) {
    // clear "input buffer"
    keyHandler.resetExtendedShortcutListener();
  }

  /**
   * Check if this event comes from the client window and if all windows should be iconified - if so,
   * iconify them.
   */
  public void windowIconified(java.awt.event.WindowEvent p1) {
    // clear "input buffer"
    keyHandler.resetExtendedShortcutListener();
  }

  /**
   * Update the frames menu.
   */
  public void windowClosing(java.awt.event.WindowEvent p1) {
    // clear "input buffer"
    keyHandler.resetExtendedShortcutListener();
  }

  /**
   * Check the activation modus:
   * <ul>
   * <li>DO NOTHING ON ACTIVATION</li>
   * <li>ACTIVATE ALL WHEN CLIENT IS ACTIVATED</li>
   * <li>ACTIVATE ALL IF ANY WINDOW IS ACTIVATED</li>
   * </ul>
   */
  public void windowActivated(java.awt.event.WindowEvent p1) {
    // clear "input buffer"
    keyHandler.resetExtendedShortcutListener();

    if (inFront) {
      timer.restart();

      return;
    }
  }

  /**
   * Activates all frames if in frame mode.
   */
  public void setAllVisible(boolean visible) {
    client.setVisible(visible);
  }

  /*
   * ######################### # Cross-Mode Management # # Focus and Repaint #
   * #########################
   */

  /**
   * The component with the ID id will gain the focus. If Frame Mode is enabled, the parent frame will
   * be activated.
   */
  public void componentRequestFocus(String id) {
    if (!components.containsKey(id))
      return;

    if (splitRoot != null) {
      restoreView(id);
    }

    // search in component table to activate directly
    if (components.get(id) != null) {
      components.get(id).requestFocus();
    }
  }

  /**
   * Repaints the component with ID id.
   */
  public void repaint(String id) {
    if (!components.containsKey(id))
      return;
    // FIXME (stm) this only works the first time; try this:
    // install to ATR modes
    // select the first (via Ctrl+Alt+T, 1) --> repaint ok
    // select the second (Ctrl+Alt+T, 2) --> no repaint, only after moving map
    // components.get(id).validate();
    // solution: call Mapper.setRenderContextChanged(true) before...;
    components.get(id).repaint();

  }

  /**
   * Refreshes region tree and paints all desktop components.
   */
  public void repaintAllComponents() {
    try {
      // FIXME hmm.... what's the best way to do this?
      context.getEventDispatcher().fire(new GameDataEvent(this, context.getGameData()), true);
      // EMapDetailsPanel details = (EMapDetailsPanel) components.get(DETAILS_IDENTIFIER);
      // if (details != null) {
      // details.setGameData(client.getData());
      // }
      // client.setData(client.getData());
      // EMapOverviewPanel overview = (EMapOverviewPanel)
      // components.get(OVERVIEWHISTORY_IDENTIFIER);
      // if (overview != null) {
      // overview.rebuildTree();
      // }

    } catch (ClassCastException e) {
      log.error("internal error: component not found", e);
    }
    MagellanDesktop.log.debug("repaint all");
    for (Component c : components.values()) {
      c.repaint();
    }
  }

  /**
   *
   */
  public void updateLaF() {
    SwingUtilities.updateComponentTreeUI(client);

    // to avoid start bug
    if (desktopMenu != null) {
      SwingUtilities.updateComponentTreeUI(desktopMenu);
    }
  }

  private DockingWindow findDockingWindow(DockingWindow root, String name) {
    if (root == null)
      return null;
    if (root.getName() != null && root.getName().equals(name))
      return root;

    for (int index = 0; index < root.getChildWindowCount(); index++) {
      DockingWindow window = findDockingWindow(root.getChildWindow(index), name);
      if (window != null)
        return window;
    }
    return null;
  }

  /**
   *
   */
  public void actionPerformed(ActionEvent p1) {
    if (p1.getSource() == timer) {
      inFront = false;
      timer.stop();
    } else if (p1.getSource() instanceof JCheckBoxMenuItem) {
      JCheckBoxMenuItem menu = (JCheckBoxMenuItem) p1.getSource();
      String action = p1.getActionCommand();
      if (action != null && action.startsWith("menu.")) {
        String name = action.substring(5);
        if (splitRoot != null) {
          if (menu.isSelected()) {
            // open dock via name
            restoreView(name);
          } else {
            // close dock
            DockingWindow window = findDockingWindow(splitRoot, name);
            if (window != null) {
              window.close();
            } else {
              menu.setSelected(false);
            }

          }

        }

      } else if (action != null && action.equals("hideTabs")) {
        setTabVisibility(!menu.isSelected());
      }
    }
  }

  /**
   * Enables or disables all docking tabs.
   */
  public synchronized void setTabVisibility(boolean showTabs) {
    Client.INSTANCE.getProperties().setProperty(PropertiesHelper.CLIENTPREFERENCES_DONT_SHOW_TABS,
        Boolean.toString(!showTabs));

    RootWindow root = splitRoot;
    RootWindowProperties prop = root.getRootWindowProperties();
    if (!showTabs) {
      prop.getTabWindowProperties().getTabbedPanelProperties().getTabAreaProperties()
          .setTabAreaVisiblePolicy(TabAreaVisiblePolicy.MORE_THAN_ONE_TAB);
    } else {
      prop.getTabWindowProperties().getTabbedPanelProperties().getTabAreaProperties()
          .setTabAreaVisiblePolicy(TabAreaVisiblePolicy.ALWAYS);
    }
  }

  /**
   * Makes a view visible.
   *
   * @param name
   */
  public void restoreView(String name) {
    View view = dockingFrameworkBuilder.getViewMap().getView(name);
    if (view != null) {
      view.restore();
      view.makeVisible();
    }
  }

  // /////////////////////////////////////
  // //
  // S H O R T C U T - H A N D L E R //
  // //
  // /////////////////////////////////////

  /**
   * A handler class for key events. It checks if the given combination is stored and calls the
   * shortcutlistener. This class is also responsible to handle extended shortcut listeners This
   * implementation only looks for pressed keys because of some mysterious behaviour on CTRL+Key
   * events.
   */
  protected class KeyHandler {
    protected class KeyboardActionListener implements ActionListener {
      protected KeyStroke stroke;

      /**
       *
       */
      public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        keyPressed(stroke, actionEvent.getSource());
      }
    }

    protected ShortcutListener lastListener = null;
    protected GameEventListener helpListener;
    protected Collection<KeyStroke> extendedListeners;
    protected Collection<Component> lastComponents;

    /**
     * Creates a new KeyHandler object.
     */
    public KeyHandler() {
      helpListener = new GameEventListener(this);
      extendedListeners = new LinkedList<KeyStroke>();
      lastComponents = new LinkedList<Component>();
    }

    /**
     *
     */
    public void connect(Collection<Component> deskElements) {
      if (lastComponents.size() > 0) {
        disconnect();
      }

      lastComponents.addAll(deskElements);

      Set<KeyStroke> set = new HashSet<KeyStroke>(shortCutTranslations.keySet());
      Set<KeyStroke> set2 = new HashSet<KeyStroke>(shortCutListeners.keySet());
      Iterator<KeyStroke> it = set.iterator();

      while (it.hasNext()) {
        set2.remove(shortCutTranslations.get(it.next()));
      }

      set.addAll(set2);

      it = set.iterator();

      while (it.hasNext()) {
        KeyStroke stroke = it.next();
        install(stroke);
      }

      extendedListeners.clear();
      lastListener = null;
    }

    /**
     *
     */
    public void disconnect() {
      if (lastComponents.size() > 0) {
        Iterator<KeyStroke> it = shortCutListeners.keySet().iterator();

        while (it.hasNext()) {
          KeyStroke str = it.next();
          remove(str);
        }

        it = extendedListeners.iterator();

        while (it.hasNext()) {
          KeyStroke str = it.next();
          remove(str);
        }

        it = shortCutTranslations.keySet().iterator();

        while (it.hasNext()) {
          KeyStroke str = it.next();
          remove(str);
        }
      }

      extendedListeners.clear();
      lastListener = null;
      lastComponents.clear();
    }

    /**
     *
     */
    public void removeStroke(KeyStroke stroke) {
      remove(stroke);
    }

    /**
     *
     */
    public void addTranslationStroke(KeyStroke str) {
      install(str);
    }

    /**
     *
     */
    public void addStroke(KeyStroke str, Object dest) {
      addStroke(str, dest, false);
    }

    /**
     *
     */
    public void addStroke(KeyStroke str, Object dest, boolean actionAware) {
      if ((findTranslation(str) == null) && !shortCutListeners.containsKey(str)) {
        install(str);
      } else if (actionAware) {
        KeyStroke newStroke = findTranslation(str);

        if ((newStroke != null) && (dest instanceof Action)) {
          ((Action) dest).putValue("accelerator", newStroke);
        }
      }

      if (shortCutListeners.containsKey(str) && dest != shortCutListeners.get(str)) {
        MagellanDesktop.log.warn("multiply used shortcut + " + str + "\n     old: "
            + shortCutListeners.get(str) + ",\n     new: " + dest);
      }
      shortCutListeners.put(str, dest);
    }

    /**
     *
     */
    public void addListener(ShortcutListener sl) {
      Iterator<KeyStroke> it = sl.getShortCuts();

      while (it.hasNext()) {
        addStroke(it.next(), sl, false);
      }
    }

    /**
     *
     */
    public void keyPressed(KeyStroke e, Object src) {
      // "translate" the key
      if (shortCutTranslations.containsKey(e)) {
        e = shortCutTranslations.get(e);
      }

      // redirect only for sub-listener
      if (lastListener != null) {
        remove(extendedListeners, false);

        if (extendedListeners.contains(e)) {
          reactOnStroke(e, lastListener);

          return;
        }

        lastListener = null;
      }

      if (shortCutListeners.containsKey(e)) {
        Object o = shortCutListeners.get(e);

        if (o instanceof ShortcutListener) {
          reactOnStroke(e, (ShortcutListener) o);
        } else if (o instanceof ActionListener) {
          /*
           * if (src instanceof JComponent) if (((JComponent)src).getTopLevelAncestor()==client) return;
           */
          ((ActionListener) o).actionPerformed(null);
        }

        return;
      }

      MagellanDesktop.log.info("Error: Unrecognized key stroke " + e);
    }

    protected void install(ShortcutListener sl, boolean flag) {
      Iterator<KeyStroke> it = sl.getShortCuts();

      while (it.hasNext()) {
        KeyStroke stroke = it.next();

        if (flag) {
          extendedListeners.add(stroke);
        }

        if (!shortCutListeners.containsKey(stroke)) {
          install(stroke);
        }
      }
    }

    protected void install(final KeyStroke stroke) {
      Iterator<Component> it = lastComponents.iterator();
      final KeyboardActionListener kal = new KeyboardActionListener();
      kal.stroke = stroke;

      Action anAction = new AbstractAction("A" + stroke) {

        public void actionPerformed(ActionEvent e) {
          kal.actionPerformed(e);
        }
      };

      while (it.hasNext()) {
        Component c = it.next();

        addToContainer(c, stroke, kal, anAction);
      }
    }

    /**
     * Adds the given key stroke to the container searching an instance of JComponent.
     */
    protected boolean addToContainer(Component c, KeyStroke s, ActionListener al, Action anAction) {
      if (c instanceof JComponent) {
        // deprecated ((JComponent) c).registerKeyboardAction(al, s, JComponent.WHEN_IN_FOCUSED_WINDOW);

        ((JComponent) c).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(s, anAction.getValue(Action.NAME));
        ((JComponent) c).getActionMap().put(anAction.getValue(Action.NAME), anAction);

        return true;
      }

      if (c instanceof Container) {
        Container con = (Container) c;

        if (con.getComponentCount() > 0) {
          Component comp[] = con.getComponents();

          for (Component element : comp) {
            if (addToContainer(element, s, al, anAction))
              return true;
          }
        }
      }

      return false;
    }

    /**
     * Removes the KeyStrokes in the collection from all desktop components. If the given flag is true,
     * all key-strokes in the collection are removed, else only non-base-level key-strokes.
     */
    protected void remove(Collection<KeyStroke> col, boolean flag) {
      Iterator<KeyStroke> it = col.iterator();

      while (it.hasNext()) {
        KeyStroke stroke = it.next();

        if (flag
            || (!shortCutTranslations.containsKey(stroke) && !shortCutListeners.containsKey(
                stroke))) {
          remove(stroke);
        }
      }
    }

    protected void remove(KeyStroke stroke) {
      Iterator<Component> it2 = lastComponents.iterator();

      while (it2.hasNext()) {
        Component c = it2.next();

        removeFromContainer(c, stroke);
      }
    }

    /**
     * Removes the given key stroke from the container searching an instance of JComponent.
     */
    protected boolean removeFromContainer(Component c, KeyStroke s) {
      if (c instanceof JComponent) {
        // deprecated ((JComponent) c).unregisterKeyboardAction(s);
        Object key = ((JComponent) c).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(s);
        ((JComponent) c).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(s);
        ((JComponent) c).getActionMap().remove(key);

        return true;
      }

      if (c instanceof Container) {
        Container con = (Container) c;

        if (con.getComponentCount() > 0) {
          Component comp[] = con.getComponents();

          for (Component element : comp) {
            if (removeFromContainer(element, s))
              return true;
          }
        }
      }

      return false;
    }

    /**
     * Resets the sub-listener.
     */
    public void resetExtendedShortcutListener() {
      remove(extendedListeners, false);
      lastListener = null;
    }

    /**
     * Performs the action for the given shortcut listener.
     */
    protected void reactOnStroke(KeyStroke stroke, ShortcutListener sl) {
      if (sl instanceof ExtendedShortcutListener) {
        ExtendedShortcutListener esl = (ExtendedShortcutListener) sl;

        if (esl.isExtendedShortcut(stroke)) {
          lastListener = esl.getExtendedShortcutListener(stroke);
          install(lastListener, true);

          return;
        }
      }

      lastListener = null;
      sl.shortCut(stroke);
    }

    /**
     * The sub-shortcutlistener must be cleared when any game event appears
     */
    protected class GameEventListener implements magellan.library.event.GameDataListener,
        magellan.client.event.SelectionListener {
      KeyHandler parent;

      /**
       * Creates a new GameEventListener object.
       */
      public GameEventListener(KeyHandler parent) {
        this.parent = parent;

        EventDispatcher e = context.getEventDispatcher();
        e.addGameDataListener(this);
        e.addSelectionListener(this);
      }

      /**
       * Invoked when different objects are activated or selected.
       */
      public void selectionChanged(magellan.client.event.SelectionEvent e) {
        parent.resetExtendedShortcutListener();
      }

      /**
       * Invoked when the current game data object becomes invalid.
       */
      public void gameDataChanged(magellan.library.event.GameDataEvent e) {
        parent.resetExtendedShortcutListener();
      }
    }
  }

  // /////////////////////////////////
  // CONFIGURATION CODE - EXTERNAL //
  // /////////////////////////////////

  /**
   * Writes the configuration of this desktop.
   */
  public void save() {

    saveTranslations();

    try {
      dockingFrameworkBuilder.write(new File(settingsDir, MagellanDesktop.DOCKING_LAYOUT_FILE));
    } catch (Throwable t) {
      MagellanDesktop.log.fatal(t.getMessage(), t);
      ErrorWindow errorWindow = new ErrorWindow(Client.INSTANCE, t.getMessage(), "", t);
      errorWindow.setVisible(true);
    }
  }

  // /////////////////////
  // WINDOW ACTIVATION //
  // /////////////////////

  /**
   * Runnable used to activate all displayed windows. This will move them to the front of the desktop.
   */
  // private class WindowActivator implements Runnable {
  // protected Window source;
  //
  // /**
  // * Creates a new WindowActivator object.
  // */
  // public WindowActivator(Window s) {
  // source = s;
  // }
  //
  // /**
  // *
  // */
  // public void run() {
  // return;
  // }
  // }

  // ///////////////////////
  // PREFERENCES ADAPTER //
  // ///////////////////////
  /**
   * @see magellan.client.swing.preferences.PreferencesFactory#createPreferencesAdapter()
   */
  public PreferencesAdapter createPreferencesAdapter() {
    return new DesktopPreferences(this, client, settings, dockingFrameworkBuilder);
  }

  protected class BackgroundPanel extends JPanel {
    /**
     * Creates a new BackgroundPanel object.
     */
    public BackgroundPanel() {
      super(new BorderLayout());
    }

    /**
     *
     */
    public void setComponent(Component c) {
      clearOpaque(c);
      this.add(c, BorderLayout.CENTER);
    }

    /**
     *
     */
    @Override
    public void paintComponent(Graphics g) {
      if (bgMode == -1) {
        super.paintComponent(g);

        return;
      }

      if (bgMode == 0) {
        if (bgColor != null) {
          g.setColor(bgColor);
          g.fillRect(0, 0, getWidth(), getHeight());
        }
      } else {
        if (bgImage != null) {
          if (bgMode == 1) { // resize
            g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
          } else { // repeat

            int w = bgImage.getWidth(this);
            int h = bgImage.getHeight(this);
            int wi = getWidth() / w;

            if ((getWidth() % w) != 0) {
              wi++;
            }

            int hi = getHeight() / h;

            if ((getHeight() % h) != 0) {
              hi++;
            }

            for (int i = 0; i < hi; i++) {
              for (int j = 0; j < wi; j++) {
                g.drawImage(bgImage, j * w, i * h, this);
              }
            }
          }
        }
      }
    }
  }

  /**
   *
   */
  @Override
  public Component add(Component c) {
    clearOpaque(c);

    return super.add(c);
  }

  /**
   *
   */
  @Override
  public void add(Component c, Object con) {
    clearOpaque(c);
    super.add(c, con);
  }

  protected void clearOpaque(Component c) {
    if (bgMode == -1)
      return;

    if (c instanceof JComponent) {
      ((JComponent) c).setOpaque(false);
    }

    if (c instanceof Container) {
      Component children[] = ((Container) c).getComponents();

      if ((children != null) && (children.length > 0)) {
        for (Component element : children) {
          clearOpaque(element);
        }
      }
    }
  }

  /**
   *
   */
  @Override
  public void paintComponent(Graphics g) {
    if (bgMode == -1) {
      super.paintComponent(g);

      return;
    }

    if (bgMode == 0) {
      if (bgColor != null) {
        g.setColor(bgColor);
        g.fillRect(0, 0, getWidth(), getHeight());
      }
    } else {
      if (bgImage != null) {
        if (bgMode == 1) { // resize
          g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
        } else { // repeat

          int w = bgImage.getWidth(this);
          int h = bgImage.getHeight(this);
          int wi = getWidth() / w;

          if ((getWidth() % w) != 0) {
            wi++;
          }

          int hi = getHeight() / h;

          if ((getHeight() % h) != 0) {
            hi++;
          }

          for (int i = 0; i < hi; i++) {
            for (int j = 0; j < wi; j++) {
              g.drawImage(bgImage, j * w, i * h, this);
            }
          }
        }
      }
    }
  }

  /**
   * Opens or closes a specific dock.
   */
  public void setVisible(String viewName, boolean setVisible) {
    if (splitRoot != null) {
      dockingFrameworkBuilder.setVisible(splitRoot, viewName, setVisible);

      if (desktopMenu != null) {
        if (setVisible) {
          setActive(viewName);
        } else {
          setInActive(viewName);
        }
      }
    }
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#viewFocusChanged(net.infonode.docking.View,
   *      net.infonode.docking.View)
   */
  public void viewFocusChanged(View previouslyFocusedView, View focusedView) {
    // do nothing
  }

  /**
   * Enabled a desktop menu entry for the given View
   */
  public void setActive(String viewName) {
    // inform desktopmenu
    if (viewName != null) {
      for (int index = 0; index < desktopMenu.getItemCount(); index++) {
        if (desktopMenu.getItem(index) instanceof JCheckBoxMenuItem) {
          JCheckBoxMenuItem menu = (JCheckBoxMenuItem) desktopMenu.getItem(index);
          if (menu.getActionCommand().equals("menu." + viewName)) {
            menu.setSelected(true);
          }
        }
      }
    }
  }

  /**
   * Disables a desktop menu entry for the given View
   */
  public void setInActive(String viewName) {
    // inform desktopmenu
    if (viewName != null) {
      for (int index = 0; index < desktopMenu.getItemCount(); index++) {
        if (desktopMenu.getItem(index) instanceof JCheckBoxMenuItem) {
          JCheckBoxMenuItem menu = (JCheckBoxMenuItem) desktopMenu.getItem(index);
          if (menu.getActionCommand().equals("menu." + viewName)) {
            menu.setSelected(false);
          }
        }
      }
    }
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowAdded(net.infonode.docking.DockingWindow,
   *      net.infonode.docking.DockingWindow)
   */
  public void windowAdded(DockingWindow addedToWindow, DockingWindow addedWindow) {
    // inform desktopmenu
    if (addedWindow.getName() != null) {
      setActive(addedWindow.getName());
    }
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowClosed(net.infonode.docking.DockingWindow)
   */
  public void windowClosed(DockingWindow window) {
    // inform desktopmenu
    if (window.getName() != null) {
      setInActive(window.getName());
    }
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowClosing(net.infonode.docking.DockingWindow)
   */
  public void windowClosing(DockingWindow window) throws OperationAbortedException {
    // do nothing
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowDocked(net.infonode.docking.DockingWindow)
   */
  public void windowDocked(DockingWindow window) {
    // do nothing
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowDocking(net.infonode.docking.DockingWindow)
   */
  public void windowDocking(DockingWindow window) throws OperationAbortedException {
    // do nothing
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowHidden(net.infonode.docking.DockingWindow)
   */
  public void windowHidden(DockingWindow window) {
    // do nothing
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowMaximized(net.infonode.docking.DockingWindow)
   */
  public void windowMaximized(DockingWindow window) {
    // do nothing
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowMaximizing(net.infonode.docking.DockingWindow)
   */
  public void windowMaximizing(DockingWindow window) throws OperationAbortedException {
    // do nothing
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowMinimized(net.infonode.docking.DockingWindow)
   */
  public void windowMinimized(DockingWindow window) {
    // do nothing
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowMinimizing(net.infonode.docking.DockingWindow)
   */
  public void windowMinimizing(DockingWindow window) throws OperationAbortedException {
    // do nothing
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowRemoved(net.infonode.docking.DockingWindow,
   *      net.infonode.docking.DockingWindow)
   */
  public void windowRemoved(DockingWindow removedFromWindow, DockingWindow removedWindow) {
    // inform desktopmenu
    if (removedWindow.getName() != null) {
      setInActive(removedFromWindow.getName());
    }
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowRestored(net.infonode.docking.DockingWindow)
   */
  public void windowRestored(DockingWindow window) {
    // do nothing
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowRestoring(net.infonode.docking.DockingWindow)
   */
  public void windowRestoring(DockingWindow window) throws OperationAbortedException {
    // do nothing
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowShown(net.infonode.docking.DockingWindow)
   */
  public void windowShown(DockingWindow window) {
    // do nothing
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowUndocked(net.infonode.docking.DockingWindow)
   */
  public void windowUndocked(DockingWindow window) {
    // do nothing
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowUndocking(net.infonode.docking.DockingWindow)
   */
  public void windowUndocking(DockingWindow window) throws OperationAbortedException {
    // do nothing
  }

  /**
   * adds the listener to the DockingFrameWork
   *
   * @param listener
   */
  public void addDockingWindowListener(DockingWindowListener listener) {
    if (splitRoot != null) {
      splitRoot.addListener(listener);
    } else {
      MagellanDesktop.log.error("unable to add DockingWindowListener! (no RootWindow)");
    }
  }

}
