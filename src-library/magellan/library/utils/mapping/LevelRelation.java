// class magellan.library.utils.mapping.LevelMapping
// created on 19.05.2008
//
// Copyright 2003-2008 by magellan project team
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
package magellan.library.utils.mapping;

import magellan.library.CoordinateID;

/**
 * Represents the relation between two map levels by some coordinate transformation. Currently
 * translation and scaling is allowed
 * 
 * @author Ralf Duckstein
 * @version 1.0, 21.05.2008
 */

public class LevelRelation extends CoordinateID {
  public int scaleX = 1;
  public int scaleY = 1;
  public int fromLevel = 1;

  public LevelRelation(CoordinateID c) {
    super(c);
    fromLevel = c.z;
  }

  public LevelRelation(CoordinateID c, int scaleX, int scaleY, int fromLevel) {
    super(c);
    this.scaleX = scaleX;
    this.scaleY = scaleY;
    this.fromLevel = fromLevel;
  }

  public LevelRelation(int translateX, int translateY, int toLevel) {
    super(translateX, translateY, toLevel);
    fromLevel = toLevel;
  }

  public LevelRelation(int translateX, int translateY, int toLevel, int scaleX, int scaleY,
      int fromLevel) {
    super(translateX, translateY, toLevel);
    this.scaleX = scaleX;
    this.scaleY = scaleY;
    this.fromLevel = fromLevel;
  }

  public CoordinateID getRelatedCoordinate(CoordinateID c) {
    if (c == null)
      return null;
    if (c.z != fromLevel)
      return null;
    return new CoordinateID(c.x * scaleX + x, c.y * scaleY + y, z);
  }

  public CoordinateID getInverseRelatedCoordinate(CoordinateID c) {
    if (c == null)
      return null;
    if (c.z != z)
      return null;
    return new CoordinateID((c.x - x) / scaleX, (c.y - y) / scaleY, fromLevel);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof CoordinateID) {
      if (super.equals(o)) {
        if (o instanceof LevelRelation) {
          LevelRelation l = (LevelRelation) o;
          return (scaleX == l.scaleX) && (scaleX == l.scaleY) && (fromLevel == l.fromLevel);
        } else {
          CoordinateID c = (CoordinateID) o;
          return (scaleX == 1) && (scaleX == 1) && (fromLevel == c.z);
        }
      } else
        return false;
    } else
      return false;
  }

  @Override
  public int hashCode() {
    return (super.hashCode() << 4) ^ fromLevel;
  }

  @Override
  public String toString() {
    return "trans([0, 0, " + fromLevel + "] -> [" + x + ", " + y + ", " + z + "]) scale(" + scaleX
        + ", " + scaleY + ")";
  }
}
