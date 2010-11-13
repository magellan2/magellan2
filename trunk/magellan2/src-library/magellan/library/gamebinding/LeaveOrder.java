// class magellan.library.gamebinding.LeaveOrder
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
import magellan.library.relation.LeaveRelation;
import magellan.library.utils.OrderToken;
import magellan.library.utils.Resources;

/**
 * A leave order (VERLASSE)
 * 
 * @author stm
 */
public class LeaveOrder extends SimpleOrder {

  /**
   * @param tokens
   * @param text
   * @param valid
   */
  public LeaveOrder(List<OrderToken> tokens, String text, boolean valid) {
    super(tokens, text, valid);
  }

  @Override
  public void execute(ExecutionState state, GameData data, Unit unit, int line) {
    if (isValid()) {
      UnitContainer uc = unit.getUnitContainer();

      if (uc != null) {
        LeaveRelation rel = new LeaveRelation(unit, uc, line);
        rel.add();
      } else {
        setWarning(Resources.get("order.leave.warning.nocontainer"));
      }
    }
  }

}
