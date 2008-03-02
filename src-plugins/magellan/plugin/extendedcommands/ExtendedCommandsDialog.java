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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import magellan.client.Client;
import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.utils.Resources;


/**
 * This is a dialog to edit the script/commands for a given unit.
 * 
 * TODO Save dialog positions (size, location, slider position)
 *
 * @author Thoralf Rickert
 * @version 1.0, 11.09.2007
 */
public class ExtendedCommandsDialog extends JDialog implements ActionListener, HyperlinkListener {
  private Client client = null;
  private BeanShellEditor scriptingArea = null;
  private JEditorPane help = null;
  private JButton backwardButton = null;
  private JButton forwardButton = null;
  private GameData world = null;
  private Unit unit = null;
  private UnitContainer container = null;
  private ExtendedCommands commands = null;
  private String script = null;
  private List<URL> history = new ArrayList<URL>();
  private int pos = 0;
  
  public ExtendedCommandsDialog(Client client, GameData data, ExtendedCommands commands, String script) {
    super(client,true);
    
    this.client = client;
    this.world = data;
    this.commands = commands;
    
    init(script);
  }
  
  public ExtendedCommandsDialog(Client client, GameData data, ExtendedCommands commands, Unit unit, String script) {
    super(client,true);
    
    this.client = client;
    this.world = data;
    this.commands = commands;
    this.unit = unit;
    
    init(script);
  }
  
  public ExtendedCommandsDialog(Client client, GameData data, ExtendedCommands commands, UnitContainer container, String script) {
    super(client,true);
    
    this.client = client;
    this.world = data;
    this.commands = commands;
    this.container = container;
    
    init(script);
  }
  
  protected void init(String script) {
    this.script = script;
    setLayout(new BorderLayout());
    setWindowSize(800, 480);
    
    JPanel editor = new JPanel(new BorderLayout());
    editor.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
    
    scriptingArea = new BeanShellEditor();
    scriptingArea.setText(script);
    scriptingArea.setCaretPosition(0);
    editor.add(new JScrollPane(scriptingArea),BorderLayout.CENTER);
    
    JPanel helpPanel = new JPanel(new BorderLayout());
    help = new JEditorPane();
    help.setContentType("text/html");
    help.setEditable(false);
    help.addHyperlinkListener(this);
    File path = null;
    if (unit != null) {
      // show help for unit commands
      path = new File(Client.getSettingsDirectory(),Resources.get("extended_commands.help.dialog.unit"));
      if (!path.exists()) path = new File(Resources.get("extended_commands.help.dialog.unit"));
      if (!path.exists()) path = null;
    } else if (container != null) {
      // show help for container commands
      path = new File(Resources.get("extended_commands.help.dialog.container"));
      if (!path.exists()) path = new File(Resources.get("extended_commands.help.dialog.container"));
      if (!path.exists()) path = null;
    } else {
      // show help for library
      path = new File(Resources.get("extended_commands.help.dialog.library"));
      if (!path.exists()) path = new File(Resources.get("extended_commands.help.dialog.library"));
      if (!path.exists()) path = null;
    }
    try {
      if (path != null) {
        URL url = path.toURI().toURL();
        help.setPage(url);
        history.add(url);
      } else {
        help.setText("Unable to locate help files...");
      }
    } catch (Exception e) {
      help.setText("Unable to locate help files...\n"+e.getMessage());
    }
    help.setCaretPosition(0);
    JScrollPane scrollPane = new JScrollPane(help);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    
    helpPanel.add(scrollPane,BorderLayout.CENTER);
    
    JPanel helpButtonPanel = new JPanel(new FlowLayout());
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
    helpPanel.add(helpButtonPanel,BorderLayout.NORTH);
    
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editor, helpPanel);
    splitPane.setOneTouchExpandable(true);
    splitPane.setDividerLocation(480);
    
    add(splitPane,BorderLayout.CENTER);
    
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    buttonPanel.add(Box.createHorizontalGlue());
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
    
    JButton okButton = new JButton(Resources.get("button.ok"));
    okButton.setRequestFocusEnabled(false);
    okButton.setActionCommand("button.ok");
    okButton.addActionListener(this);
    okButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
    buttonPanel.add(okButton);

    buttonPanel.add(Box.createHorizontalGlue());
    
    add(buttonPanel,BorderLayout.SOUTH);
  }

  /**
   * This method is called, if one of the buttons is clicked.
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equalsIgnoreCase("button.ok")) {
      // save the script in the container
      setVisible(false);
      String script = scriptingArea.getText();
      if (unit != null) {
        commands.setCommands(unit,script);
      } else if (container != null) {
        commands.setCommands(container,script);
      } else {
        commands.setLibrary(script);
      }
    } else if (e.getActionCommand().equalsIgnoreCase("button.execute")) {
      // execute the command, this means, to temporary store the script
      // and exeute it. After that, restore the old script.
      String newScript = scriptingArea.getText();

      if (unit != null) {
        commands.setCommands(unit,newScript);
        commands.execute(world, unit);
        commands.setCommands(unit, script); // reset to old script
      } else if (container != null) {
        commands.setCommands(container,newScript);
        commands.execute(world, container);
        commands.setCommands(container, script); // reset to old script
      } else {
        commands.setLibrary(newScript);
        commands.execute(world);
        commands.setLibrary(script); // reset to old script
      }
      
    } else if (e.getActionCommand().equalsIgnoreCase("button.cancel")) {
      // just do nothing
      setVisible(false);
    } else if (e.getActionCommand().equalsIgnoreCase("button.backward")) {
      // move one position back in history
      if (pos > 0) {
        pos--;
        try {
          help.setPage(history.get(pos));
        } catch (Exception exception) {}
        forwardButton.setEnabled(true);
        if (pos == 0) backwardButton.setEnabled(false);
      }
    } else if (e.getActionCommand().equalsIgnoreCase("button.forward")) {
      // move one position forward in history
      if (pos < history.size()) {
        pos++;
        try {
          help.setPage(history.get(pos));
        } catch (Exception exception) {}
        backwardButton.setEnabled(true);
        if (pos == history.size()-1) forwardButton.setEnabled(false);
      }
    }
  }

  // **********************************************************************
  /**
   * This method sets the window dimension and positions the window to the
   * center of the screen.
   */

  public void setWindowSize(int xSize, int ySize) {
    if (xSize > 0 && ySize > 0) {
      int x = (int) getToolkit().getScreenSize().width;
      int y = (int) getToolkit().getScreenSize().height;
      setSize(xSize, ySize);
      setLocation(new Point((int) (x / 2 - xSize / 2), (int) (y / 2 - ySize / 2)));
    }
  }

  /**
   * @see javax.swing.event.HyperlinkListener#hyperlinkUpdate(javax.swing.event.HyperlinkEvent)
   */
  public void hyperlinkUpdate(HyperlinkEvent e) {
    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      try {
        // Loads the new page represented by link clicked
        URL url = e.getURL() ;
        help.setPage(url) ;
        pos++;
        history.add(pos,url);
        backwardButton.setEnabled(true);
      }
      catch (Exception exc) {
      }
    }
  }
}
