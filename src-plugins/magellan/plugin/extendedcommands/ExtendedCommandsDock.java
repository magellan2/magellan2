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
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowListener;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.View;

import magellan.client.Client;
import magellan.client.desktop.MagellanDesktop;
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
public class ExtendedCommandsDock extends JPanel implements ActionListener, CaretListener, DockingWindowListener {
  public static final String IDENTIFIER = "ExtendedCommands";
  private Client client = null;
  private BeanShellEditor scriptingArea = null;
  private JComboBox priorityBox = null;
  private GameData world = null;
  private Unit unit = null;
  private UnitContainer container = null;
  private JLabel elementBox = null;
  private JLabel positionBox = null;
  private ExtendedCommands commands = null;
  private Script script = null;
  private List<URL> history = new ArrayList<URL>();
  private int pos = 0;
  private boolean visible = false;
  
  public ExtendedCommandsDock(Client client, ExtendedCommands commands) {
    this.client = client;
    this.commands = commands;
    
    init();
  }
  
  protected void init() {
    setLayout(new BorderLayout());

    JPanel editor = new JPanel(new BorderLayout());
    editor.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
    
    scriptingArea = new BeanShellEditor();
    scriptingArea.addCaretListener(this);
    editor.add(new JScrollPane(scriptingArea),BorderLayout.CENTER);
    
    add(editor,BorderLayout.CENTER);
    
    JPanel north = new JPanel();
    north.setLayout(new BoxLayout(north, BoxLayout.X_AXIS));
    north.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    JLabel label = new JLabel(Resources.get("extended_commands.element.caption"));
    north.add(label);
    
    north.add(Box.createRigidArea(new Dimension(5, 0)));
    elementBox = new JLabel();
    elementBox.setFont(elementBox.getFont().deriveFont(Font.BOLD));
    north.add(elementBox);
    
    north.add(Box.createHorizontalGlue());

    label = new JLabel(Resources.get("extended_commands.position.caption"));
    north.add(label);
    north.add(Box.createRigidArea(new Dimension(5, 0)));
    
    positionBox = new JLabel("0,0");
    north.add(positionBox);
    
    add(north,BorderLayout.NORTH);
    
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    buttonPanel.add(Box.createHorizontalGlue());
    
    label = new JLabel(Resources.get("extended_commands.priority.caption"));
    buttonPanel.add(label);
    
    priorityBox = new JComboBox(Priority.values());
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
      String scripttext = scriptingArea.getText();
      Priority prio = (Priority)priorityBox.getSelectedItem();
      int cursor = scriptingArea.getCaretPosition();
      
      if (unit != null) {
        script.setScript(scripttext);
        script.setPriority(prio);
        script.setCursor(cursor);
        commands.setCommands(unit,script);
      } else if (container != null) {
        script.setScript(scripttext);
        script.setPriority(prio);
        script.setCursor(cursor);
        commands.setCommands(container,script);
      } else {
        script.setScript(scripttext);
        script.setCursor(cursor);
        commands.setLibrary(script);
      }
    } else if (e.getActionCommand().equalsIgnoreCase("button.execute")) {
      // execute the command, this means, to temporary store the script
      // and execute it. After that, restore the old script.
      Script newScript = (Script)script.clone();
      newScript.setScript(scriptingArea.getText());

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
      
    } else if (e.getActionCommand().equalsIgnoreCase("button.save")) {
      Script newScript = (Script)script.clone();
      newScript.setScript(scriptingArea.getText());

      if (unit != null) {
        commands.setCommands(unit,newScript);
      } else if (container != null) {
        commands.setCommands(container,newScript);
      } else {
        commands.setLibrary(newScript);
      }
      
      commands.save();
      
    } else if (e.getActionCommand().equalsIgnoreCase("button.cancel")) {
      // just restore the old settings

      if (unit != null) {
        commands.setCommands(unit, script); // reset to old script
      } else if (container != null) {
        commands.setCommands(container, script); // reset to old script
      } else {
        commands.setLibrary(script); // reset to old script
      }
      
      setVisible(false);
    }
  }

  /**
   * Returns the value of world.
   * 
   * @return Returns world.
   */
  public GameData getWorld() {
    return world;
  }

  /**
   * Sets the value of world.
   *
   * @param world The value for world.
   */
  public void setWorld(GameData world) {
    this.world = world;
  }

  /**
   * Returns the value of unit.
   * 
   * @return Returns unit.
   */
  public Unit getUnit() {
    return unit;
  }

  /**
   * Sets the value of unit.
   *
   * @param unit The value for unit.
   */
  public void setUnit(Unit unit) {
    this.unit = unit;
  }

  /**
   * Returns the value of container.
   * 
   * @return Returns container.
   */
  public UnitContainer getContainer() {
    return container;
  }

  /**
   * Sets the value of container.
   *
   * @param container The value for container.
   */
  public void setContainer(UnitContainer container) {
    this.container = container;
  }

  /**
   * Returns the value of script.
   * 
   * @return Returns script.
   */
  public Script getScript() {
    return script;
  }

  /**
   * Sets the value of script.
   *
   * @param script The value for script.
   */
  public void setScript(Script script) {
    this.script = script;
    if (script != null) {
      scriptingArea.setText(script.getScript());
      scriptingArea.setCaretPosition(script.getCursor());
      priorityBox.setSelectedItem(script.getPriority());
    } else {
      scriptingArea.setText("");
      scriptingArea.setCaretPosition(0);
      priorityBox.setSelectedItem(Priority.NORMAL);
    }
    
    if (unit != null) {
      elementBox.setText(Resources.get("extended_commands.element.unit",unit.getName(),unit.getID()));
    } else if (container != null) {
      elementBox.setText(Resources.get("extended_commands.element.container",container.getName(),container.getID()));
    } else {
      elementBox.setText(Resources.get("extended_commands.element.library"));
    }
    
    // Visibility
    if (!visible) MagellanDesktop.getInstance().setVisible(IDENTIFIER, true);
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#viewFocusChanged(net.infonode.docking.View, net.infonode.docking.View)
   */
  public void viewFocusChanged(View previouslyFocusedView, View focusedView) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowAdded(net.infonode.docking.DockingWindow, net.infonode.docking.DockingWindow)
   */
  public void windowAdded(DockingWindow addedToWindow, DockingWindow addedWindow) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowClosed(net.infonode.docking.DockingWindow)
   */
  public void windowClosed(DockingWindow window) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowClosing(net.infonode.docking.DockingWindow)
   */
  public void windowClosing(DockingWindow window) throws OperationAbortedException {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowDocked(net.infonode.docking.DockingWindow)
   */
  public void windowDocked(DockingWindow window) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowDocking(net.infonode.docking.DockingWindow)
   */
  public void windowDocking(DockingWindow window) throws OperationAbortedException {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowHidden(net.infonode.docking.DockingWindow)
   */
  public void windowHidden(DockingWindow window) {
    this.visible = false;
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowMaximized(net.infonode.docking.DockingWindow)
   */
  public void windowMaximized(DockingWindow window) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowMaximizing(net.infonode.docking.DockingWindow)
   */
  public void windowMaximizing(DockingWindow window) throws OperationAbortedException {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowMinimized(net.infonode.docking.DockingWindow)
   */
  public void windowMinimized(DockingWindow window) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowMinimizing(net.infonode.docking.DockingWindow)
   */
  public void windowMinimizing(DockingWindow window) throws OperationAbortedException {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowRemoved(net.infonode.docking.DockingWindow, net.infonode.docking.DockingWindow)
   */
  public void windowRemoved(DockingWindow removedFromWindow, DockingWindow removedWindow) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowRestored(net.infonode.docking.DockingWindow)
   */
  public void windowRestored(DockingWindow window) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowRestoring(net.infonode.docking.DockingWindow)
   */
  public void windowRestoring(DockingWindow window) throws OperationAbortedException {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowShown(net.infonode.docking.DockingWindow)
   */
  public void windowShown(DockingWindow window) {
    this.visible = true;
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowUndocked(net.infonode.docking.DockingWindow)
   */
  public void windowUndocked(DockingWindow window) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowUndocking(net.infonode.docking.DockingWindow)
   */
  public void windowUndocking(DockingWindow window) throws OperationAbortedException {
  }

  /**
   * @see javax.swing.event.CaretListener#caretUpdate(javax.swing.event.CaretEvent)
   */
  public void caretUpdate(CaretEvent e) {
    if (positionBox != null && scriptingArea != null) {
      int pos = e.getDot();
      String text = scriptingArea.getText();
      int row = 1;
      int col = 1;
      for (int i=0; i<text.length(); i++) {
        if (i==pos) break;
        char c = text.charAt(i);
        if (c == '\n') {
          row++;
          col = 1;
        } else {
          col++;
        }
      }
      
      positionBox.setText(row+","+col);
    }
  }
  
}
