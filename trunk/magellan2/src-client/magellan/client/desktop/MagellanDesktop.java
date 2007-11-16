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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.LayoutManager2;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowListener;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.RootWindow;
import net.infonode.docking.View;

import magellan.client.Client;
import magellan.client.MagellanContext;
import magellan.client.event.EventDispatcher;
import magellan.client.swing.desktop.WorkSpace;
import magellan.client.swing.preferences.ExtendedPreferencesAdapter;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.client.utils.ErrorWindow;
import magellan.library.io.file.FileBackup;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * This class represents the Magellan Desktop. It contains all visible
 * components. We use this class to load, set and save their positions.
 *
 * @author Andreas
 */
public class MagellanDesktop extends JPanel implements WindowListener, ActionListener, PreferencesFactory, DockingWindowListener {
  private static final Logger log = Logger.getInstance(MagellanDesktop.class);

  /** the workSpace associated with this MagellanDesktop */
  private WorkSpace workSpace;

  /** All windows will stay were they are when a frame is activated. */
  public static final int ACTIVATION_MODE_NONE = 0;

  /** All frames are moved to front if the client window is activated. */
  public static final int ACTIVATION_MODE_CLIENT_ONLY = 1;

  /** All frames are move to front if any frame is activated. */
  public static final int ACTIVATION_MODE_ANY = 2;

  /**
   * Holds all the components. The key is the global id like NAME or OVERVIEW, the value is the
   * component.
   */
  private Map<String,Component> components;

  /** Some shortcut things */
  private Map<KeyStroke,Object> shortCutListeners = new HashMap<KeyStroke, Object>();
  private Map<KeyStroke,KeyStroke> shortCutTranslations = new HashMap<KeyStroke, KeyStroke>();
  private KeyHandler keyHandler;

  /**
   * In case of MODE_FRAME this HashMap holds all the frames. The key is the ID of the covered
   * component.
   */
  private Map<String,FrameRectangle> frames;

  /**
   * Holds the activation mode. This value is used in Framed Mode when a frame is activated. It
   * decides wether the other frames should be activated, too.
   */
  private int activationMode;

  /** Decides if all frames should be (de)iconified if the client frame is (de)iconified. */
  private boolean iconify;

  /** Holds the settings in case a mode-change occurs. */
  private Properties settings;

  /**
   * Stores the root component of the splitted desktop. So the desktop can be saved even if a
   * mode-changed occured.
   */
  private JComponent splitRoot;

  /** Holds the client frame. This is for (de)iconification compares. */
  private Client client;

  /** Holds the frame rect for Magellan in Split-Mode. */
  private Rectangle splitRect;

  /** Holds the frame rect for the main Magellan window in Frame-Mode. */
  private Rectangle frameRect;

  /**
   * Just a variable to suppress double activation events. If anyone can do it better, please DO
   * IT.
   */
  private boolean inFront = false;
  private Timer timer;

  // Split mode objects
  private DockingFrameworkBuilder splitBuilder;
  private Map<String,FrameTreeNode> splitSets;
  private String splitName;

  // Desktop menu
  private JMenu desktopMenu;

  //Objects for mode "Layout"
  private Map<String,Map<String,DesktopLayoutManager.CPref>> layoutComponents;
  private DesktopLayoutManager lManager;
  private String layoutName;
  private File magellanDir = null;
  private boolean modeInitialized = false;
  private int bgMode = -1;
  private Image bgImage = null;
  private Color bgColor = Color.red;

    
  /** the current context */
  MagellanContext context;
    
  /**
   * Creates new MagellanDesktop
   */
  public MagellanDesktop(Client client, MagellanContext context, Properties settings, Map<String,Component> components, File dir) {
    this.client = client;
    this.context = context;

    magellanDir = dir;
    timer = new Timer(1000, this);
    timer.start();
    timer.stop();
    keyHandler = new KeyHandler();
    client.addWindowListener(this);
    this.settings = settings;
    setManagedComponents(components);

    try {
      File file = getDesktopFile(false);

      if(file != null) {
        log.info("Using Desktopfile: " + file);
      }
    } catch(Exception exc) {
    }

    modeInitialized = false;

    initSplitSets();
    //initFrameRectangles(); // to find supported frames for menu

    // init desktop menu
    initDesktopMenu();

    initWorkSpace();

    // init the desktop
    if(!initSplitSet(settings.getProperty("Desktop.SplitSet", "Standard"))) {
      //try to load default
      if(!initSplitSet("Standard")) {
        Iterator<String> it = splitSets.keySet().iterator();
        boolean loaded = false;

        while(!loaded && it.hasNext()) {
          loaded = initSplitSet(it.next());
        }

        if(!loaded) { //Sorry, cannot load -> build new default set
          JOptionPane.showMessageDialog(client, Resources.get("desktop.magellandesktop.msg.corruptsettings.text"));
          System.exit(1);
        }
      }
    }
    
    validateDesktopMenu();

    loadTranslations();

    // register keystrokes
    registerKeyStrokes();

    //for adapter
    loadFrameModeSettings();

    DesktopEnvironment.init(this);
  }

  /**
   *
   */
  private void initWorkSpace() {
    if(workSpace == null) {
      workSpace = new WorkSpace();
      
      this.setLayout(new BorderLayout());
      this.removeAll();
      this.add(workSpace, BorderLayout.CENTER);
    }
    workSpace.setEnabledChooser(settings.getProperty("Desktop.EnableWorkSpaceChooser","true").equals("true"));
  }

  /**
   * 
   */
  private void setWorkSpaceChooser(boolean enabled) {
    settings.setProperty("Desktop.EnableWorkSpaceChooser", String.valueOf(enabled));
    initWorkSpace();
  }

  /**
   * Returns the Magellan Desktop Configuration file. If save is false, the method searches in
   * various places for compatibility with older version. The priorities are:
   * 
   * <ol>
   *   <li>Magellan directory</li>
   *   <li>HOME Directory</li>
   *   <li>Local directory</li>
   * </ol>
   * 
   * The file to save to is always in the magellan directory.
   */
  public File getDesktopFile(boolean save) {
    if(save) { // always save to magellan directory
      return new File(magellanDir, "magellan_desktop.ini");
    }

    File file = null;

    // LOAD
    try {
      // first look in Class-Directory
      file = new File(magellanDir, "magellan_desktop.ini");

      if(file.exists()) {
        return file;
      }
    } catch(Exception exc2) {
    }

    try {
      // look for global ini
      file = new File(System.getProperty("user.home"), "magellan_desktop.ini");

      if(file.exists()) {
        return file;
      }
    } catch(Exception exc2) {
    }

    try {
      // look for local ini
      file = new File(".", "magellan_desktop.ini");

      if(file.exists()) {
        return file;
      }
    } catch(Exception exc3) {
    }

    // return the default
    return new File(magellanDir, "magellan_desktop.ini");
  }

  /*
   ########################
   # Property access code #
   ########################
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
   * 
   */
  public Set getAvailableSplitSets() {
    return splitSets.keySet();
  }

  /**
   * 
   */
  public boolean addSplitSet(String name, List<String> set) {
    FrameTreeBuilder ftb = new FrameTreeBuilder();

    try {
      ftb.buildTree(set.iterator());
    } catch(Exception exc) {
      return false;
    }

    Object o = splitSets.put(name, ftb.getRoot());

    if(o == null) {
      JCheckBoxMenuItem item = new JCheckBoxMenuItem(name, false);
      item.addActionListener(this);
      // TR 2007-06-20 useless in docking environment 
      //setMenuGroup.add(item);
      //setMenu.add(item);
    }

    return true;
  }

  /**
   * 
   */
  public Set getAvailableLayouts() {
    return layoutComponents.keySet();
  }

  /**
   * Returns all the components available to this desktop. Keys are IDs, Values are components.
   */
  public Map getManagedComponents() {
    return components;
  }

  /**
   * Sets the components available for this desktop. This is only useful if called before a
   * mode-change.
   */
  public void setManagedComponents(Map<String,Component> components) {
    this.components = components;
  }

  /**
   * Returns the activation mode.
   */
  public int getActivationMode() {
    return activationMode;
  }

  /**
   * Sets the activation mode. Possible values are:
   * 
   * <ul>
   *   <li>ACTIVATION_MODE_NONE: Never activate other windows</li>
   *   <li>ACTIVATION_MODE_CLIENT_ONLY: Activate other windows if the client frame is activated</li>
   *   <li>ACTIVATION_MODE_ANY: Activate all frames if any of them is activated.</li>
   * </ul>
   */
  public void setActivationMode(int activationMode) {
    this.activationMode = activationMode;
  }

  /**
   * Returns the iconification mode. TRUE means, that all windows are (de)iconified if the main
   * window is (de)iconified.
   */
  public boolean isIconify() {
    return iconify;
  }

  /**
   * Sets the iconification mode. TRUE means, that all windows are (de)iconified if the main
   * window is (de)iconified.
   */
  public void setIconify(boolean iconify) {
    this.iconify = iconify;
  }

  /**
   * this function creates a FrameRectangle with given id. The name is evaluated (and translated)
   * out of the given id.
   */
  private FrameRectangle createFrameRectangle(String id) {
    String translationkey = id.toLowerCase();

    if(id.equals("NAME&DESCRIPTION")) {
      translationkey = "name";
    }

    if(id.equals("OVERVIEW&HISTORY")) {
      translationkey = "overviewandhistory";
    }
    
    return new FrameRectangle(Resources.get("desktop.magellandesktop.frame." + translationkey + ".title"), id);
  }

  /**
   * 
   */
//  private class ActivateSplitSetAction extends AbstractAction {
//    ActivateSplitSetAction(String splitSetName, String text, String tooltip) {
//      super(text);
//      this.putValue(Action.ACTION_COMMAND_KEY, splitSetName);
//      this.putValue(Action.SHORT_DESCRIPTION, tooltip);
//    }
//    /**
//     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
//     */
//    public void actionPerformed(ActionEvent e) {
//      setMode(Mode.SPLIT, e.getActionCommand());
//    }
//  }
  
  /**
   * Creates the menu "Desktop" for Magellan. At first creates a sub-menu with all frames, then a
   * sub-menu for all available split sets and at last a sub-menu with all layouts.
   */
  protected void initDesktopMenu() {
    desktopMenu = DockingFrameworkBuilder.createDesktopMenu(components,this);
  }

  /**
   * Replaces the first substring toRep in def by repBy.
   */
  protected String replace(String def, String toRep, String repBy) {
    if(def.indexOf(toRep) > -1) {
      return def.substring(0, def.indexOf(toRep)) + repBy +
           def.substring(def.indexOf(toRep) + toRep.length());
    }

    return def;
  }

  /**
   * Parses the Magellan Desktop Configuration file for Split sets and custom layouts.
   */
  protected void initSplitSets() {
    Vector<String> loadedNames = new Vector<String>();
    Vector<List<String>> loadedBlocks = new Vector<List<String>>();
    boolean read = false;
    List<String> block = new LinkedList<String>();

    // load from magellan_desktop.ini
    try {
      File mdfile = getDesktopFile(false);
      BufferedReader r = new BufferedReader(new FileReader(mdfile));
      String s = null;
      String blockName = null;
      boolean inBlock = false;

      do {
        s = r.readLine();

        if((s != null) && !s.equals("")) {
          read = true;

          if(s.startsWith("[")) {
            if(inBlock) {
              loadedNames.add(blockName);
              loadedBlocks.add(block);
            }

            block = new ArrayList<String>();
            blockName = s.substring(1, s.length() - 1);
            inBlock = true;
          } else {
            s = replace(s, "COMMANDS", "ORDERS");
            block.add(s.trim());
          }
        }
      } while(s != null);

      if(inBlock && (block.size() > 0)) { // put the last block
        loadedNames.add(blockName);
        loadedBlocks.add(block);
      }

      r.close();
    } catch(Exception exc) {
      log.warn("Error reading desktop file " + exc.toString());
      log.debug("", exc);
    }

    //maybe old-style file
    if(read && (loadedNames.size() == 0)) {
      loadedNames.add("Standard");
      loadedBlocks.add(block);
    }

    // make sure there's a default set or even any set
    if(!loadedNames.contains("Standard")) {
      loadedNames.add("Standard");
      loadedBlocks.add(createStandardSplitSet());
      log.info("Creating \"Standard\" Split-Set.");
    }

    // Parse the loaded definitions
    FrameTreeBuilder builder = new FrameTreeBuilder();
    
    for (int i = 0; i < loadedNames.size();i++) {
      String name = loadedNames.get(i);
      List<String> def = loadedBlocks.get(i);

      // that's a layout
      if(name.startsWith("Layout_")) {
        loadLayout(name.substring(7), def);
      }
      // that's a split set
      else {
        log.info("Parsing split-set definition for \"" + name + "\"...");

        FrameTreeNode node = null;

        try {
          builder.buildTree(def.iterator());
          node = builder.getRoot();
        } catch(Exception exc) {
          node = null;
        }

        if(node != null) {
          if(splitSets == null) {
            splitSets = new Hashtable<String, FrameTreeNode>();
          }

          splitSets.put(name, node);
          log.info("Successful!");
        } else {
          log.info("Unable to parse! Please rework/remove this split-set.");
        }
      }
    }

    // create default values if nothing was successfully parsed
    if(splitSets == null) {
      splitSets = new Hashtable<String, FrameTreeNode>();

      try {
        //builder.buildTree(createStandardSplitSet().iterator());
        splitSets.put("Standard", builder.getRoot());
      } catch(Exception exc) {
      }

      log.info("Creating default split set.");
    }

    if(layoutComponents == null) {
      buildDefaultLayoutComponents();
    }
  }

  /**
   * Creates the default Split set. Current implementation emulates old-style Magellan.
   */
  protected List<String> createStandardSplitSet() {
    List<String> st = new ArrayList<String>(22);
    st.add("SPLIT 400 H");
    st.add("SPLIT 200 H");
    st.add("SPLIT 400 V");
    st.add("SPLIT 300 V");
    st.add("COMPONENT OVERVIEW");
    st.add("COMPONENT HISTORY");
    st.add("/SPLIT");
    st.add("COMPONENT MINIMAP");
    st.add("/SPLIT");
    st.add("SPLIT 400 V");
    st.add("COMPONENT MAP");
    st.add("COMPONENT MESSAGES");
    st.add("/SPLIT");
    st.add("/SPLIT");
    st.add("SPLIT 400 V");
    st.add("SPLIT 200 V");
    st.add("COMPONENT NAME&DESCRIPTION");
    st.add("COMPONENT DETAILS");
    st.add("/SPLIT");
    st.add("COMPONENT ORDERS");
    st.add("/SPLIT");
    st.add("/SPLIT");

    return st;
  }

  /**
   * Creates a default layout. Current implementation emulates old-style Magellan.
   */
  protected void buildDefaultLayoutComponents() {
    lManager = new DesktopLayoutManager();

    Map<String,DesktopLayoutManager.CPref> lMap = new HashMap<String, DesktopLayoutManager.CPref>();
    lMap.put("OVERVIEW", lManager.createCPref(0, 0, 0.25, 0.5));
    lMap.put("HISTORY", lManager.createCPref(0, 0.5, 0.25, 0.25));
    lMap.put("MINIMAP", lManager.createCPref(0, 0.75, 0.25, 0.25));
    lMap.put("MAP", lManager.createCPref(0.25, 0, 0.5, 0.75));
    lMap.put("MESSAGES", lManager.createCPref(0.25, 0.75, 0.5, 0.25));
    lMap.put("NAME&DESCRIPTION", lManager.createCPref(0.75, 0, 0.25, 0.25));
    lMap.put("DETAILS", lManager.createCPref(0.75, 0.25, 0.25, 0.5));
    lMap.put("ORDERS", lManager.createCPref(0.75, 0.75, 0.25, 0.25));
    layoutComponents = new HashMap<String,Map<String,DesktopLayoutManager.CPref>>();
    layoutComponents.put("Standard", lMap);
    log.info("Building \"Standard\" Layout");
  }

  /**
   * Loads a layout with name lName out of the given definition.
   */
  protected void loadLayout(String lName, List<String> def) {
    String msg = "Loading layout \"" + lName + "\"...";

    if(lManager == null) {
      lManager = new DesktopLayoutManager();
    }

    Map<String,DesktopLayoutManager.CPref> lMap = new HashMap<String, DesktopLayoutManager.CPref>();
    Iterator<String> it = def.iterator();

    while(it.hasNext()) {
      String sdef = it.next();

      if(sdef.indexOf('=') < 1) { //syntax is COMPONENT=x;y;w;h[;configuration]

        continue;
      }

      String cName = sdef.substring(0, sdef.indexOf('='));
      String definition = sdef.substring(sdef.indexOf('=') + 1);

      DesktopLayoutManager.CPref cPref = lManager.parseCPref(definition);

      if(cPref != null) {
        lMap.put(cName, cPref);
      }
    }

    if(lMap.size() > 0) {
      if(layoutComponents == null) {
        layoutComponents = new HashMap<String,Map<String,DesktopLayoutManager.CPref>>();
      }

      layoutComponents.put(lName, lMap);
      log.info(msg + "Successful!");
    } else {
      log.info(msg + "Unable to resolve! Please rework/remove this layout.");
    }
  }

  /**
   * Loads the frame definitions out of the settings. If they don't exist, a default set is
   * created using initFrameDefault().
   */
  /*
  protected void initFrameRectangles() {
    frames = new HashMap<String, FrameRectangle>();

    // seems to be a valid properties object
    if(settings.containsKey("Desktop.Frame0")) {
      int count = 0;
      String indexString = null;

      do {
        indexString = "Desktop.Frame" + String.valueOf(count);

        // good, a key found
        if(settings.containsKey(indexString)) {
          String str = settings.getProperty(indexString);
          str = replace(str, "COMMANDS", "ORDERS");

          StringTokenizer st = new StringTokenizer(str, ",");

          try {
            String xS = st.nextToken();
            String yS = st.nextToken();
            String wS = st.nextToken();
            String hS = st.nextToken();
            String id = st.nextToken();

            // String name 
            st.nextToken();

            String status = st.nextToken();
            int x = 0;
            int y = 0;
            int w = 0;
            int h = 0;
            x = Integer.parseInt(xS);
            y = Integer.parseInt(yS);
            w = Integer.parseInt(wS);
            h = Integer.parseInt(hS);

            // juch-hu, a valid frame found
            if(components.containsKey(id)) {
              // FrameRectangle frame=new FrameRectangle(name,id);
              FrameRectangle frame = createFrameRectangle(id);
              frame.setBounds(x, y, w, h);
              frames.put(id, frame);

              if(status.equals("ICON")) {
                frame.setState(Frame.ICONIFIED);
              } else {
                frame.setState(Frame.NORMAL);
              }

              // new token for visibility?
              if(st.hasMoreTokens()) {
                String visi = st.nextToken();

                if(visi.equalsIgnoreCase("INVISIBLE")) {
                  frame.setVisible(false);
                } else {
                  frame.setVisible(true);
                }
              }

              if(st.hasMoreTokens()) {
                frame.setConfiguration(st.nextToken());
              }
            }
          } catch(Exception exc) {
          }
        }

        count++;
      } while(settings.containsKey(indexString));

      if(frames.size() > 0) {
        return;
      }

      // hu - no frames parsed, use default
    }
  }*/

  /**
   * Computes the largest free rectangle on the screen: 
   * the biggest free place without the client frame.
   */
  protected Rectangle computeRectangle(Dimension screenSize) {
    // create the screen rectangle
    Rectangle screen = new Rectangle(0, 0, screenSize.width, screenSize.height);

    //create the client rectangle
    Rectangle clientR = client.getBounds();

    // client is not on screen!
    if(!screen.intersects(clientR)) {
      return screen;
    }

    clientR = screen.intersection(clientR);

    Rectangle horizontal = null;

    // the horizontally better one(prefer below client)
    if(clientR.y > (screen.height - (clientR.y + clientR.height))) {
      horizontal = new Rectangle(screen.width, clientR.y);
    } else {
      horizontal = new Rectangle(0, clientR.y + clientR.height, screen.width,
                     screen.height - (clientR.y + clientR.height));
    }

    Rectangle vertical = null;

    // the vertically better one(prefer right)
    if(clientR.x > (screen.width - (clientR.x + clientR.width))) {
      vertical = new Rectangle(clientR.x, screen.height);
    } else {
      vertical = new Rectangle(clientR.x + clientR.width, 0,
                   screen.width - (clientR.x + clientR.width), screen.height);
    }

    // check the bigger one, prefer the horizontal one
    if((vertical.width * vertical.height) > (horizontal.width * horizontal.height)) {
      return vertical;
    }

    return horizontal;
  }

  /*
   ########################
   # Desktop init methods #
   ########################
   */

  /**
   * Sets the Magellan window bounds according to current mode.
   */
  protected void setClientBounds() {
    if(splitRect == null) { // not initialized before, load it
      splitRect = PropertiesHelper.loadRect(settings, splitRect, "Client");
    }

    if(splitRect == null) { // still null - use full screen

      Dimension d = getToolkit().getScreenSize();
      splitRect = new Rectangle(0, 0, d.width, d.height);
    }
    log.debug("ClientBounds: "+splitRect);
    client.setBounds(splitRect);
  }

  protected void unconnectFrames() {
    if(frames != null) {
      Iterator it = frames.values().iterator();
      JPanel dummy = new JPanel();

      while(it.hasNext()) {
        FrameRectangle fr = (FrameRectangle) it.next();
        JFrame f = (JFrame) fr.getConnectedFrame();

        if(f != null) {
          f.setContentPane(dummy);
          fr.connectToFrame(null);
        }
      }
    }
  }

  protected void validateDesktopMenu() {
    if (splitRoot != null && splitRoot instanceof RootWindow) {
      RootWindow root = (RootWindow)splitRoot;
      for (int i=0; i<desktopMenu.getItemCount(); i++) {
        JMenuItem menuItem = desktopMenu.getItem(i);
        if (menuItem instanceof JCheckBoxMenuItem) {
          JCheckBoxMenuItem item = (JCheckBoxMenuItem)menuItem;
          String name = item.getActionCommand().substring(5);
          DockingWindow window = findDockingWindow(root, name);
          if (window != null) {
            item.setSelected(true);
          }
        }
      }
      
      DockingFrameworkBuilder.updateLayoutMenu();
    }
  }

  /**
   * Initializes the desktop using SplitPanes.
   */
  protected boolean initSplitSet(String setName) {
    // can't find split-set
    if(!splitSets.containsKey(setName)) {
      return false;
    }

    setClientBounds();

    // clear the frames
    unconnectFrames();

    //get out area, (approximatly)
    Rectangle r = client.getBounds();
    r.x += 3;
    r.y += 20;
    r.width -= 6;
    r.height -= 23;

    if(splitBuilder == null) {
      splitBuilder = new DockingFrameworkBuilder(r);
    }

    splitBuilder.setScreen(r);
    
    try {
      splitRoot = splitBuilder.buildDesktop(splitSets.get(setName), components, new File(magellanDir,"dock-default.xml"));
      if (splitRoot != null && splitRoot instanceof RootWindow) {
        ((RootWindow)splitRoot).addListener(this);
      }
    } catch(Exception exc) {
      log.error(exc);
      return false;
    }

    if(splitRoot == null) {
      return false;
    }

    splitName = setName;
    workSpace.setContent(splitRoot);
    buildShortCutTable(splitBuilder.getComponentsUsed());

    modeInitialized = true;

    return true;
  }

  /**
   * Load the frame mode settings.
   */
  protected void loadFrameModeSettings() {
    // load activation and iconification settings
    try {
      activationMode = Integer.parseInt(settings.getProperty("Desktop.ActivationMode"));
    } catch(Exception exc) {
      activationMode = 0;
    }

    try {
      iconify = settings.getProperty("Desktop.IconificationMode").equals("true");
    } catch(Exception exc) {
      iconify = false;
    }
  }

  /*
   ######################
   # Shortcut - Methods #
   ######################
   */

  /**
   * Based on the components used by the desktop this method builds the KeyStroke-HashMap. The
   * key is the KeyStroke-Object returned by ShortcutListener.getShortCuts(), the value is the
   * listener object.
   *
   * 
   */
  protected void buildShortCutTable(Collection scomps) {
    //shortCutComponents=CollectionFactory.createHashMap();
    Iterator it = scomps.iterator();

    while(it.hasNext()) {
      Object o = it.next();

      if(o instanceof ShortcutListener) {
        ShortcutListener sl = (ShortcutListener) o;
        Iterator<KeyStroke> it2 = sl.getShortCuts();

        while(it2.hasNext()) {
          KeyStroke stroke = it2.next();
          shortCutListeners.put(stroke, o);
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

  protected void loadTranslations() {
    String s = settings.getProperty("Desktop.KeyTranslations");

    if(s != null) {
      StringTokenizer st = new StringTokenizer(s, ",");

      while(st.hasMoreTokens()) {
        try {
          KeyStroke newStroke = KeyStroke.getKeyStroke(Integer.parseInt(st.nextToken()),
                                 Integer.parseInt(st.nextToken()));
          KeyStroke oldStroke = KeyStroke.getKeyStroke(Integer.parseInt(st.nextToken()),
                                 Integer.parseInt(st.nextToken()));
          registerTranslation(newStroke, oldStroke);
        } catch(RuntimeException exc) {
        }
      }
    }
  }

  protected void saveTranslations() {
    if(shortCutTranslations.size() > 0) {
      StringBuffer buf = new StringBuffer();
      Iterator it = shortCutTranslations.entrySet().iterator();

      while(it.hasNext()) {
        Map.Entry e = (Map.Entry) it.next();
        KeyStroke stroke = (KeyStroke) e.getKey();
        buf.append(stroke.getKeyCode());
        buf.append(',');
        buf.append(stroke.getModifiers());
        buf.append(',');
        stroke = (KeyStroke) e.getValue();
        buf.append(stroke.getKeyCode());
        buf.append(',');
        buf.append(stroke.getModifiers());

        if(it.hasNext()) {
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
    if(newStroke.equals(oldStroke)) { // senseless

      return;
    }

    if(!shortCutTranslations.containsKey(oldStroke)) {
      keyHandler.removeStroke(oldStroke);
    }

    keyHandler.addTranslationStroke(newStroke);
    shortCutTranslations.put(newStroke, oldStroke);
  }

  /**
   * 
   */
  public void removeTranslation(KeyStroke stroke) {
    if(shortCutTranslations.containsKey(stroke)) {
      KeyStroke old = (KeyStroke) shortCutTranslations.get(stroke);
      keyHandler.removeStroke(stroke);
      keyHandler.install(old);
      shortCutTranslations.remove(stroke);
    }
  }

  /**
   * 
   */
  public KeyStroke getTranslation(KeyStroke newStroke) {
    return (KeyStroke) shortCutTranslations.get(newStroke);
  }

  /**
   * 
   */
  public KeyStroke findTranslation(KeyStroke oldStroke) {
    Iterator it = shortCutTranslations.keySet().iterator();

    while(it.hasNext()) {
      Object obj = it.next();

      if(shortCutTranslations.get(obj).equals(oldStroke)) {
        return (KeyStroke) obj;
      }
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
   * Registers a new KeyStroke/ActionListener pair at the desktop. If the given boolean is FALSE,
   * the listener will not be registered at the Client frame. This feature is for menu items.
   */
  public void registerShortcut(KeyStroke stroke, ActionListener al) {
    keyHandler.addStroke(stroke, al, true);
  }

  /*
   ################################
   # Frame management methods     #
   # ONLY USED WHEN IN FRAME MODE #
   ################################
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
   * Check if this event comes from the client window and if all windows should be deiconified -
   * if so, deiconify all and put them to front.
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
   * Check if this  event comes from the client window and if all windows should be iconified -
   * if so, iconify them.
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
   * 
   * <ul>
   *   <li>DO NOTHING ON ACTIVATION</li>
   *   <li>ACTIVATE ALL WHEN CLIENT IS ACTIVATED</li>
   *   <li>ACTIVATE ALL IF ANY WINDOW IS ACTIVATED</li>
   * </ul>
   */
  public void windowActivated(java.awt.event.WindowEvent p1) {
    // clear "input buffer"
    keyHandler.resetExtendedShortcutListener();

    if(inFront) {
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
   #########################
   # Cross-Mode Management #
   # Focus and Repaint     #
   #########################
   */

  /**
   * The component with the ID id will gain the focus. If Frame Mode is enabled, the parent frame
   * will be activated.
   */
  public void componentRequestFocus(String id) {
    if(!components.containsKey(id)) {
      return;
    }

    if (splitRoot != null && splitRoot instanceof RootWindow) {
      restoreView(id);
    }

    // search in component table to activate directly
    if (components.get(id)!=null)
      ((Component) components.get(id)).requestFocus();
  }

  /**
   * Repaints the component with ID id.
   */
  public void repaint(String id) {
    if(!components.containsKey(id)) {
      return;
    }
  }
  
  public void repaintAllComponents() {
    // ????
  }

  /**
   * 
   */
  public void updateLaF() {
    SwingUtilities.updateComponentTreeUI(client);

    // to avoid start bug
    if(desktopMenu != null) {
      SwingUtilities.updateComponentTreeUI(desktopMenu);
    }
  }
  
  private DockingWindow findDockingWindow(DockingWindow root, String name) {
    if (root == null) return null;
    if (root.getName() != null && root.getName().equals(name)) {
      return root;
    }

    for (int index=0; index<root.getChildWindowCount(); index++) {
      DockingWindow window = findDockingWindow(root.getChildWindow(index),name);
      if (window != null) return window;
    }
    return null;
  }

  /**
   * 
   */
  public void actionPerformed(ActionEvent p1) {
    if(p1.getSource() == timer) {
      inFront = false;
      timer.stop();
    } else if(p1.getSource() instanceof JCheckBoxMenuItem) {
      JCheckBoxMenuItem menu = (JCheckBoxMenuItem)p1.getSource();
      String action = p1.getActionCommand();
      if (action != null && action.startsWith("menu.")) {
        String name = action.substring(5);
        if (splitRoot != null && splitRoot instanceof RootWindow) {
          RootWindow root = (RootWindow)splitRoot;
          if (menu.isSelected()) {
            // open dock via name
            restoreView(name);
          } else {
            // close dock
            DockingWindow window = findDockingWindow(root, name);
            if (window != null) {
              window.close();
            } else {
              menu.setSelected(false);
            }
            
          }
          
        }
        
      }
    }
  }

   /** 
   * Makes a view visible.
   * 
   * @param name
   */
  private void restoreView(String name) {
    View view = splitBuilder.getViewMap().getView(name);
    if (view != null) {
      view.restore();
      view.makeVisible();
    }
  }

  ///////////////////////////////////////
  //                                   //
  //  S H O R T C U T - H A N D L E R  //
  //                                   //
  ///////////////////////////////////////

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
      if(lastComponents.size() > 0) {
        disconnect();
      }

      lastComponents.addAll(deskElements);

      Set<KeyStroke> set = new HashSet<KeyStroke>(shortCutTranslations.keySet());
      Set<KeyStroke> set2 = new HashSet<KeyStroke>(shortCutListeners.keySet());
      Iterator<KeyStroke> it = set.iterator();

      while(it.hasNext()) {
        set2.remove(shortCutTranslations.get(it.next()));
      }

      set.addAll(set2);

      it = set.iterator();

      while(it.hasNext()) {
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
      if(lastComponents.size() > 0) {
        Iterator it = shortCutListeners.keySet().iterator();

        while(it.hasNext()) {
          KeyStroke str = (KeyStroke) it.next();
          remove(str);
        }

        it = extendedListeners.iterator();

        while(it.hasNext()) {
          KeyStroke str = (KeyStroke) it.next();
          remove(str);
        }

        it = shortCutTranslations.keySet().iterator();

        while(it.hasNext()) {
          KeyStroke str = (KeyStroke) it.next();
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
      if((findTranslation(str) == null) && !shortCutListeners.containsKey(str)) {
        install(str);
      } else if(actionAware) {
        KeyStroke newStroke = findTranslation(str);

        if((newStroke != null) && (dest instanceof Action)) {
          ((Action) dest).putValue("accelerator", newStroke);
        }
      }

      shortCutListeners.put(str, dest);
    }

    /**
     * 
     */
    public void addListener(ShortcutListener sl) {
      Iterator it = sl.getShortCuts();

      while(it.hasNext()) {
        addStroke((KeyStroke) it.next(), sl, false);
      }
    }

    /**
     * 
     */
    public void keyPressed(KeyStroke e, Object src) {
      // "translate" the key
      if(shortCutTranslations.containsKey(e)) {
        e = (KeyStroke) shortCutTranslations.get(e);
      }

      // redirect only for sub-listener
      if(lastListener != null) {
        remove(extendedListeners, false);

        if(extendedListeners.contains(e)) {
          reactOnStroke(e, lastListener);

          return;
        }

        lastListener = null;
      }

      if(shortCutListeners.containsKey(e)) {
        Object o = shortCutListeners.get(e);

        if(o instanceof ShortcutListener) {
          reactOnStroke(e, (ShortcutListener) o);
        } else if(o instanceof ActionListener) {
          /*if (src instanceof JComponent)
              if (((JComponent)src).getTopLevelAncestor()==client)
                  return;*/
          ((ActionListener) o).actionPerformed(null);
        }

        return;
      }

      log.info("Error: Unrecognized key stroke " + e);
    }

    protected void install(ShortcutListener sl, boolean flag) {
      Iterator<KeyStroke> it = sl.getShortCuts();

      while(it.hasNext()) {
        KeyStroke stroke = it.next();

        if(flag) {
          extendedListeners.add(stroke);
        }

        if(!shortCutListeners.containsKey(stroke)) {
          install(stroke);
        }
      }
    }

    protected void install(KeyStroke stroke) {
      Iterator it = lastComponents.iterator();
      KeyboardActionListener kal = new KeyboardActionListener();
      kal.stroke = stroke;

      while(it.hasNext()) {
        Component c = (Component) it.next();

        if(c instanceof JComponent) {
          ((JComponent) c).registerKeyboardAction(kal, stroke,
                              JComponent.WHEN_IN_FOCUSED_WINDOW);
        } else if(c instanceof Component) {
          addToContainer((Component) c, stroke, kal);
        }
      }
    }

    /**
     * Removes the given key stroke from the container searching an instance of JComponent.
     */
    protected boolean addToContainer(Component c, KeyStroke s, ActionListener al) {
      if(c instanceof JComponent) {
        ((JComponent) c).registerKeyboardAction(al, s, JComponent.WHEN_IN_FOCUSED_WINDOW);

        return true;
      }

      if(c instanceof Container) {
        Container con = (Container) c;

        if(con.getComponentCount() > 0) {
          Component comp[] = con.getComponents();

          for(int i = 0; i < comp.length; i++) {
            if(addToContainer(comp[i], s, al)) {
              return true;
            }
          }
        }
      }

      return false;
    }

    /**
     * Removes the KeyStrokes in the collection from all desktop components. If the given flag
     * is true, all key-strokes in the collection are removed, else only non-base-level
     * key-strokes.
     */
    protected void remove(Collection col, boolean flag) {
      Iterator it = col.iterator();

      while(it.hasNext()) {
        KeyStroke stroke = (KeyStroke) it.next();

        if(flag ||
             (!shortCutTranslations.containsKey(stroke) &&
             !shortCutListeners.containsKey(stroke))) {
          remove(stroke);
        }
      }
    }

    protected void remove(KeyStroke stroke) {
      Iterator it2 = lastComponents.iterator();

      while(it2.hasNext()) {
        Component c = (Component) it2.next();

        if(c instanceof JComponent) {
          ((JComponent) c).unregisterKeyboardAction(stroke);
        } else if(c instanceof Component) {
          removeFromContainer((Component) c, stroke);
        }
      }
    }

    /**
     * Removes the given key stroke from the container searching an instance of JComponent.
     */
    protected boolean removeFromContainer(Component c, KeyStroke s) {
      if(c instanceof JComponent) {
        ((JComponent) c).unregisterKeyboardAction(s);

        return true;
      }

      if(c instanceof Container) {
        Container con = (Container) c;

        if(con.getComponentCount() > 0) {
          Component comp[] = con.getComponents();

          for(int i = 0; i < comp.length; i++) {
            if(removeFromContainer(comp[i], s)) {
              return true;
            }
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
      if(sl instanceof ExtendedShortcutListener) {
        ExtendedShortcutListener esl = (ExtendedShortcutListener) sl;

        if(esl.isExtendedShortcut(stroke)) {
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
                           magellan.client.event.SelectionListener
    {
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

  ///////////////////////////////////
  // CONFIGURATION CODE - INTERNAL //
  ///////////////////////////////////

  /**
   * Sets the desktop mode. If the mode changes, the desktop is newly initialized. Possible
   * values are:
   * 
   * <ul>
   *   <li>MODE_SPLIT: Build the desktop with SplitPanes.</li>
   *   <li>MODE_FRAME: Build the desktop with Frames.</li>
   *   <li>MODE_LAYOUT: Build the desktop with the special Layout.</li>
   * </ul>
   * 
   * The given parameter describes the mode.
   */
  public void setMode(Object param) {
    if(!param.equals(splitName)) {
      splitRect = client.getBounds(splitRect);
    }

    inFront = false;
    retrieveConfiguration();
    
    workSpace.removeContent();

    if((param != null) || !(param instanceof String)) {
      if(!initSplitSet((String) param)) {
        initSplitSet("Standard");
      }
    } else if(!initSplitSet(settings.getProperty("Desktop.SplitSet", "Standard"))) {
      initSplitSet("Standard");
    }

    setAllVisible(true);
    registerKeyStrokes(); // register listeners to the new frames

    splitRoot.setBorder(null);
    splitRoot.repaint();

    client.repaint(500);
  }

  /**
   * Retrieves possible init configurations out of the current desktop.
   */
  protected void retrieveConfiguration() {
    if((splitSets != null) && splitSets.containsKey(splitName)) {
      FrameTreeNode ftr = (FrameTreeNode) splitSets.get(splitName);
      retrieveFromSplitImpl(ftr);
    }
  }

  private void retrieveFromSplitImpl(FrameTreeNode ftr) {
    if(ftr == null) {
      return;
    }

    if(ftr.isLeaf()) {
      if(components.containsKey(ftr.getName())) {
                String name = ftr.getName();
                // special meaning of overview
                if(name.equals("OVERVIEW"))  {
                    name = "OVERVIEW&HISTORY";
                }  
                   
                if(components.get(name) instanceof Initializable) {
                    ftr.setConfiguration(((Initializable) components.get(name)).getComponentConfiguration());
                }
      }
    } else {
      retrieveFromSplitImpl(ftr.getChild(0));
      retrieveFromSplitImpl(ftr.getChild(1));
    }
  }

  ///////////////////////////////////
  // CONFIGURATION CODE - EXTERNAL //
  ///////////////////////////////////

  /**
   * Writes the configuration of this desktop.
   */
  public void save() {
    retrieveConfiguration();

    saveSplitModeProperties();
    try {
      saveDesktopFile();
    } catch(Exception exc) {}

    saveTranslations();
    
    try {
      splitBuilder.write(new File(magellanDir,"dock-default.xml"), (RootWindow)splitRoot);
    } catch (Throwable t) {
      log.fatal(t.getMessage(),t);
      ErrorWindow errorWindow = new ErrorWindow(Client.INSTANCE,t.getMessage(),"",t);
      errorWindow.setVisible(true);
    }
  }

  /**
   * Saves properties of Split Mode. Following properties may be set:
   * 
   * <ul>
   *   <li>Client.x,Client.y,Client.width,Client.height</li>
   *   <li>Desktop.Type</li>
   *   <li>Desktop.SplitSet</li>
   * </ul>
   */
  protected void saveSplitModeProperties() {
    settings.setProperty("Desktop.Type", "SPLIT");
    splitRect = client.getBounds();

    PropertiesHelper.saveRectangle(settings, splitRect, "Client");
    settings.setProperty("Desktop.SplitSet", splitName);
  }

  /**
   * Saves the content of splitSets and layoutComponents to the desktop file.
   */
  protected void saveDesktopFile() throws Exception {
    File magFile = getDesktopFile(true);

    if(magFile.exists()) {
      try {
        File backup = FileBackup.create(magFile);
        log.info("Created backupfile " + backup);
      } catch(IOException ie) {
        log.warn("Could not create backupfile for file " + magFile);
      }
    }

    log.info("Storing Magellan desktop configuration to " + magFile);
        
    PrintWriter out = new PrintWriter(new FileWriter(magFile));
    Iterator it = splitSets.keySet().iterator();

    while(it.hasNext()) {
      try {
        String name = (String) it.next();
        saveList(name, (FrameTreeNode) splitSets.get(name), out);
      } catch(Exception exc) {
      }
    }

    it = layoutComponents.keySet().iterator();

    while(it.hasNext()) {
      try {
        String name = (String) it.next();
        saveList("Layout_" + name, (Map) layoutComponents.get(name), out);
      } catch(Exception exc) {
      }
    }

    out.close();
  }

  /**
   * Writes the given map with the given block name to out.
   */
  private void saveList(String name, Map map, PrintWriter out) {
    out.println('[' + name + ']');

    Iterator it = map.keySet().iterator();

    while(it.hasNext()) {
      Object o = it.next();
      out.println(o.toString() + '=' + map.get(o).toString());
    }
  }

  /**
   * Writes the given FrameTreeNode structure with block name to out.
   */
  private void saveList(String name, FrameTreeNode root, PrintWriter out) {
    out.println('[' + name + ']');
    root.write(out);
  }

  /**
   * Saves the FrameRectangle structure to the given properties object.
   */
  protected void saveFrames(Map fr, Properties p) {
    // save activation and iconification setting
    p.setProperty("Desktop.ActivationMode", String.valueOf(activationMode));
    p.setProperty("Desktop.IconificationMode", String.valueOf(iconify));

    Iterator it = fr.keySet().iterator();
    int i = 0;

    while(it.hasNext()) {
      String id = (String) it.next();
      FrameRectangle f = (FrameRectangle) fr.get(id);
      String icon = null;
      String configuration = f.getConfiguration();

      if(f.getState() == Frame.ICONIFIED) {
        icon = "ICON";
      } else {
        icon = "NORMAL";
      }

      String x = String.valueOf((int) f.getX());
      String y = String.valueOf((int) f.getY());
      String w = String.valueOf((int) f.getWidth());
      String h = String.valueOf((int) f.getHeight());
      String v = null;

      if(f.isVisible()) {
        v = "VISIBLE";
      } else {
        v = "INVISIBLE";
      }

      String property = x + ',' + y + ',' + w + ',' + h + ',' + id + ',' + f.getFrameTitle() +
                ',' + icon + ',' + v;

      if(configuration != null) {
        property += (',' + configuration);
      }

      p.setProperty("Desktop.Frame" + String.valueOf(i), property);
      i++;
    }
  }

  ////////////////////////////////
  // LAYOUT-MODE Layout Manager //
  ////////////////////////////////

  /**
   * Simple layout manager for layout mode. It uses the inner class CPref to manage the
   * components.
   */
  protected class DesktopLayoutManager implements LayoutManager2 {
    private Map<Component,CPref> componentPrefs;
    private Dimension minDim;
    private Dimension prefDim;
    private Dimension cSize;

    /**
     * Creates new DesktopLayoutManager
     */
    public DesktopLayoutManager() {
      componentPrefs = new HashMap<Component, CPref>();
      minDim = new Dimension(100, 100);

      Toolkit t = Toolkit.getDefaultToolkit();
      prefDim = t.getScreenSize();

      //for frame
      prefDim.width -= 10;
      prefDim.height -= 30;
      cSize = new Dimension();
    }

    /**
     * 
     */
    public void addLayoutComponent(java.lang.String str, java.awt.Component component) {
      CPref pref = parseCPref(str);

      if(pref != null) {
        addLayoutComponent(component, pref);
      }
    }

    /**
     * 
     */
    public void layoutContainer(java.awt.Container container) {
      Iterator<Component> it = componentPrefs.keySet().iterator();
      cSize = container.getSize(cSize);

      while(it.hasNext()) {
        Component c = it.next();
        CPref cP = componentPrefs.get(c);
        c.setBounds((int) (cSize.width * cP.x), (int) (cSize.height * cP.y),
              (int) (cSize.width * cP.w), (int) (cSize.height * cP.h));
      }
    }

    /**
     * 
     */
    public java.awt.Dimension minimumLayoutSize(java.awt.Container container) {
      return minDim;
    }

    /**
     * 
     */
    public java.awt.Dimension preferredLayoutSize(java.awt.Container container) {
      return prefDim;
    }

    /**
     * 
     */
    public void removeLayoutComponent(java.awt.Component component) {
      componentPrefs.remove(component);
    }

    /**
     * 
     */
    public void addLayoutComponent(java.awt.Component component, java.lang.Object obj) {
      if(obj instanceof String) {
        addLayoutComponent((String) obj, component);
      }

      if(obj instanceof CPref) {
        componentPrefs.put(component, (CPref)obj);
      }
    }

    /**
     * 
     */
    public float getLayoutAlignmentX(java.awt.Container container) {
      return 0;
    }

    /**
     * 
     */
    public float getLayoutAlignmentY(java.awt.Container container) {
      return 0;
    }

    /**
     * 
     */
    public void invalidateLayout(java.awt.Container container) {
    }

    /**
     * 
     */
    public java.awt.Dimension maximumLayoutSize(java.awt.Container container) {
      return prefDim;
    }

    /**
     * 
     */
    public CPref parseCPref(String s) {
      try {
        StringTokenizer st = new StringTokenizer(s, ";");
        String sx = st.nextToken();
        String sy = st.nextToken();
        String sw = st.nextToken();
        String sh = st.nextToken();
        double x = Double.parseDouble(sx);
        double y = Double.parseDouble(sy);
        double w = Double.parseDouble(sw);
        double h = Double.parseDouble(sh);
        CPref pref = new CPref(x, y, w, h);

        if(st.hasMoreTokens()) {
          pref.setConfiguration(st.nextToken());
        }

        return pref;
      } catch(Exception exc) {
      }

      return null;
    }

    /**
     * 
     */
    public CPref createCPref(double x, double y, double w, double h) {
      return new CPref(x, y, w, h);
    }

    /**
     * Class for storing wished component bounds. Values are between 0 and 1 and are treated as
     * percent numbers.
     */
    public class CPref {
      protected double x;
      protected double y;
      protected double w;
      protected double h;
      protected String configuration;

      /**
       * Creates a new CPref object.
       */
      public CPref() {
        x = y = w = h = 0;
      }

      /**
       * Creates a new CPref object.
       */
      public CPref(double d1, double d2, double d3, double d4) {
        x = d1;
        y = d2;
        w = d3;
        h = d4;
      }

      /**
       * 
       */
      public String toString() {
        if(configuration == null) {
          return String.valueOf(x) + ';' + String.valueOf(y) + ';' + String.valueOf(w) +
               ';' + String.valueOf(h);
        }

        return String.valueOf(x) + ';' + String.valueOf(y) + ';' + String.valueOf(w) + ';' +
             String.valueOf(h) + ';' + configuration;
      }

      /**
       * 
       */
      public String getConfiguration() {
        return configuration;
      }

      /**
       * 
       */
      public void setConfiguration(String config) {
        configuration = config;
      }
    }
  }

  ///////////////////////
  // WINDOW ACTIVATION //
  ///////////////////////

  /**
   * Runnable used to activate all displayed windows. This will move them to the front of the
   * desktop.
   */
  private class WindowActivator implements Runnable {
    protected Window source;

    /**
     * Creates a new WindowActivator object.
     */
    public WindowActivator(Window s) {
      source = s;
    }

    /**
     * 
     */
    public void run() {
        return;
    }
  }

  /////////////////////////
  // PREFERENCES ADAPTER //
  /////////////////////////
  /**
   * @see com.eressea.swing.preferences.PreferencesFactory#createPreferencesAdapter()
   */
  public PreferencesAdapter createPreferencesAdapter() {
    return new DesktopPreferences();
  }

  /**
   * Encapsulates the preferences tab for the desktop.
   */
  private class DesktopPreferences extends JPanel implements ActionListener,
                                 ExtendedPreferencesAdapter
  {
    JComboBox modeBox;
    JTextArea actLabel;
    JTextArea icoLabel;
    JComboBox icoBox;
    JComboBox actMode;
    CardLayout card;
    JPanel center;
    List<ShortcutList> scList;
    JCheckBox enableWorkSpaceChooser;

    private final String act[] = {
                     Resources.get("desktop.magellandesktop.prefs.activationdescription.single"),
                     Resources.get("desktop.magellandesktop.prefs.activationdescription.main"),
                     Resources.get("desktop.magellandesktop.prefs.activationdescription.all")
                   };
    private final String ico[] = {
                     Resources.get("desktop.magellandesktop.prefs.iconifydescription.single"),
                     Resources.get("desktop.magellandesktop.prefs.iconifydescription.main")
                   };

    /**
     * Creates a new DesktopPreferences object.
     */
    public DesktopPreferences() {
      this.setLayout(new BorderLayout());

      JPanel up = new JPanel(new FlowLayout(FlowLayout.LEADING));
      up.add(new JLabel(Resources.get("desktop.magellandesktop.prefs.lbl.mode.caption")));

      String modeItems[] = new String[1];
      modeItems[0] = Resources.get("desktop.magellandesktop.prefs.modeitem.split");
      modeBox = new JComboBox(modeItems);
      modeBox.setSelectedIndex(0);
      modeBox.addActionListener(this);
      modeBox.setEnabled(false);
      up.add(modeBox);
      
      enableWorkSpaceChooser = new JCheckBox(Resources.get("desktop.magellandesktop.prefs.displaychooser"), workSpace.isEnabledChooser());;
      up.add(enableWorkSpaceChooser);
      
      this.add(up, BorderLayout.NORTH);

      center = new JPanel();
      center.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(),
                        Resources.get("desktop.magellandesktop.prefs.border.options")));
      center.setLayout(card = new CardLayout());

      JTextArea splitText = new JTextArea(Resources.get("desktop.magellandesktop.prefs.txt.split.text"));
      splitText.setEditable(false);
      splitText.setLineWrap(true);
      splitText.setWrapStyleWord(true);
      splitText.setBackground(center.getBackground());

      JLabel cDummy = new JLabel();
      splitText.setFont(cDummy.getFont());
      splitText.setForeground(cDummy.getForeground());
      center.add(splitText, "0");

      JPanel panel = new JPanel();
      panel.setLayout(new GridBagLayout());

      GridBagConstraints con = new GridBagConstraints();

      con.gridx = 0;
      con.gridwidth = 1;
      con.gridy = 0;
      con.gridheight = 1;
      con.fill = GridBagConstraints.HORIZONTAL;
      con.anchor = GridBagConstraints.NORTHWEST;
      con.weightx = 0.25;

      panel.add(new JLabel(Resources.get("desktop.magellandesktop.prefs.lbl.activationmode.caption")), con);

      con.gridx = 1;
      con.gridwidth = 3;
      con.weightx = 0.75;

      String actItems[] = new String[3];
      actItems[0] = Resources.get("desktop.magellandesktop.prefs.activationmode.single");
      actItems[1] = Resources.get("desktop.magellandesktop.prefs.activationmode.main");
      actItems[2] = Resources.get("desktop.magellandesktop.prefs.activationmode.all");
      actMode = new JComboBox(actItems);
      actMode.addActionListener(this);
      actLabel = new JTextArea(act[getActivationMode()]);
      actLabel.setEditable(false);
      actLabel.setBackground(this.getBackground());
      actLabel.setLineWrap(true);
      actLabel.setWrapStyleWord(true);
      actMode.setSelectedIndex(getActivationMode());
      panel.add(actMode, con);
      con.gridy = 1;
      panel.add(actLabel, con);

      con.gridx = 0;
      con.gridy = 2;
      con.gridwidth = 1;
      con.weightx = 0.25;
      panel.add(new JLabel(Resources.get("desktop.magellandesktop.prefs.lbl.iconify.caption")), con);
      con.gridx = 1;
      con.gridwidth = 3;
      con.weightx = 0.75;

      String icoItems[] = new String[2];
      icoItems[0] = Resources.get("desktop.magellandesktop.prefs.iconify.single");
      icoItems[1] = Resources.get("desktop.magellandesktop.prefs.iconify.main");
      icoBox = new JComboBox(icoItems);
      icoBox.setSelectedIndex(isIconify() ? 1 : 0);
      icoBox.addActionListener(this);
      icoLabel = new JTextArea(isIconify() ? ico[1] : ico[0]);
      icoLabel.setEditable(false);
      icoLabel.setBackground(this.getBackground());
      icoLabel.setLineWrap(true);
      icoLabel.setWrapStyleWord(true);

      panel.add(icoBox, con);
      con.gridy = 3;
      panel.add(icoLabel, con);

      center.add(panel, "1");

      splitText = new JTextArea(Resources.get("desktop.magellandesktop.prefs.txt.layout.text"));
      splitText.setEditable(false);
      splitText.setLineWrap(true);
      splitText.setWrapStyleWord(true);
      splitText.setBackground(center.getBackground());
      splitText.setFont(cDummy.getFont());
      splitText.setForeground(cDummy.getForeground());
      center.add(splitText, "2");

      this.add(center, BorderLayout.CENTER);

      scList = new ArrayList<ShortcutList>(1);
      scList.add(new ShortcutList());
    }

    /**
     * 
     */
    public void actionPerformed(java.awt.event.ActionEvent p1) {
      if(p1.getSource() == modeBox) { // change card

        switch(modeBox.getSelectedIndex()) {
        case 0:
          card.first(center);

          break;

        case 1:
          card.show(center, "1");

          break;

        case 2:
          card.last(center);

          break;
        }
      }

      if(p1.getSource() == actMode) { // change label
        actLabel.setText(act[actMode.getSelectedIndex()]);
      }

      if(p1.getSource() == icoBox) { // change label
        icoLabel.setText(ico[icoBox.getSelectedIndex()]);
      }
    }

    /**
     * 
     */
    public boolean isModeIconify() {
      return icoBox.getSelectedIndex() == 1;
    }

    /**
     * 
     */
    public java.awt.Component getComponent() {
      return this;
    }

    /**
     * 
     */
    public java.lang.String getTitle() {
      return Resources.get("desktop.magellandesktop.prefs.title");
    }

    /**
     * @see magellan.client.swing.preferences.PreferencesAdapter#initPreferences()
     * @deprecated TODO: implement it
     */
    public void initPreferences() {
        // TODO: implement it
    }
        
    /**
     * 
     */
    public void applyPreferences() {
      setIconify(isModeIconify());
      setWorkSpaceChooser(enableWorkSpaceChooser.isSelected());
    }

    /**
     * 
     */
    public List getChildren() {
      return scList;
    }

    protected class ShortcutList extends JPanel implements PreferencesAdapter, ActionListener {
      protected JTable table;
      protected DefaultTableModel model;
      protected Collator collator;
      protected Set<KeyStroke> ownShortcuts;
      protected Set<KeyStroke> otherShortcuts;

      /**
       * Creates a new ShortcutList object.
       */
      public ShortcutList() {
        try {
          collator = Collator.getInstance(magellan.library.utils.Locales.getGUILocale());
        } catch(IllegalStateException exc) {
          collator = Collator.getInstance();
        }

        if(shortCutListeners != null) {
          Object columns[] = {
                       Resources.get("desktop.magellandesktop.prefs.shortcuts.header1"),
                       Resources.get("desktop.magellandesktop.prefs.shortcuts.header2")
                     };

          Map<Object,List<KeyStroke>> listeners = new HashMap<Object, List<KeyStroke>>();
          Iterator it = shortCutListeners.entrySet().iterator();

          while(it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            Object value = entry.getValue();

            if(!listeners.containsKey(value)) {
              listeners.put(value, new LinkedList<KeyStroke>());
            }

            // try to find a translation
            KeyStroke oldStroke = (KeyStroke) entry.getKey();
            KeyStroke newStroke = findTranslation(oldStroke);

            if(newStroke != null) {
              listeners.get(value).add(newStroke);
            } else {
              listeners.get(value).add(oldStroke);
            }
          }

          Object data[][] = new Object[shortCutListeners.size() + listeners.size()][2];

          List<Object> list2 = new LinkedList<Object>(listeners.keySet());

          Collections.sort(list2, new ListenerComparator());

          it = list2.iterator();

          int i = 0;

          while(it.hasNext()) {
            Object key = it.next();
            ShortcutListener sl = null;

            if(key instanceof ShortcutListener) {
              sl = (ShortcutListener) key;
              data[i][0] = sl.getListenerDescription();

              if(data[i][0] == null) {
                data[i][0] = sl;
              }
            } else {
              data[i][0] = key;
            }

            data[i][1] = null;

            i++;

            List<KeyStroke> list = listeners.get(key);

            Collections.sort(list, new KeyStrokeComparator());

            Iterator it2 = list.iterator();

            while(it2.hasNext()) {
              Object obj = it2.next();
              data[i][0] = obj;

              if(sl != null) {
                if(shortCutTranslations.containsKey(obj)) {
                  obj = getTranslation((KeyStroke) obj);
                }

                try {
                  data[i][1] = sl.getShortcutDescription(obj);

                  if(data[i][1] == null) {
                    data[i][1] = Resources.get("desktop.magellandesktop.prefs.shortcuts.unknown");
                  }
                } catch(RuntimeException re) {
                  data[i][1] = Resources.get("desktop.magellandesktop.prefs.shortcuts.unknown");
                }
              } else {
                data[i][1] = key;
              }

              i++;
            }
          }

          model = new DefaultTableModel(data, columns);

          StrokeRenderer sr = new StrokeRenderer();
          DefaultTableColumnModel tcm = new DefaultTableColumnModel();
          TableColumn column = new TableColumn();
          column.setHeaderValue(columns[0]);
          column.setCellRenderer(sr);
          column.setCellEditor(null);
          tcm.addColumn(column);
          column = new TableColumn(1);
          column.setHeaderValue(columns[1]);
          column.setCellRenderer(sr);
          column.setCellEditor(null);
          tcm.addColumn(column);

          table = new JTable(model, tcm);
          this.setLayout(new BorderLayout());
          this.add(new JScrollPane(table), BorderLayout.CENTER);
          table.addMouseListener(getMousePressedMouseListener());
        }

        // find all java keystrokes
        Set<KeyStroke> set = new HashSet<KeyStroke>();
        Collection<Frame> desk = new LinkedList<Frame>();
        desk.add(client);

        Iterator<Frame> it1 = desk.iterator();

        while(it1.hasNext()) {
          addKeyStrokes(it1.next(), set);
        }

        Set<KeyStroke> set2 = new HashSet<KeyStroke>(shortCutListeners.keySet());
        Iterator<KeyStroke> it2 = shortCutTranslations.keySet().iterator();

        while(it2.hasNext()) {
          set2.remove(shortCutTranslations.get(it2.next()));
        }

        ownShortcuts = set2;
        ownShortcuts.addAll(shortCutTranslations.keySet());
        set.removeAll(set2);
        set.removeAll(shortCutTranslations.keySet());
        otherShortcuts = set;

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton help = new JButton(Resources.get("desktop.magellandesktop.prefs.shortcuts.help"));
        help.addActionListener(this);
        south.add(help);
        this.add(south, BorderLayout.SOUTH);
      }

      protected void addKeyStrokes(Component c, Set<KeyStroke> set) {
        if(c instanceof JComponent) {
          KeyStroke str[] = ((JComponent) c).getRegisteredKeyStrokes();

          if((str != null) && (str.length > 0)) {
            for(int i = 0; i < str.length; i++) {
              set.add(str[i]);
            }
          }
        }

        if(c instanceof Container) {
          Container con = (Container) c;

          if(con.getComponentCount() > 0) {
            for(int i = 0; i < con.getComponentCount(); i++) {
              addKeyStrokes(con.getComponent(i), set);
            }
          }
        }
      }

      /**
       * @see magellan.client.swing.preferences.PreferencesAdapter#initPreferences()
       * @deprecated TODO: implement it!
       */
      public void initPreferences() {
          // TODO: implement it
      }
            
      /**
       * 
       */
      public void applyPreferences() {
      }

      /**
       * 
       */
      public Component getComponent() {
        return this;
      }

      /**
       * 
       */
      public String getTitle() {
        return Resources.get("desktop.magellandesktop.prefs.shortcuts.title");
      }

      protected class ListenerComparator implements Comparator<Object> {
        /**
         * 
         */
        public int compare(Object o1, Object o2) {
          String s1 = null;
          String s2 = null;

          if(o1 instanceof ShortcutListener) {
            s1 = ((ShortcutListener) o1).getListenerDescription();
          }

          if(s1 == null) {
            s1 = o1.toString();
          }

          if(o2 instanceof ShortcutListener) {
            s2 = ((ShortcutListener) o2).getListenerDescription();
          }

          if(s2 == null) {
            s2 = o2.toString();
          }

          if((s1 != null) && (s2 != null)) {
            return collator.compare(s1, s2);
          } else if((s1 == null) && (s2 != null)) {
            return -1;
          } else if((s1 != null) && (s2 == null)) {
            return 1;
          }

          return 0;
        }
      }

      protected class KeyStrokeComparator implements Comparator<KeyStroke> {
        /**
         * 
         */
        public int compare(KeyStroke o1, KeyStroke o2) {
          KeyStroke k1 = (KeyStroke) o1;
          KeyStroke k2 = (KeyStroke) o2;

          if(k1.getModifiers() != k2.getModifiers()) {
            int i1 = k1.getModifiers();
            int i2 = 0;
            int j1 = k2.getModifiers();
            int j2 = 0;

            while(i1 != 0) {
              if((i1 % 2) == 1) {
                i2++;
              }

              i1 /= 2;
            }

            while(j1 != 0) {
              if((j1 % 2) == 1) {
                j2++;
              }

              j1 /= 2;
            }

            if(i2 != j2) {
              return i2 - j2;
            }

            return k1.getModifiers() - k2.getModifiers();
          }

          return k1.getKeyCode() - k2.getKeyCode();
        }
      }

      protected class ListTableModel extends DefaultTableModel {
        /**
         * Creates a new ListTableModel object.
         */
        public ListTableModel(Object data[][], Object columns[]) {
          super(data, columns);
        }

        /**
         * 
         */
        public boolean isCellEditable(int r, int c) {
          return false;
        }
      }

      protected String getKeyStroke(KeyStroke stroke) {
        String s = null;

        if(stroke.getModifiers() != 0) {
          s = KeyEvent.getKeyModifiersText(stroke.getModifiers()) + " + " +
            KeyEvent.getKeyText(stroke.getKeyCode());
        } else {
          s = KeyEvent.getKeyText(stroke.getKeyCode());
        }

        return s;
      }

      protected MouseListener getMousePressedMouseListener() {
        return new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
              if(mouseEvent.getClickCount() == 2) {
                Point p = mouseEvent.getPoint();

                if((table.columnAtPoint(p) == 0) && (table.rowAtPoint(p) >= 0)) {
                  int row = table.rowAtPoint(p);
                  Object value = table.getValueAt(row, 0);

                  if(value instanceof KeyStroke) {
                    editStroke((KeyStroke) value, row);
                  }
                }
              }
            }
          };
      }

      protected void editStroke(KeyStroke stroke, int row) {
        Component top = this.getTopLevelAncestor();
        TranslateStroke td = null;

        if(top instanceof Frame) {
          td = new TranslateStroke((Frame) top);
        } else if(top instanceof Dialog) {
          td = new TranslateStroke((Dialog) top);
        }

        td.setVisible(true);

        KeyStroke newStroke = td.getStroke();

        if((newStroke != null) && !newStroke.equals(stroke)) {
          if(ownShortcuts.contains(newStroke)) {
            JOptionPane.showMessageDialog(this, Resources.get("desktop.magellandesktop.prefs.shortcuts.error"));
          } else {
            boolean doIt = true;

            if(otherShortcuts.contains(newStroke)) {
              int res = JOptionPane.showConfirmDialog(this,
                                  Resources.get("desktop.magellandesktop.prefs.shortcuts.warning"),
                                  Resources.get("desktop.magellandesktop.prefs.shortcuts.warningtitle"),
                                  JOptionPane.YES_NO_OPTION);
              doIt = (res == JOptionPane.YES_OPTION);
            }

            if(doIt) {
              if(shortCutTranslations.containsKey(stroke)) {
                KeyStroke oldStroke = (KeyStroke) shortCutTranslations.get(stroke);
                removeTranslation(stroke);
                stroke = oldStroke;
              }

              if(shortCutListeners.containsKey(stroke) &&
                   (shortCutListeners.get(stroke) instanceof Action)) {
                ((Action) shortCutListeners.get(stroke)).putValue("accelerator",
                                          newStroke);
              }

              if(!newStroke.equals(stroke)) {
                registerTranslation(newStroke, stroke);
              }

              model.setValueAt(newStroke, row, 0);
            }
          }
        }
      }

      /**
       * 
       */
      public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
      }

      protected class InformDialog extends JDialog implements ActionListener {
        /**
         * Creates a new InformDialog object.
         */
        public InformDialog(Frame parent) {
          super(parent, true);
          init();
        }

        /**
         * Creates a new InformDialog object.
         */
        public InformDialog(Dialog parent) {
          super(parent, true);
          init();
        }

        protected void init() {
          JPanel con = new JPanel(new BorderLayout());

          StringBuffer buf = new StringBuffer();

          Object args[] = { new Integer(otherShortcuts.size()) };
          buf.append(MessageFormat.format(Resources.get("desktop.magellandesktop.prefs.shortcuts.others"), args));
          buf.append('\n');
          buf.append('\n');

          Iterator it = otherShortcuts.iterator();

          while(it.hasNext()) {
            buf.append(getKeyStroke((KeyStroke) it.next()));

            if(it.hasNext()) {
              buf.append(", ");
            }
          }

          JTextArea java = new JTextArea(buf.toString());
          java.setEditable(false);
          java.setLineWrap(true);
          java.setWrapStyleWord(true);
          con.add(new JScrollPane(java), BorderLayout.SOUTH);

          JPanel button = new JPanel(new FlowLayout(FlowLayout.CENTER));
          JButton ok = new JButton("prefs.shortcuts.dialog.ok");
          ok.addActionListener(this);
          button.add(ok);
          con.add(button, BorderLayout.SOUTH);
          this.setContentPane(con);

          this.pack();
          this.setLocationRelativeTo(this.getParent());
        }

        /**
         * 
         */
        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
          this.setVisible(false);
        }
      }

      protected class TranslateStroke extends JDialog implements ActionListener {
        protected KeyTextField text;
        protected JButton cancel;
        protected KeyStroke stroke = null;

        /**
         * Creates a new TranslateStroke object.
         */
        public TranslateStroke(Frame parent) {
          super(parent, true);
          init();
        }

        /**
         * Creates a new TranslateStroke object.
         */
        public TranslateStroke(Dialog parent) {
          super(parent, true);
          init();
        }

        protected void init() {
          JPanel con = new JPanel(new BorderLayout());
          con.add(new JLabel(Resources.get("desktop.magellandesktop.prefs.shortcuts.dialog.label")),
              BorderLayout.NORTH);
          text = new KeyTextField();
          con.add(text, BorderLayout.CENTER);

          JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
          JButton ok = new JButton(Resources.get("desktop.magellandesktop.prefs.shortcuts.dialog.ok"));
          buttons.add(ok);
          ok.addActionListener(this);
          cancel = new JButton(Resources.get("desktop.magellandesktop.prefs.shortcuts.dialog.cancel"));
          buttons.add(cancel);
          cancel.addActionListener(this);
          con.add(buttons, BorderLayout.SOUTH);
          this.setContentPane(con);
          this.pack();
          this.setSize(this.getWidth() + 5, this.getHeight() + 5);
          this.setLocationRelativeTo(this.getParent());
        }

        /**
         * 
         */
        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
          if(actionEvent.getSource() != cancel) {
            if(text.getKeyCode() != 0) {
              stroke = KeyStroke.getKeyStroke(text.getKeyCode(), text.getModifiers());
            }
          }

          this.setVisible(false);
        }

        /**
         * 
         */
        public KeyStroke getStroke() {
          return stroke;
        }

        private class KeyTextField extends JTextField implements KeyListener {
          protected int modifiers = 0;
          protected int key = 0;

          /**
           * Creates a new KeyTextField object.
           */
          public KeyTextField() {
            super(20);
            this.addKeyListener(this);
          }

          /**
           * 
           */
          public void init(int modifiers, int key) {
            this.key = key;
            this.modifiers = modifiers;

            String s = KeyEvent.getKeyModifiersText(modifiers);

            if((s != null) && (s.length() > 0)) {
              s += ('+' + KeyEvent.getKeyText(key));
            } else {
              s = KeyEvent.getKeyText(key);
            }

            setText(s);
          }

          /**
           * 
           */
          public void keyReleased(KeyEvent p1) {
            // maybe should delete any input if there's no "stable"(non-modifying) key
          }

          /**
           * 
           */
          public void keyPressed(KeyEvent p1) {
            modifiers = p1.getModifiers();
            key = p1.getKeyCode();

            // avoid double string
            if((key == KeyEvent.VK_SHIFT) || (key == KeyEvent.VK_CONTROL) ||
                 (key == KeyEvent.VK_ALT) || (key == KeyEvent.VK_ALT_GRAPH)) {
              int xored = 0;

              switch(key) {
              case KeyEvent.VK_SHIFT:
                xored = KeyEvent.SHIFT_MASK;

                break;

              case KeyEvent.VK_CONTROL:
                xored = KeyEvent.CTRL_MASK;

                break;

              case KeyEvent.VK_ALT:
                xored = KeyEvent.ALT_MASK;

                break;

              case KeyEvent.VK_ALT_GRAPH:
                xored = KeyEvent.ALT_GRAPH_MASK;

                break;
              }

              modifiers ^= xored;
            }

            String s = KeyEvent.getKeyModifiersText(modifiers);

            if((s != null) && (s.length() > 0)) {
              s += ('+' + KeyEvent.getKeyText(key));
            } else {
              s = KeyEvent.getKeyText(key);
            }

            setText(s);
            p1.consume();
          }

          /**
           * 
           */
          public void keyTyped(KeyEvent p1) {
          }

          /** 
           * To allow "tab" as a key.
           */
          public boolean isManagingFocus() {
            return true;
          }

          /**
           * 
           */
          public int getKeyCode() {
            return key;
          }

          /**
           * 
           */
          public int getModifiers() {
            return modifiers;
          }
        }
      }

      protected class StrokeRenderer extends DefaultTableCellRenderer {
        protected Font bold;
        protected Font norm;

        /**
         * Creates a new StrokeRenderer object.
         */
        public StrokeRenderer() {
          norm = this.getFont();
          bold = this.getFont().deriveFont(Font.BOLD);
        }

        /**
         * 
         */
        public Component getTableCellRendererComponent(JTable table, Object value,
                                 boolean isSelected,
                                 boolean hasFocus, int row, int column) {
          this.setFont(norm);
          super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                            column);

          if(value instanceof KeyStroke) {
            this.setText(getKeyStroke((KeyStroke) value));
          } else if(column == 0) {
            this.setFont(bold);
          }

          return this;
        }
      }
    }
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
    public void paintComponent(Graphics g) {
      if(bgMode == -1) {
        super.paintComponent(g);

        return;
      }

      if(bgMode == 0) {
        if(bgColor != null) {
          g.setColor(bgColor);
          g.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
      } else {
        if(bgImage != null) {
          if(bgMode == 1) { // resize
            g.drawImage(bgImage, 0, 0, this.getWidth(), this.getHeight(), this);
          } else { // repeat

            int w = bgImage.getWidth(this);
            int h = bgImage.getHeight(this);
            int wi = this.getWidth() / w;

            if((this.getWidth() % w) != 0) {
              wi++;
            }

            int hi = this.getHeight() / h;

            if((this.getHeight() % h) != 0) {
              hi++;
            }

            for(int i = 0; i < hi; i++) {
              for(int j = 0; j < wi; j++) {
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
  public Component add(Component c) {
    clearOpaque(c);

    return super.add(c);
  }

  /**
   * 
   */
  public void add(Component c, Object con) {
    clearOpaque(c);
    super.add(c, con);
  }

  protected void clearOpaque(Component c) {
    if(bgMode == -1) {
      return;
    }

    if(c instanceof JComponent) {
      ((JComponent) c).setOpaque(false);
    }

    if(c instanceof Container) {
      Component children[] = ((Container) c).getComponents();

      if((children != null) && (children.length > 0)) {
        for(int i = 0; i < children.length; i++) {
          clearOpaque(children[i]);
        }
      }
    }
  }

  /**
   * 
   */
  public void paintComponent(Graphics g) {
    if(bgMode == -1) {
      super.paintComponent(g);

      return;
    }

    if(bgMode == 0) {
      if(bgColor != null) {
        g.setColor(bgColor);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
      }
    } else {
      if(bgImage != null) {
        if(bgMode == 1) { // resize
          g.drawImage(bgImage, 0, 0, this.getWidth(), this.getHeight(), this);
        } else { // repeat

          int w = bgImage.getWidth(this);
          int h = bgImage.getHeight(this);
          int wi = this.getWidth() / w;

          if((this.getWidth() % w) != 0) {
            wi++;
          }

          int hi = this.getHeight() / h;

          if((this.getHeight() % h) != 0) {
            hi++;
          }

          for(int i = 0; i < hi; i++) {
            for(int j = 0; j < wi; j++) {
              g.drawImage(bgImage, j * w, i * h, this);
            }
          }
        }
      }
    }
  }



  /**
   * @see net.infonode.docking.DockingWindowListener#viewFocusChanged(net.infonode.docking.View, net.infonode.docking.View)
   */
  public void viewFocusChanged(View previouslyFocusedView, View focusedView) {
    // do nothing
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowAdded(net.infonode.docking.DockingWindow, net.infonode.docking.DockingWindow)
   */
  public void windowAdded(DockingWindow addedToWindow, DockingWindow addedWindow) {
    // inform desktopmenu
    if (addedWindow.getName() != null) {
      for (int index=0; index<desktopMenu.getItemCount(); index++) {
        JCheckBoxMenuItem menu = (JCheckBoxMenuItem)desktopMenu.getItem(index);
        if (menu.getActionCommand().equals("menu."+addedWindow.getName())) {
          menu.setSelected(true);
        }
      }
    }
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowClosed(net.infonode.docking.DockingWindow)
   */
  public void windowClosed(DockingWindow window) {
    // inform desktopmenu
    if (window.getName() != null) {
      for (int index=0; index<desktopMenu.getItemCount(); index++) {
        JCheckBoxMenuItem menu = (JCheckBoxMenuItem)desktopMenu.getItem(index);
        if (menu.getActionCommand().equals("menu."+window.getName())) {
          menu.setSelected(false);
        }
      }
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
   * @see net.infonode.docking.DockingWindowListener#windowRemoved(net.infonode.docking.DockingWindow, net.infonode.docking.DockingWindow)
   */
  public void windowRemoved(DockingWindow removedFromWindow, DockingWindow removedWindow) {
    // inform desktopmenu
    if (removedWindow.getName() != null) {
      for (int index=0; index<desktopMenu.getItemCount(); index++) {
        JCheckBoxMenuItem menu = (JCheckBoxMenuItem)desktopMenu.getItem(index);
        if (menu.getActionCommand().equals("menu."+removedWindow.getName())) {
          menu.setSelected(false);
        }
      }
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
}
