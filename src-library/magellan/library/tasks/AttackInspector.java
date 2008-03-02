//class magellan.library.tasks.AttackInspector
//created on Jul 30, 2007

//Copyright 2003-2007 by magellan project team

//Author : $Author: $
//$Id: $

//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.

//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with this program (see doc/LICENCE.txt); if not, write to the
//Free Software Foundation, Inc., 
//59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

package magellan.library.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import magellan.library.Unit;
import magellan.library.relation.AttackRelation;
import magellan.library.utils.Resources;

/**
 * An
 *
 * @author ...
 * @version 1.0, Jul 30, 2007
 */
public class AttackInspector extends AbstractInspector {

  /** The singleton instance. */
  public static final AttackInspector INSPECTOR = new AttackInspector();

  /**
   * Returns a (singleton) instance.
   * 
   * @return An instance of this class
   */
  public static AttackInspector getInstance() {
    return INSPECTOR;
  }

  protected AttackInspector() {
  }

  /**
   * @see magellan.library.tasks.AbstractInspector#reviewUnit(magellan.library.Unit, int)
   */
  public List<Problem> reviewUnit(Unit u, int type) {
    if((u == null) || u.ordersAreNull()) {
      return Collections.emptyList();
    }

    if(type == Problem.INFORMATION) {
      return Collections.emptyList();
    }

    List<Problem> problems = new ArrayList<Problem>(2);

    int line = 0;
    int wrongStatus = Integer.MAX_VALUE;

    List relations = u.getRelations(AttackRelation.class);
    for(Object o : relations) {
      if (o instanceof AttackRelation){
        AttackRelation relation = (AttackRelation) o;
        if (!relation.source.equals(u))
          continue;
        if (type == Problem.WARNING){
          if (relation.source.getFaction().getAllies().containsKey(relation.target.getFaction().getID()))
            problems.add(new CriticizedWarning(u, u, this, Resources.get("tasks.attackinspector.warning.friendlyfire"), relation.line));
        }
        // TODO define as constants, use modified combat status
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
        if (u.getCombatStatus()>3){
          wrongStatus=Math.min(wrongStatus, relation.line);
        }

      }
    }
    if (type == Problem.ERROR){
      if (wrongStatus!=Integer.MAX_VALUE)
        problems.add(new CriticizedError(u, u, this, Resources.get("tasks.attackinspector.warning.notfighting"), wrongStatus));
    }
    if(problems.isEmpty()) {
      return Collections.emptyList();
    } else {
      return problems;
    }
  }

}
