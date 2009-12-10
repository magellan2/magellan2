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

import magellan.library.Region;
import magellan.library.Rules;
import magellan.library.Ship;
import magellan.library.rules.Race;
import magellan.library.rules.RegionType;

/**
 * This class implements all Eressea specific rule informations.
 * 
 * @author Thoralf Rickert
 * @version 1.0, 19.04.2009
 */
public class EresseaGameSpecificRules implements GameSpecificRules {

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

      return new Integer(Math.max(0, inhabitants - (8 * trees) - (4 * sprouts)));
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
    if (silver >= 0) {
      return silver / 20;
    }
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
    int wage = region.getWage();
    if (race.getName().equalsIgnoreCase(
        region.getData().rules.getRace(AllanonConstants.R_ORKS).getName())) {
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
    // Reichweite (bei Schaden aufrunden)
    int rad = s.getShipType().getRange();

    if((s.getModifiedOwnerUnit() != null) && (s.getModifiedOwnerUnit().getRace() != null) &&
           s.getModifiedOwnerUnit().getRace().getAdditiveShipBonus()!=0) {
        rad+=s.getModifiedOwnerUnit().getRace().getAdditiveShipBonus();
      }

    // rad = rad*(100.0-damageRatio)/100.0
    rad = new BigDecimal(rad).multiply(new BigDecimal(100 - s.getDamageRatio()))
                 .divide(new BigDecimal(100), BigDecimal.ROUND_UP).intValue();
    
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
}
