// class magellan.client.actions.desktop.LayoutDeleteMenu
// created on 16.11.2007
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

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.library.utils.Resources;

public class LayoutDeleteAction extends MenuAction {
  /**
   * 
   */
  public LayoutDeleteAction() {
    super(Client.INSTANCE);
  }
  
  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return null;
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return null;
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("desktop.magellandesktop.menu.desktop.layout.delete.caption");
  }

  /**
   * @see magellan.client.actions.MenuAction#getTooltipTranslated()
   */
  @Override
  protected String getTooltipTranslated() {
    return null;
  }

  /**
   * @see magellan.client.actions.MenuAction#menuActionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void menuActionPerformed(ActionEvent e) {
    
  }

}
