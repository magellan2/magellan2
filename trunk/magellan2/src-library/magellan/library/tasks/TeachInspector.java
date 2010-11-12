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
import java.util.List;

import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.relation.TeachRelation;
import magellan.library.tasks.Problem.Severity;
import magellan.library.utils.Resources;

/**
 * Inspector for teach orders
 */
public class TeachInspector extends AbstractInspector {

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

  protected static final ProblemType NOTLEARNING;

  static {
    String message = Resources.get("tasks.teachinspector.notlearning.message");
    String typeName = Resources.get("tasks.teachinspector.notlearning.name", false);
    if (typeName == null) {
      typeName = message;
    }
    String description = Resources.get("tasks.teachinspector.notlearning.description", false);
    String group = Resources.get("tasks.teachinspector.notlearning.group", false);
    NOTLEARNING = new ProblemType(typeName, group, description, message);
  }

  /**
   * @see magellan.library.tasks.AbstractInspector#reviewUnit(magellan.library.Unit, Severity)
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
      int line = 0;
      for (String string : u2.getOrders()) {
        line++;
        String order = (string).trim();
        if (order.startsWith(Resources.getOrderTranslation(EresseaConstants.O_LEARN, u.getFaction()
            .getLocale()))) {
          found = true;
        }
      }
      if (!found) {
        problems.add(ProblemFactory.createProblem(Severity.WARNING, NOTLEARNING, u, this,
            relation.line));
      }
    }

    if (problems.isEmpty())
      return Collections.emptyList();
    else
      return problems;
  }

  public Collection<ProblemType> getTypes() {
    return Collections.singletonList(TeachInspector.NOTLEARNING);
  }

}
