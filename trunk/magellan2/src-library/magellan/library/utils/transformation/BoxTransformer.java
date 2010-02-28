package magellan.library.utils.transformation;

import magellan.library.CoordinateID;

/**
 * This Translator puts all coordinates into a box.
 */
public class BoxTransformer implements ReportTransformer {

  private MapTransformer.BBoxes boxes;

  /**
   * Creates a new Transformer the puts all coordinates into the boxes.
   *
   * @throws NullPointerException if boxes is <code>null</code>
   */
  public BoxTransformer(MapTransformer.BBoxes boxes) {
    if (boxes == null)
      throw new NullPointerException();
    this.boxes = boxes;
  }

  public CoordinateID transform(CoordinateID c) {
    return boxes.putInBox(c);
  }
}