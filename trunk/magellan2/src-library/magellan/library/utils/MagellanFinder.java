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

import java.io.File;
import java.util.StringTokenizer;
import java.util.zip.ZipFile;

import magellan.library.utils.logging.Logger;

/**
 * Small class for start-up help. Two search functions: The Magellan directory and the settings
 * file.
 * 
 * @author Andreas
 * @version 1.0
 * @deprecated moved to package magellan.client.utils
 */
@Deprecated
public class MagellanFinder {
  private static final Logger log = Logger.getInstance(MagellanFinder.class);

  /**
   * Tries to create/read the settings file in <code>settDir</code> (first), the user's home
   * directory (second), <code>magDirectory</code> (third) or the current directory (last). The
   * first valid location is returned.
   */
  public static File findSettingsDirectory(File magDirectory, File settDir) {
    log.info("Searching for Magellan configuration:");
    File settFileDir = settDir;
    if (settDir == null || !testFile(settFileDir)) {
      settFileDir = new File(System.getProperty("user.home"), ".magellan2");
      if (!testFile(settFileDir)) {
        settFileDir = new File(System.getProperty("user.home"));
        if (!testFile(settFileDir)) {
          settFileDir = magDirectory;
          if (!testFile(settFileDir)) {
            settFileDir = new File(".");
            if (!testFile(settFileDir)) {
              settFileDir = new File(System.getProperty("user.home"), ".magellan2");
            }
          }
        }
      }
    }
    File sFile = new File(settFileDir, "magellan.ini");
    File oFile = new File(settFileDir, "profiles.init");

    if (!sFile.exists() && !oFile.exists()) {
      StringBuilder msg =
          new StringBuilder("Using default directory ").append(settFileDir).append(".");
      log.info(msg);
      settFileDir = magDirectory;
    } else {
      log.info("Using directory '" + settFileDir.getAbsolutePath() + "'.");
    }

    return settFileDir;
  }

  /**
   * Tries to find either magellan.ini or profiles.ini in settFileDir
   * 
   * @param settFileDir
   * @return <code>true</code> if one of the ini files was found
   */
  private static boolean testFile(File settFileDir) {
    File magFile = new File(settFileDir, "magellan.ini");

    StringBuffer msg = new StringBuffer();

    if (!settFileDir.exists() || !settFileDir.canWrite() || !magFile.exists()) {
      magFile = new File(settFileDir, "profiles.ini");
      if (!settFileDir.exists() || !settFileDir.canWrite() || !magFile.exists()) {
        msg.append(magFile).append("... not found.");
        log.info(msg);
        return false;
      } else {
        msg.append(magFile).append("... found.");
        log.info(msg);
        return true;
      }
    } else {
      msg.append(magFile).append("... found.");
      log.info(msg);
      return true;
    }

  }

  /**
   * Searches for Magellan. This method scans the CLASSPATH and searches for JARs containing
   * "magellan.client.Client" or corresponding directory structures.
   */
  public static File findMagellanDirectory() {
    String classPath = System.getProperty("java.class.path", ".");
    StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);

    while (st.hasMoreTokens()) {
      String path = st.nextToken();

      // search for a jar
      try {
        if (path.endsWith(".jar") && checkJar(path)) {
          File file = new File(extractDir(path));
          log.info("Magellan directory: " + file + "(found JAR)");

          return file;
        } else {
          File file = new File(path);

          if (!file.isDirectory()) {
            file = file.getParentFile();
          }

          if (file.isDirectory()) {
            File list[] = file.listFiles();

            if (list.length > 0) {
              for (File element : list) {
                if (checkJar(element)) {
                  log.info("Magellan directory: " + file + "(found JAR)");

                  return file;
                }
              }
            }
          }
        }
      } catch (Exception exc) {
        // try something else
      }

      // search for the class
      try {
        File file = new File(path);

        if (!file.isDirectory()) {
          file = file.getParentFile();
        }

        File dir = new File(file, "magellan");

        if (dir.isDirectory()) {
          dir = new File(dir, "client");

          if (dir.isDirectory()) {
            String list[] = dir.list();

            if (list.length > 0) {
              for (String element : list) {
                if (element.equals("Client.class")) {
                  log.info("Magellan directory: " + file + "(found magellan.client.Client class)");

                  return file;
                }
              }
            }
          }
        }
      } catch (Exception exc2) {
        // try something else
      }
    }

    return new File(".");
  }

  /**
   * Extracts the directory out of the given file. If any error occurs, the current directory(".")
   * is returned.
   */
  protected static String extractDir(String file) {
    try {
      File f = new File(file);

      if (!f.isDirectory()) {
        f = f.getParentFile();
      }

      return f.toString();
    } catch (Exception exc) {
      // try something else
    }

    try {
      return new File(".").getAbsoluteFile().toString();
    } catch (Exception exc2) {
      // try something else
    }

    return ".";
  }

  /**
   * Checks if the given file is a zip and contains a "magellan/client/Client.class". These are the
   * conditions for the file to be a valid magellan Java Archive (JAR).
   */
  protected static boolean checkJar(String file) {
    return checkJar(new File(file));
  }

  /**
   * Checks if the given file is a zip and contains a "magellan/client/Client.class". These are the
   * conditions for the file to be a valid magellan Java Archive (JAR).
   */
  protected static boolean checkJar(File file) {
    try {
      ZipFile zipped = new ZipFile(file);

      if (zipped.getInputStream(zipped.getEntry("magellan/client/Client.class")) != null) {
        zipped.close();

        return true;
      }

      zipped.close();
    } catch (Exception inner) {
      // harmless
    }

    return false;
  }
}
