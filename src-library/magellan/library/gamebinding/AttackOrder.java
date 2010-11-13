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

import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.relation.AttackRelation;
import magellan.library.rules.ItemType;
import magellan.library.utils.OrderToken;
import magellan.library.utils.Resources;

/**
 * A RESERVIERE order
 * 
 * @author stm
 */
public class AttackOrder extends UnitArgumentOrder {

  protected boolean each;
  protected int amount;
  protected ItemType itemType;

  /**
   * @param tokens
   * @param text
   * @param valid
   */
  public AttackOrder(List<OrderToken> tokens, String text, boolean valid, UnitID target) {
    super(tokens, text, valid, target);
  }

  // /**
  // * @param subList
  // * @param subList2
  // * @param valid
  // * @param persistent
  // */
  // public AttackOrder(List<OrderToken> subList, List<OrderToken> subList2, boolean valid,
  // boolean persistent, Unit target) {
  // super(subList, subList2, valid, persistent, target);
  // }

  /**
   * Returns the value of each.
   * 
   * @return Returns each.
   */
  public boolean isEach() {
    return each;
  }

  /**
   * Returns the value of amount.
   * 
   * @return Returns amount.
   */
  public int getAmount() {
    return amount;
  }

  /**
   * Returns the value of itemType.
   * 
   * @return Returns itemType.
   */
  public ItemType getItemType() {
    return itemType;
  }

  /**
   * @see magellan.library.gamebinding.SimpleOrder#execute(magellan.library.gamebinding.ExecutionState,
   *      magellan.library.GameData, magellan.library.Unit, int)
   */
  @Override
  public void execute(ExecutionState state, GameData data, Unit unit, int line) {
    if (!isValid())
      return;

    Unit tUnit = getTargetUnit(data, unit, true);
    if (tUnit != null) {
      AttackRelation rel = new AttackRelation(unit, tUnit, line);
      rel.add();
    } else {
      setWarning(Resources.get("order.attack.warning.unknowntarget", target));
    }

  }

}
