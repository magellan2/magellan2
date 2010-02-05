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

public class LevelRelation {
  private CoordinateID coord;

  public int scaleX = 1;
  public int scaleY = 1;
  public int fromLevel = 1;

  public LevelRelation(CoordinateID c) {
    coord = CoordinateID.create(c);
    fromLevel = c.getZ();
  }

  public LevelRelation(CoordinateID c, int scaleX, int scaleY, int fromLevel) {
    coord = CoordinateID.create(c);
    this.scaleX = scaleX;
    this.scaleY = scaleY;
    this.fromLevel = fromLevel;
  }

  public LevelRelation(int translateX, int translateY, int toLevel) {
    coord = CoordinateID.create(translateX, translateY, toLevel);
    fromLevel = toLevel;
  }

  public LevelRelation(int translateX, int translateY, int toLevel, int scaleX, int scaleY,
      int fromLevel) {
    coord = CoordinateID.create(translateX, translateY, toLevel);
    this.scaleX = scaleX;
    this.scaleY = scaleY;
    this.fromLevel = fromLevel;
  }

  /**
   * Returns a new coordinate, scaled or null if c==null or c is in the wrong level. TODO
   * DOCUMENT-ME
   */
  public CoordinateID getRelatedCoordinate(CoordinateID c) {
    if (c == null)
      return null;
    if (c.getZ() != fromLevel)
      return null;
    return CoordinateID.create(c.getX() * scaleX + getX(), c.getY() * scaleY + getY(), getZ());
  }

  public CoordinateID getInverseRelatedCoordinate(CoordinateID c) {
    if (c == null)
      return null;
    if (c.getZ() != getZ())
      return null;
    return CoordinateID.create((c.getX() - getX()) / scaleX, (c.getY() - getY()) / scaleY, fromLevel);
  }

  // FIXME overriding equals violates the contract of equals (symmetry)!
  @Override
  public boolean equals(Object o) {
    if (o instanceof CoordinateID) {
      if (super.equals(o)) {
        if (o instanceof LevelRelation) {
          LevelRelation l = (LevelRelation) o;
          return (scaleX == l.scaleX) && (scaleX == l.scaleY) && (fromLevel == l.fromLevel);
        } else {
          CoordinateID c = (CoordinateID) o;
          return (scaleX == 1) && (scaleX == 1) && (fromLevel == c.getZ());
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
    return "trans([0, 0, " + fromLevel + "] -> [" + getX() + ", " + getY() + ", " + getZ()
        + "]) scale(" + scaleX + ", " + scaleY + ")";
  }

  public int getX() {
    return coord.getX();
  }

  public int getY() {
    return coord.getY();
  }

  public int getZ() {
    return coord.getZ();
  }
}
