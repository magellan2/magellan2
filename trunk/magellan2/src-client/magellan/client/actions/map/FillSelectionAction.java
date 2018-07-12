/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.client.actions.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import magellan.client.Client;
import magellan.library.CoordinateID;
import magellan.library.Region;
import magellan.library.gamebinding.MapMetric;
import magellan.library.utils.Direction;
import magellan.library.utils.Regions;

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
    fillConvexHull();
  }

  @SuppressWarnings("unused")
  private void fillConvexHull() {

    List<CoordinateID> hull = new ArrayList<CoordinateID>(getSelectedRegions().keySet());
    for (Iterator<CoordinateID> iterator = hull.iterator(); iterator.hasNext();) {
      CoordinateID coordinateID = iterator.next();
      if (coordinateID.getZ() != client.getLevel()) {
        iterator.remove();
      }
    }

    hull = Regions.convexHull(hull);
    if (hull.isEmpty())
      return;

    // ensure that inside is connected by adding the paths between hull vertices
    MapMetric metric = getMapMetric();
    List<CoordinateID> hull2 = new ArrayList<CoordinateID>();
    CoordinateID lastC = null;
    for (CoordinateID c : hull) {
      if (lastC == null) {
        lastC = hull.get(hull.size() - 1);
      }

      while (!lastC.equals(c)) {
        hull2.add(lastC);
        int mindiff = Integer.MAX_VALUE + 0;
        CoordinateID minc = c;
        for (Direction d : metric.getDirections()) {
          CoordinateID nextC = metric.translate(lastC, d);
          int diff = Math.abs(nextC.getY() - c.getY()) + Math.abs(nextC.getX() - c.getX());
          if (diff < mindiff) {
            mindiff = diff;
            minc = nextC;
          }
        }
        lastC = minc;

      }
    }

    hull = Regions.filter(client.getLevel(), hull);
    double[] xx = new double[hull.size()], yy = new double[hull.size()];
    Regions.coords2Points(hull, xx, yy);
    for (Region r : client.getData().getRegions()) {
      CoordinateID c = r.getCoordinate();
      if (Regions.insidePolygon(c, hull) >= 0) {
        getSelectedRegions().put(c, r);
      }
    }
    Collection<? extends Collection<Region>> comps = Regions.getComponents(getSelectedRegions());

    Collection<Region> firstComp = null;
    for (Iterator<? extends Collection<Region>> iterator = comps.iterator(); iterator.hasNext();) {
      if (firstComp == null) {
        firstComp = iterator.next();
      }
      while (iterator.hasNext()) {
        Collection<Region> currentComp = iterator.next();
        List<CoordinateID> line = new ArrayList<CoordinateID>();
        line.add(firstComp.iterator().next().getCoordinate());
        line.add(currentComp.iterator().next().getCoordinate());
        Regions.coords2Points(line, xx, yy);
        for (Region r : client.getData().getRegions()) {
          CoordinateID c = r.getCoordinate();
          if (Regions.hexagonIntersects(c, xx, yy)) {
            getSelectedRegions().put(c, r);
          }
        }
      }
    }
    updateClientSelection();
  }

  private MapMetric getMapMetric() {
    return Regions.getMapMetric(getSelectedRegions().values().iterator().next());
  }

  @SuppressWarnings("unused")
  private void fillBoundingBox() {
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

    for (Region r : client.getData().getRegions()) {
      CoordinateID c = r.getCoordinate();
      if ((c.getZ() == client.getLevel()) && (c.getX() <= maxX) && (c.getX() >= minX)
          && (c.getY() <= maxY) && (c.getY() >= minY)) {
        getSelectedRegions().put(r.getCoordinate(), r);
      }
    }

    updateClientSelection();
  }

}
