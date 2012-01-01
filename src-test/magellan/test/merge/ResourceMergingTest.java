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
import magellan.library.CoordinateID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.GameDataMerger;
import magellan.library.Region;
import magellan.library.RegionResource;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

import org.junit.Test;

public class ResourceMergingTest extends MagellanTestWithResources {

  private static final int NUM_REGIONS = 5;
  private GameDataBuilder builder;
  private GameData gd1;
  private Faction[] faction1;
  private Unit[] unit1;
  private Region[] region1;

  private GameData gd2;
  private Faction[] faction2;
  private Unit[] unit2;
  private Region[] region2;

  private GameData gd3;
  private Faction[] faction3;
  private Unit[] unit3;
  private Region[] region3;

  /**
   * Tests for correct unit sorting even if some units are invisible for other factions.
   * 
   * @throws Exception
   */
  @Test
  public void testUnitSorting() throws Exception {
    createSkeleton();

    // two trees have disappeared
    region1[0].setTrees(2);
    region3[0].setTrees(0);

    // same thing using RegionResource
    RegionResource trees;
    region1[1].addResource(trees =
        new RegionResource(EresseaConstants.I_TREES, gd1.rules
            .getItemType(EresseaConstants.I_TREES)));
    trees.setAmount(2);
    trees.setDate(gd1.getDate().getDate());
    // that's what would be nice to have: a resource with 0 trees
    region3[1].addResource(trees =
        new RegionResource(EresseaConstants.I_TREES, gd3.rules
            .getItemType(EresseaConstants.I_TREES)));
    trees.setAmount(0);
    trees.setDate(gd3.getDate().getDate());

    // but it's more likely to be like this:
    region1[2].addResource(trees =
        new RegionResource(EresseaConstants.I_TREES, gd1.rules
            .getItemType(EresseaConstants.I_TREES)));
    trees.setAmount(2);
    trees.setDate(gd1.getDate().getDate());
    // no resource for region2[2]!!

    GameData gdm = GameDataMerger.merge(gd1, gd3);

    Region[] regionM = new Region[NUM_REGIONS];
    for (int i = 0; i < NUM_REGIONS; ++i) {
      regionM[i] = gdm.getRegion(CoordinateID.create(i, 0));
    }

    assertEquals(0, regionM[0].getTrees());
    assertEquals(2, regionM[0].getOldTrees());

    assertEquals(0, regionM[1].getTrees());
    assertEquals(2, regionM[1].getOldTrees());

    assertEquals(0, regionM[2].getTrees());
    assertEquals(2, regionM[2].getOldTrees());

  }

  private void createSkeleton() throws Exception {
    builder = new GameDataBuilder();
    gd1 = builder.createSimplestGameData("Eressea", 350, false, false);

    faction1 = new Faction[NUM_REGIONS];
    unit1 = new Unit[NUM_REGIONS];
    region1 = new Region[NUM_REGIONS];

    faction1[0] = gd1.getFactions().iterator().next();
    gd1.setOwnerFaction(faction1[0].getID());
    region1[0] = gd1.getRegions().iterator().next();

    for (int i = 0; i < NUM_REGIONS; ++i) {
      if (i > 0) {
        region1[i] = builder.addRegion(gd1, i + " 0", "R" + i + " 0", "Ebene", i);
      }
      unit1[i] = builder.addUnit(gd1, "U " + i, region1[i], true);
    }

    gd2 = builder.createSimplestGameData("Eressea", 350, false, false);
    faction2 = new Faction[NUM_REGIONS];
    unit2 = new Unit[NUM_REGIONS];
    region2 = new Region[NUM_REGIONS];

    faction2[0] = gd2.getFactions().iterator().next();
    gd2.setOwnerFaction(faction2[0].getID());
    region2[0] = gd2.getRegions().iterator().next();

    for (int i = 1; i < NUM_REGIONS; ++i) {
      if (i > 0) {
        region2[i] = builder.addRegion(gd2, i + " 0", "R" + i + " 0", "Ebene", i);
      }
      unit2[i] = builder.addUnit(gd2, "U " + i, region2[i], true);
    }

    gd3 = builder.createSimplestGameData("Eressea", 351, false, false);
    faction3 = new Faction[NUM_REGIONS];
    unit3 = new Unit[NUM_REGIONS];
    region3 = new Region[NUM_REGIONS];

    faction3[0] = gd3.getFactions().iterator().next();
    gd3.setOwnerFaction(faction3[0].getID());
    region3[0] = gd3.getRegions().iterator().next();

    for (int i = 1; i < NUM_REGIONS; ++i) {
      if (i > 0) {
        region3[i] = builder.addRegion(gd3, i + " 0", "R" + i + " 0", "Ebene", i);
      }
      unit3[i] = builder.addUnit(gd3, "U " + i, region3[i], true);
    }

    gd1.postProcess();
    gd2.postProcess();
    gd3.postProcess();
  }

}
