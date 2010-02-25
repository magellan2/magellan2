package magellan.library.utils.transformation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import magellan.library.CoordinateID;

/**
 * Stores arbitrary mappings. Uses another translator as fallback.
 */
public class MapTranslator implements ReportTranslator {

  /**
   * A set of boxes for several layers.
   */
  public static class BBoxes {
    private Map<Integer, MapTranslator.BBox> boxes = new HashMap<Integer, MapTranslator.BBox>();

    /**
     * Adjusts the x-dimensions of a box.
     */
    public BBox setBoxX(int layer, int minx, int maxx) {
      BBox result = boxes.get(layer);
      if (result == null) {
        boxes.put(layer, new BBox());
      }
      boxes.get(layer).adjustX(minx, maxx);
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
      boxes.get(layer).adjustY(miny, maxy);
      return result;
    }

    /**
     * Sets a box for a layer.
     */
    public void setBox(int layer, MapTranslator.BBox box) {
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
    public MapTranslator.BBox getBox(int layer) {
      MapTranslator.BBox result = boxes.get(layer);
      return result;
    }

    /**
     * Returns all layers for which boxes have been set.
     */
    public Collection<Integer> getLayers() {
      return boxes.keySet();
    }

  }

  /**
   * A bounding box in two dimensions.
   */
  public static class BBox {
    /** The dimensions of the box */
    public int minx = Integer.MAX_VALUE, maxx = Integer.MIN_VALUE, miny = Integer.MAX_VALUE,
        maxy = Integer.MIN_VALUE;

    /**
     * Changes the box's x-dimensions.
     */
    public void adjustX(int newmin, int newmax) {
      minx = newmin;
      maxx = newmax;
    }

    /**
     * Changes the box's y-dimensions.
     */
    public void adjustY(int newmin, int newmax) {
      miny = newmin;
      maxy = newmax;
    }

    /**
     * Shifts the coordinate by the boxes dimension (x- and y- separately) until it is inside and
     * returns the result.
     */
    public CoordinateID putInBox(CoordinateID c) {
      CoordinateID newC = c;
      if (minx != Integer.MAX_VALUE && maxx != Integer.MIN_VALUE) {
        while (newC.getX() < minx) {
          newC = CoordinateID.create(newC.getX() + maxx - minx + 1, newC.getY(), newC.getZ());
        }
        while (newC.getX() > maxx) {
          newC = CoordinateID.create(newC.getX() - maxx + minx - 1, newC.getY(), newC.getZ());
        }
      }
      if (miny != Integer.MAX_VALUE && maxy != Integer.MIN_VALUE) {
        while (newC.getY() < miny) {
          newC = CoordinateID.create(newC.getX(), newC.getY() + maxy - miny + 1, newC.getZ());
        }
        while (newC.getY() > maxy) {
          newC = CoordinateID.create(newC.getX(), newC.getY() - maxy + miny - 1, newC.getZ());
        }
      }
      return newC;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this)
        return true;
      if (obj instanceof MapTranslator.BBox)
        return minx == ((MapTranslator.BBox) obj).minx && maxx == ((MapTranslator.BBox) obj).maxx
            && miny == ((MapTranslator.BBox) obj).miny && maxy == ((MapTranslator.BBox) obj).maxy;
      return false;
    }

    @Override
    public String toString() {
      return "(" + minx + "," + miny + ")(" + maxx + "," + maxy + ")";
    }

    @Override
    public int hashCode() {
      return (((minx << 5 + maxx) << 5 + miny) << 5 + maxy);
    }
  }

  Map<CoordinateID, CoordinateID> translationMap = new HashMap<CoordinateID, CoordinateID>();
  private ReportTranslator fallBack;
  private MapTranslator.BBoxes boxes = new BBoxes();

  /**
   * Creates a translator without fallback.
   */
  public MapTranslator() {
    this(null);
  }

  /**
   * Uses the specified translator when no mapping is stored.
   */
  public MapTranslator(ReportTranslator fallBack) {
    this.fallBack = fallBack;
  }

  /**
   * @see #transform(CoordinateID)
   */
  public void setBoxes(MapTranslator.BBoxes boxes) {
    this.boxes = boxes;
  }

  /**
   * Transforms a coordinate according to the stored translations. If no translation is found, the
   * fall back is used <i>and</i> the result is shifted into the box, if defined.
   * 
   * @return the transformed coordinate or <code>null</code> if no mapping and no fallback is
   *         defined.
   * @see magellan.library.utils.transformation.ReportTranslator#transform(magellan.library.CoordinateID)
   */
  public CoordinateID transform(CoordinateID c) {
    CoordinateID newC = translationMap.get(c);
    if (newC == null) {
      if (fallBack == null)
        return null;
      else {
        newC = fallBack.transform(c);
      }
      // put coordinate into bounding box
      newC = boxes.putInBox(newC);
    }
    return newC;
  }

  /**
   * Stores a new mapping. <code>rNew.getCoordinate()</code> will be mapped to
   * <code>rOld.getCoordinate()</code>.
   */
  public void addMapping(CoordinateID in, CoordinateID out) {
    translationMap.put(in, out);
    // inverseMap.put(out, in);
  }

}