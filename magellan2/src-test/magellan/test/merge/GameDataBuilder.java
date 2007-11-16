package magellan.test.merge;

import java.util.Collections;
import java.util.Locale;

import magellan.library.Border;
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.IntegerID;
import magellan.library.Island;
import magellan.library.Message;
import magellan.library.Region;
import magellan.library.Skill;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.io.GameDataReader;
import magellan.library.rules.EresseaDate;
import magellan.library.rules.SkillType;
import magellan.library.utils.MagellanFactory;


public class GameDataBuilder {

	private final int BASE_ROUND = 360;

	public GameData createSimplestGameData() throws Exception {
		return createSimplestGameData(BASE_ROUND);
	}
	
	public GameData createSimplestGameData(int round) throws Exception {
		return createSimplestGameData(round,true);
	}

	public GameData createSimplestGameData(int round, boolean addUnit) throws Exception {
		return createSimplestGameData(round,addUnit,true);
	}

	private GameData createSimplestGameData(int round, boolean addUnit, boolean postProcess) throws Exception {
		GameData data = new GameDataReader(null).createGameData("Eressea");
		
		data.base=36;
		// this is sadly needed
		//IDBaseConverter.setBase(data.base);
		
		data.noSkillPoints=true;
		
		data.setLocale(Locale.GERMAN);
		
		EresseaDate ed = new EresseaDate(round);
		ed.setEpoch(2);
		data.setDate(ed);
		
		//data.setCurTempID
		//data.mailTo
		//data.mailSubject

		
		// data.addFaction
		Faction faction = addFaction(data,"867718","Faction_867718","Meermenschen",1);

		Island island = addIsland(data,"1","Island_1");

		Region region_0_0 = addRegion(data,"0 0", "Region_0_0","Gletscher",1);
		region_0_0.setIsland(island);

		if(addUnit) {
			Unit unit = addUnit(data, "1", "Unit_1", faction, region_0_0);
		}

		if(postProcess) {
			data.postProcess();
		}
		return data;
	}

	public GameData createSimpleGameData() throws Exception {
		return createSimpleGameData(BASE_ROUND);
	}
	
	public GameData createSimpleGameData(int round) throws Exception {
		return createSimpleGameData(round, true);
	}
	
	public GameData createSimpleGameData(int round, boolean addUnit) throws Exception {
		GameData data = createSimplestGameData(round, addUnit, false);

		if(data.units().size() > 0) {
			Unit unit = (Unit) data.units().values().iterator().next();

			Skill skill1 = addSkill(unit, "Hiebwaffen", 4, 3, true); // Hiebwaffen 4 (+3)
			Skill skill2 = addSkill(unit, "Segeln", 4, 3,false); // Segeln 4
			Skill skill3 = addSkill(unit, "Magie", -1, -3,true); // Magie - (-3)
			Skill skill4 = addSkill(unit, "Steinbau", -1, -3,false); // Steinbau - 
		}
		
		data.postProcess();
		return data;
	}

	public Faction addFaction(GameData data, String number, String name, String race, int sortIndex) {
		EntityID id = EntityID.createEntityID(number, 10);

		Faction faction = MagellanFactory.createFaction(id, data);
		data.addFaction(faction);

		faction.setName(name);

		faction.setPassword(name);

		faction.setType(data.rules.getRace(StringID.create(race),true));

		faction.setSortIndex(sortIndex);
		
		return faction;
	}

	public Island addIsland(GameData data, String number, String name) {
		IntegerID id = IntegerID.create(number);

		Island island = MagellanFactory.createIsland(id, data);
		data.addIsland(island);
		
		island.setName(name);

		return island;
	}

	public Region addRegion(GameData data, String number, String name, String type, int sortIndex) {
		CoordinateID c = CoordinateID.parse(number, " ");
		
		Region region = MagellanFactory.createRegion(c, data);
		data.addRegion(region);
		
		region.setName(name);
		
		region.setType(data.rules.getRegionType(StringID.create(type), true));
		
		region.setSortIndex(sortIndex);
		return region;
	}
	
	public Unit addUnit(GameData data, String name, Region region) {
		String number = "g"+(data.units().size()+1);
		Faction faction = (Faction) data.factions().values().iterator().next();
		return addUnit(data, number, name, faction, region);
	}
	
	public Unit addUnit(GameData data, String number, String name,  Faction faction, Region region) {
		UnitID id = UnitID.createUnitID(number,data.base);

		Unit unit = MagellanFactory.createUnit(id);
		data.addUnit(unit);
		
		unit.setName(name);

		unit.setFaction(faction);

		unit.setRace(faction.getRace());
		unit.setRealRace(faction.getRace());
		
		unit.setRegion(region);
		
		unit.setOrders(Collections.singleton(""));

		return unit;
	}
	
	public Skill addLostSkill(Unit unit, String name, int level) {
		return addSkill(unit, name, -1, level,true);
	}

	public Skill addSkill(Unit unit, String name, int level) {
		return addSkill(unit, name, level, level, false);
	}
						 
	public Skill addChangedSkill(Unit unit, String name, int level, int fromLevel) {
		return addSkill(unit, name, level, fromLevel, true);
	}

	protected Skill addSkill(Unit unit, String name, int level, int change, boolean changed) {
		
		SkillType skt = unit.getRegion().getData().rules.getSkillType(StringID.create(name), true);
		int raceBonus = unit.getRealRace().getSkillBonus(skt);
		int points = Skill.getPointsAtLevel(level-raceBonus);

		Skill skill = new Skill(skt, points, level, unit.getPersons(), unit.getRegion().getData().noSkillPoints);

		skill.setChangeLevel(change);

		skill.setLevelChanged(changed);

		unit.addSkill(skill);

		return skill;
	}

	public static Message createMessage(String text) {
		// EINHEITSBOTSCHAFTEN
		// "Eine Botschaft von Kr?uterlager (ax1a): 'MessMach99?99?99!Wundsalbe!xxxx'"
		return MagellanFactory.createMessage(text);
	}

	public Border addRoad(Region region, int id, int direction, int buildRatio) {
		// GRENZE 1
		// "Straﬂe";typ
		// 0;richtung
		// 100;prozent
		Border road = MagellanFactory.createBorder(IntegerID.create(id));
		
		road.setDirection(direction);
		road.setBuildRatio(buildRatio);
		road.setType("Straﬂe");
		

		region.addBorder(road);

		return road;
	}


}
