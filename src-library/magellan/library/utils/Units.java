// class magellan.library.utils.Units
// created on Nov 23, 2008
//
// Copyright 2003-2008 by magellan project team
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
package magellan.library.utils;


import java.util.Collection;
import java.util.Iterator;

import magellan.library.Faction;
import magellan.library.Ship;
import magellan.library.Skill;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.SkillType;

public class Units {

  /**
   * Returns <code>true</code> iff f is a privileged faction. 
   * 
   * @see {@link Faction#isPrivileged()}
   */
  public static boolean isPrivileged(Faction f) {
  	return (f != null) && (f.isPrivileged());
  }

  /**
   * Returns <code>true</code> iff u is not a spy and belongs to a privileged faction.
   */
  public static boolean isPrivilegedAndNoSpy(Unit u) {
  	return (u != null) && Units.isPrivileged(u.getFaction()) && !u.isSpy();
  }
  
  public static int getCaptainSkillAmount(Ship s) {
    // FIXME shouldn't access getData() from here, maybe move to GameSpecific
    SkillType sailingSkillType = s.getData().rules.getSkillType(EresseaConstants.S_SEGELN, true);
    Unit owner = s.getModifiedOwnerUnit();
    int captainSkillAmount = 0;
    if (owner!=null){
      Skill sailingSkill = owner.getModifiedSkill(sailingSkillType);
       captainSkillAmount = (sailingSkill == null) ? 0 : sailingSkill.getLevel();
    }
    return captainSkillAmount;
  }

  public static int getSailingSkillAmount(Ship s){
    SkillType sailingSkillType = s.getData().rules.getSkillType(EresseaConstants.S_SEGELN, true);
    int sailingSkillAmount = 0;
    // pavkovic 2003.10.03: use modifiedUnits to reflect FUTURE value?
    Collection modUnits = s.modifiedUnits(); // the collection of units on the ship in the next turn

    for(Iterator sailors = modUnits.iterator(); sailors.hasNext();) {
      Unit u = (Unit) sailors.next();
      Skill sailingSkill = u.getModifiedSkill(sailingSkillType);

      if(sailingSkill != null) {
        sailingSkillAmount += (sailingSkill.getLevel() * u.getModifiedPersons());
      }
    }
    return sailingSkillAmount;
  }

}
