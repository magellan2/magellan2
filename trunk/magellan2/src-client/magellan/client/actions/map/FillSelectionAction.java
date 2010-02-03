/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.client.actions.map;

import java.util.Iterator;

import magellan.client.Client;
import magellan.library.CoordinateID;

/**
 * Adds all regions between the extreme points of the current selection.
 * 
 * @author Ulrich Küster
 */
public class FillSelectionAction extends AbstractSelectionAction {

  /**
   * Creates a new FillSelectionAction object.
   * 
   * @param client
   */
  public FillSelectionAction(Client client) {
    super(client);
  }

  /**
   * Performs the fill operation and fires a SelectionEvent.
   * 
   * @see magellan.client.actions.MenuAction#menuActionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void menuActionPerformed(java.awt.event.ActionEvent e) {
    int minX = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE;
    int minY = Integer.MAX_VALUE;
    int maxY = Integer.MIN_VALUE;

    for (CoordinateID c : getSelectedRegions().keySet()) {
      if (c.z == client.getLevel()) {
        if (c.x > maxX) {
          maxX = c.x;
        }

        if (c.y > maxY) {
          maxY = c.y;
        }

        if (c.x < minX) {
          minX = c.x;
        }

        if (c.y < minY) {
          minY = c.y;
        }
      }
    }

    for (Iterator<CoordinateID> iter = client.getData().regions().keySet().iterator(); iter.hasNext();) {
      CoordinateID c = iter.next();

      if ((c.z == client.getLevel()) && (c.x <= maxX) && (c.x >= minX) && (c.y <= maxY)
          && (c.y >= minY)) {
        getSelectedRegions().put(c, client.getData().regions().get(c));
      }
    }

    updateClientSelection();
  }

}
