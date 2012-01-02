// class magellan.library.gamebinding.RecruitmentOrder
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
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaRelationFactory.EresseaExecutionState;
import magellan.library.relation.RecruitmentRelation;
import magellan.library.relation.ReserveRelation;
import magellan.library.relation.UnitRelation;
import magellan.library.rules.Race;
import magellan.library.tasks.OrderSyntaxInspector.OrderSemanticsProblemTypes;
import magellan.library.utils.OrderToken;
import magellan.library.utils.Resources;

/**
 * A recruiting order (REKRUTIERE)
 * 
 * @author stm
 */
public class RecruitmentOrder extends SimpleOrder {

  private Race race;
  private int amount;

  /**
   * @param tokens
   * @param text
   */
  public RecruitmentOrder(List<OrderToken> tokens, String text) {
    super(tokens, text);
  }

  /**
   * @see magellan.library.gamebinding.SimpleOrder#execute(magellan.library.gamebinding.ExecutionState,
   *      magellan.library.GameData, magellan.library.Unit, int)
   */
  @Override
  public void execute(ExecutionState state, GameData data, Unit unit, int line) {
    if (!isValid())
      return;

    Race effRace = race;
    String warning = null;
    int costs = 0;
    if (race == null) {
      effRace = unit.getRace();
      if (unit.getRace() != null && unit.getRace().getRecruitmentCosts() > 0) {
        costs = amount * unit.getRace().getRecruitmentCosts();
      } else {
        warning = Resources.get("order.recruit.warning.unknowncost", unit.getRace());
      }
    } else {
      Race unitRace =
          (unit instanceof TempUnit) ? ((TempUnit) unit).getParent().getRace() : unit.getRace();
      if (race.getRecruitmentCosts() > 0) {
        costs = amount * race.getRecruitmentCosts();
      } else {
        warning = Resources.get("order.recruit.warning.unknowncost", race);
      }
      if (unit.getPersons() != 0 && !unitRace.equals(race)) {
        warning = Resources.get("order.recruit.warning.wrongrace");
      }
    }
    if (effRace != null && data.getGameSpecificRules().getRecruitmentLimit(unit, effRace) < amount) {
      if (getProblem() == null) {
        warning = Resources.get("order.recruit.warning.recruitlimit");
      }
    }

    EresseaExecutionState eState = (EresseaExecutionState) state;
    // List<UnitRelation> relations =
    // eState.reserveItem(data.getRules().getItemType(EresseaConstants.I_USILVER), false, true,
    // costs, unit, line, this);
    List<UnitRelation> relations =
        eState.acquireItem(unit, data.getRules().getItemType(EresseaConstants.I_USILVER), costs,
            false, true, true, line, this);

    RecruitmentRelation recRel = new RecruitmentRelation(unit, amount, costs, unit.getRace(), line);
    if (warning != null) {
      recRel.setWarning(warning, OrderSemanticsProblemTypes.SEMANTIC_ERROR.type);
    }
    recRel.add();

    for (UnitRelation rel : relations) {
      if (rel instanceof ReserveRelation) {
        recRel.setReserve(rel);
        recRel.costs = ((ReserveRelation) rel).amount;
      } else {
        rel.add();
      }
      if (rel.problem != null) {
        recRel.setWarning(Resources.get("order.recruit.warning.silver"),
            OrderSemanticsProblemTypes.SEMANTIC_ERROR.type);
      }
    }

    if (unit instanceof TempUnit) {
      ((TempUnit) unit).setTempRace(getRace());
    }
  }

  /**
   * Sets the value of race.
   * 
   * @param race The value for race.
   */
  public void setRace(Race race) {
    this.race = race;
  }

  /**
   * Returns the value of race.
   * 
   * @return Returns race.
   */
  public Race getRace() {
    return race;
  }

  /**
   * Sets the value of amount.
   * 
   * @param amount The value for amount.
   */
  public void setAmount(int amount) {
    this.amount = amount;
  }

  /**
   * Returns the value of amount.
   * 
   * @return Returns amount.
   */
  public int getAmount() {
    return amount;
  }

}
