// class magellan.library.gamebinding.EresseaGameSpecificRulesTest
// created on Jan 7, 2020
//
// Copyright 2003-2020 by magellan project team
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
package magellan.library.gamebinding;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Skill;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.test.GameDataBuilder;

/**
 * @author stm
 * @version 1.0, Jan 7, 2020
 */
public class EresseaGameSpecificRulesTest {

  private GameDataBuilder builder;
  private GameData data;
  private Region region;
  private Unit unit;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    builder = new GameDataBuilder();
    data = builder.createSimpleGameData();

    region = data.getRegions().iterator().next();
    unit = data.getUnits().iterator().next();

  }

  /**
   * @throws Exception
   */
  @Test
  public void testGetShipRange() throws Exception {
    EresseaGameSpecificRules rules = (EresseaGameSpecificRules) data.getGameSpecificRules();

    Ship ship = builder.addShip(data, region, "drac", "Drachenschiff", null, 100);

    assertEquals(5, rules.getShipRange(ship));

    unit.setShip(ship);
    ship.setOwner(unit);
    assertEquals(6, rules.getShipRange(ship));

    builder.addSkill(unit, "Segeln", 2);
    assertEquals(6, rules.getShipRange(ship));

    builder.addSkill(unit, "Segeln", 5);
    assertEquals(6, rules.getShipRange(ship));

    builder.addSkill(unit, "Segeln", 6);
    assertEquals(7, rules.getShipRange(ship));

    builder.addSkill(unit, "Segeln", 17);
    assertEquals(7, rules.getShipRange(ship));

    builder.addSkill(unit, "Segeln", 18);
    assertEquals(8, rules.getShipRange(ship));

    builder.addSkill(unit, "Segeln", 54);
    assertEquals(9, rules.getShipRange(ship));
  }

  @Test
  public void testSkillMerge() {
    Unit other = builder.addUnit(data, "oth", "Othership", unit.getFaction(), region);
    data.getDate().setDate(1256);

    Skill sk = builder.addSkill(other, "Ausdauer", 2);

    unit.clearOrders();
    unit.addOrder("GIB oth 1 PERSONEN");

    EresseaRelationFactory executor = new EresseaRelationFactory(data.getRules());
    executor.processOrders(region);

    assertEquals(0, unit.getModifiedPersons());
    assertEquals(2, other.getModifiedPersons());
    assertEquals(1, other.getModifiedSkill(sk.getSkillType()).getLevel());

    testMerge(1, 2, 1, 0, 2, 1);

    // /* 1xT2 merges with 2xT0 to become 3xT1 */
    // dst.level = 2;
    // dst.weeks = 3;
    testMerge(1, 2, 2, 0, 3, 1);

    // /* 1xT2 merges with 3xT0, skill goes poof */
    testMerge(1, 2, 3, 0, 4, 0); // 1 -- 0

    // /* two units with the same skills are merged: no change */
    // src.level = 2;
    // src.weeks = 3;
    testMerge(1, 2, 1, 2, 2, 2); // 3 -- 2

    // /* T4 + T6 always makes T5 */
    // src.level = 4;
    // dst.level = 6;
    testMerge(1, 4, 1, 6, 2, 5); // 6 -- 5

    testMerge(1, 99, 1, 1, 2, 69); // 51 -- 70
  }

  @Test
  public void testSkillMergeOld() {
    data.getDate().setDate(1255);
    testMerge(1, 2, 1, 0, 2, 2); // 2 -- 1

    // /* 1xT2 merges with 2xT0 to become 3xT1 */
    // dst.level = 2;
    // dst.weeks = 3;
    testMerge(1, 2, 2, 0, 3, 1);

    // /* 1xT2 merges with 3xT0, skill goes poof */
    testMerge(1, 2, 3, 0, 4, 1); // 1 -- 0

    // /* two units with the same skills are merged: no change */
    // src.level = 2;
    // src.weeks = 3;
    testMerge(1, 2, 1, 2, 2, 3); // 3 -- 2

    // /* T4 + T6 always makes T5 */
    // src.level = 4;
    // dst.level = 6;
    testMerge(1, 4, 1, 6, 2, 6); // 6 -- 5

    testMerge(1, 99, 1, 1, 2, 51); // 51 -- 70
  }

  private Unit merge1, merge2;
  int mr = 0;

  private void testMerge(int n1, int l1, int n2, int l2, int nEx, int lEx) {
    UnitID[] ids = new UnitID[data.getUnits().size()];
    int i = 0;
    for (Unit u : data.getUnits()) {
      ids[i++] = u.getID();
    }
    for (UnitID id : ids) {
      data.removeUnit(id);
    }

    merge1 = builder.addUnit(data, "mg" + (++mr), "Giver", unit.getFaction(), region);
    merge2 = builder.addUnit(data, "mr" + mr, "Getter", unit.getFaction(), region);
    merge1.clearSkills();
    merge2.clearSkills();
    merge1.clearRelations();
    merge2.clearRelations();
    merge1.clearOrders();
    merge2.clearOrders();

    merge1.setPersons(n1);
    builder.addSkill(merge1, "Ausdauer", l1);
    merge2.setPersons(n2);
    builder.addSkill(merge2, "Ausdauer", l2);

    merge1.clearOrders();
    merge1.addOrder("GIB mr" + mr + " ALLES PERSONEN");

    EresseaRelationFactory executor = new EresseaRelationFactory(data.getRules());
    executor.processOrders(region);

    assertEquals("giver not empty", 0, merge1.getModifiedPersons());
    assertEquals("wrong number of people", nEx, merge2.getModifiedPersons());
    assertEquals("wrong level", lEx, merge2.getModifiedSkills().iterator().next().getLevel());
  }

  @Test
  public void testSkillMergeThreeway() throws Exception {
    data = builder.createSimpleGameData(1256, false);
    region = data.getRegions().iterator().next();
    Faction faction = data.getFactions().iterator().next();
    Unit other = builder.addUnit(data, "mg1", "Merge 1", faction, region);
    Unit other2 = builder.addUnit(data, "mg2", "Merge 2", faction, region);
    Unit other3 = builder.addUnit(data, "mg3", "Merge 3", faction, region);

    Skill sk = builder.addSkill(other, "Ausdauer", 2);
    builder.addSkill(other2, "Ausdauer", 4);
    builder.addSkill(other3, "Ausdauer", 4);

    // m1(2) --> m3 (4->3), m2 (4) --> m1 (0->4), m3 (3) -> m2 (0->3)
    other.clearOrders();
    other.addOrder("GIB mg3 1 PERSONEN");
    other2.clearOrders();
    other2.addOrder("GIB mg1 1 PERSONEN");
    other3.clearOrders();
    other3.addOrder("GIB mg2 1 PERSONEN");

    EresseaRelationFactory executor = new EresseaRelationFactory(data.getRules());
    executor.processOrders(region);

    assertEquals(1, other.getModifiedPersons());
    assertEquals(1, other2.getModifiedPersons());
    assertEquals(1, other3.getModifiedPersons());
    assertEquals(4, other.getModifiedSkill(sk.getSkillType()).getLevel());
    assertEquals(3, other2.getModifiedSkill(sk.getSkillType()).getLevel());
    assertEquals(3, other3.getModifiedSkill(sk.getSkillType()).getLevel());
  }

  @Test
  public void testSkillMergeThreewayB() throws Exception {
    data = builder.createSimpleGameData(1256, false);
    region = data.getRegions().iterator().next();
    Faction faction = data.getFactions().iterator().next();
    Unit other3 = builder.addUnit(data, "mg3", "Merge 3", faction, region);
    Unit other = builder.addUnit(data, "mg1", "Merge 1", faction, region);
    Unit other2 = builder.addUnit(data, "mg2", "Merge 2", faction, region);

    Skill sk = builder.addSkill(other, "Ausdauer", 2);
    builder.addSkill(other2, "Ausdauer", 4);
    builder.addSkill(other3, "Ausdauer", 4);

    // m3(4) --> m2 (4->4), m1 (2) --> m3 (0->2), m2 (4) -> m1 (0->4)
    other.clearOrders();
    other.addOrder("GIB mg3 1 PERSONEN");
    other2.clearOrders();
    other2.addOrder("GIB mg1 1 PERSONEN");
    other3.clearOrders();
    other3.addOrder("GIB mg2 1 PERSONEN");

    EresseaRelationFactory executor = new EresseaRelationFactory(data.getRules());
    executor.processOrders(region);

    assertEquals(1, other.getModifiedPersons());
    assertEquals(1, other2.getModifiedPersons());
    assertEquals(1, other3.getModifiedPersons());
    assertEquals(4, other.getModifiedSkill(sk.getSkillType()).getLevel());
    assertEquals(4, other2.getModifiedSkill(sk.getSkillType()).getLevel());
    assertEquals(2, other3.getModifiedSkill(sk.getSkillType()).getLevel());
  }

  @Test
  public void testSkillMergeThreewayC() throws Exception {
    data = builder.createSimpleGameData(1256, false);
    region = data.getRegions().iterator().next();
    Faction faction = data.getFactions().iterator().next();
    Unit other3 = builder.addUnit(data, "mg3", "Merge 3", faction, region);
    Unit other2 = builder.addUnit(data, "mg2", "Merge 2", faction, region);
    Unit other = builder.addUnit(data, "mg1", "Merge 1", faction, region);

    Skill sk = builder.addSkill(other, "Ausdauer", 2);
    builder.addSkill(other2, "Ausdauer", 4);
    builder.addSkill(other3, "Ausdauer", 4);

    // m3(4) --> m2 (4->4), m2 (4) --> m1 (2->3), m1 (3) -> m3 (0->3)
    other.clearOrders();
    other.addOrder("GIB mg3 1 PERSONEN");
    other2.clearOrders();
    other2.addOrder("GIB mg1 1 PERSONEN");
    other3.clearOrders();
    other3.addOrder("GIB mg2 1 PERSONEN");

    EresseaRelationFactory executor = new EresseaRelationFactory(data.getRules());
    executor.processOrders(region);

    assertEquals(1, other.getModifiedPersons());
    assertEquals(1, other2.getModifiedPersons());
    assertEquals(1, other3.getModifiedPersons());
    assertEquals(3, other.getModifiedSkill(sk.getSkillType()).getLevel());
    assertEquals(4, other2.getModifiedSkill(sk.getSkillType()).getLevel());
    assertEquals(3, other3.getModifiedSkill(sk.getSkillType()).getLevel());
  }
}
