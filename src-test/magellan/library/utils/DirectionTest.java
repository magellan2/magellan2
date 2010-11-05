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

import java.io.File;
import java.util.Properties;

import magellan.client.MagellanContext;
import magellan.client.event.EventDispatcher;
import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.utils.logging.Logger;
import magellan.test.GameDataBuilder;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for the direction class
 * 
 * @author stm
 * @version 1.0, Feb 9, 2010
 */
public class DirectionTest {

  private static MagellanContext context;
  private static Properties settings;

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    DirectionTest.settings = new Properties(); // Client.loadSettings(PARSER_SETTINGS_DIRECTORY,
    // PARSER_SETTINGS_FILE);
    Resources.getInstance().initialize(new File("."), "");
    System.out.println(new File(".").getAbsolutePath());
    DirectionTest.context = new MagellanContext(null);
    DirectionTest.context.setProperties(DirectionTest.settings);
    DirectionTest.context.setEventDispatcher(new EventDispatcher());
    Logger.setLevel(Logger.ERROR);
    DirectionTest.context.init();
  }

  /**
   * Test method for {@link magellan.library.utils.Direction#getDir()}.
   */
  @Test
  public void testGetDir() {
    assertEquals(0, Direction.NW.getDir());
    assertEquals(1, Direction.NE.getDir());
    assertEquals(2, Direction.E.getDir());
    assertEquals(3, Direction.SE.getDir());
    assertEquals(4, Direction.SW.getDir());
    assertEquals(5, Direction.W.getDir());
    assertEquals(-1, Direction.INVALID.getDir());
    assertSame(Direction.DIR_NW, Direction.NW.getDir());
    assertSame(Direction.DIR_NE, Direction.NE.getDir());
    assertSame(Direction.DIR_SE, Direction.SE.getDir());
    assertSame(Direction.DIR_SW, Direction.SW.getDir());
    assertSame(Direction.DIR_W, Direction.W.getDir());
    assertSame(Direction.DIR_NW, Direction.NW.getDir());
    assertSame(Direction.DIR_INVALID, Direction.INVALID.getDir());
  }

  /**
   * Test method for
   * {@link magellan.library.utils.Direction#toDirection(magellan.library.CoordinateID, magellan.library.CoordinateID)}
   * .
   */
  @Test
  public void testToDirectionCoordinateIDCoordinateID() {
    assertSame(Direction.NW, Direction.toDirection(CoordinateID.ZERO, CoordinateID.create(-1, 1)));
    assertSame(Direction.NW, Direction.toDirection(CoordinateID.create(0, 0, 1), CoordinateID
        .create(-1, 1, 1)));
    assertSame(Direction.NE, Direction.toDirection(CoordinateID.ZERO, CoordinateID.create(0, 1)));
    assertSame(Direction.E, Direction.toDirection(CoordinateID.ZERO, CoordinateID.create(1, 0)));
    assertSame(Direction.SE, Direction.toDirection(CoordinateID.ZERO, CoordinateID.create(1, -1)));
    assertSame(Direction.SW, Direction.toDirection(CoordinateID.ZERO, CoordinateID.create(0, -1)));
    assertSame(Direction.W, Direction.toDirection(CoordinateID.ZERO, CoordinateID.create(-1, 0)));
    assertSame(Direction.INVALID, Direction.toDirection(CoordinateID.ZERO, CoordinateID
        .create(1, 1)));
    assertSame(Direction.INVALID, Direction.toDirection(CoordinateID.ZERO, CoordinateID.create(1,
        0, 1)));
  }

  /**
   * Test method for
   * {@link magellan.library.utils.Direction#toDirection(magellan.library.Region, magellan.library.Region)}
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
    Region r0 = data.getRegions().iterator().next();
    Region r1 = builder.addRegion(data, "1 0", "Ebene", "Ebene", 2);
    r1.setUID(1234);
    data.addRegion(r1);
    Region r2 = builder.addRegion(data, "5 0", "Wrapper", "Ebene", 3);
    r2.setUID(12345);
    data.addRegion(r2);

    r0.addNeighbor(Direction.W, r2);
    r2.addNeighbor(Direction.E, r0);

    data.getGameSpecificStuff().postProcess(data);

    assertSame(Direction.E, Direction.toDirection(r0, r1));
    assertSame(Direction.W, Direction.toDirection(r1, r0));
    assertSame(Direction.E, Direction.toDirection(r2, r0));
    assertSame(Direction.W, Direction.toDirection(r0, r2));
    assertSame(Direction.INVALID, Direction.toDirection(r1, r2));
  }

  /**
   * Test method for
   * {@link magellan.library.utils.Direction#toDirection(magellan.library.CoordinateID)}.
   */
  @Test
  public void testToDirectionCoordinateID() {
    assertSame(Direction.E, Direction.toDirection(CoordinateID.create(1, 0)));
  }

  /**
   * Test method for {@link magellan.library.utils.Direction#toDirection(int, int)}.
   */
  @Test
  public void testToDirectionIntInt() {
    assertSame(Direction.NW, Direction.toDirection(-1, 1));
    assertSame(Direction.NE, Direction.toDirection(0, 1));
    assertSame(Direction.E, Direction.toDirection(1, 0));
    assertSame(Direction.SE, Direction.toDirection(1, -1));
    assertSame(Direction.SW, Direction.toDirection(0, -1));
    assertSame(Direction.W, Direction.toDirection(-1, 0));
    assertSame(Direction.INVALID, Direction.toDirection(1, 1));
  }

  /**
   * Test method for {@link magellan.library.utils.Direction#toDirection(java.lang.String)}.
   */
  @Test
  public void testToDirectionString() {
    assertSame(Direction.NW, Direction.toDirection("NW"));
    assertSame(Direction.NE, Direction.toDirection("NO"));
    assertSame(Direction.E, Direction.toDirection("O"));
    assertSame(Direction.SE, Direction.toDirection("SO"));
    assertSame(Direction.SW, Direction.toDirection("SW"));
    assertSame(Direction.W, Direction.toDirection("W"));
    assertSame(Direction.NW, Direction.toDirection("NordWEsten"));
    assertSame(Direction.INVALID, Direction.toDirection("Nach Hause"));
  }

  /**
   * Test method for {@link magellan.library.utils.Direction#toDirection(int)}.
   */
  @Test
  public void testToDirectionInt() {
    assertSame(Direction.NW, Direction.toDirection(Direction.DIR_NW));
    assertSame(Direction.NE, Direction.toDirection(Direction.DIR_NE));
    assertSame(Direction.E, Direction.toDirection(Direction.DIR_E));
    assertSame(Direction.SE, Direction.toDirection(Direction.DIR_SE));
    assertSame(Direction.SW, Direction.toDirection(Direction.DIR_SW));
    assertSame(Direction.W, Direction.toDirection(Direction.DIR_W));
    assertSame(Direction.INVALID, Direction.toDirection(Direction.DIR_INVALID));
    assertSame(Direction.INVALID, Direction.toDirection(42));
  }

  /**
   * Test method for {@link magellan.library.utils.Direction#toCoordinate()}.
   */
  @Test
  public void testToCoordinate() {
    assertEquals(Direction.NW.toCoordinate(), CoordinateID.create(-1, 1));
    assertEquals(Direction.NE.toCoordinate(), CoordinateID.create(0, 1));
    assertEquals(Direction.E.toCoordinate(), CoordinateID.create(1, 0));
    assertEquals(Direction.SE.toCoordinate(), CoordinateID.create(1, -1));
    assertEquals(Direction.SW.toCoordinate(), CoordinateID.create(0, -1));
    assertEquals(Direction.W.toCoordinate(), CoordinateID.create(-1, 0));
    assertEquals(Direction.INVALID.toCoordinate(), CoordinateID.ZERO);
  }

  /**
   * Test method for
   * {@link magellan.library.utils.Direction#toCoordinate(magellan.library.utils.Direction)}.
   */
  @Test
  public void testToCoordinateDirection() {
    assertEquals(Direction.toCoordinate(Direction.E), CoordinateID.create(1, 0));
  }

  /**
   * Test method for {@link magellan.library.utils.Direction#toCoordinate(int)}.
   */
  @Test
  public void testToCoordinateInt() {
    assertEquals(Direction.toCoordinate(Direction.DIR_E), CoordinateID.create(1, 0));
  }

  /**
   * Test method for {@link magellan.library.utils.Direction#toString()}.
   */
  @Test
  public void testToString() {
    assertEquals("NORDOSTEN", Direction.NE.toString());
  }

  /**
   * Test method for {@link magellan.library.utils.Direction#toString(boolean)}.
   */
  @Test
  public void testToStringBoolean() {
    assertEquals("NORDOSTEN", Direction.NE.toString(false));
    assertEquals("SÜDWESTEN", Direction.SW.toString(false));
    assertEquals("NO", Direction.NE.toString(true));
  }

  /**
   * Test method for {@link magellan.library.utils.Direction#toString(int)}.
   */
  @Test
  public void testToStringInt() {
    assertEquals("NORDOSTEN", Direction.toString(Direction.DIR_NE));
  }

  /**
   * Test method for {@link magellan.library.utils.Direction#toString(int, boolean)}.
   */
  @Test
  public void testToStringIntBoolean() {
    assertEquals("NORDOSTEN", Direction.toString(Direction.DIR_NE, false));
    assertEquals("NO", Direction.toString(Direction.DIR_NE, true));
  }

  /**
   * Test method for
   * {@link magellan.library.utils.Direction#toString(magellan.library.CoordinateID)}.
   */
  @Test
  public void testToStringCoordinateID() {
    assertEquals("NORDWESTEN", Direction.toString(CoordinateID.create(-1, 1)));
  }

  /**
   * Test method for {@link magellan.library.utils.Direction#toInt(magellan.library.CoordinateID)}.
   */
  @Test
  public void testToIntCoordinateID() {
    assertSame(Direction.DIR_NW, Direction.toInt(CoordinateID.create(-1, 1)));
  }

  /**
   * Test method for {@link magellan.library.utils.Direction#toInt(java.lang.String)}.
   */
  @Test
  public void testToIntString() {
    assertSame(Direction.DIR_NE, Direction.toInt("NO"));
  }

  /**
   * Test method for {@link magellan.library.utils.Direction#getShortNames()}.
   */
  @Test
  public void testGetShortNames() {
    assertArrayEquals(new String[] { "nw", "no", "o", "so", "sw", "w" }, Direction.getShortNames()
        .toArray());
  }

  /**
   * Test method for {@link magellan.library.utils.Direction#getLongNames()}.
   */
  @Test
  public void testGetLongNames() {
    assertArrayEquals(new String[] { "nordwesten", "nordosten", "osten", "südosten", "südwesten",
        "westen" }, Direction.getLongNames().toArray());
  }

  /**
   * Test method for
   * {@link magellan.library.utils.Direction#getDifference(magellan.library.utils.Direction)}.
   */
  @Test
  public void testGetDifferenceDirection() {
    assertEquals(0, Direction.SE.getDifference(Direction.SE));
  }

  /**
   * Test method for {@link magellan.library.utils.Direction#getDifference(int)}.
   */
  @Test
  public void testGetDifferenceInt() {
    assertEquals(0, Direction.NW.getDifference(Direction.DIR_NW));
    assertEquals(1, Direction.NW.getDifference(Direction.DIR_NE));
    assertEquals(2, Direction.NW.getDifference(Direction.DIR_E));
    assertEquals(3, Direction.NW.getDifference(Direction.DIR_SE));
    assertEquals(-2, Direction.NW.getDifference(Direction.DIR_SW));
    assertEquals(-1, Direction.NW.getDifference(Direction.DIR_W));
    assertEquals(Integer.MAX_VALUE, Direction.NW.getDifference(Direction.DIR_INVALID));
    assertEquals(0 - 1, Direction.NE.getDifference(Direction.DIR_NW));
    assertEquals(1 - 1, Direction.NE.getDifference(Direction.DIR_NE));
    assertEquals(2 - 1, Direction.NE.getDifference(Direction.DIR_E));
    assertEquals(3 - 1, Direction.NE.getDifference(Direction.DIR_SE));
    assertEquals(3, Direction.NE.getDifference(Direction.DIR_SW));
    assertEquals(-2, Direction.NE.getDifference(Direction.DIR_W));
    assertEquals(0 - 2, Direction.E.getDifference(Direction.DIR_NW));
    assertEquals(1 - 2, Direction.E.getDifference(Direction.DIR_NE));
    assertEquals(2 - 2, Direction.E.getDifference(Direction.DIR_E));
    assertEquals(3 - 2, Direction.E.getDifference(Direction.DIR_SE));
    assertEquals(2, Direction.E.getDifference(Direction.DIR_SW));
    assertEquals(3, Direction.E.getDifference(Direction.DIR_W));
    assertEquals(3, Direction.SE.getDifference(Direction.DIR_NW));
    assertEquals(1 - 3, Direction.SE.getDifference(Direction.DIR_NE));
    assertEquals(2 - 3, Direction.SE.getDifference(Direction.DIR_E));
    assertEquals(3 - 3, Direction.SE.getDifference(Direction.DIR_SE));
    assertEquals(1, Direction.SE.getDifference(Direction.DIR_SW));
    assertEquals(2, Direction.SE.getDifference(Direction.DIR_W));
    assertEquals(2, Direction.SW.getDifference(Direction.DIR_NW));
    assertEquals(3, Direction.SW.getDifference(Direction.DIR_NE));
    assertEquals(-2, Direction.SW.getDifference(Direction.DIR_E));
    assertEquals(-1, Direction.SW.getDifference(Direction.DIR_SE));
    assertEquals(0, Direction.SW.getDifference(Direction.DIR_SW));
    assertEquals(1, Direction.SW.getDifference(Direction.DIR_W));
    assertEquals(1, Direction.W.getDifference(Direction.DIR_NW));
    assertEquals(2, Direction.W.getDifference(Direction.DIR_NE));
    assertEquals(3, Direction.W.getDifference(Direction.DIR_E));
    assertEquals(-2, Direction.W.getDifference(Direction.DIR_SE));
    assertEquals(-1, Direction.W.getDifference(Direction.DIR_SW));
    assertEquals(Integer.MAX_VALUE, Direction.INVALID.getDifference(Direction.DIR_INVALID));
  }

  /**
   * Test method for {@link magellan.library.utils.Direction#getDirections()}.
   */
  @Test
  public void testGetDirections() {
    assertArrayEquals(new Direction[] { Direction.NW, Direction.NE, Direction.E, Direction.SE,
        Direction.SW, Direction.W }, Direction.getDirections().toArray());
  }

  /**
   * Test method for {@link Direction#add(int)}.
   */
  @Test
  public void testAdd() throws Exception {
    assertSame(Direction.E, Direction.E.add(0));
    assertSame(Direction.SE, Direction.E.add(1));
    assertSame(Direction.SW, Direction.E.add(2));
    assertSame(Direction.W, Direction.E.add(3));
    assertSame(Direction.NW, Direction.E.add(4));
    assertSame(Direction.NE, Direction.E.add(5));
    assertSame(Direction.E, Direction.E.add(6));
    assertSame(Direction.SE, Direction.E.add(7));
    assertSame(Direction.NE, Direction.E.add(-1));
    assertSame(Direction.W, Direction.E.add(-9));
    assertSame(Direction.W, Direction.SE.add(2));
    assertSame(Direction.NW, Direction.SW.add(2));
    assertSame(Direction.NE, Direction.W.add(2));
    assertSame(Direction.E, Direction.NW.add(2));
    assertSame(Direction.SE, Direction.NE.add(2));
    assertSame(Direction.SW, Direction.E.add(2));
    assertSame(Direction.INVALID, Direction.INVALID.add(0));
    assertSame(Direction.INVALID, Direction.INVALID.add(1));
    assertSame(Direction.INVALID, Direction.INVALID.add(2));
    assertSame(Direction.INVALID, Direction.INVALID.add(3));
    assertSame(Direction.INVALID, Direction.INVALID.add(4));
    assertSame(Direction.INVALID, Direction.INVALID.add(5));
    assertSame(Direction.INVALID, Direction.INVALID.add(6));
    assertSame(Direction.INVALID, Direction.INVALID.add(-2));
  }
}
