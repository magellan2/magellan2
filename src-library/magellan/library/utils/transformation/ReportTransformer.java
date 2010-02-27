package magellan.library.utils.transformation;

import magellan.library.CoordinateID;

/**
 * An interface for classes that transform coordinates. Possibly used by a report parser to
 * translate a report.
 */
public interface ReportTransformer {
  /**
   * Return a coordinate related to c.
   */
  public CoordinateID transform(CoordinateID c);
}