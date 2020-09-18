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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import magellan.library.GameData;
import magellan.library.Order;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.relation.MaintenanceRelation;
import magellan.library.relation.UnitRelation;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

/**
 * @author stm
 * @version 1.0, Jun 20, 2012
 */
public class LearnOrderTest extends MagellanTestWithResources {

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
    unit.clearOrders();
    unit.addOrder("LERNE Ausdauer");

    Order order = unit.getOrders2().get(0);
    assertTrue(order instanceof LearnOrder);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 0);
    assertNull(order.getProblem());

    assertEquals(0, unit.getRelations().size());
  }

  @Test
  public final void testExecuteCost() {
    unit.clearOrders();
    unit.addOrder("LERNE Taktik");
    builder.addItem(data, unit, "Silber", 200);

    Order order = unit.getOrders2().get(0);
    assertTrue(order instanceof LearnOrder);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 0);
    assertNull(order.getProblem());

    List<UnitRelation> relations = unit.getRelations();
    assertEquals(1, relations.size());
    MaintenanceRelation relation = (MaintenanceRelation) relations.get(0);
    assertFalse(relation.warning);
    assertEquals(EresseaConstants.I_USILVER, relation.itemType.getID());
    assertEquals(200, relation.getCosts());
    assertEquals(0, relation.line);
    assertSame(unit, relation.origin);
    assertSame(unit, relation.source);
    assertEquals("Lernen", relation.getReason());
  }

  @Test
  public final void testExecuteCostUnpaid() {
    unit.clearOrders();
    unit.addOrder("LERNE Taktik");

    Order order = unit.getOrders2().get(0);
    assertTrue(order instanceof LearnOrder);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 0);
    assertNull(order.getProblem());

    List<UnitRelation> relations = unit.getRelations();
    assertEquals(1, relations.size());
    MaintenanceRelation relation = (MaintenanceRelation) relations.get(0);
    assertTrue(relation.warning);
    assertEquals(EresseaConstants.I_USILVER, relation.itemType.getID());
    assertEquals(0, relation.getCosts());
  }

  @Test
  public final void testExecuteWrongCost() {
    unit.clearOrders();
    unit.addOrder("LERNE Taktik 999");
    builder.addItem(data, unit, "Silber", 1000);

    Order order = unit.getOrders2().get(0);
    assertTrue(order instanceof LearnOrder);
    order.execute(new EresseaRelationFactory.EresseaExecutionState(data), data, unit, 0);
    assertNotNull(order.getProblem());

    List<UnitRelation> relations = unit.getRelations();
    assertEquals(1, relations.size());
    MaintenanceRelation relation = (MaintenanceRelation) relations.get(0);
    assertEquals(999, relation.getCosts());
  }
}
