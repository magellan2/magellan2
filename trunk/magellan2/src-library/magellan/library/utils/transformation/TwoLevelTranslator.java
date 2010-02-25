package magellan.library.utils.transformation;

import magellan.library.CoordinateID;

/**
 * Translates coordinates in two levels by a given translation.
 */
public class TwoLevelTranslator implements ReportTranslator {

  private CoordinateID bestTranslation;
  private CoordinateID bestAstralTranslation;
  private CoordinateID translate2;
  private CoordinateID translate1;

  /**
   * @param bestTranslation
   * @param bestAstralTranslation
   */
  public TwoLevelTranslator(CoordinateID bestTranslation, CoordinateID bestAstralTranslation) {
    this.bestTranslation = bestTranslation;
    this.bestAstralTranslation = bestAstralTranslation;
    translate1 = CoordinateID.create(bestTranslation.getX(), bestTranslation.getY());
    translate2 = CoordinateID.create(bestAstralTranslation.getX(), bestAstralTranslation.getY());
  }

  /**
   * If c is in the layer of bestAstralTranslation, transform it by this value, if it's in
   * bestTranslation's level, transform it by this one.
   */
  public CoordinateID transform(CoordinateID c) {
    if (c.getZ() == bestTranslation.getZ())
      return c.subtract(translate1);
    if (c.getZ() == bestAstralTranslation.getZ())
      return c.subtract(translate2);
    return c;
  }

}