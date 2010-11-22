package magellan.library.utils.transformation;

import java.util.Collection;
import java.util.Collections;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;

/**
 * Returns the coordinates unchanged.
 */
public class IdentityTransformer implements ReportTransformer {

  public CoordinateID transform(CoordinateID c) {
    return c;
  }

  /**
   * Never returns a wrapper.
   * 
   * @see magellan.library.utils.transformation.ReportTransformer#getWrappers(magellan.library.Region,
   *      magellan.library.GameData)
   */
  public Collection<Region> getWrappers(Region r, GameData data) {
    return Collections.emptyList();
  }

  /**
   * Doesn't store any translation.
   * 
   * @see magellan.library.utils.transformation.ReportTransformer#storeTranslations(GameData,
   *      GameData)
   */
  public void storeTranslations(GameData globalData, GameData addedData) {
    // no translation -- nothing to store
  }

}