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
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.Group;
import magellan.library.Order;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
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
  private String[] columnNames;
  private Map<Group, Unit> representatives = new HashMap<Group, Unit>();
  protected static final String signature = "; ---------- GroupEditor";

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

    representatives.clear();
    columnNames = null;
    for (Unit u : owner.units()) {
      if (u.getGroup() != null) {
        continue;
      }
      representatives.put(null, u);
      break;
    }

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
    columnNames = null;
    for (Unit u : group.units()) {
      representatives.put(group, u);
      break;
    }
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
    if (columnNames == null) {
      columnNames = new String[columns.size() + offset];
      for (int col = 0; col < columns.size() + offset; ++col)
        if (col == 0) {
          columnNames[col] = Resources.get("dock.GroupEditor.table.header.faction.title");
        } else if (col == 1 && hasAllied) {
          columnNames[col] =
              Resources.get("dock.GroupEditor.table.header.default.title") + " ("
                  + representatives.get(null) + ")";
        } else {
          columnNames[col] =
              columns.get(col - offset).getName() + " ("
                  + representatives.get(columns.get(col - offset)) + ")";
        }
    }

    return columnNames[pos];
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
        Map<Faction, AllianceState> myNewStates = newGroupStates.get(group);
        if (myNewStates.containsKey(faction))
          return myNewStates.get(faction);
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
        Map<Faction, AllianceState> myNewStates = newGroupStates.get(group);
        myNewStates.put(faction, state);
      } else {
        HashMap<Faction, AllianceState> myNewStates = new HashMap<Faction, AllianceState>();
        myNewStates.put(faction, state);
        newGroupStates.put(group, myNewStates);
      }
    }

    fireTableCellUpdated(rowIndex, columnIndex);
  }

  /**
   * Saves the settings from the world.
   */
  public void save() {
    String helpcommand = Resources.getOrderTranslation(EresseaConstants.O_HELP);

    if (!newStates.isEmpty()) {
      // ok, let's save the alliance settings
      save(helpcommand, representatives.get(null), newStates);
    }
    if (!newGroupStates.isEmpty()) {
      for (Group group : newGroupStates.keySet()) {
        if (group.units() == null) {
          continue;
        }
        Map<Faction, AllianceState> myNewStates = newGroupStates.get(group);
        if (!myNewStates.isEmpty()) {
          // ok, let's save the alliance settings
          save(helpcommand, representatives.get(group), myNewStates);
        }
      }
    }
  }

  /**
   * Adds multiple commands per faction to the given unit.
   */
  protected void save(String helpcommand, Unit unit, Map<Faction, AllianceState> states) {
    if (unit == null)
      return;
    Map<EntityID, Alliance> allies =
        unit.getGroup() != null ? unit.getGroup().allies() : unit.getFaction().getAllies();

    Collection<Order> newOrders = new ArrayList<Order>();
    for (Order order : unit.getOrders2()) {
      if (!order.getText().startsWith(signature)) {
        newOrders.add(order);
      }
    }
    if (newOrders.size() != unit.getOrders2().size()) {
      unit.setOrders2(newOrders);
    }

    boolean firstAdded = false;
    for (Faction faction : states.keySet()) {
      AllianceState newState = states.get(faction);
      Alliance oldState = allies != null ? allies.get(faction.getID()) : null;
      if (oldState == null) {
        oldState = new Alliance(faction);
      }

      if (newState.getCategories().isEmpty()) {
        unit.addOrder(signature + " reset " + faction);
        unit.addOrder(helpcommand + " " + faction.getID() + " "
            + Resources.getOrderTranslation("ALL") + " " + Resources.getOrderTranslation("NOT"),
            !firstAdded, 1);
        firstAdded = true;
      } else {
        unit.addOrder(signature + "new help states for " + faction, true, 1);
        // remove states no longer present

        boolean all = false;
        for (AllianceCategory cat : newState.getCategories()) {
          if (cat.getName().equals(EresseaConstants.O_ALL)) {
            unit.addOrder(helpcommand + " " + faction.getID() + " "
                + Resources.getOrderTranslation("ALL"), !firstAdded, 1);
            all = true;
            firstAdded = true;
          }
        }

        if (!all) {
          for (AllianceCategory cat : minus(oldState.getAllianceCategories(), newState
              .getCategories())) {
            if (cat.getName().equals(EresseaConstants.O_ALL)) {
              continue;
            }
            unit.addOrder(helpcommand + " " + faction.getID() + " "
                + Resources.getOrderTranslation(Alliance.ORDER_KEY_PREFIX + cat.getName()) + " "
                + Resources.getOrderTranslation("NOT"), !firstAdded, 1);
            firstAdded = true;
          }
          // add new states
          for (AllianceCategory cat : newState.getCategories()) {
            if (oldState.getAllianceCategories().contains(cat)) {
              unit.addOrder(signature + helpcommand + " " + faction.getID() + " "
                  + Resources.getOrderTranslation(Alliance.ORDER_KEY_PREFIX + cat.getName()));
            } else {
              unit.addOrder(helpcommand + " " + faction.getID() + " "
                  + Resources.getOrderTranslation(Alliance.ORDER_KEY_PREFIX + cat.getName()),
                  !firstAdded, 1);
            }
            firstAdded = true;
          }
        }
      }
    }
  }

  private <T> List<T> minus(List<T> list1, List<T> list2) {
    ArrayList<T> difference = new ArrayList<T>();
    for (T obj : list1) {
      boolean found = false;
      for (T obj2 : list2) {
        if (obj2.equals(obj)) {
          found = true;
          break;
        }
      }
      if (!found) {
        difference.add(obj);
      }
    }
    return difference;
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

  public Unit getRepresentative(int column) {
    if (column < offset)
      return representatives.get(null);
    return representatives.get(columns.get(column - offset));
  }
}
