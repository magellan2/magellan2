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
package magellan.library.merge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;

import org.junit.Test;

import magellan.library.Building;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.GameDataMerger;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.io.GameDataReader;
import magellan.library.io.file.FileTypeFactory;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;
import magellan.test.merge.WriteGameData;

public class ReportSortingTest extends MagellanTestWithResources {

  /**
   * Tests for correct unit sorting even if some units are invisible for other factions.
   * 
   * @throws Exception
   */
  @Test
  public void testUnitSorting() throws Exception {
    GameDataBuilder builder = new GameDataBuilder();
    GameData gd1 = builder.createSimplestGameData("Eressea", 350, false);
    Faction faction11 = gd1.getFactions().iterator().next();
    gd1.setOwnerFaction(faction11.getID());
    Region region11 = gd1.getRegions().iterator().next();
    Faction faction12 = builder.addFaction(gd1, "f2", "anders", "Menschen", 1);
    Unit unit111 = builder.addUnit(gd1, "z11", "1_1", faction11, region11);
    Unit unit112 = builder.addUnit(gd1, "y12", "1_2", faction11, region11);
    Unit unit121 = builder.addUnit(gd1, "x21", "2_1", faction12, region11);
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
    Unit unit221 = builder.addUnit(gd2, "x21", "2_1", faction22, region21);
    Unit unit222 = builder.addUnit(gd2, "w22", "2_2", faction22, region21);
    unit221.setSortIndex(1);
    unit222.setSortIndex(2);

    GameData gdm = GameDataMerger.merge(gd1, gd2);

    ArrayList<Unit> units = new ArrayList<Unit>(gdm.getUnits());
    assertSame(4, units.size());

    assertOrder(gdm, "z11", "y12", "x21", "w22");

    //
    assertEquals("z11", units.get(0).getID().toString());
    assertEquals("y12", units.get(1).getID().toString());
    assertEquals("x21", units.get(2).getID().toString());
    assertEquals("w22", units.get(3).getID().toString());

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
    builder.addUnit(gd1, "z10", "1_0", faction11, region11).setSortIndex(1);
    builder.addUnit(gd1, "x11", "1_1", faction11, region11).setSortIndex(2);
    builder.addUnit(gd1, "w12", "1_2", faction11, region11).setSortIndex(3);
    builder.addUnit(gd1, "u22", "2_2", faction12, region11).setSortIndex(4);
    // unit "22" invisible

    GameData gd2 = builder.createSimplestGameData("Eressea", 350, false);
    Faction faction21 = gd1.getFactions().iterator().next();
    Region region21 = gd2.getRegions().iterator().next();
    Faction faction22 = builder.addFaction(gd2, "f2", "anders", "Menschen", 1);
    gd2.setOwnerFaction(faction22.getID());
    builder.addUnit(gd2, "x11", "1_1", faction21, region21).setSortIndex(1);
    builder.addUnit(gd2, "v21", "2_1", faction22, region21).setSortIndex(2);
    builder.addUnit(gd2, "u22", "2_2", faction22, region21).setSortIndex(3);

    GameData gdm = GameDataMerger.merge(gd1, gd2);

    ArrayList<Unit> units = new ArrayList<Unit>(gdm.getUnits());
    assertSame(5, units.size());

    assertOrder(gdm, "z10", "x11", "w12", "v21", "u22");

    //
    assertEquals("z10", units.get(0).getID().toString());
    assertEquals("x11", units.get(1).getID().toString());
    assertEquals("w12", units.get(2).getID().toString());
    assertEquals("v21", units.get(3).getID().toString());
    assertEquals("u22", units.get(4).getID().toString());

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
    builder.addUnit(gd1, "y11", "1_1", faction11, region11).setSortIndex(1);
    builder.addUnit(gd1, "x12", "1_2", faction11, region11).setSortIndex(2);
    builder.addUnit(gd1, "v22", "2_2", faction12, region11).setSortIndex(3);
    // unit "22" invisible

    GameData gd2 = builder.createSimplestGameData("Eressea", 350, false);
    Faction faction21 = gd2.getFactions().iterator().next();
    Region region21 = gd2.getRegions().iterator().next();
    Faction faction22 = builder.addFaction(gd2, "f2", "anders", "Menschen", 1);
    gd2.setOwnerFaction(faction22.getID());
    builder.addUnit(gd2, "z20", "2_0", faction22, region21).setSortIndex(1);
    builder.addUnit(gd2, "y11", "1_1", faction21, region21).setSortIndex(2);
    // faction 2's report does not know where unit 1_2 belongs
    // builder.addUnit(gd2, "12", "1_2", faction11, region11).setSortIndex(2);
    builder.addUnit(gd2, "w21", "2_1", faction22, region21).setSortIndex(3);
    builder.addUnit(gd2, "v22", "2_2", faction22, region21).setSortIndex(4);

    GameData gdm = GameDataMerger.merge(gd1, gd2);

    ArrayList<Unit> units = new ArrayList<Unit>(gdm.getUnits());
    assertSame(5, units.size());

    assertOrder(gdm, "y11", "x12");
    assertOrder(gdm, "z20", "w21");
    assertOrder(gdm, "w21", "v22");

    // assertOrder(gdm, "z11", "y12");
    // assertOrder(gdm, "y12", "x22");
    // assertOrder(gdm, "v20", "z11");
    // assertOrder(gdm, "z11", "w21");
    // assertOrder(gdm, "w21", "x22");

    // this is correct, but not enforced
    // assertEquals("v20", units.get(0).getID().toString());
    // assertEquals("z11", units.get(1).getID().toString());
    // assertEquals("y12", units.get(2).getID().toString());
    // assertEquals("w21", units.get(3).getID().toString());
    // assertEquals("x22", units.get(4).getID().toString());
    //
    // assertEquals(0, units.get(0).getSortIndex());
    // assertEquals(1, units.get(1).getSortIndex());
    // assertEquals(2, units.get(2).getSortIndex());
    // assertEquals(3, units.get(3).getSortIndex());
    // assertEquals(4, units.get(4).getSortIndex());

  }

  private void assertOrder(GameData gdm, String... ids) {
    String lastID = null;
    for (String id : ids) {
      if (lastID != null) {
        assertOrder(gdm, lastID, id);
      }
      lastID = id;
    }
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
    Unit unit111 = builder.addUnit(gd1, "z11", "1_1", faction11, region11);
    Unit unit112 = builder.addUnit(gd1, "y12", "1_2", faction11, region11);
    Unit unit121 = builder.addUnit(gd1, "x21", "2_1", faction12, region11);
    Unit unit132 = builder.addUnit(gd1, "u32", "3_2", faction13, region11);
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
    Unit unit221 = builder.addUnit(gd2, "x21", "2_1", faction22, region21);
    Unit unit222 = builder.addUnit(gd2, "w22", "2_2", faction22, region21);
    Unit unit231 = builder.addUnit(gd2, "v31", "3_1", faction23, region21);
    Unit unit232 = builder.addUnit(gd2, "u32", "3_2", faction23, region21);
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
    Unit unit331 = builder.addUnit(gd3, "v31", "3_1", faction33, region31);
    Unit unit332 = builder.addUnit(gd3, "u32", "3_2", faction33, region31);
    unit331.setSortIndex(1);
    unit332.setSortIndex(2);

    GameData gdm = GameDataMerger.merge(gd1, gd2);
    GameData gdm2 = GameDataMerger.merge(gdm, gd3);

    ArrayList<Unit> units = new ArrayList<Unit>(gdm2.getUnits());
    assertSame(6, units.size());

    assertOrder(gdm, "z11", "y12");
    assertOrder(gdm, "x21", "w22");
    // not enforced:
    // assertOrder(gdm2, "31", "32");

    assertOrder(gdm2, "z11", "y12");
    assertOrder(gdm2, "x21", "w22");
    assertOrder(gdm2, "v31", "u32");

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

  /**
   * Tests that merged report's unit sorting does not depend on unit IDs.
   * 
   * @throws Exception
   */
  @Test
  public void testUnitSorting4a() throws Exception {
    GameDataBuilder builder = new GameDataBuilder();
    GameData gd1 = builder.createSimplestGameData("Eressea", 350, false);
    Faction faction11 = gd1.getFactions().iterator().next();
    gd1.setOwnerFaction(faction11.getID());
    Region region11 = gd1.getRegions().iterator().next();
    Unit unit111 = builder.addUnit(gd1, "z11", "1_1", faction11, region11);
    Unit unit112 = builder.addUnit(gd1, "x12", "1_2", faction11, region11);
    unit111.setSortIndex(1);
    unit112.setSortIndex(2);

    GameData gd2 = builder.createSimplestGameData("Eressea", 351, false);
    Region region21 = gd2.getRegions().iterator().next();
    Faction faction21 = gd2.getFactions().iterator().next();
    gd2.setOwnerFaction(faction21.getID());
    Unit unit211 = builder.addUnit(gd2, "z11", "1_1", faction21, region21);
    Unit unit212 = builder.addUnit(gd2, "x12", "1_2", faction21, region21);
    unit211.setSortIndex(1);
    unit212.setSortIndex(2);

    GameData gdm = GameDataMerger.merge(gd1, gd2);

    ArrayList<Unit> units = new ArrayList<Unit>(gdm.getUnits());
    assertSame(2, units.size());

    assertOrder(gdm, "z11", "x12");

    //
    assertEquals("z11", units.get(0).getID().toString());
    assertEquals("x12", units.get(1).getID().toString());

    //
    assertEquals(0, units.get(0).getSortIndex());
    assertEquals(1, units.get(1).getSortIndex());
  }

  /**
   * Tests that merged report's unit sorting does not depend on unit IDs. Same as
   * {@link #testUnitSorting4a()}, but with unit ID order reversed.
   * 
   * @throws Exception
   */
  @Test
  public void testUnitSorting4b() throws Exception {
    GameDataBuilder builder = new GameDataBuilder();
    GameData gd1 = builder.createSimplestGameData("Eressea", 350, false);
    Faction faction11 = gd1.getFactions().iterator().next();
    gd1.setOwnerFaction(faction11.getID());
    Region region11 = gd1.getRegions().iterator().next();
    Unit unit111 = builder.addUnit(gd1, "z11", "1_1", faction11, region11);
    Unit unit112 = builder.addUnit(gd1, "x12", "1_2", faction11, region11);
    unit111.setSortIndex(1);
    unit112.setSortIndex(2);

    GameData gd2 = builder.createSimplestGameData("Eressea", 351, false);
    Region region21 = gd2.getRegions().iterator().next();
    Faction faction21 = gd2.getFactions().iterator().next();
    gd2.setOwnerFaction(faction21.getID());
    Unit unit211 = builder.addUnit(gd2, "z11", "1_1", faction21, region21);
    Unit unit212 = builder.addUnit(gd2, "x12", "1_2", faction21, region21);
    unit211.setSortIndex(1);
    unit212.setSortIndex(2);

    GameData gdm = GameDataMerger.merge(gd1, gd2);

    ArrayList<Unit> units = new ArrayList<Unit>(gdm.getUnits());
    assertSame(2, units.size());

    assertOrder(gdm, "z11", "x12");

    //
    assertEquals("z11", units.get(0).getID().toString());
    assertEquals("x12", units.get(1).getID().toString());

    //
    assertEquals(0, units.get(0).getSortIndex());
    assertEquals(1, units.get(1).getSortIndex());
  }

  /**
   * Same as {@link #testUnitSorting4a()}, but order in second report is reversed
   * 
   * @throws Exception
   */
  @Test
  public void testUnitSorting5a() throws Exception {
    GameDataBuilder builder = new GameDataBuilder();
    GameData gd1 = builder.createSimplestGameData("Eressea", 350, false);
    Faction faction11 = gd1.getFactions().iterator().next();
    gd1.setOwnerFaction(faction11.getID());
    Region region11 = gd1.getRegions().iterator().next();
    Unit unit111 = builder.addUnit(gd1, "x11", "1_1", faction11, region11);
    Unit unit112 = builder.addUnit(gd1, "z12", "1_2", faction11, region11);
    unit111.setSortIndex(1);
    unit112.setSortIndex(2);

    GameData gd2 = builder.createSimplestGameData("Eressea", 351, false);
    Region region21 = gd2.getRegions().iterator().next();
    Faction faction21 = gd2.getFactions().iterator().next();
    gd2.setOwnerFaction(faction21.getID());
    Unit unit211 = builder.addUnit(gd2, "z12", "1_2", faction21, region21);
    Unit unit212 = builder.addUnit(gd2, "x11", "1_1", faction21, region21);
    unit211.setSortIndex(1);
    unit212.setSortIndex(2);

    ArrayList<Unit> units = new ArrayList<Unit>(gd1.getUnits());
    assertSame(2, units.size());
    assertEquals("x11", units.get(0).getID().toString());
    assertEquals("z12", units.get(1).getID().toString());

    units = new ArrayList<Unit>(gd2.getUnits());
    assertSame(2, units.size());
    assertEquals("z12", units.get(0).getID().toString());
    assertEquals("x11", units.get(1).getID().toString());

    GameData gdm = GameDataMerger.merge(gd1, gd2);

    units = new ArrayList<Unit>(gdm.getUnits());
    assertSame(2, units.size());

    assertOrder(gdm, "z12", "x11");

    // newer order overrides older order
    assertEquals("z12", units.get(0).getID().toString());
    assertEquals("x11", units.get(1).getID().toString());

    //
    assertEquals(0, units.get(0).getSortIndex());
    assertEquals(1, units.get(1).getSortIndex());
  }

  /**
   * Same as {@link #testUnitSorting4b()}, but order in second report is reversed
   * 
   * @throws Exception
   */
  @Test
  public void testUnitSorting5b() throws Exception {
    GameDataBuilder builder = new GameDataBuilder();
    GameData gd1 = builder.createSimplestGameData("Eressea", 350, false);
    Faction faction11 = gd1.getFactions().iterator().next();
    gd1.setOwnerFaction(faction11.getID());
    Region region11 = gd1.getRegions().iterator().next();
    Unit unit111 = builder.addUnit(gd1, "x11", "1_1", faction11, region11);
    Unit unit112 = builder.addUnit(gd1, "z12", "1_2", faction11, region11);
    unit111.setSortIndex(1);
    unit112.setSortIndex(2);

    GameData gd2 = builder.createSimplestGameData("Eressea", 351, false);
    Region region21 = gd2.getRegions().iterator().next();
    Faction faction21 = gd2.getFactions().iterator().next();
    gd2.setOwnerFaction(faction21.getID());
    Unit unit211 = builder.addUnit(gd2, "z12", "1_2", faction21, region21);
    Unit unit212 = builder.addUnit(gd2, "x11", "1_1", faction21, region21);
    unit211.setSortIndex(1);
    unit212.setSortIndex(2);

    GameData gdm = GameDataMerger.merge(gd1, gd2);

    ArrayList<Unit> units = new ArrayList<Unit>(gdm.getUnits());
    assertSame(2, units.size());

    assertOrder(gdm, "z12", "x11");

    //
    assertEquals("z12", units.get(0).getID().toString());
    assertEquals("x11", units.get(1).getID().toString());

    //
    assertEquals(0, units.get(0).getSortIndex());
    assertEquals(1, units.get(1).getSortIndex());
  }

  /**
   * Test for trac bug #59. Tests that merged report's unit sorting does not depend on unit IDs.
   * 
   * @throws Exception
   */
  @Test
  public void testUnitSorting4c() throws Exception {
    GameData gd1 =
        new GameDataReader(null).readGameData(FileTypeFactory.singleton().createFileType(
            new File("./test/tc009/125_Eiskappe.cr"), true, new FileTypeFactory.FileTypeChooser()));
    GameData gd2 =
        new GameDataReader(null).readGameData(FileTypeFactory.singleton().createFileType(
            new File("./test/tc009/127_Eiskappe.cr"), true, new FileTypeFactory.FileTypeChooser()));
    GameData gdm = GameDataMerger.merge(gd1, gd2);

    ArrayList<Unit> units = new ArrayList<Unit>(gdm.getUnits());
    assertSame(2, units.size());

    assertOrder(gdm, "prbc", "ags8");

    //
    assertEquals("prbc", units.get(0).getID().toString());
    assertEquals("ags8", units.get(1).getID().toString());

    //
    assertEquals(0, units.get(0).getSortIndex());
    assertEquals(1, units.get(1).getSortIndex());
  }

  /**
   * Test that a building owner info doesn't disturb unit sorting.
   * 
   * @throws Exception
   */
  @Test
  public void testUnitSorting5() throws Exception {
    GameDataBuilder builder = new GameDataBuilder();
    GameData gd2 = builder.createSimplestGameData("Eressea", 351, false);
    Faction faction21 = gd2.getFactions().iterator().next();
    gd2.setOwnerFaction(faction21.getID());
    Region region21 = gd2.getRegions().iterator().next();
    Unit unit211 = builder.addUnit(gd2, "z11", "1_1", faction21, region21);
    Unit unit212 = builder.addUnit(gd2, "y12", "1_2", faction21, region21);
    Unit unit213 = builder.addUnit(gd2, "x12", "1_3", faction21, region21);
    unit211.setSortIndex(1);
    unit212.setSortIndex(2);
    unit213.setSortIndex(3);
    Building b21 = builder.addBuilding(gd2, region21, "b1", "BURG", "Burg 1", 10);
    builder.addTo(unit211, b21);
    builder.addTo(unit212, b21);
    Building b22 = builder.addBuilding(gd2, region21, "b2", "BURG", "Burg 2", 10);
    builder.addTo(unit213, b22);

    WriteGameData.writeCR(gd2, new File("./test/tc010/gd2.cr"));

    GameData gd1 = builder.createSimplestGameData("Eressea", 350, false);
    gd2 =
        new GameDataReader(null).readGameData(FileTypeFactory.singleton().createFileType(
            new File("./test/tc010/gd2.cr"), true, new FileTypeFactory.FileTypeChooser()));
    faction21 = gd2.getFactions().iterator().next();
    ArrayList<Unit> units = new ArrayList<Unit>(faction21.units());
    assertSame(3, units.size());

    assertOrder(gd2, "z11", "y12", "x12");

    GameData gdm = GameDataMerger.merge(gd1, gd2);

    faction21 = gdm.getFactions().iterator().next();
    units = new ArrayList<Unit>(faction21.units());
    assertSame(3, units.size());

    assertOrder(gdm, "z11", "y12", "x12");

    assertEquals("z11", units.get(0).getID().toString());
    assertEquals("y12", units.get(1).getID().toString());
    assertEquals("x12", units.get(2).getID().toString());

    assertEquals(0, units.get(0).getSortIndex());
    assertEquals(1, units.get(1).getSortIndex());
    assertEquals(2, units.get(2).getSortIndex());
  }

}
