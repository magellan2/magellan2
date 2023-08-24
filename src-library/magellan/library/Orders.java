// class magellan.library.impl.Orders
// created on Jul 29, 2010
//
// Copyright 2003-2010 by magellan project team
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
package magellan.library;

import java.util.List;

import magellan.library.gamebinding.EresseaConstants;
import magellan.library.utils.OrderToken;

/**
 * A class representing a unit's orders. It extends the List interface, but only supports the
 * read-only methods.
 */
public interface Orders extends List<Order> {

  /**
   * Removes orders that match the given order up to a given length.
   * 
   * @param order pattern to remove
   * @param length denotes the number of tokens that need to be equal for a replacement. E.g.
   *          specify 2 if order is "BENENNE EINHEIT abc" and all "BENENNE EINHEIT" orders should be
   *          replaced but not all "BENENNE" orders.
   * @return <kbd>true</kbd> if at least one order was removed
   */
  public abstract boolean removeOrder(Order order, int length);

  /**
   * Returns the unit owning these orders. It is not guaranteed, that this orders object is actually
   * attached to the unit.
   * 
   * @return The unit owning these orders.
   */
  public abstract Unit getUnit();

  /**
   * Compares an order token to a order constant. This method translates the token to the unit's
   * order locale and compares it to the specified order token.
   * 
   * @param order An order
   * @param pos A token position
   * @param token An order name. Usually one of the order ("O_...") constants from
   *          {@link EresseaConstants}.
   * @return <code>true</code> if the order token at the position is a significant prefix of the
   *         translation of the given order token
   * @see OrderToken#equalsLocalToken(String)
   */
  public boolean isToken(Order order, int pos, StringID token);

  // /**
  // * @return The base (radix) for ids used by this orders
  // */
  // public abstract int getBase();
  //
  // /**
  // * Converts an order token to EntityID
  // *
  // * @param order
  // * @param pos A position between 0 (inclusively) and order.size() (exclusively)
  // * @return a unit ID corresponding to the specified token of the order
  // */
  // public abstract EntityID getEntityID(Order order, int pos);
  //
  // /**
  // * Converts an order token to UnitID
  // *
  // * @param order
  // * @param pos A position between 0 (inclusively) and order.size() (exclusively)
  // * @return a unit ID corresponding to the specified token of the order
  // */
  // public abstract UnitID getUnitID(Order order, int pos);

  /**
   * Returns the integer number represented by the specified token in the order.
   */
  public int getNumber(Order order, int pos);

  // public abstract Orders getView();

}