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

package magellan.client.utils;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.zip.ZipFile;

import magellan.client.Client;
import magellan.library.utils.logging.Logger;

/**
 * Small class for start-up help. Two search functions: The Magellan directory and the settings
 * file.
 *
 * @author Andreas
 * @version 1.0
 */
public class MagellanFinder {
  private static final Logger log = Logger.getInstance(MagellanFinder.class);

  /**
   * Tries to create/read the settings file in <code>settDir</code> (first), the user's home
   * directory (second), <code>magDirectory</code> (third) or the current directory (last). The
   * first valid location is returned.
   *
   * @param magDirectory The magellan resource directory
   * @param settDir <code>null</code> or the magellan settings directory
   * @return The directory where the configuration files reside.
   */
  public static File findSettingsDirectory(File magDirectory, File settDir) {
    MagellanFinder.log.info("Searching for Magellan configuration:");
    if (settDir != null) {
      // explicit directory set: overrides defaults
      if (hasOrCreateSettings(settDir)) {
        MagellanFinder.log.info("Using directory '" + settDir.getAbsolutePath() + "'.");
        return settDir;
      } else {
        MagellanFinder.log.error("Cannot find or create settings file.");
        return null;
      }
    }
    File[] candidates =
        new File[] { getAppDataDirectory(), new File(System.getProperty("user.home")),
            magDirectory, new File(".") };
    File settFileDir = null;
    for (File dir : candidates) {
      if (dir != null && hasSettings(dir)) {
        settFileDir = dir;
        break;
      }
    }

    if (settFileDir != null) {
      MagellanFinder.log.info("Using directory '" + settFileDir.getAbsolutePath() + "'.");
    } else {
      for (File dir : candidates) {
        if (dir != null && hasOrCreateSettings(dir)) {
          settFileDir = dir;
          break;
        }
      }
      if (settFileDir != null) {
        MagellanFinder.log.info("Creating settings in " + settFileDir.getAbsolutePath());
      } else {
        MagellanFinder.log.error("Cannot create settings file.");
      }
    }

    return settFileDir;
  }

  public static File getAppDataDirectory() {
    String os = System.getProperty("os.name");
    if (os != null) {
      os = os.toUpperCase();
      String dir;
      if (os.contains("WIN")) {
        dir = System.getenv("APPDATA");
      } else if (os.contains("MAC")) {
        dir = System.getProperty("user.home") + "/Library/Application Support";
      } else {
        dir = System.getProperty("user.home");
      }
      if (dir != null && new File(dir).exists())
        if (os.contains("WIN") || os.contains("MAC"))
          return new File(dir, "Magellan");
        else
          return new File(dir, ".magellan");
    }
    return new File(System.getProperty("user.home"), ".magellan2");
  }

  private static boolean hasOrCreateSettings(File dir) {
    File oFile = new File(dir, ProfileManager.INIFILE);
    if (!dir.exists()) {
      dir.mkdir();
    }
    if (!dir.canWrite() && !dir.setWritable(true))
      return false;

    if (oFile.exists()) {
      if (oFile.length() == 0) {
        if (!oFile.delete())
          return false;
      } else if (!oFile.canWrite() && !oFile.setWritable(true))
        return false;

      return true;
    }
    try {
      if (oFile.createNewFile()) {
        oFile.delete();
        return true;
      } else
        return false;
    } catch (IOException e) {
      return false;
    }

  }

  /**
   * Tries to find either magellan.ini or profiles.ini in settFileDir
   *
   * @param settFileDir
   * @return <code>true</code> if one of the ini files was found
   */
  private static boolean hasSettings(File settFileDir) {
    File magFile = new File(settFileDir, Client.SETTINGS_FILENAME);

    StringBuffer msg = new StringBuffer();

    if (!settFileDir.exists() || !settFileDir.canWrite() || !magFile.exists()) {
      magFile = new File(settFileDir, ProfileManager.INIFILE);
      if (!settFileDir.exists() || !settFileDir.canWrite() || !magFile.exists()) {
        msg.append(magFile).append("... not found.");
        MagellanFinder.log.info(msg);
        return false;
      } else {
        msg.append(magFile).append("... found.");
        MagellanFinder.log.info(msg);
        return true;
      }
    } else {
      msg.append(magFile).append("... found.");
      MagellanFinder.log.info(msg);
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
        if (path.endsWith(".jar") && MagellanFinder.checkJar(path)) {
          File file = new File(MagellanFinder.extractDir(path));
          MagellanFinder.log.info("Magellan directory: " + file.getAbsolutePath() + "(found JAR)");

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
                if (MagellanFinder.checkJar(element)) {
                  MagellanFinder.log.info("Magellan directory: " + file.getAbsolutePath()
                      + "(found JAR)");

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
                  MagellanFinder.log.info("Magellan directory: " + file.getAbsolutePath()
                      + "(found magellan.client.Client class)");

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

    MagellanFinder.log.info("Magellan directory: " + new File(".").getAbsolutePath()
        + "(found JAR)");
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
    return MagellanFinder.checkJar(new File(file));
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
