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
import magellan.library.Region;

/**
 * Expanding a Selection about 1 region in each direction (all neighbours, feature request)
 * 
 * @author Fiete
 */
public class ExpandSelectionAction extends AbstractSelectionAction {
  /**
   * Creates a new InvertSelectionAction object.
   * 
   * @param client
   */
  public ExpandSelectionAction(Client client) {
    super(client);
  }

  /**
   * Performs the selection expansion.
   * 
   * @see magellan.client.actions.MenuAction#menuActionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void menuActionPerformed(ActionEvent e) {
    // append the neighboring regions to the already selected regions
    if (client.getData().getSelectedRegionCoordinates() == null)
      return;
    // add all the current selected region in one move
    for (CoordinateID c : client.getData().getSelectedRegionCoordinates().keySet()) {
      Region region = client.getData().getRegion(c);
      // get neighbors
      for (Region n : region.getNeighbors().values()) {
        getSelectedRegions().put(n.getID(), n);
      }
    }

    updateClientSelection();
  }

}
