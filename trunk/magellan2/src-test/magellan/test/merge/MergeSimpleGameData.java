package magellan.test.merge;

import junit.framework.TestCase;
import magellan.library.GameData;
import magellan.library.Skill;
import magellan.library.Unit;

public class MergeSimpleGameData extends TestCase {

	public MergeSimpleGameData(String aName) {
		super(aName);
	}

	/*
	public void testMergeSameRound() throws Exception {
		GameData gd1 = new GameDataBuilder().createSimpleGameData();
		GameData gd2 = new GameDataBuilder().createSimpleGameData();
		
		GameData gd3 = GameData.merge(gd1, gd2);
	}

	public void testMergeDifferentRound() throws Exception {
		GameData gd1 = new GameDataBuilder().createSimpleGameData(351);
		GameData gd2 = new GameDataBuilder().createSimpleGameData(350);
		
		GameData gd3 = GameData.merge(gd1, gd2);
	}

	*/

	public void testMergeDifferentRounds() throws Exception {
		GameDataBuilder builder = new GameDataBuilder();
		GameData gd1 = builder.createSimpleGameData(350);
		GameData gd2 = builder.createSimpleGameData(351);
		GameData gd3 = builder.createSimpleGameData(352);

		String ausdauer = "Ausdauer";

		Unit unit1 = (Unit) gd1.units().values().iterator().next();
		Skill skill1 = builder.addSkill(unit1, ausdauer, 3);
		// System.out.println("Skill1 :"+skill1+" "+skill1.getChangeLevel());

		GameData gd4 = GameData.merge(gd1, gd2);
		//WriteGameData.writeCR(gd4, gd4.getDate().getDate()+"_MergeSimpleGameData.cr");

		GameData gd5 = GameData.merge(gd3,gd4);
		//WriteGameData.writeCR(gd5, gd5.getDate().getDate()+"_MergeSimpleGameData.cr");

		Unit  unit4  = gd4.getUnit(unit1.getID());
		Skill skill4 = unit4.getSkill(gd4.rules.getSkillType(ausdauer));
		assertNotNull(skill4);
		// System.out.println("Skill4 :"+skill4+" "+skill4.getChangeLevel());
		assertTrue(skill4.isLostSkill());

		Unit  unit5  = gd5.getUnit(unit1.getID());
		Skill skill5 = unit5.getSkill(gd5.rules.getSkillType(ausdauer));
		// System.out.println("Skill5 :"+skill5+" "+skill5.getChangeLevel());
		assertNull(skill5);

	}
}
