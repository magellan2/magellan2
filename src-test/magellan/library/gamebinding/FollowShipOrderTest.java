// class magellan.library.gamebinding.FollowShipOrderTest
// created on Jan 12, 2022
//
// Copyright 2003-2022 by magellan project team
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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Order;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.relation.FollowShipRelation;
import magellan.library.relation.MovementRelation;
import magellan.library.relation.UnitRelation;
import magellan.test.GameDataBuilder;

public class FollowShipOrderTest {

  private GameDataBuilder builder;
  private GameData data;
  private Unit unit;
  private Region region0;
  private EresseaRelationFactory relationFactory;
  private Ship ship0;
  private Region[] regions;
  private Ship ship1;
  private Unit unit1;

  @Before
  public void setUp() throws Exception {
    builder = new GameDataBuilder();
    data = builder.createSimpleGameData();
    unit = data.getUnits().iterator().next();
    ship0 = builder.addShip(data, unit.getRegion(), "sh1", "Boot", "Ship 1", 5);
    region0 = data.getRegions().iterator().next();
    relationFactory = ((EresseaRelationFactory) data.getGameSpecificStuff().getRelationFactory());
    relationFactory.stopUpdating();
  }

  private void setupShips() {
    regions = new Region[10];
    for (int x = 1; x <= 5; ++x) {
      regions[x - 1] = builder.addRegion(data, x + " 0", "Region_" + x + "_0", "Ozean", 2 * x);
      regions[x - 1 + 5] = builder.addRegion(data, x + " 1", "Region_" + x + "_1", "Ozean", 2 * x + 1);
    }
    unit.clearOrders();
    unit.setShip(ship0);
    ship0.setOwner(unit);
    ship1 = builder.addShip(data, region0, "boot", "Boot", "Boot", 5); // MM range 3
    unit1 = builder.addUnit(data, "Captain 2", region0);
    unit1.setShip(ship1);
    ship1.setOwner(unit1);
    unit1.clearOrders();
  }

  @Test
  public final void testExecuteShip() {
    setupShips();

    unit.addOrder("ROUTE o o o PAUSE o");
    unit1.addOrder("FOLGE SCHIFF " + ship0.getID());

    Order order = unit1.getOrders2().get(0);
    assertTrue(order instanceof FollowShipOrder);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit1, 0);

    order = unit.getOrders2().get(0);
    assertTrue(order instanceof MovementOrder);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 0);

    List<UnitRelation> relations = unit.getRelations();
    assertEquals(1, relations.size());
    MovementRelation move = (MovementRelation) relations.get(0);
    assertSame(unit, move.origin);
    assertSame(unit, move.source);
    assertEquals(unit, move.getTransporter());
    assertMovement(move, regions[2], region0, regions[0], regions[1], regions[2], regions[2], regions[3]);
    assertEquals(false, move.unknown);
    assertNull(move.invalidRegion);

    relations = unit1.getRelations();
    assertEquals(2, relations.size());
    FollowShipRelation frelation = (FollowShipRelation) relations.get(0);
    assertSame(unit1, frelation.origin);
    assertSame(unit1, frelation.source);
    assertEquals(ship0, frelation.target);

    MovementRelation move2 = (MovementRelation) relations.get(1);
    assertSame(unit1, frelation.origin);
    assertSame(unit1, frelation.source);
    assertEquals(unit, move.getTransporter());
    assertMovement(move2, regions[2], region0, regions[0], regions[1], regions[2], regions[2], regions[3]);
    assertEquals(false, move.unknown);
    assertNull(move2.invalidRegion);
  }

  @Test
  public final void testExecuteShipSlowFollowsFast() {
    setupShips();
    ship0 = builder.addShip(data, region0, "lang", "Langboot", "Boot", 5); // range 4
    unit.setShip(ship0);
    ship0.setOwner(unit);

    unit.addOrder("ROUTE o o o o PAUSE o");
    unit1.addOrder("FOLGE SCHIFF " + ship0.getID());

    Order order = unit1.getOrders2().get(0);
    assertTrue(order instanceof FollowShipOrder);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit1, 0);

    order = unit.getOrders2().get(0);
    assertTrue(order instanceof MovementOrder);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 0);

    List<UnitRelation> relations = unit.getRelations();
    assertEquals(1, relations.size());
    MovementRelation move = (MovementRelation) relations.get(0);
    assertSame(unit, move.origin);
    assertSame(unit, move.source);
    assertEquals(unit, move.getTransporter());
    assertMovement(move, regions[3], region0, regions[0], regions[1], regions[2], regions[3], regions[3], regions[4]);
    assertEquals(false, move.unknown);
    assertNull(move.invalidRegion);

    relations = unit1.getRelations();
    assertEquals(2, relations.size());
    FollowShipRelation frelation = (FollowShipRelation) relations.get(0);
    assertSame(unit1, frelation.origin);
    assertSame(unit1, frelation.source);
    assertEquals(ship0, frelation.target);

    MovementRelation move2 = (MovementRelation) relations.get(1);
    assertSame(unit1, frelation.origin);
    assertSame(unit1, frelation.source);
    assertEquals(unit, move.getTransporter());
    assertMovement(move2, regions[2], region0, regions[0], regions[1], regions[2], regions[3], regions[3], regions[4]);
    assertEquals(false, move.unknown);
    assertNull(move2.invalidRegion);

    assertEquals(move.getFutureMovement().get(1), move.getFutureMovement().get(0));
    assertNotEquals(move2.getFutureMovement().get(1), move2.getFutureMovement().get(0));
  }

  @Test
  public final void testExecuteShipFastFollowsSlow() {
    setupShips();
    ship1 = builder.addShip(data, region0, "lang", "Langboot", "Boot", 5); // range 4
    unit1.setShip(ship1);
    ship1.setOwner(unit1);

    unit.addOrder("ROUTE o o o PAUSE o o");
    unit1.addOrder("FOLGE SCHIFF " + ship0.getID());

    Order order = unit1.getOrders2().get(0);
    assertTrue(order instanceof FollowShipOrder);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit1, 0);

    order = unit.getOrders2().get(0);
    assertTrue(order instanceof MovementOrder);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 0);

    List<UnitRelation> relations = unit.getRelations();
    assertEquals(1, relations.size());
    MovementRelation move = (MovementRelation) relations.get(0);
    assertSame(unit, move.origin);
    assertSame(unit, move.source);
    assertEquals(unit, move.getTransporter());
    assertMovement(move, regions[2], region0, regions[0], regions[1], regions[2], regions[2], regions[3], regions[4]);
    assertEquals(false, move.unknown);
    assertNull(move.invalidRegion);

    relations = unit1.getRelations();
    assertEquals(2, relations.size());
    FollowShipRelation frelation = (FollowShipRelation) relations.get(0);
    assertSame(unit1, frelation.origin);
    assertSame(unit1, frelation.source);
    assertEquals(ship0, frelation.target);

    MovementRelation move2 = (MovementRelation) relations.get(1);
    assertSame(unit1, frelation.origin);
    assertSame(unit1, frelation.source);
    assertEquals(unit, move.getTransporter());
    assertMovement(move2, regions[2], region0, regions[0], regions[1], regions[2], regions[2], regions[3], regions[4]);
    assertEquals(false, move.unknown);
    assertNull(move2.invalidRegion);

    assertEquals(move.getFutureMovement().get(1), move.getFutureMovement().get(0));
    // assertNotEquals(move2.getFutureMovement().get(1), move2.getFutureMovement().get(0));
  }

  protected void assertMovement(MovementRelation movement, Region destination, Object... path) {
    assertMovement(movement, destination.getCoordinate(), path);
  }

  protected void assertMovement(MovementRelation movement, CoordinateID destination,
      Object... path) {
    assertEquals(path.length, movement.getMovement().size());
    assertRegionOrCoordinate(destination, movement.getDestination());
    for (int i = 0; i < path.length; ++i) {
      assertRegionOrCoordinate(path[i], movement.getMovement().get(i));
    }

  }

  protected void assertRegionOrCoordinate(Object expected, CoordinateID region) {
    if (expected instanceof Region) {
      assertEquals(((Region) expected).getCoordinate(), region);
    } else if (expected instanceof CoordinateID) {
      assertEquals(expected, region);
    } else {
      fail("no coordinate");
    }

  }

}
