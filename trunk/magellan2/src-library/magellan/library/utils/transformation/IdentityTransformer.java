package magellan.library.utils.transformation;

import magellan.library.CoordinateID;

/**
 * Returns the coordinates unchanged.
 */
public class IdentityTransformer implements ReportTransformer {

  public CoordinateID transform(CoordinateID c) {
    return c;
  }

}