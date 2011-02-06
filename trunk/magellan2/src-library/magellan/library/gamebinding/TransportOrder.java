// class magellan.library.gamebinding.TransportOrder
// created on Nov 9, 2010
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
import magellan.library.UnitID;
import magellan.library.relation.TransportRelation;
import magellan.library.utils.OrderToken;
import magellan.library.utils.Resources;

/**
 * A TRANSPORTIERE order
 * 
 * @author stm
 */
public class TransportOrder extends UnitArgumentOrder {

  /**
   * @param tokens
   * @param text
   * @param target
   */
  public TransportOrder(List<OrderToken> tokens, String text, UnitID target) {
    super(tokens, text, target);
  }

  /**
   * @see magellan.library.gamebinding.SimpleOrder#execute(magellan.library.gamebinding.ExecutionState,
   *      magellan.library.GameData, magellan.library.Unit, int)
   */
  @Override
  public void execute(ExecutionState state, GameData data, Unit unit, int line) {
    // if (!isValid())
    // return;

    Unit tUnit = getTargetUnit(data, unit, line, false);
    if (tUnit != null && tUnit.getRegion() == unit.getRegion()) {
      if (tUnit == unit) {
        setWarning(unit, line, Resources.get("order.transport.warning.treflexive"));
      } else {
        TransportRelation relation = null;
        for (TransportRelation ride : unit.getRelations(TransportRelation.class)) {
          if (ride.source == unit && ride.origin == tUnit) {
            // ride relation
            ride.target = ride.origin;
            ride.origin = ride.source;
            ride.line = line;
            relation = ride;
          }
        }
        if (relation == null) {
          relation = new TransportRelation(unit, null, tUnit, line);
          relation.add();
          relation.setWarning(Resources.get("order.transport.warning.notriding", target),
              SimpleOrder.OrderProblem);
        }
      }
    } else {
      setWarning(unit, line, Resources.get("order.transport.warning.unknowntarget", target));
    }
  }

}
