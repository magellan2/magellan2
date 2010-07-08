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

import java.math.BigDecimal;

import magellan.library.Building;
import magellan.library.Region;
import magellan.library.Rules;
import magellan.library.Ship;
import magellan.library.Skill;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.EresseaGameSpecificRules;
import magellan.library.rules.CastleType;
import magellan.library.rules.Race;

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
    } else if (maxCastle.getBuildingType().equals(getRules().getCastleType("Wachstube"))) {
      rate = .5f;
    } else if (maxCastle.getBuildingType().equals(getRules().getCastleType("Wachturm"))) {
      rate = 1;
    } else if (maxCastle.getBuildingType().equals(getRules().getCastleType("Befestigung"))) {
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

  @Override
  public int getShipRange(Ship s) {
    if (s.getSpeed() != -1 && s.getModifiedOwnerUnit() == s.getOwnerUnit())
      return s.getSpeed();

    // Reichweite (bei Schaden aufrunden)
    int rad = s.getShipType().getRange();

    if (s.getModifiedOwnerUnit() != null) {
      if (s.getModifiedOwnerUnit().getRace().getAdditiveShipBonus() != 0) {
        rad += s.getModifiedOwnerUnit().getRace().getAdditiveShipBonus();
      }

      // add +1 to speed for every six levels over half (rounded up!) minimum captain speed
      Skill sailing =
          s.getModifiedOwnerUnit().getSkill(getRules().getSkillType(EresseaConstants.S_SEGELN));
      if (sailing != null) {
        rad +=
            Math
                .max(0, (sailing.getLevel() - (1 + s.getShipType().getCaptainSkillLevel()) / 2) / 6);
      }
    }

    // rad = rad*(100.0-damageRatio)/100.0
    rad =
        new BigDecimal(rad).multiply(new BigDecimal(100 - s.getDamageRatio())).divide(
            new BigDecimal(100), BigDecimal.ROUND_UP).intValue();

    return rad;
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificRules#isToroidal()
   */
  @Override
  public boolean isToroidal() {
    return true;
  }
}
