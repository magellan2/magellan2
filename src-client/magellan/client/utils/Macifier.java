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

import java.awt.Desktop;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.AboutHandler;
import java.awt.desktop.PreferencesEvent;
import java.awt.desktop.PreferencesHandler;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.QuitResponse;
import java.awt.image.BufferedImage;

import magellan.client.Client;
import magellan.client.Client.QuitListener;
import magellan.library.utils.logging.Logger;

public class Macifier implements QuitHandler, AboutHandler, PreferencesHandler {

  private static Logger log = Logger.getInstance(Macifier.class);
  private Client client;

  /** Contains at startup the application icon of Magellan - most time only on a mac */
  private BufferedImage appIcon = null;
  private Desktop desktop;

  /**
   * @param client
   */
  public Macifier(Client client) {
    this.client = client;

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

}
