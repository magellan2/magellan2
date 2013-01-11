// class magellan.library.tasks.TeachInspector
// created on Nov 12, 2010
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
package magellan.library.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import magellan.library.GameData;
import magellan.library.Order;
import magellan.library.Skill;
import magellan.library.Unit;
import magellan.library.gamebinding.LearnOrder;
import magellan.library.relation.TeachRelation;
import magellan.library.rules.SkillType;
import magellan.library.tasks.Problem.Severity;
import magellan.library.utils.Resources;

/**
 * Inspector for teach orders
 */
public class TeachInspector extends AbstractInspector {

  public enum TeachProblemTypes {
    NOTLEARNING, REFLEXIVE, UNKNOWN_TARGET;

    public ProblemType type;

    TeachProblemTypes() {
      String name = name().toLowerCase();
      type = ProblemType.create("tasks.teachinspector", name);
    }

    ProblemType getType() {
      return type;
    }
  }

  private List<ProblemType> types;

  /**
   * @param data
   */
  protected TeachInspector(GameData data) {
    super(data);
  }

  /**
   * Returns a (singleton) instance.
   * 
   * @return An instance of this class
   */
  public static TeachInspector getInstance(GameData data) {
    return new TeachInspector(data);
  }

  /**
   * @see AbstractInspector#reviewUnit(magellan.library.Unit,
   *      magellan.library.tasks.Problem.Severity)
   */
  @Override
  public List<Problem> reviewUnit(Unit u, Severity severity) {
    if (severity != Severity.WARNING)
      return Collections.emptyList();

    // check all person transfer relations
    List<Problem> problems = new ArrayList<Problem>(2);
    for (TeachRelation relation : u.getRelations(TeachRelation.class)) {
      Unit u1 = relation.source;
      Unit u2 = relation.target;
      if (u1 != u) {
        break;
      }
      boolean found = false;
      for (Order o : u2.getOrders2())
        if (o.getProblem() == null && o instanceof LearnOrder) {
          found = true;
          SkillType skillType = getData().getRules().getSkillType(((LearnOrder) o).skillName);
          Skill ss = u2.getModifiedSkill(skillType);
          Skill ts = u.getModifiedSkill(skillType);
          if ((ss == null ? 0 : ss.getLevel()) + 2 > (ts == null ? 0 : ts.getLevel())) {
            problems.add(ProblemFactory.createProblem(Severity.WARNING,
                TeachProblemTypes.NOTLEARNING.type, u, this, Resources.get(
                    "tasks.teachinspector.notlearning.message2", u2, skillType), relation.line));
          }
        }
      if (!found) {
        problems.add(ProblemFactory.createProblem(Severity.WARNING,
            TeachProblemTypes.NOTLEARNING.type, u, this, Resources.get(
                "tasks.teachinspector.notlearning.message1", u2), relation.line));
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
      for (TeachProblemTypes t : TeachProblemTypes.values()) {
        types.add(t.getType());
      }
    }
    return types;
  }

}
