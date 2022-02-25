// class magellan.client.Help
// created on 15.11.2007
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
package magellan.client;

import java.awt.Dimension;
import java.awt.Point;
import java.net.URL;
import java.util.Properties;

import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.UnsupportedOperationException;

import magellan.library.utils.ResourcePathClassLoader;
import magellan.library.utils.logging.Logger;

/**
 * Helper Class for javax.help.HelpBroker.
 *
 * @author Thoralf Rickert, stm
 * @version 1.0, 15.11.2007
 */
public class Help {
  /**   */
  public static class HelpException extends Exception {

    /**
     * @see Exception#Exception(String)
     */
    public HelpException(String message) {
      super(message);
    }

    /**
     * @see Exception#Exception(String, Throwable)
     */
    public HelpException(String message, Throwable cause) {
      super(message, cause);
    }

  }

  private static final Logger log = Logger.getInstance(Help.class);
  private HelpBroker hb;

  /**
   * Opens the help dialog.
   *
   * @param settings
   */
  public Help(Properties settings) throws HelpException {

    try {
      ClassLoader loader = new ResourcePathClassLoader(settings);
      String language = settings.getProperty("locales.gui", "de");

      URL hsURL = loader.getResource("help/" + language + "/magellan.hs");
      if (hsURL == null) {
        hsURL = loader.getResource("help/magellan_" + language + ".hs");
      }
      if (hsURL == null) {
        hsURL = loader.getResource("help/magellan.hs");
      }
      if (hsURL == null) {
        hsURL = loader.getResource(language + "/magellan.hs");
      }
      if (hsURL == null) {
        hsURL = loader.getResource("magellan_" + language + ".hs");
      }
      if (hsURL == null) {
        hsURL = loader.getResource("magellan.hs");
      }
      if (hsURL == null) {
        Help.log.warn("Could not find magellan-help.jar");
        throw new HelpException("Could not find the magellan-help.jar");
      }

      HelpSet hs = new HelpSet(loader, hsURL);

      hb = hs.createHelpBroker();

      hb.initPresentation();

    } catch (Exception e) {
      throw new HelpException("Could not initialize the Java Help environment.", e);
    }
  }

  /**
   *
   */
  public void show() {
    hb.setDisplayed(true);
  }

  /**
   * Show the help and start a thread that waits until the help is not displayed any more and then
   * calls {@link System#exit(int)}.
   */
  public void showAndKeepAlive() {
    hb.setDisplayed(true);
    new Thread(new Runnable() {
      public void run() {
        while (hb.isDisplayed()) {
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            break;
          }
        }
        System.exit(0);
      }
    }).start();

  }

  /**
   * @see javax.help.HelpBroker#isDisplayed()
   */
  public boolean isDisplayed() {
    return hb.isDisplayed();
  }

  /**
   * @param arg0
   * @throws UnsupportedOperationException
   * @see javax.help.HelpBroker#setLocation(java.awt.Point)
   */
  public void setLocation(Point arg0) throws UnsupportedOperationException {
    hb.setLocation(arg0);
  }

  /**
   * @param arg0
   * @throws UnsupportedOperationException
   * @see javax.help.HelpBroker#setScreen(int)
   */
  public void setScreen(int arg0) throws UnsupportedOperationException {
    hb.setScreen(arg0);
  }

  /**
   * @param arg0
   * @throws UnsupportedOperationException
   * @see javax.help.HelpBroker#setSize(java.awt.Dimension)
   */
  public void setSize(Dimension arg0) throws UnsupportedOperationException {
    hb.setSize(arg0);
  }

}
