// class magellan.library.gamebinding.EresseaOrderParserTest
// created on Jun 20, 2009
//
// Copyright 2003-2009 by magellan project team
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
package magellan.library.gamebinding;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Properties;

import magellan.client.MagellanContext;
import magellan.client.completion.AutoCompletion;
import magellan.client.event.EventDispatcher;
import magellan.library.GameData;
import magellan.library.gamebinding.EresseaOrderParser.AttackReader;
import magellan.library.gamebinding.EresseaOrderParser.OrderHandler;
import magellan.library.gamebinding.EresseaOrderParser.WorkReader;
import magellan.library.utils.OrderToken;
import magellan.library.utils.Resources;
import magellan.library.utils.SelfCleaningProperties;
import magellan.library.utils.logging.Logger;
import magellan.test.GameDataBuilder;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * TODO This class must be commented
 * 
 * @author ...
 * @version 1.0, Jun 20, 2009
 */
public class EresseaOrderParserTest {

  private static MagellanContext context;
  private static Properties completionSettings;
  private static Properties settings;

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    EresseaOrderParserTest.settings = new Properties(); // Client.loadSettings(PARSER_SETTINGS_DIRECTORY,
    // PARSER_SETTINGS_FILE);
    Resources.getInstance().initialize(new File("."), "");
    System.out.println(new File(".").getAbsolutePath());
    EresseaOrderParserTest.context = new MagellanContext(null);
    EresseaOrderParserTest.context.setProperties(EresseaOrderParserTest.settings);
    EresseaOrderParserTest.context.setEventDispatcher(new EventDispatcher());
    EresseaOrderParserTest.context
        .setCompletionProperties(EresseaOrderParserTest.completionSettings =
            new SelfCleaningProperties());
    Logger.setLevel(Logger.ERROR);
    EresseaOrderParserTest.context.init();
  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  private GameData data;
  private EresseaOrderParser parser;
  private EresseaOrderParser parserCompleter;
  private AutoCompletion completion;
  private EresseaOrderCompleter completer;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    data = new GameDataBuilder().createSimpleGameData();

    parser = new EresseaOrderParser(data);
    completion = new AutoCompletion(EresseaOrderParserTest.context);
    completer = new EresseaOrderCompleter(data, completion);
    parserCompleter = new EresseaOrderParser(data, completer);
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#EresseaOrderParser(magellan.library.GameData, magellan.library.gamebinding.EresseaOrderCompleter)}
   * .
   */
  @Test
  public void testEresseaOrderParserGameDataEresseaOrderCompleter() {
    EresseaOrderParser localParser = new EresseaOrderParser(data, completer);
    Assert.assertTrue(localParser.getData() == data);
    Assert.assertTrue(localParser.getCompleter() == completer);
    Assert.assertTrue(localParser.getCommands().size() == 60);
    Assert.assertTrue(localParser.getHandlers().size() == 60);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser#initCommands()}.
   */
  @Test
  public void testInitCommands() {
    Assert.assertTrue(parser.getCommands().size() == 60);
    Assert.assertTrue(parser.getHandlers().size() == 60);
    Assert.assertTrue(parser.getCommands().contains("arbeiten"));
    Assert.assertTrue(parser.getCommands().contains("zerstören"));
    Assert.assertTrue(parser.getCommands().contains("sabotieren"));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#addCommand(java.lang.String, magellan.library.gamebinding.EresseaOrderParser.OrderHandler)}
   * .
   */
  @Test
  public void testAddCommand() {
    OrderHandler fooHandler = parser.new OrderHandler() {
      @Override
      public boolean read(OrderToken token) {
        return false;
      }
    };
    Assert.assertFalse(parser.getCommands().contains("foo"));
    Assert.assertFalse(parser.getHandlers().contains(fooHandler));
    Assert.assertTrue(parser.getHandlers(new OrderToken("foo")).size() == 0);
    parser.addCommand("foo", fooHandler);
    Assert.assertTrue(parser.getCommands().contains("foo"));
    Assert.assertTrue(parser.getHandlers().contains(fooHandler));

    Assert.assertTrue(parser.getHandlers(new OrderToken("foo")).contains(fooHandler));
    Assert.assertTrue(parser.getHandlers(new OrderToken("foo")).size() == 1);
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#removeCommand(java.lang.String)}.
   */
  @Test
  public void testRemoveCommand() {
    OrderHandler fooHandler = parser.new OrderHandler() {
      @Override
      public boolean read(OrderToken token) {
        return false;
      }
    };
    parser.addCommand("foo", fooHandler);
    Assert.assertTrue(parser.getCommands().contains("foo"));
    Assert.assertTrue(parser.getHandlers().contains(fooHandler));

    Assert.assertTrue(parser.getHandlers(new OrderToken("foo")).contains(fooHandler));
    Assert.assertTrue(parser.getHandlers(new OrderToken("foo")).size() == 1);
    parser.removeCommand("foo");
    Assert.assertFalse(parser.getCommands().contains("foo"));
    Assert.assertFalse(parser.getHandlers().contains(fooHandler));
    Assert.assertTrue(parser.getHandlers(new OrderToken("foo")).size() == 0);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser#getNextToken()}.
   */
  @Test
  public void testGetNextToken() {
    Assert.assertTrue(parser.getLastToken() == null);
    Assert.assertFalse(parser.hasNextToken());
    Assert.assertEquals(parser.getTokenIndex(), 0);

    parser.read(new StringReader("123 abc"));

    Assert.assertTrue(parser.hasNextToken());
    Assert.assertTrue(equals(parser.getLastToken(), new OrderToken("123", 0, 3,
        OrderToken.TT_UNDEF, true)));
    Assert.assertTrue(parser.hasNextToken());
    Assert.assertEquals(parser.getTokenIndex(), 1);

    OrderToken token = parser.getNextToken();

    Assert.assertTrue(token.getText().equals("abc"));
    Assert.assertTrue(parser.hasNextToken());
    Assert.assertEquals(parser.getLastToken(), token);
    Assert.assertEquals(parser.getTokenIndex(), 2);

    Assert.assertTrue(equals(parser.getNextToken(), new OrderToken(OrderToken.TT_EOC)));
    Assert.assertFalse(parser.hasNextToken());
    Assert.assertEquals(parser.getTokenIndex(), 3);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser#getNextToken()}.
   */
  @Test(expected = NullPointerException.class)
  public void testGetNextTokenNull() {
    Assert.assertTrue(parser.getNextToken() != null);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser#read(java.io.Reader)}.
   */
  @Test
  public void testRead() {
    checkOrder(""); // FIXME ???!!!
    checkOrder("AR");
    checkOrder("A", false);
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#readOrder(magellan.library.utils.OrderToken)}
   * .
   */
  @Test
  public void testReadOrder() {
    Assert.fail("Not yet implemented");
  }

  private void checkOrder(String string) {
    checkOrder(string, true);
  }

  private void checkOrder(String string, boolean result) {
    boolean retVal = parser.read(new StringReader(string));
    Assert.assertEquals("checking " + string, result, retVal);
  }

  /**
   * Testmethod for ARBEITE
   */
  @Test
  public void testArbeiteReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_WORK));
    checkOrder("ARBEITE");
    checkOrder("ARBEITEN");
    checkOrder("arbeiten");
    checkOrder("AR");
    checkOrder("ARBEITE ;");
    checkOrder("arbeitene", false);
    checkOrder("ARBEISEN", false);
    checkOrder("ARBEITE 1", false);
    checkOrder(" ARBEITE ARBEITE", false);
    checkOrder(" ARBEITE");
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.AtReader}.
   */
  @Test
  public void testAtReader() {
    checkOrder("@ARBEITE");
    checkOrder(" @ARBEITE");
    checkOrder("@", false);
    checkOrder("@@ARBEITE", false);
    checkOrder("@@", false);
    checkOrder("@ARBEITE 1", false);
    checkOrder("@// ", false); // dodgy...
    checkOrder("@; ", false);
    checkOrder(";@", true);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.AttackReader}.
   */
  @Test
  public void testAttackReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_ATTACK) + " 123");
    checkOrder("ATTACKIERE 123");
    checkOrder("ATTACKIERE xyz");
    checkOrder("ATTACKIERE TEMP xyz"); // TODO is this really legal?
    checkOrder("ATTACKIERE xyz; abc");
    checkOrder("ATTACKIERE abcde", false);
    checkOrder("ATTACKIERE", false);
    checkOrder("ATTACKIERE 123 123", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.BannerReader}.
   */
  @Test
  public void testBannerReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_BANNER) + "\"\"");
    checkOrder("BANNER  \"abc\"");
    checkOrder("BANNER  \"abc\"; bla");
    checkOrder("BANNER  'abc'");
    checkOrder("BANNER  abc", false);
    checkOrder("BANNER  \"abc\" \"abc\"", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.BeansprucheReader}.
   */
  @Test
  public void testBeansprucheReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_CLAIM) + " Sonnensegel");
    checkOrder("BEANSPRUCHE 2 Sonnensegel");
    checkOrder("BEANSPRUCHE \"Schönes Geschenk\"");
    checkOrder("BEANSPRUCHE Schönes~Geschenk");
    checkOrder("BEANSPRUCHE", false);
    checkOrder("BEANSPRUCHE 1", false); // ??
    checkOrder("BEANSPRUCHE Schönes Geschenk", false); // ??
    checkOrder("BEANSPRUCHE 1 2 Sonnensegel", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.BefoerderungReader}.
   */
  @Test
  public void testBefoerderungReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_PROMOTION));
    checkOrder("BEFÖRDERUNG");
    checkOrder("BEFÖRDER");
    checkOrder("BEFÖRDERUNG ;");
    checkOrder("BEFÖRDERE", false);
    checkOrder("BEFÖRDERUNG 1", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.BeklaueReader}.
   */
  @Test
  public void testBeklaueReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_STEAL) + " 123");
    checkOrder("BEKLAUE 1");
    checkOrder("BEKLAUE abc;");
    checkOrder("BEKLAUE TEMP abc;"); // TODO is this legal?
    checkOrder("BEKLAUE ;abc", false);
    checkOrder("BEKLAUE \"abc\"", false);
    checkOrder("BEKLAUE", false);
    checkOrder("BEKLAUE anc foo", false);
    checkOrder("BEKLAUE 20 foo", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.BelagereReader}.
   */
  @Test
  public void testBelagereReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_SIEGE) + " abc");
    checkOrder("BELAGERE abc");
    checkOrder("BELAGERUNG abc", false);
    checkOrder("BELAGERE TEMP abc", false);
    checkOrder("BELAGERE abc 123", false);
    checkOrder("BELAGERE 2 abc", false);
    checkOrder("BELAGERE \"abc\"", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.BenenneReader}.
   */
  @Test
  public void testBenenneReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_NAME) + " EINHEIT \"Foo\"");
    for (String thing : new String[] { "EINHEIT", "PARTEI", "BURG", "Gebäude", "Sägewerk",
        "SCHIFF", "REGION" }) {
      checkOrder("BENENNEN " + thing + " \"Foo\"");
      checkOrder("BENENNE " + thing + " \"Foo\"; comment");
      checkOrder("BENENNE " + thing + " \"\"", false);
      checkOrder("BENENNE " + thing + " abc", false);
      checkOrder("BENENNE " + thing + " 123 \"abc\"", false);
      checkOrder("BENENNE " + thing + " \"abc\" 123", false);
    }
    for (String thing : new String[] { "EINHEIT", "PARTEI", "BURG", "Gebäude", "Sägewerk", "SCHIFF" }) {
      checkOrder("BENENNEN FREMDE " + thing + " 123 \"Foo\"");
      checkOrder("BENENNE FREMDE " + thing + " abc \"Foo\"; comment");
      checkOrder("BENENNE FREMDE " + thing + " TEMP xyz \"Foo\""); // TODO is this really allowed?
      checkOrder("BENENNE FREMDE " + thing + " xyz \"\"", false);
      checkOrder("BENENNE FREMDE " + thing + " abc abc", false);
      checkOrder("BENENNE FREMDE " + thing + " 123 123 \"abc\"", false);
      checkOrder("BENENNE FREMDE " + thing + " 123 \"abc\" 123", false);
    }

    checkOrder("BENENNE abc \"abc\"", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.BenutzeReader}.
   */
  @Test
  public void testBenutzeReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_USE) + " Siebenmeilentee");
    checkOrder("BENUTZEN Wasser~des~Lebens");
    checkOrder("BENUTZEN \"Wasser des Lebens\"");
    checkOrder("BENUTZEN 22 Wasser~des~Lebens");
    checkOrder("BENUTZEN 22 \"Wasser des Lebens\"");
    checkOrder("BENUTZEN 22 \"Wasser des Lebens\"; \"123\"");
    checkOrder("BENUTZEN  11 22 Wasser~des~Lebens", false);
    checkOrder("BENUTZEN  22 Siebenmeilentee 3", false);
    checkOrder("BENUTZEN  22 Siebenmeilentee Heiltrank", false);
    checkOrder("BENUTZEN  BENUTZEN", true);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.BeschreibeReader}.
   */
  @Test
  public void testBeschreibeReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_DESCRIBE) + " EINHEIT \"Foo\"");
    for (String thing : new String[] { "EINHEIT", "PRIVAT", "BURG", "Sägewerk", "SCHIFF", "REGION" }) {
      checkOrder("BESCHREIBEN " + thing + " \"Foo\"");
      checkOrder("BESCHREIBE " + thing + " \"Foo\"; comment");
      checkOrder("BESCHREIBE " + thing + " \"\"", true);
      checkOrder("BESCHREIBE " + thing + " abc", false);
      checkOrder("BESCHREIBE " + thing + " 123 \"abc\"", false);
      checkOrder("BESCHREIBE " + thing + " \"abc\" 123", false);
    }
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.BetreteReader}.
   */
  @Test
  public void testBetreteReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_ENTER) + " "
        + Resources.getOrderTranslation(EresseaConstants.O_SHIP) + " foo");
    checkOrder("BetreteN BURG 1a2");
    checkOrder("BetreteN BURG 1a2; ");
    checkOrder("BetreteN BURG TEMP 123", false);
    checkOrder("BetreteN BURG \"abc\"", false);
    checkOrder("BetreteN 1 BURG abc", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.BewacheReader}.
   */
  @Test
  public void testBewacheReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_GUARD));
    checkOrder("BEWACHE");
    checkOrder("BEWACHE ; a");
    checkOrder("BEWACHE 2", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.BotschaftReader}.
   */
  @Test
  public void testBotschaftReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_MESSAGE) + " "
        + Resources.get(EresseaConstants.O_REGION) + " \"hallo\"");
    for (String thing : new String[] { "EINHEIT", "PARTEI", "BURG", "Sägewerk", "SCHIFF", "REGION" }) {
      String nr = " abc ";
      if (thing.equals("REGION")) {
        nr = "";
      }
      checkOrder("BOTSCHAFT " + thing + nr + " \"Foo\"");
      checkOrder("BOTSCHAFT " + thing + nr + " \"Foo\"; comment");
      checkOrder("BOTSCHAFT " + thing + nr + " \"\"", false);
      checkOrder("BOTSCHAFT " + thing + nr + " abc", false);
      checkOrder("BOTSCHAFT " + thing + nr + " 123 \"abc\"", false);
      checkOrder("BOTSCHAFT " + thing + nr + " \"abc\" 123", false);
    }
    checkOrder("BOTSCHAFT EINHEIT TEMP 123 \"Bar\"", true); // TODO is this really allowed?
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.DefaultReader}.
   */
  @Test
  public void testDefaultReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_DEFAULT) + " "
        + Resources.getOrderTranslation(EresseaConstants.O_WORK));
    checkOrder("DEFAULT \"ARBEITEN\"");
    checkOrder("DEFAULT 'ARBEITEN'");
    checkOrder("DEFAULT 'LERNEN Ausdauer'");
    checkOrder("DEFAULT 'LERNEN Alchemie'");
    checkOrder("DEFAULT 'LERNEN Alchemie 200'");
    checkOrder("DEFAULT 'BANNER \"abc\"'");
    checkOrder("DEFAULT 'BANNER \"abc def\"'");
    checkOrder("DEFAULT 'BANNER 'abc def''", false);
    checkOrder("DEFAULT 'LERNEN'", false);
    checkOrder("DEFAULT LERNEN Ausdauer", false);
    // TODO more checks
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.EmailReader}.
   */
  @Test
  public void testEmailReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_EMAIL) + " \"a@b.com\"");
    checkOrder("EMAIL \'a@b.com\'");
    checkOrder("EMAIL \"123@456.com\"");
    checkOrder("EMAIL \"eressea-server@eressea.upb.de\"");
    checkOrder("Email \"enno@world\"");
    checkOrder("EMAIL \"abc\"", false);
    checkOrder("EMAIL \"www.eressea.de\"", false);
    checkOrder("EMAIL stm@example.com", false);
    checkOrder("EMAIL \"hallo@world.com", false);
    checkOrder("EMAIL ", false);
    checkOrder("EMAIL \"\"", false);
    checkOrder("EMAIL \"abc@def.ghi\" \"jkl@mno.pqr\"", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.EndeReader}.
   */
  @Test
  public void testEndeReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_END));
    checkOrder("ENDE 123", false);
    checkOrder("ENDE \"123\"", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.FahreReader}.
   */
  @Test
  public void testFahreReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_RIDE) + " abc");
    checkOrder("FAHREN 123");
    checkOrder("FAHREN TEMP 456");
    checkOrder("FAHREN abcde", false);
    checkOrder("FAHREN 123 456", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.FolgeReader}.
   */
  @Test
  public void testFolgeReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_FOLLOW) + " "
        + Resources.getOrderTranslation(EresseaConstants.O_UNIT) + " abc");
    checkOrder("FOLGEN SCHIFF 123");
    checkOrder("FOLGEN SCHIFF TEMP 123", false);
    checkOrder("FOLGEN EINHEIT 123 456", false);
    checkOrder("FOLGEN EINHEIT \"abc\"", false);
    checkOrder("FOLGEN EINHEIT", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.ForscheReader}.
   */
  @Test
  public void testForscheReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_RESEARCH) + " "
        + Resources.getOrderTranslation(EresseaConstants.O_HERBS));
    checkOrder("FORSCHE", false);
    checkOrder("FORSCHE KRÄUTER 123", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.GibReader}.
   */
  @Test
  public void testGibReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_SUPPLY) + " 123 "
        + Resources.getOrderTranslation(EresseaConstants.O_HERBS));
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_GIVE) + " 123 "
        + Resources.getOrderTranslation(EresseaConstants.O_HERBS));
    checkOrder("GIB KRÄUTER", false);
    checkOrder("GIB abc \"KRÄUTER\"", false);
    checkOrder("GIB 123 KRÄUTER 123", false);

    checkOrder("GIB 123 KOMMANDO");
    checkOrder("GIB TEMP 123 KOMMANDO");
    checkOrder("GIB 0 KOMMANDO", false);
    checkOrder("GIB KOMMANDO", false);
    checkOrder("GIB abc \"KOMMANDO\"", false);
    checkOrder("GIB 123 KOMMANDO 123", false);
    checkOrder("GIB 123 123 KOMMANDO", false);

    checkOrder("GIB 123 2 Silber");
    checkOrder("GIB 0 2 Silber");
    checkOrder("GIB 123 ALLES Silber");
    checkOrder("GIB 123 ALLES \"Silber\"");
    checkOrder("GIB TEMP 123 ALLES \"Silber\"");
    checkOrder("GIB 123 123 123 Silber", false);
    checkOrder("GIB 123 2 Silber 123", false);
    checkOrder("GIB 123 123 123 Silber", false);

    checkOrder("GIB 123 ALLES PERSONEN");
    checkOrder("GIB 0 ALLES PERSONEN");
    checkOrder("GIB 123 2 PERSONEN");
    checkOrder("GIB TEMP 123 2 PERSONEN");
    checkOrder("GIB 123 ALLES PERSONEN 2", false);
    checkOrder("GIB 123 123 ALLES PERSONEN", false);
    checkOrder("GIB 123 ALLES 123 PERSONEN", false);

    checkOrder("GIB 123 EINHEIT");
    checkOrder("GIB 0 EINHEIT", false);

    checkOrder("GIB 123 2 Holz");
    checkOrder("GIB TEMP 123 2 Holz");
    checkOrder("GIB 0 2 Holz");
    checkOrder("GIB 123 2 \"Holz\"");
    checkOrder("GIB 123 2 Würziger~Wagemut");
    checkOrder("GIB 123 ALLES Holz");
    checkOrder("GIB 123 2 Würziger Wagemut", false);

    checkOrder("GIB 123 2 Würziger~Wagemut");

    checkOrder("GIB 123 2 EINHEIT", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.GruppeReader}.
   */
  @Test
  public void testGruppeReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_GROUP));
    checkOrder("GRUPPE abc");
    checkOrder("GRUPPE \"Die wilden Kerle\"");
    checkOrder("GRUPPE Hallo~Welt");
    checkOrder("GRUPPE Hallo Welt", false);
    checkOrder("GRUPPE 123 123", false);
    checkOrder("GRUPPE TEMP 123", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.HelfeReader}.
   */
  @Test
  public void testHelfeReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_HELP) + " 123 "
        + Resources.getOrderTranslation(EresseaConstants.O_ALL));
    checkOrder("HELFE 123 ALLES NICHT");
    checkOrder("HELFEN 123 GIB");
    checkOrder("HELFEN 123 GIB NICHT");
    checkOrder("HELFE 123 KÄMPFE");
    checkOrder("HELFE 123 BEWACHE");
    checkOrder("HELFE 123 SILBER");
    checkOrder("HELFE 123 PARTEITARNUNG");
    checkOrder("HELFE 123 bla", false);
    checkOrder("HELFE abcde GIB", false);
    checkOrder("HELFE 123 GIB BLA", false);
    checkOrder("HELFE 123 456", false);
    checkOrder("HELFE TEMP 456", false);
    checkOrder("HELFE 123", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.KaempfeReader}.
   */
  @Test
  public void testKaempfeReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_COMBAT));
    checkOrder("KÄMPFE AGGRESSIV");
    checkOrder("KÄMPFE HINTEN");
    checkOrder("KÄMPFE DEFENSIV");
    checkOrder("KÄMPFE NICHT");
    checkOrder("KÄMPFE FLIEHE");
    checkOrder("KÄMPFE HELFE");
    checkOrder("KÄMPFE HELFE NICHT");
    checkOrder("KÄMPFE VORNE", false); // deprecated
    checkOrder("KÄMPFE AGGRESSIV NICHT", false);
    checkOrder("KÄMPFE VORNE HINTEN", false);
    checkOrder("KÄMPFE 123 HINTEN", false);
    checkOrder("KÄMPFE FLIEHE NICHT", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.KampfzauberReader}.
   */
  @Test
  public void testKampfzauberReader() {
    GameDataBuilder.addSpells(data);

    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_COMBATSPELL) + " Hagel");
    checkOrder("KAMPFZAUBER STUFE 2 Hagel");
    checkOrder("KAMPFZAUBER Hagel NICHT");
    checkOrder("KAMPFZAUBER STUFE 2 \"Großes Fest\"", false); // no combat spell
    checkOrder("KAMPFZAUBER STUFE Hagel", false);
    checkOrder("KAMPFZAUBER Magisches Geschoß", false);
    checkOrder("KAMPFZAUBER Hagel 123", false);
    checkOrder("KAMPFZAUBER STUFE x Hagel", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.KaufeReader}.
   */
  @Test
  public void testKaufeReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_BUY) + " 2 Balsam");
    checkOrder("KAUFE 2 Öl");
    checkOrder("KAUFE 2 Oel");
    checkOrder("KAUFE Weihrauch", false);
    checkOrder("KAUFE 2 Schnickschnak", false);
    checkOrder("KAUFE 2 Öl Weihrauch", false);
    checkOrder("KAUFE", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.KontaktiereReader}.
   */
  @Test
  public void testKontaktiereReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_CONTACT) + " 123");
    checkOrder("KONTAKTIERE a");
    checkOrder("KONTAKTIERE TEMP a");
    checkOrder("KONTAKTIERE abc def", false);
    checkOrder("KONTAKTIERE", false);
    checkOrder("KONTAKTIERE \"abc\"", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.LehreReader}.
   */
  @Test
  public void testLehreReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_TEACH) + " abc");
    checkOrder("LEHRE abc 123 456 TEMP zyx");
    checkOrder("LEHRE abc Hiebwaffen", false);
    checkOrder("LEHRE", false);
    checkOrder("LEHRE Hiebwaffen", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.LerneReader}.
   */
  @Test
  public void testLerneReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_LEARN) + " Ausdauer");
    checkOrder("LERNE Hiebwaffen");
    checkOrder("LERNE \"Hiebwaffen\"");
    checkOrder("LERNE Waffenloser~Kampf");
    checkOrder("LERNE", false);
    checkOrder("LERNE foo", false);
    checkOrder("LERNE Waffenloser Kampf", false);
    checkOrder("LERNE 123 456", false);
    checkOrder("LERNE Magie 1234#", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.LocaleReader}.
   */
  @Test
  public void testLocaleReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_LOCALE), false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.MacheReader}.
   */
  @Test
  public void testMacheReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_MAKE) + " TEMP 123");
    checkOrder("MACHE BURG");
    checkOrder("MACHE 2 BURG");
    checkOrder("MACHE BURG 123");
    checkOrder("MACHE 3 BURG abc");
    checkOrder("MACHE Sägewerk");
    checkOrder("MACHE 2 Sägewerk");
    checkOrder("MACHE Boot");
    checkOrder("MACHE Trireme");
    checkOrder("MACHE SCHIFF");
    checkOrder("MACHE SCHIFF 123");
    checkOrder("MACHE 2 SCHIFF 123");
    checkOrder("MACHE STRASSE no");
    checkOrder("MACHE 2 STRASSE no");
    checkOrder("MACHE STRASSE nordwesten");
    checkOrder("MACHE KRÄUTER");
    checkOrder("MACHE 2 KRÄUTER");
    checkOrder("MACHE Pferd");
    checkOrder("MACHE 2 Schwert");
    checkOrder("MACHE 1 Pferd");
    checkOrder("MACHE", false); // actually, this is correct, but dangerous
    checkOrder("MACHE BURG");
    checkOrder("MACHE 2 BURG");
    checkOrder("MACHE BURG 123");
    checkOrder("MACHE 3 BURG abc");
    checkOrder("MACHE Sägewerk");
    checkOrder("MACHE 2 Sägewerk");
    checkOrder("MACHE \"Sägewerk\"", false); // well...
    checkOrder("MACHE Boot");
    checkOrder("MACHE Trireme");
    checkOrder("MACHE SCHIFF");
    checkOrder("MACHE SCHIFF 123");
    checkOrder("MACHE 2 SCHIFF 123");
    checkOrder("MACHE STRASSE no");
    checkOrder("MACHE 2 STRASSE no");
    checkOrder("MACHE STRASSE nordwesten");
    checkOrder("MACHE KRÄUTER");
    checkOrder("MACHE 2 KRÄUTER");
    checkOrder("MACHE Pferd");
    checkOrder("MACHE \"Pferd\"");
    checkOrder("MACHE 2 Schwert");
    checkOrder("MACHE 2 \"Rostiger Zweihänder\"");
    checkOrder("MACHE 1 Pferd");
    checkOrder("MACHE", false); // actually, this is correct, but dangerous
    checkOrder("MACHE BURGG", false);
    checkOrder("MACHE a BURG", false);
    checkOrder("MACHE a BURG 123", false);
    checkOrder("MACHE Boot 123", false);
    checkOrder("MACHE 3 Trireme a", false);
    checkOrder("MACHE a Trireme", false);
    checkOrder("MACHE SCHIFF abc def", false);
    checkOrder("MACHE STRASSE s", false);
    checkOrder("MACHE 2 STRASSE", false);
    checkOrder("MACHE STRASSE", false);
    checkOrder("MACHE KRÄUTER abc", false);
    checkOrder("MACHE Pferd abc", false);
    checkOrder("MACHE a Pferd", false);
    checkOrder("MACHE a Hurz", false);
    checkOrder("MACHE a Rostiger Bihänder", false);
    checkOrder("MACHE 2 Wasser~des~Lebens"); // TODO
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.NachReader}.
   */
  @Test
  public void testNachReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_MOVE) + " westen");
    checkOrder("NACH o");
    checkOrder("NACH so");
    checkOrder("NACH sw");
    checkOrder("NACH w");
    checkOrder("NACH nw");
    checkOrder("NACH no");
    checkOrder("NACH osten");
    checkOrder("NACH südosten");
    checkOrder("NACH suedosten");
    checkOrder("NACH westen");
    checkOrder("NACH nordw");
    checkOrder("NACH nordo");
    checkOrder("NACH o so sw w nw no o o");
    checkOrder("NACH 1 o", false);
    checkOrder("NACH", false);
    checkOrder("NACH 1", false);
    checkOrder("NACH o PAUSE", false);
  }

  // new FinalKeywordReader());
  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.NeustartReader}.
   */
  @Test
  public void testNeustartReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_RESTART) + " Trolle \"passwort\"");
    checkOrder("NEUSTART Zwerge \"\"", false);
    checkOrder("NEUSTART Zwerge", false);
    checkOrder("NEUSTART", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.NummerReader}.
   */
  @Test
  public void testNummerReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_NUMBER) + " EINHEIT");
    checkOrder("NUMMER EINHEIT 123");
    checkOrder("NUMMER PARTEI 123");
    checkOrder("NUMMER SCHIFF 123");
    checkOrder("NUMMER BURG 123");
    checkOrder("NUMMER PARTEI");
    checkOrder("NUMMER", false);
    checkOrder("NUMMER EINHEIT 123 123", false);
    checkOrder("NUMMER EINHEIT abcdefg", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.OptionReader}.
   */
  @Test
  public void testOptionReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_OPTION) + " AUSWERTUNG");
    checkOrder("OPTION PUNKTE NICHT");
    checkOrder("OPTION PUNKTE NICHT MEHR", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.ParteiReader}.
   */
  @Test
  public void testParteiReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_FACTION), false); // TODO???
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.PasswortReader}.
   */
  @Test
  public void testPasswortReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_PASSWORD) + " \"squiggy\"");
    checkOrder("PASSWORT", true);
    checkOrder("PASSWORT 123", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.PflanzeReader}.
   */
  @Test
  public void testPflanzeReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_PLANT) + " "
        + Resources.getOrderTranslation(EresseaConstants.O_HERBS));
    checkOrder("PFLANZE BÄUME");
    checkOrder("PFLANZE MALLOrnSamen");
    checkOrder("PFLANZE SAMEN");
    checkOrder("PFLANZE 4 BÄUME");
    checkOrder("PFLANZE 5 MALLOrnSamen");
    checkOrder("PFLANZE 2 SAMEN");
    checkOrder("PFLANZE", false);
    checkOrder("PFLANZE 2", false);
    checkOrder("PFLANZE ", false);
    checkOrder("PFLANZE Silber", false);
    checkOrder("PFLANZE 3 SAMEN NICHT", false);
    checkOrder("PFLANZE 123 123 SAMEN", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.PiraterieReader}.
   */
  @Test
  public void testPiraterieReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_PIRACY));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.PraefixReader}.
   */
  @Test
  public void testPraefixReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_PREFIX));
    checkOrder("PRÄFIX Nebel");
    checkOrder("PRÄFIX Blubb"); // do not currently test for allowed prefixes
    checkOrder("PRÄFIX Bla blubb", false);
    checkOrder("PRÄFIX 2", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.RegionReader}.
   */
  @Test
  public void testRegionReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_REGION) + " 1,1"); // FIXME read
    // comma'd
    // coordinate
    checkOrder("REGION", false);
    checkOrder("REGION 123", false);
    checkOrder("REGION abc,def", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.RekrutiereReader}.
   */
  @Test
  public void testRekrutiereReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_RECRUIT) + " 1");
    checkOrder("REKRUTIERE 5");
    checkOrder("REKRUTIERE 0", true); // TODO should we return false here?
    checkOrder("REKRUTIERE", false);
    checkOrder("REKRUTIERE 1 2", false);
    checkOrder("REKRUTIERE 1 Zwerg", false); // for E2, this is an error
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.ReserviereReader}.
   */
  @Test
  public void testReserviereReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_RESERVE));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.RouteReader}.
   */
  @Test
  public void testRouteReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_ROUTE));
    checkOrder("ROUTE o");
    checkOrder("ROUTE so");
    checkOrder("ROUTE sw");
    checkOrder("ROUTE w");
    checkOrder("ROUTE nw");
    checkOrder("ROUTE no");
    checkOrder("ROUTE osten");
    checkOrder("ROUTE südosten");
    checkOrder("ROUTE suedosten");
    checkOrder("ROUTE westen");
    checkOrder("ROUTE nordw");
    checkOrder("ROUTE nordo");
    checkOrder("ROUTE o so sw w nw no o o");
    checkOrder("ROUTE o PAUSE");
    checkOrder("ROUTE o PAUSE w pause pause");
    checkOrder("ROUTE PAUSE w pause pause");
    checkOrder("ROUTE 1 o", false);
    checkOrder("ROUTE", false);
    checkOrder("ROUTE 1", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.SortiereReader}.
   */
  @Test
  public void testSortiereReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_SORT) + " westen");
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.SpioniereReader}.
   */
  @Test
  public void testSpioniereReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_SPY));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.StirbReader}.
   */
  @Test
  public void testStirbReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_QUIT));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.TarneReader}.
   */
  @Test
  public void testTarneReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_HIDE));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.TransportiereReader}.
   */
  @Test
  public void testTransportiereReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_CARRY));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.TreibeReader}.
   */
  @Test
  public void testTreibeReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_TAX));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.UnterhalteReader}.
   */
  @Test
  public void testUnterhalteReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_ENTERTAIN));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.UrsprungReader}.
   */
  @Test
  public void testUrsprungReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_ORIGIN));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.VergesseReader}.
   */
  @Test
  public void testVergesseReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_FORGET));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.VerkaufeReader}.
   */
  @Test
  public void testVerkaufeReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_SELL));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.FinalKeywordReader}.
   */
  @Test
  public void testFinalKeywordReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_LEAVE));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.ZaubereReader}.
   */
  @Test
  public void testZaubereReader() {
    GameDataBuilder.addSpells(data);

    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_CAST) + " \"Großes Fest\"");

    checkOrder("ZAUBERE STUFE 2 Schild abc");
    checkOrder("ZAUBERE STUFE 2 \"Großes Fest\"");
    checkOrder("ZAUBERE Schild NICHT", true); // TODO cave: NICHT is read as spell parameter
    checkOrder("ZAUBERE \"Großes Fest\" NICHT", false);
    checkOrder("ZAUBERE Hagel", false); // combat spell
    checkOrder("ZAUBERE STUFE Schild abc", false);
    checkOrder("ZAUBERE Magisches Geschoß", false);
    checkOrder("ZAUBERE \"Großes Fest\" 123", false);
    checkOrder("ZAUBERE STUFE x Schild 123", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.ZeigeReader}.
   */
  @Test
  public void testZeigeReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_SHOW));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.ZerstoereReader}.
   */
  @Test
  public void testZerstoereReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_DESTROY));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.ZuechteReader}.
   */
  @Test
  public void testZuechteReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_GROW));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.SabotiereReader}.
   */
  @Test
  public void testSabotiereReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_SABOTAGE));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#getHandlers(magellan.library.utils.OrderToken)}
   * .
   */
  @Test
  public void testGetHandlers() {
    ArrayList<OrderHandler> list = parser.getHandlers(new OrderToken("a"));
    Assert.assertTrue(list != null);
    if (list == null)
      return;
    Assert.assertTrue(list.size() == 2);
    Assert.assertTrue(list.get(0).getClass().equals(WorkReader.class));
    Assert.assertTrue(list.get(1).getClass().equals(AttackReader.class));
    list = parser.getHandlers(new OrderToken("arbei"));
    Assert.assertTrue(list != null);
    if (list == null)
      return;
    Assert.assertTrue(list.size() == 1);
    list = parser.getHandlers(new OrderToken("aga"));
    Assert.assertTrue(list != null);
    if (list == null)
      return;
    Assert.assertTrue(list.size() == 0);
  }

  @Test(expected = NullPointerException.class)
  public void testGetHandlersNull() {
    parser.getHandlers(null);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser#readDescription()}.
   */
  @Test
  public void testReadDescription() {
    parser.read(new StringReader("a \"abc\""));
    Assert.assertTrue(parser.readDescription());
    parser.read(new StringReader("\"abc\""));
    Assert.assertFalse(parser.readDescription());
    parser.read(new StringReader("a \"\""));
    Assert.assertTrue(parser.readDescription());
    parser.read(new StringReader("a \"\" a"));
    Assert.assertFalse(parser.readDescription());
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#readDescription(boolean)}.
   */
  @Test
  public void testReadDescriptionBoolean() {
    parser.read(new StringReader("a \"abc\""));
    Assert.assertTrue(parser.readDescription(true));
    parser.read(new StringReader("\"abc\""));
    Assert.assertFalse(parser.readDescription(true));
    parser.read(new StringReader("a \"\""));
    Assert.assertTrue(parser.readDescription(true));
    parser.read(new StringReader("a \"\""));
    Assert.assertFalse(parser.readDescription(false));
    parser.read(new StringReader("a \"\" a"));
    Assert.assertFalse(parser.readDescription(true));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#readDescription(magellan.library.utils.OrderToken)}
   * .
   */
  @Test
  public void testReadDescriptionOrderToken() {

  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#readDescription(magellan.library.utils.OrderToken, boolean)}
   * .
   */
  @Test
  public void testReadDescriptionOrderTokenBoolean() {
    parser.read(new StringReader("abc"));
    OrderToken token = parser.getLastToken();
    Assert.assertFalse(parser.readDescription(token, true));
    parser.read(new StringReader("abc"));
    token = parser.getLastToken();
    Assert.assertFalse(parser.readDescription(token, false));
    parser.read(new StringReader("\"abc\" \"abc\""));
    token = parser.getLastToken();
    Assert.assertFalse(parser.readDescription(token, false));
    parser.read(new StringReader(""));
    token = parser.getLastToken();
    Assert.assertFalse(parser.readDescription(token, true));
    parser.read(new StringReader("abc ; abc"));
    token = parser.getLastToken();
    Assert.assertFalse(parser.readDescription(token, true));
    parser.read(new StringReader("abc ; abc"));
    token = parser.getLastToken();
    Assert.assertFalse(parser.readDescription(token, false));
    parser.read(new StringReader("\"abc\""));
    token = parser.getLastToken();
    Assert.assertTrue(parser.readDescription(token, true));
    parser.read(new StringReader("\"abc\""));
    token = parser.getLastToken();
    Assert.assertTrue(parser.readDescription(token, false));
    parser.read(new StringReader("\"abc\"; 123"));
    token = parser.getLastToken();
    Assert.assertTrue(parser.readDescription(token, true));
    parser.read(new StringReader("\"abc\"; 123"));
    token = parser.getLastToken();
    Assert.assertTrue(parser.readDescription(token, false));
    parser.read(new StringReader("\"\""));
    token = parser.getLastToken();
    Assert.assertTrue(parser.readDescription(token, true));
    parser.read(new StringReader("\"\""));
    token = parser.getLastToken();
    Assert.assertFalse(parser.readDescription(token, false));
    parser.read(new StringReader("\"\";abc"));
    token = parser.getLastToken();
    Assert.assertTrue(parser.readDescription(token, true));
    parser.read(new StringReader("\"\";abc"));
    token = parser.getLastToken();
    Assert.assertFalse(parser.readDescription(token, false));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#readFinalKeyword(magellan.library.utils.OrderToken)}
   * .
   */
  @Test
  public void testReadFinalKeyword() {
    parser.read(new StringReader("abc"));
    OrderToken token = parser.getLastToken();
    Assert.assertTrue(parser.readFinalKeyword(token));
    Assert.assertTrue(token.ttype == OrderToken.TT_KEYWORD);
    parser.read(new StringReader("abc abc"));
    token = parser.getLastToken();
    Assert.assertFalse(parser.readFinalKeyword(token));
    Assert.assertTrue(token.ttype == OrderToken.TT_KEYWORD);
    parser.read(new StringReader(""));
    token = parser.getLastToken();
    Assert.assertFalse(parser.readFinalKeyword(token));
    Assert.assertTrue(token.ttype == OrderToken.TT_KEYWORD);
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#readFinalString(magellan.library.utils.OrderToken)}
   * .
   */
  @Test
  public void testReadFinalString() {
    parser.read(new StringReader("abc"));
    OrderToken token = parser.getLastToken();
    Assert.assertTrue(parser.readFinalString(token));
    parser.read(new StringReader("abc ;123"));
    token = parser.getLastToken();
    Assert.assertTrue(parser.readFinalString(token));
    parser.read(new StringReader("abc abc"));
    token = parser.getLastToken();
    Assert.assertFalse(parser.readFinalString(token));
    parser.read(new StringReader("\"abc abc\""));
    token = parser.getLastToken();
    Assert.assertTrue(parser.readFinalString(token));
    parser.read(new StringReader("\"abc abc"));
    token = parser.getLastToken();
    Assert.assertFalse(parser.readFinalString(token));
    parser.read(new StringReader(""));
    token = parser.getLastToken();
    Assert.assertFalse(parser.readFinalString(token));
    parser.read(new StringReader("1234#"));
    token = parser.getLastToken();
    try {
      parser.readFinalString(token);
      Assert.assertFalse(true);
    } catch (IllegalArgumentException e) {
      // should throw exception!
    }
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#readFinalID(magellan.library.utils.OrderToken)}
   * .
   */
  @Test
  public void testReadFinalID() {
    parser.read(new StringReader("abc"));
    OrderToken token = parser.getLastToken();
    Assert.assertTrue(parser.readFinalID(token));
    Assert.assertTrue(token.ttype == OrderToken.TT_ID);
    parser.read(new StringReader("abc abc"));
    token = parser.getLastToken();
    Assert.assertFalse(parser.readFinalID(token));
    Assert.assertTrue(token.ttype == OrderToken.TT_ID);
    parser.read(new StringReader("")); // TODO what should happen here?
    token = parser.getLastToken();
    Assert.assertFalse(parser.readFinalID(token));
    Assert.assertTrue(token.ttype == OrderToken.TT_ID);
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#readFinalNumber(magellan.library.utils.OrderToken)}
   * .
   */
  @Test
  public void testReadFinalNumber() {
    parser.read(new StringReader("abc"));
    OrderToken token = parser.getLastToken();
    Assert.assertTrue(parser.readFinalNumber(token));
    Assert.assertTrue(token.ttype == OrderToken.TT_NUMBER);
    parser.read(new StringReader("abc abc"));
    token = parser.getLastToken();
    Assert.assertFalse(parser.readFinalNumber(token));
    Assert.assertTrue(token.ttype == OrderToken.TT_NUMBER);
    parser.read(new StringReader(""));
    token = parser.getLastToken();
    Assert.assertFalse(parser.readFinalNumber(token));
    Assert.assertTrue(token.ttype == OrderToken.TT_NUMBER);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser#checkNextFinal()}.
   */
  @Test
  public void testCheckNextFinal() {
    parser.read(new StringReader("abc"));
    Assert.assertTrue(parser.checkNextFinal());
    parser.read(new StringReader(""));
    Assert.assertFalse(parser.checkNextFinal());
    parser.read(new StringReader("; abc"));
    Assert.assertFalse(parser.checkNextFinal());
    parser.read(new StringReader("abc; abc"));
    Assert.assertTrue(parser.checkNextFinal());
    parser.read(new StringReader("abc abc"));
    Assert.assertFalse(parser.checkNextFinal());
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#checkFinal(magellan.library.utils.OrderToken)}
   * .
   */
  @Test
  public void testCheckFinal() {
    Assert.assertTrue(parser.checkFinal(new OrderToken(OrderToken.TT_EOC)));
    Assert.assertTrue(parser.checkFinal(new OrderToken(OrderToken.TT_COMMENT)));
    Assert.assertFalse(parser.checkFinal(new OrderToken(OrderToken.TT_STRING)));
    parser.read(new StringReader(""));
    Assert.assertTrue(parser.checkFinal(parser.getLastToken()));
    parser.read(new StringReader("; abc"));
    Assert.assertTrue(parser.checkFinal(parser.getLastToken()));
    parser.read(new StringReader("abc; abc"));
    Assert.assertFalse(parser.checkFinal(parser.getLastToken()));
    Assert.assertTrue(parser.checkFinal(parser.getNextToken()));
    parser.read(new StringReader("abc"));
    Assert.assertFalse(parser.checkFinal(parser.getLastToken()));
    Assert.assertTrue(parser.checkFinal(parser.getNextToken()));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#unexpected(magellan.library.utils.OrderToken)}
   * .
   */
  @Test
  public void testUnexpected() {
    Assert.assertTrue(parser.getErrorMessage() == null);
    parser.setErrMsg("error");
    Assert.assertTrue(parser.getErrorMessage().equals("error"));
    parser.setErrMsg(null);
    Assert.assertTrue(parser.getErrorMessage() == null);
    parser.read(new StringReader("ARBEITEN"));
    Assert.assertTrue(parser.getErrorMessage() == null);
    parser.read(new StringReader("ARBEITEN 2"));
    Assert.assertTrue(parser.getErrorMessage().equals(
        "Unexpected token 2: Undefined(9, 10), not followed by Space"));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isNumeric(java.lang.String, int, int, int)}
   * .
   */
  @Test
  public void testIsNumericStringIntIntInt() {

  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isNumeric(java.lang.String)}.
   */
  @Test
  public void testIsNumericString() {
    Assert.assertTrue(parser.isNumeric("123456567"));
    Assert.assertTrue(parser.isNumeric("-2", 10, -10, 0));
    Assert.assertTrue(parser.isNumeric("abc", 36, 13368, 13368));
    Assert.assertTrue(parser.isNumeric("ff", 16, 0, 256));
    Assert.assertFalse(parser.isNumeric("ff", 16, 0, 100));
    Assert.assertFalse(parser.isNumeric("-2"));
    Assert.assertFalse(parser.isNumeric("1 2"));
    Assert.assertFalse(parser.isNumeric("1,2"));
    Assert.assertFalse(parser.isNumeric("1.2"));
    Assert.assertFalse(parser.isNumeric("--"));
    Assert.assertFalse(parser.isNumeric("a"));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser#isID(java.lang.String)}.
   */
  @Test
  public void testIsID() {
    Assert.assertTrue(parser.isID("TEMP abc"));
    Assert.assertFalse(parser.isID("TEMP abc", false));
    Assert.assertTrue(parser.isID("12"));
    Assert.assertTrue(parser.isID("abc"));
    Assert.assertTrue(parser.isID("2ac"));
    Assert.assertFalse(parser.isID("12345"));
    Assert.assertFalse(parser.isID("1,3"));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isTempID(java.lang.String)}.
   */
  @Test
  public void testIsTempID() {
    Assert.assertTrue(parser.isTempID("TEMP abc"));
    Assert.assertTrue(parser.isTempID("TEMP 1"));
    Assert.assertFalse(parser.isTempID("1,3"));
    Assert.assertFalse(parser.isTempID("abc"));
    Assert.assertFalse(parser.isTempID(" TEMP abc "));
    Assert.assertFalse(parser.isTempID(" TEMP TEMP temp"));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser#isRID(java.lang.String)}
   * .
   */
  @Test
  public void testIsRID() {
    Assert.assertTrue(parser.isRID("1,3"));
    Assert.assertTrue(parser.isRID("1,-3"));
    Assert.assertTrue(parser.isRID("-1,-3323"));
    Assert.assertTrue(parser.isRID("1,3,4"));
    Assert.assertFalse(parser.isRID("1, 3"));
    Assert.assertFalse(parser.isRID(" 1,3"));
    Assert.assertFalse(parser.isRID("1,3 "));
    Assert.assertFalse(parser.isRID("1,, 3"));
    Assert.assertFalse(parser.isRID("1 3"));
    Assert.assertFalse(parser.isRID("1 -3"));
    Assert.assertFalse(parser.isRID("-a, 1"));
    Assert.assertFalse(parser.isRID("123"));
    Assert.assertFalse(parser.isRID("a, b"));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isQuoted(java.lang.String)}.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testIsQuoted() {
    Assert.assertFalse(parser.isQuoted("abc"));
    Assert.assertTrue(parser.isQuoted("\"abc\""));
    Assert.assertFalse(parser.isQuoted("'abc'"));
    Assert.assertFalse(parser.isQuoted("abc5d"));
    Assert.assertFalse(parser.isQuoted("567"));
    Assert.assertFalse(parser.isQuoted("\"abc'"));
    Assert.assertFalse(parser.isQuoted("'123"));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isSingleQuoted(java.lang.String)}.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testIsSingleQuoted() {
    Assert.assertFalse(parser.isSingleQuoted("abc"));
    Assert.assertFalse(parser.isSingleQuoted("\"abc\""));
    Assert.assertTrue(parser.isSingleQuoted("'abc'"));
    Assert.assertFalse(parser.isSingleQuoted("abc5d"));
    Assert.assertFalse(parser.isSingleQuoted("567"));
    Assert.assertFalse(parser.isSingleQuoted("\"abc'"));
    Assert.assertFalse(parser.isSingleQuoted("'123"));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isSimpleString(java.lang.String)}.
   */
  @Test
  public void testIsSimpleString() {
    Assert.assertTrue(parser.isSimpleString("abc"));
    Assert.assertFalse(parser.isSimpleString("\"abc\""));
    Assert.assertFalse(parser.isSimpleString("'abc'"));
    Assert.assertTrue(parser.isSimpleString("abc5d"));
    Assert.assertFalse(parser.isSimpleString("567"));
    Assert.assertFalse(parser.isSimpleString("\"abc'"));
    Assert.assertFalse(parser.isSimpleString("'123"));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isString(java.lang.String)}.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testIsStringString() {
    Assert.assertTrue(parser.isString("abc"));
    Assert.assertTrue(parser.isString("\"abc\""));
    Assert.assertTrue(parser.isString("'abc'"));
    Assert.assertTrue(parser.isString("abc5d"));
    Assert.assertFalse(parser.isString("567"));
    Assert.assertFalse(parser.isString("\"abc'"));
    Assert.assertFalse(parser.isString("'123"));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isString(magellan.library.utils.OrderToken)}
   * .
   */
  @Test
  public void testIsStringOrderToken() {
    OrderToken token = new OrderToken("");
    Assert.assertTrue(parser.isString(token, false) == parser.isString(token));
    token = new OrderToken(OrderToken.TT_OPENING_QUOTE);
    Assert.assertTrue(parser.isString(token, false) == parser.isString(token));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isString(magellan.library.utils.OrderToken, boolean)}
   * .
   */
  @Test
  public void testIsStringOrderTokenBoolean() {
    OrderToken token = new OrderToken("");
    Assert.assertFalse(parser.isString(token, true));
    Assert.assertFalse(parser.isString(token, false));
    token = new OrderToken("a");
    Assert.assertFalse(parser.isString(token, true));
    Assert.assertTrue(parser.isString(token, false));
    token = new OrderToken(OrderToken.TT_OPENING_QUOTE);
    Assert.assertTrue(parser.isString(token, true));
    Assert.assertTrue(parser.isString(token, false));
    token = new OrderToken("'abc");
    Assert.assertFalse(parser.isString(token, true));
    Assert.assertFalse(parser.isString(token, false));
    token = new OrderToken("5");
    Assert.assertFalse(parser.isString(token, true));
    Assert.assertFalse(parser.isString(token, false));
    token = new OrderToken(OrderToken.TT_EOC);
    Assert.assertTrue(parser.isString(token, true));
    Assert.assertTrue(parser.isString(token, false));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#getString(magellan.library.utils.OrderToken)}
   * .
   */
  @Test
  public void testGetString() {
    parser.read(new StringReader("abc"));
    OrderToken[] result = parser.getString(parser.getLastToken());
    Assert.assertTrue(result[0] == null);
    OrderToken contentToken = new OrderToken("abc", 0, 3, OrderToken.TT_STRING, false);
    Assert.assertTrue(result[1] != null && equals(result[1], contentToken));
    Assert.assertTrue(result[2] == null);
    OrderToken nextToken = new OrderToken(OrderToken.TT_EOC);
    Assert.assertTrue(equals(result[3], nextToken));

    parser.read(new StringReader("@"));
    result = parser.getString(parser.getLastToken());
    Assert.assertTrue(result[0] == null);
    contentToken = new OrderToken("@", 0, 1, OrderToken.TT_STRING, false);
    Assert.assertTrue(result[1] == null);
    Assert.assertTrue(result[2] == null);

    parser.read(new StringReader("\"abc\""));
    result = parser.getString(parser.getLastToken());
    OrderToken openingToken = new OrderToken("\"", 0, 1, OrderToken.TT_OPENING_QUOTE, false);
    Assert.assertTrue(equals(result[0], openingToken));
    contentToken = new OrderToken("abc", 1, 4, OrderToken.TT_STRING, false);
    Assert.assertTrue(result[1] != null && equals(result[1], contentToken));
    OrderToken closingToken = new OrderToken("\"", 4, 5, OrderToken.TT_CLOSING_QUOTE, false);
    Assert.assertTrue(equals(result[2], closingToken));
    nextToken = new OrderToken(OrderToken.TT_EOC);
    Assert.assertTrue(equals(result[3], nextToken));

    parser.read(new StringReader("'abc'"));
    result = parser.getString(parser.getLastToken());
    openingToken = new OrderToken("'", 0, 1, OrderToken.TT_OPENING_QUOTE, false);
    Assert.assertTrue(equals(result[0], openingToken));
    contentToken = new OrderToken("abc", 1, 4, OrderToken.TT_STRING, false);
    Assert.assertTrue(result[1] != null && equals(result[1], contentToken));
    closingToken = new OrderToken("'", 4, 5, OrderToken.TT_CLOSING_QUOTE, false);
    Assert.assertTrue(equals(result[2], closingToken));
    nextToken = new OrderToken(OrderToken.TT_EOC);
    Assert.assertTrue(equals(result[3], nextToken));

    parser.read(new StringReader("\"a"));
    result = parser.getString(parser.getLastToken());
    openingToken = new OrderToken("\"", 0, 1, OrderToken.TT_OPENING_QUOTE, false);
    Assert.assertTrue(equals(result[0], openingToken));
    contentToken = new OrderToken("a", 1, 2, OrderToken.TT_STRING, false);
    Assert.assertTrue(result[1] != null && equals(result[1], contentToken));
    closingToken = null;
    Assert.assertTrue(equals(result[2], closingToken));
    nextToken = new OrderToken(OrderToken.TT_EOC);
    Assert.assertTrue(equals(result[3], nextToken));

    parser.read(new StringReader("\"a'"));
    result = parser.getString(parser.getLastToken());
    openingToken = new OrderToken("\"", 0, 1, OrderToken.TT_OPENING_QUOTE, false);
    Assert.assertTrue(equals(result[0], openingToken));
    contentToken = new OrderToken("a'", 1, 3, OrderToken.TT_STRING, false);
    Assert.assertTrue(result[1] != null && equals(result[1], contentToken));
    closingToken = null;
    Assert.assertTrue(equals(result[2], closingToken));
    nextToken = new OrderToken(OrderToken.TT_EOC);
    Assert.assertTrue(equals(result[3], nextToken));

  }

  protected boolean equals(OrderToken orderToken, OrderToken nextToken) {
    if (orderToken == null || nextToken == null)
      return orderToken == nextToken;
    return orderToken.getText().equals(nextToken.getText())
        && orderToken.getStart() == nextToken.getStart()
        && orderToken.getEnd() == nextToken.getEnd() && orderToken.ttype == nextToken.ttype
        && orderToken.followedBySpace() == nextToken.followedBySpace();
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isEmailAddress(java.lang.String)}.
   */
  @Test
  public void testIsEmailAddress() {
    Assert.assertTrue(parser != null);
    if (parser != null) {
      Assert.assertTrue(parser.isEmailAddress("a@b.com"));
      Assert.assertTrue(parser.isEmailAddress("123@234.com"));
      Assert.assertTrue(parser.isEmailAddress("a.b.c.defg.a@hallo.bla.bla.com"));
      Assert.assertFalse(parser.isEmailAddress(""));
      Assert.assertFalse(parser.isEmailAddress("a"));
      Assert.assertFalse(parser.isEmailAddress("a.b"));
      Assert.assertFalse(parser.isEmailAddress("a@b"));
      Assert.assertFalse(parser.isEmailAddress("@b"));
      Assert.assertFalse(parser.isEmailAddress("@b.com"));
      Assert.assertFalse(parser.isEmailAddress(".@."));
    }
  }

}
