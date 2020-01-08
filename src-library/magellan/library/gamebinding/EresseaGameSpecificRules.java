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
package magellan.library.gamebinding;

import java.math.BigDecimal;
import java.util.Map;

import magellan.library.Alliance;
import magellan.library.Building;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.Order;
import magellan.library.Region;
import magellan.library.Rules;
import magellan.library.Ship;
import magellan.library.Skill;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.gamebinding.e3a.MaintainOrder;
import magellan.library.relation.RecruitmentRelation;
import magellan.library.rules.CastleType;
import magellan.library.rules.ItemType;
import magellan.library.rules.Race;
import magellan.library.rules.RegionType;
import magellan.library.rules.UnitContainerType;
import magellan.library.utils.logging.Logger;

/**
 * This class implements all Eressea specific rule informations.
 *
 * @author Thoralf Rickert
 * @version 1.0, 19.04.2009
 */
public class EresseaGameSpecificRules implements GameSpecificRules {

  private static Logger log = Logger.getInstance(EresseaGameSpecificRules.class);

  private Rules rules;

  protected EresseaGameSpecificRules(Rules rules) {
    this.rules = rules;
  }

  /**
   * Returns the value of rules.
   *
   * @return Returns rules.
   */
  public Rules getRules() {
    return rules;
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificRules#getMaxWorkers(magellan.library.Region)
   */
  public Integer getMaxWorkers(Region region) {

    if ((region.getTrees() != -1) && (region.getSprouts() != -1) && (region.getType() != null)) {
      int inhabitants = ((RegionType) region.getType()).getInhabitants();
      int trees = Math.max(region.getTrees(), 0);
      int sprouts = Math.max(region.getSprouts(), 0);

      return Integer.valueOf(Math.max(0, inhabitants - (8 * trees) - (4 * sprouts)));
    }

    return null;
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificRules#getMaxEntertain(magellan.library.Region)
   */
  public Integer getMaxEntertain(Region region) {
    return maxEntertain(region.getSilver());
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificRules#getMaxOldEntertain(magellan.library.Region)
   */
  public Integer getMaxOldEntertain(Region region) {
    return maxEntertain(region.getOldSilver());
  }

  /**
   * Return the silver that can be earned through entertainment in a region with the given amount of
   * silver.
   */
  private int maxEntertain(int silver) {
    if (silver >= 0)
      return silver / 20;
    return -1;
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificRules#isShip(magellan.library.Ship)
   */
  public boolean isShip(Ship ship) {
    // in Eressea is a ship a ship.
    return true;
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificRules#canLandInRegion(magellan.library.Ship,
   *      magellan.library.Region)
   */
  public boolean canLandInRegion(Ship ship, Region region) {
    if (region.getType().getID().equals(EresseaConstants.RT_FIREWALL))
      return false;

    if (ship.getType().getID().equals(EresseaConstants.ST_BOAT))
      return true; // can land everywhere

    if (region.getType().getID().equals(EresseaConstants.RT_PLAIN))
      return true;
    if (region.getType().getID().equals(EresseaConstants.RT_FOREST))
      return true;
    if (region.getType().getID().equals(EresseaConstants.RT_OCEAN))
      return true;

    return false;
  }

  /**
   * Calculates the wage for the units of a certain faction in the specified region.
   */
  public int getWage(Region region, Race race) {
    int wage = region.getWage() - 1;
    if (getRules().getRace(EresseaConstants.R_ORKS) == null
        || race.getID().equals(getRules().getRace(EresseaConstants.R_ORKS).getID())) {
      switch (wage) {
      case 12:
        wage = 11;

        break;

      case 13:
        wage = 12;

        break;

      case 14:
        wage = 12;

        break;

      case 15:
        wage = 13;

        break;
      }
    }

    return wage;
  }

  public int getShipRange(Ship s) {
    if (s.getSpeed() != -1 && s.getModifiedOwnerUnit() == s.getOwnerUnit())
      return s.getSpeed();

    // Reichweite (bei Schaden aufrunden)
    int rad = s.getShipType().getRange();

    if (s.getShipType().getRangeFormula() != null) {
      if (s.getShipType().getRangeFormula().equals(
          "$range + max(0, log_3(($currentcaptainlevel / $captainlevel)))")) {
        if (s.getModifiedOwnerUnit() != null) {
          Skill sailing =
              s.getModifiedOwnerUnit().getSkill(getRules().getSkillType(EresseaConstants.S_SEGELN));
          if (sailing != null) {
            for (int sail = Math.max(0, (sailing.getLevel() / s.getShipType()
                .getCaptainSkillLevel())); sail >= 3; sail /= 3) {
              ++rad;
            }
          }
        }
      } else if (s.getShipType().getRangeFormula().equals(
          "$range + max(0, ($currentcaptainlevel - (1 + $captainlevel) / 2) / 6)")) {
        if (s.getModifiedOwnerUnit() != null) {
          // Kapitäne erhalten einen Bonus von +1 auf die Reichweite ihrer Schiffe für je 6 Stufen
          // Segeln-Talent über dem halben Mindest-Kapitänstalent (aufgerundet, siehe Tabelle).
          // add +1 to speed for every six levels over half (rounded up!) minimum captain speed
          Skill sailing =
              s.getModifiedOwnerUnit().getSkill(getRules().getSkillType(EresseaConstants.S_SEGELN));
          if (sailing != null) {
            rad +=
                Math.max(0, (sailing.getLevel() - (1 + s.getShipType().getCaptainSkillLevel()) / 2)
                    / 6);
          }
        }
      } else {
        log.fine("unknown ship type formula " + s.getShipType().getRangeFormula() + " in " + s
            .getShipType().getName());
      }
    }

    if ((s.getModifiedOwnerUnit() != null) && (s.getModifiedOwnerUnit().getRace() != null)
        && s.getModifiedOwnerUnit().getRace().getAdditiveShipBonus() != 0) {
      rad += s.getModifiedOwnerUnit().getRace().getAdditiveShipBonus();
    }

    // rad = rad*(100.0-damageRatio)/100.0
    rad =
        new BigDecimal(rad).multiply(new BigDecimal(100 - s.getDamageRatio())).divide(
            new BigDecimal(100), BigDecimal.ROUND_UP).intValue();

    return rad;
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificRules#getPeasantMaintenance(magellan.library.Region)
   */
  public int getPeasantMaintenance(Region region) {
    return 10;
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificRules#getTeachFactor()
   */
  public int getTeachFactor() {
    return 10;
  }

  public int getSilverPerWeightUnit() {
    return 100;
  }

  /**
   * Returns true if the type is a castle (Befestigung, Turm, ...)
   *
   * @see magellan.library.gamebinding.GameSpecificRules#isCastle(magellan.library.rules.UnitContainerType)
   */
  public boolean isCastle(UnitContainerType type) {
    return type instanceof CastleType;
  }

  public int getMaxHorsesWalking(Unit u) {
    int skillLevel = 0;
    Skill s = u.getModifiedSkill(getRules().getSkillType(EresseaConstants.S_REITEN, true));

    if (s != null) {
      skillLevel = s.getLevel();
    }
    return ((skillLevel * u.getModifiedPersons() * 4) + u.getModifiedPersons());
  }

  public int getMaxHorsesRiding(Unit u) {
    int skillLevel = 0;
    Skill s = u.getModifiedSkill(getRules().getSkillType(EresseaConstants.S_REITEN, true));

    if (s != null) {
      skillLevel = s.getLevel();
    }
    return (skillLevel * u.getModifiedPersons() * 2);
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificRules#isPooled(magellan.library.Unit,
   *      magellan.library.StringID)
   */
  public boolean isPooled(Unit unit, StringID type) {
    if (unit.getData().getDate().getDate() > 558
        && getRules().getGameSpecificStuff().getName().equalsIgnoreCase("eressea"))
      // the pools were activated in Eressea starting from report no. 559
      return true;
    if (unit.getFaction() == null || unit.getFaction().getOptions() == null) {
      log.fine("Don't know if pool is active for " + unit);
      return false;
    }
    if (EresseaConstants.I_USILVER.equals(type))
      return unit.getFaction().getOptions() != null
          && unit.getFaction().getOptions().isActive(EresseaConstants.OC_SILVERPOOL);
    else
      return unit.getFaction().getOptions().isActive(EresseaConstants.OC_ITEMPOOL);
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificRules#isPooled(magellan.library.Unit,
   *      magellan.library.rules.ItemType)
   */
  public boolean isPooled(Unit unit, ItemType type) {
    return isPooled(unit, type.getID());
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificRules#getRecruitmentLimit(magellan.library.Unit,
   *      magellan.library.rules.Race)
   */
  public int getRecruitmentLimit(Unit u, Race race) {
    if (u.getRegion() == null)
      return 0;
    int recruited = 0;
    for (RecruitmentRelation rrel : u.getRegion().getZeroUnit().getRelations(
        RecruitmentRelation.class)) {
      recruited +=
          rrel.amount / rrel.race.getRecruitmentFactor()
              + (rrel.amount % rrel.race.getRecruitmentFactor() > 0 ? 1 : 0);
    }
    return (u.getRegion().maxRecruit() - recruited) * race.getRecruitmentFactor();
  }

  public boolean isAllied(Faction faction, Faction ally, int aState) {
    if (faction == null || ally == null)
      return false;
    if (faction.equals(ally))
      return true;
    Map<EntityID, Alliance> allies = faction.getAllies();
    if (allies == null)
      return false;
    Alliance alliance = allies.get(ally.getID());
    return alliance != null && alliance.getState(aState);
  }

  public int getMaxTrade(Region region) {
    if (region.getPeasants() > 0) {
      int volume = region.getPeasants() / 100;
      for (Building b : region.buildings())
        if (b.getBuildingType().getID().equals(EresseaConstants.B_CARAVANSEREI)) {
          volume *= 2;
        }
      return volume;
    }
    return -1;
  }

  public Unit getMaintainer(Building b) {
    Unit owner = b.getModifiedOwnerUnit();
    if (owner == null && b.getBuildingType().isMaintainedByRegionOwner()) {
      owner = b.getRegion().getOwnerUnit();
    }
    boolean maintain = true;
    if (owner != null) {
      for (Order order : owner.getOrders2()) {
        if (order instanceof MaintainOrder) {
          MaintainOrder mOrder = (MaintainOrder) order;
          if (mOrder.isValid() && mOrder.isNot()
              && (mOrder.getBuilding() == null || b.getID().equals(mOrder.getBuilding()))) {
            maintain = false;
            break;
          }
        }
      }
    }

    if (maintain)
      return owner;
    else
      return null;
  }
}
