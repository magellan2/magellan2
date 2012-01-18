package magellan.library.merge;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import magellan.library.GameData;
import magellan.library.IntegerID;
import magellan.library.impl.MagellanIslandImpl;
import magellan.test.GameDataBuilder;

import org.junit.Before;
import org.junit.Test;

public class IslandMergerTest {
  private static final IntegerID SOME_ID = IntegerID.create(42);
  private static final String OLDER_NAME = "Eine Insel mit 2 Bergen";
  private static final String NEWER_NAME = "Eine Insel mit 1 Berg";

  private IslandMerger merger;
  private GameData olderGD, newerGD, resultGD;

  @Before
  public void setUp() throws Exception {
    olderGD = new GameDataBuilder().createSimpleGameData(1);
    newerGD = new GameDataBuilder().createSimpleGameData(2);
    resultGD = new GameDataBuilder().createSimpleGameData(2);
    merger = new IslandMerger(olderGD, newerGD, resultGD);
  }

  @Test
  public void nameOfNewerGameShouldBeSetWhenAllNamesAreSet() {
    createIsland(olderGD, SOME_ID, OLDER_NAME);
    createIsland(newerGD, SOME_ID, NEWER_NAME);
    merge();
    assertThat(resultGD.getIsland(SOME_ID).getName(), is(NEWER_NAME));
  }

  @Test
  public void nameOfOlderGameShouldBeSetWhenNewerNameNotSet() {
    createIsland(olderGD, SOME_ID, OLDER_NAME);
    createIsland(newerGD, SOME_ID, null);
    merge();
    assertThat(resultGD.getIsland(SOME_ID).getName(), is(OLDER_NAME));
  }

  private void merge() {
    merger.firstPass();
    merger.secondPass();
  }

  private static void createIsland(GameData gameData, IntegerID id, String name) {
    MagellanIslandImpl island = new MagellanIslandImpl(id, gameData);
    island.setName(name);
    gameData.addIsland(island);
  }
}
