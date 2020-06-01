// class magellan.library.utils.RoutePlannerTest
// created on May 31, 2020
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
package magellan.library.utils;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.EresseaMapMetric;
import magellan.library.utils.RoutePlanner.Costs;
import magellan.library.utils.UnitRoutePlanner.LandCosts;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

public class RoutePlannerTest extends MagellanTestWithResources {

  private Unit unit;
  private GameDataBuilder builder;
  private GameData data;

  @Before
  public void setUp() throws Exception {
    builder = new GameDataBuilder();
    data = builder.createSimpleGameData();
    unit = data.getUnits().iterator().next();
    unit.clearOrders();
  }

  static class UnitCosts implements Costs {

    private int max;

    public UnitCosts(int max) {
      this.max = max;
    }

    public boolean isExhausted(LinkedList<Region> curPath) {
      return curPath.size() > max;
    }

  }

  @Test
  public void testAddOrders() {
    Region east = builder.addRegion(data, "1 0", "East", "Ebene", 1);

    RoutePlanner planner = new RoutePlanner();

    List<String> orders = new LinkedList<String>();
    List<Region> path = new LinkedList<Region>();
    path.add(unit.getRegion());
    path.add(east);
    int mode = 0; // RoutePlanner.MODE_CONTINUOUS; // RoutePlanner.MODE_RETURN, RoutePlanner.MODE_STOP
    boolean useVorlage = false;
    Costs costs = new LandCosts(unit);
    RoutePlanner.addOrders(orders, path, mode, useVorlage, costs);
    assertEquals(1, orders.size());
    assertEquals("NACH O", orders.get(0));

    orders.clear();
    mode = RoutePlanner.MODE_CONTINUOUS;
    RoutePlanner.addOrders(orders, path, mode, useVorlage, costs);
    assertEquals(1, orders.size());
    assertEquals("ROUTE O PAUSE", orders.get(0));

    orders.clear();
    mode = RoutePlanner.MODE_CONTINUOUS | RoutePlanner.MODE_STOP;
    RoutePlanner.addOrders(orders, path, mode, useVorlage, costs);
    assertEquals(1, orders.size());
    assertEquals("ROUTE O PAUSE PAUSE", orders.get(0));

  }

  @Test
  public void testAddOrdersRound() {
    Region east = builder.addRegion(data, "1 0", "East", "Ebene", 1);

    RoutePlanner planner = new RoutePlanner();

    List<String> orders = new LinkedList<String>();
    List<Region> path = new LinkedList<Region>();
    path.add(unit.getRegion());
    path.add(east);
    path.add(east);
    path.add(unit.getRegion());
    Costs costs = new LandCosts(unit);
    boolean useVorlage = false;

    int mode = RoutePlanner.MODE_CONTINUOUS | RoutePlanner.MODE_RETURN;
    RoutePlanner.addOrders(orders, path, mode, useVorlage, costs);
    assertEquals(1, orders.size());
    assertEquals("ROUTE O PAUSE W PAUSE", orders.get(0));

    orders.clear();
    mode = RoutePlanner.MODE_CONTINUOUS | RoutePlanner.MODE_RETURN | RoutePlanner.MODE_STOP;
    RoutePlanner.addOrders(orders, path, mode, useVorlage, costs);
    assertEquals(1, orders.size());
    assertEquals("ROUTE O PAUSE W PAUSE PAUSE", orders.get(0));

    orders.clear();
    mode = 0;
    useVorlage = false;
    RoutePlanner.addOrders(orders, path, mode, useVorlage, costs);
    assertEquals(2, orders.size());
    assertEquals("NACH O", orders.get(0));
    assertEquals("// NACH W", orders.get(1));

    orders.clear();
    mode = 0;
    useVorlage = true;
    RoutePlanner.addOrders(orders, path, mode, useVorlage, costs);
    assertEquals(2, orders.size());
    assertEquals("NACH O", orders.get(0));
    assertEquals("// #after 1 { NACH W }", orders.get(1));

    orders.clear();
    mode = RoutePlanner.MODE_CONTINUOUS | RoutePlanner.MODE_STOP;
    useVorlage = true;
    RoutePlanner.addOrders(orders, path, mode, useVorlage, costs);
    assertEquals(2, orders.size());
    assertEquals("ROUTE O PAUSE", orders.get(0));
    assertEquals("// #after 1 { ROUTE W PAUSE PAUSE }", orders.get(1));
  }

  @Test
  public void testAddOrdersLong() {
    List<Region> path = new LinkedList<Region>();
    Region[] east = new Region[5];
    path.add(unit.getRegion());
    for (int i = 0; i < east.length; ++i) {
      east[i] = builder.addRegion(data, (i + 1) + " 0", "East " + i, "Ebene", i + 1);
      path.add(east[i]);
    }

    RoutePlanner planner = new RoutePlanner();

    List<String> orders = new LinkedList<String>();
    Costs costs = new LandCosts(unit);
    boolean useVorlage = false;

    int mode = 0;
    RoutePlanner.addOrders(orders, path, mode, useVorlage, costs);
    assertEquals(5, orders.size());
    assertEquals("NACH O", orders.get(0));
    for (int i = 1; i < east.length; ++i) {
      assertEquals("" + i, "// NACH O", orders.get(i));
    }

    orders.clear();
    mode = RoutePlanner.MODE_CONTINUOUS;
    RoutePlanner.addOrders(orders, path, mode, useVorlage, costs);
    assertEquals(1, orders.size());
    assertEquals("ROUTE O PAUSE O PAUSE O PAUSE O PAUSE O PAUSE", orders.get(0));

    orders.clear();
    mode = RoutePlanner.MODE_CONTINUOUS;
    costs = RoutePlanner.ZERO_COSTS;
    RoutePlanner.addOrders(orders, path, mode, useVorlage, costs);
    assertEquals(1, orders.size());
    assertEquals("ROUTE O O O O O PAUSE", orders.get(0));
  }

  @Test
  public void testAddOrdersStreet() {
    List<Region> path = new LinkedList<Region>();
    Region[] east = new Region[5];
    path.add(unit.getRegion());
    for (int i = 0; i < east.length; ++i) {
      east[i] = builder.addRegion(data, (i + 1) + " 0", "East " + i, "Ebene", i + 1);
      path.add(east[i]);
    }
    int dirE = EresseaMapMetric.E.getDirCode(), dirW = EresseaMapMetric.W.getDirCode();
    builder.addRoad(unit.getRegion(), dirE, 100);
    builder.addRoad(east[0], dirW, 100);

    RoutePlanner planner = new RoutePlanner();

    List<String> orders = new LinkedList<String>();
    Costs costs = new LandCosts(unit);
    boolean useVorlage = false;
    int mode = RoutePlanner.MODE_CONTINUOUS;

    orders.clear();
    RoutePlanner.addOrders(orders, path, mode, useVorlage, costs);
    assertEquals(1, orders.size());
    assertEquals("ROUTE O PAUSE O PAUSE O PAUSE O PAUSE O PAUSE", orders.get(0));

    builder.addRoad(east[0], dirE, 100);
    builder.addRoad(east[1], dirW, 100);
    orders.clear();
    RoutePlanner.addOrders(orders, path, mode, useVorlage, costs);
    assertEquals(1, orders.size());
    assertEquals("ROUTE O O PAUSE O PAUSE O PAUSE O PAUSE", orders.get(0));
  }

  @Test
  public void testAddOrdersBug() {
    List<Region> path = new LinkedList<Region>();
    Region[] east = new Region[5];
    path.add(unit.getRegion());
    builder.addItem(data, unit, "Pferd", 1);
    builder.addSkill(unit, "Reiten", 2);
    for (int i = 0; i < east.length; ++i) {
      east[i] = builder.addRegion(data, (i + 1) + " 0", "East " + i, "Ebene", i + 1);
      path.add(east[i]);
    }
    int dirE = EresseaMapMetric.E.getDirCode(), dirW = EresseaMapMetric.W.getDirCode();
    builder.addRoad(unit.getRegion(), dirE, 100);

    builder.addRoad(east[3], dirE, 100);
    builder.addRoad(east[4], dirW, 100);

    RoutePlanner planner = new RoutePlanner();

    List<String> orders = new LinkedList<String>();
    Costs costs = new LandCosts(unit);
    boolean useVorlage = false;
    int mode = 0;

    orders.clear();
    RoutePlanner.addOrders(orders, path, mode, useVorlage, costs);
    assertEquals(3, orders.size());
    assertEquals("NACH O O", orders.get(0));
    assertEquals("// NACH O O", orders.get(1));
    assertEquals("// NACH O", orders.get(2));
  }

  @Test
  public void testAddOrdersBug2() {
    List<Region> path = new LinkedList<Region>();
    Region[] east = new Region[5];
    path.add(unit.getRegion());

    east[0] = builder.addRegion(data, "1 0", "East 1", "Ebene", 1);
    east[1] = builder.addRegion(data, "2 0", "East 2", "Ebene", 1);
    east[2] = builder.addRegion(data, "2 1", "East 3", "Ebene", 1);
    east[3] = builder.addRegion(data, "1 2", "East 5", "Ebene", 1);
    east[4] = builder.addRegion(data, "1 1", "Shortcut", "Ebene", 1);
    int dirE = EresseaMapMetric.E.getDirCode(),
        dirNE = EresseaMapMetric.NE.getDirCode(),
        dirNW = EresseaMapMetric.NW.getDirCode(),
        dirW = EresseaMapMetric.W.getDirCode(),
        dirSW = EresseaMapMetric.SW.getDirCode(), dirSE = EresseaMapMetric.SE.getDirCode();
    builder.addRoad(unit.getRegion(), dirE, 100);
    builder.addRoad(east[0], dirW, 100);
    builder.addRoad(east[0], dirE, 100);
    builder.addRoad(east[1], dirW, 100);
    builder.addRoad(east[1], dirNE, 100);
    builder.addRoad(east[2], dirSW, 100);
    builder.addRoad(east[2], dirNW, 100);
    builder.addRoad(east[3], dirSE, 100);

    UnitRoutePlanner planner = new UnitRoutePlanner();

    boolean useRange = true, useVorlage = false;
    int mode = RoutePlanner.MODE_RETURN;

    List<String> orders = planner.getOrders(unit, data, unit.getRegion().getCoordinate(), east[3].getCoordinate(), null,
        useRange, mode, useVorlage);

    assertEquals(4, orders.size());
    assertEquals("NACH O O", orders.get(0));
    assertEquals("// NACH NO NW", orders.get(1));
    assertEquals("// NACH SO SW", orders.get(2));
    assertEquals("// NACH W W", orders.get(3));
  }

  @Test
  public void testAddOrdersShip() {
    List<Region> path = new LinkedList<Region>();
    Region[] east = new Region[5];
    path.add(unit.getRegion());
    Ship ship = builder.addShip(data, unit.getRegion(), "x", "Boot", "Boot x", 5);
    unit.setShip(ship);
    ship.setOwner(unit);

    for (int i = 0; i < east.length; ++i) {
      east[i] = builder.addRegion(data, "0 " + (i + 1), "East " + i, "Ozean", i + 1);
      path.add(east[i]);
    }

    RoutePlanner planner = new RoutePlanner();

    List<String> orders = new LinkedList<String>();
    Costs costs = new ShipRoutePlanner.ShipCosts(ship, 4, data.getRules().getBuildingType(EresseaConstants.B_HARBOUR));
    boolean useVorlage = false;
    int mode = 0;

    orders.clear();
    RoutePlanner.addOrders(orders, path, mode, useVorlage, costs);
    assertEquals(2, orders.size());
    assertEquals("NACH NO NO NO NO", orders.get(0));
    assertEquals("// NACH NO", orders.get(1));
  }

  @Test
  public void testAddOrdersShipLand() {
    List<Region> path = new LinkedList<Region>();
    Region[] east = new Region[5];
    path.add(unit.getRegion());
    Ship ship = builder.addShip(data, unit.getRegion(), "x", "Boot", "Boot x", 5);
    unit.setShip(ship);
    ship.setOwner(unit);

    for (int i = 0; i < east.length; ++i) {
      String type = i == 2 ? "Ebene" : "Ozean";
      east[i] = builder.addRegion(data, "0 " + (i + 1), "East " + i, type, i + 1);
      path.add(east[i]);
    }

    RoutePlanner planner = new RoutePlanner();

    List<String> orders = new LinkedList<String>();
    Costs costs = new ShipRoutePlanner.ShipCosts(ship, 4, data.getRules().getBuildingType(EresseaConstants.B_HARBOUR));
    boolean useVorlage = false;
    int mode = 0;

    orders.clear();
    RoutePlanner.addOrders(orders, path, mode, useVorlage, costs);
    assertEquals(2, orders.size());
    assertEquals("NACH NO NO NO", orders.get(0));
    assertEquals("// NACH NO NO", orders.get(1));
  }

  @Test
  public void testAddOrdersShipHarbour() {
    List<Region> path = new LinkedList<Region>();
    Region[] east = new Region[5];
    path.add(unit.getRegion());
    Ship ship = builder.addShip(data, unit.getRegion(), "x", "Boot", "Boot x", 5);
    unit.setShip(ship);
    ship.setOwner(unit);

    for (int i = 0; i < east.length; ++i) {
      String type = i == 2 ? "Ebene" : "Ozean";
      east[i] = builder.addRegion(data, "0 " + (i + 1), "East " + i, type, i + 1);
      path.add(east[i]);
    }
    builder.addBuilding(data, east[2], "h", "Hafen", "Hafen h", 25);

    RoutePlanner planner = new RoutePlanner();

    List<String> orders = new LinkedList<String>();
    Costs costs = new ShipRoutePlanner.ShipCosts(ship, 4, data.getRules().getBuildingType(EresseaConstants.B_HARBOUR));
    boolean useVorlage = false;
    int mode = 0;

    orders.clear();
    RoutePlanner.addOrders(orders, path, mode, useVorlage, costs);
    assertEquals(2, orders.size());
    assertEquals("NACH NO NO NO NO", orders.get(0));
    assertEquals("// NACH NO", orders.get(1));
  }
}