// class magellan.library.gamebinding.AllanonOrderCompleter
// created on 17.04.2008
//
// Copyright 2003-2008 by magellan project team
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import magellan.library.Building;
import magellan.library.GameData;
import magellan.library.Ship;
import magellan.library.Skill;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.completion.CompleterSettingsProvider;
import magellan.library.completion.Completion;
import magellan.library.rules.SkillType;
import magellan.library.utils.Resources;

/**
 *
 * @author Thoralf Rickert
 * @version 1.0, 17.04.2008
 */
public class AllanonOrderCompleter extends EresseaOrderCompleter {
  /**
   * Creates a new <tt>AllanonOrderCompleter</tt> taking context information from the specified
   * <tt>GameData</tt> object.
   *
   * @param gd The <tt>GameData</tt> this completer uses as context.
   */
  public AllanonOrderCompleter(GameData gd, CompleterSettingsProvider ac) {
    super(gd,ac);
  }


  /**
   * Returns the learn cost for a specific skill.
   *
   * @param skillType the skill to be learned
   * @param unit the Unit
   *
   * @return the cost to learn a skill for the given unit. If the unit has no persons the cost
   *       for one person is returned.
   */
  @Override
  public int getSkillCost(SkillType skillType, Unit unit) {
    int cost = 0;

    if(skillType.getID().equals(AllanonConstants.S_TAKTIK) ||
         skillType.getID().equals(AllanonConstants.S_KRAEUTERKUNDE) ||
         skillType.getID().equals(AllanonConstants.S_ALCHIMIE) ||
         skillType.getID().equals(AllanonConstants.S_MEUCHELN) ||
         skillType.getID().equals(AllanonConstants.S_MECHANIK)) {
      int level = 0;
      Skill skill = (unit != null) ? unit.getSkill(skillType) : null;

      if(skill != null && unit!=null) {
        if(skill.noSkillPoints()) {
          level = skill.getLevel() - skill.getModifier(unit);
        } else {
          int days = unit.getSkill(skillType).getPointsPerPerson();
          level = (int) Math.floor(Math.sqrt((days / 15.0) + 0.25) - 0.5);
        }
      }
      
      int nextLevel = level + 1;
      cost = nextLevel*50;
      
    } else if(skillType.getID().equals(AllanonConstants.S_MAGIE)) {
      // get magic level without modifier
      int level = 0;
      Skill skill = (unit != null) ? unit.getSkill(skillType) : null;

      if(skill != null) {
        if(skill.noSkillPoints()) {
          level = skill.getLevel() - skill.getModifier(unit);
        } else {
          int days = unit.getSkill(skillType).getPointsPerPerson();
          level = (int) Math.floor(Math.sqrt((days / 15.0) + 0.25) - 0.5);
        }
      }
      
      int nextLevel = level + 1;
      cost = (nextLevel*nextLevel) *50;
    }

    if(unit != null) {
      if((unit.getModifiedBuilding() != null) &&
         unit.getModifiedBuilding().getType().equals(getData().rules.getBuildingType(StringID.create("Akademie")))) {
        if(cost == 0) {
          cost = 100;
        } else {
          cost *= 2;
        }
      }

      cost *= Math.max(1, unit.getModifiedPersons());
    }

    return cost;
  }

  /**
   * 
   * @see magellan.library.gamebinding.EresseaOrderCompleter#cmplt()
   */
  @Override
  protected void cmplt() {
    super.cmplt();
    getCompletions().add(new Completion(Resources.getOrderTranslation(AllanonConstants.O_ANWERBEN)));
    getCompletions().add(new Completion(Resources.getOrderTranslation(AllanonConstants.O_BEANSPRUCHE)," "));
    getCompletions().add(new Completion(Resources.getOrderTranslation(AllanonConstants.O_MEUCHELN)," "));
  }
  
  
  /**
   * @see magellan.library.gamebinding.EresseaOrderCompleter#cmpltBetrete()
   */
  @Override
  protected void cmpltBetrete() {
    Iterator<Building> iter1 = getRegion().buildings().iterator();

    if(iter1.hasNext()) {
      getCompletions().add(new Completion(Resources.getOrderTranslation(AllanonConstants.O_CASTLE)," ", 7));
    }

    for(; iter1.hasNext();) {
      UnitContainer uc = iter1.next();

      if(!uc.equals(getUnit().getBuilding())) {
        getCompletions().add(new Completion(uc.getName() + " (" + uc.getID() + ")", Resources.getOrderTranslation(AllanonConstants.O_CASTLE) + " " + uc.getID() + " ;" + uc.getName(), "", Completion.DEFAULT_PRIORITY-1));
        getCompletions().add(new Completion(uc.getID() + " (" + uc.getName() + ")", Resources.getOrderTranslation(AllanonConstants.O_CASTLE) + " " + uc.getID() + " ;" + uc.getName(), "", Completion.DEFAULT_PRIORITY));
      }
    }
    
    // Allanon is a little bit problematic with "Karawane" because it is marked as SHIP but of type "Karawane",
    // so we have to take care about the ship type

    Collection<Ship> iter2 = getRegion().ships();
    List<Ship> caravans = new ArrayList<Ship>();
    List<Ship> ships = new ArrayList<Ship>();
    for (Ship ship : iter2) {
      if (ship.getType().getID().equals(AllanonConstants.ST_KARAWANE)) {
        caravans.add(ship);
      } else {
        ships.add(ship);
      }
    }
    
    if (caravans.size() > 0) {
      getCompletions().add(new Completion(Resources.getOrderTranslation(AllanonConstants.O_KARAWANE), " ", 7));

      for(UnitContainer uc : caravans) {
        if(!uc.equals(getUnit().getShip())) {
          getCompletions().add(new Completion(uc.getName() + " (" + uc.getID() + ")", Resources.getOrderTranslation(AllanonConstants.O_KARAWANE) + " " + uc.getID() + " ;" + uc.getName(), "", Completion.DEFAULT_PRIORITY-1));
          getCompletions().add(new Completion(uc.getID() + " (" + uc.getName() + ")", Resources.getOrderTranslation(AllanonConstants.O_KARAWANE) + " " + uc.getID() + " ;" + uc.getName(), "", Completion.DEFAULT_PRIORITY));
        }
      }
    }
    
    
    if (ships.size() > 0) {
      getCompletions().add(new Completion(Resources.getOrderTranslation(AllanonConstants.O_SHIP), " ", 7));

      for(UnitContainer uc : ships) {
        if(!uc.equals(getUnit().getShip())) {
          getCompletions().add(new Completion(uc.getName() + " (" + uc.getID() + ")", Resources.getOrderTranslation(AllanonConstants.O_SHIP) + " " + uc.getID() + " ;" + uc.getName(), "", Completion.DEFAULT_PRIORITY-1));
          getCompletions().add(new Completion(uc.getID() + " (" + uc.getName() + ")", Resources.getOrderTranslation(AllanonConstants.O_SHIP) + " " + uc.getID() + " ;" + uc.getName(), "", Completion.DEFAULT_PRIORITY));
        }
      }
    }
  }
  
  /**
   * @see magellan.library.gamebinding.EresseaOrderCompleter#cmpltBenenne()
   */
  @Override
  protected void cmpltBenenne() {
    super.cmpltBenenne();

    if((getUnit().getShip() != null) && (getUnit().getShip().getType().getID().equals(AllanonConstants.ST_KARAWANE)) && (getUnit().getShip().getOwnerUnit() != null) && getUnit().getShip().getOwnerUnit().equals(getUnit())) {
      getCompletions().add(new Completion(Resources.getOrderTranslation(AllanonConstants.O_KARAWANE),Resources.getOrderTranslation(AllanonConstants.O_KARAWANE)," \"\"", Completion.DEFAULT_PRIORITY, 1));
    }
  }
  
  /**
   * @see magellan.library.gamebinding.EresseaOrderCompleter#cmpltBeanspruche()
   */
  @Override
  protected void cmpltBeanspruche(){
    getCompletions().add(new Completion(Resources.get("gamebinding.eressea.eresseaordercompleter.amount"), "1", " "));
    getCompletions().add(new Completion(getData().rules.getItemType(EresseaConstants.I_SILVER).getOrderName()));
  }


}
