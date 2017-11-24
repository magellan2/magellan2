// class magellan.library.gamebinding.SimpleOrderTest
// created on Nov 12, 2017
//
// Copyright 2003-2017 by magellan project team
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
package magellan.library.gamebinding;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import magellan.library.utils.OrderToken;

public class SimpleOrderTest {

  private static final OrderToken pToken = new OrderToken("@", 0, 1, OrderToken.TT_PERSIST, false);
  private static final OrderToken eToken = new OrderToken("!", 0, 1, OrderToken.TT_EXCLAM, false);

  @Test
  public void testSimpleOrderOrderTokenString() {
    SimpleOrder order = new SimpleOrder(new OrderToken("ARBEITE"), "ARBEITE");
    assertEquals(1, order.getTokens().size());
    assertEquals("", order.getPrefix());
    assertEquals("ARBEITE", order.getText());

  }

  @Test
  public void testSimpleOrderListOfOrderTokenString() {
    List<OrderToken> tokens = Arrays.asList(new OrderToken("ARBEITE"));
    SimpleOrder order = new SimpleOrder(tokens, "ARBEITE");
    assertEquals(1, order.getTokens().size());
    assertEquals("", order.getPrefix());
    assertEquals("ARBEITE", order.getText());
    tokens = Arrays.asList(eToken, pToken, new OrderToken("ARBEITE"));
    order = new SimpleOrder(tokens, "!@ARBEITE");
    assertEquals(1, order.getTokens().size());
    assertEquals("!@", order.getPrefix());
  }

  @Test
  public void testIsPersistent() {
    SimpleOrder order = new SimpleOrder(pToken, "@");
    assertEquals(true, order.isPersistent());
  }

  @Test
  public void testIsEmpty() {
    SimpleOrder order = new SimpleOrder(new OrderToken("ARBEITE"), "ARBEITE");
    assertEquals(false, order.isEmpty());

    order = new SimpleOrder(new OrderToken(OrderToken.TT_EOC), "");
    assertEquals(true, order.isEmpty());

    List<OrderToken> tokens = Arrays.asList(eToken, pToken);
    order = new SimpleOrder(tokens, "!@");
    assertEquals(true, order.isEmpty());

    tokens = Arrays.asList(eToken, pToken, new OrderToken("ARBEITE"));
    order = new SimpleOrder(tokens, "!@ARBEITE");
    assertEquals(false, order.isEmpty());
  }

}
