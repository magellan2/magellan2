// class magellan.library.gamebinding.MovementOrder
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.relation.ControlRelation;
import magellan.library.relation.LeaveRelation;
import magellan.library.relation.MovementRelation;
import magellan.library.relation.UnitRelation;
import magellan.library.utils.Direction;
import magellan.library.utils.OrderToken;
import magellan.library.utils.Resources;

/**
 * A movement order (like MOVE, ROUTE)
 * 
 * @author stm
 */
public class MovementOrder extends SimpleOrder {

  /**
   * true if this is a ROUTE order
   */
  private boolean permanent;

  /**
   * @param tokens
   * @param text
   * @param valid
   * @param permanent
   */
  public MovementOrder(List<OrderToken> tokens, String text, boolean valid, boolean permanent) {
    super(tokens, text, valid);
    this.permanent = permanent;
  }

  /**
   * @see magellan.library.gamebinding.SimpleOrder#execute(magellan.library.gamebinding.ExecutionState,
   *      magellan.library.GameData, magellan.library.Unit, int)
   */
  @Override
  public void execute(ExecutionState state, GameData data, Unit unit, int line) {
    if (!isValid())
      return;

    List<CoordinateID> modifiedMovement = new ArrayList<CoordinateID>(2);

    // dissect the order into pieces to detect which way the unit
    // is taking
    Region currentRegion = unit.getRegion();
    CoordinateID currentCoord = unit.getRegion().getCoordinate();

    Locale locale = unit.getFaction().getLocale();
    for (OrderToken token : getTokens()) {
      if (modifiedMovement.isEmpty()) {
        modifiedMovement.add(currentCoord);
        // skip first token
        continue;
      }

      Direction movement = Direction.toDirection(token.getText(), locale);

      // try to get the next region; take "wrap around" regions into account
      CoordinateID nextCoord = currentCoord;
      Region nextRegion = currentRegion;
      if (movement != Direction.INVALID) {
        // try to get next region from the neighbor relation; not possible if the movement goes
        // through an unknown region
        nextRegion = currentRegion != null ? currentRegion.getNeighbors().get(movement) : null;
        // if the nextRegion is unknown for some region, fall back to coordinte movement
        nextCoord =
            nextRegion == null ? currentCoord.translate(movement.toCoordinate()) : nextRegion
                .getCoordinate();
        if (nextRegion == null) {
          nextRegion = unit.getRegion().getData().getRegion(nextCoord);
        }
      }

      modifiedMovement.add(nextCoord);
      currentCoord = nextCoord;
      currentRegion = nextRegion;
    }

    (new MovementRelation(unit, modifiedMovement, line)).add();

    // check whether the unit leaves a container
    UnitContainer leftUC = unit.getBuilding();

    if (leftUC == null) {
      leftUC = unit.getShip();
      if (leftUC != null)
        if (leftUC.getModifiedOwnerUnit() == unit) {
          leftUC = null;
        } else if (leftUC.getModifiedUnit(unit.getID()) != null) {
          setWarning(Resources.get("order.move.warning.leaveship"));
        }
    }

    if (leftUC != null) {
      UnitRelation rel = new LeaveRelation(unit, leftUC, line, true, false);

      if (leftUC.getModifiedOwnerUnit() == unit) {
        for (Unit otherUnit : leftUC.modifiedUnits()) {
          if (otherUnit != unit) {
            ControlRelation crel = new ControlRelation(unit, otherUnit, line, true);
            crel.add();
            setWarning(Resources.get("order.move.warning.implicitcommand"));
            break;
          }
        }
      }
      rel.add();

    }
  }

  /**
   * Sets the value of permanent.
   * 
   * @param permanent The value for permanent.
   */
  public void setPermanent(boolean permanent) {
    this.permanent = permanent;
  }

  /**
   * Returns the value of permanent.
   * 
   * @return Returns permanent.
   */
  public boolean isPermanent() {
    return permanent;
  }

}
