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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
import magellan.library.rules.ShipType;
import magellan.library.rules.SkillType;
import magellan.library.utils.filters.UnitFilter;
import magellan.library.utils.logging.Logger;

public class Units {
  /** a specialized container that behaves much like item */
  public static class StatItem implements Comparable<StatItem> {
    /** units having this item */
    public List<UnitWrapper> units = new ArrayList<UnitWrapper>(5);

    private ItemType type;
    private long amount;
    private long unmodifiedAmount = 0;

    /**
     * Creates a new StatItem object.
     */
    public StatItem(ItemType type, long amount) {
      this.type = type;
      this.amount = amount;
    }

    /**
     * @param amount
     */
    public void setAmount(long amount) {
      this.amount = amount;
    }

    /**
     */
    public long getAmount() {
      return amount;
    }

    /**
     */
    public ItemType getItemType() {
      return type;
    }

    /**
     * compare by type name
     */
    public int compareTo(StatItem o) {
      return type.getName().compareTo(o.type.getName());
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof StatItem)
        return compareTo((StatItem) obj) != 0;
      else
        return false;
    }

    @Override
    public int hashCode() {
      return type.getName().hashCode();
    }

    /**
     * Returns the value of unmodifiedAmount.
     *
     * @return Returns unmodifiedAmount.
     */
    public long getUnmodifiedAmount() {
      return unmodifiedAmount;
    }

    /**
     * Sets the value of unmodifiedAmount.
     *
     * @param unmodifiedAmount The value for unmodifiedAmount.
     */
    public void setUnmodifiedAmount(long unmodifiedAmount) {
      this.unmodifiedAmount = unmodifiedAmount;
    }

    /**
     * Returns the type name.
     */
    public String getName() {
      return type.getName();
    }
  }

  /**
   * Contains extended info about a unit.
   */
  public static class UnitWrapper {
    private Unit unit = null;
    private long number = -1;
    private long unmodifiedNumber = -1;

    /**
     * Creates a new UnitWrapper object.
     */
    public UnitWrapper(Unit u) {
      this(u, -1);
    }

    /**
     * Creates a new UnitWrapper object.
     */
    public UnitWrapper(Unit u, long num) {
      unit = u;
      number = num;
    }

    /**
     */
    public Unit getUnit() {
      return unit;
    }

    /**
     */
    public long getAmount() {
      return number;
    }

    /**
     */
    @Override
    public String toString() {
      if (number > -1) {
        if (unmodifiedNumber > -1 && unmodifiedNumber != number)
          return unit.toString() + ": " + unmodifiedNumber + " (" + number + ")";
        else
          return unit.toString() + ": " + number;
      }

      return unit.toString();
    }

    /**
     * Returns the value of unmodifiedNumber.
     *
     * @return Returns unmodifiedNumber.
     */
    public long getUnmodifiedAmount() {
      return unmodifiedNumber;
    }

    /**
     * Sets the value of unmodifiedNumber.
     *
     * @param unmodifiedNumber The value for unmodifiedNumber.
     */
    public void setUnmodifiedAmount(long unmodifiedNumber) {
      this.unmodifiedNumber = unmodifiedNumber;
    }
  }

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
      Units.log.info("Units cache recalculated "
          + Units.recount
          + " times for "
          + (Units.data != null && Units.data.getRegions() != null ? Units.data.getRegions().size()
              : "?") + " regions.");
      Units.recount = 0;
      Units.data = e.getGameData();
      Units.containerPrivItems.clear();
      Units.containerAllItems.clear();
    }
  };

  /**
   * Returns a {@link GameDataListener} that clears cached data when new data is loaded.
   */
  public static GameDataListener getGameDataListener() {
    return Units.gameDataListener;
  }

  /**
   * Returns <code>true</code> iff f is a privileged faction.
   *
   * @see Faction#isPrivileged()
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

  /**
   * Returns true if the help status for aState of the faction includes ally.
   *
   * @param faction The source faction
   * @param ally The potential ally
   * @param aState The help state, e.g. {@link EresseaConstants#A_GUARD}.
   * @return true if the faction's alliance value for the ally includes the state.
   */
  public static boolean isAllied(Faction faction, Faction ally, int aState) {
    return isAllied(faction, ally, aState, false);
  }

  /**
   * Returns true if the help status for aState of the faction includes ally.
   *
   * @param faction The source faction
   * @param ally The potential ally
   * @param aState The help state, e.g. {@link EresseaConstants#A_GUARD}.
   * @param any if <code>true</code>, this method returns true if the state matches exactly.
   *          Otherwise only one of the states that <code>aState</code> is composed of must match.
   * @return true if the faction's alliance value for the ally includes the state.
   */
  public static boolean isAllied(Faction faction, Faction ally, int aState, boolean any) {
    boolean result = false;
    if (!any)
      return faction.getData().getGameSpecificRules().isAllied(faction, ally, aState);
    else {
      for (int i = 1; aState != 0; aState = aState >> 1) {
        if ((aState & 1) != 0) {
          result |= faction.getData().getGameSpecificRules().isAllied(faction, ally, i);
        }
        i = i << 1;
      }
    }
    return result;
  }

  /**
   * Returns the sailing skill value of the ship's owner
   */
  public static int getCaptainSkillLevel(Ship s) {
    // FIXME shouldn't access getData() from here, maybe move to GameSpecific
    SkillType sailingSkillType = s.getData().getRules().getSkillType(EresseaConstants.S_SEGELN);
    Unit owner = s.getModifiedOwnerUnit();
    int captainSkillLevel = 0;
    if (owner != null) {
      Skill sailingSkill = owner.getModifiedSkill(sailingSkillType);
      captainSkillLevel = (sailingSkill == null) ? 0 : sailingSkill.getLevel();
    }
    return captainSkillLevel;
  }

  /**
   * Returns the number of persons of the ship's owner
   */
  public static int getCaptainPersons(Ship s) {
    Unit owner = s.getOwnerUnit();
    int captainPersons = 0;
    if (owner != null) {
      captainPersons = owner.getPersons();
    }
    return captainPersons;
  }

  /**
   * Returns the number of persons of the ship's owner
   */
  public static int getModifiedCaptainPersons(Ship s) {
    Unit owner = s.getModifiedOwnerUnit();
    int captainPersons = 0;
    if (owner != null) {
      captainPersons = owner.getModifiedPersons();
    }
    return captainPersons;
  }

  /**
   * Returns the sailing skill sum of the ship's crew.
   */
  public static int getSailingSkillAmount(Ship s) {
    SkillType sailingSkillType = s.getData().getRules().getSkillType(EresseaConstants.S_SEGELN);
    // Fiete 20190824: neue Galeone hat einen mindestLevel...auf diesen prüfen
    ShipType sT = s.getShipType();
    int minSailorLevel = sT.getMinSailorLevel(); // not set = -1
    int sailingSkillAmount = 0;
    // pavkovic 2003.10.03: use modifiedUnits to reflect FUTURE value?
    Collection<Unit> modUnits = s.modifiedUnits(); // the collection of units on the ship in the
    // next turn

    for (Unit u : modUnits) {
      Skill sailingSkill = u.getModifiedSkill(sailingSkillType);

      if (sailingSkill != null) {
        if (sailingSkill.getLevel() > minSailorLevel) {
          sailingSkillAmount += (sailingSkill.getLevel() * u.getModifiedPersons());
        }
      }
    }
    return sailingSkillAmount;
  }

  private static Map<UnitContainer, Reference<Map<ID, Units.StatItem>>> containerPrivItems =
      new HashMap<UnitContainer, Reference<Map<ID, Units.StatItem>>>();

  private static Map<UnitContainer, Reference<Map<ID, Units.StatItem>>> containerAllItems =
      new HashMap<UnitContainer, Reference<Map<ID, Units.StatItem>>>();

  protected static UnitFilter privFilter = new UnitFilter() {
    @Override
    public boolean acceptUnit(Unit u) {
      return u.getFaction() != null && u.getFaction().hasGiveAlliance()
          || u.getFaction().isPrivileged();
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
  public static Collection<Units.StatItem> getContainerPrivilegedUnitItems(
      UnitContainer container) {
    return Units.getContainerUnitItems(container, Units.containerPrivItems, Units.privFilter);
  }

  /**
   * Returns the items of all units that are stationed in this region The amount of the items of a
   * particular item type are added up, so two units with 5 pieces of silver yield one silver item
   * of amount 10 here.
   */
  public static Collection<Units.StatItem> getContainerAllUnitItems(UnitContainer container) {
    return Units.getContainerUnitItems(container, Units.containerAllItems, Units.allFilter);
  }

  /**
   * Returns the items of all units that are stationed in this region and belonging to a faction
   * that has at least a privileged trust level. The amount of the items of a particular item type
   * are added up, so two units with 5 pieces of silver yield one silver item of amount 10 here.
   */
  protected static Collection<Units.StatItem> getContainerUnitItems(UnitContainer container,
      Map<UnitContainer, Reference<Map<ID, Units.StatItem>>> map, UnitFilter filter) {
    Map<ID, Units.StatItem> result = null;
    if (map.containsKey(container)) {
      result = map.get(container).get();
      if (result == null) {
        // debugging
        Units.recount++;
      }
    }

    if (result == null) {
      result = Units.calculateItems(container, map, filter);
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
  private static Map<ID, Units.StatItem> calculateItems(UnitContainer container,
      Map<UnitContainer, Reference<Map<ID, Units.StatItem>>> map, UnitFilter filter) {
    Map<ID, Units.StatItem> result = new HashMap<ID, Units.StatItem>();

    for (Unit u : container.units()) {
      // if(u.getFaction().isPrivileged()) {
      if (filter.acceptUnit(u)) {
        for (Item item : u.getItems()) {
          Units.StatItem i = result.get(item.getItemType().getID());

          if (i == null) {
            i = new Units.StatItem(item.getItemType(), 0);
            result.put(item.getItemType().getID(), i);
          }

          i.setAmount(i.getAmount() + item.getAmount());
        }
      }
    }
    map.put(container, new SoftReference<Map<ID, Units.StatItem>>(result));
    return result;
  }

  /**
   * Returns a specific item from the {@link #getContainerPrivilegedUnitItems(UnitContainer)}
   * collection identified by the item type or <code>null</code> if no such item exists in the
   * region.
   */
  public static Units.StatItem
      getContainerPrivilegedUnitItem(UnitContainer container, ItemType type) {
    return Units.getContainerUnitItem(container, type, Units.containerPrivItems, Units.privFilter);
  }

  /**
   * Returns a specific item from the {@link #getContainerAllUnitItems(UnitContainer)} collection
   * identified by the item type or <code>null</code> if no such item exists in the region.
   */
  public static Units.StatItem getContainerAllUnitItem(UnitContainer container, ItemType type) {
    return Units.getContainerUnitItem(container, type, Units.containerAllItems, Units.allFilter);
  }

  protected static Units.StatItem getContainerUnitItem(UnitContainer container, ItemType type,
      Map<UnitContainer, Reference<Map<ID, Units.StatItem>>> map, UnitFilter filter) {
    if (!map.containsKey(container)) {
      Units.calculateItems(container, map, filter);
    }

    Map<ID, Units.StatItem> resultMap = map.get(container).get();
    if (resultMap == null) {
      resultMap = Units.calculateItems(container, map, filter);
      Units.recount++;
    }

    return resultMap.get(type.getID());
  }

  /**
   * Returns an item corresponding to unit's faction's total amount of this item in unit's region.
   * TODO is it worth moving caching it for each faction and region?
   */
  public static Units.StatItem getContainerFactionUnitItem(UnitContainer container, Unit unit,
      ItemType type) {
    Units.StatItem result = new Units.StatItem(type, 0);
    long amount = 0;
    for (Unit u : container.units()) {
      if (u.getFaction() == unit.getFaction()) {
        Item uItem = u.getItem(type);
        if (uItem != null) {
          amount += uItem.getAmount();
        }
      }
    }
    result.setAmount(amount);
    return result;
  }

}
