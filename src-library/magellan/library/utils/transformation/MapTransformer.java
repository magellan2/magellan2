package magellan.library.utils.transformation;

import java.util.HashMap;
import java.util.Map;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.gamebinding.MapMetric;

/**
 * Stores arbitrary mappings. Uses another translator as fallback.
 */
public class MapTransformer extends BoxTransformer {

  private MapMetric metric;
  Map<CoordinateID, CoordinateID> translationMap = new HashMap<CoordinateID, CoordinateID>();
  private ReportTransformer fallBack;

  /**
   * Creates a translator without fallback.
   */
  public MapTransformer(MapMetric metric) {
    this(null, metric);
  }

  /**
   * Uses the specified translator when no mapping is stored.
   * 
   * @param metric
   */
  public MapTransformer(ReportTransformer fallBack, MapMetric metric) {
    super(new BBoxes(metric));
    this.fallBack = fallBack;
    this.metric = metric;
  }

  /**
   * Transforms a coordinate according to the stored translations. If no translation is found, the
   * fall back is used <i>and</i> the result is shifted into the box, if defined.
   * 
   * @return the transformed coordinate or <code>null</code> if no mapping and no fallback is
   *         defined.
   * @see magellan.library.utils.transformation.ReportTransformer#transform(magellan.library.CoordinateID)
   */
  @Override
  public CoordinateID transform(CoordinateID c) {
    CoordinateID newC = translationMap.get(c);
    if (newC == null) {
      if (fallBack == null)
        return null;
      else {
        newC = fallBack.transform(c);
      }
      // put coordinate into bounding box
      newC = getBoxes().putInBox(newC);
    }
    // return translation from map, which must already be in box
    return newC;
  }

  /**
   * Stores a new mapping. <code>rNew.getCoordinate()</code> will be mapped to
   * <code>rOld.getCoordinate()</code>.
   */
  public void addMapping(CoordinateID in, CoordinateID out) {
    translationMap.put(in, out);
    // inverseMap.put(out, in);
  }

  /**
   * Uses fall-back transformer to store translations.
   * 
   * @see magellan.library.utils.transformation.ReportTransformer#storeTranslations(GameData,
   *      GameData)
   */
  @Override
  public void storeTranslations(GameData globalData, GameData addedData) {
    fallBack.storeTranslations(globalData, addedData);
  }
}