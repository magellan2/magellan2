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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import magellan.library.GameData;
import magellan.library.Order;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.relation.EnterRelation;
import magellan.library.relation.InterUnitRelation;
import magellan.library.relation.LeaveRelation;
import magellan.library.relation.ShipTransferRelation;
import magellan.library.relation.UnitRelation;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

/**
 * @author stm
 * @version 1.0, Jan 10, 2020
 */
public class GiveOrderTest extends MagellanTestWithResources {

  private GameDataBuilder builder;
  private GameData data;
  private Unit unit;
  private Region region0;
  private EresseaRelationFactory relationFactory;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    builder = new GameDataBuilder();
    data = builder.createSimpleGameData();
    unit = data.getUnits().iterator().next();
    region0 = data.getRegions().iterator().next();
    relationFactory = ((EresseaRelationFactory) data.getGameSpecificStuff().getRelationFactory());
    relationFactory.stopUpdating();
  }

  /**
   * Test basic operation
   */
  @Test
  public void testGiveShip() {
    Unit unit2 = builder.addUnit(data, "two", "Other", unit.getFaction(), region0);

    unit.clearOrders();
    unit.addOrder("GIB two 1 SCHIFF");

    Order order = unit.getOrders2().get(0);
    assertTrue(order instanceof GiveOrder);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 0);

    assertEquals("Unit_1 (1) besitzt kein Schiff", order.getProblem().getMessage());

    Ship ship = builder.addShip(data, region0, "sing", "Trireme", "Trireme", 200);
    Ship ship2 = builder.addShip(data, region0, "conv", "Trireme", "Konvoi", 200);

    // transfer ship from fleet to fleet
    addToShip(unit, ship);
    addToShip(unit2, ship2);
    changeAmount(ship, 2);
    changeAmount(ship2, 2);
    assertEquals(400, ship.getModifiedSize());
    assertEquals(400, ship2.getModifiedSize());

    order.setProblem(null);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 0);
    assertNull(order.getProblem());

    List<UnitRelation> relations = unit.getRelations();
    assertEquals(2, relations.size());
    ShipTransferRelation str = (ShipTransferRelation) relations.get(0);
    InterUnitRelation iur = (InterUnitRelation) relations.get(1);
    List<UnitRelation> shipRelations = ship.getRelations();
    assertEquals(1, shipRelations.size());
    shipRelations = ship2.getRelations();
    assertEquals(1, shipRelations.size());

    assertEquals(0, str.line);
    assertSame(unit, str.origin);
    assertSame(unit, str.source);
    assertSame(unit2, str.target);
    assertSame(ship, str.ship);
    assertSame(1, str.amount);

    assertEquals(3, ship2.getModifiedAmount());
    assertEquals(1, ship.getModifiedAmount());
    assertEquals(600, ship2.getModifiedSize());
    assertEquals(200, ship.getModifiedSize());

    // transfer all ships to fleet, moving units
    order.setProblem(null);
    unit.clearRelations();
    unit2.clearRelations();
    ship.clearRelations();
    ship2.clearRelations();
    changeAmount(ship, 1);
    changeAmount(ship2, 1);
    assertEquals(200, ship.getModifiedSize());
    assertEquals(200, ship2.getModifiedSize());

    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 0);
    assertNull(order.getProblem());

    relations = unit.getRelations();
    assertEquals(4, relations.size());
    str = (ShipTransferRelation) relations.get(0);
    iur = (InterUnitRelation) relations.get(1);
    LeaveRelation lr = (LeaveRelation) relations.get(2);
    EnterRelation er = (EnterRelation) relations.get(3);
    shipRelations = ship.getRelations();
    assertEquals(2, shipRelations.size());
    shipRelations = ship2.getRelations();
    assertEquals(2, shipRelations.size());

    assertEquals(0, str.line);
    assertSame(unit, str.origin);
    assertSame(unit, str.source);
    assertSame(unit2, str.target);
    assertSame(ship, str.ship);
    assertSame(1, str.amount);
  }

  @Test
  public void testGive0Ship() {
    unit.clearOrders();
    unit.addOrder("GIB 0 2 SCHIFF");
    Ship ship = builder.addShip(data, region0, "sing", "Trireme", "Trireme", 200);
    ship.setDamageRatio(3);
    changeAmount(ship, 3);

    addToShip(unit, ship);

    Order order = unit.getOrders2().get(0);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 0);

    assertNull(order.getProblem());
    assertEquals(1, data.getShips().size());
    assertEquals(1, ship.getModifiedAmount());
    assertEquals(1, ship.getTempShips().size());
    Ship ship2 = ship.getTempShips().toArray(new Ship[] {})[0];
    assertEquals(2, ship2.getModifiedAmount());
    assertEquals("-sing", ship2.getID().toString());
    assertEquals(0, ship2.getAmount());
    assertEquals(2, ship2.getModifiedAmount());
    assertEquals(0, ship2.getSize());
    assertEquals(2 * ship.getShipType().getMaxSize(), ship2.getModifiedSize());
    assertEquals(0, ship2.getCargo());
    assertEquals(ship.getShoreId(), ship2.getShoreId());
    assertNull(ship2.getUnits());
    assertEquals(3, ship2.getDamageRatio());
    assertEquals(region0, ship.getRegion());
    assertEquals(ship.getType(), ship2.getType());
    assertEquals(ship.getSpeed(), ship2.getSpeed());
    assertEquals(0, ship2.getCapacity());
    assertEquals(2 * ship.getModifiedMaxCapacity(), ship2.getModifiedMaxCapacity());
  }

  // TODO übergabe an einheit für neues schiff
  // TODO verzauberte Schiffe
  // TODO keine Übergabe an leere Flotte auf see
  // TODO same faction
  // TODO same ship -> u2 leave ship
  // TODO ship type
  // TODO coast
  // GIB 0 an Land
  // GIB 0 ALLES SCHIFF invalid
  // TODO GIB xyz ALLES SCHIF und xyz existiert nicht --> Warnung!

  /**
   *
   */
  @Test
  public void testCreateDeleteShip() {
    unit.clearOrders();
    unit.addOrder("GIB 0 1 SCHIFF");
    unit.addOrder("GIB 0 1 SCHIFF");
    Ship ship = builder.addShip(data, region0, "sing", "Trireme", "Trireme", 200);
    changeAmount(ship, 3);

    addToShip(unit, ship);
    assertEquals(1, data.getShips().size());

    EresseaRelationFactory executor = new EresseaRelationFactory(data.rules);
    executor.processOrders(region0);

    assertEquals(1, data.getShips().size());

    Ship tempShip = ship.getTempShips().toArray(new Ship[] {})[1];
    assertNull(data.getShip(tempShip.getID()));

    unit.clearOrders();
    executor.processOrders(region0);

    assertEquals(1, data.getShips().size());
    assertEquals(0, ship.getTempShips().size());
  }

  private void addToShip(Unit unit0, Ship ship) {
    unit0.setShip(ship);
    if (ship.getUnits().size() == 1) {
      ship.setOwner(unit0);
      ship.setOwnerUnit(unit0);
    }
  }

  private void changeAmount(Ship ship, int amount) {
    ship.setAmount(amount);
    ship.setSize(amount * ship.getShipType().getMaxSize());
  }
}
