// class magellan.test.merge.GameDataMergerTest
// created on Feb 28, 2011
//
// Copyright 2003-2011 by magellan project team
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
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.GameDataMerger;
import magellan.library.Region;
import magellan.library.Skill;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

import org.junit.Test;

/**
 * Tests aspects of skill merging.
 * 
 * @author stm
 * @version 1.0, Sep 26, 2012
 */
public class MergeUnitSkillsTest extends MagellanTestWithResources {

  private GameDataBuilder builder;
  private GameData gd01;
  private GameData gd02;
  private GameData gd03;
  private GameData gd11;
  private GameData gd12;
  private GameData gd13;
  private Faction faction01a;
  private Faction faction01b;
  private Faction faction02a;
  private Faction faction02b;
  private Faction faction03c;
  private Faction faction11a;
  private Faction faction11b;
  private Faction faction12a;
  private Faction faction12b;
  private Faction faction13a;
  private Faction faction13b;
  private Faction faction13c;
  private Region region01;
  private Region region02;
  private Region region03;
  private Region region11;
  private Region region12;
  private Region region13;
  private Unit unit01a;
  @SuppressWarnings("unused")
  private Unit unit01b;
  @SuppressWarnings("unused")
  private Unit unit02a;
  private Unit unit02b;
  private Unit unit03c;
  private Unit unit11a;
  @SuppressWarnings("unused")
  private Unit unit11b;
  @SuppressWarnings("unused")
  private Unit unit12a;
  private Unit unit12b;
  @SuppressWarnings("unused")
  private Unit unit13a;
  @SuppressWarnings("unused")
  private Unit unit13b;
  private Unit unit13c;

  protected void create(String game) throws Exception {
    builder = new GameDataBuilder();
    gd01 = builder.createSimpleGameData(game, 350, false);
    gd02 = builder.createSimpleGameData(game, 350, false);
    gd03 = builder.createSimpleGameData(game, 350, false);
    gd11 = builder.createSimpleGameData(game, 351, false);
    gd12 = builder.createSimpleGameData(game, 351, false);
    gd13 = builder.createSimpleGameData(game, 351, false);

    faction01a = builder.addFaction(gd01, "aaa", "AAA", "Menschen", 1);
    gd01.setOwnerFaction(faction01a.getID());
    faction01b = builder.addFaction(gd01, "bbb", "BBB", "Menschen", 1);
    faction02b = builder.addFaction(gd02, "bbb", "BBB", "Menschen", 1);
    gd02.setOwnerFaction(faction02b.getID());
    faction02a = builder.addFaction(gd02, "aaa", "AAA", "Menschen", 1);
    faction03c = builder.addFaction(gd03, "ccc", "CCC", "Menschen", 1);
    gd03.setOwnerFaction(faction03c.getID());

    faction11a = builder.addFaction(gd11, "aaa", "AAA", "Menschen", 1);
    gd11.setOwnerFaction(faction11a.getID());
    faction11b = builder.addFaction(gd11, "bbb", "BBB", "Menschen", 1);
    faction12b = builder.addFaction(gd12, "bbb", "BBB", "Menschen", 1);
    gd12.setOwnerFaction(faction12b.getID());
    faction12a = builder.addFaction(gd12, "aaa", "AAA", "Menschen", 1);
    faction13c = builder.addFaction(gd13, "ccc", "CCC", "Menschen", 1);
    gd13.setOwnerFaction(faction13c.getID());
    faction13a = builder.addFaction(gd13, "aaa", "AAA", "Menschen", 1);
    faction13b = builder.addFaction(gd13, "bbb", "BBB", "Menschen", 1);

    region01 = gd01.getRegions().iterator().next();
    region02 = gd02.getRegions().iterator().next();
    region03 = gd03.getRegions().iterator().next();
    region11 = gd11.getRegions().iterator().next();
    region12 = gd12.getRegions().iterator().next();
    region13 = gd13.getRegions().iterator().next();

    unit01a = builder.addUnit(gd01, "aa", "AA", faction01a, region01, true);
    unit01b = builder.addUnit(gd01, "bb", "BB", faction01b, region01, false);
    unit02a = builder.addUnit(gd02, "aa", "AA", faction02a, region02, false);
    unit02b = builder.addUnit(gd02, "bb", "BB", faction02b, region02, true);
    unit03c = builder.addUnit(gd03, "cc", "CC", faction03c, region03, true);

    unit11a = builder.addUnit(gd11, "aa", "AA", faction11a, region11, true);
    unit11b = builder.addUnit(gd11, "bb", "BB", faction11b, region11, false);
    unit12a = builder.addUnit(gd12, "aa", "AA", faction12a, region12, false);
    unit12b = builder.addUnit(gd12, "bb", "BB", faction12b, region12, true);
    unit13a = builder.addUnit(gd13, "aa", "AA", faction13a, region13, false);
    unit13b = builder.addUnit(gd13, "bb", "BB", faction13b, region13, false);
    unit13c = builder.addUnit(gd13, "cc", "CC", faction13c, region13, true);

    builder.addSkill(unit01a, "Hiebwaffen", 7);
    builder.addSkill(unit01a, "Ausdauer", 3);
    builder.addSkill(unit01a, "Wahrnehmung", 2);
    builder.addSkill(unit02b, "Taktik", 3);
    builder.addSkill(unit02b, "Wahrnehmung", 2);
    builder.addSkill(unit02b, "Ausdauer", 1);
    builder.addSkill(unit03c, "Tarnung", 1);
    builder.addSkill(unit03c, "Wahrnehmung", 2);

    builder.addSkill(unit11a, "Hiebwaffen", 8);
    builder.addSkill(unit11a, "Wahrnehmung", 2);
    builder.addSkill(unit11a, "Bergbau", 3);
    builder.addSkill(unit12b, "Taktik", 4);
    builder.addSkill(unit12b, "Wahrnehmung", 2);
    builder.addSkill(unit13c, "Tarnung", 2);
    builder.addSkill(unit13c, "Wahrnehmung", 2);
  }

  /**
   * Test method for
   * {@link magellan.library.GameDataMerger#mergeUnit(GameData, Unit, GameData, Unit, boolean, boolean, magellan.library.utils.transformation.ReportTransformer)}
   * .
   * 
   * @throws Exception
   */
  @Test
  public final void testMergeSkills() throws Exception {
    create("eressea");

    GameData gdm = GameDataMerger.merge(gd01, gd02);
    assertSkill(gdm, "aa", "Hiebwaffen", 7, 0);
    assertSkill(gdm, "aa", "Ausdauer", 3, 0);
    assertSkill(gdm, "aa", "Wahrnehmung", 2, 0);

    assertSkill(gdm, "bb", "Taktik", 3, 0);
    assertSkill(gdm, "bb", "Ausdauer", 1, 0);
    assertSkill(gdm, "bb", "Wahrnehmung", 2, 0);
  }

  /**
   * Test method for
   * {@link magellan.library.GameDataMerger#mergeUnit(GameData, Unit, GameData, Unit, boolean, boolean, magellan.library.utils.transformation.ReportTransformer)}
   * 
   * @throws Exception
   */
  @Test
  public final void testMergeSkills2() throws Exception {
    create("eressea");

    GameData gdm = GameDataMerger.merge(gd01, gd11);
    assertSkill(gdm, "aa", "Hiebwaffen", 8, 1);
    assertSkill(gdm, "aa", "Ausdauer", 0, -3);
    assertSkill(gdm, "aa", "Wahrnehmung", 2, 0);
    assertSkill(gdm, "aa", "Bergbau", 3, 3);
  }

  /**
   * Test method for
   * {@link magellan.library.GameDataMerger#mergeUnit(GameData, Unit, GameData, Unit, boolean, boolean, magellan.library.utils.transformation.ReportTransformer)}
   * 
   * @throws Exception
   */
  @Test
  public final void testMergeSkills3() throws Exception {
    create("eressea");

    GameData gdm = GameDataMerger.merge(gd01, gd02);
    gdm = GameDataMerger.merge(gdm, gd11);

    assertSkill(gdm, "aa", "Hiebwaffen", 8, 1);
    assertSkill(gdm, "aa", "Ausdauer", 0, -3);
    assertSkill(gdm, "aa", "Wahrnehmung", 2, 0);
    assertSkill(gdm, "aa", "Bergbau", 3, 3);

    assertSkill(gdm, "bb", "Taktik", 3, 0);
    assertSkill(gdm, "bb", "Ausdauer", 1, 0);
    assertSkill(gdm, "bb", "Wahrnehmung", 2, 0);
  }

  /**
   * Test method for
   * {@link magellan.library.GameDataMerger#mergeUnit(GameData, Unit, GameData, Unit, boolean, boolean, magellan.library.utils.transformation.ReportTransformer)}
   * 
   * @throws Exception
   */
  @Test
  public final void testMergeSkills4() throws Exception {
    create("eressea");

    GameData gdm = GameDataMerger.merge(gd01, gd02);
    gdm = GameDataMerger.merge(gdm, gd11);
    gdm = GameDataMerger.merge(gdm, gd12);

    assertSkill(gdm, "aa", "Hiebwaffen", 8, 1);
    assertSkill(gdm, "aa", "Ausdauer", 0, -3);
    assertSkill(gdm, "aa", "Wahrnehmung", 2, 0);
    assertSkill(gdm, "aa", "Bergbau", 3, 3);

    assertSkill(gdm, "bb", "Taktik", 4, 1);
    assertSkill(gdm, "bb", "Ausdauer", 0, -1);
    assertSkill(gdm, "bb", "Wahrnehmung", 2, 0);
  }

  /**
   * Test method for
   * {@link magellan.library.GameDataMerger#mergeUnit(GameData, Unit, GameData, Unit, boolean, boolean, magellan.library.utils.transformation.ReportTransformer)}
   * 
   * @throws Exception
   */
  @Test
  public final void testMergeSkills5() throws Exception {
    create("eressea");

    GameData gdm = GameDataMerger.merge(gd01, gd02);
    gdm = GameDataMerger.merge(gdm, gd11);
    gdm = GameDataMerger.merge(gdm, gd12);
    gdm = GameDataMerger.merge(gdm, gd13);

    assertSkill(gdm, "aa", "Hiebwaffen", 8, 1);
    assertSkill(gdm, "aa", "Ausdauer", 0, -3);
    assertSkill(gdm, "aa", "Wahrnehmung", 2, 0);
    assertSkill(gdm, "aa", "Bergbau", 3, 3);

    assertSkill(gdm, "bb", "Taktik", 4, 1);
    assertSkill(gdm, "bb", "Ausdauer", 0, -1);
    assertSkill(gdm, "bb", "Wahrnehmung", 2, 0);

    assertSkill(gdm, "cc", "Tarnung", 2, 0);
    assertSkill(gdm, "cc", "Wahrnehmung", 2, 0);
  }

  /**
   * Test method for hidden units in
   * {@link magellan.library.GameDataMerger#mergeUnit(GameData, Unit, GameData, Unit, boolean, boolean, magellan.library.utils.transformation.ReportTransformer)}
   * 
   * @throws Exception
   */
  @Test
  public final void testMergeSkills6() throws Exception {
    create("eressea");

    GameData gdm = GameDataMerger.merge(gd01, gd03);
    gdm = GameDataMerger.merge(gdm, gd11);
    gdm = GameDataMerger.merge(gdm, gd13);

    assertSkill(gdm, "aa", "Hiebwaffen", 8, 1);
    assertSkill(gdm, "aa", "Ausdauer", 0, -3);
    assertSkill(gdm, "aa", "Wahrnehmung", 2, 0);
    assertSkill(gdm, "aa", "Bergbau", 3, 3);

    assertSkill(gdm, "cc", "Tarnung", 2, 1);
    assertSkill(gdm, "cc", "Wahrnehmung", 2, 0);
  }

  /**
   * Test method for
   * {@link magellan.library.GameDataMerger#mergeUnit(GameData, Unit, GameData, Unit, boolean, boolean, magellan.library.utils.transformation.ReportTransformer)}
   * 
   * @throws Exception
   */
  @Test
  public final void testMergeSkills7() throws Exception {
    create("eressea");

    Region ost0 = builder.addRegion(gd01, "1,0", "Ost", "Ebene", 1);
    Region ost1 = builder.addRegion(gd11, "1,0", "Ost", "Ebene", 1);
    builder.addUnit(gd01, "oo", "Ost", faction01a, ost0, true);
    builder.addUnit(gd11, "oo", "Ost", faction11a, ost1, true);

    GameData gdm = GameDataMerger.merge(gd01, gd02);

    gdm = GameDataMerger.merge(gdm, gd12);
    gdm = GameDataMerger.merge(gdm, gd11);

    assertSkill(gdm, "aa", "Hiebwaffen", 8, 1);
    assertSkill(gdm, "aa", "Ausdauer", 0, -3);
    assertSkill(gdm, "aa", "Wahrnehmung", 2, 0);
    assertSkill(gdm, "aa", "Bergbau", 3, 3);

    assertSkill(gdm, "bb", "Taktik", 4, 1);
    assertSkill(gdm, "bb", "Ausdauer", 0, -1);
    assertSkill(gdm, "bb", "Wahrnehmung", 2, 0);

    assertEquals(0, gdm.getOldUnits().size());
    assertEquals(3, gdm.getUnits().size());
  }

  protected static void assertSkill(GameData gd, String id, String skillName, int level, int change) {
    Unit unit = gd.getUnit(UnitID.createUnitID(id, gd.base));
    Skill skill = unit.getSkill(gd.getRules().getSkillType(StringID.create(skillName)));
    if (skill != null) {
      assertEquals(level, skill.getLevel());
      assertEquals(change, skill.getChangeLevel());
    } else {
      assertEquals(0, level);
      assertEquals(0, change);
    }
  }

}
