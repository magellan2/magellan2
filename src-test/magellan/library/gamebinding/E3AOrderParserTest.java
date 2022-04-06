// class magellan.library.gamebinding.E3AOrderParserTest
// created on Jun 12, 2012
//
// Copyright 2003-2012 by magellan project team
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

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import magellan.client.completion.AutoCompletion;
import magellan.library.Faction;
import magellan.library.Region;
import magellan.library.gamebinding.EresseaOrderParser.ArbeiteReader;
import magellan.library.gamebinding.EresseaOrderParser.AttackReader;
import magellan.library.gamebinding.e3a.E3AOrderCompleter;
import magellan.library.gamebinding.e3a.E3AOrderParser;
import magellan.library.utils.OrderToken;
import magellan.test.GameDataBuilder;

/**
 * Tests for E3 order parser. inherits from EresseaOrderParserTest to ensure functionality of E2
 * where required.
 */
public class E3AOrderParserTest extends EresseaOrderParserTest {

  /**
   * @throws java.lang.Exception
   */
  @Override
  @Before
  public void setUp() throws Exception {
    builder = new GameDataBuilder();
    builder.setGameName("E3");
    data = builder.createSimpleGameData();

    Region region = data.getRegions().iterator().next();
    Faction faction = data.getFactions().iterator().next();
    builder.addBuilding(data, region, "burg", "Burg", "große Burg", 200);
    builder.addShip(data, region, "ship", "Einbaum", "ein Boot", 50);
    builder.addUnit(data, "zwei", "Zweite", faction, region);

    E3AOrderParser parser = new E3AOrderParser(data);
    setParser(parser);
    completion = new AutoCompletion(context.getProperties(), context.getEventDispatcher());
    E3AOrderCompleter completer = new E3AOrderCompleter(data, completion);
    completer.setParser(parser);
    setCompleter(completer);
  }

  @Override
  @Test
  public void testEresseaOrderParserGameDataEresseaOrderCompleter() {
    /* nop */
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser#initCommands()}.
   */
  @Override
  @Test
  public void testInitCommands() {
    assertSame(53, getParser().getCommands().size());
    assertSame(53, getParser().getHandlers().size());
    assertTrue(getParser().getCommands().contains(EresseaConstants.OC_WORK));
    assertTrue(getParser().getCommands().contains(EresseaConstants.OC_DESTROY));
    assertTrue(!getParser().getCommands().contains(EresseaConstants.OC_SABOTAGE));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.BeklaueReader}.
   */
  @Override
  @Test
  public void testBeklaueReader() {
    // no such order in E3
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.ForscheReader}.
   */
  @Override
  @Test
  public void testForscheReader() {
    // no such order in E3
  }

  @Override
  public void testSupplyReader() {
    // no such order in E3
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.HelfeReader}.
   */
  @Override
  @Test
  public void testHelfeReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_HELP) + " 123 "
        + getOrderTranslation(EresseaConstants.OC_ALL));
    checkOrder("HELFE 123 ALLES NICHT");
    checkOrder("HELFE 123 GIB");
    checkOrder("HELFE 123 GIB NICHT");
    checkOrder("HELFE 123 BEWACHE");
    checkOrder("HELFE 123 SILBER");
    checkOrder("HELFE 123 PARTEITARNUNG", false);
    checkOrder("HELFE 123 bla", false);
    checkOrder("HELFE abcde GIB", false);
    checkOrder("HELFE 123 GIB BLA", false);
    checkOrder("HELFE 123 456", false);
    checkOrder("HELFE TEMP 456", false);
    checkOrder("HELFE 123", false);
    checkOrder("HELFE 123 KÄMPFE", false); // not allowed in E3
  }

  @Override
  public void testKaufeReader() {
    // no such order in E3
  }

  @Override
  public void testLehreReader() {
    // no such order in E3
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.MacheReader}.
   */
  @Override
  @Test
  public void testMacheReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_MAKE) + " TEMP 123");
    checkOrder("MACHE BURG");
    checkOrder("MACHE 2 BURG");
    checkOrder("MACHE BURG 123");
    checkOrder("MACHE 3 BURG abc");
    checkOrder("MACHE Sägewerk");
    checkOrder("MACHE 2 Sägewerk");
    checkOrder("MACHE Einbaum");
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
    checkOrder("MACHE Pferd Pferd", false);
    checkOrder("MACHE 2 Schwert");
    checkOrder("MACHE 1 Pferd");
    checkOrder("MACHE", false); // actually, this is correct, but dangerous
    checkOrder("MACHE \"Sägewerk\"", false); // well...
    checkOrder("MACHE \"Pferd\"");
    checkOrder("MACHE 2 Schwert");
    checkOrder("MACHE 2 \"Rostiger Zweihänder\"");
    checkOrder("MACHE 1 Pferd");
    checkOrder("MACHE", false); // actually, this is correct, but dangerous
    checkOrder("MACHE BURGG", false);
    checkOrder("MACHE a BURG", false);
    checkOrder("MACHE a BURG 123", false);
    checkOrder("MACHE Einbaum 123", false);
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
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.RekrutiereReader}.
   */
  @Override
  @Test
  public void testRekrutiereReader() {
    checkOrder(getOrderTranslation(EresseaConstants.OC_RECRUIT) + " 1");
    checkOrder("REKRUTIERE 5");
    checkOrder("REKRUTIERE 0", true); // TODO should we return false here?
    checkOrder("REKRUTIERE 1 Zwerg", true); // for E2, this is an error
    checkOrder("REKRUTIERE", false);
    checkOrder("REKRUTIERE 1 2", false);
  }

  @Override
  public void testSpioniereReader() {
    // no such order in E3
  }

  @Override
  public void testTarneReader() {
    // no such order in E3
  }

  @Override
  public void testTreibeReader() {
    // no such order in E3
  }

  @Override
  public void testUnterhalteReader() {
    // no such order in E3
  }

  @Override
  public void testVerkaufeReader() {
    // no such order in E3
  }

  @Override
  public void testSabotiereReader() {
    // no such order in E3
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#getHandlers(magellan.library.utils.OrderToken)}
   * .
   */
  @Override
  @Test
  public void testGetHandlers() {
    List<OrderHandler> list = getParser().getHandlers(new OrderToken("a"));
    assertTrue(list != null);
    assertTrue(list.size() == 3);
    for (OrderHandler reader : list) {
      assertTrue(reader.getClass().equals(ArbeiteReader.class)
          || reader.getClass().equals(AttackReader.class)
          || reader.getClass().getName().contains("AllianzReader"));
    }
    list = getParser().getHandlers(new OrderToken("arbei"));
    assertTrue(list != null);
    assertTrue(list.size() == 1);
    list = getParser().getHandlers(new OrderToken("aga"));
    assertTrue(list != null);
    assertTrue(list.size() == 0);
  }

  @Override
  public void testInfinitive() {
    // already tested in Eressea
  }

  @Test
  public void testDrachensgrab() throws Exception {
    builder = new GameDataBuilder();
    builder.setGameName("Drachensgrab");
    data = builder.createSimpleGameData();

    E3AOrderParser parser = new E3AOrderParser(data);
    setParser(parser);
    checkOrder("UNTERHALTE");
  }

}
