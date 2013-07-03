// class magellan.library.AtlantisWrapTest
// created on Apr 30, 2013
//
// Copyright 2003-2013 by magellan project team
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
package magellan.library;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import magellan.library.gamebinding.atlantis.AtlantisMapMetric;
import magellan.library.io.GameDataReader;
import magellan.library.io.file.FileTypeFactory;
import magellan.library.utils.logging.Logger;
import magellan.library.utils.transformation.BoxTransformer;
import magellan.library.utils.transformation.BoxTransformer.BBoxes;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author stm
 */
public class AtlantisWrapTest extends MagellanTestWithResources {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    setLocale(EN_LOCALE);
    Logger.setLevel(Logger.WARN);
    initResources();
  }

  private GameDataBuilder builder;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    builder = new GameDataBuilder();
    builder.setGameName(GameDataBuilder.ATLANTIS);

    // data = builder.createSimplestGameData(350, true);
    // region00 = data.getRegions().iterator().next();
    // unit001 = builder.addUnit(data, "U001", region00);
    // region58 = builder.addRegion(data, "5,8", "R58", "plain", 1);
    // unit581 = builder.addUnit(data, "U581", region58);
    // region58 = builder.addRegion(data, "5,8", "R58", "plain", 1);
    // unit581 = builder.addUnit(data, "U581", region58);

  }

  /**
   * @throws IOException
   */
  @Test
  public final void test1() throws IOException {
    GameData gd1 =
        new GameDataReader(null).readGameData(FileTypeFactory.singleton().createFileType(
            new File("./test/tc011/atlantis_mini.r"), true, new FileTypeFactory.FileTypeChooser()));

    BBoxes boxes = new BBoxes(gd1.getGameSpecificStuff().getMapMetric());
    boxes.setBoxX(0, 0, 23);
    boxes.setBoxY(0, 0, 22);
    GameData gd2 = GameDataMerger.merge(gd1, new BoxTransformer(boxes));
    gd2.postProcess();
    Region region00 = gd2.getRegion(CoordinateID.create(0, 0));
    // x is 24, because of the rectangular bounding box correction
    Region region01 = gd2.getRegion(CoordinateID.create(24, 22));
    Region region02 = gd2.getRegion(CoordinateID.create(23, 22));

    assertNotNull(region00);
    assertNotNull(region01);
    assertNotNull(region02);
    assertEquals(region01, region00.getNeighbors().get(AtlantisMapMetric.N));
    assertEquals(region02, region00.getNeighbors().get(AtlantisMapMetric.M));
    assertEquals(region02, region01.getNeighbors().get(AtlantisMapMetric.W));
  }

  /**
   * @throws IOException
   */
  @Test
  public final void test2() throws IOException {
    GameData gd1 =
        new GameDataReader(null).readGameData(FileTypeFactory.singleton().createFileType(
            new File("./test/tc011/atlantis_mini.r"), true, new FileTypeFactory.FileTypeChooser()));

    BBoxes boxes = new BBoxes(gd1.getGameSpecificStuff().getMapMetric());
    boxes.setBoxX(0, -11, 12);
    boxes.setBoxY(0, -11, 11);
    GameData gd2 = GameDataMerger.merge(gd1, new BoxTransformer(boxes));
    gd2.postProcess();
    Region region00 = gd2.getRegion(CoordinateID.create(0, 0));
    Region region01 = gd2.getRegion(CoordinateID.create(0, -1));
    Region region02 = gd2.getRegion(CoordinateID.create(-1, -1));

    assertNotNull(region00);
    assertNotNull(region01);
    assertNotNull(region02);
    assertEquals(region01, region00.getNeighbors().get(AtlantisMapMetric.N));
    assertEquals(region02, region00.getNeighbors().get(AtlantisMapMetric.M));
    assertEquals(region02, region01.getNeighbors().get(AtlantisMapMetric.W));
  }

}
