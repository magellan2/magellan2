// class magellan.library.utils.Resources
// created on 29.04.2007
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
package magellan.library.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;

import magellan.library.utils.logging.Logger;

/**
 * This class loads and holds the resources for Magellan.
 * 
 * @author Thoralf Rickert
 * @version 1.0, 29.04.2007
 */
public class Resources {
  private static final Logger log = Logger.getInstance(Resources.class);
  private static final String DEFAULT = "default";
  private static Resources _instance = null;

  private Hashtable<String, MyResourceBundle> bundles = new Hashtable<String, MyResourceBundle>();
  private File resourceDirectory;
  // private static int callCount = 0;

  /**
   * Returns the translated string for the specified class and string using the order locale. The
   * corresponding properties file must be named like the class c with all '.' (periods) replaced by
   * '-' (dashes).
   */
  private static Set<String> loggedOrderTranslations = new HashSet<String>();

  private static Collection<URL> staticResourcePaths = new LinkedList<URL>();

  /**
   * This constructor loads all available resources into a hashtable.
   */
  private Resources() {
    //
  }

  /**
   * This method returns the singleton of this class
   */
  public static synchronized Resources getInstance() {
    if (Resources._instance == null) {
      Resources._instance = new Resources();
    }
    return Resources._instance;
  }

  /**
   * Changes the resource directory.
   * 
   * @param resourceDirectory
   */
  public void setResourceDirectory(File resourceDirectory) {
    this.resourceDirectory = resourceDirectory;
  }

  /**
   * Returns the current resource directory.
   * 
   * @return the current resource directory
   */
  public static File getResourceDirectory() {
    return getInstance().resourceDirectory;
  }

  /**
   * This method makes it possible to add specific resource files to this Resources map. F.e. call
   * initialize("mapedit_") to search and load resource files from files called
   * "mapedit_resources.properties".
   */
  public void initialize(File resourceDirectory, String prefix) {
    if (prefix == null) {
      prefix = "";
    }
    this.resourceDirectory = resourceDirectory;

    Resources.log.info("Initializing resources for prefix '" + prefix + "' in "
        + this.resourceDirectory);

    File etcDirectory = new File(this.resourceDirectory, "etc");

    if (!etcDirectory.exists()) {
      Resources.log.info("Could not find resources in directory " + etcDirectory);
      // hmmm, maybe one directory level up (special Eclipse problem with bin directory)
      etcDirectory = new File(this.resourceDirectory.getParentFile(), "etc");
      if (!etcDirectory.exists()) {
        Resources.log.info("Could not find resources in directory " + etcDirectory);
        // okay, I'll give up...
        throw new RuntimeException("Could NOT find location Magellan");
      }
    }

    Resources.log.info("Searching resources in " + etcDirectory);
    File[] files = etcDirectory.listFiles(new ResourceFilenameFilter(prefix));

    for (File file : files) {
      String resourceName = file.getName();
      if (resourceName.equalsIgnoreCase(prefix + "resources.properties")) {
        // default resource...
        resourceName = Resources.DEFAULT;
      } else {
        resourceName = resourceName.substring(prefix.length() + 10, resourceName.length() - 11);
      }
      Resources.log.info("Load resource '" + file.getName() + "' as " + resourceName);
      try {
        FileInputStream stream = new FileInputStream(file);
        MyResourceBundle bundle = new MyResourceBundle(stream);
        if (bundles.containsKey(resourceName)) {
          MyResourceBundle parentBundle = bundles.get(resourceName);
          parentBundle.add(bundle);
        } else {
          bundles.put(resourceName, bundle);
        }
      } catch (Exception exception) {
        Resources.log.error("Could not load resource '" + file + "'");
      }
    }

    // check();
  }

  /**
   * Checks if all resource bundle contain the same set of keys.
   */
  public void check() {
    ResourceBundle defaultBundle = Resources.getResourceBundle(null);
    for (Locale l : Resources.getAvailableLocales()) {
      ResourceBundle firstBundle = Resources.getResourceBundle(l);
      if (firstBundle != null && defaultBundle != firstBundle) {
        compareKeys(defaultBundle, firstBundle);
        compareKeys(firstBundle, defaultBundle);
      }
      for (Locale l2 : Resources.getAvailableLocales()) {
        ResourceBundle currentBundle = Resources.getResourceBundle(l2);
        if (firstBundle == null || currentBundle == null || defaultBundle == currentBundle
            || defaultBundle == firstBundle || firstBundle == currentBundle) {
          continue;
        }
        compareKeys(firstBundle, currentBundle);
      }
    }
  }

  private void compareKeys(ResourceBundle firstBundle, ResourceBundle otherBundle) {
    for (Enumeration<String> keys = firstBundle.getKeys(); keys.hasMoreElements();) {
      String key = keys.nextElement();
      boolean foundIt = false;
      for (Enumeration<String> otherKeys = otherBundle.getKeys(); otherKeys.hasMoreElements()
          && !foundIt;) {
        String otherKey = otherKeys.nextElement();
        if (key.equals(otherKey)) {
          foundIt = true;
        }
      }
      if (!foundIt) {
        Resources.log.warn("key " + key + " in bundle " + firstBundle.getLocale()
            + " not available in bundle " + otherBundle.getLocale());
      }
      /*
       * if (!otherBundle.containsKey(key)) { log.warn("key " + key + " in bundle " +
       * firstBundle.getLocale() + " not available in bundle " + otherBundle.getLocale()); }
       */
    }
  }

  /**
   * Returns a list of all available translations including the default translation as ENGLISH.
   */
  public static List<Locale> getAvailableLocales() {
    Resources resources = Resources.getInstance();
    List<Locale> locales = new ArrayList<Locale>();
    for (String bundlename : resources.bundles.keySet()) {
      Locale locale = null;
      if (bundlename.equals(Resources.DEFAULT)) {
        locale = Locale.ENGLISH;
      } else {
        if (bundlename.indexOf("_") > 0) {
          String language = bundlename.substring(0, bundlename.indexOf("_"));
          String country = bundlename.substring(bundlename.indexOf("_") + 1);
          locale = new Locale(language, country);
        } else {
          locale = new Locale(bundlename);
        }
      }
      if (locale == null) {
        continue;
      }
      if (locales.contains(locale)) {
        continue;
      }
      locales.add(locale);
    }
    return locales;
  }

  /**
   * This method returns the wanted resourcebundle.
   */
  public static ResourceBundle getResourceBundle(Locale locale) {
    Resources resources = Resources.getInstance();
    if (locale == null) {
      if (resources.bundles.containsKey(Resources.DEFAULT))
        return resources.bundles.get(Resources.DEFAULT);
    } else {
      String localeName = locale.toString();
      if (resources.bundles.containsKey(localeName))
        return resources.bundles.get(localeName);
      localeName = locale.getCountry();
      if (resources.bundles.containsKey(localeName))
        return resources.bundles.get(localeName);
      localeName = locale.getLanguage();
      if (resources.bundles.containsKey(localeName))
        return resources.bundles.get(localeName);
    }
    return null;
  }

  /**
   * Returns the resource for the resource key in the default locale of the running machine. If the
   * resource is not available in this locale, this method tries to find the resource in the default
   * resources.
   * 
   * @param key should not contain spaces
   * @return the value for the key. If the key could not be found in any resource, the key is
   *         returned.
   */
  public static String get(String key) {
    return Resources.get(key, true);
  }

  /**
   * Returns the resource for the resource key in the default locale of the running machine. If the
   * resource is not available in this locale, this method tries to find the resource in the default
   * resources.
   * 
   * @param key should not contain spaces
   * @param args These arguments are applied to the resource. See
   *          {@link MessageFormat#format(Object)}.
   * @return the value for the key. If the key could not be found in any resource, the key is
   *         returned.
   */
  public static String get(String key, Object... args) {
    String value = Resources.get(key);
    if (value != null) {
      value = new MessageFormat(value).format(args);
    }
    return value;
  }

  /**
   * Returns the resource for the resource key in the default locale of the running machine. If the
   * resource is not available in this locale, this method tries to find the resource in the default
   * resources.
   * 
   * @param key should not contain spaces
   * @param returnKey
   * @return the value for the key. If the key could not be found in any resource, the key is
   *         returned if <code>returnKey==true</code>, otherwise <code>null</code> is returned.
   */
  public static String get(String key, boolean returnKey) {
    return Resources.get(key, Locale.getDefault(), returnKey);
  }

  /**
   * 
   */
  public Enumeration<String> getKeys(Locale locale) {
    if (locale == null) {
      if (bundles.containsKey(Resources.DEFAULT))
        return bundles.get(Resources.DEFAULT).getKeys();
    } else {
      String localeName = locale.toString();
      if (bundles.containsKey(localeName))
        return bundles.get(localeName).getKeys();
      localeName = locale.getCountry();
      if (bundles.containsKey(localeName))
        return bundles.get(localeName).getKeys();
      localeName = locale.getLanguage();
      if (bundles.containsKey(localeName))
        return bundles.get(localeName).getKeys();
    }
    return null;
  }

  /**
   * Returns the resource for the resource key in the given locale. If the resource is not available
   * in this locale the method tries to find the resource in the default locale of this machine and
   * if it is not available in this locale too then it tries the default resource.
   * 
   * @param key should not contain spaces
   * @param locale This locale is used before default locales
   * @param returnKey
   * @return the value for the key. If the key could not be found in any resource, the key is
   *         returned if <code>returnKey==true</code>, otherwise <code>null</code> is returned.
   */
  public static String get(String key, Locale locale, boolean returnKey) {
    if (locale == null) {
      locale = Locale.getDefault();
    }
    if (key.startsWith("magellan.")) {
      Resources.log.warn("Using deprecated resource key with prefix 'magellan.'. The key '" + key
          + "' will be truncated.");
      key = key.substring(9);
    }
    Resources resources = Resources.getInstance();
    String result = resources.getResource(key, locale);

    if (result == null) {
      if (locale != Locale.getDefault()) {
        result = resources.getResource(key, Locale.getDefault());
      }
      if (result == null) {
        result = resources.getResource(key, null);
      }
    }
    if (result == null && returnKey) {
      Resources.log.warn("Could not find the resource key '" + key + "' in the resources. See "
          + Resources.getLine() + " for more details");
      result = key;
      // try to find out who calls us
    }
    return result;
  }

  /**
   * Returns the resource for the resource key in the default locale of the running machine. If the
   * resource is not available in this locale, this method tries to find the resource in the default
   * resources.
   */
  public static String getFormatted(String key, Object... args) {
    String message = Resources.get(key, false);
    if (message == null) {
      Resources.log.warn("Could not find the resource key '" + key + "' in the resources. See "
          + Resources.getLine() + " for more details");
      return key;
    }
    return (new java.text.MessageFormat(message)).format(args);
  }

  protected static StringWriter sw = new StringWriter();
  protected static PrintWriter pw = new PrintWriter(Resources.sw);

  /**
   * Liefert die Zeilennummer, von der eine Operation aufgerufen wurde.
   */
  private static String getLine() {
    // int lineNumber = 0;
    Throwable throwable = new Throwable();
    if (Resources.sw == null) {
      Resources.sw = new StringWriter();
    }
    if (Resources.pw == null) {
      Resources.pw = new PrintWriter(Resources.sw);
    }
    throwable.printStackTrace(Resources.pw);
    throwable = null;
    String s = Resources.sw.toString();
    Resources.sw.getBuffer().setLength(0);
    StringTokenizer tokenizer = new StringTokenizer(s, "\n");
    String caller1 = null;
    String caller2 = null;
    boolean t = true;
    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();
      // die erste Zeile enthält unser Throwable
      if (t) {
        t = false;
        continue;
      }
      // Suche nach der ersten Zeile, die nicht in dieser Klasse liegt.
      if (token.indexOf(Resources.class.getName()) < 0) {
        caller1 = token;
        if (tokenizer.hasMoreTokens()) {
          caller2 = tokenizer.nextToken();
        }
        break;
      }
    }
    if (caller1 == null)
      return caller1;
    caller1 = caller1.trim().substring(3);
    if (caller2 != null) {
      caller1 = caller1 + " & " + caller2.trim().substring(3);
    }
    return caller1;
  }

  /**
   * Attempts to get the translation of the given order key in the current order locale. If no
   * translation is found, the key is returned.
   * 
   * @param key An order key
   * @return The translation as found in the Resources or the key if no translation is found
   */
  public static String getOrderTranslation(String key) {
    return Resources.getOrderTranslation(key, Locales.getOrderLocale());
  }

  /**
   * Attempts to get the translation of the given rulesItem (german) key in the current order
   * locale. If no translation is found, the key is returned.
   * 
   * @param key An string key of a name of an Item in rules.cr
   * @return The translation as found in the Resources or the key if no translation is found
   */
  public static String getRuleItemTranslation(String key) {
    return Resources.getRuleItemTranslation(key, Locales.getOrderLocale());
  }

  /**
   * Attempts to get the translation of the given order key in the given locale. If no translation
   * is found, the key is returned.
   * 
   * @param key An rule item key. May contain spaces, but they will be removed before lookup.
   * @param locale
   * @return The translation as found in the Resources or the key if no translation is found
   */
  public static String getRuleItemTranslation(String key, Locale locale) {
    Resources resources = Resources.getInstance();
    String trimKey = key.trim().replaceAll(" ", "");

    trimKey = "rules." + trimKey;
    String translation = resources.getResource(trimKey, locale);

    if (translation != null) {
      if (Resources.log.isDebugEnabled() && !Resources.loggedOrderTranslations.contains(trimKey)) {
        Resources.log.debug("Resources.getOrderTranslation(" + trimKey + "," + locale + "): \""
            + translation + "\"");
        Resources.loggedOrderTranslations.add(trimKey);
      }

      return translation;
    }

    // no translation found, give back key
    if (Resources.log.isDebugEnabled() && !Resources.loggedOrderTranslations.contains(trimKey)) {
      Resources.log.debug("Resources.getOrderTranslation(" + trimKey + "," + locale + "): \""
          + trimKey + "\"");
      Resources.loggedOrderTranslations.add(trimKey);
    }

    // no translation found, give back key
    if (!Locale.GERMAN.equals(locale)) {
      Resources.log.warn("Resources.getOrderTranslation(" + trimKey + "," + locale
          + "): no valid translation found, returning key");
    }

    return trimKey;

  }

  /**
   * Attempts to get the translation of the given order key in the given locale. If no translation
   * is found, the key is returned.
   * 
   * @param key An order key. May contain spaces, but they will be removed before lookup.
   * @param locale If this is <code>null</code>, the {@link #DEFAULT} locale is used.
   * @return The translation as found in the Resources or the key if no translation is found
   */
  public static String getOrderTranslation(String key, Locale locale) {
    Resources resources = Resources.getInstance();
    String trimkey = key.trim().replaceAll(" ", "");
    if (trimkey != key) {
      log.warn("key was invalid: " + key);
    }
    trimkey = "orders." + trimkey;
    String translation = resources.getResource(trimkey, locale);

    if (translation != null) {
      if (Resources.log.isDebugEnabled() && !Resources.loggedOrderTranslations.contains(trimkey)) {
        Resources.log.debug("Resources.getOrderTranslation(" + trimkey + "," + locale + "): \""
            + translation + "\"");
        Resources.loggedOrderTranslations.add(trimkey);
      }

      return translation;
    }

    // no translation found, give back key
    if (Resources.log.isDebugEnabled() && !Resources.loggedOrderTranslations.contains(trimkey)) {
      Resources.log.debug("Resources.getOrderTranslation(" + trimkey + "," + locale + "): \""
          + trimkey + "\"");
      Resources.loggedOrderTranslations.add(trimkey);
    }

    // no translation found, give back key
    if (!Locale.ENGLISH.equals(locale)) {
      Resources.log.warn("Resources.getOrderTranslation(" + trimkey + "," + locale
          + "): no valid translation found, returning key");
      // (new RuntimeException()).printStackTrace();
    }

    return trimkey;

  }

  /**
   * Returns the resource paths the static methods of this class operate on.
   * 
   * @see ResourcePathClassLoader
   */
  public static Collection<URL> getStaticPaths() {
    return Collections.unmodifiableCollection(Resources.staticResourcePaths);
  }

  /**
   * Tells this loader which resource paths to search for classes and resources when operating
   * statically.
   */
  public static void setStaticPaths(Collection<URL> paths) {
    if (paths == null) {
      Resources.staticResourcePaths = new LinkedList<URL>();
    } else {
      Resources.staticResourcePaths = paths;
    }
  }

  /**
   * loads RessourcePaths(static) from the given settings
   * 
   * @param settings
   */
  public static void initStaticPaths(Properties settings) {
    List<URL> resourcePaths = new ArrayList<URL>();

    for (String location : PropertiesHelper.getList(settings, "Resources.preferredPathList")) {
      try {
        resourcePaths.add(new URL(location));
      } catch (MalformedURLException e) {
        Resources.log.error(e);
      }
    }
    Resources.setStaticPaths(resourcePaths);
  }

  /**
   * Stores the specified resource paths to the specified settings.
   */
  public static void storePaths(Collection<URL> resourcePaths, Properties settings) {
    if (resourcePaths == null) {
      resourcePaths = new LinkedList<URL>();
    }

    PropertiesHelper.setList(settings, "Resources.preferredPathList", resourcePaths);
  }

  /**
   * This method tries to find a resource in the set of bundles.
   * 
   * @param key should not contain spaces
   * @param locale If this is <code>null</code>, the {@link #DEFAULT} locale is used.
   * @return The value for this key in the resources or <code>null</code> if the key couldn't be
   *         found in any of the registered bundles
   */
  private String getResource(String key, Locale locale) {
    // removed for performance reasons; callers must ensure proper keys
    String trimKey = key;
    if (key.contains(" ")) {
      log.warn("invalid resource key " + key);
      trimKey = key.trim().replace(" ", "");
    }

    // if (++callCount % 100000 == 1) {
    // log.fine("getResource call #" + callCount);
    // }
    if (locale == null) {
      if (bundles.containsKey(Resources.DEFAULT)
          && bundles.get(Resources.DEFAULT).containsKey(trimKey))
        return bundles.get(Resources.DEFAULT).getResource(trimKey);
    } else {
      String localeName = locale.toString();
      if (Locale.ENGLISH.equals(locale)) {
        localeName = Resources.DEFAULT;
      }
      if (bundles.containsKey(localeName) && bundles.get(localeName).containsKey(trimKey))
        return bundles.get(localeName).getResource(trimKey);
      localeName = locale.getCountry();
      if (bundles.containsKey(localeName) && bundles.get(localeName).containsKey(trimKey))
        return bundles.get(localeName).getResource(trimKey);
      localeName = locale.getLanguage();
      if (bundles.containsKey(localeName) && bundles.get(localeName).containsKey(trimKey))
        return bundles.get(localeName).getResource(trimKey);
    }
    return null;
  }

  public static void main(String[] args) {
    System.out.println(Resources.getAvailableLocales());
  }

  public static URL file2URL(File file) throws MalformedURLException {
    return new URL("jar:" + file.toURI().toURL().toString() + "!/");
  }
}

/**
 * A small filter for resource file names...
 */
class ResourceFilenameFilter implements FilenameFilter {
  private String prefix;

  public ResourceFilenameFilter() {
    this(null);
  }

  public ResourceFilenameFilter(String prefix) {
    if (prefix == null) {
      prefix = "";
    }
    this.prefix = prefix;
  }

  public boolean accept(File dir, String name) {
    return (name.startsWith(prefix + "resources") && name.endsWith(".properties"));
  }

}

/**
 * Small wrapper for JDK1.6 ResourceBunde with Reader and containsKey()...
 */
class MyResourceBundle extends PropertyResourceBundle {
  /** Contains all sub bundles (loaded via plugins) */
  private List<MyResourceBundle> childResources = new ArrayList<MyResourceBundle>();

  /**
   * 
   */
  public MyResourceBundle(InputStream stream) throws IOException {
    super(stream);
  }

  /**
   * Adds a child resource bundle to this bundle. For example is "mapedit_resources.properties" a
   * child of "resources.properties". This childs will be used when the master bundle does not
   * contain the key.
   */
  public void add(MyResourceBundle bundle) {
    childResources.add(bundle);
  }

  /**
   * @see java.util.ResourceBundle#getString(java.lang.String)
   */
  public String getResource(String key) {
    try {
      return getString(key);
    } catch (Exception exception) {
      if (childResources.size() > 0) {
        for (MyResourceBundle child : childResources) {
          if (child.containsKey(key))
            return child.getResource(key);
        }
      }
    }
    throw new MissingResourceException("Can't find resource key " + key, this.getClass().getName(),
        key);
  }

  /**
   * This method tries to find the resource key in this resource bundle. If it cannot be found it
   * tries all child resource bundles.
   * 
   * @see java.util.ResourceBundle#containsKey(java.lang.String)
   */
  // @Override
  @Override
  public boolean containsKey(String key) {
    try {
      getString(key);
      return true;
    } catch (Exception exception) {
      // let us try the childs...
      if (childResources.size() > 0) {
        for (MyResourceBundle child : childResources) {
          if (child.containsKey(key))
            return true;
        }
      }
      return false;
    }
  }
}