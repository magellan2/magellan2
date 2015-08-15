// class magellan.library.tasks.TransferInspectorTest
// created on May 29, 2015
//
// Copyright 2003-2015 by magellan project team
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
import static org.junit.Assert.assertTrue;

import java.util.List;

import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaRelationFactory;
import magellan.library.tasks.TransferInspector.TransferProblemTypes;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

import org.junit.Before;
import org.junit.Test;

/**
 * @author stm
 */
public class TransferInspectorTest extends MagellanTestWithResources {

  private GameDataBuilder builder;
  private GameData gd;
  private EresseaRelationFactory processor;
  private Unit u;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    builder = new GameDataBuilder();
    gd = builder.createSimpleGameData("eressea", 350, true);
    u = gd.getUnits().iterator().next();

    processor = ((EresseaRelationFactory) gd.getGameSpecificStuff().getRelationFactory());
    processor.stopUpdating();
  }

  /***/
  @Test
  public final void testLost() {
    u.clearOrders();
    builder.addItem(gd, u, "Schwert", 5);
    u.addOrder("GIB 0 ALLES PERSONEN");
    refreshOrders(processor, gd);

    TransferInspector mInspector = new TransferInspector(gd);
    List<Problem> problems = mInspector.findProblems(u);
    assertEquals(1, problems.size());
    Problem p = problems.get(0);
    assertTrue(p.toString().length() > 0);
    assertEquals(TransferProblemTypes.LOST_ITEMS.getType(), p.getType());
    assertEquals(u, p.getObject());
  }

  /***/
  @Test
  public final void testNoProblem() {
    u.clearOrders();
    builder.addItem(gd, u, "Schwert", 5);
    u.addOrder("GIB 0 ALLES PERSONEN");
    u.addOrder("GIB 0 ALLES Schwert");
    refreshOrders(processor, gd);

    TransferInspector mInspector = new TransferInspector(gd);
    List<Problem> problems = mInspector.findProblems(u);
    assertEquals(0, problems.size());
  }

}
