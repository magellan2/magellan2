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

import java.util.Properties;

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
      "https://magellan2.github.io/api/locations";

  /** Time to wait between connection attempts. */
  private static final long HTTP_TIMEOUT = 3600000;

  /** Key for basic Magellan URL */
  public static final String WWW_ROOT = "www.root";

  /** Key for Magellan bugtracker URL */
  public static final String WWW_BUGS = "www.bugtracker";

  /** Key for nightly version */
  public static final String VERSIONS = "api.versions";

  /** Key for download URL */
  public static final String WWW_DOWNLOAD = "www.download";

  /** Key for files URL */
  public static final String WWW_FILES = "www.files";

  private static long lastAccessed = 0;

  private static Properties bundle, defaultBundle;

  static {
    initResourceBundle();
  }

  private static void initResourceBundle() {
    defaultBundle = new Properties();

    for (String[] keyVal : new String[][] { { WWW_ROOT, "https://magellan2.github.io" },
        { WWW_BUGS, "https://magellan2.github.io/bugs" },
        { WWW_DOWNLOAD, "https://magellan2.github.io/de/download/" },
        { "www.download.de", "https://magellan2.github.io/de/download/" },
        { "www.download.en", "https://magellan2.github.io/en/download/" },
        { WWW_FILES, "https://github.com/magellan2/magellan2/releases" },
        { "www.fernando", "https://de.wikipedia.org/wiki/Ferdinand_Magellan" },
        { VERSIONS, "https://magellan2.github.io/api/versions" } }) {
      defaultBundle.put(keyVal[0], keyVal[1]);
    }

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
    if (lastAccessed == 0) {
      log.warn("URLs not initialized");
    }
    String url = bundle.getProperty(key);
    if (url != null)
      return url;

    log.warn("Could not find Magellan URL " + key);
    return defaultBundle.getProperty(key);
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
          bundle = JsonAdapter.parsePropertiesMap(result.getResult());
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
    return getMagellanUrl(WWW_ROOT);
  }

}
