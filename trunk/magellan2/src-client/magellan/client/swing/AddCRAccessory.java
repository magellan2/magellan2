// class magellan.client.swing.AddCRAccessory
// created on Dec 20, 2007
//
// Copyright 2003-2007 by magellan project team
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
package magellan.client.swing;

import java.awt.GridBagConstraints;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;

import magellan.library.utils.Resources;

/**
 * Plugin for options for the add CR dialog
 */
public class AddCRAccessory extends HistoryAccessory {
  private JCheckBox chkSort;
  private JCheckBox chkInteractive;

  /**
   * Creates a new OpenOrdersAccessory object.
   */
  public AddCRAccessory(Properties setting, JFileChooser fileChooser) {
    super(setting, fileChooser);

    GridBagConstraints c = new GridBagConstraints();

    chkSort = new JCheckBox(Resources.get("addcraccessory.chk.sort.caption"));
    chkSort.setToolTipText(Resources.get("addcraccessory.chk.sort.tooltip"));

    c.anchor = GridBagConstraints.NORTHWEST;
    c.gridx = 0;
    c.gridy = 2;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    this.add(chkSort, c);

    chkInteractive = new JCheckBox(Resources.get("addcraccessory.chk.interactive.caption"));
    chkInteractive.setToolTipText(Resources.get("addcraccessory.chk.interactive.tooltip"));

    c.anchor = GridBagConstraints.NORTHWEST;
    c.gridx = 0;
    c.gridy = 3;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    this.add(chkInteractive, c);

  }

  /**
   * Returns <code>true</code> if the sort box is selected.
   */
  public boolean getSort() {
    return chkSort.isSelected();
  }

  /**
   * Selects or un-selects the sort box.
   */
  public void setSort(boolean sort) {
    chkSort.setSelected(sort);
  }

  /**
   * Returns <code>true</code> if the interactive box is selected.
   */
  public boolean getInteractive() {
    return chkInteractive.isSelected();
  }

  /**
   * Selects or un-selects the interactive box.
   */
  public void setInteractive(boolean interactive) {
    chkInteractive.setSelected(interactive);
  }

}
