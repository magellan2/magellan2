// class magellan.client.swing.map.AdvancedTextCellRendererTest
// created on May 3, 2022
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
package magellan.client.swing.map;

import static org.junit.Assert.assertEquals;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import magellan.client.Client;
import magellan.client.ClientProvider;
import magellan.library.GameData;
import magellan.test.GameDataBuilder;

public class AdvancedTextCellRendererTest {

  private static GameData data;
  private static Client client;
  private static CellGeometry cg;
  private static Dimension cellSize;
  private static AdvancedTextCellRenderer atcr;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    cg = new CellGeometry() {

      @Override
      public Dimension getCellSize() {
        return cellSize;
      }
    };
    data = new GameDataBuilder().createSimplestGameData();
    client = ClientProvider.getClient();
  }

  @Before
  public void setUp() {
    atcr = new AdvancedTextCellRenderer(cg, client.getMagellanContext());
    BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    atcr.init(data, bi.getGraphics(), new Rectangle());
  }

  private void adjustCell(int w, int h) {
    if (w < 0) {
      w = 9999;
    }
    if (h < 0) {
      h = 9999;
    }
    cellSize = new Dimension((int) Math.ceil(w / .9), h);
  }

  @Test
  public void testCutString() {
    atcr.setBreakLines(false);

    // assumptions about font size
    int xw = atcr.getWidth("x");
    int dotw = atcr.getWidth(".");

    adjustCell(xw + xw / 2, -1);

    // one x fits
    String[] x = atcr.breakString("x", cg);
    assertEquals(1, x.length);
    assertEquals("x", x[0]);

    // two x don't fit
    x = atcr.breakString("xx", cg);
    assertEquals(1, x.length);
    assertEquals("x", x[0]);

    adjustCell(xw + dotw * 3, -1);
    // assertEquals(18, (int) (cg.getCellSize().width * 0.9));
    // ellipsis
    x = atcr.breakString("xxxx", cg);
    assertEquals(1, x.length);
    assertEquals("x...", x[0]);

    adjustCell(xw + dotw * 3 - 1, -1);
    // assertEquals(16, (int) (cg.getCellSize().width * 0.9));
    // ellipsis
    x = atcr.breakString("xxxx", cg);
    assertEquals(1, x.length);
    assertEquals("x..", x[0]);

    adjustCell(2 * xw, -1);
    x = atcr.breakString("xx", cg);
    assertEquals(1, x.length);
    assertEquals("xx", x[0]);
  }

  @Test
  public void testBreakString() {
    atcr.setBreakLines(true);

    // assumptions about font size
    int xw = atcr.getWidth("x");
    int spw = atcr.getWidth(" ");

    adjustCell(xw + spw, -1);

    // one x fits
    String[] x = atcr.breakString("x", cg);
    assertEquals(1, x.length);
    assertEquals("x", x[0]);

    // two x don't fit
    x = atcr.breakString("xx", cg);
    assertEquals(2, x.length);
    assertEquals("x", x[0]);

    // eat whitespace between new line breaks
    x = atcr.breakString("x  x", cg);
    assertEquals(2, x.length);
    assertEquals("x", x[0]);
    assertEquals("x", x[1]);

    // break at newlines
    x = atcr.breakString("x\nx", cg);
    assertEquals(2, x.length);
    assertEquals("x", x[0]);
    assertEquals("x", x[1]);

    // keep multiple newlines
    x = atcr.breakString("x\n \nx", cg);
    assertEquals(3, x.length);
    assertEquals("x", x[0]);
    assertEquals("", x[1]);
    assertEquals("x", x[2]);

    // keep leading space after intentional newlines
    x = atcr.breakString("x\n x x", cg);
    assertEquals(3, x.length);
    assertEquals("x", x[0]);
    assertEquals(" x", x[1]);
    assertEquals("x", x[2]);
  }

  @Test
  public void testBreakTooLong() {
    atcr.setBreakLines(true);
    int xw = atcr.getWidth("x");
    int mw = atcr.getWidth("M");
    adjustCell(Math.max(mw, xw) - 1, -1);

    // if one part does not fit, delete everything
    String[] x = atcr.breakString("xMx", cg);
    assertEquals(0, x.length);

    x = atcr.breakString("xx", cg);
    assertEquals(2, x.length);
  }

  @Test
  public void testBreakTooHigh() {
    atcr.setBreakLines(true);

    // assumptions about font size
    int xh = atcr.getMaxHeight(new String[] { "X" });
    adjustCell(-1, xh);

    // one x fits
    String[] x = atcr.breakString("X", cg);
    assertEquals(1, x.length);
    assertEquals("X", x[0]);

    // skip second line
    x = atcr.breakString("X\nX", cg);
    assertEquals(1, x.length);
    assertEquals("X", x[0]);

    adjustCell(-1, xh * 2);
    x = atcr.breakString("X\nX", cg);
    assertEquals(2, x.length);
    assertEquals("X", x[0]);
    assertEquals("X", x[1]);

    adjustCell(-1, xh * 2 + 1);
    x = atcr.breakString("X\nX\nX", cg);
    assertEquals(2, x.length);
    assertEquals("X", x[0]);
    assertEquals("X", x[1]);

    adjustCell(-1, xh * 3 - 1);
    x = atcr.breakString("X\nX\nX", cg);
    assertEquals(2, x.length);
    assertEquals("X", x[0]);
    assertEquals("X", x[1]);
  }
}
