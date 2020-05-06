// class magellan.test.AbstractTest
// created on Nov 16, 2010
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
package magellan.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.File;
import java.util.Locale;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import magellan.client.MagellanContext;
import magellan.client.event.EventDispatcher;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.gamebinding.EresseaRelationFactory;
import magellan.library.utils.Locales;
import magellan.library.utils.Resources;
import magellan.library.utils.SelfCleaningProperties;
import magellan.library.utils.logging.Logger;

/**
 * A template for magellan test classes that need to load resources.
 *
 * @author stm
 * @version 1.0, Nov 16, 2010
 */
public abstract class MagellanTestWithResources {
  protected static final Locale DE_LOCALE = Locale.GERMANY;
  protected static final Locale EN_LOCALE = Locale.US;

  protected static Properties settings;
  protected static MagellanContext context;
  protected static SelfCleaningProperties completionSettings;
  private static File resourceDir = new File(".");
  private static Locale locale;
  protected static Locale defaultLocale = DE_LOCALE;

  protected static void setLocale(Locale alocale) {
    locale = alocale;
  }

  public static Locale getLocale() {
    return locale;
  }

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    defaultLocale = DE_LOCALE;
    setDefaultLocale();

    Logger.setLevel(Logger.WARN);
    initResources();
  }

  protected static void setDefaultLocale() {
    setLocale(defaultLocale);
  }

  /**
   *
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    if (context != null) {
      context.getEventDispatcher().quit();
    }
    defaultLocale = DE_LOCALE;
  }

  protected static void initResources() {
    Locales.setOrderLocale(locale);
    Locales.setGUILocale(locale);
    settings = new Properties(); // Client.loadSettings(PARSER_SETTINGS_DIRECTORY,
    // PARSER_SETTINGS_FILE);
    settings.setProperty("locales.orders", locale.getLanguage());
    Resources.getInstance().initialize(resourceDir, "");
    // System.out.println(new File(".").getAbsolutePath());
    context = new MagellanContext(null);
    context.setProperties(settings);
    context.setEventDispatcher(new EventDispatcher());
    // context.setCompletionProperties(completionSettings = new SelfCleaningProperties());
    Logger.setLevel(Logger.ERROR);
    context.init();

    assertEquals(true, true);
    assertSame(true, true);
  }

  protected static void setResourceDir(String dir) {
    resourceDir = new File(dir);
  }

  protected void refreshOrders(EresseaRelationFactory processor, GameData gd) {
    processor.stopUpdating();
    for (Region r : gd.getRegions()) {
      processor.processRegionNow(r);
    }
  }

}
