// class magellan.library.impl.MagellanIdentifiableImplTest
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
package magellan.library.impl;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import magellan.library.GameData;
import magellan.library.IntegerID;
import magellan.library.UnitID;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link MagellanIdentifiableImpl} (mainly compare/equals).
 * 
 * @author stm
 */
public class MagellanIdentifiableImplTest extends MagellanTestWithResources {

  private MagellanUnitImpl unit1;
  private MagellanUnitImpl unit2;
  private MagellanUnitImpl unit3;
  private MagellanUnitImpl unit4;
  private MagellanTempUnitImpl tempUnit1;
  private MagellanTempUnitImpl tempUnit2;
  private MagellanMessageImpl message1;
  private MagellanMessageImpl message2;
  private MagellanUnitImpl unit5;
  private MagellanMessageImpl message3;
  private MagellanMessageImpl message4;
  private GameData data;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    data = new GameDataBuilder().createSimplestGameData();
    unit1 = new MagellanUnitImpl(UnitID.createUnitID(42, 36), data);
    unit2 = new MagellanUnitImpl(UnitID.createUnitID(42, 36), data);
    unit3 = new MagellanUnitImpl(UnitID.createUnitID(42, 10), data);
    unit4 = new MagellanUnitImpl(UnitID.createUnitID(4242, 36), data);
    unit5 = new MagellanUnitImpl(UnitID.createUnitID(-42, 36), data);
    tempUnit1 = new MagellanTempUnitImpl(UnitID.createUnitID(-42, 36), unit1);
    tempUnit2 = new MagellanTempUnitImpl(UnitID.createUnitID(-43, 36), unit1);
    message1 = new MagellanMessageImpl(IntegerID.create(42));
    message2 = new MagellanMessageImpl("Hello World!");
    message3 = new MagellanMessageImpl(IntegerID.create(1234));
    message4 = new MagellanMessageImpl("Hello Worlds!");
  }

  /**
   * Test method for {@link magellan.library.impl.MagellanIdentifiableImpl#equals(java.lang.Object)}
   * .
   */
  @Test
  public final void testEqualsObject() {
    assertTrue(unit1.equals(unit1));
    assertTrue(unit1.equals(unit2));
    assertTrue(unit1.equals(unit3));
    assertTrue(!unit1.equals(unit4));
    assertTrue(!unit1.equals(unit5));
    assertTrue(!unit1.equals(tempUnit1));
    assertTrue(!unit1.equals(tempUnit2));
    assertTrue(!unit1.equals(message1));
    assertTrue(!unit1.equals(message2));

    assertTrue(unit2.equals(unit1));
    assertTrue(unit2.equals(unit2));
    assertTrue(unit2.equals(unit3));
    assertTrue(!unit2.equals(unit4));
    assertTrue(!unit2.equals(unit5));
    assertTrue(!unit2.equals(tempUnit1));
    assertTrue(!unit2.equals(tempUnit2));
    assertTrue(!unit2.equals(message1));
    assertTrue(!unit2.equals(message2));

    assertTrue(unit3.equals(unit1));
    assertTrue(unit3.equals(unit2));
    assertTrue(unit3.equals(unit3));
    assertTrue(!unit3.equals(unit4));
    assertTrue(!unit3.equals(unit5));
    assertTrue(!unit3.equals(tempUnit1));
    assertTrue(!unit3.equals(tempUnit2));
    assertTrue(!unit3.equals(message1));
    assertTrue(!unit3.equals(message2));

    assertTrue(!unit4.equals(unit1));
    assertTrue(!unit4.equals(unit2));
    assertTrue(!unit4.equals(unit3));
    assertTrue(unit4.equals(unit4));
    assertTrue(!unit4.equals(unit5));
    assertTrue(!unit4.equals(tempUnit1));
    assertTrue(!unit4.equals(tempUnit2));
    assertTrue(!unit4.equals(message1));
    assertTrue(!unit4.equals(message2));

    assertTrue(unit1.equals(unit1));
    assertTrue(unit2.equals(unit1));
    assertTrue(unit3.equals(unit1));
    assertTrue(!unit4.equals(unit1));
    assertTrue(!unit5.equals(unit1));
    assertTrue(!tempUnit1.equals(unit1));
    assertTrue(!message1.equals(unit1));

    assertTrue(!tempUnit1.equals(unit1));
    assertTrue(!tempUnit1.equals(unit2));
    assertTrue(!tempUnit1.equals(unit3));
    assertTrue(!tempUnit1.equals(unit4));
    assertTrue(tempUnit1.equals(unit5));
    assertTrue(tempUnit1.equals(tempUnit1));
    assertTrue(!tempUnit1.equals(tempUnit2));
    assertTrue(!tempUnit1.equals(message1));
    assertTrue(!tempUnit1.equals(message2));

    assertTrue(!tempUnit2.equals(unit1));
    assertTrue(!tempUnit2.equals(unit2));
    assertTrue(!tempUnit2.equals(unit3));
    assertTrue(!tempUnit2.equals(unit4));
    assertTrue(!tempUnit2.equals(unit5));
    assertTrue(!tempUnit2.equals(tempUnit1));
    assertTrue(tempUnit2.equals(tempUnit2));
    assertTrue(!tempUnit2.equals(message1));
    assertTrue(!tempUnit2.equals(message2));

    assertTrue(!message1.equals(unit1));
    assertTrue(!message1.equals(unit2));
    assertTrue(!message1.equals(unit3));
    assertTrue(!message1.equals(unit4));
    assertTrue(!message1.equals(unit5));
    assertTrue(!message1.equals(tempUnit1));
    assertTrue(!message1.equals(tempUnit2));
    assertTrue(message1.equals(message1));
    assertTrue(!message1.equals(message2));

    assertTrue(!message2.equals(unit1));
    assertTrue(!message2.equals(unit2));
    assertTrue(!message2.equals(unit3));
    assertTrue(!message2.equals(unit4));
    assertTrue(!message2.equals(unit5));
    assertTrue(!message2.equals(tempUnit1));
    assertTrue(!message2.equals(tempUnit2));
    assertTrue(!message2.equals(message1));
    assertTrue(message2.equals(message2));

    assertTrue(!message2.equals(message4));
    assertTrue(!message3.equals(message4));
    assertTrue(!message4.equals(message2));
    assertTrue(!message4.equals(message3));

  }

  /**
   * Test method for
   * {@link magellan.library.impl.MagellanIdentifiableImpl#compareTo(java.lang.Object)}.
   */
  @Test
  public final void testCompareTo() {
    assertTrue(unit1.compareTo(unit1) == 0);
    assertTrue(unit1.compareTo(unit2) == 0);
    assertTrue(unit1.compareTo(unit3) == 0);
    assertTrue(unit1.compareTo(unit4) < 0);
    assertTrue(unit1.compareTo(unit5) > 0);
    assertTrue(unit1.compareTo(tempUnit1) > 0);
    assertTrue(unit1.compareTo(tempUnit2) > 0);
    try {
      assertTrue(unit1.compareTo(message1) < 0);
      fail("exception expected");
    } catch (ClassCastException e) {
      // expected
    }
    try {
      assertTrue(unit1.compareTo(message2) < 0);
      fail("exception expected");
    } catch (ClassCastException e) {
      // expected
    }

    assertTrue(unit2.compareTo(unit1) == 0);
    assertTrue(unit2.compareTo(unit2) == 0);
    assertTrue(unit2.compareTo(unit3) == 0);
    assertTrue(unit2.compareTo(unit4) < 0);
    assertTrue(unit2.compareTo(unit5) > 0);
    assertTrue(unit2.compareTo(tempUnit1) > 0);
    assertTrue(unit2.compareTo(tempUnit2) > 0);
    try {
      assertTrue(unit2.compareTo(message1) < 0);
      fail("exception expected");
    } catch (ClassCastException e) {
      // expected
    }
    try {
      assertTrue(unit2.compareTo(message2) < 0);
      fail("exception expected");
    } catch (ClassCastException e) {
      // expected
    }

    assertTrue(unit3.compareTo(unit1) == 0);
    assertTrue(unit3.compareTo(unit2) == 0);
    assertTrue(unit3.compareTo(unit3) == 0);
    assertTrue(unit3.compareTo(unit4) < 0);
    assertTrue(unit3.compareTo(unit5) > 0);
    assertTrue(unit3.compareTo(tempUnit1) > 0);
    assertTrue(unit3.compareTo(tempUnit2) > 0);

    assertTrue(unit1.compareTo(unit1) == 0);
    assertTrue(unit2.compareTo(unit1) == 0);
    assertTrue(unit3.compareTo(unit1) == 0);
    assertTrue(unit4.compareTo(unit1) > 0);
    assertTrue(unit5.compareTo(unit1) < 0);
    assertTrue(tempUnit1.compareTo(unit1) < 0);
    assertTrue(tempUnit2.compareTo(unit1) < 0);
    try {
      assertTrue(message1.compareTo(unit1) < 0);
      fail("exception expected");
    } catch (ClassCastException e) {
      // expected
    }
    try {
      assertTrue(message2.compareTo(unit1) < 0);
      fail("exception expected");
    } catch (ClassCastException e) {
      // expected
    }

    assertTrue(tempUnit1.compareTo(tempUnit2) < 0);
    assertTrue(tempUnit2.compareTo(tempUnit1) > 0);

    assertTrue(message1.compareTo(message1) == 0);
    assertTrue(message1.compareTo(message2) > 0);
    assertTrue(message1.compareTo(message3) < 0);
    assertTrue(message1.compareTo(message4) > 0);

    assertTrue(message2.compareTo(message1) < 0);
    assertTrue(message2.compareTo(message2) == 0);
    assertTrue(message2.compareTo(message3) < 0);
    assertTrue(message2.compareTo(message4) < 0);

    assertTrue(message3.compareTo(message1) > 0);
    assertTrue(message3.compareTo(message2) > 0);
    assertTrue(message3.compareTo(message3) == 0);
    assertTrue(message3.compareTo(message4) > 0);

    assertTrue(message4.compareTo(message1) < 0);
    assertTrue(message4.compareTo(message2) > 0);
    assertTrue(message4.compareTo(message3) < 0);
    assertTrue(message4.compareTo(message4) == 0);
  }
}
