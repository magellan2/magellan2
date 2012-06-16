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
import static org.junit.Assert.assertTrue;
import magellan.library.AllianceGroup;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.GameDataMerger;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

import org.junit.Test;

/**
 * DOCUMENT-ME
 * 
 * @author ...
 * @version 1.0, Feb 28, 2011
 */
public class GameDataMergerTest extends MagellanTestWithResources {

  private GameDataBuilder builder;
  private GameData gd01;
  private GameData gd02;
  private GameData gd11;
  private GameData gd12;
  private GameData gd21;
  private GameData gd22;
  private Unit unit011;
  private Unit unit021;
  private Unit unit012;
  private Unit unit111;
  private Unit unit121;
  private Region region011;
  private Faction faction011;
  private Faction faction021;
  private Faction faction012;
  private Faction faction022;
  private Faction faction111;
  private Faction faction121;
  private Faction faction112;
  private Faction faction122;


  private void create(String game) throws Exception {
    builder = new GameDataBuilder();
    gd01 = builder.createSimpleGameData(game, 350, true);
    gd02 = builder.createSimpleGameData(game, 350, true);
    gd11 = builder.createSimpleGameData(game, 351, true);
    gd12 = builder.createSimpleGameData(game, 351, true);
    gd21 = builder.createSimpleGameData(game, 352, true);
    gd22 = builder.createSimpleGameData(game, 352, true);
    unit011 = gd01.getUnits().iterator().next();
    unit021 = gd02.getUnits().iterator().next();
    unit111 = gd11.getUnits().iterator().next();
    unit121 = gd12.getUnits().iterator().next();
    region011 = unit011.getRegion();
    faction011 = unit011.getFaction();
    faction021 = unit021.getFaction();
    faction012 = builder.addFaction(gd01, "drac", "Drachen", "Goblins", 1);
    faction022 = builder.addFaction(gd02, "drac", "Drachen", "Goblins", 1);
    faction111 = unit111.getFaction();
    faction121 = unit121.getFaction();
    faction112 = builder.addFaction(gd11, "drac", "Drachen", "Goblins", 1);
    faction122 = builder.addFaction(gd12, "drac", "Drachen", "Goblins", 1);
    unit012 = builder.addUnit(gd01, "zwei", "Zwei", faction012, region011);
  }

  /**
   * Test method for
   * {@link magellan.library.GameDataMerger#mergeFaction(magellan.library.GameData, magellan.library.Faction, magellan.library.GameData, magellan.library.Faction, boolean, magellan.library.utils.transformation.ReportTransformer)}
   * .
   * 
   * @throws Exception
   */
  @Test
  public final void testMergeAlliance1() throws Exception {
    create("eressea");

    // two different states in the same rounds -- shouldn't happen, always overwrite
    builder.addAlliance(faction011, faction012, EresseaConstants.A_GIVE);
    builder.addAlliance(faction021, faction022, EresseaConstants.A_GUARD);

    GameData gd0m = GameDataMerger.merge(gd01, gd02);

    assertEquals(1, gd0m.getFaction(faction011.getID()).getAllies().size());
    assertEquals(faction012.getID(), gd0m.getFaction(faction011.getID()).getAllies().values()
        .iterator().next().getFaction().getID());
    assertEquals(EresseaConstants.A_GUARD, gd0m.getFaction(faction011.getID()).getAllies().values()
        .iterator().next().getState());
  }

  /**
   * Test method for
   * {@link magellan.library.GameDataMerger#mergeFaction(magellan.library.GameData, magellan.library.Faction, magellan.library.GameData, magellan.library.Faction, boolean, magellan.library.utils.transformation.ReportTransformer)}
   * .
   * 
   * @throws Exception
   */
  @Test
  public final void testMergeAlliance1b() throws Exception {
    create("eressea");

    // overwrite state with state from new round
    builder.addAlliance(faction011, faction012, EresseaConstants.A_GIVE);
    builder.addAlliance(faction111, faction112, EresseaConstants.A_GUARD);

    GameData gd1m = GameDataMerger.merge(gd01, gd11);
    assertEquals(1, gd1m.getFaction(faction011.getID()).getAllies().size());
    assertEquals(faction112.getID(), gd1m.getFaction(faction111.getID()).getAllies().get(
        faction112.getID()).getFaction().getID());
    assertEquals(EresseaConstants.A_GUARD, gd1m.getFaction(faction111.getID()).getAllies().get(
        faction112.getID()).getState());
  }

  /**
   * Test method for
   * {@link magellan.library.GameDataMerger#mergeFaction(magellan.library.GameData, magellan.library.Faction, magellan.library.GameData, magellan.library.Faction, boolean, magellan.library.utils.transformation.ReportTransformer)}
   * .
   * 
   * @throws Exception
   */
  @Test
  public final void testMergeAlliance2() throws Exception {
    create("e3");

    // alliance does not change state
    builder.addAlliance(faction011, faction012, EresseaConstants.A_GIVE);
    AllianceGroup alliance = new AllianceGroup(EntityID.createEntityID("alli", 36), "Allianz 1");
    alliance.addFaction(faction021);
    alliance.addFaction(faction022);
    faction021.setAlliance(alliance);

    GameData gd0m = GameDataMerger.merge(gd01, gd02);
    assertEquals(1, gd0m.getFaction(faction011.getID()).getAllies().size());
    assertEquals(faction012.getID(), gd0m.getFaction(faction011.getID()).getAllies().values()
        .iterator().next().getFaction().getID());
    assertEquals(EresseaConstants.A_GIVE, gd0m.getFaction(faction011.getID()).getAllies().values()
        .iterator().next().getState());

  }

  /**
   * Test that E3 Alliance groups do not screw up alliance merging. Test method for
   * {@link magellan.library.GameDataMerger#mergeFaction(magellan.library.GameData, magellan.library.Faction, magellan.library.GameData, magellan.library.Faction, boolean, magellan.library.utils.transformation.ReportTransformer)}
   * .
   * 
   * @throws Exception
   */
  @Test
  public final void testMergeAlliance3() throws Exception {
    create("e3");

    builder.addAlliance(faction011, faction012, EresseaConstants.A_GIVE);

    AllianceGroup alliance = new AllianceGroup(EntityID.createEntityID("alli", 36), "Allianz 1");
    alliance.addFaction(faction021);
    alliance.addFaction(faction022);
    gd02.addAllianceGroup(alliance);
    faction021.setAlliance(alliance);

    // post processing should not change help states
    gd02.postProcess();

    GameData gd0m = GameDataMerger.merge(gd01, gd02);
    assertEquals(1, gd0m.getFaction(faction011.getID()).getAllies().size());
    assertEquals(faction012.getID(), gd0m.getFaction(faction011.getID()).getAllies().values()
        .iterator().next().getFaction().getID());
    assertEquals(EresseaConstants.A_GIVE, gd0m.getFaction(faction011.getID()).getAllies().values()
        .iterator().next().getState());

  }

  /**
   * Test method for
   * {@link magellan.library.GameDataMerger#mergeFaction(magellan.library.GameData, magellan.library.Faction, magellan.library.GameData, magellan.library.Faction, boolean, magellan.library.utils.transformation.ReportTransformer)}
   * .
   * 
   * @throws Exception
   */
  @Test
  public final void testMergeAlliance4() throws Exception {
    create("eressea");

    // old help states are kept in new round if new state is unknown
    builder.addAlliance(faction011, faction012, EresseaConstants.A_GIVE);

    GameData gd0m = GameDataMerger.merge(gd01, gd11);

    assertEquals(1, gd0m.getFaction(faction011.getID()).getAllies().size());
    assertEquals(faction012.getID(), gd0m.getFaction(faction011.getID()).getAllies().values()
        .iterator().next().getFaction().getID());
    assertEquals(EresseaConstants.A_GIVE, gd0m.getFaction(faction011.getID()).getAllies().values()
        .iterator().next().getState());
  }

  /**
   * Test method for
   * {@link magellan.library.GameDataMerger#mergeFaction(magellan.library.GameData, magellan.library.Faction, magellan.library.GameData, magellan.library.Faction, boolean, magellan.library.utils.transformation.ReportTransformer)}
   * .
   * 
   * @throws Exception
   */
  @Test
  public final void testMergeAlliance4b() throws Exception {
    create("eressea");

    // old help states are reset in new round if new alliance is empty, but we are report owners
    builder.addAlliance(faction011, faction012, EresseaConstants.A_GIVE);

    gd11.setOwnerFaction(faction011.getID());

    GameData gd0m = GameDataMerger.merge(gd01, gd11);

    assertTrue(gd0m.getFaction(faction011.getID()).getAllies() == null
        || 0 == gd0m.getFaction(faction011.getID()).getAllies().size());
  }

}
