package magellan.test.merge;

import junit.framework.TestCase;
import magellan.library.GameData;
import magellan.test.GameDataBuilder;

public class MergeSimplestGameData extends TestCase {

	public MergeSimplestGameData(String aName) {
		super(aName);
	}

	public void testMergeSameRound() throws Exception {
		GameData gd1 = new GameDataBuilder().createSimplestGameData();
		GameData gd2 = new GameDataBuilder().createSimplestGameData();
		
		/*GameData gd3 = */GameData.merge(gd1, gd2);
	}

	public void testMergeDifferentRound() throws Exception {
		GameData gd1 = new GameDataBuilder().createSimplestGameData(351);
		GameData gd2 = new GameDataBuilder().createSimplestGameData(350);
		
		/*GameData gd3 = */GameData.merge(gd1, gd2);
	}

}
