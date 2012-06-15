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
import magellan.library.GameData;
import magellan.library.Unit;
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
    changer = (EresseaOrderChanger) data.getRules().getGameSpecificStuff().getOrderChanger();
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

}
