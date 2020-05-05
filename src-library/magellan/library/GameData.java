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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import magellan.library.Region.Visibility;
import magellan.library.completion.OrderParser;
import magellan.library.event.UnitChangeEvent;
import magellan.library.event.UnitChangeListener;
import magellan.library.gamebinding.GameSpecificRules;
import magellan.library.gamebinding.GameSpecificStuff;
import magellan.library.gamebinding.MapMergeEvaluator;
import magellan.library.gamebinding.MapMetric;
import magellan.library.io.cr.Loader;
import magellan.library.io.file.FileType;
import magellan.library.rules.BuildingType;
import magellan.library.rules.Date;
import magellan.library.rules.EresseaDate;
import magellan.library.rules.MessageType;
import magellan.library.rules.Race;
import magellan.library.rules.RegionType;
import magellan.library.rules.SkillType;
import magellan.library.tasks.GameDataInspector;
import magellan.library.tasks.Problem;
import magellan.library.tasks.Problem.Severity;
import magellan.library.tasks.ProblemFactory;
import magellan.library.utils.CollectionFactory;
import magellan.library.utils.Direction;
import magellan.library.utils.IDBaseConverter;
import magellan.library.utils.Locales;
import magellan.library.utils.MagellanFactory;
import magellan.library.utils.MemoryManagment;
import magellan.library.utils.Regions;
import magellan.library.utils.ReportMerger;
import magellan.library.utils.ReportMerger.AssignData;
import magellan.library.utils.Resources;
import magellan.library.utils.TranslationType;
import magellan.library.utils.Translations;
import magellan.library.utils.UserInterface;
import magellan.library.utils.comparator.IDComparator;
import magellan.library.utils.comparator.NameComparator;
import magellan.library.utils.logging.Logger;
import magellan.library.utils.mapping.LevelRelation;
import magellan.library.utils.transformation.IdentityTransformer;
import magellan.library.utils.transformation.ReportTransformer;
import magellan.library.utils.transformation.TwoLevelTransformer;

/**
 * This is the central class for collecting all the data representing one computer report.
 * <p>
 * The maps units, regions and so on are declared as abstract methods and the getX and addX provide
 * access to them. This allows for subclasses that implicitly represent only a certain part of the
 * game data by declaring certain maps as <tt>null</tt> and returning <tt>null</tt> on the
 * corresponding getX() methods. This concept has so far not been applied and you usually operate on
 * the <tt>CompleteData</tt> subclass.
 * </p>
 */
public abstract class GameData implements Cloneable, Addeable {

  private static final Logger log = Logger.getInstance(GameData.class);

  /**
   * Game specific and usually fixed data (like races etc.).
   * 
   * @deprecated use {@link #getRules()}
   */
  @Deprecated
  public final Rules rules;

  private final MapMetric mapMetric;

  private final GameSpecificStuff gameSpecific;

  private final GameSpecificRules gameRules;

  /** The name of the game. */
  public String gameName;

  /**
   * The base (radix) in which ids are interpreted. The default value is 10. Note that all internal
   * and cr representation is always decimal.
   */
  public int base = 10;

  /**
   * version number of computer report (CR)
   */
  public int version = 66;

  /**
   * Indicates whether in this report skill points are to be expected or whether they are
   * meaningful, respectively.
   */
  public boolean noSkillPoints = false;

  public List<Problem> errors;

  private OrderParser parser;

  /** encoding */
  protected String encoding = FileType.DEFAULT_ENCODING.toString();

  private EntityID ownerFaction;

  private Map<EntityID, Map<Integer, CoordinateID>> coordinateTranslations =
      new LinkedHashMap<EntityID, Map<Integer, CoordinateID>>();

  private Map<Integer, Map<Integer, LevelRelation>> levelRelations =
      new LinkedHashMap<Integer, Map<Integer, LevelRelation>>();

  private Map<ID, AllianceGroup> alliancegroups;

  protected Map<CoordinateID, Region> wrappers = new LinkedHashMap<CoordinateID, Region>();
  protected Map<Region, Region> originals = new LinkedHashMap<Region, Region>();

  protected Map<CoordinateID, Region> voids = new LinkedHashMap<CoordinateID, Region>();

  /**
   * The current TempUnit-ID. This means, if a new TempUnit is created, it's suggested ID is usually
   * curTempID and if this suggestion is accepted by the user (which means, that a TempUnit with
   * this id was created) curTempID is increased. A value of -1 indicates, that the variable is
   * uninitialized and a value of 0 that the old system shall be used (which means, that the
   * suggested temp id shall be calculated out of the id of the parent unit of the tempunit).
   */
  protected int curTempID = -1;

  /** Contains all attributes */
  private Map<String, String> attributes = new LinkedHashMap<String, String>();

  /**
   * The current file attached to the game data. If it is null, the save as dialog shall be opened.
   */
  private FileType filetype = null;

  /**
   * The 'round' this game data belongs to. Note that this imposes a restriction on how fine-grained
   * date information can be applied to game data or certain parts of it. This will probably have to
   * be changed the one or the other way.
   */
  protected Date date = new EresseaDate(0);

  /**
   * Contains the date of the report (it's not the date of the report).
   */
  protected long timestamp = System.currentTimeMillis() / 1000;

  /** The 'mail' connection this game data belongs to. This may be null */
  public String mailTo;

  /** The 'mail' subject for this game data. This may be null */
  public String mailSubject;

  /** The "Build" attribute */
  public String build;

  /** The "max_units" attribute */
  public int maxUnits = -1;

  private Region activeRegion;

  private Faction nullFaction;

  private Region nullRegion;

  private Race nullRace;

  /**
   * A collection of all units. The keys are <tt>Integer</tt> objects containing the unit's ids. The
   * values consist of objects of class <tt>Unit</tt>. TEMP units are not included, they are only
   * stored in the unit collection of their parents and their regions and in the tempUnits map.
   * 
   * @return returns the units map
   * @deprecated Try using {@link #getUnits()}
   */
  @Deprecated
  public Map<UnitID, Unit> units() {
    return Collections.unmodifiableMap(unitView());
  }

  /**
   * Returns a collection of all units in the data.
   */
  public Collection<Unit> getUnits() {
    return Collections.unmodifiableCollection(unitView().values());
  }

  /** Returns a modifiable view of the units. */
  protected abstract Map<UnitID, Unit> unitView();

  /**
   * A collection of tempUnits. The keys are <tt>Integer</tt> objects containinng the unit's ids.
   * The values consist of objects of class <tt>TempUnit</tt>.
   * 
   * @return returns the tempunits map
   * @deprecated
   */
  @Deprecated
  public Map<UnitID, TempUnit> tempUnits() {
    return Collections.unmodifiableMap(tempUnitView());
  }

  /** Returns a modifiable of the temp units. */
  protected abstract Map<UnitID, TempUnit> tempUnitView();

  /** Returns a modifiable view of the old units. */
  protected abstract Map<UnitID, Unit> oldUnitsView();

  /**
   * All regions in this game data. The keys are <tt>Coordinate</tt> objects containing the id of
   * each region. The values consist of objects of class <tt>Region</tt>.<br />
   * <b>Attention</b>: You might not always get what you expect if work based on coordinates in a
   * cylinder- or torus-shaped world! <a>
   * 
   * @see Region#getNeighbours()
   * @return returns the regions map
   * @see #getRegion(CoordinateID)
   * @deprecated Try using {@link #getRegions()}
   */
  @Deprecated
  public Map<CoordinateID, Region> regions() {
    return Collections.unmodifiableMap(regionView());
  }

  /**
   * Returns a collection of all regions of the report.
   */
  public Collection<Region> getRegions() {
    return Collections.unmodifiableCollection(regionView().values());
  }

  /** Returns a modifiable view of the regions. */
  protected abstract Map<CoordinateID, Region> regionView();

  /**
   * All factions in this game data. The keys are <tt>Integer</tt> objects containg the id of each
   * faction. The values consist of objects of class <tt>Faction</tt>. One of these factions can be
   * referenced by the ownerFaction attribute.
   * 
   * @return returns the factions map
   * @deprecated Try using {@link #getFactions()}
   */
  @Deprecated
  public Map<EntityID, Faction> factions() {
    return Collections.unmodifiableMap(factionView());
  }

  /**
   * Returns a collection of all the report's factions.
   */
  public Collection<Faction> getFactions() {
    return Collections.unmodifiableCollection(factionView().values());
  }

  /** Returns a modifiable view of the factions. */
  protected abstract Map<EntityID, Faction> factionView();

  /**
   * All buildings in this game data. The keys are <tt>Integer</tt> objects containg the id of each
   * building. The values consist of objects of class <tt>Building</tt>.
   * 
   * @return returns the buildings map
   * @deprecated Try using {@link #getBuildings()}
   */
  @Deprecated
  public Map<EntityID, Building> buildings() {
    return Collections.unmodifiableMap(buildingView());
  }

  /**
   * Returns an unmodifiable view of all buildings in the data.
   */
  public Collection<Building> getBuildings() {
    return Collections.unmodifiableCollection(buildingView().values());
  }

  /** Returns a modifiable view of the buildings. */
  protected abstract Map<EntityID, Building> buildingView();

  /**
   * All ships in this game data. The keys are <tt>Integer</tt> objects containing the id of each
   * ship. The values consist of objects of class <tt>Ship</tt>.
   * 
   * @return returns the ships map
   * @deprecated Try using {@link #getShips()}
   */
  @Deprecated
  public Map<EntityID, Ship> ships() {
    return Collections.unmodifiableMap(shipView());
  }

  /**
   * Returns an unmodifiable view of all ships in the data.
   */
  public Collection<Ship> getShips() {
    return Collections.unmodifiableCollection(shipView().values());
  }

  /** Returns a modifiable view of the ships. */
  protected abstract Map<EntityID, Ship> shipView();

  /**
   * All message types in this game data. The keys are <tt>Integer</tt> objects containg the id of
   * each message type. The values consist of <tt>MessageType</tt> objects.
   * 
   * @return returns the messageType map
   */
  public Map<IntegerID, MessageType> msgTypes() {
    return Collections.unmodifiableMap(msgTypeView());
  }

  /** Returns a modifiable view of the message types. */
  protected abstract Map<IntegerID, MessageType> msgTypeView();

  /**
   * All magic spells in this game data. The keys are <tt>Integer</tt> objects containing the id of
   * each spell. The values consist of objects of class <tt>Spell</tt>.
   * 
   * @return returns the spells map
   * @deprecated Try using {@link #getSpells()}
   */
  @Deprecated
  public Map<StringID, Spell> spells() {
    return Collections.unmodifiableMap(spellView());
  }

  /**
   * Returns an unmodifiable view of all spells in the data.
   */
  public Collection<Spell> getSpells() {
    return Collections.unmodifiableCollection(spellView().values());
  }

  /** Returns a modifiable view of the spells. */
  protected abstract Map<StringID, Spell> spellView();

  /**
   * All potions in this game data. The keys are <tt>Integer</tt> objects containg the id of each
   * potion. The values consist of objects of class <tt>Potion</tt>.
   * 
   * @return returns the potions map
   * @deprecated
   */
  @Deprecated
  public Map<IntegerID, Potion> potions() {
    return Collections.unmodifiableMap(potionView());
  }

  /**
   * Returns an unmodifiable view of all potions in the data.
   */
  public Collection<Potion> getPotions() {
    return Collections.unmodifiableCollection(potionView().values());
  }

  /** Returns a modifiable view of the potions. */
  protected abstract Map<IntegerID, Potion> potionView();

  /**
   * All islands in this game data. The keys are <tt>Integer</tt> objects containing the id of each
   * island. The values consist of objects of class <tt>Island</tt>.
   * 
   * @return returns the islands map
   * @deprecated
   */
  @Deprecated
  public Map<IntegerID, Island> islands() {
    return Collections.unmodifiableMap(islandView());
  }

  /**
   * Returns an unmodifiable collection of all islands in the data.
   */
  public Collection<Island> getIslands() {
    return Collections.unmodifiableCollection(islandView().values());
  }

  /** Returns a modifiable view of the islands. */
  public abstract Map<IntegerID, Island> islandView();

  // /**
  // * All HotSpots existing for this game data. Hot spots are used to quickly access regions of
  // * interest on the map. The keys are Integer representations of the hot spot id, the values are
  // * Coordinate objects.
  // *
  // * @deprecated Try using {@link #getHotSpots()}
  // */
  // @Deprecated
  // public Map<IntegerID, HotSpot> hotSpots() {
  // return Collections.unmodifiableMap(hotSpotView());
  // }
  //
  // /**
  // * Returns a unmodifiable view of all HotSpots of the data.
  // */
  // public Collection<HotSpot> getHotSpots() {
  // return Collections.unmodifiableCollection(hotSpotView().values());
  // }
  //
  // /** Returns a modifiable view of the hot spots. */
  // protected abstract Map<IntegerID, HotSpot> hotSpotView();

  /**
   * Represents the table of translations from the report.
   */
  public abstract Translations translations();

  /**
   * Returns a collection of all known AllianceGroups.
   */
  public Collection<AllianceGroup> getAllianceGroups() {
    if (alliancegroups == null)
      return Collections.emptyList();
    else
      return Collections.unmodifiableCollection(alliancegroups.values());
  }

  /**
   * is set to true, if while proceeding some functions (e.g. CRParse) and we are running out of
   * memory... data may be corrupted or empty then
   */
  protected boolean outOfMemory = false;

  /**
   * sortIndex is used to keep objects from CRParser to CRWriter in an order. maxSortIndex is set
   * after CRParse and Used for creation of new Objects (e.g. MapEdit Plugin) and increased.
   */
  private int maxSortIndex = 0;

  private Random random;

  private Set<Long> inventedUIDs = new HashSet<Long>();

  private Set<UnitChangeListener> changeListeners;

  /**
   * Creates a new GameData object with the name of "default".
   * 
   * @param _rules Valid rules for the game
   */
  public GameData(Rules _rules) {
    this(_rules, "default");
  }

  // static int created = 0;
  // static int deleted = 0;

  /**
   * Creates a new GameData object.
   * 
   * @param rules Valid rules for the game
   * @param name The game name (like "Eressea", "E3", ...)
   * @throws NullPointerException if <code>rules==null</code>
   */
  public GameData(Rules rules, String name) {
    if (rules == null)
      throw new NullPointerException();
    this.rules = rules;
    gameSpecific = rules.getGameSpecificStuff();
    mapMetric = gameSpecific.getMapMetric();
    gameRules = gameSpecific.getGameSpecificRules();
    gameName = name;

    random = new Random();
    // // for profiling purposes
    // created++;
    // System.err.println("============== data: "+created+" - "+deleted);
    // Runtime.getRuntime().runFinalization();
    // System.gc();
  }

  // @Override
  // protected void finalize() throws Throwable {
  // deleted++;
  // System.err.println("==="+this.filename+"=== data: "+created+" - "+deleted);
  // super.finalize();
  // }

  /**
   * @see magellan.library.Addeable#addAttribute(java.lang.String, java.lang.String)
   */
  public void addAttribute(String key, String value) {
    attributes.put(key, value);
  }

  /**
   * Add a unit to the specified game data. If units() is <tt>null</tt>, this method has no effect.
   * 
   * @param u the unit to be added.
   */

  public void addUnit(Unit u) {
    Unit old = unitView().put(u.getID(), u);
    if (old != null) {
      addError(ProblemFactory.createProblem(Severity.ERROR,
          GameDataInspector.GameDataProblemTypes.DUPLICATEUNITID.type, u.getRegion(), u, null, u,
          null, Resources.get("gamedata.problem.duplicateunit.message", u, old), -1));
    }
    oldUnitsView().remove(u.getID());
  }

  /**
   * Add a temp unit.
   */
  public abstract void addTempUnit(TempUnit t);

  /**
   * Add a unit that doesn't exist any more.
   */
  public void addOldUnit(Unit newUnit) {
    if (oldUnitsView().containsKey(newUnit.getID()))
      throw new IllegalArgumentException(newUnit + " already exists");

    oldUnitsView().put(newUnit.getID(), newUnit);
  }

  /**
   * Returns a collection of all old units in the data.
   */
  public Collection<Unit> getOldUnits() {
    return Collections.unmodifiableCollection(oldUnitsView().values());
  }

  /**
   * Returns an old unit
   * 
   * @param id
   * @return the old unit with the given id, or <code>null</code> if it doesn't exist
   */
  public Unit getOldUnit(UnitID id) {
    return (oldUnitsView() == null) ? null : oldUnitsView().get(id);
  }

  /**
   * Add a region to the specified game data. If regions() is <tt>null</tt>, this method has no
   * effect.
   * 
   * @param r the region to be added.
   */

  public void addRegion(Region r) {
    if (wrappers.containsKey(r.getID())) {
      wrappers.remove(r.getID());
    }
    if (voids.containsKey(r.getID())) {
      voids.remove(r.getID());
    }
    Region old = regionView().put(r.getID(), r);
    if (old != null) {
      if (old.getVisibility() != Visibility.WRAP && old.getRegionType() != RegionType.unknown) {
        if (r.getRegionType() == RegionType.unknown) {
          regionView().put(r.getID(), old);
          r = old;
        } else {
          addError(ProblemFactory.createProblem(Severity.ERROR,
              GameDataInspector.GameDataProblemTypes.DUPLICATEREGIONID.type, r, null, null, r,
              null, Resources.get("gamedata.problem.duplicateregionid.message", r, old), -1));
        }
      }
    }

    Map<Direction, Region> neighbors = Regions.getCoordinateNeighbours(this, r.getCoordinate());
    for (Direction d : neighbors.keySet()) {
      Region n = neighbors.get(d);
      n.addNeighbor(getMapMetric().opposite(d), r);
      r.addNeighbor(d, n);
    }
  }

  /**
   * Add a faction to the specified game data. If factions() is <tt>null</tt>, this method has no
   * effect.
   * 
   * @param f the faction to be added.
   */

  public void addFaction(Faction f) {
    factionView().put(f.getID(), f);
  }

  /**
   * Add a building to the specified game data. If buildings() is <tt>null</tt>, this method has no
   * effect.
   * 
   * @param b the building to be added.
   */

  public void addBuilding(Building b) {
    Building old = buildingView().put(b.getID(), b);
    if (old != null) {
      addError(ProblemFactory.createProblem(Severity.ERROR,
          GameDataInspector.GameDataProblemTypes.DUPLICATEBUILDINGID.type, b.getRegion(), null,
          null, b, null, Resources.get("gamedata.problem.duplicatebuilding.message", b, old), -1));
    }
  }

  /**
   * Add a ship to the specified game data. If ships() is <tt>null</tt>, this method has no effect.
   * 
   * @param s the ship to be added.
   */

  public void addShip(Ship s) {
    Ship old = shipView().put(s.getID(), s);
    if (old != null) {
      addError(ProblemFactory.createProblem(Severity.ERROR,
          GameDataInspector.GameDataProblemTypes.DUPLICATESHIPID.type, s.getRegion(), null, null,
          s, null, Resources.get("gamedata.problem.duplicateship.message", s, old), -1));
    }
  }

  /**
   * Add a message type to the specified game data. If msgTypes() is <tt>null</tt>, this method has
   * no effect.
   * 
   * @param type the message type to be added.
   */

  public void addMsgType(MessageType type) {
    msgTypeView().put(type.getID(), type);
  }

  /**
   * Add a spell to the specified game data. If spells() is <tt>null</tt>, this method has no
   * effect.
   * 
   * @param s the spells to be added.
   */

  public void addSpell(Spell s) {
    spellView().put(s.getID(), s);
  }

  /**
   * Add a potion to the specified game data. If potions() is <tt>null</tt>, this method has no
   * effect.
   * 
   * @param p the potion to be added.
   */

  public void addPotion(Potion p) {
    potionView().put(p.getID(), p);
  }

  /**
   * Add an island to the specified game data. If islands() is <tt>null</tt>, this method has no
   * effect.
   * 
   * @param i the island to be added.
   */

  public void addIsland(Island i) {
    islandView().put(i.getID(), i);
  }

  // public abstract void addHotSpot(HotSpot h);

  /**
   * Add an AllianceGroup.
   */
  public void addAllianceGroup(AllianceGroup alliance) {
    if (alliancegroups == null) {
      alliancegroups = CollectionFactory.<ID, AllianceGroup> createSyncOrderedMap(1);
    }
    alliancegroups.put(alliance.getID(), alliance);
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
   * Retrieve a unit from units() by id.
   * 
   * @param id the id of the unit to be retrieved.
   * @return an instance of class <tt>Unit</tt> or <tt>null</tt> if there is no unit with the
   *         specified id or if units() is <tt>null</tt>.
   */
  public Unit getUnit(ID id) {
    return (unitView() == null) ? null : unitView().get(id);
  }

  /**
   * Returns the temp unit with the specified ID, <code>null</code> if it doesn not exist.
   */
  public TempUnit getTempUnit(UnitID id) {
    return tempUnitView().get(id);
  }

  /**
   * Retrieve a region from regions() by id.<br />
   * <b>Attention</b>: You might not always get what you expect if work based on coordinates in a
   * cylinder- or torus-shaped world! <a>
   * 
   * @see Region#getNeighbours()
   * @param c region coordinate
   * @return an instance of class <tt>Region</tt> or <tt>null</tt> if there is no region with the
   *         specified coordinates or if regions() is <tt>null</tt>.
   */
  public Region getRegion(CoordinateID c) {
    return (regionView() == null) ? null : regionView().get(c);
  }

  /**
   * Retrieve a faction from factions() by id.
   * 
   * @param id the id of the faction to be retrieved.
   * @return an instance of class <tt>Faction</tt> or <tt>null</tt> if there is no faction with the
   *         specified id or if factions() is <tt>null</tt>.
   */
  public Faction getFaction(ID id) {
    return (factionView() == null) ? null : (factionView().get(id) != null ? factionView().get(id)
        : (id.equals(getNullFaction().getID()) ? getNullFaction() : null));
  }

  /**
   * Retrieve a building from buildings() by id.
   * 
   * @param id the id of the building to be retrieved.
   * @return an instance of class <tt>Building</tt> or <tt>null</tt> if there is no building with
   *         the specified id or if buildings() is <tt>null</tt>.
   */
  public Building getBuilding(ID id) {
    return (buildingView() == null) ? null : (Building) buildingView().get(id);
  }

  /**
   * Retrieve a ship from ships() by id.
   * 
   * @param id the id of the ship to be retrieved.
   * @return an instance of class <tt>Ship</tt> or <tt>null</tt> if there is no ship with the
   *         specified id or if ships() is <tt>null</tt>.
   */
  public Ship getShip(ID id) {
    return (shipView() == null) ? null : (Ship) shipView().get(id);
  }

  /**
   * Retrieve a message type from msgTypes() by id.
   * 
   * @param id the id of the message type to be retrieved.
   * @return an instance of class <tt>MessageType</tt> or <tt>null</tt> if there is no message type
   *         with the specified id or if msgTypes() is <tt>null</tt>.
   */
  public MessageType getMsgType(ID id) {
    return (msgTypeView() == null) ? null : (MessageType) msgTypeView().get(id);
  }

  /**
   * Retrieve a spell from spells() by id.
   * 
   * @param id the id of the spell to be retrieved.
   * @return an instance of class <tt>Spell</tt> or <tt>null</tt> if there is no spell with the
   *         specified id or if spells() is <tt>null</tt>.
   */
  public Spell getSpell(ID id) {
    return (spellView() == null) ? null : (Spell) spellView().get(id);
  }

  /**
   * Retrieve a spell from spells() by Name. used for orderReader / completer
   * 
   * @param spellName the name of the spell to be retrieved.
   * @return an instance of class <tt>Spell</tt> or <tt>null</tt> if there is no spell with the
   *         specified id or if spells() is <tt>null</tt>.
   */
  public Spell getSpell(String spellName) {
    if (spellView() == null || spellView().size() == 0)
      return null;
    for (Spell spell : spellView().values()) {
      if (spell.getName().equalsIgnoreCase(spellName))
        return spell;
    }
    return null;
  }

  /**
   * Retrieve a potion from potions() by id.
   * 
   * @param id the id of the potion to be retrieved.
   * @return an instance of class <tt>Potion</tt> or <tt>null</tt> if there is no potion with the
   *         specified id or if potions() is <tt>null</tt>.
   */
  public Potion getPotion(ID id) {
    return (potionView() == null) ? null : (Potion) potionView().get(id);
  }

  /**
   * Retrieve a island from islands() by id.
   * 
   * @param id the id of the island to be retrieved.
   * @return an instance of class <tt>Island</tt> or <tt>null</tt> if there is no island with the
   *         specified id or if islands() is <tt>null</tt>.
   */
  public Island getIsland(ID id) {
    return (islandView() == null) ? null : (Island) islandView().get(id);
  }

  /**
   * Returns the AllianceGroup with the specified ID if it exists, otherwise <code>null</code>.
   */
  public AllianceGroup getAllianceGroup(ID allianceID) {
    if (alliancegroups == null)
      return null;
    return alliancegroups.get(allianceID);
  }

  // /**
  // * Retrieve a hot spot from hotSpots() by its id.
  // *
  // * @param id the id of the hot spot to be retrieved.
  // * @return an instance of class <tt>HotSpot</tt> or <tt>null</tt> if there is no hot spot with
  // the
  // * specified id or if hotSpots() is <tt>null</tt> .
  // */
  // public HotSpot getHotSpot(IntegerID id) {
  // return (hotSpotView() == null) ? null : (HotSpot) hotSpotView().get(id);
  // }

  /**
   * Removes unit from the report.
   */
  public void removeUnit(UnitID id) {
    unitView().remove(id);
  }

  /**
   * Removes a temp unit from the collection of temp units.
   */
  public void removeTemp(UnitID key) {
    tempUnitView().remove(key);
  }

  /**
   * Removes a faction from the report.
   */
  public void removeFaction(EntityID id) {
    // TODO (stm) remove units??
    factionView().remove(id);
  }

  /**
   * Removes a region from the data. Also removes units, buildings, ships, and hot spots.
   * 
   * @return The region that was removed, or <code>null</code> if the region wasn't found
   */
  public Region removeRegion(Region r) {
    Region removed = regionView().remove(r.getID());
    if (removed != null) {
      for (Direction d : removed.getNeighbors().keySet()) {
        if (removed.getNeighbors().get(d).getNeighbors().get(getMapMetric().opposite(d)) == removed) {
          removed.getNeighbors().get(d).removeNeighbor(getMapMetric().opposite(d));
        }
      }
      for (Direction d : getMapMetric().getDirections()) {
        removed.removeNeighbor(d);
      }
      for (Unit u : removed.units()) {
        if (unitView().remove(u.getID()) == null) {
          log.warn("could not remove unit " + u);
        }
        for (TempUnit t : u.tempUnits()) {
          if (tempUnitView().remove(t) == null) {
            log.warn("could not remove TEMP unit " + t);
          }
        }
      }
      for (Ship s : removed.ships())
        if (shipView().remove(s) == null) {
          log.warn("could not remove ship " + s);
        }
      for (Building b : removed.buildings())
        if (buildingView().remove(b) == null) {
          log.warn("could not remove TEMP unit " + b);
        }
      // for (HotSpot h : hotSpotView().values()) {
      // if (h.getCenter().equals(removed.getID())) {
      // removeHotSpot(h.getID());
      // }
      // }
    }
    return removed;
  }

  /**
   * Removes the island from the report.
   */
  public void removeIsland(IntegerID islandID) {
    islandView().remove(islandID);
  }

  // /**
  // * Remove a hot spot from hotSpots() by its id.
  // *
  // * @param id the id of the hot spot to be removed.
  // */
  // public void removeHotSpot(IntegerID id) {
  // if (hotSpotView() != null) {
  // hotSpotView().remove(id);
  // }
  // }

  /**
   * Sets the new set of islands.
   */
  public abstract void setIslands(Map<IntegerID, Island> islands);

  /**
   * Returns a map selected regions.
   */
  public abstract Map<CoordinateID, Region> getSelectedRegionCoordinates();

  /**
   * Adds a region to the selection
   */
  public abstract void addSelectedRegionCoordinate(Region region);

  /**
   * Set a collection of selected regions.
   * 
   * @param regions the Map of coordinates of selected regions
   */
  public abstract void setSelectedRegionCoordinates(Map<CoordinateID, Region> regions);

  // /**
  // * Add or set a hot spot to the specified game data. If hotSpots() is <tt>null</tt>, this method
  // * has no effect.
  // *
  // * @param h the hot spot to be added.
  // */
  // public void setHotSpot(HotSpot h) {
  // if (hotSpotView() != null) {
  // hotSpotView().put(h.getID(), h);
  // }
  // }

  /**
   * This method sets the current temp id.
   */
  public int getCurTempID() {
    return curTempID;
  }

  /**
   * This method sets the current Temp ID with respect to the possible max value of the current
   * base. The value also has to be >= -1
   * 
   * @param newTempID Temp ID
   */
  public void setCurTempID(int newTempID) {
    curTempID = Math.max(-1, Math.min(newTempID, IDBaseConverter.getMaxId(base)));
  }

  /**
   * This method interprets the string as a number in the {@link #base} and sets the current Temp ID
   * with respect to the possible max value of the current base. The value also has to be >= -1
   * 
   * @param s Temp ID
   */
  public void setCurTempID(String s) {
    setCurTempID("".equals(s) ? 0 : IDBaseConverter.parse(s, base));
  }

  /**
   * Retrieve a translation from translations().
   * 
   * @param key the key of the translation to be retrieved.
   * @return an instance of class <tt>String</tt>. If no translation could be found, the name of the
   *         object is returned.
   */
  public String getTranslation(Named key) {
    return key == null ? null : getTranslation(key.getName());
  }

  /**
   * Retrieve a translation from translations().
   * 
   * @param key the key of the translation to be retrieved.
   * @return an instance of class <tt>String</tt>. If no translation could be found, the key is
   *         returned.
   */
  public String getTranslation(String key) {

    return translations().getTranslation(key);
    /*
     * String retVal = (key == null || translations() == null) ? null : (String)
     * translations().get(key); return retVal != null ? retVal : key;
     */
  }

  /**
   * Puts a translation into the translation table.
   * 
   * @param from a language independent key.
   * @param to the language dependent translation of key.
   */
  public void addTranslation(String from, String to, int source) {

    translations().addTranslation(from, to, source);
    if (getRules() != null) {
      // dynamically add translation key to rules to access object by name
      getRules().changeName(from, to);
    }

    /*
     * if (translations() != null) { translations().put(from, to); if (rules != null) { //
     * dynamically add translation key to rules to access object by name rules.changeName(from, to);
     * } }
     */
  }

  /**
   * Set a date, or a 'round', for this game data.
   * 
   * @param d the new date.
   */
  public void setDate(Date d) {
    date = d;
  }

  /**
   * Get the date associated with this game data.
   * 
   * @return rules.Date object
   */
  public Date getDate() {
    return date;
  }

  /**
   * Sets the valid locale for this report. Currently, this is only used to remember this setting
   * and write it back into the cr.
   */
  public abstract void setLocale(Locale l);

  /**
   * Returns the locale of this report. Currently, this is only used to remember this setting and
   * write it back into the cr.
   */
  public abstract Locale getLocale();

  /**
   * returns a clone of the game data (using CRWriter/CRParser trick encapsulated in Loader)
   * 
   * @throws CloneNotSupportedException If cloning doesn't succeed
   * @see java.lang.Object#clone()
   */
  @Override
  public GameData clone() throws CloneNotSupportedException {
    // return new Loader().cloneGameData(this);
    return clone(new IdentityTransformer());
  }

  /**
   * Returns a clone of the game data (using CRWriter/CRParser trick encapsulated in Loader) and at
   * the same time translates the origin to <code>newOrigin</code>.
   * 
   * @throws CloneNotSupportedException If cloning doesn't succeed
   */
  public GameData clone(CoordinateID newOrigin) throws CloneNotSupportedException {
    return clone(new TwoLevelTransformer(newOrigin, CoordinateID.ZERO));
  }

  /**
   * Returns a clone of the game data (using CRWriter/CRParser trick encapsulated in Loader) and at
   * the same time translates the new report.
   * 
   * @throws CloneNotSupportedException If cloning doesn't succeed
   */
  public GameData clone(ReportTransformer coordinateTranslator) throws CloneNotSupportedException {
    try {
      if (MemoryManagment.isFreeMemory(estimateSize() * 3)) {
        GameData.log.info("cloning in memory");
        GameData clonedData = new Loader().cloneGameDataInMemory(this, coordinateTranslator);
        if (clonedData == null || clonedData.isOutOfMemory()) {
          GameData.log.info("cloning externally after failed memory-clone-attempt");
          clonedData = new Loader().cloneGameData(this, coordinateTranslator);
        }
        return clonedData;
      }
    } catch (Exception e) {
      log.error("Loader.cloneGameData failed!", e);
    }
    GameData.log.info("cloning externally");
    return new Loader().cloneGameData(this, coordinateTranslator);

  }

  /**
   * Returns an estimate of the memory (in bytes) needed to store this game data. This can be a very
   * rough estimate!
   * 
   * @return An estimate of the memory needed to store this game data
   */
  public abstract long estimateSize();

  /** Game specific and usually fixed data (like races etc.). */
  public final Rules getRules() {
    return rules;
  }

  /**
   * Provides the encapsulating of game specific stuff
   * 
   * @see GameSpecificStuff
   */
  public final GameSpecificStuff getGameSpecificStuff() {
    return gameSpecific;
  }

  /**
   * Shortcut for getGameSpecificStuff().getGameSpecificRules().
   * 
   * @see GameSpecificStuff
   */
  public final GameSpecificRules getGameSpecificRules() {
    return gameRules;
  }

  /**
   * Returns the game's map metric.
   * 
   * @see GameSpecificStuff
   */
  public final MapMetric getMapMetric() {
    return mapMetric;
  }

  /**
   * Returns an appropriate order parser.
   * 
   * @see GameSpecificStuff
   */
  public final OrderParser getOrderParser() {
    if (parser == null) {
      parser = getGameSpecificStuff().getOrderParser(this);
    }
    return parser;
  }

  /**
   * This method can be called after loading or merging a report to avoid double messages and to set
   * some game specific stuff.
   */
  public void postProcess() {
    // FIXME(stm) does it harm to call this more than once???
    // if (postProcessed)
    // return;

    log.fine("start GameData postProcess");

    // unfortunately, this is necessary because there are parse errors when orders are added to
    // units before the report has been completely read...
    for (Unit u : getUnits()) {
      u.reparseOrders();
    }
    for (Region r : getRegions()) {
      r.refreshUnitRelations(true);
    }

    // do game specific post processing
    getGameSpecificStuff().postProcess(this);

    postProcessTheVoid();

    // postProcessErrors();

    // unfortunately, this is necessary because there are parse errors when orders are added to
    // units before the report has been completely read...
    for (Unit u : getUnits()) {
      u.reparseOrders();
    }
    for (Region r : getRegions()) {
      r.refreshUnitRelations(true);
    }

    log.fine("finished GameData postProcess");
  }

  public void postProcessErrors() {
    Map<Long, Region> regionMap = new HashMap<Long, Region>(getRegions().size() * 5 / 4 + 5, .8f);
    Region original = null;
    Region copy = null;
    for (Region r : getRegions()) {
      if (r.hasUID() && !r.getVisibility().equals(Visibility.WRAP)) {
        Region old = regionMap.get(r.getUID());
        if (old == null) {
          regionMap.put(r.getUID(), r);
        } else {
          original = old;
          copy = r;
          addError(ProblemFactory.createProblem(Severity.ERROR,
              GameDataInspector.GameDataProblemTypes.DUPLICATEREGIONUID.type, original, null, null,
              copy, null, Resources.get("gamedata.problem.duplicateregionuid", r, old), -1));
        }
      }
    }

    // if (count > 1) {
    // message.append("...and " + (count - 1) + " more.");
    // }
    // if (count > 0) {
    // }
    regionMap.clear();

  }

  /**
   * Post process of Island objects. The Regions of the GameData are attached to their Island.
   */
  public void postProcessIslands() {
    GameData data = this;

    // create a map of region maps for every Island
    final Map<Island, Map<CoordinateID, Region>> islandMap =
        new Hashtable<Island, Map<CoordinateID, Region>>();

    for (final Region r : data.getRegions()) {
      if (r.getIsland() != null) {
        Map<CoordinateID, Region> actRegionMap = islandMap.get(r.getIsland());

        if (actRegionMap == null) {
          actRegionMap = new Hashtable<CoordinateID, Region>();
          islandMap.put(r.getIsland(), actRegionMap);
        }

        actRegionMap.put(r.getID(), r);
      }
    }

    // setRegions for every Island in the map of region maps.
    for (final Island island : islandMap.keySet()) {
      final Map<CoordinateID, Region> actRegionMap = islandMap.get(island);
      island.setRegions(actRegionMap);
    }

  }

  /**
   * Set data to default values: RegionTypes, faction races, unit name/race/faction
   */
  public void postProcessUnknown() {

    GameData data = this;

    // fix unknown stuff
    for (Region r : data.getRegions()) {
      if (r.getRegionType() == null) {
        r.setType(RegionType.unknown);
      }
    }

    // search for the races of the factions in the report.
    for (Faction faction : data.getFactions()) {
      // if the race is already set in the report ignore this algorithm
      if (faction.getType() != null) {
        continue;
      }

      final Map<Race, Integer> personsPerRace = new HashMap<Race, Integer>();

      // iterate thru all units and count the races of them
      final Collection<Unit> units = faction.units();
      for (final Unit unit : units) {
        final Race race = unit.getRace();
        if (race == null) {
          continue;
        }
        if (personsPerRace.containsKey(race)) {
          final int amount = personsPerRace.get(race) + unit.getPersons();
          personsPerRace.put(race, amount);
        } else {
          personsPerRace.put(race, unit.getPersons());
        }
      }

      // find the race with the most persons in it - this is the race of the
      // faction.
      int maxPersons = 0;
      Race race = null;
      for (final Race aRace : personsPerRace.keySet()) {
        final int amount = personsPerRace.get(aRace);
        if (amount > maxPersons) {
          maxPersons = amount;
          race = aRace;
        }
      }

      if (race != null) {
        faction.setType(race);
      }
    }

    fixUnknown(data.getUnits(), data);
    fixUnknown(data.getOldUnits(), data);
  }

  private void fixUnknown(Collection<Unit> units, GameData data) {
    // there can be dummy units (UnitContainer owners and such), find and remove these
    if (units != null) {
      // Collection<UnitID> dummyUnitIDs = new LinkedList<UnitID>();

      for (Unit unit : units) {
        if (unit.getName() == null) {
          // dummyUnitIDs.add(unit.getID());
          unit.setName("???");
        }
        if (unit.getRegion() == null) {
          unit.setRegion(data.getNullRegion());
        }
        if (unit.getFaction() == null) {
          unit.setFaction(data.getNullFaction());
        }
        if (unit.getRace() == null) {
          unit.setRace(data.getNullRace());
        }
      }

      // for (UnitID id : dummyUnitIDs) {
      // data.removeUnit(id);
      // }
    }
  }

  /**
   * adding Default Translations to the translations
   */
  public void postProcessDefaultTranslations() {
    // FIXME we should probably add translations directly to the rules files!
    // Skilltypes
    for (Iterator<SkillType> iter = getRules().getSkillTypeIterator(); iter.hasNext();) {
      SkillType skillType = iter.next();
      String key = "skill." + skillType.getID().toString();
      if (!translations().contains(key)) {
        // we have to add
        String translated = Resources.getRuleItemTranslation(key);
        if (translated.startsWith("rules.skill.")) {
          translated = skillType.getID().toString();
        }
        addTranslation(skillType.getID().toString(), translated, TranslationType.sourceMagellan);
      }
    }

    for (Iterator<BuildingType> iter = getRules().getBuildingTypeIterator(); iter.hasNext();) {
      BuildingType type = iter.next();
      String key = "building." + type.getID().toString();
      if (!translations().contains(key)) {
        // we have to add
        String translated = Resources.getRuleItemTranslation(key);
        if (translated.startsWith("rules.building.")) {
          translated = type.getID().toString();
        }

        addTranslation(type.getID().toString(), translated, TranslationType.sourceMagellan);
      }
    }
  }

  /**
   * scans the regions for missing regions, for regions with regionType "The Void" or "Leere" These
   * Regions are not created in the world on the server, but we are so near, that we should have
   * some information about it. So we add these Regions with the special RegionType "Leere"
   */
  public void postProcessTheVoid() {
    for (Region curRegion : regionView().values()) {
      if (curRegion.getVisibility().greaterEqual(Region.Visibility.TRAVEL)) {
        // should have all neighbors
        for (Direction d : getMapMetric().getDirections()) {
          if (curRegion.getNeighbors().get(d) == null) {
            // Missing Neighbor
            CoordinateID c = getMapMetric().translate(curRegion.getCoordinate(), d);
            addVoid(c);
          }
        }
      }
    }
  }

  /**
   * Adds the order locale of Magellan if locale is null. This should prevent some NPE with the
   * sideeffect to store a locale in a locale-less game data object.
   */
  public void postProcessLocale() {
    if (getLocale() == null) {
      setLocale(Locales.getOrderLocale());
    }
  }

  /**
   * This function post processes the message blocks to remove duplicate messages. In former times
   * this has been done while loading the game data but this had a negative time tradeoff (O(n^2)).
   * This functions needs about O(n log n).
   */
  public void postProcessMessages() {
    // faction.messages
    for (Faction o : factionView().values()) {
      postProcessMessages(o.getMessages());
    }

    // region.messages
    for (Region o : regionView().values()) {
      postProcessMessages(o.getMessages());
    }
  }

  /**
   * This functions post processes the potions to remove duplicate potions (potions with different
   * ID but the same name) It is tricky to determine which is the latest potion definition in the
   * code as far as i know there was no change in potion-definitions last months but only the IDs
   * chanegd over time - so it does not matter finally we could keep the potion with the highest ID
   * - asuming that this was the last potion-definition reveived from the server
   */
  public void postProcessPotions() {
    if (potionView() == null || potionView().size() == 0)
      // nothing to do
      return;
    // just for info:
    int count_before = potionView().size();
    // The final Map of the potions
    HashMap<String, Potion> newPotions = new LinkedHashMap<String, Potion>();
    // To sort the Potions after ID we use a simple List
    // and fill it with our Potions
    List<Potion> sortedPotions = new ArrayList<Potion>(potionView().values());
    // use normal name and ID comparator
    Comparator<Unique> idCmp = IDComparator.DEFAULT;
    Collections.sort(sortedPotions, new NameComparator(idCmp));

    // fill our new PotionMap with the PotionNames as Keys
    for (Potion p : sortedPotions) {
      // some info, if replacing
      if (newPotions.containsKey(p.getName().toLowerCase())) {
        Potion oldPotion = newPotions.get(p.getName().toLowerCase());
        GameData.log.info("removing Potion " + oldPotion.getName() + "(ID: "
            + oldPotion.getID().toString() + ")");
      }
      newPotions.put(p.getName().toLowerCase(), p);
    }
    // we have a clean PotionMap now -> set the real Potion Map
    // erease all potions
    potionView().clear();
    // fill again from our PotionMap
    for (Potion p : newPotions.values()) {
      addPotion(p);
    }
    // ready
    // some info?
    int count_after = potionView().size();
    if (count_before != count_after) {
      GameData.log.info("postProcessPotions: changing number of potions from " + count_before
          + " to " + count_after);
    }

  }

  /**
   * Post process a given list of messages. To remove duplicate messages we put all messages in an
   * ordered hash table and put them back into the messages collection.
   */
  private void postProcessMessages(Collection<Message> messages) {
    if (messages == null)
      return;

    Map<Message, Message> ht = CollectionFactory.<Message, Message> createSyncOrderedMap();

    for (Message msg : messages) {
      if (ht.put(msg, msg) != null) {
        GameData.log.debug("Duplicate message \"" + msg.getText() + "\" found, removing it.");
      }
    }

    messages.clear();
    messages.addAll(ht.values());
  }

  /**
   * DOCUMENT-ME
   */
  public void postProcessAfterTrustlevelChange() {
    getGameSpecificStuff().postProcessAfterTrustlevelChange(this);
  }

  /**
   * removes all "Leere" Regions needed for merging
   */
  public void removeTheVoid() {
    List<Region> todelete = new ArrayList<Region>();
    for (CoordinateID actRegionID : regionView().keySet()) {
      Region actRegion = getRegion(actRegionID);
      if (actRegion.getRegionType().equals(RegionType.theVoid)) {
        todelete.add(actRegion);
      }
    }
    for (Region r : todelete) {
      removeRegion(r);
    }
  }

  public int getMaxSortIndex() {
    return ++maxSortIndex;
  }

  public void setMaxSortIndex(int maxSortIndex) {
    this.maxSortIndex = maxSortIndex;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public String getEncoding() {
    return encoding;
  }

  /**
   * Returns the current active region.
   */
  public Region getActiveRegion() {
    return activeRegion;
  }

  /**
   * Marks a new region as the active region.
   * 
   * @param region
   */
  public void setActiveRegion(Region region) {
    activeRegion = region;
  }

  /**
   * Returns the relation of two map layers.
   * 
   * @return the <code>CoordinateID</code> of the toLevel region which is accessible by the
   *         fromLevel region with CoordinateID <0, 0, fromLevel>.
   */
  public LevelRelation getLevelRelation(int fromLevel, int toLevel) {
    Map<Integer, LevelRelation> relations = levelRelations.get(toLevel);
    if (relations == null) {
      relations = new LinkedHashMap<Integer, LevelRelation>();
      levelRelations.put(toLevel, relations);
    }
    LevelRelation lr = relations.get(fromLevel);
    if (lr == null) {
      MapMergeEvaluator mh = getGameSpecificStuff().getMapMergeEvaluator();
      lr = mh.getLevelRelation(this, fromLevel, toLevel);
      relations.put(fromLevel, lr);
    }
    return lr;
  }

  /**
   * This method should wrap the mapping information former contained in
   * magellan.client.swing.MapperPanel.setLevel(int)
   * 
   * @param level
   * @return Mapped Coordinate
   */
  public CoordinateID getRelatedCoordinate(CoordinateID c, int level) {
    LevelRelation lr = getLevelRelation(c.getZ(), level);
    if (lr == null)
      return null;
    return lr.getRelatedCoordinate(c);
  }

  public CoordinateID getRelatedCoordinateUI(CoordinateID c, int level) {
    CoordinateID result = getRelatedCoordinate(c, level);
    if (result == null) {
      LevelRelation lr = getLevelRelation(level, c.getZ());
      if (lr == null)
        return null;
      return lr.getInverseRelatedCoordinate(c);
    }
    return result;
  }

  /**
   * Removes all translations for Faction f.
   * 
   * @param f
   */
  public void clearTranslations(EntityID f) {
    coordinateTranslations.remove(f);
  }

  /**
   * Returns a coordinate translation of <code>otherFaction</code> this report. This is the
   * coordinate vector that has to be added to the origin (with z-coordinate <code>layer</code>) of
   * this report to get the origin of <code>otherFaction</code>. In other words, you have to
   * subtract this translation from <code>otherFaction</code>'s coordinates in layer
   * <code>layer</code> to get coordinates of this report. The coordinate translation of the owner
   * faction is <i>not</i> always (0, 0, layer).
   * 
   * @param otherFaction
   * @param layer
   * @return The coordinate translation of <code>otherFaction</code> to the owner faction.
   *         <code>null</code> if the translation is unknown
   */
  public CoordinateID getCoordinateTranslation(EntityID otherFaction, int layer) {
    Map<Integer, CoordinateID> layerMap = getCoordinateTranslationMap(otherFaction);
    if (layerMap == null)
      return null;
    else
      return layerMap.get(layer);
  }

  protected Map<EntityID, Map<Integer, CoordinateID>> getCoordinateTranslations() {
    return coordinateTranslations;
  }

  /**
   * Returns the immutable map of all known coordinate translations of <code>otherFaction</code>.
   * This is a mapping of layers to coordinateIDs. This is the coordinate vector that has to be
   * added to the origin (with z-coordinate <code>layer</code>) of this report to get the origin of
   * <code>otherFaction</code>. In other words, you have to subtract this translation from
   * <code>otherFaction</code>'s coordinates in layer <code>layer</code> to get coordinates of this
   * report. The coordinate translation of the owner faction is <i>not</i> always (0, 0, layer).
   * 
   * @return The map of coordinate translations of faction <code>otherFaction</code> or
   *         <code>null</code> if unknown
   * @see #getCoordinateTranslation(EntityID, int)
   */
  public Map<Integer, CoordinateID> getCoordinateTranslationMap(EntityID otherFaction) {
    if (coordinateTranslations.get(otherFaction) == null)
      return null;
    return Collections.unmodifiableMap(coordinateTranslations.get(otherFaction));
  }

  /**
   * Sets the coordinate translation of <code>otherFaction</code> to the owner faction. This is a
   * mapping of layers to coordinateIDs. This is the coordinate vector that has to be added to the
   * origin (with z-coordinate <code>layer</code>) of this report to get the origin of
   * <code>otherFaction</code>. In other words, you have to subtract this translation from
   * <code>otherFaction</code>'s coordinates in layer <code>layer</code> to get coordinates of this
   * report. The coordinate translation of the owner faction is <i>not</i> always (0, 0, layer).
   * 
   * @param otherFaction
   * @param usedTranslation
   */
  public void setCoordinateTranslation(EntityID otherFaction, CoordinateID usedTranslation) {
    Map<Integer, CoordinateID> layerMap = coordinateTranslations.get(otherFaction);
    if (layerMap == null) {
      layerMap = new LinkedHashMap<Integer, CoordinateID>();
      coordinateTranslations.put(otherFaction, layerMap);
    }
    layerMap.put(usedTranslation.getZ(), usedTranslation);
  }

  /**
   * The faction that this report is for.
   * 
   * @return The owner faction. <code>null</code> for unknown is possible.
   */
  public EntityID getOwnerFaction() {
    return ownerFaction;
  }

  /**
   * Changes the owner faction. Clears all coordinate translations.
   * 
   * @param ownerFaction
   */
  public void setOwnerFaction(EntityID ownerFaction) {
    if (this.ownerFaction != null) {
      // TODO (stm) we could translate the translations if we have a
      // translation from old owner faction to ownerFaction. For now, we simply
      // forget
      // all translations...
      GameData.log.warn("owner faction changed");
    }
    this.ownerFaction = ownerFaction;
  }

  /**
   * Returns the name of the game (e.g. "eresea").
   */
  public String getGameName() {
    return gameName;
  }

  /**
   * Sets the name of the game (e.g. "eresea").
   */
  public void setGameName(String newName) {
    gameName = newName;
  }

  /**
   * Sets the FileType (basically the file name this report was read from).
   */
  public void setFileType(FileType filetype) {
    this.filetype = filetype;
  }

  /**
   * Returns the FileType (basically the file name this report was read from).
   */
  public FileType getFileType() {
    return filetype;
  }

  /**
   * Sets the date of the report (it's not the date of the report).
   */
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * Returns the date of the report (it's not the date of the report).
   */
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * Adds a "void" region at <code>c</code>.
   * 
   * @return the new void region
   * @throws IllegalArgumentException if there was already a region at <code>c</code>.
   */
  public Region addVoid(CoordinateID c) {
    if (regionView().containsKey(c) || wrappers().containsKey(c))
      throw new IllegalArgumentException("there is a region at " + c);
    Region aVoid = MagellanFactory.createVoid(c, this);
    voids.put(c, aVoid);
    return aVoid;
  }

  /**
   * Returns all "void" regions, i.e., regions inserted only because we should have seen a region at
   * this place, but there is none for some region. Most functions do not need to take these regions
   * into account. The exception are displaying function, for example in the Mapper.
   */
  public Map<CoordinateID, Region> voids() {
    return Collections.unmodifiableMap(voids);
  }

  /**
   * Makes r a "wraparound" region, removing it as a normal region. A wrapper is a region which is
   * not a real region, but is only a placeholder for a another region (most probably to represent a
   * cylinder- or torus-shaped world.
   * 
   * @param wrapper The placeholder region
   * @param original The original region, represented by the wrapper.
   */
  public void makeWrapper(Region wrapper, Region original) {
    if (wrapper == null || original == null)
      throw new NullPointerException();

    // wrappers without region ID are dangerous
    // if this happens, we invent an ID for them
    // this is a hack, but what else can we do if the id is missing?
    if (!wrapper.hasUID()) {
      log.warn("wrapper without region ID: " + wrapper);
      if (original.hasUID()) {
        wrapper.setUID(original.getUID());
      } else {
        log.warn("inventing region ID for " + original);

        long newUID = random.nextLong();
        long min = ((long) Integer.MIN_VALUE) * 4096;
        // ensure that the invented ID doesn't occur in a server report
        // assumes that server IDs are 32bit
        while (inventedUIDs.contains(newUID) || newUID >= min) {
          newUID = random.nextLong();
        }
        inventedUIDs.add(newUID);
        original.setUID(newUID);
        wrapper.setUID(newUID);
      }
    } else {
      if (!original.hasUID() || wrapper.getUID() != original.getUID()) {
        log.error("wrapper and original region ID do not match: " + wrapper + " " + original);
      }
    }
    removeRegion(wrapper);
    wrappers.put(wrapper.getID(), wrapper);
    originals.put(wrapper, original);
  }

  /**
   * Returns a view of al "wraparound" regions.
   * 
   * @see #makeWrapper(Region, Region)
   */
  public Map<CoordinateID, Region> wrappers() {
    return Collections.unmodifiableMap(wrappers);
  }

  /**
   * Gets the real region for a wrapper region.
   * 
   * @return The region corresponding to this wrapper, or <code>null</code> if none is known.
   * @see #makeWrapper(Region, Region)
   */
  public Region getOriginal(Region wrapper) {
    return originals.get(wrapper);
  }

  /**
   * Returns a dummy region which may be used for units without region. This region should not be
   * contained in {@link #regions()}.
   */
  public Region getNullRegion() {
    if (nullRegion == null) {
      nullRegion = MagellanFactory.createRegion(CoordinateID.getInvalid(), this);
      nullRegion.setType(RegionType.unknown);
    }
    return nullRegion;
  }

  /**
   * Returns a dummy faction which may be used for units without faction. This faction should not be
   * contained in {@link #factions()}.
   */
  public Faction getNullFaction() {
    if (nullFaction == null) {
      nullFaction =
          MagellanFactory.createFaction(EntityID.createEntityID(Integer.MIN_VALUE, base), this);
    }
    return nullFaction;
  }

  /**
   * Returns a dummy race which may be used for units without race. This race should not be
   * contained in rules.{@link Rules#getRaces()}.
   */
  public Race getNullRace() {
    if (nullRace == null) {
      nullRace = new Race(StringID.create(Resources.get("unit.race.personen.name")));
    }
    return nullRace;
  }

  public void addError(Problem err) {
    if (errors == null) {
      errors = new ArrayList<Problem>();
    }
    errors.add(err);
  }

  public List<Problem> getErrors() {
    if (errors == null)
      return Collections.emptyList();
    else
      return Collections.unmodifiableList(errors);
  }

  /**
   * Sets the value of outOfMemory.
   * 
   * @param outOfMemory The value for outOfMemory.
   */
  public void setOutOfMemory(boolean outOfMemory) {
    this.outOfMemory = outOfMemory;
    if (isOutOfMemory() && !outOfMemory) {
      addError(ProblemFactory.createProblem(Severity.WARNING,
          GameDataInspector.GameDataProblemTypes.OUTOFMEMORY.type, null, null, null, null, null,
          GameDataInspector.GameDataProblemTypes.OUTOFMEMORY.type.getMessage(), -1));
    }
  }

  /**
   * Returns the value of outOfMemory.
   * 
   * @return Returns outOfMemory.
   */
  public boolean isOutOfMemory() {
    return outOfMemory;
  }

  /**
   * Tries to repair errors in this data and returns the repaired data.
   * 
   * @param ui2
   * @return The repaired data (may be <code>this</code>)
   */
  public GameData repair(UserInterface ui2) {
    MyAssigner assigner = new MyAssigner();
    final GameData data = this;

    for (Problem p : data.getErrors()) {
      if (p.getType() == GameDataInspector.GameDataProblemTypes.DUPLICATEREGIONUID.type) {
        Region original = p.getRegion();
        if (p.getObject() instanceof Region) {
          Region copy = (Region) p.getObject();
          if (original.getData() == data) {
            original.setUID(Region.INVALID_UID);
          }
          if (copy.getData() == data) {
            copy.setUID(Region.INVALID_UID);
          }
        }
      }
    }

    try {
      new ReportMerger(this, data.filetype.getFile(), new ReportMerger.Loader() {
        public GameData load(File aFile) {
          return data;
        }
      }, assigner).merge(ui2, true, false, false);
    } catch (IOException e) {
      log.error("invalid filetype");
      // this will probably not happen, since the Exception would have been thrown before. Anyway...
      assigner.data2 = this;
    }
    if (assigner.data2 == null) {
      assigner.data2 = this;
    }

    return assigner.data2;
  }

  public boolean isSameRound(GameData otherGameData) {
    return date.equals(otherGameData.date);
  }

  protected static class MyAssigner implements AssignData {
    GameData data2 = null;

    public void assign(GameData _data) {
      data2 = _data;
    }
  }

  public void fireOrdersChanged(Object source, Unit u, Object cause) {
    if (changeListeners != null) {
      UnitChangeEvent event = new UnitChangeEvent(source, u, cause);
      for (UnitChangeListener listener : getUnitChangeListeners()) {
        listener.unitChanged(event);
      }
    }
  }

  public void addUnitChangeListener(UnitChangeListener l) {
    if (l == null)
      throw new NullPointerException();
    if (changeListeners == null) {
      changeListeners = new LinkedHashSet<UnitChangeListener>();
    }
    changeListeners.add(l);
  }

  public void removeUnitChangeListener(UnitChangeListener l) {
    if (changeListeners != null) {
      changeListeners.remove(l);
    }
  }

  protected Collection<UnitChangeListener> getUnitChangeListeners() {
    if (changeListeners == null)
      return Collections.emptyList();
    return changeListeners;
  }

  public void fireOrdersChanged(Object source, Region r, Object cause) {
    for (UnitChangeListener listener : getUnitChangeListeners()) {
      // long time = System.currentTimeMillis();
      for (Unit u : r.units()) {
        if (changeListeners != null) {
          UnitChangeEvent event = new UnitChangeEvent(source, u, cause);
          listener.unitChanged(event);
        }
      }
      // log.finest((System.currentTimeMillis() - time) + " " + listener);
    }
  }

  public abstract void addBookmark(Bookmark bookmark);

  public abstract Bookmark getBookmark(Selectable selection);

  public abstract Collection<Bookmark> getBookmarks();

  public abstract void removeBookmark(Selectable selection);

  /**
   * Returns the maximum number of units per faction or -1 if this is unknown
   */
  public int getMaxUnits() {
    return maxUnits;
  }

}
