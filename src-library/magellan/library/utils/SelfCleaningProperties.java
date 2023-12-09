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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import magellan.library.utils.logging.Logger;

/**
 * This is a self cleaning properties implementation. The cleaning is done after loading the
 * properties.
 * 
 * @author Ilja Pavkovic
 */
public class SelfCleaningProperties extends OrderedOutputProperties {
  private static final Logger log = Logger.getInstance(SelfCleaningProperties.class);

  /**
   * Creates new SelfCleaningProperties
   */
  public SelfCleaningProperties() {
  }

  /**
   * Creates a new SelfCleaningProperties object.
   * 
   * @param defaults the defaults.
   */
  public SelfCleaningProperties(Properties defaults) {
    super(defaults);
  }

  /**
   * Loads the properties from the given input stream and cleans them afterwards.
   */
  @Override
  public synchronized void load(InputStream inStream) throws IOException {
    super.load(inStream);
    doClean();
  }

  /**
   * This operation iterates other a copy of all keys to check if they can be cleaned
   */
  private void doClean() {
    // copy keys to avoid ConcurrentModification
    List<?> copy = new ArrayList<Object>(keySet());
    for (Object name : copy) {
      if (name instanceof String) {
        doClean((String) name);
      } else {
        SelfCleaningProperties.log.error("property key is not a String: " + name);
      }
    }
  }

  private static final String propertiesToRemove[] = { "AgingProperties.numberofsessions",
      "AgingProperties.sessionsofkeys", "Client.checkVersionOnStartup",
      "EMapDetailsPanel.units.showDetailedSkills" };

  private boolean doRemoveProperties(String name) {
    for (String property : SelfCleaningProperties.propertiesToRemove) {
      if (doRemoveProperty(name, property))
        return true;
    }

    return false;
  }

  /**
   * this operation check possible clean states on a given property name. Add more cleanings here
   * (and take care to return a possibly new key name value!)
   * 
   * @param name the name of the current property to check for cleaning.
   */
  private void doClean(String name) {
    if (doRemoveProperties(name))
      return;

    name = doCleanCompareValue(name);
    name = doExpandValue(name, PropertiesHelper.ORDERWRITER_OUTPUT_FILE, "|");
    name = doExpandValue(name, "CRWriterDialog.outputFile", "|");
    name = doExpandValue(name, "Client.fileHistory", "|");
    name = renameProperty(name, "DirectoryHistory", PropertiesHelper.HISTORY_ACCESSORY_DIRECTORY_HISTORY);
    name = doExpandValue(name, PropertiesHelper.HISTORY_ACCESSORY_DIRECTORY_HISTORY, "|");
    doExpandRectangle(name, "TempUnitDialog.bounds", ",");
  }

  /**
   * Removes the given property.
   */
  private boolean doRemoveProperty(String oldName, String key) {
    if (oldName.equals(key)) {
      remove(oldName);

      return true;
    } else
      return false;
  }

  private String doExpandFactionColors(String oldName, String key) {
    if (oldName.equals(key)) {
      int i = 0;
      String delim = ";";

      for (StringTokenizer st = new StringTokenizer(getProperty(oldName), delim); st
          .hasMoreTokens(); i++) {
        String value = st.nextToken();
        setProperty(oldName + ".name." + i, value);

        value = st.nextToken();
        value += delim;
        value += st.nextToken();
        value += delim;
        value += st.nextToken();
        setProperty(oldName + ".color." + i, value);
      }

      remove(oldName);

      String newName = oldName + ".name.count";
      setProperty(newName, String.valueOf(i));

      String newName2 = oldName + ".color.count";
      setProperty(newName2, String.valueOf(i));
      SelfCleaningProperties.log.error("SelfCleaningProperties.doClean: Expanded property "
          + oldName + " to " + newName);

      return newName;
    }

    return oldName;
  }

  /**
   * rename property e.g. Ausdauer.compareValue to ClientPreferences.compareValue.Ausdauer
   */
  private String doCleanCompareValue(String name) {
    if (name.endsWith(".compareValue")) {
      String newName =
          "ClientPreferences.compareValue." + name.substring(0, name.lastIndexOf(".compareValue"));
      renameProperty(name, newName);

      return newName;
    }

    return name;
  }

  /**
   * Expands value in form a|b|... for property <kbd>key</kbd>
   */
  private String doExpandValue(String name, String key, String delim) {
    if (name.equals(key))
      return expandList(name, delim);

    return name;
  }

  /**
   * generic function to expand a string into a list
   */
  private String expandList(String oldName, String delim) {
    int i = 0;

    for (StringTokenizer st = new StringTokenizer(getProperty(oldName), delim); st.hasMoreTokens(); i++) {
      String value = st.nextToken();
      setProperty(oldName + "." + i, value);
    }

    remove(oldName);

    String newName = oldName + ".count";
    setProperty(newName, String.valueOf(i));
    SelfCleaningProperties.log.error("SelfCleaningProperties.doClean: Expanded property " + oldName
        + " to " + newName);

    return newName;
  }

  /**
   * Expands value in form x|y|width|height for property <kbd>key</kbd>
   */
  private String doExpandRectangle(String name, String key, String delim) {
    if (name.equals(key))
      return expandRectangle(name, delim);

    return name;
  }

  /**
   * generic function to expand a string into a list
   */
  private String expandRectangle(String oldName, String delim) {
    StringTokenizer st = new StringTokenizer(getProperty(oldName), delim);
    if (st.hasMoreTokens()) {
      String x = st.nextToken();
      if (st.hasMoreTokens()) {
        String y = st.nextToken();
        if (st.hasMoreTokens()) {
          String width = st.nextToken();
          if (st.hasMoreTokens()) {
            String height = st.nextToken();
            setProperty(oldName + ".x", x);
            setProperty(oldName + ".y", y);
            setProperty(oldName + ".width", width);
            setProperty(oldName + ".height", height);
            remove(oldName);
          }
        }
      }
    }

    SelfCleaningProperties.log.error("SelfCleaningProperties.doClean: Expanded rectangle "
        + oldName);

    return oldName;
  }

  /**
   * generic function to rename a property
   */
  private String renameProperty(String key, String oldName, String newName) {
    if (key.equals(oldName))
      return renameProperty(oldName, newName);

    return key;
  }

  /**
   * generic function to rename a property
   */
  private String renameProperty(String oldName, String newName) {
    String value = getProperty(oldName);
    remove(oldName);
    setProperty(newName, value);
    SelfCleaningProperties.log.error("SelfCleaningProperties.doClean: Renamed property " + oldName
        + " to " + newName);

    return newName;
  }
}
