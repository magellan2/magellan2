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
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaRelationFactory.EresseaExecutionState;
import magellan.library.relation.MaintenanceRelation;
import magellan.library.relation.ReserveRelation;
import magellan.library.relation.UnitRelation;
import magellan.library.rules.ItemType;
import magellan.library.tasks.MaintenanceInspector;
import magellan.library.utils.MagellanFactory;
import magellan.library.utils.OrderToken;
import magellan.library.utils.Resources;

/**
 * A promotion order (BEFOERDERE)
 *
 * @author stm
 */
public class PromoteOrder extends SimpleOrder {

  /**
   * @param tokens
   * @param text
   */
  public PromoteOrder(List<OrderToken> tokens, String text) {
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

    int costs = 0;
    for (Unit u : unit.getFaction().units()) {
      if (!u.isSpy()) {
        costs += u.getModifiedPersons();
      }
    }

    costs = unit.getModifiedPersons() * costs;

    EresseaExecutionState eState = (EresseaExecutionState) state;

    ItemType silverType = data.getRules().getItemType(EresseaConstants.I_USILVER);

    List<UnitRelation> relations = eState.acquireItem(unit, silverType, costs, false, true, false,
        line, this);

    MaintenanceRelation mRel =
        new MaintenanceRelation(unit, MagellanFactory.createNullContainer(data), costs, silverType,
            Resources
                .get("util.units.node.promotion"), "hero", line, false);

    for (UnitRelation rel : relations) {
      if (rel instanceof ReserveRelation) {
        mRel.setCosts(((ReserveRelation) rel).amount);
      } else {
        rel.add();
      }
      if (rel.problem != null) {
        mRel.warning = true;
        mRel.setWarning(Resources.get("order.maintenance.warning.silver"),
            MaintenanceInspector.MaintenanceProblemTypes.BUILDINGMAINTENANCE.type);
      }
    }
    mRel.add();

  }

}
