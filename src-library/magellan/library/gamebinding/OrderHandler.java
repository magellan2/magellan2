// class magellan.library.gamebinding.OrderHandler
// created on Apr 16, 2013
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
package magellan.library.gamebinding;

import magellan.library.Order;
import magellan.library.completion.OrderParser;
import magellan.library.utils.OrderToken;

/**
 * An OrderHandler parses an order and create an {@link Order}.
 *
 * @author stm
 */
public abstract class OrderHandler {

  private final OrderParser orderParser;

  /**
   * @param abstractOrderParser
   */
  protected OrderHandler(OrderParser abstractOrderParser) {
    orderParser = abstractOrderParser;
  }

  protected SimpleOrder order;
  protected boolean valid;

  /**
   * Reads an order.
   *
   * @param token First token of the order ("GIB", "LERNE" or the like) that must match the concrete
   *          OrderHandler class.
   * @return true if the order is valid
   */
  public boolean read(OrderToken token, String text) {
    init(token, text);
    valid = readIt(token);

    getOrder().setValid(valid);
    postProcess();

    return getOrder().isValid();
  }

  /**
   * Called before readIt. Initialize state here, i.e., all fields that are set while the order is
   * parsed.
   */
  protected void init(OrderToken token, String text) {
    order = new SimpleOrder(orderParser.getTokens(), text);
  }

  protected void postProcess() {
    // nothing to do, but can be overwritten
  }

  /**
   * Reads an order.
   *
   * @param token First token of the order ("GIB", "LERNE" or the like) that must match the concrete
   *          OrderHandler class.
   * @return true if the order is valid
   */
  protected abstract boolean readIt(OrderToken token);

  /**
   * @return The order
   */
  public SimpleOrder getOrder() {
    return order;
  }

}