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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import magellan.client.Client;
import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.utils.Resources;


/**
 * This is a dialog to edit the script/commands for a given unit.
 *
 * @author Thoralf Rickert
 * @version 1.0, 11.09.2007
 */
public class ExtendedCommandsDialog extends JDialog implements ActionListener {
  private BeanShellEditor scriptingArea = null;
  private GameData world = null;
  private Unit unit = null;
  private UnitContainer container = null;
  private ExtendedCommands commands = null;
  private String script = null;
  
  public ExtendedCommandsDialog(Client client, GameData data, ExtendedCommands commands, Unit unit, String script) {
    super(client,true);
    
    this.world = data;
    this.commands = commands;
    this.unit = unit;
    
    init(script);
  }
  
  public ExtendedCommandsDialog(Client client, GameData data, ExtendedCommands commands, UnitContainer container, String script) {
    super(client,true);
    
    this.world = data;
    this.commands = commands;
    this.container = container;
    
    init(script);
  }
  
  protected void init(String script) {
    this.script = script;
    setLayout(new BorderLayout());
    setWindowSize(640, 480);
    
    JPanel center = new JPanel(new BorderLayout());
    center.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
    
    scriptingArea = new BeanShellEditor();
    scriptingArea.setText(script);
    center.add(new JScrollPane(scriptingArea),BorderLayout.CENTER);
    add(center,BorderLayout.CENTER);
    
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
      }
      
    } else if (e.getActionCommand().equalsIgnoreCase("button.cancel")) {
      // just do nothing
      setVisible(false);
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
}
