// class magellan.plugin.extendedcommands.ExtendedCommandsTest
// created on Jun 30, 2020
//
// Copyright 2003-2020 by magellan project team
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
package magellan.plugin.extendedcommands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import magellan.library.GameData;
import magellan.library.utils.Locales;
import magellan.library.utils.logging.AbstractLogListener;
import magellan.library.utils.logging.LogListener;
import magellan.library.utils.logging.Logger;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

public class ExtendedCommandsTest extends MagellanTestWithResources {

  // private static Client client;
  private static GameDataBuilder builder;

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    setLocale(DE_LOCALE);
    initResources();

    Logger.setLevel(Logger.WARN);

    settings.setProperty("locales.orders", DE_LOCALE.getLanguage());
    Locales.setGUILocale(DE_LOCALE);
    Locales.setOrderLocale(DE_LOCALE);
  }

  private static GameData data;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    data = new GameDataBuilder().createSimpleGameData(350);
  }

  private String runScript(String name) throws IOException {
    ExtendedCommands ec = new ExtendedCommands((String) null);
    StringBuilder logBuffer = new StringBuilder();

    AbstractLogListener abstractlistener = new AbstractLogListener();
    Logger.addLogListener(new LogListener() {

      public void log(int aLevel, Object aObj, Throwable aThrowable) {
        String str = abstractlistener.getMessage(aLevel, aObj, aThrowable);
        logBuffer.append(str);
      }
    });

    ec.setUseThread(false);
    ec.execute(getScript(name), data, null, null);

    return logBuffer.toString();
  }

  private String getScript(String name) throws IOException {
    return Files.readString(Path.of("src-test/magellan/plugin/extendedcommands/script" + name + ".txt"),
        StandardCharsets.ISO_8859_1);
  }

  private void assertMatches(String regexp, String input) {
    assertTrue("Pattern \"" + regexp + "\" does not match\n" + input,
        Pattern.compile(regexp, Pattern.DOTALL).matcher(input).matches());
  }

  @Test
  public void testHappyScript() throws IOException {
    String log = runScript("Happy");
    assertEquals("", log);
  }

  @Test
  public void testHappy2Script() throws IOException {
    String log = runScript("Happy2");
    assertEquals("", log);
  }

  @Test
  public void testIncomplete() throws IOException {
    String log = runScript("Incomplete");
    assertMatches(".*source is incomplete.*", log);
  }

  @Test
  public void testRejected() throws IOException {
    String log = runScript("Rejected");
    assertMatches(".*variable x might not have been initialized.*at 3:4:.*", log);
  }

  @Test
  public void testMultipleWarnings() throws IOException {
    String log = runScript("MultipleWarnings");
    assertEquals("", log);
  }

  @Test
  public void testLongWarnings() throws IOException {
    String log = runScript("LongWarnings");
    assertMatches(".*variable y might not have been initialized.*at 64:5:.*x=y;.*", log);
    assertMatches(".*\\.\\.\\..*", log);
  }

  @Test
  public void testImport() throws IOException {
    String log = runScript("Import");
    assertMatches(".*import statement after first code statement.*", log);
  }

  @Test
  public void testAbort() throws IOException {
    String log = runScript("Abort");
    assertMatches(".*illegal.*at 1:9:9.*void a\\( \\{.*", log);
  }

  @Test
  public void testUnresolved() throws IOException {
    String log = runScript("Unresolved");
    assertMatches(".*unresolved references.*ABC\\(\\).*void ABC\\(\\).*", log);
  }

}
