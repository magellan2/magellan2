// class magellan.library.utils.DirectionTest
// created on Feb 9, 2010
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
package magellan.library.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.gamebinding.EresseaMapMetric;
import magellan.library.gamebinding.MapMetric;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

/**
 * Tests for the direction class
 * 
 * @author stm
 * @version 1.0, Feb 9, 2010
 */
public class DirectionTest extends MagellanTestWithResources {

  private static final int DIR_NW = 0;
  private static final int DIR_NE = 1;
  private static final int DIR_E = 2;
  private static final int DIR_SE = 3;
  private static final int DIR_SW = 4;
  private static final int DIR_W = 5;

  private Direction NW = EresseaMapMetric.NW;
  private Direction NE = EresseaMapMetric.NE;
  private Direction E = EresseaMapMetric.E;
  private Direction SE = EresseaMapMetric.SE;
  private Direction SW = EresseaMapMetric.SW;
  private Direction W = EresseaMapMetric.W;
  private MapMetric metric;

  @Before
  public void setUp() throws Exception {
    GameDataBuilder builder = new GameDataBuilder();
    GameData data = builder.createSimpleGameData();
    metric = data.getGameSpecificStuff().getMapMetric();
  }

  /**
   * Test method for {@link magellan.library.utils.Direction#getDirCode()}.
   */
  @Test
  public void testGetDir() {
    assertEquals(0, NW.getDirCode());
    assertEquals(1, NE.getDirCode());
    assertEquals(2, E.getDirCode());
    assertEquals(3, SE.getDirCode());
    assertEquals(4, SW.getDirCode());
    assertEquals(5, W.getDirCode());
    assertEquals(Direction.DIR_INVALID, Direction.INVALID.getDirCode());
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.MapMetric#getDirection(magellan.library.CoordinateID, magellan.library.CoordinateID)}
   * .
   */
  @Test
  public void testToDirectionCoordinateIDCoordinateID() {
    assertSame(NW, metric.getDirection(CoordinateID.ZERO, CoordinateID.create(-1, 1)));
    assertSame(NW, metric.getDirection(CoordinateID.create(0, 0, 1), CoordinateID.create(-1, 1, 1)));
    assertSame(NE, metric.getDirection(CoordinateID.ZERO, CoordinateID.create(0, 1)));
    assertSame(E, metric.getDirection(CoordinateID.ZERO, CoordinateID.create(1, 0)));
    assertSame(SE, metric.getDirection(CoordinateID.ZERO, CoordinateID.create(1, -1)));
    assertSame(SW, metric.getDirection(CoordinateID.ZERO, CoordinateID.create(0, -1)));
    assertSame(W, metric.getDirection(CoordinateID.ZERO, CoordinateID.create(-1, 0)));
    assertSame(Direction.INVALID, metric.getDirection(CoordinateID.ZERO, CoordinateID.create(1, 1)));
    assertSame(Direction.INVALID, metric.getDirection(CoordinateID.ZERO, CoordinateID.create(1, 0,
        1)));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.MapMetric#getDirection(magellan.library.Region, magellan.library.Region)}
   * .
   */
  @Test
  public void testToDirectionRegionRegion() {
    GameDataBuilder builder = (new GameDataBuilder());
    GameData data = null;
    try {
      data = builder.createSimplestGameData();
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
    if (data == null) {
      fail();
      return;
    }
    MapMetric metric = data.getGameSpecificStuff().getMapMetric();
    Region r0 = data.getRegions().iterator().next();
    Region r1 = builder.addRegion(data, "1 0", "Ebene", "Ebene", 2);
    r1.setUID(1234);
    data.addRegion(r1);
    Region r2 = builder.addRegion(data, "5 0", "Wrapper", "Ebene", 3);
    r2.setUID(12345);
    data.addRegion(r2);

    r0.addNeighbor(W, r2);
    r2.addNeighbor(E, r0);

    data.postProcess();
    data.postProcessTheVoid();

    assertSame(E, metric.getDirection(r0, r1));
    assertSame(W, metric.getDirection(r1, r0));
    assertSame(E, metric.getDirection(r2, r0));
    assertSame(W, metric.getDirection(r0, r2));
    assertSame(Direction.INVALID, metric.getDirection(r1, r2));
  }

  // /**
  // * Test method for
  // * {@link magellan.library.utils.Direction#toDirection(magellan.library.CoordinateID)}.
  // */
  // @Test
  // public void testToDirectionCoordinateID() {
  // assertSame(E, metric.getDirection(CoordinateID.create(1, 0)));
  // }
  //
  // /**
  // * Test method for {@link magellan.library.utils.Direction#toDirection(int, int)}.
  // */
  // @Test
  // public void testToDirectionIntInt() {
  // assertSame(Direction.NW, metric.getDirection(-1, 1));
  // assertSame(Direction.NE, metric.getDirection(0, 1));
  // assertSame(Direction.E, metric.getDirection(1, 0));
  // assertSame(Direction.SE, metric.getDirection(1, -1));
  // assertSame(Direction.SW, metric.getDirection(0, -1));
  // assertSame(Direction.W, metric.getDirection(-1, 0));
  // assertSame(Direction.INVALID, metric.getDirection(1, 1));
  // }

  /**
   * Test method for {@link magellan.library.gamebinding.MapMetric#toDirection(int)}.
   */
  @Test
  public void testToDirectionInt() {
    assertSame(NW, metric.toDirection(DIR_NW));
    assertSame(NE, metric.toDirection(DIR_NE));
    assertSame(E, metric.toDirection(DIR_E));
    assertSame(SE, metric.toDirection(DIR_SE));
    assertSame(SW, metric.toDirection(DIR_SW));
    assertSame(W, metric.toDirection(DIR_W));
    assertSame(Direction.INVALID, metric.toDirection(Direction.DIR_INVALID));
    assertSame(Direction.INVALID, metric.toDirection(42));
  }

  /**
   * Test method for {@link magellan.library.utils.Direction#toCoordinate()}.
   */
  @Test
  public void testToCoordinate() {
    assertEquals(NW.toCoordinate(), CoordinateID.create(-1, 1));
    assertEquals(NE.toCoordinate(), CoordinateID.create(0, 1));
    assertEquals(E.toCoordinate(), CoordinateID.create(1, 0));
    assertEquals(SE.toCoordinate(), CoordinateID.create(1, -1));
    assertEquals(SW.toCoordinate(), CoordinateID.create(0, -1));
    assertEquals(W.toCoordinate(), CoordinateID.create(-1, 0));
    assertEquals(Direction.INVALID.toCoordinate(), CoordinateID.ZERO);
  }

  /**
   * Test method for
   * {@link magellan.library.utils.Direction#toCoordinate()}.
   */
  @Test
  public void testToCoordinateDirection() {
    assertEquals(E.toCoordinate(), CoordinateID.create(1, 0));
  }

  //
  // /**
  // * Test method for {@link magellan.library.utils.Direction#toCoordinate(int)}.
  // */
  // @SuppressWarnings("deprecation")
  // @Test
  // public void testToCoordinateInt() {
  // assertEquals(Direction.DIR_E.toCoordinate(), CoordinateID.create(1, 0));
  // }

  /**
   * Test method for {@link magellan.library.utils.Direction#toString()}.
   */
  @Test
  public void testId() {
    assertEquals("NW", NW.getId().toString());
    assertEquals("NE", NE.getId().toString());
    assertEquals("E", E.getId().toString());
    assertEquals("SE", SE.getId().toString());
    assertEquals("SW", SW.getId().toString());
    assertEquals("W", W.getId().toString());
  }

  // /**
  // * Test method for {@link
  // magellan.library.utils.Direction#toInt(magellan.library.CoordinateID)}.
  // */
  // @SuppressWarnings("deprecation")
  // @Test
  // public void testToIntCoordinateID() {
  // assertSame(Direction.DIR_NW, Direction.toInt(CoordinateID.create(-1, 1)));
  // }

  // /**
  // * Test method for {@link magellan.library.utils.Direction#toInt(java.lang.String)}.
  // */
  // @SuppressWarnings("deprecation")
  // @Test
  // public void testToIntString() {
  // assertSame(Direction.DIR_NE, Direction.toInt("NO"));
  // }

  /**
   * Test method for {@link magellan.library.gamebinding.MapMetric#getDifference(Direction, Direction)}
   * 
   */
  @Test
  public void testGetDifferenceDirection() {
    assertEquals(0, metric.getDifference(SE, SE));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.MapMetric#getDifference(Direction, Direction)}.
   */
  @Test
  public void testGetDifferenceInt() {
    assertEquals(0, metric.getDifference(NW, NW));
    assertEquals(1, metric.getDifference(NW, NE));
    assertEquals(2, metric.getDifference(NW, E));
    assertEquals(3, metric.getDifference(NW, SE));
    assertEquals(-2, metric.getDifference(NW, SW));
    assertEquals(-1, metric.getDifference(NW, W));
    assertEquals(Integer.MAX_VALUE, metric.getDifference(NW, Direction.INVALID));
    assertEquals(0 - 1, metric.getDifference(NE, NW));
    assertEquals(1 - 1, metric.getDifference(NE, NE));
    assertEquals(2 - 1, metric.getDifference(NE, E));
    assertEquals(3 - 1, metric.getDifference(NE, SE));
    assertEquals(3, metric.getDifference(NE, SW));
    assertEquals(-2, metric.getDifference(NE, W));
    assertEquals(0 - 2, metric.getDifference(E, NW));
    assertEquals(1 - 2, metric.getDifference(E, NE));
    assertEquals(2 - 2, metric.getDifference(E, E));
    assertEquals(3 - 2, metric.getDifference(E, SE));
    assertEquals(2, metric.getDifference(E, SW));
    assertEquals(3, metric.getDifference(E, W));
    assertEquals(3, metric.getDifference(SE, NW));
    assertEquals(1 - 3, metric.getDifference(SE, NE));
    assertEquals(2 - 3, metric.getDifference(SE, E));
    assertEquals(3 - 3, metric.getDifference(SE, SE));
    assertEquals(1, metric.getDifference(SE, SW));
    assertEquals(2, metric.getDifference(SE, W));
    assertEquals(2, metric.getDifference(SW, NW));
    assertEquals(3, metric.getDifference(SW, NE));
    assertEquals(-2, metric.getDifference(SW, E));
    assertEquals(-1, metric.getDifference(SW, SE));
    assertEquals(0, metric.getDifference(SW, SW));
    assertEquals(1, metric.getDifference(SW, W));
    assertEquals(1, metric.getDifference(W, NW));
    assertEquals(2, metric.getDifference(W, NE));
    assertEquals(3, metric.getDifference(W, E));
    assertEquals(-2, metric.getDifference(W, SE));
    assertEquals(-1, metric.getDifference(W, SW));
    assertEquals(Integer.MAX_VALUE, metric.getDifference(Direction.INVALID, Direction.INVALID));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.MapMetric#getDirections()}.
   */
  @Test
  public void testGetDirections() {
    assertArrayEquals(new Direction[] { NW, NE, E, SE, SW, W }, metric.getDirections().toArray());
  }

  /**
   * 
   */
  @Test
  public void testSpiralPattern() {
    final StringBuilder builder = new StringBuilder();
    Utils.spiralPattern(CoordinateID.create(0, 0), 2, new Utils.SpiralVisitor<CoordinateID>() {
      public boolean visit(CoordinateID c, int distance) {
        builder.append(c.toString(",")).append(" ");
        return false;
      }

      public CoordinateID getResult() {
        return null;
      }
    });
    assertEquals(
        "0,0 1,0 0,1 -1,1 -1,0 0,-1 1,-1 2,0 1,1 0,2 -1,2 -2,2 -2,1 -2,0 -1,-1 0,-2 1,-2 2,-2 2,-1 ",
        builder.toString());

    final StringBuilder builder2 = new StringBuilder();
    Integer result =
        Utils.spiralPattern(CoordinateID.create(1, 2), 2, new Utils.SpiralVisitor<Integer>() {
          public boolean visit(CoordinateID c, int distance) {
            builder2.append(c.toString(",")).append(" ");
            return false;
          }

          public Integer getResult() {
            return new Integer(42);
          }
        });
    assertEquals("1,2 2,2 1,3 0,3 0,2 1,1 2,1 3,2 2,3 1,4 0,4 -1,4 -1,3 -1,2 0,1 1,0 2,0 3,0 3,1 ",
        builder2.toString());
    assertEquals(Integer.valueOf(42), result);

  }
  // /**
  // * Test method for {@link Direction#add(int)}.
  // */
  // @Test
  // public void testAdd() throws Exception {
  // assertSame(E, metric.add(E, 0));
  // assertSame(SE, metric.add(E, 1));
  // assertSame(SW, metric.add(E, 2));
  // assertSame(W, metric.add(E, 3));
  // assertSame(NW, metric.add(E, 4));
  // assertSame(NE, metric.add(E, 5));
  // assertSame(E, metric.add(E, 6));
  // assertSame(SE, metric.add(E, 7));
  // assertSame(NE, metric.add(E, -1));
  // assertSame(W, metric.add(E, -9));
  // assertSame(W, metric.add(SE, 2));
  // assertSame(NW, metric.add(SW, 2));
  // assertSame(NE, metric.add(W, 2));
  // assertSame(E, metric.add(NW, 2));
  // assertSame(SE, metric.add(NE, 2));
  // assertSame(SW, metric.add(E, 2));
  // assertSame(Direction.INVALID, metric.add(Direction.INVALID, 0));
  // assertSame(Direction.INVALID, metric.add(Direction.INVALID, 1));
  // assertSame(Direction.INVALID, metric.add(Direction.INVALID, 2));
  // assertSame(Direction.INVALID, metric.add(Direction.INVALID, 3));
  // assertSame(Direction.INVALID, metric.add(Direction.INVALID, 4));
  // assertSame(Direction.INVALID, metric.add(Direction.INVALID, 5));
  // assertSame(Direction.INVALID, metric.add(Direction.INVALID, 6));
  // assertSame(Direction.INVALID, metric.add(Direction.INVALID, -2));
  // }
}
