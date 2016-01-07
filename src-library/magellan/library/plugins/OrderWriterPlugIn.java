// class magellan.library.plugins.OrderWriterPlugIn
// created on 14.10.2015
//
// Copyright 2003-2015 by magellan project team
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
package magellan.library.plugins;

import magellan.library.Unit;

/**
 * Classes that implement this interface can be added to the OrderWriter Environment and can operate
 * inside the OrderWriting process.
 * 
 * @author Thoralf Rickert-Wendt
 * @version 1.0, 14.10.2015
 */
public interface OrderWriterPlugIn {
  /**
   * During the writing process a plugin can decide, if the command for a specific unit should be
   * written out.
   */
  public boolean ignoreUnit(Unit unit);
}
