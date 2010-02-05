/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.client.actions.map;

import java.awt.event.ActionEvent;

import magellan.client.Client;
import magellan.library.CoordinateID;

/**
 * Inverts the current selections (selects unselected regions). Works only on current layer!
 * 
 * @author Ulrich Küster
 */
public class InvertSelectionAction extends AbstractSelectionAction {

  /**
   * Creates a new InvertSelectionAction object.
   * 
   * @param client
   */
  public InvertSelectionAction(Client client) {
    super(client);
  }

  /**
   * Performs the selection change and fires SelectionEvent.
   * 
   * @see magellan.client.actions.MenuAction#menuActionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void menuActionPerformed(ActionEvent e) {
    getSelectedRegions().clear();

    // add all regions that belong to the active level XOR were selected befor
    for (CoordinateID c : client.getData().regions().keySet()) {
      if ((c.getZ() == client.getLevel()) ^ client.getSelectedRegions().containsKey(c)) {
        getSelectedRegions().put(c, client.getData().regions().get(c));
      }
    }

    updateClientSelection();
  }

}
