package magellan.library.merge;

import static magellan.library.utils.MagellanFactory.createIsland;
import magellan.library.GameData;
import magellan.library.IntegerID;
import magellan.library.Island;

public class IslandMerger extends EntityMerger<Island, IntegerID> {

  public IslandMerger(GameData olderGD, GameData newerGD, GameData resultGD) {
    super(olderGD, newerGD, resultGD);
  }

  public void firstPass() {
    mergeEntityViews(olderGD.islandView(), resultGD.islandView());
  }

  public void secondPass() {
    mergeEntityViews(newerGD.islandView(), resultGD.islandView());
  }

  @Override
  protected Island createEntity(IntegerID id, GameData gameData) {
    return createIsland(id, gameData);
  }

  @Override
  protected void mergeEntity(Island source, Island target, PropertyMerger<Island> propertyMerger) {
    propertyMerger.mergeObjects("name", "description");

    target.invalidateRegions();

    if (source.getAttributeSize() > 0) {
      for (final String key : source.getAttributeKeys()) {
        if (!target.containsAttribute(key)) {
          target.addAttribute(key, source.getAttribute(key));
        }
      }
    }
  }
}
