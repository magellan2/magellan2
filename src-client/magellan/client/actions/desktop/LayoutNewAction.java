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

import javax.swing.JOptionPane;

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.client.desktop.DockingFrameworkBuilder;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * With the help of the action it is possible to create new docking layouts.
 *
 * @author ...
 * @version 1.0, 19.11.2007
 */
public class LayoutNewAction extends MenuAction {
  private static final Logger log = Logger.getInstance(LayoutNewAction.class);
  
  /**
   * 
   */
  public LayoutNewAction() {
    super(Client.INSTANCE);
  }
  
  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("desktop.magellandesktop.menu.desktop.layout.new.accelerator",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("desktop.magellandesktop.menu.desktop.layout.new.mnemonic",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("desktop.magellandesktop.menu.desktop.layout.new.caption");
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
    log.info("LayoutNewAction.actionPerformed() called");
    
    Object result = JOptionPane.showInputDialog(
        Client.INSTANCE, 
        Resources.get("desktop.magellandesktop.msg.layout.new.caption"),
        Resources.get("desktop.magellandesktop.msg.layout.new.title"),
        JOptionPane.PLAIN_MESSAGE,
        null,
        null,
        Resources.get("desktop.magellandesktop.msg.layout.new.default")
        );
    if (result == null) return;
    String newLayoutName = result.toString();
    
    if (DockingFrameworkBuilder.getInstance().getLayout(newLayoutName) != null) {
      JOptionPane.showMessageDialog(Client.INSTANCE, Resources.get("desktop.magellandesktop.msg.layout.new.exists"));
      return;
    }
    
    DockingFrameworkBuilder.getInstance().createNewLayout(newLayoutName);
  }

}
