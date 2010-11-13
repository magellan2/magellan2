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
import magellan.library.Order;
import magellan.library.StringID;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaRelationFactory.EresseaExecutionState;
import magellan.library.relation.UnitRelation;
import magellan.library.rules.ItemType;
import magellan.library.utils.OrderToken;
import magellan.library.utils.Resources;

/**
 * A RESERVIERE order.
 * 
 * @author stm
 */
public class ReserveOrder extends SimpleOrder {

  protected boolean each;
  protected int amount;
  protected StringID itemID;

  /**
   * @param tokens
   * @param text
   * @param valid
   */
  public ReserveOrder(List<OrderToken> tokens, String text, boolean valid) {
    super(tokens, text, valid);
  }

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
  public StringID getItemType() {
    return itemID;
  }

  @Override
  public void execute(ExecutionState state, GameData data, Unit unit, int line) {
    if (!isValid())
      return;

    if (unit instanceof TempUnit) {
      setWarning(Resources.get("order.reserve.warning.temp"));
      return;
    }

    EresseaExecutionState eState = (EresseaExecutionState) state;

    ItemType itemType = data.getRules().getItemType(itemID);

    // RESERVE [EACH] <amount> <object><EOC>
    // RESERVIERE [JE] <amount> <object><EOC>
    // RESERVIERE ALLES <object><EOC>
    if (itemType != null) {
      boolean warning = false;
      int realAmount = amount;
      // get the item from the list of modified items
      if (amount == Order.ALL) {
        // if the specified amount is 'all', convert u to a decent number
        // TODO how exactly does RESERVIERE ALLES <item> work??
        // realAmount =
        // unit.getModifiedItem(itemType) != null ? unit.getModifiedItem(itemType).getAmount() : 0;
        // FIXME warn as long as this feature is broken
        warning = true;
        setWarning(Resources.get("order.reserve.warning.all"));
      } else {
        if (each) {
          // according to eressea source code it is modified persons (at this time, i.e. before
          // recruit etc.
          realAmount = amount * unit.getModifiedPersons();
        }
      }
      List<UnitRelation> relations =
          eState.reserveItem(itemType, amount == Order.ALL, realAmount, unit, line, this);
      for (UnitRelation rel : relations) {
        rel.warning |= warning;
        rel.add();
      }
    } else {
      setWarning(Resources.get("order.reserve.warning.unknownitem", itemID));
    }
  }

}
