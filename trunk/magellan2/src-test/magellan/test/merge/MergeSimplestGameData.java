package magellan.test.merge;

import magellan.library.GameData;
import magellan.library.GameDataMerger;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

import org.junit.Test;

public class MergeSimplestGameData extends MagellanTestWithResources {

  // public MergeSimplestGameData(String aName) {
  // super(aName);
  // }

  @Test
  public void testMergeSameRound() throws Exception {
    GameData gd1 = new GameDataBuilder().createSimplestGameData();
    GameData gd2 = new GameDataBuilder().createSimplestGameData();

    /* GameData gd3 = */GameDataMerger.merge(gd1, gd2);
  }

  @Test
  public void testMergeDifferentRound() throws Exception {
    GameData gd1 = new GameDataBuilder().createSimplestGameData(351);
    GameData gd2 = new GameDataBuilder().createSimplestGameData(350);

    /* GameData gd3 = */GameDataMerger.merge(gd1, gd2);
  }

}
