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
import magellan.library.Item;
import magellan.library.StringID;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.gamebinding.EresseaRelationFactory.EresseaExecutionState;
import magellan.library.relation.ControlRelation;
import magellan.library.relation.PersonTransferRelation;
import magellan.library.relation.UnitRelation;
import magellan.library.relation.UnitTransferRelation;
import magellan.library.rules.ItemCategory;
import magellan.library.rules.ItemType;
import magellan.library.utils.OrderToken;
import magellan.library.utils.Resources;

/**
 * An order with one or more units as arguments.
 * 
 * @author stm
 */
public class GiveOrder extends UnitArgumentOrder {

  protected String type;
  protected boolean each;
  protected int amount;
  protected ItemType itemType;
  protected boolean all;

  /**
   * @param tokens
   * @param text
   * @param valid
   * @param type
   */
  public GiveOrder(List<OrderToken> tokens, String text, boolean valid, UnitID target, String type) {
    super(tokens, text, valid, target);
    this.type = type;
  }

  /**
   * Returns the value of type.
   * 
   * @return Returns type.
   */
  public String getType() {
    return type;
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
  public ItemType getItemType() {
    return itemType;
  }

  /**
   * Returns the value of all.
   * 
   * @return Returns all.
   */
  public boolean isAll() {
    return all;
  }

  @Override
  public void execute(ExecutionState state, GameData data, Unit unit, int line) {
    // GIB 0|<enr> (ALLES|EINHEIT|KRaeUTER|KOMMANDO|((([JE] <amount>)|ALLES)
    // (SILBER|PERSONEN|<gegenstand>)))
    if (!isValid())
      return;

    if (unit instanceof TempUnit && type != EresseaConstants.O_CONTROL) {
      setWarning(Resources.get("order.give.warning.temp"));
      return;
    }

    Unit tUnit = getTargetUnit(data, unit, true);

    if (tUnit != null) {
      EresseaExecutionState eState = (EresseaExecutionState) state;

      if (!tUnit.equals(unit)) {
        if (type == EresseaConstants.O_CONTROL) {
          UnitRelation rel = new ControlRelation(unit, tUnit, line);
          if (unit.getUnitContainer() == null || unit.getUnitContainer().getOwnerUnit() != unit) {
            // better warn too often than too rarely
            rel.warning = true;
            setWarning(Resources.get("order.give.warning.nocommand"));
          }
          rel.add();
        } else if (type == EresseaConstants.O_UNIT) {
          UnitRelation rel = new UnitTransferRelation(unit, tUnit, unit.getRace(), line);
          rel.add();
        } else if (type == EresseaConstants.O_MEN) {
          PersonTransferRelation rel =
              new PersonTransferRelation(unit, tUnit, -1, unit.getRace(), line, false);
          if (all) {
            rel.amount = unit.getModifiedPersons();
          } else {
            // if not, only transfer the minimum amount the unit has
            rel.amount = Math.min(unit.getModifiedPersons(), amount);
          }

          rel.add();
        } else if (type == EresseaConstants.O_HERBS) {
          // create relations for all herbs the unit carries
          ItemCategory herbCategory = data.rules.getItemCategory(StringID.create(("HERBS")));

          if ((herbCategory != null)) {
            for (ItemType i : eState.getHerbTypes()) {
              List<UnitRelation> relations = eState.giveItem(unit, tUnit, true, 0, i, line, this);
              for (UnitRelation rel : relations) {
                rel.add();
              }
            }
          }

        } else if (type == EresseaConstants.O_GIVE) {
          if (itemType != null) {
            List<UnitRelation> relations =
                eState.giveItem(unit, tUnit, all, each ? tUnit.getModifiedPersons() * amount
                    : amount, itemType, line, this);
            for (UnitRelation rel : relations) {
              rel.add();
            }
          } else {
            // in u case the order looks like:
            // GIVE <unit id> ALLES<EOC>
            if (all) {
              for (Item i : unit.getModifiedItems()) {

                List<UnitRelation> relations =
                    eState.giveItem(unit, tUnit, all, 0, i.getItemType(), line, this);
                for (UnitRelation rel : relations) {
                  rel.add();
                }
              }
            }
          }
        } else {
          setWarning(Resources.get("order.give.warning.unknowntype"));
        }
      } else {
        // relation to myself? you're sick
        // setValid(false);
        setWarning(Resources.get("order.give.warning.reflexive"));
      }
    } else {
      setWarning(Resources.get("order.give.warning.unknowntarget", target));
    }

  }

}
