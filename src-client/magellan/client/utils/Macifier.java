// class magellan.client.Macifier
// created on Feb 1, 2022
//
// Copyright 2003-2022 by magellan project team
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
package magellan.client.utils;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.AboutHandler;
import java.awt.desktop.PreferencesEvent;
import java.awt.desktop.PreferencesHandler;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.QuitResponse;
import java.awt.image.BufferedImage;

import org.simplericity.macify.eawt.ApplicationEvent;
import org.simplericity.macify.eawt.ApplicationListener;
import org.simplericity.macify.eawt.DefaultApplication;

import magellan.client.Client;
import magellan.client.Client.QuitListener;
import magellan.library.GameData;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.utils.TrustLevels;
import magellan.library.utils.logging.Logger;

public class Macifier implements ApplicationListener, QuitHandler, AboutHandler, PreferencesHandler {

  private static Logger log = Logger.getInstance(Macifier.class);
  private Client client;

  /** Contains at startup the application icon of Magellan - most time only on a mac */
  private BufferedImage appIcon = null;
  private DefaultApplication application;
  private boolean isMac;
  private Desktop desktop;

  /**
   * @param client
   */
  public Macifier(Client client) {
    this.client = client;

    application = new DefaultApplication();
    isMac = application.isMac();
    if (isMac) {
      application.addPreferencesMenuItem();
      application.setEnabledPreferencesMenu(true);
      application.addAboutMenuItem();
      application.setEnabledAboutMenu(true);
      application.addApplicationListener(this);

      appIcon = application.getApplicationIconImage();
    }

    if (Desktop.isDesktopSupported()) {
      desktop = Desktop.getDesktop();

      for (Desktop.Action a : Desktop.Action.values()) {
        log.finer(a.toString() + ": " + desktop.isSupported(a));
      }
      if (desktop.isSupported(Desktop.Action.APP_SUDDEN_TERMINATION)) {
        log.fine("Sudden termination disabled.");
        desktop.disableSuddenTermination();
      }
      if (desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
        log.fine("Set quit handler");
        desktop.setQuitHandler(this);
      }
      if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
        log.fine("Set about handler");
        desktop.setAboutHandler(this);
      }
      if (desktop.isSupported(Desktop.Action.APP_PREFERENCES)) {
        log.fine("Set preferences handler");
        desktop.setPreferencesHandler(this);
      }
    }
  }

  /**
   * @return
   */
  public boolean isDesktopSupported() {
    return Desktop.isDesktopSupported();
  }

  public void handleQuitRequestWith(QuitEvent evt, QuitResponse res) {
    log.fine("Request to quit by " + (evt != null ? evt.getSource() : null));
    QuitListener ql = new QuitListener() {

      public void performQuit() {
        res.performQuit();
      }

      public void cancelQuit() {
        res.cancelQuit();
      }
    };
    client.quit(ql, true);

  }

  public void handlePreferences(PreferencesEvent e) {
    log.fine("Got preferences request");
    client.showPreferences();
  }

  public void handleAbout(AboutEvent e) {
    log.fine("Got about request");
    client.showInfoDialog();
  }

  public boolean isMac() {
    return isMac;
  }

  /**
   * Handles mac specific about menu action event
   *
   * @see org.simplericity.macify.eawt.ApplicationListener#handleAbout(org.simplericity.macify.eawt.ApplicationEvent)
   */
  public void handleAbout(ApplicationEvent event) {
    log.info("app open about event");
    event.setHandled(true);
    client.showInfoDialog();
  }

  /**
   * Handles mac specific open application event (I think, we don't use it)
   *
   * @see org.simplericity.macify.eawt.ApplicationListener#handleOpenApplication(org.simplericity.macify.eawt.ApplicationEvent)
   */
  public void handleOpenApplication(ApplicationEvent event) {
    log.info("app open event");
  }

  /**
   * Handles mac specific file open operation (if someone opens a cr in the finder)
   *
   * @see org.simplericity.macify.eawt.ApplicationListener#handleOpenFile(org.simplericity.macify.eawt.ApplicationEvent)
   */
  public void handleOpenFile(ApplicationEvent event) {
    log.info("app open file event");
  }

  /**
   * Handles mac specific preferences menu action event
   *
   * @see org.simplericity.macify.eawt.ApplicationListener#handlePreferences(org.simplericity.macify.eawt.ApplicationEvent)
   */
  public void handlePreferences(ApplicationEvent event) {
    log.info("app open prev event");
    event.setHandled(true);
    client.showPreferences();
  }

  /**
   * Handles mac specific print event
   *
   * @see org.simplericity.macify.eawt.ApplicationListener#handlePrintFile(org.simplericity.macify.eawt.ApplicationEvent)
   */
  public void handlePrintFile(ApplicationEvent event) {
    log.info("app print file event");
  }

  /**
   * Handles mac specific quit menu event
   *
   * @see org.simplericity.macify.eawt.ApplicationListener#handleQuit(org.simplericity.macify.eawt.ApplicationEvent)
   */
  public void handleQuit(ApplicationEvent event) {
    log.info("app quit event");
    event.setHandled(true);
    client.quit(true);
  }

  /**
   * Handles a reopen event - what ever that means
   *
   * @see org.simplericity.macify.eawt.ApplicationListener#handleReOpenApplication(org.simplericity.macify.eawt.ApplicationEvent)
   */
  public void handleReOpenApplication(ApplicationEvent event) {
    log.info("app reopen event");
  }

  ///////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Replaces the application icon in a Mac application with the default mac icon of Magellan. This
   * can be used, when the icon was changed (f.e. with the blue circle)
   *
   * @see #setAdditionalIconInfo(int)
   */
  public void setDefaultIconInfo() {
    if (application == null)
      return;
    if (appIcon == null) {
      appIcon = application.getApplicationIconImage();
      if (appIcon == null)
        return;
    }
    BufferedImage originalIcon = appIcon;

    BufferedImage newIcon = new BufferedImage(originalIcon.getWidth(), originalIcon.getHeight(),
        BufferedImage.TYPE_INT_ARGB);

    Graphics2D graphics = (Graphics2D) newIcon.getGraphics();
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    graphics.setColor(Color.decode("#0000E4"));

    graphics.drawImage(originalIcon, 0, 0, null);

    application.setApplicationIconImage(newIcon);
  }

  /**
   * Adds a small hint to the icon (only available on mac os)
   */
  public void setAdditionalIconInfo(int data) {
    if (application == null)
      return;
    if (appIcon == null) {
      // appIcon = application.getApplicationIconImage();
      if (appIcon == null)
        return;
    }
    BufferedImage originalIcon = appIcon;

    int width = originalIcon.getWidth() / 4;

    BufferedImage newIcon = new BufferedImage(originalIcon.getWidth(), originalIcon.getHeight(),
        BufferedImage.TYPE_INT_ARGB);

    Graphics2D graphics = (Graphics2D) newIcon.getGraphics();

    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    graphics.setColor(Color.decode("#0000E4"));

    graphics.drawImage(originalIcon, 0, 0, null);

    graphics.fillOval(originalIcon.getWidth() - width, 0, width - 5, width - 5);

    graphics.setColor(Color.WHITE);
    graphics.setFont(new Font("Helvetica", Font.BOLD, (width / 4)));
    graphics.drawString(Integer.toString(data), originalIcon.getWidth() - (width - (width / 4)),
        width - (int) (width / 2.5));

    graphics.dispose();

    application.setApplicationIconImage(newIcon);
  }

  public void updateAppIconCaption(GameData data) {
    if (data == null)
      return;

    int units = 0;
    int done = 0;

    for (Unit u : data.getUnits()) {
      if (TrustLevels.isPrivileged(u.getFaction())) {
        units++;

        if (u.isOrdersConfirmed()) {
          done++;
        }
      }

      // also count temp units
      for (TempUnit tempUnit : u.tempUnits()) {
        Unit u2 = tempUnit;

        if (TrustLevels.isPrivileged(u2.getFaction())) {
          units++;

          if (u2.isOrdersConfirmed()) {
            done++;
          }
        }
      }
    }

    if (units > 0 && done < units) {
      setAdditionalIconInfo(units - done);
    } else {
      setDefaultIconInfo();
    }
  }

}
