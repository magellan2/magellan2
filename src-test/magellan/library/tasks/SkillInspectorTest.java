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

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaRelationFactory;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

/**
 * @author stm
 */
public class SkillInspectorTest extends MagellanTestWithResources {

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
    gd = builder.createSimplestGameData();
    u = builder.addUnit(gd, "U1", gd.getRegions().iterator().next());

    processor = ((EresseaRelationFactory) gd.getGameSpecificStuff().getRelationFactory());
    processor.stopUpdating();
  }

  /***/
  @Test
  public final void testNoProblem() {
    Unit u2 = builder.addUnit(gd, "u2", "Other", u.getFaction(), u.getRegion());
    builder.addSkill(u, "Ausdauer", 2);
    builder.addSkill(u2, "Ausdauer", 2);

    u.clearOrders();
    u.addOrder("GIB u2 ALLES PERSONEN");
    refreshOrders(processor, gd);

    SkillInspector mInspector = new SkillInspector(gd);
    List<Problem> problems = mInspector.findProblems(u);
    assertEquals(0, problems.size());
  }

  /***/
  @Test
  public final void testTransferProblem() {
    Unit u2 = builder.addUnit(gd, "u2", "Other", u.getFaction(), u.getRegion());
    builder.addSkill(u, "Ausdauer", 3);
    builder.addSkill(u2, "Ausdauer", 2);

    u.clearOrders();
    u.addOrder("GIB u2 ALLES PERSONEN");
    refreshOrders(processor, gd);

    SkillInspector mInspector = new SkillInspector(gd);
    List<Problem> problems = mInspector.findProblems(u);
    assertEquals(1, problems.size());
    assertEquals(u, problems.get(0).getObject());
  }

  /***/
  @Test
  public final void testTempTransferProblem() {
    Unit u2 = builder.addUnit(gd, "u2", "Other", u.getFaction(), u.getRegion());
    builder.addSkill(u, "Ausdauer", 3);
    builder.addSkill(u2, "Ausdauer", 2);

    u.setPersons(3);
    u.clearOrders();
    u.addOrder("MACHE TEMP x");
    u.addOrder("ENDE");
    u.addOrder("GIB TEMP x 1 PERSONEN");
    u.addOrder("GIB u2 ALLES PERSONEN");
    refreshOrders(processor, gd);

    SkillInspector mInspector = new SkillInspector(gd);
    List<Problem> problems = mInspector.findProblems(u);
    assertEquals(1, problems.size());
    assertEquals(u, problems.get(0).getObject());
  }
}
