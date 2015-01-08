// class magellan.test.merge.MergeUnitTransferTest
// created on Apr 22, 2014
//
// Copyright 2003-2014 by magellan project team
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
import static org.junit.Assert.assertNull;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.GameDataMerger;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.test.GameDataBuilder;

import org.junit.Test;

public class MergeUnitTransferTest {

  @Test
  public void testMerge() throws Exception {
    GameDataBuilder builder = new GameDataBuilder();
    GameData gd1 = builder.createSimpleGameData(350);
    Faction f1_1 = gd1.getFactions().iterator().next();
    Region region1 = gd1.getRegions().iterator().next();

    GameData gd2 = builder.createSimpleGameData(351);
    Faction f2_2 = builder.addFaction(gd2, "f2", "Other faction", "Menschen", 1);
    Region region2 = gd2.getRegions().iterator().next();

    Unit unit1_1 = builder.addUnit(gd1, "tran", "Transferred", f1_1, region1);
    Unit unit2_1 = builder.addUnit(gd2, "tran", "Transferred", f2_2, region2);
    unit2_1.setHideFaction(true);

    GameData gd4 = GameDataMerger.merge(gd1, gd2);
    // WriteGameData.writeCR(gd4, gd4.getDate().getDate()+"_MergeSimpleGameData.cr");

    Unit unit4 = gd4.getUnit(unit1_1.getID());
    assertEquals(unit1_1.getID(), unit4.getID());
    assertEquals(unit2_1.getID(), unit4.getID());

    assertEquals(f2_2.getID(), unit4.getFaction().getID());
    Faction f4_1 = gd4.getFaction(f1_1.getID());
    Faction f4_2 = gd4.getFaction(f2_2.getID());
    assertNull(f4_1.getUnit(unit4.getID()));
    assertEquals(unit4, f4_2.getUnit(unit4.getID()));

  }

  @Test
  public void testMerge2() throws Exception {
    GameDataBuilder builder = new GameDataBuilder();
    GameData gd1 = builder.createSimpleGameData(350);
    Faction f1_1 = gd1.getFactions().iterator().next();
    Region region1 = gd1.getRegions().iterator().next();

    GameData gd2 = builder.createSimpleGameData(351);
    // Faction f3_1 = gd3.getFaction(f1_1.getID());

    GameData gd3 = builder.createSimpleGameData(351);
    Faction f2_2 = builder.addFaction(gd3, "f2", "Other faction", "Menschen", 1);
    Region region2 = gd3.getRegions().iterator().next();

    Unit unit1_1 = builder.addUnit(gd1, "tran", "Transferred", f1_1, region1, true);
    Unit unit2_1 = builder.addUnit(gd3, "tran", "Transferred", f2_2, region2, false);
    unit2_1.setHideFaction(true);

    GameData gdNew1 = GameDataMerger.merge(gd1, gd2);
    GameData gdNew2 = GameDataMerger.merge(gdNew1, gd3);
    // WriteGameData.writeCR(gd4, gd4.getDate().getDate()+"_MergeSimpleGameData.cr");

    Unit unit4 = gdNew2.getUnit(unit1_1.getID());
    assertEquals(unit1_1.getID(), unit4.getID());
    assertEquals(unit2_1.getID(), unit4.getID());

    assertEquals(f2_2.getID(), unit4.getFaction().getID());
    Faction f4_1 = gdNew2.getFaction(f1_1.getID());
    Faction f4_2 = gdNew2.getFaction(f2_2.getID());
    assertNull(f4_1.getUnit(unit4.getID()));
    assertEquals(unit4, f4_2.getUnit(unit4.getID()));

  }

}
