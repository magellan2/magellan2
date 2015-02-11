// class magellan.library.gamebinding.E3AOrderParserTest
// created on Jun 12, 2012
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
import static org.junit.Assert.assertSame;

import java.util.Collections;
import java.util.List;

import magellan.client.completion.AutoCompletion;
import magellan.library.Building;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.gamebinding.e3a.E3AOrderCompleter;
import magellan.library.gamebinding.e3a.E3AOrderParser;
import magellan.library.relation.MaintenanceRelation;
import magellan.library.rules.ItemType;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for E3 order parser. inherits from EresseaOrderParserTest to ensure functionality of E2
 * where required.
 */
public class E3RelationFactoryTest extends MagellanTestWithResources {

  @SuppressWarnings("unused")
  private E3AOrderParser parser;
  @SuppressWarnings("unused")
  private E3AOrderCompleter completer;
  private GameDataBuilder builder;
  private GameData data;
  private AutoCompletion completion;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    builder = new GameDataBuilder();
    builder.setGameName("E3");
    data = builder.createSimpleGameData();

    Region region = data.getRegions().iterator().next();
    Faction faction = data.getFactions().iterator().next();
    builder.addBuilding(data, region, "burg", "Burg", "groﬂe Burg", 200);
    builder.addShip(data, region, "ship", "Einbaum", "ein Boot", 50);
    builder.addUnit(data, "zwei", "Zweite", faction, region);

    parser = new E3AOrderParser(data);
    completion = new AutoCompletion(context);
    completer = new E3AOrderCompleter(data, completion);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser#initCommands()}.
   */
  @Test
  public void testBuildingMaintenance() {
    Region region = data.getRegions().iterator().next();
    Unit unit = data.getUnits().iterator().next();
    Building berg = builder.addBuilding(data, region, "berg", "Bergwerk", "Ein Wergberk", 10);
    unit.addOrder("BETRETE BURG berg");

    refreshOrders();

    assertEquals(1, unit.getRelations(MaintenanceRelation.class).size());
    checkRelation(unit, 0, berg, getItemType(EresseaConstants.I_USILVER), true);

    builder.addItem(data, unit, "Silber", 1000);

    refreshOrders();

    assertEquals(1, unit.getRelations(MaintenanceRelation.class).size());
    checkRelation(unit, 500, berg, getItemType(EresseaConstants.I_USILVER), false);

    unit.addOrder("BEZAHLE NICHT");
    refreshOrders();

    assertEquals(0, unit.getRelations(MaintenanceRelation.class).size());
    // checkRelation(unit, 250, berg, getItemType(EresseaConstants.I_USILVER), true);

    unit.clearOrders();
    unit.addOrder("BETRETE BURG berg");
    berg.setEffects(Collections
        .singletonList("Der Zahn der Zeit kann diesen Mauern nichts anhaben. xyz"));
    refreshOrders();

    assertEquals(0, unit.getRelations(MaintenanceRelation.class).get(0).getCosts());
  }

  protected void refreshOrders() {
    for (Region r : data.getRegions()) {
      ((EresseaRelationFactory) data.getGameSpecificStuff().getRelationFactory()).processOrders(r);
    }
  }

  private void checkRelation(Unit unit, int costs, UnitContainer build, ItemType itemType,
      boolean warning) {
    List<MaintenanceRelation> rels = unit.getRelations(MaintenanceRelation.class);
    MaintenanceRelation rel = rels.iterator().next();

    String text = "testing relation of " + unit;

    assertSame(text, build, rel.target);
    assertEquals(text, costs, rel.getCosts());
    assertEquals(text, getItemType(EresseaConstants.I_USILVER), rel.itemType);
    assertSame(text, unit, rel.origin);
    assertSame(text, unit, rel.source);
    assertEquals(text, warning, rel.warning);
  }

  private ItemType getItemType(StringID iType) {
    return data.getRules().getItemType(iType);
  }

}
