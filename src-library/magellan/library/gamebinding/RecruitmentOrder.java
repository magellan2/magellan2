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
import magellan.library.relation.UnitRelation;
import magellan.library.rules.Race;
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
   * @param valid
   */
  public RecruitmentOrder(List<OrderToken> tokens, String text, boolean valid) {
    super(tokens, text, valid);
  }

  /**
   * @see magellan.library.gamebinding.SimpleOrder#execute(magellan.library.gamebinding.ExecutionState,
   *      magellan.library.GameData, magellan.library.Unit, int)
   */
  @Override
  public void execute(ExecutionState state, GameData data, Unit u, int line) {
    if (!isValid())
      return;

    RecruitmentRelation recRel;
    Race effRace = race;
    if (race == null) {
      effRace = u.getRace();
      if (u.getRace() != null && u.getRace().getRecruitmentCosts() > 0) {
        recRel =
            new RecruitmentRelation(u, amount, amount * u.getRace().getRecruitmentCosts(), line,
                false);
      } else {
        recRel = new RecruitmentRelation(u, amount, 0, line, true);
        setWarning(Resources.get("order.recruit.warning.unknowncost", u.getRace()));
      }
    } else {
      Race unitRace = (u instanceof TempUnit) ? ((TempUnit) u).getParent().getRace() : u.getRace();
      if (race.getRecruitmentCosts() > 0) {
        int cost = amount * race.getRecruitmentCosts();
        recRel = new RecruitmentRelation(u, getAmount(), cost, race, line, false);
      } else {
        recRel = new RecruitmentRelation(u, getAmount(), 0, race, line, true);
        setWarning(Resources.get("order.recruit.warning.unknowncost", race));
      }
      if (u.getPersons() != 0 && !unitRace.equals(race)) {
        recRel.warning = true;
        // setValid(false);
        setWarning(Resources.get("order.recruit.warning.wrongrace"));
      }
    }
    if (effRace != null
        && data.getGameSpecificRules().getRecruitmentLimit(u, effRace) < recRel.amount) {
      if (getWarning() == null) {
        setWarning(Resources.get("order.recruit.warning.recruitlimit"));
      }
    }
    recRel.add();

    EresseaExecutionState eState = (EresseaExecutionState) state;
    List<UnitRelation> relations =
        eState.reserveItem(data.getRules().getItemType(EresseaConstants.I_USILVER), false,
            recRel.costs, u, line, this);
    for (UnitRelation rel : relations) {
      rel.add();
      if (rel.warning) {
        setWarning(Resources.get("order.recruit.warning.silver"));
      }
    }

    if (u instanceof TempUnit) {
      ((TempUnit) u).setTempRace(getRace());
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
