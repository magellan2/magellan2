/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;

import magellan.client.actions.MenuAction;
import magellan.client.actions.edit.FindAction;
import magellan.client.actions.edit.QuickFindAction;
import magellan.client.actions.edit.RedoAction;
import magellan.client.actions.edit.UndoAction;
import magellan.client.actions.extras.AlchemyAction;
import magellan.client.actions.extras.ArmyStatsAction;
import magellan.client.actions.extras.ConversionAction;
import magellan.client.actions.extras.FactionStatsAction;
import magellan.client.actions.extras.HelpAction;
import magellan.client.actions.extras.InfoAction;
import magellan.client.actions.extras.OptionAction;
import magellan.client.actions.extras.ProfileAction;
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
import magellan.client.actions.map.FillInsideAction;
import magellan.client.actions.map.FillSelectionAction;
import magellan.client.actions.map.InvertSelectionAction;
import magellan.client.actions.map.IslandAction;
import magellan.client.actions.map.MapSaveAction;
import magellan.client.actions.map.OpenSelectionAction;
import magellan.client.actions.map.SaveSelectionAction;
import magellan.client.actions.map.SelectAllAction;
import magellan.client.actions.map.SelectIslandsAction;
import magellan.client.actions.map.SelectNothingAction;
import magellan.client.actions.map.SetGirthAction;
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
import magellan.client.extern.MainMenuProvider;
import magellan.client.preferences.ClientPreferences;
import magellan.client.preferences.DetailsViewAutoCompletionPreferences;
import magellan.client.preferences.Install4J;
import magellan.client.swing.AskForPasswordDialog;
import magellan.client.swing.DebugDock;
import magellan.client.swing.DialogProvider;
import magellan.client.swing.ECheckPanel;
import magellan.client.swing.InfoDialog;
import magellan.client.swing.InternationalizedDataPanel;
import magellan.client.swing.MagellanLookAndFeel;
import magellan.client.swing.MapperPanel;
import magellan.client.swing.MenuProvider;
import magellan.client.swing.MessagePanel;
import magellan.client.swing.ProgressBarUI;
import magellan.client.swing.StartWindow;
import magellan.client.swing.TempUnitDialog;
import magellan.client.swing.TipOfTheDay;
import magellan.client.swing.map.CellGeometry;
import magellan.client.swing.map.MapCellRenderer;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.client.swing.tasks.TaskTablePanel;
import magellan.client.swing.tree.NodeWrapperFactory;
import magellan.client.utils.BookmarkDock;
import magellan.client.utils.BookmarkManager;
import magellan.client.utils.ErrorWindow;
import magellan.client.utils.FileHistory;
import magellan.client.utils.IconAdapterFactory;
import magellan.client.utils.LanguageDialog;
import magellan.client.utils.MagellanFinder;
import magellan.client.utils.PluginSettingsFactory;
import magellan.client.utils.ProfileManager;
import magellan.client.utils.RendererLoader;
import magellan.client.utils.ResourceSettingsFactory;
import magellan.client.utils.SelectionHistory;
import magellan.client.utils.SwingUtils;
import magellan.library.CoordinateID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.GameDataMerger;
import magellan.library.HasRegion;
import magellan.library.Message;
import magellan.library.MissingData;
import magellan.library.Region;
import magellan.library.TempUnit;
import magellan.library.TrustLevel;
import magellan.library.Unit;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.io.GameDataReader;
import magellan.library.io.cr.CRWriter;
import magellan.library.io.file.FileBackup;
import magellan.library.io.file.FileType;
import magellan.library.io.file.FileType.ReadOnlyException;
import magellan.library.io.file.FileTypeFactory;
import magellan.library.rules.Date;
import magellan.library.tasks.GameDataInspector;
import magellan.library.tasks.Inspector;
import magellan.library.tasks.InspectorInterceptor;
import magellan.library.tasks.Problem;
import magellan.library.utils.Locales;
import magellan.library.utils.Log;
import magellan.library.utils.MagellanImages;
import magellan.library.utils.MagellanUrl;
import magellan.library.utils.MarkovNameGenerator;
import magellan.library.utils.MemoryManagment;
import magellan.library.utils.NameGenerator;
import magellan.library.utils.NullUserInterface;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Regions;
import magellan.library.utils.Resources;
import magellan.library.utils.SelfCleaningProperties;
import magellan.library.utils.TrustLevels;
import magellan.library.utils.UserInterface;
import magellan.library.utils.Utils;
import magellan.library.utils.VersionInfo;
import magellan.library.utils.logging.Logger;
import magellan.library.utils.transformation.BoxTransformer;
import magellan.library.utils.transformation.BoxTransformer.BBox;
import magellan.library.utils.transformation.BoxTransformer.BBoxes;

/**
 * This class is the root of all evil. It represents also the main entry point into the application
 * and also the basic frame the application creates. It is a singleton which is instantiated from
 * {@link #main(String[])} and stored in {@link #INSTANCE}
 *
 * @author $Author: $
 * @version $Revision: 388 $
 */
public class Client extends JFrame implements ShortcutListener, PreferencesFactory {
  private static final Logger log = Logger.getInstance(Client.class);

  /** The name of the magellan settings file. */
  public static final String SETTINGS_FILENAME = "magellan.ini";

  /**
   * The name of the ini file for order completions.
   *
   * @deprecated A separate completion file is no longer actively supported
   */
  @Deprecated
  public static final String COMPLETIONSETTINGS_FILENAME = "magellan_completions.ini";

  public static final String DEFAULT_LAF = "Nimbus";

  public static final String[] FALLBACK_LAF = { "Metal", "Windows" };

  /** This is the instance of this class */
  public static Client INSTANCE;

  private List<JPanel> panels;

  private MapperPanel mapPanel;

  /** The overview panel */
  private EMapOverviewPanel overviewPanel;

  /** The details panel */
  private EMapDetailsPanel detailsPanel;

  /** The message panel */
  private MessagePanel messagePanel;

  /** The ECheck panel */
  private ECheckPanel echeckPanel;

  /** The open tasks panel */
  private TaskTablePanel taskPanel;

  private FileHistory fileHistory;

  private JMenu factionOrdersMenu;

  private JMenu factionOrdersMenuNot;

  private JMenu invertAllOrdersConfirmation;

  private List<NodeWrapperFactory> nodeWrapperFactories;

  private List<PreferencesFactory> preferencesAdapterList;

  private MenuAction saveAction;

  private OptionAction optionAction;

  private MagellanDesktop desktop;

  private ReportObserver reportState;

  /** Manager for setting and activating bookmarks. */
  private BookmarkManager bookmarkManager;

  /** Central undo manager - specialized to deliver change events */
  private MagellanUndoManager undoMgr;

  /** Directory for binaries */
  private static File binDirectory;

  /**
   * Directory for resources. Usually identical to binDirectory, but can be used to load texts, images
   * and the like from elsewhere
   */
  private static File resourceDirectory;

  /** Directory of "magellan.ini" etc. */
  private static File settingsDirectory;

  public static File logFile;

  /** show order status in title */
  protected boolean showStatus = false;

  protected JMenuItem progressItem;

  /** start window, disposed after first init */
  protected static StartWindow startWindow;

  /** contains the list of all loadable plugins */
  protected Collection<MagellanPlugIn> plugIns = new ArrayList<MagellanPlugIn>();

  private Macifier macifier;

  /**
   * Dummy implementation, for testing.
   */
  protected Client() {
    Client.INSTANCE = this;
    Client.binDirectory = MagellanFinder.findMagellanDirectory();
    Client.resourceDirectory = new File(".");
    Client.settingsDirectory = Client.resourceDirectory;

    // get new dispatcher
    EventDispatcher dispatcher = new EventDispatcher();

    Properties settings = initNewSettings();

    context = new MagellanContext(this);
    context.setEventDispatcher(dispatcher);
    context.setProperties(settings);
    context.init();

    // context.setGameData(gd);

    // List<Container> topLevelComponents = new LinkedList<Container>();
    // Map<String, Component> components = initComponents(topLevelComponents);

    // desktop = MagellanDesktop.getInstance();
    // desktop.init(this, context, settings, components, Client.getSettingsDirectory());

    // setContentPane(desktop);

    // do it here because we need the desktop menu
    // setJMenuBar(createMenuBar(topLevelComponents));

    // disable log messages that are only good for console mode
    // NullUserInterface.setLogLevel(Logger.WARN);
  }

  /**
   * Creates a new Client object taking its data from <kbd>gd</kbd>.
   * <p>
   * Preferences are read from and stored in a file called <kbd>magellan.ini</kbd>. This file is usually
   * located in the user's home directory, which is the Windows directory in a Microsoft Windows
   * environment.
   * </p>
   *
   * @param gd
   * @param binDir The directory where magellan files are situated
   * @param resourceDir The directory where magellan configuration files are situated
   * @param settingsDir The directory where the settings are situated
   */
  protected Client(GameData gd, File binDir, File resourceDir, File settingsDir, File logFile) {
    this(gd, binDir, resourceDir, settingsDir, true, logFile);
  }

  /**
   * Creates a new Client object taking its data from <kbd>gd</kbd>.
   * <p>
   * Preferences are read from and stored in a file called <kbd>magellan.ini</kbd>. This file is usually
   * located in the user's home directory, which is the Windows directory in a Microsoft Windows
   * environment.
   * </p>
   *
   * @param gd
   * @param binDir The directory where magellan files are situated
   * @param resourceDir The directory where magellan configuration files are situated
   * @param settingsDir The directory where the settings are situated
   * @param ask show the ask locale dialog, used for testing only
   */
  @SuppressWarnings("deprecation")
  protected Client(GameData gd, File binDir, File resourceDir, File settingsDir, boolean ask,
      File logFile) {
    Client.INSTANCE = this;
    Client.binDirectory = binDir;
    Client.resourceDirectory = resourceDir;
    Client.settingsDirectory = settingsDir;
    Client.logFile = logFile;

    // get new dispatcher
    EventDispatcher dispatcher = new EventDispatcher();

    Client.startWindow.progress(1, Resources.get("clientstart.1"));
    Properties settings = Client.loadSettings(Client.settingsDirectory, Client.SETTINGS_FILENAME);
    String lastSavedVersion = null;
    if (settings == null) {
      Client.log.info("Client.loadSettings: settings file " + Client.SETTINGS_FILENAME
          + " does not exist, using default values.");
      settings = initNewSettings();
      initLocales(settings, ask);
    } else {
      initLocales(settings, false);

      fixSettings(settings);

      lastSavedVersion = settings.getProperty("Client.SemanticVersion");
      if (lastSavedVersion == null) {
        lastSavedVersion = settings.getProperty(PropertiesHelper.VERSION);
      }
      if (lastSavedVersion == null) {
        lastSavedVersion = "0.0.0";
      }
    }
    if (VersionInfo.getVersion(resourceDir) != null) {
      settings.setProperty(PropertiesHelper.VERSION, VersionInfo.getVersion(resourceDir));
      settings.setProperty(PropertiesHelper.SEMANTIC_VERSION, VersionInfo.getSemanticVersion(resourceDir));
    }
    if (lastSavedVersion != null) {
      settings.setProperty(PropertiesHelper.LAST_VERSION, lastSavedVersion);
    }

    showStatus = PropertiesHelper.getBoolean(settings, "Client.ShowOrderStatus", false);

    // initialize the context, this has to be very early.
    context = new MagellanContext(this);
    context.setEventDispatcher(dispatcher);
    context.setProperties(settings);
    context.init();

    context.setGameData(gd);
    // init icon, fonts, repaint shortcut, L&F, window things
    initUI();

    // create management and observer objects
    dispatcher.addSelectionListener(SelectionHistory.getSelectionEventHook());
    dispatcher.addTempUnitListener(SelectionHistory.getTempUnitEventHook());
    bookmarkManager = new BookmarkManager(dispatcher);
    undoMgr = new MagellanUndoManager();
    reportState = new ReportObserver(dispatcher);

    // load plugins
    initPlugIns();

    // init components
    Client.startWindow.progress(2, Resources.get("clientstart.2"));
    panels = new LinkedList<JPanel>();
    nodeWrapperFactories = new LinkedList<NodeWrapperFactory>();

    List<Container> topLevelComponents = new LinkedList<Container>();
    Map<String, Component> components = initComponents(topLevelComponents);

    // dispatcher.addGameDataListener(Units.getGameDataListener());

    // init desktop
    Client.startWindow.progress(3, Resources.get("clientstart.3"));
    SwingUtils.setBounds(this, settings, "Client", true);

    desktop = MagellanDesktop.getInstance();
    desktop.init(this, context, settings, components, Client.getSettingsDirectory());

    setContentPane(desktop);

    // load plugins
    // initPlugIns();

    // do it here because we need the desktop menu
    setJMenuBar(createMenuBar(topLevelComponents));

    // enable EventDisplayer
    // new
    // com.eressea.util.logging.EventDisplayDialog(this,false,dispatcher).setVisible(true);

    // disable log messages that are only good for console mode
    NullUserInterface.setLogLevel(Logger.WARN);
  }

  // ////////////////////////
  // BASIC initialization //
  // ////////////////////////
  public static final String[][] colorProperties = new String[][] { {
      PropertiesHelper.MESSAGETYPE_SECTION_EVENTS_COLOR,
      PropertiesHelper.MESSAGETYPE_SECTION_MOVEMENTS_COLOR,
      PropertiesHelper.MESSAGETYPE_SECTION_ECONOMY_COLOR,
      PropertiesHelper.MESSAGETYPE_SECTION_MAGIC_COLOR,
      PropertiesHelper.MESSAGETYPE_SECTION_STUDY_COLOR,
      PropertiesHelper.MESSAGETYPE_SECTION_PRODUCTION_COLOR,
      PropertiesHelper.MESSAGETYPE_SECTION_ERRORS_COLOR,
      PropertiesHelper.MESSAGETYPE_SECTION_BATTLE_COLOR,
      PropertiesHelper.MESSAGETYPE_SECTION_UNKNOWN_COLOR
  }, {
      "#009999", // Format: #RRGGBB
      "#000000",
      "#000066",
      "#666600",
      "#006666",
      "#009900",
      "#990000",
      "#999900",
      "#555555"
  }, {
      "events",
      "movement",
      "economy",
      "magic",
      "study",
      "production",
      "errors",
      "battle",
      "unknown"
  }
  };

  private void fixSettings(Properties settings) {
    // backward compatibility for white message tags (it's now the text color)
    for (int c = 0; c < colorProperties[0].length; ++c) {
      if (settings.getProperty(colorProperties[0][c], "-").equals("#FFFFFF")) {
        settings.setProperty(colorProperties[0][c], colorProperties[1][c]);
      }
    }
  }

  protected static Properties initNewSettings() {
    SelfCleaningProperties settings = new SelfCleaningProperties();
    settings.setProperty(PropertiesHelper.CLIENT_LOOK_AND_FEEL, Client.DEFAULT_LAF);
    settings.setProperty(PropertiesHelper.ADVANCEDSHAPERENDERER
        + PropertiesHelper.ADVANCEDSHAPERENDERER_S_SETS, ",Einkaufsgut");
    settings.setProperty(PropertiesHelper.ADVANCEDSHAPERENDERER
        + PropertiesHelper.ADVANCEDSHAPERENDERER_S_CURRENT_SET, "Einkaufsgut");
    settings.setProperty(PropertiesHelper.ADVANCEDSHAPERENDERER + ".Einkaufsgut"
        + PropertiesHelper.ADVANCEDSHAPERENDERER_CURRENT,
        "\u00A7if\u00A7<\u00A7price\u00A7\u00D6l\u00A7-1\u00A71\u00A7else\u00A7if\u00A7<\u00A7price\u00A7Weihrauch\u00A7-1\u00A72\u00A7else\u00A7if\u00A7<\u00A7price\u00A7Seide\u00A7-1\u00A73\u00A7else\u00A7if\u00A7<\u00A7price\u00A7Myrrhe\u00A7-1\u00A74\u00A7else\u00A7if\u00A7<\u00A7price\u00A7Juwel\u00A7-1\u00A75\u00A7else\u00A7if\u00A7<\u00A7price\u00A7Gew\u00FCrz\u00A7-1\u00A76\u00A7else\u00A7if\u00A7<\u00A7price\u00A7Balsam\u00A7-1\u00A77\u00A7end\u00A7end\u00A7end\u00A7end\u00A7end\u00A7end\u00A7");
    settings.setProperty(PropertiesHelper.ADVANCEDSHAPERENDERER + ".Einkaufsgut"
        + PropertiesHelper.ADVANCEDSHAPERENDERER_MAXIMUM, "10");
    settings.setProperty(PropertiesHelper.ADVANCEDSHAPERENDERER + ".Einkaufsgut"
        + PropertiesHelper.ADVANCEDSHAPERENDERER_COLORS,
        "0.0;223,131,39;0.12162162;220,142,24;0.14864865;153,153,153;0.23648648;153,153,153;0.26013514;204,255,255;0.3445946;204,255,255;0.3716216;0,204,0;0.42905405;0,204,0;0.46283785;255,51,0;0.5371622;255,51,0;0.5608108;255,255,0;0.6317568;255,255,0;0.6621622;51,51,255;1.0;0,51,255");
    settings.setProperty(PropertiesHelper.ADVANCEDSHAPERENDERER + ".Einkaufsgut"
        + PropertiesHelper.ADVANCEDSHAPERENDERER_VALUES, "0.0;0.0;1.0;1.0");
    settings.setProperty(PropertiesHelper.ADVANCEDSHAPERENDERER + ".Einkaufsgut"
        + PropertiesHelper.ADVANCEDSHAPERENDERER_MINIMUM, "0");

    // Message Panel Default colors.
    for (int c = 0; c < colorProperties[0].length; ++c) {
      settings.setProperty(colorProperties[0][c], colorProperties[1][c]);
    }

    DetailsViewAutoCompletionPreferences.applyDefault(settings);

    // try to set path to ECheck
    initECheckPath(settings);

    return settings;
  }

  private MagellanContext context;

  private TitleLine title;

  private NameGenerator generator;

  /**
   * Load the file fileName in the given directory into the settings object.
   */
  public static Properties loadSettings(File directory, String fileName) {
    Properties settings = new SelfCleaningProperties();
    // settings = new OrderedOutputProperties();
    // settings = new AgingProperties();

    settings.clear();

    File settingsFile = new File(directory, fileName);

    // load settings from file
    if (settingsFile.exists()) {
      try {
        BufferedInputStream stream = null;
        try {
          settings.load(stream = new BufferedInputStream(new FileInputStream(settingsFile)));
          Client.log.info("Client.loadSettings: successfully loaded " + settingsFile);
        } finally {
          if (stream != null) {
            stream.close();
          }
        }
      } catch (IOException e) {
        Client.log.error("Client.loadSettings: Error while loading " + settingsFile, e);
        return null;
      }
    } else
      return null;
    return settings;
  }

  protected void initLocales(Properties settings, boolean ask) {
    if (ask) {
      LanguageDialog ld = new LanguageDialog(Client.startWindow, settings);

      if (ld.languagesFound()) {
        // startWindow.toBack();
        Point p = Client.startWindow.getLocation();
        ld.setLocation((int) p.getX() + (Client.startWindow.getWidth() - ld.getWidth()) / 2, (int) p
            .getY() - ld.getHeight() / 2);
        Locale locale = ld.show();
        // startWindow.toFront();
        if (locale == null) {
          // without this decision we cannot start the application
          Client.log.error("can't work without locale");
          quit(false);
        } else if (!locale.equals(Locale.getDefault())) {
          settings.setProperty("locales.gui", locale.getLanguage());
          settings.setProperty("locales.orders", locale.getLanguage());
        }
      }
    }

    if (settings.getProperty("locales.gui") != null) {
      Locales.setGUILocale(new Locale(settings.getProperty("locales.gui")));
    } else {
      Locales.setGUILocale(Locale.getDefault());
    }
    if (settings.getProperty("locales.orders") != null) {
      Locales.setOrderLocale(new Locale(settings.getProperty("locales.orders")));
    } else {
      Locales.setOrderLocale(Locale.GERMAN);
    }
    Client.log.info("GUI locale: " + Locales.getGUILocale() + settings.getProperty("locales.gui")
        + ", orders locale: " + Locales.getOrderLocale() + settings.getProperty("locales.orders"));
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
   * Returns the message panel.
   */
  public MessagePanel getMessagePanel() {
    return messagePanel;
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
   * Inits base UI things: # frame icon # window event things # fonts # repaint shortcut # L&amp;F
   */
  protected void initUI() {
    Image iconImage = Client.getApplicationIcon();

    // set the application icon
    if (iconImage != null) {
      setIconImage(iconImage);
    }

    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        quit(true);
      }
    });

    // select all in textfields on focus gain globally
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(
        "permanentFocusOwner", new PropertyChangeListener() {
          public void propertyChange(final PropertyChangeEvent e) {
            if (e.getNewValue() instanceof JTextField) {
              // invokeLater needed for JFormattedTextField
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  JTextField textField = (JTextField) e.getNewValue();
                  textField.selectAll();
                }
              });
            }
          }
        });

    // initialize client shortcut - F5 to repaint
    DesktopEnvironment.registerShortcutListener(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), this);

    // init L&F
    initLookAndFeels();
  }

  private void setFontSize(float fScale) {
    try {

      String[] fonts = { "ArrowButton.font",
          "Button.font",
          "CheckBox.font",
          "CheckBoxMenuItem.acceleratorFont",
          "CheckBoxMenuItem.font",
          "ColorChooser.font",
          "ComboBox.font",
          "DesktopIcon.font",
          "DesktopPane.font",
          "EditorPane.font",
          "FileChooser.font",
          "FileChooser.listFont",
          "FormattedTextField.font",
          "InternalFrame.font",
          "InternalFrame.titleFont",
          "InternalFrameTitlePane.font",
          "Label.font",
          "List.font",
          "Menu.acceleratorFont",
          "MenuBar.font",
          "Menu.font",
          "MenuItem.acceleratorFont",
          "MenuItem.font",
          "OptionPane.buttonFont",
          "OptionPane.font",
          "OptionPane.messageFont",
          "OptionPane.titleFont",
          "Panel.font",
          "PasswordField.font",
          "PopupMenu.font",
          "PopupMenuSeparator.font",
          "ProgressBar.font",
          "RadioButton.font",
          "RadioButtonMenuItem.acceleratorFont",
          "RadioButtonMenuItem.font",
          "RootPane.font",
          "ScrollBar.font",
          "ScrollBarThumb.font",
          "ScrollBarTrack.font",
          "ScrollPane.font",
          "Separator.font",
          "Slider.font",
          "SliderThumb.font",
          "SliderTrack.font",
          "Spinner.font",
          "SplitPane.font",
          "TabbedPane.font",
          "Table.font",
          "TableHeader.font",
          "TextArea.font",
          "TextField.font",
          "TextPane.font",
          "TitledBorder.font",
          "ToggleButton.font",
          "ToolBar.font",
          "ToolTip.font",
          "Tree.font",
          "Viewport.font",
          "defaultFont" };

      {
        Set<String> sFonts = new HashSet<String>(Arrays.asList(fonts));
        UIDefaults lTable = UIManager.getLookAndFeelDefaults();
        Enumeration<?> eKeys = lTable.keys();
        Map<Font, Integer> sizes = new HashMap<Font, Integer>();
        Map<Font, Font> derivatives = new HashMap<Font, Font>();
        while (eKeys.hasMoreElements()) {
          Object key = eKeys.nextElement();
          Object val = lTable.get(key);
          Font font;
          if (val instanceof Font) {
            font = (Font) val;
          } else {
            font = UIManager.getFont(key);
          }

          if (font != null) {
            Integer n = sizes.get(font);
            // log.finest(key + ":" + font);
            if (n == null) {
              sizes.put(font, 1);
              Font scaledFont = font;
              if (fScale != 1.0f && !derivatives.containsValue(font)) {
                scaledFont = new FontUIResource(font.deriveFont(font.getSize2D() * fScale));
                derivatives.put(font, scaledFont);
                font = scaledFont;
              }
            } else {
              sizes.put(font, n + 1);
              font = derivatives.get(font);
            }
            UIManager.put(key, font);
            lTable.put(key, font);
            if (!sFonts.contains(key)) {
              log.warn("unknown font key " + key);
            }
          }
        }
        int max = 0;
        Font maxS = null;
        for (Font s : sizes.keySet()) {
          log.finer("font " + s + ": " + sizes.get(s));
          if (sizes.get(s) > max) {
            max = sizes.get(s);
            maxS = s;
          }
        }
        if (maxS != null) {
          Font font = maxS;
          if (fScale != 1.0f) {
            font = derivatives.get(font);
          }
          log.fine("new default font: " + font);
          for (String key : fonts) {
            if (UIManager.get(key) == null) {
              log.fine("new font " + key);
              UIManager.put(key, font);
              lTable.put(key, font);
            }
          }

        }
      }
    } catch (Throwable e) {
      Client.log.error(e);
    }

  }

  // ////////////////////////////
  // COMPONENT initialization //
  // ////////////////////////////

  /**
   * Initializes the Magellan components. The returned hashtable holds all components with well-known
   * desktop keywords.
   *
   * @param topLevel
   */
  protected Map<String, Component> initComponents(List<Container> topLevel) {
    Map<String, Component> components = new Hashtable<String, Component>();

    // configure and add map panel
    // get cell geometry
    CellGeometry geo = new CellGeometry("cellgeometry.txt");

    // load custom renderers
    // ForcedFileClassLoader.directory = filesDirectory;
    RendererLoader rl = new RendererLoader(Client.getResourceDirectory(), ".", geo,
        getProperties());
    Collection<MapCellRenderer> cR = rl.loadRenderers();

    // init mapper
    mapPanel = new MapperPanel(getMagellanContext(), cR, geo);
    mapPanel.setMinimumSize(new Dimension(100, 10));
    panels.add(mapPanel);
    components.put(MagellanDesktop.MAP_IDENTIFIER, mapPanel);
    components.put(MagellanDesktop.MINIMAP_IDENTIFIER, mapPanel.getMinimapComponent());
    topLevel.add(mapPanel);

    // configure and add message panel
    messagePanel = new MessagePanel(getDispatcher(), getData(), getProperties());
    messagePanel.setMinimumSize(new Dimension(100, 10));
    panels.add(messagePanel);
    nodeWrapperFactories.add(messagePanel.getNodeWrapperFactory());
    components.put(MagellanDesktop.MESSAGES_IDENTIFIER, messagePanel);
    topLevel.add(messagePanel);

    // configure and add details panel
    detailsPanel = new EMapDetailsPanel(getDispatcher(), getData(), getProperties(), undoMgr, new DialogProvider() {

      public TempUnitDialog create() {
        TempUnitDialog d = new TempUnitDialog(Client.this, Client.this, getProperties());
        d.setNameGenerator(getNameGenerator());
        return d;
      }
    });
    detailsPanel.setMinimumSize(new Dimension(100, 10));
    panels.add(detailsPanel);
    nodeWrapperFactories.add(detailsPanel.getNodeWrapperFactory());

    Container c = detailsPanel.getNameAndDescriptionPanel();
    components.put(MagellanDesktop.NAMEDESCRIPTION_IDENTIFIER, c);
    components.put(MagellanDesktop.NAME_IDENTIFIER, c.getComponent(0));
    components.put(MagellanDesktop.DESCRIPTION_IDENTIFIER, c.getComponent(1));
    components.put(MagellanDesktop.DETAILS_IDENTIFIER, detailsPanel.getDetailsPanel());
    components.put(MagellanDesktop.ORDERS_IDENTIFIER, detailsPanel.getOrderEditor());

    // this keyword is deprecated
    components.put(MagellanDesktop.COMMANDS_IDENTIFIER, detailsPanel.getOrderEditor());
    topLevel.add(detailsPanel);

    // configure and add overview panel
    overviewPanel = new EMapOverviewPanel(getDispatcher(), getData(), getProperties());
    overviewPanel.setMinimumSize(new Dimension(100, 10));
    panels.add(overviewPanel);
    components.put(MagellanDesktop.OVERVIEW_IDENTIFIER, overviewPanel.getOverviewComponent());
    components.put(MagellanDesktop.HISTORY_IDENTIFIER, overviewPanel.getHistoryComponent());
    components.put(MagellanDesktop.OVERVIEWHISTORY_IDENTIFIER, overviewPanel);
    nodeWrapperFactories.add(overviewPanel.getNodeWrapperFactory());
    topLevel.add(overviewPanel);

    echeckPanel = new ECheckPanel(getDispatcher(), getData(), getProperties(), getSelectedRegions()
        .values());
    components.put(MagellanDesktop.ECHECK_IDENTIFIER, echeckPanel);

    taskPanel = new TaskTablePanel(getDispatcher(), getData(), getProperties());
    components.put(MagellanDesktop.TASKS_IDENTIFIER, taskPanel);

    components.put(MagellanDesktop.DEBUG_IDENTIFIER, DebugDock.getInstance());
    components.put(MagellanDesktop.BOOKMARKS_IDENTIFIER, BookmarkDock.getInstance());

    // armyStatsPanel = new ArmyStatsPanel(getDispatcher(), getData(), getProperties(), true);
    // components.put(ArmyStatsPanel.IDENTIFIER, armyStatsPanel);

    // tradeOrganizer = new TradeOrganizer(this, getDispatcher(), getData(), getProperties());
    // components.put(TradeOrganizer.IDENTIFIER, tradeOrganizer);

    Client.log.info("Checking for dock-providers...(MagellanPlugIns)");
    for (MagellanPlugIn plugIn : plugIns) {
      Map<String, Component> plugInDocks = plugIn.getDocks();
      if (plugInDocks != null && plugInDocks.size() > 0) {
        components.putAll(plugInDocks);
      }
    }

    return components;
  }

  // //////////////////////////
  // MENUBAR initialization //
  // //////////////////////////

  /**
   * Creates a menu bar to be added to this frame.
   *
   * @param components
   */
  private JMenuBar createMenuBar(Collection<Container> components) {
    JMenuBar menuBar = new JMenuBar();

    // create static menus
    menuBar.add(createFileMenu());
    menuBar.add(createEditMenu());
    menuBar.add(createOrdersMenu());
    menuBar.add(createBookmarkMenu());
    menuBar.add(createMapMenu());

    // create dynamix menus -- currently not used (stm)
    Map<String, JMenu> topLevel = new HashMap<String, JMenu>();
    List<JMenu> direction = new LinkedList<JMenu>();
    Client.log.info("Checking for menu-providers...");

    for (Container o : components) {
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

    // currently not used (stm)
    // for (JMenu menu : topLevel.values()) {
    // menuBar.add(menu);
    // }

    // desktop and extras last
    menuBar.add(desktop.getDesktopMenu());

    // add external modules if some can be found
    JMenu plugInMenu = null;
    Client.log.info("Checking for menu-providers...(MagellanPlugIns)");
    for (MagellanPlugIn plugIn : plugIns) {
      List<JMenuItem> plugInMenuItems = plugIn.getMenuItems();
      if (plugInMenuItems != null && plugInMenuItems.size() > 0) {
        if (plugInMenu == null) {
          plugInMenu = new JMenu(Resources.get("client.menu.plugins.caption"));
          plugInMenu.setMnemonic(Resources.get("client.menu.plugins.mnemonic").charAt(0));
          menuBar.add(plugInMenu);
        } else {
          plugInMenu.addSeparator();
        }
        for (JMenuItem menuItem : plugInMenuItems) {
          plugInMenu.add(menuItem);
        }
      }
    }

    // the special menu - a plugin with own main menu entry
    for (MagellanPlugIn plugIn : plugIns) {
      if (plugIn instanceof MainMenuProvider) {
        MainMenuProvider p = (MainMenuProvider) plugIn;
        JMenu newJMenu = p.getJMenu();
        if (newJMenu != null) {
          menuBar.add(newJMenu);
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
    file.addSeparator();
    addMenuItem(file, new SaveOrdersAction(this, SaveOrdersAction.Mode.DIALOG));
    addMenuItem(file, new SaveOrdersAction(this, SaveOrdersAction.Mode.MAIL));
    addMenuItem(file, new SaveOrdersAction(this, SaveOrdersAction.Mode.FILE));
    addMenuItem(file, new SaveOrdersAction(this, SaveOrdersAction.Mode.CLIPBOARD));
    addMenuItem(file, new SaveOrdersAction(this, SaveOrdersAction.Mode.PUT_ON_SERVER));
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
    addMenuItem(edit, new QuickFindAction(this));

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
    invertAllOrdersConfirmation.setMnemonic(Resources.get("client.menu.orders.invert.mnemonic")
        .charAt(0));
    ordersMenu.add(invertAllOrdersConfirmation);

    updateConfirmMenu();

    return ordersMenu;
  }

  private void refillChangeFactionConfirmation(JMenu aMenu, int aConfirmationType) {
    if (aMenu.getItemCount() == 0) {
      // fill basic faction "all units"
      addMenuItem(aMenu, new ChangeFactionConfirmationAction(this, null, aConfirmationType, false,
          false));
      addMenuItem(aMenu, new ChangeFactionConfirmationAction(this, null, aConfirmationType, true,
          false));
      addMenuItem(aMenu, new ChangeFactionConfirmationAction(this, null, aConfirmationType, false,
          true));
      addMenuItem(aMenu, new ChangeFactionConfirmationAction(this, null, aConfirmationType, true,
          true));
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
      for (Faction f : getData().getFactions()) {
        if (TrustLevels.isPrivileged(f) && !f.units().isEmpty()) {
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
    addMenuItem(map, new SetGirthAction(this));
    addMenuItem(map, new IslandAction(this));
    addMenuItem(map, new MapSaveAction(this, mapPanel));
    map.addSeparator();
    addMenuItem(map, new SelectAllAction(this));
    addMenuItem(map, new SelectNothingAction(this));
    addMenuItem(map, new InvertSelectionAction(this));
    addMenuItem(map, new SelectIslandsAction(this));
    addMenuItem(map, new FillSelectionAction(this));
    addMenuItem(map, new FillInsideAction(this));
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
    toggle.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, InputEvent.CTRL_DOWN_MASK));
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
    backward.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, InputEvent.SHIFT_DOWN_MASK));
    backward.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        bookmarkManager.jumpBackward();
      }
    });
    bookmarks.add(backward);

    JMenuItem clear = new JMenuItem(Resources.get("client.menu.bookmarks.clear.caption"));
    clear.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        bookmarkManager.clearBookmarks();
      }
    });
    bookmarks.add(clear);

    JMenuItem save = new JMenuItem(Resources.get("client.menu.bookmarks.save.caption"));
    save.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        bookmarkManager.saveBookmarks();
      }
    });
    bookmarks.add(save);

    JMenuItem load = new JMenuItem(Resources.get("client.menu.bookmarks.load.caption"));
    load.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        bookmarkManager.loadBookmarks();
      }
    });
    bookmarks.add(load);

    return bookmarks;
  }

  protected JMenu createExtrasMenu() {
    JMenu extras = new JMenu(Resources.get("client.menu.extras.caption"));
    extras.setMnemonic(Resources.get("client.menu.extras.mnemonic").charAt(0));
    addMenuItem(extras, new FactionStatsAction(this));
    addMenuItem(extras, new ArmyStatsAction(this));
    addMenuItem(extras, new TradeOrganizerAction(this));
    addMenuItem(extras, new AlchemyAction(this));

    // addMenuItem(extras, new TaskTableAction(this));
    // addMenuItem(extras, new ECheckAction(this));
    addMenuItem(extras, new VorlageAction(this));
    extras.addSeparator();
    addMenuItem(extras, new ConversionAction(this));
    extras.addSeparator();
    addMenuItem(extras, new RepaintAction(this));
    addMenuItem(extras, new TileSetAction(this, mapPanel));
    extras.addSeparator();
    preferencesAdapterList = new ArrayList<PreferencesFactory>(8);
    preferencesAdapterList.add(this);
    preferencesAdapterList.add(desktop);
    preferencesAdapterList.add(overviewPanel);
    preferencesAdapterList.add(detailsPanel);
    preferencesAdapterList.add(mapPanel);
    preferencesAdapterList.add(taskPanel);
    preferencesAdapterList.add(new IconAdapterFactory(nodeWrapperFactories));
    preferencesAdapterList.add(new ResourceSettingsFactory(plugIns, getProperties()));

    preferencesAdapterList.add(new PluginSettingsFactory(plugIns, getProperties()));

    optionAction = new OptionAction(this, preferencesAdapterList);
    addMenuItem(extras, optionAction);
    addMenuItem(extras, new ProfileAction(this));

    extras.addSeparator();
    addMenuItem(extras, new HelpAction(this));
    addMenuItem(extras, new TipOfTheDayAction(this));
    extras.addSeparator();
    addMenuItem(extras, new InfoAction(this));

    return extras;
  }

  /**
   * @param font
   * @return the screen metrics of the specified font in the default toolkit
   * @deprecated As of Java 1.2, the Font method getLineMetrics should be used.
   * @see Toolkit#getFontMetrics(Font)
   */
  @Deprecated
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
     * Changes the active object
     *
     * @see magellan.client.event.SelectionListener#selectionChanged(magellan.client.event.SelectionEvent)
     */
    public void selectionChanged(SelectionEvent se) {
      if (se.isSingleSelection()) {
        activeObject = se.getActiveObject();
      } else {
        activeObject = null;
      }
      if (se.getActiveObject() instanceof Region) {
        getData().setActiveRegion((Region) se.getActiveObject());
      } else if (se.getActiveObject() instanceof HasRegion) {
        Region r = ((HasRegion) se.getActiveObject()).getRegion();
        if (r != null && r != getData().getNullRegion()) {
          getData().setActiveRegion(r);
        }
      }
    }

    /**
     * Bookmarks the active object.
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      if (activeObject != null) {
        bookmarkManager.toggleBookmark(activeObject);
      }
    }
  }

  /**
   * Adds a new menu item to the specify menu associating it with the specified action, setting its
   * mnemonic and registers its accelerator if it has one.
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

    @SuppressWarnings("unused")
    MenuActionObserver foo = new MenuActionObserver(item, action);

    return item;
  }

  private static class MessageWithLink extends JEditorPane {
    private static final long serialVersionUID = 1L;

    public MessageWithLink(String htmlBody) {
      super("text/html", "<html><body style=\"" + getStyle() + "\">" + htmlBody + "</body></html>");
      addHyperlinkListener(new HyperlinkListener() {
        @Override
        public void hyperlinkUpdate(HyperlinkEvent e) {
          if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
            try {
              if (e.getURL() != null) {
                Macifier.browse(e.getURL().toURI());
              }
            } catch (Exception ex) {
              log.warn(ex);
            }
          }
        }
      });
      setEditable(false);
      setBorder(null);
    }

    static StringBuffer getStyle() {
      // for copying style
      JLabel label = new JLabel();
      Font font = label.getFont();
      Color color = label.getBackground();

      // create some css from the label's font
      StringBuffer style = new StringBuffer("font-family:" + font.getFamily() + ";");
      style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
      style.append("font-size:" + font.getSize() + "pt;");
      style.append("background-color: rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ");");
      return style;
    }
  }

  /**
   * START &amp; END Code
   */
  public static void main(String args[]) {
    try {
      /* set the stderr to stdout while there is no log attached */
      System.setErr(System.out);
      Logger.activateDefaultLogListener(true);

      /* Fiete 20151017: keep the old sorting, needed for FFTools */
      System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

      // Fiete 20061208
      // set finalizer prio to max
      magellan.library.utils.MemoryManagment.setFinalizerPriority(Thread.MAX_PRIORITY);

      final Parameters parameters = parseCommandLine(args);

      /* determine default value for files directory */
      parameters.binDir = MagellanFinder.findMagellanDirectory();

      if (parameters.resourceDir == null) {
        parameters.resourceDir = parameters.binDir;
      }
      parameters.settingsDir = MagellanFinder.findSettingsDirectory(parameters.resourceDir,
          parameters.settingsDir);
      Resources.getInstance().initialize(parameters.resourceDir, "");
      MagellanLookAndFeel.setMagellanDirectory(parameters.resourceDir);
      MagellanImages.setMagellanDirectory(parameters.resourceDir);

      // initialize start window
      Icon startIcon = MagellanImages.ABOUT_MAGELLAN;

      Client.startWindow = new StartWindow(startIcon, 5, parameters.resourceDir);
      Client.startWindow.setVisible(true);
      Client.startWindow.progress(0, Resources.get("clientstart.0"));

      ProfileManager.init(parameters.settingsDir);
      if (parameters.profile != null) {
        ProfileManager.setProfile(parameters.profile);
      }

      if (ProfileManager.getCurrentProfile() == null || ProfileManager.getProfileDirectory() == null || ProfileManager
          .isAlwaysAsk()
          || parameters.startPM) {
        if (!ProfileManager.showProfileChooser(Client.startWindow)) {
          log.info("Abort requested by ProfileChooser");
          System.exit(0);
        } else {
          ProfileManager.saveSettings();
        }
      }
      parameters.settingsDir = ProfileManager.getProfileDirectory();
      if (parameters.help) {
        Properties settings = Client.loadSettings(parameters.settingsDir, SETTINGS_FILENAME);
        if (settings == null) {
          settings = Client.initNewSettings();
        }
        new Help(settings).showAndKeepAlive();
        Client.startWindow.setVisible(false);
        return;
      }

      // tell the user where we expect ini files and errors.txt
      PropertiesHelper.setSettingsDirectory(parameters.settingsDir);

      final File logFile = startLog(parameters);

      final File tBinDir = parameters.binDir;
      final File tResourceDir = parameters.resourceDir;
      final File tsettFileDir = ProfileManager.getProfileDirectory();
      final String tReport = parameters.report;

      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          try {
            // can't call loadRules from here, so we initially work with an
            // empty ruleset.
            // This is not very nice, though...
            GameData data = new MissingData();

            // new CompleteData(new com.eressea.rules.Eressea(), "void");
            Client c = new Client(data, tBinDir, tResourceDir, tsettFileDir, logFile);
            // setup a singleton instance of this client
            Client.INSTANCE = c;

            versionCheck(c, tBinDir, tResourceDir, tsettFileDir);

            c.macify();

            File crFile = null;

            if (tReport == null) {
              // if no report is given on startup, we check if we can load the last
              // loaded report.
              boolean loadLastReport = PropertiesHelper.getBoolean(c.getProperties(),
                  PropertiesHelper.CLIENTPREFERENCES_LOAD_LAST_REPORT, true);
              if (loadLastReport) {
                crFile = c.fileHistory.getLastExistingReport();
                if (crFile == null) {
                  // okay, ask for a file...
                  crFile = OpenCRAction.getFileFromFileChooser(c, Client.startWindow);
                }
              }
            } else {
              crFile = new File(tReport);
            }

            if (crFile != null) {
              Client.startWindow.progress(4, Resources.get("clientstart.4"));

              c.loadCRThread(crFile);
            }

            c.setReportChanged(false);

            Client.startWindow.progress(5, Resources.get("clientstart.5"));
            c.setAllVisible(true);
            Client.startWindow.setVisible(false);
            Client.startWindow.dispose();
            Client.startWindow = null;

            // show tip of the day window
            if (c.getProperties().getProperty("TipOfTheDay.showTips", "true").equals("true") || c
                .getProperties().getProperty("TipOfTheDay.firstTime", "true").equals("true")) {
              TipOfTheDay totd = new TipOfTheDay(c, c.getProperties());

              if (totd.doShow()) {
                // totd.setVisible(true);
                totd.showTipDialog();
                totd.showNextTip();
              }
            }
          } catch (Throwable t) {
            bailOut(t);
          }
        }

      });
    } catch (Throwable exc) { // any fatal error
      bailOut(exc);
    }
  }

  private static void versionCheck(Client c, File tBinDir, File tResourceDir, File tConfigDir) {
    try {
      boolean checkVersion = false;

      Install4J i4 = new Install4J(tBinDir, tConfigDir);

      if (i4.isActive()) {
        checkVersion = false;
        if (i4.isSetByInstaller()) {
          String schedule = i4.getUpdateSchedule();
          if (schedule != null) {
            boolean check = !schedule.equals(Install4J.CHECK_NEVER);
            log.fine("updated version check at start to " + check);
            PropertiesHelper.setBoolean(c.getProperties(), VersionInfo.PROPERTY_KEY_UPDATECHECK_CHECK, check);
          }
        }
      } else {
        log.info("Install4J configuration not found");
      }

      String currentVersion = VersionInfo.getSemanticVersion(tResourceDir);
      if (checkVersion) {
        String newestVersion = VersionInfo.getNewestVersion(c.getProperties(), checkVersion ? null
            : Client.startWindow);
        if (!Utils.isEmpty(newestVersion)) {
          Client.log.info("Newest Version on server: " + newestVersion);
          Client.log.info("Current Version: " + currentVersion);
          if (VersionInfo.isNewer(newestVersion, currentVersion)) {
            String url = MagellanUrl.getMagellanUrl(MagellanUrl.WWW_DOWNLOAD + "." + Locales
                .getGUILocale().getLanguage());
            if (url == null) {
              url = MagellanUrl.getMagellanUrl(MagellanUrl.WWW_DOWNLOAD);
            }

            JOptionPane.showMessageDialog(Client.startWindow,
                new MessageWithLink(Resources.get("client.new_version", new Object[] { newestVersion, url })),
                "",
                JOptionPane.INFORMATION_MESSAGE);
          }
        }
      }

      String lastVersion = c.getProperties().getProperty("Client.LastVersion");
      if (lastVersion == null || !lastVersion.equals(currentVersion)) {
        UpdateDialog dlg = new UpdateDialog(startWindow, lastVersion, currentVersion);
        dlg.setModalityType(ModalityType.TOOLKIT_MODAL);
        dlg.setVisible(true);
        if (!dlg.getResult()) {
          c.quit(false);
        }
      }
    } catch (Throwable e) {
      log.error("Could not check version.", e);
    }
  }

  protected static void bailOut(Throwable t) {
    Client.log.error("A fatal error occured: ", t); // print it so it can be written to errors.txt

    // try to create a nice output
    String out = "A fatal error occured: " + t.toString();

    JOptionPane.showMessageDialog(new JFrame(), out);
    System.exit(1);
  }

  /**
   * Open the log file and log basic information to it.
   *
   * @param parameters
   * @throws IOException if an I/O error occurs
   */
  protected static File startLog(Parameters parameters) throws IOException {
    // now redirect stderr through our log
    Log LOG = new Log(parameters.settingsDir);

    if (parameters.logLevel == null) {
      Properties settings = Client.loadSettings(parameters.settingsDir, Client.SETTINGS_FILENAME);
      if (settings != null) {
        String level = settings.getProperty("Client.logLevel");
        if (level != null) {
          Logger.setLevel(level);
          Client.log.info("Client.main (settings): Set logging to " + level);
        }
      }
    }

    // logging with level warning to get this information even if user selected low debug level...
    Logger.activateDefaultLogListener(true);
    Client.log.warn("Start writing error file with encoding " + LOG.getEncoding() + ", log level "
        + Logger.getLevel(Logger.getLevel()));

    Client.log.info("resource directory: " + parameters.resourceDir);
    Client.log.info("settings directory: " + parameters.settingsDir);

    String version = VersionInfo.getSemanticVersion(parameters.binDir);
    if (version == null) {
      version = VersionInfo.getVersion(parameters.resourceDir);
    }
    if (version == null) {
      Client.log.warn("no magellan version available");
    } else {
      Client.log.warn("This is Magellan Version " + version);
    }

    try {
      Client.log.warn("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch")
          + " " + System.getProperty("os.version"));
      Client.log.warn("Java Version: " + System.getProperty("java.version") + " " + System
          .getProperty("java.vendor"));
      Client.log.warn("Java Spec: " + System.getProperty("java.specification.version") + " "
          + System.getProperty("java.specification.vendor") + " " + System.getProperty(
              "java.specification.name"));
      Client.log.warn("VM Version: " + System.getProperty("java.vm.version") + " " + System
          .getProperty("java.vm.vendor") + " " + System.getProperty("java.vm.name"));
      Client.log.warn("VM Specification: " + System.getProperty("java.vm.specification.version")
          + " " + System.getProperty("java.vm.specification.vendor") + " " + System.getProperty(
              "java.vm.specification.name"));
      Client.log.warn("Java Class Version: " + System.getProperty("java.class.version"));
    } catch (SecurityException e) {
      Client.log.warn("Unable to retrieve system properties: " + e);
    }
    return LOG.getFile();
  }

  /**
   * Stores command line parameters
   */
  public static class Parameters {
    /** the program directory */
    public File binDir;
    /** the directory for resources (images, languages etc.) */
    public File resourceDir;
    /** the directory to store ini files and stuff in */
    public File settingsDir;
    /** the name of the profile */
    public String profile;
    /** the report to be loaded on startup */
    public String report;
    /** Indicates that the help option was given */
    public boolean help = false;
    /** Whether to show the profile manager dialog */
    public boolean startPM = false;
    /** manual log level */
    public String logLevel;
  }

  /**
   * Recognizes the following parameters:<br />
   * <kbd>-log X</kbd> -- set log level X<br />
   * <kbd>--help</kbd> -- start only help dialog<br />
   * <kbd>-d dir</kbd> -- set resource directory<br />
   * <kbd>-s dir</kbd> -- set settings (aka profiles) directory<br />
   * <kbd>-p profile</kbd> -- set profile<br />
   * <kbd>-pm</kbd> -- show profile manager<br />
   * <kbd>-s dir</kbd> -- set settings (aka profiles) directory<br />
   * <kbd>file.[cr|bz2|zip]</kbd> -- set report<br />
   *
   * @param args
   */
  protected static Parameters parseCommandLine(String[] args) {
    Parameters result = new Parameters();

    /* process command line parameters */
    for (int i = 0; i < args.length; ++i) {
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
          result.logLevel = level;
          Client.log.info("Client.main: Set logging to " + level);

          if ("A".equals(level)) {
            Client.log.awt("Start logging of awt events to awtdebug.txt.");
          }
        }
      } else if (args[i].equals("--help")) {
        result.help = true;
      } else if (args[i].equals("-d") && (args.length > (i + 1))) {
        i++;

        try {
          File tmpFile = new File(args[i]).getCanonicalFile();

          if (tmpFile.exists() && tmpFile.isDirectory() && tmpFile.canWrite()) {
            result.resourceDir = tmpFile;
          } else {
            Client.log.info("Client.main(): the specified files directory does not "
                + "exist, is not a directory or is not writeable.");
          }
        } catch (Throwable e) {
          Client.log.error("Client.main(): the specified files directory is invalid.", e);
        }
      } else if (args[i].equals("-s") && (args.length > (i + 1))) {
        i++;

        try {
          File tmpFile = new File(args[i]).getCanonicalFile();

          if (tmpFile.exists() && tmpFile.isDirectory() && tmpFile.canWrite()) {
            result.settingsDir = tmpFile;
          } else {
            Client.log.info("Client.main(): the specified files directory does not "
                + "exist, is not a directory or is not writeable.");
          }
        } catch (Throwable e) {
          Client.log.error("Client.main(): the specified files directory is invalid.", e);
        }
      } else if (args[i].equals("-p") && (args.length > (i + 1))) {
        i++;

        result.profile = args[i];
      } else if (args[i].equals("-pm")) {
        result.startPM = true;
      } else {
        if (args[i].toLowerCase().endsWith(".cr") || args[i].toLowerCase().endsWith(".bz2")
            || args[i].toLowerCase().endsWith(".zip")) {
          result.report = args[i];
        }
      }

    }
    return result;
  }

  /**
   * Asks the user whether the current report should be saved but does not do it!
   *
   * @return {@link JOptionPane#YES_OPTION} if the user selected to save the report,
   *         {@link JOptionPane#NO_OPTION} if the user selected not to save it,
   *         {@link JOptionPane#CANCEL_OPTION} if saving is not possible or the user selected to
   *         cancel the operation.
   */
  public int askToSave() {
    if (reportState.isStateChanged()) {
      String msg = null;

      try {
        if (getData() != null && getData().getFileType() != null && getData().getFileType()
            .getFile() != null) {
          Object msgArgs[] = { getData().getFileType().getFile().getAbsolutePath() };
          msg = (new MessageFormat(Resources.get("client.msg.quit.confirmsavefile.text"))).format(
              msgArgs);
        } else {
          msg = Resources.get("client.msg.quit.confirmsavenofile.text");
        }
      } catch (IOException io) {
        Client.log.error("", io);
        msg = Resources.get("client.msg.quit.confirmsavenofile.text");
      }

      switch (JOptionPane.showConfirmDialog(this, msg, Resources.get(
          "client.msg.quit.confirmsave.title"), JOptionPane.YES_NO_CANCEL_OPTION)) {
      case JOptionPane.YES_OPTION:

        return JOptionPane.YES_OPTION;

      case JOptionPane.CANCEL_OPTION:
        return JOptionPane.CANCEL_OPTION;
      }
    }

    return JOptionPane.NO_OPTION;
  }

  /**
   * Saves the current data and waits until saving is finished.
   *
   * @return <code>true</code> if data was successfully saved.
   */
  protected boolean saveSynchronously() {
    CRWriter crw = saveReport();
    if (crw == null)
      return false;
    try {
      while (crw.savingInProgress()) {
        Thread.sleep(500);
      }
    } catch (InterruptedException e) {
      log.info("save thread interrupted", e);
      Thread.currentThread().interrupt();
      return false;
    }
    return true;
  }

  /**
   * Tries to determine the correct file type for the current data and starts saving it, if
   * successful. Saving is done in a new thread.
   *
   * @return The writer that has started saving in another thread. You can check progress with
   *         {@link CRWriter#savingInProgress()}.
   */
  protected CRWriter saveReport() {
    FileType filetype = getData().getFileType();
    if (filetype == null) {
      File file = FileSaveAsAction.getFile(this);
      if (file != null) {
        try {
          filetype = FileTypeFactory.singleton().createFileType(file, false);
        } catch (IOException e) {
          Client.log.error("could not open " + file + " for saving: " + e);
          return null;
        }
      }
    }
    if (filetype == null) {
      Client.log.error("Could not determine file for saving");
      return null;
    }

    return saveReport(filetype);
  }

  /**
   * Starts saving the current data to the given filetype.
   *
   * @param filetype
   * @return The writer that has started saving in another thread. You can check progress with
   *         {@link CRWriter#savingInProgress()}.
   */
  public CRWriter saveReport(FileType filetype) {
    CRWriter crw = null;
    try {

      // log.info("debugging: doSaveAction (FileType) called for FileType: " + filetype.toString());
      // write cr to file
      Client.log.info("Client.saveReport Using encoding: " + getData().getEncoding());
      UserInterface ui = new ProgressBarUI(this);
      crw = new CRWriter(getData(), ui, filetype, getData().getEncoding(), Integer.parseInt(
          getProperties().getProperty("Client.CRBackups.count", FileBackup.DEFAULT_BACKUP_LEVEL
              + "")));
      crw.writeAsynchronously();
      crw.close();

      // everything worked fine, so reset reportchanged state and also store new FileType settings
      setReportChanged(false);
      getData().setFileType(filetype);

      // getData().resetToUnchanged();
      updateTitleCaption();
      getProperties().setProperty("Client.lastCRSaved", filetype.getName());
    } catch (ReadOnlyException exc) {
      Client.log.error(exc);
      JOptionPane.showMessageDialog(this, Resources.getFormatted(
          "actions.filesaveasaction.msg.filesave.readonly", filetype.getName()), Resources.get(
              "actions.filesaveasaction.msg.filesave.error.title"), JOptionPane.ERROR_MESSAGE);
    } catch (IOException exc) {
      Client.log.error(exc);
      JOptionPane.showMessageDialog(this, exc.toString(), Resources.get(
          "actions.filesaveasaction.msg.filesave.error.title"), JOptionPane.ERROR_MESSAGE);
    }
    return crw;
  }

  /**
   * This method should be called before the application is terminated in order to store GUI settings
   * etc.
   *
   * @param storeSettings store the settings to magellan.ini if <code>storeSettings</code> is
   *          <code>true</code>.
   */
  public void quit(final boolean storeSettings) {
    quit(new QuitListener() {

      public void performQuit() {
        log.fine("Exiting");
        System.exit(0);
      }

      public void cancelQuit() {
        // nop
      }
    }, storeSettings);
  }

  /**
   * Adapter to java.avt.QuitResponse
   */
  public interface QuitListener {
    /** Signal that quitting should be aborted */
    void cancelQuit();

    /** Signal that quitting can be performed */
    void performQuit();
  }

  public void quit(QuitListener ql, boolean storeSettings) {
    final ProgressBarUI ui = new ProgressBarUI(this);
    final int response;
    log.fine(new RuntimeException("quit was called from ..."));

    // ask to save report
    if (reportState != null && reportState.isStateChanged()) {
      response = askToSave();
    } else {
      response = JOptionPane.NO_OPTION;
    }

    if (response == JOptionPane.CANCEL_OPTION) {
      log.fine("Cancelling quit response");
      ql.cancelQuit();
      return;
    }

    new Thread(new Runnable() {
      public void run() {
        doQuit(storeSettings, response == JOptionPane.YES_OPTION, ui, ql);
      }
    }).start();

    log.fine("returning");
  }

  protected void doQuit(boolean storeSettings, boolean save, UserInterface ui, QuitListener ql) {
    log.fine("Closing down...");

    if (save) {
      ui.setProgress("saving", 1);
      if (!saveSynchronously()) {
        log.fine("Cancelling quit response");
        ql.cancelQuit();
        return;
      }
      log.fine("save returned");
    }

    ui.setProgress("plugins", 1);
    for (MagellanPlugIn plugIn : getPlugIns()) {
      plugIn.quit(storeSettings);
    }

    ui.setProgress("windows", 1);
    PropertiesHelper.saveRectangle(getProperties(), Client.this.getBounds(), "Client");
    saveExtendedState();
    setVisible(false);

    if (panels != null) {
      for (JPanel jPanel : panels) {
        InternationalizedDataPanel p = (InternationalizedDataPanel) jPanel;
        p.quit();
      }
    }

    if (getNameGenerator() != null) {
      getNameGenerator().quit();
    }

    if (fileHistory != null) {
      fileHistory.storeFileHistory();
    }

    ui.setProgress("settings", 1);
    // store settings to file
    if (storeSettings) {
      // save the desktop
      desktop.save();

      try {
        // if necessary, use settings file in local directory
        File settingsFile = new File(Client.getSettingsDirectory(), Client.SETTINGS_FILENAME);

        if (settingsFile.exists() && settingsFile.canWrite()) {
          try {
            File backup = FileBackup.create(settingsFile);
            Client.log.info("Created backupfile " + backup);
          } catch (IOException ie) {
            Client.log.warn("Could not create backupfile for file " + settingsFile);
          }
        }

        if (settingsFile.exists() && !settingsFile.canWrite())
          throw new IOException("cannot write " + settingsFile);
        else {
          Client.log.info("Storing Magellan configuration to " + settingsFile);

          getProperties().store(new FileOutputStream(settingsFile), "");
        }
      } catch (IOException ioe) {
        Client.log.error(ioe);
      }
    }
    log.fine("Telling requester to quit");
    ql.performQuit();
  }

  /**
   * Sets the name of the current loaded data file.
   */
  // public void setDataFile(File file) {
  // this.dataFile = file;
  // }
  /**
   * Returns the name of the current loaded data file. If the result is null - then this does not
   * mean, that there is no report loaded - but not correctly set...
   */

  // public File getDataFile() {
  // return dataFile;
  // }
  // //////////////////
  // GAME DATA Code //
  // //////////////////
  /**
   * Loads game data from a file and returns it.
   *
   * @param ui
   * @param fileName
   * @return the game data read or <code>null</code> if something went wrong
   */
  public GameData loadCR(UserInterface ui, File fileName) {
    GameData data = null;
    Client client = this;
    if (ui == null) {
      ui = new NullUserInterface();
      // ProgressBarUI(client);
    }

    try {
      ui.setMaximum(-1);
      ui.show();
      // FIXME(stm) maybe not pass ui to the reader here!?!
      data = new GameDataReader(ui).readGameData(FileTypeFactory.singleton().createFileType(
          fileName, true, new ClientFileTypeChooser(client)));
      if (data == null)
        throw new NullPointerException();
    } catch (FileTypeFactory.NoValidEntryException e) {
      ui.ready();
      JOptionPane.showMessageDialog(client, Resources.get("client.msg.loadcr.missingcr.text.1")
          + fileName + Resources.get("client.msg.loadcr.missingcr.text.2"), Resources.get(
              "client.msg.loadcr.error.title"), JOptionPane.ERROR_MESSAGE);
      return null;
    } catch (FileNotFoundException exc) {
      ui.ready();
      JOptionPane.showMessageDialog(client, Resources.get("client.msg.loadcr.error.text") + exc
          .toString(), Resources.get("client.msg.loadcr.error.title"), JOptionPane.ERROR_MESSAGE);
      Client.log.info(exc);
      return null;
    } catch (Throwable exc) {
      ui.ready();
      JOptionPane.showMessageDialog(client, Resources.get("client.msg.loadcr.error.text") + exc
          .toString(), Resources.get("client.msg.loadcr.error.title"), JOptionPane.ERROR_MESSAGE);
      Client.log.warn(exc);
      return null;
    }

    if (data.isOutOfMemory()) {
      JOptionPane.showMessageDialog(client, Resources.get("client.msg.outofmemory.text"), Resources
          .get("client.msg.outofmemory.title"), JOptionPane.ERROR_MESSAGE);
      Client.log.error(Resources.get("client.msg.outofmemory.text"));
    }
    if (!MemoryManagment.isFreeMemory(data.estimateSize())) {
      JOptionPane.showMessageDialog(client, Resources.get("client.msg.lowmem.text"), Resources.get(
          "client.msg.lowmem.title"), JOptionPane.WARNING_MESSAGE);
    }
    @SuppressWarnings("unused")
    int bE = 0, rE = 0, ruE = 0, sE = 0, uE = 0, mE = 0;
    for (Problem p : data.getErrors()) {
      if (p.getType() == GameDataInspector.GameDataProblemTypes.DUPLICATEREGIONID.type) {
        rE++;
        Region c = (Region) p.getObject();
        log.info("Problem: Duplicate Region ID: '" + p.getRegion().getName() + "' (" + p.getRegion()
            .getID() + ") <> '" + c.getName() + "' (" + c.getID() + ")");
      }
      if (p.getType() == GameDataInspector.GameDataProblemTypes.DUPLICATEREGIONUID.type) {
        ruE++;
        Region c = (Region) p.getObject();
        log.info("Problem: Duplicate Region UID: '" + p.getRegion().getName() + "' (" + p
            .getRegion().getID() + " - " + p.getRegion().getUID() + ") <> '" + c.getName() + "' ("
            + c.getID() + " - " + c.getUID() + ")");
      }
      if (p.getType() == GameDataInspector.GameDataProblemTypes.DUPLICATEBUILDINGID.type) {
        bE++;
      }
      if (p.getType() == GameDataInspector.GameDataProblemTypes.DUPLICATESHIPID.type) {
        sE++;
      }
      if (p.getType() == GameDataInspector.GameDataProblemTypes.DUPLICATEUNITID.type) {
        uE++;
      }
      if (p.getType() == GameDataInspector.GameDataProblemTypes.OUTOFMEMORY.type) {
        mE++;
      }
    }
    if (ruE > 0) {
      Client.log.error("report with errors: " + (rE + ruE) + " " + uE + " " + bE + " " + sE);
      if (JOptionPane.showConfirmDialog(client, Resources.get(
          "client.msg.reporterrors.text.question", fileName, rE + ruE, uE, bE, sE), Resources.get(
              "client.msg.reporterrors.title"),
          JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

        // merge report with itself to resolve wrapping

        UserInterface ui2;
        if (ui instanceof NullUserInterface) {
          ui2 = new NullUserInterface();
        } else {
          ui2 = new ProgressBarUI(this);
        }
        ui2.show();
        return data.repair(ui2);
      }
    } else if (bE > 0 || rE > 0 || sE > 0 || uE > 0) {
      Client.log.error("report with errors: " + rE + " " + uE + " " + bE + " " + sE);
      // JOptionPane.showMessageDialog(client, Resources.get("client.msg.reporterrors.text",
      // fileName,
      // rE, uE, bE, sE), Resources.get("client.msg.reporterrors.title"),
      // JOptionPane.WARNING_MESSAGE);
    }

    return data;
  }

  /**
   * This method asynchronously loads a CR into the client. Modality is ensured via a
   * {@link UserInterface}.
   *
   * @param fileName The file name to be loaded.
   */
  public void loadCRThread(final File fileName) {
    loadCRThread(false, fileName);
  }

  /**
   * This method asynchronously loads a CR into the client. Modality is ensured via a
   * {@link UserInterface}.
   *
   * @param saveFirst If <code>true</code>, this method attempts to first save the current data.
   * @param fileName The file name to be loaded.
   */
  public void loadCRThread(final boolean saveFirst, final File fileName) {

    final UserInterface ui = new ProgressBarUI(this);
    ui.show();

    new Thread(new Runnable() {
      public void run() {
        try {
          Client client = Client.INSTANCE;

          GameData data = null;

          // save old data
          if (!saveFirst || saveSynchronously()) {
            data = loadCR(ui, fileName);
          }

          if (data != null) {
            client.setData(data);
            client.setReportChanged(false);
            // client.setAdditionalIconInfo(data.getDate().getDate());
          }
        } finally {
          ui.ready();
        }
      }
    }, "loadCRThread").start();

    /* this is here just for debugging reasons */
    if (saveFirst && !saveFirst) {
      saveSynchronously();
    }
  }

  /**
   * Sets the origin of this client's data to newOrigin.
   *
   * @param newOrigin The region in the GameData that is going to be the new origin
   */
  public void setOrigin(CoordinateID newOrigin) {
    GameData newData = null;
    try {
      newData = getData().clone(newOrigin);
      if (newData == null)
        throw new NullPointerException();
      if (newData.isOutOfMemory()) {
        JOptionPane.showMessageDialog(this, Resources.get("client.msg.outofmemory.text"), Resources
            .get("client.msg.outofmemory.title"), JOptionPane.ERROR_MESSAGE);
        Client.log.error(Resources.get("client.msg.outofmemory.text"));
      }
    } catch (final CloneNotSupportedException e) {
      throw new IllegalStateException("implementation error");
    }
    if (!MemoryManagment.isFreeMemory(newData.estimateSize())) {
      JOptionPane.showMessageDialog(this, Resources.get("client.msg.lowmem.text"), Resources.get(
          "client.msg.lowmem.title"), JOptionPane.WARNING_MESSAGE);
    }

    // FIXME(stm) do not change on out of memory!?
    setData(newData);
    setReportChanged(true);
  }

  /**
   * Sets the girth of the world in all layers.
   *
   * @param newBorders
   */
  public void setGirth(BBoxes newBorders) {
    // TODO compare with known borders
    for (Integer layer : newBorders.getLayers()) {
      BBox box = newBorders.getBox(layer);
      if (box.getMinx() >= box.getMaxx()) {
        box.setX(Integer.MAX_VALUE, Integer.MIN_VALUE);
      }
      if (box.getMiny() >= box.getMaxy()) {
        box.setY(Integer.MAX_VALUE, Integer.MIN_VALUE);
      }
    }

    GameData newData = GameDataMerger.merge(getData(), new BoxTransformer(newBorders));
    if (newData == null)
      throw new NullPointerException();
    if (newData.isOutOfMemory()) {
      JOptionPane.showMessageDialog(this, Resources.get("client.msg.outofmemory.text"), Resources
          .get("client.msg.outofmemory.title"), JOptionPane.ERROR_MESSAGE);
      Client.log.error(Resources.get("client.msg.outofmemory.text"));
    }
    // FIXME(stm) do not change on out of memory!?
    setData(newData);
    setReportChanged(true);
  }

  /**
   * Callbacks of FileTypeFactory are handled by this object. Right now it returns the first ZipEntry
   * to mimic old cr loading behaviour for zip files.
   */
  private static class ClientFileTypeChooser extends FileTypeFactory.FileTypeChooser {
    Client client;

    /**
     * Creates a new ClientFileTypeChooser object.
     *
     * @param client the parent Client object
     */
    public ClientFileTypeChooser(Client client) {
      this.client = client;
    }

    /**
     * open selection window to choose a zipentry
     *
     * @see magellan.library.io.file.FileTypeFactory.FileTypeChooser#chooseZipEntry(java.util.zip.ZipEntry[])
     */
    @Override
    public ZipEntry chooseZipEntry(ZipEntry entries[]) {
      String stringEntries[] = new String[entries.length];

      for (int i = 0; i < entries.length; i++) {
        stringEntries[i] = entries[i].toString();
      }

      Object selected = JOptionPane.showInputDialog(client.getRootPane(), Resources.get(
          "client.msg.loadcr.multiplezipentries.text"), Resources.get(
              "client.msg.loadcr.multiplezipentries.title"), JOptionPane.QUESTION_MESSAGE, null,
          stringEntries, stringEntries[0]);

      if (selected == null)
        return null;

      for (ZipEntry entrie : entries) {
        if (selected.equals(entrie.toString()))
          return entrie;
      }

      return null;
    }
  }

  /**
   * Do some additional checks after loading a report.
   *
   * @param aData the currently loaded game data
   */
  private void postProcessLoadedCR(GameData aData) {
    // show a warning if no password is set for any of the privileged
    // factions
    boolean factionsWithoutPassword = true;

    if (aData != null) {
      if (aData.getFactions() == null || aData.getFactions().size() == 0) {
        factionsWithoutPassword = false;
      }
    }

    if ((aData != null) && (aData.getFactions() != null)) {
      boolean yesToAll = false, noToAll = false;
      for (Faction f : aData.getFactions()) {

        if (f.getPassword() == null) {
          // take password from settings but only if it is not an
          // empty string
          String pwd = getProperties().getProperty("Faction.password." + (f.getID()).intValue(),
              null);

          if ((pwd != null) && !pwd.equals("")) {
            f.setPassword(pwd);
          }
        }

        // now check whether this faction has a password and eventually
        // set Trustlevel
        if ((f.getPassword() != null) && !f.isTrustLevelSetByUser()) {
          f.setTrustLevel(TrustLevel.TL_PRIVILEGED);
        }

        if (f.getPassword() != null) {
          factionsWithoutPassword = false;
        }

        // check messages whether the password was changed
        if (f.getMessages() != null) {
          for (Message m : f.getMessages()) {

            // check message id (new and old)
            if ((m.getMessageType() != null) && (((m.getMessageType().getID())
                .intValue() == 1784377885) || ((m.getMessageType().getID()).intValue() == 19735))) {
              // this message indicates that the password has been
              // changed

              String value = m.getAttribute("value");

              // if the password in the message is valid and
              // does not match
              // the password already set anyway set it for
              // the faction and in the settings
              if (value != null) {
                String password = value;

                if (!password.equals("") && !password.equals(f.getPassword())) {
                  // ask user for confirmation to take new
                  // password from message
                  String oMessage = Resources.get(
                      "client.msg.postprocessloadedcr.acceptnewpassword.text", new Object[] { f
                          .toString(), password });
                  String oTitle = Resources.get(
                      "client.msg.postprocessloadedcr.acceptnewpassword.title");
                  String[] oOptions = { Resources.get("button.yes"), Resources.get("button.no"),
                      Resources.get("button.yestoall"), Resources.get("button.notoall") };
                  boolean usePasswd = yesToAll;
                  if (!noToAll && !yesToAll) {
                    int answer = JOptionPane.showOptionDialog(getRootPane(), oMessage, oTitle,
                        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, oOptions,
                        0);
                    switch (answer) {
                    case 0:
                      usePasswd = true;
                      break;
                    case 1:
                      usePasswd = false;
                      break;
                    case 2:
                      usePasswd = true;
                      yesToAll = true;
                      break;
                    case 3:
                      usePasswd = false;
                      noToAll = true;
                      break;
                    default:
                      usePasswd = false;
                      break;
                    }
                  }
                  if (!noToAll && usePasswd) {
                    f.setPassword(password);

                    if (!f.isTrustLevelSetByUser()) { // password set
                      f.setTrustLevel(TrustLevel.TL_PRIVILEGED);
                    }

                    factionsWithoutPassword = false;

                    if (getProperties() != null) {
                      getProperties().setProperty("Faction.password." + (f.getID()).intValue(), f
                          .getPassword());
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

      if (factionsWithoutPassword) { // no password set for any faction
        // okay, let's ask if there is a passwort to set.
        AskForPasswordDialog dialog = new AskForPasswordDialog(this, aData);
        dialog.setVisible(true);
      }

      // recalculate the status of regions - coastal or not?
      Regions.calculateCoastBorders(aData);

      // postProceddTheVoid moved to GameData.postProcess:
    }

    if (aData != null && !PropertiesHelper.getBoolean(getProperties(), "map.creating.void",
        false)) {
      aData.removeTheVoid();
    }
  }

  // ///////////////
  // UPDATE Code //
  // ///////////////
  private void updateTitleCaption() {
    String titleline;
    if (title == null) {
      title = new TitleLine();
    }

    try {
      titleline = title.createTitle(getData(), showStatus, true);
    } catch (Throwable e) {
      titleline = title.createTitle(getData(), showStatus, false);
      Client.log.error("createTitle failed!", e);
    }

    setTitle(titleline);
  }

  private class TitleLine {
    private String oldTitle1;
    private String oldTitle3;
    private Date date;
    private String oldTitle;

    public String createTitle(GameData data, boolean showStatusOverride, boolean longTitle) {
      // set frame title (date)
      StringBuilder title1 = new StringBuilder(reportState.isStateChanged() ? "*" : "").append(
          "Magellan");
      StringBuilder title2 = new StringBuilder();
      StringBuilder title3 = new StringBuilder();

      String version = VersionInfo.getSemanticVersion(Client.getBinaryDirectory());
      if (version == null) {
        version = VersionInfo.getSemanticVersion(Client.getResourceDirectory());
      }

      if (version != null) {
        title1.append(" ").append(version);
      }

      // pavkovic 2002.05.7: data may be null in this situation
      if (data != null) {
        if (data.getFileType() != null) {
          String file;

          try {
            file = data.getFileType().getFile().toString();
          } catch (IOException e) {
            file = data.getFileType().toString();
          }

          file = file.substring(file.lastIndexOf(File.separator) + 1);
          title1.append(" [").append(file).append("]");
        }

        if (data.getOwnerFaction() != null) {
          title1.append(" - ").append(data.getOwnerFaction().toString());
        }

        if (data.getDate() != null) {
          title2.append(" - ").append(data.getDate().toString(showStatusOverride ? Date.TYPE_SHORT
              : Date.TYPE_PHRASE_AND_SEASON)).append(" (").append(data.getDate().getDate()).append(
                  ")");
        }

        if (longTitle) {
          if (showStatusOverride) {
            int units = 0;
            int done = 0;

            for (Unit u : data.getUnits()) {
              if (TrustLevels.isPrivileged(u.getFaction())) {
                units++;

                if (u.isOrdersConfirmed()) {
                  done++;
                }
              }

              // also count temp units
              for (TempUnit tempUnit : u.tempUnits()) {
                Unit u2 = tempUnit;

                if (TrustLevels.isPrivileged(u2.getFaction())) {
                  units++;

                  if (u2.isOrdersConfirmed()) {
                    done++;
                  }
                }
              }
            }

            if (units > 0) {
              BigDecimal percent = (new BigDecimal((done * 100) / ((float) units))).setScale(2,
                  RoundingMode.DOWN);
              title3.append(" (").append(units).append(" ").append(Resources.get(
                  "client.title.unit")).append(", ").append(done).append(" ").append(Resources.get(
                      "client.title.done")).append(", ").append(Resources.get(
                          "client.title.thatare")).append(" ").append(percent).append(" ").append(
                              Resources.get("client.title.percent")).append(")");
            }
          }

        }
      }

      // this prevents that the title "flickers" when it changes too often
      if (!title1.toString().equals(oldTitle1) || ((data == null || data.getDate() == null)
          ? date != null : !data.getDate().equals(date)) || !title3.toString().equals(oldTitle3)) {
        date = data != null ? data.getDate() : null;
        oldTitle1 = title1.toString();
        oldTitle3 = title3.toString();
        oldTitle = title1.append(title2).append(title3).toString();
      }

      return oldTitle;
    }
  }

  /**
   * Updates the order confirmation menu after the game data changed.
   */
  private void updateConfirmMenu() {
    refillChangeFactionConfirmation(factionOrdersMenu,
        ChangeFactionConfirmationAction.SETCONFIRMATION);
    refillChangeFactionConfirmation(factionOrdersMenuNot,
        ChangeFactionConfirmationAction.REMOVECONFIRMATION);
    refillChangeFactionConfirmation(invertAllOrdersConfirmation,
        ChangeFactionConfirmationAction.INVERTCONFIRMATION);
  }

  /**
   * Updates the plugins after GameData Change
   */
  private void updatePlugIns() {
    if (plugIns != null && plugIns.size() > 0) {
      for (MagellanPlugIn plugIn : plugIns) {
        try {
          plugIn.init(getData());
        } catch (Throwable t) {
          ErrorWindow errorWindow = new ErrorWindow(this, t.getMessage(), "", t);
          errorWindow.setVisible(true);
        }
      }
    }
  }

  /**
   * Called after GameData changes. Also called via EventDispatcher thread to ensure graphical changes
   * do occur.
   */
  private void updatedGameData() {
    updateConfirmMenu();
    updatePlugIns();
    updateTitleCaption();

    if (getData().getCurTempID() == -1) {
      String s = getProperties().getProperty("ClientPreferences.TempIDsInitialValue", "");

      try {
        getData().setCurTempID("".equals(s) ? 0 : Integer.parseInt(s, getData().base));
      } catch (java.lang.NumberFormatException nfe) {
        // do nothing
      }
    }

    // getDispatcher().fire(new GameDataEvent(this, getData(), true));

    if (getSelectedObjects() != null) {
      getDispatcher().fire(getSelectedObjects());
    }
    // if we have active Region, center on it
    Region activeRegion = getData().getActiveRegion();
    if (activeRegion != null) {
      getDispatcher().fire(SelectionEvent.create(this, activeRegion));
    } else {
      // suggestion by enno...if we have no active region but we have 0,0..center on 0,0
      CoordinateID cID = CoordinateID.ZERO;
      activeRegion = getData().getRegion(cID);
      if (activeRegion != null) {
        getDispatcher().fire(SelectionEvent.create(this, activeRegion));
      }
    }

    // also inform system about the new selection found in the GameData
    // object
    getDispatcher().fire(SelectionEvent.create(this, getData().getSelectedRegionCoordinates()
        .values()));
  }

  // ////////////
  // L&F Code //
  // ////////////
  /**
   * @param laf
   */
  public void setLookAndFeel(String laf) {
    setLookAndFeel(laf, false);
  }

  /**
   * @param laf
   * @param force
   */
  public void setLookAndFeel(String laf, boolean force) {
    LookAndFeel lafSet;

    float fScale = PropertiesHelper.getFloat(getProperties(), "Client.FontScale", 1.0f);

    if (!force && MagellanLookAndFeel.equals(laf) && laf.equals(getProperties().getProperty(
        PropertiesHelper.CLIENT_LOOK_AND_FEEL, ""))) {
      lafSet = null;
    } else {
      lafSet = MagellanLookAndFeel.setLookAndFeel(laf);
      if (lafSet == null) {
        log.warn("Could not set LaF to " + laf);
      }

      for (String fLaf : Client.FALLBACK_LAF) {
        if (lafSet == null) {
          lafSet = MagellanLookAndFeel.setLookAndFeel(fLaf);
        }
      }
    }

    if (lafSet == null)
      return;

    if (lafSet instanceof MetalLookAndFeel) {
      MagellanLookAndFeel.loadBackground(getProperties());
    }

    setFontSize(fScale);
    // FIXME we set laf and font size twice in order to update everything reliably. Seems like a hack.
    MagellanLookAndFeel.setLookAndFeel(lafSet.getName());
    setFontSize(fScale);
    updateLaF();

    getProperties().setProperty(PropertiesHelper.CLIENT_LOOK_AND_FEEL, laf);
  }

  /**
   * Returns current LaF
   */
  public String getLookAndFeel() {
    return getProperties().getProperty(PropertiesHelper.CLIENT_LOOK_AND_FEEL, Client.DEFAULT_LAF);
  }

  /**
   * Applies current LaF to main window.
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
    setLookAndFeel(getProperties().getProperty(PropertiesHelper.CLIENT_LOOK_AND_FEEL,
        Client.DEFAULT_LAF));
  }

  /**
   * DOCUMENT-ME
   */
  public String[] getLookAndFeels() {
    return MagellanLookAndFeel.getLookAndFeelNames().toArray(new String[] {});
  }

  // ///////////////////
  // HISTORY methods //
  // ///////////////////

  /**
   * Adds a single file to the file history.
   *
   * @param f
   */
  public void addFileToHistory(File f) {
    fileHistory.addFileToHistory(f);
  }

  /**
   * Returns the maximum number of entries in the history of loaded files.
   */
  public int getMaxFileHistorySize() {
    return fileHistory.getMaxFileHistorySize();
  }

  /**
   * Allows to set the maximum number of files appearing in the file history.
   *
   * @param size
   */
  public void setMaxFileHistorySize(int size) {
    fileHistory.setMaxFileHistorySize(size);
  }

  // ///////////////////
  // PROPERTY Access //
  // ///////////////////
  /**
   * Changes to the report state can be done here. Normally, a change is recognized by the following
   * events.
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
   * Returns true if the report has changed since last save/load.
   */
  public boolean isReportChanged() {
    return reportState.isStateChanged();
  }

  /**
   * Get the selected Regions. The returned map can be empty but is never null. This is a wrapper
   * function so we dont need to give away MapperPanel.
   */
  public Map<CoordinateID, Region> getSelectedRegions() {
    return mapPanel.getSelectedRegions();
  }

  /**
   * Get the Level on the mapper Panel. This is a wrapper function so we dont need to give away
   * MapperPanel.
   */
  public int getLevel() {
    return mapPanel.getLevel();
  }

  // //////////////////////////
  // GENERAL ACCESS METHODS //
  // //////////////////////////

  /**
   * Returns the global settings used by Magellan.
   */
  public Properties getProperties() {
    if (context == null)
      return null;
    return context.getProperties();
  }

  /**
   * Sets a new GameData and notifies all game data listeners.
   *
   * @param newData
   */
  public void setData(GameData newData) {
    log.fine("setData " + (newData.getFileType() != null ? newData.getFileType().getName()
        : "???"));
    context.setGameData(newData);
    postProcessLoadedCR(newData);
    log.fine("fire(GameDataEvent)");
    getDispatcher().fire(new GameDataEvent(this, getData(), true));
  }

  /**
   * Returns the current GameData.
   */
  public GameData getData() {
    return context.getGameData();
  }

  /**
   *
   */
  public MagellanDesktop getDesktop() {
    return desktop;
  }

  /**
   * Returns the current event dispatcher.
   */
  public EventDispatcher getDispatcher() {
    return context.getEventDispatcher();
  }

  /**
   * Returns the directory where the binaries are.
   */
  public static File getBinaryDirectory() {
    return Client.binDirectory;
  }

  /**
   * Returns the directory the local copy of Magellan is inside. Usually identical to binDirectory,
   * but can be used to load texts, images and the like from elsewhere.
   *
   * @deprecated Use {@link #getResourceDirectory()}
   */
  @Deprecated
  public static File getMagellanDirectory() {
    return Client.resourceDirectory;
  }

  /**
   * Returns the directory for the Magellan resources. Usually identical to binDirectory, but can be
   * used to load texts, images and the like from elsewhere.
   */
  public static File getResourceDirectory() {
    return Client.resourceDirectory;
  }

  /**
   * Returns the directory for the Magellan settings.
   */
  public static File getSettingsDirectory() {
    return Client.settingsDirectory;
  }

  public NameGenerator getNameGenerator() {
    if (generator == null) {
      generator = new MarkovNameGenerator(getProperties(), getSettingsDirectory());
    }
    return generator;
  }

  /**
   * Returns the value of logFile.
   *
   * @return Returns logFile.
   */
  public static File getLogFile() {
    return logFile;
  }

  /**
   * @return the BookmarkManager associated with this Client-Object
   */
  public BookmarkManager getBookmarkManager() {
    return bookmarkManager;
  }

  /**
   * Returns <code>true</code> if order status should be shown in title.
   */
  public boolean isShowingStatus() {
    return showStatus;
  }

  /**
   * Changes the progress display behaviour.
   *
   * @param bool If <code>true</code>, the progress is shown in the window title
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
    if (getProperties() == null)
      return;
    getProperties().setProperty("Client.extendedState", String.valueOf(getExtendedState()));
  }

  private void resetExtendedState() {
    int state = PropertiesHelper.getInteger(getProperties(), "Client.extendedState", -1);

    if (state != -1) {
      if (!getToolkit().isFrameStateSupported(state)) {
        log.warn("unsupported state " + state);
        getProperties().setProperty("Client.extendedState", String.valueOf(Frame.NORMAL));
        state = Frame.NORMAL;
      } else {
        setExtendedState(state);
      }
    }

  }

  // The repaint functions are overwritten to repaint the whole Magellan
  // Desktop. This is necessary because of the desktop mode FRAME.
  /**
   * @see java.awt.Component#repaint()
   */
  @Override
  public void repaint() {
    super.repaint();

    if (desktop != null) {
      desktop.repaintAllComponents();
    }
  }

  /**
   * Repaints all components.
   *
   * @param millis maximium time in milliseconds before update
   * @see Component#repaint(long)
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
   */
  public Iterator<KeyStroke> getShortCuts() {
    return null; // not used - we register directly with a KeyStroke
  }

  /**
   * Repaints the client.
   *
   * @see magellan.client.desktop.ShortcutListener#shortCut(javax.swing.KeyStroke)
   */
  public void shortCut(javax.swing.KeyStroke shortcut) {
    desktop.repaintAllComponents();
  }

  /**
   * @see magellan.client.desktop.ShortcutListener#getShortcutDescription(javax.swing.KeyStroke)
   */
  public String getShortcutDescription(KeyStroke stroke) {
    return Resources.get("client.shortcut.description");
  }

  /**
   * @see magellan.client.desktop.ShortcutListener#getListenerDescription()
   */
  public String getListenerDescription() {
    return Resources.get("client.shortcut.title");
  }

  /**
   * Returns an adapter for the preferences of this class.
   */
  public PreferencesAdapter createPreferencesAdapter() {
    return new ClientPreferences(getProperties(), this);
  }

  // /////////////////
  // INNER Classes //
  // /////////////////
  private static class MenuActionObserver implements PropertyChangeListener {
    protected JMenuItem item;

    /**
     * Creates a new MenuActionObserver object.
     *
     * @param item DOCUMENT-ME
     * @param action DOCUMENT-ME
     */
    public MenuActionObserver(JMenuItem item, Action action) {
      this.item = item;
      action.addPropertyChangeListener(this);
    }

    /**
     * DOCUMENT-ME
     *
     * @param e DOCUMENT-ME
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
  protected class ReportObserver implements GameDataListener, OrderConfirmListener,
      TempUnitListener, UnitOrdersListener {
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
     * Returns <code>true</code> if the report was changed.
     */
    public boolean isStateChanged() {
      return stateChanged;
    }

    /**
     * Changes the state to <code>newState</code>.
     */
    public void setStateChanged(boolean newState) {
      stateChanged = newState;

      if (!newState) {
        lastClear = System.currentTimeMillis();
      }
      updateTitleCaption();
    }

    /**
     * Sets the state to changed if the event occurred after the last clear event.
     *
     * @see magellan.client.event.OrderConfirmListener#orderConfirmationChanged(magellan.client.event.OrderConfirmEvent)
     */
    public void orderConfirmationChanged(OrderConfirmEvent e) {
      if ((getData() != null) && isShowingStatus()) {
        updateTitleCaption();
      }

      if (lastClear < e.getTimestamp()) {
        stateChanged = true;
        updateTitleCaption();
      }
    }

    /**
     * Sets the state to changed if the event occurred after the last clear event.
     *
     * @see magellan.client.event.TempUnitListener#tempUnitCreated(magellan.client.event.TempUnitEvent)
     */
    public void tempUnitCreated(TempUnitEvent e) {
      if (lastClear < e.getTimestamp()) {
        stateChanged = true;
        updateTitleCaption();
      }
    }

    /**
     * Sets the state to changed if the event occurred after the last clear event.
     *
     * @see magellan.client.event.TempUnitListener#tempUnitDeleting(magellan.client.event.TempUnitEvent)
     */
    public void tempUnitDeleting(TempUnitEvent e) {
      if (lastClear < e.getTimestamp()) {
        stateChanged = true;
        updateTitleCaption();
      }
    }

    /**
     * Updates the caption and sets changed state if the event occurred after the last call of
     * <code>setChangedState(false)</code>.
     *
     * @see magellan.library.event.GameDataListener#gameDataChanged(magellan.library.event.GameDataEvent)
     */
    public void gameDataChanged(GameDataEvent e) {
      if ((lastClear < e.getTimestamp()) && (e.getGameData() != null)) {
        stateChanged = true;
      } else {
        stateChanged = false;
      }
      updateTitleCaption();
      updatedGameData();
    }

    /**
     * Sets the state to changed if the event occurred after the last clear event.
     *
     * @see magellan.client.event.UnitOrdersListener#unitOrdersChanged(magellan.client.event.UnitOrdersEvent)
     */
    public void unitOrdersChanged(UnitOrdersEvent e) {
      if (lastClear < e.getTimestamp()) {
        if (!stateChanged) {
          stateChanged = true;
          updateTitleCaption();
        } else {
          stateChanged = true;
        }
      }
    }
  }

  /**
   * Returns all currenty selected objects.
   */
  public SelectionEvent getSelectedObjects() {
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
    // helper: store Magellan-Dir in properties toBe changed
    properties.setProperty("plugin.helper.bindir", Client.getBinaryDirectory().toString());
    properties.setProperty("plugin.helper.resourcedir", Client.getResourceDirectory().toString());
    List<Class<MagellanPlugIn>> plugInClasses = new ArrayList<Class<MagellanPlugIn>>(loader
        .getExternalModuleClasses(properties));
    Collections.sort(plugInClasses, new Comparator<Class<MagellanPlugIn>>() {

      public int compare(Class<MagellanPlugIn> o1, Class<MagellanPlugIn> o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });

    for (Class<MagellanPlugIn> plugInClass : plugInClasses) {
      try {
        Constructor<MagellanPlugIn> constructor = plugInClass.getConstructor();
        try {
          if (!constructor.trySetAccessible())
            return;
        } catch (NoSuchMethodError e) {
          // must be pre java 9, this is fine
        }

        MagellanPlugIn plugIn = constructor.newInstance();

        plugIn.init(this, properties);
        plugIns.add(plugIn);
      } catch (Throwable t) {
        ErrorWindow errorWindow = new ErrorWindow(this, t);
        errorWindow.setVisible(true);
      }
    }
  }

  /**
   * Returns a String representing all parts of the component for debugging.
   */
  public static String debug(Component comp) {
    StringBuilder result = new StringBuilder();
    if (comp instanceof Container) {
      Container container = (Container) comp;
      result.append("Container: ").append(container).append("\n");
      result.append("{");
      Component[] comps = container.getComponents();
      for (Component acomp : comps) {
        result.append(" ").append(Client.debug(acomp)).append("\n");
      }
      result.append(")");
    } else {
      result.append("Component: ").append(comp).append("\n");
    }
    return result.toString();
  }

  /**
   * on windows-OS tries to locate the included ECheck.exe and if found save the path into properties
   */
  public static void initECheckPath(Properties settings) {
    // check if we have a windows os
    String osName = System.getProperty("os.name");
    osName = osName.toLowerCase();
    if (osName.indexOf("windows") > -1) {
      Client.log.info("Windows OS detected (" + osName + ").");
      addEcheckFile(settings, new String[] { "ECheck.exe", "echeck.exe" });
    } else if (osName.indexOf("linux") > -1) {
      Client.log.info("Linux OS detected (" + osName + ").");
      addEcheckFile(settings, new String[] { "echeck" });
    } else if (osName.indexOf("mac") > -1) {
      Client.log.info("Mac OS detected (" + osName + ").");
      addEcheckFile(settings, new String[] { "echeck.macos" });
    } else {
      Client.log.info("Unknown OS detected (" + osName + ").");
    }
  }

  private static void addEcheckFile(Properties settings, String[] strings) {
    File echeckFile = null;
    for (String name : strings) {
      echeckFile = new File(new File(Client.getResourceDirectory(), "echeck"), name);
      Client.log.info("checking for ECheck: " + echeckFile);
      if (echeckFile.exists()) {
        break;
      } else {
        echeckFile = null;
      }
    }
    if (echeckFile != null) {
      settings.setProperty("JECheckPanel.echeckEXE", echeckFile.toString());
      Client.log.info("set echeckEXE to: " + echeckFile.toString());
    } else {
      Client.log.info("ECheck.exe not found");
    }
  }

  private void macify() {
    macifier = new Macifier(this);

    log.info(macifier.isDesktopSupported() ? "Desktop supported" : "Desktop not supported");
  }

  /**
   * Display the preferences dialog.
   * 
   * @return if the dialog was shown.
   */
  public boolean showPreferences() {
    optionAction.menuActionPerformed(null);
    return true;
  }

  /**
   * Display the InfoDialog.
   * 
   * @return true if the dialog was shown.
   */
  public boolean showInfoDialog() {
    new InfoDialog(Client.INSTANCE).setVisible(true);
    return true;
  }

  /**
   * Adds a new inspector to the taskpanel
   */
  public void addInspector(Inspector inspector) {
    if (taskPanel != null) {
      taskPanel.addInspector(inspector);
    }
  }

  /**
   * Adds a new inspector interceptor to the taskpanel inspectors
   */
  public void addInspectorInterceptor(InspectorInterceptor interceptor) {
    if (taskPanel != null) {
      taskPanel.addInspectorInterceptor(interceptor);
    }
  }

}
