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
import magellan.library.Ship;
import magellan.library.rules.Race;


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
  
  /**
   * Returns true, if the given ship is really a ship, because
   * f.e. in Allanon a Karawane is marked as a ship, but it's
   * travelling on land.
   */
  public boolean isShip(Ship ship);
  
  /**
   * This method checks if a ship can land in a specific region
   */
  public boolean canLandInRegion(Ship ship, Region region);


  /**
   * Returns the wage for <code>race</code> in <code>region</code>. 
   */
  public int getWage(Region region, Race race);

}
