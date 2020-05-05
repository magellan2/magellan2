// class magellan.library.gamebinding.UnitsOrder
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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.relation.TeachRelation;
import magellan.library.tasks.Problem.Severity;
import magellan.library.tasks.ProblemFactory;
import magellan.library.tasks.TeachInspector;
import magellan.library.utils.OrderToken;
import magellan.library.utils.Resources;

/**
 * A teaching order.
 * 
 * @author stm
 */
public class TeachOrder extends SimpleOrder {

  List<UnitID> units;

  /**
   * @param tokens
   * @param text
   */
  public TeachOrder(List<OrderToken> tokens, String text) {
    super(tokens, text);
  }

  /**
   * @see magellan.library.gamebinding.SimpleOrder#execute(magellan.library.gamebinding.ExecutionState,
   *      magellan.library.GameData, magellan.library.Unit, int)
   */
  @Override
  public void execute(ExecutionState state, GameData data, Unit unit, int line) {
    if (units == null)
      return;

    for (UnitID targetID : units) {
      Unit target = unit.getRegion().getUnit(targetID);
      if (target != null) {
        TeachRelation rel = new TeachRelation(unit, target, line);
        if (unit.equals(target)) {
          rel.setWarning(Resources.get("order.teach.warning.reflexive", targetID),
              TeachInspector.TeachProblemTypes.REFLEXIVE.type);
        }
        rel.add();
      } else {
        setProblem(ProblemFactory.createProblem(Severity.WARNING,
            TeachInspector.TeachProblemTypes.UNKNOWN_TARGET.type, unit, null, Resources.get(
                "order.all.unknownunit", targetID), line));
      }
    }
  }

  /**
   * Adds a target unit.
   * 
   * @param newUnit The unit to be added, possibly <code>null</code>.
   */
  public void addUnit(UnitID newUnit) {
    if (units == null) {
      units = new LinkedList<UnitID>();
    }
    units.add(newUnit);
  }

  /**
   * Return the target units. May contain <code>null</code> elements.
   */
  public List<UnitID> getUnits() {
    return units == null ? Collections.<UnitID> emptyList() : Collections.unmodifiableList(units);
  }
}
