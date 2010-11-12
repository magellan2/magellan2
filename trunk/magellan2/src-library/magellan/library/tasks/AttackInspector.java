// class magellan.library.tasks.AttackInspector
// created on Jul 30, 2007

// Copyright 2003-2007 by magellan project team

// Author : $Author: $
// $Id: $

// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.

// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package magellan.library.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.relation.AttackRelation;
import magellan.library.tasks.Problem.Severity;
import magellan.library.utils.Resources;

/**
 * An
 * 
 * @author ...
 * @version 1.0, Jul 30, 2007
 */
public class AttackInspector extends AbstractInspector {

  enum AttackProblemTypes {
    ATTACKSELF, FRIENDLYFIRE, NOTFIGHTING, NOTFIGHTING4GUARD;

    private ProblemType type;

    AttackProblemTypes() {
      String name = name().toLowerCase();
      String message = Resources.get("tasks.attackinspector." + name + ".message");
      String typeName = Resources.get("tasks.attackinspector." + name + ".name", false);
      if (typeName == null) {
        typeName = message;
      }
      String description = Resources.get("tasks.attackinspector." + name + ".description", false);
      String group = Resources.get("tasks.attackinspector." + name + ".group", false);
      type = new ProblemType(typeName, group, description, message);
    }

    ProblemType getType() {
      return type;
    }
  }

  private Collection<ProblemType> types;

  /**
   * Returns a (singleton) instance.
   * 
   * @return An instance of this class
   */
  public static AttackInspector getInstance(GameData data) {
    return new AttackInspector(data);
  }

  protected AttackInspector(GameData data) {
    super(data);
  }

  /**
   * @see magellan.library.tasks.AbstractInspector#reviewUnit(magellan.library.Unit, Severity)
   */
  @Override
  public List<Problem> reviewUnit(Unit u, Severity severity) {
    if ((u == null) || u.ordersAreNull())
      return Collections.emptyList();

    if (severity == Severity.INFORMATION)
      return Collections.emptyList();

    List<Problem> problems = new ArrayList<Problem>(2);

    // int line = 0;
    int wrongStatus = Integer.MAX_VALUE;

    List<?> relations = u.getRelations(AttackRelation.class);
    for (Object o : relations) {
      if (o instanceof AttackRelation) {
        AttackRelation relation = (AttackRelation) o;
        if (!relation.source.equals(u)) {
          continue;
        }
        if (severity == Severity.WARNING) {
          if (relation.target == relation.source) {
            problems.add(ProblemFactory.createProblem(severity, AttackProblemTypes.ATTACKSELF
                .getType(), u, this, relation.line));
          } else if (relation.source.getFaction() == relation.target.getFaction()
              || (relation.source.getFaction().getAllies() != null && relation.source.getFaction()
                  .getAllies().containsKey(relation.target.getFaction().getID()))) {
            problems.add(ProblemFactory.createProblem(severity, AttackProblemTypes.FRIENDLYFIRE
                .getType(), u, this, relation.line));
          }
        }
        // TODO define as constants
        // pre 57:
        // 0: VORNE
        // 1: HINTEN
        // 2: NICHT
        // 3: FLIEHE
        //
        // 57 and later:
        // 0 AGGRESSIV: 1. Reihe, flieht nie.
        // 1 VORNE: 1. Reihe, kämpfen bis 20% HP
        // 2 HINTEN: 2. Reihe, kämpfen bis 20% HP
        // 3 DEFENSIV: 2. Reihe, kämpfen bis 90% HP
        // 4 NICHT: 3. Reihe, kämpfen bis 90% HP
        // 5 FLIEHE: 4. Reihe, flieht immer.

        // Fiete 20080521: changing here to modified Combat status
        if (u.getModifiedCombatStatus() > EresseaConstants.CS_DEFENSIVE) {
          wrongStatus = Math.min(wrongStatus, relation.line);
        }

      }
    }
    if (severity == Severity.ERROR) {
      if (wrongStatus != Integer.MAX_VALUE) {
        problems.add(ProblemFactory.createProblem(severity, AttackProblemTypes.NOTFIGHTING
            .getType(), u, this, wrongStatus));
      }

      // guard and not fighting (fleeing)?
      if (u.getModifiedGuard() != 0 && u.getModifiedCombatStatus() == EresseaConstants.CS_FLEE) {
        problems.add(ProblemFactory.createProblem(severity, AttackProblemTypes.NOTFIGHTING4GUARD
            .getType(), u, this));
      }

    }
    if (problems.isEmpty())
      return Collections.emptyList();
    else
      return problems;
  }

  public Collection<ProblemType> getTypes() {
    if (types == null) {
      types = new LinkedList<ProblemType>();
      for (AttackProblemTypes t : AttackProblemTypes.values()) {
        types.add(t.getType());
      }
    }
    return types;
  }

}
