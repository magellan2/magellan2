// class magellan.library.tasks.ProblemFactory
// created on Jun 9, 2009
//
// Copyright 2003-2009 by magellan project team
//
// Author : $Author: stm$
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

import magellan.library.Faction;
import magellan.library.HasRegion;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.tasks.Problem.Severity;

public class ProblemFactory {

  public static SimpleProblem createProblem(Severity severity, ProblemType type, Region region,
      Unit owner, Faction faction, Object object, Inspector inspector, String message, int line) {
    return new SimpleProblem(severity, type, region, owner, faction, object, inspector, message,
        line);
  }

  /**
   * Creates a problem. Tries to deduce region and faction from the Unit, and inspector and message
   * from the ProblemType.
   * 
   * @param severity
   * @param type
   * @param u
   * @param line
   */
  public static SimpleProblem createProblem(Severity severity, ProblemType type, Unit u,
      Inspector inspector, int line) {
    return new SimpleProblem(severity, type, u.getRegion(), u, u.getFaction(), u, inspector, type
        .getMessage(), line);
  }

  /**
   * Creates a problem without line. Tries to deduce region and faction from the Unit, and inspector
   * and message from the ProblemType.
   * 
   * @param severity
   * @param type
   * @param u
   */
  public static SimpleProblem createProblem(Severity severity, ProblemType type, Unit u,
      Inspector inspector) {
    return new SimpleProblem(severity, type, u.getRegion(), u, u.getFaction(), u, inspector, type
        .getMessage(), -1);
  }

  /**
   * Creates a problem. Tries to deduce unit, region and faction from the UnitContainer, and
   * inspector and message from the ProblemType.
   * 
   * @param severity
   * @param type
   * @param c
   * @param line
   */
  public static SimpleProblem createProblem(Severity severity, ProblemType type, UnitContainer c,
      Inspector inspector, int line) {
    return new SimpleProblem(severity, type,
        c.getOwner() == null ? null : c.getOwner().getRegion(), c.getOwner(), c.getOwner() == null
            ? null : c.getOwner().getFaction(), c, inspector, type.getMessage(), line);
  }

  /**
   * Creates a problem without line. Tries to deduce unit, region and faction from the
   * UnitContainer, and inspector and message from the ProblemType.
   * 
   * @param severity
   * @param type
   * @param c
   */
  public static SimpleProblem createProblem(Severity severity, ProblemType type, UnitContainer c,
      Inspector inspector) {
    Region r = null;
    if (c instanceof HasRegion) {
      r = ((HasRegion) c).getRegion();
    } else {
      r = c.getOwner() == null ? null : c.getOwner().getRegion();
    }

    return new SimpleProblem(severity, type, r, c.getOwner(), c.getOwner() == null ? null : c
        .getOwner().getFaction(), c, inspector, type.getMessage(), -1);
  }

  /**
   * Creates a problem. Tries to deduce region and faction from the Unit, and the inspector from the
   * ProblemType but uses the given message.
   * 
   * @param severity
   * @param type
   * @param u
   * @param inspector
   * @param message
   * @param line
   */
  public static SimpleProblem createProblem(Severity severity, ProblemType type, Unit u,
      Inspector inspector, String message, int line) {
    return new SimpleProblem(severity, type, u.getRegion(), u, u.getFaction(), u, inspector,
        message, line);
  }

}
