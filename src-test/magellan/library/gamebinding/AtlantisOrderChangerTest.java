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
package magellan.library.gamebinding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import magellan.library.GameData;
import magellan.library.Unit;
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

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    setLocale(EN_LOCALE);
    MagellanTestWithResources.setUpBeforeClass();
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
    changer = (AtlantisOrderChanger) data.getRules().getGameSpecificStuff().getOrderChanger();
  }

  @Test
  public final void testCreateOrder() {
    fail("Not yet implemented");
  }

  @Test
  public final void testAddCombatOrder() {
    fail("Not yet implemented");
  }

  @Test
  public final void testAddDescribeUnitContainerOrder() {
    fail("Not yet implemented");
  }

  @Test
  public final void testAddDescribeUnitOrder() {
    fail("Not yet implemented");
  }

  @Test
  public final void testAddDescribeUnitPrivateOrder() {
    fail("Not yet implemented");
  }

  @Test
  public final void testAddHideOrder() {
    fail("Not yet implemented");
  }

  @Test
  public final void testAddGroupOrder() {
    fail("Not yet implemented");
  }

  @Test
  public final void testAddNamingOrderUnitString() {
    fail("Not yet implemented");
  }

  @Test
  public final void testAddNamingOrderUnitUnitContainerString() {
    fail("Not yet implemented");
  }

  @Test
  public final void testAddRecruitOrder() {
    fail("Not yet implemented");
  }

  @Test
  public final void testAddGiveOrder() {
    unit.clearOrders();
    Unit unit2 = builder.addUnit(data, "2", "U2", unit.getFaction(), unit.getRegion());
    changer.addGiveOrder(unit, unit2, 1, AtlantisConstants.I_WOOD, "foo");
    assertEquals("GIVE 2 1 wood ; foo", unit.getOrders2().get(0).toString());

    changer.addGiveOrder(unit, unit2, 5, AtlantisConstants.I_USILVER, null);
    assertEquals("PAY 2 5", unit.getOrders2().get(1).toString());

    changer.addGiveOrder(unit, unit2, 3, EresseaConstants.I_MEN, null);
    assertEquals("TRANSFER 2 3", unit.getOrders2().get(2).toString());
  }

  @Test
  public final void testAddMultipleHideOrder() {
    fail("Not yet implemented");
  }

  @Test
  public final void testDisableLongOrders() {
    fail("Not yet implemented");
  }

  @Test
  public final void testIsLongOrderString() {
    fail("Not yet implemented");
  }

  @Test
  public final void testIsLongOrderOrder() {
    fail("Not yet implemented");
  }

  @Test
  public final void testAreCompatibleLongOrders() {
    fail("Not yet implemented");
  }

  @Test
  public final void testGetOrderTranslation() {
    fail("Not yet implemented");
  }

  @Test
  public final void testGetOrderLocaleObject() {
    fail("Not yet implemented");
  }

  @Test
  public final void testGetOrderLocaleStringIDObjectArray() {
    fail("Not yet implemented");
  }

  @Test
  public final void testGetOrderStringIDLocaleObjectArray() {
    fail("Not yet implemented");
  }

  @Test
  public final void testExtractTempUnits() {
    fail("Not yet implemented");
  }

  @Test
  public final void testGetTempOrders() {
    fail("Not yet implemented");
  }

}
