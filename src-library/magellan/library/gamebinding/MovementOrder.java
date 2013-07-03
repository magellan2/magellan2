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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.relation.ControlRelation;
import magellan.library.relation.LeaveRelation;
import magellan.library.relation.MovementRelation;
import magellan.library.relation.TransportRelation;
import magellan.library.relation.UnitRelation;
import magellan.library.tasks.MovementInspector;
import magellan.library.tasks.OrderSyntaxInspector.OrderSemanticsProblemTypes;
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
  private List<Direction> directions = new ArrayList<Direction>(2);

  /**
   * @param tokens
   * @param text
   * @param permanent
   */
  public MovementOrder(List<OrderToken> tokens, String text, boolean permanent) {
    super(tokens, text);
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

    MovementRelation mRel =
        data.getGameSpecificStuff().getMovementEvaluator().getMovement(unit, getDirections());
    mRel.line = line;

    if (mRel.invalidRegion != null) {
      mRel.setWarning(Resources.get("order.move.warning.moveinvalid", mRel.invalidRegion),
          MovementInspector.MovementProblemTypes.MOVE_INVALID.getType());
    }
    removeMovements(unit);

    mRel.add();

    Set<Unit> passengers = new HashSet<Unit>();
    if (unit.getModifiedShip() != null && unit.getModifiedShip().getModifiedOwnerUnit() == unit) {
      for (Unit passenger : unit.getModifiedShip().modifiedUnits()) {
        if (passenger != unit && passenger.getRelations(MovementRelation.class).isEmpty()) {
          passengers.add(passenger);
        }
      }
    } else {
      for (TransportRelation transportRel : unit.getRelations(TransportRelation.class)) {
        if (transportRel.target != null && transportRel.target != unit) {
          removeMovements(transportRel.target);
          passengers.add(transportRel.target);
          implicitLeave(transportRel.target, mRel, line);
        }
      }
    }
    for (Unit passenger : passengers) {
      MovementRelation transportRel =
          new MovementRelation(passenger, unit, mRel.getInitialMovement(),
              mRel.getFutureMovement(), mRel.unknown, mRel.invalidRegion, mRel.rounds, line);
      transportRel.add();
    }

    implicitLeave(unit, mRel, line);
  }

  private List<Direction> getDirections() {
    return directions;
  }

  private void implicitLeave(Unit unit, MovementRelation mRel, int line) {
    // check whether the unit leaves a container
    UnitContainer leftUC = unit.getBuilding();

    if (leftUC == null) {
      leftUC = unit.getShip();
      if (leftUC != null) {
        // unit is on ship: issue warning if it's not the owner
        if (leftUC.getModifiedOwnerUnit() == unit) {
          leftUC = null;
        } else if (leftUC.getModifiedUnit(unit.getID()) != null) {
          if (leftUC.getModifiedOwnerUnit() != unit) {
            mRel.setWarning(Resources.get("order.move.warning.leaveship"),
                OrderSemanticsProblemTypes.SEMANTIC_WARNING.type);
          }
        }
      }
    }

    if (leftUC != null) {
      UnitRelation rel = new LeaveRelation(unit, leftUC, line, true);

      if (leftUC.getModifiedOwnerUnit() == unit) {
        for (Unit otherUnit : leftUC.modifiedUnits()) {
          if (otherUnit != unit) {
            ControlRelation crel = new ControlRelation(unit, otherUnit, line);
            crel.setWarning(Resources.get("order.move.warning.implicitcommand"),
                OrderSemanticsProblemTypes.SEMANTIC_ERROR.type);
            crel.add();
            break;
          }
        }
      }
      rel.add();
    }
  }

  private void removeMovements(Unit unit) {
    if (unit.getRelations(MovementRelation.class) != null) {
      for (MovementRelation oldRelation : unit.getRelations(MovementRelation.class)) {
        unit.removeRelation(oldRelation);
      }
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

  public void addDirection(Direction direction) {
    directions.add(direction);
  }

}
