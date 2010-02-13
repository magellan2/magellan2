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
import magellan.library.utils.Islands;

/**
 * Selects the regions of all islands that have a selected region.
 * 
 * @author Ulrich Küster
 */
public class SelectIslandsAction extends AbstractSelectionAction {

  /**
   * Creates a new SelectIslandsAction object.
   * 
   * @param client
   */
  public SelectIslandsAction(Client client) {
    super(client);
  }

  /**
   * Expands the selection and notifies client.
   * 
   * @see magellan.client.actions.map.AbstractSelectionAction#menuActionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void menuActionPerformed(ActionEvent e) {
    // add all regions, that were selected before and don't belong to the active level
    // or that belong to the active level region _and_ that belong to an island
    // that contained at least one selected region before
    for (CoordinateID c : client.getSelectedRegions().keySet()) {
      if (c.getZ() == client.getLevel()) {
        getSelectedRegions().putAll(
            Islands.getIsland(client.getData().rules, client.getData().regions(), client.getData()
                .getRegion(c)));
      }
    }

    updateClientSelection();
  }

}
