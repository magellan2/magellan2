/*
 *  Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe,
 *                          Stefan Goetz, Sebastian Pappert,
 *                          Klaas Prause, Enno Rehling,
 *                          Sebastian Tusk, Ulrich Kuester,
 *                          Ilja Pavkovic
 *
 * This file is part of the Eressea Java Code Base, see the
 * file LICENSING for the licensing information applying to
 * this file.
 *
 */

package magellan.library.impl;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import magellan.library.Building;
import magellan.library.CombatSpell;
import magellan.library.CoordinateID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Group;
import magellan.library.HasRegion;
import magellan.library.ID;
import magellan.library.IntegerID;
import magellan.library.Item;
import magellan.library.Message;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Skill;
import magellan.library.Spell;
import magellan.library.StringID;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.UnitID;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.MovementEvaluator;
import magellan.library.relation.AttackRelation;
import magellan.library.relation.CombatStatusRelation;
import magellan.library.relation.EnterRelation;
import magellan.library.relation.GuardRegionRelation;
import magellan.library.relation.InterUnitRelation;
import magellan.library.relation.ItemTransferRelation;
import magellan.library.relation.LeaveRelation;
import magellan.library.relation.MovementRelation;
import magellan.library.relation.PersonTransferRelation;
import magellan.library.relation.RecruitmentRelation;
import magellan.library.relation.ReserveRelation;
import magellan.library.relation.TeachRelation;
import magellan.library.relation.TransportRelation;
import magellan.library.relation.UnitContainerRelation;
import magellan.library.relation.UnitRelation;
import magellan.library.rules.ItemType;
import magellan.library.rules.MessageType;
import magellan.library.rules.Race;
import magellan.library.rules.SkillType;
import magellan.library.utils.Cache;
import magellan.library.utils.CacheHandler;
import magellan.library.utils.IDBaseConverter;
import magellan.library.utils.Locales;
import magellan.library.utils.MagellanFactory;
import magellan.library.utils.OrderToken;
import magellan.library.utils.OrderTokenizer;
import magellan.library.utils.OrderWriter;
import magellan.library.utils.OrderedHashtable;
import magellan.library.utils.Regions;
import magellan.library.utils.Resources;
import magellan.library.utils.Sorted;
import magellan.library.utils.Taggable;
import magellan.library.utils.comparator.LinearUnitTempUnitComparator;
import magellan.library.utils.comparator.SortIndexComparator;
import magellan.library.utils.guiwrapper.CacheableOrderEditor;
import magellan.library.utils.logging.Logger;

/**
 * @author $Author: $
 * @version $Revision: 389 $
 */
public class MagellanUnitImpl extends MagellanRelatedImpl implements Unit, HasRegion, Sorted,
    Taggable {
  private static final Logger log = Logger.getInstance(MagellanUnitImpl.class);
  private static final String CONFIRMEDTEMPCOMMENT = ";" + OrderWriter.CONFIRMEDTEMP;
  private static final String TAG_PREFIX_TEMP = ";" + "ejcTagTemp "; // grammar for ejcTag:
  // ";ejcTempTag tag numbervalue|'stringvalue'"

  /** The private description of the unit. */
  private String privDesc = null; // private description

  /** The displayed race of the unit. */
  private Race race = null;

  /** The real race of the (daemon) unit */
  private Race realRace = null;

  /**
   * unitID of the "father"-mage or mother.mage...
   */
  private ID familiarmageID = null;

  /** The weight in silver */
  private int weight = -1;

  /** an object encapsulation the orders of this unit as <tt>String</tt> objects */
  private final Orders ordersObject = new Orders();

  /** Comments modifiable by the user. The comments are represented as String objects. */
  /** analog to comments in unitcontainer **/
  private List<String> comments = null;

  /**
   * @see magellan.library.Unit#ordersAreNull()
   */
  public boolean ordersAreNull() {
    return ordersObject.ordersAreNull();
  }

  /**
   * @see magellan.library.Unit#ordersHaveChanged()
   */
  public boolean ordersHaveChanged() {
    return ordersObject.ordersHaveChanged();
  }

  /**
   * @see magellan.library.Unit#setOrdersChanged(boolean)
   */
  public void setOrdersChanged(boolean changed) {
    ordersObject.setOrdersChanged(changed);
  }

  /**
   * Clears the orders and refreshes the relations
   */
  public void clearOrders() {
    clearOrders(true);
  }

  /**
   * Clears the orders and possibly refreshes the relations
   * 
   * @param refreshRelations if true also refresh the relations of the unit.
   */
  public void clearOrders(boolean refreshRelations) {
    ordersObject.clearOrders();

    if (refreshRelations) {
      refreshRelations();
    }
  }

  /**
   * Removes the order at position <tt>i</tt> and refreshes the relations
   */
  public void removeOrderAt(int i) {
    removeOrderAt(i, true);
  }

  /**
   * Removes the order at position <tt>i</tt> and possibly refreshes the relations
   * 
   * @param refreshRelations if true also refresh the relations of the unit.
   */
  public void removeOrderAt(int i, boolean refreshRelations) {
    ordersObject.removeOrderAt(i);

    if (refreshRelations) {
      refreshRelations(i);
    }
  }

  /**
   * Adds the order at position <tt>i</tt> and refreshes the relations
   * 
   * @param i An index between 0 and getOrders().getSize() (inclusively), or -1 to add at the end.
   * @param newOrders
   */
  public void addOrderAt(int i, String newOrders) {
    addOrderAt(i, newOrders, true);
  }

  /**
   * Adds the order at position <tt>i</tt> and possibly refreshes the relations
   * 
   * @param i An index between 0 and getOrders().getSize() (inclusively), or -1 to add at the end.
   * @param newOrders
   * @param refreshRelations if true also refresh the relations of the unit.
   */
  public void addOrderAt(int i, String newOrders, boolean refreshRelations) {
    if (i < 0) {
      ordersObject.addOrder(newOrders);
    } else {
      ordersObject.addOrderAt(i, newOrders);
    }

    if (refreshRelations) {
      refreshRelations(i);
    }
  }

  /**
   * Adds the order and refreshes the relations
   * 
   * @param newOrders
   */
  public void addOrders(String newOrders) {
    addOrders(newOrders, true);
  }

  /**
   * Adds the order and possibly refreshes the relations
   * 
   * @param newOrders
   * @param refreshRelations if true also refresh the relations of the unit.
   */
  public void addOrders(String newOrders, boolean refreshRelations) {
    addOrders(Collections.singleton(newOrders), refreshRelations);
  }

  /**
   * Adds the orders and refreshes the relations
   * 
   * @param newOrders
   */
  public void addOrders(Collection<String> newOrders) {
    addOrders(newOrders, true);
  }

  /**
   * Adds the orders and possibly refreshes the relations
   * 
   * @param newOrders
   * @param refreshRelations If true also refresh the relations of the unit
   */
  public void addOrders(Collection<String> newOrders, boolean refreshRelations) {
    final int newPos = ordersObject.addOrders(newOrders);

    if (refreshRelations) {
      refreshRelations(newPos);
    }
  }

  /**
   * Sets the orders and refreshes the relations
   * 
   * @param newOrders
   */
  public void setOrders(Collection<String> newOrders) {
    setOrders(newOrders, true);
  }

  /**
   * Sets the orders and possibly refreshes the relations
   * 
   * @param newOrders
   * @param refreshRelations if true also refresh the relations of the unit.
   */
  public void setOrders(Collection<String> newOrders, boolean refreshRelations) {
    ordersObject.setOrders(newOrders);

    if (refreshRelations) {
      refreshRelations();
    }
  }

  /**
   * Delivers a readonly collection of alle orders of this unit.
   */
  public List<String> getOrders() {
    final List<String> orders = ordersObject.getOrders();
    if (orders != null)
      return Collections.unmodifiableList(orders);
    return Collections.emptyList();
  }

  /** The number of persons of the unit */
  private int persons = 1;

  /** DOCUMENT-ME */
  private int guard = 0;

  /** DOCUMENT-ME */
  private Building siege = null; // belagert

  /** DOCUMENT-ME */
  private int stealth = -1; // getarnt

  /** the current amount of aura */
  private int aura = -1;

  /** the maximum amount of aura */
  private int auraMax = -1;

  /** DOCUMENT-ME */
  private int combatStatus = -1; // Kampfstatus

  /** DOCUMENT-ME */
  private boolean unaided = false; // if attacked, this unit will not be helped by allied units

  /** DOCUMENT-ME */
  private boolean hideFaction = false; // Parteitarnung

  /** DOCUMENT-ME */
  private Unit follows = null; // folgt-Tag

  /** DOCUMENT-ME */
  private boolean isHero = false; // hero-tag

  /** DOCUMENT-ME */
  private String health = null;

  /** DOCUMENT-ME */
  private boolean isStarving = false; // hunger-Tag

  // (stm 09-06-08) had to get rid of the soft reference again as it leads to problems with
  // updates of unit relations.
  // protected SoftReference<Cache> cacheReference = null;

  /**
   * The cache object containing cached information that may be not related enough to be
   * encapsulated as a function and is time consuming to gather.
   */
  private Cache cache;

  /**
   * Messages directly sent to this unit. The list contains instances of class <tt>Message</tt> with
   * type -1 and only the text set.
   */
  private List<Message> unitMessages = null;

  /** A map for unknown tags */
  private Map<String, String> tagMap = null;

  /**
   * A list containing <tt>String</tt> objects, specifying effects on this <tt>Unit</tt> object.
   */
  private List<String> effects = null;

  /** true indicates that the unit has orders confirmed by an user. */
  private boolean ordersConfirmed = false;

  /** DOCUMENT-ME */
  private Map<StringID, Skill> skills = null; // maps SkillType.getID() objects to Skill objects
  private boolean skillsCopied = false;

  /**
   * The items carried by this unit. The keys are the IDs of the item's type, the values are the
   * Item objects themselves.
   */
  private Map<StringID, Item> items = null;

  /**
   * The spells known to this unit. The keys are the IDs of the spells, the values are the Spell
   * objects themselves.
   */
  private Map<ID, Spell> spells = null;

  /**
   * Contains the spells this unit has set for use in a combat. This map contains data if a unit has
   * a magic skill and has actively set combat spells. The values in this map are objects of type
   * CombatSpell, the keys are their ids.
   */
  private Map<ID, CombatSpell> combatSpells = null;

  /** The group this unit belongs to. */
  private Group group = null;

  /**
   * Sets the group this unit belongs to.
   * 
   * @param g the group of the unit
   */
  public void setGroup(Group g) {
    if (group != null) {
      group.removeUnit(getID());
    }

    group = g;

    if (group != null) {
      group.addUnit(this);
    }
  }

  /**
   * Returns the group this unit belongs to.
   * 
   * @return the group this unit belongs to
   */
  public Group getGroup() {
    return group;
  }

  /** The previous id of this unit. */
  private UnitID alias = null;

  /**
   * Sets an alias id for this unit.
   * 
   * @param id the alias id for this unit
   */
  public void setAlias(UnitID id) {
    alias = id;
  }

  /**
   * Returns the alias, i.e. the id of this unit it had in the last turn (e.g. after a NUMMER
   * order).
   * 
   * @return the alias or null, if the id did not change.
   */
  public UnitID getAlias() {
    return alias;
  }

  /**
   * Returns the item of the specified type if the unit owns such an item, otherwise
   * <code>null</code>.
   */
  public Item getItem(ItemType type) {
    return (items != null) ? (Item) items.get(type.getID()) : null;
  }

  /**
   * Indicates that this unit belongs to a different faction than it pretends to. A unit cannot
   * disguise itself as a different faction and at the same time be a spy of another faction,
   * therefore, setting this attribute to true results in having the guiseFaction attribute set to
   * null.
   */
  private boolean isSpy = false;

  /**
   * Sets whether is unit really belongs to its unit or only pretends to do so. A unit cannot
   * disguise itself as a different faction and at the same time be a spy of another faction,
   * therefore, setting this attribute to true results in having the guiseFaction attribute set to
   * null.
   */
  public void setSpy(boolean bool) {
    isSpy = bool;

    if (isSpy) {
      setGuiseFaction(null);
    }
  }

  /**
   * Returns whether this unit only pretends to belong to its faction. A unit cannot disguise itself
   * as a different faction and at the same time be a spy of another faction.
   * 
   * @return true if the unit is identified as spy
   */
  public boolean isSpy() {
    return isSpy;
  }

  /**
   * If this unit is disguised and pretends to belong to a different faction this field holds that
   * faction, else it is null.
   */
  private Faction guiseFaction = null;

  /**
   * Sets the faction this unit pretends to belong to. A unit cannot disguise itself as a different
   * faction and at the same time be a spy of another faction, therefore, setting a value other than
   * null results in having the spy attribute set to false.
   */
  public void setGuiseFaction(Faction f) {
    guiseFaction = f;

    if (f != null) {
      setSpy(false);
    }
  }

  /**
   * Returns the faction this unit pretends to belong to. If the unit is not disguised null is
   * returned. A unit cannot disguise itself as a different faction and at the same time be a spy of
   * another faction.
   */
  public Faction getGuiseFaction() {
    return guiseFaction;
  }

  /**
   * Adds an item to the unit. If the unit already has an item of the same type, the item is
   * overwritten with the specified item object.
   * 
   * @return the specified item i.
   */
  public Item addItem(Item i) {
    if (items == null) {
      items = new OrderedHashtable<StringID, Item>(4);
    }

    items.put(i.getItemType().getID(), i);
    invalidateCache();

    return i;
  }

  /** The temp id this unit had before becoming a real unit. */
  private UnitID tempID = null;

  /**
   * Sets the temp id this unit had before becoming a real unit.
   */
  public void setTempID(UnitID id) {
    tempID = id;
  }

  /**
   * Returns the id the unit had when it was still a temp unit. This id is only set in the turn
   * after the unit turned from a temp unit into to a real unit.
   * 
   * @return the temp id or null, if this unit was no temp unit in the previous turn.
   */
  public UnitID getTempID() {
    return tempID;
  }

  /** The region this unit is currently in. */
  protected Region region = null;

  /**
   * Sets the region this unit is in. If this unit already has a different region set it removes
   * itself from the collection of units in that region.
   */
  public void setRegion(Region r) {
    if (r != getRegion()) {
      if (region != null) {
        region.removeUnit(getID());
      }

      if (r != null) {
        r.addUnit(this);
      }

      region = r;
    }
  }

  /**
   * Returns the region this unit is staying in.
   */
  public Region getRegion() {
    return region;
  }

  /** The faction this unit belongs to. */
  private Faction faction = null;

  /**
   * Sets the faction for this unit. If this unit already has a different faction set it removes
   * itself from the collection of units in that faction.
   */
  public void setFaction(Faction faction) {
    if (faction != getFaction()) {
      if (this.faction != null) {
        this.faction.removeUnit(getID());
      }

      if (faction != null) {
        faction.addUnit(this);
      }

      this.faction = faction;
    }
  }

  /**
   * Returns the faction this unit belongs to.
   */
  public Faction getFaction() {
    return faction;
  }

  /** The building this unit stays in. */
  private Building building = null;

  /**
   * Sets the building this unit is staying in. If the unit already is in another building this
   * method removes it from the unit collection of that building.
   */
  public void setBuilding(Building building) {
    if (this.building != null) {
      this.building.removeUnit(getID());
    }

    this.building = building;

    if (this.building != null) {
      this.building.addUnit(this);
    }
  }

  /**
   * Returns the building this unit is staying in.
   */
  public Building getBuilding() {
    return building;
  }

  /** The ship this unit is on. */
  private Ship ship = null;

  /**
   * Sets the ship this unit is on. If the unit already is on another ship this method removes it
   * from the unit collection of that ship.
   */
  public void setShip(Ship ship) {
    if (this.ship != null) {
      this.ship.removeUnit(getID());
    }

    this.ship = ship;

    if (this.ship != null) {
      this.ship.addUnit(this);
    }
  }

  /**
   * Returns the ship this unit is on.
   */
  public Ship getShip() {
    return ship;
  }

  // units are sorted in unit containers with this index
  private int sortIndex = -1;

  /**
   * Sets an index indicating how instances of class are sorted in the report.
   */
  public void setSortIndex(int index) {
    sortIndex = index;
  }

  /**
   * Returns an index indicating how instances of class are sorted in the report.
   */
  public int getSortIndex() {
    return sortIndex;
  }

  /** A unit dependent prefix to be prepended to this faction's race name. */
  private String raceNamePrefix = null;

  /**
   * Sets the unit dependent prefix for the race name.
   */
  public void setRaceNamePrefix(String prefix) {
    raceNamePrefix = prefix;
  }

  /**
   * Returns the unit dependent prefix for the race name.
   */
  public String getRaceNamePrefix() {
    return raceNamePrefix;
  }

  /**
   * Returns the name of this unit's race including the prefixes of itself, its faction and group if
   * it has such and those prefixes are set.
   * 
   * @param data The GameData
   * @return the name or null if this unit's race or its name is not set.
   */
  public String getRaceName(GameData data) {
    Race tempRace = getDisguiseRace();
    if (tempRace == null) {
      tempRace = getRace();
    }
    if (tempRace != null) {
      if (getRaceNamePrefix() != null)
        return data.getTranslation(getRaceNamePrefix()) + tempRace.getName().toLowerCase();
      else {
        if ((group != null) && (group.getRaceNamePrefix() != null))
          return data.getTranslation(group.getRaceNamePrefix()) + tempRace.getName().toLowerCase();
        else {
          if ((faction != null) && (faction.getRaceNamePrefix() != null))
            return data.getTranslation(faction.getRaceNamePrefix())
                + tempRace.getName().toLowerCase();
          else
            return tempRace.getName();
        }
      }
    }

    return null;
  }

  /**
   * @return The String of the RealRace. If no RealRace is known( = null) the normal raceName is
   *         returned.
   */
  public String getSimpleRealRaceName() {
    if (realRace == null)
      return getSimpleRaceName();
    else
      return realRace.getName();
  }

  /**
   * Delivers the info "typ" from CR without any prefixes and translations used for displaying the
   * according race icon
   * 
   * @return Name of the race
   */
  public String getSimpleRaceName() {
    if (race == null)
      return Resources.get("unit.race.personen.name");
    return race.getName();
  }

  /** A map containing all temp units created by this unit. */
  private Map<ID, TempUnit> tempUnits = null;

  /** A collection view of the temp units. */
  private Collection<TempUnit> tempUnitCollection = null;

  /**
   * Returns the child temp units created by this unit's orders.
   */
  public Collection<TempUnit> tempUnits() {
    if (tempUnitCollection == null) {
      if (tempUnits != null && tempUnits.values() != null) {
        tempUnitCollection = Collections.unmodifiableCollection(tempUnits.values());
      } else {
        tempUnitCollection = new ArrayList<TempUnit>();
      }
    }

    return tempUnitCollection;
  }

  /**
   * Return the child temp unit with the specified ID.
   */
  public Unit getTempUnit(ID key) {
    if (tempUnits != null)
      return tempUnits.get(key);

    return null;
  }

  /**
   * Adds a temp unit to this unit.
   */
  private TempUnit addTemp(TempUnit u) {
    if (tempUnits == null) {
      tempUnits = new LinkedHashMap<ID, TempUnit>();

      // enforce the creation of a new collection view
      tempUnitCollection = null;
    }

    tempUnits.put(u.getID(), u);

    return u;
  }

  /**
   * Removes a temp unit from the list of child temp units created by this unit's orders.
   */
  private MagellanUnitImpl removeTemp(ID key) {
    MagellanUnitImpl ret = null;

    if (tempUnits != null) {
      ret = (MagellanUnitImpl) tempUnits.remove(key);

      if (tempUnits.isEmpty()) {
        tempUnits = null;
      }
    }

    return ret;
  }

  /**
   * Clears the list of temp units created by this unit. Clears only the caching collection, does
   * not perform clean-up like deleteTemp() does.
   */
  public void clearTemps() {
    if (tempUnits != null) {
      tempUnits.clear();
      tempUnits = null;
    }
  }

  /**
   * Returns all orders including the orders necessary to issue the creation of all the child temp
   * units of this unit.
   */
  public List<String> getCompleteOrders() {
    return getCompleteOrders(false);
  }

  /**
   * Returns all orders including the orders necessary to issue the creation of all the child temp
   * units of this unit.
   * 
   * @param writeUnitTagsAsVorlageComment If this is <code>true</code>, unit tags are also added as
   *          Vorlage comments
   */
  public List<String> getCompleteOrders(boolean writeUnitTagsAsVorlageComment) {
    final List<String> cmds = new LinkedList<String>();
    if (!ordersAreNull()) {
      cmds.addAll(ordersObject.getOrders());
    }

    if (writeUnitTagsAsVorlageComment && hasTags()) {
      for (String tag : getTagMap().keySet()) {
        cmds.add("// #after 1 { #tag EINHEIT " + tag.replace(' ', '~') + " '" + getTag(tag) + "' }");
      }
    }

    cmds.addAll(getTempOrders(writeUnitTagsAsVorlageComment));

    return cmds;
  }

  /**
   * Returns the orders necessary to issue the creation of all the child temp units of this unit.
   */
  protected List<String> getTempOrders(boolean writeUnitTagsAsVorlageComment) {
    final List<String> cmds = new LinkedList<String>();

    for (TempUnit u : tempUnits()) {
      cmds.add(Resources.getOrderTranslation(EresseaConstants.O_MAKE) + " "
          + Resources.getOrderTranslation(EresseaConstants.O_TEMP) + " " + u.getID().toString());
      cmds.addAll(u.getCompleteOrders(writeUnitTagsAsVorlageComment));

      if (u.isOrdersConfirmed()) {
        cmds.add(MagellanUnitImpl.CONFIRMEDTEMPCOMMENT);
      }

      if (u.hasTags()) {
        final Map<String, String> tempUnitTags = u.getTagMap();
        for (String tag : u.getTagMap().keySet()) {
          final String value = tempUnitTags.get(tag);
          cmds.add(MagellanUnitImpl.TAG_PREFIX_TEMP + tag + " " + value.replace(' ', '~'));
        }
      }

      cmds.add(Resources.getOrderTranslation(EresseaConstants.O_END));
    }

    return cmds;
  }

  /**
   * Creates a new temp unit with this unit as the parent. The temp unit is fully initialised, i.e.
   * it is added to the region units collection in the specified game data,it inherits the faction,
   * building or ship, region, faction stealth status, group, race and combat status settings and
   * adds itself to the corresponding unit collections.
   * 
   * @throws IllegalArgumentException If <code>key</code> is negative
   */
  public TempUnit createTemp(GameData data, UnitID key) {
    if ((key).intValue() >= 0)
      throw new IllegalArgumentException(
          "Unit.createTemp(): cannot create temp unit with non-negative ID.");

    final TempUnit t = MagellanFactory.createTempUnit(key, this);
    addTemp(t);
    t.setPersons(0);
    t.setHideFaction(hideFaction);
    t.setCombatStatus(combatStatus);
    t.setOrdersConfirmed(false);

    if (race != null) {
      t.setRace(race);
    }

    if (realRace != null) {
      t.setRealRace(realRace);
    }

    if (getRegion() != null) {
      t.setRegion(getRegion());
    }

    if (getShip() != null) {
      t.setShip(getShip());
    } else if (getBuilding() != null) {
      t.setBuilding(getBuilding());
    }

    if (getFaction() != null) {
      t.setFaction(getFaction());
    }

    if (group != null) {
      t.setGroup(group);
    }

    // add to tempunits in gamedata
    if ((getRegion() != null) && (getRegion().getData() != null)) {
      data.addTempUnit(t);
    } else {
      MagellanUnitImpl.log
          .warn("Unit.createTemp(): Warning: Couldn't add temp unit to game data. Couldn't access game data");
    }

    return t;
  }

  /**
   * Removes a temp unit with this unit as the parent completely from the game data.
   */
  public void deleteTemp(UnitID key, GameData data) {
    final TempUnit t = (TempUnit) removeTemp(key);

    if (t != null) {
      t.clearOrders();
      t.refreshRelations();

      t.setPersons(0);
      t.setRace(null);
      t.setRealRace(null);
      t.setRegion(null);
      t.setShip(null);
      t.setBuilding(null);
      t.setFaction(null);
      t.setGroup(null);

      t.clearCache();

      t.setParent(null);
      data.removeTemp(key);
      // data.tempUnits().remove(key);

      // enforce refreshing of unit relations in the whole region
      if (getRegion() != null) {
        getRegion().refreshUnitRelations(true);
      }

    }
  }

  /**
   * Resets the cache of this unit to its uninitalized state.
   */
  private void invalidateCache() {
    if (hasCache()) {
      final Cache cache1 = getCache();
      cache1.modifiedName = null;
      cache1.modifiedSkills = null;
      cache1.modifiedItems = null;
      cache1.unitWeight = -1;
      cache1.modifiedUnitWeight = -1;
      cache1.modifiedPersons = -1;
      cache1.modifiedCombatStatus = -2;
      cache1.modifiedUnaidedValidated = false;
      cache1.modifiedGuard = -1;
    }
  }

  /**
   * @see magellan.library.Named#getModifiedName()
   */
  @Override
  public String getModifiedName() {
    final Cache cache1 = getCache();
    if (cache1.modifiedName == null) {
      cache1.modifiedName = super.getModifiedName();
    }
    return cache1.modifiedName != null ? cache1.modifiedName : getName();
  }

  /**
   * Returns a Collection over the relations this unit has to other units. The iterator returns
   * <tt>UnitRelation</tt> objects. An empty iterator is returned if the relations have not been set
   * up so far or if there are no relations. To have the relations to other units properly set up
   * the refreshRelations() method has to be invoked.
   */
  @Override
  protected Collection<UnitRelation> getRelations() {
    final Cache cache1 = getCache();
    if (cache1.relations == null) {
      cache1.relations = new ArrayList<UnitRelation>();
    }
    return cache1.relations;
  }

  /**
   * @see magellan.library.impl.MagellanRelatedImpl#addRelation(magellan.library.relation.UnitRelation)
   */
  @Override
  public void addRelation(UnitRelation rel) {
    super.addRelation(rel);
    invalidateCache();
  }

  /**
   * @see magellan.library.impl.MagellanRelatedImpl#removeRelation(magellan.library.relation.UnitRelation)
   */
  @Override
  public UnitRelation removeRelation(UnitRelation rel) {
    final UnitRelation ret = super.removeRelation(rel);
    if (ret != null) {
      invalidateCache();
    }
    return ret;
  }

  /**
   * deliver all directly related units
   */
  public void getRelatedUnits(Collection<Unit> units) {
    units.add(this);

    for (InterUnitRelation iur : this.getRelations(InterUnitRelation.class)) {
      units.add(iur.source);

      if (iur.target != null) {
        units.add(iur.target);
      }
    }
  }

  /**
   * Recursively retrieves all units that are related to this unit via one of the specified
   * relations (excatly, not as subclasses).
   * 
   * @param units all units gathered so far to prevent loops.
   * @param relations a set of classes naming the types of relations that are eligible for regarding
   *          a unit as related to some other unit.
   */
  public void getRelatedUnits(Set<Unit> units, Set<UnitRelation.ID> relations) {
    units.add(this);

    for (UnitRelation rel : this.getRelations()) {
      if (relations.contains(UnitRelation.getClassID(rel.getClass()))) {
        final Unit src = rel.source;
        Unit target = null;

        if (rel instanceof InterUnitRelation) {
          target = ((InterUnitRelation) rel).target;
        }

        if (units.add(src)) {
          src.getRelatedUnits(units, relations);
        }

        if (units.add(target) && target != null) {
          target.getRelatedUnits(units, relations);
        }
      }
    }
  }

  /**
   * Returns a List of the reached coordinates of the unit's movement starting with the current
   * region or an empty list if unit is not moving.
   * 
   * @return A list of coordinates, empty list means no movement
   */
  public List<CoordinateID> getModifiedMovement() {
    if (ordersAreNull())
      return Collections.emptyList();

    final List<MovementRelation> movementRelations = getRelations(MovementRelation.class);

    if (movementRelations.isEmpty())
      return Collections.emptyList();

    return Collections.unmodifiableList((movementRelations.iterator().next()).movement);
  }

  /**
   * @see magellan.library.Unit#getModifiedShip()
   */
  public Ship getModifiedShip() {
    final UnitContainer uc = getModifiedUnitContainer();
    return (uc instanceof Ship) ? (Ship) uc : null;
  }

  /**
   * @see magellan.library.Unit#getModifiedBuilding()
   */
  public Building getModifiedBuilding() {
    final UnitContainer uc = getModifiedUnitContainer();
    return (uc instanceof Building) ? (Building) uc : null;
  }

  /**
   * @see magellan.library.Unit#getModifiedSkill(magellan.library.rules.SkillType)
   */
  public Skill getModifiedSkill(SkillType type) {
    Skill s = null;

    if (!hasCache() || (getCache().modifiedSkills == null)) {
      // the cache is invalid, refresh
      refreshModifiedSkills();
    }

    if (hasCache() && (getCache().modifiedSkills != null)) {
      s = getCache().modifiedSkills.get(type.getID());
    }

    return s;
  }

  /**
   * Returns the skills of this unit as they would appear after the orders for person transfers are
   * processed.
   */
  public Collection<Skill> getModifiedSkills() {
    final Cache cache1 = getCache();
    if (!hasCache() || (cache1.modifiedSkills == null)) {
      refreshModifiedSkills();
    }

    if (hasCache() && (cache1.modifiedSkills != null)) {
      if (cache1.modifiedSkills.values() != null)
        return Collections.unmodifiableCollection(cache1.modifiedSkills.values());
      else
        return Collections.emptyList();
    }

    return Collections.emptyList();
  }

  /**
   * Updates the cache with the skills of this unit as they would appear after the orders for person
   * transfers are processed. If the cache object or the modified skills field is still null after
   * invoking this function, the skill modifications cannot be determined accurately.
   */
  private synchronized void refreshModifiedSkills() {
    final Cache cache1 = getCache();

    // clear existing modified skills
    // there is special case: to reduce memory consumption
    // cache.modifiedSkills can point to the real skills and
    // you don't want to clear THAT
    // that also means that this should be the only place where
    // cache.modifiedSkills is modified
    if ((cache1.modifiedSkills != null) && (cache1.modifiedSkills != skills)) {
      cache1.modifiedSkills.clear();
    }

    // if there are no relations, cache.modfiedSkills can point
    // directly to the skills and we can bail out
    if (getRelations().isEmpty()) {
      cache1.modifiedSkills = skills;

      return;
    }

    // get all related units (as set) and sort it in a list afterwards
    final Set<Unit> relatedUnits = new HashSet<Unit>();
    final Set<UnitRelation.ID> relationTypes = new HashSet<UnitRelation.ID>();
    relationTypes.add(UnitRelation.getClassID(PersonTransferRelation.class));
    relationTypes.add(UnitRelation.getClassID(RecruitmentRelation.class));
    this.getRelatedUnits(relatedUnits, relationTypes);

    /* sort related units according to report order */
    final List<Unit> sortedUnits = new LinkedList<Unit>(relatedUnits);
    Collections.sort(sortedUnits, new LinearUnitTempUnitComparator(new SortIndexComparator<Unit>(
        null)));

    /* clone units with all aspects relevant for skills */
    final Map<ID, MagellanUnitImpl> clones = new Hashtable<ID, MagellanUnitImpl>();

    for (Unit unit : relatedUnits) {
      final MagellanUnitImpl u = (MagellanUnitImpl) unit;
      MagellanUnitImpl clone = null;

      try {
        clone = new MagellanUnitImpl(u.getID().clone());
        clone.persons = u.getPersons();
        clone.race = u.race;
        clone.realRace = u.realRace;
        clone.region = u.region;
        clone.isStarving = u.isStarving;
        clone.isHero = u.isHero;

        for (Skill s : u.getSkills()) {
          clone.addSkill(new Skill(s.getSkillType(), s.getPoints(), s.getLevel(), clone.persons, s
              .noSkillPoints()));
        }
        clones.put(clone.getID(), clone);
      } catch (final CloneNotSupportedException e) {
        // won't fail
      }

    }

    // now modify the skills according to changes introduced by the relations

    /*
     * indicates that a skill is lost through person transfers or recruiting. May not be
     * Integer.MIN_VALUE to avoid wrap- around effects but should also be fairly negative so no
     * modifier can push it up to positive values.
     */
    final int lostSkillLevel = (Integer.MIN_VALUE / 2);

    for (Unit unit : sortedUnits) {
      final MagellanUnitImpl srcUnit = (MagellanUnitImpl) unit;

      for (UnitRelation unitRel : srcUnit.getRelations()) {
        if (!(unitRel.source.equals(srcUnit)) || !(unitRel instanceof PersonTransferRelation)) {
          continue;
        }

        final PersonTransferRelation rel = (PersonTransferRelation) unitRel;
        final Unit srcClone = clones.get(srcUnit.getID());
        final Unit targetUnit = rel.target;
        final Unit targetClone = clones.get(targetUnit.getID());
        final int transferredPersons = Math.max(0, Math.min(srcClone.getPersons(), rel.amount));

        if (transferredPersons == 0) {
          continue;
        }

        /* modify the target clone */
        /*
         * first modify all skills that are available in the target clone
         */
        for (Skill targetSkill : targetClone.getSkills()) {
          Skill srcSkill = srcClone.getSkill(targetSkill.getSkillType());
          final int skillModifier = targetSkill.getModifier(targetClone);

          if (srcSkill == null) {
            /*
             * skill exists only in the target clone, this is equivalent to a target skill at 0.
             * Level is set to lostSkillLevel to avoid confusion about level modifiers in case of
             * noSkillPoints. If skill points are relevant this value is ignored anyway.
             */
            srcSkill =
                new Skill(targetSkill.getSkillType(), 0, lostSkillLevel, srcClone.getPersons(),
                    targetSkill.noSkillPoints());
          }

          if (targetSkill.noSkillPoints()) {
            /*
             * Math.max(0, ...) guarantees that the true skill level cannot drop below 0. This also
             * important to handle the Integer.MIN_VALUE case below
             */
            final int transferredSkillFactor =
                Math.max(0, srcSkill.getLevel() - skillModifier) * transferredPersons;
            final int targetSkillFactor =
                Math.max(0, targetSkill.getLevel() - skillModifier) * targetClone.getPersons();
            final int newSkillLevel =
                (int) (((float) (transferredSkillFactor + targetSkillFactor)) / (float) (transferredPersons + targetClone
                    .getPersons()));

            /*
             * newSkillLevel == 0 means that that the skill is lost by this transfer but we may not
             * set the skill level to 0 + skillModifier since this would indicate an existing skill
             * depending on the modifier. Thus lostSkillLevel is used to distinctly mark the
             * staleness of this skill.
             */
            targetSkill.setLevel((newSkillLevel > 0) ? (newSkillLevel + skillModifier)
                : lostSkillLevel);
          } else {
            targetSkill.setPoints(targetSkill.getPoints()
                + (int) ((srcSkill.getPoints() * transferredPersons) / (float) srcClone
                    .getPersons()));
          }
        }

        /*
         * now modify the skills that only exist in the source clone
         */
        for (Skill srcSkill : srcClone.getSkills()) {
          Skill targetSkill = targetClone.getSkill(srcSkill.getSkillType());

          if (targetSkill == null) {
            /*
             * skill exists only in the source clone, this is equivalent to a source skill at 0.
             * Level is set to lostSkillLevel to avoid confusion about level modifiers in case of
             * noSkillPoints. If skill points are relevant this value is ignored anyway.
             */
            targetSkill =
                new Skill(srcSkill.getSkillType(), 0, lostSkillLevel, targetClone.getPersons(),
                    srcSkill.noSkillPoints());
            targetClone.addSkill(targetSkill);

            if (srcSkill.noSkillPoints()) {
              /*
               * Math.max(0, ...) guarantees that the true skill level cannot drop below 0. This
               * also important to handle the lostSkillLevel case below
               */
              final int skillModifier = srcSkill.getModifier(srcClone);
              final int transferredSkillFactor =
                  Math.max(0, srcSkill.getLevel() - skillModifier) * transferredPersons;
              final int newSkillLevel =
                  (int) (((float) transferredSkillFactor) / (float) (transferredPersons + targetClone
                      .getPersons()));

              /*
               * newSkillLevel == 0 means that that the skill is lost by this transfer but we may
               * not set the skill level to 0 + skillModifier since this would indicate an existing
               * skill depending on the modifier. Thus lostSkillLevel is used to distinctly mark the
               * staleness of this skill.
               */
              targetSkill.setLevel((newSkillLevel > 0) ? (newSkillLevel + skillModifier)
                  : lostSkillLevel);
            } else {
              final int newSkillPoints =
                  (int) (srcSkill.getPoints() * (transferredPersons / (float) srcClone.getPersons()));
              targetSkill.setPoints(newSkillPoints);
            }
          }

          /*
           * modify the skills in the source clone (no extra loop for this)
           */
          if (!srcSkill.noSkillPoints()) {
            final int transferredSkillPoints =
                (int) ((srcSkill.getPoints() * transferredPersons) / (float) srcClone.getPersons());
            srcSkill.setPoints(srcSkill.getPoints() - transferredSkillPoints);
          }
        }

        srcClone.setPersons(srcClone.getPersons() - transferredPersons);
        targetClone.setPersons(targetClone.getPersons() + transferredPersons);
      }
    }

    /* modify the skills according to recruitment */
    final MagellanUnitImpl clone = clones.get(getID());

    /* update the person and level information in all clone skills */
    if (clone.getSkills().size() > 0) {
      cache1.modifiedSkills = new Hashtable<StringID, Skill>();

      for (Skill skill : clone.getSkills()) {
        skill.setPersons(clone.persons);

        /*
         * When skill points are relevant, all we did up to now, was to keep track of these while
         * the skill level was ignored - update it now
         */
        if (!skill.noSkillPoints()) {
          skill.setLevel(skill.getLevel(clone, false));
        } else {
          /*
           * If skill points are not relevant we always take skill modifiers into account but we
           * marked 'lost' skills by Integer.MIN_VALUE which has to be fixed here
           */
          if (skill.getLevel() == lostSkillLevel) {
            skill.setLevel(0);
          }
        }

        /*
         * inject clone skills into real unit (no extra loop for this
         */
        if ((skill.getPoints() > 0) || (skill.getLevel() > 0)) {
          cache1.modifiedSkills.put(skill.getSkillType().getID(), skill);
        }
      }
    }
  }

  /**
   * Returns the unit container this belongs to. (ship, building or null)
   */
  public UnitContainer getUnitContainer() {
    if (getShip() != null)
      return getShip();
    if (getBuilding() != null)
      return getBuilding();
    return null;
  }

  /**
   * Returns the modified unit container this unit belongs to. (ship, building or null)
   */
  public UnitContainer getModifiedUnitContainer() {
    for (UnitContainerRelation ucr : getRelations(UnitContainerRelation.class)) {
      if (ucr instanceof EnterRelation)
        // fast return: first EnterRelation wins
        return ucr.target;
      else if (ucr instanceof LeaveRelation && ucr.target.equals(getUnitContainer()))
        // fast return: first LeaveRelation wins
        // we only left our container
        return null;
    }

    // we stayed in our ship
    return getUnitContainer();
  }

  /**
   * Returns the skill of the specified type if the unit has such a skill, else null is returned.
   */
  public Skill getSkill(SkillType type) {
    return (skills != null) ? (Skill) skills.get(type.getID()) : null;
  }

  /**
   * Returns the skill of the specified type if the unit has such a skill, else null is returned.
   */
  public Skill getSkill(StringID type) {
    return (skills != null) ? (Skill) skills.get(type) : null;
  }

  /**
   * Adds a skill to unit's collection of skills. If the unit already has a skill of the same type
   * it is overwritten with the the new skill object.
   * 
   * @return the specified skill s.
   */
  public Skill addSkill(Skill s) {
    if (skills == null) {
      skills = new OrderedHashtable<StringID, Skill>(11);
    }

    skills.put(s.getSkillType().getID(), s);

    return s;
  }

  /**
   * Returns all skills this unit has.
   * 
   * @return a collection of Skill objects.
   */
  public Collection<Skill> getSkills() {
    if (skills != null && skills.values() != null)
      return Collections.unmodifiableCollection(skills.values());
    else
      return Collections.emptyList();
  }

  public boolean isSkillsCopied() {
    return skillsCopied;
  }

  public void setSkillsCopied(boolean skillsCopied) {
    this.skillsCopied = skillsCopied;
  }

  /**
   * Removes all skills from this unit.
   */
  public void clearSkills() {
    final Cache cache1 = getCache();
    if (skills != null) {
      skills.clear();
      skills = null;

      if (hasCache() && (cache1.modifiedSkills != null)) {
        cache1.modifiedSkills.clear();
        cache1.modifiedSkills = null;
      }
    }
  }

  /**
   * Returns all the items this unit possesses.
   * 
   * @return a collection of Item objects.
   */
  public Collection<Item> getItems() {
    if (items != null && items.values() != null)
      return Collections.unmodifiableCollection(items.values());
    else
      return Collections.emptyList();
  }

  public void setItems(Map<StringID, Item> items) {
    this.items = items;
  }

  public Map<StringID, Item> getItemMap() {
    return items;
  }

  /**
   * Removes all items from this unit.
   */
  public void clearItems() {
    if (items != null) {
      items.clear();
      items = null;
      invalidateCache();
    }
  }

  /**
   * Returns the item of the specified type as it would appear after the orders of this unit have
   * been processed, i.e. the amount of the item might be modified by transfer orders. If the unit
   * does not have an item of the specified type nor is given one by some other unit, null is
   * returned.
   */
  public Item getModifiedItem(ItemType type) {
    Item i = null;

    final Cache cache1 = getCache();
    if (!hasCache() || (cache1.modifiedItems == null)) {
      refreshModifiedItems();
    }

    if (hasCache() && (cache1.modifiedItems != null)) {
      i = cache1.modifiedItems.get(type.getID());
    }

    return i;
  }

  /**
   * Returns a collection of the reserve relations concerning the given Item.
   * 
   * @param itemType
   * @return a collection of ReserveRelation objects.
   */
  public Collection<ReserveRelation> getItemReserveRelations(ItemType itemType) {
    final List<ReserveRelation> ret = new ArrayList<ReserveRelation>(getRelations().size());

    for (ReserveRelation rel : getRelations(ReserveRelation.class)) {
      if (rel.itemType.equals(itemType)) {
        ret.add(rel);
      }
    }

    return ret;
  }

  /**
   * Returns a collection of the itemrelations concerning the given Item.
   * 
   * @return a collection of ItemTransferRelation objects.
   */
  public List<ItemTransferRelation> getItemTransferRelations(ItemType type) {
    final List<ItemTransferRelation> ret =
        new ArrayList<ItemTransferRelation>(getRelations().size());

    for (ItemTransferRelation rel : getRelations(ItemTransferRelation.class)) {
      if (rel.itemType.equals(type)) {
        ret.add(rel);
      }
    }

    return ret;
  }

  /**
   * Returns a collection of the personrelations associated with this unit
   * 
   * @return a collection of PersonTransferRelation objects.
   */
  public List<PersonTransferRelation> getPersonTransferRelations() {
    final List<PersonTransferRelation> ret = getRelations(PersonTransferRelation.class);

    if (MagellanUnitImpl.log.isDebugEnabled()) {
      MagellanUnitImpl.log.debug("Unit.getPersonTransferRelations for " + this);
      MagellanUnitImpl.log.debug(ret);
    }

    return ret;
  }

  /**
   * Returns the items of this unit as they would appear after the orders of this unit have been
   * processed.
   * 
   * @return a collection of Item objects.
   */
  public Collection<Item> getModifiedItems() {
    final Cache cache1 = getCache();
    if (!hasCache() || (cache1.modifiedItems == null)) {
      refreshModifiedItems();
    }

    if (cache1.modifiedItems != null && cache1.modifiedItems.values() != null)
      return Collections.unmodifiableCollection(cache1.modifiedItems.values());
    else
      return Collections.emptyList();
  }

  /**
   * Deduces the modified items from the current items and the relations between this and other
   * units.
   */
  private synchronized void refreshModifiedItems() {
    final Cache cache1 = getCache();
    // 0. clear existing data structures
    if (hasCache() && (cache1.modifiedItems != null)) {
      cache1.modifiedItems.clear();
    }

    if (cache1.modifiedItems == null) {
      cache1.modifiedItems = new Hashtable<StringID, Item>(getItems().size() + 1);
    }

    // 1. check whether there is anything to do at all
    if (((items == null) || (items.size() == 0)) && getRelations().isEmpty())
      return;

    // 2. clone items
    for (Item i : getItems()) {
      cache1.modifiedItems.put(i.getItemType().getID(), new Item(i.getItemType(), i.getAmount()));
    }

    // 3a. now check relations for possible modifications; RESERVE orders first
    for (UnitRelation rel : getRelations()) {
      if (rel instanceof ReserveRelation) {
        final ReserveRelation itr = (ReserveRelation) rel;
        Item modifiedItem = cache1.modifiedItems.get(itr.itemType.getID());

        if (modifiedItem != null) { // the transferred item can be found among this unit's items
          // nothing to do
        } else { // the transferred item is not among the items the unit already has

          if (equals(itr.source)) {
            modifiedItem = new Item(itr.itemType, -itr.amount);
          } else {
            modifiedItem = new Item(itr.itemType, itr.amount);
          }

          cache1.modifiedItems.put(itr.itemType.getID(), modifiedItem);
        }
      }
    }
    // 3b. now check relations for possible modifications; GIVE orders second
    for (UnitRelation rel : getRelations()) {
      if (rel instanceof ItemTransferRelation) {
        final ItemTransferRelation itr = (ItemTransferRelation) rel;
        Item modifiedItem = cache1.modifiedItems.get(itr.itemType.getID());

        if (modifiedItem != null) { // the transferred item can be found among this unit's items

          if (equals(itr.source)) {
            modifiedItem.setAmount(modifiedItem.getAmount() - itr.amount);
          } else {
            modifiedItem.setAmount(modifiedItem.getAmount() + itr.amount);
          }
        } else { // the transferred item is not among the items the unit already has

          if (equals(itr.source)) {
            modifiedItem = new Item(itr.itemType, -itr.amount);
          } else {
            modifiedItem = new Item(itr.itemType, itr.amount);
          }

          cache1.modifiedItems.put(itr.itemType.getID(), modifiedItem);
        }
      }
    }

    /*
     * 4. iterate again to mimick that recruit orders are processed after give orders, not very nice
     * but probably not very expensive
     */
    for (UnitRelation rel : getRelations()) {
      if (rel instanceof RecruitmentRelation) {
        final RecruitmentRelation rr = (RecruitmentRelation) rel;

        final Item modifiedItem = cache1.modifiedItems.get(EresseaConstants.I_USILVER);

        if (modifiedItem != null) {
          final Race recruitmentRace = rr.race;

          if ((recruitmentRace != null) && (recruitmentRace.getRecruitmentCosts() > 0)) {
            modifiedItem.setAmount(modifiedItem.getAmount()
                - (rr.amount * recruitmentRace.getRecruitmentCosts()));
          }
        }
      }
    }
  }

  /**
   * @see magellan.library.Unit#getPersons()
   */
  public int getPersons() {
    return persons;
  }

  /**
   * Returns the number of persons in this unit as it would be after the orders of this and other
   * units have been processed since it may be modified by transfer orders.
   */
  public int getModifiedPersons() {
    final Cache cache1 = getCache();
    if (cache1.modifiedPersons == -1) {
      cache1.modifiedPersons = getPersons();

      for (PersonTransferRelation ptr : getPersonTransferRelations()) {
        if (equals(ptr.source)) {
          cache1.modifiedPersons -= ptr.amount;
        } else {
          cache1.modifiedPersons += ptr.amount;
        }
      }
    }

    return cache1.modifiedPersons;
  }

  /**
   * Returns the new Combat Status of this unit as it would be after the orders of this unit
   */
  public int getModifiedCombatStatus() {
    final Cache cache1 = getCache();
    if (cache1.modifiedCombatStatus == -2) {
      cache1.modifiedCombatStatus = getCombatStatus();
      // we only need to check relations for units, we know the
      // tha actual combat status - do we?
      if (cache1.modifiedCombatStatus > -1) {
        for (CombatStatusRelation rel : getRelations(CombatStatusRelation.class)) {
          cache1.modifiedCombatStatus = rel.newCombatStatus;
        }
      }
    }

    return cache1.modifiedCombatStatus;
  }

  /**
   * Returns the new (expected) guard value of this unit as it would be after the orders of this
   * unit (and the unit is still allive next turn) (@TODO: do we need a region.getModifiedGuards -
   * List? guess and hope not)
   */
  public int getModifiedGuard() {
    final Cache cache1 = getCache();
    if (cache1.modifiedGuard == -1) {
      cache1.modifiedGuard = getGuard();

      for (GuardRegionRelation rel : getRelations(GuardRegionRelation.class)) {
        cache1.modifiedGuard = rel.guard;
      }
    }
    return cache1.modifiedGuard;
  }

  /**
   * Returns the new Unaided status of this unit as it would be after the orders of this unit
   */
  public boolean getModifiedUnaided() {
    final Cache cache1 = getCache();
    if (!cache1.modifiedUnaidedValidated) {
      cache1.modifiedUnaidedValidated = true;
      cache1.modifiedUnaided = isUnaided();
      // we only need to check relations for units, we know the
      // tha actual combat status - do we?
      if (getCombatStatus() > -1) {
        for (CombatStatusRelation rel : getRelations(CombatStatusRelation.class)) {
          if (rel.newUnaidedSet) {
            cache1.modifiedUnaided = rel.newUnaided;
          }
        }
      }
    }

    return cache1.modifiedUnaided;
  }

  /**
   * @return true if weight is well known and NOT evaluated by Magellan
   */
  public boolean isWeightWellKnown() {
    return weight != -1;
  }

  /**
   * Returns the initial overall weight of this unit (persons and items) in silver. If this
   * information is available from the report we use this. Otherwise we call the game specific
   * weight calculation.
   * 
   * @return the initial weight of the unit
   * @deprecated use {@link MovementEvaluator#getWeight(Unit)}
   */
  @Deprecated
  public int getWeight() {
    return getRegion().getData().getGameSpecificStuff().getMovementEvaluator().getWeight(this);
  }

  /**
   * @see magellan.library.Unit#getSimpleWeight()
   */
  public int getSimpleWeight() {
    return weight;
  }

  /**
   * Returns the maximum payload in silver of this unit when it travels by horse. Horses, carts and
   * persons are taken into account for this calculation. If the unit has a sufficient skill in
   * horse riding but there are too many carts for the horses, the weight of the additional carts
   * are also already considered.
   * 
   * @return the payload in silver, CAP_NO_HORSES if the unit does not possess horses or
   *         CAP_UNSKILLED if the unit is not sufficiently skilled in horse riding to travel on
   *         horseback.
   * @deprecated use {@link MovementEvaluator#getPayloadOnHorse(Unit)}
   */
  @Deprecated
  public int getPayloadOnHorse() {
    return getRegion().getData().getGameSpecificStuff().getMovementEvaluator().getPayloadOnHorse(
        this);
  }

  /**
   * Returns the maximum payload in silver of this unit when it travels on foot. Horses, carts and
   * persons are taken into account for this calculation. If the unit has a sufficient skill in
   * horse riding but there are too many carts for the horses, the weight of the additional carts
   * are also already considered. The calculation also takes into account that trolls can tow carts.
   * 
   * @return the payload in silver, CAP_UNSKILLED if the unit is not sufficiently skilled in horse
   *         riding to travel on horseback.
   * @deprecated use {@link MovementEvaluator#getPayloadOnFoot(Unit)}
   */
  @Deprecated
  public int getPayloadOnFoot() {
    return getRegion().getData().getGameSpecificStuff().getMovementEvaluator().getPayloadOnFoot(
        this);
  }

  /**
   * Returns the weight of all items of this unit that are not horses or carts in silver
   * 
   * @deprecated use {@link MovementEvaluator#getLoad(Unit)}
   */
  @Deprecated
  public int getLoad() {
    return getRegion().getData().getGameSpecificStuff().getMovementEvaluator().getLoad(this);
  }

  /**
   * Returns the weight of all items of this unit that are not horses or carts in silver based on
   * the modified items.
   * 
   * @deprecated use {@link MovementEvaluator#getModifiedLoad(Unit)}
   */
  @Deprecated
  public int getModifiedLoad() {
    return getRegion().getData().getGameSpecificStuff().getMovementEvaluator()
        .getModifiedLoad(this);
  }

  /**
   * Returns the number of regions this unit is able to travel within one turn based on the riding
   * skill, horses, carts and load of this unit.
   * 
   * @deprecated use {@link MovementEvaluator#getRadius(Unit)}
   */
  @Deprecated
  public int getRadius() {
    return getRegion().getData().getGameSpecificStuff().getMovementEvaluator().getRadius(this);
  }

  /**
   * Returns the overall weight (persons, items) of this unit in silver by calling the game specific
   * calculation for the modified weight. Generally this should take care of modified persons and
   * modified items.
   * 
   * @return the modified weight of the unit
   * @deprecated use {@link MovementEvaluator#getModifiedWeight(Unit)}
   */
  @Deprecated
  public int getModifiedWeight() {
    return getRegion().getData().getGameSpecificStuff().getMovementEvaluator().getModifiedWeight(
        this);
  }

  /**
   * Returns all units this unit is transporting as passengers.
   * 
   * @return A Collection of transported <code>Unit</code>s
   */
  public Collection<Unit> getPassengers() {
    final Collection<Unit> passengers = new LinkedList<Unit>();

    for (TransportRelation tr : getRelations(TransportRelation.class)) {
      if (equals(tr.source)) {
        passengers.add(tr.target);
      }
    }

    return passengers;
  }

  /**
   * Returns all units indicating by their orders that they would transport this unit as a passenger
   * (if there is more than one such unit, that is a semantical error of course).
   * 
   * @return A Collection of <code>Unit</code>s carrying this one
   */
  public Collection<Unit> getCarriers() {
    final Collection<Unit> carriers = new LinkedList<Unit>();

    for (TransportRelation tr : getRelations(TransportRelation.class)) {
      if (equals(tr.target)) {
        carriers.add(tr.source);
      }
    }

    return carriers;
  }

  /**
   * Returns a Collection of all the units that are taught by this unit.
   * 
   * @return A Collection of <code>Unit</code>s taught by this unit
   */
  public Collection<Unit> getPupils() {
    final Collection<Unit> pupils = new LinkedList<Unit>();
    for (TeachRelation tr : getRelations(TeachRelation.class)) {
      if (equals(tr.source)) {
        if (tr.target != null) {
          pupils.add(tr.target);
        }
      }
    }
    return pupils;
  }

  /**
   * Returns a Collection of all the units that are teaching this unit.
   * 
   * @return A Collection of <code>Unit</code>s teaching this unit
   */
  public Collection<Unit> getTeachers() {
    final Collection<Unit> teachers = new LinkedList<Unit>();
    for (TeachRelation tr : getRelations(TeachRelation.class)) {
      if (equals(tr.target)) {
        if (tr.source != null) {
          teachers.add(tr.source);
        }
      }
    }
    return teachers;
  }

  /**
   * @see magellan.library.Unit#getAttackVictims()
   */
  public Collection<Unit> getAttackVictims() {
    final Collection<Unit> ret = new LinkedList<Unit>();

    for (AttackRelation ar : getRelations(AttackRelation.class)) {
      if (ar.source.equals(this)) {
        ret.add(ar.target);
      }
    }

    return ret;
  }

  /**
   * @see magellan.library.Unit#getAttackAggressors()
   */
  public Collection<Unit> getAttackAggressors() {
    final Collection<Unit> ret = new LinkedList<Unit>();

    for (AttackRelation ar : getRelations(AttackRelation.class)) {
      if (ar.target.equals(this)) {
        ret.add(ar.source);
      }
    }

    return ret;
  }

  /**
   * remove relations that are originating from us with a line number &gt;= <tt>from</tt>
   */
  private void removeRelationsOriginatingFromUs(int from) {
    final Collection<UnitRelation> deathRow = new LinkedList<UnitRelation>();

    for (UnitRelation r : getRelations()) {
      if (equals(r.origin) && (r.line >= from)) {
        if (r instanceof InterUnitRelation) {
          InterUnitRelation ir = (InterUnitRelation) r;
          if (ir.target != null) {
            // remove relations in target units
            if (ir.target.equals(this)) {
              if (ir.source != this) {
                ir.source.removeRelation(r);
              }
            } else {
              if (ir.target != this) {
                ir.target.removeRelation(r);
              }
            }
          }
        } else {
          if (r instanceof UnitContainerRelation) {
            // remove relations in target unit containers
            if (((UnitContainerRelation) r).target != null) {
              ((UnitContainerRelation) r).target.removeRelation(r);
            }
          }
        }

        // remove relation afterwards
        deathRow.add(r);
      }
    }

    for (UnitRelation unitRelation : deathRow) {
      removeRelation(unitRelation);
    }
  }

  // FIXME "No relation of a unit can affect an object outside the region". This might not be true
  // any more for familiars or ZAUBERE.
  /**
   * Parses the orders of this unit and detects relations between units established by those orders.
   * When does this method have to be called? No relation of a unit can affect an object outside the
   * region that unit is in. So when all relations regarding a certain unit as target or source need
   * to be determined, this method has to be called for each unit in the same region. Since
   * relations are defined by unit orders, modified orders may lead to different relations.
   * Therefore refreshRelations() has to be invoked on a unit after its orders were modified.
   */
  public void refreshRelations() {
    refreshRelations(1);
  }

  /**
   * Parses the orders of this unit <i>beginning at the <code>from</code>th order</i> and detects
   * relations between units established by those orders. When does this method have to be called?
   * No relation of a unit can affect an object outside the region that unit is in. So when all
   * relations regarding a certain unit as target or source need to be determined, this method has
   * to be called for each unit in the same region. Since relations are defined by unit orders,
   * modified orders may lead to different relations. Therefore refreshRelations() has to be invoked
   * on a unit after its orders were modified.
   * 
   * @param from Start from this line
   */
  public synchronized void refreshRelations(int from) {
    if (ordersObject.ordersAreNull() || (getRegion() == null))
      return;

    invalidateCache();
    removeRelationsOriginatingFromUs(from);
    // FIXME shouldn't access GameSpecific like this
    addAndSpreadRelations(getRegion().getData().getGameSpecificStuff().getRelationFactory()
        .createRelations(this, from));
  }

  private void addAndSpreadRelations(Collection<UnitRelation> newRelations) {
    if (MagellanUnitImpl.log.isDebugEnabled()) {
      MagellanUnitImpl.log.debug("Relations for " + this);
      MagellanUnitImpl.log.debug(newRelations);
    }

    for (UnitRelation r : newRelations) {
      addRelation(r);

      if (r.source != this) {
        r.source.addRelation(r);

        continue;
      }

      if (r instanceof InterUnitRelation) {
        final InterUnitRelation iur = (InterUnitRelation) r;

        if ((iur.target != null) && (iur.target != this)) {
          iur.target.addRelation(r);
        }

        continue;
      }

      if (r instanceof UnitContainerRelation) {
        final UnitContainerRelation ucr = (UnitContainerRelation) r;

        if (ucr.target != null) {
          ucr.target.addRelation(r);
        }

        continue;
      }
    }
  }

  /**
   * Returns a String representation of this unit.
   */
  @Override
  public String toString() {
    return toString(true);
  }

  /**
   * @param withName
   */
  public String toString(boolean withName) {
    if (withName) {
      String myName = getModifiedName();
      if (myName == null) {
        myName = getName();
      }
      if (myName == null) {
        myName = Resources.get("unit.unit") + " " + toString(false);
      }
      return myName + " (" + toString(false) + ")";
    } else
      return id.toString();
  }

  /**
   * Kinda obvious, right?
   */
  public MagellanUnitImpl(UnitID id) {
    super(id);
  }

  /**
   * Add a order to the unit's orders. This function ensures that TEMP units are not affected by the
   * operation.
   * 
   * @return <tt>true</tt> if the order was successfully added.
   */
  public boolean addOrder(String order) {
    return addOrder(order, false, 0);
  }

  /**
   * Add a order to the unit's orders. This function ensures that TEMP units are not affected by the
   * operation.
   * 
   * @param order the order to add.
   * @param replace if <tt>true</tt>, the order replaces any other of the unit's orders of the same
   *          type. If <tt>false</tt> the order is simply added.
   * @param length denotes the number of tokens that need to be equal for a replacement. E.g.
   *          specify 2 if order is "BENENNE EINHEIT abc" and all "BENENNE EINHEIT" orders should be
   *          replaced but not all "BENENNE" orders.
   * @return <tt>true</tt> if the order was successfully added.
   */
  public boolean addOrder(String order, boolean replace, int length) {
    if ((order == null) || order.trim().equals("") || (replace && (length < 1)))
      return false;

    if (replace) {
      removeOrder(order, length);
    }

    addOrderAt(-1, order);

    return true;
  }

  /**
   * Removes orders that match the given order up to a given length.
   * 
   * @param order pattern to remove
   * @param length denotes the number of tokens that need to be equal for a replacement. E.g.
   *          specify 2 if order is "BENENNE EINHEIT abc" and all "BENENNE EINHEIT" orders should be
   *          replaced but not all "BENENNE" orders.
   * @return <tt>true</tt> if at least one order was removed
   */
  public boolean removeOrder(String order, int length) {
    if ((order == null) || order.trim().equals(""))
      return false;

    // parse order until there are enough match tokens
    int tokenCounter = 0;
    final Collection<OrderToken> matchTokens = new LinkedList<OrderToken>();
    OrderTokenizer ct = new OrderTokenizer(new StringReader(order));
    OrderToken t = ct.getNextToken();

    while ((t.ttype != OrderToken.TT_EOC) && (tokenCounter++ < length)) {
      matchTokens.add(t);
      t = ct.getNextToken();
    }

    // order does not contain enough match tokens, abort
    if (matchTokens.size() < length)
      return false;

    boolean result = false;

    // if replace, delete matching orders first
    if (!ordersAreNull()) {
      boolean tempBlock = false;

      // cycle through this unit's orders
      for (final ListIterator<String> cmds = ordersObject.getOrders().listIterator(); cmds
          .hasNext();) {
        final String cmd = cmds.next();
        ct = new OrderTokenizer(new StringReader(cmd));
        t = ct.getNextToken();

        // skip empty orders and comments
        if ((OrderToken.TT_EOC == t.ttype) || (OrderToken.TT_COMMENT == t.ttype)) {
          continue;
        }

        if (false == tempBlock) {
          if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_MAKE))) {
            t = ct.getNextToken();

            if (OrderToken.TT_EOC == t.ttype) {
              continue;
            } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_TEMP))) {
              tempBlock = true;

              continue;
            }
          } else {
            // compare the current unit order and tokens of the one to add
            boolean removeOrder = true;

            for (final Iterator<OrderToken> iter = matchTokens.iterator(); iter.hasNext()
                && (t.ttype != OrderToken.TT_EOC);) {
              final OrderToken matchToken = iter.next();

              if (!(t.equalsToken(matchToken.getText()) || matchToken.equalsToken(t.getText()))) {
                removeOrder = false;

                break;
              }

              t = ct.getNextToken();
            }

            if (removeOrder) {
              cmds.remove();
              result = true;
            }

            continue;
          }
        } else {
          if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_END))) {
            tempBlock = false;

            continue;
          }
        }
      }
    }
    return result;
  }

  /**
   * Scans this unit's orders for temp units to create. It constructs them as TempUnit objects and
   * removes the corresponding orders from this unit. Uses the default order locale to parse the
   * orders.
   * 
   * @param tempSortIndex an index for sorting units (required to reconstruct the original order in
   *          the report) which is incremented with each new temp unit.
   * @return the new sort index. <tt>return value</tt> - sortIndex is the number of temp units read
   *         from this unit's orders.
   */
  public int extractTempUnits(GameData data, int tempSortIndex) {
    return extractTempUnits(data, tempSortIndex, Locales.getOrderLocale());
  }

  /**
   * Scans this unit's orders for temp units to create. It constructs them as TempUnit objects and
   * removes the corresponding orders from this unit.
   * 
   * @param tempSortIndex an index for sorting units (required to reconstruct the original order in
   *          the report) which is incremented with each new temp unit.
   * @param locale the locale to parse the orders with.
   * @return the new sort index. <tt>return value</tt> - sortIndex is the number of temp units read
   *         from this unit's orders.
   */
  public int extractTempUnits(GameData data, int tempSortIndex, Locale locale) {
    if (!ordersAreNull()) {
      TempUnit tempUnit = null;

      for (final Iterator<String> cmdIterator = ordersObject.getOrders().iterator(); cmdIterator
          .hasNext();) {
        final String line = cmdIterator.next();
        final OrderTokenizer ct = new OrderTokenizer(new StringReader(line));
        OrderToken token = ct.getNextToken();

        if (tempUnit == null) {
          if (token.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_MAKE, locale))) {
            token = ct.getNextToken();

            if (token.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_TEMP, locale))) {
              token = ct.getNextToken();

              try {
                final int base = (getID()).getRadix();
                final int idInt = IDBaseConverter.parse(token.getText(), base);
                final UnitID orderTempID = UnitID.createUnitID(idInt * -1, base);

                if (getRegion() == null || getRegion().getUnit(orderTempID) == null) {
                  tempUnit = createTemp(data, orderTempID);
                  tempUnit.setSortIndex(++tempSortIndex);
                  cmdIterator.remove();
                  token = ct.getNextToken();

                  if (token.ttype != OrderToken.TT_EOC) {
                    tempUnit.addOrders(Resources.getOrderTranslation(EresseaConstants.O_NAME,
                        locale)
                        + " "
                        + Resources.getOrderTranslation(EresseaConstants.O_UNIT, locale)
                        + " " + token.getText(), false);
                  }
                } else {
                  MagellanUnitImpl.log.warn("Unit.extractTempUnits(): region " + getRegion()
                      + " already contains a temp unit with the id " + orderTempID
                      + ". This temp unit remains in the orders of its parent "
                      + "unit instead of being created as a unit in its own right.");
                }
              } catch (final NumberFormatException e) {
              }
            }
          }
        } else {
          cmdIterator.remove();

          if (token.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_END, locale))) {
            tempUnit = null;
          } else {
            scanTempOrder(tempUnit, line);
          }
        }
      }
    }

    return tempSortIndex;
  }

  private void scanTempOrder(TempUnit tempUnit, String line) {
    boolean scanned = false;
    if (MagellanUnitImpl.CONFIRMEDTEMPCOMMENT.equals(line.trim())) {
      tempUnit.setOrdersConfirmed(true);
      scanned = true;
    }
    if (!scanned && line.trim().startsWith(MagellanUnitImpl.TAG_PREFIX_TEMP)) {
      String tag = null;
      String value = null;
      final StringTokenizer st = new StringTokenizer(line);
      if (st.hasMoreTokens()) {
        // ignore TAG_PREFIX_TEMP
        st.nextToken();
      }
      if (st.hasMoreTokens()) {
        tag = st.nextToken();
      }
      if (st.hasMoreTokens()) {
        value = st.nextToken().replace('~', ' ');
      }
      if (tag != null && value != null) {
        tempUnit.putTag(tag, value);
        scanned = true;
      }
    }
    if (!scanned) {
      tempUnit.addOrders(line, false);
    }
  }

  /*************************************************************************************
   * Taggable methods
   */

  /**
   * @see magellan.library.utils.Taggable#deleteAllTags()
   */
  public void deleteAllTags() {
    tagMap = null;
  }

  /**
   * @see magellan.library.utils.Taggable#putTag(java.lang.String, java.lang.String)
   */
  public String putTag(String tag, String value) {
    if (tag.equals("$tm_trigger")) {
      // (new ExtendedTests()).test(this);
    }

    if (tagMap == null) {
      tagMap = new LinkedHashMap<String, String>(1);
    }

    return tagMap.put(tag, value);
  }

  /**
   * @see magellan.library.utils.Taggable#getTag(java.lang.String)
   */
  public String getTag(String tag) {
    if (tagMap == null)
      return null;

    return tagMap.get(tag);
  }

  /**
   * @see magellan.library.utils.Taggable#removeTag(java.lang.String)
   */
  public String removeTag(String tag) {
    if (tagMap == null)
      return null;

    return tagMap.remove(tag);
  }

  /**
   * @see magellan.library.utils.Taggable#containsTag(java.lang.String)
   */
  public boolean containsTag(String tag) {
    if (tagMap == null)
      return false;

    return tagMap.containsKey(tag);
  }

  /**
   * @see magellan.library.utils.Taggable#getTagMap()
   */
  public Map<String, String> getTagMap() {
    if (tagMap == null) {
      tagMap = new LinkedHashMap<String, String>(1);
    }

    return Collections.unmodifiableMap(tagMap);
  }

  /**
   * @see magellan.library.utils.Taggable#hasTags()
   */
  public boolean hasTags() {
    return (tagMap != null) && !tagMap.isEmpty();
  }

  /**
   * a (hopefully) small class for handling orders in the Unit object
   */
  private static class Orders {
    private List<String> orders = null;
    private boolean changed = false;

    /**
     * Creates a new Orders object.
     */
    public Orders() {
    }

    /**
     * Returns the number of orders (lines).
     * 
     * @return
     */
    public int getSize() {
      return (orders == null) ? 0 : orders.size();
    }

    /**
     * Adds all Lines in <code>newOrders</code> to the orders
     * 
     * @param newOrders A collections of strings that are order lines.
     * @return The number of orders <em>befor</em> addition
     */
    public int addOrders(Collection<String> newOrders) {
      final int oldSize = getSize();

      if (newOrders == null)
        return oldSize;

      if (orders == null) {
        orders = new ArrayList<String>(newOrders.size());
      }

      orders.addAll(newOrders);
      changed = true;

      return oldSize;
    }

    /**
     * Replaces the orders by <code>newOrders</code>.
     * 
     * @param newOrders A collections of strings that are order lines.
     */
    public void setOrders(Collection<String> newOrders) {
      clearOrders();
      addOrders(newOrders);
    }

    /**
     * Returns the collection of orders which is backed by the orders and <i>not</i> immutable and
     * may be <code>null</code>.
     */
    public List<String> getOrders() {
      if (orders == null)
        return null;
      return orders;
    }

    /**
     * Deletes all orders and internally sets them to <code>null</code>.
     */
    public void removeOrders() {
      clearOrders();
      orders = null;
    }

    /**
     * Deletes all orders.
     */
    public void clearOrders() {
      if (orders != null) {
        orders.clear();
      }
    }

    /**
     * Returns <code>true</code> iff the orders are <code>null</code>.
     */
    public boolean ordersAreNull() {
      return orders == null;
    }

    /**
     * Inserts the specified order at the end.
     * 
     * @param newOrders
     */
    public void addOrder(String newOrders) {
      if (orders == null) {
        orders = new ArrayList<String>(1);
      }

      orders.add(newOrders);
    }

    /**
     * Inserts the specified order at the specified position.
     * 
     * @param i An index between 0 and getOrders().getSize() (inclusively)
     * @param newOrders
     */
    public void addOrderAt(int i, String newOrders) {
      if (orders == null) {
        orders = new ArrayList<String>(1);
      }

      orders.add(i, newOrders);
    }

    /**
     * Removes the order at the specified position. Shifts any subsequent elements to the left
     * (subtracts one from their indices).
     * 
     * @param i the index of the element to removed. The first order is 0!
     * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &gt;=
     *           getSize()).
     */
    public void removeOrderAt(int i) {
      if (orders == null) {
        orders = new ArrayList<String>(1);
      }

      orders.remove(i);
    }

    /**
     * Returns <code>true</code> if orders have been added and
     * <code>setOrdersChanged(<code>false</code>)</code> has not been called subsequently. Or if
     * <code>setOrdersChanged(<code>true</code>)</code> has been called.
     */
    public boolean ordersHaveChanged() {
      return changed;
    }

    /**
     * Manual override of ordersHaveChanged status.
     */
    public void setOrdersChanged(boolean changed) {
      this.changed = changed;
    }
  }

  /**
   * Returns the value of aura.
   * 
   * @return Returns aura.
   */
  public int getAura() {
    return aura;
  }

  /**
   * Sets the value of aura.
   * 
   * @param aura The value for aura.
   */
  public void setAura(int aura) {
    this.aura = aura;
  }

  /**
   * Returns the value of auraMax.
   * 
   * @return Returns auraMax.
   */
  public int getAuraMax() {
    return auraMax;
  }

  /**
   * Sets the value of auraMax.
   * 
   * @param auraMax The value for auraMax.
   */
  public void setAuraMax(int auraMax) {
    this.auraMax = auraMax;
  }

  /**
   * @see magellan.library.HasCache#hasCache()
   */
  public boolean hasCache() {
    // return cacheReference!=null && cacheReference.get()!=null;
    return cache != null;
  }

  /**
   * Returns the value of cache.
   * 
   * @return Returns cache.
   */
  public Cache getCache() {
    // Cache c;
    // if (cacheReference!=null && (c = cacheReference.get())!=null)
    // return c;
    // else{
    // c = new Cache();
    // cacheReference = new SoftReference<Cache>(c);
    // return c;
    // }
    if (cache == null) {
      cache = new Cache();
    }
    return cache;
  }

  /**
   * Sets the value of cache.
   * 
   * @param cache The value for cache.
   */
  public void setCache(Cache cache) {
    // cacheReference = new SoftReference<Cache>(cache);
    this.cache = cache;
  }

  /**
   * @see magellan.library.Unit#clearCache()
   */
  public void clearCache() {
    // if (cacheReference==null)
    // return;
    // Cache c = cacheReference.get();
    // if (c!=null)
    // c.clear();
    // cacheReference.clear();
    // cacheReference = null;
    if (cache == null)
      return;
    cache.clear();
    cache = null;
  }

  /**
   * @see magellan.library.HasCache#addCacheHandler(magellan.library.utils.CacheHandler)
   */
  public void addCacheHandler(CacheHandler handler) {
    getCache().addHandler(handler);
  }

  /**
   * @see magellan.library.Unit#getOrderEditor()
   */
  public CacheableOrderEditor getOrderEditor() {
    final Cache cache1 = getCache();
    if (hasCache() && cache1.orderEditor != null)
      return cache1.orderEditor;
    else
      return null;
  }

  public void setOrderEditor(CacheableOrderEditor editor) {
    getCache().orderEditor = editor;
  }

  /**
   * Returns the value of combatSpells.
   * 
   * @return Returns combatSpells.
   */
  public Map<ID, CombatSpell> getCombatSpells() {
    return combatSpells;
  }

  /**
   * Sets the value of combatSpells.
   * 
   * @param combatSpells The value for combatSpells.
   */
  public void setCombatSpells(Map<ID, CombatSpell> combatSpells) {
    this.combatSpells = combatSpells;
  }

  /**
   * Returns the value of combatStatus.
   * 
   * @return Returns combatStatus.
   */
  public int getCombatStatus() {
    return combatStatus;
  }

  /**
   * Sets the value of combatStatus.
   * 
   * @param combatStatus The value for combatStatus.
   */
  public void setCombatStatus(int combatStatus) {
    this.combatStatus = combatStatus;
  }

  /**
   * Returns the value of comments.
   * 
   * @return Returns comments.
   */
  public List<String> getComments() {
    return comments;
  }

  /**
   * Sets the value of comments.
   * 
   * @param comments The value for comments.
   */
  public void setComments(List<String> comments) {
    this.comments = comments;
  }

  /**
   * Returns the value of effects.
   * 
   * @return Returns effects.
   */
  public List<String> getEffects() {
    return effects;
  }

  /**
   * Sets the value of effects.
   * 
   * @param effects The value for effects.
   */
  public void setEffects(List<String> effects) {
    this.effects = effects;
  }

  /**
   * Returns the value of familiarmageID.
   * 
   * @return Returns familiarmageID.
   */
  public ID getFamiliarmageID() {
    return familiarmageID;
  }

  /**
   * Sets the value of familiarmageID.
   * 
   * @param familiarmageID The value for familiarmageID.
   */
  public void setFamiliarmageID(ID familiarmageID) {
    this.familiarmageID = familiarmageID;
  }

  /**
   * Returns the value of follows.
   * 
   * @return Returns follows.
   */
  public Unit getFollows() {
    return follows;
  }

  /**
   * Sets the value of follows.
   * 
   * @param follows The value for follows.
   */
  public void setFollows(Unit follows) {
    this.follows = follows;
  }

  /**
   * Returns the value of guard.
   * 
   * @return Returns guard.
   */
  public int getGuard() {
    return guard;
  }

  /**
   * Sets the value of guard.
   * 
   * @param guard The value for guard.
   */
  public void setGuard(int guard) {
    this.guard = guard;
  }

  /**
   * Returns the value of health.
   * 
   * @return Returns health.
   */
  public String getHealth() {
    return health;
  }

  /**
   * Sets the value of health.
   * 
   * @param health The value for health.
   */
  public void setHealth(String health) {
    this.health = health;
  }

  /**
   * Returns the value of hideFaction.
   * 
   * @return Returns hideFaction.
   */
  public boolean isHideFaction() {
    return hideFaction;
  }

  /**
   * Sets the value of hideFaction.
   * 
   * @param hideFaction The value for hideFaction.
   */
  public void setHideFaction(boolean hideFaction) {
    this.hideFaction = hideFaction;
  }

  /**
   * Returns the value of isHero.
   * 
   * @return Returns isHero.
   */
  public boolean isHero() {
    return isHero;
  }

  /**
   * Sets the value of isHero.
   * 
   * @param isHero The value for isHero.
   */
  public void setHero(boolean isHero) {
    this.isHero = isHero;
  }

  /**
   * Returns the value of isStarving.
   * 
   * @return Returns isStarving.
   */
  public boolean isStarving() {
    return isStarving;
  }

  /**
   * Sets the value of isStarving.
   * 
   * @param isStarving The value for isStarving.
   */
  public void setStarving(boolean isStarving) {
    this.isStarving = isStarving;
  }

  /**
   * Returns the value of ordersConfirmed.
   * 
   * @return Returns ordersConfirmed.
   */
  public boolean isOrdersConfirmed() {
    return ordersConfirmed;
  }

  /**
   * Sets the value of ordersConfirmed.
   * 
   * @param ordersConfirmed The value for ordersConfirmed.
   */
  public void setOrdersConfirmed(boolean ordersConfirmed) {
    this.ordersConfirmed = ordersConfirmed;
  }

  /**
   * Returns the value of privDesc.
   * 
   * @return Returns privDesc.
   */
  public String getPrivDesc() {
    return privDesc;
  }

  /**
   * Sets the value of privDesc.
   * 
   * @param privDesc The value for privDesc.
   */
  public void setPrivDesc(String privDesc) {
    this.privDesc = privDesc;
  }

  /**
   * @see magellan.library.Unit#getRace()
   */
  public Race getRace() {
    if (realRace != null)
      return realRace;
    else
      return race;
  }

  /**
   * Sets the value of race.
   * 
   * @param race The value for race.
   */
  public void setRace(Race race) {
    this.race = race;
  }

  /**
   * @see magellan.library.Unit#getDisguiseRace()
   */
  public Race getDisguiseRace() {
    if (realRace != null)
      return race;
    else
      return null;
  }

  /**
   * Sets the value of realRace.
   * 
   * @param realRace The value for realRace.
   */
  public void setRealRace(Race realRace) {
    this.realRace = realRace;
  }

  /**
   * Returns the value of siege.
   * 
   * @return Returns siege.
   */
  public Building getSiege() {
    return siege;
  }

  /**
   * Sets the value of siege.
   * 
   * @param siege The value for siege.
   */
  public void setSiege(Building siege) {
    this.siege = siege;
  }

  /**
   * Returns the value of spells.
   * 
   * @return Returns spells.
   */
  public Map<ID, Spell> getSpells() {
    return spells;
  }

  /**
   * Sets the value of spells.
   * 
   * @param spells The value for spells.
   */
  public void setSpells(Map<ID, Spell> spells) {
    this.spells = spells;
  }

  /**
   * Returns the value of stealth.
   * 
   * @return Returns stealth.
   */
  public int getStealth() {
    return stealth;
  }

  /**
   * Sets the value of stealth.
   * 
   * @param stealth The value for stealth.
   */
  public void setStealth(int stealth) {
    this.stealth = stealth;
  }

  /**
   * Returns the value of unaided.
   * 
   * @return Returns unaided.
   */
  public boolean isUnaided() {
    return unaided;
  }

  /**
   * Sets the value of unaided.
   * 
   * @param unaided The value for unaided.
   */
  public void setUnaided(boolean unaided) {
    this.unaided = unaided;
  }

  /**
   * Returns the value of unitMessages.
   * 
   * @return Returns unitMessages.
   */
  public List<Message> getUnitMessages() {
    return unitMessages;
  }

  /**
   * Sets the value of unitMessages.
   * 
   * @param unitMessages The value for unitMessages.
   */
  public void setUnitMessages(List<Message> unitMessages) {
    this.unitMessages = unitMessages;
  }

  /**
   * Sets the value of persons.
   * 
   * @param persons The value for persons.
   */
  public void setPersons(int persons) {
    this.persons = persons;
  }

  /**
   * Sets the value of skills.
   * 
   * @param skills The value for skills.
   */
  public void setSkills(Map<StringID, Skill> skills) {
    this.skills = skills;
  }

  /**
   * Sets the value of weight.
   * 
   * @param weight The value for weight.
   */
  public void setWeight(int weight) {
    this.weight = weight;
  }

  public Map<StringID, Skill> getSkillMap() {
    return skills;
  }

  /**
   * this function inspects travelthru an travelthruship to find the movement in the past
   * 
   * @return List of coordinates from start to end region.
   */
  public List<CoordinateID> getPastMovement(GameData data) {
    final Cache cache1 = getCache();
    if (cache1.movementPath == null) {
      // the result may be null!
      cache1.movementPath = Regions.getMovement(data, this);
    }

    if (cache1.movementPath == null)
      return Collections.emptyList();
    else
      return Collections.unmodifiableList(cache1.movementPath);
  }

  /**
   * Checks if the unit's movement was passive (transported or shipped).
   * 
   * @return
   */
  public boolean isPastMovementPassive() {
    final Cache cache1 = getCache();
    if (cache1.movementPathIsPassive == null) {
      cache1.movementPathIsPassive = evaluatePastMovementPassive() ? Boolean.TRUE : Boolean.FALSE;
    }

    return cache1.movementPathIsPassive.booleanValue();
  }

  private static final MessageType transportMessageType =
      new MessageType(IntegerID.create(891175669));

  private boolean evaluatePastMovementPassive() {
    final Unit u = this;
    if (u.getShip() != null) {
      if (u.equals(u.getShip().getOwnerUnit())) {
        // unit is on ship and the owner
        if (MagellanUnitImpl.log.isDebugEnabled()) {
          MagellanUnitImpl.log.debug("PathCellRenderer(" + u + "):false on ship");
        }

        return false;
      }

      // unit is on a ship and not the owner
      if (MagellanUnitImpl.log.isDebugEnabled()) {
        MagellanUnitImpl.log.debug("PathCellRenderer(" + u + "):true on ship");
      }

      return true;
    }

    // we assume a transportation to be passive, if
    // there is no message of type 891175669
    if (u.getFaction() == null) {
      if (MagellanUnitImpl.log.isDebugEnabled()) {
        MagellanUnitImpl.log.debug("PathCellRenderer(" + u + "):false no faction");
      }

      return false;
    }

    if (u.getFaction().getMessages() == null) {
      // faction has no message at all
      if (MagellanUnitImpl.log.isDebugEnabled()) {
        MagellanUnitImpl.log.debug("PathCellRenderer(" + u + "):false no faction");
      }

      return true;
    }

    for (Message m : u.getFaction().getMessages()) {
      if (false) {
        if (MagellanUnitImpl.log.isDebugEnabled()) {
          if (MagellanUnitImpl.transportMessageType.equals(m.getMessageType())) {
            MagellanUnitImpl.log.debug("PathCellRenderer(" + u + ") Message " + m);

            if ((m.getAttributes() != null) && (m.getAttributes().get("unit") != null)) {
              MagellanUnitImpl.log.debug("PathCellRenderer(" + u + ") Unit   "
                  + m.getAttributes().get("unit"));
              // FIXME actually it should be creatUnitID(*, 10, data.base), but it doesn't matter
              // here
              MagellanUnitImpl.log.debug("PathCellRenderer(" + u + ") UnitID "
                  + UnitID.createUnitID(m.getAttributes().get("unit"), 10));
            }
          }
        }
      }

      if (MagellanUnitImpl.transportMessageType.equals(m.getMessageType())
          && (m.getAttributes() != null) && (m.getAttributes().get("unit") != null)
          && u.getID().equals(UnitID.createUnitID(m.getAttributes().get("unit"), 10))) { // FIXME
        // 10,
        // data.base
        // found a transport message; this is only valid in
        // units with active movement
        if (MagellanUnitImpl.log.isDebugEnabled()) {
          MagellanUnitImpl.log.debug("PathCellRenderer(" + u + "):false with message " + m);
        }

        return false;
      }
    }

    if (MagellanUnitImpl.log.isDebugEnabled()) {
      MagellanUnitImpl.log.debug("PathCellRenderer(" + u + "):true with messages");
    }

    return true;
  }

  /**
   * Returns the id uniquely identifying this object.
   */
  @Override
  public UnitID getID() {
    return (UnitID) super.getID();
  }

}
