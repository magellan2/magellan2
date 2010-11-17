// class magellan.library.rules.EresseaDateTest
// created on Feb 21, 2010
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
package magellan.library.rules;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import magellan.test.MagellanTestWithResources;

import org.junit.Test;

/**
 * Tests for class EresseaDate
 * 
 * @author stm
 * @version 1.0, Feb 21, 2010
 */
public class EresseaDateTest extends MagellanTestWithResources {

  /**
   * Test method for {@link EresseaDate#getWeekFromStart()}.
   */
  @Test
  public void testGet() {
    EresseaDate date = new EresseaDate(1);
    Assert.assertEquals(1, date.getWeekFromStart());
    date = new EresseaDate(1);
    date.setEpoch(1);
    Assert.assertEquals(1, date.getWeekFromStart());
    date = new EresseaDate(184);
    date.setEpoch(2);
    Assert.assertEquals(0, date.getWeekFromStart());
    date = new EresseaDate(1);
    date.setEpoch(3);
    Assert.assertEquals(0, date.getWeekFromStart());

    date = new EresseaDate(1, 1, 1, 1);
    Assert.assertEquals(0, date.getWeekFromStart());
    date.setEpoch(2);
    Assert.assertEquals(-183, date.getWeekFromStart());
    date.setEpoch(3);
    Assert.assertEquals(0, date.getWeekFromStart());

    date = new EresseaDate(184, -6, 3, 1);
    Assert.assertEquals(0, date.getWeekFromStart());
    date.setEpoch(2);
    Assert.assertEquals(0, date.getWeekFromStart());
    date.setEpoch(3);
    Assert.assertEquals(183, date.getWeekFromStart());

    date = new EresseaDate(1, 0, 0, 0);
    Assert.assertEquals(-29, date.getWeekFromStart());

  }

  /**
   * Test method for {@link magellan.library.rules.EresseaDate#getSeason()}.
   */
  @Test
  public final void testGetSeason() {
    EresseaDate date = new EresseaDate(1);
    date.setEpoch(1);
    Assert.assertEquals(Date.UNKNOWN, date.getSeason());

    date.setEpoch(2);
    for (int d = 185; d < 187; ++d) {
      date.setDate(d);
      Assert.assertEquals("Date " + d + " epoch " + 2, Date.SUMMER, date.getSeason());
    }
    for (int d = 187; d < 193; ++d) {
      date.setDate(d);
      Assert.assertEquals("Date " + d + " epoch " + 2, Date.AUTUMN, date.getSeason());
    }
    for (int d = 193; d < 202; ++d) {
      date.setDate(d);
      Assert.assertEquals("Date " + d + " epoch " + 2, Date.WINTER, date.getSeason());
    }
    for (int d = 202; d < 208; ++d) {
      date.setDate(d);
      Assert.assertEquals("Date " + d + " epoch " + 2, Date.SPRING, date.getSeason());
    }
    for (int d = 208; d < 214; ++d) {
      date.setDate(d);
      Assert.assertEquals("Date " + d + " epoch " + 2, Date.SUMMER, date.getSeason());
    }
    date.setDate(214);
    Assert.assertEquals("Date " + 214 + " epoch " + 2, Date.AUTUMN, date.getSeason());

    date.setEpoch(3);
    for (int d = 1; d < 4; ++d) {
      date.setDate(d);
      Assert.assertEquals("Date " + d + " epoch " + 3, Date.SUMMER, date.getSeason());
    }
    for (int d = 4; d < 10; ++d) {
      date.setDate(d);
      Assert.assertEquals("Date " + d + " epoch " + 3, Date.AUTUMN, date.getSeason());
    }
    for (int d = 10; d < 19; ++d) {
      date.setDate(d);
      Assert.assertEquals("Date " + d + " epoch " + 3, Date.WINTER, date.getSeason());
    }
    for (int d = 19; d < 25; ++d) {
      date.setDate(d);
      Assert.assertEquals("Date " + d + " epoch " + 3, Date.SPRING, date.getSeason());
    }
    for (int d = 25; d < 31; ++d) {
      date.setDate(d);
      Assert.assertEquals("Date " + d + " epoch " + 3, Date.SUMMER, date.getSeason());
    }

    // second age -6, 2, 3);
    date = new EresseaDate(616, -6, 2, 3);
    date.setEpoch(2);
    Assert.assertEquals(Date.SUMMER, date.getSeason());
    date = new EresseaDate(618, -6, 2, 3);
    date.setEpoch(2);
    Assert.assertEquals(Date.SUMMER, date.getSeason());
    date = new EresseaDate(619, -6, 2, 3);
    date.setEpoch(2);
    Assert.assertEquals(Date.AUTUMN, date.getSeason());
    date = new EresseaDate(624, -6, 2, 3);
    date.setEpoch(2);
    Assert.assertEquals(Date.AUTUMN, date.getSeason());
    date = new EresseaDate(625, -6, 2, 3);
    date.setEpoch(2);
    Assert.assertEquals(Date.WINTER, date.getSeason());
    date = new EresseaDate(633, -6, 2, 3);
    date.setEpoch(2);
    Assert.assertEquals(Date.WINTER, date.getSeason());
    date = new EresseaDate(634, -6, 2, 3);
    date.setEpoch(2);
    Assert.assertEquals(Date.SPRING, date.getSeason());
    date = new EresseaDate(639, -6, 2, 3);
    date.setEpoch(2);
    Assert.assertEquals(Date.SPRING, date.getSeason());
    date = new EresseaDate(640, -6, 2, 3);
    date.setEpoch(2);
    Assert.assertEquals(Date.SUMMER, date.getSeason());
    date = new EresseaDate(645, -6, 2, 3);
    date.setEpoch(2);
    Assert.assertEquals(Date.SUMMER, date.getSeason());
    date = new EresseaDate(646, -6, 2, 3);
    date.setEpoch(2);
    Assert.assertEquals(Date.AUTUMN, date.getSeason());

    // third age
    date = new EresseaDate(1, 1, 1, 1);
    date.setEpoch(3);
    Assert.assertEquals(Date.SUMMER, date.getSeason());
    date = new EresseaDate(3, 1, 1, 1);
    date.setEpoch(3);
    Assert.assertEquals(Date.SUMMER, date.getSeason());
    date = new EresseaDate(4, 1, 1, 1);
    date.setEpoch(3);
    Assert.assertEquals(Date.AUTUMN, date.getSeason());
    date = new EresseaDate(9, 1, 1, 1);
    date.setEpoch(3);
    Assert.assertEquals(Date.AUTUMN, date.getSeason());
    date = new EresseaDate(10, 1, 1, 1);
    date.setEpoch(3);
    Assert.assertEquals(Date.WINTER, date.getSeason());
    date = new EresseaDate(18, 1, 1, 1);
    date.setEpoch(3);
    Assert.assertEquals(Date.WINTER, date.getSeason());
    date = new EresseaDate(19, 1, 1, 1);
    date.setEpoch(3);
    Assert.assertEquals(Date.SPRING, date.getSeason());
    date = new EresseaDate(24, 1, 1, 1);
    date.setEpoch(3);
    Assert.assertEquals(Date.SPRING, date.getSeason());
    date = new EresseaDate(25, 1, 1, 1);
    date.setEpoch(3);
    Assert.assertEquals(Date.SUMMER, date.getSeason());
    date = new EresseaDate(30, 1, 1, 1);
    date.setEpoch(3);
    Assert.assertEquals(Date.SUMMER, date.getSeason());
    date = new EresseaDate(31, 1, 1, 1);
    date.setEpoch(3);
    Assert.assertEquals(Date.AUTUMN, date.getSeason());

  }

  /**
   * Test method for {@link magellan.library.rules.EresseaDate#toString(int)}.
   */
  @Test
  public final void testToStringInt() {
    EresseaDate date = new EresseaDate(1);
    date.setEpoch(3);
    List<String> dates = new ArrayList<String>(29);
    for (int d = 1; d < 29; ++d) {
      date.setDate(d);
      dates.add(date.toString(Date.TYPE_SHORT));
    }
    dates.clear();
    for (int d = 1; d < 29; ++d) {
      date.setDate(d);
      dates.add(date.toString(Date.TYPE_LONG));
    }
    dates.clear();
    for (int d = 1; d < 29; ++d) {
      date.setDate(d);
      dates.add(date.toString(Date.TYPE_PHRASE));
    }
    dates.clear();
    for (int d = 1; d < 29; ++d) {
      date.setDate(d);
      dates.add(date.toString(Date.TYPE_PHRASE_AND_SEASON));
    }
    dates.clear();
  }

  /**
   * Test method for {@link magellan.library.rules.EresseaDate#getEpoch()}.
   */
  @Test
  public final void testGetEpoch() {
    EresseaDate date = new EresseaDate(1);
    Assert.assertEquals(1, date.getEpoch());
    date.setEpoch(1);
    Assert.assertEquals(1, date.getEpoch());
    date.setEpoch(2);
    Assert.assertEquals(2, date.getEpoch());
  }

  /**
   * Test method for {@link magellan.library.rules.EresseaDate#copy()}.
   */
  @Test
  public final void testCopy() {
    EresseaDate date = new EresseaDate(1);
    EresseaDate newDate = date.copy();
    Assert.assertNotSame(date, newDate);
    Assert.assertEquals(newDate.getDate(), date.getDate());
    Assert.assertEquals(newDate.getEpoch(), date.getEpoch());
    Assert.assertEquals(newDate.getSeason(), date.getSeason());

    date = new EresseaDate(42);
    date.setEpoch(2);
    newDate = date.copy();
    Assert.assertNotSame(date, newDate);
    Assert.assertEquals(newDate.getDate(), date.getDate());
    Assert.assertEquals(newDate.getEpoch(), date.getEpoch());
    Assert.assertEquals(newDate.getSeason(), date.getSeason());

    date = new EresseaDate(242);
    date.setEpoch(3);
    newDate = date.copy();
    Assert.assertNotSame(date, newDate);
    Assert.assertEquals(newDate.getDate(), date.getDate());
    Assert.assertEquals(newDate.getEpoch(), date.getEpoch());
    Assert.assertEquals(newDate.getSeason(), date.getSeason());
  }

  /**
   * Test method for {@link magellan.library.rules.Date#getDate()}.
   */
  @Test
  public final void testGetDate() {
    EresseaDate date = new EresseaDate(242);
    Assert.assertEquals(242, date.getDate());
    date.setDate(42);
    Assert.assertEquals(42, date.getDate());
  }

}
