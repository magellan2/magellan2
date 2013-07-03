// class magellan.library.gamebinding.atlantis.AtlantisMapMetricTest
// created on Apr 29, 2013
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
package magellan.library.gamebinding.atlantis;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.gamebinding.MapMetric;
import magellan.library.utils.Direction;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

import org.junit.Before;
import org.junit.Test;

public class AtlantisMapMetricTest extends MagellanTestWithResources {

  private static final int DIR_M = 0;
  private static final int DIR_N = 1;
  private static final int DIR_E = 2;
  private static final int DIR_Y = 3;
  private static final int DIR_S = 4;
  private static final int DIR_W = 5;

  private Direction M = AtlantisMapMetric.M;
  private Direction N = AtlantisMapMetric.N;
  private Direction E = AtlantisMapMetric.E;
  private Direction Y = AtlantisMapMetric.Y;
  private Direction S = AtlantisMapMetric.S;
  private Direction W = AtlantisMapMetric.W;

  private MapMetric metric;

  @Before
  public void setUp() throws Exception {
    GameDataBuilder builder = new GameDataBuilder();
    builder.setGameName("atlantis");
    GameData data = builder.createSimpleGameData();
    metric = data.getGameSpecificStuff().getMapMetric();
  }

  @Test
  public void test() {
    assertTrue(metric instanceof AtlantisMapMetric);
  }

  /**
   * Test method for {@link magellan.library.utils.Direction#getDir()}.
   */
  @Test
  public void testGetDir() {
    assertEquals(0, M.getDirCode());
    assertEquals(1, N.getDirCode());
    assertEquals(2, E.getDirCode());
    assertEquals(3, Y.getDirCode());
    assertEquals(4, S.getDirCode());
    assertEquals(5, W.getDirCode());
    assertEquals(Direction.DIR_INVALID, Direction.INVALID.getDirCode());
  }

  /**
   * Test method for
   * {@link magellan.library.utils.Direction#toDirection(magellan.library.CoordinateID, magellan.library.CoordinateID)}
   * .
   */
  @Test
  public void testToDirectionCoordinateIDCoordinateID() {
    assertSame(M, metric.getDirection(CoordinateID.ZERO, CoordinateID.create(-1, -1)));
    assertSame(M, metric.getDirection(CoordinateID.create(0, 0, 1), CoordinateID.create(-1, -1, 1)));
    assertSame(N, metric.getDirection(CoordinateID.ZERO, CoordinateID.create(0, -1)));
    assertSame(E, metric.getDirection(CoordinateID.ZERO, CoordinateID.create(1, 0)));
    assertSame(Y, metric.getDirection(CoordinateID.ZERO, CoordinateID.create(1, 1)));
    assertSame(S, metric.getDirection(CoordinateID.ZERO, CoordinateID.create(0, 1)));
    assertSame(W, metric.getDirection(CoordinateID.ZERO, CoordinateID.create(-1, 0)));
    assertSame(Direction.INVALID, metric
        .getDirection(CoordinateID.ZERO, CoordinateID.create(-1, 1)));
    assertSame(Direction.INVALID, metric
        .getDirection(CoordinateID.ZERO, CoordinateID.create(1, -1)));
    assertSame(Direction.INVALID, metric.getDirection(CoordinateID.ZERO, CoordinateID.create(1, 0,
        1)));
  }

  /**
   * Test method for
   * {@link magellan.library.utils.Direction#toDirection(magellan.library.Region, magellan.library.Region)}
   * .
   */
  @Test
  public void testToDirectionRegionRegion() {
    GameDataBuilder builder = (new GameDataBuilder());
    builder.setGameName("atlantis");
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
  // assertSame(Direction.M, metric.getDirection(-1, 1));
  // assertSame(Direction.N, metric.getDirection(0, 1));
  // assertSame(Direction.E, metric.getDirection(1, 0));
  // assertSame(Direction.Y, metric.getDirection(1, -1));
  // assertSame(Direction.S, metric.getDirection(0, -1));
  // assertSame(Direction.W, metric.getDirection(-1, 0));
  // assertSame(Direction.INVALID, metric.getDirection(1, 1));
  // }

  /**
   * Test method for {@link magellan.library.utils.Direction#toDirection(int)}.
   */
  @Test
  public void testToDirectionInt() {
    assertSame(M, metric.toDirection(DIR_M));
    assertSame(N, metric.toDirection(DIR_N));
    assertSame(E, metric.toDirection(DIR_E));
    assertSame(Y, metric.toDirection(DIR_Y));
    assertSame(S, metric.toDirection(DIR_S));
    assertSame(W, metric.toDirection(DIR_W));
    assertSame(Direction.INVALID, metric.toDirection(Direction.DIR_INVALID));
    assertSame(Direction.INVALID, metric.toDirection(42));
  }

  /**
   * Test method for {@link magellan.library.utils.Direction#toCoordinate()}.
   */
  @Test
  public void testToCoordinate() {
    assertEquals(M.toCoordinate(), CoordinateID.create(-1, -1));
    assertEquals(N.toCoordinate(), CoordinateID.create(0, -1));
    assertEquals(E.toCoordinate(), CoordinateID.create(1, 0));
    assertEquals(Y.toCoordinate(), CoordinateID.create(1, 1));
    assertEquals(S.toCoordinate(), CoordinateID.create(0, 1));
    assertEquals(W.toCoordinate(), CoordinateID.create(-1, 0));
    assertEquals(Direction.INVALID.toCoordinate(), CoordinateID.ZERO);
  }

  /**
   * Test method for
   * {@link magellan.library.utils.Direction#toCoordinate(magellan.library.utils.Direction)}.
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
    assertEquals("M", M.getId().toString());
    assertEquals("N", N.getId().toString());
    assertEquals("E", E.getId().toString());
    assertEquals("Y", Y.getId().toString());
    assertEquals("S", S.getId().toString());
    assertEquals("W", W.getId().toString());
  }

  // /**
  // * Test method for {@link
  // magellan.library.utils.Direction#toInt(magellan.library.CoordinateID)}.
  // */
  // @SuppressWarnings("deprecation")
  // @Test
  // public void testToIntCoordinateID() {
  // assertSame(Direction.DIR_M, Direction.toInt(CoordinateID.create(-1, 1)));
  // }

  // /**
  // * Test method for {@link magellan.library.utils.Direction#toInt(java.lang.String)}.
  // */
  // @SuppressWarnings("deprecation")
  // @Test
  // public void testToIntString() {
  // assertSame(Direction.DIR_N, Direction.toInt("NO"));
  // }

  /**
   * Test method for
   * {@link magellan.library.utils.Direction#getDifference(magellan.library.utils.Direction)}.
   */
  @Test
  public void testGetDifferenceDirection() {
    assertEquals(0, metric.getDifference(S, S));
  }

  /**
   * Test method for {@link magellan.library.utils.Direction#getDifference(int)}.
   */
  @Test
  public void testGetDifferenceInt() {
    assertEquals(0, metric.getDifference(M, M));
    assertEquals(1, metric.getDifference(M, N));
    assertEquals(2, metric.getDifference(M, E));
    assertEquals(3, metric.getDifference(M, Y));
    assertEquals(-2, metric.getDifference(M, S));
    assertEquals(-1, metric.getDifference(M, W));
    assertEquals(Integer.MAX_VALUE, metric.getDifference(M, Direction.INVALID));
    assertEquals(0 - 1, metric.getDifference(N, M));
    assertEquals(1 - 1, metric.getDifference(N, N));
    assertEquals(2 - 1, metric.getDifference(N, E));
    assertEquals(3 - 1, metric.getDifference(N, Y));
    assertEquals(3, metric.getDifference(N, S));
    assertEquals(-2, metric.getDifference(N, W));
    assertEquals(0 - 2, metric.getDifference(E, M));
    assertEquals(1 - 2, metric.getDifference(E, N));
    assertEquals(2 - 2, metric.getDifference(E, E));
    assertEquals(3 - 2, metric.getDifference(E, Y));
    assertEquals(2, metric.getDifference(E, S));
    assertEquals(3, metric.getDifference(E, W));
    assertEquals(3, metric.getDifference(Y, M));
    assertEquals(1 - 3, metric.getDifference(Y, N));
    assertEquals(2 - 3, metric.getDifference(Y, E));
    assertEquals(3 - 3, metric.getDifference(Y, Y));
    assertEquals(1, metric.getDifference(Y, S));
    assertEquals(2, metric.getDifference(Y, W));
    assertEquals(2, metric.getDifference(S, M));
    assertEquals(3, metric.getDifference(S, N));
    assertEquals(-2, metric.getDifference(S, E));
    assertEquals(-1, metric.getDifference(S, Y));
    assertEquals(0, metric.getDifference(S, S));
    assertEquals(1, metric.getDifference(S, W));
    assertEquals(1, metric.getDifference(W, M));
    assertEquals(2, metric.getDifference(W, N));
    assertEquals(3, metric.getDifference(W, E));
    assertEquals(-2, metric.getDifference(W, Y));
    assertEquals(-1, metric.getDifference(W, S));
    assertEquals(Integer.MAX_VALUE, metric.getDifference(Direction.INVALID, Direction.INVALID));
  }

  /**
   * Test method for {@link magellan.library.utils.Direction#getDirections()}.
   */
  @Test
  public void testGetDirections() {
    assertArrayEquals(new Direction[] { M, N, E, Y, S, W }, metric.getDirections().toArray());
  }

  // /**
  // * Test method for {@link Direction#add(int)}.
  // */
  // @Test
  // public void testAdd() throws Exception {
  // assertSame(E, metric.add(E, 0));
  // assertSame(Y, metric.add(E, 1));
  // assertSame(S, metric.add(E, 2));
  // assertSame(W, metric.add(E, 3));
  // assertSame(M, metric.add(E, 4));
  // assertSame(N, metric.add(E, 5));
  // assertSame(E, metric.add(E, 6));
  // assertSame(Y, metric.add(E, 7));
  // assertSame(N, metric.add(E, -1));
  // assertSame(W, metric.add(E, -9));
  // assertSame(W, metric.add(Y, 2));
  // assertSame(M, metric.add(S, 2));
  // assertSame(N, metric.add(W, 2));
  // assertSame(E, metric.add(M, 2));
  // assertSame(Y, metric.add(N, 2));
  // assertSame(S, metric.add(E, 2));
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
