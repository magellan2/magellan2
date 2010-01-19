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
 * Selects all regions in the current layer
 * 
 * @author Ulrich Küster
 */
public class SelectAllAction extends AbstractSelectionAction {

  /**
   * Creates a new SelectAllAction object.
   * 
   * @param client
   */
  public SelectAllAction(Client client) {
    super(client);
  }

  /**
   * Performs the new selection and notifies client.
   */
  @Override
  public void menuActionPerformed(ActionEvent e) {
    for (Iterator iter = client.getData().regions().keySet().iterator(); iter.hasNext();) {
      CoordinateID c = (CoordinateID) iter.next();

      if (c.z == client.getLevel()) {
        getSelectedRegions().put(c, client.getData().regions().get(c));
      }
    }

    updateClientSelection();
  }

}
