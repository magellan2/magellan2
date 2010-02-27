package magellan.library.utils.transformation;

import magellan.library.CoordinateID;

/**
 * This Translator puts all coordinates into a box.
 */
public class BoxTransformer implements ReportTransformer {

  private MapTransformer.BBoxes boxes;

  public BoxTransformer(MapTransformer.BBoxes boxes) {
    this.boxes = boxes;
  }

  public CoordinateID transform(CoordinateID c) {
    return boxes.putInBox(c);
  }
}