// class magellan.library.gamebinding.EnterOrderTest
// created on Jan 17, 2020
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import magellan.library.Building;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.relation.EnterRelation;
import magellan.library.relation.LeaveRelation;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

public class EnterOrderTest extends MagellanTestWithResources {

  private GameDataBuilder builder;
  private GameData data;
  private Unit unit;
  private Region region0;
  private EresseaRelationFactory relationFactory;
  private Faction faction0;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    builder = new GameDataBuilder();
    data = builder.createSimpleGameData();
    unit = data.getUnits().iterator().next();
    faction0 = unit.getFaction();
    region0 = data.getRegions().iterator().next();
    relationFactory = ((EresseaRelationFactory) data.getGameSpecificStuff().getRelationFactory());
    relationFactory.stopUpdating();
  }

  @Test
  public void testEnter() {
    Building b0 = builder.addBuilding(data, region0, "b0", "Burg", "Empty Castle", 10);
    Building b1 = builder.addBuilding(data, region0, "b1", "Burg", "Owned Castle", 10);
    Building b2 = builder.addBuilding(data, region0, "b2", "Burg", "Left Castle", 10);
    Building b3 = builder.addBuilding(data, region0, "b3", "Burg", "Entered Castle", 10);

    Unit unit2 = builder.addUnit(data, "Owner", region0);
    builder.addTo(unit2, b1);
    builder.addTo(unit, b2);

    unit.addOrder("BETRETE BURG b3");

    EresseaRelationFactory executor = new EresseaRelationFactory(data.rules);
    executor.processOrders(region0);

    assertContainerUnits(b0);
    assertContainerUnits(b1, unit2);
    assertEquals(b1, unit2.getModifiedBuilding());

    assertContainerUnits(b2);
    assertContainerUnits(b3, unit);
    assertEquals(b3, unit.getModifiedBuilding());
    assertEquals(LeaveRelation.class, unit.getRelations().get(0).getClass());
    LeaveRelation rel = (LeaveRelation) unit.getRelations().get(0);
    assertEquals(rel.target, b2);
    assertEquals(EnterRelation.class, unit.getRelations().get(1).getClass());
  }

  private void assertContainerUnits(UnitContainer uc, Unit... args) {
    if (args.length == 0) {
      assertNull(uc.getModifiedOwnerUnit());
      assertEquals(0, uc.modifiedUnits().size());
    } else {
      assertEquals(args[0], uc.getModifiedOwnerUnit());
      assertEquals(args.length, uc.modifiedUnits().size());
      assertCollection(uc.modifiedUnits(), args);
      assertEquals(args[0], uc.getModifiedUnit(args[0].getID()));
      if (args.length > 1) {
        assertEquals(args[1], uc.getModifiedUnit(args[1].getID()));
      }
    }
  }

  private void assertCollection(Collection<Unit> col2, Unit... args) {
    assertEquals(args.length, col2.size());

    int i = 0;
    for (Unit u : col2) {
      assertSame(u, args[i++]);
    }
  }

  @Test
  public void testEnterEnter() {
    Building b1 = builder.addBuilding(data, region0, "b1", "Burg", "Burg 1", 10);
    Building b2 = builder.addBuilding(data, region0, "b2", "Burg", "Burg 2", 10);

    unit.addOrder("BETRETE BURG b1");
    unit.addOrder("BETRETE BURG b2");

    EresseaRelationFactory executor = new EresseaRelationFactory(data.rules);
    executor.processOrders(region0);

    assertEquals(b2, unit.getModifiedBuilding());
    assertEquals(1, b2.modifiedUnits().size());
    assertEquals(unit, b2.getModifiedOwnerUnit());
    assertEquals(0, b1.modifiedUnits().size());
  }

  @Test
  public void testEnterLeave() {
    Building b1 = builder.addBuilding(data, region0, "b1", "Burg", "Burg 1", 10);

    unit.addOrder("BETRETE BURG b1");
    unit.addOrder("VERLASSE");

    EresseaRelationFactory executor = new EresseaRelationFactory(data.rules);
    executor.processOrders(region0);

    assertEquals(b1, unit.getModifiedBuilding());
    assertEquals(1, b1.modifiedUnits().size());
    assertEquals(unit, b1.getModifiedOwnerUnit());
  }
}
