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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
package magellan.library.gamebinding.e3a;

import magellan.library.Building;
import magellan.library.Faction;
import magellan.library.Region;
import magellan.library.Rules;
import magellan.library.Ship;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.EresseaGameSpecificRules;
import magellan.library.rules.CastleType;
import magellan.library.rules.ItemType;
import magellan.library.rules.Race;
import magellan.library.utils.logging.Logger;

/**
 * This class implements all Eressea specific rule informations.
 * 
 * @author Thoralf Rickert
 * @version 1.0, 19.04.2009
 */
public class E3AGameSpecificRules extends EresseaGameSpecificRules {

  protected E3AGameSpecificRules(Rules rules) {
    super(rules);
  }

  /**
   * Returns the tax income
   * 
   * @see magellan.library.gamebinding.GameSpecificRules#getMaxEntertain(magellan.library.Region)
   */
  @Override
  public Integer getMaxEntertain(Region region) {
    int maxsize = 0;
    Building maxCastle = null;
    for (Building building : region.buildings()) {
      if (building.getType() instanceof CastleType) {
        if (building.getSize() > maxsize) {
          maxCastle = building;
          maxsize = building.getSize();
        }
      }
    }
    float rate = 0;
    if (maxCastle == null) {
      rate = 0;
    } else if (maxCastle.getBuildingType().equals(
        getRules().getCastleType(E3AConstants.B_GUARDHOUSE))) {
      rate = .5f;
    } else if (maxCastle.getBuildingType().equals(
        getRules().getCastleType(E3AConstants.B_GUARDTOWER))) {
      rate = 1;
    } else {
      if (maxCastle.getBuildingType().equals(getRules().getCastleType("Befestigung"))) {
        rate = 1;
      } else if (maxCastle.getBuildingType().equals(getRules().getCastleType("Turm"))) {
        rate = 2;
      } else if (maxCastle.getBuildingType().equals(getRules().getCastleType("Burg"))) {
        rate = 3;
      } else if (maxCastle.getBuildingType().equals(getRules().getCastleType("Festung"))) {
        rate = 4;
      } else if (maxCastle.getBuildingType().equals(getRules().getCastleType("Zitadelle"))) {
        rate = 5;
      }
      if (maxCastle.getBuildingType().getBuildSkillLevel() - 1 != rate) {
        Logger.getInstance(this.getClass()).warn("peasant wage inconsistency with " + maxCastle);
      }
    }

    rate = Math.min(rate, region.getMorale() / 2f);

    return region.getMourning() == 1 ? 0 : (int) (rate * region.getSilver() / 100);
  }

  /**
   * Returns 0.
   * 
   * @see magellan.library.gamebinding.GameSpecificRules#getMaxOldEntertain(magellan.library.Region)
   *      FIXME maybe return something depending on morale (and biggest castle or watch)
   */
  @Override
  public Integer getMaxOldEntertain(Region region) {
    return 0;
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificRules#isShip(magellan.library.Ship)
   */
  @Override
  public boolean isShip(Ship ship) {
    return true;
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificRules#canLandInRegion(magellan.library.Ship,
   *      magellan.library.Region)
   */
  @Override
  public boolean canLandInRegion(Ship ship, Region region) {
    if (region.getType().getID().equals(EresseaConstants.RT_FIREWALL))
      return false;

    if (ship.getType().getID().equals(E3AConstants.ST_EINBAUM)
        || ship.getType().getID().equals(E3AConstants.ST_KUTTER)
        || ship.getType().getID().equals(E3AConstants.ST_BARKE)
        || ship.getType().getID().equals(E3AConstants.ST_KOENIGSBARKE))
      return true; // can land everywhere

    if (ship.getType().getID().equals(E3AConstants.ST_FLOSS)) {
      if (region.getType().getID().equals(EresseaConstants.RT_PLAIN))
        return true;
      if (region.getType().getID().equals(EresseaConstants.RT_FOREST))
        return true;
      if (region.getType().getID().equals(EresseaConstants.RT_OCEAN))
        return true;
      if (region.getType().getID().equals(EresseaConstants.RT_SWAMP))
        return true;
      if (region.getType().getID().equals(EresseaConstants.RT_DESERT))
        return true;
    }

    if (region.getType().getID().equals(EresseaConstants.RT_PLAIN))
      return true;
    if (region.getType().getID().equals(EresseaConstants.RT_FOREST))
      return true;
    if (region.getType().getID().equals(EresseaConstants.RT_OCEAN))
      return true;

    return false;
  }

  /**
   * @see magellan.library.gamebinding.EresseaGameSpecificRules#getWage(magellan.library.Region,
   *      magellan.library.rules.Race)
   */
  @Override
  public int getWage(Region region, Race race) {
    if (race.equals(getRules().getRace(EresseaConstants.R_GOBLINS)))
      return 6;
    else if (race.getRecruitmentCosts() > 0)
      return 10;
    else
      return -1;
  }

  /**
   * Returns true. Material and silver pools are always active in E3.
   * 
   * @see magellan.library.gamebinding.EresseaGameSpecificRules#isPooled(magellan.library.Unit,
   *      magellan.library.StringID)
   */
  @Override
  public boolean isPooled(Unit unit, StringID typeID) {
    return true;
  }

  /**
   * Returns true. Material and silver pools are always active in E3.
   * 
   * @see magellan.library.gamebinding.EresseaGameSpecificRules#isPooled(magellan.library.Unit,
   *      magellan.library.rules.ItemType)
   */
  @Override
  public boolean isPooled(Unit unit, ItemType type) {
    return true;
  }

  /**
   * Interprets Alliance as HELp COMBAT.
   * 
   * @see magellan.library.gamebinding.EresseaGameSpecificRules#isAllied(magellan.library.Faction,
   *      magellan.library.Faction, int)
   */
  @Override
  public boolean isAllied(Faction faction, Faction ally, int aState) {
    return super.isAllied(faction, ally, aState)
        || ((aState | EresseaConstants.A_COMBAT) != 0 && faction.getAlliance() != null
            && faction.getAlliance().getFactions().contains(ally.getID()) && super.isAllied(
                faction, ally, aState ^ EresseaConstants.A_COMBAT));
  }

  @Override
  public int getMaxTrade(Region region) {
    // we could use the unofficial official rule here or even compute the volume of neighboring
    // regions, but that would be somewhat pointless and fragile...
    return -1;
  }
}
