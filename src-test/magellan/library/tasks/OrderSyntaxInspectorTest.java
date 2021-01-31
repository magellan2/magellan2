// class magellan.library.tasks.OrderSyntaxInspectorTest
// created on Jan 30, 2021
//
// Copyright 2003-2021 by magellan project team
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
package magellan.library.tasks;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaRelationFactory;
import magellan.library.tasks.OrderSyntaxInspector.OrderSyntaxProblemTypes;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

public class OrderSyntaxInspectorTest extends MagellanTestWithResources {

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
    faction0.setTrustLevel(Faction.TL_PRIVILEGED);
    region0 = data.getRegions().iterator().next();
    relationFactory = ((EresseaRelationFactory) data.getGameSpecificStuff().getRelationFactory());
    relationFactory.stopUpdating();
  }

  @Test
  public void testFindProblemsHappy() {
    unit.clearOrders();

    process(region0);

    OrderSyntaxInspector inspector = new OrderSyntaxInspector(data);
    List<Problem> problems = inspector.findProblems(unit);
    assertEquals(1, problems.size());
    assertEquals(OrderSyntaxProblemTypes.NO_ORDERS.getType(), problems.iterator().next().getType());

    unit.addOrder("GIB 0 ALLES Silber");
    process(region0);

    problems = inspector.findProblems(unit);
    assertEquals(1, problems.size());
    assertEquals(OrderSyntaxProblemTypes.NO_LONG_ORDER.getType(), problems.iterator().next().getType());

    unit.addOrder("LERNE Ausdauer");
    process(region0);

    problems = inspector.findProblems(unit);
    assertEquals(0, problems.size());
  }

  @Test
  public void testFindProblemsAttackiere() {
    unit.clearOrders();
    builder.addUnit(data, "foe", "Feind", builder.addFaction(data, "foe", "Feinde", "Menschen", 2), region0);
    unit.addOrder("ATTACKIERE foe");

    process(region0);

    OrderSyntaxInspector inspector = new OrderSyntaxInspector(data);
    List<Problem> problems = inspector.findProblems(unit);
    assertEquals(1, problems.size());
    assertEquals(OrderSyntaxProblemTypes.NO_LONG_ORDER.getType(), problems.iterator().next().getType());

    unit.addOrder("LERNE Ausdauer");
    process(region0);

    problems = inspector.findProblems(unit);
    assertEquals(0, problems.size());

  }

  private void process(Region region) {

    relationFactory.processRegionNow(region);
  }

}
