// class magellan.library.gamebinding.FollowOrder
// created on Nov 15, 2010
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
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.relation.FollowShipRelation;
import magellan.library.relation.FollowUnitRelation;
import magellan.library.utils.OrderToken;
import magellan.library.utils.Resources;

/**
 * A FOLLOW order
 */
public class FollowShipOrder extends UCArgumentOrder {

  /**
   * @param tokens
   * @param text
   */
  public FollowShipOrder(List<OrderToken> tokens, String text, EntityID target) {
    super(tokens, text, target, UCArgumentOrder.T_SHIP);
  }

  @Override
  public void execute(ExecutionState state, GameData data, Unit unit, int line) {
    Ship target = (Ship) getContainer(data, unit, UCArgumentOrder.T_SHIP, true);
    if (!unit.getRelations(FollowUnitRelation.class).isEmpty()
        || !unit.getRelations(FollowShipRelation.class).isEmpty()) {
      setWarning(unit, line, Resources.get("order.follow.warning.duplicate"));
    }
    if (target != null) {

      if (target != unit.getShip()) {
        FollowShipRelation rel = new FollowShipRelation(unit, target, line);
        rel.add();
      } else {
        setWarning(unit, line, Resources.get("order.move.warning.unitfollowsself"));
      }
    } else {
      setWarning(unit, line, Resources.get("order.all.warning.unknowntarget", target));
    }
  }
}
