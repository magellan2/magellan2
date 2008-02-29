// class magellan.plugin.extendedcommands.PlugInAction
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

import java.awt.event.ActionEvent;


/**
 * An enum for all action types in this plugin.
 *
 * @author Thoralf Rickert
 * @version 1.0, 11.09.2007
 */
public enum PlugInAction {
  EXECUTE_ALL("mainmenu.execute"),
  CONFIGURE_ALL("mainmenu.configure"),
  LIBRARY_EDIT("mainmenu.library"),
  UNKNOWN("");
  
  private String id;
  
  private PlugInAction(String id) {
    this.id = id;
  }
  
  public String getID() {
    return id;
  }
  
  public static PlugInAction getAction(ActionEvent e) {
    if (e == null) return UNKNOWN;
    for (PlugInAction action : values()) {
      if (action.id.equalsIgnoreCase(e.getActionCommand())) return action;
    }
    return UNKNOWN;
  }

}
