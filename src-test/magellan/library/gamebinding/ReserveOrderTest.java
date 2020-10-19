// class magellan.library.gamebinding.GiveOrderTest
// created on Jan 10, 2020
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
import magellan.library.Order;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.relation.ReserveRelation;
import magellan.library.relation.TransferRelation;
import magellan.library.relation.UnitRelation;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

/**
 * @author stm
 */
public class ReserveOrderTest extends MagellanTestWithResources {

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

  /**
   * 
   */
  @Test
  public void testReserve() {
    builder.addItem(data, unit, "Silber", 3);

    unit.clearOrders();
    unit.addOrder("RESERVIERE 2 Silber");

    Order order = unit.getOrders2().get(0);
    assertTrue(order instanceof ReserveOrder);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 1);

    assertProblem(unit, false);
    assertReserve(unit, 0, 2);

    clear();

    Unit unit2 = builder.addUnit(data, "two", "Other", unit.getFaction(), region0);
    builder.addItem(data, unit2, "Silber", 1);
    unit.addOrder("RESERVIERE 4 Silber");

    order = unit.getOrders2().get(0);
    assertTrue(order instanceof ReserveOrder);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 1);

    assertProblem(unit, false);
    assertProblem(unit2, false);
    assertTransfer(unit2, 0, unit, 0, 1);
    assertReserve(unit, 1, 4);

    clear();

    Unit unit3 = builder.addUnit(data, "tree", "Third", unit.getFaction(), region0);
    builder.addItem(data, unit3, "Silber", 1);
    unit.addOrder("RESERVIERE 4 Silber");

    process(region0);

    assertProblem(unit, false);
    assertProblem(unit2, false);
    assertProblem(unit3, false);

    assertRelations(unit, 2);
    assertRelations(unit2, 1);
    assertRelations(unit3, 0);
    assertReserve(unit, 1, 4);
    assertTransfer(unit2, 0, unit, 0, 1);

    clear();

    unit.addOrder("RESERVIERE 4 Silber");
    unit2.addOrder("RESERVIERE 1 Silber");

    process(region0);

    assertReserve(unit, 1, 4);
    assertReserve(unit2, 0, 1);
    assertTransfer(unit3, 0, unit, 0, 1);

  }

  /**
   * 
   */
  @Test
  public void testReserveFaction() {
    Faction faction1 = builder.addFaction(data, "oth", "Others", "Mensch", 1);
    Unit unit2 = builder.addUnit(data, "foe", "Foe", faction1, region0, false);

    builder.addItem(data, unit2, "Silber", 3);

    unit.clearOrders();
    unit.addOrder("RESERVIERE 2 Silber");

    Order order = unit.getOrders2().get(0);
    assertTrue(order instanceof ReserveOrder);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 1);

    assertProblem(unit, true);
    assertRelations(unit, 1);
    assertRelations(unit2, 0);
    assertReserve(unit, 0, 0);
  }

  private void assertProblem(Unit unit, boolean problem) {
    Order problemOrder = null;
    for (Order o : unit.getOrders2())
      if (o.getProblem() != null) {
        problemOrder = o;
      }
    UnitRelation problemRelation = null;
    for (UnitRelation rel : unit.getRelations()) {
      if (rel.problem != null) {
        problemRelation = rel;
      }
    }
    if (problem) {
      assertEquals(problem, problemOrder != null || problemRelation != null);
    }
  }

  private void assertRelations(Unit unit, int number) {
    assertEquals(number, unit.getRelations().size());
  }

  private void assertTransfer(Unit source, int pos1, Unit target, int pos2, int amount) {
    TransferRelation transfer = (TransferRelation) source.getRelations().get(pos1);
    assertEquals(amount, transfer.amount);
    assertEquals(source, transfer.source);
    assertEquals(target, transfer.target);
    transfer = (TransferRelation) target.getRelations().get(pos2);
    assertEquals(amount, transfer.amount);
    assertEquals(source, transfer.source);
    assertEquals(target, transfer.target);
  }

  private void assertReserve(Unit unit, int pos, int amount) {
    ReserveRelation reserve = (ReserveRelation) unit.getRelations().get(pos);
    assertEquals(amount, reserve.amount);
  }

  private void clear() {
    for (Unit u : data.getUnits()) {
      unit.clearRelations();
      u.clearOrders();
    }

  }

  private void process(Region region) {
    EresseaRelationFactory executor = new EresseaRelationFactory(data.getRules());
    executor.processOrders(region);
  }

}
