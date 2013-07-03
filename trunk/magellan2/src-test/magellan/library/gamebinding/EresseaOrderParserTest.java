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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;

import magellan.client.completion.AutoCompletion;
import magellan.library.Faction;
import magellan.library.Region;
import magellan.library.completion.Completer;
import magellan.library.completion.OrderParser;
import magellan.library.gamebinding.AbstractOrderParser.TokenBucket;
import magellan.library.utils.OrderToken;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests if valid orders are parsed correctly
 * 
 * @author stm
 * @version 1.0, Jun 20, 2009
 */
public class EresseaOrderParserTest extends AbstractOrderParserTest {

  EresseaOrderParser parser;
  EresseaOrderCompleter completer;

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    setLocale(DE_LOCALE);
    MagellanTestWithResources.initResources();
  }

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    builder = new GameDataBuilder();
    data = builder.createSimpleGameData();

    Region region = data.getRegions().iterator().next();
    Faction faction = data.getFactions().iterator().next();
    builder.addBuilding(data, region, "burg", "Burg", "gro�e Burg", 200);
    builder.addShip(data, region, "ship", "Langboot", "ein Langboot", 50);
    builder.addUnit(data, "zwei", "Zweite", faction, region);

    setParser(new EresseaOrderParser(data));
    completion = new AutoCompletion(context);
    setCompleter(new EresseaOrderCompleter(data, completion));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#EresseaOrderParser(magellan.library.GameData, magellan.library.gamebinding.EresseaOrderCompleter)}
   * .
   */
  @Test
  public void testEresseaOrderParserGameDataEresseaOrderCompleter() {
    EresseaOrderParser localParser =
        new EresseaOrderParser(data, (EresseaOrderCompleter) getCompleter());
    assertTrue(localParser.getData() == data);
    assertTrue(localParser.getCompleter() == getCompleter());
    assertSame(61, localParser.getCommands().size());
    assertSame(61, localParser.getHandlers().size());
  }

  /**
   * 
   */
  @Test
  public void testCommentReader() {
    checkOrder(EresseaConstants.O_PCOMMENT);
    checkOrder("//");
    checkOrder("// ");
    checkOrder("// HALLO");
    checkOrder("//;", false);
    checkOrder("//ARBEITE", false);
    checkOrder("///", false);
    checkOrder("////", false);

    checkOrder("");
    checkOrder(" ");
    checkOrder(";");
    checkOrder("; askj baskjdb");
    checkOrder(";;;");
    checkOrder(" ; abc");
  }

  /**
   * Testmethod for ARBEITE
   */
  @Test
  public void testArbeiteReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_WORK));
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
   * Test method for @ orders.
   */
  @Test
  public void testAtReader() {
    checkOrder("@ARBEITE");
    checkOrder(" @ARBEITE");
    checkOrder("@", false);
    checkOrder("@@ARBEITE", false); // server actually accepts this and turn it into @ARBEITE...
    checkOrder("@@", false);
    checkOrder("@ARBEITE 1", false);
    checkOrder(";@", true);
    checkOrder("@; ", false);
    checkOrder("@  ; ", false);
    checkOrder("@// ", false); // Server actually accepts this; dodgy...
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.AttackReader}.
   */
  @Test
  public void testAttackReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_ATTACK) + " 123");
    checkOrder("ATTACKIERE 123");
    checkOrder("ATTACKIERE xyz");
    checkOrder("ATTACKIERE TEMP xyz"); // this is actually legal
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
    checkOrder(getOrderTranslation(EresseaConstants.OC_BANNER) + "\"\"");
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
    checkOrder(getOrderTranslation(EresseaConstants.OC_CLAIM) + " Sonnensegel");
    checkOrder("BEANSPRUCHE 2 Sonnensegel");
    checkOrder("BEANSPRUCHE \"Sch�nes Geschenk\"");
    checkOrder("BEANSPRUCHE Sch�nes~Geschenk");
    checkOrder("BEANSPRUCHE", false);
    checkOrder("BEANSPRUCHE 1", false); // ??
    checkOrder("BEANSPRUCHE Sch�nes Geschenk", false); // ??
    checkOrder("BEANSPRUCHE 1 2 Sonnensegel", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.BefoerderungReader}.
   */
  @Test
  public void testBefoerderungReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_PROMOTION));
    checkOrder("BEF�RDERUNG");
    checkOrder("BEF�RDER");
    checkOrder("BEF�RDERUNG ;");
    checkOrder("BEF�RDERE", false);
    checkOrder("BEF�RDERUNG 1", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.BeklaueReader}.
   */
  @Test
  public void testBeklaueReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_STEAL) + " 123");
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
    checkOrder(getOrderTranslation(EresseaConstants.OC_SIEGE) + " burg");
    checkOrder("BELAGERE burg");
    checkOrder("BELAGERE abc", false);
    checkOrder("BELAGERUNG burg", false);
    checkOrder("BELAGERE TEMP burg", false);
    checkOrder("BELAGERE burg 123", false);
    checkOrder("BELAGERE 2 burg", false);
    checkOrder("BELAGERE \"burg\"", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.BenenneReader}.
   */
  @Test
  public void testBenenneReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_NAME) + " EINHEIT \"Foo\"");
    for (String thing : new String[] { "EINHEIT", "PARTEI", "BURG", "Geb�ude", "S�gewerk",
        "SCHIFF", "REGION" }) {
      checkOrder("BENENNEN " + thing + " \"Foo\"");
      checkOrder("BENENNE " + thing + " \"Foo\"; comment");
      checkOrder("BENENNE " + thing + " \"\"", false);
      checkOrder("BENENNE " + thing + " abc", false);
      checkOrder("BENENNE " + thing + " 123 \"abc\"", false);
      checkOrder("BENENNE " + thing + " \"abc\" 123", false);
    }

    checkOrder("BENENNE FREMDE EINHEIT zwei \"abc\"");
    checkOrder("BENENNE FREMDE EINHEIT zwei \"\"", false);
    checkOrder("BENENNE FREMDE PARTEI zwei \"abc\"", true); // no such faction
    checkOrder("BENENNE FREMDE BURG burg \"abc\"");
    checkOrder("BENENNE FREMDE SCHIFF ship \"abc\"");

    // for (String thing : new String[] { "EINHEIT", "PARTEI", "BURG", "Geb�ude", "S�gewerk",
    // "SCHIFF" }) {
    // checkOrder("BENENNEN FREMDE " + thing + " 123 \"Foo\"");
    // checkOrder("BENENNE FREMDE " + thing + " abc \"Foo\"; comment");
    // checkOrder("BENENNE FREMDE " + thing + " TEMP xyz \"Foo\""); // this is really allowed (for
    // // units)
    // checkOrder("BENENNE FREMDE " + thing + " xyz \"\"", false);
    // checkOrder("BENENNE FREMDE " + thing + " abc abc", false);
    // checkOrder("BENENNE FREMDE " + thing + " 123 123 \"abc\"", false);
    // checkOrder("BENENNE FREMDE " + thing + " 123 \"abc\" 123", false);
    // }

    checkOrder("BENENNE abc \"abc\"", false);

    // FIXME: these ambiguous commands shouldn't be accepted (maybe?)
    if (DO_KNOWN_FAILURES) {
      for (String thing : new String[] { "S", "E" }) {
        checkOrder("BENENNEN " + thing + " \"Foo\"", false); // is it SCHIFF or S�gewerk or
                                                             // Steinbruch??
        checkOrder("BENENNE " + thing + " \"Foo\"; comment", false);
      }
    }
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.BenutzeReader}.
   */
  @Test
  public void testBenutzeReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_USE) + " Siebenmeilentee");
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
    checkOrder(getOrderTranslation(EresseaConstants.OC_DESCRIBE) + " EINHEIT \"Foo\"");
    for (String thing : new String[] { "EINHEIT", "PRIVAT", "BURG", "S�gewerk", "SCHIFF", "REGION" }) {
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
    checkOrder(getOrderTranslation(EresseaConstants.OC_ENTER) + " "
        + getOrderTranslation(EresseaConstants.OC_SHIP) + " ship");
    checkOrder("BetreteN BURG burg");
    checkOrder("BetreteN BURG burg; ");
    checkOrder("BetreteN BURG abc", true); // no such building
    checkOrder("BetreteN BURG TEMP 123", false);
    checkOrder("BetreteN BURG \"burg\"", false);
    checkOrder("BetreteN 1 BURG burg", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.BewacheReader}.
   */
  @Test
  public void testBewacheReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_GUARD));
    checkOrder("BEWACHE");
    checkOrder("BEWACHE ; a");
    checkOrder("BEWACHE 2", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.BotschaftReader}.
   */
  @Test
  public void testBotschaftReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_MESSAGE) + " "
        + getOrderTranslation(EresseaConstants.OC_REGION) + " \"hallo\"");
    for (String thing : new String[] { "EINHEIT", "PARTEI", "BURG", "S�gewerk", "SCHIFF", "REGION" }) {
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
    checkOrder(getOrderTranslation(EresseaConstants.OC_DEFAULT) + " "
        + getOrderTranslation(EresseaConstants.OC_WORK));
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
    checkOrder(getOrderTranslation(EresseaConstants.OC_EMAIL) + " \"a@b.com\"");
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
    checkOrder(getOrderTranslation(EresseaConstants.OC_END));
    checkOrder("ENDE 123", false);
    checkOrder("ENDE \"123\"", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.FahreReader}.
   */
  @Test
  public void testFahreReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_RIDE) + " zwei");
    checkOrder("FAHREN zwei");
    checkOrder("FAHREN TEMP 456");
    checkOrder("FAHREN abc"); // invisible unit allowed
    checkOrder("FAHREN abcde", false);
    checkOrder("FAHREN zwei zwei", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.FolgeReader}.
   */
  @Test
  public void testFolgeReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_FOLLOW) + " "
        + getOrderTranslation(EresseaConstants.OC_UNIT) + " zwei");
    checkOrder("FOLGEN SCHIFF ship");
    checkOrder("FOLGEN SCHIFF 123", false);
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
    checkOrder(getOrderTranslation(EresseaConstants.OC_RESEARCH) + " "
        + getOrderTranslation(EresseaConstants.OC_HERBS));
    checkOrder("FORSCHE", false);
    checkOrder("FORSCHE KR�UTER 123", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.GibReader}.
   */
  @Test
  public void testSupplyReader() {
    // there is an undocumented supply order in Eressea
    checkOrder(getOrderTranslation(EresseaConstants.OC_SUPPLY) + " 123 "
        + getOrderTranslation(EresseaConstants.OC_HERBS));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.GibReader}.
   */
  @Test
  public void testGibReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_GIVE) + " 123 "
        + getOrderTranslation(EresseaConstants.OC_HERBS));
    checkOrder("GIB KR�UTER", false);
    checkOrder("GIB abc \"KR�UTER\"", false);
    checkOrder("GIB 123 KR�UTER 123", false);

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
    checkOrder("GIB 123 2 W�rziger~Wagemut");
    checkOrder("GIB 123 ALLES Holz");
    checkOrder("GIB 123 2 W�rziger Wagemut", false);

    checkOrder("GIB 123 2 W�rziger~Wagemut");

    checkOrder("GIB 123 2 EINHEIT", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.GruppeReader}.
   */
  @Test
  public void testGruppeReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_GROUP));
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
    checkOrder(getOrderTranslation(EresseaConstants.OC_HELP) + " 123 "
        + getOrderTranslation(EresseaConstants.OC_ALL));
    checkOrder("HELFE 123 ALLES NICHT");
    checkOrder("HELFEN 123 GIB");
    checkOrder("HELFEN 123 GIB NICHT");
    checkOrder("HELFE 123 K�MPFE");
    checkOrder("HELFE 123 BEWACHEN");
    checkOrder("HELFE 123 SILBER");
    checkOrder("HELFE 123 PARTEITARNUNG");
    checkOrder("HELFE 123 bla", false);
    checkOrder("HELFE abcde GIB", false);
    checkOrder("HELFE 123 GIB BLA", false);
    checkOrder("HELFE 123 456", false);
    checkOrder("HELFE TEMP 456", false);
    checkOrder("HELFE 123", false);
    checkOrder("HELFE 123 K�MPFEN", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.KaempfeReader}.
   */
  @Test
  public void testKaempfeReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_COMBAT));
    checkOrder("K�MPFE AGGRESSIV");
    checkOrder("K�MPFE HINTEN");
    checkOrder("K�MPFE DEFENSIV");
    checkOrder("K�MPFE NICHT");
    checkOrder("K�MPFE FLIEHE");
    checkOrder("K�MPFE HELFE");
    checkOrder("K�MPFE HELFE NICHT");
    checkOrder("K�MPFE VORNE", false); // deprecated
    checkOrder("K�MPFE AGGRESSIV NICHT", false);
    checkOrder("K�MPFE VORNE HINTEN", false);
    checkOrder("K�MPFE 123 HINTEN", false);
    checkOrder("K�MPFE FLIEHE NICHT", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.KampfzauberReader}.
   */
  @Test
  public void testKampfzauberReader() {
    GameDataBuilder.addSpells(data);

    checkOrder(getOrderTranslation(EresseaConstants.OC_COMBATSPELL) + " Hagel");
    checkOrder("KAMPFZAUBER STUFE 2 Hagel");
    checkOrder("KAMPFZAUBER Hagel NICHT");
    checkOrder("KAMPFZAUBER STUFE 2 \"Gro�es Fest\"", false); // no combat spell
    checkOrder("KAMPFZAUBER STUFE Hagel", false);
    checkOrder("KAMPFZAUBER Magisches Gescho�", false);
    checkOrder("KAMPFZAUBER Hagel 123", false);
    checkOrder("KAMPFZAUBER STUFE x Hagel", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.KaufeReader}.
   */
  @Test
  public void testKaufeReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_BUY) + " 2 Balsam");
    checkOrder("KAUFE 2 �l");
    checkOrder("KAUFE 2 Oel");
    checkOrder("KAUFE Weihrauch", false);
    checkOrder("KAUFE 2 Schnickschnak", false);
    checkOrder("KAUFE 2 �l Weihrauch", false);
    checkOrder("KAUFE", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.KontaktiereReader}.
   */
  @Test
  public void testKontaktiereReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_CONTACT) + " 123");
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
    checkOrder(getOrderTranslation(EresseaConstants.OC_TEACH) + " abc");
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
    checkOrder(getOrderTranslation(EresseaConstants.OC_LEARN) + " Ausdauer");
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
    // this is a valid order, but not /inside/ a unit
    checkOrder(getOrderTranslation(EresseaConstants.OC_LOCALE), false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.MacheReader}.
   */
  @Test
  public void testMacheReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_MAKE) + " TEMP 123");
    checkOrder("MACHE BURG");
    checkOrder("MACHE 2 BURG");
    checkOrder("MACHE BURG 123");
    checkOrder("MACHE 3 BURG abc");
    checkOrder("MACHE S�gewerk");
    checkOrder("MACHE 2 S�gewerk");
    checkOrder("MACHE Boot");
    checkOrder("MACHE Trireme");
    checkOrder("MACHE SCHIFF");
    checkOrder("MACHE SCHIFF 123");
    checkOrder("MACHE 2 SCHIFF 123");
    checkOrder("MACHE STRASSE no");
    checkOrder("MACHE 2 STRASSE no");
    checkOrder("MACHE STRASSE nordwesten");
    checkOrder("MACHE KR�UTER");
    checkOrder("MACHE 2 KR�UTER");
    checkOrder("MACHE Pferd");
    checkOrder("MACHE Pferd Pferd", false);
    checkOrder("MACHE 2 Schwert");
    checkOrder("MACHE 1 Pferd");
    checkOrder("MACHE", false); // actually, this is correct, but dangerous
    checkOrder("MACHE \"S�gewerk\"", false); // well...
    checkOrder("MACHE \"Pferd\"");
    checkOrder("MACHE 2 Schwert");
    checkOrder("MACHE 2 \"Rostiger Zweih�nder\"");
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
    checkOrder("MACHE KR�UTER abc", false);
    checkOrder("MACHE Pferd abc", false);
    checkOrder("MACHE a Pferd", false);
    checkOrder("MACHE a Hurz", false);
    checkOrder("MACHE a Rostiger Bih�nder", false);
    checkOrder("MACHE 2 Wasser~des~Lebens"); // TODO
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.NachReader}.
   */
  @Test
  public void testNachReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_MOVE) + " westen");
    checkOrder("NACH o");
    checkOrder("NACH so");
    checkOrder("NACH sw");
    checkOrder("NACH w");
    checkOrder("NACH nw");
    checkOrder("NACH no");
    checkOrder("NACH osten");
    checkOrder("NACH s�dosten");
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
    checkOrder(getOrderTranslation(EresseaConstants.OC_RESTART) + " Trolle \"passwort\"");
    checkOrder("NEUSTART Zwerge \"\"", false);
    checkOrder("NEUSTART Zwerge", false);
    checkOrder("NEUSTART", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.NummerReader}.
   */
  @Test
  public void testNummerReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_NUMBER) + " EINHEIT");
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
    checkOrder(getOrderTranslation(EresseaConstants.OC_OPTION) + " AUSWERTUNG");
    checkOrder("OPTION PUNKTE NICHT");
    checkOrder("OPTION PUNKTE NICHT MEHR", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.ParteiReader}.
   */
  @Test
  public void testParteiReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_FACTION), false); // TODO???
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.PasswortReader}.
   */
  @Test
  public void testPasswortReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_PASSWORD) + " \"squiggy\"");
    checkOrder("PASSWORT", true);
    checkOrder("PASSWORT 123", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.PflanzeReader}.
   */
  @Test
  public void testPflanzeReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_PLANT) + " "
        + getOrderTranslation(EresseaConstants.OC_HERBS));
    checkOrder("PFLANZE B�UME");
    checkOrder("PFLANZE MALLOrnSamen");
    checkOrder("PFLANZE SAMEN");
    checkOrder("PFLANZE 4 B�UME");
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
    checkOrder(getOrderTranslation(EresseaConstants.OC_PIRACY));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.PraefixReader}.
   */
  @Test
  public void testPraefixReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_PREFIX));
    checkOrder("PR�FIX Nebel");
    checkOrder("PR�FIX Blubb"); // do not currently test for allowed prefixes
    checkOrder("PR�FIX Bla blubb", false);
    checkOrder("PR�FIX 2", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.RegionReader}.
   */
  @Test
  public void testRegionReader() {
    // this is a valid order, but not /inside/ a unit
    checkOrder(getOrderTranslation(EresseaConstants.OC_REGION) + " 1,1", false);
    // FIXME read comma'd coordinate
    /*
     * checkOrder(getOrderTranslation(EresseaConstants.OC_REGION) + " 1,1"); checkOrder("REGION",
     * false); checkOrder("REGION 1 3", false); checkOrder("REGION 123", false);
     * checkOrder("REGION abc,def", false);
     */
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.RekrutiereReader}.
   */
  @Test
  public void testRekrutiereReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_RECRUIT) + " 1");
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
    checkOrder(getOrderTranslation(EresseaConstants.OC_RESERVE) + " 1 "
        + data.rules.getItemType(EresseaConstants.I_USILVER).getName());
    checkOrder("RESERVIEREN ALLES Holz");
    checkOrder("RESERVIERE 2 Silber");
    checkOrder("RESERVIERE JE 1 Holz");
    checkOrder("RESERVIERE 1 Flabberghast"); // item does not exist
    checkOrder("RESERVIEREN ALLES", false);
    checkOrder("RESERVIEREN JE", false);
    checkOrder("RESERVIEREN JE 2", false);
    checkOrder("RESERVIEREN JE 2 ALLES", false);
    checkOrder("RESERVIERE JE Holz", false);
    checkOrder("RESERVIERE 1.5 Holz", false);
    checkOrder("RESERVIERE 2 1 Holz", false);
    checkOrder("RESERVIERE 1 1", false);
    checkOrder("RESERVIERE", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.RouteReader}.
   */
  @Test
  public void testRouteReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_ROUTE) + " "
        + getOrderTranslation(EresseaConstants.OC_NE));
    checkOrder("ROUTE o");
    checkOrder("ROUTE so");
    checkOrder("ROUTE sw");
    checkOrder("ROUTE w");
    checkOrder("ROUTE nw");
    checkOrder("ROUTE no");
    checkOrder("ROUTE osten");
    checkOrder("ROUTE s�dosten");
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
    checkOrder(getOrderTranslation(EresseaConstants.OC_SORT) + " "
        + getOrderTranslation(EresseaConstants.OC_BEFORE) + " " + "123");
    checkOrder("SORTIERE VOR abc");
    checkOrder("SORTIERE HINTER abc");
    checkOrder("SORTIERE VOR ", false);
    checkOrder("SORTIERE", false);
    checkOrder("SORTIERE abc abc", false);
    checkOrder("SORTIERE 1 abc", false);
    checkOrder("SORTIERE VOR abcdefg", false);
    checkOrder("SORTIERE VOR abc 123", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.SpioniereReader}.
   */
  @Test
  public void testSpioniereReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_SPY) + " abc");
    checkOrder("SPIONIERE 123");
    checkOrder("SPIONIERE 123 123", false);
    checkOrder("SPIONIERE", false);
    checkOrder("SPIONIERE ALLES", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.StirbReader}.
   */
  @Test
  public void testStirbReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_QUIT) + " \"abc\"");
    checkOrder("STIRB", false);
    checkOrder("STIRB 123", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.TarneReader}.
   */
  @Test
  public void testTarneReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_HIDE));
    checkOrder("TARNE 0");
    checkOrder("TARNE 1");
    checkOrder("TARNE NICHT", false);
    checkOrder("TARNE xyz", false);
    checkOrder("TARNE 0 1", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.TransportiereReader}.
   */
  @Test
  public void testTransportiereReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_CARRY) + " zwei");
    checkOrder("TRANSPORTIERE zwei NICHT", false);
    checkOrder("TRANSPORTIERE NICHT", false);
    checkOrder("TRANSPORTIERE zwei zwei", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.TreibeReader}.
   */
  @Test
  public void testTreibeReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_TAX));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.UnterhalteReader}.
   */
  @Test
  public void testUnterhalteReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_ENTERTAIN));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.UrsprungReader}.
   */
  @Test
  public void testUrsprungReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_ORIGIN) + " 1 1");
    checkOrder("URSPRUNG 1", false);
    checkOrder("URSPRUNG 1 2 3", false);
    checkOrder("URSPRUNG 2,3", false);
    checkOrder("URSPRUNG a b", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.VergesseReader}.
   */
  @Test
  public void testVergesseReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_FORGET) + " Hiebwaffen");
    checkOrder("VERGESSE", false);
    checkOrder("VERGESSE Tuten", false);
    checkOrder("VERGESSE Hiebwaffen Ausdauer", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.VerkaufeReader}.
   */
  @Test
  public void testVerkaufeReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_SELL) + " 1 Balsam");
    checkOrder("VERKAUFE ALLES Balsam");
    checkOrder("VERKAUFE Balsam", false);
    checkOrder("VERKAUFE 2 3 Balsam", false);
    checkOrder("VERKAUFE Balsam Balsam", false);
    checkOrder("VERKAUFE 2", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.FinalKeywordReader}.
   */
  @Test
  public void testFinalKeywordReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_LEAVE));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.ZaubereReader}.
   */
  @Test
  public void testZaubereReader() {
    GameDataBuilder.addSpells(data);

    checkOrder(getOrderTranslation(EresseaConstants.OC_CAST) + " \"Gro�es Fest\"");

    checkOrder("ZAUBERE STUFE 2 Schild zwei");
    checkOrder("ZAUBERE STUFE 2 \"Gro�es Fest\"");
    checkOrder("ZAUBERE Schild NICHT", true); // TODO cave: NICHT is read as spell parameter
    checkOrder("ZAUBERE \"Gro�es Fest\" NICHT", false);
    checkOrder("ZAUBERE Hagel", false); // combat spell
    checkOrder("ZAUBERE STUFE Schild zwei", false);
    checkOrder("ZAUBERE Magisches Gescho�", false);
    checkOrder("ZAUBERE \"Gro�es Fest\" zwei", false);
    checkOrder("ZAUBERE STUFE x Schild zwei", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.ZeigeReader}.
   */
  @Test
  public void testZeigeReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_SHOW) + " Schild");
    checkOrder("ZEIGE ALLES ZAUBER");
    checkOrder("ZEIGE ALLES Tr�nke");
    checkOrder("ZEIGE Zwerg");
    checkOrder("ZEIGE Schild");
    checkOrder("ZEIGE ALLES", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.ZerstoereReader}.
   */
  @Test
  public void testZerstoereReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_DESTROY));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.ZuechteReader}.
   */
  @Test
  public void testZuechteReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_GROW) + " PFERDE");
    checkOrder("Z�CHTE KR�UTER");
    checkOrder("Z�CHTE 2 KR�UTER");
    checkOrder("Z�CHTE 2", false);
    checkOrder("Z�CHTE 2 3", false);
    checkOrder("Z�CHTE Flachwurz 2", false);
    checkOrder("Z�CHTE Flachwurz", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.SabotiereReader}.
   */
  @Test
  public void testSabotiereReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_SABOTAGE) + " SCHIFF");
    checkOrder("SABOTIERE", false);
    checkOrder("SABOTIERE SCHIFF ship", false);
  }

  /** Test method for {@link EresseaOrderParser.TokenBucket#mergeTempTokens(int)}. */
  @Test
  public void testMergeTempTokensWithoutTemps() {
    TokenBucket bucket = getParser().new TokenBucket();
    bucket.read(new StringReader("LEHREN 123 456 678"));
    bucket.mergeTempTokens(36);
    assertThat(bucket.size(), is(5));
    assertThat(bucket.get(0).getText(), is("LEHREN"));
    assertThat(bucket.get(1).getText(), is("123"));
    assertThat(bucket.get(2).getText(), is("456"));
    assertThat(bucket.get(3).getText(), is("678"));
    assertTrue(new OrderToken(OrderToken.TT_EOC).equalsAll(bucket.get(4)));
  }

  /** Test method for {@link EresseaOrderParser.TokenBucket#mergeTempTokens(int)}. */
  @Test
  public void testMergeTempTokensWithTemp() {
    TokenBucket bucket = getParser().new TokenBucket();
    bucket.read(new StringReader("LEHREN TEMP 123"));
    bucket.mergeTempTokens(36);
    assertThat(bucket.size(), is(3));
    assertThat(bucket.get(0).getText(), is("LEHREN"));
    assertThat(bucket.get(1).getText(), is("TEMP 123"));
  }

  /** Test method for {@link EresseaOrderParser.TokenBucket#mergeTempTokens(int)}. */
  @Test
  public void testMergeTempTokensWithTwoTemps() {
    TokenBucket bucket = getParser().new TokenBucket();
    bucket.read(new StringReader("LEHREN TEMP 123 TEMP 456"));
    bucket.mergeTempTokens(36);
    assertThat(bucket.size(), is(4));
    assertThat(bucket.get(0).getText(), is("LEHREN"));
    assertThat(bucket.get(1).getText(), is("TEMP 123"));
    assertThat(bucket.get(2).getText(), is("TEMP 456"));
  }

  /** Test method for {@link EresseaOrderParser.TokenBucket#mergeTempTokens(int)}. */
  @Test
  public void testMergeTempTokensWithMixed() {
    TokenBucket bucket = getParser().new TokenBucket();
    bucket.read(new StringReader("LEHREN TEMP 123 456 TEMP abc"));
    bucket.mergeTempTokens(36);
    assertThat(bucket.size(), is(5));
    assertThat(bucket.get(0).getText(), is("LEHREN"));
    assertThat(bucket.get(1).getText(), is("TEMP 123"));
    assertThat(bucket.get(2).getText(), is("456"));
    assertThat(bucket.get(3).getText(), is("TEMP abc"));
  }

  // /**
  // * Test method for {@link magellan.library.utils.Direction#getShortNames()}.
  // */
  // @Test
  // public void testGetShortNames() {
  // assertArrayEquals(new String[] { "nw", "no", "o", "so", "sw", "w" },
  // Direction.getShortNames()
  // .toArray());
  // }
  //
  // /**
  // * Test method for {@link magellan.library.utils.Direction#getLongNames()}.
  // */
  // @Test
  // public void testGetLongNames() {
  // assertArrayEquals(new String[] { "nordwesten", "nordosten", "osten", "s�dosten", "s�dwesten",
  // "westen" }, Direction.getLongNames().toArray());
  // }

  // /**
  // * Test method for {@link magellan.library.utils.Direction#toDirection(java.lang.String)}.
  // */
  // @Test
  // public void testToDirectionString() {
  // assertSame(Direction.NW, Direction.toDirection("NW"));
  // assertSame(Direction.NE, Direction.toDirection("NO"));
  // assertSame(Direction.E, Direction.toDirection("O"));
  // assertSame(Direction.SE, Direction.toDirection("SO"));
  // assertSame(Direction.SW, Direction.toDirection("SW"));
  // assertSame(Direction.W, Direction.toDirection("W"));
  // assertSame(Direction.NW, Direction.toDirection("NordWEsten"));
  // assertSame(Direction.INVALID, Direction.toDirection("Nach Hause"));
  // }

  // /**
  // * Test method for {@link magellan.library.utils.Direction#toString(boolean)}.
  // */
  // @Test
  // public void testToStringBoolean() {
  // assertEquals("NORDOSTEN", Direction.NE.toString(false));
  // assertEquals("S�DWESTEN", Direction.SW.toString(false));
  // assertEquals("NO", Direction.NE.toString(true));
  // }
  //
  // /**
  // * Test method for {@link magellan.library.utils.Direction#toString(int)}.
  // */
  // @Test
  // public void testToStringInt() {
  // assertEquals("NORDOSTEN", Direction.toString(Direction.DIR_NE));
  // }
  //
  // /**
  // * Test method for {@link magellan.library.utils.Direction#toString(int, boolean)}.
  // */
  // @Test
  // public void testToStringIntBoolean() {
  // assertEquals("NORDOSTEN", Direction.toString(Direction.DIR_NE, false));
  // assertEquals("NO", Direction.toString(Direction.DIR_NE, true));
  // }
  //
  // /**
  // * Test method for
  // * {@link magellan.library.utils.Direction#toString(magellan.library.CoordinateID)}.
  // */
  // @Test
  // public void testToStringCoordinateID() {
  // assertEquals("NORDWESTEN", Direction.toString(CoordinateID.create(-1, 1)));
  // }

  /**
   * Returns the value of parser.
   * 
   * @return Returns parser.
   */
  @Override
  protected AbstractOrderParser getParser() {
    return parser;
  }

  /**
   * Sets the value of parser.
   * 
   * @param parser The value for parser.
   */
  @Override
  protected void setParser(OrderParser parser) {
    this.parser = (EresseaOrderParser) parser;
  }

  /**
   * Returns the value of completer.
   * 
   * @return Returns completer.
   */
  @Override
  protected Completer getCompleter() {
    return completer;
  }

  /**
   * Sets the value of completer.
   * 
   * @param completer The value for completer.
   */
  @Override
  protected void setCompleter(AbstractOrderCompleter completer) {
    this.completer = (EresseaOrderCompleter) completer;
  }

}
