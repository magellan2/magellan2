/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.client.actions.map;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import magellan.client.Client;
import magellan.library.CoordinateID;
import magellan.library.Region;
import magellan.library.gamebinding.MapMetric;
import magellan.library.utils.Direction;

/**
 * Adds all regions "inside" the current selection. -&gt; in each of the 6 directions a region in the
 * current selection is found within 300 regions
 *
 * @author Fiete Fietz
 */
public class FillInsideAction extends AbstractSelectionAction {

  /**
   * Creates a new FillSelectionAction object.
   *
   * @param client
   */
  public FillInsideAction(Client client) {
    super(client);
  }

  /**
   * Performs the fill operation and fires a SelectionEvent.
   *
   * @see magellan.client.actions.MenuAction#menuActionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void menuActionPerformed(java.awt.event.ActionEvent e) {
    if (getSelectedRegions().isEmpty())
      return;

    Map<CoordinateID, Integer> status = new HashMap<CoordinateID, Integer>();
    int minx = Integer.MAX_VALUE, miny = Integer.MAX_VALUE, maxx = Integer.MIN_VALUE, maxy =
        Integer.MIN_VALUE;
    for (CoordinateID c : getSelectedRegions().keySet()) {
      status.put(c, 0);
      minx = Math.min(minx, c.getX());
      miny = Math.min(miny, c.getY());
      maxx = Math.max(maxx, c.getX());
      maxy = Math.max(maxy, c.getY());
    }

    MapMetric metric = client.getData().getGameSpecificStuff().getMapMetric();

    Stack<CoordinateID> bag = new Stack<CoordinateID>();

    for (int currentStatus = 1;; ++currentStatus) {
      for (Region r : client.getData().getRegions()) {
        CoordinateID c = r.getCoordinate();
        if (status.get(c) == null) {
          if (c.getX() < minx || c.getX() > maxx || c.getY() < miny || c.getY() > maxy) {
            //
          } else {
            bag.add(r.getCoordinate());
            break;
          }
        }
      }
      if (bag.isEmpty())
        return;

      boolean border = false;
      while (!bag.isEmpty()) {
        CoordinateID currentC = bag.pop();
        status.put(currentC, currentStatus);

        for (Direction dir : metric.getDirections()) {
          CoordinateID c = metric.translate(currentC, dir);
          if (c.getX() < minx || c.getX() > maxx || c.getY() < miny || c.getY() > maxy) {
            // Region r = client.getData().getRegion(c);
            // if (r == null) {
            border = true;
          } else if (status.get(c) == null && client.getData().getRegion(c) != null) {
            bag.push(c);
          }
        }
      }
      if (!border) {
        addToSelection(status, currentStatus);
      }

    }
  }

  private void addToSelection(Map<CoordinateID, Integer> toAdd, int currentStatus) {
    for (CoordinateID c : toAdd.keySet()) {
      if (toAdd.get(c) == currentStatus) {
        getSelectedRegions().put(c, client.getData().getRegion(c));
      }
    }
    updateClientSelection();
  }

}
