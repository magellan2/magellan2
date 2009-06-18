// class magellan.library.gamebinding.AllanonGameSpecificRules
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
package magellan.library.gamebinding.e3a;

import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.EresseaGameSpecificRules;
import magellan.library.gamebinding.GameSpecificRules;
import magellan.library.rules.Race;

/**
 * This class implements all Eressea specific rule informations.
 *
 * @author Thoralf Rickert
 * @version 1.0, 19.04.2009
 */
public class E3AGameSpecificRules extends EresseaGameSpecificRules implements GameSpecificRules {
  private static GameSpecificRules _instance = null;

  private E3AGameSpecificRules() {
  }
  
  public static synchronized GameSpecificRules getInstance() {
    if (_instance == null) _instance = new E3AGameSpecificRules();
    return _instance;
  }
  
  /**
   * Returns 0.
   * 
   * @see magellan.library.gamebinding.GameSpecificRules#getMaxEntertain(magellan.library.Region)
   * FIXME maybe return something depending on morale (and biggest castle or watch)
   */
  public Integer getMaxEntertain(Region region) {
    return Integer.MIN_VALUE;
  }

  /**
   * Returns 0.
   * 
   * @see magellan.library.gamebinding.GameSpecificRules#getMaxOldEntertain(magellan.library.Region)
   * FIXME maybe return something depending on morale (and biggest castle or watch)
   */
  public Integer getMaxOldEntertain(Region region) {
    return 0;
  }

  
  
  /**
   * @see magellan.library.gamebinding.GameSpecificRules#isShip(magellan.library.Ship)
   */
  public boolean isShip(Ship ship) {
    return true;
  }
  
  /**
   * @see magellan.library.gamebinding.GameSpecificRules#canLandInRegion(magellan.library.Ship, magellan.library.Region)
   */
  public boolean canLandInRegion(Ship ship, Region region) {
    if (region.getType().getID().equals(EresseaConstants.RT_FIREWALL)) return false;
    
    if (ship.getType().getID().equals(EresseaConstants.ST_BOAT)) return true; // can land everywhere
    
    if (region.getType().getID().equals(EresseaConstants.RT_PLAIN)) return true;
    if (region.getType().getID().equals(EresseaConstants.RT_FOREST)) return true;
    if (region.getType().getID().equals(EresseaConstants.RT_OCEAN)) return true;
    
    return false;
  }

  /**
   * @see magellan.library.gamebinding.EresseaGameSpecificRules#getWage(magellan.library.Region, magellan.library.rules.Race)
   */
  @Override
  public int getWage(Region region, Race race) {
    if (race.equals(region.getData().rules.getRace(EresseaConstants.R_GOBLINS)))
      return 6;
    else
      return 10;
  }

  
}
