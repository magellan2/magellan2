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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import magellan.library.utils.logging.Logger;

/**
 * A class for retrieving versioning information about Magellan.
 */
public class VersionInfo {
  private static final Logger log = Logger.getInstance(VersionInfo.class);

  /** Boolean Property to set, if UpdateCheck should be done */
  public static final String PROPERTY_KEY_UPDATECHECK_CHECK = "UpdateCheck.Check";
  /** Timestamp of failed UpdateCheck */
  public static final String PROPERTY_KEY_UPDATECHECK_FAILED = "UpdateCheck.Failed";
  /** ResourceKey to show that the update process failed */
  public static final String RESOURCE_KEY_NOUPDATE_AVAIL = "versioninfo.infodlg.updatefailed";

  private static final String DEFAULT_VERSION_URL = "https://magellan2.github.io/api/versions";

  private static String Version;
  private static String lastKey;

  /**
   * Gets the Version of this Instance.
   *
   * @deprecated replaced by {@link #getSemanticVersion(File)}
   */
  @Deprecated
  public static String getVersion(File magellanDirectory) {
    return getVersion(magellanDirectory, false);
  }

  /**
   * Returns the current version of this instance.
   */
  public static String getSemanticVersion(File magellanDirectory) {
    return getVersion(magellanDirectory, true);
  }

  protected static String getVersion(File magellanDirectory, boolean useSemanticVersion) {
    if (!useSemanticVersion)
      return getVersion(magellanDirectory, "VERSION");
    else
      return getVersion(magellanDirectory, "SEMANTIC_VERSION");
  }

  protected static String getVersion(File magellanDirectory, String key) {
    if (key.equals(VersionInfo.lastKey))
      return VersionInfo.Version;
    if (magellanDirectory == null)
      return null;
    VersionInfo.lastKey = key;
    try {
      File versionFile = new File(magellanDirectory, "etc/VERSION");
      if (!versionFile.exists()) {
        // hmmm, maybe one directory level up (special Eclipse problem with bin directory)
        versionFile = new File(magellanDirectory.getParentFile(), "etc/VERSION");
        if (!versionFile.exists()) {
          // last try, eclipse special...try just Mag2 Dir
          versionFile = new File(magellanDirectory.getParentFile(), "VERSION");
          if (!versionFile.exists())
            // okay, I'll give up...
            return null;
        }
      }

      ResourceBundle bundle = new PropertyResourceBundle(new FileInputStream(versionFile));
      VersionInfo.Version = bundle.getString(key);
      return VersionInfo.Version;
    } catch (IOException e) {
      // do nothing, not important
    } catch (MissingResourceException e) {
      // do nothing, not important
    }

    return null;
  }

  /**
   * This method tries to get the newest Version from the webserver.
   */
  public static String getNewestVersion(Properties properties, JFrame parent) {

    if (properties == null)
      return null;
    boolean check =
        Boolean.valueOf(properties.getProperty(VersionInfo.PROPERTY_KEY_UPDATECHECK_CHECK, String
            .valueOf(true)));

    MagellanUrl.retrieveLocations(properties);
    String versionsUrl = MagellanUrl.getMagellanUrl(MagellanUrl.VERSIONS);
    if (versionsUrl == null) {
      versionsUrl = VersionInfo.DEFAULT_VERSION_URL;
    }
    long failedTimestamp =
        Long.valueOf(properties.getProperty(VersionInfo.PROPERTY_KEY_UPDATECHECK_FAILED, String
            .valueOf(0)));

    boolean doCheck = check;

    // if the last failed time was now-7 days, then we try to check again.
    if (failedTimestamp > 0l && doCheck) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis(failedTimestamp);
      calendar.add(Calendar.DATE, 7);
      if (calendar.before(Calendar.getInstance())) {
        doCheck = true;
      } else {
        doCheck = false;
      }
    }

    String newestVersion = null;

    // make a connection and try to check....
    if (doCheck) {
      try {
        HTTPClient client = new HTTPClient(properties);
        HTTPResult result = client.get(versionsUrl);
        if (result != null && result.getStatus() == 200) {
          Properties props = JsonAdapter.parsePropertiesMap(result.getResult(), true);
          newestVersion = props.getProperty("versions.stable.raw");
          if (!Utils.isEmpty(newestVersion) && newestVersion.charAt(0) == 'v') {
            newestVersion = newestVersion.substring(1);
          }
        }
        if (client.isConnectionFailed() && !(parent == null)) {
          JOptionPane.showMessageDialog(parent, Resources
              .get(VersionInfo.RESOURCE_KEY_NOUPDATE_AVAIL));
        }
      } catch (Exception exception) {
        VersionInfo.log.info("", exception);
        failedTimestamp = Calendar.getInstance().getTimeInMillis();
        if (parent != null) {
          JOptionPane.showMessageDialog(parent, Resources
              .get(VersionInfo.RESOURCE_KEY_NOUPDATE_AVAIL));
        }
      }
    }

    properties.setProperty(VersionInfo.PROPERTY_KEY_UPDATECHECK_CHECK, String.valueOf(check));
    properties.setProperty(VersionInfo.PROPERTY_KEY_UPDATECHECK_FAILED, String
        .valueOf(failedTimestamp));

    return newestVersion;
  }

  /**
   * Returns rue if firstVersion is strictly greater than secondVersion.
   * 
   */
  public static boolean isNewer(String firstVersion, String secondVersion) {
    if (Utils.isEmpty(secondVersion) || Utils.isEmpty(firstVersion))
      return false;

    VersionInfo.log.debug("Current: " + secondVersion);
    VersionInfo.log.debug("Newest : " + firstVersion);

    Version a = new Version(firstVersion);
    Version b = new Version(secondVersion);
    return a.isNewer(b);
  }
}
