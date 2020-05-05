// class magellan.library.gamebinding.EresseaOrderProcessingTest
// created on Dec 30, 2014
//
// Copyright 2003-2014 by magellan project team
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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import magellan.library.Building;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.IntegerID;
import magellan.library.Item;
import magellan.library.Order;
import magellan.library.Region;
import magellan.library.Rules;
import magellan.library.Unit;
import magellan.library.gamebinding.e3a.MaintainOrder;
import magellan.library.relation.MaintenanceRelation;
import magellan.library.rules.ItemType;
import magellan.library.utils.OrderToken;
import magellan.test.GameDataBuilder;

/**
 * @author stm
 * @version 1.0, Jan 2, 2015
 */
public class EresseaOrderProcessingTest {

  private GameDataBuilder builder;
  private GameData data;
  private Region region;
  private Faction faction;
  private Unit unit1;
  private Unit unit2;
  private Unit unit3;
  private ItemType silverType;
  private EresseaRelationFactory executor;
  private ItemType woodType;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    builder = new GameDataBuilder();
    data = builder.createSimpleGameData();
    Rules rules = data.getRules();
    silverType = rules.getItemType(EresseaConstants.I_USILVER);
    woodType = rules.getItemType(EresseaConstants.I_WOOD);

    executor = new EresseaRelationFactory(rules);

    region = data.getRegions().iterator().next();
    faction = data.getFactions().iterator().next();
    unit1 = data.getUnit(IntegerID.create("1"));
    unit2 = builder.addUnit(data, "2", "Zweite", faction, region);
    unit3 = builder.addUnit(data, "3", "Dreite", faction, region);

    unit1.clearOrders();
    unit2.clearOrders();
    unit3.clearOrders();
  }

  /**
   * @throws Exception
   */
  @Test
  public final void testReserveOwn() throws Exception {
    unit1.addItem(new Item(silverType, 50));
    unit2.addItem(new Item(silverType, 50));
    unit3.addItem(new Item(silverType, 10));

    unit1.addOrder("RESERVIERE 100 Silber");
    unit2.addOrder("RESERVIERE 100 Silber");

    executor.processOrders(region);

    assertEquals(60, unit1.getModifiedItem(silverType).getAmount());
    assertEquals(50, unit2.getModifiedItem(silverType).getAmount());
    assertEquals(0, unit3.getModifiedItem(silverType).getAmount());
  }

  /**
   * @throws Exception
   */
  @Test
  public final void testReserveGive() throws Exception {
    unit1.addItem(new Item(silverType, 50));

    unit1.addOrder("RESERVIERE 50 Silber");
    unit1.addOrder("GIB 2 50 Silber"); // don't give away reserved stuff

    executor.processOrders(region);

    assertEquals(50, unit1.getModifiedItem(silverType).getAmount());
    assertEquals(0, unit2.getModifiedItem(silverType).getAmount());
    assertEquals(null, unit3.getModifiedItem(silverType));
  }

  /**
   * @throws Exception
   */
  @Test
  public final void testGiveRest() throws Exception {
    unit1.addItem(new Item(silverType, 50));

    unit1.addOrder("RESERVIERE 20 Silber"); // take unit1's silver
    unit1.addOrder("GIB 3 50 Silber"); // only 30 left

    executor.processOrders(region);

    assertEquals(20, unit1.getModifiedItem(silverType).getAmount());
    assertEquals(null, unit2.getModifiedItem(silverType));
    assertEquals(30, unit3.getModifiedItem(silverType).getAmount());
  }

  /**
   * @throws Exception
   */
  @Test
  public final void testReserveOtherStuff() throws Exception {
    unit1.addItem(new Item(silverType, 50));

    unit2.addOrder("RESERVIERE 20 Silber"); // take unit1's silver
    unit1.addOrder("GIB 3 50 Silber"); // only 30 left

    executor.processOrders(region);

    assertEquals(0, unit1.getModifiedItem(silverType).getAmount());
    assertEquals(20, unit2.getModifiedItem(silverType).getAmount());
    assertEquals(30, unit3.getModifiedItem(silverType).getAmount());
  }

  /**
   * @throws Exception
   */
  @Test
  public final void testGiveOtherUnitsStuff() throws Exception {
    unit1.addItem(new Item(silverType, 50));

    unit2.addOrder("GIB 3 50 Silber"); // give unit1's silver to unit3

    executor.processOrders(region);

    assertEquals(0, unit1.getModifiedItem(silverType).getAmount());
    assertEquals(0, unit2.getModifiedItem(silverType).getAmount());
    assertEquals(50, unit3.getModifiedItem(silverType).getAmount());
  }

  /**
   * @throws Exception
   */
  @Test
  public final void testGiveGive() throws Exception {
    unit1.addItem(new Item(silverType, 50));
    unit2.addItem(new Item(woodType, 50));

    unit1.addOrder("GIB 2 50 Silber");
    unit2.addOrder("GIB 3 50 Silber"); // don't pass on stuff

    unit1.addOrder("GIB 3 50 Holz"); // this happens first
    unit2.addOrder("GIB 1 50 Holz"); // no unreserved wood left

    executor.processOrders(region);

    assertEquals(0, unit1.getModifiedItem(silverType).getAmount());
    assertEquals(50, unit2.getModifiedItem(silverType).getAmount());
    assertEquals(0, unit3.getModifiedItem(silverType).getAmount());

    assertEquals(0, unit1.getModifiedItem(woodType).getAmount());
    assertEquals(0, unit2.getModifiedItem(woodType).getAmount());
    assertEquals(50, unit3.getModifiedItem(woodType).getAmount());
  }

  /**
   * @throws Exception
   */
  @Test
  public final void testReserveAll() throws Exception {
    unit1.addItem(new Item(silverType, 50));
    unit3.addItem(new Item(silverType, 50));

    unit1.addOrder("RESERVIERE 60 Silber");
    unit1.addOrder("RESERVIERE ALLES Silber");
    unit1.addOrder("GIB 2 50 Silber");

    executor.processOrders(region);

    assertEquals(60, unit1.getModifiedItem(silverType).getAmount());
    assertEquals(40, unit2.getModifiedItem(silverType).getAmount());
    assertEquals(0, unit3.getModifiedItem(silverType).getAmount());
  }

  @Test
  public final void testBuildingMaintenance() throws Exception {
    Building building = builder.addBuilding(data, region, "b", "Leuchtturm", "Leuchti", 10);
    unit1.setBuilding(building);
    building.setOwner(unit1);
    unit1.addItem(new Item(silverType, 200));

    executor.processOrders(region);
    assertEquals(100, unit1.getModifiedItem(silverType).getAmount());
  }

  @Test
  public final void testRegionOwnerMaintenance() throws Exception {
    Building building = builder.addBuilding(data, region, "b", "Leuchtturm", "Leuchti", 10);
    Building castle = builder.addBuilding(data, region, "cast", "Burg", "Burg", 50);
    unit1.setBuilding(castle);
    castle.setOwner(unit1);
    unit1.addItem(new Item(silverType, 200));
    building.getBuildingType().setMaintendByRegionOwner(true);

    executor.processOrders(region);
    assertEquals(100, unit1.getModifiedItem(silverType).getAmount());
  }

  @Test
  public final void testRegionOwnerPayNot() throws Exception {
    Building building = builder.addBuilding(data, region, "b", "Leuchtturm", "Leuchti", 10);
    Building castle = builder.addBuilding(data, region, "cast", "Burg", "Burg", 50);
    unit1.setBuilding(castle);
    castle.setOwner(unit1);
    unit1.addItem(new Item(silverType, 200));
    Collection<Order> orders = new LinkedList<Order>();
    MaintainOrder mOrder;
    orders.add(mOrder = new MaintainOrder(Collections.<OrderToken> emptyList(), "BEZAHLE NICHT b"));
    mOrder.setBuilding(EntityID.createEntityID("b", data.base));
    mOrder.setValid(true);
    mOrder.setNot(true);
    unit1.addOrders2(orders);

    building.getBuildingType().setMaintendByRegionOwner(true);

    executor.processOrders(region);
    assertEquals(200, unit1.getModifiedItem(silverType).getAmount());
  }

  @Test
  public final void testPromote() throws Exception {
    unit1.addOrder("BEFOERDERE");
    unit1.addItem(new Item(silverType, 200));

    executor.processOrders(region);

    int costs = 0;
    for (Unit u : unit1.getFaction().units()) {
      costs += u.getPersons();
    }
    costs *= unit1.getPersons();
    assertTrue(costs <= 200);
    assertEquals(200 - costs, unit1.getModifiedItem(silverType).getAmount());
    assertEquals(1, unit1.getRelations(MaintenanceRelation.class).size());
  }

}
