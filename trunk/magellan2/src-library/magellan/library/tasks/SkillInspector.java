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
import java.util.Collections;
import java.util.List;

import magellan.library.Skill;
import magellan.library.Unit;
import magellan.library.relation.PersonTransferRelation;
import magellan.library.relation.UnitRelation;
import magellan.library.utils.Resources;

public class SkillInspector extends AbstractInspector {
  /** The singleton instance. */
  public static final SkillInspector INSPECTOR = new SkillInspector();

  /**
   * Returns a (singleton) instance.
   * 
   * @return An instance of this class
   */
  public static SkillInspector getInstance() {
    return SkillInspector.INSPECTOR;
  }

  protected SkillInspector() {
  }

  /**
   * @see magellan.library.tasks.AbstractInspector#reviewUnit(magellan.library.Unit, int)
   */
  @Override
  public List<Problem> reviewUnit(Unit u, int type) {
    if(type != Problem.WARNING) {
      return Collections.emptyList();
    }

    // check all person transfer relations
    List<Problem> problems = new ArrayList<Problem>(2);
    int maxDecrease = 0;
    for (UnitRelation rel : u.getRelations(PersonTransferRelation.class)){
      if (rel instanceof PersonTransferRelation){
        PersonTransferRelation relation = (PersonTransferRelation) rel;
        Unit u1 = relation.source;
        Unit u2 = relation.target;
        if (u1!=u)
          break;
        // FIXME when passing persons to an empty unit, no warning is necessary
        // except when multiple units pass persons to the empty unit
        for (Skill skill : u1.getModifiedSkills()){
          Skill skill1 = u1.getSkill(skill.getSkillType()); 
          Skill skill2 = u2.getSkill(skill.getSkillType());
          // if a skill of the source unit is higher than the target unit, issue a warning at the source
          if (skill1!=null){
            if (skill2 ==null ||              skill1.getLevel() > skill2.getLevel()){
              problems.add(new CriticizedWarning(u, u1, this, Resources.get("tasks.skillinspector.warning.skilldecrease", maxDecrease)));
              break;
            }
          }
          // if a skill of the target unit is higher than the source unit, issue a warning at the target
          if (skill2!=null){
            if (skill1 ==null ||              skill2.getLevel() > skill1.getLevel()){
              problems.add(new CriticizedWarning(u, u2, this, Resources.get("tasks.skillinspector.warning.skilldecrease", maxDecrease)));
              break;
            }
          }
        }
      }
    }

    if(problems.isEmpty()) {
      return Collections.emptyList();
    } else {
      return problems;
    }
  }


}
