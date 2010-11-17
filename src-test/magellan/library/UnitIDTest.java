// class magellan.library.UnitIDTest
// created on Aug 7, 2010
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
package magellan.library;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test for UnitID
 * 
 * @author stm
 */
public class UnitIDTest {

  /**
   * Test for {@link UnitID#getNextDecimalID(int, int, boolean)}.
   */
  @Test
  public final void testGetNextDecimalTempID() {
    testID("2", "1", true);
    testID("9", "8", true);
    testID("10", "9", true);
    testID("20", "19", true);
    testID("100", "99", true);
    testID("10", "a", true);
    testID("200", "1a7", true);
    testID("1000", "a0n", true);
    testID("910", "909", true);
    testID("1", "9999", true);

    testID("9998", "9999", false);
    testID("9999", "99a9", false);
    testID("9989", "9990", false);
    testID("319", "320", false);
    testID("9", "10", false);
    testID("9", "c", false);
    testID("8", "9", false);
    testID("9999", "1", false);

    testID("1", "99999", true);
    testID("9999", "99999", false);
    assertEquals(1, UnitID.getNextDecimalID(-1, 36, true));
    assertEquals(1, UnitID.getNextDecimalID(-10000, 36, true));
    assertEquals(1, UnitID.getNextDecimalID(-10000000, 36, true));
    assertEquals(UnitID.createUnitID("9999", 36).intValue(), UnitID
        .getNextDecimalID(-10, 36, false));
  }

  private void testID(String target, String string, boolean ascending) {
    assertEquals(UnitID.createUnitID(target, 36).intValue(), UnitID.getNextDecimalID(UnitID
        .createUnitID(string, 36).intValue(), 36, ascending));
  }

}
