// class magellan.library.impl.MagellanOrdersImplementationTest
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
package magellan.library.impl;

import static org.junit.Assert.assertEquals;
import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.completion.OrderParser;
import magellan.library.gamebinding.EresseaConstants;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

import org.junit.Before;
import org.junit.Test;

public class MagellanOrdersImplementationTest extends MagellanTestWithResources {

  private GameDataBuilder builder;
  private GameData data;
  private Unit unit;
  private OrderParser parser;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    builder = new GameDataBuilder();
    data = builder.createSimpleGameData();
    unit = data.getUnits().iterator().next();
    parser = data.getOrderParser();
  }

  @Test
  public final void testIsToken() {
    MagellanOrdersImplementation orders = getOrders(unit, "REKRUTIERE 1");
    assertEquals(true, orders.isToken(orders.get(0), 0, EresseaConstants.O_RECRUIT));

    orders = getOrders(unit, "ARBEITEN");
    assertEquals(true, orders.isToken(orders.get(0), 0, EresseaConstants.O_WORK));

    orders = getOrders(unit, "ARBEITE");
    assertEquals(true, orders.isToken(orders.get(0), 0, EresseaConstants.O_WORK));

    orders = getOrders(unit, "ARBEITEND");
    assertEquals(false, orders.isToken(orders.get(0), 0, EresseaConstants.O_WORK));
  }

  protected MagellanOrdersImplementation getOrders(Unit unit2, String... string) {
    MagellanOrdersImplementation orders = new MagellanOrdersImplementation(unit);
    for (String order : string) {
      orders.add(parser.parse(order, DE_LOCALE));
    }
    return orders;
  }

}
