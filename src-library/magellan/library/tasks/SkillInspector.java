// class magellan.library.tasks.SkillInspector
// created on Apr 22, 2009
//
// Copyright 2003-2009 by magellan project team
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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
package magellan.library.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import magellan.library.GameData;
import magellan.library.Skill;
import magellan.library.Unit;
import magellan.library.ZeroUnit;
import magellan.library.relation.PersonTransferRelation;
import magellan.library.tasks.Problem.Severity;

public class SkillInspector extends AbstractInspector {
  /** The singleton instance. */
  // public static final SkillInspector INSPECTOR = new SkillInspector();

  /**
   * Returns a (singleton) instance.
   * 
   * @return An instance of this class
   */
  public static SkillInspector getInstance(GameData data) {
    return new SkillInspector(data);
  }

  protected static final ProblemType SKILLDECREASE = ProblemType.create("tasks.skillinspector",
      "skilldecrease");

  protected SkillInspector(GameData data) {
    super(data);
  }

  /**
   * @see AbstractInspector#findProblems(magellan.library.Unit)
   */
  @Override
  public List<Problem> findProblems(Unit u) {

    // check all person transfer relations
    List<Problem> problems = new ArrayList<Problem>(2);
    for (PersonTransferRelation relation : u.getRelations(PersonTransferRelation.class)) {
      Unit u1 = relation.source;
      Unit u2 = relation.target;
      if (u1 != u || (u2 instanceof ZeroUnit)
          || !(u2.getPersons() > 0 || u2.getRelations(PersonTransferRelation.class).size() > 1)) {
        continue;
      }
      List<Skill> skills = new LinkedList<Skill>(u1.getModifiedSkills());
      skills.addAll(u2.getModifiedSkills());
      boolean found1 = false, found2 = false;
      for (Skill skill : skills) {
        Skill skill1 = u1.getSkill(skill.getSkillType());
        Skill skill2 = u2.getSkill(skill.getSkillType());
        // if a skill of the source unit is higher than the target unit, issue a warning at the
        // source
        if (!found1 && skill1 != null) {
          if (skill2 == null || skill1.getLevel() > skill2.getLevel()) {
            // when passing persons to an empty unit, no warning is necessary
            // except when multiple units pass persons to the empty unit
            // this is not perfect yet:
            found1 = true;
            problems.add(ProblemFactory.createProblem(Severity.WARNING,
                SkillInspector.SKILLDECREASE, u, this, relation.line));
          }
        }
        // if a skill of the target unit is higher than the source unit, issue a warning at the
        // target
        if (!found2 && skill2 != null) {
          if (skill1 == null || skill2.getLevel() > skill1.getLevel()) {
            found2 = true;
            problems.add(ProblemFactory.createProblem(Severity.WARNING,
                SkillInspector.SKILLDECREASE, u.getRegion(), u, u.getFaction(), u2, this,
                SkillInspector.SKILLDECREASE.getMessage(), relation.line));
          }
        }
        if (found1 && found2) {
          break;
        }
      }
    }

    if (problems.isEmpty())
      return Collections.emptyList();
    else
      return problems;
  }

  public Collection<ProblemType> getTypes() {
    return Collections.singletonList(SkillInspector.SKILLDECREASE);
  }
}
