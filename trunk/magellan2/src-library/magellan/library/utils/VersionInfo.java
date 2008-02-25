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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import magellan.library.utils.logging.Logger;


/**
 * A class for retrieving versioning information about Magellan.
 */
public class VersionInfo {
	private static final Logger log = Logger.getInstance(VersionInfo.class);
  
  public static final String PROPERTY_KEY_UPDATECHECK_CHECK  = "UpdateCheck.Check";
  public static final String PROPERTY_KEY_UPDATECHECK_URL    = "UpdateCheck.URL";
  public static final String PROPERTY_KEY_UPDATECHECK_FAILED = "UpdateCheck.Failed";
  
  private static final String DEFAULT_CHECK_URL = "http://magellan.log-out.net/release/VERSION";

  private static String Version = null;
  private static boolean versionIsSet=false;
  
  
	/**
	 * Gets the Version of this Instance.
	 */
	public static String getVersion(File magellanDirectory) {
	  if (versionIsSet){
	    return Version;
	  }
	  if (magellanDirectory==null){
	    return null;
	  }
	  versionIsSet=true;
		try {
		  File versionFile = new File(magellanDirectory,"etc/VERSION");
		  if (!versionFile.exists()) {
	      // hmmm, maybe one directory level up (special Eclipse problem with bin directory)
	      versionFile = new File(magellanDirectory.getParentFile(),"etc/VERSION");
	      if (!versionFile.exists()) {
	        // last try, eclipse special...try just Mag2 Dir
	        versionFile = new File(magellanDirectory.getParentFile(),"VERSION");
	        if (!versionFile.exists()) {
  	        // okay, I'll give up...
  	        return null;
	        }
	      }
	    }
		  
			// ResourceBundle bundle = new PropertyResourceBundle(new FileInputStream("etc/VERSION"));
			ResourceBundle bundle = new PropertyResourceBundle(new FileInputStream(versionFile));
			Version = bundle.getString("VERSION");
			return Version;
		} catch(IOException e) {
		} catch(MissingResourceException e) {
		}

		return null;
	}
  
  /**
   * This method tries to get the newest Version from the webserver.
   */
  public static String getNewestVersion(Properties properties) {
    if (properties == null) return null;
    String urlstring = properties.getProperty(PROPERTY_KEY_UPDATECHECK_URL,DEFAULT_CHECK_URL);
    boolean check = new Boolean(properties.getProperty(PROPERTY_KEY_UPDATECHECK_CHECK,String.valueOf(true)));
    long failedTimestamp = new Long(properties.getProperty(PROPERTY_KEY_UPDATECHECK_FAILED,String.valueOf(0)));
    
    boolean doCheck = check;
    
    if (urlstring.length()<1) urlstring = DEFAULT_CHECK_URL;
    
    // reset url string to new location
    properties.setProperty(PROPERTY_KEY_UPDATECHECK_URL, DEFAULT_CHECK_URL);
    
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
        HTTPResult result = client.get(urlstring);
        if (result != null && result.getStatus() == 200) {
          // okay, lets get the version from the downloaded file
          InputStream inputStream = new ByteArrayInputStream(result.getResult());
          ResourceBundle bundle = new PropertyResourceBundle(inputStream);
          newestVersion = bundle.getString("VERSION");
        }
      } catch (Exception exception) {
        log.info("",exception);
        failedTimestamp = Calendar.getInstance().getTimeInMillis();
      }
    }
    
    
    properties.setProperty(PROPERTY_KEY_UPDATECHECK_URL,    urlstring);
    properties.setProperty(PROPERTY_KEY_UPDATECHECK_CHECK,  String.valueOf(check));
    properties.setProperty(PROPERTY_KEY_UPDATECHECK_FAILED, String.valueOf(failedTimestamp));
    
    return newestVersion;
  }
  
  public static boolean isNewer(String currentVersion, String newVersion) {
    if (Utils.isEmpty(currentVersion) || Utils.isEmpty(newVersion)) return false;
    
    log.debug("Current: "+currentVersion);
    log.debug("Newest : "+newVersion);
    
    Version a = new Version(currentVersion,".",false);
    Version b = new Version(newVersion,".",false);
    return b.isNewer(a);
  }
}
