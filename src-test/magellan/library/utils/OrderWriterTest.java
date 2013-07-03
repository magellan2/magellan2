// class magellan.library.utils.OrderWriterTest
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;

import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.io.MockWriter;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

public abstract class OrderWriterTest extends MagellanTestWithResources {

  private GameDataBuilder builder;
  private GameData data;
  protected MockWriter writer;
  protected BufferedWriter bwriter;
  protected Faction faction;
  protected Region region;
  protected Unit unit;
  protected OrderWriter orderWriter;

  public void init(String gameName) throws Exception {
    builder = new GameDataBuilder();
    builder.setGameName(gameName);
    data = builder.createSimpleGameData();
    faction = data.getFactions().iterator().next();
    region = data.getRegions().iterator().next();
    unit = data.getUnits().iterator().next();

    setOrderWriter();
    orderWriter.setGameData(data);
    orderWriter.setFaction(faction);

    writer = new MockWriter();
    bwriter = new BufferedWriter(writer);

  }

  protected abstract void setOrderWriter();

  protected void assertLines(boolean exact, String... regexps) throws IOException {
    bwriter.flush();
    BufferedReader reader = new BufferedReader(new StringReader(writer.toString()));
    String line = reader.readLine();
    int lnr;
    for (lnr = 0; lnr < regexps.length && line != null; ++lnr) {
      assertTrue("line " + lnr + "\n" + line + "\ndoes not match\n" + regexps[lnr], line
          .matches(regexps[lnr]));
      line = reader.readLine();
    }
    assertEquals(regexps.length, lnr);
    if (exact) {
      assertEquals("too many lines", null, line);
    }
  }

}