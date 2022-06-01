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

import java.util.Map;

import magellan.library.Building;
import magellan.library.Faction;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Skill;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.rules.ItemType;
import magellan.library.rules.Race;
import magellan.library.rules.UnitContainerType;

/**
 * This interface must be implemented to create game specific rule informations like maxWorkers,
 * maxEntertainers and so on.
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
   * Returns true, if the given ship is really a ship, because f.e. in Allanon a Karawane is marked
   * as a ship, but it's travelling on land.
   */
  public boolean isShip(Ship ship);

  /**
   * This method checks if a ship can land in a specific region
   */
  public boolean canLandInRegion(Ship ship, Region region);

  /**
   * Returns the current maximum range of a ship.
   */
  public int getShipRange(Ship s);

  /**
   * Returns the wage for <code>race</code> in <code>region</code> or -1 if unknown or not
   * applicable.
   */
  public int getWage(Region region, Race race);

  /**
   * Returns the amount of silver that peasants need to survive.
   */
  public int getPeasantMaintenance(Region region);

  /**
   * Returns the amount of students that one teacher can teach.
   */
  public int getTeachFactor();

  /**
   * Returns the number of silver pieces per weight unit (GE).
   */
  public int getSilverPerWeightUnit();

  /**
   * Returns true if the type is a castle (Befestigung, Turm, ...)
   */
  public boolean isCastle(UnitContainerType type);

  /**
   * Returns the maximum number of horses the unit can handle while walking.
   *
   * @param u
   */
  public int getMaxHorsesWalking(Unit u);

  /**
   * Returns the maximum number of horses the unit can handle while riding.
   *
   * @param u
   */
  public int getMaxHorsesRiding(Unit u);

  /**
   * Returns true if the specified unit can access the pool for the specified item.
   *
   * @param unit
   * @param typeID May be <code>null</code>. In that case, the status of the general material pool
   *          is returned.
   * @return <code>true</code> if the unit can access the pool for the type.
   */
  public boolean isPooled(Unit unit, StringID typeID);

  /**
   * Returns true if the specified unit can access the pool for the specified item.
   *
   * @param unit
   * @param type May be <code>null</code>. In that case, the status of the general material pool is
   *          returned.
   * @return <code>true</code> if the unit can access the pool for the type.
   */
  public boolean isPooled(Unit unit, ItemType type);

  /**
   * Returns the maximum possible number of recruits that the specified unit can recruit in the
   * specified race.
   *
   * @param u
   * @param race
   */
  public int getRecruitmentLimit(Unit u, Race race);

  /**
   * Returns true if the faction has the specified HELP state to an ally.
   */
  public boolean isAllied(Faction faction, Faction ally, int aState);

  /**
   * Returns the trade volume of the region
   */
  public int getMaxTrade(Region region);

  /**
   * Returns the unit that pays maintenance for a building.
   *
   * @return if there is no such building or the owner is not paying
   */
  public Unit getMaintainer(Building b);

  public Map<StringID, Skill> getModifiedSkills(Unit unit);

}
