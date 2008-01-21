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

package magellan.client;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.plaf.FontUIResource;

import magellan.client.actions.MenuAction;
import magellan.client.actions.edit.FindAction;
import magellan.client.actions.edit.RedoAction;
import magellan.client.actions.edit.UndoAction;
import magellan.client.actions.extras.ArmyStatsAction;
import magellan.client.actions.extras.FactionStatsAction;
import magellan.client.actions.extras.HelpAction;
import magellan.client.actions.extras.InfoAction;
import magellan.client.actions.extras.OptionAction;
import magellan.client.actions.extras.RepaintAction;
import magellan.client.actions.extras.TileSetAction;
import magellan.client.actions.extras.TipOfTheDayAction;
import magellan.client.actions.extras.TradeOrganizerAction;
import magellan.client.actions.extras.VorlageAction;
import magellan.client.actions.file.AbortAction;
import magellan.client.actions.file.AddCRAction;
import magellan.client.actions.file.ExportCRAction;
import magellan.client.actions.file.FileSaveAction;
import magellan.client.actions.file.FileSaveAsAction;
import magellan.client.actions.file.OpenCRAction;
import magellan.client.actions.file.OpenOrdersAction;
import magellan.client.actions.file.QuitAction;
import magellan.client.actions.file.SaveOrdersAction;
import magellan.client.actions.map.AddSelectionAction;
import magellan.client.actions.map.ExpandSelectionAction;
import magellan.client.actions.map.FillSelectionAction;
import magellan.client.actions.map.InvertSelectionAction;
import magellan.client.actions.map.IslandAction;
import magellan.client.actions.map.MapSaveAction;
import magellan.client.actions.map.OpenSelectionAction;
import magellan.client.actions.map.SaveSelectionAction;
import magellan.client.actions.map.SelectAllAction;
import magellan.client.actions.map.SelectIslandsAction;
import magellan.client.actions.map.SelectNothingAction;
import magellan.client.actions.map.SetOriginAction;
import magellan.client.actions.orders.ChangeFactionConfirmationAction;
import magellan.client.actions.orders.ConfirmAction;
import magellan.client.actions.orders.FindPreviousUnconfirmedAction;
import magellan.client.actions.orders.UnconfirmAction;
import magellan.client.desktop.DesktopEnvironment;
import magellan.client.desktop.MagellanDesktop;
import magellan.client.desktop.ShortcutListener;
import magellan.client.event.EventDispatcher;
import magellan.client.event.OrderConfirmEvent;
import magellan.client.event.OrderConfirmListener;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.client.event.TempUnitEvent;
import magellan.client.event.TempUnitListener;
import magellan.client.event.UnitOrdersEvent;
import magellan.client.event.UnitOrdersListener;
import magellan.client.extern.MagellanPlugIn;
import magellan.client.extern.MagellanPlugInLoader;
import magellan.client.resource.ResourceSettingsFactory;
import magellan.client.swing.ArmyStatsPanel;
import magellan.client.swing.ECheckPanel;
import magellan.client.swing.InternationalizedDataPanel;
import magellan.client.swing.MagellanLookAndFeel;
import magellan.client.swing.MapperPanel;
import magellan.client.swing.MenuProvider;
import magellan.client.swing.MessagePanel;
import magellan.client.swing.ProgressBarUI;
import magellan.client.swing.StartWindow;
import magellan.client.swing.TipOfTheDay;
import magellan.client.swing.TradeOrganizer;
import magellan.client.swing.map.CellGeometry;
import magellan.client.swing.map.MapCellRenderer;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.client.swing.tasks.TaskTablePanel;
import magellan.client.swing.tree.IconAdapterFactory;
import magellan.client.swing.tree.NodeWrapperFactory;
import magellan.client.utils.BookmarkManager;
import magellan.client.utils.ErrorWindow;
import magellan.client.utils.FileHistory;
import magellan.client.utils.LanguageDialog;
import magellan.client.utils.NameGenerator;
import magellan.client.utils.RendererLoader;
import magellan.client.utils.SelectionHistory;
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.IntegerID;
import magellan.library.Message;
import magellan.library.MissingData;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.io.GameDataReader;
import magellan.library.io.cr.CRWriter;
import magellan.library.io.file.FileBackup;
import magellan.library.io.file.FileType;
import magellan.library.io.file.FileTypeFactory;
import magellan.library.io.file.FileType.ReadOnlyException;
import magellan.library.rules.EresseaDate;
import magellan.library.utils.JVMUtilities;
import magellan.library.utils.Locales;
import magellan.library.utils.Log;
import magellan.library.utils.MagellanFinder;
import magellan.library.utils.MagellanImages;
import magellan.library.utils.MemoryManagment;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Regions;
import magellan.library.utils.Resources;
import magellan.library.utils.SelfCleaningProperties;
import magellan.library.utils.TrustLevels;
import magellan.library.utils.UserInterface;
import magellan.library.utils.Utils;
import magellan.library.utils.VersionInfo;
import magellan.library.utils.logging.Logger;


/**
 * This class is the root of all evil. It represents also the main entry point
 * into the application and also the basic frame the application creates.
 * 
 * @author $Author: $
 * @version $Revision: 388 $
 */
public class Client extends JFrame implements ShortcutListener, PreferencesFactory {
  private static final Logger log = Logger.getInstance(Client.class);
  
  /** This is the instance of this class */
  public static Client INSTANCE = null;

  private List<JPanel> panels = null;

  private MapperPanel mapPanel = null;

  private EMapOverviewPanel overviewPanel = null;

  private EMapDetailsPanel detailsPanel = null;

  private MessagePanel messagePanel = null;

  private ECheckPanel echeckPanel = null;
  
  private TaskTablePanel taskPanel = null;
  
  private ArmyStatsPanel armyStatsPanel = null;
  
  private TradeOrganizer tradeOrganizer = null;
  
  /**
   * @deprecated, use info from GameData
   */
  private File dataFile = null;

  /** 
   * indicates that the user loaded a report at least once
   * in order to decide about showing a save dialog when
   * quitting
   */
  private boolean everLoadedReport = false; // 

  private FileHistory fileHistory;

  private JMenu factionOrdersMenu;

  private JMenu factionOrdersMenuNot;

  private JMenu invertAllOrdersConfirmation;

  private List<NodeWrapperFactory> nodeWrapperFactories;

  private List<Object> preferencesAdapterList;

  private MenuAction saveAction;

  private OptionAction optionAction;

  private MagellanDesktop desktop;

  private ReportObserver reportState;

  /** Manager for setting and activating bookmarks. */
  private BookmarkManager bookmarkManager;

  /** Central undo manager - specialized to deliver change events */
  private MagellanUndoManager undoMgr = null;

  /** Magellan Directories */
  private static File filesDirectory = null;

  /** Directory of "magellan.ini" etc. */
  private static File settingsDirectory = null;  

  /** show order status in title */
  protected boolean showStatus = false;

  protected JMenuItem progressItem;

  /** start window, disposed after first init */
  protected static StartWindow startWindow;
  
  protected Collection<MagellanPlugIn> plugIns = new ArrayList<MagellanPlugIn>();

  /**
   * Creates a new Client object taking its data from <tt>gd</tt>.
   * <p>
   * Preferences are read from and stored in a file called <tt>client.ini</tt>.
   * This file is usually located in the user's home directed, which is the
   * Windows directory in a Microsoft Windows environment.
   * </p>
   * 
   * @param gd
   * @param fileDir
   * @param settingsDir
   */
  public Client(GameData gd, File fileDir, File settingsDir) {
    filesDirectory = fileDir;
    settingsDirectory = settingsDir;
    
    // get new dispatcher
    EventDispatcher dispatcher = new EventDispatcher();

    startWindow.progress(1, Resources.get("clientstart.1"));
    Properties settings = loadSettings(settingsDirectory, "magellan.ini");
    if (settings == null) {
      log.info("Client.loadSettings: settings file " + "magellan.ini" + " does not exist, using default values.");
      settings = new SelfCleaningProperties();
      settings.setProperty("Client.lookAndFeel","Windows");
      settings.setProperty("AdvancedShapeRenderer.Sets",",Einkaufsgut");
      settings.setProperty("AdvancedShapeRenderer.Einkaufsgut.Cur","\u00A7if\u00A7<\u00A7price\u00A7\u00D6l\u00A7-1\u00A71\u00A7else\u00A7if\u00A7<\u00A7price\u00A7Weihrauch\u00A7-1\u00A72\u00A7else\u00A7if\u00A7<\u00A7price\u00A7Seide\u00A7-1\u00A73\u00A7else\u00A7if\u00A7<\u00A7price\u00A7Myrrhe\u00A7-1\u00A74\u00A7else\u00A7if\u00A7<\u00A7price\u00A7Juwel\u00A7-1\u00A75\u00A7else\u00A7if\u00A7<\u00A7price\u00A7Gew\u00FCrz\u00A7-1\u00A76\u00A7else\u00A7if\u00A7<\u00A7price\u00A7Balsam\u00A7-1\u00A77\u00A7end\u00A7end\u00A7end\u00A7end\u00A7end\u00A7end\u00A7");
      settings.setProperty("AdvancedShapeRenderer.Einkaufsgut.Max","10");
      settings.setProperty("AdvancedShapeRenderer.Einkaufsgut.Colors","0.0;223,131,39;0.12162162;220,142,24;0.14864865;153,153,153;0.23648648;153,153,153;0.26013514;204,255,255;0.3445946;204,255,255;0.3716216;0,204,0;0.42905405;0,204,0;0.46283785;255,51,0;0.5371622;255,51,0;0.5608108;255,255,0;0.6317568;255,255,0;0.6621622;51,51,255;1.0;0,51,255");
      settings.setProperty("AdvancedShapeRenderer.Einkaufsgut.Values","0.0;0.0;1.0;1.0");
      settings.setProperty("AdvancedShapeRenderer.Einkaufsgut.Min","0");
      // Message Panel Default colors.
      settings.setProperty("messagetype.section.events.color","-"); // Format: #RRGGBB
      settings.setProperty("messagetype.section.movement.color","-");// Format: #RRGGBB
      settings.setProperty("messagetype.section.economy.color","-");// Format: #RRGGBB
      settings.setProperty("messagetype.section.magic.color","-");// Format: #RRGGBB
      settings.setProperty("messagetype.section.study.color","-");// Format: #RRGGBB
      settings.setProperty("messagetype.section.production.color","-");// Format: #RRGGBB
      settings.setProperty("messagetype.section.errors.color","-");// Format: #RRGGBB
      settings.setProperty("messagetype.section.battle.color","-");// Format: #RRGGBB
      
      // try to set path to ECheck
      this.initECheckPath(settings);
      
      initLocales(settings, true);
    } else {
      initLocales(settings, false);
    }

    showStatus = PropertiesHelper.getboolean(settings, "Client.ShowOrderStatus", false);

    Properties completionSettings = loadSettings(settingsDirectory, "magellan_completions.ini");
    if (completionSettings == null) completionSettings = new SelfCleaningProperties();

    // initialize the context, this has to be very early.
    context = new MagellanContext(this);
    context.setEventDispatcher(dispatcher);
    context.setProperties(settings);
    context.setCompletionProperties(completionSettings);
    context.init();

    context.setGameData(gd);
    // init icon, fonts, repaint shortcut, L&F, window things
    initUI();

    // create management and observer objects
    dispatcher.addSelectionListener(SelectionHistory.getSelectionEventHook());
    dispatcher.addTempUnitListener(SelectionHistory.getTempUnitEventHook());
    bookmarkManager = new BookmarkManager(dispatcher, settings);
    undoMgr = new MagellanUndoManager();
    reportState = new ReportObserver(dispatcher);

    // load plugins
    initPlugIns();
    
    // init components
    startWindow.progress(2, Resources.get("clientstart.2"));
    panels = new LinkedList<JPanel>();
    nodeWrapperFactories = new LinkedList<NodeWrapperFactory>();

    List<Container> topLevelComponents = new LinkedList<Container>();
    Map<String,Component> components = initComponents(topLevelComponents);

    // init desktop
    startWindow.progress(3, Resources.get("clientstart.3"));
    Rectangle bounds = PropertiesHelper.loadRect(settings, null, "Client");
    if (bounds != null)
      setBounds(bounds);

    desktop = MagellanDesktop.getInstance();
    desktop.init(this, context, settings, components, settingsDirectory);

    setContentPane(desktop);
    
    // load plugins
    // initPlugIns();

    // do it here because we need the desktop menu
    setJMenuBar(createMenuBar(topLevelComponents));

    // enable EventDisplayer
    // new
    // com.eressea.util.logging.EventDisplayDialog(this,false,dispatcher).setVisible(true);
  }

  // ////////////////////////
  // BASIC initialization //
  // ////////////////////////

  private MagellanContext context;

  /**
   * Load the file fileName in the given directory into the settings object.
   * 
   * @param directory
   *          DOCUMENT-ME
   * @param fileName
   *          DOCUMENT-ME
   */
  protected Properties loadSettings(File directory, String fileName) {
    Properties settings = new SelfCleaningProperties();
    // settings = new OrderedOutputProperties();
    // settings = new AgingProperties();

    settings.clear();

    File settingsFile = new File(directory, fileName);

    // load settings from file
    if (settingsFile.exists()) {
      try {
        settings.load(new BufferedInputStream(new FileInputStream(settingsFile)));
        log.info("Client.loadSettings: successfully loaded " + settingsFile);
      } catch (IOException e) {
        log.error("Client.loadSettings: Error while loading " + settingsFile, e);
        return null;
      }
    } else {
      return null;
    }
    return settings;
  }

  protected void initLocales(Properties settings, boolean ask) {
    if (ask) {
      LanguageDialog ld = new LanguageDialog(startWindow, settings);

      if (ld.languagesFound()) {
        startWindow.toBack();
        Point p = startWindow.getLocation();
        ld.setLocation((int) p.getX()+(startWindow.getWidth()-ld.getWidth())/2, (int) p.getY()-ld.getHeight()/2);
        Locale locale = ld.show();
        startWindow.toFront();
        if (locale == null) {
          // without this decision we cannot start the application
          log.error("can't work without locale");
          quit(false);
        }

        if (!locale.equals(Locale.getDefault())) {
          settings.setProperty("locales.gui", locale.getLanguage());
          settings.setProperty("locales.orders", locale.getLanguage());
        }
      }
    }

    if (settings.getProperty("locales.gui") != null)
      Locales.setGUILocale(new Locale(settings.getProperty("locales.gui")));
    else
      Locales.setGUILocale(Locale.getDefault());
    if (settings.getProperty("locales.orders") != null)
      Locales.setOrderLocale(new Locale(settings.getProperty("locales.orders")));
    else
      Locales.setOrderLocale(Locale.GERMAN);
    log.info("GUI locale: " + Locales.getGUILocale() + settings.getProperty("locales.gui") + ", orders locale: " + Locales.getOrderLocale() + settings.getProperty("locales.orders"));
  }

  // TODO (stm) this is used by exactly once in the whole project. Why do we
  // need context anyway?
  /**
   * Returns the MagellanContext
   */
  public MagellanContext getMagellanContext() {
    return context;
  }

  /**
   * Returns the application icon
   * 
   * @return the application icon
   */
  public static Image getApplicationIcon() {
    // set the application icon
    ImageIcon icon = MagellanImages.ABOUNT_APPLICATION_ICON;

    return (icon == null) ? null : icon.getImage();
  }

  /**
   * Inits base UI things: # frame icon # window event things # fonts # repaint
   * shortcut # L&F
   */
  protected void initUI() {
    Image iconImage = getApplicationIcon();

    // set the application icon
    if (iconImage != null) {
      setIconImage(iconImage);
    }

    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        quit(true);
      }
    });

    /* setup font size */
    try {
      float fScale = PropertiesHelper.getfloat(getProperties(), "Client.FontScale", 1.0f);

      if (fScale != 1.0f) {
        // TODO(pavkovic): the following code bloats the fonts in an
        // undesired way, perhaps
        // we remove this configuration option?
        UIDefaults table = UIManager.getDefaults();
        Enumeration eKeys = table.keys();

        while (eKeys.hasMoreElements()) {
          Object obj = eKeys.nextElement();
          Font font = UIManager.getFont(obj);

          if (font != null) {
            font = new FontUIResource(font.deriveFont(font.getSize2D() * fScale));
            UIManager.put(obj, font);
          }
        }
      }
    } catch (Exception e) {
      log.error(e);
    }

    // initialize client shortcut - F5 to repaint
    DesktopEnvironment.registerShortcutListener(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), this);

    // init L&F
    initLookAndFeels();
  }

  // ////////////////////////////
  // COMPONENT initialization //
  // ////////////////////////////

  /**
   * Initializes the Magellan components. The returned hashtable holds all
   * components with well-known desktop keywords.
   * 
   * @param topLevel
   *          DOCUMENT-ME
   * 
   */
  protected Map<String,Component> initComponents(List<Container> topLevel) {
    Map<String,Component> components = new Hashtable<String, Component>();

    // configure and add map panel
    // get cell geometry
    CellGeometry geo = new CellGeometry("cellgeometry.txt");

    // load custom renderers
    // ForcedFileClassLoader.directory = filesDirectory;
    RendererLoader rl = new RendererLoader(filesDirectory, ".", geo, getProperties());
    Collection<MapCellRenderer> cR = rl.loadRenderers();
    
    // init mapper
    mapPanel = new MapperPanel(getMagellanContext(), cR, geo);
    mapPanel.setMinimumSize(new Dimension(100, 10));
    mapPanel.setScaleFactor(PropertiesHelper.getfloat(getProperties(), "Map.scaleFactor", 1.0f));
    panels.add(mapPanel);
    components.put("MAP", mapPanel);
    components.put("MINIMAP", mapPanel.getMinimap());
    topLevel.add(mapPanel);

    // configure and add message panel
    messagePanel = new MessagePanel(getDispatcher(), getData(), getProperties());
    messagePanel.setMinimumSize(new Dimension(100, 10));
    panels.add(messagePanel);
    nodeWrapperFactories.add(messagePanel.getNodeWrapperFactory());
    components.put("MESSAGES", messagePanel);
    topLevel.add(messagePanel);

    // configure and add details panel
    detailsPanel = new EMapDetailsPanel(getDispatcher(), getData(), getProperties(), undoMgr);
    detailsPanel.setMinimumSize(new Dimension(100, 10));
    panels.add(detailsPanel);
    nodeWrapperFactories.add(detailsPanel.getNodeWrapperFactory());

    Container c = (Container) detailsPanel.getNameAndDescriptionPanel();
    components.put("NAME&DESCRIPTION", c);
    components.put("NAME", c.getComponent(0));
    components.put("DESCRIPTION", c.getComponent(1));
    components.put("DETAILS", detailsPanel.getDetailsPanel());
    components.put("ORDERS", detailsPanel.getOrderEditor());
    
    // this keyword is deprecated
    components.put("COMMANDS", detailsPanel.getOrderEditor());
    topLevel.add(detailsPanel);

    // configure and add overview panel
    overviewPanel = new EMapOverviewPanel(getDispatcher(), getProperties());
    overviewPanel.setMinimumSize(new Dimension(100, 10));
    panels.add(overviewPanel);
    components.put(EMapOverviewPanel.IDENTIFIER, overviewPanel.getOverviewComponent());
    components.put("HISTORY", overviewPanel.getHistoryComponent());
    components.put("OVERVIEW&HISTORY", overviewPanel);
    nodeWrapperFactories.add(overviewPanel.getNodeWrapperFactory());
    topLevel.add(overviewPanel);
    
    echeckPanel = new ECheckPanel(getDispatcher(), getData(), getProperties(), getSelectedRegions().values());
    components.put(ECheckPanel.IDENTIFIER, echeckPanel);
    
    taskPanel = new TaskTablePanel(getDispatcher(), getData(), getProperties());
    components.put(TaskTablePanel.IDENTIFIER, taskPanel);

//    armyStatsPanel = new ArmyStatsPanel(getDispatcher(), getData(), getProperties(), true);
//    components.put(ArmyStatsPanel.IDENTIFIER, armyStatsPanel);

//    tradeOrganizer = new TradeOrganizer(this, getDispatcher(), getData(), getProperties());
//    components.put(TradeOrganizer.IDENTIFIER, tradeOrganizer);

    return components;
  }

  // //////////////////////////
  // MENUBAR initialization //
  // //////////////////////////

  /**
   * Creates a menu bar to be added to this frame.
   * 
   * @param components
   *          DOCUMENT-ME
   * 
   */
  private JMenuBar createMenuBar(Collection components) {
    JMenuBar menuBar = new JMenuBar();

    // create static menus
    menuBar.add(createFileMenu());
    menuBar.add(createEditMenu());
    menuBar.add(createOrdersMenu());
    menuBar.add(createBookmarkMenu());
    menuBar.add(createMapMenu());

    // create dynamix menus
    Map<String,JMenu> topLevel = new HashMap<String, JMenu>();
    List<JMenu> direction = new LinkedList<JMenu>();
    Iterator it = components.iterator();
    log.info("Checking for menu-providers...");

    while (it.hasNext()) {
      Object o = it.next();

      if (o instanceof MenuProvider) {
        MenuProvider mp = (MenuProvider) o;

        if (mp.getSuperMenu() != null) {
          if (!topLevel.containsKey(mp.getSuperMenu())) {
            topLevel.put(mp.getSuperMenu(), new JMenu(mp.getSuperMenuTitle()));
            direction.add(topLevel.get(mp.getSuperMenu()));
          }

          JMenu top = topLevel.get(mp.getSuperMenu());
          top.add(mp.getMenu());
        } else {
          direction.add(mp.getMenu());
        }
      }
    }
    
    // desktop and extras last
    menuBar.add(desktop.getDesktopMenu());

    // add external modules if some can be found
    JMenu plugInMenu = null;
    log.info("Checking for menu-providers...(MagellanPlugIns)");
    for (MagellanPlugIn plugIn : plugIns) {
      List<JMenuItem> plugInMenuItems = plugIn.getMenuItems();
      if (plugInMenuItems != null && plugInMenuItems.size()>0) {
        if (plugInMenu == null) {
          plugInMenu = new JMenu(Resources.get("client.menu.plugins.caption"));
          menuBar.add(plugInMenu);
        } else {
          plugInMenu.addSeparator();
        }
        for (JMenuItem menuItem : plugInMenuItems) {
          plugInMenu.add(menuItem);
        }
      }
    }

    menuBar.add(createExtrasMenu());
    return menuBar;
  }

  protected JMenu createFileMenu() {
    JMenu file = new JMenu(Resources.get("client.menu.file.caption"));
    file.setMnemonic(Resources.get("client.menu.file.mnemonic").charAt(0));
    addMenuItem(file, new OpenCRAction(this));
    addMenuItem(file, new AddCRAction(this));
    addMenuItem(file, new OpenOrdersAction(this));
    file.addSeparator();
    saveAction = new FileSaveAction(this);
    addMenuItem(file, saveAction);
    addMenuItem(file, new FileSaveAsAction(this));
    addMenuItem(file, new SaveOrdersAction(this));
    file.addSeparator();
    addMenuItem(file, new ExportCRAction(this));
    file.addSeparator();

    // now create the file history since we have all data
    fileHistory = new FileHistory(this, getProperties(), file, file.getItemCount());
    fileHistory.buildFileHistoryMenu();
    file.addSeparator();
    addMenuItem(file, new AbortAction(this));
    addMenuItem(file, new QuitAction(this));

    return file;
  }

  protected JMenu createEditMenu() {
    JMenu edit = new JMenu(Resources.get("client.menu.edit.caption"));
    edit.setMnemonic(Resources.get("client.menu.edit.mnemonic").charAt(0));
    addMenuItem(edit, new UndoAction(this, undoMgr));
    addMenuItem(edit, new RedoAction(this, undoMgr));
    edit.addSeparator();
    addMenuItem(edit, new FindAction(this));

    return edit;
  }

  protected JMenu createOrdersMenu() {
    JMenu ordersMenu = new JMenu(Resources.get("client.menu.orders.caption"));
    ordersMenu.setMnemonic(Resources.get("client.menu.orders.mnemonic").charAt(0));
    addMenuItem(ordersMenu, new UnconfirmAction(this, overviewPanel));
    addMenuItem(ordersMenu, new FindPreviousUnconfirmedAction(this, overviewPanel));

    addMenuItem(ordersMenu, new ConfirmAction(this, overviewPanel));

    // add factionordersmenu to ordersmenu
    factionOrdersMenu = new JMenu(Resources.get("client.menu.orders.all.caption"));
    factionOrdersMenu.setMnemonic(Resources.get("client.menu.orders.all.mnemonic").charAt(0));
    ordersMenu.add(factionOrdersMenu);

    // add factionordersmenunot to ordersmenu
    factionOrdersMenuNot = new JMenu(Resources.get("client.menu.orders.allnot.caption"));
    factionOrdersMenuNot.setMnemonic(Resources.get("client.menu.orders.allnot.mnemonic").charAt(0));
    ordersMenu.add(factionOrdersMenuNot);

    // add factionordersmenu to ordersmenu
    invertAllOrdersConfirmation = new JMenu(Resources.get("client.menu.orders.invert.caption"));
    invertAllOrdersConfirmation.setMnemonic(Resources.get("client.menu.orders.invert.mnemonic").charAt(0));
    ordersMenu.add(invertAllOrdersConfirmation);

    updateConfirmMenu();

    return ordersMenu;
  }

  private void refillChangeFactionConfirmation(JMenu aMenu, int aConfirmationType) {
    if (aMenu.getItemCount() == 0) {
      // fill basic faction "all units"
      addMenuItem(aMenu, new ChangeFactionConfirmationAction(this, null, aConfirmationType, false, false));
      addMenuItem(aMenu, new ChangeFactionConfirmationAction(this, null, aConfirmationType, true, false));
      addMenuItem(aMenu, new ChangeFactionConfirmationAction(this, null, aConfirmationType, false, true));
      addMenuItem(aMenu, new ChangeFactionConfirmationAction(this, null, aConfirmationType, true, true));
    } else {
      JMenuItem all = aMenu.getItem(0);
      JMenuItem allSel = aMenu.getItem(1);
      JMenuItem spy = aMenu.getItem(2);
      JMenuItem spySel = aMenu.getItem(3);
      aMenu.removeAll();
      aMenu.add(all);
      aMenu.add(allSel);
      aMenu.add(spy);
      aMenu.add(spySel);
    }

    if (getData() != null) {
      // add all privileged factions
      for (Iterator iter = getData().factions().values().iterator(); iter.hasNext();) {
        Faction f = (Faction) iter.next();

        if ((f.isPrivileged()) && !f.units().isEmpty()) {
          aMenu.add(new ChangeFactionConfirmationAction(this, f, aConfirmationType, false, false));
          aMenu.add(new ChangeFactionConfirmationAction(this, f, aConfirmationType, true, false));
        }
      }
    }
  }

  protected JMenu createMapMenu() {
    JMenu map = new JMenu(Resources.get("client.menu.map.caption"));
    map.setMnemonic(Resources.get("client.menu.map.mnemonic").charAt(0));
    addMenuItem(map, new SetOriginAction(this));
    addMenuItem(map, new IslandAction(this));
    addMenuItem(map, new MapSaveAction(this, mapPanel));
    map.addSeparator();
    addMenuItem(map, new SelectAllAction(this));
    addMenuItem(map, new SelectNothingAction(this));
    addMenuItem(map, new InvertSelectionAction(this));
    addMenuItem(map, new SelectIslandsAction(this));
    addMenuItem(map, new FillSelectionAction(this));
    addMenuItem(map, new ExpandSelectionAction(this));
    map.addSeparator();
    addMenuItem(map, new OpenSelectionAction(this));
    addMenuItem(map, new AddSelectionAction(this));
    addMenuItem(map, new SaveSelectionAction(this));

    return map;
  }

  protected JMenu createBookmarkMenu() {
    JMenu bookmarks = new JMenu(Resources.get("client.menu.bookmarks.caption"));
    bookmarks.setMnemonic(Resources.get("client.menu.bookmarks.mnemonic").charAt(0));

    JMenuItem toggle = new JMenuItem(Resources.get("client.menu.bookmarks.toggle.caption"));
    toggle.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, KeyEvent.CTRL_MASK));
    toggle.addActionListener(new ToggleBookmarkAction());
    bookmarks.add(toggle);

    JMenuItem forward = new JMenuItem(Resources.get("client.menu.bookmarks.forward.caption"));
    forward.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
    forward.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        bookmarkManager.jumpForward();
      }
    });
    bookmarks.add(forward);

    JMenuItem backward = new JMenuItem(Resources.get("client.menu.bookmarks.backward.caption"));
    backward.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, KeyEvent.SHIFT_MASK));
    backward.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        bookmarkManager.jumpBackward();
      }
    });
    bookmarks.add(backward);

    JMenuItem show = new JMenuItem(Resources.get("client.menu.bookmarks.show.caption"));
    show.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, KeyEvent.ALT_MASK));
    show.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        bookmarkManager.showDialog(Client.this);
      }
    });
    bookmarks.add(show);

    JMenuItem clear = new JMenuItem(Resources.get("client.menu.bookmarks.clear.caption"));
    clear.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        bookmarkManager.clearBookmarks();
      }
    });
    bookmarks.add(clear);

    return bookmarks;
  }

  protected JMenu createExtrasMenu() {
    JMenu extras = new JMenu(Resources.get("client.menu.extras.caption"));
    extras.setMnemonic(Resources.get("client.menu.extras.mnemonic").charAt(0));
    addMenuItem(extras, new FactionStatsAction(this));
    addMenuItem(extras, new ArmyStatsAction(this));
    addMenuItem(extras, new TradeOrganizerAction(this));

//    addMenuItem(extras, new TaskTableAction(this));
//    addMenuItem(extras, new ECheckAction(this));
    addMenuItem(extras, new VorlageAction(this));
    extras.addSeparator();
    addMenuItem(extras, new RepaintAction(this));
    addMenuItem(extras, new TileSetAction(this, mapPanel));
    extras.addSeparator();
    preferencesAdapterList = new ArrayList<Object>(8);
    preferencesAdapterList.add(this);
    preferencesAdapterList.add(desktop);
    preferencesAdapterList.add(overviewPanel);
    preferencesAdapterList.add(detailsPanel);
    preferencesAdapterList.add(mapPanel);
    preferencesAdapterList.add(messagePanel);
    preferencesAdapterList.add(new IconAdapterFactory(nodeWrapperFactories));
    preferencesAdapterList.add(new ResourceSettingsFactory(getProperties()));
    optionAction = new OptionAction(this, preferencesAdapterList);
    addMenuItem(extras, optionAction);

    // TODO(pavkovic): currently EresseaOptionPanel is broken, I deactivated
    // it.
    extras.addSeparator();
    addMenuItem(extras, new HelpAction(this));
    addMenuItem(extras, new TipOfTheDayAction(this));
    extras.addSeparator();
    addMenuItem(extras, new InfoAction(this));

    return extras;
  }
  
  public static FontMetrics getDefaultFontMetrics(Font font) {
    return Toolkit.getDefaultToolkit().getFontMetrics(font);
  }


  private class ToggleBookmarkAction implements ActionListener, SelectionListener {
    private Object activeObject;

    /**
     * Creates a new ToggleBookmarkAction object.
     */
    public ToggleBookmarkAction() {
      getDispatcher().addSelectionListener(this);
    }

    /**
     * DOCUMENT-ME
     * 
     * @param se
     *          DOCUMENT-ME
     */
    public void selectionChanged(SelectionEvent se) {
      activeObject = se.getActiveObject();
    }

    /**
     * DOCUMENT-ME
     * 
     * @param e
     *          DOCUMENT-ME
     */
    public void actionPerformed(ActionEvent e) {
      bookmarkManager.toggleBookmark(activeObject);
    }
  }

  /**
   * Adds a new menu item to the specifie menu associating it with the specified
   * action, setting its mnemonic and registers its accelerator if it has one.
   * 
   * @return the menu item created.
   */
  private JMenuItem addMenuItem(JMenu parentMenu, MenuAction action) {
    JMenuItem item = parentMenu.add(action);
    item.setMnemonic(action.getMnemonic());

    if (action.getAccelerator() != null) {
      DesktopEnvironment.registerActionListener(action.getAccelerator(), action);
      item.setAccelerator(action.getAccelerator());
    }

    new MenuActionObserver(item, action);

    return item;
  }

  // ////////////////////
  // START & END Code //
  // ////////////////////
  /**
   * START & END Code
   */
  public static void main(String args[]) {
    try {
      String report = null; // the report to be loaded on startup
      File fileDir = null; // the directory to store ini files and
      File settFileDir = null;
      // stuff in

      /* set the stderr to stdout while there is no log attached */
      System.setErr(System.out);

      // Fiete 20061208
      // set finalizer prio to max
      magellan.library.utils.MemoryManagment.setFinalizerPriority(Thread.MAX_PRIORITY);

      /* determine default value for files directory */
      fileDir = MagellanFinder.findMagellanDirectory();


      /* process command line parameters */
      int i = 0;

      while (i < args.length) {
        if (args[i].toLowerCase().startsWith("-log")) {
          String level = null;

          if (args[i].toLowerCase().startsWith("-log=") && (args[i].length() > 5)) {
            level = args[i].charAt(5) + "";
          } else if (args[i].equals("-log") && (args.length > (i + 1))) {
            i++;
            level = args[i];
          }

          if (level != null) {
            level = level.toUpperCase();
            Logger.setLevel(level);
            log.info("Client.main: Set logging to " + level);

            if ("A".equals(level)) {
              log.awt("Start logging of awt events to awtdebug.txt.");
            }
          }
        } else if (args[i].equals("--help")) {
          Help.open(args);
          return;
        } else if (args[i].equals("-d") && (args.length > (i + 1))) {
          i++;

          try {
            File tmpFile = new File(args[i]).getCanonicalFile();

            if (tmpFile.exists() && tmpFile.isDirectory() && tmpFile.canWrite()) {
              fileDir = tmpFile;
            } else {
              log.info("Client.main(): the specified files directory does not " + "exist, is not a directory or is not writeable.");
            }
          } catch (Exception e) {
            log.error("Client.main(): the specified files directory is invalid.", e);
          }
        } else {
          if (args[i].toLowerCase().endsWith(".cr") || args[i].toLowerCase().endsWith(".bz2") || args[i].toLowerCase().endsWith(".zip")) {
            report = args[i];
          }
        }

        i++;
      }
      
      settFileDir = MagellanFinder.findSettingsDirectory(fileDir, settFileDir);
      Resources.getInstance().initialize(fileDir, "");
      MagellanLookAndFeel.setMagellanDirectory(fileDir);
      MagellanImages.setMagellanDirectory(fileDir);
      
      
      // initialize start window
      Icon startIcon = MagellanImages.ABOUT_MAGELLAN;

      startWindow = new StartWindow(startIcon, 5,fileDir);

      startWindow.setVisible(true);

      startWindow.progress(0, Resources.get("clientstart.0"));


      
      // tell the user where we expect ini files and errors.txt
      log.info("Client.main(): directory used for ini files: " + settFileDir.toString());

      // now redirect stderr through our log

      Log LOG = new Log(fileDir);
      System.setErr(LOG.getPrintStream());

      log.warn("Start writing error file with encoding " + LOG.encoding + ", log level " + Logger.getLevel(Logger.getLevel()));

      String version = VersionInfo.getVersion(fileDir);
      if (version == null) {
        log.warn("no magellan version available");
      } else {
        log.warn("This is Magellan Version " + version);
      }
      
      
      try {
        log.warn("OS: "+System.getProperty("os.name")+" "+System.getProperty("os.arch")+" "+System.getProperty("os.version"));
        log.warn("Java Version: "+System.getProperty("java.version")+" "+System.getProperty("java.vendor"));
        log.warn("Java Spec: "+System.getProperty("java.specification.version")+" "+System.getProperty("java.specification.vendor")+" "+System.getProperty("java.specification.name"));
        log.warn("VM Version: "+System.getProperty("java.vm.version")+" "+System.getProperty("java.vm.vendor")+" "+System.getProperty("java.vm.name"));
        log.warn("VM Specification: "+System.getProperty("java.vm.specification.version")+" "+System.getProperty("java.vm.specification.vendor")+" "+System.getProperty("java.vm.specification.name"));
        log.warn("Java Class Version: "+System.getProperty("java.class.version"));
      } catch (SecurityException e) {
        log.warn("Unable to retrieve system properties: "+e);
      }
      
      final File tFileDir = fileDir;
      final File tsettFileDir = settFileDir;
      final String tReport = report;
      

      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          // can't call loadRules from here, so we initially work with an
          // empty ruleset.
          // This is not very nice, though...
          GameData data = new MissingData();
          
          // new CompleteData(new com.eressea.rules.Eressea(), "void");
          Client c = new Client(data, tFileDir, tsettFileDir);
          // setup a singleton instance of this client
          INSTANCE = c;
    
          if (tReport != null) {
            startWindow.progress(4, Resources.get("clientstart.4"));
    
            File crFile = new File(tReport);
    
            c.dataFile = crFile;
    
            // load new data
            //c.setData(c.loadCR(crFile));
            c.loadCRThread(crFile);
          }
    
          c.setReportChanged(false);
          
          startWindow.progress(5, Resources.get("clientstart.5"));
          c.setAllVisible(true);
          startWindow.setVisible(false);
          startWindow.dispose();
          startWindow = null;
          
          String newestVersion = VersionInfo.getNewestVersion(c.getProperties());
          if (!Utils.isEmpty(newestVersion)) {
            String currentVersion = VersionInfo.getVersion(tFileDir);
            log.info("Newest Version on server: "+newestVersion);
            log.info("Current Version: "+currentVersion);
            if (VersionInfo.isNewer(currentVersion, newestVersion)) {
              JOptionPane.showMessageDialog(c, Resources.get("client.new_version",new Object[]{newestVersion}));
            }
          }
    
    
          // show tip of the day window
          if (c.getProperties().getProperty("TipOfTheDay.showTips", "true").equals("true") || c.getProperties().getProperty("TipOfTheDay.firstTime", "true").equals("true")) {
            TipOfTheDay totd = new TipOfTheDay(c, c.getProperties());
    
            if (totd.doShow()) {
              // totd.setVisible(true);
              totd.showTipDialog();
              totd.showNextTip();
            }
          }
        }
      });
    } catch (Throwable exc) { // any fatal error
      log.error(exc); // print it so it can be written to errors.txt

      // try to create a nice output
      String out = "A fatal error occured: " + exc.toString();

      log.error(out, exc);
      JOptionPane.showMessageDialog(new JFrame(), out);
      System.exit(1);
    }
  }

  /**
   * Asks the user whether the current report should be saved and does so if
   * necessary. Called before open and close actions.
   * 
   * @return whether the action shall be continued (user did not press cancel
   *         button)
   */
  public boolean askToSave(boolean wait) {
    if (reportState.isStateChanged()) {
      String msg = null;

      if (dataFile != null) {
        Object msgArgs[] = { dataFile.getAbsolutePath() };
        msg = (new MessageFormat(Resources.get("client.msg.quit.confirmsavefile.text"))).format(msgArgs);
      } else {
        msg = Resources.get("client.msg.quit.confirmsavenofile.text");
      }

      switch (JOptionPane.showConfirmDialog(this, msg, Resources.get("client.msg.quit.confirmsave.title"), JOptionPane.YES_NO_CANCEL_OPTION)) {
      case JOptionPane.YES_OPTION:

        try {
          CRWriter crw = saveReport(getData().filetype);
          if (wait) {
            while (crw.savingInProgress()) {
              try {
                this.wait(1000);
              } catch (Exception e) {}
            }
          }
        } catch (Exception e) {
          log.error(e);

          return false;
        }

        break;

      case JOptionPane.CANCEL_OPTION:
        return false;
      }
    }

    return true;
  }
  
  public CRWriter saveReport(FileType filetype) {
    CRWriter crw = null;
    try {
      
      // log.info("debugging: doSaveAction (FileType) called for FileType: " + filetype.toString());
      // write cr to file
      log.info("Using encoding: "+getData().getEncoding());
      ProgressBarUI ui = new ProgressBarUI(this);
      crw = new CRWriter(ui,filetype,getData().getEncoding());
      crw.write(getData());
      crw.close();

      // everything worked fine, so reset reportchanged state and also store new FileType settings
      setReportChanged(false);
      getData().filetype = filetype;
      getData().resetToUnchanged();
      getProperties().setProperty("Client.lastCRSaved", filetype.getName());
    } catch(ReadOnlyException exc) {
      log.error(exc);
      JOptionPane.showMessageDialog(this, Resources.getFormatted("actions.filesaveasaction.msg.filesave.readonly", filetype.getName()),
          Resources.get("actions.filesaveasaction.msg.filesave.error.title"),
                      JOptionPane.ERROR_MESSAGE);
    } catch(IOException exc) {
      log.error(exc);
      JOptionPane.showMessageDialog(this, exc.toString(),
          Resources.get("actions.filesaveasaction.msg.filesave.error.title"),
                      JOptionPane.ERROR_MESSAGE);
    }
    return crw;
  }

  /**
   * This method should be called before the application is terminated in order
   * to store GUI settings etc.
   * 
   * @param storeSettings
   *          store the settings to magellan.ini if <code>storeSettings</code>
   *          is <code>true</code>.
   */
  public void quit(boolean storeSettings) {
    if (reportState!=null && reportState.isStateChanged()) {
      if (!askToSave(true)) {
        return; // cancel or exception
      }
    }
    
    for (MagellanPlugIn plugIn : getPlugIns()) {
      plugIn.quit(storeSettings);
    }

    saveExtendedState();
    this.setVisible(false);

    if (panels != null) {
      for (Iterator iter = panels.iterator(); iter.hasNext();) {
        InternationalizedDataPanel p = (InternationalizedDataPanel) iter.next();
        p.quit();
      }
    }

    NameGenerator.quit();

    if (fileHistory != null) fileHistory.storeFileHistory();

    // store settings to file
    if (storeSettings) {
      // save the desktop
      desktop.save();

      try {
        // if necessary, use settings file in local directory
        File settingsFile = new File(settingsDirectory, "magellan.ini");
        
        if (settingsFile.exists() && settingsFile.canWrite()) {
          try {
            File backup = FileBackup.create(settingsFile);
            log.info("Created backupfile " + backup);
          } catch (IOException ie) {
            log.warn("Could not create backupfile for file " + settingsFile);
          }
        }

        if (settingsFile.exists() && !settingsFile.canWrite()){
          throw new IOException("cannot write "+settingsFile);
        }else{
          log.info("Storing Magellan configuration to " + settingsFile);

          getProperties().store(new FileOutputStream(settingsFile), "");
        }
      } catch (IOException ioe) {
        log.error(ioe);
      }
    }

    System.exit(0);
  }

  // //////////////////
  // GAME DATA Code //
  // //////////////////
  
  public GameData loadCR(UserInterface ui, File fileName) {
    GameData data = null;
    Client client = this;
    if (ui == null) ui = new ProgressBarUI(client);
    
    try {
      data = new GameDataReader(ui).readGameData(FileTypeFactory.singleton().createFileType(fileName, true, new ClientFileTypeChooser(client)));
      everLoadedReport = true;
    } catch (FileTypeFactory.NoValidEntryException e) {
      JOptionPane.showMessageDialog(client, Resources.get("client.msg.loadcr.missingcr.text.1") + fileName + Resources.get("client.msg.loadcr.missingcr.text.2"), Resources.get("client.msg.loadcr.error.title"), JOptionPane.ERROR_MESSAGE);
    } catch (Exception exc) {
      // here we also catch RuntimeExceptions on purpose!
      // } catch (IOException exc) {
      JOptionPane.showMessageDialog(client, Resources.get("client.msg.loadcr.error.text") + exc.toString(), Resources.get("client.msg.loadcr.error.title"), JOptionPane.ERROR_MESSAGE);
      log.error(exc);
    }

    if (data!=null && data.outOfMemory) {
      JOptionPane.showMessageDialog(client, Resources.get("client.msg.outofmemory.text"), Resources.get("client.msg.outofmemory.title"), JOptionPane.ERROR_MESSAGE);
      log.error(Resources.get("client.msg.outofmemory.text"));
    }
    if (!MemoryManagment.isFreeMemory(data.estimateSize())){
      JOptionPane.showMessageDialog(client, Resources.get("client.msg.lowmem.text"), Resources.get("client.msg.lowmem.title"), JOptionPane.WARNING_MESSAGE);
    }

    return data;
  }

  /**
   * This method loads a CR into the client.
   * 
   * @param fileName
   *          The file name to be loaded.
   * @return a new <tt>GameData</tt> object filled with the data from the CR.
   */
  public void loadCRThread(final File fileName) {
    
    final UserInterface ui = new ProgressBarUI(this);
    
    new Thread(new Runnable() {
      public void run() {
        Client client = Client.INSTANCE;
        GameData data = null;
    
        data = loadCR(ui,fileName);
        
        if(data != null) {
          client.setData(data);
          client.setReportChanged(false);
          
          if (client.getSelectedObjects()!=null){
            client.getDispatcher().fire(new SelectionEvent(this,client.getSelectedObjects(),null));
          }
          
          Region activeRegion = data.getActiveRegion();
          if (activeRegion != null) {
            client.getDispatcher().fire(new SelectionEvent(client, new ArrayList(), activeRegion, SelectionEvent.ST_REGIONS));
          }
        }
      }
    }).start();
  }
  
  /**
   * Sets the origin of this client's data to newOrigin.
   * 
   * @param newOrigin
   *          The region in the GameData that is going to be the new origin
   */
  public void setOrigin(CoordinateID newOrigin) {
    GameData newData = null;
    try {
      newData = (GameData) getData().clone(newOrigin);
      if (newData!=null && newData.outOfMemory) {
        JOptionPane.showMessageDialog(this, Resources.get("client.msg.outofmemory.text"), Resources.get("client.msg.outofmemory.title"), JOptionPane.ERROR_MESSAGE);
        log.error(Resources.get("client.msg.outofmemory.text"));
      }
      if (!MemoryManagment.isFreeMemory(newData.estimateSize())){
        JOptionPane.showMessageDialog(this, Resources.get("client.msg.lowmem.text"), Resources.get("client.msg.lowmem.title"), JOptionPane.WARNING_MESSAGE);
      }
    } catch (final CloneNotSupportedException e) {
      e.printStackTrace();
    }

    if (newData != null) {
      setData(newData);
      setReportChanged(false);
    }
  }

  /**
   * Callbacks of FileTypeFactory are handled by this object. Right now it
   * returns the first ZipEntry to mimic old cr loading behaviour for zip files.
   */
  private static class ClientFileTypeChooser extends FileTypeFactory.FileTypeChooser {
    Client client;

    /**
     * Creates a new ClientFileTypeChooser object.
     * 
     * @param client
     *          the parent Client object
     */
    public ClientFileTypeChooser(Client client) {
      this.client = client;
    }

    /**
     * open selection window to choose a zipentry
     * 
     * @see magellan.library.io.file.FileTypeFactory.FileTypeChooser#chooseZipEntry(java.util.zip.ZipEntry[])
     */
    public ZipEntry chooseZipEntry(ZipEntry entries[]) {
      String stringEntries[] = new String[entries.length];

      for (int i = 0; i < entries.length; i++) {
        stringEntries[i] = entries[i].toString();
      }

      Object selected = JOptionPane.showInputDialog(client.getRootPane(), Resources.get("client.msg.loadcr.multiplezipentries.text"), Resources.get("client.msg.loadcr.multiplezipentries.title"), JOptionPane.QUESTION_MESSAGE, null, stringEntries, stringEntries[0]);

      if (selected == null) {
        return null;
      }

      for (int i = 0; i < entries.length; i++) {
        if (selected.equals(entries[i].toString())) {
          return entries[i];
        }
      }

      return null;
    }
  }

  /**
   * Do some additional checks after loading a report.
   * 
   * @param aData
   *          the currently loaded game data
   */
  private void postProcessLoadedCR(GameData aData) {
    // show a warning if no password is set for any of the privileged
    // factions
    boolean privFacsWoPwd = true;

    if ((aData != null) && (aData.factions() != null)) {
      for (Iterator factions = aData.factions().values().iterator(); factions.hasNext();) {
        Faction f = (Faction) factions.next();

        if (f.getPassword() == null) {
          // take password from settings but only if it is not an
          // empty string
          String pwd = getProperties().getProperty("Faction.password." + ((EntityID) f.getID()).intValue(), null);

          if ((pwd != null) && !pwd.equals("")) {
            f.setPassword(pwd);
          }
        }

        // now check whether this faction has a password and eventually
        // set Trustlevel
        if ((f.getPassword() != null) && !f.isTrustLevelSetByUser()) {
          f.setTrustLevel(Faction.TL_PRIVILEGED);
        }

        if (f.getPassword() != null) {
          privFacsWoPwd = false;
        }

        // check messages whether the password was changed
        if (f.getMessages() != null) {
          for (Iterator<Message> iter = f.getMessages().iterator(); iter.hasNext();) {
            Message m = iter.next();

            // check message id (new and old)
            if ((m.getMessageType() != null) && ((((IntegerID) m.getMessageType().getID()).intValue() == 1784377885) || (((IntegerID) m.getMessageType().getID()).intValue() == 19735))) {
              // this message indicates that the password has been
              // changed
              if (m.getAttributes() != null) {
                String value = m.getAttributes().get("value");

                // if the password in the message is valid and
                // does not match
                // the password already set anyway set it for
                // the faction and in the settings
                if (value != null) {
                  String password = value;

                  if (!password.equals("") && !password.equals(f.getPassword())) {
                    // ask user for confirmation to take new
                    // password from message
                    Object msgArgs[] = { f.toString() };

                    if (JOptionPane.showConfirmDialog(getRootPane(), (new java.text.MessageFormat(Resources.get("client.msg.postprocessloadedcr.acceptnewpassword.text"))).format(msgArgs), Resources.get("client.msg.postprocessloadedcr.acceptnewpassword.title"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                      f.setPassword(password);

                      if (!f.isTrustLevelSetByUser()) { // password
                        // set
                        f.setTrustLevel(Faction.TL_PRIVILEGED);
                      }

                      privFacsWoPwd = false;

                      if (getProperties() != null) {
                        getProperties().setProperty("Faction.password." + ((EntityID) f.getID()).intValue(), f.getPassword());
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }

      // recalculate default-trustlevels after CR-Load
      TrustLevels.recalculateTrustLevels(aData);

      if (privFacsWoPwd) { // no password set for any faction
        JOptionPane.showMessageDialog(getRootPane(), Resources.get("client.msg.postprocessloadedcr.missingpassword.text"));
      }
      
      // recalculate the status of regions - coastal or not?
      Regions.calculateCoastBorders(aData);
    }
  }

  // ///////////////
  // UPDATE Code //
  // ///////////////
  private void updateTitleCaption() {
    String title = createTitle(getData(), showStatus, false);

    try {
      title = createTitle(getData(), showStatus, true);
    } catch (Exception e) {
      log.error("createTitle failed!", e);
    }

    setTitle(title);
  }

  private String createTitle(GameData data, boolean showStatusOverride, boolean longTitle) {
    // set frame title (date)
    String title = "Magellan";

    String version = VersionInfo.getVersion(filesDirectory);

    if (version != null) {
      title += (" " + version);
    }

    // pavkovic 2002.05.7: data may be null in this situation
    if (data == null) {
      return title;
    }

    if (data.filetype != null) {
      String file;

      try {
        file = data.filetype.getFile().toString();
      } catch (IOException e) {
        file = data.filetype.toString();
      }

      file = file.substring(file.lastIndexOf(File.separator) + 1);
      title += (" [" + file + "]");
    }

    if (data.getDate() != null) {
      title = title + " - " + data.getDate().toString(showStatusOverride ? EresseaDate.TYPE_SHORT : EresseaDate.TYPE_PHRASE_AND_SEASON) + " (" + data.getDate().getDate() + ")";
    }

    if (!longTitle) {
      return title;
    }

    if (showStatusOverride) {
      int units = 0;
      int done = 0;

      for (Iterator iter = data.units().values().iterator(); iter.hasNext();) {
        Unit u = (Unit) iter.next();

        if (u.getFaction().isPrivileged()) {
          units++;

          if (u.isOrdersConfirmed()) {
            done++;
          }
        }

        // also count temp units
        for (Iterator iter2 = u.tempUnits().iterator(); iter2.hasNext();) {
          Unit u2 = (Unit) iter2.next();

          if (u2.getFaction().isPrivileged()) {
            units++;

            if (u2.isOrdersConfirmed()) {
              done++;
            }
          }
        }
      }

      if (units > 0) {
        BigDecimal percent = (new BigDecimal((done * 100) / ((float) units))).setScale(2, BigDecimal.ROUND_DOWN);
        title += (" (" + units + " " + Resources.get("client.title.unit") + ", " + done + " " + Resources.get("client.title.done") + ", " + Resources.get("client.title.thatare") + " " + percent + " " + Resources.get("client.title.percent") + ")");
      }
    }

    return title;
  }

  /**
   * Updates the order confirmation menu after the game data changed.
   */
  private void updateConfirmMenu() {
    refillChangeFactionConfirmation(factionOrdersMenu, ChangeFactionConfirmationAction.SETCONFIRMATION);
    refillChangeFactionConfirmation(factionOrdersMenuNot, ChangeFactionConfirmationAction.REMOVECONFIRMATION);
    refillChangeFactionConfirmation(invertAllOrdersConfirmation, ChangeFactionConfirmationAction.INVERTCONFIRMATION);
  }

  /**
   * Updates the plugins after GameData Change
   *
   */
  private void updatePlugIns(){
    if (this.plugIns!=null && this.plugIns.size()>0){
      for (MagellanPlugIn plugIn : this.plugIns) {
        try {
          plugIn.init(getData());
        } catch (Throwable t) {
          ErrorWindow errorWindow = new ErrorWindow(this,t.getMessage(),"",t);
          errorWindow.setVisible(true);
        }
      }
    }
  }
  
  /**
   * Called after GameData changes. Also called via EventDispatcher thread to
   * ensure graphical changes do occur.
   */
  private void updatedGameData() {
    updateTitleCaption();
    updateConfirmMenu();
    updatePlugIns();
    
    if (getData().getCurTempID() == -1) {
      String s = getProperties().getProperty("ClientPreferences.TempIDsInitialValue", "");

      try {
        getData().setCurTempID("".equals(s) ? 0 : Integer.parseInt(s, getData().base));
      } catch (java.lang.NumberFormatException nfe) {
      }
    }

    // pavkovic 2004.01.04:
    // this method behaves at if the gamedata has been loaded by this
    // method.
    // this is not true at all but true enough for our needs.
    // dispatcher.fire(new GameDataEvent(this, data));
    getDispatcher().fire(new GameDataEvent(this, getData(), true));
    // also inform system about the new selection found in the GameData
    // object
    getDispatcher().fire(new SelectionEvent(this, getData().getSelectedRegionCoordinates().values(), null, SelectionEvent.ST_REGIONS));

  }

  // ////////////
  // L&F Code //
  // ////////////
  /**
   * @param laf
   */
  public void setLookAndFeel(String laf) {
    boolean lafSet = true;

    if (MagellanLookAndFeel.equals(laf) && laf.equals(getProperties().getProperty("Client.lookAndFeel", ""))) {
      lafSet = false;
    } else {
      lafSet = MagellanLookAndFeel.setLookAndFeel(laf);

      if (!lafSet) {
        laf = "Metal";
        lafSet = MagellanLookAndFeel.setLookAndFeel("Metal");
      }
    }

    if (laf.equals("Metal")) {
      MagellanLookAndFeel.loadBackground(getProperties());
    }

    if (!lafSet) {
      return;
    }

    updateLaF();

    getProperties().setProperty("Client.lookAndFeel", laf);
  }

  /**
   * DOCUMENT-ME
   */
  public void updateLaF() {
    // call updateUI in MagellanDesktop
    if (desktop != null) {
      desktop.updateLaF();
    }

    // call updateUI on preferences
    if (optionAction != null) {
      optionAction.updateLaF();
    }
  }

  private void initLookAndFeels() {
    setLookAndFeel(getProperties().getProperty("Client.lookAndFeel", "Metal"));
  }

  /**
   * DOCUMENT-ME
   * 
   * 
   */
  public String[] getLookAndFeels() {
    return (String[]) MagellanLookAndFeel.getLookAndFeelNames().toArray(new String[] {});
  }

  // ///////////////////
  // HISTORY methods //
  // ///////////////////

  /**
   * Adds a single file to the file history.
   * 
   * @param f
   *          DOCUMENT-ME
   */
  public void addFileToHistory(File f) {
    fileHistory.addFileToHistory(f);
  }

  /**
   * Returns the maximum number of entries in the history of loaded files.
   * 
   * 
   */
  public int getMaxFileHistorySize() {
    return fileHistory.getMaxFileHistorySize();
  }

  /**
   * Allows to set the maximum number of files appearing in the file history.
   * 
   * @param size
   *          DOCUMENT-ME
   */
  public void setMaxFileHistorySize(int size) {
    fileHistory.setMaxFileHistorySize(size);
  }

  // ///////////////////
  // PROPERTY Access //
  // ///////////////////
  /**
   * Changes to the report state can be done here. Normally, a change is
   * recognized by the following events.
   * 
   * @param changed
   */
  public void setReportChanged(boolean changed) {
    if (changed == false) {
      // for call from FileSaveAsAction
      updateTitleCaption();
    }

    reportState.setStateChanged(changed);
  }

  /**
   * DOCUMENT-ME
   * 
   * 
   */
  public boolean isReportChanged() {
    return reportState.isStateChanged();
  }

  /**
   * Get the selected Regions. The returned map can be empty but is never null.
   * This is a wrapper function so we dont need to give away MapperPanel.
   * 
   * 
   */
  public Map<CoordinateID,Region> getSelectedRegions() {
    return mapPanel.getSelectedRegions();
  }

  /**
   * Get the Level on the mapper Panel. This is a wrapper function so we dont
   * need to give away MapperPanel.
   * 
   * 
   */
  public int getLevel() {
    return mapPanel.getLevel();
  }

  // //////////////////////////
  // GENERAL ACCESS METHODS //
  // //////////////////////////

  /**
   * Returns the global settings used by Magellan.
   * 
   * 
   */
  public Properties getProperties() {
    if (context == null) return null;
    return context.getProperties();
  }

  /**
   * DOCUMENT-ME
   * 
   * @param newData
   *          DOCUMENT-ME
   */
  public void setData(GameData newData) {
    context.setGameData(newData);
    postProcessLoadedCR(newData);

    if (newData != null && PropertiesHelper.getboolean(getProperties(), "map.creating.void", false)) {
      newData.postProcessTheVoid();
    }

    getDispatcher().fire(new GameDataEvent(this, newData));

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        Client.this.updatedGameData();
      }
    });
  }

  /**
   * DOCUMENT-ME
   * 
   * 
   */
  public GameData getData() {
    return context.getGameData();
  }

  /**
   * DOCUMENT-ME
   * 
   * 
   */
  public MagellanDesktop getDesktop() {
    return desktop;
  }

  /**
   * 
   */
  public EventDispatcher getDispatcher() {
    return context.getEventDispatcher();
  }

  /**
   * Returns the directory the local copy of Magellan is inside.
   */
  public static File getMagellanDirectory() {
    return filesDirectory;
  }

  /**
   * Returns the directory for the Magellan settings. 
   */
  public static File getSettingsDirectory() {
    return settingsDirectory;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return the BookmarkManager associated with this Client-Object
   */
  public BookmarkManager getBookmarkManager() {
    return bookmarkManager;
  }

  /**
   * DOCUMENT-ME
   * 
   * 
   */
  public boolean isShowingStatus() {
    return showStatus;
  }

  /**
   * DOCUMENT-ME
   * 
   * @param bool
   *          DOCUMENT-ME
   */
  public void setShowStatus(boolean bool) {
    if (showStatus != bool) {
      showStatus = bool;
      getProperties().setProperty("Client.ShowOrderStatus", showStatus ? "true" : "false");

      if (getData() != null) {
        updateTitleCaption();
      }
    }
  }

  // /////////////////////////////
  // REPAINT & VISIBILITY Code //
  // /////////////////////////////
  /**
   * @param v
   */
  public void setAllVisible(boolean v) {
    desktop.setAllVisible(v);
    resetExtendedState();
  }

  private void saveExtendedState() {
    if (getProperties() == null) return;
    getProperties().setProperty("Client.extendedState", String.valueOf(JVMUtilities.getExtendedState(this)));
  }

  private void resetExtendedState() {
    int state = new Integer(getProperties().getProperty("Client.extendedState", "-1")).intValue();

    if (state != -1) {
      JVMUtilities.setExtendedState(this, state);
    }
  }

  // The repaint functions are overwritten to repaint the whole Magellan
  // Desktop. This is necessary because of the desktop mode FRAME.
  /**
   * @see java.awt.Component#repaint()
   */
  public void repaint() {
    super.repaint();

    if (desktop != null) {
      desktop.repaintAllComponents();
    }
  }

  /**
   * DOCUMENT-ME
   * 
   * @param millis
   *          DOCUMENT-ME
   */
  public void repaint(int millis) {
    super.repaint(millis);

    if (desktop != null) {
      desktop.repaintAllComponents();
    }
  }

  // /////////////////
  // SHORTCUT Code //
  // /////////////////

  /**
   * Empty because registered directly.
   * 
   * 
   */
  public Iterator<KeyStroke> getShortCuts() {
    return null; // not used - we register directly with a KeyStroke
  }

  /**
   * Repaints the client.
   * 
   * @param shortcut
   *          DOCUMENT-ME
   */
  public void shortCut(javax.swing.KeyStroke shortcut) {
    desktop.repaintAllComponents();
  }

  /**
   * DOCUMENT-ME
   * 
   * @param stroke
   *          DOCUMENT-ME
   * 
   */
  public String getShortcutDescription(Object stroke) {
    return Resources.get("client.shortcut.description");
  }

  /**
   * DOCUMENT-ME
   * 
   * 
   */
  public String getListenerDescription() {
    return Resources.get("client.shortcut.title");
  }

  /**
   * Returns an adapter for the preferences of this class.
   * 
   * 
   */
  public PreferencesAdapter createPreferencesAdapter() {
    return new ClientPreferences(getProperties(), this);
  }

  // /////////////////
  // INNER Classes //
  // /////////////////
  private class MenuActionObserver implements PropertyChangeListener {
    protected JMenuItem item;

    /**
     * Creates a new MenuActionObserver object.
     * 
     * @param item
     *          DOCUMENT-ME
     * @param action
     *          DOCUMENT-ME
     */
    public MenuActionObserver(JMenuItem item, Action action) {
      this.item = item;
      action.addPropertyChangeListener(this);
    }

    /**
     * DOCUMENT-ME
     * 
     * @param e
     *          DOCUMENT-ME
     */
    public void propertyChange(PropertyChangeEvent e) {
      if ((e.getPropertyName() != null) && e.getPropertyName().equals("accelerator")) {
        item.setAccelerator((KeyStroke) e.getNewValue());
      }
    }
  }

  /**
   * Simple class to look for events changing the data.
   */
  protected class ReportObserver implements GameDataListener, OrderConfirmListener, TempUnitListener, UnitOrdersListener {
    protected boolean stateChanged = false;

    protected long lastClear;

    /**
     * Creates a new ReportObserver object.
     */
    public ReportObserver(EventDispatcher e) {

      e.addGameDataListener(this);
      e.addOrderConfirmListener(this);
      e.addTempUnitListener(this);
      e.addUnitOrdersListener(this);

      lastClear = -1;
    }

    /**
     * DOCUMENT-ME
     * 
     * 
     */
    public boolean isStateChanged() {
      return stateChanged;
    }

    /**
     * DOCUMENT-ME
     * 
     * @param newState
     *          DOCUMENT-ME
     */
    public void setStateChanged(boolean newState) {
      stateChanged = newState;

      if (!newState) {
        lastClear = System.currentTimeMillis();
      }
    }

    /**
     * DOCUMENT-ME
     * 
     * @param e
     *          DOCUMENT-ME
     */
    public void orderConfirmationChanged(OrderConfirmEvent e) {
      if ((getData() != null) && isShowingStatus()) {
        updateTitleCaption();
      }

      if (lastClear < e.getTimestamp()) {
        stateChanged = true;
      }
    }


    /**
     * @see magellan.client.event.TempUnitListener#tempUnitCreated(magellan.client.event.TempUnitEvent)
     */
    public void tempUnitCreated(TempUnitEvent e) {
      if (lastClear < e.getTimestamp()) {
        stateChanged = true;
      }
    }

    /**
     * @see magellan.client.event.TempUnitListener#tempUnitDeleting(magellan.client.event.TempUnitEvent)
     */
    public void tempUnitDeleting(TempUnitEvent e) {
      if (lastClear < e.getTimestamp()) {
        stateChanged = true;
      }
    }

    /**
     * DOCUMENT-ME
     * 
     * @param e
     *          DOCUMENT-ME
     */
    public void gameDataChanged(GameDataEvent e) {
      if ((lastClear < e.getTimestamp()) && (e.getGameData() != null)) {
        stateChanged = true;
      } else {
        stateChanged = false;
      }
    }

    /**
     * DOCUMENT-ME
     * 
     * @param e
     *          DOCUMENT-ME
     */
    public void unitOrdersChanged(UnitOrdersEvent e) {
      if (lastClear < e.getTimestamp()) {
        stateChanged = true;
      }
    }
  }

  public Collection getSelectedObjects() {
    return overviewPanel.getSelectedObjects();
  }
  
  /**
   * Returns a list of all loaded magellan plugins.
   */
  public Collection<MagellanPlugIn> getPlugIns() {
    return plugIns;
  }

  /**
   * This method tries to load all Magellan PlugIns.
   */  
  public void initPlugIns() {
    MagellanPlugInLoader loader = new MagellanPlugInLoader();
    Properties properties = getProperties();
    // helper: stote Magellan-Dir in properties toBe changed
    properties.setProperty("plugin.helper.magellandir", filesDirectory.toString());
    Collection<Class<MagellanPlugIn>> plugInClasses = loader.getExternalModuleClasses(properties);
    
    for (Class<MagellanPlugIn> plugInClass : plugInClasses) {
      try {
        MagellanPlugIn plugIn = plugInClass.newInstance();
        plugIn.init(this, properties);
        plugIns.add(plugIn);
      } catch (Throwable t) {
        ErrorWindow errorWindow = new ErrorWindow(this,t.getMessage(),"",t);
        errorWindow.setVisible(true);
      }
    }
  }

  /**
   * Returns a String representing all parts of the component for debugging. 
   */
  public static String debug(Component comp) {
    String result = "";
    if (comp instanceof Container) {
      Container container = (Container)comp;
      result = "Container: "+container +"\n";
      result+="{";
      Component[] comps = container.getComponents();
      for (Component acomp : comps) result+=" "+debug(acomp)+"\n";
      result+=")";
    } else {
      result = "Component: "+comp+"\n";
    }
    return result;
  }
  
  /**
   * on windows-OS tries to locate the included ECheck.exe and
   * if found save the path into properties
   */
  public void initECheckPath(Properties settings){
    // check if we have a windows os
    String osName = System.getProperty("os.name");
    osName = osName.toLowerCase();
    if (osName.indexOf("windows")>-1){
      log.info("new ini. windows OS detected. (" + osName + ")");
      // we have a windows OS
      // lets asume the location
      String actPath = settingsDirectory + File.separator + "echeck" + File.separator + "ECheck.exe";
      log.info("checking for ECheck: " + actPath);
      File echeckFile = new File(actPath);
      if (echeckFile.exists()){
        // yep, we have an ECheck.exe here
        // lets add to the properties
        settings.setProperty("JECheckPanel.echeckEXE", echeckFile.toString());
        log.info("set echeckEXE to: " + echeckFile.toString());
      } else {
        log.info("ECheck.exe not found"); 
      }
    } else {
      log.info("new ini. non - windows OS detected. (" + osName + ")");
    }
  }
  
}
