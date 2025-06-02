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

package magellan.library.utils;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.KeyStroke;

import magellan.library.utils.logging.Logger;

/**
 * This class logically accesses values in a given Properties object
 */
public class PropertiesHelper {
  /** logging instance */
  private static final Logger log = Logger.getInstance(PropertiesHelper.class);

  /** Property type none: Prefix for advanced shape renderer settings */
  public static final String ADVANCEDSHAPERENDERER = "AdvancedShapeRenderer";

  /** Property type String: Postfix for advanced share renderer color settings */
  public static final String ADVANCEDSHAPERENDERER_COLORS = ".Colors";

  /** Property type String: Postfix for advanced share renderer current settings */
  public static final String ADVANCEDSHAPERENDERER_CURRENT = ".Cur";

  /** Property type String: Postfix for advanced share renderer maximum settings */
  public static final String ADVANCEDSHAPERENDERER_MAXIMUM = ".Max";

  /** Property type String: Postfix for advanced share renderer minimum settings */
  public static final String ADVANCEDSHAPERENDERER_MINIMUM = ".Min";

  /** Property type String: Postfix for advanced share renderer values settings */
  public static final String ADVANCEDSHAPERENDERER_VALUES = ".Values";

  /** Property type String: Name of current advanced shape renderer set */
  public static final String ADVANCEDSHAPERENDERER_S_CURRENT_SET = ".CurrentSet";

  /** Property type List&lt;String>: List of all available advanced shape renderers */
  public static final String ADVANCEDSHAPERENDERER_S_SETS = ".Sets";

  /** Property type String: ... */
  public static final String ADVANCEDSHAPERENDERER_TOOLTIP = ".Tooltip";

  /** Property type String: ... */
  public static final String ADVANCEDSHAPERENDERER_UNKNOWN = ".Unknown";

  /** */
  public static final String ATR_CURRENT_SET = "ATR.CurrentSet";

  /** */
  public static final String ATR_HORIZONTAL_ALIGN = "ATR.horizontalAlign";

  /** */
  public static final String ATR_SETS = "ATR.Sets";

  /** */
  public static final String AUTOCOMPLETION_COMPLETION_GUI = "AutoCompletion.CompletionGUI";

  /** Property type boolean: */
  public static final String AUTOCOMPLETION_EMPTY_STUB_MODE = "AutoCompletion.EmptyStubMode";

  /** Property type boolean: */
  public static final String AUTOCOMPLETION_HOTKEY_MODE = "AutoCompletion.HotKeyMode";

  /** Property type boolean: */
  public static final String AUTOCOMPLETION_ENABLED = "AutoCompletion.Enabled";

  /** */
  public static final String AUTOCOMPLETION_KEYS_BREAK = "AutoCompletion.Keys.Break";

  /** */
  public static final String AUTOCOMPLETION_KEYS_START = "AutoCompletion.Keys.Start";

  /** */
  public static final String AUTOCOMPLETION_KEYS_COMPLETE = "AutoCompletion.Keys.Complete";

  /** Property type number: */
  public static final String AUTOCOMPLETION_KEYS_CYCLE_BACKWARD = "AutoCompletion.Keys.Cycle.Backward";

  /** Property type number: */
  public static final String AUTOCOMPLETION_KEYS_CYCLE_FORWARD = "AutoCompletion.Keys.Cycle.Forward";

  /** */
  public static final String AUTOCOMPLETION_LIMIT_MAKE_COMPLETION = "AutoCompletion.limitMakeCompletion";

  /** Property type number: */
  public static final String AUTOCOMPLETION_SELF_DEFINED_COMPLETIONS_COUNT =
      "AutoCompletion.SelfDefinedCompletions.count";

  /** Property type number: */
  public static final String AUTOCOMPLETION_TIME = "AutoCompletion.ActivationTime";

  /** Property type boolean: */
  public static final String BORDERCELLRENDERER_USE_SEASON_IMAGES = "BoderCellRenderer.useSeasonImages";

  /** Property type String: */
  public static final String CELLRENDERER_CUSTOM_STYLESETS = "CellRenderer.CustomStylesets";

  /** Property type String: */
  public static final String CELLRENDERER_EMPHASIZE_STYLE = "CellRenderer.Emphasize.Style";

  /** Property type boolean: */
  public static final String CELLRENDERER_SHOW_TOOLTIPS = "CellRenderer.ShowToolTips";

  /** Property type String: */
  public static final String CELLRENDERER_SKILL_ICON_TEXT_COLOR_MAP = "CellRenderer.SkillIconTextColorMap";

  /** Property type List&lt;String>: */
  public static final String CELLRENDERER_STYLESETS = "CellRenderer.Stylesets.";

  /** Property type String: */
  public static final String CLIENT_LOOK_AND_FEEL = "Client.lookAndFeel";

  /** Property type boolean: If true, don't show tabs in docking layout. */
  public static final String CLIENTPREFERENCES_DONT_SHOW_TABS = "ClientPreferences.dontShowTabs";

  /** Property type boolean: If true, show workspace chooser */
  public static final String DESKTOP_ENABLE_WORKSPACE_CHOOSER = "Desktop.EnableWorkSpaceChooser";

  /** Property type String: Name of the used Splitset */
  public static final String DESKTOP_SPLITSET = "Desktop.SplitSet";

  /** Property type String: Name of the used desktop type (SPLIT) */
  public static final String DESKTOP_TYPE = "Desktop.Type";

  /** Property type Color (#RRGGBB): background color for event messages */
  public static final String MESSAGETYPE_SECTION_EVENTS_COLOR = "messagetype.section.events.color";

  /** Property type Color (#RRGGBB): background color for movement messages */
  public static final String MESSAGETYPE_SECTION_MOVEMENTS_COLOR = "messagetype.section.movement.color";

  /** Property type Color (#RRGGBB): background color for economy messages */
  public static final String MESSAGETYPE_SECTION_ECONOMY_COLOR = "messagetype.section.economy.color";

  /** Property type Color (#RRGGBB): background color for magic messages */
  public static final String MESSAGETYPE_SECTION_MAGIC_COLOR = "messagetype.section.magic.color";

  /** Property type Color (#RRGGBB): background color for study messages */
  public static final String MESSAGETYPE_SECTION_STUDY_COLOR = "messagetype.section.study.color";

  /** Property type Color (#RRGGBB): background color for production messages */
  public static final String MESSAGETYPE_SECTION_PRODUCTION_COLOR = "messagetype.section.production.color";

  /** Property type Color (#RRGGBB): background color for error messages */
  public static final String MESSAGETYPE_SECTION_ERRORS_COLOR = "messagetype.section.errors.color";

  /** Property type Color (#RRGGBB): background color for battle messages */
  public static final String MESSAGETYPE_SECTION_BATTLE_COLOR = "messagetype.section.battle.color";

  /** Property type Color (#RRGGBB): background color for battle messages */
  public static final String MESSAGETYPE_SECTION_UNKNOWN_COLOR = "messagetype.section.unknown.color";

  public static final String CLIENTPREFERENCES_LOAD_LAST_REPORT = "ClientPreferences.LoadLastReport";

  public static final String CLIENT_LAST_CR_ADDED = "Client.lastCRAdded";

  public static final String CLIENT_LAST_SELECTED_ADD_CR_FILEFILTER = "Client.lastSelectedAddCRFileFilter";

  public static final String CLIENT_LAST_SELECTED_ADD_CR_FILEFILTER_ID = "Client.lastSelectedAddCRFileFilterId";

  public static final String CLIENT_LAST_SELECTED_OPEN_CR_FILEFILTER = "Client.lastSelectedOpenCRFileFilter";

  public static final String CLIENT_LAST_SELECTED_OPEN_CR_FILEFILTER_ID = "Client.lastSelectedOpenCRFileFilterId";

  public static final String BUILDINGRENDERER_RENDER = "BuildingTypeRenderer.Render.";

  /** prefix of OrderWriter's property, not a property itself */
  public static final String ORDEREDITOR_PREFIX = "OrderWriter.";

  public static final String ORDERWRITER_MAILSERVER_HOST = "OrderWriter.mailServer";

  public static final String ORDERWRITER_MAILSERVER_PORT = "OrderWriter.mailServerPort";

  public static final String ORDERWRITER_MAILSERVER_USERNAME = "OrderWriter.serverUsername";

  public static final String ORDERWRITER_MAILSERVER_PASSWORD = "OrderWriter.serverPassword";

  public static final String ORDERWRITER_MAILSERVER_PASSWORD_ENCRYPTED = "OrderWriter.serverPassword.encrypted";

  public static final String ORDERWRITER_MAILSERVER_ASKPWD = "OrderWriter.askPassword";

  public static final String ORDERWRITER_MAILSERVER_USEAUTH = "OrderWriter.useAuth";

  public static final String ORDERWRITER_MAILSERVER_RECIPIENT = "OrderWriter.mailRecipient";

  public static final String ORDERWRITER_MAILSERVER_RECIPIENT2 = "OrderWriter.mailRecipient2";

  public static final String ORDERWRITER_MAILSERVER_SENDER = "OrderWriter.mailSender";

  public static final String ORDERWRITER_MAILSERVER_SUBJECT = "OrderWriter.mailSubject";

  public static final String ORDERWRITER_MAILSERVER_USE_CR_SETTINGS = "OrderWriter.useSettingsFromCr";

  public static final String ORDERWRITER_MAILSERVER_CC2SENDER = "OrderWriter.CCToSender";

  public static final String ORDERWRITER_MAILSERVER_SSL = "OrderWriter.encryptionSSL";

  public static final String ORDERWRITER_MAILSERVER_TLS = "OrderWriter.encryptionTLS";

  public static final String ORDERWRITER_OUTPUT_FILE = "OrderWriter.outputFile";

  public static final String ORDERWRITER_AUTO_FILENAME = "OrderWriter.autoFileName";

  public static final String ORDERWRITER_SERVER_URL = "OrderWriter.serverLocation";

  public static final String ORDERWRITER_SERVER_SAVE_FILE = "OrderWriter.serverSafeFile";

  public static final String ORDERWRITER_SERVER_TIMEOUT = "OrderWriter.serverTimeout";

  public static final String ORDERWRITER_WRITE_TAGS_AS_VORLAGE_COMMENT = "OrderWriter.writeUnitTagsAsVorlageComment";

  public static final String ORDERWRITER_SELECTED_REGIONS = "OrderWriter.includeSelRegionsOnly";

  public static final String ORDERWRITER_ADD_ECHECK_COMMENTS = "OrderWriter.addECheckComments";

  public static final String ORDERWRITER_REMOVE_SC_COMMENTS = "OrderWriter.removeSCComments";

  public static final String ORDERWRITER_REMOVE_SS_COMMENTS = "OrderWriter.removeSSComments";

  public static final String ORDERWRITER_CONFIRMED_ONLY = "OrderWriter.confirmedOnly";

  public static final String ORDERWRITER_FACTION = "OrderWriter.faction";

  public static final String ORDERWRITER_FIXED_WIDTH = "OrderWriter.fixedWidth";

  /** Property type boolean: */
  public static final String TASKTABLE_RESTRICT_TO_OWNER = "TaskTable.restrictToOwner";

  /** Property type boolean: */
  public static final String TASKTABLE_RESTRICT_TO_PASSWORD = "TaskTable.restrictToPassword";

  /** Property type boolean: */
  public static final String TASKTABLE_RESTRICT_TO_SELECTION = "TaskTable.restrictToSelection";

  /** Property type boolean: */
  public static final String TASKTABLE_RESTRICT_TO_ACTIVEREGION = "TaskTable.restrictToAvtiveRegion";

  /** Property type boolean: */
  public static final String TASKTABLE_SHOW_GLOBAL = "TaskTable.showGlobal";

  /** Property type boolean: */
  public static final String TASKTABLE_INSPECTORS_ATTACK = "TaskTable.inspectors.attack";

  /** Property type boolean: */
  public static final String TASKTABLE_INSPECTORS_TRANSFER = "TaskTable.inspectors.transfer";

  /** Property type boolean: */
  public static final String TASKTABLE_INSPECTORS_MESSAGE = "TaskTable.inspectors.message";

  /** Property type boolean: */
  public static final String TASKTABLE_INSPECTORS_MOVEMENT = "TaskTable.inspectors.movement";

  /** Property type boolean: */
  public static final String TASKTABLE_INSPECTORS_ORDER_SYNTAX = "TaskTable.inspectors.ordersyntax";

  /** Property type boolean: */
  public static final String TASKTABLE_INSPECTORS_SHIP = "TaskTable.inspectors.ship";

  /** Property type boolean: */
  public static final String TASKTABLE_INSPECTORS_BUILDING = "TaskTable.inspectors.building";

  /** Property type boolean: */
  public static final String TASKTABLE_INSPECTORS_SKILL = "TaskTable.inspectors.skill";

  /** Property type boolean: */
  public static final String TASKTABLE_INSPECTORS_TEACH = "TaskTable.inspectors.teach";

  /** Property type boolean: */
  public static final String TASKTABLE_INSPECTORS_TODO = "TaskTable.inspectors.todo";

  /** Property type boolean: */
  public static final String TASKTABLE_INSPECTORS_GAMEDATA = "TaskTable.insepctors.gamedata";

  /** Property type boolean: */
  public static final String TASKTABLE_INSPECTORS_MAINTENANCE = "TaskTable.inspectors.maintenance";

  /** Property type list (of strings) **/
  public static final String HISTORY_ACCESSORY_DIRECTORY_HISTORY = "HistoryAccessory.directoryHistory";

  /** Property type boolean: */
  public static final String ADD_CR_ACCESSORY_SORT = "AddCRAccessory.sort";

  /** Property type boolean: */
  public static final String ADD_CR_ACCESSORY_INTERACTIVE = "AddCRAccessory.interactive";

  /**
   * Property type list of names
   *
   * @deprecated we use an ignore list now
   */
  @Deprecated
  public static final String TASKTABLE_INSPECTORS_LIST = "TaskTable.inspectors.list";

  /** Property type list (of ignored problem types) */
  public static final String TASKTABLE_INSPECTORS_IGNORE_LIST = "TaskTable.inspectors.ignored";

  /** Property type boolean: edit orders for all factions */
  public static final String ORDEREDITOR_EDITALLFACTIONS = "OrderEditor.editAllFactions";

  /** Property type boolean: use multi editor layout */
  public static final String ORDEREDITOR_MULTIEDITORLAYOUT = "OrderEditor.multiEditorLayout";

  /** Property type boolean: hide temp unit buttons */
  public static final String ORDEREDITOR_HIDEBUTTONS = "OrderEditor.hideButtons";

  /** Property type String: last file for loading/saving bookmarks */
  public static final String BOOKMARKMANAGER_LASTFILE = "Bookmarkmanager.lastFile";

  /** Property type String: sort mode "coordinates" or "islands" */
  public static final String REGIONOVERVIEW_SORTCRITERIA = "EMapOverviewPanel.sortRegionsCriteria";

  /** Property type boolean: show islands "coordinates" or "islands" */
  public static final String REGIONOVERVIEW_DISPLAYISLANDS = "EMapOverviewPanel.displayIslands";

  /**
   * Property type String (version number): current version number
   * 
   * @deprecated replaced by {@link #SEMANTIC_VERSION}
   */
  @Deprecated
  public static final String VERSION = "Client.Version";

  /** Property type String (version number): current version number */
  public static final String SEMANTIC_VERSION = "Client.SemanticVersion";

  /** Property type String (semantic version number): last used version number */
  public static final String LAST_VERSION = "Client.LastVersion";

  /** Property type bounds (x, y, width, height): dialog dimensions */
  public static final String FILE_CHOOSER_BOUNDS = "Filechooser.bounds";

  /** Property type bounds (x, y, width, height): dialog dimensions */
  public static final String ALCHEMY_DIALOG_BOUNDS = "AlchemyDialog.bounds";

  /**
   * Searches the property with the given key and if it exists, it tries to convert it into a boolean and returns that
   * value. If something goes wrong or the key couldn't be found the default value def
   * is returned.
   */
  public static boolean getBoolean(Properties p, String key, boolean def) {
    String val = p.getProperty(key);

    if (val != null)
      return Boolean.valueOf(val).booleanValue();

    return def;
  }

  /**
   * Searches the property with the given key and if it exists, it tries to convert it into a Color and returns that
   * value. If something goes wrong or the key couldn't be found the default value def
   * is returned.
   */
  public static Color getColor(Properties p, String key, Color def) {
    String val = p.getProperty(key);

    if (val != null) {
      Color color = Utils.getColor(val);
      if (color != null)
        return color;
    }

    return def;
  }

  /**
   * Searches the property with the given key and if it exists, it tries to convert it into a float and returns that
   * value. If something goes wrong or the key couldn't be found the default value def
   * is returned.
   */
  public static float getFloat(Properties p, String key, float def) {
    String val = p.getProperty(key);

    if (val != null) {
      try {
        return Float.valueOf(val).floatValue();
      } catch (NumberFormatException nfe) {
        return def;
      }
    }

    return def;
  }

  /**
   * Searches the property with the given key and if it exists, it tries to convert it into an integer and returns that
   * value. If something goes wrong or the key couldn't be found the default value
   * def is returned.
   */
  public static int getInteger(Properties p, String key, int def) {
    String val = p.getProperty(key);

    if (val != null) {
      try {
        return Integer.valueOf(val).intValue();
      } catch (NumberFormatException nfe) {
        return def;
      }
    }

    return def;
  }

  /**
   * Searches the property with the given key and if it exists, it tries to convert it into a KeyStroke and returns that
   * value. If something goes wrong or the key couldn't be found the default value def is returned.
   */
  public static KeyStroke getKeyStroke(Properties p, String key, KeyStroke def) {
    String val = p.getProperty(key);
    if (val == null)
      return def;
    StringTokenizer st = new StringTokenizer(val, ",");
    if (st.countTokens() != 2)
      return def;
    try {
      int mod = Integer.parseInt(st.nextToken());
      int code = Integer.parseInt(st.nextToken());
      return KeyStroke.getKeyStroke(code, mod);
    } catch (Exception e) {
      return def;
    }
  }

  /**
   * Stores the keystroke in the settings with the key. The KeyStroke is stored as 'modifiers,keycode'.
   */
  public static void setKeyStroke(Properties settings, String key, KeyStroke ks) {
    settings.setProperty(key, ks.getModifiers() + "," + ks.getKeyCode());
  }

  /**
   * Extracts properties by given prefix. If there exists a key called prefix.count this is used as order
   *
   * @return list of strings; empty list if the property is undefined
   */
  public static List<String> getList(Properties p, String prefix) {
    List<String> ret = new LinkedList<String>();
    String count = p.getProperty(prefix + ".count");

    if (count == null) {
      boolean hasMore = true;
      for (int i = 0; hasMore; i++) {
        String prop = p.getProperty(prefix + "." + i);

        if (prop == null) {
          prop = p.getProperty(prefix + i);
        }

        if (prop != null) {
          ret.add(prop);
        } else {
          hasMore = false;
        }
      }
    } else {
      for (int i = 0, max = Integer.parseInt(count); i < max; i++) {
        String prop = p.getProperty(prefix + "." + i);

        if (prop != null) {
          ret.add(prop);
        }
      }
    }

    return ret;
  }

  /**
   * Delivers a list of all keys having the prefix <kbd>prefix</kbd>
   */
  public static List<String> getPrefixedList(Properties p, String prefix) {
    List<String> ret = new LinkedList<String>();

    for (Enumeration<?> e = p.propertyNames(); e.hasMoreElements();) {
      String key = (String) e.nextElement();

      if (key.startsWith(prefix)) {
        ret.add(p.getProperty(key));
      }
    }

    return ret;
  }

  /**
   * Searches the property with the given key and if it exists, it tries to convert it into a String and returns that
   * value. If something goes wrong or the key couldn't be found the default value
   * defaultValue is returned.
   */
  public static String getString(Properties p, String key, String defaultValue) {
    if (p == null)
      return defaultValue;
    if (key == null)
      return defaultValue;
    return p.getProperty(key, defaultValue);
  }

  /**
   * Loads a rectangle from the settings using the given key. If <code>r</code> is null, a new object is created. Else
   * the result is stored in <code>r</code>. In either case, the resulting rectangle
   * is returned.
   *
   * @return The loaded rectangle or null if an error occurs.
   */
  public static Rectangle loadRect(Properties settings, Rectangle r, String key) {
    if (r == null) {
      r = new Rectangle();
    }

    try {
      r.x = Integer.parseInt(settings.getProperty(key + ".x"));
      r.y = Integer.parseInt(settings.getProperty(key + ".y"));
      r.width = Integer.parseInt(settings.getProperty(key + ".width"));
      r.height = Integer.parseInt(settings.getProperty(key + ".height"));
    } catch (Exception exc) {
      PropertiesHelper.log.warn("Bad rectangle: " + key);
      PropertiesHelper.log.debug("", exc);
      return null;
    }
    return r;
  }

  /**
   * Saves the rectangle r with property-key key to the settings. The rectangle is stored as key.x, key.y, key.width,
   * key.height.
   */
  public static void saveRectangle(Properties settings, Rectangle r, String key) {
    settings.setProperty(key + ".x", String.valueOf(r.x));
    settings.setProperty(key + ".y", String.valueOf(r.y));
    settings.setProperty(key + ".width", String.valueOf(r.width));
    settings.setProperty(key + ".height", String.valueOf(r.height));
  }

  /**
   * Sets the given color to the key using the format #RRGGBB
   */
  public static void setColor(Properties p, String key, Color color) {
    if (p == null)
      return;
    if (key == null)
      return;
    if (color == null) {
      p.remove(key);
      return;
    }
    p.setProperty(key, Utils.getColor(color));
  }

  /**
   * a) remove old properties b) set prefix.count value c) set prefix.0 .. prefix.n values
   */
  public static void setList(Properties p, String prefix, Collection<?> list) {
    // a) remove old properties
    for (String string : PropertiesHelper.getPrefixedList(p, prefix)) {
      p.remove(string);
    }

    // b) set prefix.count value
    p.setProperty(prefix + ".count", Integer.toString(list.size()));

    // c) set prefix.0 .. prefix.n values
    int i = 0;

    for (Iterator<?> iter = list.iterator(); iter.hasNext(); i++) {
      Object value = iter.next();
      p.setProperty(prefix + "." + i, value.toString());
    }
  }

  /**
   * Sets the given color to the key using the format #RRGGBB
   */
  public static void setBoolean(Properties p, String key, boolean value) {
    if (value) {
      p.setProperty(key, "true");
    } else {
      p.setProperty(key, "false");
    }
  }

  private static File settingsDir = null;

  /**
   * Sets the directory for configuration configuration is stored.
   *
   * @param newSettingsDir
   */
  public static void setSettingsDirectory(File newSettingsDir) {
    PropertiesHelper.log.info("PropertiesHelper: directory used for rule files: " + newSettingsDir.toString());
    PropertiesHelper.settingsDir = newSettingsDir;
  }

  /**
   * Returns the directory where configuration is stored.
   *
   * @return The directory where configuration is stored
   */
  public static File getSettingsDirectory() {
    return PropertiesHelper.settingsDir;
  }

}
