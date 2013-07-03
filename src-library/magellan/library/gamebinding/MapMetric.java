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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc., 
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
package magellan.library.gamebinding;

import java.util.List;

import magellan.library.CoordinateID;
import magellan.library.Region;
import magellan.library.utils.Direction;

public interface MapMetric {

  Direction getDirection(CoordinateID from, CoordinateID to);

  Direction getDirection(Region from, Region to);

  Direction toDirection(int dirCode);

  List<Direction> getDirections();

  Direction opposite(Direction d);

  CoordinateID translate(CoordinateID c, Direction direction);

  // int getDifference(int dirCode);

  // int getDirCode(Direction direction);
  //
  /**
   * Returns the difference to the specified directions. E.g., <code>getDifference(NE, W) ==
   -2</code> ,
   * <code>getDifference(NE, SE) == 2</code>, <code>getDifference(NE, SW) == 3</code>. Differences
   * to {@link #INVALID} are always {@link Integer#MAX_VALUE}.
   */
  int getDifference(Direction from, Direction to);
  //
  // // TODO essential?
  // CoordinateID toCoordinate(Direction d);

}
