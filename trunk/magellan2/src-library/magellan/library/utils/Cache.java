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

package magellan.library.utils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.ID;
import magellan.library.Item;
import magellan.library.Skill;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.relation.UnitRelation;
import magellan.library.utils.guiwrapper.CacheableOrderEditor;

/**
 * A class for caching data that is time consuming to compute or wasteful to allocate but frequently
 * needed. Objects of this type are available in units and in all UnitContainer subclasses. If
 * fields are added, please comment on where the field is used and with wich scope!
 */
public class Cache {
  private Collection<CacheHandler> handlers = null;

  /* used in Unit and UnitContainer for modified name */
  public String modifiedName = null;

  /** used in swing.completion.* classes per unit */
  public CacheableOrderEditor orderEditor = null;

  // used in swing.map.RegionImageCellRenderer per region
  // public int fogOfWar = -1;

  /** used in Unit and UnitContainer for relations between or to units */
  public List<UnitRelation> relations = null;

  /**
   * used in Unit for skills after person transfers and recruiting
   * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! IMPORTANT: do not modify this
   * thing (except for assignments) since it may point to the Unit.skills map!!
   * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
   */
  public Map<StringID, Skill> modifiedSkills = null;

  /** DOCUMENT-ME */
  public Map<StringID, Item> modifiedItems = null;

  /** @deprecated currently unused */
  @Deprecated
  public int unitWeight = -1;

  /** @deprecated currently unused */
  @Deprecated
  public int modifiedUnitWeight = -1;

  /** DOCUMENT-ME */
  public int modifiedPersons = -1;

  /**
   * The expected combat status at beginning next turn acording to actual orders If cache is not
   * calculated, status is -2
   */
  public int modifiedCombatStatus = -2;

  public int modifiedGuard = -1;

  /**
   * The expected unaided - status at beginning next turn acording to actual orders cache status is
   * detected with modifiedUnaidedValidated
   */
  public boolean modifiedUnaided = false;

  /**
   * just a checker, if modifiedUnaided was already validated
   */
  public boolean modifiedUnaidedValidated = false;

  /**
   * used in UnitContainer !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! IMPORTANT:
   * do not modify this thing (except for assignments) since it may point to the UnitContainer.units
   * map!! !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
   */
  public Map<EntityID, Unit> modifiedContainerUnits = null;

  /**
   * Used in Region for creating a list of Items of priviliged factions in the region
   * 
   * @deprecated replaced by method in Units
   */
  @Deprecated
  public Map<ID, Item> regionItems = null;

  /**
   * Used in Region for creating a list of Items of all factions in the region
   * 
   * @deprecated replaced by method in Units
   */
  @Deprecated
  public Map<ID, Item> allRegionItems = null;

  /**
   * Used in Unit (FIXME(pavkovic): right now used in PathCellRenderer) to store movement
   * information extracted from travelThru (-Ship) and faction messages
   */
  public List<CoordinateID> movementPath = null;

  /** DOCUMENT-ME */
  public Boolean movementPathIsPassive = null;

  public Cache() {
    super();
  }

  /**
   * Register a CacheHandler.
   * 
   * @param h
   */
  public void addHandler(CacheHandler h) {
    if (handlers == null) {
      handlers = new LinkedList<CacheHandler>();
    }

    handlers.add(h);
  }

  /**
   * Un-register a CacheHandler.
   * 
   * @param h
   */
  public void removeHandler(CacheHandler h) {
    if (handlers != null) {
      handlers.remove(h);
    }
  }

  /**
   * Clears all members of this Cache.
   */
  public void clear() {
    if (handlers != null) {
      for (CacheHandler h : handlers) {
        h.clearCache(this);
      }
    }

    orderEditor = null;

    // fogOfWar = -1;
    if (relations != null) {
      relations.clear();
      relations = null;
    }

    modifiedSkills = null;

    if (modifiedItems != null) {
      modifiedItems.clear();
      modifiedItems = null;
    }

    unitWeight = -1;
    modifiedUnitWeight = -1;
    modifiedPersons = -1;
    modifiedContainerUnits = null;
    modifiedCombatStatus = -2;
    modifiedUnaidedValidated = false;
    modifiedGuard = -1;

    if (regionItems != null) {
      regionItems.clear();
      regionItems = null;
    }

    if (allRegionItems != null) {
      allRegionItems.clear();
      allRegionItems = null;
    }

    movementPath = null;
    movementPathIsPassive = null;
  }
}
