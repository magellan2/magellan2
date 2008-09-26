// class magellan.plugin.groupeditor.GroupEditorDock
// created on 25.09.2008
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
package magellan.plugin.groupeditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import magellan.client.Client;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.utils.MagellanImages;
import magellan.library.utils.Resources;
import magellan.library.utils.comparator.FactionTrustComparator;
import magellan.library.utils.logging.Logger;

/**
 * This plugin allows it to control the state of all faction to a group based
 * on a table layout. This plugin provides a dock which contains a table. The
 * column contains the groups. The rows are the factions. In the cells you can
 * find the help state of a group for a faction.
 *
 * @author Thoralf Rickert
 * @version 1.0, 25.09.2008
 */
public class GroupEditorDock extends JPanel implements ActionListener, GameDataListener  {
  private static Logger log = Logger.getInstance(GroupEditorDock.class); 
  public static final String IDENTIFIER = "GroupEditor";
  private Client client = null;
  private GameData world = null;
  protected JComboBox factionBox = null;
  protected GroupEditorTableModel model = null;
  protected JTable table = null;

  /**
   * Created a new instance of this class
   */
  public GroupEditorDock(Client client) {
    setClient(client);
    init();
    
    client.getDispatcher().addGameDataListener(this);
  }
  
  /**
   * Initializes the GUI
   */
  protected void init() {
    setLayout(new BorderLayout());
    
    factionBox = new JComboBox();
    factionBox.setActionCommand("faction.changed");
    factionBox.addActionListener(this);
    add(factionBox,BorderLayout.NORTH);
    
    model = new GroupEditorTableModel();
    table = new JTable(model) {
      protected JTableHeader createDefaultTableHeader() {
        return new JTableHeader(columnModel) {
          public String getToolTipText(MouseEvent e) {
            int index = columnModel.getColumnIndexAtX(e.getPoint().x);
            Object tip = columnModel.getColumn(index).getHeaderValue();
            if (tip == null) return "";
            return tip.toString();
          }
        };
      }
    };

    table.setDefaultRenderer(AllianceState.class, new AllianceStateRenderer());
        
    add(new JScrollPane(table),BorderLayout.CENTER);
    
    JPanel south = new JPanel();
    south.setLayout(new BoxLayout(south, BoxLayout.X_AXIS));
    south.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    south.add(Box.createHorizontalGlue());
    
    JButton saveButton = new JButton(Resources.get("dock.GroupEditor.save.button"), MagellanImages.getImageIcon("etc/images/gui/actions/save_edit.gif"));
    saveButton.setRequestFocusEnabled(false);
    saveButton.setVerticalTextPosition(JButton.CENTER);
    saveButton.setHorizontalTextPosition(JButton.LEADING);
    saveButton.setActionCommand("button.save");
    saveButton.addActionListener(this);
    saveButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
    south.add(saveButton);

    add(south,BorderLayout.SOUTH);
    
  }
  
  /**
   * This method updates the list of factions inside
   * the faction choose box.
   */
  protected void update() {
    
    factionBox.removeActionListener(this);
    factionBox.removeAllItems();
    
    if (world.factions() == null) return;
    
    List<Faction> factions = new ArrayList<Faction>(world.factions().values());
    Collections.sort(factions, FactionTrustComparator.DEFAULT_COMPARATOR);
    Faction owner = null;
    
    for (Faction faction : factions) {
      // if (faction.getPassword() == null) continue;
      factionBox.addItem(faction);
      if (world.getOwnerFaction() != null && world.getOwnerFaction().equals(faction.getID())) owner = faction;
    }
    if (owner == null && factions.size()>0) owner = factions.get(0);
    if (owner != null) factionBox.setSelectedItem(owner);
    
    factionBox.addActionListener(this);
    
    if (owner == null) return; // ok, we can't do anything, if there is no faction.
    
    model.setOwner(owner);
    
    for (int i=0; i<table.getColumnCount(); i++) {
      if (i == 0) continue;
      TableColumn column = table.getColumnModel().getColumn(i);
      AllianceStateComboBox box = new AllianceStateComboBox(world);
      DefaultCellEditor editor = new DefaultCellEditor(box);
      column.setCellEditor(editor);
    }
  }
  
  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent event) {
    log.info("ActionEvent '"+event.getActionCommand()+"' retrieved");
    if (event.getActionCommand().equals("faction.changed")) {
      Faction owner = (Faction)factionBox.getSelectedItem();
      model.setOwner(owner);
      
      for (int i=0; i<table.getColumnCount(); i++) {
        if (i == 0) continue;
        TableColumn column = table.getColumnModel().getColumn(i);
        AllianceStateComboBox box = new AllianceStateComboBox(world);
        DefaultCellEditor editor = new DefaultCellEditor(box);
        column.setCellEditor(editor);
      }
    } else if (event.getActionCommand().equals("button.save")) {
      if (JOptionPane.showConfirmDialog(getClient(), Resources.get("dock.GroupEditor.save.message"), Resources.get("dock.GroupEditor.save.title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
        model.save();
      }
    }
  }

  /**
   * Returns the value of client.
   * 
   * @return Returns client.
   */
  public Client getClient() {
    return client;
  }

  /**
   * Sets the value of client.
   *
   * @param client The value for client.
   */
  public void setClient(Client client) {
    this.client = client;
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
    update();
  }

  /**
   * @see magellan.library.event.GameDataListener#gameDataChanged(magellan.library.event.GameDataEvent)
   */
  public void gameDataChanged(GameDataEvent e) {
    setWorld(e.getGameData());
  }
}
