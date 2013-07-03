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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.EresseaSpecificStuff;
import magellan.library.gamebinding.GameSpecificOrderReader.Status;
import magellan.library.io.MockReader;
import magellan.library.utils.OrderReader.LineHandler;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

import org.junit.Before;
import org.junit.Test;

public class OrderReaderTest extends MagellanTestWithResources {

  private GameDataBuilder builder;
  private GameData data;
  private OrderReader orderReader;
  private MockReader reader;
  private boolean echeck = true;
  private boolean addLocale = true;
  private Faction faction;
  private Region region;
  private Unit unit;

  @Before
  public void setUp() throws Exception {
    builder = new GameDataBuilder();
    data = builder.createSimpleGameData();
    faction = data.getFactions().iterator().next();
    region = data.getRegions().iterator().next();
    unit = data.getUnits().iterator().next();

    orderReader = new EresseaSpecificStuff().getOrderReader(data);
    reader = new MockReader();
  }

  @Test
  public final void testRead() throws IOException {
    addEresseaHeader();
    addFooter();
    orderReader.read(reader);
    Status status = orderReader.getStatus();
    assertEquals(0, status.errors);
    assertEquals(0, status.confirmedUnitsNotOverwritten);
    assertEquals(1, status.factions);
    assertEquals(0, status.units);
    assertEquals(0, status.unknownUnits);
    assertEquals("de", orderReader.getLocale().getLanguage());
  }

  @Test
  public final void testGetHandlers() throws IOException {
    List<LineHandler> handlers = orderReader.getHandlers("E");
    assertEquals(2, handlers.size());
    handlers = orderReader.getHandlers("");
    assertEquals(5, handlers.size());
    handlers = orderReader.getHandlers("ER");
    assertEquals(1, handlers.size());
    handlers = orderReader.getHandlers("ERESSEA");
    assertEquals(1, handlers.size());
  }

  @Test
  public final void testRead2() throws IOException {
    addUnit(unit);
    addFooter();
    orderReader.read(reader);
    Status status = orderReader.getStatus();
    assertEquals(1, status.errors);
  }

  @Test
  public final void testDefaultHandle() throws IOException {
    addEresseaHeader();
    addFooter();
    reader.addLine("VERDAKEN");
    orderReader.read(reader);
    Status status = orderReader.getStatus();
    assertEquals(1, status.errors);
  }

  @Test
  public final void testDefaultHandle2() throws IOException {
    addEresseaHeader();
    addUnit(unit);
    reader.addLine("VERDAKEN");
    addFooter();
    orderReader.read(reader);
    Status status = orderReader.getStatus();
    assertEquals(0, status.errors);
    assertEquals(1, unit.getOrders2().size());
    assertEquals("VERDAKEN", unit.getOrders2().get(0).getText());
  }

  @Test
  public final void testCommentHandle() throws IOException {
    addEresseaHeader();
    addUnit(unit);
    reader.addLine("; some comment");
    reader.addLine(" ; 2nd comment");
    reader.addLine(";bestaetigt");
    addFooter();

    orderReader.read(reader);
    Status status = orderReader.getStatus();
    assertEquals(0, status.errors);
    assertEquals(2, unit.getOrders2().size());
    assertEquals("; some comment", unit.getOrders2().get(0).getText());
    assertEquals(" ; 2nd comment", unit.getOrders2().get(1).getText());
    assertTrue(unit.isOrdersConfirmed());
  }

  @Test
  public final void testCommentHandle2() throws IOException {
    addEresseaHeader();
    addUnit(unit);
    reader.addLine("; some comment");
    reader.addLine(";bestaetigt");
    addFooter();

    orderReader.setIgnoreSemicolonComments(true);
    orderReader.read(reader);
    Status status = orderReader.getStatus();
    assertEquals(0, status.errors);
    assertEquals(0, unit.getOrders2().size());
    assertTrue(unit.isOrdersConfirmed());
  }

  @Test
  public final void testEndFaction2() {
    orderReader.currentFaction = faction;
    orderReader.currentUnit = unit;
    orderReader.currentRegion = "Blaregion";

    orderReader.endFaction();
    assertNull(orderReader.currentFaction);
    assertNull(orderReader.currentRegion);
    assertNull(orderReader.currentUnit);
  }

  @Test
  public final void testEndFaction() throws IOException {
    orderReader.new StartingHandler().handle("FACTION iLja foo", null);
    orderReader.new RegionHandler().handle(orderReader.command = "REGION 0,0 ; null", null);
    orderReader.new UnitHandler().handle(orderReader.command = "UNIT " + unit.getID().toString(),
        null);

    assertNotNull(orderReader.currentFaction);
    assertNotNull(orderReader.currentRegion);
    assertNotNull(orderReader.currentUnit);

    orderReader.new NextHandler().handle("NAECHSTER", null);

    assertNull(orderReader.currentFaction);
    assertNull(orderReader.currentRegion);
    assertNull(orderReader.currentUnit);
  }

  @Test
  public void testLocale() throws IOException {
    setOrderLocale(EN_LOCALE);

    addEresseaHeader();
    addUnit(unit);
    assertTrue(reader.getContent().contains("UNIT 1"));
    reader.addLine("VERDAKEN");
    addFooter();
    orderReader.read(reader);
    Status status = orderReader.getStatus();
    assertEquals(0, status.errors);
    assertEquals(1, unit.getOrders2().size());
    assertEquals("VERDAKEN", unit.getOrders2().get(0).getText());
  }

  protected void setOrderLocale(Locale alocale) {
    MagellanTestWithResources.setLocale(alocale);
    orderReader.setLocale(alocale);
  }

  protected String getOrderTranslation(StringID orderId) {
    return data.getGameSpecificStuff().getOrderChanger().getOrder(getLocale(), orderId);
  }

  protected void addEresseaHeader() {
    reader.add(data.getRules().getOrderfileStartingString()).add(" ").add(
        faction.getID().toString()).add(" \"foo\"");
    if (echeck) {
      reader.addLine("; TIMESTAMP 1292032043485");
      reader.addLine("; Magellan Version 2.0.5 (build 42)");
      reader.addLine("; ECHECK -r80 -s -l -w4 -v4.3.2");
    }
    if (addLocale) {
      reader.addLine("LOCALE " + getLocale().getLanguage());
    }
  }

  protected void addFooter() {
    reader.addLine(getOrderTranslation(EresseaConstants.OC_NEXT));
  }

  protected void addRegion(Region r) {
    reader.add(getOrderTranslation(EresseaConstants.OC_REGION)).add(" ").add(
        r.getCoordinate().toString(",")).add("; ").add(r.getName());
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
