package magellan.library;

import org.junit.Test;

import magellan.test.GameDataBuilder;

public class GameDataMergerTest {
	  @Test
	  public void mergeThreeReports() throws Exception {
		  GameData gd1 = new GameDataBuilder().createSimpleGameData(1, true);
		  GameData gd2 = new GameDataBuilder().createSimpleGameData(2, true);
		  GameData gd3 = new GameDataBuilder().createSimpleGameData(3, false);
		  GameData firstMerge = GameDataMerger.merge(gd3, gd2);
		  GameDataMerger.merge(gd1, firstMerge);
	  }
}