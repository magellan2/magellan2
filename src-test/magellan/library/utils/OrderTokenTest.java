// class magellan.library.utils.OrderTokenTest
// created on Jun 14, 2012
//
// Copyright 2003-2012 by magellan project team
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

import org.junit.Test;

public class OrderTokenTest {

  @Test
  public final void testEqualsCompletedToken() {
    OrderToken token = new OrderToken("ARBEITE", 0, 7, OrderToken.TT_KEYWORD, true);
    assertEquals(true, token.equalsCompletedToken("ARBEITE"));
    assertEquals(true, token.equalsCompletedToken("ARBEITEN"));
    assertEquals(false, token.equalsCompletedToken("ATTACKIERE"));
    token = new OrderToken("ARBEITE", 0, 7, OrderToken.TT_KEYWORD, false);
    assertEquals(true, token.equalsCompletedToken("ARBEITE"));
    assertEquals(true, token.equalsCompletedToken("ARBEITEN"));
    assertEquals(false, token.equalsCompletedToken("ATTACKIERE"));
  }

  @Test
  public final void testEqualsToken() {
    OrderToken token = new OrderToken("ARBEITE", 0, 7, OrderToken.TT_KEYWORD, true);
    assertEquals(true, token.equalsToken("ARBEITE"));
    assertEquals(true, token.equalsToken("ARBEITEN"));
    assertEquals(false, token.equalsToken("ATTACKIERE"));
    token = new OrderToken("ARBEITE", 0, 7, OrderToken.TT_KEYWORD, false);
    assertEquals(true, token.equalsToken("ARBEITE"));
    assertEquals(false, token.equalsToken("ARBEITEN"));
    assertEquals(false, token.equalsToken("ATTACKIERE"));
  }

}
