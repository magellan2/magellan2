// class magellan.plugin.groupeditor.GroupEditorTest
// created on Feb 14, 2015
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
package magellan.plugin.groupeditor;

import static org.junit.Assert.assertEquals;

import java.util.regex.Pattern;

import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Order;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.test.GameDataBuilder;

import org.junit.Before;
import org.junit.Test;

/**
 * @author stm
 */
public class GroupEditorTest {

  private static final String HELP = "HELFE";
  private static final String NOT = "NICHT";
  private static final String GUARD = "BEWACHE";
  private static final String GIVE = "GIB";
  private static final String STEALTH = "PARTEITARNUNG";
  private static final String SILVER = "SILBER";
  private static final String FIGHT = "KÄMPFE";
  private static final String ALL = "ALLES";
  private GroupEditorTableModel model;
  private GameDataBuilder builder;
  private GameData world;
  private Unit unit;
  private Faction faction2;

  protected void assertHelpOrders(Unit aUnit, int number) {
    int count = 0;
    for (Order order : aUnit.getOrders2()) {
      if (order.getText().startsWith(HELP)) {
        count++;
      }
    }
    assertEquals(number, count);
  }

  protected void assertOrder(Unit aUnit, Faction faction, String state, boolean activate) {
    Pattern pattern =
        Pattern.compile(HELP + "  *" + faction.getID() + "  *" + state + "(  *" + NOT + "){0,1}");
    boolean found = false;
    for (Order order : aUnit.getOrders2()) {
      if (pattern.matcher(order.getText()).matches()) {
        if (activate != order.getText().endsWith(NOT)) {
          found = true;
        } else {
          assertEquals(pattern.toString(), order.getText());
        }
      }
    }
    if (!found) {
      assertEquals(pattern.toString(), orders(aUnit));
    }
  }

  private String orders(Unit aUnit) {
    StringBuilder allOrders = new StringBuilder();
    for (Order order : aUnit.getOrders2()) {
      allOrders.append(order.getText()).append("\n");
    }
    return allOrders.toString();
  }

  /**
   */
  @Before
  public void setUp() throws Exception {
    initFixture(null);
  }

  private void initFixture(String gameName) throws Exception {
    model = new GroupEditorTableModel();
    builder = new GameDataBuilder();
    if (gameName != null) {
      builder.setGameName(gameName);
    }
    world = builder.createSimpleGameData();
    unit = world.getUnits().iterator().next();
    faction2 = builder.addFaction(world, "2", "F2", "human", 2);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSave() throws Exception {
    builder.addAlliance(unit.getFaction(), faction2, EresseaConstants.A_GUARD);

    model.setOwner(unit.getFaction());
    AllianceState state = new AllianceState(world);
    state.addCategory(world.getRules().getAllianceCategory(EresseaConstants.OC_GIVE));
    model.setValueAt(state, 0, model.getColumnCount() - 1);
    model.save();
    assertHelpOrders(unit, 2);
    assertOrder(unit, faction2, GUARD, false);
    assertOrder(unit, faction2, GIVE, true);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSaveAll() throws Exception {
    builder.addAlliance(unit.getFaction(), faction2, EresseaConstants.A_GUARD);

    model.setOwner(unit.getFaction());
    AllianceState state = new AllianceState(world);
    state.addCategory(world.getRules().getAllianceCategory(EresseaConstants.OC_ALL));
    model.setValueAt(state, 0, model.getColumnCount() - 1);
    model.save();
    assertHelpOrders(unit, 1);
    assertOrder(unit, faction2, ALL, true);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSaveNothing() throws Exception {
    builder.addAlliance(unit.getFaction(), faction2, EresseaConstants.A_GUARD);

    model.setOwner(unit.getFaction());
    AllianceState state = new AllianceState(world);
    model.setValueAt(state, 0, model.getColumnCount() - 1);
    model.save();
    assertHelpOrders(unit, 1);
    assertOrder(unit, faction2, ALL, false);
  }

  /**
   * Test that correct sub-orders are added if help status is ALL.
   */
  @Test
  public void testSaveAllNot() throws Exception {
    builder.addAlliance(unit.getFaction(), faction2, world.getRules().getAllianceCategory(
        EresseaConstants.OC_ALL).getBitMask());

    model.setOwner(unit.getFaction());
    AllianceState state = new AllianceState(world);
    state.addCategory(world.getRules().getAllianceCategory(EresseaConstants.OC_GIVE));
    model.setValueAt(state, 0, model.getColumnCount() - 1);
    model.save();
    assertHelpOrders(unit, 5);
    assertOrder(unit, faction2, GIVE, true);
    assertOrder(unit, faction2, GUARD, false);
    assertOrder(unit, faction2, STEALTH, false);
    assertOrder(unit, faction2, SILVER, false);
    assertOrder(unit, faction2, FIGHT, false);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSaveAllNotE3() throws Exception {
    initFixture(GameDataBuilder.E3);
    builder.addAlliance(unit.getFaction(), faction2, world.getRules().getAllianceCategory(
        EresseaConstants.OC_ALL).getBitMask());

    model.setOwner(unit.getFaction());
    AllianceState state = new AllianceState(world);
    state.addCategory(world.getRules().getAllianceCategory(EresseaConstants.OC_GIVE));
    model.setValueAt(state, 0, model.getColumnCount() - 1);
    model.save();
    assertOrder(unit, faction2, GIVE, true);
    assertOrder(unit, faction2, GUARD, false);
    assertOrder(unit, faction2, SILVER, false);
    assertHelpOrders(unit, 3);
  }

}
