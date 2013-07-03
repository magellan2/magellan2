// class magellan.library.io.AtlantisOrderWriterTest
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

import static org.junit.Assert.assertFalse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import magellan.library.gamebinding.AtlantisSpecificStuff;

import org.junit.Before;
import org.junit.Test;

public class AtlantisOrderWriterTest extends OrderWriterTest {

  @Before
  public void setUp() throws Exception {
    init("Atlantis");
  }

  @Override
  protected void setOrderWriter() {
    orderWriter = (OrderWriter) new AtlantisSpecificStuff().getOrderWriter();
  }

  @Test
  public final void test() throws IOException {
    orderWriter.write(bwriter);
    assertNotLines("REGION.*", "LOCALE.*", "NÄCHSTER", "NAECHSTER", "NEXT");
  }

  @Test
  public final void testWriteFooter() throws IOException {
    orderWriter.writeFooter(bwriter);
    assertLines(true);
  }

  @Test
  public final void testWriteRegionLine() throws IOException {
    orderWriter.writeRegionLine(bwriter, region);

    assertLines(true, ";REGION 0,0 ;Region_0_0");
  }

  protected void assertNotLines(String... regexps) throws IOException {
    bwriter.flush();
    BufferedReader reader = new BufferedReader(new StringReader(writer.toString()));
    String line = reader.readLine();
    int lnr;
    for (lnr = 0; lnr < regexps.length && line != null; ++lnr) {
      for (String regexp : regexps) {
        assertFalse("line " + line + " not allowed" + regexp, line.matches(regexp));
      }
      line = reader.readLine();
    }
  }

}
