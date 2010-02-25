package magellan.library.utils.transformation;

import magellan.library.CoordinateID;

/**
 * Returns the coordinates unchanged.
 */
public class IdentityTranslator implements ReportTranslator {

  public CoordinateID transform(CoordinateID c) {
    return c;
  }

}