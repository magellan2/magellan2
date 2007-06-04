package magellan.test.merge;

import java.util.LinkedList;

import junit.framework.TestCase;
import magellan.library.GameData;
import magellan.library.Message;
import magellan.library.Unit;

public class MergeWithUnitMessages extends TestCase {

	public MergeWithUnitMessages(String aName) {
		super(aName);
	}

	// bugzilla bug #8??
	public void testMergeDifferentRound() throws Exception {
		GameDataBuilder builder = new GameDataBuilder();

		GameData gd1 = builder.createSimpleGameData(350);
		GameData gd2 = builder.createSimpleGameData(351);

		Unit u1 = gd1.units().values().iterator().next();
		u1.setUnitMessages(new LinkedList<Message>());
		u1.getUnitMessages().add(GameDataBuilder.createMessage("TEST_m1"));

		Unit u2 = (Unit) gd2.units().values().iterator().next();
		u2.setUnitMessages(new LinkedList<Message>());
		u2.getUnitMessages().add(GameDataBuilder.createMessage("TEST_m2"));

		GameData gd4 = GameData.merge(gd1, gd2);
//		// WriteGameData.writeCR(gdMerged, gdMerged.getDate().getDate()+"_gd.cr");

		Unit u4 = gd4.getUnit(u1.getID());
		
		assertTrue(u4 != null);
		assertTrue(u4.getUnitMessages() != null);
		assertEquals(1, u4.getUnitMessages().size());
	}

	public void testMergeSameRound() throws Exception {
		GameDataBuilder builder = new GameDataBuilder();

		GameData gd1 = builder.createSimpleGameData(351);
		GameData gd2 = builder.createSimpleGameData(351);

		Unit u1 = (Unit) gd1.units().values().iterator().next();
		u1.setUnitMessages(new LinkedList<Message>());
		u1.getUnitMessages().add(GameDataBuilder.createMessage("TEST_m1"));

		Unit u2 = (Unit) gd2.units().values().iterator().next();
		u2.setUnitMessages(new LinkedList<Message>());
		u2.getUnitMessages().add(GameDataBuilder.createMessage("TEST_m2"));

		GameData gd4 = GameData.merge(gd1, gd2);
//		// WriteGameData.writeCR(gdMerged, gdMerged.getDate().getDate()+"_gd.cr");

		Unit u4 = gd4.getUnit(u1.getID());
		
		assertTrue(u4 != null);
		assertTrue(u4.getUnitMessages() != null);
		assertEquals(2, u4.getUnitMessages().size());
	}


}
