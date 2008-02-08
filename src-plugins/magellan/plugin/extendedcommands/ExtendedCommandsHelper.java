// class magellan.plugin.extendedcommands.ExtendedCommandsHelper
// created on 02.02.2008
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
package magellan.plugin.extendedcommands;

import java.util.Collection;

import magellan.library.GameData;
import magellan.library.Item;
import magellan.library.Skill;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.UnitID;

/**
 * A Helper class to have some shortcuts
 *
 * @author Thoralf Rickert
 * @version 1.0, 11.09.2007
 */
public class ExtendedCommandsHelper {

  private GameData world = null;
  private Unit unit = null;
  private UnitContainer container = null;
  
  public ExtendedCommandsHelper(GameData world, Unit unit) {
    this.world = world;
    this.unit = unit;
  }
  
  public ExtendedCommandsHelper(GameData world, UnitContainer container) {
    this.world = world;
    this.container = container;
  }
  
  /**
   * Returns true, if the current unit is in the region with the
   * given name.
   */
  public boolean unitIsInRegion(String regionName) {
    return unit.getRegion().getName().equalsIgnoreCase(regionName);
  }
  
  /**
   * Returns the unit with the given Unit-ID in the current region.
   */
  public Unit getUnitInRegion(String unitId) {
    return unit.getRegion().getUnit(UnitID.createUnitID(unitId, world.base));
  }
  
  /**
   * Returns true, if the current unit sees another unit with
   * the given unit id.
   */
  public boolean unitSeesOtherUnit(String unitId) {
    Unit otherunit = getUnitInRegion(unitId);
    return otherunit != null;
  }
  
  /**
   * Returns the number of items of a unit with the given
   * item name. For example:
   * 
   *  int horses = getItemCount(unit,"Pferd")
   *  
   * returns the number of horses of the given unit.
   */
  public int getItemCount(Unit unit, String itemTypeName) {
    Collection<Item> items = unit.getItems();
    if (items != null) {
      for (Item item : items) {
        if (item.getItemType().getName().equalsIgnoreCase(itemTypeName)) {
          return item.getAmount();
        }
      }
    }
    return 0;
  }
  
  /**
   * This method returns the amount of silver of the given
   * unit. This is a shortcut for
   *  
   *  getItemCount(unit,"Silber")
   */
  public int getSilver(Unit unit) {
    return getItemCount(unit, "Silber");
  }
  
  /**
   * Returns the number of persons in this unit.
   */
  public int getPersons(Unit unit) {
    return unit.getPersons();
  }
  
  /**
   * Returns the skill level of a unit. For example
   *  getLevel(unit,"Unterhaltung")
   */
  public int getLevel(Unit unit, String skillName) {
    Collection<Skill> skills = unit.getSkills();
    if (skills != null) {
      for (Skill skill : skills) {
        if (skill.getSkillType().getName().equalsIgnoreCase(skillName)) {
          return skill.getLevel();
        }
      }
    }
    return 0;
  }
  
  /**
   * Adds an order to the current unit.
   */
  public void addOrder(String order) {
    unit.addOrder(order, false, 0);
  }
}
