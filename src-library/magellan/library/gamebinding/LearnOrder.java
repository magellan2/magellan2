// class magellan.library.gamebinding.LearnOrder
// created on Nov 13, 2010
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
import magellan.library.Skill;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaRelationFactory.EresseaExecutionState;
import magellan.library.relation.MaintenanceRelation;
import magellan.library.rules.ItemType;
import magellan.library.rules.SkillType;
import magellan.library.utils.MagellanFactory;
import magellan.library.utils.OrderToken;
import magellan.library.utils.Resources;

/**
 * A LERNE order.
 */
public class LearnOrder extends SimpleOrder {

  /**
   * The name of the learned skill.
   */
  public String skillName;
  public SkillType skillType;
  public Integer cost;

  private boolean auto;

  /**
   * @param tokens
   * @param text
   */
  public LearnOrder(List<OrderToken> tokens, String text) {
    super(tokens, text);
  }

  /**
   * Set for LERNE AUTO
   */
  public void setAuto(boolean auto) {
    this.auto = auto;
  }

  /**
   * Returns true if this is a LERNE AUTO order
   */
  public boolean isAuto() {
    return auto;
  }

  @Override
  public void execute(ExecutionState state, GameData data, Unit unit, int line) {
    if (skillType == null)
      return;
    EresseaExecutionState eState = (EresseaExecutionState) state;

    ItemType silverType = data.getRules().getItemType(EresseaConstants.I_USILVER);

    int realCost = getSkillCost(skillType, unit);
    if (cost != null && realCost != cost) {
      setWarning(unit, line, Resources.get("order.learn.warning.cost", unit, skillName, cost, realCost));
      realCost = Math.max(realCost, cost);
    }

    if (realCost > 0) {
      // List<UnitRelation> relations = eState.acquireItem(unit, silverType, realCost, false, true, false, true, line,
      // this);

      MaintenanceRelation mRel =
          new MaintenanceRelation(unit, MagellanFactory.createNullContainer(data), realCost, silverType,
              Resources
                  .get("util.units.node.learn"), "skills", line, false);

      // TODO deactivate for now, reserving after actual give/reserve step is more tricky
      // for (UnitRelation rel : relations) {
      // if (rel instanceof ReserveRelation) {
      // mRel.setCosts(((ReserveRelation) rel).amount);
      // } else {
      // rel.add();
      // }
      // if (rel.problem != null) {
      // mRel.warning = true;
      // mRel.setWarning(Resources.get("order.learn.warning.silver"),
      // MaintenanceInspector.MaintenanceProblemTypes.LEARNCOSTS.type);
      // }
      // }
      mRel.add();
    }
  }

  public static int getSkillCost(SkillType skillType, Unit someUnit) {
    int cost = 0;

    final Skill sk = someUnit.getSkill(skillType);
    if (sk == null) {
      cost = skillType.getCost(1);
    } else {
      cost = skillType.getCost(1 + sk.getLevel() - sk.getModifier(someUnit));
    }

    if ((someUnit.getModifiedBuilding() != null)
        && someUnit.getModifiedBuilding().getType().equals(
            someUnit.getData().getRules().getBuildingType(EresseaConstants.B_ACADEMY))) {
      if (cost == 0) {
        // cost = 50;
        cost = 50;
      } else {
        // cost *= 2;
        cost *= 2;
      }
    }

    cost *= Math.max(1, someUnit.getModifiedPersons());

    return cost;
  }

}
