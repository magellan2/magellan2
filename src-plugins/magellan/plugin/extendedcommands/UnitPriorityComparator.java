// class magellan.plugin.extendedcommands.UnitPriorityComparator
// created on 02.09.2010
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
package magellan.plugin.extendedcommands;

import java.util.Comparator;

import magellan.library.Unit;

/**
 * Compares two units with their script priorities
 * 
 * @author Thoralf Rickert
 * @version 1.0, 12.04.2008
 */
public class UnitPriorityComparator implements Comparator<Unit> {
  protected ExtendedCommands commands = null;

  /**
   * 
   */
  public UnitPriorityComparator(ExtendedCommands commands) {
    this.commands = commands;
  }

  public int compare(Unit o1, Unit o2) {
    Script s1 = commands.getCommands(o1);
    Script s2 = commands.getCommands(o2);
    return s1.getPriority().compareTo(s2.getPriority());
  }

}
