package magellan.library.utils.transformation;

import magellan.library.CoordinateID;

/**
 * This Translator puts all coordinates into a box.
 */
public class BoxTranslator implements ReportTranslator {

  private MapTranslator.BBoxes boxes;

  public BoxTranslator(MapTranslator.BBoxes boxes) {
    this.boxes = boxes;
  }

  public CoordinateID transform(CoordinateID c) {
    return boxes.putInBox(c);
  }
}