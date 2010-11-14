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

import java.io.IOException;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for OrderTokenizer
 */
public class OrderTokenizerTest {

  private static final OrderToken EOC = new OrderToken("", -1, -1, OrderToken.TT_EOC, false);

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * Test method for {@link magellan.library.utils.OrderTokenizer#OrderTokenizer(java.io.Reader)}.
   */
  @Test
  public final void testOrderTokenizer() {
    // nothing to test
  }

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

  protected void doTest(String string, OrderToken... orderToken) {
    StringReader in = new StringReader(string);
    OrderTokenizer tokenizer = new OrderTokenizer(in);
    OrderToken token = null;
    for (int i = 0; i < orderToken.length; ++i) {
      token = tokenizer.getNextToken();
      assertEqualToken(i, orderToken[i], token);
    }
    assertEqualToken(-1, EOC, token);
  }

  /**
   * Test method for {@link magellan.library.utils.OrderTokenizer#readQuote(int)}.
   * 
   * @throws IOException
   */
  @Test
  public final void testReadQuote() throws IOException {
  }

  protected void assertEqualToken(int i, OrderToken expected, OrderToken actual) {
    assertEquals("token " + i + " text", expected.getText(), actual.getText());
    assertSame("token " + i + " start", expected.getStart(), actual.getStart());
    assertSame("token " + i + " end", expected.getEnd(), actual.getEnd());
    assertSame("token " + i + " type", expected.ttype, actual.ttype);
    assertSame("token " + i + " followed", expected.followedBySpace(), actual.followedBySpace());
  }

  @Test
  public final void testComment() {
    doTest(";", new OrderToken(";", 0, 1, OrderToken.TT_COMMENT, true), EOC);
    doTest(";;", new OrderToken(";;", 0, 2, OrderToken.TT_COMMENT, true), EOC);
    doTest("; abc \"foo\"", new OrderToken("; abc \"foo\"", 0, 11, OrderToken.TT_COMMENT, true),
        EOC);
  }

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
    // FIXME the server interprets ;-comments also after //
    doTest("//;", new OrderToken("//", 0, 2, OrderToken.TT_UNDEF, true), new OrderToken(";", 2, 3,
        OrderToken.TT_COMMENT, true), EOC);
    doTest("abc //", new OrderToken("abc", 0, 3, OrderToken.TT_UNDEF, true), new OrderToken("//",
        4, 6, OrderToken.TT_UNDEF, false), EOC);
  }

}
