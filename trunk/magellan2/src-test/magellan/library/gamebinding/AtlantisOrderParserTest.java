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
package magellan.library.gamebinding;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import magellan.client.completion.AutoCompletion;
import magellan.library.Faction;
import magellan.library.Region;
import magellan.library.completion.OrderParser;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AtlantisOrderParserTest extends EresseaOrderParserTest {

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    MagellanTestWithResources.initResources();
  }

  /**
   * @throws java.lang.Exception
   */
  @Override
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

    setParser(new AtlantisOrderParser(data));
    completion = new AutoCompletion(context);
    setCompleter(new AtlantisOrderCompleter(data, completion));
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
    assertSame(36, getParser().getCommands().size());
    assertSame(36, getParser().getHandlers().size());
    assertTrue(getParser().getCommands().contains(AtlantisConstants.O_WORK));
    assertTrue(getParser().getCommands().contains(AtlantisConstants.O_DEMOLISH));
    assertTrue(!getParser().getCommands().contains(EresseaConstants.O_SABOTAGE));
  }

  protected void bareTest(String command) {
    checkOrder(command);
    checkOrder(command + " 1", false);
    checkOrder(command + " string", false);
  }

  protected void idTest(String command, boolean multi) {
    checkOrder(command + " " + 2);
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
    checkOrder(command + "string");
    checkOrder(command + "\"string\"");
    checkOrder(command + "string not", false);
    checkOrder(command + "\"string\" \"not\"", false);
    checkOrder(command, false);
  }

  protected void directionTest(String command) {
    checkOrder(command + " North");
    checkOrder(command + " West");
    checkOrder(command + " Mir");
    checkOrder(command + " Ydd");
    checkOrder(command + " South");
    checkOrder(command + " East");

    checkOrder(command, false);
    checkOrder(command + " North West", false);
    checkOrder(command + " 1", false);

  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.FormReader}.
   */
  @Test
  public void testFormReader() {
    // FORM u1
    idTest(getOrderTranslation(AtlantisConstants.O_FORM), false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.AcceptReader}.
   */
  @Test
  public void testAcceptReader() {
    // ACCEPT f1
    idTest(getOrderTranslation(AtlantisConstants.O_ACCEPT), false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.AddressReader}.
   */
  @Test
  public void testAddressReader() {
    // ADDRESS Address
    checkOrder(getOrderTranslation(AtlantisConstants.O_ADDRESS) + " " + "123@abc.com");
    checkOrder("ADDRESS \"a@foo.com\"");
    checkOrder("ADDRESS", false);
    checkOrder("ADDRESS 1 2", false);
    checkOrder("ADDRESS abc", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.AdmitReader}.
   */
  @Test
  public void testAdmitReader() {
    // ADMIT f1
    idTest(getOrderTranslation(AtlantisConstants.O_ADMIT), false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.AllyReader}.
   */
  @Test
  public void testAllyReader() {
    // ALLY f1 01
    checkOrder(getOrderTranslation(AtlantisConstants.O_ALLY) + " 3 0");
    checkOrder("ALLY 3 1");
    checkOrder("ALLY", false);
    checkOrder("ALLY 1", false);
    checkOrder("ALLY 3 NOT", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.BehindReader}.
   */
  @Test
  public void testBehindReader() {
    // BEHIND 01
    flagTest(getOrderTranslation(AtlantisConstants.O_BEHIND));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.CombatReader}.
   */
  @Test
  public void testCombatReader() {
    // COMBAT spell
    stringTest(getOrderTranslation(AtlantisConstants.O_COMBAT));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.DisplayReader}.
   */
  @Test
  public void testDisplayReader() {
    // DISPLAY (UNIT | BUILDING SHIP) string
    stringTest(getOrderTranslation(AtlantisConstants.O_DISPLAY) + " UNIT");
    stringTest(getOrderTranslation(AtlantisConstants.O_DISPLAY) + " BUILDING");
    stringTest(getOrderTranslation(AtlantisConstants.O_DISPLAY) + " SHIP");

    checkOrder("DISPLAY", false);
    checkOrder("DISPLAY UNIT", false);
    checkOrder("DISPLAY string", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.GuardReader}.
   */
  @Test
  public void testGuardReader() {
    // GUARD 01
    flagTest(getOrderTranslation(AtlantisConstants.O_GUARD));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.NameReader}.
   */
  @Test
  public void testNameReader() {
    // NAME (FACTION | UNIT | BUILDING | SHIP) name
    stringTest(getOrderTranslation(AtlantisConstants.O_NAME) + " "
        + getOrderTranslation(EresseaConstants.O_NOT));
    stringTest(getOrderTranslation(AtlantisConstants.O_NAME) + " UNIT");
    stringTest(getOrderTranslation(AtlantisConstants.O_NAME) + " BUILDING");
    stringTest(getOrderTranslation(AtlantisConstants.O_NAME) + " SHIP");
    stringTest(getOrderTranslation(AtlantisConstants.O_NAME) + " FACTION");

    checkOrder("DISPLAY", false);
    checkOrder("DISPLAY UNIT", false);
    checkOrder("DISPLAY string", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.PasswordReader}.
   */
  @Test
  public void testPasswordReader() {
    // PASSWORD password
    stringTest(getOrderTranslation(AtlantisConstants.O_PASSWORD));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.ReshowReader}.
   */
  @Test
  public void testReshowReader() {
    // RESHOW spell
    stringTest(getOrderTranslation(AtlantisConstants.O_RESHOW));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.FindReader}.
   */
  @Test
  public void testFindReader() {
    // FIND f1
    idTest(getOrderTranslation(AtlantisConstants.O_FIND), false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.BoardReader}.
   */
  @Test
  public void testBoardReader() {
    // BOARD s1
    idTest(getOrderTranslation(AtlantisConstants.O_BOARD), false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.EnterReader}.
   */
  @Test
  public void testEnterReader() {
    // ENTER b1
    idTest(getOrderTranslation(AtlantisConstants.O_ENTER), false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.LeaveReader}.
   */
  @Test
  public void testLeaveReader() {
    // LEAVE
    bareTest(getOrderTranslation(AtlantisConstants.O_LEAVE));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.PromoteReader}.
   */
  @Test
  public void testPromoteReader() {
    // PROMOTE u1
    idTest(getOrderTranslation(AtlantisConstants.O_PROMOTE), false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.AttackReader}.
   */
  @Override
  @Test
  public void testAttackReader() {
    // ATTACK (u1 | PEASANTS)
    idTest(getOrderTranslation(AtlantisConstants.O_ATTACK), false);
    checkOrder("ATTACK PEASANTS");
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.DemolishReader}.
   */
  @Test
  public void testDemolishReader() {
    // DEMOLISH
    bareTest(getOrderTranslation(AtlantisConstants.O_DEMOLISH));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.GiveReader}.
   */
  @Test
  public void testGiveReader() {
    // GIVE u1 1 item
    checkOrder(getOrderTranslation(AtlantisConstants.O_GIVE) + " " + " 3 5 wood");
    checkOrder("GIVE", false);
    checkOrder("GIVE 3", false);
    checkOrder("GIVE 3 5", false);
    checkOrder("GIVE 3 5 6", false);
    checkOrder("GIVE a b c", false);
    checkOrder("GIVE 1 5 wood 5", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.PayReader}.
   */
  @Test
  public void testPayReader() {
    // PAY u1 1
    checkOrder(getOrderTranslation(AtlantisConstants.O_PAY) + " 5 17");
    checkOrder("PAY", false);
    checkOrder("PAY 5", false);
    checkOrder("PAY 5 7 8", false);
    checkOrder("PAY a b", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.SinkReader}.
   */
  @Test
  public void testSinkReader() {
    // SINK
    bareTest(getOrderTranslation(AtlantisConstants.O_SINK));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.TransferReader}.
   */
  @Test
  public void testTransferReader() {
    // TRANSFER (u1 | PEASANTS) 1
    checkOrder(getOrderTranslation(AtlantisConstants.O_TRANSFER) + " 3 15");
    checkOrder("TRANSFER PEASANTS 5");
    checkOrder("TRANSFER", false);
    checkOrder("TRANSFER 5", false);
    checkOrder("TRANSFER 5 6 7", false);
    checkOrder("TRANSFER a b", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.TaxReader}.
   */
  @Test
  public void testTaxReader() {
    // TAX
    bareTest(getOrderTranslation(AtlantisConstants.O_TAX));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.RecruitReader}.
   */
  @Test
  public void testRecruitReader() {
    // RECRUIT 1
    checkOrder(getOrderTranslation(AtlantisConstants.O_RECRUIT) + " 999");
    checkOrder("RECRUIT", false);
    checkOrder("RECRUIT a", false);
    checkOrder("RECRUIT 1 2", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.QuitReader}.
   */
  @Test
  public void testQuitReader() {
    // QUIT password
    stringTest(getOrderTranslation(AtlantisConstants.O_QUIT));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.MoveReader}.
   */
  @Test
  public void testMoveReader() {
    // MOVE (N | W | M | S | W | Y)
    directionTest(getOrderTranslation(AtlantisConstants.O_MOVE));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.SailReader}.
   */
  @Test
  public void testSailReader() {
    // SAIL (N | W | M | S | W | Y)
    directionTest(getOrderTranslation(AtlantisConstants.O_SAIL));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.BuildReader}.
   */
  @Test
  public void testBuildReader() {
    // BUILD (BUILDING [b1]) | (SHIP [s1|type])
    checkOrder(getOrderTranslation(AtlantisConstants.O_BUILD) + " "
        + getOrderTranslation(EresseaConstants.O_NOT));
    checkOrder("BUILD", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.EntertainReader}.
   */
  @Test
  public void testEntertainReader() {
    // ENTERTAIN
    bareTest(getOrderTranslation(AtlantisConstants.O_ENTERTAIN));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.ProduceReader}.
   */
  @Test
  public void testProduceReader() {
    // PRODUCE item
    stringTest(getOrderTranslation(AtlantisConstants.O_PRODUCE));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.ResearchReader}.
   */
  @Test
  public void testResearchReader() {
    // RESEARCH [1]
    checkOrder(getOrderTranslation(AtlantisConstants.O_RESEARCH) + " 5");
    checkOrder("RESEARCH");
    checkOrder("RESEARCH 1 2", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.StudyReader}.
   */
  @Test
  public void testStudyReader() {
    // STUDY skill
    stringTest(getOrderTranslation(AtlantisConstants.O_STUDY));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.TeachReader}.
   */
  @Test
  public void testTeachReader() {
    // TEACH u1+
    idTest(getOrderTranslation(AtlantisConstants.O_TEACH), true);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.WorkReader}.
   */
  @Test
  public void testWorkReader() {
    // WORK
    bareTest(getOrderTranslation(AtlantisConstants.O_WORK));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.AtlantisOrderParser.CastReader}.
   */
  @Test
  public void testCastReader() {
    // CAST spell
    stringTest(getOrderTranslation(AtlantisConstants.O_CAST));
  }

}
