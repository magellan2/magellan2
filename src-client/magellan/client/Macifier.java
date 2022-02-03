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
package magellan.client;

import java.awt.Desktop;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.AboutHandler;
import java.awt.desktop.PreferencesEvent;
import java.awt.desktop.PreferencesHandler;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.QuitResponse;

import magellan.client.Client.QuitListener;
import magellan.library.utils.logging.Logger;

/**
 * Handles Desktop requests, mostly needed on Macs.
 *
 */
public class Macifier implements QuitHandler, AboutHandler, PreferencesHandler {
  private static Logger log = Logger.getInstance(Macifier.class);
  private Client client;

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
   * Returns whether {@link java.awt.Desktop} is supported on the current platform.
   */
  public boolean isDesktopSupported() {
    return Desktop.isDesktopSupported();
  }

  /**
   * @see java.awt.desktop.QuitHandler#handleQuitRequestWith(java.awt.desktop.QuitEvent, java.awt.desktop.QuitResponse)
   */
  public void handleQuitRequestWith(QuitEvent evt, QuitResponse res) {
    log.fine("Request to quit by " + (evt != null ? evt.getSource() : null));

    final boolean savingDone[] = new boolean[] { false };
    QuitListener ql = new QuitListener() {

      public void performQuit() {
        log.fine("Do quit");
        res.performQuit();
        savingDone[0] = true;
      }

      public void cancelQuit() {
        log.fine("Cancel quit");
        res.cancelQuit();
        savingDone[0] = true;
      }
    };

    client.quit(ql, true);

    // we tried to use Toolkit.getDefaultToolkit().getSystemEventQueue().createSecondaryLoop(), but it did not work on
    // MacOs due to "Invalid parameter not satisfying: [self canBeconMainWindow]"

    // do not return until shutdown is complete and either performQuit or cancelQuit has been called.
    while (!savingDone[0]) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        // try again
      }
    }
  }

  /**
   * @see java.awt.desktop.PreferencesHandler#handlePreferences(java.awt.desktop.PreferencesEvent)
   */
  public void handlePreferences(PreferencesEvent e) {
    log.fine("Got preferences request");
    client.showPreferences();
  }

  /**
   * @see java.awt.desktop.AboutHandler#handleAbout(java.awt.desktop.AboutEvent)
   */
  public void handleAbout(AboutEvent e) {
    log.fine("Got about request");
    client.showInfoDialog();
  }

}
