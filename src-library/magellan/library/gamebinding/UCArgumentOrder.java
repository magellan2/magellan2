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

import magellan.library.Building;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.relation.UnitContainerRelation;
import magellan.library.utils.OrderToken;
import magellan.library.utils.Resources;

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
   * The order type, one of {@link #T_BUILDING}, {@link #T_FACTION}, {@link #T_SHIP},
   * {@link #T_REGION}, or {@link #T_UNKNOWN}.
   */
  public int type;

  /**
   * @param tokens
   * @param text
   * @param type
   */
  public UCArgumentOrder(List<OrderToken> tokens, String text, EntityID target, int type) {
    super(tokens, text);
    container = target;
    this.type = type;
  }

  /**
   * Returns the value of container.
   * 
   * @return Returns container.
   */
  public EntityID getContainer() {
    return container;
  }

  /**
   * @param data
   * @param unit
   * @param type
   * @param sameRegionOnly
   * @return The unit continer. It can be safely cast to {@link Building}, {@link Ship}, {@link Faction}, or
   *         {@link Region}, according to they value of type.
   */
  protected UnitContainer getContainer(GameData data, Unit unit, int type, boolean sameRegionOnly) {
    switch (type) {
    case T_BUILDING:
      return sameRegionOnly ? unit.getRegion().getBuilding(container) : data.getBuilding(container);
    case T_SHIP:
      return sameRegionOnly ? unit.getRegion().getShip(container) : data.getShip(container);
    case T_FACTION:
      return data.getFaction(container);
    case T_REGION:
      return unit.getRegion();
    }
    return null;
  }

  @Override
  public void execute(ExecutionState state, GameData data, Unit unit, int line) {
    if (!isValid())
      return;

    UnitContainer tContainer = getContainer(data, unit, type, true);

    if (tContainer != null) {
      new UnitContainerRelation(unit, tContainer, line).add();
    } else {
      setWarning(unit, line, Resources.get("order.all.warning.unknowntarget", container));
    }
  }

}
