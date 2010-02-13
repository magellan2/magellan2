// class magellan.plugin.groupeditor.GroupEditorTableModel
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import magellan.library.Alliance;
import magellan.library.Faction;
import magellan.library.Group;
import magellan.library.Unit;
import magellan.library.rules.AllianceCategory;
import magellan.library.utils.Resources;
import magellan.library.utils.comparator.FactionTrustComparator;
import magellan.library.utils.comparator.IDComparator;
import magellan.library.utils.comparator.NameComparator;

/**
 * Represents the list off all possible alliance states
 * 
 * @author Thoralf Rickert
 * @version 1.0, 25.09.2008
 */
public class GroupEditorTableModel extends AbstractTableModel {
  protected Faction owner = null;
  protected List<Group> columns = new ArrayList<Group>();
  protected List<Faction> rows = new ArrayList<Faction>();
  protected boolean hasAllied = true;
  protected int offset = 2;
  protected Map<Faction, AllianceState> newStates = new HashMap<Faction, AllianceState>();
  protected Map<Group, Map<Faction, AllianceState>> newGroupStates =
      new HashMap<Group, Map<Faction, AllianceState>>();

  /**
   * Returns the value of faction.
   * 
   * @return Returns faction.
   */
  public Faction getOwner() {
    return owner;
  }

  /**
   * Sets the value of faction.
   * 
   * @param owner The value for faction.
   */
  public void setOwner(Faction owner) {
    this.owner = owner;

    if (owner.getAllies() == null) {
      // no allied set (without groups)
      hasAllied = false;
      offset = 1;
    } else {
      hasAllied = true;
      offset = 2;
    }

    columns.clear();
    newStates.clear();
    newGroupStates.clear();

    if (owner.getGroups() != null) {
      List<Group> groups = new ArrayList<Group>(owner.getGroups().values());
      Collections.sort(groups, new NameComparator(IDComparator.DEFAULT));

      for (Group group : groups) {
        addColumn(group);
      }
    }

    rows.clear();
    List<Faction> factions = new ArrayList<Faction>(owner.getData().getFactions());
    Collections.sort(factions, FactionTrustComparator.DEFAULT_COMPARATOR);

    for (Faction faction : factions) {
      if (faction.getID().equals(owner.getID())) {
        continue; // ignore yourself
      }
      addRow(faction);
    }

    fireTableStructureChanged();
  }

  /**
   * Adds a new column
   */
  public void addColumn(Group group) {
    columns.add(group);
  }

  /**
   * Adds a new row
   */
  public void addRow(Faction faction) {
    rows.add(faction);
  }

  /**
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  public int getColumnCount() {
    return columns.size() + offset;
  }

  /**
   * @see javax.swing.table.AbstractTableModel#getColumnName(int)
   */
  @Override
  public String getColumnName(int pos) {
    if (pos == 0)
      return Resources.get("dock.GroupEditor.table.header.faction.title");
    if (pos == 1 && hasAllied)
      return Resources.get("dock.GroupEditor.table.header.default.title");
    return columns.get(pos - offset).getName();
  }

  /**
   * @see javax.swing.table.TableModel#getRowCount()
   */
  public int getRowCount() {
    return rows.size();
  }

  /**
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  public Object getValueAt(int rowIndex, int columnIndex) {
    Faction faction = rows.get(rowIndex);

    if (columnIndex == 0)
      // return faction name
      return faction;
    else if (columnIndex == 1 && hasAllied) {
      // return default settings
      if (newStates.containsKey(faction))
        return newStates.get(faction);
      return getAlliedState(faction, owner.getAllies().values());

    } else {
      // return group specific settings
      Group group = columns.get(columnIndex - offset);
      if (newGroupStates.containsKey(group)) {
        Map<Faction, AllianceState> newStates = newGroupStates.get(group);
        if (newStates.containsKey(faction))
          return newStates.get(faction);
      }
      return getAlliedState(faction, group.allies().values());
    }
  }

  /**
   * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
   */
  @Override
  public void setValueAt(Object value, int rowIndex, int columnIndex) {
    Faction faction = rows.get(rowIndex);
    AllianceState state = (AllianceState) value;
    if (columnIndex == 1 && hasAllied) {
      // global setting
      newStates.put(faction, state);
    } else {
      // find group
      Group group = columns.get(columnIndex - offset);
      if (newGroupStates.containsKey(group)) {
        Map<Faction, AllianceState> newStates = newGroupStates.get(group);
        newStates.put(faction, state);
      } else {
        HashMap<Faction, AllianceState> newStates = new HashMap<Faction, AllianceState>();
        newStates.put(faction, state);
        newGroupStates.put(group, newStates);
      }
    }

    fireTableCellUpdated(rowIndex, columnIndex);
  }

  /**
   * Saves the settings from the world.
   */
  public void save() {
    String helpcommand = Resources.getOrderTranslation("HELP");

    if (!newStates.isEmpty()) {
      // ok, let's find a suitable unit (without a group)
      for (Unit unit : owner.units()) {
        if (unit.getGroup() != null) {
          continue;
        }
        // ok, let's save the alliance settings
        save(helpcommand, unit, newStates);
        break;
      }
    }
    if (!newGroupStates.isEmpty()) {
      for (Group group : newGroupStates.keySet()) {
        if (group.units() == null) {
          continue;
        }
        Map<Faction, AllianceState> newStates = newGroupStates.get(group);
        if (!newStates.isEmpty()) {
          for (Unit unit : group.units()) {
            // ok, let's save the alliance settings
            save(helpcommand, unit, newStates);
            break;
          }
        }
      }
    }
  }

  /**
   * Adds multiple commands per faction to the given unit.
   */
  protected void save(String helpcommand, Unit unit, Map<Faction, AllianceState> states) {
    for (Faction faction : states.keySet()) {
      AllianceState state = states.get(faction);
      unit.addOrders("; ----------------------");
      unit.addOrders("; reset " + faction);
      unit.addOrders(helpcommand + " " + faction.getID() + " "
          + Resources.getOrderTranslation("ALL") + " " + Resources.getOrderTranslation("NOT"));
      unit.addOrders("; new help states for " + faction);
      for (AllianceCategory category : state.getCategories()) {
        unit.addOrders(helpcommand + " " + faction.getID() + " "
            + Resources.getOrderTranslation(Alliance.ORDER_KEY_PREFIX + category.getName()));
      }
    }
  }

  /**
   * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
   */
  @Override
  public Class<?> getColumnClass(int c) {
    Object o = getValueAt(0, c);
    if (o == null)
      return super.getColumnClass(c);
    return o.getClass();
  }

  /**
   * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
   */
  @Override
  public boolean isCellEditable(int row, int col) {
    return col > 0;
  }

  /**
   * Returns the string representation of the allied state of a given faction towards the given
   * group.
   */
  public AllianceState getAlliedState(Faction faction, Collection<Alliance> allied) {
    for (Alliance ally : allied) {
      if (ally.getFaction().equals(faction)) {
        AllianceState state = new AllianceState();
        state.addCategories(ally.getAllianceCategories());
        return state;
      }
    }
    return new AllianceState();
  }

}
