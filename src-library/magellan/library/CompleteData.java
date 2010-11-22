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

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import magellan.library.rules.MessageType;
import magellan.library.tasks.GameDataInspector;
import magellan.library.tasks.Problem.Severity;
import magellan.library.tasks.ProblemFactory;
import magellan.library.utils.OrderedHashtable;
import magellan.library.utils.Resources;
import magellan.library.utils.Translations;

/**
 * An implementation of the <tt>GameData</tt> supporting all of the attributes defined there. No
 * maps are defined as <tt>null</tt>.
 * 
 * @see magellan.library.GameData
 */
public class CompleteData extends GameData {

  protected Map<CoordinateID, Region> regions = new OrderedHashtable<CoordinateID, Region>();
  protected Map<UnitID, Unit> units = new OrderedHashtable<UnitID, Unit>();
  protected Map<UnitID, TempUnit> tempUnits = new OrderedHashtable<UnitID, TempUnit>();
  protected Map<EntityID, Faction> factions = new OrderedHashtable<EntityID, Faction>();
  protected Map<EntityID, Ship> ships = new OrderedHashtable<EntityID, Ship>();
  protected Map<EntityID, Building> buildings = new OrderedHashtable<EntityID, Building>();
  protected Map<IntegerID, Island> islands = new OrderedHashtable<IntegerID, Island>();
  protected Map<IntegerID, MessageType> msgTypes = new OrderedHashtable<IntegerID, MessageType>();
  protected Map<StringID, Spell> spells = new OrderedHashtable<StringID, Spell>();
  protected Map<IntegerID, Potion> potions = new OrderedHashtable<IntegerID, Potion>();
  protected Map<IntegerID, HotSpot> hotSpots = new OrderedHashtable<IntegerID, HotSpot>();
  protected Translations translations = new Translations();
  protected Locale locale = null;
  protected Map<CoordinateID, Region> selectedRegions = new TreeMap<CoordinateID, Region>();

  /**
   * @see magellan.library.GameData#setIslands(java.util.Map)
   */
  @Override
  public void setIslands(Map<IntegerID, Island> islands) {
    if (islands == null) {
      this.islands = new OrderedHashtable<IntegerID, Island>();
    } else {
      this.islands = new OrderedHashtable<IntegerID, Island>(islands);
    }
  }

  /**
   * @see magellan.library.GameData#addTempUnit(magellan.library.TempUnit)
   */
  @Override
  public void addTempUnit(TempUnit t) {
    TempUnit old = tempUnits.put(t.getID(), t);
    if (old != null) {
      addError(ProblemFactory.createProblem(Severity.ERROR,
          GameDataInspector.GameDataProblemTypes.DUPLICATEUNITID.type, t.getRegion(), t, null, t,
          null, Resources.get("gamedata.problem.duplicateunit.message", t, old), -1));
    }

  }

  /**
   * Returns a collection of the coordinates of selected regions.
   * 
   * @see magellan.library.GameData#getSelectedRegionCoordinates()
   */
  @Override
  public Map<CoordinateID, Region> getSelectedRegionCoordinates() {
    return Collections.unmodifiableMap(selectedRegions);
  }

  /**
   * @see magellan.library.GameData#addSelectedRegionCoordinate(magellan.library.Region)
   */
  @Override
  public void addSelectedRegionCoordinate(Region region) {
    selectedRegions.put(region.getCoordinate(), region);
  }

  /**
   * set a collection of selected regions.
   * 
   * @param regions the map of coordinates of selected regions
   */
  @Override
  public void setSelectedRegionCoordinates(Map<CoordinateID, Region> regions) {
    selectedRegions = new TreeMap<CoordinateID, Region>();
    if (regions != null) {
      selectedRegions.putAll(regions);
    }
  }

  @Override
  public void addHotSpot(HotSpot h) {
    hotSpots.put(h.getID(), h);
  }

  /**
   * DOCUMENT-ME
   */
  @Override
  public Translations translations() {
    return translations;
  }

  /**
   * DOCUMENT-ME
   */
  @Override
  public void setLocale(Locale l) {
    locale = l;
  }

  /**
   * DOCUMENT-ME
   */
  @Override
  public Locale getLocale() {
    return locale;
  }

  /**
   * Creates a new, empty CompleteData object.
   * 
   * @param rules Valid rules for the game
   */
  public CompleteData(Rules rules) {
    super(rules);
  }

  /**
   * Creates a new CompleteData object.
   * 
   * @param rules Valid rules for the game
   * @param name The game name (like "Eressea", "E3", ...)
   */
  public CompleteData(Rules rules, String name) {
    super(rules, name);
  }

  /**
   * @see magellan.library.GameData#estimateSize()
   */
  @Override
  public long estimateSize() {
    return regionView().size() * 1000 + units.size() * 1000;
  }

  /**
   * @see magellan.library.GameData#buildingView()
   */
  @Override
  protected Map<EntityID, Building> buildingView() {
    return buildings;
  }

  /**
   * @see magellan.library.GameData#factionView()
   */
  @Override
  protected Map<EntityID, Faction> factionView() {
    return factions;
  }

  /**
   * @see magellan.library.GameData#hotSpotView()
   */
  @Override
  protected Map<IntegerID, HotSpot> hotSpotView() {
    return hotSpots;
  }

  /**
   * @see magellan.library.GameData#islandView()
   */
  @Override
  protected Map<IntegerID, Island> islandView() {
    return islands;
  }

  /**
   * @see magellan.library.GameData#msgTypeView()
   */
  @Override
  protected Map<IntegerID, MessageType> msgTypeView() {
    return msgTypes;
  }

  /**
   * @see magellan.library.GameData#potionView()
   */
  @Override
  protected Map<IntegerID, Potion> potionView() {
    return potions;
  }

  /**
   * @see magellan.library.GameData#regionView()
   */
  @Override
  protected Map<CoordinateID, Region> regionView() {
    return regions;
  }

  /**
   * @see magellan.library.GameData#shipView()
   */
  @Override
  protected Map<EntityID, Ship> shipView() {
    return ships;
  }

  /**
   * @see magellan.library.GameData#spellView()
   */
  @Override
  protected Map<StringID, Spell> spellView() {
    return spells;
  }

  /**
   * @see magellan.library.GameData#tempUnitView()
   */
  @Override
  protected Map<UnitID, TempUnit> tempUnitView() {
    return tempUnits;
  }

  /**
   * @see magellan.library.GameData#unitView()
   */
  @Override
  protected Map<UnitID, Unit> unitView() {
    return units;
  }

}
