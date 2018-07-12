// class magellan.library.utils.RegionsTest
// created on Jul 5, 2018
//
// Copyright 2003-2018 by magellan project team
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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
package magellan.library.utils;

import static magellan.library.utils.RegionsAssert.assertDistance;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import magellan.library.CoordinateID;

public class RegionsTest {

  @Test
  public void testGetDist() {
    CoordinateID r1 = CoordinateID.create(1, 0);
    assertEquals(0, Regions.getDist(r1, r1));

    CoordinateID r2 = CoordinateID.create(2, 0);
    assertDistance(r1, r2, 1);

    CoordinateID r3 = CoordinateID.create(1, 1);
    assertDistance(r1, r3, 1);

    for (int x = 2; x > -5; --x) {
      CoordinateID r4 = CoordinateID.create(x, 3);
      if (x >= 1) {
        assertDistance(r1, r4, x - 1 + 3);
      } else if (x >= -2) {
        assertDistance(r1, r4, 3);
      } else {
        assertDistance(r1, r4, -(x - 1));
      }
    }
    CoordinateID r6 = CoordinateID.create(-2, 3);
    assertDistance(r1, r6, 3);
  }

  @Test
  public void testConvexHull() {
    ArrayList<CoordinateID> points = new ArrayList<CoordinateID>();
    addPoint(points, 0, 0);

    assertArrayEquals(new CoordinateID[] { points.get(0) }, Regions
        .convexHull(points)
        .toArray(new CoordinateID[] {}));

    addPoint(points, 10, -2);
    assertArrayEquals(new CoordinateID[] { points.get(1), points.get(0) }, Regions
        .convexHull(points)
        .toArray(new CoordinateID[] {}));

    addPoint(points, 12, 3);
    addPoint(points, -2, 3);
    addPoint(points, 0, 3);
    addPoint(points, 3, 0);
    addPoint(points, 1, 1);
    addPoint(points, 5, -1);

    assertArrayEquals(new CoordinateID[] { points.get(1), points.get(2), points.get(3), points.get(0) }, Regions
        .convexHull(points)
        .toArray(new CoordinateID[] {}));

  }

  @Test
  public void testCollinearConvexHull() {
    ArrayList<CoordinateID> points = new ArrayList<CoordinateID>();
    addPoint(points, 0, 0);
    addPoint(points, -2, 1);
    addPoint(points, 2, -1);

    assertArrayEquals(new CoordinateID[] { points.get(2), points.get(1) }, Regions
        .convexHull(points)
        .toArray(new CoordinateID[] {}));
  }

  private void addPoint(List<CoordinateID> points, int x, int y) {
    points.add(CoordinateID.create(x, y));
  }

  @Test
  public void testInsideConvex() {
    List<CoordinateID> points = new ArrayList<CoordinateID>();
    addPoint(points, 0, 0);
    addPoint(points, 12, -4);
    addPoint(points, 12, 3);

    points = Regions.convexHull(points);
    assertEquals(1, Regions.insideConvex(CoordinateID.create(8, 1), points));
    assertEquals(0, Regions.insideConvex(CoordinateID.create(0, 0), points));
    assertEquals(-1, Regions.insideConvex(CoordinateID.create(-1, 0), points));
    assertEquals(0, Regions.insideConvex(CoordinateID.create(3, -1), points));
    assertEquals(0, Regions.insideConvex(CoordinateID.create(6, -2), points));
    assertEquals(0, Regions.insideConvex(CoordinateID.create(9, -3), points));
  }

  @Test
  public void testInsidePolygon() {
    List<CoordinateID> points = new ArrayList<CoordinateID>();
    addPoint(points, 0, 0);
    addPoint(points, 12, 0);
    addPoint(points, 6, 6);

    assertEquals(0, Regions.insidePolygon(CoordinateID.create(0, 0), points));
    assertEquals(1, Regions.insidePolygon(CoordinateID.create(6, 1), points));
    assertEquals(-1, Regions.insidePolygon(CoordinateID.create(5, 6), points));
    assertEquals(0, Regions.insidePolygon(CoordinateID.create(5, 5), points));
    assertEquals(-1, Regions.insidePolygon(CoordinateID.create(4, 5), points));
    assertEquals(-1, Regions.insidePolygon(CoordinateID.create(6, 7), points));
    assertEquals(-1, Regions.insidePolygon(CoordinateID.create(6, -1), points));
    assertEquals(-1, Regions.insidePolygon(CoordinateID.create(-1, 0), points));
    assertEquals(0, Regions.insidePolygon(CoordinateID.create(2, 0), points));
    assertEquals(-1, Regions.insidePolygon(CoordinateID.create(13, 0), points));

    points = new ArrayList<CoordinateID>();
    addPoint(points, 0, 0);
    addPoint(points, 2, 1);
    addPoint(points, 4, 2);
    addPoint(points, 0, 2);

    assertEquals(1, Regions.insidePolygon(CoordinateID.create(1, 1), points));

    points = new ArrayList<CoordinateID>();
    addPoint(points, 0, 0);
    addPoint(points, 3, -1);
    addPoint(points, 4, 0);
    addPoint(points, 2, 0);
    assertEquals(0, Regions.insidePolygon(CoordinateID.create(2, 0), points));

    points = new ArrayList<CoordinateID>();
    addPoint(points, -3, 0);
    addPoint(points, 0, -3);
    addPoint(points, 0, 0);
    assertEquals(-1, Regions.insidePolygon(CoordinateID.create(0, -4), points));
  }

  @Test
  public void testAlmostIntersects() {
    // general
    assertTrue(Regions.almostIntersects(0, 0, 2, 0, 1, 1, 1, -1));
    assertTrue(Regions.almostIntersects(0, 0, 5, 1, -1, 1, 4, -1));
    assertTrue(Regions.almostIntersects(0, 0, 5, 1, 4, -1, -1, 1));
    assertTrue(Regions.almostIntersects(5, 1, 0, 0, 4, -1, -1, 1));
    assertTrue(Regions.almostIntersects(5, 1, 0, 0, -1, 1, 4, -1));

    // touching
    assertTrue(Regions.almostIntersects(0, 0, 2, 0, 2, 1, 2, -1));
    assertFalse(Regions.almostIntersects(0, 0, 2, 0, 2.0001, 1, 2.0001, -1));
    assertFalse(Regions.almostIntersects(0, 0, 2, 0, 3, 1, 3, -1));
    assertFalse(Regions.almostIntersects(0, 0, 2, 0, -0.001, 1, -0.001, -1));
    // parallel
    assertFalse(Regions.almostIntersects(0, 0, 2, 0, 0, .0001, 2, .0001));
    assertTrue(Regions.almostIntersects(0, 0, 2, 0, 0, 0, 2, 0));
    assertTrue(Regions.almostIntersects(0, 0, 2, 0, -1, 0, 1, 0));
    assertTrue(Regions.almostIntersects(0, 0, 2, 0, 1, 0, 3, 0));
    assertTrue(Regions.almostIntersects(0, 0, 2, 0, .5, 0, 1.5, 0));
    assertTrue(Regions.almostIntersects(0, 0, 2, 0, -1, 0, 3, 0));
    assertTrue(Regions.almostIntersects(0, 0, 2, 0, 2, 0, 3, 0));
  }
}
