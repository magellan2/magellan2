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

package magellan.client.extern;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import magellan.library.utils.ResourcePathClassLoader;
import magellan.library.utils.logging.Logger;

/**
 * Loads all external modules that can be found. Please see com.eressea.extern.ExternalModule for
 * documentation.
 *
 * @author Ulrich Küster
 * @author Thoralf Rickert
 */
public abstract class AbstractPlugInLoader<T> {
  private static final Logger log = Logger.getInstance(AbstractPlugInLoader.class);

  /**
   * Searches the resource paths for classes that implement the interface
   * com.eressea.extern.ExternalModule. Returns them as Collection of Class objects.
   */
  public abstract Collection<Class<T>> getExternalModuleClasses(Properties settings);

  /**
   *
   */
  protected Collection<String> getPathsFromResourcePathClassLoader(
      ResourcePathClassLoader resLoader, Properties settings) {
    Collection<String> paths = new ArrayList<String>();

    for (URL url : resLoader.getPaths()) {

      String path = null;
      try {
        path = URLDecoder.decode(url.getFile(), "UTF-8");
      } catch (Throwable exception) {
        AbstractPlugInLoader.log.error("", exception);
        continue;
      }

      if (path.startsWith("file:///")) {
        path = path.substring(7, path.length());
      } else if (path.startsWith("file:/")) {
        path = path.substring(5, path.length());
      }

      if (path.endsWith("!/")) {
        path = path.substring(0, path.length() - 2);
      }

      paths.add(path);
    }

    return paths;
  }

  /**
   *
   */
  protected Collection<String> getPathsFromClassPath() {
    Collection<String> paths = new ArrayList<String>();

    // String classpath = System.getProperty("java.class.path");
    // classpath += System.getProperty("path.separator")+path;
    StringTokenizer st =
        new StringTokenizer(System.getProperty("java.class.path"), System
            .getProperty("path.separator"));

    while (st.hasMoreTokens()) {
      paths.add(st.nextToken());
    }

    return paths;
  }

  /**
   *
   */
  protected Collection<Class<T>> getClassesFromPath(ClassLoader resLoader,
      Class<T> externalModuleClass, String path) {
    return getClassesFromPath(resLoader, externalModuleClass, path, null, getLastCapitalizedString(
        externalModuleClass.getName()).toLowerCase()
        + ".class");
  }

  /**
   *
   */
  protected Collection<Class<T>> getClassesFromPath(ClassLoader resLoader,
      Class<T> externalModuleClass, String path, String packagePrefix, String postfix) {
    Collection<Class<T>> classes = new ArrayList<Class<T>>();

    try {
      File file = new File(path);
      if (file.exists()) {
        if (file.isDirectory()) {
          AbstractPlugInLoader.log.debug("Searching in " + file.getAbsolutePath() + "...");

          // add files or subdirectories to search list
          File newPaths[] = file.listFiles();

          for (File newPath : newPaths) {
            // add in first position
            String newPrefix = packagePrefix == null ? "" : packagePrefix + file.getName() + ".";
            classes.addAll(getClassesFromPath(resLoader, externalModuleClass, newPath
                .getAbsolutePath(), newPrefix, postfix));
          }
        } else if (file.getName().toLowerCase().endsWith(".jar")
            || file.getName().toLowerCase().endsWith(".zip")) {
          AbstractPlugInLoader.log.info("Searching " + file.getAbsolutePath() + "...");

          ZipFile zip = new ZipFile(file);

          for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
            ZipEntry entry = e.nextElement();

            if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(postfix)) {
              AbstractPlugInLoader.log.finer("entry " + entry.getName());
              // class file found!
              // check whether it implements ExternalModule
              String name = entry.getName();
              name = name.substring(0, name.indexOf(".class")).replace('\\', '.').replace('/', '.');
              // AbstractPlugInLoader.log.fine("entry1 " + name);

              AbstractPlugInLoader.log.finer("loading " + name + " from " + resLoader.getClass()
                  + " from " + ((ResourcePathClassLoader) resLoader).getPaths() + "...");
              Class<?> foundClass = resLoader.loadClass(name);
              AbstractPlugInLoader.log.finer("... loaded");
              // AbstractPlugInLoader.log.fine("entry2 " + foundClass);
              Class<?> interfaces[] = foundClass.getInterfaces();
              // AbstractPlugInLoader.log.fine("entry3 " + interfaces.length);
              boolean found = false;

              if (AbstractPlugInLoader.log.isDebugEnabled()) {
                for (Class<?> ainterface : interfaces) {
                  AbstractPlugInLoader.log.debug("interface: " + ainterface.getName());
                }
              }

              for (int i = 0; (i < interfaces.length) && !found; i++) {
                if (interfaces[i].equals(externalModuleClass)) {
                  found = true;
                  break;
                }
              }

              if (found) {
                // found a class that implements ExternalModule
                AbstractPlugInLoader.log.info("Found " + foundClass.getName());
                try {
                  // should be okay, we checked the interface above
                  @SuppressWarnings("unchecked")
                  Class<T> c = (Class<T>) foundClass;
                  classes.add(c);
                } catch (Throwable exc) {
                  AbstractPlugInLoader.log.error("cannot use " + foundClass.getName(), exc);
                }
              }
            }
          }
          zip.close();
        } else if (file.getName().toLowerCase().endsWith(postfix)) {
          String name = file.getName();
          name = name.substring(0, name.indexOf(".class")).replace('\\', '.').replace('/', '.');

          Class<?> foundClass;

          try {
            foundClass = resLoader.loadClass(name);
          } catch (ClassNotFoundException e) {
            // pavkovic 2003.07.09: now retry with prefix
            name = packagePrefix + name;
            foundClass = resLoader.loadClass(name);
          }

          Class<?> interfaces[] = foundClass.getInterfaces();

          for (Class<?> interface1 : interfaces) {
            if (interface1.equals(externalModuleClass)) {
              // found a class that implements ExternalModule
              AbstractPlugInLoader.log.info("Found " + foundClass.getName());
              try {
                // should be okay, we checked the interface above
                @SuppressWarnings("unchecked")
                Class<T> c = (Class<T>) foundClass;
                classes.add(c);
              } catch (Throwable exc) {
                AbstractPlugInLoader.log.error("cannot use " + foundClass.getName(), exc);
              }
              break;
            }
          }
        }
      } else {
        AbstractPlugInLoader.log.info("File not found: " + file);
      }
    } catch (IOException ioe) {
      AbstractPlugInLoader.log.info(ioe);
    } catch (NoClassDefFoundError ncdfe) {
      AbstractPlugInLoader.log.info(ncdfe);
    } catch (ClassNotFoundException cnfe) {
      AbstractPlugInLoader.log.info(cnfe);
    }

    return classes;
  }

  /**
   *
   */
  protected Collection<Class<T>> getExternalModuleClasses(Properties settings,
      Class<T> externalModuleClass) {
    Collection<Class<T>> classes = new HashSet<Class<T>>();

    ResourcePathClassLoader resLoader = new ResourcePathClassLoader(settings);

    // pathes to search
    Collection<String> paths = new ArrayList<String>();

    // a) read possible paths from ResourcePathClassLoader
    // b) read property java.class.path and iterate over the entries
    if (settings.getProperty("ExternalModuleLoader.searchResourcePathClassLoader", "true").equals(
        "true")) {
      paths.addAll(getPathsFromResourcePathClassLoader(resLoader, settings));
    }

    if (settings.getProperty("ExternalModuleLoader.searchClassPath", "true").equals("true")) {
      paths.addAll(getPathsFromClassPath());
    }

    // search explicit the magellan dir for the magellan-plugins.jar
    paths.add(settings.getProperty("plugin.helper.resourcedir") + File.separator
        + "magellan-plugins.jar");
    paths.add(settings.getProperty("plugin.helper.bindir") + File.separator
        + "magellan-plugins.jar");

    for (String path : paths) {
      log.fine("searching in " + path);
      classes.addAll(getClassesFromPath(resLoader, externalModuleClass, path));
    }

    return classes;
  }

  /**
   * delivers last capitalized String, e.g.: for input "StringBuffer.class" this function returns
   * "Buffer.class"
   */
  protected String getLastCapitalizedString(String aString) {
    StringCharacterIterator iter = new StringCharacterIterator(aString);

    int length = 1;
    for (char c = iter.last(); c != CharacterIterator.DONE; c = iter.previous(), ++length) {
      if ((c >= 'A') && (c <= 'Z') && length > 2) {
        if (AbstractPlugInLoader.log.isDebugEnabled()) {
          AbstractPlugInLoader.log.debug("ExternalModuleLoader.getLastCapitalizedString(" + aString
              + "): " + aString.substring(iter.getIndex()));
        }

        return aString.substring(iter.getIndex());
      }
    }

    return aString;
  }
}
