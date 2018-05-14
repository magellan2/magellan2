/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.client.actions.map;

import java.util.ArrayList;

import magellan.client.Client;
import magellan.library.CoordinateID;
import magellan.library.Region;

/**
 * Adds all regions "IN" the current selection. -> in each of the 6 directions a region in the
 * current selection is found within 300 regions
 *
 * @author Fiete Fietz
 */
public class FillSelectionAction2 extends AbstractSelectionAction {

  /**
   * Creates a new FillSelectionAction object.
   *
   * @param client
   */
  public FillSelectionAction2(Client client) {
    super(client);
  }

  /**
   * Performs the fill operation and fires a SelectionEvent.
   *
   * @see magellan.client.actions.MenuAction#menuActionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void menuActionPerformed(java.awt.event.ActionEvent e) {
    System.out.print("FillSelectionAction2 started");
    ArrayList<CoordinateID> toAdd = new ArrayList<CoordinateID>();
    for (Region r : client.getData().getRegions()) {
      CoordinateID c = r.getCoordinate();
      if (isInSelection(c)) {
        toAdd.add(c);
      }
    }
    if (toAdd.size() > 0) {
      System.out.print("FillSelectionAction2: " + toAdd.size() + " regions to add");
      for (CoordinateID c : toAdd) {
        getSelectedRegions().put(c, client.getData().getRegion(c));
      }
      updateClientSelection();
    }
    System.out.print("FillSelectionAction2 finished");
  }

  private boolean isInSelection(CoordinateID c) {
    for (int i = 1; i <= 6; i++) {
      if (!findBorder(i, c))
        return false;
    }
    return true;
  }

  private boolean findBorder(int dir, CoordinateID c) {
    boolean erg = false;
    int i = 0;
    CoordinateID test = c.clone();
    CoordinateID changeC = CoordinateID.create(0, 0, 0);
    if (dir == 1) {
      changeC = CoordinateID.create(-1, 1, 0);
    }
    if (dir == 2) {
      changeC = CoordinateID.create(0, 1, 0);
    }
    if (dir == 3) {
      changeC = CoordinateID.create(1, 0, 0);
    }
    if (dir == 4) {
      changeC = CoordinateID.create(1, -1, 0);
    }
    if (dir == 5) {
      changeC = CoordinateID.create(0, -1, 0);
    }
    if (dir == 6) {
      changeC = CoordinateID.create(-1, 0, 0);
    }

    while (!erg && i <= 300) {
      test = test.translate(changeC);
      i = i + 1;
      if (getSelectedRegions().keySet().contains(test)) {
        erg = true;
      }
    }

    return erg;
  }
}
