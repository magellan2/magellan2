// class magellan.client.actions.map.AbstractSelectionAction
// created on Jan 19, 2010
//
// Copyright 2003-2010 by magellan project team
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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
package magellan.client.actions.map;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.library.CoordinateID;
import magellan.library.Region;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.utils.Resources;

/**
 * An abstract base class for selection action handling the selection updates.
 * 
 * @author stm
 * @version 1.0, Jan 19, 2010
 */
public abstract class AbstractSelectionAction extends MenuAction implements SelectionListener,
    GameDataListener {

  private Map<CoordinateID, Region> selectedRegions = new TreeMap<CoordinateID, Region>();

  /**
   * Creates the abstract action and registers as {@link GameDataListener} and
   * {@link SelectionListener}.
   * 
   * @param client
   */
  public AbstractSelectionAction(Client client) {
    super(client);
    this.client.getDispatcher().addSelectionListener(this);
    this.client.getDispatcher().addGameDataListener(this);
  }

  /**
   * Returns the value of selectedRegions. Changes to the returned object will be reflected by
   * future calls of this method.
   * 
   * @return Returns selectedRegions.
   */
  public Map<CoordinateID, Region> getSelectedRegions() {
    return selectedRegions;
  }

  /**
   * Updates the currentselection. 
   */
  public void selectionChanged(SelectionEvent e) {
    if(e.getSource() == this) {
      return;
    }

    if (e.getSelectionType() == SelectionEvent.ST_REGIONS && e.getSelectedObjects() != null) {
      selectedRegions.clear();

      for (Iterator<Object> iter = e.getSelectedObjects().iterator(); iter.hasNext();) {
        Object o = iter.next();

        if(o instanceof Region) {
          Region r = (Region) o;
          selectedRegions.put(r.getID(), r);
        }
      }
    }
  }

  /**
   * Clears the current selection.
   * 
   * @see magellan.library.event.GameDataListener#gameDataChanged(magellan.library.event.GameDataEvent)
   */
  public void gameDataChanged(GameDataEvent e) {
    selectedRegions.clear();
  }

  /**
   * Should change the selection and call {@link #updateClientSelection()}.
   * 
   * @see magellan.client.actions.MenuAction#menuActionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public abstract void menuActionPerformed(ActionEvent e);

  
  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get(getResourcePrefix()+".accelerator",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get(getResourcePrefix()+".mnemonic",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get(getResourcePrefix()+".name");
  }

  /**
   * Always returns <code>null</code>! Overwrite if you want a tool tip.
   * 
   * @see magellan.client.actions.MenuAction#getTooltipTranslated()
   */
  @Override
  protected String getTooltipTranslated() {
    return null;
    // return Resources.get(getResourcePrefix()+".tooltip");
  }

  /**
   * Returns a prefix used for the resource keys of the class.
   */
  protected String getResourcePrefix() {
    return "actions."+this.getClass().getSimpleName().toLowerCase();
  }

  /**
   * Notifies the client of the new selection.
   */
  protected void updateClientSelection() {
    client.getData().setSelectedRegionCoordinates(getSelectedRegions());
    client.getDispatcher().fire(SelectionEvent.create(this, getSelectedRegions().values()));
//        new SelectionEvent(this, getSelectedRegions().values(), null, SelectionEvent.ST_REGIONS));
  }

}
