// class magellan.library.tasks.MovementInspectorTest
// created on Nov 12, 2012
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
package magellan.library.tasks;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import magellan.library.Building;
import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaRelationFactory;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

/**
 * Tests for MovementInspector
 *
 * @author stm
 */
public class MovementInspectorTest extends MagellanTestWithResources {

  private GameDataBuilder builder;
  private GameData gd;
  private EresseaRelationFactory processor;
  private Unit u;

  @BeforeClass
  public static void setUpBeforeClass() {
    GameDataBuilder.setNullResources(true);
  }

  @AfterClass
  public static void afterClass() {
    GameDataBuilder.setNullResources(false);
  }

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    builder = new GameDataBuilder();
    gd = builder.createSimpleGameData("eressea", 350, true);
    u = gd.getUnits().iterator().next();
    Building b = builder.addBuilding(gd, u.getRegion(), "burg", "Turm", "Turm", 10);
    builder.addTo(u, b);

    processor = ((EresseaRelationFactory) gd.getGameSpecificStuff().getRelationFactory());
    processor.stopUpdating();
  }

  /**
   * Tests {@link MovementInspector#findProblems(Unit)}.
   */
  @Test
  public final void testReviewUnitUnitSeverity() {
    u.clearOrders();
    u.addOrder("NACH o");
    refreshOrders(processor, gd);

    MovementInspector mInspector = new MovementInspector(gd);
    List<Problem> problems = mInspector.findProblems(u);
    Problem ownerProblem = null;
    for (Problem p : problems) {
      if (p.getType().equals(MovementInspector.MovementProblemTypes.OWNERLEAVES.getType())) {
        assertNull(ownerProblem);
        ownerProblem = p;
      }
    }
    assertNotNull(ownerProblem);
  }

  /**
   * Tests {@link MovementInspector#findProblems(Unit)}.
   */
  @Test
  public final void testReviewUnitUnitSeverity2() {
    u.clearOrders();
    u.addOrder("NACH o");
    u.addOrder("VERLASSE");
    refreshOrders(processor, gd);

    MovementInspector mInspector = new MovementInspector(gd);
    List<Problem> problems = mInspector.findProblems(u);
    Problem ownerProblem = null;
    for (Problem p : problems) {
      if (p.getType().equals(MovementInspector.MovementProblemTypes.OWNERLEAVES.getType())) {
        ownerProblem = p;
      }
    }
    assertNull(ownerProblem);
  }

  /**
   * Tests {@link MovementInspector#findProblems(Unit)}.
   */
  @Test
  public final void testReviewUnitUnitSeverity3() {
    Unit u2 = builder.addUnit(gd, "2", "Nummer 2", u.getFaction(), u.getRegion());
    builder.addTo(u2, u.getBuilding());
    u.clearOrders();
    u.addOrder("NACH o");
    u.addOrder("GIB 2 KOMMANDO");
    refreshOrders(processor, gd);

    MovementInspector mInspector = new MovementInspector(gd);
    List<Problem> problems = mInspector.findProblems(u);
    Problem ownerProblem = null;
    for (Problem p : problems) {
      if (p.getType().equals(MovementInspector.MovementProblemTypes.OWNERLEAVES.getType())) {
        ownerProblem = p;
      }
    }
    assertNull(ownerProblem);
  }

}
