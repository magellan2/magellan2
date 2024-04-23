// class magellan.library.gamebinding.EresseaMovementEvaluatorTest
// created on Sep 12, 2020
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
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

/**
 * Tests for MovementEvaluator
 *
 */
public class EresseaMovementEvaluatorTest extends MagellanTestWithResources {

  private Unit unit;
  private GameDataBuilder builder;
  private GameData data;
  private EresseaMovementEvaluator movement;
  private Region region;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    builder = new GameDataBuilder();
    data = builder.createSimpleGameData();
    region = data.getRegions().iterator().next();
    unit = data.getUnits().iterator().next();
    unit.clearOrders();
    movement = new EresseaMovementEvaluator(data.getRules());
  }

  /**
   * 
   */
  @Test
  public void testGetPayloadOnHorse() {
    assertEquals(MovementEvaluator.CAP_NO_HORSES, movement.getPayloadOnHorse(unit));
    builder.addItem(data, unit, "Pferd", 1);
    unit.clearCache();
    assertEquals(MovementEvaluator.CAP_UNSKILLED, movement.getPayloadOnHorse(unit));

    builder.addSkill(unit, "Reiten", 1);
    unit.clearCache();
    assertEquals(1000, movement.getPayloadOnHorse(unit));

    builder.addItem(data, unit, "Wagen", 1);
    unit.clearCache();
    assertTrue(0 > movement.getPayloadOnHorse(unit));

    builder.addItem(data, unit, "Pferd", 2);
    unit.clearCache();
    assertEquals(10000 + 2 * 2000 - 1000, movement.getPayloadOnHorse(unit));

    unit.setPersons(2);
    unit.clearCache();
    assertEquals(10000 + 2 * 2000 - 2 * 1000, movement.getPayloadOnHorse(unit));

    builder.addItem(data, unit, "Wagen", 2);
    unit.clearCache();
    assertEquals(10000 + 2 * 2000 - 2 * 1000 - 4000, movement.getPayloadOnHorse(unit));

    builder.addItem(data, unit, "Wagen", 1);
    builder.addItem(data, unit, "Pferd", 3);
    unit.clearCache();
    assertEquals(10000 + 3 * 2000 - 2 * 1000, movement.getPayloadOnHorse(unit));

    builder.addItem(data, unit, "Wagen", 0);
    builder.addItem(data, unit, "Katapult", 1);
    builder.addItem(data, unit, "Pferd", 2);
    unit.clearCache();
    assertEquals(0 + 2 * 2000 - 2 * 1000, movement.getPayloadOnHorse(unit));

    unit.setPersons(1);
    builder.addSkill(unit, "Reiten", 5);
    builder.addItem(data, unit, "Wagen", 3);
    builder.addItem(data, unit, "Katapult", 2);
    builder.addItem(data, unit, "Pferd", 10);
    unit.clearCache();
    assertEquals(3 * 10000 + 10 * 2000 - 1 * 1000, movement.getPayloadOnHorse(unit));

    unit.setPersons(1);
    builder.addSkill(unit, "Reiten", 5);
    builder.addItem(data, unit, "Wagen", 3);
    builder.addItem(data, unit, "Katapult", 3);
    builder.addItem(data, unit, "Pferd", 10);
    unit.clearCache();
    assertEquals(3 * 10000 + 10 * 2000 - 1 * 1000 - 10000, movement.getPayloadOnHorse(unit));
  }

  /**
   * 
   */
  @Test
  public void testGetPayloadOnFoot() {
    assertEquals(540, movement.getPayloadOnFoot(unit));
    unit.setPersons(0);
    unit.clearCache();
    assertEquals(0, movement.getPayloadOnFoot(unit));

    unit.setPersons(2);
    unit.clearCache();
    assertEquals(1080, movement.getPayloadOnFoot(unit));

    unit.setPersons(1);
    builder.addItem(data, unit, "Pferd", 1);
    unit.clearCache();
    assertEquals(2000 + 540, movement.getPayloadOnFoot(unit));

    builder.addItem(data, unit, "Wagen", 1);
    unit.setPersons(10);
    unit.clearCache();
    assertEquals(10 * 540 + 2000 - 4000, movement.getPayloadOnFoot(unit));

    unit.setPersons(1);
    unit.clearCache();
    assertTrue(0 > movement.getPayloadOnFoot(unit));

    builder.addItem(data, unit, "Pferd", 2);
    unit.clearCache();
    assertEquals(MovementEvaluator.CAP_UNSKILLED, movement.getPayloadOnFoot(unit));

    unit.setPersons(2);
    unit.clearCache();
    assertEquals(2 * 540 + 2 * 2000 + 10000, movement.getPayloadOnFoot(unit));

    builder.addItem(data, unit, "Wagen", 2);
    unit.clearCache();
    assertEquals(2 * 540 + 2 * 2000 + 10000 - 4000, movement.getPayloadOnFoot(unit));

    builder.addItem(data, unit, "Pferd", 3);
    unit.clearCache();
    assertEquals(MovementEvaluator.CAP_UNSKILLED, movement.getPayloadOnFoot(unit));

    builder.addSkill(unit, "Reiten", 1);
    unit.clearCache();
    assertEquals(2 * 540 + 3 * 2000 + 10000 - 4000, movement.getPayloadOnFoot(unit));
  }

  /**
   * 
   */
  @Test
  public void testTrollPayload() {
    assertEquals(540, movement.getPayloadOnFoot(unit));
    Faction trolls = builder.addFaction(data, "trll", "troll", "Trolle", 2);
    Unit troll = builder.addUnit(data, "trll", "Ein Troll", trolls, region);

    builder.addItem(data, troll, "Wagen", 1);
    troll.clearCache();
    assertTrue(0 > movement.getPayloadOnFoot(troll));
    troll.setPersons(4);
    troll.clearCache();
    assertEquals(4 * 1080 + 10000, movement.getPayloadOnFoot(troll));

    builder.addItem(data, troll, "Pferd", 4);
    builder.addItem(data, troll, "Wagen", 3);
    troll.clearCache();
    assertEquals(4 * 1080 + 3 * 10000 + 4 * 2000, movement.getPayloadOnFoot(troll));

    builder.addItem(data, troll, "Pferd", 4);
    builder.addItem(data, troll, "Wagen", 2);
    builder.addItem(data, troll, "Katapult", 1);
    troll.clearCache();
    assertEquals(4 * 1080 + 2 * 10000 + 4 * 2000, movement.getPayloadOnFoot(troll));

    // Trolls may use horses *and* pull carts at the same time
    builder.addItem(data, troll, "Pferd", 4);
    builder.addItem(data, troll, "Wagen", 4);
    builder.addItem(data, troll, "Katapult", 0);
    troll.clearCache();
    assertEquals(4 * 1080 + 3 * 10000 + 4 * 2000 - 4000, movement.getPayloadOnFoot(troll));

    builder.addSkill(troll, "Reiten", 1);
    assertEquals(2 * 10000 + 4 * 2000 - 2 * 4000 - 4 * 2000, movement.getPayloadOnHorse(troll));
  }

}
