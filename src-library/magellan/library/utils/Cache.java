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
import magellan.library.Item;
import magellan.library.Skill;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.relation.UnitRelation;
import magellan.library.utils.guiwrapper.CacheableOrderEditor;

/**
 * A class for caching data that is time consuming to compute or wasteful to allocate but frequently
 * needed. Objects of this type are available in units and in all UnitContainer subclasses. If
 * fields are added, please comment on where the field is used and with wich scope!
 */
public class Cache {
  private Collection<CacheHandler> handlers = null;

  // units and containers

  /** used in Unit and UnitContainer for modified name */
  public String modifiedName = null;

  /** used in Unit and UnitContainer for relations between or to units */
  public List<UnitRelation> relations = null;

  // units

  /** used in swing.completion.* classes per unit */
  public CacheableOrderEditor orderEditor = null;

  /**
   * used in Unit for skills after person transfers and recruiting
   * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! IMPORTANT: do not modify this
   * thing (except for assignments) since it may point to the Unit.skills map!!
   * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
   */
  public Map<StringID, Skill> modifiedSkills = null;

  /** unit items */
  public Map<StringID, Item> modifiedItems = null;

  /** number of persons after orders */
  public int modifiedPersons = -1;

  /** guard status after orders */
  public int modifiedGuard = -1;

  /**
   * The expected combat status at beginning next turn according to actual orders If cache is not
   * calculated, status is EresseaConstants.CS_INIT;
   */
  public int modifiedCombatStatus = EresseaConstants.CS_INIT;

  /**
   * The expected unaided - status at beginning next turn according to actual orders cache status is
   * detected with modifiedUnaidedValidated
   */
  public boolean modifiedUnaided = false;

  /**
   * just a checker, if modifiedUnaided was already validated
   */
  public boolean modifiedUnaidedValidated = false;

  /**
   * Used in Unit (FIXME(pavkovic): right now used in PathCellRenderer) to store movement
   * information extracted from travelThru (-Ship) and faction messages
   */
  public List<CoordinateID> movementPath = null;

  public Boolean movementPathIsPassive = null;

  public CoordinateID destination;

  /**     */
  public UnitContainer modifiedContainer;

  // containers

  /** used for amount of ships in a fleet */
  public int modifiedAmount = -1;

  /** used for size ships in a fleet */
  public int modifiedSize = -1;

  /**
   * used in UnitContainer !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! IMPORTANT:
   * do not modify this thing (except for assignments) since it may point to the UnitContainer.units
   * map!! !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
   */
  public Map<EntityID, Unit> modifiedContainerUnits = null;

  /**  */
  public Unit modifiedOwner;

  /** */
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

    modifiedPersons = -1;
    modifiedAmount = -1;
    modifiedSize = -1;
    if (modifiedContainerUnits != null) {
      modifiedContainerUnits.clear();
    }
    modifiedContainerUnits = null;
    modifiedCombatStatus = EresseaConstants.CS_INIT;
    modifiedUnaidedValidated = false;
    modifiedGuard = -1;

    movementPath = null;
    movementPathIsPassive = null;
    destination = null;

    modifiedOwner = null;
    modifiedContainer = null;
  }
}
