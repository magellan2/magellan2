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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import magellan.library.Building;
import magellan.library.CombatSpell;
import magellan.library.CoordinateID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Group;
import magellan.library.ID;
import magellan.library.Item;
import magellan.library.Message;
import magellan.library.Order;
import magellan.library.Orders;
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
import magellan.library.gamebinding.GameSpecificStuff;
import magellan.library.gamebinding.MovementEvaluator;
import magellan.library.relation.AttackRelation;
import magellan.library.relation.CombatStatusRelation;
import magellan.library.relation.GuardRegionRelation;
import magellan.library.relation.InterUnitRelation;
import magellan.library.relation.ItemTransferRelation;
import magellan.library.relation.MaintenanceRelation;
import magellan.library.relation.MovementRelation;
import magellan.library.relation.PersonTransferRelation;
import magellan.library.relation.RecruitmentRelation;
import magellan.library.relation.ReserveRelation;
import magellan.library.relation.TeachRelation;
import magellan.library.relation.TransportRelation;
import magellan.library.relation.UnitRelation;
import magellan.library.rules.ItemType;
import magellan.library.rules.Race;
import magellan.library.rules.SkillType;
import magellan.library.utils.Cache;
import magellan.library.utils.CacheHandler;
import magellan.library.utils.CollectionFactory;
import magellan.library.utils.Locales;
import magellan.library.utils.MagellanFactory;
import magellan.library.utils.OrderWriter;
import magellan.library.utils.Regions;
import magellan.library.utils.Resources;
import magellan.library.utils.comparator.LinearUnitTempUnitComparator;
import magellan.library.utils.comparator.SortIndexComparator;
import magellan.library.utils.guiwrapper.CacheableOrderEditor;
import magellan.library.utils.logging.Logger;

/**
 * @author $Author: $
 * @version $Revision: 389 $
 */
public class MagellanUnitImpl extends MagellanRelatedImpl implements Unit {
  private static final Logger log = Logger.getInstance(MagellanUnitImpl.class);

  public static final String CONFIRMEDTEMPCOMMENT = EresseaConstants.O_COMMENT
      + OrderWriter.CONFIRMEDTEMP;

  /**
   * grammar for ejcTag: ";ejcTempTag tag numbervalue|'stringvalue'"
   */
  public static final String TAG_PREFIX_TEMP = EresseaConstants.O_COMMENT + "ejcTagTemp ";

  /** The private description of the unit. */
  private String privDesc; // private description

  /** The displayed race of the unit. */
  private Race race;

  /** The real race of the (daemon) unit */
  private Race realRace;

  /**
   * unitID of the "father"-mage or mother.mage...
   */
  private ID familiarmageID;

  /** The weight in silver */
  private int weight = -1;

  /** an object encapsulation the orders of this unit as <tt>String</tt> objects */
  private MagellanOrdersImplementation ordersObject;

  /** Comments modifiable by the user. The comments are represented as String objects. */
  /** analog to comments in unitcontainer **/
  private List<String> comments;

  /** The number of persons of the unit */
  private int persons = 1;
  /** guard flag (only one flag left in modern Eressea) */
  private int guard = 0;
  /** The building that is besieged by this unit */
  private Building siege; // belagert
  /** stealth level */
  private int stealth = -1; // getarnt
  /** the current amount of aura */
  private int aura = -1;
  /** the maximum amount of aura */
  private int auraMax = -1;
  /** combat status code */
  private int combatStatus = -1; // Kampfstatus
  /** HELFE KÄMPFE NICHT status */
  private boolean unaided; // if attacked, this unit will not be helped by allied units
  /** faction hidden (PARTEITARNUNG) */
  private boolean hideFaction; // Parteitarnung
  /** follows tag */
  private Unit follows; // folgt-Tag
  /** hero tag */
  private boolean isHero; // hero-tag
  /** health tag */
  private String health;
  /** hunger tag */
  private boolean isStarving; // hunger-Tag

  // (stm 09-06-08) had to get rid of the soft reference again as it leads to problems with
  // updates of unit relations.
  // protected SoftReference<Cache> cacheReference;
  /**
   * The cache object containing cached information that may be not related enough to be
   * encapsulated as a function and is time consuming to gather.
   */
  private Cache cache;
  /**
   * Messages directly sent to this unit. The list contains instances of class <tt>Message</tt> with
   * type -1 and only the text set.
   */
  private List<Message> unitMessages;
  /** A map for unknown tags */
  private Map<String, String> tagMap;
  /**
   * A list containing <tt>String</tt> objects, specifying effects on this <tt>Unit</tt> object.
   */
  private List<String> effects;
  /** true indicates that the unit has orders confirmed by an user. */
  private boolean ordersConfirmed;
  /** all the unit's skills */
  private Map<StringID, Skill> skills; // maps SkillType.getID() objects to Skill objects
  // private boolean skillsCopied;
  /**
   * The items carried by this unit. The keys are the IDs of the item's type, the values are the
   * Item objects themselves.
   */
  private Map<StringID, Item> items;
  /**
   * The spells known to this unit. The keys are the IDs of the spells, the values are the Spell
   * objects themselves.
   */
  private Map<ID, Spell> spells;
  /**
   * Contains the spells this unit has set for use in a combat. This map contains data if a unit has
   * a magic skill and has actively set combat spells. The values in this map are objects of type
   * CombatSpell, the keys are their ids.
   */
  private Map<ID, CombatSpell> combatSpells;
  /** The group this unit belongs to. */
  private Group group;
  /** The previous id of this unit. */
  private UnitID alias;
  /**
   * Indicates that this unit belongs to a different faction than it pretends to. A unit cannot
   * disguise itself as a different faction and at the same time be a spy of another faction,
   * therefore, setting this attribute to true results in having the guiseFaction attribute set to
   * null.
   */
  private boolean isSpy;
  /**
   * If this unit is disguised and pretends to belong to a different faction this field holds that
   * faction, else it is null.
   */
  private Faction guiseFaction;
  /** The temp id this unit had before becoming a real unit. */
  private UnitID tempID;
  /** The region this unit is currently in. */
  protected Region region;
  /** The faction this unit belongs to. */
  private Faction faction;
  /** The building this unit stays in. */
  private Building building;
  /** The ship this unit is on. */
  private Ship ship;
  // units are sorted in unit containers with this index
  private int sortIndex = -1;
  /** A unit dependent prefix to be prepended to this faction's race name. */
  private String raceNamePrefix;
  /** A map containing all temp units created by this unit. */
  private Map<ID, TempUnit> tempUnits;
  /** A collection view of the temp units. */
  private Collection<TempUnit> tempUnitCollection;
  private GameData data;

  private static UnitContainer nullContainer = new NullContainer();

  private static int deprecationCounter = 0;

  /**
   * @see magellan.library.Unit#ordersAreNull()
   */
  public boolean ordersAreNull() {
    return ordersObject == null;
  }

  /**
   * Returns (or creates) the Orders object.
   */
  protected Orders getOrdersObject() {
    if (ordersObject == null) {
      ordersObject = new MagellanOrdersImplementation(this);
    }
    return ordersObject;
  }

  /**
   * Clears the orders
   *
   * @see magellan.library.Unit#clearOrders()
   */
  public void clearOrders() {
    if (!ordersAreNull()) {
      ordersObject.clear();
    }

    processOrders();
  }

  /**
   * Clears the orders and possibly refreshes the relations
   *
   * @param refreshRelations if true also refresh the relations of the unit.
   * @see magellan.library.Unit#clearOrders(boolean)
   * @deprecated relation refreshing is now done event-based
   */
  @Deprecated
  public void clearOrders(boolean refreshRelations) {
    clearOrders();
  }

  /**
   * Removes the order at position <tt>i</tt> and refreshes the relations
   *
   * @see magellan.library.Unit#removeOrderAt(int)
   */
  public void removeOrderAt(int i) {
    ordersObject.remove(i);

    processOrders();
  }

  /**
   * Removes the order at position <tt>i</tt> and possibly refreshes the relations
   *
   * @param refreshRelations if true also refresh the relations of the unit.
   * @see magellan.library.Unit#removeOrderAt(int, boolean)
   * @deprecated relation refreshing is now done event-based
   */
  @Deprecated
  public void removeOrderAt(int i, boolean refreshRelations) {
    removeOrderAt(i);
  }

  /**
   * @see magellan.library.Unit#removeOrder(java.lang.String, int)
   */
  public boolean removeOrder(String order, int length) {
    if (ordersAreNull())
      return false;
    boolean retVal = getOrdersObject().removeOrder(createOrder(order), length);

    if (retVal) {
      processOrders();
    }

    return retVal;
  }

  /**
   * Removes orders that match the given order up to a given length.
   *
   * @param order pattern to remove
   * @param length denotes the number of tokens that need to be equal for a replacement. E.g.
   *          specify 2 if order is "BENENNE EINHEIT abc" and all "BENENNE EINHEIT" orders should be
   *          replaced but not all "BENENNE" orders.
   * @param refreshRelations
   * @return <tt>true</tt> if at least one order was removed
   * @see magellan.library.Unit#removeOrder(java.lang.String, int, boolean)
   * @deprecated relation refreshing is now done event-based
   */
  @Deprecated
  public boolean removeOrder(String order, int length, boolean refreshRelations) {
    return removeOrder(order, length);
  }

  /**
   * Add a order to the unit's orders. This function ensures that TEMP units are not affected by the
   * operation.
   *
   * @return <tt>true</tt> if the order was successfully added.
   * @see magellan.library.Unit#addOrder(java.lang.String)
   */
  public boolean addOrder(String order) {
    if ((order == null) || order.trim().equals(""))
      return false;
    addOrderAt(-1, createOrder(order));

    processOrders();

    return true;
  }

  /**
   * Add a order to the unit's orders. This function ensures that TEMP units are not affected by the
   * operation.
   *
   * @return <tt>true</tt> if the order was successfully added.
   * @see magellan.library.Unit#addOrder(java.lang.String, boolean)
   * @deprecated relation refreshing is now done event-based
   */
  @Deprecated
  public boolean addOrder(String order, boolean refreshRelations) {
    return addOrder(order);
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
   * @see magellan.library.Unit#addOrder(java.lang.String, boolean, int)
   */
  public boolean addOrder(String order, boolean replace, int length) {
    if ((order == null) || order.trim().equals("") || (replace && (length < 1)))
      return false;

    boolean removed = false;
    if (replace) {
      removed = removeOrder(order, length);
    }

    addOrderAt(-1, order);

    processOrders();
    return true;
  }

  /**
   * Add specified order at the end.
   *
   * @param line
   */
  public void addOrder(Order line) {
    addOrders2(Collections.singletonList(line));
  }

  /**
   * @see magellan.library.Unit#addOrder(magellan.library.Order, boolean)
   * @deprecated relation refreshing is now done event-based
   */
  @Deprecated
  public void addOrder(Order line, boolean refreshRelations) {
    addOrders2(Collections.singletonList(line));
  }

  /**
   * Adds the order at position <tt>i</tt> and refreshes the relations
   *
   * @param pos An index between 0 and getOrders().getSize() (inclusively), or -1 to add at the end.
   * @param newOrder
   * @see magellan.library.Unit#addOrderAt(int, java.lang.String)
   */
  public void addOrderAt(int pos, String newOrder) {
    addOrderAt(pos, createOrder(newOrder));
  }

  /**
   * Adds the order at position <tt>i</tt> and possibly refreshes the relations
   *
   * @param pos An index between 0 and getOrders().getSize() (inclusively), or -1 to add at the end.
   * @param newOrder
   * @param refreshRelations if true also refresh the relations of the unit.
   * @see magellan.library.Unit#addOrderAt(int, java.lang.String, boolean)
   * @deprecated relation refreshing is now done event-based
   */
  @Deprecated
  public void addOrderAt(int pos, String newOrder, boolean refreshRelations) {
    addOrderAt(pos, newOrder);
  }

  /**
   * @see magellan.library.Unit#addOrderAt(int, magellan.library.Order)
   */
  public void addOrderAt(int pos, Order newOrder) {
    if (pos < 0) {
      getOrdersObject().add(newOrder);
    } else {
      getOrdersObject().add(pos, newOrder);
    }

    processOrders();
  }

  /**
   * @see magellan.library.Unit#addOrderAt(int, magellan.library.Order, boolean)
   * @deprecated relation refreshing is now done event-based
   */
  @Deprecated
  public void addOrderAt(int pos, Order newOrder, boolean refreshRelations) {
    addOrderAt(pos, newOrder);
  }

  /**
   * @see magellan.library.Unit#replaceOrder(int, Order)
   */
  public void replaceOrder(int pos, Order newOrder) {
    ordersObject.set(pos, newOrder);

    processOrders();
  }

  /**
   * @see magellan.library.Unit#replaceOrder(int, magellan.library.Order, boolean)
   * @deprecated relation refreshing is now done event-based
   */
  @Deprecated
  public void replaceOrder(int pos, Order newOrder, boolean refreshRelations) {
    replaceOrder(pos, newOrder);
  }

  /**
   * @see magellan.library.Unit#createOrder(java.lang.String)
   */
  public Order createOrder(String newOrder) {
    return getData().getOrderParser().parse(newOrder, getLocale());
  }

  /**
   * Adds the orders and refreshes the relations
   *
   * @param newOrders
   * @see magellan.library.Unit#addOrders(java.util.Collection)
   */
  public void addOrders(Collection<String> newOrders) {
    final int newPos = getOrdersObject().size();
    for (String line : newOrders) {
      getOrdersObject().add(createOrder(line));
    }

    processOrders();
  }

  /**
   * Adds the orders and possibly refreshes the relations
   *
   * @param newOrders
   * @param refreshRelations If true also refresh the relations of the unit
   * @see magellan.library.Unit#addOrders(java.util.Collection, boolean)
   * @deprecated relation refreshing is now done event-based
   */
  @Deprecated
  public void addOrders(Collection<String> newOrders, boolean refreshRelations) {
    addOrders(newOrders);
  }

  /**
   * @see magellan.library.Unit#addOrders2(java.util.Collection, boolean)
   * @deprecated relation refreshing is now done event-based
   */
  @Deprecated
  public void addOrders2(Collection<Order> newOrders, boolean refreshRelations) {
    addOrders2(newOrders);
  }

  /**
   * @see magellan.library.Unit#addOrders2(java.util.Collection, boolean)
   */
  public void addOrders2(Collection<Order> newOrders) {
    final int newPos = getOrdersObject().size();
    getOrdersObject().addAll(newOrders);
    processOrders();
  }

  /**
   * Sets the orders and refreshes the relations
   *
   * @param newOrders my be <code>null</code>
   * @see magellan.library.Unit#setOrders(java.util.Collection)
   */
  public void setOrders(Collection<String> newOrders) {
    if (newOrders == null) {
      ordersObject = null;
      return;
    }
    if (!ordersAreNull()) {
      ordersObject.clear();
    }
    addOrders(newOrders);
  }

  /**
   * Sets the orders and possibly refreshes the relations
   *
   * @param newOrders
   * @param refreshRelations if true also refresh the relations of the unit.
   * @see magellan.library.Unit#setOrders(java.util.Collection, boolean)
   * @deprecated relation refreshing is now done event-based
   */
  @Deprecated
  public void setOrders(Collection<String> newOrders, boolean refreshRelations) {
    setOrders(newOrders);
  }

  /**
   * @see magellan.library.Unit#setOrders2(java.util.Collection)
   */
  public void setOrders2(Collection<Order> newOrders) {
    if (newOrders == null) {
      ordersObject = null;
    } else {
      getOrdersObject().clear();
      getOrdersObject().addAll(newOrders);
    }
    processOrders();
  }

  /**
   * @see magellan.library.Unit#setOrders2(java.util.Collection, boolean)
   * @deprecated relation refreshing is now done event-based
   */
  @Deprecated
  public void setOrders2(Collection<Order> newOrders, boolean refresh) {
    setOrders2(newOrders);
  }

  /**
   * @see magellan.library.Unit#getOrders()
   * @deprecated Use {@link #getOrders2()}
   */
  @Deprecated
  public List<String> getOrders() {
    if (ordersAreNull())
      return Collections.emptyList();
    List<String> list = new ArrayList<String>(getOrders2().size());
    for (Order order : getOrders2()) {
      list.add(order.toString());
    }
    return Collections.unmodifiableList(list);
  }

  /**
   * @see magellan.library.Unit#getOrders2()
   */
  public Orders getOrders2() {
    if (ordersAreNull())
      return new MagellanOrdersImplementation(this);
    return ordersObject.getView();
  }

  /**
   * Sets the group this unit belongs to.
   *
   * @param g the group of the unit
   * @see magellan.library.Unit#setGroup(magellan.library.Group)
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
   * @see magellan.library.Unit#getGroup()
   */
  public Group getGroup() {
    return group;
  }

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
      items = CollectionFactory.<StringID, Item> createOrderedMap(3, .8f);
    }

    items.put(i.getItemType().getID(), i);
    // FIXME necessary?
    invalidateCache(true);

    return i;
  }

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

  public Locale getLocale() {
    if (getFaction() == null)
      return Locales.getOrderLocale();
    return getFaction().getLocale();
  }

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

  /**
   * @see magellan.library.Unit#enter(magellan.library.UnitContainer)
   */
  public void enter(UnitContainer newUC) {
    UnitContainer uc = getModifiedUnitContainer();
    if (uc != null) {
      uc.leave(this);
    }

    if (newUC == null) {
      // null container indicates no container, as opposed to "same as getContainer()"
      newUC = nullContainer;
    }
    getCache().modifiedContainer = newUC;

    newUC.enter(this);
  }

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
   * @param gdata The GameData
   * @return the name or null if this unit's race or its name is not set.
   */
  public String getRaceName(GameData gdata) {
    Race tempRace = getDisguiseRace();
    if (tempRace == null) {
      tempRace = getRace();
    }
    if (tempRace != null) {
      if (getRaceNamePrefix() != null)
        return gdata.getTranslation(getRaceNamePrefix()) + tempRace.getName().toLowerCase();
      else {
        if ((group != null) && (group.getRaceNamePrefix() != null))
          return gdata.getTranslation(group.getRaceNamePrefix()) + tempRace.getName().toLowerCase();
        else {
          if ((faction != null) && (faction.getRaceNamePrefix() != null))
            return gdata.getTranslation(faction.getRaceNamePrefix())
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
  public Orders getCompleteOrders() {
    return getCompleteOrders(false);
  }

  /**
   * Returns all orders including the orders necessary to issue the creation of all the child temp
   * units of this unit.
   *
   * @param writeUnitTagsAsVorlageComment If this is <code>true</code>, unit tags are also added as
   *          Vorlage comments
   */
  public Orders getCompleteOrders(boolean writeUnitTagsAsVorlageComment) {
    final List<Order> cmds = new LinkedList<Order>();
    if (!ordersAreNull()) {
      cmds.addAll(ordersObject);
    }

    if (writeUnitTagsAsVorlageComment && hasTags()) {
      for (String tag : getTagMap().keySet()) {
        cmds.add(createOrder(EresseaConstants.O_PCOMMENT + " #after 1 { #tag EINHEIT "
            + tag.replace(' ', '~') + " '" + getTag(tag) + "' }"));
      }
    }

    cmds.addAll(getData().getGameSpecificStuff().getOrderChanger().getTempOrders(
        writeUnitTagsAsVorlageComment, this));

    return new MagellanOrdersImplementation(this, Collections.unmodifiableList(cmds));
  }

  protected String getOrderTranslation(StringID orderId) {
    return data.getGameSpecificStuff().getOrderChanger().getOrderO(getLocale(), orderId).getText();
  }

  /**
   * Creates a new temp unit with this unit as the parent. The temp unit is fully initialised, i.e.
   * it is added to the region units collection in the specified game data,it inherits the faction,
   * building or ship, region, faction stealth status, group, race and combat status settings and
   * adds itself to the corresponding unit collections.
   *
   * @throws IllegalArgumentException If <code>key</code> is negative
   */
  public TempUnit createTemp(GameData gdata, UnitID key) {
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
    if ((getRegion() != null) && (getData() != null)) {
      gdata.addTempUnit(t);
    } else {
      MagellanUnitImpl.log
          .warn(
              "Unit.createTemp(): Warning: Couldn't add temp unit to game data. Couldn't access game data");
    }

    // FIXME(stm) fire unitorderschanged?)

    return t;
  }

  /**
   * Removes a temp unit with this unit as the parent completely from the game data.
   */
  public void deleteTemp(UnitID key, GameData gdata) {
    final TempUnit t = (TempUnit) removeTemp(key);

    if (t != null) {
      t.clearOrders();

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
      gdata.removeTemp(key);

      processOrders();
    }
  }

  /**
   * Resets the cache of this unit to its uninitialized state.
   */
  private void invalidateCache(boolean reset) {
    if (hasCache()) {
      final Cache cache1 = getCache();
      cache1.modifiedName = null;
      cache1.modifiedSkills = null;
      cache1.modifiedItems = null;
      cache1.modifiedPersons = -1;
      cache1.modifiedAmount = -1;
      cache1.modifiedSize = -1;
      cache1.modifiedCombatStatus = EresseaConstants.CS_INIT;
      cache1.modifiedUnaidedValidated = false;
      cache1.modifiedGuard = -1;

      if (reset) {
        cache1.modifiedContainer = null;
      }
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
   * up so far or if there are no relations.
   */
  @Override
  public List<UnitRelation> getRelations() {
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
    // FIXME proactive recalculation of modified items!?!
    invalidateCache(false);
  }

  /**
   * @see magellan.library.impl.MagellanRelatedImpl#removeRelation(magellan.library.relation.UnitRelation)
   */
  @Override
  public UnitRelation removeRelation(UnitRelation rel) {
    final UnitRelation ret = super.removeRelation(rel);
    if (ret != null) {
      invalidateCache(false);
    }
    return ret;
  }

  /**
   * deliver all directly related units
   *
   * @see magellan.library.Unit#getRelatedUnits(java.util.Collection)
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
   * relations (exactly, not as subclasses).
   *
   * @param units all units gathered so far to prevent loops.
   * @param relations a set of classes naming the types of relations that are eligible for regarding
   *          a unit as related to some other unit.
   * @see magellan.library.Unit#getRelatedUnits(java.util.Set, java.util.Set)
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

    return Collections.unmodifiableList((movementRelations.iterator().next()).getMovement());
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
    if (type == null)
      return null;
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
    final List<Unit> sortedUnits = new ArrayList<Unit>(relatedUnits);
    Collections.sort(sortedUnits, new LinearUnitTempUnitComparator(new SortIndexComparator<Unit>(
        null)));

    /* clone units with all aspects relevant for skills */
    final Map<ID, MagellanUnitImpl> clones = new Hashtable<ID, MagellanUnitImpl>();

    for (Unit unit : relatedUnits) {
      final MagellanUnitImpl u = (MagellanUnitImpl) unit;
      MagellanUnitImpl clone = null;

      clone = new MagellanUnitImpl(u.getID().clone(), getData());
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
      // } catch (final CloneNotSupportedException e) {
      // // won't fail
      // }

    }

    // now modify the skills according to changes introduced by the relations

    /*
     * indicates that a skill is lost through person transfers or recruiting. May not be
     * Integer.MIN_VALUE to avoid wrap- around effects but should also be fairly negative so no
     * modifier can push it up to positive values.
     */
    final int lostSkillLevel = Skill.SPECIAL_LEVEL;

    for (Unit unit : sortedUnits) {
      final MagellanUnitImpl srcUnit = (MagellanUnitImpl) unit;

      for (UnitRelation unitRel : srcUnit.getRelations()) {
        if ((unitRel.source != null && !(unitRel.source.equals(srcUnit)))
            || !(unitRel instanceof PersonTransferRelation)) {
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
                (int) (((float) (transferredSkillFactor + targetSkillFactor))
                    / (float) (transferredPersons + targetClone
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
                  (int) (((float) transferredSkillFactor) / (float) (transferredPersons
                      + targetClone
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
                  (int) (srcSkill.getPoints() * (transferredPersons / (float) srcClone
                      .getPersons()));
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
   *
   * @see magellan.library.Unit#getUnitContainer()
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
    if (!hasCache() || getCache().modifiedContainer == null)
      return getUnitContainer();

    UnitContainer maybeContainer = getCache().modifiedContainer;
    return maybeContainer == nullContainer ? null : maybeContainer;
  }

  /**
   * Returns the skill of the specified type if the unit has such a skill, else null is returned.
   */
  public Skill getSkill(SkillType type) {
    return (type != null && skills != null) ? (Skill) skills.get(type.getID()) : null;
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
      skills = CollectionFactory.<StringID, Skill> createSyncOrderedMap(3, .8f);
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
      // FIXME necessary?
      invalidateCache(true);
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
   * Returns a collection of the item relations concerning the given Item.
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
   * Returns a collection of the person relations associated with this unit
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
        applyRelation(cache1, (ReserveRelation) rel);
      }
      // }
      // // 3b. now check relations for possible modifications; GIVE orders second
      // for (UnitRelation rel : getRelations()) {
      if (rel instanceof ItemTransferRelation) {
        applyRelation(cache1, (ItemTransferRelation) rel);
      }
      // }
      //
      // /*
      // * 4. iterate again to mimick that recruit orders are processed after give orders, not very
      // nice
      // * but probably not very expensive
      // */
      // for (UnitRelation rel : getRelations()) {
      if (rel instanceof RecruitmentRelation) {
        applyRelation(cache1, (RecruitmentRelation) rel);
      }
      // }
      // // 5. check building upkeep
      // for (UnitRelation rel : getRelations()) {
      if (rel instanceof MaintenanceRelation) {
        applyRelation(cache1, (MaintenanceRelation) rel);
      }
    }
  }

  private void applyRelation(Cache cache1, ReserveRelation resr) {
    Item modifiedItem = cache1.modifiedItems.get(resr.itemType.getID());

    if (modifiedItem != null) { // the transferred item can be found among this unit's items
      // nothing to do
    } else { // the transferred item is not among the items the unit already has
      modifiedItem = new Item(resr.itemType, 0);
      cache1.modifiedItems.put(resr.itemType.getID(), modifiedItem);
    }
  }

  private void applyRelation(Cache cache1, ItemTransferRelation itr) {
    Item modifiedItem = cache1.modifiedItems.get(itr.itemType.getID());

    if (modifiedItem != null) {
      modifiedItem.setChanged(true);
      // the transferred item can be found among this unit's items
      if (equals(itr.source)) {
        modifiedItem.setAmount(modifiedItem.getAmount() - itr.amount);
      }
      if (equals(itr.target)) {
        modifiedItem.setAmount(modifiedItem.getAmount() + itr.amount);
      }
    } else {
      // the transferred item is not among the items the unit already has
      if (equals(itr.source)) {
        modifiedItem = new Item(itr.itemType, -itr.amount);
      } else if (equals(itr.target)) {
        modifiedItem = new Item(itr.itemType, itr.amount);
      } else {
        // we're neither source nor target, but we triggered a transfer between two unit
        // (material pool)
        modifiedItem = new Item(itr.itemType, 0);
      }

      modifiedItem.setChanged(true);
      cache1.modifiedItems.put(itr.itemType.getID(), modifiedItem);
    }
  }

  private void applyRelation(Cache cache1, RecruitmentRelation rr) {
    Item modifiedItem = cache1.modifiedItems.get(EresseaConstants.I_USILVER);

    if (modifiedItem != null) {
      modifiedItem.setChanged(true);
      modifiedItem.setAmount(modifiedItem.getAmount() - rr.costs);
    } else {
      if (equals(rr.target)) {
        modifiedItem =
            new Item(getData().getRules().getItemType(EresseaConstants.I_USILVER), -rr.costs);
        modifiedItem.setChanged(true);
        cache1.modifiedItems.put(EresseaConstants.I_USILVER, modifiedItem);
      }
    }
  }

  private void applyRelation(Cache cache1, MaintenanceRelation rr) {
    Item modifiedItem = cache1.modifiedItems.get(rr.itemType.getID());

    if (modifiedItem != null) {
      modifiedItem.setChanged(true);
      modifiedItem.setAmount(modifiedItem.getAmount() - rr.getCosts());
    } else {
      modifiedItem = new Item(rr.itemType, -rr.getCosts());
      modifiedItem.setChanged(true);
      cache1.modifiedItems.put(rr.itemType.getID(), modifiedItem);
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
    if (cache1.modifiedCombatStatus == EresseaConstants.CS_INIT) {
      cache1.modifiedCombatStatus = getCombatStatus();
      // we only need to check relations for units, we know the
      // the actual combat status - do we?
      // if (cache1.modifiedCombatStatus > -1) {
      for (CombatStatusRelation rel : getRelations(CombatStatusRelation.class)) {
        if (!rel.newUnaidedSet) {
          cache1.modifiedCombatStatus = rel.newCombatStatus;
        }
      }
      // }
    }

    return cache1.modifiedCombatStatus;
  }

  /**
   * Returns the new (expected) guard value of this unit as it would be after the orders of this
   * unit (and the unit is still alive next turn) (@TODO: do we need a region.getModifiedGuards -
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
      // the actual combat status - do we?
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
    return getData().getGameSpecificStuff().getMovementEvaluator().getWeight(this);
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
    return getData().getGameSpecificStuff().getMovementEvaluator().getPayloadOnHorse(this);
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
    return getData().getGameSpecificStuff().getMovementEvaluator().getPayloadOnFoot(this);
  }

  /**
   * Returns the weight of all items of this unit that are not horses or carts in silver
   *
   * @deprecated use {@link MovementEvaluator#getLoad(Unit)}
   */
  @Deprecated
  public int getLoad() {
    return getData().getGameSpecificStuff().getMovementEvaluator().getLoad(this);
  }

  /**
   * Returns the weight of all items of this unit that are not horses or carts in silver based on
   * the modified items.
   *
   * @deprecated use {@link MovementEvaluator#getModifiedLoad(Unit)}
   */
  @Deprecated
  public int getModifiedLoad() {
    return getData().getGameSpecificStuff().getMovementEvaluator().getModifiedLoad(this);
  }

  /**
   * Returns the number of regions this unit is able to travel within one turn based on the riding
   * skill, horses, carts and load of this unit.
   *
   * @deprecated use {@link MovementEvaluator#getRadius(Unit)}
   */
  @Deprecated
  public int getRadius() {
    return getData().getGameSpecificStuff().getMovementEvaluator().getRadius(this);
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
    return getData().getGameSpecificStuff().getMovementEvaluator().getModifiedWeight(this);
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

  // "No relation of a unit can affect an object outside the region". This might not be true
  // any more for familiars or ZAUBERE.
  /**
   * Parses the orders of this unit and detects relations between units established by those orders.
   * When does this method have to be called? No relation of a unit can affect an object outside the
   * region that unit is in. So when all relations regarding a certain unit as target or source need
   * to be determined, this method has to be called for each unit in the same region. Since
   * relations are defined by unit orders, modified orders may lead to different relations.
   * Therefore refreshRelations() has to be invoked on a unit after its orders were modified.
   *
   * @deprecated relation refreshing is now done event-based
   */
  @Deprecated
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
   * @deprecated relation refreshing is now done event-based
   */
  @Deprecated
  public synchronized void refreshRelations(int from) {
    if (deprecationCounter++ < 10) {
      log.warn("calling deprecated refreshRelations", new Exception());
    }
    processOrders();
  }

  private void processOrders() {
    if (ordersAreNull() || (getRegion() == null))
      return;

    getData().getGameSpecificStuff().getRelationFactory().createRelations(getRegion());
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
  public MagellanUnitImpl(UnitID id, GameData data) {
    super(id);
    this.data = data;
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
  public int extractTempUnits(GameData gdata, int tempSortIndex) {
    return extractTempUnits(gdata, tempSortIndex, getLocale());
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
  public int extractTempUnits(GameData gdata, int tempSortIndex, Locale locale) {
    return gdata.getGameSpecificStuff().getOrderChanger().extractTempUnits(gdata, tempSortIndex,
        locale, this);
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
      // Faction f = (this).getFaction();
      // (new E3CommandParser(data, ExtendedCommandsProvider.createHelper(null, data, null, f)))
      // .execute(f);
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
    this.spells = CollectionFactory.createSyncOrderedMap(spells.size());
    this.spells.putAll(spells);
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

  /**
   * @see magellan.library.Unit#getSkillMap()
   */
  public Map<StringID, Skill> getSkillMap() {
    return skills;
  }

  /**
   * this function inspects travelthru an travelthruship to find the movement in the past
   *
   * @return List of coordinates from start to end region.
   */
  public List<CoordinateID> getPastMovement(GameData gdata) {
    final Cache cache1 = getCache();
    if (cache1.movementPath == null) {
      // the result may be null!
      cache1.movementPath = Regions.getMovement(gdata, this);
    }

    if (cache1.movementPath == null)
      return Collections.emptyList();
    else
      return Collections.unmodifiableList(cache1.movementPath);
  }

  /**
   * Checks if the unit's movement was passive (transported or shipped).
   *
   * @return <code>true</code> if the unit's past movement was passive
   * @deprecated Use {@link #isPastMovementPassive(GameSpecificStuff)}
   */
  @Deprecated
  public boolean isPastMovementPassive() {
    return isPastMovementPassive(getData().getGameSpecificStuff());
  }

  public boolean isPastMovementPassive(GameSpecificStuff gameSpecificStuff) {
    final Cache cache1 = getCache();
    if (cache1.movementPathIsPassive == null) {
      cache1.movementPathIsPassive = false;
      // Boolean.valueOf(gameSpecificStuff.getMovementEvaluator().isPastMovementPassive(this));
    }

    return cache1.movementPathIsPassive.booleanValue();
  }

  /**
   * Returns the id uniquely identifying this object.
   */
  @Override
  public UnitID getID() {
    return (UnitID) super.getID();
  }

  /**
   * @see magellan.library.Unit#getData()
   */
  public GameData getData() {
    return data;
  }

  /**
   * @see magellan.library.impl.MagellanRelatedImpl#clearRelations()
   */
  public void clearRelations() {
    if (getCache().relations != null) {
      getCache().relations.clear();
    }
    // clearCache();
    // FIXME
    invalidateCache(true);
    // getCache().clear();
  }

  /**
   * @see magellan.library.Unit#reparseOrders()
   */
  public void reparseOrders() {
    if (!ordersAreNull()) {
      for (int i = 0; i < getOrdersObject().size(); ++i) {
        getOrdersObject().set(i, createOrder(ordersObject.get(i).getText()));
      }
    }
  }

  /**
   * @see magellan.library.Unit#setNewRegion(magellan.library.CoordinateID)
   */
  public void setNewRegion(CoordinateID destination) {
    getCache().destination = destination;
  }

  /**
   * @see magellan.library.Unit#getNewRegion()
   */
  public CoordinateID getNewRegion() {
    if (getCache().destination == null)
      if (getRegion() != null)
        return getCache().destination = getRegion().getCoordinate();
      else
        return null;
    return getCache().destination;
  }

  /**
   * @see magellan.library.Unit#detach()
   */
  public void detach() {
    if (region != null) {
      region.removeUnit(getID());
    }
    if (faction != null) {
      faction.removeUnit(getID());
    }
    setGroup(null);
    setShip(null);
    setBuilding(null);
    // if (group != null) {
    // group.removeUnit(getID());
    // }
    // if (building != null) {
    // building.removeUnit(getID());
    // }
    // if (ship != null) {
    // ship.removeUnit(getID());
    // }
    clearCache();
  }

  /**
   * @see magellan.library.Unit#isDetailsKnown()
   */
  public boolean isDetailsKnown() {
    // this appears to be the only attribute that is always and only defined for your own units
    return getCombatStatus() != -1;
  }

}
