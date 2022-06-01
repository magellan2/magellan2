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

import java.awt.BorderLayout;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.net.URL;
import java.util.Properties;

import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.JHelpContentViewer;
import javax.help.JHelpNavigator;
import javax.help.UnsupportedOperationException;
import javax.swing.JDialog;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

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
  private static Help help;
  private HelpBroker hb;
  private HelpSet hs;

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

      hs = new HelpSet(loader, hsURL);

      hb = hs.createHelpBroker();

      hb.initPresentation();

      if (help == null) {
        help = this;
      }

    } catch (Exception e) {
      throw new HelpException("Could not initialize the Java Help environment.", e);
    }
  }

  /**
   *
   */
  public void show() {
    hb.setDisplayed(true);

    // new CSH.DisplayHelpAfterTracking(hb).actionPerformed(new ActionEvent(Client.INSTANCE, 0, "HELP"));

    // try {
    // hb.showID("intro", "javax.help.MainWindow", "main window");
    // } catch (Exception ee) {
    // System.err.println("trouble with visiting id; " + ee);
    // }
  }

  /**
   * Creates a JDialog showing the given topic and a navigation
   * 
   */
  public JDialog getHelpDialog(Window parent, String topic) {
    JHelpContentViewer cv = new JHelpContentViewer(hs);

    JHelpNavigator mnav = (JHelpNavigator) hs.getNavigatorView("TOC").createNavigator(cv.getModel());
    JHelpNavigator enav = (JHelpNavigator) hs.getNavigatorView("ETOC").createNavigator(cv.getModel());
    JHelpNavigator snav = (JHelpNavigator) hs.getNavigatorView("Search").createNavigator(cv.getModel());

    cv.setCurrentID(topic);

    JDialog jd = new JDialog(parent);
    jd.setModalityType(ModalityType.MODELESS);
    jd.setModalExclusionType(ModalExclusionType.TOOLKIT_EXCLUDE);

    jd.setLayout(new BorderLayout());

    JTabbedPane tabs = new JTabbedPane();
    // Icon chap = new ImageIcon("help/de/images/chapTopic.gif");
    tabs.addTab("\uD83D\uDD6E", null, mnav, mnav.getNavigatorLabel());
    tabs.addTab("\uD83D\uDD6E", null, enav, enav.getNavigatorLabel());
    tabs.addTab("\uD83D\uDD0D", null, snav, snav.getNavigatorLabel());

    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabs, cv);
    split.setOneTouchExpandable(true);
    split.setDividerLocation(.1);
    jd.add(split, BorderLayout.CENTER);

    jd.pack();
    jd.setSize(800, 450);
    return jd;
  }

  public static void main(String[] args) {
    // Demo
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        try {
          Properties settings = new Properties();
          Help h = new Help(settings);
          JDialog d = h.getHelpDialog(null, "intro");
          d.setVisible(true);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    });
  }

  /**
   * Displays the topic with the given ID.
   */
  public void showTopic(String id) {
    hb.setDisplayed(true);
    hb.setCurrentID(id);
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

  /**
   * Returns a (weakly) unique instance.
   * 
   * @param settings
   * @throws HelpException
   */
  public static Help getInstance(Properties settings) throws HelpException {
    if (help == null) {
      help = new Help(settings);
    }
    return help;
  }

}
