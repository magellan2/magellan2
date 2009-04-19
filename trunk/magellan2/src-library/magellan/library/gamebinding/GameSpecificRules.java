// class magellan.library.gamebinding.GameSpecificRules
// created on 19.04.2009
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
package magellan.library.gamebinding;

import magellan.library.Region;


/**
 * This interface must be implemented to create game specific
 * rule informations like maxWorkers, maxEntertainers and so on.
 *
 * @author Thoralf Rickert
 * @version 1.0, 19.04.2009
 */
public interface GameSpecificRules {

  /**
   * Returns the amount of max workers in a specific region.
   */
  public Integer getMaxWorkers(Region region);
  
  
  /**
   * Returns the amount of max entertainment in a specific region.
   */
  public Integer getMaxEntertain(Region region);
  
  /**
   * Returns the amount of max entertainment in a specific region.
   */
  public Integer getMaxOldEntertain(Region region);
}
