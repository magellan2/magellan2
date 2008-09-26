// class magellan.plugin.groupeditor.AllianceStateCellRenderer
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

import java.awt.Color;
import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableCellRenderer;

import magellan.library.utils.MagellanImages;

public class AllianceStateRenderer extends JLabel implements ListCellRenderer, TableCellRenderer {
  public AllianceStateRenderer() {
    setOpaque(true);
  }
  
  /**
   * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
   */
  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    return getRendererComponent(list, list.getSelectionBackground(), list.getSelectionForeground(), value, isSelected, cellHasFocus);
  }

  /**
   * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
   */
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    return getRendererComponent(table, table.getSelectionBackground(), table.getSelectionForeground(), value, isSelected, hasFocus);
  }
  
  protected Component getRendererComponent(JComponent component, Color sBackground, Color sForeground, Object value, boolean isSelected, boolean hasFocus) {
    int bitmask = 0;
    String text = "";
    AllianceState state = (AllianceState)value;
    if (state != null) bitmask = state.getBitMask();
    if (state != null) text = state.toString();
    

    if (isSelected) {
        setBackground(sBackground);
        setForeground(sForeground);
    } else {
        setBackground(component.getBackground());
        setForeground(component.getForeground());
    }
    
    setToolTipText(state.toString());

    //Set the icon and text.  If icon was null, say so.
    ImageIcon icon = MagellanImages.getImageIcon("etc/images/icons/alliancestate_"+bitmask+".gif");
    setIcon(icon);
    setText(text);
    setFont(component.getFont());

    return this;
  }
  
}