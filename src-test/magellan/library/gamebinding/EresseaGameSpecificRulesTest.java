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

import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Unit;
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

}
