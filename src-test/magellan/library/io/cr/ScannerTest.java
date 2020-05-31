// class magellan.library.io.cr.ScannerTest
// created on May 25, 2020
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
package magellan.library.io.cr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

/**
 * Tests for {@link Scanner}.
 */
public class ScannerTest {

  static class ScannerState extends Scanner {

    public ScannerState() throws IOException {
      super(new StringReader(""));
    }

    public ScannerState(boolean eof) throws IOException {
      super(new StringReader(""));
      this.eof = eof;
    }

    public ScannerState setArgc(int c) {
      argc = c;
      if (lnr == 0) {
        lnr = 1;
      }
      return this;
    }

    public ScannerState setArgv0(String arg) {
      argv[0] = arg;
      if (lnr == 0) {
        lnr = 1;
      }
      return this;
    }

    public ScannerState setArgv1(String arg) {
      argv[1] = arg;
      if (lnr == 0) {
        lnr = 1;
      }
      return this;
    }

    public ScannerState setString0(boolean newFlag) {
      isString[0] = newFlag;
      if (lnr == 0) {
        lnr = 1;
      }
      return this;
    }

    public ScannerState setString1(boolean newFlag) {
      isString[1] = newFlag;
      if (lnr == 0) {
        lnr = 1;
      }
      return this;
    }

    public ScannerState setIdBlock(boolean newFlag) {
      isIdBlock = newFlag;
      isBlock = newFlag;
      if (lnr == 0) {
        lnr = 1;
      }
      return this;
    }

    public ScannerState setBlock(boolean newFlag) {
      isBlock = newFlag;
      if (lnr == 0) {
        lnr = 1;
      }
      return this;
    }

    public ScannerState setLine(int nr) {
      lnr = nr;
      return this;
    }

    public ScannerState setEof(boolean flag) {
      eof = flag;
      return this;
    }

  }

  @Test
  public void testInitial() throws IOException {
    Scanner sc = getScanner("");
    assertEquals(0, sc.argc);
    assertNull(sc.argv[0]);
    assertNull(sc.argv[1]);
    assertEquals(false, sc.eof);
    assertEquals(false, sc.isBlock);
    assertEquals(false, sc.isIdBlock);
    assertEquals(false, sc.isString[0]);
    assertEquals(false, sc.isString[1]);
    assertEquals(0, sc.lnr);
    assertState(sc, new ScannerState(false));

    sc.getNextToken();
    assertTrue(sc.eof);

    assertState(sc, new ScannerState(true).setLine(1));
  }

  @Test
  public void testBlock() throws IOException {
    Scanner sc = getScanner("BLOCK");
    sc.getNextToken();
    assertState(sc, new ScannerState().setArgc(1).setArgv0("BLOCK").setBlock(true));
  }

  @Test
  public void testTwoBlocks() throws IOException {
    Scanner sc = getScanner("BLOCK\nBLUCK");
    sc.getNextToken();
    assertState(sc, new ScannerState().setArgc(1).setArgv0("BLOCK").setBlock(true));
    sc.getNextToken();
    assertState(sc, new ScannerState().setArgc(1).setArgv0("BLUCK").setBlock(true).setLine(2));
    sc.getNextToken();
    assertState(sc, new ScannerState().setArgc(0).setLine(3).setEof(true));
  }

  @Test
  public void testEmptyLine() throws IOException {
    Scanner sc = getScanner("BLOCK\n\nBLUCK");
    sc.getNextToken();
    assertState(sc, new ScannerState().setArgc(1).setArgv0("BLOCK").setBlock(true));
    sc.getNextToken();
    assertState(sc, new ScannerState().setArgc(1).setArgv0("BLUCK").setBlock(true).setLine(3));
    sc.getNextToken();
    assertState(sc, new ScannerState().setArgc(0).setLine(4).setEof(true));
  }

  @Test
  public void testIdBlock() throws IOException {
    Scanner sc = getScanner("BLOCK 123");
    sc.getNextToken();
    assertState(sc, new ScannerState().setArgc(1).setArgv0("BLOCK 123").setIdBlock(true));
  }

  @Test
  public void testNumTag() throws IOException {
    Scanner sc = getScanner("123;Hodor");
    sc.getNextToken();
    assertState(sc, new ScannerState().setArgc(2).setArgv0("123").setArgv1("Hodor"));
  }

  @Test
  public void testStringTag() throws IOException {
    Scanner sc = getScanner("\"123\";Hodor");
    sc.getNextToken();
    assertState(sc, new ScannerState().setArgc(2).setArgv0("123").setArgv1("Hodor").setString0(true));
  }

  @Test
  public void testString() throws IOException {
    Scanner sc = getScanner("\"abc123\"");
    sc.getNextToken();
    assertState(sc, new ScannerState().setArgc(1).setArgv0("abc123").setString0(true));
  }

  @Test
  public void testBotchedString() throws IOException {
    Scanner sc = getScanner("\"abc123\nxyz"); // error; should be \"abc123\"\nxyz, probably
    sc.getNextToken();
    assertState(sc, new ScannerState().setArgc(0)); // could be argv[0]==abc123 too
    sc.getNextToken();
    assertState(sc, new ScannerState().setArgc(1).setArgv0("xyz").setLine(2));
  }

  @Test
  public void testEscapedString() throws IOException {
    Scanner sc = getScanner("\"backs\\\\\\\\back\\\\quote\\\"single\\'done\"");
    sc.getNextToken();

    assertState(sc, new ScannerState().setArgc(1).setArgv0("backs\\\\back\\quote\"single\\'done").setString0(true));
  }

  @Test
  public void testEscapedQuote() throws IOException {
    Scanner sc = getScanner("\"DEFAULT \\\"ARBEITE\\\"\"");
    sc.getNextToken();
    assertState(sc, new ScannerState().setArgc(1).setArgv0("DEFAULT \"ARBEITE\"").setString0(true));
  }

  @Test
  public void testWrongEscapedString() throws IOException {
    // error; should be \"\\\\bc\" or \"abc\"
    Scanner sc = getScanner("\"a\\bc\"");
    sc.getNextToken();

    assertState(sc, new ScannerState().setArgc(1).setArgv0("a\\bc").setString0(true));
  }

  private void assertState(Scanner sc, ScannerState state) {
    assertEquals(state.argc, sc.argc);
    assertEquals(state.argv[0], sc.argv[0]);
    if (state.argv[0] == null) {
      assertNull(sc.argv[0]);
    } else {
      assertEquals(state.argv[0], sc.argv[0]);
    }
    if (state.argv[1] == null) {
      assertNull(sc.argv[1]);
    } else {
      assertEquals(state.argv[1], sc.argv[1]);
    }

    assertEquals(state.eof, sc.eof);
    assertEquals(state.isBlock, sc.isBlock);
    assertEquals(state.isIdBlock, sc.isIdBlock);
    assertEquals(state.isString[0], sc.isString[0]);
    assertEquals(state.isString[1], sc.isString[1]);
    assertEquals(state.lnr, sc.lnr);
  }

  private Scanner getScanner(String input) throws IOException {
    return new Scanner(new StringReader(input));
  }

}
