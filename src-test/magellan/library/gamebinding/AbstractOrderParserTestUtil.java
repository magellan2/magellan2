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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Test;

import magellan.client.completion.AutoCompletion;
import magellan.library.GameData;
import magellan.library.Order;
import magellan.library.StringID;
import magellan.library.completion.Completer;
import magellan.library.completion.OrderParser;
import magellan.library.gamebinding.EresseaOrderParser.ArbeiteReader;
import magellan.library.gamebinding.EresseaOrderParser.AttackReader;
import magellan.library.utils.OrderToken;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestUtil;
import magellan.test.MagellanTestWithResources;

public abstract class AbstractOrderParserTestUtil extends MagellanTestWithResources {

  protected static boolean DO_KNOWN_FAILURES = MagellanTestUtil.isInternalTesting(); 
  protected GameData data;
  protected AutoCompletion completion;
  protected GameDataBuilder builder;

  public AbstractOrderParserTestUtil() {
    super();
  }

  protected String getOrderTranslation(StringID orderId) {
    return data.getGameSpecificStuff().getOrderChanger().getOrderO(getLocale(), orderId).getText();
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser#initCommands()}.
   */
  @Test
  public void testInitCommands() {
    assertSame(62, getParser().getCommands().size());
    assertSame(62, getParser().getHandlers().size());
    assertTrue(getParser().getCommands().contains(EresseaConstants.OC_WORK));
    assertTrue(getParser().getCommands().contains(EresseaConstants.OC_DESTROY));
    assertTrue(getParser().getCommands().contains(EresseaConstants.OC_SABOTAGE));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.AbstractOrderParser#addCommand(StringID, OrderHandler)}.
   */
  @Test
  public void testAddCommand() {
    OrderHandler fooHandler = new OrderHandler(getParser()) {
      @Override
      protected boolean readIt(OrderToken token) {
        return false;
      }
    };
    assertFalse(getParser().getCommands().contains(StringID.create("foo")));
    assertFalse(getParser().getHandlers().contains(fooHandler));
    assertTrue(getParser().getHandlers(new OrderToken("foo")).size() == 0);
    getParser().addCommand(StringID.create("foo"), fooHandler);
    assertTrue(getParser().getCommands().contains(StringID.create("foo")));
    assertTrue(getParser().getHandlers().contains(fooHandler));

    assertTrue(getParser().getHandlers(new OrderToken("foo")).contains(fooHandler));
    assertTrue(getParser().getHandlers(new OrderToken("foo")).size() == 1);
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.AbstractOrderParser#removeCommand(StringID)}.
   */
  @Test
  public void testRemoveCommand() {
    OrderHandler fooHandler = new OrderHandler(getParser()) {
      @Override
      protected boolean readIt(OrderToken token) {
        return false;
      }
    };
    getParser().addCommand(StringID.create("foo"), fooHandler);
    assertTrue(getParser().getCommands().contains(StringID.create("foo")));
    assertTrue(getParser().getHandlers().contains(fooHandler));

    assertTrue(getParser().getHandlers(new OrderToken("foo")).contains(fooHandler));
    assertTrue(getParser().getHandlers(new OrderToken("foo")).size() == 1);
    getParser().removeCommand(StringID.create("foo"));
    assertFalse(getParser().getCommands().contains(StringID.create("foo")));
    assertFalse(getParser().getHandlers().contains(fooHandler));
    assertTrue(getParser().getHandlers(new OrderToken("foo")).size() == 0);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser#getNextToken()}.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testGetNextToken() {
    assertTrue(getParser().getLastToken() == null);
    assertFalse(getParser().hasNextToken());
    assertEquals(getParser().getTokenIndex(), 0);

    getParser().read(new StringReader("123 abc"));

    assertTrue(getParser().hasNextToken());
    assertTrue(equals(getParser().getLastToken(), new OrderToken("123", 0, 3, OrderToken.TT_UNDEF,
        true)));
    assertTrue(getParser().hasNextToken());
    assertEquals(getParser().getTokenIndex(), 1);

    OrderToken token = getParser().getNextToken();

    assertTrue(token.getText().equals("abc"));
    assertTrue(getParser().hasNextToken());
    assertEquals(getParser().getLastToken(), token);
    assertEquals(getParser().getTokenIndex(), 2);

    assertTrue(equals(getParser().getNextToken(), new OrderToken(OrderToken.TT_EOC)));
    assertFalse(getParser().hasNextToken());
    assertEquals(getParser().getTokenIndex(), 3);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser#getNextToken()}.
   */
  @Test(expected = NullPointerException.class)
  public void testGetNextTokenNull() {
    assertTrue(getParser().getNextToken() != null);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser#read(java.io.Reader)}.
   */
  @Test
  public void testRead() {
    checkOrder("ARB");
    checkOrder(""); // FIXME ???!!!
    checkOrder("A", false);
  }

  protected Order checkOrder(String string) {
    return checkOrder(string, true);
  }

  protected Order checkOrder(String string, boolean result) {
    Order order = getParser().parse(string, getLocale());
    assertEquals("checking " + string, result, order.isValid());
    return order;
  }

  protected void testLong(String string, boolean isLong) {
    Order order = getParser().parse(string, getLocale());
    assertEquals("long order " + string, isLong, order.isLong());
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#getHandlers(magellan.library.utils.OrderToken)}
   * .
   */
  @Test
  public void testGetHandlers() {
    List<OrderHandler> list = getParser().getHandlers(new OrderToken("a"));
    assertTrue(list != null);
    assertTrue(list.size() == 2);
    for (OrderHandler handler : list) {
      assertTrue(handler.getClass().equals(AttackReader.class)
          || handler.getClass().equals(ArbeiteReader.class));
    }
    list = getParser().getHandlers(new OrderToken("arbei"));
    assertTrue(list != null);
    assertTrue(list.size() == 1);
    list = getParser().getHandlers(new OrderToken("aga"));
    assertTrue(list != null);
    assertTrue(list.size() == 0);
  }

  /**
   * Null token should be matched by no handler
   */
  @Test(expected = NullPointerException.class)
  public void testGetHandlersNull() {
    getParser().getHandlers(null);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser#readDescription()}.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testReadDescription() {
    getParser().read(new StringReader("a \"abc\""));
    assertNotNull(getParser().readDescription());
    getParser().read(new StringReader("\"abc\""));
    assertEquals(null, getParser().readDescription());
    getParser().read(new StringReader("a \"\""));
    assertEquals("", getParser().readDescription());
    getParser().read(new StringReader("a \"\" a"));
    assertEquals(null, getParser().readDescription());
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#readDescription(boolean)}.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testReadDescriptionBoolean() {
    getParser().read(new StringReader("a \"abc\""));
    assertNotNull(getParser().readDescription(true));
    getParser().read(new StringReader("\"abc\""));
    assertEquals(null, getParser().readDescription(true));
    getParser().read(new StringReader("a \"\""));
    assertNotNull(getParser().readDescription(true));
    getParser().read(new StringReader("a \"\""));
    assertEquals(null, getParser().readDescription(false));
    getParser().read(new StringReader("a \"\" a"));
    assertEquals(null, getParser().readDescription(true));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#readDescription(magellan.library.utils.OrderToken, boolean)}
   * .
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testReadDescriptionOrderTokenBoolean() {
    getParser().read(new StringReader("abc"));
    OrderToken token = getParser().getLastToken();
    assertEquals(null, getParser().readDescription(token, true));
    getParser().read(new StringReader("abc"));
    token = getParser().getLastToken();
    assertEquals(null, getParser().readDescription(token, false));
    getParser().read(new StringReader("\"abc\" \"abc\""));
    token = getParser().getLastToken();
    assertEquals(null, getParser().readDescription(token, false));
    getParser().read(new StringReader(""));
    token = getParser().getLastToken();
    assertEquals(null, getParser().readDescription(token, true));
    getParser().read(new StringReader("abc ; abc"));
    token = getParser().getLastToken();
    assertEquals(null, getParser().readDescription(token, true));
    getParser().read(new StringReader("abc ; abc"));
    token = getParser().getLastToken();
    assertEquals(null, getParser().readDescription(token, false));
    getParser().read(new StringReader("\"abc\""));
    token = getParser().getLastToken();
    assertNotNull(getParser().readDescription(token, true));
    getParser().read(new StringReader("\"abc\""));
    token = getParser().getLastToken();
    assertNotNull(getParser().readDescription(token, false));
    getParser().read(new StringReader("\"abc\"; 123"));
    token = getParser().getLastToken();
    assertNotNull(getParser().readDescription(token, true));
    getParser().read(new StringReader("\"abc\"; 123"));
    token = getParser().getLastToken();
    assertNotNull(getParser().readDescription(token, false));
    getParser().read(new StringReader("\"\""));
    token = getParser().getLastToken();
    assertNotNull(getParser().readDescription(token, true));
    getParser().read(new StringReader("\"\""));
    token = getParser().getLastToken();
    assertEquals(null, getParser().readDescription(token, false));
    getParser().read(new StringReader("\"\";abc"));
    token = getParser().getLastToken();
    assertNotNull(getParser().readDescription(token, true));
    getParser().read(new StringReader("\"\";abc"));
    token = getParser().getLastToken();
    assertEquals(null, getParser().readDescription(token, false));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#readFinalKeyword(magellan.library.utils.OrderToken)}
   * .
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testReadFinalKeyword() {
    getParser().read(new StringReader("abc"));
    OrderToken token = getParser().getLastToken();
    assertTrue(getParser().readFinalKeyword(token));
    assertTrue(token.ttype == OrderToken.TT_KEYWORD);
    getParser().read(new StringReader("abc abc"));
    token = getParser().getLastToken();
    assertFalse(getParser().readFinalKeyword(token));
    assertTrue(token.ttype == OrderToken.TT_KEYWORD);
    getParser().read(new StringReader(""));
    token = getParser().getLastToken();
    assertFalse(getParser().readFinalKeyword(token));
    assertTrue(token.ttype == OrderToken.TT_KEYWORD);
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#readFinalString(magellan.library.utils.OrderToken)}
   * .
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testReadFinalString() {
    getParser().read(new StringReader("abc"));
    OrderToken token = getParser().getLastToken();
    assertTrue(getParser().readFinalString(token));
    getParser().read(new StringReader("abc ;123"));
    token = getParser().getLastToken();
    assertTrue(getParser().readFinalString(token));
    getParser().read(new StringReader("abc abc"));
    token = getParser().getLastToken();
    assertFalse(getParser().readFinalString(token));
    getParser().read(new StringReader("\"abc abc\""));
    token = getParser().getLastToken();
    assertTrue(getParser().readFinalString(token));
    getParser().read(new StringReader("\"abc abc"));
    token = getParser().getLastToken();
    assertFalse(getParser().readFinalString(token));
    getParser().read(new StringReader(""));
    token = getParser().getLastToken();
    assertFalse(getParser().readFinalString(token));
    getParser().read(new StringReader("1234#"));
    token = getParser().getLastToken();
    try {
      getParser().readFinalString(token);
      assertFalse(true);
    } catch (IllegalArgumentException e) {
      // should throw exception!
    }
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#readFinalID(magellan.library.utils.OrderToken)}
   * .
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testReadFinalID() {
    getParser().read(new StringReader("abc"));
    OrderToken token = getParser().getLastToken();
    assertTrue(getParser().readFinalID(token));
    assertTrue(token.ttype == OrderToken.TT_ID);

    getParser().read(new StringReader("abc abc"));
    token = getParser().getLastToken();
    assertFalse(getParser().readFinalID(token));
    assertTrue(token.ttype == OrderToken.TT_ID);

    getParser().read(new StringReader("")); // TODO what should happen here?
    token = getParser().getLastToken();
    try {
      getParser().readFinalID(token);
      fail("should throw exception");
    } catch (NoSuchElementException e) {
      // okay
    }
    assertTrue(token.ttype == OrderToken.TT_ID);
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#readFinalNumber(magellan.library.utils.OrderToken)}
   * .
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testReadFinalNumber() {
    getParser().read(new StringReader("abc"));
    OrderToken token = getParser().getLastToken();
    assertTrue(getParser().readFinalNumber(token));
    assertTrue(token.ttype == OrderToken.TT_NUMBER);
    getParser().read(new StringReader("abc abc"));
    token = getParser().getLastToken();
    assertFalse(getParser().readFinalNumber(token));
    assertTrue(token.ttype == OrderToken.TT_NUMBER);
    getParser().read(new StringReader(""));
    token = getParser().getLastToken();
    assertFalse(getParser().readFinalNumber(token));
    assertTrue(token.ttype == OrderToken.TT_NUMBER);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser#checkNextFinal()}.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testCheckNextFinal() {
    getParser().read(new StringReader("abc"));
    assertTrue(getParser().checkNextFinal());
    getParser().read(new StringReader(""));
    assertFalse(getParser().checkNextFinal());
    getParser().read(new StringReader("; abc"));
    assertFalse(getParser().checkNextFinal());
    getParser().read(new StringReader("abc; abc"));
    assertTrue(getParser().checkNextFinal());
    getParser().read(new StringReader("abc abc"));
    assertFalse(getParser().checkNextFinal());
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#checkFinal(magellan.library.utils.OrderToken)}
   * .
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testCheckFinal() {
    assertTrue(getParser().checkFinal(new OrderToken(OrderToken.TT_EOC)));
    assertTrue(getParser().checkFinal(new OrderToken(OrderToken.TT_COMMENT)));
    assertFalse(getParser().checkFinal(new OrderToken(OrderToken.TT_STRING)));
    getParser().read(new StringReader(""));
    assertTrue(getParser().checkFinal(getParser().getLastToken()));
    getParser().read(new StringReader("; abc"));
    assertTrue(getParser().checkFinal(getParser().getLastToken()));
    getParser().read(new StringReader("abc; abc"));
    assertFalse(getParser().checkFinal(getParser().getLastToken()));
    assertTrue(getParser().checkFinal(getParser().getNextToken()));
    getParser().read(new StringReader("abc"));
    assertFalse(getParser().checkFinal(getParser().getLastToken()));
    assertTrue(getParser().checkFinal(getParser().getNextToken()));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#unexpected(magellan.library.utils.OrderToken)}
   * .
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testUnexpected() {
    assertTrue(getParser().getErrorMessage() == null);
    getParser().setErrMsg("error");
    assertTrue(getParser().getErrorMessage().equals("error"));
    getParser().setErrMsg(null);
    assertTrue(getParser().getErrorMessage() == null);
    getParser().read(new StringReader("ARBEITE"));
    assertTrue(getParser().getErrorMessage() == null);
    getParser().read(new StringReader("MACHE TEMP 1 \"123\""));
    assertTrue(getParser().getErrorMessage() == null);
    getParser().read(new StringReader("NUMMER EINHEIT ; hello"));
    assertTrue(getParser().getErrorMessage() == null);
    getParser().read(new StringReader("ARBEITE 2"));
    assertTrue(getParser().getErrorMessage().equals(
        "Unexpected token 2: Undefined(8, 9), not followed by Space"));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isNumeric(java.lang.String, int, int, int)}
   * .
   */
  @Test
  public void testIsNumericString() {
    assertTrue(getParser().isNumeric("123456567"));
    assertTrue(getParser().isNumeric("-2", 10, -10, 0));
    assertTrue(getParser().isNumeric("abc", 36, 13368, 13368));
    assertTrue(getParser().isNumeric("ff", 16, 0, 256));
    assertFalse(getParser().isNumeric("ff", 16, 0, 100));
    assertFalse(getParser().isNumeric("-2"));
    assertFalse(getParser().isNumeric("1 2"));
    assertFalse(getParser().isNumeric("1,2"));
    assertFalse(getParser().isNumeric("1.2"));
    assertFalse(getParser().isNumeric("--"));
    assertFalse(getParser().isNumeric("a"));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser#isID(java.lang.String)}.
   */
  @Test
  public void testIsID() {
    assertTrue(getParser().isID("TEMP abc"));
    assertFalse(getParser().isID("TEMP abc", false));
    assertTrue(getParser().isID("12"));
    assertTrue(getParser().isID("abc"));
    assertTrue(getParser().isID("2ac"));
    assertFalse(getParser().isID("12345"));
    assertFalse(getParser().isID("1,3"));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isTempID(java.lang.String)}.
   */
  @Test
  public void testIsTempID() {
    assertTrue(getParser().isTempID("TEMP abc"));
    assertTrue(getParser().isTempID("TEMP 1"));
    assertFalse(getParser().isTempID("1,3"));
    assertFalse(getParser().isTempID("abc"));
    assertFalse(getParser().isTempID(" TEMP abc "));
    assertFalse(getParser().isTempID(" TEMP TEMP temp"));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser#isRID(java.lang.String)}
   * .
   */
  @Test
  public void testIsRID() {
    assertTrue(getParser().isRID("1,3"));
    assertTrue(getParser().isRID("1,-3"));
    assertTrue(getParser().isRID("-1,-3323"));
    assertTrue(getParser().isRID("1,3,4"));
    assertFalse(getParser().isRID("1, 3"));
    assertFalse(getParser().isRID(" 1,3"));
    assertFalse(getParser().isRID("1,3 "));
    assertFalse(getParser().isRID("1,, 3"));
    assertFalse(getParser().isRID("1 3"));
    assertFalse(getParser().isRID("1 -3"));
    assertFalse(getParser().isRID("-a, 1"));
    assertFalse(getParser().isRID("123"));
    assertFalse(getParser().isRID("a, b"));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isQuoted(java.lang.String)}.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testIsQuoted() {
    assertFalse(getParser().isQuoted("abc"));
    assertTrue(getParser().isQuoted("\"abc\""));
    assertFalse(getParser().isQuoted("'abc'"));
    assertFalse(getParser().isQuoted("abc5d"));
    assertFalse(getParser().isQuoted("567"));
    assertFalse(getParser().isQuoted("\"abc'"));
    assertFalse(getParser().isQuoted("'123"));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isSingleQuoted(java.lang.String)}.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testIsSingleQuoted() {
    assertFalse(getParser().isSingleQuoted("abc"));
    assertFalse(getParser().isSingleQuoted("\"abc\""));
    assertTrue(getParser().isSingleQuoted("'abc'"));
    assertFalse(getParser().isSingleQuoted("abc5d"));
    assertFalse(getParser().isSingleQuoted("567"));
    assertFalse(getParser().isSingleQuoted("\"abc'"));
    assertFalse(getParser().isSingleQuoted("'123"));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isAnyString(java.lang.String)}.
   */
  @Test
  public void testIsAnyString() {
    assertTrue(getParser().isAnyString("abc"));
    assertTrue(getParser().isAnyString("abc@123"));
    assertTrue(getParser().isAnyString("\"abc\""));
    assertTrue(getParser().isAnyString("'abc'"));
    assertTrue(getParser().isAnyString("abc5d"));
    assertTrue(getParser().isAnyString("567"));
    assertTrue(getParser().isAnyString("\"abc'"));
    assertTrue(getParser().isAnyString("'123"));
    assertFalse(getParser().isAnyString("\n"));
    assertTrue(getParser().isAnyString(
        "сп#.=\u0142\u0138@\u20ac\u277c\ua9c5\ufb21\ud80c\udc49\u2f24\u30f0\u0631\u0627\u0644\u0639"));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isSimpleString(java.lang.String)}.
   */
  @Test
  public void testIsSimpleString() {
    assertTrue(getParser().isSimpleString("abc"));
    assertFalse(getParser().isSimpleString("abc@123"));
    assertFalse(getParser().isSimpleString("\"abc\""));
    assertFalse(getParser().isSimpleString("'abc'"));
    assertTrue(getParser().isSimpleString("abc5d"));
    assertFalse(getParser().isSimpleString("567"));
    assertFalse(getParser().isSimpleString("\"abc'"));
    assertFalse(getParser().isSimpleString("'123"));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isString(java.lang.String)}.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testIsStringString() {
    assertTrue(getParser().isString("abc"));
    assertTrue(getParser().isString("\"abc\""));
    assertTrue(getParser().isString("'abc'"));
    assertTrue(getParser().isString("abc5d"));
    assertFalse(getParser().isString("567"));
    assertFalse(getParser().isString("\"abc'"));
    assertFalse(getParser().isString("'123"));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isString(magellan.library.utils.OrderToken)}
   * .
   */
  @Test
  public void testIsStringOrderToken() {
    OrderToken token = new OrderToken("");
    assertTrue(getParser().isString(token, false, true) == getParser().isString(token));
    token = new OrderToken(OrderToken.TT_OPENING_QUOTE);
    assertTrue(getParser().isString(token, false, true) == getParser().isString(token));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isString(magellan.library.utils.OrderToken, boolean, boolean)}
   * .
   */
  @Test
  public void testIsStringOrderTokenBoolean() {
    OrderToken token = new OrderToken("");
    assertFalse(getParser().isString(token, true, true));
    assertFalse(getParser().isString(token, false, true));
    token = new OrderToken("a");
    assertFalse(getParser().isString(token, true, true));
    assertTrue(getParser().isString(token, false, true));
    token = new OrderToken(OrderToken.TT_OPENING_QUOTE);
    assertTrue(getParser().isString(token, true, true));
    assertTrue(getParser().isString(token, false, true));
    token = new OrderToken("'abc");
    assertFalse(getParser().isString(token, true, true));
    assertFalse(getParser().isString(token, false, true));
    token = new OrderToken("5");
    assertFalse(getParser().isString(token, true, true));
    assertFalse(getParser().isString(token, false, true));
    token = new OrderToken(OrderToken.TT_EOC);
    assertTrue(getParser().isString(token, true, true));
    assertTrue(getParser().isString(token, false, true));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#getString(magellan.library.utils.OrderToken)}
   * .
   */
  @SuppressWarnings({ "deprecation", "null" })
  @Test
  public void testGetString() {
    getParser().read(new StringReader("abc"));
    OrderToken[] result = getParser().getString(getParser().getLastToken());
    assertTrue(result[0] == null);
    OrderToken contentToken = new OrderToken("abc", 0, 3, OrderToken.TT_STRING, false);
    assertTrue(result[1] != null && equals(result[1], contentToken));
    assertTrue(result[2] == null);
    OrderToken nextToken = new OrderToken(OrderToken.TT_EOC);
    assertTrue(equals(result[3], nextToken));

    getParser().read(new StringReader("@"));
    result = getParser().getString(getParser().getLastToken());
    assertTrue(result[0] == null);
    contentToken = new OrderToken("@", 0, 1, OrderToken.TT_STRING, false);
    assertTrue(result[1] == null);
    assertTrue(result[2] == null);

    getParser().read(new StringReader("\"abc\""));
    result = getParser().getString(getParser().getLastToken());
    OrderToken openingToken = new OrderToken("\"", 0, 1, OrderToken.TT_OPENING_QUOTE, false);
    assertTrue(equals(result[0], openingToken));
    contentToken = new OrderToken("abc", 1, 4, OrderToken.TT_STRING, false);
    assertTrue(result[1] != null && equals(result[1], contentToken));
    OrderToken closingToken = new OrderToken("\"", 4, 5, OrderToken.TT_CLOSING_QUOTE, false);
    assertTrue(equals(result[2], closingToken));
    nextToken = new OrderToken(OrderToken.TT_EOC);
    assertTrue(equals(result[3], nextToken));

    if (contains(getParser().getQuotes(), '\'')) {
      getParser().read(new StringReader("'abc'"));
      result = getParser().getString(getParser().getLastToken());
      openingToken = new OrderToken("'", 0, 1, OrderToken.TT_OPENING_QUOTE, false);
      assertTrue(equals(result[0], openingToken));
      contentToken = new OrderToken("abc", 1, 4, OrderToken.TT_STRING, false);
      assertTrue(result[1] != null && equals(result[1], contentToken));
      closingToken = new OrderToken("'", 4, 5, OrderToken.TT_CLOSING_QUOTE, false);
      assertTrue(equals(result[2], closingToken));
      nextToken = new OrderToken(OrderToken.TT_EOC);
      assertTrue(equals(result[3], nextToken));
    }

    getParser().read(new StringReader("\"a"));
    result = getParser().getString(getParser().getLastToken());
    openingToken = new OrderToken("\"", 0, 1, OrderToken.TT_OPENING_QUOTE, false);
    assertTrue(equals(result[0], openingToken));
    contentToken = new OrderToken("a", 1, 2, OrderToken.TT_STRING, false);
    assertTrue(result[1] != null && equals(result[1], contentToken));
    closingToken = null;
    assertTrue(equals(result[2], closingToken));
    nextToken = new OrderToken(OrderToken.TT_EOC);
    assertTrue(equals(result[3], nextToken));

    getParser().read(new StringReader("\"a'"));
    result = getParser().getString(getParser().getLastToken());
    openingToken = new OrderToken("\"", 0, 1, OrderToken.TT_OPENING_QUOTE, false);
    assertTrue(equals(result[0], openingToken));
    contentToken = new OrderToken("a'", 1, 3, OrderToken.TT_STRING, false);
    assertTrue(result[1] != null && equals(result[1], contentToken));
    closingToken = null;
    assertTrue(equals(result[2], closingToken));
    nextToken = new OrderToken(OrderToken.TT_EOC);
    assertTrue(equals(result[3], nextToken));

  }

  private boolean contains(char[] characters, char c) {
    for (char member : characters)
      if (member == c)
        return true;
    return false;
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
    assertTrue(getParser() != null);
    if (getParser() != null) {
      assertTrue(getParser().isEmailAddress("a@b.com"));
      assertTrue(getParser().isEmailAddress("123@234.com"));
      assertTrue(getParser().isEmailAddress("a.b.c.defg.a@hallo.bla.bla.com"));
      assertFalse(getParser().isEmailAddress(""));
      assertFalse(getParser().isEmailAddress("a"));
      assertFalse(getParser().isEmailAddress("a.b"));
      assertFalse(getParser().isEmailAddress("@b"));
      assertFalse(getParser().isEmailAddress("@b.com"));
      assertTrue(getParser().isEmailAddress("jsmith@[192.168.2.1]"));
      // FIXME these tests fail
      if (DO_KNOWN_FAILURES) {
        assertFalse(getParser().isEmailAddress(".@.")); // shouldn't be allowed, but is
        assertFalse(getParser().isEmailAddress("a@b")); // shouldn't be allowed, but is
        assertFalse(getParser().isEmailAddress("a.@b.com")); // shouldn't be allowed, but is
        assertFalse(getParser().isEmailAddress(".a@b.com")); // shouldn't be allowed, but is
        // shouldn't be allowed, but is
        assertTrue(getParser().isEmailAddress("\"!#$%&'*+-/=?^_`{|}~\"@example.com"));
      }
    }
  }

  protected abstract AbstractOrderParser getParser();

  protected abstract void setParser(OrderParser parser);

  protected abstract Completer getCompleter();

  protected abstract void setCompleter(AbstractOrderCompleter completer);

}