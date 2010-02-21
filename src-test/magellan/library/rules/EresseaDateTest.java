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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import junit.framework.Assert;
import magellan.client.MagellanContext;
import magellan.client.event.EventDispatcher;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for class EresseaDate
 * 
 * @author stm
 * @version 1.0, Feb 21, 2010
 */
public class EresseaDateTest {

  private static MagellanContext context;
  private static Properties settings;

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    settings = new Properties(); // Client.loadSettings(PARSER_SETTINGS_DIRECTORY,
    // PARSER_SETTINGS_FILE);
    Resources.getInstance().initialize(new File("."), "");
    Locale.setDefault(new Locale("de"));
    System.out.println(new File(".").getAbsolutePath());
    context = new MagellanContext(null);
    context.setProperties(settings);
    context.setEventDispatcher(new EventDispatcher());
    Logger.setLevel(Logger.ERROR);
    context.init();
  }

  /**
   * Test method for {@link EresseaDate#getWeekFromStart()}.
   */
  @Test
  public void testGet() {
    EresseaDate date = new EresseaDate(1);
    date.setEpoch(1);
    Assert.assertEquals(1, date.getWeekFromStart());
    date = new EresseaDate(185);
    date.setEpoch(2);
    Assert.assertEquals(1, date.getWeekFromStart());
    date = new EresseaDate(1);
    date.setEpoch(3);
    Assert.assertEquals(0, date.getWeekFromStart());

    // second age
    date = new EresseaDate(185, 6, 7, 0);
    Assert.assertEquals(1, date.getWeekFromStart());
    date = new EresseaDate(185, 6, 7, 0);
    date.setEpoch(2);
    Assert.assertEquals(1, date.getWeekFromStart());
    date = new EresseaDate(185, 6, 7, 0);
    date.setEpoch(3);
    Assert.assertEquals(1, date.getWeekFromStart());

    // third age
    date = new EresseaDate(1, 0, 0, 0);
    Assert.assertEquals(0, date.getWeekFromStart());

    date = new EresseaDate(616, 6, 7, 0);
    date.setEpoch(2);
    Assert.assertEquals(Date.SUMMER, date.getSeason());
    date = new EresseaDate(619, 6, 7, 0);
    date.setEpoch(2);
    Assert.assertEquals(Date.AUTUMN, date.getSeason());
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
