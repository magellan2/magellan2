// class magellan.library.gamebinding.AbstractOrderParserTest
// created on Apr 19, 2013
//
// Copyright 2003-2013 by magellan project team
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
package magellan.library.gamebinding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import magellan.client.completion.AutoCompletion;
import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.completion.Completion;
import magellan.library.utils.logging.Logger;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

public class OrderCompleterTest extends MagellanTestWithResources {

  protected GameData data;
  protected AutoCompletion completion;
  protected GameDataBuilder builder;
  private EresseaOrderParser parser;
  private EresseaOrderCompleter completer;
  private Unit unit;

  public OrderCompleterTest() {
    super();
  }

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    setLocale(DE_LOCALE);
    Logger.setLevel(Logger.WARN);
    MagellanTestWithResources.initResources();
  }

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    builder = new GameDataBuilder();
    builder.setGameName("E3");
    data = builder.createSimpleGameData();
    unit = data.getUnits().iterator().next();

    completion = new AutoCompletion(context.getProperties(), context.getEventDispatcher());
    completer = new EresseaOrderCompleter(data, completion);
    parser = new EresseaOrderParser(data, completer);
    completer.setParser(getParser());
    getParser().setCompleter(completer);

    completer.unit = unit;
    completer.region = unit.getRegion();
  }

  /**
   *
   */
  @Test
  public void testEmptyReader() {
    List<Completion> completions = completer.getCompletions(unit, "");
    assertEquals(41, completions.size());
    assertTrue(completions.contains(new Completion("ARBEITE")));
    assertTrue(completions.contains(new Completion("ATTACKIERE", " ")));
    assertFalse(completions.contains(new Completion("UNTERHALTE", " ")));

    completions = completer.getCompletions(unit, "");
    assertEquals(41, completions.size());
    assertTrue(completions.contains(new Completion("ARBEITE")));
    assertTrue(completions.contains(new Completion("ATTACKIERE", " ")));
    assertFalse(completions.contains(new Completion("UNTERHALTE", " ")));

    completions = completer.getCompletions(unit, "AR");
    assertEquals(1, completions.size());
    assertTrue(completions.contains(new Completion("ARBEITE")));

    completions = completer.getCompletions(unit, "@");
    assertEquals(41, completions.size());

    completions = completer.getCompletions(unit, "!");
    assertEquals(41, completions.size());
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.UnterhalteReader}.
   */
  @Test
  public void testUnterhalteReader() {

  }

  @Test
  public void testPasswortCompleter() {
    checkCompletions("PASSWORT ", "\"<Passwort>", "\"");
    checkCompletions("PASSWORT \"",
        "\"<Passwort>", "\"",
        "\"", "\"");
    checkCompletions("PASSWORT \"a",
        "\"<Passwort>\"", "\"\"",
        "\"a\"", "\"a\"");
    checkCompletions("PASSWORT \"a\"",
        "\"<Passwort>\"", "\"\"",
        "\"a\"", "\"a\"");
    checkCompletions("PASSWORT \"a\" ");
  }

  @Test
  public void testStirbCompleter() {
    checkCompletions("STIRB ", "\"<Passwort>", "\"");
    checkCompletions("STIRB \"", "\"<Passwort>", "\"", "\"", "\"");
    checkCompletions("STIRB \"a", "\"<Passwort>\"", "\"\"", "\"a\"", "\"a\"");
    checkCompletions("STIRB \"a\"",
        "\"<Passwort>\"", "\"\"",
        "\"a\"", "\"a\"",
        "PARTEI", "PARTEI");
    checkCompletions("STIRB \"a\" ", "PARTEI");
    checkCompletions("STIRB \"a\" PARTEI ",
        "Faction_867718 (iLja)", "iLja; Faction_867718",
        "iLja (Faction_867718)", "iLja; Faction_867718");
    checkCompletions("STIRB \"a\" PARTEI x ");
  }

  private List<Completion> checkCompletions(String string, String... expected) {
    if (expected.length == 1) {
      expected = new String[] { expected[0], expected[0] };
    }
    getParser().parse(string, getLocale());
    List<Completion> completions = completer.getCompletions();
    assertTrue(expected.length % 2 == 0);
    for (int i = 0; i < Math.min(completions.size(), expected.length / 2); ++i) {
      assertEquals("name " + i, expected[2 * i], completions.get(i).getName());
      assertEquals("value " + i, expected[2 * i + 1], completions.get(i).getValue());
    }
    assertEquals(expected.length / 2, completions.size());
    return completions;
  }

  public EresseaOrderParser getParser() {
    return parser;
  }

  public void setParser(EresseaOrderParser parser) {
    this.parser = parser;
  }

}