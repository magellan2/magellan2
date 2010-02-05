/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.client.actions.map;

import java.awt.event.ActionEvent;
import java.util.Iterator;

import magellan.client.Client;
import magellan.library.CoordinateID;

/**
 * Clears the selection
 * 
 * @author Ulrich Küster
 */
public class SelectNothingAction extends AbstractSelectionAction {
  /**
   * Creates a new SelectNothingAction object.
   * 
   * @param client
   */
  public SelectNothingAction(Client client) {
    super(client);
  }

  /**
   * Clears selection in the current layer and notifies client.
   * 
   * @see magellan.client.actions.map.AbstractSelectionAction#menuActionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void menuActionPerformed(ActionEvent e) {
    for (Iterator<CoordinateID> iter = getSelectedRegions().keySet().iterator(); iter.hasNext();) {
      CoordinateID c = iter.next();

      if (c.getZ() == client.getLevel()) {
        iter.remove();
      }
    }

    updateClientSelection();
  }

}
