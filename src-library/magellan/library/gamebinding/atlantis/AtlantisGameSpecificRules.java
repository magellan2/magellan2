// class magellan.library.gamebinding.AtlantisGameSpecificRules
// created on Apr 16, 2013
//
// Copyright 2003-2013 by magellan project team
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
package magellan.library.gamebinding.atlantis;

import java.util.Map;

import magellan.library.Building;
import magellan.library.Faction;
import magellan.library.Region;
import magellan.library.Rules;
import magellan.library.Ship;
import magellan.library.Skill;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.gamebinding.GameSpecificRules;
import magellan.library.rules.ItemType;
import magellan.library.rules.Race;
import magellan.library.rules.UnitContainerType;

/**
 * Rules for Atlantis game
 */
public class AtlantisGameSpecificRules implements GameSpecificRules {

  private Rules rules;

  public AtlantisGameSpecificRules(Rules rules) {
    this.rules = rules;
  }

  public Integer getMaxWorkers(Region region) {
    return region.getRegionType().getInhabitants();
  }

  public Integer getMaxEntertain(Region region) {
    if (region.getSilver() > 0)
      return region.getSilver() / 20;
    else
      return -1;
  }

  public Integer getMaxOldEntertain(Region region) {
    if (region.getOldSilver() > 0)
      return region.getOldSilver() / 20;
    else
      return -1;
  }

  public boolean isShip(Ship ship) {
    return true;
  }

  public boolean canLandInRegion(Ship ship, Region region) {
    return true;
  }

  public int getShipRange(Ship s) {
    return 1;
  }

  public int getWage(Region region, Race race) {
    return region.getWage();
  }

  public int getPeasantMaintenance(Region region) {
    return 10;
  }

  public int getTeachFactor() {
    return 10;
  }

  public int getSilverPerWeightUnit() {
    return Integer.MAX_VALUE;
  }

  public boolean isCastle(UnitContainerType type) {
    return true;
  }

  public int getMaxHorsesWalking(Unit u) {
    return Integer.MAX_VALUE;
  }

  public int getMaxHorsesRiding(Unit u) {
    return 0;
  }

  public boolean isPooled(Unit unit, StringID typeID) {
    return false;
  }

  public boolean isPooled(Unit unit, ItemType type) {
    return false;
  }

  public int getRecruitmentLimit(Unit u, Race race) {
    return Integer.MAX_VALUE;
  }

  public boolean isAllied(Faction faction, Faction ally, int aState) {
    return false;
  }

  public int getMaxTrade(Region region) {
    return 0;
  }

  public Unit getMaintainer(Building b) {
    return b.getModifiedOwnerUnit();
  }

  public Map<StringID, Skill> getModifiedSkills(Unit unit) {
    return null;
  }

}
