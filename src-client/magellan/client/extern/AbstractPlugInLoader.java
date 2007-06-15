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
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import magellan.library.utils.ResourcePathClassLoader;
import magellan.library.utils.logging.Logger;

/**
 * Loads all external modules that can be found. Please see
 * com.eressea.extern.ExternalModule for documentation.
 * 
 * @author Ulrich Küster
 * @author Thoralf Rickert
 */
public abstract class AbstractPlugInLoader<T> {
  private static final Logger log = Logger.getInstance(AbstractPlugInLoader.class);

  /**
   * Searches the resource paths for classes that implement the interface
   * com.eressea.extern.ExternalModule. Returns them as Collection of Class
   * objects.
   */
  public abstract Collection<Class<T>> getExternalModuleClasses(Properties settings);

  /**
   * 
   */
  protected Collection<String> getPathsFromResourcePathClassLoader(ResourcePathClassLoader resLoader, Properties settings) {
    Collection<String> paths = new ArrayList<String>();

    for (Iterator<URL> iter = resLoader.getPaths().iterator(); iter.hasNext();) {
      URL url = iter.next();
      String s = url.getFile();

      if (s.startsWith("file:/")) {
        s = s.substring(6, s.length());
      }

      if (s.endsWith("!/")) {
        s = s.substring(0, s.length() - 2);
      }

      paths.add(s);
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
    StringTokenizer st = new StringTokenizer(System.getProperty("java.class.path"), System.getProperty("path.separator"));

    while (st.hasMoreTokens()) {
      paths.add(st.nextToken());
    }

    return paths;
  }

  /**
   * 
   */
  protected Collection<Class<T>> getClassesFromPath(ClassLoader resLoader, Class externalModuleClass, String path) {
    return getClassesFromPath(resLoader, externalModuleClass, path, null, getLastCapitalizedString(externalModuleClass.getName()).toLowerCase() + ".class");
  }

  /**
   * 
   */
  protected Collection<Class<T>> getClassesFromPath(ClassLoader resLoader, Class externalModuleClass, String path, String packagePrefix, String postfix) {
    Collection<Class<T>> classes = new ArrayList<Class<T>>();

    try {
      File file = new File(path);

      if (file.exists()) {
        if (file.isDirectory()) {
          log.debug("Searching in " + file.getAbsolutePath() + "...");

          // add files or subdirectories to search list
          File newPaths[] = file.listFiles();

          for (int i = 0; i < newPaths.length; i++) {
            // add in first position
            String newPrefix = packagePrefix == null ? "" : packagePrefix + file.getName() + ".";
            classes.addAll(getClassesFromPath(resLoader, externalModuleClass, newPaths[i].getAbsolutePath(), newPrefix, postfix));
          }
        } else if (file.getName().toLowerCase().endsWith(".jar") || file.getName().toLowerCase().endsWith(".zip")) {
          log.info("Searching " + file.getAbsolutePath() + "...");

          ZipFile zip = new ZipFile(file);

          for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
            ZipEntry entry = e.nextElement();

            if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(postfix)) {
              // class file found!
              // check whether it implements ExternalModule
              String name = entry.getName();
              name = name.substring(0, name.indexOf(".class")).replace('\\', '.').replace('/', '.');

              Class foundClass = resLoader.loadClass(name);
              Class interfaces[] = foundClass.getInterfaces();
              boolean found = false;

              for (int i = 0; (i < interfaces.length) && !found; i++) {
                if (interfaces[i].equals(externalModuleClass)) {
                  found = true;
                }
              }

              if (found) {
                // found a class that implements ExternalModule
                classes.add(foundClass);
                log.info("Found " + foundClass.getName());
              }
            }
          }
        } else if (file.getName().toLowerCase().endsWith(postfix)) {
          String name = file.getName();
          name = name.substring(0, name.indexOf(".class")).replace('\\', '.').replace('/', '.');

          Class foundClass;

          try {
            foundClass = resLoader.loadClass(name);
          } catch (ClassNotFoundException e) {
            // pavkovic 2003.07.09: now retry with prefix
            name = packagePrefix + name;
            foundClass = resLoader.loadClass(name);
          }

          Class interfaces[] = foundClass.getInterfaces();

          for (int i = 0; i < interfaces.length; i++) {
            if (interfaces[i].equals(externalModuleClass)) {
              // found a class that implements ExternalModule
              classes.add(foundClass);
              log.info("Found " + foundClass.getName());

              break;
            }
          }
        }
      } else {
        log.info("File not found: " + file);
      }
    } catch (IOException ioe) {
      log.error(ioe);
    } catch (NoClassDefFoundError ncdfe) {
      log.error(ncdfe);
    } catch (ClassNotFoundException cnfe) {
      log.error(cnfe);
    }

    return classes;
  }

  /**
   * 
   */
  protected Collection<Class<T>> getExternalModuleClasses(Properties settings, Class externalModuleClass) {
    Collection<Class<T>> classes = new HashSet<Class<T>>();

    ResourcePathClassLoader resLoader = new ResourcePathClassLoader(settings);

    // pathes to search
    Collection<String> paths = new ArrayList<String>();

    // a) read possible paths from ResourcePathClassLoader
    // b) read property java.class.path and iterate over the entries
    if (settings.getProperty("ExternalModuleLoader.searchResourcePathClassLoader", "true").equals("true")) {
      paths.addAll(getPathsFromResourcePathClassLoader(resLoader, settings));
    }

    if (settings.getProperty("ExternalModuleLoader.searchClassPath", "true").equals("true")) {
      paths.addAll(getPathsFromClassPath());
    }

    for (Iterator<String> iter = paths.iterator(); iter.hasNext();) {
      String path = iter.next();
      classes.addAll(getClassesFromPath(resLoader, externalModuleClass, path));
    }

    return classes;
  }

  /**
   * delivers last capitalized String, e.g.: for input "StringBuffer.class" this
   * function returns "Buffer.class"
   */
  protected String getLastCapitalizedString(String aString) {
    StringCharacterIterator iter = new StringCharacterIterator(aString);

    for (char c = iter.last(); c != CharacterIterator.DONE; c = iter.previous()) {
      if ((c >= 'A') && (c <= 'Z')) {
        if (log.isDebugEnabled()) {
          log.debug("ExternalModuleLoader.getLastCapitalizedString(" + aString + "): " + aString.substring(iter.getIndex()));
        }

        return aString.substring(iter.getIndex());
      }
    }

    return aString;
  }
}
