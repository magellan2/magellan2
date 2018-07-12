// class magellan.library.gamebinding.MapMetric
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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
package magellan.library.gamebinding;

import java.util.List;

import magellan.library.CoordinateID;
import magellan.library.Region;
import magellan.library.utils.Direction;
import magellan.library.utils.transformation.BoxTransformer.BBox;

/**
 * A map metric handles the properties of a coordinate system, mainly that of directions mapping to
 * coordinates and vice versa.
 *
 * @author stm
 */
public interface MapMetric {

  /**
   * Returns the direction from a coordinate to an adjacent coordinate.
   *
   * @return The corresponding direction, {@link Direction#INVALID} if from and to are not adjacent.
   */
  Direction getDirection(CoordinateID from, CoordinateID to);

  /**
   * Returns the direction from a coordinate to an adjacent coordinate.
   *
   * @return The corresponding direction, {@link Direction#INVALID} if from and to are not adjacent.
   */
  Direction getDirection(Region from, Region to);

  /**
   * Returns the direction for an internal direction code.
   *
   * @see Direction#getDirCode()
   */
  Direction toDirection(int dirCode);

  /**
   * Returns a list of all directions, sorted by ascending direction code.
   */
  List<Direction> getDirections();

  /**
   * Returns the direction opposite to d.
   */
  Direction opposite(Direction d);

  /**
   * Returns the coordinate that is adjacent to c in the specified direction.
   *
   * @return The translated coordinate, {@link CoordinateID#INVALID} if direction ==
   *         {@link Direction#INVALID}
   */
  CoordinateID translate(CoordinateID c, Direction direction);

  /**
   * Returns the difference to the specified directions. E.g., <code>getDifference(NE, W) ==
   -2</code> , <code>getDifference(NE, SE) == 2</code>, <code>getDifference(NE, SW) == 3</code>.
   * Differences to {@link Direction#INVALID} are always {@link Integer#MAX_VALUE}.
   */
  int getDifference(Direction from, Direction to);

  /**
   * Returns a bounding box suited for this coordinate system.
   */
  BBox createBBox();

}
