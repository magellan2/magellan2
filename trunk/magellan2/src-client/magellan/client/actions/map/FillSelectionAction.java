/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.client.actions.map;

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
      if (c.getZ() == client.getLevel()) {
        if (c.getX() > maxX) {
          maxX = c.getX();
        }

        if (c.getY() > maxY) {
          maxY = c.getY();
        }

        if (c.getX() < minX) {
          minX = c.getX();
        }

        if (c.getY() < minY) {
          minY = c.getY();
        }
      }
    }

    for (CoordinateID c : client.getData().regions().keySet()) {
      if ((c.getZ() == client.getLevel()) && (c.getX() <= maxX) && (c.getX() >= minX)
          && (c.getY() <= maxY) && (c.getY() >= minY)) {
        getSelectedRegions().put(c, client.getData().getRegion(c));
      }
    }

    updateClientSelection();
  }

}
