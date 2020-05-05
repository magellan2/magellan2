// class magellan.library.gamebinding.EnterOrder
// created on Oct 12, 2010
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

import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.relation.EnterRelation;
import magellan.library.relation.LeaveRelation;
import magellan.library.utils.OrderToken;
import magellan.library.utils.Resources;

/**
 * Represents a BETRETE order
 * 
 * @author stm
 */
public class EnterOrder extends UCArgumentOrder {

  /**
   * @param tokens
   * @param text
   * @param type The order type, either BUILDING or SHIP.
   */
  public EnterOrder(List<OrderToken> tokens, String text, int type) {
    super(tokens, text, null, type);
  }

  /**
   * @see magellan.library.gamebinding.SimpleOrder#execute(magellan.library.gamebinding.ExecutionState,
   *      magellan.library.GameData, magellan.library.Unit, int)
   */
  @Override
  public void execute(ExecutionState state, GameData data, Unit unit, int line) {
    if (!isValid())
      return;

    UnitContainer target = getContainer(data, unit, type, true);
    // check whether the unit leaves a container
    UnitContainer leftUC = unit.getModifiedBuilding();
    if (leftUC == null) {
      leftUC = unit.getModifiedShip();
    }

    EnterRelation enter = null;
    if (target != null) {
      enter = new EnterRelation(unit, target, line);
    } else {
      setWarning(unit, line, Resources.get("order.enter.warning.unknowntarget", container));
    }

    if (leftUC != null && leftUC != this) {
      LeaveRelation leave = new LeaveRelation(unit, leftUC, line, true);
      leave.add();
    }
    if (enter != null) {
      enter.add();
    }
  }

}
