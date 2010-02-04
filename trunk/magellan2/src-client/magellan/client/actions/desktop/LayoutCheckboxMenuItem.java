// class magellan.client.actions.desktop.LayoutAction
// created on 18.11.2007
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
package magellan.client.actions.desktop;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;

import magellan.client.desktop.DockingFrameworkBuilder;
import magellan.client.desktop.DockingLayout;

/**
 * This class represents a menu item for one Docking layout. By selecting this item, all other items
 * are disabled.
 * 
 * @author Thoralf Rickert
 * @version 1.0, 18.11.2007
 */
public class LayoutCheckboxMenuItem extends JCheckBoxMenuItem implements ActionListener {
  private DockingLayout layout = null;

  public LayoutCheckboxMenuItem(DockingLayout layout) {
    super(layout.getName(), layout.isActive());
    this.layout = layout;

    addActionListener(this);
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void actionPerformed(ActionEvent evt) {
    LayoutCheckboxMenuItem source = (LayoutCheckboxMenuItem) evt.getSource();
    if (source == null)
      return;
    if (layout.isActive())
      return;
    if (isSelected()) {
      DockingFrameworkBuilder.getInstance().setActiveLayout(layout);
    }
  }
}
