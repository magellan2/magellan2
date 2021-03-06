package magellan.library.merge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;

import org.junit.Test;

import magellan.library.GameData;
import magellan.library.GameDataMerger;
import magellan.library.Message;
import magellan.library.Unit;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

/**
 * 
 */
public class MergeWithUnitMessages extends MagellanTestWithResources {

  // public MergeWithUnitMessages(String aName) {
  // super(aName);
  // }

  /** bugzilla bug #8?? */
  @Test
  public void testMergeDifferentRound() throws Exception {
    GameDataBuilder builder = new GameDataBuilder();

    GameData gd1 = builder.createSimpleGameData(350);
    GameData gd2 = builder.createSimpleGameData(351);

    Unit u1 = gd1.getUnits().iterator().next();
    u1.setUnitMessages(new LinkedList<Message>());
    u1.getUnitMessages().add(GameDataBuilder.createMessage("TEST_m1"));

    Unit u2 = gd2.getUnits().iterator().next();
    u2.setUnitMessages(new LinkedList<Message>());
    u2.getUnitMessages().add(GameDataBuilder.createMessage("TEST_m2"));

    GameData gd4 = GameDataMerger.merge(gd1, gd2);
    // // WriteGameData.writeCR(gdMerged, gdMerged.getDate().getDate()+"_gd.cr");

    Unit u4 = gd4.getUnit(u1.getID());

    assertTrue(u4 != null);
    assertTrue(u4.getUnitMessages() != null);
    assertEquals(1, u4.getUnitMessages().size());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testMergeSameRound() throws Exception {
    GameDataBuilder builder = new GameDataBuilder();

    GameData gd1 = builder.createSimpleGameData(351);
    GameData gd2 = builder.createSimpleGameData(351);

    Unit u1 = gd1.getUnits().iterator().next();
    u1.setUnitMessages(new LinkedList<Message>());
    u1.getUnitMessages().add(GameDataBuilder.createMessage("TEST_m1"));

    Unit u2 = gd2.getUnits().iterator().next();
    u2.setUnitMessages(new LinkedList<Message>());
    u2.getUnitMessages().add(GameDataBuilder.createMessage("TEST_m2"));

    GameData gd4 = GameDataMerger.merge(gd1, gd2);
    // // WriteGameData.writeCR(gdMerged, gdMerged.getDate().getDate()+"_gd.cr");

    Unit u4 = gd4.getUnit(u1.getID());

    assertTrue(u4 != null);
    assertTrue(u4.getUnitMessages() != null);
    assertEquals(2, u4.getUnitMessages().size());
  }

}
