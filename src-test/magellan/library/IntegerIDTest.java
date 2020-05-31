// class magellan.library.IntegerIDTest
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Test class for {@link IntegerID}
 * 
 * @author stm
 */
public class IntegerIDTest {

  /**
   * Test method for {@link magellan.library.IntegerID#hashCode()}.
   */
  @Test
  public final void testHashCode() {
    IntegerID id1 = IntegerID.create(5), id2 = new IntegerID(5), id3 = new IntegerID("5"), id4 =
        IntegerID.create("5"), id5 = IntegerID.create("6");

    assertEquals(id1.hashCode(), id2.hashCode());
    assertEquals(id1.hashCode(), id3.hashCode());
    assertEquals(id1.hashCode(), id4.hashCode());
    assertNotSame(id1.hashCode(), id5.hashCode()); // this is not strictly necessary
  }

  /**
   * Test method for {@link magellan.library.IntegerID#IntegerID(int)}.
   */
  @Test
  public final void testIntegerIDInt() {
    IntegerID id1 = new IntegerID(6);
    assertEquals(6, id1.id);
    id1 = new IntegerID(-1);
    assertEquals(-1, id1.id);
    id1 = new IntegerID(Integer.MAX_VALUE);
    assertEquals(Integer.MAX_VALUE, id1.id);
  }

  /**
   * Test method for {@link magellan.library.IntegerID#IntegerID(java.lang.String)}.
   */
  @Test
  public final void testIntegerIDString() {
    IntegerID id1 = new IntegerID("6");
    assertEquals(6, id1.id);
    id1 = new IntegerID("-1");
    assertEquals(-1, id1.id);
    id1 = new IntegerID(String.valueOf(Integer.MAX_VALUE));
    assertEquals(Integer.MAX_VALUE, id1.id);
    try {
      id1 = new IntegerID("abc");
      fail();
    } catch (NumberFormatException e) {
      // expected!
    }
  }

  /**
   * Test method for {@link magellan.library.IntegerID#create(int)}.
   */
  @Test
  public final void testCreateInt() {
    IntegerID id1 = IntegerID.create(6);
    assertEquals(6, id1.id);
    id1 = IntegerID.create(-1);
    assertEquals(-1, id1.id);
    id1 = IntegerID.create(Integer.MAX_VALUE);
    assertEquals(Integer.MAX_VALUE, id1.id);

  }

  /**
   * Test method for {@link magellan.library.IntegerID#create(java.lang.String)}.
   */
  @Test
  public final void testCreateString() {
    IntegerID id1 = IntegerID.create("6");
    assertEquals(6, id1.id);
    id1 = IntegerID.create("-1");
    assertEquals(-1, id1.id);
    id1 = IntegerID.create(String.valueOf(Integer.MAX_VALUE));
    assertEquals(Integer.MAX_VALUE, id1.id);
    try {
      id1 = IntegerID.create("abc");
      fail();
    } catch (NumberFormatException e) {
      // expected!
    }
  }

  /**
   * Test method for {@link magellan.library.IntegerID#toString()}.
   */
  @Test
  public final void testToString() {
    IntegerID id1 = IntegerID.create(5);
    assertEquals("5", id1.toString());
    id1 = IntegerID.create(-4242);
    assertEquals("-4242", id1.toString());
  }

  /**
   * Test method for {@link magellan.library.IntegerID#toString(java.lang.String)}.
   */
  @Test
  public final void testToStringString() {
    IntegerID id1 = IntegerID.create(5);
    assertEquals("5", id1.toString("-"));
    id1 = IntegerID.create(-4242);
    assertEquals("-4242", id1.toString("."));
  }

  /**
   * Test method for {@link magellan.library.IntegerID#intValue()}.
   */
  @Test
  public final void testIntValue() {
    IntegerID id1 = new IntegerID("6");
    assertEquals(6, id1.intValue());
    id1 = new IntegerID("-1");
    assertEquals(-1, id1.intValue());
    id1 = new IntegerID(String.valueOf(Integer.MAX_VALUE));
    assertEquals(Integer.MAX_VALUE, id1.intValue());
  }

  /**
   * Test method for {@link magellan.library.IntegerID#equals(java.lang.Object)}.
   */
  @SuppressWarnings("unlikely-arg-type")
  @Test
  public final void testEqualsObject() {
    IntegerID id1 = IntegerID.create(42), id2 = IntegerID.create(42), id3 = IntegerID.create("42"), id4 =
        IntegerID.create(-42);
    assertTrue(id1.equals(id1));
    assertTrue(id1.equals(id2));
    assertTrue(id1.equals(id3));
    assertFalse(id1.equals(id4));
    assertTrue(id2.equals(id1));
    assertTrue(id2.equals(id2));
    assertTrue(id2.equals(id3));
    assertFalse(id2.equals(id4));
    assertTrue(id3.equals(id1));
    assertTrue(id3.equals(id2));
    assertTrue(id3.equals(id3));
    assertFalse(id3.equals(id4));
    assertTrue(id4.equals(id4));
    assertFalse(id4.equals(id3));
    assertFalse(id4.equals(id2));
    assertFalse(id4.equals(id1));

    IntegerID id5 = UnitID.createUnitID("abc", 36), id6 = UnitID.createUnitID(42, 10);
    assertTrue(id1.equals(id6));
    assertTrue(id6.equals(id1));
    assertFalse(id5.equals(id1));
    assertFalse(id1.equals(id5));

    IntegerID id8 = new MyIntegerID();
    assertTrue(id1.equals(id8));
    assertTrue(id8.equals(id1));
    assertFalse(id6.equals(id8));

    LongID id9 = LongID.create(42l);
    assertFalse(id1.equals(id9));
    assertFalse(id9.equals(id1));
    assertFalse(id8.equals(id9));
    assertFalse(id9.equals(id8));

  }

  class MyIntegerID extends IntegerID {
    MyIntegerID() {
      super(42);
    }
  }

  /**
   * Test method for {@link magellan.library.IntegerID#compareTo(java.lang.Object)}.
   */
  @Test
  public final void testCompareTo() {
    IntegerID id1 = IntegerID.create(42), id2 = IntegerID.create(42), id3 = IntegerID.create("42"), id4 =
        IntegerID.create(-42), id5 = IntegerID.create(4242);
    assertTrue(id1.compareTo(id1) == 0);
    assertTrue(id1.compareTo(id2) == 0);
    assertTrue(id1.compareTo(id3) == 0);
    assertTrue(id1.compareTo(id4) > 0);
    assertTrue(id1.compareTo(id5) < 0);

    assertTrue(id2.compareTo(id1) == 0);
    assertTrue(id2.compareTo(id2) == 0);
    assertTrue(id2.compareTo(id3) == 0);
    assertTrue(id2.compareTo(id4) > 0);
    assertTrue(id2.compareTo(id5) < 0);

    assertTrue(id3.compareTo(id1) == 0);
    assertTrue(id3.compareTo(id2) == 0);
    assertTrue(id3.compareTo(id3) == 0);
    assertTrue(id3.compareTo(id4) > 0);
    assertTrue(id3.compareTo(id5) < 0);

    assertTrue(id4.compareTo(id1) < 0);
    assertTrue(id4.compareTo(id2) < 0);
    assertTrue(id4.compareTo(id3) < 0);
    assertTrue(id4.compareTo(id4) == 0);
    assertTrue(id4.compareTo(id5) < 0);

    assertTrue(id5.compareTo(id1) > 0);
    assertTrue(id5.compareTo(id2) > 0);
    assertTrue(id5.compareTo(id3) > 0);
    assertTrue(id5.compareTo(id4) > 0);
    assertTrue(id5.compareTo(id5) == 0);

    IntegerID id6 = UnitID.createUnitID("abc", 36), id7 = UnitID.createUnitID(42, 10);
    assertTrue(id1.compareTo(id6) < 0);
    assertTrue(id1.compareTo(id7) == 0);
    assertTrue(id6.compareTo(id7) > 0);
    assertTrue(id7.compareTo(id6) < 0);

    IntegerID id8 = new MyIntegerID();
    assertTrue(id1.compareTo(id8) == 0);
    assertTrue(id8.compareTo(id1) == 0);
    assertTrue(id1.compareTo(id5) < 0);
    assertTrue(id5.compareTo(id1) > 0);

    try {
      id7.compareTo(id8);
      fail("Exception expected");
    } catch (ClassCastException e) {
      // expected!
    }

    LongID id9 = LongID.create(42l);
    try {
      assertTrue(id1.compareTo(id9) == 0);
      fail("Exception expected");
    } catch (ClassCastException e) {
      // expected!
    }

    IntegerID id10 = IntegerID.create(-41);
    assertTrue(id4.compareTo(id10) > 0);
  }

  /**
   * Test method for {@link magellan.library.IntegerID#clone()}.
   */
  @Test
  public final void testClone() {
    IntegerID id1 = IntegerID.create(15);
    assertEquals(id1.clone(), id1);
    // not required: assertNotSame(id1.clone(), id1);
    assertTrue(id1.clone().getClass() == IntegerID.class);
    IntegerID id2 = UnitID.createUnitID("ii", 36);
    assertEquals(id2.clone(), id2);
    // not required: assertNotSame(id1.clone(), id1);
    assertTrue(id2.clone().getClass() == UnitID.class);
  }

}
