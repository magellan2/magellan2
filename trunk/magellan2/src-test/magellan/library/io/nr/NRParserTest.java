// class magellan.library.io.nr.NRParserTest
// created on Apr 19, 2013
//
// Copyright 2003-2013 by magellan project team
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
package magellan.library.io.nr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import magellan.library.Building;
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.GameData;
import magellan.library.Item;
import magellan.library.Rules;
import magellan.library.Ship;
import magellan.library.Skill;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.io.RulesReader;

import org.junit.Before;
import org.junit.Test;

public class NRParserTest {

  private static final String ATLANTIS = "atlantis";
  private NRParser parser;
  private GameData data;
  private Rules rules;

  @Before
  public void setUp() throws Exception {
    parser = new NRParser(null);
    rules = new RulesReader().readRules(ATLANTIS);
    data = rules.getGameSpecificStuff().createGameData(ATLANTIS);
  }

  @Test
  public final void testStart() throws IOException {
    MockReader reader = new MockReader();
    addStartReport(reader, 1, "In the Beginning");

    parser.read(reader, data);

    assertSame(0, parser.getErrors());
    assertSame(7, data.getRegions().size());
    assertSame(2, data.getUnits().size());
    assertEquals("Leighlin", data.getRegion(CoordinateID.ZERO).getName());
    assertEquals(400, data.getRegion(CoordinateID.ZERO).getPeasants());
    assertEquals(600, data.getRegion(CoordinateID.ZERO).getSilver());
    Unit unit1, unit2;
    assertEquals("Unit 1", (unit1 = data.getUnit(EntityID.createEntityID(1, 10))).getName());
    assertEquals("Unit 2", (unit2 = data.getUnit(EntityID.createEntityID(3, 10))).getName());
    assertEquals(5000, getItem(unit1, "silver"));
    assertEquals(-1, getItem(unit2, "silver"));
  }

  @Test
  public final void testUnit() throws IOException {
    MockReader reader = new MockReader();
    reader.addHeader(1, "January", 1);
    reader.addRegion("Leighlin", 0, 0, "forest", "south, east, mir, ydd", 400, 600);
    reader.addUnit("Unit 1", 42, "Faction 1", 1, 1000, "work", 5, new Object[] { "stealth", 1, 30,
        "observation", 3, 180 }, new Object[] { 10, "wood", 5, "stone" });

    parser.read(reader, data);

    assertSame(0, parser.getErrors());
    Unit unit1;
    assertEquals("Unit 1", (unit1 = data.getUnit(EntityID.createEntityID(42, 10))).getName());
    assertEquals(1000, getItem(unit1, "silver"));
    assertEquals(10, getItem(unit1, "wood"));
    assertEquals(5, getItem(unit1, "stone"));
    assertEquals(5, unit1.getPersons());
    assertEquals(1, getSkill(unit1, "stealth"));
    assertEquals(3, getSkill(unit1, "observation"));
  }

  @Test
  public final void testShip() throws IOException {
    MockReader reader = new MockReader();
    reader.addHeader(1, "January", 1);
    reader.addRegion("Leighlin", 0, 0, "forest", "south, east, mir, ydd", 400, 600);
    reader.addShip("Ship1", 99, "longboat", "desc s99");
    reader.addUnit("Unit 1", 42, "Faction 1", 1, 1000, "work", 5, new Object[] { "stealth", 1, 30,
        "observation", 3, 180 }, new Object[] { 10, "wood", 5, "stone" });
    reader.addLine("");
    reader.addUnit("Unit 2", 43, "Faction 1", 1, 1000, "work");

    parser.read(reader, data);

    assertSame(0, parser.getErrors());
    Unit unit1, unit2;
    assertEquals("Unit 1", (unit1 = data.getUnit(EntityID.createEntityID(42, 10))).getName());
    assertEquals("Unit 2", (unit2 = data.getUnit(EntityID.createEntityID(43, 10))).getName());
    Ship ship;
    assertEquals("Ship1", (ship = data.getShip(EntityID.createEntityID(99, 10))).getName());
    assertEquals(unit1.getRegion(), ship.getRegion());
    assertTrue(unit1.getRegion().getShip(EntityID.createEntityID(99, 10)) == ship);

    assertEquals(ship, unit1.getShip());
    assertNull(unit2.getShip());
    assertEquals("Longboat", ship.getType().toString());
    assertEquals("desc s99", ship.getDescription());
  }

  @Test
  public final void testBuilding() throws IOException {
    MockReader reader = new MockReader();
    reader.addHeader(1, "January", 1);
    reader.addRegion("Leighlin", 0, 0, "forest", "south, east, mir, ydd", 400, 600);
    reader.addBuilding("Castle 99", 98, 12, "desc c99");
    reader.addUnit("Unit 1", 42, "Faction 1", 1, 1000, "work", 5, new Object[] { "stealth", 1, 30,
        "observation", 3, 180 }, new Object[] { 10, "wood", 5, "stone" });
    reader.addLine("");
    reader.addUnit("Unit 2", 43, "Faction 1", 1, 1000, "work");

    parser.read(reader, data);

    assertSame(0, parser.getErrors());
    Unit unit1, unit2;
    assertEquals("Unit 1", (unit1 = data.getUnit(EntityID.createEntityID(42, 10))).getName());
    assertEquals("Unit 2", (unit2 = data.getUnit(EntityID.createEntityID(43, 10))).getName());
    Building building;
    assertEquals("Castle 99", (building = data.getBuilding(EntityID.createEntityID(98, 10)))
        .getName());
    assertEquals(unit1.getRegion(), building.getRegion());
    assertTrue(unit1.getRegion().getBuilding(EntityID.createEntityID(98, 10)) == building);

    assertEquals(building, unit1.getBuilding());
    assertNull(unit2.getShip());
    assertEquals("Building", building.getType().toString());
    assertEquals("desc c99", building.getDescription());
  }

  private int getSkill(Unit unit, String skillName) {
    Skill skill = unit.getSkill(rules.getSkillType(StringID.create(skillName)));
    if (skill != null)
      return skill.getLevel();
    else
      return -1;
  }

  protected int getItem(Unit unit, String itemName) {
    Item item = unit.getItem(rules.getItemType(StringID.create(itemName)));
    if (item != null)
      return item.getAmount();
    else
      return -1;
  }

  protected void addHeader(MockReader reader, int fnum, String date) {
    reader.addHeader(fnum, date);
    reader.addRegion("Leighlin", 0, 0, "forest", "south, east, mir, ydd", 400, 600);
  }

  protected void addStartReport(MockReader reader, int fnum, String date) {
    reader.addHeader(fnum, date);
    reader.addMessage("Your password is 'lichtgriffel'.");
    reader.addRegion("Leighlin", 0, 0, "forest", "south, east, mir, ydd", 400, 600);
    reader.addUnit("Unit 1", 1, "Faction1", 1, 5000, "work");
    reader.addUnit("Unit 2", 3, null, 0, 0, null);
  }

}
