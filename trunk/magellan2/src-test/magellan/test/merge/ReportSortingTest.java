// class magellan.test.merge.ReportSortingTest
// created on Nov 16, 2010
//
// Copyright 2003-2010 by magellan project team
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
package magellan.test.merge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.GameDataMerger;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

import org.junit.Test;

public class ReportSortingTest extends MagellanTestWithResources {

  @Test
  public void testUnitSorting() throws Exception {
    GameDataBuilder builder = new GameDataBuilder();
    GameData gd1 = builder.createSimplestGameData("Eressea", 350, false);
    Faction faction11 = gd1.getFactions().iterator().next();
    gd1.setOwnerFaction(faction11.getID());
    Region region11 = gd1.getRegions().iterator().next();
    Faction faction12 = builder.addFaction(gd1, "f2", "anders", "Menschen", 1);
    Unit unit111 = builder.addUnit(gd1, "11", "1_1", faction11, region11);
    Unit unit112 = builder.addUnit(gd1, "12", "1_2", faction11, region11);
    Unit unit121 = builder.addUnit(gd1, "21", "2_1", faction12, region11);
    unit111.setSortIndex(1);
    unit112.setSortIndex(2);
    unit121.setSortIndex(3);
    // unit "22" invisible

    GameData gd2 = builder.createSimplestGameData("Eressea", 350, false);
    Region region21 = gd2.getRegions().iterator().next();
    Faction faction22 = builder.addFaction(gd2, "f2", "anders", "Menschen", 1);
    gd2.setOwnerFaction(faction22.getID());
    // invisible Unit unit211 = builder.addUnit(gd2, "11", "1_1", faction21, region21);
    // invisible Unit unit212 = builder.addUnit(gd2, "12", "1_2", faction22, region21);
    Unit unit221 = builder.addUnit(gd2, "21", "2_1", faction22, region21);
    Unit unit222 = builder.addUnit(gd2, "22", "2_2", faction22, region21);
    unit221.setSortIndex(1);
    unit222.setSortIndex(2);

    GameData gdm = GameDataMerger.merge(gd1, gd2);

    ArrayList<Unit> units = new ArrayList<Unit>(gdm.getUnits());
    assertSame(4, units.size());

    //
    assertEquals("11", units.get(0).getID().toString());
    assertEquals("12", units.get(1).getID().toString());
    assertEquals("21", units.get(2).getID().toString());
    assertEquals("22", units.get(3).getID().toString());

    //
    assertEquals(0, units.get(0).getSortIndex());
    assertEquals(1, units.get(1).getSortIndex());
    assertEquals(2, units.get(2).getSortIndex());
    assertEquals(3, units.get(3).getSortIndex());

  }

  @Test
  public void testUnitSorting2() throws Exception {
    GameDataBuilder builder = new GameDataBuilder();

    GameData gd1 = builder.createSimplestGameData("Eressea", 350, false);
    Faction faction11 = gd1.getFactions().iterator().next();
    gd1.setOwnerFaction(faction11.getID());
    Region region11 = gd1.getRegions().iterator().next();
    Faction faction12 = builder.addFaction(gd1, "f2", "anders", "Menschen", 1);
    builder.addUnit(gd1, "10", "1_0", faction11, region11).setSortIndex(1);
    builder.addUnit(gd1, "11", "1_1", faction11, region11).setSortIndex(2);
    builder.addUnit(gd1, "12", "1_2", faction11, region11).setSortIndex(3);
    builder.addUnit(gd1, "22", "2_2", faction12, region11).setSortIndex(4);
    // unit "22" invisible

    GameData gd2 = builder.createSimplestGameData("Eressea", 350, false);
    Faction faction21 = gd1.getFactions().iterator().next();
    Region region21 = gd2.getRegions().iterator().next();
    Faction faction22 = builder.addFaction(gd2, "f2", "anders", "Menschen", 1);
    gd2.setOwnerFaction(faction22.getID());
    builder.addUnit(gd2, "11", "1_1", faction21, region21).setSortIndex(1);
    builder.addUnit(gd2, "21", "2_1", faction22, region21).setSortIndex(2);
    builder.addUnit(gd2, "22", "2_2", faction22, region21).setSortIndex(3);

    GameData gdm = GameDataMerger.merge(gd1, gd2);

    ArrayList<Unit> units = new ArrayList<Unit>(gdm.getUnits());
    assertSame(5, units.size());

    //
    assertEquals("10", units.get(0).getID().toString());
    assertEquals("11", units.get(1).getID().toString());
    assertEquals("12", units.get(2).getID().toString());
    assertEquals("21", units.get(3).getID().toString());
    assertEquals("22", units.get(4).getID().toString());

    //
    assertEquals(0, units.get(0).getSortIndex());
    assertEquals(1, units.get(1).getSortIndex());
    assertEquals(2, units.get(2).getSortIndex());
    assertEquals(3, units.get(3).getSortIndex());
    assertEquals(4, units.get(4).getSortIndex());

  }

  @Test
  public void testUnitSorting2b() throws Exception {
    GameDataBuilder builder = new GameDataBuilder();
    GameData gd1 = builder.createSimplestGameData("Eressea", 350, false);
    Faction faction11 = gd1.getFactions().iterator().next();
    gd1.setOwnerFaction(faction11.getID());
    Region region11 = gd1.getRegions().iterator().next();
    Faction faction12 = builder.addFaction(gd1, "f2", "anders", "Menschen", 1);
    builder.addUnit(gd1, "11", "1_1", faction11, region11).setSortIndex(1);
    builder.addUnit(gd1, "12", "1_2", faction11, region11).setSortIndex(2);
    builder.addUnit(gd1, "22", "2_2", faction12, region11).setSortIndex(3);
    // unit "22" invisible

    GameData gd2 = builder.createSimplestGameData("Eressea", 350, false);
    Faction faction21 = gd2.getFactions().iterator().next();
    Region region21 = gd2.getRegions().iterator().next();
    Faction faction22 = builder.addFaction(gd2, "f2", "anders", "Menschen", 1);
    gd2.setOwnerFaction(faction22.getID());
    builder.addUnit(gd2, "20", "1_0", faction22, region21).setSortIndex(1);
    builder.addUnit(gd2, "11", "1_1", faction21, region21).setSortIndex(2);
    // builder.addUnit(gd2, "12", "1_2", faction11, region11).setSortIndex(2);
    builder.addUnit(gd2, "21", "2_1", faction22, region21).setSortIndex(3);
    builder.addUnit(gd2, "22", "2_2", faction22, region21).setSortIndex(4);

    GameData gdm = GameDataMerger.merge(gd1, gd2);

    ArrayList<Unit> units = new ArrayList<Unit>(gdm.getUnits());
    assertSame(5, units.size());

    assertOrder(gdm, "11", "12");
    assertOrder(gdm, "20", "21");
    assertOrder(gdm, "21", "22");

    // this is correct, but not enforced
    // assertEquals("20", units.get(0).getID().toString());
    // assertEquals("11", units.get(1).getID().toString());
    // assertEquals("12", units.get(2).getID().toString());
    // assertEquals("21", units.get(3).getID().toString());
    // assertEquals("22", units.get(4).getID().toString());
    //
    // assertEquals(0, units.get(0).getSortIndex());
    // assertEquals(1, units.get(1).getSortIndex());
    // assertEquals(2, units.get(2).getSortIndex());
    // assertEquals(3, units.get(3).getSortIndex());
    // assertEquals(4, units.get(4).getSortIndex());

  }

  private void assertOrder(GameData gdm, String id1, String id2) {
    assertTrue("index of " + id1 + " not < " + id2, gdm.getUnit(
        EntityID.createEntityID(id1, gdm.base)).getSortIndex() < gdm.getUnit(
        EntityID.createEntityID(id2, gdm.base)).getSortIndex());
  }

  @Test
  public void testUnitSorting3() throws Exception {
    GameDataBuilder builder = new GameDataBuilder();
    GameData gd1 = builder.createSimplestGameData("Eressea", 350, false);
    Faction faction11 = gd1.getFactions().iterator().next();
    gd1.setOwnerFaction(faction11.getID());
    Region region11 = gd1.getRegions().iterator().next();
    Faction faction12 = builder.addFaction(gd1, "f2", "anders", "Menschen", 1);
    Faction faction13 = builder.addFaction(gd1, "f3", "Gobos", "Goblins", 2);
    Unit unit111 = builder.addUnit(gd1, "11", "1_1", faction11, region11);
    Unit unit112 = builder.addUnit(gd1, "12", "1_2", faction11, region11);
    Unit unit121 = builder.addUnit(gd1, "21", "2_1", faction12, region11);
    Unit unit132 = builder.addUnit(gd1, "32", "3_2", faction13, region11);
    unit111.setSortIndex(1);
    unit112.setSortIndex(2);
    unit121.setSortIndex(3);
    unit132.setSortIndex(4);
    // unit "22" and "31" invisible

    GameData gd2 = builder.createSimplestGameData("Eressea", 350, false);
    Region region21 = gd2.getRegions().iterator().next();
    Faction faction22 = builder.addFaction(gd2, "f2", "anders", "Menschen", 1);
    gd2.setOwnerFaction(faction22.getID());
    Faction faction23 = builder.addFaction(gd2, "f3", "Gobos", "Goblins", 2);
    Unit unit221 = builder.addUnit(gd2, "21", "2_1", faction22, region21);
    Unit unit222 = builder.addUnit(gd2, "22", "2_2", faction22, region21);
    Unit unit231 = builder.addUnit(gd2, "31", "3_1", faction23, region21);
    Unit unit232 = builder.addUnit(gd2, "32", "3_2", faction23, region21);
    unit221.setSortIndex(1);
    unit222.setSortIndex(2);
    unit231.setSortIndex(3);
    unit232.setSortIndex(4);
    // units 11, 12 invisible

    GameData gd3 = builder.createSimplestGameData("Eressea", 350, false);
    Region region31 = gd3.getRegions().iterator().next();
    Faction faction33 = builder.addFaction(gd3, "f3", "Gobos", "Goblins", 2);
    gd3.setOwnerFaction(faction33.getID());
    // invisible Unit unit211 = builder.addUnit(gd2, "11", "1_1", faction21, region21);
    // invisible Unit unit212 = builder.addUnit(gd2, "12", "1_2", faction22, region21);
    // Unit unit221 = builder.addUnit(gd2, "21", "2_1", faction22, region21);
    // Unit unit222 = builder.addUnit(gd2, "22", "2_2", faction22, region21);
    Unit unit331 = builder.addUnit(gd3, "31", "3_1", faction33, region31);
    Unit unit332 = builder.addUnit(gd3, "32", "3_2", faction33, region31);
    unit331.setSortIndex(1);
    unit332.setSortIndex(2);

    GameData gdm = GameDataMerger.merge(gd1, gd2);
    GameData gdm2 = GameDataMerger.merge(gdm, gd3);

    ArrayList<Unit> units = new ArrayList<Unit>(gdm2.getUnits());
    assertSame(6, units.size());

    assertOrder(gdm, "11", "12");
    assertOrder(gdm, "21", "22");
    // not enforced:
    // assertOrder(gdm2, "31", "32");

    assertOrder(gdm2, "11", "12");
    assertOrder(gdm2, "21", "22");
    assertOrder(gdm2, "31", "32");

    // this would be correct, but is not enforced...
    // assertEquals("11", units.get(0).getID().toString());
    // assertEquals("12", units.get(1).getID().toString());
    // assertEquals("21", units.get(2).getID().toString());
    // assertEquals("22", units.get(3).getID().toString());
    // assertEquals("31", units.get(4).getID().toString());
    // assertEquals("32", units.get(5).getID().toString());
    //
    // assertEquals(0, units.get(0).getSortIndex());
    // assertEquals(1, units.get(1).getSortIndex());
    // assertEquals(2, units.get(2).getSortIndex());
    // assertEquals(3, units.get(3).getSortIndex());
    // assertEquals(4, units.get(4).getSortIndex());
    // assertEquals(5, units.get(5).getSortIndex());

  }

}
