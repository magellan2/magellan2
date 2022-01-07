// class magellan.library.utils.JECheckTest
// created on Jan 6, 2022
//
// Copyright 2003-2022 by magellan project team
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
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import magellan.library.utils.JECheck.ECheckMessage;
import magellan.test.MagellanTestWithResources;

public class JECheckTest extends MagellanTestWithResources {

  @Test
  public void testgetLineEmpty() throws Exception {
    BufferedReader reader = new BufferedReader(new StringReader(""));
    assertEquals(null, JECheck.getLine(reader));
  }

  @Test
  public void testgetLineOne() throws Exception {
    BufferedReader reader = new BufferedReader(new StringReader("1"));
    assertEquals("1", JECheck.getLine(reader));
    assertEquals(null, JECheck.getLine(reader));
  }

  @Test
  public void testgetLineOneHalf() throws Exception {
    BufferedReader reader = new BufferedReader(new StringReader("a\nb"));
    assertEquals("a", JECheck.getLine(reader));
    assertEquals("b", JECheck.getLine(reader));
    assertEquals(null, JECheck.getLine(reader));
  }

  @Test
  public void testgetLineTwo() throws Exception {
    BufferedReader reader = new BufferedReader(new StringReader("a\nb\n"));
    assertEquals("a", JECheck.getLine(reader));
    assertEquals("b", JECheck.getLine(reader));
    assertEquals(null, JECheck.getLine(reader));
  }

  @Test
  public void testGetMessagesEmpty() throws IOException, ParseException {
    StringReader reader = new StringReader("");
    Collection<ECheckMessage> msgs = JECheck.getMessages(reader);
    assertEquals(1, msgs.size());
    assertEquals(-1, msgs.iterator().next().getLineNr());
    assertEquals("ECheck-Ausgabe ist leer!", msgs.iterator().next().getMessage());
  }

  @Test
  public void testGetMessagesHappy() throws IOException, ParseException {
    // Fehler in Datei meldungen.txt Zeile 13: `BEHIND'
    StringReader reader = new StringReader("in.txt|version|4.6.8|Jan  6 2022\r\n" +
        "in.txt:faction:dael\r\n" +
        "in.txt|warnings|0\r\n" +
        "in.txt|errors|0\r\n");
    Collection<ECheckMessage> msgs = JECheck.getMessages(reader);
    assertEquals(0, msgs.size());
  }

  @Test
  public void testGetMessagesHappyError() throws IOException, ParseException {
    // Fehler in Datei meldungen.txt Zeile 13: `BEHIND'
    StringReader reader = new StringReader("in.txt|version|4.6.8|Jan  6 2022\r\n" +
        ":faction:\r\n" +
        "in.txt|10|0|Error thing\r\n" +
        "in.txt|11|3|Warning thing\r\n" +
        "in.txt|warnings|1\r\n" +
        "in.txt|errors|1\r\n");
    ArrayList<ECheckMessage> msgs = new ArrayList<JECheck.ECheckMessage>(JECheck.getMessages(reader));
    assertEquals(2, msgs.size());
    ECheckMessage msg = msgs.get(0);
    assertEquals(10, msg.getLineNr());
    assertEquals("Error thing", msg.getMessage());
    assertEquals(JECheck.ECheckMessage.ERROR, msg.getType());
    assertEquals(0, msg.getWarningLevel());
    msg = msgs.get(1);
    assertEquals(JECheck.ECheckMessage.WARNING, msg.getType());
    assertEquals(3, msg.getWarningLevel());
    assertEquals("Warning thing", msg.getMessage());
  }

  @Test
  public void testGetMessagesBarebones() throws IOException, ParseException {
    StringReader reader = new StringReader("in.txt|10|0|Error thing\r\n");
    ArrayList<ECheckMessage> msgs = new ArrayList<JECheck.ECheckMessage>(JECheck.getMessages(reader));
    assertEquals(4, msgs.size());
    ECheckMessage msg = msgs.get(0);
    assertEquals(10, msg.getLineNr());
    assertEquals("Error thing", msg.getMessage());
    assertEquals("Der Fuﬂtext der ECheck-Ausgabe ist nicht wie erwartet: 0", msgs.get(1).getMessage());
    assertEquals("Kann Anzahl der Fehler und Warnungen von ECheck nicht finden", msgs.get(2).getMessage());
    assertEquals("ECheck hat anscheinend keine Partei erkannt.", msgs.get(3).getMessage());
  }

  @Test
  public void testGetMessagesFehler() throws IOException, ParseException {
    StringReader reader = new StringReader("Fehler in Datei meldungen.txt Zeile 13: `BEHIND'\r\n" +
        "in.txt|version|4.6.8|Jan  6 2022\r\n" +
        "in.txt:faction:dael\r\n" +
        "in.txt|10|0|Error thing\r\n" +
        "in.txt|warnings|0\r\n" +
        "in.txt|errors|1\r\n");
    ArrayList<ECheckMessage> msgs = new ArrayList<JECheck.ECheckMessage>(JECheck.getMessages(reader));
    assertEquals(2, msgs.size());
    assertTrue(msgs.get(0).getMessage().contains("Fehler in Datei meldungen.txt Zeile 13: `BEHIND'"));
    assertEquals(-1, msgs.get(0).getLineNr());
  }

  @Test
  public void testGetMessagesMultipleFactions() throws IOException, ParseException {
    StringReader reader = new StringReader("in.txt|version|4.6.8|Jan  6 2022\r\n" +
        "in.txt:faction:foo\r\n" +
        "in.txt:faction:bar\r\n" +
        "in.txt|warnings|0\r\n" +
        "in.txt|errors|0\r\n");
    ArrayList<ECheckMessage> msgs = new ArrayList<JECheck.ECheckMessage>(JECheck.getMessages(reader));
    assertEquals(1, msgs.size());
    assertEquals(-1, msgs.get(0).getLineNr());
    assertEquals("ECheck hat mehr als eine Partei gefunden.", msgs.get(0).getMessage());
  }
}
