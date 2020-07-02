// class magellan.library.gamebinding.AtlantisOrderParserTest
// created on Apr 16, 2013
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
package magellan.library.gamebinding.atlantis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import magellan.client.completion.AutoCompletion;
import magellan.library.Faction;
import magellan.library.Region;
import magellan.library.completion.OrderParser;
import magellan.library.gamebinding.AbstractOrderCompleter;
import magellan.library.gamebinding.AbstractOrderParserTestUtil;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.OrderHandler;
import magellan.library.utils.OrderToken;
import magellan.library.utils.logging.Logger;
import magellan.test.GameDataBuilder;

public class AtlantisOrderParserTest extends AbstractOrderParserTestUtil {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    setLocale(EN_LOCALE);
    Logger.setLevel(Logger.WARN);
    initResources();
  }

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    builder = new GameDataBuilder();
    builder.setGameName("Atlantis");
    data = builder.createSimpleGameData();
    data.base = 10;

    Region region = data.getRegions().iterator().next();
    Faction faction = data.getFactions().iterator().next();
    // builder.addBuilding(data, region, "burg", "Burg", "groﬂe Burg", 200);
    // builder.addShip(data, region, "ship", "Einbaum", "ein Boot", 50);
    // builder.addUnit(data, "zwei", "Zweite", faction, region);

    setParser(parser = new AtlantisOrderParser(data));
    completion = new AutoCompletion(context.getProperties(), context.getEventDispatcher());
    setCompleter(completer = new AtlantisOrderCompleter(data, completion));
    completer.setParser(parser);
  }

  private AtlantisOrderParser parser;
  private AtlantisOrderCompleter completer;

  /**
   * Returns the value of parser.
   *
   * @return Returns parser.
   */
  @Override
  protected AtlantisOrderParser getParser() {
    return parser;
  }

  /**
   * Sets the value of parser.
   *
   * @param parser The value for parser.
   */
  @Override
  protected void setParser(OrderParser parser) {
    this.parser = (AtlantisOrderParser) parser;
  }

  /**
   * Returns the value of completer.
   *
   * @return Returns completer.
   */
  @Override
  protected AtlantisOrderCompleter getCompleter() {
    return completer;
  }

  /**
   * Sets the value of completer.
   *
   * @param completer The value for completer.
   */
  @Override
  protected void setCompleter(AbstractOrderCompleter completer) {
    this.completer = (AtlantisOrderCompleter) completer;
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser#initCommands()}.
   */
  @Override
  @Test
  public void testInitCommands() {
    assertSame(37, getParser().getCommands().size());
    assertSame(37, getParser().getHandlers().size());
    assertTrue(getParser().getCommands().contains(AtlantisConstants.OC_WORK));
    assertTrue(getParser().getCommands().contains(AtlantisConstants.OC_DEMOLISH));
    assertTrue(!getParser().getCommands().contains(EresseaConstants.OC_SABOTAGE));
  }

  protected void bareTest(String command) {
    checkOrder(command);
    checkOrder(command + " 1", false);
    checkOrder(command + " string", false);
  }

  protected void idTest(String command, boolean multi) {
    checkOrder(command + "  2");
    checkOrder(command, false);
    checkOrder(command + " 1 2", multi);
    checkOrder(command + " 1 2 3", multi);
    checkOrder(command + " NOT", false);
  }

  protected void flagTest(String command) {
    checkOrder(command + " 0");
    checkOrder(command + " 1");
    checkOrder(command, false);
    checkOrder(command + " 1 2", false);
    checkOrder(command + " NOT", false);
  }

  protected void stringTest(String command) {
    // checkOrder(command + " string"); // legal, but not preferred
    checkOrder(command + " \"string\"");
    checkOrder(command + " string not", false);
    checkOrder(command + " \"string\" \"not\"", false);
    checkOrder(command, false);
  }

  protected void directionTest(String command) {
    checkOrder(command + " North");
    checkOrder(command + " West");
    checkOrder(command + " Mir");
    checkOrder(command + " Ydd");
    checkOrder(command + " South");
    checkOrder(command + " East");

    checkOrder(command + " N");
    checkOrder(command + " W");
    checkOrder(command + " M");
    checkOrder(command + " Y");
    checkOrder(command + " S");
    checkOrder(command + " E");

    checkOrder(command, false);
    checkOrder(command + " North West", false);
    checkOrder(command + " 1", false);

  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.FormReader}.
   */
  @Test
  public void testFormReader() {
    // FORM u1
    testLong(getOrderTranslation(AtlantisConstants.OC_FORM), false);
    idTest(getOrderTranslation(AtlantisConstants.OC_FORM), false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.EndReader}.
   */
  @Test
  public void testEndReader() {
    // FORM u1
    testLong(getOrderTranslation(AtlantisConstants.OC_END), false);
    bareTest(getOrderTranslation(AtlantisConstants.OC_END));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.AcceptReader}.
   */
  @Test
  public void testAcceptReader() {
    // ACCEPT f1
    testLong(getOrderTranslation(AtlantisConstants.OC_ACCEPT), false);
    idTest(getOrderTranslation(AtlantisConstants.OC_ACCEPT), false);
    checkOrder("ACCEPT NEW 123", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.AddressReader} .
   */
  @Test
  public void testAddressReader() {
    // ADDRESS Address
    testLong(getOrderTranslation(AtlantisConstants.OC_ADDRESS) + " " + "123@abc.com", false);
    checkOrder(getOrderTranslation(AtlantisConstants.OC_ADDRESS) + " " + "\"123@abc.com\"");
    checkOrder("ADDRESS \"a@foo.com\"");
    checkOrder("ADDRESS", false);
    checkOrder("ADDRESS moon@earth.org", false);
    checkOrder("ADDRESS 1 2", false);
    // we could check for a real email address here...
    // checkOrder("ADDRESS abc", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.AdmitReader}.
   */
  @Test
  public void testAdmitReader() {
    // ADMIT f1
    testLong(getOrderTranslation(AtlantisConstants.OC_ADMIT), false);
    idTest(getOrderTranslation(AtlantisConstants.OC_ADMIT), false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.AllyReader}.
   */
  @Test
  public void testAllyReader() {
    // ALLY f1 01
    testLong(getOrderTranslation(AtlantisConstants.OC_ALLY) + " 3 0", false);
    checkOrder(getOrderTranslation(AtlantisConstants.OC_ALLY) + " 3 0");
    checkOrder("ALLY 3 1");
    checkOrder("ALLY", false);
    checkOrder("ALLY 1", false);
    checkOrder("ALLY 3 NOT", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.BehindReader}.
   */
  @Test
  public void testBehindReader() {
    // BEHIND 01
    testLong(getOrderTranslation(AtlantisConstants.OC_BEHIND), false);
    flagTest(getOrderTranslation(AtlantisConstants.OC_BEHIND));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.CombatReader}.
   */
  @Test
  public void testCombatReader() {
    // COMBAT spell
    testLong(getOrderTranslation(AtlantisConstants.OC_COMBAT), false);
    stringTest(getOrderTranslation(AtlantisConstants.OC_COMBAT));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.DisplayReader} .
   */
  @Test
  public void testDisplayReader() {
    // DISPLAY (UNIT | BUILDING SHIP) string
    testLong(getOrderTranslation(AtlantisConstants.OC_DISPLAY) + " UNIT \"bla\"", false);
    stringTest(getOrderTranslation(AtlantisConstants.OC_DISPLAY) + " UNIT");
    stringTest(getOrderTranslation(AtlantisConstants.OC_DISPLAY) + " BUILDING");
    stringTest(getOrderTranslation(AtlantisConstants.OC_DISPLAY) + " SHIP");

    // actually, this is legal, but we require it to be included in quotes
    checkOrder("DISPLAY UNIT 'name'", false);
    checkOrder("DISPLAY UNIT \"'name'\"", true);

    checkOrder("DISPLAY", false);
    checkOrder("DISPLAY UNIT", false);
    checkOrder("DISPLAY string", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.GuardReader}.
   */
  @Test
  public void testGuardReader() {
    // GUARD 01
    testLong(getOrderTranslation(AtlantisConstants.OC_GUARD), false);
    flagTest(getOrderTranslation(AtlantisConstants.OC_GUARD));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.NameReader}.
   */
  @Test
  public void testNameReader() {
    // NAME (FACTION | UNIT | BUILDING | SHIP) name
    testLong(getOrderTranslation(AtlantisConstants.OC_NAME) + " "
        + getOrderTranslation(AtlantisConstants.OC_UNIT) + " Name", false);
    stringTest(getOrderTranslation(AtlantisConstants.OC_NAME) + " UNIT");
    stringTest(getOrderTranslation(AtlantisConstants.OC_NAME) + " BUILDING");
    stringTest(getOrderTranslation(AtlantisConstants.OC_NAME) + " SHIP");
    stringTest(getOrderTranslation(AtlantisConstants.OC_NAME) + " FACTION");

    checkOrder("NAME", false);
    checkOrder("NAME UNIT", false);
    checkOrder("NAME string", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.PasswordReader}.
   */
  @Test
  public void testPasswordReader() {
    // PASSWORD password
    testLong(getOrderTranslation(AtlantisConstants.OC_PASSWORD), false);
    stringTest(getOrderTranslation(AtlantisConstants.OC_PASSWORD));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.ReshowReader}.
   */
  @Test
  public void testReshowReader() {
    // RESHOW spell
    testLong(getOrderTranslation(AtlantisConstants.OC_RESHOW), false);
    stringTest(getOrderTranslation(AtlantisConstants.OC_RESHOW));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.FindReader}.
   */
  @Test
  public void testFindReader() {
    // FIND f1
    testLong(getOrderTranslation(AtlantisConstants.OC_FIND), false);
    idTest(getOrderTranslation(AtlantisConstants.OC_FIND), false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.BoardReader}.
   */
  @Test
  public void testBoardReader() {
    // BOARD s1
    testLong(getOrderTranslation(AtlantisConstants.OC_BOARD), false);
    idTest(getOrderTranslation(AtlantisConstants.OC_BOARD), false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.EnterReader}.
   */
  @Test
  public void testEnterReader() {
    // ENTER b1
    testLong(getOrderTranslation(AtlantisConstants.OC_ENTER), false);
    idTest(getOrderTranslation(AtlantisConstants.OC_ENTER), false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.LeaveReader}.
   */
  @Test
  public void testLeaveReader() {
    // LEAVE
    testLong(getOrderTranslation(AtlantisConstants.OC_LEAVE), false);
    bareTest(getOrderTranslation(AtlantisConstants.OC_LEAVE));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.PromoteReader} .
   */
  @Test
  public void testPromoteReader() {
    // PROMOTE u1
    testLong(getOrderTranslation(AtlantisConstants.OC_PROMOTE), false);
    idTest(getOrderTranslation(AtlantisConstants.OC_PROMOTE), false);
    checkOrder("PROMOTE NEW 123", true);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.AttackReader}.
   */
  @Test
  public void testAttackReader() {
    // ATTACK (u1 | PEASANTS)
    testLong(getOrderTranslation(AtlantisConstants.OC_ATTACK), false);
    idTest(getOrderTranslation(AtlantisConstants.OC_ATTACK), false);
    checkOrder("ATTACK PEASANTS");
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.DemolishReader}.
   */
  @Test
  public void testDemolishReader() {
    // DEMOLISH
    testLong(getOrderTranslation(AtlantisConstants.OC_DEMOLISH), false);
    bareTest(getOrderTranslation(AtlantisConstants.OC_DEMOLISH));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.GiveReader}.
   */
  @Test
  public void testGiveReader() {
    // GIVE u1 1 item
    testLong(getOrderTranslation(AtlantisConstants.OC_GIVE) + " " + " 3 5 wood", false);
    checkOrder(getOrderTranslation(AtlantisConstants.OC_GIVE) + " " + " 3 5 wood");
    checkOrder("GIVE 1 2 silver", false); // give silver not allowed
    checkOrder("GIVE", false);
    checkOrder("GIVE 3", false);
    checkOrder("GIVE 3 5", false);
    checkOrder("GIVE 3 5 6", false);
    checkOrder("GIVE a b c", false);
    checkOrder("GIVE 1 5 wood 5", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.GiveReader}
   */
  @Test
  public void testPayReader() {
    // PAY u1 1
    testLong(getOrderTranslation(AtlantisConstants.OC_PAY) + " 5 17", false);
    checkOrder(getOrderTranslation(AtlantisConstants.OC_PAY) + " 5 17");
    checkOrder("PAY NEW 5 999");
    checkOrder("PAY", false);
    checkOrder("PAY", false);
    checkOrder("PAY 5", false);
    checkOrder("PAY 5 7 8", false);
    checkOrder("PAY a b", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.SinkReader}.
   */
  @Test
  public void testSinkReader() {
    // SINK
    testLong(getOrderTranslation(AtlantisConstants.OC_SINK), false);
    bareTest(getOrderTranslation(AtlantisConstants.OC_SINK));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.GiveReader}.
   */
  @Test
  public void testTransferReader() {
    // TRANSFER (u1 | PEASANTS) 1
    testLong(getOrderTranslation(AtlantisConstants.OC_TRANSFER) + " 3 15", false);
    checkOrder(getOrderTranslation(AtlantisConstants.OC_TRANSFER) + " 3 15");
    checkOrder("TRANSFER PEASANTS 5");
    checkOrder("TRANSFER", false);
    checkOrder("TRANSFER 5", false);
    checkOrder("TRANSFER 5 6 7", false);
    checkOrder("TRANSFER a b", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.TaxReader}.
   */
  @Test
  public void testTaxReader() {
    // TAX
    testLong(getOrderTranslation(AtlantisConstants.OC_TAX), false);
    bareTest(getOrderTranslation(AtlantisConstants.OC_TAX));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.RecruitReader} .
   */
  @Test
  public void testRecruitReader() {
    // RECRUIT 1
    testLong(getOrderTranslation(AtlantisConstants.OC_RECRUIT) + " 999", false);
    checkOrder(getOrderTranslation(AtlantisConstants.OC_RECRUIT) + " 999");
    checkOrder("RECRUIT", false);
    checkOrder("RECRUIT a", false);
    checkOrder("RECRUIT 1 2", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.QuitReader}.
   */
  @Test
  public void testQuitReader() {
    // QUIT password
    testLong(getOrderTranslation(AtlantisConstants.OC_QUIT), false);
    stringTest(getOrderTranslation(AtlantisConstants.OC_QUIT));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.MoveReader}.
   */
  @Test
  public void testMoveReader() {
    // MOVE (N | W | M | S | W | Y)
    testLong(getOrderTranslation(AtlantisConstants.OC_MOVE), true);
    directionTest(getOrderTranslation(AtlantisConstants.OC_MOVE));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.SailReader}.
   */
  @Test
  public void testSailReader() {
    // SAIL (N | W | M | S | W | Y)
    testLong(getOrderTranslation(AtlantisConstants.OC_SAIL), true);
    directionTest(getOrderTranslation(AtlantisConstants.OC_SAIL));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.BuildReader}.
   */
  @Test
  public void testBuildReader() {
    // BUILD (BUILDING [b1]) | (SHIP s1) | (shiptype)
    testLong(getOrderTranslation(AtlantisConstants.OC_BUILD) + " "
        + getOrderTranslation(EresseaConstants.OC_BUILDING), true);
    idTest(getOrderTranslation(AtlantisConstants.OC_BUILD) + " "
        + getOrderTranslation(EresseaConstants.OC_BUILDING), false);
    idTest(getOrderTranslation(AtlantisConstants.OC_BUILD) + " "
        + getOrderTranslation(EresseaConstants.OC_SHIP), false);

    checkOrder("BUILD Longboat");
    checkOrder("BUILD Clipper");
    checkOrder("BUILD Galleon");

    checkOrder("BUILD Dragonship", false);

    checkOrder("BUILD", false);
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.EntertainReader}.
   */
  @Test
  public void testEntertainReader() {
    // ENTERTAIN
    testLong(getOrderTranslation(AtlantisConstants.OC_ENTERTAIN), true);
    bareTest(getOrderTranslation(AtlantisConstants.OC_ENTERTAIN));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.ProduceReader} .
   */
  @Test
  public void testProduceReader() {
    // PRODUCE item
    testLong(getOrderTranslation(AtlantisConstants.OC_PRODUCE), true);
    checkOrder("PRODUCE wood");
    checkOrder("PRODUCE \"wood\"");
    checkOrder("PRODUCE wood not", false);
    checkOrder("PRODUCE \"wood\" \"not\"", false);
    checkOrder("PRODUCE gnargls", false);
    checkOrder("PRODUCE", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.ResearchReader}.
   */
  @Test
  public void testResearchReader() {
    // RESEARCH [1]
    testLong(getOrderTranslation(AtlantisConstants.OC_RESEARCH) + " 5", true);
    checkOrder(getOrderTranslation(AtlantisConstants.OC_RESEARCH) + " 5");
    checkOrder("RESEARCH");
    checkOrder("RESEARCH 1 2", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.StudyReader}.
   */
  @Test
  public void testStudyReader() {
    // STUDY skill
    testLong(getOrderTranslation(AtlantisConstants.OC_STUDY), true);
    stringTest(getOrderTranslation(AtlantisConstants.OC_STUDY));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.TeachReader}.
   */
  @Test
  public void testTeachReader() {
    // TEACH u1+
    idTest(getOrderTranslation(AtlantisConstants.OC_TEACH), true);
    checkOrder("TEACH NEW 123", true);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.WorkReader}.
   */
  @Test
  public void testWorkReader() {
    // WORK
    testLong(getOrderTranslation(AtlantisConstants.OC_WORK), true);
    bareTest(getOrderTranslation(AtlantisConstants.OC_WORK));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.atlantis.AtlantisOrderParser.CastReader}.
   */
  @Test
  public void testCastReader() {
    // CAST spell
    testLong(getOrderTranslation(AtlantisConstants.OC_CAST), true);
    stringTest(getOrderTranslation(AtlantisConstants.OC_CAST));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser#read(java.io.Reader)}.
   */
  @Override
  @Test
  public void testRead() {
    checkOrder("AR", false);
    checkOrder(""); // FIXME ???!!!
    checkOrder("A", false);
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
    assertSame(list.size(), 0);
    list = getParser().getHandlers(new OrderToken("wor"));
    assertSame(list.size(), 0);
    list = getParser().getHandlers(new OrderToken("aga"));
    assertSame(list.size(), 0);
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#unexpected(magellan.library.utils.OrderToken)}
   * .
   */
  @Override
  @SuppressWarnings("deprecation")
  @Test
  public void testUnexpected() {
    assertTrue(getParser().getErrorMessage() == null);
    getParser().setErrMsg("error");
    assertTrue(getParser().getErrorMessage().equals("error"));
    getParser().setErrMsg(null);
    assertTrue(getParser().getErrorMessage() == null);
    getParser().read(new StringReader("WORK"));
    assertTrue(getParser().getErrorMessage() == null);
    getParser().read(new StringReader("WORK 2"));
    assertEquals(getParser().getErrorMessage(),
        "Unexpected token 2: Undefined(5, 6), not followed by Space");
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser#isID(java.lang.String)}.
   */
  /**
   * @see magellan.library.gamebinding.AbstractOrderParserTestUtil#testIsID()
   */
  @Override
  @Test
  public void testIsID() {
    assertTrue(getParser().isID("NEW 123"));
    assertFalse(getParser().isID("TEMP 123"));
    assertFalse(getParser().isID("NEW 123", false));
    assertFalse(getParser().isID("NEW abc"));
    assertTrue(getParser().isID("12"));
    assertFalse(getParser().isID("abc"));
    assertFalse(getParser().isID("2ac"));
    assertTrue(getParser().isID("1234567"));
    assertFalse(getParser().isID("1,3"));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isTempID(java.lang.String)}.
   */
  @Override
  @Test
  public void testIsTempID() {
    assertTrue(getParser().isTempID("NEW 1"));
    assertFalse(getParser().isTempID("NEW abc"));
    assertFalse(getParser().isTempID("TEMP 1"));
    assertFalse(getParser().isTempID("1,3"));
    assertFalse(getParser().isTempID("abc"));
    assertFalse(getParser().isTempID(" NEW 123 "));
    assertFalse(getParser().isTempID(" NEW NEWtemp"));
  }

}
