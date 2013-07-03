// class magellan.library.gamebinding.EresseaMapMetric
// created on Apr 29, 2013
//
// Copyright 2003-2013 by magellan project team
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc., 
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
package magellan.library.gamebinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import magellan.library.CoordinateID;
import magellan.library.Region;
import magellan.library.Rules;
import magellan.library.utils.Direction;

public class EresseaMapMetric implements MapMetric {

  public static final Direction NW = new Direction(0, EresseaConstants.OC_NW, CoordinateID.create(
      -1, 1), "pfeil5");
  public static final Direction NE = new Direction(1, EresseaConstants.OC_NE, CoordinateID.create(
      0, 1), "pfeil5");
  public static final Direction E = new Direction(2, EresseaConstants.OC_E, CoordinateID.create(1,
      0), "pfeil5");
  public static final Direction SE = new Direction(3, EresseaConstants.OC_SE, CoordinateID.create(
      1, -1), "pfeil5");
  public static final Direction SW = new Direction(4, EresseaConstants.OC_SW, CoordinateID.create(
      0, -1), "pfeil5");
  public static final Direction W = new Direction(5, EresseaConstants.OC_W, CoordinateID.create(-1,
      0), "pfeil5");

  private Rules rules;
  private static final List<Direction> directions;

  static {
    directions = new ArrayList<Direction>(6);
    directions.add(NW);
    directions.add(NE);
    directions.add(E);
    directions.add(SE);
    directions.add(SW);
    directions.add(W);
  }

  public EresseaMapMetric(Rules rules) {
    this.rules = rules;
  }

  public Direction getDirection(CoordinateID from, CoordinateID to) {
    if (to.getZ() != from.getZ())
      return Direction.INVALID;
    return toDirection(to.getX() - from.getX(), to.getY() - from.getY());

  }

  /**
   * Converts to coordinates to a direction.
   * 
   * @see #toDirection(CoordinateID)
   */
  public Direction toDirection(int x, int y) {
    if (x == -1) {
      if (y == 0)
        return W;
      else if (y == 1)
        return NW;
    } else if (x == 0) {
      if (y == -1)
        return SW;
      else if (y == 1)
        return NE;
    } else if (x == 1) {
      if (y == -1)
        return SE;
      else if (y == 0)
        return E;
    }

    return Direction.INVALID;
  }

  public Direction getDirection(Region from, Region to) {
    Map<Direction, Region> neighbors = from.getNeighbors();
    for (Direction d : neighbors.keySet()) {
      if (neighbors.get(d) == to)
        return d;
    }
    return Direction.INVALID;
  }

  public Direction toDirection(int dirCode) {
    switch (dirCode) {
    case 0:
      return NW;
    case 1:
      return NE;
    case 2:
      return E;
    case 3:
      return SE;
    case 4:
      return SW;
    case 5:
      return W;

    default:
      return Direction.INVALID;
    }
  }

  public List<Direction> getDirections() {
    return directions;
  }

  public Direction opposite(Direction d) {
    return directions.get((d.getDirCode() + 3) % 6);
  }

  public CoordinateID translate(CoordinateID c, Direction direction) {
    return c.translate(direction.toCoordinate());
  }

  public int getDifference(Direction from, Direction to) {
    if (from == Direction.INVALID || to == Direction.INVALID)
      return Integer.MAX_VALUE;
    int dir = from.getDirCode(), toDir = to.getDirCode();
    return (dir - toDir + 3 >= 0 ? 3 - (dir - toDir + 3) % 6 : (toDir - dir + 3) % 6 - 3);

  }

}
