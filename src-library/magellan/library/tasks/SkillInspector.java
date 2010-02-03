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
import magellan.library.relation.PersonTransferRelation;
import magellan.library.relation.UnitRelation;
import magellan.library.tasks.Problem.Severity;
import magellan.library.utils.Resources;

public class SkillInspector extends AbstractInspector {
  /** The singleton instance. */
//  public static final SkillInspector INSPECTOR = new SkillInspector();

  /**
   * Returns a (singleton) instance.
   * 
   * @return An instance of this class
   */
  public static SkillInspector getInstance(GameData data) {
    return new SkillInspector(data);
  }

  protected static final ProblemType SKILLDECREASE;

  static {
    String message = Resources.get("tasks.skillinspector.skilldecrease.message");
    String typeName = Resources.get("tasks.skillinspector.skilldecrease.name", false);
    if (typeName == null)
      typeName = message;
    String description = Resources.get("tasks.skillinspector.skilldecrease.description", false);
    String group = Resources.get("tasks.skillinspector.skilldecrease.group", false);
    SKILLDECREASE = new ProblemType(typeName, group, description, message);
  }

  protected SkillInspector(GameData data) {
    super(data);
  }

  /**
   * @see magellan.library.tasks.AbstractInspector#reviewUnit(magellan.library.Unit, int)
   */
  @Override
  public List<Problem> reviewUnit(Unit u, Severity severity) {
    if (severity != Severity.WARNING) {
      return Collections.emptyList();
    }

    // check all person transfer relations
    List<Problem> problems = new ArrayList<Problem>(2);
    for (PersonTransferRelation relation : u.getRelations(PersonTransferRelation.class)) {
        Unit u1 = relation.source;
        Unit u2 = relation.target;
        if (u1 != u)
          break;
        List<Skill> skills = new LinkedList<Skill>(u1.getModifiedSkills());
        skills.addAll(u2.getModifiedSkills());
        for (Skill skill : skills) {
          Skill skill1 = u1.getSkill(skill.getSkillType());
          Skill skill2 = u2.getSkill(skill.getSkillType());
          // if a skill of the source unit is higher than the target unit, issue a warning at the
          // source
          if (skill1 != null) {
            if (skill2 == null || skill1.getLevel() > skill2.getLevel()) {
              // when passing persons to an empty unit, no warning is necessary
              // except when multiple units pass persons to the empty unit
              // this is not perfect yet:
              if (u2.getPersons() > 0
                  || (u2.getRelations(PersonTransferRelation.class).size() > 1)) {
                problems.add(ProblemFactory.createProblem(Severity.WARNING, SKILLDECREASE, u, this,
                    relation.line));
                break;
              }
            }
          }
          // if a skill of the target unit is higher than the source unit, issue a warning at the
          // target
          if (skill2 != null) {
            if (skill1 == null || skill2.getLevel() > skill1.getLevel()) {
              problems.add(ProblemFactory.createProblem(Severity.WARNING, SKILLDECREASE, u
                  .getRegion(), u, u.getFaction(), u2, this, SKILLDECREASE.getMessage(),
                  relation.line));
              break;
            }
          }
        }
      
    }

    if (problems.isEmpty()) {
      return Collections.emptyList();
    } else {
      return problems;
    }
  }

  public Collection<ProblemType> getTypes() {
    return Collections.singletonList(SKILLDECREASE);
  }
}
