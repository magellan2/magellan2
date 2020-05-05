// class magellan.library.gamebinding.UnitOrderingTest
// created on Jan 28, 2020
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import magellan.library.GameData;
import magellan.library.Order;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.test.GameDataBuilder;

public class UnitOrderingTest {

  private GameDataBuilder builder;
  private GameData data;
  private Unit unit;
  private EresseaRelationFactory relationFactory;
  private Region region0;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    builder = new GameDataBuilder();
    data = builder.createSimpleGameData();
    unit = data.getUnits().iterator().next();
    region0 = unit.getRegion();
    relationFactory = ((EresseaRelationFactory) data.getGameSpecificStuff().getRelationFactory());
    relationFactory.stopUpdating();
  }

  @Test
  public void testHasNext() {
    unit.addOrder("GIB 1 2 3");
    unit.addOrder("BETRETE");

    UnitOrdering uo = new UnitOrdering();

    assertFalse(uo.hasNext());

    uo.reset(3);
    assertFalse(uo.hasNext());

    uo.add(unit.getOrders2().get(0), 3, unit, 4);
    uo.add(unit.getOrders2().get(1), 1, unit, 5);
    uo.add(unit.getOrders2().get(2), 2, unit, 6);

    assertTrue(uo.hasNext());

    assertNext(uo, unit.getOrders2().get(0), unit, 3, 4);
    assertNext(uo, unit.getOrders2().get(1), unit, 1, 5);
    assertNext(uo, unit.getOrders2().get(2), unit, 2, 6);
    assertFalse(uo.hasNext());

    uo.sort(data.getUnits());
    uo.reset();

    assertNext(uo, unit.getOrders2().get(1), unit, 1, 5);
    assertNext(uo, unit.getOrders2().get(2), unit, 2, 6);
    assertNext(uo, unit.getOrders2().get(0), unit, 3, 4);
    assertFalse(uo.hasNext());

  }

  private void assertNext(UnitOrdering uo, Order order, Unit unit0, int prio, int line) {
    assertTrue(uo.hasNext());
    assertEquals(order, uo.getOrder());
    assertEquals(line, uo.getLine());
    assertEquals(prio, uo.getPriority());
    assertEquals(unit0, uo.getUnit());
    uo.consume();
  }

  @Test
  public void testUnitOrder() {
    Unit unit2 = builder.addUnit(data, "Unit 2", region0);
    unit2.clearOrders();
    unit2.addOrder("BETRETE 2");
    unit.addOrder("GIB 1 2 3");
    unit.addOrder("BETRETE 1");

    UnitOrdering uo = new UnitOrdering();

    assertFalse(uo.hasNext());

    uo.reset(4);
    assertFalse(uo.hasNext());

    uo.add(unit2.getOrders2().get(0), 1, unit2, 7); // BETRETE
    uo.add(unit.getOrders2().get(0), 3, unit, 4); // empty
    uo.add(unit.getOrders2().get(1), 2, unit, 5); // GIB
    uo.add(unit.getOrders2().get(2), 1, unit, 6); // BETRETE

    assertTrue(uo.hasNext());
    assertNext(uo, unit2.getOrders2().get(0), unit2, 1, 7);
    assertNext(uo, unit.getOrders2().get(0), unit, 3, 4);
    assertNext(uo, unit.getOrders2().get(1), unit, 2, 5);
    assertNext(uo, unit.getOrders2().get(2), unit, 1, 6);
    assertFalse(uo.hasNext());

    uo.sort(data.getUnits());
    uo.reset();

    assertNext(uo, unit.getOrders2().get(2), unit, 1, 6); // u1 BETRETE
    assertNext(uo, unit2.getOrders2().get(0), unit2, 1, 7); // u2 BETRETE
    assertNext(uo, unit.getOrders2().get(1), unit, 2, 5); // u1 GIB
    assertNext(uo, unit.getOrders2().get(0), unit, 3, 4); // u1 empty

    uo.sort(data.getUnits());
    uo.reset();

    assertNext(uo, unit.getOrders2().get(2), unit, 1, 6); // u1 BETRETE
    assertNext(uo, unit2.getOrders2().get(0), unit2, 1, 7); // u2 BETRETE
    assertNext(uo, unit.getOrders2().get(1), unit, 2, 5); // u1 GIB
    assertNext(uo, unit.getOrders2().get(0), unit, 3, 4); // u1 empty
  }

  @Test
  public void testUnitReorder() {
    Unit unit2 = builder.addUnit(data, "Unit 2", region0);
    unit2.addOrder("BENENNE");
    unit2.addOrder("BETRETE 2");
    unit.addOrder("GIB 1 2 3");
    unit.addOrder("BETRETE 1");

    UnitOrdering uo = new UnitOrdering();

    assertFalse(uo.hasNext());

    uo.reset(5);

    // omit unit2.order[0]
    uo.add(unit2.getOrders2().get(1), 1, unit2, 7); // BETRETE
    uo.add(unit.getOrders2().get(0), 3, unit, 4); // empty
    uo.add(unit.getOrders2().get(1), 2, unit, 5); // GIB
    uo.add(unit.getOrders2().get(2), 1, unit, 6); // BETRETE

    uo.sort(data.getUnits());

    // 4 orders, sorted by prio and unit order
    assertNext(uo, unit.getOrders2().get(2), unit, 1, 6); // u1 BETRETE
    assertNext(uo, unit2.getOrders2().get(1), unit2, 1, 7); // u2 BETRETE
    assertNext(uo, unit.getOrders2().get(1), unit, 2, 5); // u1 GIB
    assertNext(uo, unit.getOrders2().get(0), unit, 3, 4); // u1 empty

    uo.reset();
    uo.insert(unit, unit2);

    // only affected orders would be BETRETE, but current prio is already 1 => no new order
    assertNext(uo, unit.getOrders2().get(2), unit, 1, 6); // u1 BETRETE
    assertNext(uo, unit2.getOrders2().get(1), unit2, 1, 7); // u2 BETRETE
    assertNext(uo, unit.getOrders2().get(1), unit, 2, 5); // u1 GIB
    assertNext(uo, unit.getOrders2().get(0), unit, 3, 4); // u1 empty

    uo.add(unit2.getOrders2().get(0), 0, unit2, 8);
    uo.reset();
    uo.sort(data.getUnits());

    // 5 orders, natural order
    assertNext(uo, unit2.getOrders2().get(0), unit2, 0, 8); // u2 BENENNE
    assertNext(uo, unit.getOrders2().get(2), unit, 1, 6); // u1 BETRETE
    assertNext(uo, unit2.getOrders2().get(1), unit2, 1, 7); // u2 BETRETE
    assertNext(uo, unit.getOrders2().get(1), unit, 2, 5); // u1 GIB
    assertNext(uo, unit.getOrders2().get(0), unit, 3, 4); // u1 empty

    uo.insert(unit, unit2);
    uo.reset();

    // reset after insert => no change in order
    assertNext(uo, unit2.getOrders2().get(0), unit2, 0, 8); // u2 BENENNE
    assertNext(uo, unit.getOrders2().get(2), unit, 1, 6); // u1 BETRETE
    assertNext(uo, unit2.getOrders2().get(1), unit2, 1, 7); // u2 BETRETE
    assertNext(uo, unit.getOrders2().get(1), unit, 2, 5); // u1 GIB
    assertNext(uo, unit.getOrders2().get(0), unit, 3, 4); // u1 empty

    uo.sort(data.getUnits());
    uo.reset();
    uo.insert(unit, unit2);

    // BETRETE orders re-ordered
    assertNext(uo, unit2.getOrders2().get(0), unit2, 0, 8); // u2 BENENNE
    assertNext(uo, unit2.getOrders2().get(1), unit2, 1, 7); // u2 BETRETE
    assertNext(uo, unit.getOrders2().get(2), unit, 1, 6); // u1 BETRETE
    assertNext(uo, unit.getOrders2().get(1), unit, 2, 5); // u1 GIB
    assertNext(uo, unit.getOrders2().get(0), unit, 3, 4); // u1 empty
  }

}
