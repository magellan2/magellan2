// class magellan.library.gamebinding.UnitMaintenanceOrder
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

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.relation.MovementRelation;
import magellan.library.utils.logging.Logger;

/**
 * Virtual order for paying maintenance
 *
 * @author stm
 */
public class UnitMaintenanceOrder {

  /**
   * Pays maintenance for all units in <code>unit</code>'s region!
   *
   * @see magellan.library.gamebinding.SimpleOrder#execute(magellan.library.gamebinding.ExecutionState,
   *      magellan.library.GameData, magellan.library.Unit, int)
   */
  public static void execute(Region region, ExecutionState state, GameData data) {
    for (Unit u : region.units()) {
      CoordinateID destination = u.getRegion().getCoordinate();

      List<MovementRelation> movements = u.getRelations(MovementRelation.class);
      if (movements.size() > 1) {
        Logger.getInstance(UnitMaintenanceOrder.class)
        .warn("unit has more than one movement: " + u);
      }

      if (movements.size() > 0) {
        destination = movements.iterator().next().getDestination();
      }
      CoordinateID oldDestination = u.getNewRegion();
      if (destination != oldDestination) {
        Region newRegion;
        if (oldDestination != null) {
          newRegion = data.getRegion(oldDestination);
          if (newRegion != null) {
            newRegion.removeMaintenance(u);
          }
        }
        u.setNewRegion(destination);
        newRegion = data.getRegion(destination);
        if (newRegion != null && newRegion != u.getRegion()) {
          newRegion.addMaintenance(u);
        }
      }
    }

  }

}
