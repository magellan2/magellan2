// class magellan.library.gamebinding.BuildingMaintenanceOrder
// created on Jun 19, 2012
//
// Copyright 2003-2012 by magellan project team
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

import magellan.library.Building;
import magellan.library.GameData;
import magellan.library.Item;
import magellan.library.Order;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaRelationFactory.EresseaExecutionState;
import magellan.library.gamebinding.e3a.MaintainOrder;
import magellan.library.relation.MaintenanceRelation;
import magellan.library.relation.ReserveRelation;
import magellan.library.relation.UnitRelation;
import magellan.library.tasks.MaintenanceInspector;
import magellan.library.utils.Resources;

/**
 * A virtual order for paying building maintenance in the region.
 *
 * @author stm
 * @version 1.0, Jun 19, 2012
 */
public class BuildingMaintenanceOrder {

  /**
   * Pays building maintenance for <em>all</em> buildings in <code>unit</code>'s region!
   *
   * @see magellan.library.gamebinding.SimpleOrder#execute(magellan.library.gamebinding.ExecutionState,
   *      magellan.library.GameData, magellan.library.Unit, int)
   */
  public static void execute(Region region, ExecutionState state, GameData data) {
    EresseaExecutionState eState = (EresseaExecutionState) state;

    for (Building b : region.buildings()) {
      boolean maintain = true;
      if (b.getEffects() != null) {
        for (String eff : b.getEffects()) {
          if (eff.startsWith("Der Zahn der Zeit kann diesen Mauern nichts anhaben.")
              || eff.startsWith("Time cannot touch these walls.")) {
            maintain = false;
            break;
          }
        }
      }

      Unit owner = b.getModifiedOwnerUnit();
      if (owner != null) {
        for (Order order : owner.getOrders2()) {
          if (order instanceof MaintainOrder)
            if (((MaintainOrder) order).isNot()) {
              maintain = false;
              break;
            }
        }
        if (maintain) {
          for (Item i : b.getBuildingType().getMaintenanceItems()) {
            // List<UnitRelation> relations =
            // state.reserveItem(i.getItemType(), false, true, i.getAmount(), owner, -1, null);
            List<UnitRelation> relations =
                eState.acquireItem(owner, i.getItemType(), i.getAmount(), false, true, true, -1,
                    null);
            MaintenanceRelation mRel =
                new MaintenanceRelation(owner, b, i.getAmount(), i.getItemType(), -1, false);
            for (UnitRelation rel : relations) {
              if (rel instanceof ReserveRelation) {
                // mRel.setReserve((ReserveRelation)rel);
                mRel.costs = ((ReserveRelation) rel).amount;
              } else {
                rel.add();
              }
              if (rel.problem != null) {
                mRel.warning = true;
                mRel.setWarning(Resources.get("order.maintenance.warning.silver"),
                    MaintenanceInspector.MaintenanceProblemTypes.BUILDINGMAINTENANCE.type);
                // mRel.setWarning(Resources.get("order.maintenance.warning.silver"),
                // OrderSemanticsProblemTypes.SEMANTIC_ERROR.type);
              }
            }
            mRel.add();
          }
        }
      }
    }

  }
}
