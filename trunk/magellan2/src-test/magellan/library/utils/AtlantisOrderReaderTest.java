// class magellan.library.utils.OrderReaderTest
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

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.GameSpecificOrderReader.Status;
import magellan.library.gamebinding.atlantis.AtlantisSpecificStuff;
import magellan.library.io.MockReader;
import magellan.library.utils.OrderReader.LineHandler;
import magellan.library.utils.logging.Logger;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AtlantisOrderReaderTest extends MagellanTestWithResources {

  private GameDataBuilder builder;
  private GameData data;
  private OrderReader orderReader;
  private MockReader reader;
  private boolean echeck = true;
  private boolean addLocale = true;
  private Faction faction;
  private Region region;
  private Unit unit;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    setLocale(EN_LOCALE);
    Logger.setLevel(Logger.WARN);
    initResources();
  }

  @Before
  public void setUp() throws Exception {
    builder = new GameDataBuilder();
    builder.setGameName("Atlantis");
    data = builder.createSimpleGameData();
    faction = data.getFactions().iterator().next();
    region = data.getRegions().iterator().next();
    unit = data.getUnits().iterator().next();

    orderReader = new AtlantisSpecificStuff().getOrderReader(data);
    reader = new MockReader();
    setOrderLocale(EN_LOCALE);
  }

  @Test
  public final void testRead() throws IOException {
    addAtlantisHeader();
    addFooter();
    orderReader.read(reader);
    Status status = orderReader.getStatus();
    assertEquals(0, status.errors);
    assertEquals(0, status.confirmedUnitsNotOverwritten);
    assertEquals(1, status.factions);
    assertEquals(0, status.units);
    assertEquals(0, status.unknownUnits);
    assertEquals("en", orderReader.getLocale().getLanguage());
  }

  @Test
  public final void testGetHandlers() throws IOException {
    orderReader.addHandler("FOOBAR", orderReader.new UnitHandler());

    List<LineHandler> handlers = orderReader.getHandlers("F");
    assertEquals(0, handlers.size());
    handlers = orderReader.getHandlers("");
    assertEquals(0, handlers.size());
    handlers = orderReader.getHandlers("FOOBAR");
    assertEquals(1, handlers.size());
  }

  protected void setOrderLocale(Locale alocale) {
    MagellanTestWithResources.setLocale(alocale);
    orderReader.setLocale(alocale);
  }

  protected String getOrderTranslation(StringID orderId) {
    return data.getGameSpecificStuff().getOrderChanger().getOrder(getLocale(), orderId).getText();
  }

  protected void addAtlantisHeader() {
    reader.add(data.getRules().getOrderfileStartingString()).add(" ").add(
        faction.getID().toString()).add(" \"foo\"");
    if (echeck) {
      reader.addLine("; TIMESTAMP 1292032043485");
      reader.addLine("; Magellan Version 2.0.5 (build 42)");
      reader.addLine("; ECHECK -r80 -s -l -w4 -v4.3.2");
    }
  }

  protected void addFooter() {
    // no footer
  }

  protected void addRegion(Region r) {
    // no region tag
  }

  protected void addUnit(Unit unit) {
    reader.add(getOrderTranslation(EresseaConstants.OC_UNIT)).add(" ").add(unit.getID().toString());
    if (echeck) {
      reader.addLine(";           Weltenforscher [1,480$,Srzgm]");
    } else {
      reader.addLine("");
    }
  }

}
