// class magellan.library.gamebinding.ReserveOrder
// created on Aug 12, 2010
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
package magellan.library.gamebinding;

import java.util.List;

import magellan.library.EntityID;
import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.utils.OrderToken;

/**
 * An order with one or more units as arguments.
 * 
 * @author stm
 */
public class UCArgumentOrder extends SimpleOrder {

  /**
   * Marks an unknown type
   */
  public static final int T_UNKNOWN = 0;
  /**
   * Marks a BURG order.
   */
  public static final int T_BUILDING = 1;
  /**
   * Marks a ship order.
   */
  public static final int T_SHIP = 2;
  /**
   * Marks a ship order.
   */
  public static final int T_FACTION = 3;
  /**
   * Marks a ship order.
   */
  public static final int T_REGION = 4;

  protected EntityID container;

  /**
   * @param tokens
   * @param text
   * @param valid
   */
  public UCArgumentOrder(List<OrderToken> tokens, String text, boolean valid, EntityID target) {
    super(tokens, text, valid);
    container = target;
  }

  // /**
  // * @param subList
  // * @param subList2
  // * @param valid
  // * @param persistent
  // */
  // public UCArgumentOrder(List<OrderToken> subList, List<OrderToken> subList2, boolean valid,
  // boolean persistent, UnitContainer target) {
  // super(subList, subList2, valid, persistent);
  // container = target;
  // }

  /**
   * Returns the value of container.
   * 
   * @return Returns container.
   */
  public EntityID getContainer() {
    return container;
  }

  protected UnitContainer getContainer(GameData data, Unit unit, int type, boolean sameRegionOnly) {
    switch (type) {
    case T_BUILDING:
      return sameRegionOnly ? unit.getRegion().getBuilding(container) : data.getBuilding(container);
    case T_SHIP:
      return sameRegionOnly ? unit.getRegion().getShip(container) : data.getShip(container);
    case T_FACTION:
      return data.getFaction(container);
    }
    return null;
  }

}
