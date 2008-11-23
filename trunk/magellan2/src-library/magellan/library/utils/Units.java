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


import magellan.library.Faction;
import magellan.library.Unit;

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

}
