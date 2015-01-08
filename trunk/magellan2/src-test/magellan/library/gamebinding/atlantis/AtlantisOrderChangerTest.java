// class magellan.library.gamebinding.AtlantisOrderChangerTest
// created on Apr 19, 2013
//
// Copyright 2003-2013 by magellan project team
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
package magellan.library.gamebinding.atlantis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import magellan.library.Building;
import magellan.library.GameData;
import magellan.library.Order;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.RulesException;
import magellan.library.utils.logging.Logger;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AtlantisOrderChangerTest extends MagellanTestWithResources {

  private GameData data;
  private GameDataBuilder builder;
  private Unit unit;
  private AtlantisOrderChanger changer;
  private Region region;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    setLocale(EN_LOCALE);
    Logger.setLevel(Logger.WARN);
    initResources();
  }

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    builder = new GameDataBuilder();
    builder.setGameName("Atlantis");
    builder.setLocale(EN_LOCALE);
    data = builder.createSimpleGameData();
    data.base = 10;
    unit = data.getUnits().iterator().next();
    region = unit.getRegion();
    changer = (AtlantisOrderChanger) data.getGameSpecificStuff().getOrderChanger();
  }

  @Test
  public final void testCreateOrder() {
    unit.clearOrders();
    Order order = changer.createOrder(unit, "Hello, Atlantis!");
    assertEquals("Hello, Atlantis!", order.getText());
  }

  @Test
  public final void testAddCombatOrder() {
    unit.clearOrders();
    changer.addCombatOrder(unit, -1);
    assertEquals(0, unit.getOrders2().size());
    unit.clearOrders();
    changer.addCombatOrder(unit, AtlantisConstants.CS_FRONT);
    assertEquals("BEHIND 0", unit.getOrders2().get(0).getText());
    unit.clearOrders();
    changer.addCombatOrder(unit, AtlantisConstants.CS_REAR);
    assertEquals("BEHIND 1", unit.getOrders2().get(0).getText());
  }

  @Test
  public final void testAddDescribeUnitContainerOrder() {
    unit.clearOrders();
    Ship ship = builder.addShip(data, region, "42", "Longboat", "LB", 100);
    Building building = builder.addBuilding(data, region, "43", "Building", "Castle", 10);
    changer.addDescribeUnitContainerOrder(unit, ship, "a ship");
    changer.addDescribeUnitContainerOrder(unit, building, "a building");
    assertEquals("DISPLAY SHIP \"a ship\"", unit.getOrders2().get(0).getText());
    assertEquals("DISPLAY BUILDING \"a building\"", unit.getOrders2().get(1).getText());
  }

  @Test
  public final void testAddDescribeUnitOrder() {
    unit.clearOrders();
    changer.addDescribeUnitOrder(unit, "a unit");
    assertEquals("DISPLAY UNIT \"a unit\"", unit.getOrders2().get(0).getText());
  }

  @Test
  public final void testAddDescribeUnitPrivateOrder() {
    unit.clearOrders();
    changer.addDescribeUnitPrivateOrder(unit, "hello");
    assertEquals(0, unit.getOrders2().size());
  }

  @Test
  public final void testAddHideOrder() {
    unit.clearOrders();
    changer.addHideOrder(unit, "15");
    assertEquals(0, unit.getOrders2().size());
  }

  @Test
  public final void testAddGroupOrder() {
    unit.clearOrders();
    changer.addGroupOrder(unit, "Renegades");
    assertEquals(0, unit.getOrders2().size());
  }

  @Test
  public final void testAddNamingOrderUnitString() {
    unit.clearOrders();
    changer.addNamingOrder(unit, "my unit");
    assertEquals("NAME UNIT \"my unit\"", unit.getOrders2().get(0).getText());
  }

  @Test
  public final void testAddNamingOrderUnitUnitContainerString() {
    unit.clearOrders();
    Ship ship = builder.addShip(data, region, "42", "Longboat", "LB", 100);
    Building building = builder.addBuilding(data, region, "43", "Building", "Castle", 10);
    changer.addNamingOrder(unit, building, "my building");
    changer.addNamingOrder(unit, ship, "my ship");
    assertEquals("NAME BUILDING \"my building\"", unit.getOrders2().get(0).getText());
    assertEquals("NAME SHIP \"my ship\"", unit.getOrders2().get(1).getText());
  }

  @Test
  public final void testAddRecruitOrder() {
    unit.clearOrders();
    changer.addRecruitOrder(unit, 14);
    assertEquals("RECRUIT 14", unit.getOrders2().get(0).getText());
  }

  @Test
  public final void testAddGiveOrder() {
    unit.clearOrders();
    Unit unit2 = builder.addUnit(data, "2", "U2", unit.getFaction(), unit.getRegion());
    changer.addGiveOrder(unit, unit2, 1, AtlantisConstants.I_WOOD, "foo");
    assertEquals("GIVE 2 1 wood ; foo", unit.getOrders2().get(0).toString());

    changer.addGiveOrder(unit, unit2, 5, AtlantisConstants.I_USILVER, null);
    assertTrue(unit.getOrders2().get(1).toString().matches("PAY 2 5 *"));

    changer.addGiveOrder(unit, unit2, 3, EresseaConstants.I_MEN, null);
    assertEquals("TRANSFER 2 3", unit.getOrders2().get(2).toString());
  }

  @Test
  public final void testAddMultipleHideOrder() {
    unit.clearOrders();
    changer.addMultipleHideOrder(unit);
    assertEquals("NAME UNIT Unit", unit.getOrders2().get(0).getText());
    assertEquals("DISPLAY UNIT \"\"", unit.getOrders2().get(1).getText());
  }

  @Test
  public final void testDisableLongOrders() {
    unit.clearOrders();
    unit.addOrder("WORK");
    unit.addOrder("PAY 123 15");
    changer.disableLongOrders(unit);
    assertEquals(";WORK", unit.getOrders2().get(0).getText());
    assertEquals("PAY 123 15", unit.getOrders2().get(1).getText());
  }

  @Test
  public final void testIsLongOrderString() {
    assertTrue(changer.isLongOrder("WORK"));
    assertFalse(changer.isLongOrder("W"));
    assertTrue(changer.isLongOrder("TEACH"));
    assertFalse(changer.isLongOrder("TAX"));
  }

  @Test
  public final void testIsLongOrderOrder() {
    unit.clearOrders();
    unit.addOrder("WORK");
    unit.addOrder("W");
    unit.addOrder("ENTERTAIN");
    unit.addOrder("DEMOLISH");
    assertTrue(changer.isLongOrder(unit.getOrders2().get(0)));
    assertFalse(changer.isLongOrder(unit.getOrders2().get(1)));
    assertTrue(changer.isLongOrder(unit.getOrders2().get(2)));
    assertFalse(changer.isLongOrder(unit.getOrders2().get(3)));
  }

  @Test
  public final void testAreCompatibleLongOrders() {
    unit.clearOrders();
    unit.addOrder("WORK");
    unit.addOrder("W");
    unit.addOrder("ENTERTAIN");
    unit.addOrder("DEMOLISH");
    assertSame(2, changer.areCompatibleLongOrders(unit.getOrders2()));
  }

  @Test
  public final void testGetOrderTranslation() {
    assertEquals("ALLY", changer.getOrderTranslation(AtlantisConstants.OC_ALLY, unit).getText());
  }

  @Test
  public final void testGetOrderLocaleObject() {
    assertEquals("ALLY", changer.getOrder(EN_LOCALE, AtlantisConstants.OC_ALLY).toString());
    assertEquals("ALLY", changer.getOrder(DE_LOCALE, AtlantisConstants.OC_ALLY).getText());
  }

  @Test
  public final void testGetOrderLocaleStringIDObjectArray() {
    assertEquals("ALLY UNIT 1 12345", changer.getOrder(EN_LOCALE, AtlantisConstants.OC_ALLY,
        new Object[] { AtlantisConstants.OC_UNIT, unit.getID(), "12345" }).getText());
  }

  @Test
  public final void testGetOrderStringIDLocaleObjectArray() {
    try {
      assertEquals("ALLY UNIT 1 12345", changer.getOrder(AtlantisConstants.OC_ALLY, EN_LOCALE,
          new Object[] { AtlantisConstants.OC_UNIT, unit.getID(), "12345" }).getText());
    } catch (RulesException e) {
      fail();
    }
    try {
      assertEquals("ALLY UNIT 1 12345", changer.getOrder(AtlantisConstants.OC_ALLY, DE_LOCALE,
          new Object[] { AtlantisConstants.OC_UNIT, unit.getID(), "12345" }).getText());
      fail();
    } catch (RulesException e) {
      // wrong locale
    }
    try {
      changer.getOrder(StringID.create("MAKE"), EN_LOCALE, new Object[] {});
      fail();
    } catch (RulesException e) {
      // exception expected
    }
    try {
      changer.getOrder(AtlantisConstants.OC_ADMIT, EN_LOCALE, new Object[] { StringID
          .create("MAKE") });
      fail();
    } catch (RulesException e) {
      // exception expected
    }
  }

  @Test
  public final void testExtractTempUnits() {
    unit.clearOrders();
    unit.addOrder("TEACH NEW 123");
    unit.addOrder("FORM 123");
    unit.addOrder("STUDY Entertainment");
    unit.addOrder("END");
    assertEquals(1, changer.extractTempUnits(data, 0, getLocale(), unit));
    Unit temp = unit.getTempUnit(UnitID.createUnitID(-123, data.base));
    Unit temp2 = data.getTempUnit(UnitID.createUnitID(-123, data.base));
    assertEquals("STUDY Entertainment", temp.getOrders2().get(0).getText());

    assertEquals(1, unit.getOrders2().size());
    assertEquals("TEACH NEW 123", unit.getOrders2().get(0).getText());
    assertSame(temp, temp2);
    assertEquals(1, temp.getOrders2().size());
  }

  @Test
  public final void testGetTempOrders() {
    unit.clearOrders();
    unit.addOrder("TEACH NEW 123");
    unit.addOrder("FORM 123");
    unit.addOrder("STUDY Entertainment");
    unit.addOrder("END");
    changer.extractTempUnits(data, 0, getLocale(), unit);
    ArrayList<Order> orders = new ArrayList<Order>(changer.getTempOrders(false, unit));
    assertEquals("FORM 123", orders.get(0).getText());
    assertEquals("STUDY Entertainment", orders.get(1).getText());
    assertEquals("END", orders.get(2).getText());
  }

  @Test
  public void testGetTempOrders2() throws Exception {
    unit.clearOrders();
    unit.createTemp(data, UnitID.createTempID(data, null, unit));
    Collection<? extends Order> orders = changer.getTempOrders(false, unit);
    assertSame(2, orders.size());
    Iterator<? extends Order> it = orders.iterator();
    assertEquals("FORM 1", it.next().getText());
    assertEquals("END", it.next().getText());
  }

}
