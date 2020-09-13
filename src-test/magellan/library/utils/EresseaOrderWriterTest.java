// class magellan.library.gamebinding.EresseaOrderWriterTest
// created on Apr 24, 2013
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
package magellan.library.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import magellan.library.gamebinding.EresseaOrderWriter;
import magellan.library.gamebinding.EresseaSpecificStuff;

public class EresseaOrderWriterTest extends OrderWriterTestUtil {

  @Before
  public void setUp() throws Exception {
    init("Eressea");
  }

  @Override
  protected void setOrderWriter() {
    orderWriter = (OrderWriter) new EresseaSpecificStuff().getOrderWriter();
  }

  @Test
  public final void testWriteBufferedWriter() throws IOException {
    orderWriter.write(writer);
    writer.flush();
    assertTrue(writer.toString().startsWith("ERESSEA iLja \"Faction_867718\""));
  }

  @Test
  public final void testWriteHeader() throws IOException {
    orderWriter.write(writer);
    assertLines(false, "ERESSEA iLja \"Faction_867718\"", ";TIMESTAMP [0-9]+",
        ";Magellan Version .+", "; ECHECK -r80 -s -l -w4 -v4.3.2", "LOCALE de");
  }

  @Test
  public final void testWriteOrderfileStartingString() throws IOException {
    orderWriter.writeOrderfileStartingString(bwriter);

    assertLines(true, "ERESSEA iLja \"Faction_867718\"");
  }

  @Test
  public final void testWriteLocale() throws IOException {
    orderWriter.writeLocale(bwriter);

    assertLines(true, "LOCALE de");
  }

  @Test
  public final void testWriteRegionLine() throws IOException {
    orderWriter.writeRegionLine(bwriter, region);

    assertLines(true, "REGION 0,0 ; Region_0_0", "; ECheck Lohn 0");
  }

  @Test
  public final void testWriteRegion() throws IOException {
    unit.clearOrders();
    orderWriter.writeRegion(region, region.units(), bwriter);

    assertLines(true, "REGION 0,0 ; Region_0_0", "; ECheck Lohn 0",
        "EINHEIT 1;\\s+Unit_1 \\[1,0\\$\\]");
  }

  @Test
  public final void testWriteUnitLine() throws IOException {
    orderWriter.writeUnitLine(bwriter, unit);

    assertLines(true, "EINHEIT 1;\\s+Unit_1 \\[1,0\\$\\]");
  }

  @Test
  public final void testWriteUnit() throws IOException {
    unit.clearOrders();
    unit.addOrder("LAGERdoek");
    orderWriter.writeUnit(unit, bwriter);

    assertLines(true, "EINHEIT 1;\\s+Unit_1 \\[1,0\\$\\]", "LAGERdoek");
  }

  @Test
  public final void testWriteOrders() throws IOException {
    unit.clearOrders();
    unit.addOrder("; hello");
    unit.addOrder("// hello");
    unit.addOrder("hello");
    orderWriter.setRemoveComments(false, false);
    orderWriter.writeOrders(unit.getOrders2(), bwriter);
    assertLines(true, "; hello", "// hello", "hello");
  }

  @Test
  public final void testWriteOrders1() throws IOException {
    unit.clearOrders();
    unit.addOrder("; hello");
    unit.addOrder("// hello");
    unit.addOrder("hello");
    orderWriter.setRemoveComments(true, false);
    orderWriter.writeOrders(unit.getOrders2(), bwriter);
    assertLines(true, "// hello", "hello");
  }

  @Test
  public final void testWriteOrders2() throws IOException {
    unit.clearOrders();
    unit.addOrder("; hello");
    unit.addOrder("// hello");
    unit.addOrder("hello");
    orderWriter.setRemoveComments(false, true);
    orderWriter.writeOrders(unit.getOrders2(), bwriter);
    assertLines(true, "; hello", "hello");
  }

  @Test
  public final void testWriteFooter() throws IOException {
    orderWriter.writeFooter(bwriter);
    assertLines(true, "NÄCHSTER");
  }

  @Test
  public final void testWriteFooterEn() throws IOException {
    faction.setLocale(EN_LOCALE);
    orderWriter.writeFooter(bwriter);
    assertLines(true, "NEXT");
  }

  @Test
  public final void testWriteln() throws IOException {
    orderWriter.setForceUnixLineBreaks(false);
    orderWriter.writeln(bwriter, "test123");
    bwriter.flush();
    assertEquals("test123" + System.getProperty("line.separator"), writer.toString());
  }

  @Test
  public final void testWritelnUnix() throws IOException {
    orderWriter.setForceUnixLineBreaks(true);
    orderWriter.writeln(bwriter, "test123");
    bwriter.flush();
    assertEquals("test123\n", writer.toString());
  }

  @Test
  public final void testWriteCommentLine() throws IOException {
    orderWriter.writeCommentLine(bwriter, "comment123");
    bwriter.flush();
    assertEquals(";comment123" + System.lineSeparator(), writer.toString());
  }

  @Test
  public final void testGetSyntaxCheckOptions() throws IOException {
    orderWriter.setFaction(null);
    assertEquals(" -s -l -w4 -v" + EresseaOrderWriter.ECHECKVERSION, orderWriter
        .getSyntaxCheckOptions());
  }

  @Test
  public final void testGetSyntaxCheckOptions2() throws IOException {
    assertEquals("-r80 -s -l -w4 -v" + EresseaOrderWriter.ECHECKVERSION, orderWriter
        .getSyntaxCheckOptions());
  }

}
