package magellan.library.utils.transformation;

import magellan.library.CoordinateID;

/**
 * Translates coordinates in two levels by a given translation.
 */
public class TwoLevelTransformer implements ReportTransformer {

  private CoordinateID bestTranslation;
  private CoordinateID bestAstralTranslation;

  /**
   * @param bestTranslation
   * @param bestAstralTranslation
   */
  public TwoLevelTransformer(CoordinateID bestTranslation, CoordinateID bestAstralTranslation) {
    this.bestTranslation = bestTranslation;
    this.bestAstralTranslation = bestAstralTranslation;
  }

  /**
   * If c is in the layer of bestAstralTranslation, transform it by this value, if it's in
   * bestTranslation's level, transform it by this one.
   */
  public CoordinateID transform(CoordinateID c) {
    if (c.getZ() == bestTranslation.getZ())
      return c.inverseTranslateInLayer(bestTranslation);
    if (c.getZ() == bestAstralTranslation.getZ())
      return c.inverseTranslateInLayer(bestAstralTranslation);
    return c;
  }

}