// class magellan.library.utils.MagellanURL
// created on Apr 12, 2013
//
// Copyright 2003-2013 by magellan project team
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ListResourceBundle;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import magellan.library.utils.logging.Logger;

/**
 * Utility class maintaining important URLs for Magellan.
 * 
 * @author stm
 */
public class MagellanUrl {
  private static final Logger log = Logger.getInstance(MagellanUrl.class);

  /**
   * This URL should point to a file containing all other URLs (in properties file format: <br />
   * <code>
   * key1 = value1<br />
   * key2 = value2<br />
   * # ....
   * </code>
   */
  public static final String MAGELLAN_LOCATIONS_URL =
      "http://magellan-client.sourceforge.net/locations.properties";

  /** Time to wait between connection attempts. */
  private static final long HTTP_TIMEOUT = 3600000;

  private static long lastAccessed = 0;

  private static ResourceBundle bundle, defaultBundle;

  static {
    initResourceBundle();
  }

  private static void initResourceBundle() {
    defaultBundle = new ListResourceBundle() {
      @Override
      protected Object[][] getContents() {
        return new Object[][] { { "www.root", "http://magellan-client.sourceforge.net" },
            { "www.bugtracker", "http://sourceforge.net/bugs" },
            { "www.homepage.alt", "http://magellan.narabi.de" },
            { "www.download", "http://magellan.narabi.de/download_en.php" },
            { "www.download.de", "http://magellan.narabi.de/download_de.php" },
            { "www.download.en", "http://magellan.narabi.de/download_en.php" },
            { "www.files", "http://sourceforge.net/projects/magellan-client/files/" },
            { "www.fernando", "http://en.wikipedia.org/wiki/Ferdinand_Magellan" },
            { "version.release", "http://magellan.narabi.de/release/VERSION" },
            { "version.nightly", "http://magellan.narabi.de/nightly-build/VERSION" } };
      }
    };

    bundle = defaultBundle;
  }

  /**
   * Returns a URL, if possible fetched from the remote file at {@link #MAGELLAN_LOCATIONS_URL}.
   * Current url keys include "www.root", "www.bugtracker", "www.download", "version.release",
   * "version.nightly".
   * 
   * @param key
   * @return The url for the key, <code>null</code> if it could not be found and isn't one of the
   *         default values.
   */
  public static String getMagellanUrl(String key) {
    try {
      if (lastAccessed == 0) {
        log.warn("URLs not initialized");
      }
      return bundle.getString(key);
    } catch (Exception e) {
      log.warn("Could not find Magellan URL " + key, e);
      try {
        return defaultBundle.getString(key);
      } catch (MissingResourceException mre) {
        log.warn("not in default", mre);
        return null;
      }
    }
  }

  /**
   * Tries to access the locations file remotely and read the values.
   * 
   * @param properties Client settings, see {@link HTTPClient#HTTPClient(Properties)}
   * @see HTTPClient
   */
  public static void retrieveLocations(Properties properties) {
    retrieveLocations(properties, false);
  }

  /**
   * Tries to access the locations file remotely and read the values.
   * 
   * @param properties Client settings, see {@link HTTPClient#HTTPClient(Properties)}
   * @param force If this is <code>true</code>, the timeout is ignored.
   * @see HTTPClient
   */
  public static void retrieveLocations(Properties properties, boolean force) {
    if (force || System.currentTimeMillis() - lastAccessed > HTTP_TIMEOUT) {
      lastAccessed = System.currentTimeMillis();
      try {
        HTTPClient client = new HTTPClient(properties);
        HTTPResult result = client.get(MAGELLAN_LOCATIONS_URL);
        if (result != null && result.getStatus() == 200) {
          // okay, lets get the version from the downloaded file
          InputStream inputStream = new ByteArrayInputStream(result.getResult());
          bundle = new PropertyResourceBundle(inputStream);
        }
        if (client.isConnectionFailed()) {
          log.info("could not connect to magellan locations " + MAGELLAN_LOCATIONS_URL);
        }
      } catch (Exception exception) {
        log.info("", exception);
        initResourceBundle();
      }
    }
  }

  /**
   * Returns Magellan's primary WWW URL.
   */
  public static String getRootUrl() {
    return getMagellanUrl("www.root");
  }

}
