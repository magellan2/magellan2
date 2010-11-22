package magellan.library.utils.transformation;

import java.util.Collection;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Region.Visibility;
import magellan.library.utils.MagellanFactory;

/**
 * An interface for classes that transform coordinates. Possibly used by a report parser to
 * translate a report.
 */
public interface ReportTransformer {
  /**
   * Return a coordinate related to c.
   */
  public CoordinateID transform(CoordinateID c);

  /**
   * Returns all wrapper regions for the data that "wrap to" r. I.e., if the region lies at the
   * (right or left )edge of its box, this method returns wrappers to this region that are at an
   * opposing edge and have the same ID as r.
   * 
   * @return All wrapper regions to r
   * @see GameData#wrappers()
   * @see MagellanFactory#createWrapper(CoordinateID, Region, GameData)
   * @see Visibility#WRAP
   */
  public Collection<Region> getWrappers(Region r, GameData data);

  /**
   * Store the found inter-faction translations into the new data.
   * 
   * @param globalData
   */
  public void storeTranslations(GameData globalData, GameData addedData);

}