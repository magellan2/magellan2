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
   * @param valid
   * @param target
   */
  public TransportOrder(List<OrderToken> tokens, String text, boolean valid, UnitID target) {
    super(tokens, text, valid, target);
  }

  @Override
  public void execute(ExecutionState state, GameData data, Unit unit, int line) {
    if (!isValid())
      return;

    Unit tUnit = getTargetUnit(data, unit, true);
    if (tUnit != null) {
      new TransportRelation(unit, tUnit, line).add();
    } else {
      setWarning(Resources.get("order.transport.warning.unknowntarget", target));
    }

  }

}
