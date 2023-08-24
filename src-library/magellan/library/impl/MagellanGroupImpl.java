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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import magellan.library.Alliance;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Group;
import magellan.library.ID;
import magellan.library.IntegerID;
import magellan.library.Unit;
import magellan.library.utils.CollectionFactory;

/**
 * A class representing a group of units within a faction.
 */
public class MagellanGroupImpl extends MagellanNamedImpl implements Group {
  private Faction faction = null;
  private Map<EntityID, Alliance> allies = CollectionFactory
      .<EntityID, Alliance> createSyncOrderedMap();

  private static Map<String, String> tagMap = null; // Map for external tags
  /** Contains all attributes */
  private Map<String, String> attributes = new LinkedHashMap<String, String>();

  /**
   * Create a new <kbd>Group</kbd> object.
   * 
   * @param id the id of this group.
   * @param data the game data this group belongs to.
   */
  public MagellanGroupImpl(IntegerID id, GameData data) {
    this(id, data, null, null);
  }

  /**
   * Create a new <kbd>Group</kbd> object.
   * 
   * @param id the id of this group.
   * @param data the game data this group belongs to.
   * @param name the name of this group.
   */
  public MagellanGroupImpl(IntegerID id, GameData data, String name) {
    this(id, data, name, null);
  }

  /**
   * Create a new <kbd>Group</kbd> object.
   * 
   * @param id the id of this group.
   * @param data the game data this group belongs to.
   * @param name the name of this group.
   * @param faction the faction this group belongs to.
   */
  public MagellanGroupImpl(IntegerID id, GameData data, String name, Faction faction) {
    super(id);
    // this.data = data;
    setName(name);
    this.faction = faction;
  }

  /**
   * Set the faction this group belongs to.
   */
  public void setFaction(Faction f) {
    faction = f;
  }

  /**
   * Get the faction this group belongs to.
   */
  public Faction getFaction() {
    return faction;
  }

  /**
   * The alliances specific to this group. The map returned by this function contains <kbd>ID</kbd>
   * objects as keys with the id of the faction that alliance references. The values are instances
   * of class <kbd>Alliance</kbd>. The return value is never null.
   */
  public Map<EntityID, Alliance> allies() {
    return allies;
  }

  /** A group dependent prefix to be prepended to this faction's race name. */
  private String raceNamePrefix = null;

  /**
   * Sets the group dependent prefix for the race name.
   */
  public void setRaceNamePrefix(String prefix) {
    raceNamePrefix = prefix;
  }

  /**
   * Returns the group dependent prefix for the race name.
   */
  public String getRaceNamePrefix() {
    return raceNamePrefix;
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

  /** All units that are in this container. */
  private Map<ID, Unit> units = null;

  /** Provides a collection view of the unit map. */
  private Collection<Unit> unitCollection = null;

  /**
   * Returns an unmodifiable collection of all the units in this container.
   */
  public Collection<Unit> units() {
    if (units == null)
      return Collections.emptyList();

    if (unitCollection == null) {
      if (units != null && units.values() != null) {
        unitCollection = Collections.unmodifiableCollection(units.values());
      }
    }

    return unitCollection;
  }

  /**
   * Retrieve a unit in this container by id.
   */
  public Unit getUnit(ID key) {
    if (units != null)
      return units.get(key);
    else
      return null;
  }

  /**
   * Adds a unit to this container. This method should only be invoked by Unit.setXXX() methods.
   */
  public void addUnit(Unit u) {
    if (units == null) {
      units = new LinkedHashMap<ID, Unit>();

      /* enforce the creation of a new collection view */
      unitCollection = null;
    }

    units.put(u.getID(), u);
  }

  /**
   * Removes a unit from this container. This method should only be invoked by Unit.setXXX()
   * methods.
   */
  public Unit removeUnit(ID key) {
    if (units != null) {
      Unit u = units.remove(key);

      if (units.isEmpty()) {
        units = null;
      }

      return u;
    } else
      return null;
  }

  /**
   * Returns a String representation of this group object.
   */
  @Override
  public String toString() {
    // pavkovic 2004.01.04: for a Group id is more a technical connection so we dont
    // want to see it.
    // return name + " (" + id + ")";
    return getName();
  }

  // EXTERNAL TAG METHODS

  /*************************************************************************************
   * Taggable methods
   */

  /**
   * @see magellan.library.utils.Taggable#deleteAllTags()
   */
  public void deleteAllTags() {
    MagellanGroupImpl.tagMap = null;
  }

  /**
   * @see magellan.library.utils.Taggable#putTag(java.lang.String, java.lang.String)
   */
  public String putTag(String tag, String value) {
    if (MagellanGroupImpl.tagMap == null) {
      MagellanGroupImpl.tagMap = new LinkedHashMap<String, String>(1);
    }

    return MagellanGroupImpl.tagMap.put(tag, value);
  }

  /**
   * @see magellan.library.utils.Taggable#getTag(java.lang.String)
   */
  public String getTag(String tag) {
    if (MagellanGroupImpl.tagMap == null)
      return null;

    return MagellanGroupImpl.tagMap.get(tag);
  }

  /**
   * @see magellan.library.utils.Taggable#removeTag(java.lang.String)
   */
  public String removeTag(String tag) {
    if (MagellanGroupImpl.tagMap == null)
      return null;

    return MagellanGroupImpl.tagMap.remove(tag);
  }

  /**
   * @see magellan.library.utils.Taggable#containsTag(java.lang.String)
   */
  public boolean containsTag(String tag) {
    if (MagellanGroupImpl.tagMap == null)
      return false;

    return MagellanGroupImpl.tagMap.containsKey(tag);
  }

  /**
   * @see magellan.library.utils.Taggable#getTagMap()
   */
  public Map<String, String> getTagMap() {
    if (MagellanGroupImpl.tagMap == null) {
      MagellanGroupImpl.tagMap = new LinkedHashMap<String, String>(1);
    }

    return Collections.unmodifiableMap(MagellanGroupImpl.tagMap);
  }

  /**
   * @see magellan.library.utils.Taggable#hasTags()
   */
  public boolean hasTags() {
    return (MagellanGroupImpl.tagMap != null) && !MagellanGroupImpl.tagMap.isEmpty();
  }

  /**
   * @see magellan.library.Group#setAllies(java.util.Map)
   */
  public void setAllies(Map<EntityID, Alliance> allies) {
    this.allies = allies;
  }

  /**
   * @see magellan.library.Addeable#addAttribute(java.lang.String, java.lang.String)
   */
  public void addAttribute(String key, String value) {
    attributes.put(key, value);
  }

  /**
   * @see magellan.library.Addeable#containsAttribute(java.lang.String)
   */
  public boolean containsAttribute(String key) {
    return attributes.containsKey(key);
  }

  /**
   * @see magellan.library.Addeable#getAttribute(java.lang.String)
   */
  public String getAttribute(String key) {
    return attributes.get(key);
  }

  /**
   * @see magellan.library.Addeable#getAttributeKeys()
   */
  public List<String> getAttributeKeys() {
    return new ArrayList<String>(attributes.keySet());
  }

  /**
   * @see magellan.library.Addeable#getAttributeSize()
   */
  public int getAttributeSize() {
    return attributes.size();
  }

  /**
   * @see magellan.library.Identifiable#getID()
   */
  @Override
  public IntegerID getID() {
    return (IntegerID) super.getID();
  }
}
