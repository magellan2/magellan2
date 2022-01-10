// class magellan.plugin.extendedcommands.scripts.EAssert
// created on Jan 3, 2016
//
// Copyright 2003-2016 by magellan project team
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
package magellan.plugin.extendedcommands.scripts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import magellan.library.Unit;

public class EAssert {
  @SuppressWarnings("deprecation")
  public static boolean containsOrder(Unit unit2, String string) {
    return unit2.getOrders().contains(string);
  }

  public static void assertComment(Unit u, int lineNr, boolean shortComment, boolean longComment) {
    assertTrue("expected comment, but not enough orders", u.getOrders2().size() > lineNr);
    String actual = u.getOrders2().get(lineNr).getText();
    assertTrue("no long comment: " + actual, !longComment || actual.startsWith("// "));
    assertTrue("no short comment: " + actual, !shortComment || actual.startsWith(";"));
  }

  public static void assertOrder(String expected, Unit u, int lineNr) {
    assertTrue("expected " + expected + ", but not enough orders", u.getOrders2().size() > lineNr);
    String actual = u.getOrders2().get(lineNr).getText();
    if (!(actual.startsWith(expected) && (actual.length() == expected.length() || actual.substring(
        expected.length()).trim().startsWith(";")))) {
      assertEquals(expected, actual);
    }
  }

  public static void assertOrder(String message, String expected, Unit u, int lineNr) {
    assertTrue("expected " + expected + ", but not enough orders", u.getOrders2().size() > lineNr);
    String actual = u.getOrders2().get(lineNr).getText();
    if (!(actual.startsWith(expected) && (actual.length() == expected.length() || actual.substring(
        expected.length()).trim().startsWith(";")))) {
      assertEquals(message, expected, actual);
    }
  }

  public static void assertError(String expected, Unit u, int lineNr) {
    assertError(expected, u, lineNr, "; TODO", "(Fehler");
  }

  public static void assertWarning(String expected, Unit u, int lineNr) {
    assertError(expected, u, lineNr, "; TODO", "");
  }

  public static void assertError(String expected, Unit u, int lineNr, String prefix,
      String warning) {
    int min, max;
    String found = null;
    if (lineNr > 0) {
      assertTrue("expected order \"" + expected + "\", but not enough orders", u.getOrders2()
          .size() > lineNr);
      min = lineNr;
      max = lineNr;
    } else {
      assertTrue("expected order \"" + expected + "\" but unit has no orders", u.getOrders2()
          .size() > 0);
      min = 0;
      max = u.getOrders2().size() - 1;
    }

    for (int currentLine = min; currentLine <= max && found == null; ++currentLine) {
      String actual = u.getOrders2().get(currentLine).getText();
      if (actual.contains(expected) && actual.startsWith(prefix) && actual.contains(warning)) {
        found = actual;
      }
    }
    if (found == null) {
      assertEquals("; TODO: " + expected + " " + warning, found);
    }
  }

  public static void assertMessage(String expected, Unit u, int lineNr) {
    assertError(expected, u, lineNr, "; ", "");
  }

  public static void assertWarning(Warning w, boolean amount, boolean skill, boolean hidden,
      boolean foreign) {
    assertEquals("amount was not " + amount, amount, w.contains(Warning.C_AMOUNT));
    assertEquals("skill was not " + skill, skill, w.contains(Warning.C_SKILL));
    assertEquals("hidden was not " + hidden, hidden, w.contains(Warning.C_HIDDEN));
    assertEquals("foreign was not " + foreign, foreign, w.contains(Warning.C_FOREIGN));
  }
}
