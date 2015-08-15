// class magellan.library.utils.filters.UnitFilterTest
// created on Apr 28, 2015
//
// Copyright 2003-2015 by magellan project team
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
package magellan.library.utils.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.utils.MagellanFactory;
import magellan.test.GameDataBuilder;

import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class UnitFilterTest {

  private static GameData data;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    data = new GameDataBuilder().createSimplestGameData();
  }

  private static Collection<Unit> createUnits(int n) {
    ArrayList<Unit> units = new ArrayList<Unit>(n);
    for (int i = 0; i < n; ++i) {
      units.add(createUnit(i));
    }
    return units;
  }

  /**
   *
   */
  @Test
  public final void testSameNotSame() {
    UnitFilter filter = createFilter();
    List<Unit> col = Collections.emptyList();
    Collection<Unit> col2 = filter.acceptUnits(col, true);
    assertSame(col, col2);
    col2 = filter.acceptUnits(col, false);
    assertNotSame(col, col2);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testRemove() throws Exception {
    UnitFilter filter = createFilter(2, 4);
    Collection<Unit> col = createUnits(5);
    Collection<Unit> col2 = filter.acceptUnits(col, true);
    assertCollection(col2, 2, 4);
  }

  private void assertCollection(Collection<Unit> col2, int... args) {
    assertEquals(args.length, col2.size());

    int i = 0;
    for (Unit u : col2) {
      assertEquals(args[i++], u.getID().intValue());
    }

  }

  private static Unit createUnit(int i) {
    return MagellanFactory.createUnit(UnitID.createUnitID(i, data.base), data);
  }

  private UnitFilter createFilter(final int... args) {
    return new UnitFilter() {

      @Override
      public boolean acceptUnit(Unit u) {
        return (Arrays.binarySearch(args, u.getID().intValue()) >= 0);
      }
    };
  }

}
