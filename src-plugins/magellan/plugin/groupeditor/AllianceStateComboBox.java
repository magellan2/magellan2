// class magellan.plugin.groupeditor.GroupEditorTableCellRenderer
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

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JTable;

import magellan.library.Alliance;
import magellan.library.GameData;
import magellan.library.rules.AllianceCategory;
import magellan.library.utils.Resources;

/**
 * Shows a JComboBox as default renderer for the table. 
 *
 * @author Thoralf Rickert
 * @version 1.0, 25.09.2008
 */
public class AllianceStateComboBox extends JComboBox {
  private GameData world = null;
  private List<AllianceState> states = new ArrayList<AllianceState>();
  
  public AllianceStateComboBox(GameData world) {
    this.world = world;
    removeAllItems();
    states.clear();
    
    addItem(new AllianceState()); // empty string for nothing set
    
    AllianceState max = new AllianceState();
    max.addCategory(getMaxAllianceCategory());
    states.add(max);
    
    Iterator<AllianceCategory> categories = world.rules.getAllianceCategoryIterator();
    
    while (categories.hasNext()) {
      AllianceCategory category = categories.next();
      // deprecated - not nice, but necessary (PERCEPTION is not longer allowed but must be available in the settings)
      if (world.getGameName() != null && world.getGameName().equalsIgnoreCase("ERESSEA") && category.getName().equalsIgnoreCase("PERCEPTON")) continue;
      addItems(category, states, new ArrayList<AllianceCategory>());
    }
    
    Collections.sort(states, new AllianceStateComparator());
    for (AllianceState state : states) {
      addItem(state);
    }
    
    setRenderer(new AllianceStateRenderer());
    
  }
  
  /**
   * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
   */
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    
    if (value != null) {
      System.out.println(value+ " "+value.getClass().getName());
      if (value instanceof Alliance) {
        Alliance alliance = (Alliance)value;
        for (AllianceState state : states) {
          if (state.getBitMask() == alliance.getState()) {
            setSelectedItem(state);
            break;
          }
        }
      }
    }
    return this;
  }

  /**
   * recursive method to find all possible alliance category assignments
   */
  protected void addItems(AllianceCategory category, List<AllianceState> states, List<AllianceCategory> cats) {
    if (cats.contains(category)) return;
    
    AllianceState state = new AllianceState();
    state.addCategory(category);
    state.addCategories(cats);
    
    if (contains(states,state)) return;
    
    states.add(state);
    
    if (category.getParent() == null) return;
    cats.add(category);

    Iterator<AllianceCategory> categories = world.rules.getAllianceCategoryIterator();
    while (categories.hasNext()) {
      AllianceCategory nextcat = categories.next();
      if (nextcat.getParent() == null) continue;
      // deprecated - not nice, but necessary (PERCEPTION is not longer allowed but must be available in the settings)
      if (world.getGameName() != null && world.getGameName().equalsIgnoreCase("ERESSEA") && category.getName().equalsIgnoreCase("PERCEPTON")) continue;
      addItems(nextcat, states, cats);
    }
  }
  
  /**
   * Checks, if this is already a state inside the given list.
   */
  protected boolean contains(List<AllianceState> states, AllianceState state) {
    for (AllianceState s : states) {
      if (state.equals(s)) return true;
    }
    return false;
  }
  
  /**
   * returns the maximum alliance category (which must be ALL)
   */
  protected AllianceCategory getMaxAllianceCategory() {
    Iterator<AllianceCategory> iter = world.rules.getAllianceCategoryIterator();

    if(iter.hasNext()) {
      AllianceCategory ret = iter.next();

      while(iter.hasNext()) {
        AllianceCategory ac = iter.next();

        if(ac.compareTo(ret) > 0) {
          ret = ac;
        }
      }

      return ret;
    }

    return null;
  }

  /**
   * @see javax.swing.JComponent#getToolTipText()
   */
  @Override
  public String getToolTipText() {
    return getSelectedItem().toString();
  }

  
}

/**
 * Contains a specific possible alliance state which is actually
 * a list of alliance categories
 * 
 * @author Thoralf Rickert
 * @version 1.0, 25.09.2008
 */
class AllianceState {
  protected List<AllianceCategory> categories = new ArrayList<AllianceCategory>();
  
  /**
   * Adds an alliance category to this state
   */
  public void addCategory(AllianceCategory category) {
    this.categories.add(category);
  }
  
  /**
   * Adds an alliance category list to this state
   */
  public void addCategories(List<AllianceCategory> categories) {
    this.categories.addAll(categories);
  }
  
  /**
   * Returns all categories
   */
  public List<AllianceCategory> getCategories() {
    return categories;
  }
  
  /**
   * Returns the possible bitmask based on the inner
   * alliance categories
   */
  public int getBitMask() {
    int bitmask = 0;
    for (AllianceCategory s : categories) {
      bitmask = bitmask + s.getBitMask();
    }
    return bitmask;
  }
  
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    if (o instanceof AllianceState) {
      return ((AllianceState)o).getBitMask() == getBitMask();
    } else {
      return false;
    }
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    for (AllianceCategory category : categories) buffer.append(Resources.getOrderTranslation(Alliance.ORDER_KEY_PREFIX+category.getName())).append(" ");
    return buffer.toString();
  }
}

class AllianceStateComparator implements Comparator<AllianceState> {
  /**
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  public int compare(AllianceState o1, AllianceState o2) {
    return o1.getBitMask() - o2.getBitMask();
  }
  
}
