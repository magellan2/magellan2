// class magellan.library.gamebinding.MovementOrderTest
// created on Jun 20, 2012
//
// Copyright 2003-2012 by magellan project team
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
import static org.junit.Assert.assertNotNull;
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
import magellan.library.relation.MovementRelation;
import magellan.library.relation.UnitRelation;
import magellan.library.tasks.Problem;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

/**
 * @author stm
 * @version 1.0, Jun 20, 2012
 */
public class MovementOrderTest extends MagellanTestWithResources {

  protected static final CoordinateID coord1 = CoordinateID.create(1, 0);
  protected static final CoordinateID coord2 = CoordinateID.create(2, 0);
  protected static final CoordinateID coord3 = CoordinateID.create(3, 0);
  protected static final CoordinateID coord4 = CoordinateID.create(4, 0);

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
   * Test method for
   * {@link magellan.library.gamebinding.MovementOrder#execute(magellan.library.gamebinding.ExecutionState, magellan.library.GameData, magellan.library.Unit, int)}
   * .
   */
  @Test
  public final void testExecuteNormal() {
    Region region1 = builder.addRegion(data, "1 0", "Region_1_0", "Ebene", 0);
    Region region2 = builder.addRegion(data, "2 0", "Region_2_0", "Ebene", 0);
    unit.clearOrders();
    unit.addOrder("NACH o o");

    Order order = unit.getOrders2().get(0);
    assertTrue(order instanceof MovementOrder);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 0);
    List<UnitRelation> relations = unit.getRelations();
    assertTrue(relations.size() == 1);
    MovementRelation relation = (MovementRelation) relations.get(0);
    assertEquals(0, relation.line);
    assertSame(unit, relation.origin);
    assertSame(unit, relation.source);
    assertEquals(unit, relation.getTransporter());
    assertMovement(relation, 1, region0, region1, region2);
    assertEquals(false, relation.unknown);
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.MovementOrder#execute(magellan.library.gamebinding.ExecutionState, magellan.library.GameData, magellan.library.Unit, int)}
   * .
   */
  @Test
  public final void testExecuteMoreNormal() {
    Region region1 = builder.addRegion(data, "1 0", "Region_1_0", "Ebene", 0);
    Region region2 = builder.addRegion(data, "2 0", "Region_2_0", "Ebene", 0);
    unit.clearOrders();
    unit.addOrder("NACH o");

    Order order = unit.getOrders2().get(0);
    assertTrue(order instanceof MovementOrder);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 0);
    List<UnitRelation> relations = unit.getRelations();
    assertTrue(relations.size() == 1);
    MovementRelation relation = (MovementRelation) relations.get(0);
    assertEquals(0, relation.line);
    assertSame(unit, relation.origin);
    assertSame(unit, relation.source);
    assertEquals(unit, relation.getTransporter());
    assertMovement(relation, 1, region0, region1);
    assertEquals(false, relation.unknown);
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.MovementOrder#execute(magellan.library.gamebinding.ExecutionState, magellan.library.GameData, magellan.library.Unit, int)}
   * .
   */
  @Test
  public final void testExecuteRiding() {
    Region region1 = builder.addRegion(data, "1 0", "Region_1_0", "Ebene", 0);
    Region region2 = builder.addRegion(data, "2 0", "Region_2_0", "Ebene", 0);
    unit.clearOrders();
    unit.addOrder("NACH o o o");
    builder.addItem(data, unit, "Pferd", 1);
    builder.addSkill(unit, "Reiten", 1);

    Order order = unit.getOrders2().get(0);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 0);
    List<UnitRelation> relations = unit.getRelations();
    MovementRelation relation = (MovementRelation) relations.get(0);
    assertEquals(unit, relation.getTransporter());
    assertMovement(relation, region2, region0, region1, region2, coord3);
    assertEquals(true, relation.unknown);
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.MovementOrder#execute(magellan.library.gamebinding.ExecutionState, magellan.library.GameData, magellan.library.Unit, int)}
   * .
   */
  @Test
  public final void testExecutePause() {
    Region region1 = builder.addRegion(data, "1 0", "Region_1_0", "Ebene", 0);
    Region region2 = builder.addRegion(data, "2 0", "Region_2_0", "Ebene", 0);
    unit.clearOrders();
    unit.addOrder("ROUTE o PAUSE o o");
    builder.addItem(data, unit, "Pferd", 1);
    builder.addSkill(unit, "Reiten", 1);

    Order order = unit.getOrders2().get(0);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 0);
    List<UnitRelation> relations = unit.getRelations();
    MovementRelation relation = (MovementRelation) relations.get(0);
    assertEquals(unit, relation.getTransporter());
    assertMovement(relation, 1, region0, region1, region1, region2, coord3);
    assertEquals(true, relation.unknown);
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.MovementOrder#execute(magellan.library.gamebinding.ExecutionState, magellan.library.GameData, magellan.library.Unit, int)}
   * .
   */
  @Test
  public final void testExecutePause2() {
    Region region1 = builder.addRegion(data, "1 0", "Region_1_0", "Ebene", 0);
    Region region2 = builder.addRegion(data, "2 0", "Region_2_0", "Ebene", 0);
    unit.clearOrders();
    unit.addOrder("ROUTE o o PAUSE o");
    builder.addItem(data, unit, "Pferd", 1);
    builder.addSkill(unit, "Reiten", 1);

    Order order = unit.getOrders2().get(0);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 0);
    List<UnitRelation> relations = unit.getRelations();
    MovementRelation relation = (MovementRelation) relations.get(0);
    assertEquals(unit, relation.getTransporter());
    assertMovement(relation, 2, region0, region1, region2, region2, coord3);
    assertEquals(true, relation.unknown);
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.MovementOrder#execute(magellan.library.gamebinding.ExecutionState, magellan.library.GameData, magellan.library.Unit, int)}
   * .
   */
  @Test
  public final void testExecuteWalkOcean() {
    Region region1 = builder.addRegion(data, "1 0", "Region_1_0", "Ozean", 0);
    unit.clearOrders();
    unit.addOrder("ROUTE o o");

    Order order = unit.getOrders2().get(0);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 0);
    List<UnitRelation> relations = unit.getRelations();
    MovementRelation relation = (MovementRelation) relations.get(0);
    assertMovement(relation, region0, region0, region1, coord2);
    assertNotNull(relation.problem);
    assertNull(order.getProblem());
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.MovementOrder#execute(magellan.library.gamebinding.ExecutionState, magellan.library.GameData, magellan.library.Unit, int)}
   * .
   */
  @Test
  public final void testExecuteOverloaded() {
    Region region1 = builder.addRegion(data, "1 0", "Region_1_0", "Ebene", 0);
    unit.clearOrders();
    unit.addOrder("ROUTE o o");
    builder.addItem(data, unit, "Stein", 9999);

    Order order = unit.getOrders2().get(0);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 0);
    List<UnitRelation> relations = unit.getRelations();
    MovementRelation relation = (MovementRelation) relations.get(0);
    assertMovement(relation, region0, region0, region1, coord2);
    assertNull(relation.problem);
    assertNull(order.getProblem());
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.MovementOrder#execute(magellan.library.gamebinding.ExecutionState, magellan.library.GameData, magellan.library.Unit, int)}
   * .
   */
  @Test
  public final void testExecuteShip() {
    Region region1 = builder.addRegion(data, "1 0", "Region_1_0", "Ozean", 0);
    Region region2 = builder.addRegion(data, "2 0", "Region_2_0", "Ozean", 0);
    Region region3 = builder.addRegion(data, "3 0", "Region_3_0", "Ozean", 0);
    Region region4 = builder.addRegion(data, "4 0", "Region_4_0", "Ozean", 0);
    unit.clearOrders();
    unit.addOrder("NACH o o o o");
    Ship ship = builder.addShip(data, region0, "boot", "Boot", "Boot", 5); // range 3
    unit.setShip(ship);
    ship.setOwner(unit);

    Order order = unit.getOrders2().get(0);
    assertTrue(order instanceof MovementOrder);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 0);
    List<UnitRelation> relations = unit.getRelations();
    assertTrue(relations.size() == 1);
    MovementRelation relation = (MovementRelation) relations.get(0);
    assertSame(unit, relation.origin);
    assertSame(unit, relation.source);
    assertEquals(unit, relation.getTransporter());
    assertMovement(relation, region3, region0, region1, region2, region3, region4);
    assertEquals(false, relation.unknown);
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.MovementOrder#execute(magellan.library.gamebinding.ExecutionState, magellan.library.GameData, magellan.library.Unit, int)}
   * .
   */
  @Test
  public final void testExecuteShipNoOcean() {
    Region region1 = builder.addRegion(data, "1 0", "Region_1_0", "Ebene", 0);
    unit.clearOrders();
    unit.addOrder("NACH o o");
    Ship ship = builder.addShip(data, region0, "boot", "Boot", "Boot", 5);
    unit.setShip(ship);
    ship.setOwner(unit);

    Order order = unit.getOrders2().get(0);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 0);
    List<UnitRelation> relations = unit.getRelations();
    MovementRelation relation = (MovementRelation) relations.get(0);

    assertMovement(relation, region0, region0, region1, coord2);
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.MovementOrder#execute(magellan.library.gamebinding.ExecutionState, magellan.library.GameData, magellan.library.Unit, int)}
   * .
   */
  @Test
  public final void testExecuteShipLandEnds() {
    Region region1 = builder.addRegion(data, "1 0", null, "Ozean", 0);
    Region region2 = builder.addRegion(data, "2 0", null, "Ebene", 0);
    unit.clearOrders();
    unit.addOrder("NACH o o o");
    Ship ship = builder.addShip(data, region0, "tri", "Trireme", "Boot", 200);
    unit.setShip(ship);
    ship.setOwner(unit);

    Order order = unit.getOrders2().get(0);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 0);
    List<UnitRelation> relations = unit.getRelations();
    MovementRelation relation = (MovementRelation) relations.get(0);

    assertMovement(relation, region2, region0, region1, region2, coord3);
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.MovementOrder#execute(magellan.library.gamebinding.ExecutionState, magellan.library.GameData, magellan.library.Unit, int)}
   * .
   */
  @Test
  public final void testExecuteShipPassenger() {
    Region region1 = builder.addRegion(data, "1 0", "Region_1_0", "Ozean", 0);
    Region region2 = builder.addRegion(data, "2 0", "Region_2_0", "Ozean", 0);
    unit.clearOrders();
    unit.addOrder("NACH o o");
    Ship ship = builder.addShip(data, region0, "boot", "Boot", "Boot", 5);
    unit.setShip(ship);
    ship.setOwner(unit);
    Unit unit2 = builder.addUnit(data, "Passenger", region0);
    unit2.setShip(ship);

    Order order = unit.getOrders2().get(0);
    assertTrue(order instanceof MovementOrder);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 0);
    List<UnitRelation> relations = unit.getRelations();
    assertEquals(1, relations.size());
    relations = unit2.getRelations();
    assertEquals(1, relations.size());
    MovementRelation relation = (MovementRelation) relations.get(0);
    assertSame(unit, relation.origin);
    assertSame(unit2, relation.source);
    assertEquals(unit, relation.getTransporter());
    assertMovement(relation, region2, region0, region1, region2);
    assertEquals(false, relation.unknown);
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.MovementOrder#execute(magellan.library.gamebinding.ExecutionState, magellan.library.GameData, magellan.library.Unit, int)}
   * .
   */
  @Test
  public final void testExecuteShipPassengerLeaving() {
    builder.addRegion(data, "1 0", "Region_1_0", "Ozean", 0);
    builder.addRegion(data, "2 0", "Region_2_0", "Ozean", 0);
    unit.clearOrders();
    unit.addOrder("NACH o o");
    Ship ship = builder.addShip(data, region0, "boot", "Boot", "Boot", 5);
    unit.setShip(ship);
    ship.setOwner(unit);
    Unit unit2 = builder.addUnit(data, "Passenger", region0);
    unit2.setShip(ship);
    unit2.clearOrders();
    unit2.addOrder("NACH w");

    relationFactory.processOrders(region0);

    // unit 2 has its own move order ==> implicit leave ==> not on ship
    List<UnitRelation> relations = unit2.getRelations();
    assertEquals(2, relations.size());
    for (UnitRelation relation : relations) {
      if (relation instanceof MovementRelation) {
        assertEquals(unit2, relation.origin);
      }
    }
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.MovementOrder#execute(magellan.library.gamebinding.ExecutionState, magellan.library.GameData, magellan.library.Unit, int)}
   * .
   */
  @Test
  public final void testExecuteShipPassengerLeavingViceVersa() {
    builder.addRegion(data, "1 0", "Region_1_0", "Ozean", 0);
    builder.addRegion(data, "2 0", "Region_2_0", "Ozean", 0);
    unit.clearOrders();
    unit.addOrder("NACH w");
    Ship ship = builder.addShip(data, region0, "boot", "Boot", "Boot", 5);
    unit.setShip(ship);
    Unit unit2 = builder.addUnit(data, "Passenger", region0);
    unit2.setShip(ship);
    ship.setOwner(unit2);
    unit2.clearOrders();
    unit2.addOrder("NACH o o");

    relationFactory.processOrders(region0);

    // unit 2 has its own move order ==> implicit leave ==> not on ship
    List<UnitRelation> relations = unit.getRelations();
    assertEquals(2, relations.size());
    for (UnitRelation relation : relations) {
      if (relation instanceof MovementRelation) {
        assertEquals(unit, relation.origin);
      }
    }
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.MovementOrder#execute(magellan.library.gamebinding.ExecutionState, magellan.library.GameData, magellan.library.Unit, int)}
   * .
   */
  @Test
  public final void testExecuteShipPassengerAndTransported() {
    builder.addRegion(data, "1 0", "Region_1_0", "Ozean", 0);
    builder.addRegion(data, "2 0", "Region_2_0", "Ozean", 0);

    Unit unit2 = builder.addUnit(data, "Unit 2", region0);
    Unit unit3 = builder.addUnit(data, "Unit 3", region0);

    Ship ship = builder.addShip(data, region0, "boot", "Boot", "Boot", 5);

    testExecuteShipPassengerAndTransported(ship, unit, unit2, unit3);
    testExecuteShipPassengerAndTransported(ship, unit, unit3, unit2);
    testExecuteShipPassengerAndTransported(ship, unit2, unit, unit3);
    testExecuteShipPassengerAndTransported(ship, unit2, unit3, unit);
    testExecuteShipPassengerAndTransported(ship, unit3, unit, unit2);
    testExecuteShipPassengerAndTransported(ship, unit3, unit2, unit);
  }

  /**
   *
   */
  @Test
  public final void testFollowAndMove() {
    builder.addUnit(data, "u2", "U2", unit.getFaction(), region0);

    unit.clearOrders();
    unit.addOrder("NACH o");
    unit.addOrder("FOLGE EINHEIT u2");

    relationFactory.processOrders(region0);
    Order order = unit.getOrders2().get(0);
    Problem warning = order.getProblem();

    assertEquals("FOLGE- und NACH-Befehle", warning.getMessage());
  }

  /**
   *
   */
  @Test
  public final void testFollowCycle() {
    Unit unit2 = builder.addUnit(data, "u2", "U2", unit.getFaction(), region0);
    Unit unit3 = builder.addUnit(data, "u3", "Unit 3", unit.getFaction(), region0);

    unit.clearOrders();
    unit.addOrder("NACH o");

    unit2.clearOrders();
    unit2.addOrder("FOLGE EINHEIT 1");
    unit2.addOrder("FOLGE EINHEIT u3");
    unit3.addOrder("FOLGE EINHEIT u2");

    relationFactory.processOrders(region0);
    Problem warning = unit.getOrders2().get(0).getProblem();
    assertEquals("Einheit folgt sich selbst", warning.getMessage());
    warning = unit2.getOrders2().get(1).getProblem();
    assertEquals("Mehr als ein FOLGE-Befehl", warning.getMessage());
  }

  private void
      testExecuteShipPassengerAndTransported(Ship ship, Unit unit1, Unit unit2, Unit unit3) {
    unit1.clearOrders();
    unit1.addOrder("NACH o o");

    unit1.setShip(ship);

    unit2.clearOrders();
    unit2.addOrder("NACH w");
    unit2.setShip(ship);

    unit3.clearOrders();
    unit3.addOrder("FAHRE " + unit2.getID());
    unit2.addOrder("TRANSPORTIERE " + unit3.getID());
    unit3.setShip(ship);

    ship.setOwner(unit1);

    relationFactory.processOrders(region0);

    // unit is transported ==> implicit leave ==> not on ship
    List<UnitRelation> relations = unit3.getRelations();
    assertEquals(3, relations.size());
    for (UnitRelation relation : relations) {
      if (relation instanceof MovementRelation) {
        assertEquals(unit2, relation.origin);
      }
    }
  }

  protected void assertMovement(MovementRelation movement, int destination, Object... regions) {
    assertEquals(regions.length, movement.getMovement().size());
    assertRegionOrCoordinate(regions[destination], movement.getDestination());
    assertEquals(destination + 1, movement.getInitialMovement().size());
    for (int i = 0; i <= destination; ++i) {
      assertRegionOrCoordinate(regions[i], movement.getInitialMovement().get(i));
    }
    if (regions.length - destination > 1) {
      assertEquals(regions.length - destination, movement.getFutureMovement().size());
      for (int i = 0; i < regions.length - destination; ++i) {
        assertRegionOrCoordinate(regions[i + destination], movement.getFutureMovement().get(i));
      }
    } else {
      assertEquals(0, movement.getFutureMovement().size());
    }

  }

  protected void assertMovement(MovementRelation movement, Region destination, Object... regions) {
    assertMovement(movement, destination.getCoordinate(), regions);
  }

  protected void assertMovement(MovementRelation movement, CoordinateID destination,
      Object... regions) {
    assertEquals(regions.length, movement.getMovement().size());
    assertRegionOrCoordinate(destination, movement.getDestination());
    for (int i = 0; i < regions.length; ++i) {
      assertRegionOrCoordinate(regions[i], movement.getMovement().get(i));
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
