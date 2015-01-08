// class magellan.library.gamebinding.EresseaOrderChangerTest
// created on Jun 14, 2012
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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import magellan.library.GameData;
import magellan.library.Order;
import magellan.library.Ship;
import magellan.library.StringID;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.UnitID;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for EresseaOrderChanger
 *
 * @author stm
 */
public class EresseaOrderChangerTest extends MagellanTestWithResources {

  private GameData data;
  private GameDataBuilder builder;
  private Unit unit;
  private EresseaOrderChanger changer;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    builder = new GameDataBuilder();
    data = builder.createSimpleGameData();
    unit = data.getUnits().iterator().next();
    unit.clearOrders();
    changer = (EresseaOrderChanger) data.getGameSpecificStuff().getOrderChanger();
  }

  @Test
  public final void testAreCompatibleLongOrdersFollow() {
    unit.addOrder("FOLGE EINHEIT abc");
    unit.addOrder("ARBEITE");
    assertEquals(-1, changer.areCompatibleLongOrders(unit.getOrders2()));

    unit.clearOrders();
    unit.addOrder("FOLGE SCHIFF abc");
    unit.addOrder("ARBEITE");
    assertEquals(-1, changer.areCompatibleLongOrders(unit.getOrders2()));

    unit.addOrder("FOLGE EINHEIT abc");
    unit.addOrder("FOLGE EINHEIT bcd");
    assertEquals(-1, changer.areCompatibleLongOrders(unit.getOrders2()));
  }

  /**
   * Test for {@link EresseaOrderChanger#areCompatibleLongOrders(magellan.library.Orders)}
   */
  @Test
  public final void testAreCompatibleLongOrders() {
    unit.addOrder("ARBEITE");
    unit.addOrder("GIB 123 4 Stein");
    assertEquals(-1, changer.areCompatibleLongOrders(unit.getOrders2()));

    unit.clearOrders();
    unit.addOrder("ARBEITE");
    unit.addOrder("KAUFE 3 Balsam");
    unit.addOrder("VERKAUFE 3 Balsam");
    assertEquals(1, changer.areCompatibleLongOrders(unit.getOrders2()));

    unit.clearOrders();
    unit.addOrder("KAUFE 3 Balsam");
    unit.addOrder("VERKAUFE 3 Balsam");
    unit.addOrder("ARBEITE");
    assertEquals(2, changer.areCompatibleLongOrders(unit.getOrders2()));

    unit.clearOrders();
    unit.addOrder("ARBEITE");
    unit.addOrder("LERNE Hiebwaffen");
    assertEquals(1, changer.areCompatibleLongOrders(unit.getOrders2()));

    unit.clearOrders();
    unit.addOrder("KAUFE 2 Öl");
    unit.addOrder("VERKAUFE 3 Balsam");
    unit.addOrder("VERKAUFE 3 Balsam");
    assertEquals(-1, changer.areCompatibleLongOrders(unit.getOrders2()));

    unit.clearOrders();
    unit.addOrder("KAUFE 2 Öl");
    unit.addOrder("KAUFE 3 Balsam");
    unit.addOrder("VERKAUFE 3 Balsam");
    assertEquals(1, changer.areCompatibleLongOrders(unit.getOrders2()));

    unit.clearOrders();
    unit.addOrder("ZAUBERE \"Kawumpss\"");
    unit.addOrder("ZAUBERE \"Kawumpss\"");
    assertEquals(-1, changer.areCompatibleLongOrders(unit.getOrders2()));

    unit.clearOrders();
    unit.addOrder("ARBEITE");
    unit.addOrder("ZAUBERE \"Kawumpss\"");
    assertEquals(1, changer.areCompatibleLongOrders(unit.getOrders2()));

    unit.clearOrders();
    unit.addOrder("ZAUBERE \"Kawumpss\"");
    unit.addOrder("ARBEITE");
    assertEquals(1, changer.areCompatibleLongOrders(unit.getOrders2()));

    unit.clearOrders();
    unit.addOrder("ATTACKIERE");
    unit.addOrder("LERNE Hiebwaffen");
    unit.addOrder("ARBEITEN");
    assertEquals(2, changer.areCompatibleLongOrders(unit.getOrders2()));

    unit.clearOrders();
    unit.addOrder("ATTACKIERE");
    unit.addOrder("ARBEITEN");
    unit.addOrder("LERNE Hiebwaffen");
    unit.addOrder("ZAUBERE 'Katzeklo'");
    assertEquals(2, changer.areCompatibleLongOrders(unit.getOrders2()));

  }

  /**
   * Test for
   * {@link EresseaOrderChanger#addGiveOrder(Unit, Unit, int, magellan.library.StringID, String)}
   */
  @Test
  public void testAddGiveOrder() throws Exception {
    Unit unit2 = builder.addUnit(data, "targ", "Target", unit.getFaction(), unit.getRegion());
    unit.clearOrders();
    changer.addGiveOrder(unit, unit2, 1, EresseaConstants.I_WOOD, null);
    assertEquals("GIB targ 1 Holz", unit.getOrders2().get(0).toString());

    unit.clearOrders();
    changer.addGiveOrder(unit, unit2, -1, EresseaConstants.I_WOOD, null);
    assertEquals("GIB targ JE 1 Holz", unit.getOrders2().get(0).toString());

    unit.clearOrders();
    changer.addGiveOrder(unit, unit2, OrderChanger.ALL, EresseaConstants.I_WOOD, null);
    assertEquals("GIB targ ALLES Holz", unit.getOrders2().get(0).toString());

    unit.clearOrders();
    changer.addGiveOrder(unit, unit2, OrderChanger.ALL, null, null);
    assertEquals("GIB targ ALLES", unit.getOrders2().get(0).toString());

    unit.clearOrders();
    changer.addGiveOrder(unit, unit2, 2, EresseaConstants.I_MEN, null);
    assertEquals("GIB targ 2 PERSONEN", unit.getOrders2().get(0).toString());

    unit.clearOrders();
    changer.addGiveOrder(unit, unit2, 1, StringID.create("Foo"), null);
    assertEquals("; unknown item Foo", unit.getOrders2().get(0).toString());

    unit.clearOrders();
    changer.addGiveOrder(unit, unit2, 1, null, null);
    assertEquals("; illegal amount and no item 1", unit.getOrders2().get(0).toString());
  }

  /**
   * Test for
   * {@link EresseaOrderChanger#addGiveOrder(Unit, Unit, int, magellan.library.StringID, String)}
   */
  @Test
  public void testAddGiveOrderTemp() throws Exception {
    TempUnit unit2 = unit.createTemp(data, UnitID.createUnitID(-42, data.base));
    unit.clearOrders();
    changer.addGiveOrder(unit, unit2, 4, EresseaConstants.I_UHORSE, "to temp");
    assertEquals("GIB TEMP 16 4 Pferd ; to temp", unit.getOrders2().get(0).toString());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testExtractTempOrders() throws Exception {
    unit.clearOrders();
    unit.addOrder("LERNEN Unterhaltung");
    unit.addOrder("MACHEN TEMP 123");
    unit.addOrder("REKRUTIEREN 1");
    unit.addOrder("ARBEITEN");
    unit.addOrder("ENDE");
    unit.addOrder("GIB TEMP 123 50 Silber");

    int ret = changer.extractTempUnits(data, 0, getLocale(), unit);
    assertEquals(1, ret);
    assertEquals(2, unit.getOrders2().size());
    assertEquals("LERNEN Unterhaltung", unit.getOrders2().get(0).getText());
    assertEquals("GIB TEMP 123 50 Silber", unit.getOrders2().get(1).getText());
    UnitID tempID = UnitID.createUnitID("123", data.base);
    tempID = UnitID.createUnitID(-tempID.intValue(), data.base);
    TempUnit tempUnit = data.getTempUnit(tempID);
    Unit tempUnit2 = unit.getTempUnit(tempID);
    assertSame(tempUnit, tempUnit2);
    assertEquals(2, tempUnit2.getOrders2().size());
    assertEquals("REKRUTIEREN 1", tempUnit.getOrders2().get(0).getText());
    assertEquals("ARBEITEN", tempUnit.getOrders2().get(1).getText());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testGetTempOrders() throws Exception {
    unit.clearOrders();
    unit.createTemp(data, UnitID.createTempID(data, null, unit));
    Collection<? extends Order> orders = changer.getTempOrders(false, unit);
    assertSame(2, orders.size());
    Iterator<? extends Order> it = orders.iterator();
    assertEquals("MACHEN TEMP 1", it.next().getText());
    assertEquals("ENDE", it.next().getText());
  }

  /**
   *
   */
  @Test
  public void testAddNamingOrder() throws Exception {
    changer.addNamingOrder(unit, "Me");
    assertEquals("BENENNEN EINHEIT \"Me\"", unit.getOrders2().get(0).toString());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testCreateNamingOrder() throws Exception {
    String order = changer.createNamingOrder("Me", unit);
    assertEquals("BENENNEN EINHEIT \"Me\"", order);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testAddNamingContainerOrder() throws Exception {
    UnitContainer container =
        builder.addBuilding(data, unit.getRegion(), "foo", "Burg", "Not", 200);
    changer.addNamingOrder(unit, container, "It");
    assertEquals("BENENNEN BURG \"It\"", unit.getOrders2().get(0).toString());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testCreateNamingContainerOrder() throws Exception {
    UnitContainer container =
        builder.addBuilding(data, unit.getRegion(), "foo", "Burg", "Not", 200);
    assertEquals("BENENNEN BURG \"It\"", changer.createNamingOrder(container, "It", unit));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testAddDescribeUnitContainerOrder() throws Exception {
    UnitContainer container =
        builder.addBuilding(data, unit.getRegion(), "foo", "Burg", "Not", 200);
    changer.addDescribeUnitContainerOrder(unit, container, "It");
    assertEquals("BESCHREIBEN BURG \"It\"", unit.getOrders2().get(0).toString());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testCreateDescribeUnitContainerOrder() throws Exception {
    UnitContainer container =
        builder.addBuilding(data, unit.getRegion(), "foo", "Burg", "Not", 200);
    assertEquals("BESCHREIBEN BURG \"It\"", changer.createDescribeUnitContainerOrder(container,
        unit, "It"));
    assertEquals("BESCHREIBEN REGION \"It\"", changer.createDescribeUnitContainerOrder(unit
        .getRegion(), unit, "It"));
    assertEquals("BANNER \"It\"", changer.createDescribeUnitContainerOrder(unit.getFaction(), unit,
        "It"));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testAddDescribeUnitPrivateOrder() throws Exception {
    changer.addDescribeUnitPrivateOrder(unit, "Me");
    assertEquals("BESCHREIBEN PRIVAT \"Me\"", unit.getOrders2().get(0).toString());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testCreateDescribeUnitPrivateOrder() throws Exception {
    assertEquals("BESCHREIBEN PRIVAT \"Me\"", changer.createDescribeUnitPrivateOrder("Me", unit));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testAddDescribeUnitOrder() throws Exception {
    changer.addDescribeUnitOrder(unit, "Me");
    assertEquals("BESCHREIBEN EINHEIT \"Me\"", unit.getOrders2().get(0).toString());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testCreateDescribeUnitOrder() throws Exception {
    assertEquals("BESCHREIBEN EINHEIT \"Me\"", changer.createDescribeUnitOrder("Me", unit));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testAddHideOrder() throws Exception {
    changer.addHideOrder(unit, "1");
    assertEquals("TARNEN 1", unit.getOrders2().get(0).toString());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testCreateHideOrder() throws Exception {
    assertEquals("TARNEN 2", changer.createHideOrder(unit, "2").toString());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testCreateOrder() throws Exception {
    Order order = changer.createOrder(unit, "Bla");
    assertEquals("Bla", order.getText());
    assertEquals(0, unit.getOrders2().size());

    order = changer.createOrder(unit, "ARBEITEN");
    assertEquals("ARBEITEN", order.getText());
    assertTrue(order.isLong());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testAddCombatOrder() throws Exception {
    changer.addCombatOrder(unit, 2);
    assertEquals("KÄMPFEN HINTEN", unit.getOrders2().get(0).toString());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testGetCombatOrder() throws Exception {
    assertEquals("KÄMPFEN HINTEN", changer.getCombatOrder(unit, 2));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testAddRecruitOrder() throws Exception {
    changer.addRecruitOrder(unit, 15);
    assertEquals("REKRUTIEREN 15", unit.getOrders2().get(0).toString());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testAddMultipleHideOrder() throws Exception {
    Ship ship = builder.addShip(data, unit.getRegion(), "sh", "Trireme", "ship", 200);
    unit.setShip(ship);

    changer.addMultipleHideOrder(unit);
    int line = 0;
    assertEquals("NUMMER EINHEIT ", unit.getOrders2().get(line++).toString());
    assertEquals("BENENNEN EINHEIT \"\"", unit.getOrders2().get(line++).toString());
    assertEquals("BESCHREIBEN EINHEIT \"\"", unit.getOrders2().get(line++).toString());
    assertEquals("TARNEN PARTEI", unit.getOrders2().get(line++).toString());
    assertEquals("NUMMER SCHIFF", unit.getOrders2().get(line++).toString());
    assertEquals("BENENNEN SCHIFF \"\"", unit.getOrders2().get(line++).toString());
    assertEquals("BESCHREIBEN SCHIFF \"\"", unit.getOrders2().get(line++).toString());
    assertEquals("// NUMMER EINHEIT " + unit.getID(), unit.getOrders2().get(line++).toString());
    assertEquals("// BENENNEN EINHEIT \"" + unit.getName() + "\"", unit.getOrders2().get(line++)
        .toString());
    assertEquals("// TARNEN PARTEI NICHT", unit.getOrders2().get(line++).toString());
    assertEquals("// NUMMER SCHIFF sh", unit.getOrders2().get(line++).toString());
    assertEquals("// BENENNEN SCHIFF \"ship\"", unit.getOrders2().get(line++).toString());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testDisableLongOrders() throws Exception {
    unit.addOrder("ARBEIT");
    unit.addOrder("GIB 123 4 Silber");
    unit.addOrder("VERKAUFE ALLES Balsam");
    unit.addOrder("FOLGE EINHEIT 123");
    changer.disableLongOrders(unit);
    assertEquals("; ARBEIT", unit.getOrders2().get(0).toString());
    assertEquals("GIB 123 4 Silber", unit.getOrders2().get(1).toString());
    assertEquals("; VERKAUFE ALLES Balsam", unit.getOrders2().get(2).toString());
    assertEquals("; FOLGE EINHEIT 123", unit.getOrders2().get(3).toString());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testIsLongOrder() throws Exception {
    assertEquals(true, changer.isLongOrder(changer.createOrder(unit, "ARBEITE")));
    assertEquals(false, changer.isLongOrder(changer.createOrder(unit, "GIB 123 4 Silber")));
    assertEquals(true, changer.isLongOrder(changer.createOrder(unit, "LERNE")));
    assertEquals(false, changer.isLongOrder(changer.createOrder(unit, "MACHE TEMP 123")));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testIsLongButShort() throws Exception {
    assertEquals(false, changer.isLongButShort("MACHE", DE_LOCALE));
    assertEquals(true, changer.isLongButShort("MACHEN TEMP 123", DE_LOCALE));
    // should be true, but isn't; therefore deprecated
    // assertEquals(true, changer.isLongButShort("MACH TEMP 123", DE_LOCALE));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testGetLongOrdersTranslated() throws Exception {
    ArrayList<String> orders = changer.getLongOrders(DE_LOCALE);
    assertEquals(22, orders.size());
    assertEquals("ARBEITEN", orders.get(0));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testGetLongOrderTokens() throws Exception {
    ArrayList<StringID> tokens = changer.getLongOrderTokens();
    assertEquals(22, tokens.size());
    assertEquals(EresseaConstants.OC_WORK, tokens.get(0));
    assertEquals(EresseaConstants.OC_GROW, tokens.get(21));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testGetTemp() throws Exception {
    assertEquals("TEMP", changer.getTemp(DE_LOCALE));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testAddGroupOrder() throws Exception {
    changer.addGroupOrder(unit, "group");
    assertEquals("GRUPPE \"group\"", unit.getOrders2().get(0).toString());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testGetOrderTranslation() throws Exception {
    assertEquals("LERNEN", changer.getOrderTranslation(EresseaConstants.OC_LEARN, unit));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testGetTokenLocalized() throws Exception {
    assertEquals("ALLES", changer.getTokenLocalized(DE_LOCALE, EresseaConstants.OC_ALL));
    assertEquals("TEMP 1", changer.getTokenLocalized(DE_LOCALE, UnitID.createTempID(data, settings,
        unit)));
    assertEquals("foo", changer.getTokenLocalized(DE_LOCALE, "foo"));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testGetOrderLocaleStringID() throws Exception {
    assertEquals("BEWACHEN", changer.getOrder(DE_LOCALE, EresseaConstants.OC_GUARD));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testGetOrderLocaleStringIDObjects() throws Exception {
    assertEquals("GIB 0 ALLES", changer.getOrder(DE_LOCALE, EresseaConstants.OC_GIVE, new Object[] {
        0, EresseaConstants.OC_ALL }));

    try {
      String order =
          changer.getOrder(DE_LOCALE, StringID.create("BOGUSCOMMAND"), new Object[] { 2 });
      assertEquals("BOGUSCOMMAND 2", order);
    } catch (Exception e) {
      fail("unexpected exception " + e);
    }

  }

  /**
   * @throws Exception
   */
  @Test
  public void testGetOrderStringIDLocale() throws Exception {
    assertEquals("MACHEN", changer.getOrder(EresseaConstants.OC_MAKE, DE_LOCALE));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testGetOrderStringIDLocaleObjects() throws Exception {
    assertEquals("GIB 0 ALLES", changer.getOrder(EresseaConstants.OC_GIVE, DE_LOCALE, new Object[] {
        0, EresseaConstants.OC_ALL }));
    try {
      changer.getOrder(StringID.create("BOGUSCOMMAND"), DE_LOCALE, new Object[] {});
      fail();
    } catch (RulesException e) {
      // ok
    }
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testExtractTempUnits() throws Exception {
    int sort = changer.extractTempUnits(data, 0, DE_LOCALE, unit);
    assertEquals(1, data.getUnits().size());
    assertEquals(0, data.tempUnits().size());
    assertEquals(0, sort);

    unit.addOrder("ORDER 1");
    unit.addOrder("MACHEN TEMP 2");
    unit.addOrder("CONTENT");
    unit.addOrder("ENDE");
    unit.addOrder("ORDER 2");
    sort = changer.extractTempUnits(data, 0, DE_LOCALE, unit);
    assertEquals(1, data.getUnits().size());
    assertEquals(1, data.tempUnits().size());
    assertEquals(1, sort);
    assertEquals(2, unit.getOrders2().size());
    assertEquals("ORDER 1", unit.getOrders2().get(0).toString());
    assertEquals("ORDER 2", unit.getOrders2().get(1).toString());

    Unit temp = null;
    for (Unit u : data.tempUnits().values()) {
      if (u != unit) {
        temp = u;
        break;
      }
    }
    assertNotNull(temp);
    assertTrue(temp.getID().intValue() < 0);
    assertEquals(1, temp.getOrders2().size());
    assertEquals("CONTENT", temp.getOrders2().get(0).toString());

  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testExtractTempUnitsWithName() throws Exception {
    unit.addOrder("MACHEN TEMP 2 \"He\"");
    unit.addOrder("ENDE");
    changer.extractTempUnits(data, 0, DE_LOCALE, unit);
    assertEquals(1, data.getUnits().size());
    assertEquals(1, data.tempUnits().size());
    assertEquals(0, unit.getOrders2().size());

    Unit temp = null;
    for (Unit u : data.tempUnits().values()) {
      if (u != unit) {
        temp = u;
        break;
      }
    }
    assertNotNull(temp);
    assertEquals(1, temp.getOrders2().size());
    assertEquals("BENENNEN EINHEIT \"He\"", temp.getOrders2().get(0).toString());

  }

}
