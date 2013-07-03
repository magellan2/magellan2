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
import magellan.library.utils.transformation.BoxTransformer.BBox;

/**
 * Map metric for the Eressea coordinate system.
 */
public class EresseaMapMetric implements MapMetric {

  /** Direction northwest */
  public static final Direction NW = new Direction(0, EresseaConstants.OC_NW, CoordinateID.create(
      -1, 1), "pfeil0");
  /** Direction northeast */
  public static final Direction NE = new Direction(1, EresseaConstants.OC_NE, CoordinateID.create(
      0, 1), "pfeil1");
  /** Direction east */
  public static final Direction E = new Direction(2, EresseaConstants.OC_E, CoordinateID.create(1,
      0), "pfeil2");
  /** Direction southeast */
  public static final Direction SE = new Direction(3, EresseaConstants.OC_SE, CoordinateID.create(
      1, -1), "pfeil3");
  /** Direction southwest */
  public static final Direction SW = new Direction(4, EresseaConstants.OC_SW, CoordinateID.create(
      0, -1), "pfeil4");
  /** Direction west */
  public static final Direction W = new Direction(5, EresseaConstants.OC_W, CoordinateID.create(-1,
      0), "pfeil5");

  @SuppressWarnings("unused")
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

  /**
   * 
   */
  public EresseaMapMetric(Rules rules) {
    this.rules = rules;
  }

  public Direction getDirection(CoordinateID from, CoordinateID to) {
    if (to.getZ() != from.getZ())
      return Direction.INVALID;
    return toDirection(to.getX() - from.getX(), to.getY() - from.getY());

  }

  /**
   * Converts coordinates to a direction.
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

  public BBox createBBox() {
    return new EBBox(-1, 2, 0, 1);
  }

  /**
   * A bounding box with "trapezoid correction".
   */
  public final static class EBBox implements BBox {

    /** The dimensions of the box */
    protected int minx = Integer.MAX_VALUE;
    /** The dimensions of the box */
    protected int maxx = Integer.MIN_VALUE;
    /** The dimensions of the box */
    protected int miny = Integer.MAX_VALUE;
    /** The dimensions of the box */
    protected int maxy = Integer.MIN_VALUE;

    /**
     * Returns the value of minx.
     * 
     * @return Returns minx.
     */
    public final int getMinx() {
      return minx;
    }

    /**
     * Returns the value of maxx.
     * 
     * @return Returns maxx.
     */
    public final int getMaxx() {
      return maxx;
    }

    /**
     * Returns the value of miny.
     * 
     * @return Returns miny.
     */
    public final int getMiny() {
      return miny;
    }

    /**
     * Returns the value of maxy.
     * 
     * @return Returns maxy.
     */
    public final int getMaxy() {
      return maxy;
    }

    /**
     * A coordinate is in this box if (x+fxy*y/dxy, y+fyx*x/dxy) is in (minx, miny) / (maxx, maxy).
     * This results in nicer "rectangular" boxes instead of the "trapezoid" boxes if we just
     * compared (x,y) with (minx, miny) / (maxx, maxy) in a hexagonal map.
     */
    public EBBox(int fxy, int dxy, int fyx, int dyx) {
      this.fxy = fxy;
      this.dxy = dxy;
      this.fyx = fyx;
      this.dyx = dyx;
    }

    // private int centery = 0, centerx = 0;

    private int dxy = 2;
    private int fxy = -1;
    private int dyx = 2;
    private int fyx = 0;

    /**
     * Changes the box's x-dimensions.
     */
    public final void setX(int newmin, int newmax) {
      minx = newmin;
      maxx = newmax;
    }

    /**
     * Changes the box's y-dimensions.
     */
    public final void setY(int newmin, int newmax) {
      miny = newmin;
      maxy = newmax;
    }

    /**
     * Enlarge the box to contain c if necessary.
     */
    public final void adjust(CoordinateID c) {
      adjust(c.getX(), c.getY());
    }

    /**
     * Enlarge the box to contain the point (x,y) if necessary.
     */
    public final void adjust(int x, int y) {
      if (x > maxx) {
        maxx = x;
      }
      if (y > maxy) {
        maxy = y;
      }
      if (x < minx) {
        minx = x;
      }
      if (y < miny) {
        miny = x;
      }
    }

    /**
     * Shifts the coordinate by the box's dimension (x- and y- separately) until it is inside and
     * returns the result.
     */
    public final CoordinateID putInBox(CoordinateID c) {
      return putInBoxX(putInBoxY(putInBoxX(c)));
    }

    /**
     * Shifts the x-coordinate into the box's width until it is inside and returns the result.
     */
    public final CoordinateID putInBoxX(CoordinateID c) {
      if (minx != Integer.MAX_VALUE && maxx != Integer.MIN_VALUE) {
        while (leftOf(c)) {
          c = CoordinateID.create(c.getX() + maxx - minx + 1, c.getY(), c.getZ());
        }
        while (rightOf(c)) {
          c = CoordinateID.create(c.getX() - maxx + minx - 1, c.getY(), c.getZ());
        }
      }
      return c;
    }

    /**
     * Shifts the y-coordinate by the box's height until it is inside and returns the result.
     */
    public final CoordinateID putInBoxY(CoordinateID c) {
      if (miny != Integer.MAX_VALUE && maxy != Integer.MIN_VALUE) {
        while (under(c)) {
          c = CoordinateID.create(c.getX(), c.getY() + maxy - miny + 1, c.getZ());
        }
        while (above(c)) {
          c = CoordinateID.create(c.getX(), c.getY() - maxy + miny - 1, c.getZ());
        }
      }
      return c;
    }

    /**
     * Returns true if the coordinate is above the box.
     */
    public final boolean above(CoordinateID newC) {
      return maxy != Integer.MIN_VALUE && newC.getY() > maxy + fyx * newC.getX() / dyx;
    }

    /**
     * Returns true if the coordinate is under the box.
     */
    public final boolean under(CoordinateID newC) {
      return miny != Integer.MAX_VALUE && newC.getY() < miny + fyx * newC.getX() / dyx;
    }

    /**
     * Returns true if the coordinate is right of the box.
     */
    public final boolean rightOf(CoordinateID newC) {
      return maxx != Integer.MIN_VALUE && newC.getX() > maxx + fxy * newC.getY() / dxy;
    }

    /**
     * Returns true if the coordinate is left of the box.
     */
    public final boolean leftOf(CoordinateID newC) {
      return minx != Integer.MAX_VALUE && newC.getX() < minx + fxy * newC.getY() / dxy;
    }

    @Override
    public final boolean equals(Object obj) {
      if (obj == this)
        return true;
      if (obj instanceof EBBox) {
        EBBox box = (EBBox) obj;
        return minx == box.minx && maxx == box.maxx && miny == box.miny && maxy == box.maxy;
        // && centerx == ((BBox) obj).centerx && centery == ((BBox) obj).centery
      } else if (obj instanceof BBox) {
        BBox box = (BBox) obj;
        return minx == box.getMinx() && maxx == box.getMaxx() && miny == box.getMiny()
            && maxy == box.getMaxy();
        // && centerx == ((BBox) obj).centerx && centery == ((BBox) obj).centery
      }
      return false;
    }

    @Override
    public final String toString() {
      return "x: " + (minx == Integer.MAX_VALUE ? "MIN" : minx) + "/"
          + (maxx == Integer.MIN_VALUE ? "MAX" : maxx) + ", y: "
          + (miny == Integer.MAX_VALUE ? "MIN" : miny) + "/"
          + (maxy == Integer.MIN_VALUE ? "MAX" : maxy);
      // (" + centerx + "," + centery + ")
    }

    @Override
    public final int hashCode() {
      return (((minx << 5 + maxx) << 5 + miny) << 5 + maxy);// << 5 + centerx) << 5 + centery;
    }

    public final boolean isOnBorder(CoordinateID c) {
      return (c.getX() == minx + fxy * c.getY() / dxy // + box.centerx
          || c.getX() == maxx + fxy * c.getY() / dxy // + box.centerx
          || c.getY() == miny + fyx * c.getX() / dyx // ..
      || c.getY() == maxy + fyx * c.getX() / dyx // ...
      );
    }

    public boolean isInside(CoordinateID newC) {
      return !((maxy != Integer.MIN_VALUE && newC.getY() > maxy + fyx * newC.getX() / dyx)
          || (miny != Integer.MAX_VALUE && newC.getY() < miny + fyx * newC.getX() / dyx)
          || (maxx != Integer.MIN_VALUE && newC.getX() > maxx + fxy * newC.getY() / dxy) || (minx != Integer.MAX_VALUE && newC
          .getX() < minx + fxy * newC.getY() / dxy));

    }
  }

}
