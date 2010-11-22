package magellan.library.utils.transformation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.utils.Direction;
import magellan.library.utils.MagellanFactory;

/**
 * This Translator puts all coordinates into a box.
 */
public class BoxTransformer implements ReportTransformer {

  /**
   * A set of boxes for several layers.
   */
  public static class BBoxes {
    private Map<Integer, BBox> boxes = new HashMap<Integer, BBox>(3);

    /**
     * Adjusts the x-dimensions of a box.
     */
    public BBox setBoxX(int layer, int minx, int maxx) {
      BBox result = boxes.get(layer);
      if (result == null) {
        boxes.put(layer, new BBox());
      }
      boxes.get(layer).setX(minx, maxx);
      return result;
    }

    /**
     * Adjusts the y-dimension of a box.
     */
    public BBox setBoxY(int layer, int miny, int maxy) {
      BBox result = boxes.get(layer);
      if (result == null) {
        boxes.put(layer, new BBox());
      }
      boxes.get(layer).setY(miny, maxy);
      return result;
    }

    /**
     * Sets a box for a layer.
     */
    public void setBox(int layer, BBox box) {
      boxes.put(layer, box);
    }

    /**
     * Transforms a coordinate into the box for the matching layer.
     * 
     * @see BBox#putInBox(CoordinateID)
     */
    public CoordinateID putInBox(CoordinateID c) {
      if (getBox(c.getZ()) == null)
        return c;
      return getBox(c.getZ()).putInBox(c);
    }

    /**
     * Returns the box for the specified layer or <code>null</code> if it has not been set.
     */
    public BBox getBox(int layer) {
      BBox result = boxes.get(layer);
      return result;
    }

    /**
     * Returns all layers for which boxes have been set.
     */
    public Collection<Integer> getLayers() {
      return boxes.keySet();
    }

    @Override
    public String toString() {
      return "@Boxes " + boxes.toString();
    }
  }

  /**
   * A bounding box in two dimensions.
   */
  public static class BBox {
    /** The dimensions of the box */
    public int minx = Integer.MAX_VALUE;
    /** The dimensions of the box */
    public int maxx = Integer.MIN_VALUE;
    /** The dimensions of the box */
    public int miny = Integer.MAX_VALUE;
    /** The dimensions of the box */
    public int maxy = Integer.MIN_VALUE;

    private int centery = 0, centerx = 0;

    /**
     * Changes the box's x-dimensions.
     */
    public void setX(int newmin, int newmax) {
      minx = newmin;
      maxx = newmax;
    }

    /**
     * Changes the box's y-dimensions.
     */
    public void setY(int newmin, int newmax) {
      miny = newmin;
      maxy = newmax;
    }

    /**
     * Enlarge the box to contain c if necessary.
     */
    public void adjust(CoordinateID c) {
      adjust(c.getX(), c.getY());
    }

    /**
     * Enlarge the box to contain the point (x,y) if necessary.
     */
    public void adjust(int x, int y) {
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
    public CoordinateID putInBox(CoordinateID c) {
      return putInBoxX(putInBoxY(c));
    }

    /**
     * Shifts the x-coordinate into the box's width until it is inside and returns the result.
     */
    public CoordinateID putInBoxX(CoordinateID c) {
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
    public CoordinateID putInBoxY(CoordinateID c) {
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
    public boolean above(CoordinateID newC) {
      return maxy != Integer.MIN_VALUE && newC.getY() > maxy;
    }

    /**
     * Returns true if the coordinate is under the box.
     */
    public boolean under(CoordinateID newC) {
      return miny != Integer.MAX_VALUE && newC.getY() < miny;
    }

    /**
     * Returns true if the coordinate is right of the box.
     */
    public boolean rightOf(CoordinateID newC) {
      return maxx != Integer.MIN_VALUE && newC.getX() > maxx - newC.getY() / 2 + centerx;
    }

    /**
     * Returns true if the coordinate is left of the box.
     */
    public boolean leftOf(CoordinateID newC) {
      return minx != Integer.MAX_VALUE && newC.getX() < minx - newC.getY() / 2 + centerx;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this)
        return true;
      if (obj instanceof BBox)
        return minx == ((BBox) obj).minx && maxx == ((BBox) obj).maxx && miny == ((BBox) obj).miny
            && maxy == ((BBox) obj).maxy && centerx == ((BBox) obj).centerx
            && centery == ((BBox) obj).centery;
      return false;
    }

    @Override
    public String toString() {
      return "x: " + (minx == Integer.MAX_VALUE ? "MIN" : minx) + "/"
          + (maxx == Integer.MIN_VALUE ? "MAX" : maxx) + ", y: "
          + (miny == Integer.MAX_VALUE ? "MIN" : miny) + "/"
          + (maxy == Integer.MIN_VALUE ? "MAX" : maxy);
      // (" + centerx + "," + centery + ")
    }

    @Override
    public int hashCode() {
      return ((((minx << 5 + maxx) << 5 + miny) << 5 + maxy) << 5 + centerx) << 5 + centery;
    }
  }

  private BBoxes boxes;

  /**
   * Creates a new Transformer the puts all coordinates into the boxes.
   * 
   * @throws NullPointerException if boxes is <code>null</code>
   */
  public BoxTransformer(BBoxes boxes) {
    if (boxes == null)
      throw new NullPointerException();
    for (int layer : boxes.getLayers()) {
      BBox box = boxes.getBox(layer);
      if (box.minx >= box.maxx && box.minx != Integer.MAX_VALUE && box.maxx != Integer.MIN_VALUE)
        throw new IllegalArgumentException(box.toString());
      if (box.miny >= box.maxy && box.miny != Integer.MAX_VALUE && box.maxy != Integer.MIN_VALUE)
        throw new IllegalArgumentException(box.toString());
    }
    this.boxes = boxes;
  }

  /**
   * @see magellan.library.utils.transformation.ReportTransformer#transform(magellan.library.CoordinateID)
   */
  public CoordinateID transform(CoordinateID c) {
    return boxes.putInBox(c);
  }

  /**
   * @see #transform(CoordinateID)
   */
  public void setBoxes(BBoxes boxes) {
    this.boxes = boxes;
  }

  protected BBoxes getBoxes() {
    return boxes;
  }

  /**
   * @see magellan.library.utils.transformation.ReportTransformer#getWrappers(magellan.library.Region,
   *      magellan.library.GameData)
   */
  public Collection<Region> getWrappers(Region r, GameData data) {
    BBox box = getBoxes().getBox(r.getCoordinate().getZ());

    if (box != null) {
      // if the region is at the edge of the box, shift it in every direction, put in box and shift
      // back again to get wrapper position
      CoordinateID c = box.putInBox(r.getCoordinate());
      if (c.getX() == box.minx - c.getY() / 2 + box.centerx
          || c.getX() == box.maxx - c.getY() / 2 + box.centerx || c.getY() == box.miny
          || c.getY() == box.maxy) {
        Map<CoordinateID, Region> result = new HashMap<CoordinateID, Region>();
        for (Direction d : Direction.values()) {
          CoordinateID c2 = c.add(d.toCoordinate());
          CoordinateID c3 = box.putInBox(c2);
          if (!c2.equals(c3)) {
            c3 = c3.translate(d.add(3).toCoordinate());
            result.put(c3, MagellanFactory.createWrapper(c3, r, data));
          }
        }
        return result.values();
      }
    }
    return Collections.emptyList();
  }

  /**
   * Doesn't store any translation.
   * 
   * @see magellan.library.utils.transformation.ReportTransformer#storeTranslations(GameData,
   *      GameData)
   */
  public void storeTranslations(GameData globalData, GameData addedData) {
    // no translation -- nothing to store
  }
}