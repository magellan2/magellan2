// class magellan.library.gamebinding.SimpleOrderFactory
// created on Jan 10, 2015
//
// Copyright 2003-2015 by magellan project team
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

import java.util.List;

import magellan.library.Order;
import magellan.library.utils.OrderToken;

/**
 * @author stm
 */
public class SimpleOrderFactory {

  /**
   * Creates an order consisting of just one token.
   *
   * @param text The complete text of the order
   */
  public Order getInstance(String text) {
    return new SimpleOrder(new OrderToken(text), text);
  }

  /**
   * Creates a new order from a list of tokens.
   *
   * @param tokens The sequence of order tokens
   * @param text The complete text of the order
   */
  public Order getInstance(List<OrderToken> tokens, String text) {
    return new SimpleOrder(tokens, text);
  }

}
