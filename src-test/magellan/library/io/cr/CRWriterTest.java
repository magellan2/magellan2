// class magellan.library.io.cr.CRWriterTest
// created on Jun 6, 2020
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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import magellan.library.CompleteData;
import magellan.library.GameData;
import magellan.library.IntegerID;
import magellan.library.Message;
import magellan.library.Rules;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.impl.MagellanMessageImpl;
import magellan.library.rules.MessageType;
import magellan.library.utils.TranslationType;
import magellan.test.GameDataBuilder;

public class CRWriterTest {

  private GameDataBuilder builder;
  private GameData data;
  private StringWriter strWriter;
  private CRWriter writer;

  @Before
  public void setUp() throws Exception {
    builder = new GameDataBuilder();
    data = builder.createSimpleGameData();
    strWriter = null;
  }

  @Test
  public void testWriteMessage() throws IOException {
    writer = setupWriter();
    Message message = new MagellanMessageImpl("abc");
    writer.writeMessage(message);

    assertOutput("MESSAGE -1\n-1;type\n\"abc\";rendered\n");
  }

  private void assertOutput(String string) throws IOException {
    if (writer != null) {
      writer.close();
    }
    assertEquals(string.replace("\n", System.lineSeparator()), strWriter.toString());
  }

  private CRWriter setupWriter() {
    strWriter = new StringWriter();
    writer = new CRWriter(data, null, strWriter);
    return writer;
  }

  @Test
  public void testWriteMessageId() throws IOException {
    setupWriter();

    Map<String, String> attribs = new HashMap<String, String>();
    attribs.put("key", "value");
    MessageType type = new MessageType(IntegerID.create(42), "Some type");
    Message message = new MagellanMessageImpl(IntegerID.create(123), "abc", type, attribs);
    writer.writeMessage(message);

    assertOutput("MESSAGE 123\n42;type\n\"abc\";rendered\n\"value\";key\n");
  }

  @Test
  public void testWriteMessageSequence() throws IOException {
    setupWriter();

    Collection<Message> messages = new ArrayList<Message>();
    messages.add(new MagellanMessageImpl("abc"));
    writer.writeMessageSequence(messages);

    assertOutput("\"abc\"\n");
  }

  @Test
  public void testWriteMessageBlock() throws IOException {
    setupWriter();

    Collection<Message> messages = new ArrayList<Message>();
    messages.add(new MagellanMessageImpl("abc"));
    writer.writeMessageBlock("HODOR", messages);

    assertOutput("HODOR\n\"abc\"\n");
  }

  @Test
  public void testWriteStringSequence() throws IOException {
    setupWriter();

    Collection<String> strings = new ArrayList<String>();
    strings.add("Hodor!");
    strings.add("a\\nb\\\\c\"d"); // a\nb\\c"d -> "a\\nb\\\\c\"d"
    writer.writeStringSequence(strings);
    writer.close();
    assertOutput("\"Hodor!\"\n\"a\\\\nb\\\\\\\\c\\\"d\"\n");
  }

  @Test
  public void testWriteStringBlock() throws IOException {
    setupWriter();

    Collection<String> strings = new ArrayList<String>();
    strings.add("Hodor!");
    strings.add("a\\nb\\\\c\"d"); // a\nb\\c"d -> a\\nb\\\\c\"d
    writer.writeStringBlock("HODOR", strings);

    assertOutput("HODOR\n\"Hodor!\"\n\"a\\\\nb\\\\\\\\c\\\"d\"\n");
  }

  @Test
  public void testWriteRead() throws IOException {
    setupWriter();

    Unit unit = data.getUnits().iterator().next();
    unit.clearOrders();
    unit.addOrder("ARBEITE");
    String order;
    unit.addOrder(order = "a\\n\\\\b\"c");
    writer.doWrite();
    writer.close();

    CRParser parser = new CRParser(null);
    Rules rules = data.getRules();
    GameData data2 = new CompleteData(rules);
    StringReader strReader = new StringReader(strWriter.toString());
    parser.read(strReader, data2);
    Unit unit2 = data2.getUnits().iterator().next();
    assertEquals(unit.getID(), unit2.getID());
    assertEquals(order, unit2.getOrders2().get(1).toString());
  }

  @Test
  public void testTrueTypeSerialization() throws Exception {
    GameDataBuilder localBuilder = new GameDataBuilder();
    GameData localData = localBuilder.createSimpleGameData();
    // Add English translations for races
    localData.addTranslation("Dämonen", "Demons", TranslationType.sourceMagellan);
    localData.addTranslation("Katzen", "Cats", TranslationType.sourceMagellan);

    Unit unit = localData.getUnits().iterator().next();
    unit.setRace(localData.getRules().getRace(EresseaConstants.R_KATZEN));
    unit.setRealRace(localData.getRules().getRace(EresseaConstants.R_DAEMONEN));

    StringWriter myStrWriter = new StringWriter();
    CRWriter myWriter = new CRWriter(localData, null, myStrWriter);
    myWriter.writeSynchronously();
    myWriter.close();

    String writtenCr = myStrWriter.toString();
    System.out.println(writtenCr);

    GameData newGameData = new CRParser(null).read(new StringReader(writtenCr), new CompleteData(localBuilder
        .createSimpleGameData().getRules()));
    Unit unit2 = newGameData.getUnits().iterator().next();

    // Ensure the deserialization did work.
    assertEquals(unit2.getRace().getID(), EresseaConstants.R_DAEMONEN);
  }

}
