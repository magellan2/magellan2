// class magellan.library.utils.Units
// created on Nov 23, 2008
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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
package magellan.library.utils;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.Item;
import magellan.library.Ship;
import magellan.library.Skill;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.ItemType;
import magellan.library.rules.SkillType;
import magellan.library.utils.filters.UnitFilter;
import magellan.library.utils.logging.Logger;

public class Units {
  private static final Logger log = Logger.getInstance(Units.class);

  private static long recount = 0;
  private static GameData data;
  
  private static GameDataListener gameDataListener = new GameDataListener() {

    /**
     * Clears the container items map when new game data has been loaded.
     * 
     * @see magellan.library.event.GameDataListener#gameDataChanged(magellan.library.event.GameDataEvent)
     */
    public void gameDataChanged(GameDataEvent e) {
      log.info("Units cache recalculated "+recount+" times for "+(data!=null&&data.regions()!=null?data.regions().size():"?")+" regions.");
      recount = 0;
      data = e.getGameData();
      containerPrivItems.clear();
      containerAllItems.clear();
    }
  };

  /**
   * Returns a {@link GameDataListener} that clears cached data when new data is loaded.
   */
  public static GameDataListener getGameDataListener() {
    return gameDataListener;
  }

  /**
   * Returns <code>true</code> iff f is a privileged faction.
   * 
   * @see {@link Faction#isPrivileged()}
   */
  public static boolean isPrivileged(Faction f) {
    return (f != null) && (f.isPrivileged());
  }

  /**
   * Returns <code>true</code> iff u is not a spy and belongs to a privileged faction.
   */
  public static boolean isPrivilegedAndNoSpy(Unit u) {
    return (u != null) && Units.isPrivileged(u.getFaction()) && !u.isSpy();
  }

  public static int getCaptainSkillAmount(Ship s) {
    // FIXME shouldn't access getData() from here, maybe move to GameSpecific
    SkillType sailingSkillType = s.getData().rules.getSkillType(EresseaConstants.S_SEGELN, true);
    Unit owner = s.getModifiedOwnerUnit();
    int captainSkillAmount = 0;
    if (owner != null) {
      Skill sailingSkill = owner.getModifiedSkill(sailingSkillType);
      captainSkillAmount = (sailingSkill == null) ? 0 : sailingSkill.getLevel();
    }
    return captainSkillAmount;
  }

  public static int getSailingSkillAmount(Ship s) {
    SkillType sailingSkillType = s.getData().rules.getSkillType(EresseaConstants.S_SEGELN, true);
    int sailingSkillAmount = 0;
    // pavkovic 2003.10.03: use modifiedUnits to reflect FUTURE value?
    Collection<Unit> modUnits = s.modifiedUnits(); // the collection of units on the ship in the next turn

    for (Iterator<Unit> sailors = modUnits.iterator(); sailors.hasNext();) {
      Unit u = sailors.next();
      Skill sailingSkill = u.getModifiedSkill(sailingSkillType);

      if (sailingSkill != null) {
        sailingSkillAmount += (sailingSkill.getLevel() * u.getModifiedPersons());
      }
    }
    return sailingSkillAmount;
  }

  private static Map<UnitContainer, Reference<Map<ID, Item>>> containerPrivItems =
      new HashMap<UnitContainer, Reference<Map<ID, Item>>>();

  private static Map<UnitContainer, Reference<Map<ID, Item>>> containerAllItems =
      new HashMap<UnitContainer, Reference<Map<ID, Item>>>();

  protected static UnitFilter privFilter = new UnitFilter() {
    @Override
    public boolean acceptUnit(Unit u) {
      return u.getFaction().hasGiveAlliance() || u.getFaction().isPrivileged();
    }
  };
  protected static UnitFilter allFilter = new UnitFilter() {
    @Override
    public boolean acceptUnit(Unit u) {
      return true;
    }
  };

  /**
   * Returns the items of all units that are stationed in this region and belonging to a faction
   * that has at least a privileged trust level. <br>
   * Fiete 20061224: ...and the factions with "GIVE" alliances too. <br>
   * The amount of the items of a particular item type are added up, so two units with 5 pieces of
   * silver yield one silver item of amount 10 here.
   */
  public static Collection<Item> getContainerPrivilegedUnitItems(UnitContainer container) {
    return getContainerUnitItems(container, containerPrivItems, privFilter);
  }

  /**
   * Returns the items of all units that are stationed in this region The amount of the items of a
   * particular item type are added up, so two units with 5 pieces of silver yield one silver item
   * of amount 10 here.
   */
  public static Collection<Item> getContainerAllUnitItems(UnitContainer container) {
    return getContainerUnitItems(container, containerAllItems, allFilter);
  }

  /**
   * Returns the items of all units that are stationed in this region and belonging to a faction
   * that has at least a privileged trust level. The amount of the items of a particular item type
   * are added up, so two units with 5 pieces of silver yield one silver item of amount 10 here.
   */
  protected static Collection<Item> getContainerUnitItems(UnitContainer container,
      Map<UnitContainer, Reference<Map<ID, Item>>> map, UnitFilter filter) {
    Map<ID, Item> result = null;
    if (map.containsKey(container)){
      result = map.get(container).get();
      if (result == null){
        // debugging
        result = null;
        recount++;
      }
    }

    if (result == null) {
      result = calculateItems(container, map, filter);
    }

    return Collections.unmodifiableCollection(result.values());
  }

  /**
   * Returns the items of all units that are stationed in this region and belonging to a faction
   * that has at least a privileged trust level. <br>
   * Fiete 20061224: ...and the factions with "GIVE" alliances too. <br>
   * The amount of the items of a particular item type are added up, so two units with 5 pieces of
   * silver yield one silver item of amount 10 here.
   */
  private static Map<ID, Item> calculateItems(UnitContainer container,
      Map<UnitContainer, Reference<Map<ID, Item>>> map, UnitFilter filter) {
    Map<ID, Item> result = new HashMap<ID, Item>();

    for (Iterator<Unit> iter = container.units().iterator(); iter.hasNext();) {
      Unit u = iter.next();

      // if(u.getFaction().isPrivileged()) {
      if (filter.acceptUnit(u)) {
        for (Item item : u.getItems()) {
          Item i = result.get(item.getItemType().getID());

          if (i == null) {
            i = new Item(item.getItemType(), 0);
            result.put(item.getItemType().getID(), i);
          }

          i.setAmount(i.getAmount() + item.getAmount());
        }
      }
    }
    map.put(container, new SoftReference<Map<ID, Item>>(result));
    return result;
  }

  /**
   * Returns a specific item from the {@link #getContainerPrivilegedUnitItems(UnitContainer)}
   * collection identified by the item type or <code>null</code> if no such item exists in the
   * region.
   */
  public static Item getContainerPrivilegedUnitItem(UnitContainer container, ItemType type) {
    return getContainerUnitItem(container, type, containerPrivItems, privFilter);
  }

  /**
   * Returns a specific item from the {@link #getContainerAllUnitItems(UnitContainer)} collection
   * identified by the item type or <code>null</code> if no such item exists in the region.
   */
  public static Item getContainerAllUnitItem(UnitContainer container, ItemType type) {
    return getContainerUnitItem(container, type, containerAllItems, allFilter);
  }

  protected static Item getContainerUnitItem(UnitContainer container, ItemType type,
      Map<UnitContainer, Reference<Map<ID, Item>>> map, UnitFilter filter) {
    if (!map.containsKey(container))
      calculateItems(container, map, filter);

    Map<ID, Item> resultMap = map.get(container).get();
    if (resultMap == null){
      resultMap = calculateItems(container, map, filter);
      recount++;
    }

    return resultMap.get(type.getID());
  }

}
