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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import magellan.library.Building;
import magellan.library.Faction;
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
  public void testGiveCommand() {
    Ship ship = builder.addShip(data, region0, "ship", "Trireme", "Trireme", 200);
    Unit unit2 = builder.addUnit(data, "two", "Zwei", faction0, region0);

    builder.addTo(unit, ship);
    builder.addTo(unit2, ship);

    unit.clearOrders();
    unit.addOrder("GIB two KOMMANDO");

    process(region0);

    assertEquals(unit2, ship.getModifiedOwnerUnit());
  }

  @Test
  public void testGiveGiveCommand() {
    Ship ship = builder.addShip(data, region0, "ship", "Trireme", "Trireme", 200);
    Unit unit2 = builder.addUnit(data, "two", "Zwei", faction0, region0);
    Unit unit3 = builder.addUnit(data, "tree", "Drei", faction0, region0);

    builder.addTo(unit, ship);
    builder.addTo(unit2, ship);
    builder.addTo(unit3, ship);

    unit.clearOrders();
    unit.addOrder("GIB two KOMMANDO");
    unit.addOrder("GIB tree KOMMANDO");

    process(region0);

    assertEquals(unit2, ship.getModifiedOwnerUnit());
  }

  @Test
  public void testGiveGiveCommandOk() {
    Ship ship = builder.addShip(data, region0, "ship", "Trireme", "Trireme", 200);
    Unit unit2 = builder.addUnit(data, "two", "Zwei", faction0, region0);
    Unit unit3 = builder.addUnit(data, "tree", "Drei", faction0, region0);

    builder.addTo(unit, ship);
    builder.addTo(unit2, ship);
    builder.addTo(unit3, ship);

    unit.clearOrders();
    unit.addOrder("GIB two KOMMANDO");
    unit2.addOrder("GIB tree KOMMANDO");

    process(region0);

    assertEquals(unit3, ship.getModifiedOwnerUnit());
  }

  @Test
  public void testEnterGiveCommand() {
    Ship ship = builder.addShip(data, region0, "ship", "Trireme", "Trireme", 200);
    Ship ship2 = builder.addShip(data, region0, "sh2", "Trireme", "Trireme", 200);

    Unit unit2 = builder.addUnit(data, "two", "Zwei", faction0, region0);
    Unit unit3 = builder.addUnit(data, "tree", "Drei", faction0, region0);

    builder.addTo(unit, ship);
    builder.addTo(unit2, ship);
    builder.addTo(unit3, ship);

    unit.clearOrders();
    unit.addOrder("BETRETE SCHIFF sh2");
    unit.addOrder("GIB tree KOMMANDO");

    process(region0);

    assertEquals(unit, ship2.getModifiedOwnerUnit());
    assertEquals(ship2, unit.getModifiedShip());
    assertEquals(ship, unit3.getModifiedShip());
    assertEquals(unit2, ship.getModifiedOwnerUnit());
  }

  @Test
  public void testGiveLeave() {
    Unit unit2 = builder.addUnit(data, "two", "Unit2", faction0, region0);
    Unit unit3 = builder.addUnit(data, "tree", "Unit3", faction0, region0);

    Building b1 = builder.addBuilding(data, region0, "b1", "Burg", "Burg 1", 10);

    unit.addOrder("GIB two KOMMANDO");
    unit2.addOrder("VERLASSE");
    builder.addTo(unit, b1);

    builder.addTo(unit, b1);
    builder.addTo(unit2, b1);
    builder.addTo(unit3, b1);

    EresseaRelationFactory executor = new EresseaRelationFactory(data.rules);
    executor.processOrders(region0);

    assertEquals(unit3, b1.getModifiedOwnerUnit());
  }

  @Test
  public void testGiveLeave2() {
    Unit unit2 = builder.addUnit(data, "two", "Unit2", faction0, region0);
    Unit unit3 = builder.addUnit(data, "tree", "Unit3", faction0, region0);

    Building b1 = builder.addBuilding(data, region0, "b1", "Burg", "Burg 1", 10);

    unit.addOrder("GIB tree KOMMANDO");
    unit.addOrder("VERLASSE");
    builder.addTo(unit, b1);

    builder.addTo(unit, b1);
    builder.addTo(unit2, b1);
    builder.addTo(unit3, b1);

    EresseaRelationFactory executor = new EresseaRelationFactory(data.rules);
    executor.processOrders(region0);

    assertEquals(unit3, b1.getModifiedOwnerUnit());
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
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 1);

    assertEquals("Unit_1 (1) besitzt kein Schiff", order.getProblem().getMessage());

    Ship ship = builder.addShip(data, region0, "sing", "Trireme", "Trireme", 200);
    Ship ship2 = builder.addShip(data, region0, "conv", "Trireme", "Konvoi", 200);

    // transfer ship from fleet to fleet
    builder.addTo(unit, ship);
    builder.addTo(unit2, ship2);
    changeAmount(ship, 2);
    changeAmount(ship2, 2);
    assertEquals(400, ship.getModifiedSize());
    assertEquals(400, ship2.getModifiedSize());

    order.setProblem(null);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 1);
    assertNull(order.getProblem());

    List<UnitRelation> relations = unit.getRelations();
    assertEquals(2, relations.size());
    ShipTransferRelation str = (ShipTransferRelation) relations.get(0);
    InterUnitRelation iur = (InterUnitRelation) relations.get(1);
    List<UnitRelation> shipRelations = ship.getRelations();
    assertEquals(1, shipRelations.size());
    shipRelations = ship2.getRelations();
    assertEquals(1, shipRelations.size());

    assertEquals(1, str.line);
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

    process(region0);
    // order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 1);
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

    assertEquals(1, str.line);
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

    builder.addTo(unit, ship);

    Order order = unit.getOrders2().get(0);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 1);

    assertNull(order.getProblem());
    assertEquals(1, data.getShips().size());
    assertEquals(1, ship.getModifiedAmount());
    assertEquals(1, ship.getTempShips().size());
    Ship ship2 = ship.getTempShips().toArray(new Ship[] {})[0];
    assertEquals("-sing", ship2.getID().toString());
    assertEquals(0, ship2.getAmount());
    assertEquals(2, ship2.getModifiedAmount());
    assertEquals(0, ship2.getSize());
    assertEquals(2 * ship.getShipType().getMaxSize(), ship2.getModifiedSize());
    assertEquals(0, ship2.getCargo());
    assertEquals(ship.getShoreId(), ship2.getShoreId());
    assertEquals(0, ship2.units().size());
    assertEquals(3, ship2.getDamageRatio());
    assertEquals(region0, ship.getRegion());
    assertEquals(ship.getType(), ship2.getType());
    assertEquals(ship.getSpeed(), ship2.getSpeed());
    assertEquals(0, ship2.getCapacity());
    assertEquals(2 * ship.getModifiedMaxCapacity(), ship2.getModifiedMaxCapacity());
  }

  // TODO: Anleitung: GIB 0 ALLES Schiff -> kein Umsteigen!
  // TODO: Bug? GIB 0 ALLES Schiff -> unit auf Konvoi ohne Schiffe
  //
  // TODO keine Übergabe an leere Flotte auf see
  // TODO GIB xyz ALLES SCHIF und xyz existiert nicht --> Warnung!
  //
  // TODO nodirection wird zu direction bei übergabe

  @Test
  public void testGive0AllShips() {
    unit.clearOrders();
    unit.addOrder("GIB 0 2 SCHIFF");
    Ship ship = builder.addShip(data, region0, "sing", "Trireme", "Trireme", 200);
    changeAmount(ship, 2);

    builder.addTo(unit, ship);

    Order order = unit.getOrders2().get(0);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 1);

    assertNull(order.getProblem());
    assertEquals(1, data.getShips().size());
    assertEquals(0, ship.getModifiedAmount());
    assertEquals(1, ship.getTempShips().size());
    Ship ship2 = ship.getTempShips().get(0);
    assertEquals(2, ship2.getModifiedAmount());

    assertEquals(0, ship2.units().size());

    // FIXME assertEquals(0, ship.units().size()); // ???
  }

  @Test
  public void testGiveAllShips() {
    Ship ship = builder.addShip(data, region0, "conv", "Trireme", "Trireme", 200);

    builder.addTo(unit, ship);

    unit.clearOrders();
    unit.addOrder("GIB oth 1 SCHIFF"); // give all ships

    process(region0);

    assertEquals(1, ship.getTempShips().size());
    assertEquals(0, ship.getModifiedAmount());
    assertEquals(ship, unit.getModifiedShip());
    // FIXME assertNull(unit.getModifiedShip()); // ???
  }

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

    builder.addTo(unit, ship);
    assertEquals(1, data.getShips().size());

    process(region0);

    assertEquals(1, data.getShips().size());
    assertEquals(2, ship.getTempShips().size());

    Ship tempShip = ship.getTempShips().toArray(new Ship[] {})[1];
    assertNull(data.getShip(tempShip.getID()));

    unit.clearOrders();

    process(region0);

    assertEquals(1, data.getShips().size());
    assertEquals(0, ship.getTempShips().size());
  }

  /**
  *
  */
  @Test
  public void testCreateTooManyShips() {
    unit.clearOrders();
    unit.addOrder("GIB 0 1 SCHIFF");
    unit.addOrder("GIB 0 1 SCHIFF");
    unit.addOrder("GIB 0 1 SCHIFF");
    Ship ship = builder.addShip(data, region0, "sing", "Trireme", "Trireme", 200);
    changeAmount(ship, 2);

    builder.addTo(unit, ship);
    assertEquals(1, data.getShips().size());

    process(region0);

    assertEquals(1, data.getShips().size());
    assertEquals(2, ship.getTempShips().size());
    assertEquals(0, ship.getModifiedAmount());
    assertNotNull(unit.getOrders2().get(2).getProblem());
  }

  @Test
  public void testCreateTooManyShips2() {
    Ship ship = builder.addShip(data, region0, "sing", "Trireme", "Trireme", 200);
    Ship ship2 = builder.addShip(data, region0, "two", "Trireme", "Trireme", 200);
    changeAmount(ship, 1);

    Unit other = builder.addUnit(data, "oth", "Othership", faction0, region0);

    unit.clearOrders();
    unit.addOrder("GIB oth 1 SCHIFF");
    unit.addOrder("GIB oth 1 SCHIFF");

    builder.addTo(unit, ship);
    builder.addTo(other, ship2);
    assertEquals(2, data.getShips().size());

    process(region0);

    assertEquals(2, data.getShips().size());
    assertEquals(0, ship.getTempShips().size());
    assertEquals(0, ship.getModifiedAmount());
    assertEquals(2, ship2.getModifiedAmount());
    assertNotNull(unit.getOrders2().get(1).getProblem());
  }

  @Test
  public void testCreateTempTemp() {
    Ship ship = builder.addShip(data, region0, "sing", "Trireme", "Trireme", 200);
    Unit same = builder.addUnit(data, "same", "Sameship", faction0, region0);

    changeAmount(ship, 2);
    unit.clearOrders();
    unit.addOrder("GIB same 1 SCHIFF");
    same.addOrder("GIB 0 1 SCHIFF");

    builder.addTo(unit, ship);
    builder.addTo(same, ship);

    process(region0);

    assertEquals(2, ship.getTempShips().size());
    assertTrue(ship.getTempShips().get(0).getTempShips().isEmpty());
  }

  @Test
  public void testGiveToNonCaptain() {
    Ship ship = builder.addShip(data, region0, "conv", "Trireme", "Trireme", 200);
    Ship ship2 = builder.addShip(data, region0, "sing", "Trireme", "Trireme", 200);

    Region region1 = builder.addRegion(data, "1 0", "Nachbar", "Ebene", 1);

    Unit captain = builder.addUnit(data, "capt", "Captain", faction0, region0);
    Unit other = builder.addUnit(data, "oth", "Othership", faction0, region0);
    Unit same = builder.addUnit(data, "same", "Sameship", faction0, region0);
    Unit outside = builder.addUnit(data, "out", "Outside", faction0, region0);
    Unit elsewhere = builder.addUnit(data, "else", "Elsewhere", faction0, region1);
    Faction otherFaction = builder.addFaction(data, "foe", "Foes", "Menschen", 1);
    Unit foreign = builder.addUnit(data, "for", "Foreign Unit", otherFaction, region0);
    changeAmount(ship, 10);

    unit.clearOrders();
    unit.addOrder("GIB capt 1 SCHIFF"); // transfer 1
    unit.addOrder("GIB oth 1 SCHIFF"); // error
    unit.addOrder("GIB same 1 SCHIFF"); // 1 temp ship
    unit.addOrder("GIB out 1 SCHIFF"); // 1 temp ship
    unit.addOrder("GIB for 1 SCHIFF"); // error
    unit.addOrder("GIB else 1 SCHIFF"); // error
    unit.addOrder("GIB xxxx 1 SCHIFF"); // error, 1 temp ship

    builder.addTo(unit, ship);
    builder.addTo(same, ship);
    builder.addTo(captain, ship2);
    builder.addTo(other, ship2);

    process(region0);

    assertEquals(2, data.getShips().size());
    assertEquals(3, ship.getTempShips().size());
    assertEquals(0, ship2.getTempShips().size());
    assertEquals(6, ship.getModifiedAmount());
    assertEquals(2, ship2.getModifiedAmount());

    assertEquals(ship.getTempShips().toArray()[0], same.getModifiedShip());

    assertNull(unit.getOrders2().get(0).getProblem());
    assertNotNull(unit.getOrders2().get(1).getProblem());
    assertNull(unit.getOrders2().get(2).getProblem());
    assertNull(unit.getOrders2().get(3).getProblem());
    assertNotNull(unit.getOrders2().get(4).getProblem());
    assertNotNull(unit.getOrders2().get(5).getProblem());
    assertNotNull(unit.getOrders2().get(6).getProblem());
  }

  // TODO
  // GIB d2rk 1 SCHIFF
  //
  // GIB TEMP Lpg1 3 PERSONEN
  // GIB TEMP Lpg1 ALLES
  // MACHE TEMP Lpg1
  // BETRETE SCHIFF kbxv; GelbKaefer~11
  // ENDE
  // -> Temp auf kbxv!

  @Test
  public void testGiveGive() {
    Ship ship = builder.addShip(data, region0, "ship", "Trireme", "Trireme", 200);
    Ship ship2 = builder.addShip(data, region0, "sh2", "Trireme", "Trireme", 200);

    Unit same = builder.addUnit(data, "same", "Sameship", faction0, region0);
    Unit other = builder.addUnit(data, "oth", "Othership", faction0, region0);
    changeAmount(ship2, 1);

    builder.addTo(unit, ship);
    builder.addTo(same, ship);
    builder.addTo(other, ship2);

    // order: unit, same, other
    unit.clearOrders();
    unit.addOrder("BETRETE SCHIFF sh2"); // transfers command to same, order: same, other, unit
    other.addOrder("GIB 1 KOMMANDO"); // command of sh2 to unit, order unchanged
    same.addOrder("GIB 1 1 SCHIFF"); // transfer ship to sh2; moves same to ship2, order:
    // same, other, unit (for GIB); other, unit, same (for subsequent priorities)
    unit.addOrder("GIB oth 1 SCHIFF"); // transfer from ship2 sing to new temp ship, order:
                                       // other, unit, same (for subsequent priorities)

    process(region0);

    // expected: unit commands sh2 (2 ships), other commands new ship (1 ship)

    assertEquals(2, data.getShips().size());
    assertEquals(1, ship2.getTempShips().size());
    assertEquals(0, ship.getTempShips().size());
    assertEquals(0, ship.getModifiedAmount());
    assertEquals(1, ship2.getModifiedAmount());
    Ship tempShip = ship2.getTempShips().iterator().next();
    assertEquals(1, tempShip.getModifiedAmount());

    assertEquals(unit, ship2.getModifiedOwnerUnit());
    assertEquals(other, tempShip.getModifiedOwnerUnit());
    assertEquals(null, ship.getModifiedOwnerUnit());
    assertEquals(ship2, same.getModifiedShip());
  }

  @Test
  public void testGiveWrongType() {
    Ship ship = builder.addShip(data, region0, "ship", "Trireme", "Trireme", 200);
    Ship ship2 = builder.addShip(data, region0, "sh2", "Karavelle", "Karavelle", 250);

    Unit other = builder.addUnit(data, "oth", "Othership", faction0, region0);
    changeAmount(ship, 2);

    builder.addTo(unit, ship);
    builder.addTo(other, ship2);

    unit.clearOrders();
    unit.addOrder("GIB oth 1 SCHIFF"); // wrong ship type

    process(region0);

    assertNotNull(unit.getOrders2().get(0).getProblem());
    assertEquals(2, ship.getModifiedAmount());
  }

  @Test
  public void testGiveCursed() {
    Ship ship = builder.addShip(data, region0, "ship", "Trireme", "Trireme", 200);
    Ship ship2 = builder.addShip(data, region0, "sh2", "Trireme", "Anderes", 250);

    Unit other = builder.addUnit(data, "oth", "Othership", faction0, region0);

    builder.addTo(unit, ship);
    builder.addTo(other, ship2);

    ship2.setEffects(Collections.singletonList("Cursed"));

    unit.clearOrders();
    unit.addOrder("GIB oth 1 SCHIFF"); // other ship cursed
    other.clearOrders();
    other.addOrder("GIB 0 1 SCHIFF"); // ship cursed

    process(region0);

    assertNotNull(unit.getOrders2().get(0).getProblem());
    assertNotNull(other.getOrders2().get(0).getProblem());
    assertEquals(0, ship.getModifiedAmount());
    assertEquals(1, ship2.getModifiedAmount());
  }

  @Test
  public void testGiveAllOcean() {
    Region ocean = builder.addRegion(data, "1 0", null, "Ozean", 1);
    Ship ship = builder.addShip(data, ocean, "ship", "Trireme", "Trireme", 200);
    ship.setAmount(2);

    unit.setRegion(ocean);
    builder.addTo(unit, ship);
    unit.clearOrders();
    unit.addOrder("GIB 0 1 SCHIFF"); // okay
    unit.addOrder("GIB 0 1 SCHIFF"); // don't give last ship on ocean

    process(ocean);

    assertNull(unit.getOrders2().get(0).getProblem());
    assertNotNull(unit.getOrders2().get(1).getProblem());
    assertEquals(0, ship.getModifiedAmount()); // do it anyway
  }

  @Test
  public void testGiveCoast() {
    Ship ship = builder.addShip(data, region0, "ship", "Trireme", "Trireme", 200);
    Ship shipNone = builder.addShip(data, region0, "ship", "Trireme", "Trireme", 200);
    Ship shipnw = builder.addShip(data, region0, "ship", "Trireme", "Trireme", 200);
    Ship ship4 = builder.addShip(data, region0, "ship", "Trireme", "Trireme", 200);
    Ship ship5 = builder.addShip(data, region0, "ship", "Trireme", "Trireme", 200);
    Ship shipNone2 = builder.addShip(data, region0, "ship", "Trireme", "Trireme", 200);
    ship.setAmount(10);
    shipNone.setAmount(10);
    shipnw.setAmount(10);
    ship4.setAmount(10);
    ship5.setAmount(10);
    shipNone2.setAmount(10);

    Unit none = builder.addUnit(data, "none", "Coast", unit.getFaction(), region0);
    Unit nw = builder.addUnit(data, "nw", "Other", unit.getFaction(), region0);
    Unit nw2 = builder.addUnit(data, "nw2", "Other", unit.getFaction(), region0);
    Unit ne = builder.addUnit(data, "ne", "Other", unit.getFaction(), region0);
    Unit none2 = builder.addUnit(data, "non2", "Other", unit.getFaction(), region0);
    builder.addTo(unit, ship);
    builder.addTo(none, shipNone);
    builder.addTo(nw, shipnw);
    builder.addTo(nw2, ship4);
    builder.addTo(ne, ship5);
    builder.addTo(none2, shipNone2);

    shipnw.setShoreId(1);
    ship4.setShoreId(1);
    ship5.setShoreId(2);

    unit.clearOrders();
    nw.clearOrders();
    unit.addOrder("GIB none 1 SCHIFF"); // okay
    unit.addOrder("GIB ne 1 SCHIFF"); // okay
    nw.addOrder("GIB nw2 1 SCHIFF"); // okay
    nw.addOrder("GIB non2 1 SCHIFF"); // okay, non2 should get coast
    nw.addOrder("GIB ne 1 SCHIFF"); // not okay

    assertEquals(-1, shipNone.getShoreId());
    assertEquals(-1, shipNone2.getShoreId());
    assertEquals(1, shipnw.getShoreId());

    process(region0);

    assertNull(unit.getOrders2().get(0).getProblem());
    assertNull(unit.getOrders2().get(1).getProblem());
    assertNull(nw.getOrders2().get(0).getProblem());
    assertNotNull(nw.getOrders2().get(1).getProblem());
    assertNotNull(nw.getOrders2().get(2).getProblem());

    assertEquals(8, ship.getModifiedAmount());
    assertEquals(8, shipnw.getModifiedAmount()); // do it anyway
    assertEquals(-1, ship.getShoreId());
    assertEquals(-1, shipNone.getShoreId());
    assertEquals(1, shipnw.getShoreId());
    assertEquals(1, ship4.getShoreId());
    assertEquals(2, ship5.getShoreId());
    // FIXME assertEquals(shipnw.getShoreId(), shipNone.getShoreId());
  }

  private void process(Region region) {
    EresseaRelationFactory executor = new EresseaRelationFactory(data.rules);
    executor.processOrders(region);
  }

  private void changeAmount(Ship ship, int amount) {
    ship.setAmount(amount);
    ship.setSize(amount * ship.getShipType().getMaxSize());
  }
}
