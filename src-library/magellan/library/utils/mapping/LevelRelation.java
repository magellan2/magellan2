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

  private int scaleX = 1;
  private int scaleY = 1;
  private int fromLevel = 1;

  /**
   * Creates a relation with <code>fromLevel == toLevel == c.getZ()</code> and
   * <code>scaleX == scaleY == 1</code>.
   * 
   * @see #LevelRelation(int, int, int, int, int, int)
   */
  public LevelRelation(CoordinateID c) {
    coord = CoordinateID.create(c);
    fromLevel = c.getZ();
  }

  /**
   * Creates a relation with <code>toLevel == c.getZ()</code>.
   * 
   * @see #LevelRelation(int, int, int, int, int, int)
   */
  public LevelRelation(CoordinateID c, int scaleX, int scaleY, int fromLevel) {
    coord = CoordinateID.create(c);
    this.scaleX = scaleX;
    this.scaleY = scaleY;
    this.fromLevel = fromLevel;
  }

  /**
   * Creates a relation with <code>fromLevel == toLevel</code>.
   * 
   * @see #LevelRelation(int, int, int, int, int, int)
   */
  public LevelRelation(int translateX, int translateY, int toLevel) {
    coord = CoordinateID.create(translateX, translateY, toLevel);
    fromLevel = toLevel;
  }

  /**
   * @param translateX x-coordinate of the region in toLevel centered below
   *          <code>(0,0,fromLevel)</code>.
   * @param translateY y-coordinate of the region in toLevel centered below
   *          <code>(0,0,fromLevel)</code>.
   * @param toLevel
   * @param scaleX this many regions in toLevel fit into one region of fromLevel
   * @param scaleY this many regions in toLevel fit into one region of fromLevel
   * @param fromLevel
   */
  public LevelRelation(int translateX, int translateY, int toLevel, int scaleX, int scaleY,
      int fromLevel) {
    coord = CoordinateID.create(translateX, translateY, toLevel);
    this.scaleX = scaleX;
    this.scaleY = scaleY;
    this.fromLevel = fromLevel;
  }

  /**
   * Translates a coordinate in {@link #getFromLevel()} to a coordinate in {@link #getZ()}
   * 
   * @return a new coordinate, scaled or <code>null</code> if <code>c==null</code> or <code>c</code>
   *         is in the wrong level.
   */
  public CoordinateID getRelatedCoordinate(CoordinateID c) {
    if (c == null)
      return null;
    if (c.getZ() != fromLevel)
      return null;
    return CoordinateID.create(c.getX() * scaleX + getX(), c.getY() * scaleY + getY(), getZ());
  }

  /**
   * Translates a coordinate in {@link #getZ()} to a coordinate in {@link #getFromLevel()}
   * 
   * @return a new coordinate, scaled or <code>null</code> if <code>c==null</code> or <code>c</code>
   *         is in the wrong level.
   */
  public CoordinateID getInverseRelatedCoordinate(CoordinateID c) {
    if (c == null)
      return null;
    if (c.getZ() != getZ())
      return null;
    return CoordinateID.create((c.getX() - getX()) / scaleX, (c.getY() - getY()) / scaleY,
        fromLevel);
  }

  /**
   * Returns true if
   * <code>c.equals({@link #getX()}, {@link #getY()}, {@link #getZ()}) &amp;&amp; scaleX == scaleY == 1</code>.
   */
  public boolean equals(CoordinateID c) {
    return c.equals(coord) && (scaleX == 1) && (scaleY == 1) && (fromLevel == c.getZ());
  }

  /**
   * Returns true if o is a LevelRelation with all parameters equal to this one's.
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    if (o instanceof LevelRelation) {
      LevelRelation l = (LevelRelation) o;
      return l.equals(coord) && (scaleX == l.scaleX) && (scaleX == l.scaleY)
          && (fromLevel == l.fromLevel);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return (((coord.hashCode() << 4) + fromLevel) << 4) + scaleX;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "trans([0, 0, " + fromLevel + "] -> [" + getX() + ", " + getY() + ", " + getZ()
        + "]) scale(" + scaleX + ", " + scaleY + ")";
  }

  /**
   * Returns the x value.
   */
  public int getX() {
    return coord.getX();
  }

  /**
   * Returns the y value.
   */
  public int getY() {
    return coord.getY();
  }

  /**
   * Returns the z value (which is the "toLevel").
   */
  public int getZ() {
    return coord.getZ();
  }

  /**
   * Returns the value of fromLevel.
   * 
   * @return Returns fromLevel.
   */
  public int getFromLevel() {
    return fromLevel;
  }

  /**
   * Returns the value of scaleX.
   * 
   * @return Returns scaleX.
   */
  public int getScaleX() {
    return scaleX;
  }

  /**
   * Returns the value of scaleY.
   * 
   * @return Returns scaleY.
   */
  public int getScaleY() {
    return scaleY;
  }
}
