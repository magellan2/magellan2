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

package magellan.library;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import magellan.library.gamebinding.GameSpecificStuff;
import magellan.library.rules.AllianceCategory;
import magellan.library.rules.BuildingType;
import magellan.library.rules.CastleType;
import magellan.library.rules.FactionType;
import magellan.library.rules.ItemCategory;
import magellan.library.rules.ItemType;
import magellan.library.rules.ObjectType;
import magellan.library.rules.OptionCategory;
import magellan.library.rules.OrderType;
import magellan.library.rules.Race;
import magellan.library.rules.RegionType;
import magellan.library.rules.ShipType;
import magellan.library.rules.SkillCategory;
import magellan.library.rules.SkillType;

/**
 * <p>
 * A class summarizing the static information about a game system (a set of rules).
 * </p>
 * <p>
 * If internationalization is a concern, implementing sub-classes should ensure that the
 * access-methods to the various collections (<tt>getXXX()</tt>) return their objects not only by
 * their (usually language-independent) id but also by their (language-dependent) name as it may be
 * supplied by the user.
 * </p>
 * <p>
 * Iterators should enumerate items in the order in which they were inserted
 * </p>
 * <p>
 * If necessary, subclasses could also provide additional access methods to distinguish between an
 * access by id or name.
 * </p>
 * <p>
 * The methods called getXXX(ID id, boolean add) adds and returns a new Object.
 * </p>
 */
public interface Rules {
  /**
   * Returns the region type with given id. If there is no such skill type and
   * <code>add == true</code>, a new skill type is added and returns. Otherwise, <code>null</code> is
   * returned.
   */
  public RegionType getRegionType(StringID id, boolean add);

  /**
   * Shorthand for getRegionType(id, false).
   */
  public RegionType getRegionType(StringID id);

  /**
   * Returns an iterator over all region types.
   */
  public Iterator<RegionType> getRegionTypeIterator();

  /**
   * Returns a collection of all region types.
   */
  public Collection<RegionType> getRegionTypes();

  /**
   * get RegionType by (possibly localized) name.
   */
  public RegionType getRegionType(String id, boolean add);

  /**
   * Shorthand for getRegionType(id, false);
   */
  public RegionType getRegionType(String id);

  /**
   * Shorthand for getRace(id, false)
   */
  public Race getRace(StringID id);

  /**
   * Returns the race with given id. If there is no such skill type and <code>add == true</code>, a
   * new skill type is added and returns. Otherwise, <code>null</code> is returned.
   */
  public Race getRace(StringID id, boolean add);

  /**
   * Returns an iterator over all races.
   */
  public Iterator<Race> getRaceIterator();

  /**
   * Returns an iterator over all races.
   */
  public Collection<Race> getRaces();

  /**
   * get Race by (possibly localized) name
   */
  public Race getRace(String id, boolean add);

  /**
   * shorthand for getRace(id, false)
   */
  public Race getRace(String id);

  /**
   * Shorthand for getShipType(id, false)
   */
  public ShipType getShipType(StringID id);

  /**
   * Returns the ship type with given id. If there is no such skill type and <code>add == true</code>,
   * a new skill type is added and returns. Otherwise, <code>null</code> is returned.
   */
  public ShipType getShipType(StringID id, boolean add);

  /**
   * Returns an iterator over all ship types.
   */
  public Iterator<ShipType> getShipTypeIterator();

  /**
   * Returns an collection of all ship types.
   */
  public Collection<ShipType> getShipTypes();

  /**
   * get ShipType by (possibly localized) name
   */
  public ShipType getShipType(String id, boolean add);

  /**
   * Shorthand for getShipType(id, false)
   */
  public ShipType getShipType(String id);

  /**
   * Shorthand for getBuildingType(id, false)
   */
  public BuildingType getBuildingType(StringID id);

  /**
   * Returns the building type with given id. If there is no such skill type and
   * <code>add == true</code>, a new skill type is added and returns. Otherwise, <code>null</code> is
   * returned.
   */
  public BuildingType getBuildingType(StringID id, boolean add);

  /**
   * Returns an iterator over all building types (including CastleTypes).
   */
  public Iterator<BuildingType> getBuildingTypeIterator();

  /**
   * Returns a collection of all building types (including CastleTypes).
   */
  public Collection<BuildingType> getBuildingTypes();

  /**
   * get BuildingType by (possibly localized) name
   */
  public BuildingType getBuildingType(String id, boolean add);

  /**
   * Shorthand for getBuildingType(id, false)
   */
  public BuildingType getBuildingType(String id);

  /**
   * Shorthand for getCastleType(id, false)
   */
  public CastleType getCastleType(StringID id);

  /**
   * Returns the castle type with given id. If there is no such skill type and
   * <code>add == true</code>, a new skill type is added and returns. Otherwise, <code>null</code> is
   * returned.
   */
  public CastleType getCastleType(StringID id, boolean add);

  /**
   * Returns an iterator of all castle types.
   */
  public Iterator<CastleType> getCastleTypeIterator();

  /**
   * Returns a collection of all castle types.
   */
  public Collection<CastleType> getCastleTypes();

  /**
   * get CastleType by (possibly localized) name
   */
  public CastleType getCastleType(String id, boolean add);

  /**
   * Shorthand for getCastleType(id, false)
   */
  public CastleType getCastleType(String id);

  /**
   * Shorthand for getItemType(id, false)
   */
  public ItemType getItemType(StringID id);

  /**
   * Returns the item type with given id. If there is no such skill type and <code>add == true</code>,
   * a new skill type is added and returns. Otherwise, <code>null</code> is returned.
   */
  public ItemType getItemType(StringID id, boolean add);

  /**
   * Returns an iterator over all item types.
   */
  public Iterator<ItemType> getItemTypeIterator();

  /**
   * Returns a collection of all item types.
   */
  public Collection<ItemType> getItemTypes();

  /**
   * get ItemType by (possibly localized) name
   *
   * @param id An id, like "Silber".
   * @param add If this is <code>true</code>, a type will be added if it does not exist, yet.
   * @return The item type corresponding to the id or <code>null</code> if the type is unknown or
   *         <code>id</code> is <code>null</code> or empty.
   */
  public ItemType getItemType(String id, boolean add);

  /**
   * Shorthand for getItemType(id, false).
   *
   * @param id An id, like "Silber".
   * @return The item type corresponding to the id or <code>null</code> if the type is unknown.
   */
  public ItemType getItemType(String id);

  /**
   * Returns the skill type with the given id.
   *
   * @return the skill type with the given id or <code>null</code> if there is no such skill type.
   */
  public SkillType getSkillType(StringID id);

  /**
   * Returns the skill type with given id. If there is no such skill type and
   * <code>add == true</code>, a new skill type is added and returns. Otherwise, <code>null</code> is
   * returned.
   */
  public SkillType getSkillType(StringID id, boolean add);

  /**
   * Returns an iterator over all skill types.
   */
  public Iterator<SkillType> getSkillTypeIterator();

  /**
   * Returns a collection of all skill types.
   */
  public Collection<SkillType> getSkillTypes();

  /**
   * get SkillType by (possibly localized) name
   */
  public SkillType getSkillType(String id, boolean add);

  /**
   * Shorthand for getSkillType(id, false)
   */
  public SkillType getSkillType(String id);

  /**
   * Shorthand for getItemCategory(id, false)
   */
  public ItemCategory getItemCategory(StringID id);

  /**
   * Returns the item category with given id. If there is no such skill type and
   * <code>add == true</code>, a new skill type is added and returns. Otherwise, <code>null</code> is
   * returned.
   */
  public ItemCategory getItemCategory(StringID id, boolean add);

  /**
   * Returns an iterator over all item categories.
   */
  public Iterator<ItemCategory> getItemCategoryIterator();

  /**
   * Returns a collection of all item categories.
   */
  public Collection<ItemCategory> getItemCategories();

  /**
   * get ItemCategory by (possibly localized) name
   */
  public ItemCategory getItemCategory(String id, boolean add);

  /**
   * Shorthand for getItemCategory(id, false)
   */
  public ItemCategory getItemCategory(String id);

  /**
   * Shorthand for getSkillCategory(id, false)
   */
  public SkillCategory getSkillCategory(StringID id);

  /**
   * Returns the skill category with given id. If there is no such skill type and
   * <code>add == true</code>, a new skill type is added and returns. Otherwise, <code>null</code> is
   * returned.
   */
  public SkillCategory getSkillCategory(StringID id, boolean add);

  /**
   * Returns an iterator over all skill categories.
   */
  public Iterator<SkillCategory> getSkillCategoryIterator();

  /**
   * Returns a collection of all skill categories.
   */
  public Collection<SkillCategory> getSkillCategories();

  /**
   * get SkillCategory by (possibly localized) name
   */
  public SkillCategory getSkillCategory(String id, boolean add);

  /**
   * Shorthand for getSkillCategory(id, false)
   */
  public SkillCategory getSkillCategory(String id);

  /**
   * Shorthand for getOptionCategory(id, false)
   */
  public OptionCategory getOptionCategory(StringID id);

  /**
   * Returns the option category with given id. If there is no such skill type and
   * <code>add == true</code>, a new skill type is added and returns. Otherwise, <code>null</code> is
   * returned.
   */
  public OptionCategory getOptionCategory(StringID id, boolean add);

  /**
   * Returns an iterator over all option categories.
   */
  public Iterator<OptionCategory> getOptionCategoryIterator();

  /**
   * Returns a collection of all option categories.
   */
  public Collection<OptionCategory> getOptionCategories();

  /**
   * get OptionCategory by (possibly localized) name
   */
  public OptionCategory getOptionCategory(String id, boolean add);

  /**
   * Shorthand for getOptionCategory(id, false)
   */
  public OptionCategory getOptionCategory(String id);

  /**
   * Returns the alliance category with the given id.
   */
  public AllianceCategory getAllianceCategory(StringID id);

  /**
   * Returns the alliance category with the given id. If add is true and the id is not available, it
   * will be added
   */
  public AllianceCategory getAllianceCategory(StringID id, boolean add);

  /**
   * Returns a list of all possible alliance categories.
   */
  public Iterator<AllianceCategory> getAllianceCategoryIterator();

  /**
   * Returns a list of all possible alliance categories.
   */
  public Collection<AllianceCategory> getAllianceCategories();

  /**
   * get AllianceCategory by (possibly localized) name
   */
  public AllianceCategory getAllianceCategory(String id, boolean add);

  /**
   * get AllianceCategory by (possibly localized) name
   */
  public AllianceCategory getAllianceCategory(String id);

  /**
   * Changes the name of an object identified by the given old name.
   *
   * @return the modified object type or null, if no object type is registered with the specified id.
   */
  public ObjectType changeName(String from, String to);

  /**
   * Returns the GameSpecificStuff object for the name specified by setGameSpecificClassName.
   *
   * @throws IOException If rules are not readable
   */
  public void setGameSpecificStuffClassName(String className) throws IOException;

  /**
   * Returns the GameSpecificStuff object for the name specified by setGameSpecificClassName.
   */
  public GameSpecificStuff getGameSpecificStuff();

  /**
   * Gets the game specific string with which order files should start.
   *
   * @return the game specific line with which order files should start, not including line break
   */
  public String getOrderfileStartingString();

  /**
   * Sets the game specific string with which order files should start.
   *
   * @param startingString
   */
  public void setOrderfileStartingString(String startingString);

  /**
   * Shorthand for getOrder(StringID.create(id)).
   *
   * @see #getOrder(StringID)
   */
  public OrderType getOrder(String id);

  /**
   * Returns the order type with the given id.
   */
  public OrderType getOrder(StringID id);

  /**
   * Returns the order type with the given id. If add is true and the id is not available, it will be
   * added.
   */
  public OrderType getOrder(StringID id, boolean add);

  /**
   * Returns all orders.
   */
  public Collection<OrderType> getOrders();

  /**
   * Sets the game name.
   */
  public void setGameName(String name);

  /**
   * Returns the game number.
   */
  public String getGameName();

  /**
   * Returns the faction type with the given id. If add is true and the id is not available, it will be
   * added.
   */
  public FactionType getFaction(StringID id, boolean add);

  /**
   * Returns the faction type with the given id.
   */
  public FactionType getFaction(StringID id);

  /**
   * Return all faction types.
   */
  public Collection<FactionType> getFactions();
}
