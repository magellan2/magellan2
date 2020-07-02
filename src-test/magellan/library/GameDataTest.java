package magellan.library;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import magellan.library.rules.GenericRules;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

/**
 * 
 */
public class GameDataTest extends MagellanTestWithResources {

  private static final int SOME_ROUND = 1, ANOTHER_ROUND = 42;

  @Test
  public void isSameRoundWhenRoundsAreEqual() throws Exception {
    GameData gd1 = new GameDataBuilder().createSimpleGameData(SOME_ROUND);
    GameData gd2 = new GameDataBuilder().createSimpleGameData(SOME_ROUND);
    assertTrue(gd1.isSameRound(gd2));
  }

  @Test
  public void isNotSameRoundWhenRoundsAreDifferent() throws Exception {
    GameData gd1 = new GameDataBuilder().createSimpleGameData(SOME_ROUND);
    GameData gd2 = new GameDataBuilder().createSimpleGameData(ANOTHER_ROUND);
    assertFalse(gd1.isSameRound(gd2));
  }

  @Test
  public void dateIsInitializedWithEresseaRoundZero() {
    assertEquals(0, createEmptyGameData().getDate().getDate());
  }

  @Test
  public void timestampIsInitialized() {
    assertTrue(createEmptyGameData().getTimestamp() > 0);
  }

  private EmptyData createEmptyGameData() {
    return new EmptyData(new GenericRules());
  }
}
