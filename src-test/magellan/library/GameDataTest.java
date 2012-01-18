package magellan.library;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import magellan.library.rules.GenericRules;
import magellan.test.GameDataBuilder;

import org.junit.Test;

public class GameDataTest {

  private static final int SOME_ROUND = 1, ANOTHER_ROUND = 42;

  @Test
  public void isSameRoundWhenRoundsAreEqual() throws Exception {
    GameData gd1 = new GameDataBuilder().createSimpleGameData(SOME_ROUND);
    GameData gd2 = new GameDataBuilder().createSimpleGameData(SOME_ROUND);
    assertThat(gd1.isSameRound(gd2), is(true));
  }

  @Test
  public void isNotSameRoundWhenRoundsAreDifferent() throws Exception {
    GameData gd1 = new GameDataBuilder().createSimpleGameData(SOME_ROUND);
    GameData gd2 = new GameDataBuilder().createSimpleGameData(ANOTHER_ROUND);
    assertThat(gd1.isSameRound(gd2), is(false));
  }

  @Test
  public void dateIsInitializedWithEresseaRoundZero() {
    assertThat(createEmptyGameData().getDate().getDate(), is(0));
  }

  @Test
  public void timestampIsInitialized() {
    assertThat(createEmptyGameData().getTimestamp() > 0, is(true));
  }

  private EmptyData createEmptyGameData() {
    return new EmptyData(new GenericRules());
  }
}
