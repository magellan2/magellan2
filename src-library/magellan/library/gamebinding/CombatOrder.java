// class magellan.library.gamebinding.CombatOrder
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
import magellan.library.relation.CombatStatusRelation;
import magellan.library.relation.UnitRelation;
import magellan.library.utils.OrderToken;

/**
 * A combat status order (KÄMPFE). Not an attack order.
 * 
 * @author stm
 */
public class CombatOrder extends SimpleOrder {

  private int status = EresseaConstants.CS_HELPYES;

  /**
   * @param tokens
   * @param text
   * @param valid
   */
  public CombatOrder(List<OrderToken> tokens, String text, boolean valid) {
    super(tokens, text, valid);
  }

  /**
   * @see magellan.library.gamebinding.SimpleOrder#execute(magellan.library.gamebinding.ExecutionState,
   *      magellan.library.GameData, magellan.library.Unit, int)
   */
  @Override
  public void execute(ExecutionState state, GameData data, Unit unit, int line) {
    if (!isValid())
      return;

    UnitRelation rel;
    switch (status) {
    case EresseaConstants.CS_HELPYES:
      rel = new CombatStatusRelation(unit, false, line);
      break;
    case EresseaConstants.CS_HELPNOT:
      rel = new CombatStatusRelation(unit, true, line);
      break;

    default:
      rel = new CombatStatusRelation(unit, status, line);
      break;
    }
    rel.add();
  }

  /**
   * Changes the status value
   */
  public void setStatus(int status) {
    this.status = status;
  }

  /**
   * Returns the value of status.
   * 
   * @return Returns status.
   */
  public int getStatus() {
    return status;
  }

}
