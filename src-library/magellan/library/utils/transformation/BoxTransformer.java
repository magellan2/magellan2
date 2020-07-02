package magellan.library.utils.transformation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.gamebinding.MapMetric;
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
    private MapMetric metric;

    /**
     * Creates an empty set of boxes suited for the given metric.
     */
    public BBoxes(MapMetric metric) {
      this.metric = metric;
    }

    /**
     * Adjusts the x-dimensions of a box.
     */
    public BBox setBoxX(int layer, int minx, int maxx) {
      BBox result = boxes.get(layer);
      if (result == null) {
        boxes.put(layer, metric.createBBox());
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
        boxes.put(layer, metric.createBBox());
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
  public static interface BBox {

    /**
     * Returns the maximum x value for this box. Note that this does not mean that all coordinate in
     * the box have x &lt; getMaxx.
     */
    public abstract int getMaxx();

    /**
     * Returns the minimum x value for this box. Note that this does not mean that all coordinate in
     * the box have x&gt;getMinx.
     */
    public abstract int getMinx();

    /**
     * Returns the minimum y value for this box. Note that this does not mean that all coordinate in
     * the box have y&gt;getMiny.
     */
    public abstract int getMiny();

    /**
     * Returns the maximum y value for this box. Note that this does not mean that all coordinate in
     * the box have y&lt;getMaxy.
     */
    public abstract int getMaxy();

    /**
     * Changes the box's x-dimensions.
     */
    public abstract void setX(int minx2, int maxx2);

    /**
     * Changes the box's y-dimensions.
     */
    public abstract void setY(int maxValue, int minValue);

    /**
     * Returns <code>true</code> if the coordinate is inside the box.
     */
    public abstract boolean isInside(CoordinateID c);

    /**
     * Returns <code>true</code> if the coordinate is on the border of the Box, i.e., if an adjacent
     * coordinate is not in the box.
     */
    public abstract boolean isOnBorder(CoordinateID c);

    /**
     * Shifts the coordinate by the box's dimension (x- and y- separately) until it is inside and
     * returns the result.
     */
    public abstract CoordinateID putInBox(CoordinateID c);

    /**
     * Returns true if the coordinate is left of this box (smaller x value).
     */
    public abstract boolean leftOf(CoordinateID c);

    /**
     * Returns true if the coordinate is right of this box (larger x value).
     */
    public abstract boolean rightOf(CoordinateID c);

    /**
     * Returns true if the coordinate is below this box (smaller y value).
     */
    public abstract boolean under(CoordinateID c);

    /**
     * Returns true if the coordinate is above this box (larger y value).
     */
    public abstract boolean above(CoordinateID c);

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
      if (box.getMinx() >= box.getMaxx() && box.getMinx() != Integer.MAX_VALUE
          && box.getMaxx() != Integer.MIN_VALUE)
        throw new IllegalArgumentException(box.toString());
      if (box.getMiny() >= box.getMaxy() && box.getMiny() != Integer.MAX_VALUE
          && box.getMaxy() != Integer.MIN_VALUE)
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
      MapMetric metric = data.getGameSpecificStuff().getMapMetric();
      // if the region is at the edge of the box, shift it in every direction, put in box and shift
      // back again to get wrapper position
      CoordinateID c = box.putInBox(r.getCoordinate());
      if (box.isOnBorder(c)) {
        Map<CoordinateID, Region> result = new HashMap<CoordinateID, Region>();
        for (Direction d : metric.getDirections()) {
          CoordinateID c2 = metric.translate(c, d); // c.add(d.toCoordinate());
          CoordinateID c3 = box.putInBox(c2);
          if (!c2.equals(c3)) {
            c3 = metric.translate(c3, metric.opposite(d)); // c3.translate(d.add(3).toCoordinate());
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