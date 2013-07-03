// class magellan.library.utils.OrderTokenizerTest
// created on Nov 14, 2010
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
package magellan.library.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.StringReader;

import org.junit.Test;

/**
 * Tests for OrderTokenizer
 */
public class OrderTokenizerTest {

  private static final OrderToken EOC = new OrderToken("", -1, -1, OrderToken.TT_EOC, false);

  protected void doTest(String string, OrderToken... orderTokens) {
    StringReader in = new StringReader(string);
    OrderTokenizer tokenizer = new OrderTokenizer(in);
    doTest(tokenizer, string, orderTokens);
  }

  protected void doTest(OrderTokenizer tokenizer, String string, OrderToken... orderToken) {

    OrderToken token = null;
    for (int i = 0; i < orderToken.length; ++i) {
      token = tokenizer.getNextToken();
      assertEqualToken(i, orderToken[i], token);
    }
    assertEqualToken(-1, EOC, token);
  }

  protected void assertEqualToken(int i, OrderToken expected, OrderToken actual) {
    assertEquals("token " + i + " text", expected.getText(), actual.getText());
    assertSame("token " + i + " start", expected.getStart(), actual.getStart());
    assertSame("token " + i + " end", expected.getEnd(), actual.getEnd());
    assertSame("token " + i + " type", expected.ttype, actual.ttype);
    assertSame("token " + i + " followed", expected.followedBySpace(), actual.followedBySpace());
  }

  /**
   * Fun with whitespace.
   */
  @Test
  public final void testWhitespace() {
    doTest(" ", EOC);

    doTest(" ARBEITEN ", new OrderToken("ARBEITEN", 1, 9, OrderToken.TT_UNDEF, true), EOC);
    doTest("@ARBEITEN ", new OrderToken("@", 0, 1, OrderToken.TT_PERSIST, false), new OrderToken(
        "ARBEITEN", 1, 9, OrderToken.TT_UNDEF, true), EOC);
    doTest("@ ARBEITEN ", new OrderToken("@", 0, 1, OrderToken.TT_PERSIST, false), new OrderToken(
        "ARBEITEN", 2, 10, OrderToken.TT_UNDEF, true), EOC);
    doTest(" @ARBEITEN ", new OrderToken("@", 1, 2, OrderToken.TT_PERSIST, false), new OrderToken(
        "ARBEITEN", 2, 10, OrderToken.TT_UNDEF, true), EOC);
    doTest(" @ ARBEITEN ", new OrderToken("@", 1, 2, OrderToken.TT_PERSIST, false), new OrderToken(
        "ARBEITEN", 3, 11, OrderToken.TT_UNDEF, true), EOC);

  }

  /**
   * Fun with quotes.
   */
  @Test
  public final void testString() {
    doTest("", EOC);
    doTest("\"\"", new OrderToken("\"", 0, 1, OrderToken.TT_OPENING_QUOTE, false), new OrderToken(
        "", 1, 1, OrderToken.TT_STRING, false), new OrderToken("\"", 1, 2,
        OrderToken.TT_CLOSING_QUOTE, false), new OrderToken("", -1, -1, OrderToken.TT_EOC, false));

    doTest("\"", new OrderToken("\"", 0, 1, OrderToken.TT_OPENING_QUOTE, false), new OrderToken("",
        -1, -1, OrderToken.TT_EOC, false));

    doTest("\"abc\"", new OrderToken("\"", 0, 1, OrderToken.TT_OPENING_QUOTE, false),
        new OrderToken("abc", 1, 4, OrderToken.TT_STRING, false), new OrderToken("\"", 4, 5,
            OrderToken.TT_CLOSING_QUOTE, false), new OrderToken("", -1, -1, OrderToken.TT_EOC,
            false));

    doTest("\"abc", new OrderToken("\"", 0, 1, OrderToken.TT_OPENING_QUOTE, false), new OrderToken(
        "abc", 1, 4, OrderToken.TT_STRING, false), new OrderToken("", -1, -1, OrderToken.TT_EOC,
        false));

    doTest("DEFAULT 'LERNEN U", new OrderToken("DEFAULT", 0, 7, OrderToken.TT_UNDEF, true),
        new OrderToken("'", 8, 9, OrderToken.TT_OPENING_QUOTE, false), new OrderToken("LERNEN U",
            9, 17, OrderToken.TT_STRING, false), new OrderToken("", -1, -1, OrderToken.TT_EOC,
            false));

  }

  /**
   * Test for ;-comments
   */
  @Test
  public final void testComment() {
    doTest(";", new OrderToken(";", 0, 1, OrderToken.TT_COMMENT, true), EOC);
    doTest(";;", new OrderToken(";;", 0, 2, OrderToken.TT_COMMENT, true), EOC);
    doTest("; abc \"foo\"", new OrderToken("; abc \"foo\"", 0, 11, OrderToken.TT_COMMENT, true),
        EOC);
  }

  /**
   * Fun with //-comments.
   */
  @Test
  public final void testOComment() {
    doTest("/", new OrderToken("/", 0, 1, OrderToken.TT_UNDEF, false), EOC);
    doTest("// ", new OrderToken("// ", 0, 3, OrderToken.TT_COMMENT, true), EOC);
    doTest("//", new OrderToken("//", 0, 2, OrderToken.TT_COMMENT, true), EOC);
    doTest("// abc \"foo\"", new OrderToken("// abc \"foo\"", 0, 12, OrderToken.TT_COMMENT, true),
        EOC);
    doTest("//\t", new OrderToken("//\t", 0, 3, OrderToken.TT_COMMENT, true), EOC);
    doTest("//\n", new OrderToken("//", 0, 3, OrderToken.TT_COMMENT, true), EOC);
    doTest("//\"", new OrderToken("//", 0, 2, OrderToken.TT_UNDEF, true), new OrderToken("\"", 2,
        3, OrderToken.TT_OPENING_QUOTE, false), EOC);
    // FIXME the server interprets ;-comments also after //. But I don't care at the moment
    doTest("//;", new OrderToken("//", 0, 2, OrderToken.TT_UNDEF, true), new OrderToken(";", 2, 3,
        OrderToken.TT_COMMENT, true), EOC);
    doTest("abc //", new OrderToken("abc", 0, 3, OrderToken.TT_UNDEF, true), new OrderToken("//",
        4, 6, OrderToken.TT_UNDEF, false), EOC);
  }

  @Test
  public final void testQuotes() {
    String string;
    StringReader in;
    OrderTokenizer tokenizer;

    string = "";
    in = new StringReader(string);
    tokenizer = new OrderTokenizer(in);
    tokenizer.setQuotes(new char[] { '"' });
    doTest(tokenizer, string, EOC);

    string = "\"\"";
    in = new StringReader(string);
    tokenizer = new OrderTokenizer(in);
    doTest(tokenizer, string, new OrderToken("\"", 0, 1, OrderToken.TT_OPENING_QUOTE, false),
        new OrderToken("", 1, 1, OrderToken.TT_STRING, false), new OrderToken("\"", 1, 2,
            OrderToken.TT_CLOSING_QUOTE, false), new OrderToken("", -1, -1, OrderToken.TT_EOC,
            false));

    string = "\"";
    in = new StringReader(string);
    tokenizer = new OrderTokenizer(in);
    tokenizer.setQuotes(new char[] { '"' });
    doTest(tokenizer, string, new OrderToken("\"", 0, 1, OrderToken.TT_OPENING_QUOTE, false),
        new OrderToken("", -1, -1, OrderToken.TT_EOC, false));

    string = "\"abc\"";
    in = new StringReader(string);
    tokenizer = new OrderTokenizer(in);
    tokenizer.setQuotes(new char[] { '"' });
    doTest(tokenizer, string, new OrderToken("\"", 0, 1, OrderToken.TT_OPENING_QUOTE, false),
        new OrderToken("abc", 1, 4, OrderToken.TT_STRING, false), new OrderToken("\"", 4, 5,
            OrderToken.TT_CLOSING_QUOTE, false), new OrderToken("", -1, -1, OrderToken.TT_EOC,
            false));

    string = "'abc'";
    in = new StringReader(string);
    tokenizer = new OrderTokenizer(in);
    tokenizer.setQuotes(new char[] { '"' });
    doTest(tokenizer, string, new OrderToken("'abc'", 0, 5, OrderToken.TT_UNDEF, false),
        new OrderToken("", -1, -1, OrderToken.TT_EOC, false));

    string = "'one two'";
    in = new StringReader(string);
    tokenizer = new OrderTokenizer(in);
    tokenizer.setQuotes(new char[] { '"' });
    doTest(tokenizer, string, new OrderToken("'one", 0, 4, OrderToken.TT_UNDEF, true),
        new OrderToken("two'", 5, 9, OrderToken.TT_UNDEF, false), new OrderToken("", -1, -1,
            OrderToken.TT_EOC, false));

  }
}
