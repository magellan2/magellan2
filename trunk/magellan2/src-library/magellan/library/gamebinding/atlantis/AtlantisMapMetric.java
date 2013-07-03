// class magellan.library.gamebinding.atlantis.AtlantisMapMetric
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
package magellan.library.gamebinding.atlantis;

import java.util.ArrayList;
import java.util.List;

import magellan.library.CoordinateID;
import magellan.library.Rules;
import magellan.library.gamebinding.EresseaMapMetric;
import magellan.library.utils.Direction;
import magellan.library.utils.transformation.BoxTransformer.BBox;

/**
 * The metric for the atlantis game hexagonal map.
 * 
 * @author stm
 */
public class AtlantisMapMetric extends EresseaMapMetric {

  /** The direction Mir ("northwest") */
  public static final Direction M = new Direction(0, AtlantisConstants.OC_M, CoordinateID.create(
      -1, -1), "pfeil0");
  /** The direction North */
  public static final Direction N = new Direction(1, AtlantisConstants.OC_N, CoordinateID.create(0,
      -1), "pfeil1");
  /** The direction East */
  public static final Direction E = new Direction(2, AtlantisConstants.OC_E, CoordinateID.create(1,
      0), "pfeil2");
  /** The direction Mir ("northeast") */
  public static final Direction Y = new Direction(3, AtlantisConstants.OC_Y, CoordinateID.create(1,
      1), "pfeil3");
  /** The direction South */
  public static final Direction S = new Direction(4, AtlantisConstants.OC_S, CoordinateID.create(0,
      1), "pfeil4");
  /** The direction West */
  public static final Direction W = new Direction(5, AtlantisConstants.OC_W, CoordinateID.create(
      -1, 0), "pfeil5");

  private static final List<Direction> directions;

  static {
    directions = new ArrayList<Direction>(6);
    directions.add(M);
    directions.add(N);
    directions.add(E);
    directions.add(Y);
    directions.add(S);
    directions.add(W);
  }

  /**
   * 
   */
  public AtlantisMapMetric(Rules rules) {
    super(rules);
  }

  @Override
  public Direction toDirection(int x, int y) {
    if (x == -1) {
      if (y == 0)
        return W;
      else if (y == -1)
        return M;
    } else if (x == 0) {
      if (y == 1)
        return S;
      else if (y == -1)
        return N;
    } else if (x == 1) {
      if (y == 1)
        return Y;
      else if (y == 0)
        return E;
    }

    return Direction.INVALID;
  }

  @Override
  public Direction toDirection(int dirCode) {
    switch (dirCode) {
    case 0:
      return M;
    case 1:
      return N;
    case 2:
      return E;
    case 3:
      return Y;
    case 4:
      return S;
    case 5:
      return W;

    default:
      return Direction.INVALID;
    }
  }

  @Override
  public List<Direction> getDirections() {
    return directions;
  }

  @Override
  public Direction opposite(Direction d) {
    return directions.get((d.getDirCode() + 3) % 6);
  }

  @Override
  public BBox createBBox() {
    return new EresseaMapMetric.EBBox(1, 2, 0, 1);
  }
}
