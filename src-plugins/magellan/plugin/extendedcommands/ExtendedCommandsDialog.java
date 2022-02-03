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
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import magellan.client.Client;
import magellan.client.Macifier;
import magellan.client.utils.SwingUtils;
import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * This is a dialog to edit the script/commands for a given unit. TODO Save dialog positions (size,
 * location, slider position)
 * 
 * @author Thoralf Rickert
 * @version 1.0, 11.09.2007
 */
public class ExtendedCommandsDialog extends JDialog implements ActionListener, HyperlinkListener {
  private static Logger log = Logger.getInstance(ExtendedCommandsDialog.class);

  private Client client = null;
  private BeanShellEditor scriptingArea = null;
  private JEditorPane help = null;
  private JButton homeButton = null;
  private JButton backwardButton = null;
  private JButton forwardButton = null;
  private JButton browserButton = null;
  private JComboBox priorityBox = null;
  private JSplitPane splitPane = null;
  private GameData world = null;
  private Unit unit = null;
  private UnitContainer container = null;
  private ExtendedCommands commands = null;
  private Script script = null;
  private List<URL> history = new ArrayList<URL>();
  private int pos = 0;

  public ExtendedCommandsDialog(Client client, GameData data, ExtendedCommands commands,
      Script script) {
    super(client, true);

    this.client = client;
    world = data;
    this.commands = commands;

    init(script);
  }

  public ExtendedCommandsDialog(Client client, GameData data, ExtendedCommands commands, Unit unit,
      Script script) {
    super(client, true);

    this.client = client;
    world = data;
    this.commands = commands;
    this.unit = unit;

    init(script);
  }

  public ExtendedCommandsDialog(Client client, GameData data, ExtendedCommands commands,
      UnitContainer container, Script script) {
    super(client, true);

    this.client = client;
    world = data;
    this.commands = commands;
    this.container = container;

    init(script);
  }

  protected void init(Script script) {
    this.script = script;
    setLayout(new BorderLayout());
    setWindowSize(800, 480);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        updateWindowSettings();
      }
    });

    JPanel editor = new JPanel(new BorderLayout());
    editor.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));

    scriptingArea = new BeanShellEditor();
    scriptingArea.setText(script.getScript());
    scriptingArea.setCaretPosition(script.getCursor());
    editor.add(new JScrollPane(scriptingArea), BorderLayout.CENTER);

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

    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editor, helpPanel);
    splitPane.setOneTouchExpandable(true);
    splitPane.setDividerLocation(PropertiesHelper.getInteger(client.getProperties(),
        "ExtendedCommandsDialog.slider", 480));

    add(splitPane, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    buttonPanel.add(Box.createHorizontalGlue());

    JLabel label = new JLabel(Resources.get("extended_commands.priority.caption"));
    buttonPanel.add(label);

    priorityBox = new JComboBox(Priority.values());
    priorityBox.setSelectedItem(script.getPriority());
    buttonPanel.add(priorityBox);

    buttonPanel.add(Box.createRigidArea(new Dimension(30, 0)));

    JButton cancelButton = new JButton(Resources.get("button.cancel"));
    cancelButton.setRequestFocusEnabled(false);
    cancelButton.setActionCommand("button.cancel");
    cancelButton.addActionListener(this);
    cancelButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
    buttonPanel.add(cancelButton);

    buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));

    JButton executeButton = new JButton(Resources.get("button.execute"));
    executeButton.setRequestFocusEnabled(false);
    executeButton.setActionCommand("button.execute");
    executeButton.addActionListener(this);
    executeButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
    buttonPanel.add(executeButton);

    buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));

    JButton saveButton = new JButton(Resources.get("button.save"));
    saveButton.setRequestFocusEnabled(false);
    saveButton.setActionCommand("button.save");
    saveButton.addActionListener(this);
    saveButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
    buttonPanel.add(saveButton);

    buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));

    JButton okButton = new JButton(Resources.get("button.ok"));
    okButton.setRequestFocusEnabled(false);
    okButton.setActionCommand("button.ok");
    okButton.addActionListener(this);
    okButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
    buttonPanel.add(okButton);

    buttonPanel.add(Box.createHorizontalGlue());

    add(buttonPanel, BorderLayout.SOUTH);
  }

  /**
   * This method is called, if one of the buttons is clicked.
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equalsIgnoreCase("button.ok")) {
      // save the script in the container
      updateWindowSettings();
      setVisible(false);
      String scripttext = scriptingArea.getText();
      Priority prio = (Priority) priorityBox.getSelectedItem();
      int cursor = scriptingArea.getCaretPosition();

      if (unit != null) {
        script.setScript(scripttext);
        script.setPriority(prio);
        script.setCursor(cursor);
        commands.setCommands(unit, script);
      } else if (container != null) {
        script.setScript(scripttext);
        script.setPriority(prio);
        script.setCursor(cursor);
        commands.setCommands(container, script);
      } else {
        script.setScript(scripttext);
        script.setCursor(cursor);
        commands.setLibrary(script);
      }
    } else if (e.getActionCommand().equalsIgnoreCase("button.execute")) {
      // execute the command, this means, to temporary store the script
      // and execute it. After that, restore the old script.
      Script newScript = (Script) script.clone();
      newScript.setScript(scriptingArea.getText());

      if (unit != null) {
        commands.setCommands(unit, newScript);
        commands.execute(world, unit);
        commands.setCommands(unit, script); // reset to old script
      } else if (container != null) {
        commands.setCommands(container, newScript);
        commands.execute(world, container);
        commands.setCommands(container, script); // reset to old script
      } else {
        commands.setLibrary(newScript);
        commands.execute(world);
        commands.setLibrary(script); // reset to old script
      }

    } else if (e.getActionCommand().equalsIgnoreCase("button.save")) {
      Script newScript = (Script) script.clone();
      newScript.setScript(scriptingArea.getText());

      if (unit != null) {
        commands.setCommands(unit, newScript);
      } else if (container != null) {
        commands.setCommands(container, newScript);
      } else {
        commands.setLibrary(newScript);
      }

      commands.save();

    } else if (e.getActionCommand().equalsIgnoreCase("button.cancel")) {
      // just do nothing
      updateWindowSettings();
      setVisible(false);
    } else if (e.getActionCommand().equalsIgnoreCase("button.home")) {
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
   * This method sets the window dimension and positions the window to the center of the screen.
   */
  public void setWindowSize(int xSize, int ySize) {
    int width =
        PropertiesHelper.getInteger(client.getProperties(), "ExtendedCommandsDialog.width", xSize);
    int height =
        PropertiesHelper.getInteger(client.getProperties(), "ExtendedCommandsDialog.height", ySize);

    if (width <= 0) {
      width = xSize;
    }
    if (height <= 0) {
      height = ySize;
    }
    setSize(width, height);

    SwingUtils.setLocation(this, client.getProperties(), "ExtendedCommandsDialog.x",
        "ExtendedCommandsDialog.y");
  }

  /**
   * Sets the window positions in the configuration.
   */
  public void updateWindowSettings() {
    client.getProperties().setProperty("ExtendedCommandsDialog.x", "" + getLocation().x);
    client.getProperties().setProperty("ExtendedCommandsDialog.y", "" + getLocation().y);
    client.getProperties().setProperty("ExtendedCommandsDialog.width", "" + getSize().width);
    client.getProperties().setProperty("ExtendedCommandsDialog.height", "" + getSize().height);
    client.getProperties().setProperty("ExtendedCommandsDialog.slider",
        "" + splitPane.getDividerLocation());
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
    if (unit != null) {
      // show help for unit commands
      path =
          new File(Client.getResourceDirectory(), Resources
              .get("extended_commands.help.dialog.unit"));
      if (!path.exists()) {
        path = new File(Resources.get("extended_commands.help.dialog.unit"));
      }
      if (!path.exists()) {
        path = null;
      }
    } else if (container != null) {
      // show help for container commands
      path =
          new File(Client.getResourceDirectory(), Resources
              .get("extended_commands.help.dialog.container"));
      if (!path.exists()) {
        path = new File(Resources.get("extended_commands.help.dialog.container"));
      }
      if (!path.exists()) {
        path = null;
      }
    } else {
      // show help for library
      path =
          new File(Client.getResourceDirectory(), Resources
              .get("extended_commands.help.dialog.library"));
      if (!path.exists()) {
        path = new File(Resources.get("extended_commands.help.dialog.library"));
      }
      if (!path.exists()) {
        path = null;
      }
    }
    try {
      if (path != null) {
        URL url = path.toURI().toURL();
        help.setPage(url);
        help.setContentType("text/html; charset=ISO-8859-1");
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
