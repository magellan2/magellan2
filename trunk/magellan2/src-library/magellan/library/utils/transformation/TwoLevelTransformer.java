package magellan.library.utils.transformation;

import java.util.Collections;
import java.util.List;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.gamebinding.EresseaSpecificStuff;

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

  /**
   * Never returns a wrapper.
   * 
   * @see magellan.library.utils.transformation.ReportTransformer#getWrappers(magellan.library.Region,
   *      magellan.library.GameData)
   */
  public List<Region> getWrappers(Region r, GameData data) {
    return Collections.emptyList();
  }

  /**
   * @see magellan.library.utils.transformation.ReportTransformer#storeTranslations(magellan.library.GameData,
   *      magellan.library.GameData)
   */
  public void storeTranslations(GameData globalData, GameData addedData) {
    boolean hasAstral = false;
    boolean hasNormal = false;
    for (Region r : globalData.getRegions()) {
      if (r.getCoordinate().getZ() == 0) {
        hasNormal = true;
        if (hasAstral) {
          break;
        }
      } else if (r.getCoordinate().getZ() == 0) {
        hasAstral = true;
        if (hasNormal) {
          break;
        }
      }
    }

    if (hasNormal) {
      TransformerFinder.storeTranslations(0, bestTranslation, globalData, addedData);
    }
    if (hasAstral) {
      TransformerFinder.storeTranslations(EresseaSpecificStuff.ASTRAL_LAYER, bestAstralTranslation,
          globalData, addedData);
    }
  }

}