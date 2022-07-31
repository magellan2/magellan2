// class magellan.client.utils.ProfileManager
// created on Jul 16, 2010
//
// Copyright 2003-2010 by magellan project team
//
// Author : stm
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
package magellan.client.utils;

import java.awt.Frame;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import magellan.client.Client;
import magellan.client.desktop.MagellanDesktop;
import magellan.library.utils.OrderedOutputProperties;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * Handles multiple profiles for settings.
 *
 * @author stm
 */
public class ProfileManager {
  private static final Logger log = Logger.getInstance(ProfileManager.class);

  /**
   * Informs about problems during profile handling.
   */
  public static class ProfileException extends Exception {

    /**
     * Creates a new Exception with specified message and cause.
     *
     * @see Exception#Exception(String, Throwable)
     * @param message
     * @param cause
     */
    public ProfileException(String message, Throwable cause) {
      super(message, cause);
    }

    /**
     * Creates a new Exception with specified message.
     *
     * @see Exception#Exception(String)
     * @param message
     */
    public ProfileException(String message) {
      super(message);
    }

  }

  /**
   * The name of the profile configuration file.
   */
  public static final String INIFILE = "profiles.ini";

  protected static final String XCMD_FILENAME = "extendedcommands.xml";

  // constants for keys and values
  protected static final String CURRENT_PROFILE = "profile.current";
  protected static final String ASK_ALWAYS = "alwaysask";
  protected static final String PROFILE_PREFIX = "profile.";
  protected static final String NAME = ".name";
  protected static final String DIRECTORY = ".directory";

  private static final String DEFAULT = "default";

  private static Properties settings;

  private static File settingsDir;

  private static File settingsFile;

  /**
   * Reads the profile information from the {@link #INIFILE}. If there is no inifile in
   * <code>parameters.settingsDir</code>, new settings are created with a default profile.
   *
   * @param parameters
   * @return The name of the current profile. <code>null</code> if there was an I/O error while
   *         reading the inifile
   */
  public static String init(Client.Parameters parameters) {
    settings = new OrderedOutputProperties();
    settings.clear();

    settingsDir = parameters.settingsDir;
    settingsFile = new File(parameters.settingsDir, INIFILE);

    // load settings from file
    if (settingsFile.exists()) {
      try {
        settings.loadFromXML(new BufferedInputStream(new FileInputStream(settingsFile)));
        log.info("Found profiles.ini: " + settingsFile);
        checkProfiles();
      } catch (IOException e) {
        log.error("Error while loading " + settingsFile, e);
        return null;
      }
    }

    if (getProfiles().isEmpty()) {
      // try to create a default directory 20 times
      Random r = new Random();
      File dir = new File(getSettingsDirectory(), DEFAULT);
      for (int i = 0; i < 20 && !dir.mkdir(); ++i) {
        dir = new File(getSettingsDirectory(), DEFAULT + r.nextInt());
      }
      if (dir.isDirectory() && dir.canWrite()) {
        settings.setProperty(CURRENT_PROFILE, dir.getName());
        settings.setProperty(ASK_ALWAYS, "1");
        settings.setProperty(PROFILE_PREFIX + dir.getName() + NAME, dir.getName());
        settings.setProperty(PROFILE_PREFIX + dir.getName() + DIRECTORY, dir.getName());
        copyLegacy();
      } else {
        log.warn("could not create profile directory in " + getSettingsDirectory());
        return null;
      }
    }
    if (parameters.profile != null) {
      if (!setProfile(parameters.profile))
        return null;
    }
    return getCurrentProfile();
  }

  /**
   * Moves settings files from {@link #settingsDir} to the profile dir.
   */
  @SuppressWarnings("deprecation")
  private static void copyLegacy() {
    try {
      mvFile(Client.SETTINGS_FILENAME);
      mvFile(Client.COMPLETIONSETTINGS_FILENAME);
      mvFile(MagellanDesktop.DOCKING_LAYOUT_FILE);
      mvFile(XCMD_FILENAME); // ExtendedCommands.COMANDFILENAME
    } catch (Exception e) {
      log.error("Could not copy legacy settings to profile directory.");
    }

  }

  private static void mvFile(String settingsFilename) {
    File dest = new File(getProfileDirectory(), settingsFilename);
    File iniFile = new File(settingsDir, settingsFilename);
    if (iniFile.exists() && iniFile.isFile()) {
      if (iniFile.renameTo(dest)) {
        log.info("Moved " + iniFile + " to " + dest);
      } else {
        log.info("Could not move " + iniFile + " to " + dest);
      }
    }
  }

  /**
   * Returns the directory of the current profile.
   *
   * @return The directory of the current profile.
   */
  public static File getProfileDirectory() {
    return getProfileDirectory(getCurrentProfile());
  }

  /**
   * Returns the directory of the specified profile.
   *
   * @param name A profile name
   * @return The directory of the specified profile.
   */
  public static File getProfileDirectory(String name) {
    if (settingsDir == null || name == null)
      return null;
    String file = settings.getProperty(PROFILE_PREFIX + name + DIRECTORY);
    if (file == null)
      return null;
    return new File(settingsDir, file);
  }

  /**
   * Returns the currently active profile.
   *
   * @return The currently active profile.
   */
  public static String getCurrentProfile() {
    return settings.getProperty(CURRENT_PROFILE);
  }

  /**
   * Returns <code>true</code> if the "always ask at startup" property is set.
   *
   * @return <code>true</code> if the "always ask at startup" property is set.
   */
  public static boolean isAlwaysAsk() {
    return !"0".equals(settings.getProperty(ASK_ALWAYS));
  }

  /**
   * Sets the "always ask at startup" property.
   *
   * @param newValue
   */
  public static void setAlwaysAsk(boolean newValue) {
    settings.setProperty(ASK_ALWAYS, newValue ? "1" : "0");
  }

  /**
   * Shows a dialog to manage profiles.
   *
   * @param parent The parent frame for the JDialog.
   * @return <code>true</code> if the dialog was confirmed
   */
  public static boolean showProfileChooser(Frame parent) {
    ProfileDialog dlg = new ProfileDialog(parent);
    dlg.setVisible(true);
    return dlg.getResult();
  }

  protected static void checkProfiles() {
    for (String name : getProfiles()) {
      File dir = getProfileDirectory(name);
      if (dir == null || !dir.canRead() || !dir.canWrite()) {
        try {
          remove(name, false);
          if (name.equals(getCurrentProfile())) {
            settings.remove(CURRENT_PROFILE);
          }
        } catch (ProfileException e) {
          log.error("error impossible"); // only while deleting
        }
      }
    }
  }

  /**
   * Returns a list of known profiles.
   *
   * @return a list of known profiles.
   */
  public static Collection<String> getProfiles() {
    List<String> result = new ArrayList<String>();
    for (Object o : settings.keySet()) {
      String key = (String) o;
      if (key.startsWith(PROFILE_PREFIX))
        if (key.endsWith(NAME)) {
          String name = settings.getProperty(key);
          if (name != null && !name.isBlank()) {
            result.add(name);
          }
        }
    }
    Collections.sort(result);
    return result;
  }

  /**
   * Removes a profile. Tries to delete the profile directory if <code>removFiles</code> is
   * <code>true</code>.
   *
   * @param name
   * @param removeFiles
   * @return true if there was such a profile and it has been removed.
   * @throws ProfileException if the profile directory could not be removed
   */
  public static boolean remove(String name, boolean removeFiles) throws ProfileException {
    String prefix = PROFILE_PREFIX + name;
    if (settings.getProperty(prefix + NAME) != null) {
      File dir = getProfileDirectory(name);
      for (Iterator<?> iterator = settings.keySet().iterator(); iterator.hasNext();) {
        String key = (String) iterator.next();
        if (key.startsWith(prefix)) {
          iterator.remove();
        }
      }
      if (removeFiles) {
        boolean ok = true;
        try {
          for (File f : dir.listFiles()) {
            ok &= f.delete();
          }
          ok &= dir.delete();
        } catch (SecurityException e) {
          ok = false;
        } catch (NullPointerException e) {
          ok = false;
        }
        if (!ok)
          throw new ProfileException(Resources.get("profilemanager.exc.nodelete", name));
      }
      return true;
    } else
      return false;
  }

  /**
   * Adds a new profile and creates its directory. If <code>copyFrom</code> is not <code>null</code>
   * , all files from the corresponding directory are copied to the new profile directory.
   *
   * @param name A non-blank name
   * @param copyFrom
   * @throws ProfileException if the profile directory could not be created, or the files could not
   *           be copied
   */
  public static void add(String name, String copyFrom) throws ProfileException {
    if (settings.getProperty(PROFILE_PREFIX + name + NAME) != null)
      return;
    if (name == null || name.isBlank())
      return;

    File outDir = new File(settingsDir, name);
    if (outDir.isDirectory()) {
      log.warn(outDir + " already exists.");
    }

    if (copyFrom != null) {
      if (settings.getProperty(PROFILE_PREFIX + copyFrom + DIRECTORY) == null)
        throw new ProfileException(Resources.get("profilemanager.exc.dirdoesnotexist", copyFrom));
      File inDir =
          new File(settingsDir, settings.getProperty(PROFILE_PREFIX + copyFrom + DIRECTORY));
      if (!inDir.exists())
        throw new ProfileException(Resources.get("profilemanager.exc.dirdoesnotexist", inDir));
      else {
        for (File f : inDir.listFiles()) {
          if (!f.isFile() || !f.canRead())
            throw new ProfileException(Resources.get("profilemanager.exc.couldnotreadfile", f));
          else {
            if (!outDir.isDirectory() && !outDir.mkdir())
              throw new ProfileException(Resources.get(
                  "profilemanager.exc.couldnotcreatedirectory", outDir.toString()));
            File outFile = new File(outDir, f.getName());
            if (outFile.exists())
              throw new ProfileException(Resources.get("profilemanager.exc.fileexists", outFile
                  .toString()));

            try {
              if (outFile.createNewFile()) {
                InputStream is = new BufferedInputStream(new FileInputStream(f));
                OutputStream os = new BufferedOutputStream(new FileOutputStream(outFile));
                int b = 0;
                while ((b = is.read()) != -1) {
                  os.write(b);
                }
                os.close();
                is.close();
              } else {
                log.warn("could not copy " + outFile);
              }
            } catch (FileNotFoundException e) {
              throw new ProfileException(Resources.get("profilemanager.exc.filenotfound", f
                  .toString()));
            } catch (IOException e) {
              throw new ProfileException(Resources
                  .get("profilemanager.exc.ioerror", e.getMessage()));
            } catch (SecurityException e) {
              throw new ProfileException(Resources
                  .get("profilemanager.exc.security", f.toString()));
            }
          }
        }
      }
    } else if (!outDir.mkdir() && !outDir.isDirectory())
      throw new ProfileException(Resources
          .get("profilemanager.exc.couldnotcreatedirectory", outDir));
    settings.setProperty(PROFILE_PREFIX + name + NAME, name);
    settings.setProperty(PROFILE_PREFIX + name + DIRECTORY, name);
  }

  /**
   * Changes the active profile.
   *
   * @param name
   * @return <code>true</code> if the specified profile exists.
   */
  public static boolean setProfile(String name) {
    if (settings.getProperty(PROFILE_PREFIX + name + NAME) == null)
      return false;
    settings.setProperty(CURRENT_PROFILE, name);
    return true;
  }

  /**
   * Returns the settings directory where {@link #INIFILE} is stored.
   *
   * @return the settings directory
   */
  public static File getSettingsDirectory() {
    return settingsDir;
  }

  /**
   * Writes the settings to the {@link #INIFILE} in the settings directory.
   *
   * @throws IOException if an I/O error occurs
   */
  public static void saveSettings() throws IOException {
    BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(settingsFile));
    settings.storeToXML(os, "Magellan profile settings");
    os.close();
  }

}
