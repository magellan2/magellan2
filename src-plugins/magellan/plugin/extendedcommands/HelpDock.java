// class magellan.plugin.extendedcommands.ExtendedCommandsDialog
// created on 02.02.2008
//
// Copyright 2003-2008 by magellan project team
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
package magellan.plugin.extendedcommands;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import magellan.client.Client;
import magellan.client.Macifier;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * This is a dialog to display ExtendedCommands help
 * 
 * @author Thoralf Rickert
 * @version 1.0, 11.09.2007
 */
public class HelpDock extends JPanel implements ActionListener, HyperlinkListener {
  public static final String IDENTIFIER = "ExtendedCommandsHelp";
  private static Logger log = Logger.getInstance(HelpDock.class);

  private JEditorPane help = null;
  private JButton homeButton = null;
  private JButton backwardButton = null;
  private JButton forwardButton = null;
  private JButton browserButton = null;
  private List<URL> history = new ArrayList<URL>();
  private int pos = 0;

  public HelpDock() {
    init();
  }

  protected void init() {
    setLayout(new BorderLayout());

    JPanel helpPanel = new JPanel(new BorderLayout());
    help = new JEditorPane();
    help.setContentType("text/html");
    help.setEditable(false);
    help.addHyperlinkListener(this);
    goHome();
    help.setCaretPosition(0);
    JScrollPane scrollPane = new JScrollPane(help);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

    helpPanel.add(scrollPane, BorderLayout.CENTER);

    JPanel helpButtonPanel = new JPanel(new FlowLayout());
    homeButton = new JButton("Home");
    homeButton.setEnabled(false);
    homeButton.setActionCommand("button.home");
    homeButton.addActionListener(this);
    helpButtonPanel.add(homeButton);
    backwardButton = new JButton("<<");
    backwardButton.setEnabled(false);
    backwardButton.setActionCommand("button.backward");
    backwardButton.addActionListener(this);
    helpButtonPanel.add(backwardButton);
    forwardButton = new JButton(">>");
    forwardButton.setEnabled(false);
    forwardButton.setActionCommand("button.forward");
    forwardButton.addActionListener(this);
    helpButtonPanel.add(forwardButton);
    if (isBrowserAvailable()) {
      browserButton = new JButton("Browser");
      browserButton.setEnabled(true);
      browserButton.setActionCommand("button.browse");
      browserButton.addActionListener(this);
      helpButtonPanel.add(browserButton);
    }
    helpPanel.add(helpButtonPanel, BorderLayout.NORTH);

    add(helpPanel, BorderLayout.CENTER);
  }

  /**
   * This method is called, if one of the buttons is clicked.
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equalsIgnoreCase("button.home")) {
      goHome();
    } else if (e.getActionCommand().equalsIgnoreCase("button.backward")) {
      if (!homeButton.isEnabled()) {
        homeButton.setEnabled(true);
      }
      // move one position back in history
      if (pos > 0) {
        pos--;
        try {
          help.setPage(history.get(pos));
        } catch (Throwable exception) {
          log.error(exception);
        }
        forwardButton.setEnabled(true);
        if (pos == 0) {
          backwardButton.setEnabled(false);
        }
      }
    } else if (e.getActionCommand().equalsIgnoreCase("button.forward")) {
      if (!homeButton.isEnabled()) {
        homeButton.setEnabled(true);
      }
      // move one position forward in history
      if (pos < history.size()) {
        pos++;
        try {
          help.setPage(history.get(pos));
        } catch (Throwable exception) {
          log.error(exception);
        }
        backwardButton.setEnabled(true);
        if (pos == history.size() - 1) {
          forwardButton.setEnabled(false);
        }
      }
    } else if (e.getActionCommand().equalsIgnoreCase("button.browse")) {
      try {
        // Loads the new page represented by link clicked
        URI uri = help.getPage().toURI();

        Macifier.browse(uri);
      } catch (Throwable exception) {
        log.error(exception);
      }
    }
  }

  /**
   * @see javax.swing.event.HyperlinkListener#hyperlinkUpdate(javax.swing.event.HyperlinkEvent)
   */
  public void hyperlinkUpdate(HyperlinkEvent e) {
    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      try {
        // Loads the new page represented by link clicked
        URL url = e.getURL();
        help.setPage(url);
        pos++;
        history.add(pos, url);
        backwardButton.setEnabled(true);
        if (!homeButton.isEnabled()) {
          homeButton.setEnabled(true);
        }
      } catch (Throwable exception) {
        log.error(exception);
      }
    }
  }

  public boolean isBrowserAvailable() {
    return Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
  }

  /**
   * Sets the homepage.
   */
  protected void goHome() {
    File path = null;
    path =
        new File(Client.getResourceDirectory(), Resources
            .get("extended_commands.help.dialog.overview"));
    if (!path.exists()) {
      path = new File(Resources.get("extended_commands.help.dialog.overview"));
    }
    if (!path.exists()) {
      path = null;
    }

    try {
      if (path != null) {
        URL url = path.toURI().toURL();
        help.setPage(url);
        history.add(url);
      } else {
        help.setText("Unable to locate help files...");
      }
    } catch (Throwable e) {
      log.error(e);
      help.setText("Unable to locate help files...\n" + e.getMessage());
    }
  }
}
