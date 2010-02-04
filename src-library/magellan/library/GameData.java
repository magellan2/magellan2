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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import magellan.library.Region.Visibility;
import magellan.library.gamebinding.GameSpecificStuff;
import magellan.library.gamebinding.MapMergeEvaluator;
import magellan.library.io.cr.Loader;
import magellan.library.io.file.FileType;
import magellan.library.rules.Date;
import magellan.library.rules.EresseaDate;
import magellan.library.rules.MessageType;
import magellan.library.rules.RegionType;
import magellan.library.rules.SkillType;
import magellan.library.utils.Encoding;
import magellan.library.utils.IDBaseConverter;
import magellan.library.utils.Locales;
import magellan.library.utils.MagellanFactory;
import magellan.library.utils.MemoryManagment;
import magellan.library.utils.OrderedHashtable;
import magellan.library.utils.Regions;
import magellan.library.utils.Resources;
import magellan.library.utils.TranslationType;
import magellan.library.utils.Translations;
import magellan.library.utils.comparator.IDComparator;
import magellan.library.utils.comparator.NameComparator;
import magellan.library.utils.logging.Logger;
import magellan.library.utils.mapping.LevelRelation;

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

  /** Game specific and usually fixed data (like races etc.). */
  public final Rules rules;

  /** The name of the game. */
  public String gameName;

  /** encoding */
  protected String encoding = FileType.DEFAULT_ENCODING.toString();

  private EntityID ownerFaction;

  private Map<EntityID, Map<Integer, CoordinateID>> coordinateTranslations =
      new HashMap<EntityID, Map<Integer, CoordinateID>>();

  private Map<Integer, Map<Integer, LevelRelation>> levelRelations =
      new HashMap<Integer, Map<Integer, LevelRelation>>();

  private Map<ID, AllianceGroup> alliancegroups;

  /**
   * The current TempUnit-ID. This means, if a new TempUnit is created, it's suggested ID is usually
   * curTempID and if this suggestion is accepted by the user (which means, that a TempUnit with
   * this id was created) curTempID is increased. A value of -1 indicates, that the variable is
   * uninitialized and a value of 0 that the old system shall be used (which means, that the
   * suggested temp id shall be calculated out of the id of the parent unit of the tempunit).
   */
  protected int curTempID = -1;

  /** Contains all attributes */
  private Map<String, String> attributes = new HashMap<String, String>();

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
   * This method sets the current temp id with respect to the possible max value of the current
   * base. The value also has to be >= -1
   * 
   * @param newTempID temp id
   */
  public void setCurTempID(int newTempID) {
    curTempID = Math.max(-1, Math.min(newTempID, IDBaseConverter.getMaxId(base)));
  }

  /**
   * DOCUMENT-ME
   */
  public void setCurTempID(String s) {
    setCurTempID("".equals(s) ? 0 : IDBaseConverter.parse(s, base));
  }

  /**
   * This method sets the current temp id.
   */
  public int getCurTempID() {
    return curTempID;
  }

  /**
   * The current file attached to the game data. If it is null, the save as dialog shall be opened.
   */
  private FileType filetype = null;

  /**
   * The 'round' this game data belongs to. Note that this imposes a restriction on how fine-grained
   * date information can be applied to game data or certain parts of it. This will probably have to
   * be changed the one or the other way.
   */
  protected Date date = null;

  /**
   * Contains the date of the report (it's not the date of the report).
   */
  protected long timestamp = 0;

  /** The 'mail' connection this game data belongs to. This may be null */
  public String mailTo = null;

  /** The 'mail' subject for this game data. This may be null */
  public String mailSubject = null;

  /**
   * A collection of all units. The keys are <tt>Integer</tt> objects containg the unit's ids. The
   * values consist of objects of class <tt>Unit</tt>. TEMP units are not included, they are only
   * stored in the unit collection of their parents and their regions and in the tempUnits map.
   * 
   * @return returns the units map
   */
  public abstract Map<UnitID, Unit> units();

  /**
   * A collection of tempUnits. The keys are <tt>Integer</tt> objects containg the unit's ids. The
   * values consist of objects of class <tt>TempUnit</tt>.
   * 
   * @return returns the tempunits map
   */
  public abstract Map<UnitID, TempUnit> tempUnits();

  /**
   * All regions in this game data. The keys are <tt>Coordinate</tt> objects containg the id of each
   * region. The values consist of objects of class <tt>Region</tt>.
   * 
   * @return returns the regions map
   */
  public abstract Map<CoordinateID, Region> regions();

  /**
   * All factions in this game data. The keys are <tt>Integer</tt> objects containg the id of each
   * faction. The values consist of objects of class <tt>Faction</tt>. One of these factions can be
   * referenced by the ownerFaction attribute.
   * 
   * @return returns the factions map
   */
  public abstract Map<EntityID, Faction> factions();

  /**
   * All buildings in this game data. The keys are <tt>Integer</tt> objects containg the id of each
   * building. The values consist of objects of class <tt>Building</tt>.
   * 
   * @return returns the buildings map
   */
  public abstract Map<EntityID, Building> buildings();

  /**
   * All ships in this game data. The keys are <tt>Integer</tt> objects containing the id of each
   * ship. The values consist of objects of class <tt>Ship</tt>.
   * 
   * @return returns the ships map
   */
  public abstract Map<EntityID, Ship> ships();

  /**
   * All message types in this game data. The keys are <tt>Integer</tt> objects containg the id of
   * each message type. The values consist of <tt>MessageType</tt> objects.
   * 
   * @return returns the messageType map
   */
  public abstract Map<IntegerID, MessageType> msgTypes();

  /**
   * All magic spells in this game data. The keys are <tt>Integer</tt> objects containg the id of
   * each spell. The values consist of objects of class <tt>Spell</tt>.
   * 
   * @return returns the spells map
   */
  public abstract Map<StringID, Spell> spells();

  /**
   * All potions in this game data. The keys are <tt>Integer</tt> objects containg the id of each
   * potion. The values consist of objects of class <tt>Potion</tt>.
   * 
   * @return returns the potions map
   */
  public abstract Map<IntegerID, Potion> potions();

  /**
   * All islands in this game data. The keys are <tt>Integer</tt> objects containing the id of each
   * island. The values consist of objects of class <tt>Island</tt>.
   * 
   * @return returns the islands map
   */
  public abstract Map<IntegerID, Island> islands();

  /**
   * All HotSpots existing for this game data. Hot spots are used to quickly access regions of
   * interest on the map. The keys are Integer representations of the hot spot id, the values are
   * Coordinate objects.
   */
  public abstract Map<IntegerID, HotSpot> hotSpots();

  /**
   * Represents the table of translations from the report.
   */
  public abstract Translations translations();

  /**
   * is set to true, if while proceeding some functions (e.g. CRParse) and we are running out of
   * memory... data may be corrupted or empty then
   */
  public boolean outOfMemory = false;

  /**
   * sortIndex is used to keep objects from CRParser to CRWriter in an order. maxSortIndex is set
   * after CRParse and Used for creation of new Objects (e.g. MapEdit Plugin) and increased.
   */
  private int maxSortIndex = 0;

  /**
   * Creates a new GameData object with the name of "default".
   * 
   * @param rules Valid rules for the game
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
    gameName = name;
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
   * Returns the AllianceGroup with the specified ID if it exists, otherwise <code>null</code>.
   */
  public AllianceGroup getAllianceGroup(ID allianceID) {
    if (alliancegroups == null)
      return null;
    return alliancegroups.get(allianceID);
  }

  /**
   * Returns a collection of all known AllianceGroups.
   */
  public Collection<AllianceGroup> getAllianceGroups() {
    if (alliancegroups == null)
      return Collections.emptyList();
    else
      return alliancegroups.values();
  }

  public void addAllianceGroup(AllianceGroup alliance) {
    if (alliancegroups == null) {
      alliancegroups = new OrderedHashtable<ID, AllianceGroup>(1);
    }
    alliancegroups.put(alliance.getID(), alliance);
  }

  /**
   * Retrieve a building from buildings() by id.
   * 
   * @param id the id of the building to be retrieved.
   * @return an instance of class <tt>Building</tt> or <tt>null</tt> if there is no building with
   *         the specified id or if buildings() is <tt>null</tt>.
   */
  public Building getBuilding(ID id) {
    return (buildings() == null) ? null : (Building) buildings().get(id);
  }

  /**
   * Retrieve a ship from ships() by id.
   * 
   * @param id the id of the ship to be retrieved.
   * @return an instance of class <tt>Ship</tt> or <tt>null</tt> if there is no ship with the
   *         specified id or if ships() is <tt>null</tt>.
   */
  public Ship getShip(ID id) {
    return (ships() == null) ? null : (Ship) ships().get(id);
  }

  /**
   * Retrieve a faction from factions() by id.
   * 
   * @param id the id of the faction to be retrieved.
   * @return an instance of class <tt>Faction</tt> or <tt>null</tt> if there is no faction with the
   *         specified id or if factions() is <tt>null</tt>.
   */
  public Faction getFaction(ID id) {
    return (factions() == null) ? null : factions().get(id);
  }

  /**
   * Retrieve a unit from units() by id.
   * 
   * @param id the id of the unit to be retrieved.
   * @return an instance of class <tt>Unit</tt> or <tt>null</tt> if there is no unit with the
   *         specified id or if units() is <tt>null</tt>.
   */
  public Unit getUnit(ID id) {
    return (units() == null) ? null : (Unit) units().get(id);
  }

  /**
   * Retrieve a region from regions() by id.
   * 
   * @param c region coordinate
   * @return an instance of class <tt>Region</tt> or <tt>null</tt> if there is no region with the
   *         specified coordinates or if regions() is <tt>null</tt>.
   */
  public Region getRegion(CoordinateID c) {
    // TODO (stm) why was this here??
    // CoordinateID id = new CoordinateID(c);
    // return (regions() == null) ? null : (Region) regions().get(id);
    return (regions() == null) ? null : regions().get(c);
  }

  /**
   * Retrieve a message type from msgTypes() by id.
   * 
   * @param id the id of the message type to be retrieved.
   * @return an instance of class <tt>MessageType</tt> or <tt>null</tt> if there is no message type
   *         with the specified id or if msgTypes() is <tt>null</tt>.
   */
  public MessageType getMsgType(ID id) {
    return (msgTypes() == null) ? null : (MessageType) msgTypes().get(id);
  }

  /**
   * Retrieve a spell from spells() by id.
   * 
   * @param id the id of the spell to be retrieved.
   * @return an instance of class <tt>Spell</tt> or <tt>null</tt> if there is no spell with the
   *         specified id or if spells() is <tt>null</tt>.
   */
  public Spell getSpell(ID id) {
    return (spells() == null) ? null : (Spell) spells().get(id);
  }

  /**
   * Retrieve a spell from spells() by Name. used for orderReader / completer
   * 
   * @param spellName the name of the spell to be retrieved.
   * @return an instance of class <tt>Spell</tt> or <tt>null</tt> if there is no spell with the
   *         specified id or if spells() is <tt>null</tt>.
   */
  public Spell getSpell(String spellName) {
    if (spells() == null || spells().size() == 0)
      return null;
    for (Spell spell : spells().values()) {
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
    return (potions() == null) ? null : (Potion) potions().get(id);
  }

  /**
   * Retrieve a island from islands() by id.
   * 
   * @param id the id of the island to be retrieved.
   * @return an instance of class <tt>Island</tt> or <tt>null</tt> if there is no island with the
   *         specified id or if islands() is <tt>null</tt>.
   */
  public Island getIsland(ID id) {
    return (islands() == null) ? null : (Island) islands().get(id);
  }

  /**
   * Add a faction to the specified game data. If factions() is <tt>null</tt>, this method has no
   * effect.
   * 
   * @param f the faction to be added.
   */
  public void addFaction(Faction f) {
    if (factions() != null) {
      factions().put(f.getID(), f);
    }
  }

  /**
   * Add a unit to the specified game data. If units() is <tt>null</tt>, this method has no effect.
   * 
   * @param u the unit to be added.
   */
  public void addUnit(Unit u) {
    if (units() != null) {
      units().put(u.getID(), u);
    }
  }

  /**
   * Add a region to the specified game data. If regions() is <tt>null</tt>, this method has no
   * effect.
   * 
   * @param r the region to be added.
   */
  public void addRegion(Region r) {
    if (regions() != null) {
      regions().put(r.getID(), r);
    }
  }

  /**
   * Add a ship to the specified game data. If ships() is <tt>null</tt>, this method has no effect.
   * 
   * @param s the ship to be added.
   */
  public void addShip(Ship s) {
    if (ships() != null) {
      ships().put(s.getID(), s);
    }
  }

  /**
   * Add a building to the specified game data. If buildings() is <tt>null</tt>, this method has no
   * effect.
   * 
   * @param b the building to be added.
   */
  public void addBuilding(Building b) {
    if (buildings() != null) {
      buildings().put(b.getID(), b);
    }
  }

  /**
   * Add a message type to the specified game data. If msgTypes() is <tt>null</tt>, this method has
   * no effect.
   * 
   * @param type the message type to be added.
   */
  public void addMsgType(MessageType type) {
    if (msgTypes() != null) {
      msgTypes().put(type.getID(), type);
    }
  }

  /**
   * Add a spell to the specified game data. If spells() is <tt>null</tt>, this method has no
   * effect.
   * 
   * @param s the spells to be added.
   */
  public void addSpell(Spell s) {
    if (spells() != null) {
      spells().put(s.getID(), s);
    }
  }

  /**
   * Add a potion to the specified game data. If potions() is <tt>null</tt>, this method has no
   * effect.
   * 
   * @param p the potion to be added.
   */
  public void addPotion(Potion p) {
    if (potions() != null) {
      potions().put(p.getID(), p);
    }
  }

  /**
   * Add an island to the specified game data. If islands() is <tt>null</tt>, this method has no
   * effect.
   * 
   * @param i the island to be added.
   */
  public void addIsland(Island i) {
    if (islands() != null) {
      islands().put(i.getID(), i);
    }
  }

  /**
   * Returns a map selected regions.
   */
  public abstract Map<CoordinateID, Region> getSelectedRegionCoordinates();

  /**
   * set a collection of selected regions.
   * 
   * @param regions the Map of coordinates of selected regions
   */
  public abstract void setSelectedRegionCoordinates(Map<CoordinateID, Region> regions);

  /**
   * Add or set a hot spot to the specified game data. If hotSpots() is <tt>null</tt>, this method
   * has no effect.
   * 
   * @param h the hot spot to be added.
   */
  public void setHotSpot(HotSpot h) {
    if (hotSpots() != null) {
      hotSpots().put(h.getID(), h);
    }
  }

  /**
   * Retrieve a hot spot from hotSpots() by its id.
   * 
   * @param id the id of the hot spot to be retrieved.
   * @return an instance of class <tt>HotSpot</tt> or <tt>null</tt> if there is no hot spot with the
   *         specified id or if hotSpots() is <tt>null</tt> .
   */
  public HotSpot getHotSpot(IntegerID id) {
    return (hotSpots() == null) ? null : (HotSpot) hotSpots().get(id);
  }

  /**
   * Remove a hot spot from hotSpots() by its id.
   * 
   * @param id the id of the hot spot to be removed.
   */
  public void removeHotSpot(IntegerID id) {
    if (hotSpots() != null) {
      hotSpots().remove(id);
    }
  }

  /**
   * Puts a translation into the translation table.
   * 
   * @param from a language independent key.
   * @param to the language dependent translation of key.
   */
  public void addTranslation(String from, String to, int source) {

    translations().addTranslation(from, to, source);
    if (rules != null) {
      // dynamically add translation key to rules to access object by name
      rules.changeName(from, to);
    }

    /*
     * if (translations() != null) { translations().put(from, to); if (rules != null) { //
     * dynamically add translation key to rules to access object by name rules.changeName(from, to);
     * } }
     */
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
   * meaningful, respecitively.
   */
  public boolean noSkillPoints = false;

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
   * Merges the specified dataset with this dataset.
   * 
   * @param gd1 the first game data object for merging
   * @param gd2 the second game data object for merging
   * @return the new merged game data object
   * @throws IllegalArgumentException if first and second game data object are from different game
   *           types.
   */
  public static GameData merge(GameData gd1, GameData gd2) {
    // make sure, the game types are the same.
    if (!gd1.getGameName().equalsIgnoreCase(gd2.getGameName()))
      throw new IllegalArgumentException("GameData.merge(): Can't merge different game types. ("
          + gd1.getGameName() + " via " + gd2.getGameName() + ")");

    // make sure that a date object is available
    if (gd1.getDate() == null) {
      gd1.setDate(new EresseaDate(0));
    }

    if (gd2.getDate() == null) {
      gd2.setDate(new EresseaDate(0));
    }

    // setting FileType to gd1
    if (gd1.filetype != null) {
      gd2.setFileType(gd1.getFileType());
    }

    if (gd1.getDate().compareTo(gd2.getDate()) > 0)
      return GameData.mergeIt(gd2, gd1);
    else
      return GameData.mergeIt(gd1, gd2);
  }

  /**
   * Merges the two game data containers yielding a third one. By convention, olderGD must not be
   * newer than newerGD. The resulting game data container inherits the rules and name from
   * <b>newerGD</b>.
   * 
   * @param olderGD A GameData object, must be the older one of the two
   * @param newerGD The newer GameData object.
   * @return the merged GameData
   */
  private static GameData mergeIt(GameData olderGD, GameData newerGD) {
    // 2002.02.20 pavkovic: the newer rules are in GameData gd2. So we take
    // them for the new GameData
    // FIXME(pavkovic) rules should be loaded instead of just used in this
    // situation
    GameData resultGD = new CompleteData(newerGD.rules, newerGD.getGameName());
    if (olderGD.getFileType() != null) {
      resultGD.setFileType(olderGD.getFileType());
    }

    /***********************************************************************/
    /**************************** ADDING PHASE *****************************/
    /***********************************************************************/

    /**************************** DATE ***************************/
    EresseaDate date = new EresseaDate(newerGD.getDate().getDate());
    date.setEpoch(((EresseaDate) newerGD.getDate()).getEpoch());
    resultGD.setDate(date);

    // new report - new timestamp
    resultGD.setTimestamp(System.currentTimeMillis() / 1000);

    // verify the encodings of the two reports
    String oldEncoding = olderGD.getEncoding();
    String newEncoding = newerGD.getEncoding();

    // GameData.log.info("Old Encoding: "+oldEncoding);
    // GameData.log.info("New Encoding: "+newEncoding);

    if (oldEncoding != null && newEncoding != null) {
      if (oldEncoding.equalsIgnoreCase(newEncoding)) {
        // do nothing
        GameData.log.debug("Do nothing");
        resultGD.setEncoding(oldEncoding);
      } else if (oldEncoding.equalsIgnoreCase(Encoding.UTF8.toString())
          || newEncoding.equalsIgnoreCase(Encoding.UTF8.toString())) {
        // if one of the reports has UTF-8 Encoding, we use it always.
        GameData.log.info("Set UTF-8 because one report match");
        resultGD.setEncoding(Encoding.UTF8.toString());
      } else {
        // okay, we have differnt encodings, but none of them is UTF-8 - what
        // now?
        GameData.log.info("Encoding does not match (" + oldEncoding + " vs. " + newEncoding
            + "), using new encoding");
        resultGD.setEncoding(newEncoding);
      }
    } else {
      // okay, this should never happen (no encoding in the reports)
      // so, we set the default encoding
      GameData.log.info("Set UTF-8 as default");
      resultGD.setEncoding(Encoding.UTF8.toString());
    }

    GameData.log.info("Old Encoding: " + oldEncoding + ",New Encoding: " + newEncoding
        + ",Result-Encoding: " + resultGD.getEncoding());

    boolean sameRound = olderGD.getDate().equals(newerGD.getDate());

    /**************************** MAIL TO, MAIL SUBJECT ***************************/
    if (newerGD.mailTo != null) {
      resultGD.mailTo = newerGD.mailTo;
    } else {
      resultGD.mailTo = olderGD.mailTo;
    }

    if (newerGD.mailSubject != null) {
      resultGD.mailSubject = newerGD.mailSubject;
    } else {
      resultGD.mailSubject = olderGD.mailSubject;
    }

    /**************************** BASE ***************************/
    if (newerGD.base != 0) {
      resultGD.base = newerGD.base;
    } else {
      resultGD.base = olderGD.base;
    }

    /**
     * Tracking an Bug warn, if we do not have 36 with eressea or vinyambar and set it to 36
     */
    String actGameName = newerGD.getGameName().toLowerCase();
    if ((actGameName.indexOf("eressea") > -1 || actGameName.indexOf("vinyambar") > -1)
        && (newerGD.base != 36)) {
      // this should not happen
      GameData.log.warn("BASE ERROR !! merged report could have not base36 !! Changed to base36.");
      newerGD.base = 36;
    }

    // NOSKILLPOINTS: the newer report determines the skill point handling
    resultGD.noSkillPoints = newerGD.noSkillPoints;

    // curTempID
    // (it is assured, that at least on of the GameData-objects
    // contains a default value for curTempID)
    if (newerGD.curTempID != -1) {
      resultGD.curTempID = newerGD.curTempID;
    } else {
      resultGD.curTempID = olderGD.curTempID;
    }

    if (olderGD.getAttributeSize() > 0) {
      for (String key : olderGD.getAttributeKeys()) {
        if (!newerGD.containsAttribute(key)) {
          newerGD.addAttribute(key, olderGD.getAttribute(key));
        }
      }
    }

    // version - we take the CR version from the newer report
    resultGD.version = newerGD.version;

    /**************************** LOCALE ***************************/
    if (sameRound) {
      // if the added report is from the same round it is the newer
      // one, but we would stay then with the current locale
      if (olderGD.getLocale() != null) {
        resultGD.setLocale(olderGD.getLocale());
      } else {
        resultGD.setLocale(newerGD.getLocale());
      }
    } else {
      // if we do not have the same round then we use the newer locale
      // as we must have a chance to change the locale by adding a report
      // but we don't want that if we add an older report.
      if (newerGD.getLocale() != null) {
        resultGD.setLocale(newerGD.getLocale());
      } else {
        resultGD.setLocale(olderGD.getLocale());
      }
    }

    /**************************** TRANSLATIONS ***************************/
    // simple objects, created and merged in one step
    // for safety we should merge the translation directly
    // after setting the local of the result report
    if (resultGD.translations() != null) {
      if (olderGD.getLocale().equals(resultGD.getLocale())) {
        resultGD.translations().addAll(olderGD.translations(), resultGD.rules);
      } else {
        resultGD.translations().clear();
      }
      if (newerGD.getLocale().equals(resultGD.getLocale())) {
        resultGD.translations().addAll(newerGD.translations(), resultGD.rules);
      }
    }

    /**************************** MESSAGETYPES ***************************/
    // simple objects, created and merged in one step
    // locale has to be considered.
    if (olderGD.msgTypes() != null) {
      for (MessageType mt : olderGD.msgTypes().values()) {
        MessageType newMT = null;

        try {
          newMT = new MessageType(mt.getID().clone());
        } catch (CloneNotSupportedException e) {
          GameData.log.error(e);
        }

        MessageType.merge(olderGD, mt, resultGD, newMT);
        resultGD.addMsgType(newMT);
      }
    }

    if (newerGD.msgTypes() != null) {
      for (MessageType mt : newerGD.msgTypes().values()) {
        MessageType newMT = resultGD.getMsgType(mt.getID());

        if (newMT == null) {
          try {
            newMT = new MessageType(mt.getID().clone());
          } catch (CloneNotSupportedException e) {
            GameData.log.error(e);
          }
        }

        MessageType.merge(newerGD, mt, resultGD, newMT);
        resultGD.addMsgType(newMT);
      }
    }

    /**************************** SPELLS ***************************/
    // simple objects, created and merged in one step
    if (olderGD.spells() != null) {
      for (Spell spell : olderGD.spells().values()) {
        Spell newSpell = null;

        try {
          newSpell = MagellanFactory.createSpell(spell.getID().clone(), resultGD);

          MagellanFactory.mergeSpell(olderGD, spell, resultGD, newSpell);
          resultGD.addSpell(newSpell);
        } catch (CloneNotSupportedException e) {
          GameData.log.error(e);
        }
      }
    }

    if (newerGD.spells() != null) {
      for (Spell spell : newerGD.spells().values()) {
        Spell newSpell = resultGD.getSpell(spell.getID());

        try {
          if (newSpell == null) {
            newSpell = MagellanFactory.createSpell(spell.getID().clone(), resultGD);
          }

          MagellanFactory.mergeSpell(newerGD, spell, resultGD, newSpell);
          resultGD.addSpell(newSpell);
        } catch (CloneNotSupportedException e) {
          GameData.log.error(e);
        }
      }
    }

    /**************************** POTIONS ***************************/
    // simple objects, created and merged in one step
    if (olderGD.potions() != null) {
      for (Potion potion : olderGD.potions().values()) {
        Potion newPotion = null;

        try {
          newPotion = MagellanFactory.createPotion(potion.getID().clone());
        } catch (CloneNotSupportedException e) {
          GameData.log.error(e);
        }

        MagellanFactory.mergePotion(olderGD, potion, resultGD, newPotion);
        resultGD.addPotion(newPotion);
      }
    }

    if (newerGD.potions() != null) {
      for (Potion potion : newerGD.potions().values()) {
        Potion newPotion = resultGD.getPotion(potion.getID());

        if (newPotion == null) {
          try {
            newPotion = MagellanFactory.createPotion(potion.getID().clone());
          } catch (CloneNotSupportedException e) {
            GameData.log.error(e);
          }
        }

        MagellanFactory.mergePotion(newerGD, potion, resultGD, newPotion);
        resultGD.addPotion(newPotion);
      }
    }

    /**************************** OWNER FACTION ***************************/
    if (olderGD.getOwnerFaction() != null) {
      resultGD.setOwnerFaction(olderGD.getOwnerFaction());
    }
    // never change owner faction
    // else
    // resultGD.setOwnerFaction(newerGD.getOwnerFaction());

    /**************************** COORDINATE TRANSLATIONS ***************************/
    for (EntityID factionID : olderGD.coordinateTranslations.keySet()) {
      for (Integer layer : olderGD.coordinateTranslations.get(factionID).keySet()) {
        CoordinateID oldTranslation = olderGD.getCoordinateTranslation(factionID, layer);
        if (oldTranslation != null) {
          resultGD.setCoordinateTranslation(factionID, oldTranslation);
        }
      }
    }
    for (EntityID factionID : newerGD.coordinateTranslations.keySet()) {
      for (Integer layer : newerGD.coordinateTranslations.get(factionID).keySet()) {
        CoordinateID oldTranslation = olderGD.getCoordinateTranslation(factionID, layer);
        CoordinateID newTranslation = newerGD.getCoordinateTranslation(factionID, layer);
        if (oldTranslation != null && newTranslation != null
            && !oldTranslation.equals(newTranslation)) {
          GameData.log.warn("coordinate translations do not match " + factionID + "," + layer + ":"
              + oldTranslation + "!=" + newTranslation);
          resultGD.setCoordinateTranslation(factionID, oldTranslation);
        } else {
          if (oldTranslation != null) {
            resultGD.setCoordinateTranslation(factionID, oldTranslation);
          } else if (newTranslation != null) {
            resultGD.setCoordinateTranslation(factionID, newTranslation);
          } else {
            GameData.log.warn("unexpected case");
          }
        }
      }
    }

    /**************************** ALLIANCES ***************************/
    if (olderGD.alliancegroups != null && sameRound) {
      for (AllianceGroup alliance : olderGD.alliancegroups.values()) {
        try {
          resultGD.addAllianceGroup(MagellanFactory.createAlliance(alliance.getID().clone(),
              resultGD));
        } catch (CloneNotSupportedException e) {
          GameData.log.error(e);
        }
      }
    }

    if (newerGD.alliancegroups != null) {
      for (AllianceGroup alliance : newerGD.alliancegroups.values()) {
        if (resultGD.getAllianceGroup(alliance.getID()) == null) {
          try {
            resultGD.addAllianceGroup(MagellanFactory.createAlliance(alliance.getID().clone(),
                resultGD));
          } catch (CloneNotSupportedException e) {
            GameData.log.error(e);
          }
        }
      }
    }

    /**************************** FACTIONS ***************************/
    // complex object, just add faction without merging here
    if (olderGD.factions() != null) {
      for (Faction f : olderGD.factions().values()) {
        try {
          resultGD.addFaction(MagellanFactory.createFaction(f.getID().clone(), resultGD));
        } catch (CloneNotSupportedException e) {
          GameData.log.error(e);
        }
      }
    }

    if (newerGD.factions() != null) {
      for (Faction f : newerGD.factions().values()) {
        if (resultGD.getFaction(f.getID()) == null) {
          try {
            resultGD.addFaction(MagellanFactory.createFaction(f.getID().clone(), resultGD));
          } catch (CloneNotSupportedException e) {
            GameData.log.error(e);
          }
        }
      }
    }

    /**************************** REGIONS ***************************/
    // complex object, just add faction without merging here
    // this just adds all the regions to newGD. No content yet.
    if (olderGD.regions() != null) {
      for (Region r : olderGD.regions().values()) {
        try {
          resultGD.addRegion(MagellanFactory.createRegion(r.getID().clone(), resultGD));
        } catch (CloneNotSupportedException e) {
          GameData.log.error(e);
        }
      }
    }

    if (newerGD.regions() != null) {
      for (Region r : newerGD.regions().values()) {
        if (resultGD.getRegion(r.getID()) == null) {
          try {
            resultGD.addRegion(MagellanFactory.createRegion(r.getID().clone(), resultGD));
          } catch (CloneNotSupportedException e) {
            GameData.log.error(e);
          }
        }
      }
    }

    /**************************** ISLANDS ***************************/
    // complex object, just add without merging here
    if (olderGD.islands() != null) {
      for (Island i : olderGD.islands().values()) {
        try {
          resultGD.addIsland(MagellanFactory.createIsland(i.getID().clone(), resultGD));
        } catch (CloneNotSupportedException e) {
          GameData.log.error(e);
        }
      }
    }

    if (newerGD.islands() != null) {
      for (Island i : newerGD.islands().values()) {
        if (olderGD.getIsland(i.getID()) == null) {
          try {
            resultGD.addIsland(MagellanFactory.createIsland(i.getID().clone(), resultGD));
          } catch (CloneNotSupportedException e) {
            GameData.log.error(e);
          }
        }
      }
    }

    /**************************** HOTSPOTS ***************************/
    // complex object, just add without merging here
    if (olderGD.hotSpots() != null) {
      for (HotSpot h : olderGD.hotSpots().values()) {
        try {
          resultGD.setHotSpot(MagellanFactory.createHotSpot(h.getID().clone()));
        } catch (CloneNotSupportedException e) {
          GameData.log.error(e);
        }
      }
    }

    if (newerGD.hotSpots() != null) {
      for (HotSpot h : newerGD.hotSpots().values()) {
        if (resultGD.getHotSpot(h.getID()) == null) {
          try {
            resultGD.setHotSpot(MagellanFactory.createHotSpot(h.getID().clone()));
          } catch (CloneNotSupportedException e) {
            GameData.log.error(e);
          }
        }
      }
    }

    /**************************** BUILDINGS ***************************/
    // complex object, just add without merging here
    if (newerGD.buildings() != null) {
      for (Building b : newerGD.buildings().values()) {
        try {
          resultGD.addBuilding(MagellanFactory.createBuilding(b.getID().clone(), resultGD));
        } catch (CloneNotSupportedException e) {
          GameData.log.error(e);
        }
      }
    }

    if (olderGD.buildings() != null) {
      // buildings are persistent.
      // Accept old buildings not occurring in the new report
      // only if there are no units in that region
      for (Building oldBuilding : olderGD.buildings().values()) {
        Building curBuilding = newerGD.getBuilding(oldBuilding.getID());

        if (curBuilding == null) {
          // check if the building disappeared because we do
          // not know the region anymore or if it was
          // destroyed
          Region curRegion = null;
          if (oldBuilding.getRegion() == null) {
            GameData.log.errorOnce("Building without Region!" + oldBuilding.toString());
          } else {
            if (oldBuilding.getRegion().getID() == null) {
              GameData.log.errorOnce("Region without ID!");
            } else {
              curRegion = newerGD.getRegion(oldBuilding.getRegion().getID());

              if ((curRegion == null) || curRegion.getVisibility().compareTo(Visibility.TRAVEL) < 0) {
                try {
                  resultGD.addBuilding(MagellanFactory.createBuilding(oldBuilding.getID().clone(),
                      resultGD));
                } catch (CloneNotSupportedException e) {
                  GameData.log.error(e);
                }
              } else {
                // skip this building
              }
            }
          }
        } else {
          // the building occurs in gd2 so we already
          // included its current version in newGD
        }
      }
    }

    /**************************** SHIPS ***************************/
    // complex object, just add without merging here
    if (sameRound && (olderGD.ships() != null)) {
      for (Ship s : olderGD.ships().values()) {
        try {
          resultGD.addShip(MagellanFactory.createShip(s.getID().clone(), resultGD));
        } catch (CloneNotSupportedException e) {
          GameData.log.error(e);
        }
      }
    }

    if (newerGD.ships() != null) {
      for (Ship s : newerGD.ships().values()) {
        if (resultGD.getShip(s.getID()) == null) {
          try {
            resultGD.addShip(MagellanFactory.createShip(s.getID().clone(), resultGD));
          } catch (CloneNotSupportedException e) {
            GameData.log.error(e);
          }
        }
      }
    }

    /**************************** UNITS ***************************/
    // complex object, just add without merging here

    /*
     * Note: To gather the information needed for level changes, report one is always treated. But
     * in the case of unequal dates only units that are also in the second report are added to the
     * new one and temp units are ignored. IDs are used for comparison.
     */
    if (olderGD.units() != null) {
      for (Unit u : olderGD.units().values()) {
        if (sameRound || (newerGD.getUnit(u.getID()) != null)) {
          // TODO (stm): Isn't that nonsense? Doesn't it suffice to add the
          // units of the new report
          // if they are not from the same round?
          try {
            resultGD.addUnit(MagellanFactory.createUnit(u.getID().clone()));
          } catch (CloneNotSupportedException e) {
            GameData.log.error(e);
          }
        }
      }
    }

    if (newerGD.units() != null) {
      for (Unit u : newerGD.units().values()) {
        if (resultGD.getUnit(u.getID()) == null) {
          try {
            resultGD.addUnit(MagellanFactory.createUnit(u.getID().clone()));
          } catch (CloneNotSupportedException e) {
            GameData.log.error(e);
          }
        }
      }
    }

    /***********************************************************************/
    /********************** MERGING PHASE -- FIRST PASS ********************/
    /***********************************************************************/

    /**************************** ALLIANCES ***************************/
    if (olderGD.alliancegroups != null && sameRound) {
      for (AllianceGroup curAlliance : olderGD.alliancegroups.values()) {
        AllianceGroup newAlliance = resultGD.getAllianceGroup(curAlliance.getID());

        MagellanFactory.mergeAlliance(olderGD, curAlliance, resultGD, newAlliance);
      }
    }

    /**************************** MERGE FACTIONS ***************************/
    // complex object FIRST PASS
    if (olderGD.factions() != null) {
      for (Faction curFaction : olderGD.factions().values()) {
        Faction newFaction = resultGD.getFaction(curFaction.getID());

        // first pass
        MagellanFactory.mergeFaction(olderGD, curFaction, resultGD, newFaction);
      }
    }

    /**************************** MERGE REGIONS ***************************/
    // complex object FIRST PASS
    if (olderGD.regions() != null) {
      for (Region oldRegion : olderGD.regions().values()) {
        Region resultRegion = resultGD.getRegion(oldRegion.getID());

        // first pass
        MagellanFactory.mergeRegion(olderGD, oldRegion, resultGD, resultRegion, !sameRound, true);
      }
    }

    /**************************** MERGE ISLANDS ***************************/
    // complex object FIRST PASS
    if (olderGD.islands() != null) {
      for (Island curIsland : olderGD.islands().values()) {
        Island newIsland = resultGD.getIsland(curIsland.getID());

        // first pass
        MagellanFactory.mergeIsland(olderGD, curIsland, resultGD, newIsland);
      }
    }

    /**************************** MERGE HOTSPOTS ***************************/
    // complex object FIRST PASS
    if (olderGD.hotSpots() != null) {
      for (HotSpot curHotSpot : olderGD.hotSpots().values()) {
        HotSpot newHotSpot = resultGD.getHotSpot(curHotSpot.getID());
        // first pass
        MagellanFactory.mergeHotSpot(olderGD, curHotSpot, resultGD, newHotSpot);
      }
    }

    /**************************** MERGE BUILDINGS ***************************/
    // complex object FIRST PASS
    if (olderGD.buildings() != null) {
      for (Building curBuilding : olderGD.buildings().values()) {
        Building newBuilding = resultGD.getBuilding(curBuilding.getID());

        if (newBuilding != null) {
          // first pass
          MagellanFactory.mergeBuilding(olderGD, curBuilding, resultGD, newBuilding);
        }
      }
    }

    /**************************** MERGE SHIPS ***************************/
    // complex object FIRST PASS
    if ((olderGD.ships() != null)) {
      for (Ship curShip : olderGD.ships().values()) {
        Ship newShip = resultGD.getShip(curShip.getID());

        // only merge ships from the "older" game data if they are from the same
        // round
        if (sameRound) {
          // first pass
          MagellanFactory.mergeShip(olderGD, curShip, resultGD, newShip);
        } else {
          // TODO (stm 2007-02-19) this is a workaround, we need a nicer
          // solution
          MagellanFactory.mergeComments(curShip, newShip);
        }
      }
    }

    /***********************************************************************/
    /********************** MERGING PHASE -- SECOND PASS *******************/
    /***********************************************************************/

    /**************************** ALLIANCES ***************************/
    if (newerGD.alliancegroups != null) {
      for (AllianceGroup curAlliance : newerGD.alliancegroups.values()) {
        AllianceGroup newAlliance = resultGD.getAllianceGroup(curAlliance.getID());

        MagellanFactory.mergeAlliance(olderGD, curAlliance, resultGD, newAlliance);
      }
    }

    /**************************** MERGE FACTIONS, SECOND PASS ***************************/
    // must be done before merging units to keep group information
    if (newerGD.factions() != null) {
      for (Faction curFaction : newerGD.factions().values()) {
        Faction newFaction = resultGD.getFaction(curFaction.getID());

        // second pass
        MagellanFactory.mergeFaction(newerGD, curFaction, resultGD, newFaction);
      }
    }

    /**************************** MERGE UNITS ***************************/

    /*
     * Note: To gather level change informations all units are used. If the dates are equal, a fully
     * merge is done, if not, only the skills are retrieved.
     */
    for (Unit resultUnit : resultGD.units().values()) {
      // find the second first since we may need the temp id
      Unit newerUnit = newerGD.findUnit(resultUnit.getID(), null, null);

      // find a temp ID to gather information out of the temp unit
      ID tempID = null;
      Region newRegion = null;

      if ((newerUnit != null) && !sameRound) {
        // only use temp ID if reports have different date
        tempID = newerUnit.getTempID();

        if (tempID != null) {
          tempID = UnitID.createUnitID(-((UnitID) tempID).intValue(), newerGD.base);
        }

        newRegion = newerUnit.getRegion();
      }

      // now get the unit of the first report
      Unit olderUnit = olderGD.findUnit(resultUnit.getID(), tempID, newRegion);
      // first merge step
      if (olderUnit != null) {
        if (sameRound) { // full merge
          MagellanFactory.mergeUnit(olderGD, olderUnit, resultGD, resultUnit, sameRound, true);
        } else { // only copy the skills to get change-level base
          if ((newerUnit != null)
              && ((newerUnit.getSkills() != null) || (olderUnit.getFaction().isPrivileged()))) {
            MagellanFactory.copySkills(olderUnit, resultUnit);
          }
        }
      }

      // second merge step
      if (newerUnit != null) {
        MagellanFactory.mergeUnit(newerGD, newerUnit, resultGD, resultUnit, sameRound, false);
      }
    }

    /**************************** MERGE REGIONS, SECOND PASS ***************************/
    if (resultGD.regions() != null) {
      for (Region resultRegion : resultGD.regions().values()) {
        Region newerRegion = newerGD.getRegion(resultRegion.getID());
        if (newerRegion != null) {
          // second pass
          MagellanFactory.mergeRegion(newerGD, newerRegion, resultGD, resultRegion, !sameRound,
              false);
        } else {
          // region not present in new report
          if (!sameRound && resultRegion.getVisibility() != Visibility.NULL) {
            GameData.log.warn("region should not be visible: " + resultRegion.getName());
            resultRegion.setVisibility(Visibility.NULL);
          }
        }
      }
    }

    /**************************** MERGE ISLANDS, SECOND PASS ***************************/
    if (newerGD.islands() != null) {
      for (Island curIsland : newerGD.islands().values()) {
        Island newIsland = resultGD.getIsland(curIsland.getID());

        // second pass
        MagellanFactory.mergeIsland(newerGD, curIsland, resultGD, newIsland);
      }
    }

    /**************************** MERGE HOTSPOTS, SECOND PASS ***************************/
    if (newerGD.hotSpots() != null) {
      for (HotSpot curHotSpot : newerGD.hotSpots().values()) {
        HotSpot newHotSpot = resultGD.getHotSpot(curHotSpot.getID());
        // second pass
        MagellanFactory.mergeHotSpot(newerGD, curHotSpot, resultGD, newHotSpot);
      }
    }

    /**************************** MERGE BUILDINGS, SECOND PASS ***************************/
    if (newerGD.buildings() != null) {
      for (Building curBuilding : newerGD.buildings().values()) {
        Building newBuilding = resultGD.getBuilding(curBuilding.getID());

        if (newBuilding != null) {
          // second pass
          MagellanFactory.mergeBuilding(newerGD, curBuilding, resultGD, newBuilding);
        }
      }
    }

    /**************************** MERGE SHIPS, SECOND PASS ***************************/
    if (newerGD.ships() != null) {
      for (Ship curShip : newerGD.ships().values()) {
        Ship newShip = resultGD.getShip(curShip.getID());

        // second pass
        MagellanFactory.mergeShip(newerGD, curShip, resultGD, newShip);
      }
    }

    resultGD.postProcess();
    resultGD.resetToUnchanged();

    return resultGD;
  }

  protected Unit findUnit(ID id, ID tempID, Region newRegion) {
    // search for a temp unit
    if (tempID != null) {
      if (newRegion == null) {
        Iterator<Unit> it = units().values().iterator();

        while (it.hasNext()) {
          Unit u = it.next();
          Unit u2 = u.getTempUnit(tempID);

          if (u2 != null)
            return u2;
        }
      } else {
        Map<CoordinateID, Region> m =
            Regions.getAllNeighbours(regions(), newRegion.getID(), 3, null);

        if (m != null) {
          Iterator<Region> it = m.values().iterator();

          while (it.hasNext()) {
            Region r = it.next();
            Unit u2 = r.getUnit(tempID);

            if (u2 != null)
              return u2;
          }
        }
      }
    }

    // standard search
    return getUnit(id);
  }

  /**
   * This function checks if the game data have been manipulated somehow (merge will lead to a
   * filetype null). TODO (stm) nobody uses this
   */
  public boolean gameDataChanged(GameData g) {
    if (g.getFileType() == null)
      return true;

    for (Unit u : g.units().values()) {
      if (u.ordersHaveChanged())
        return true;
    }

    return false;
  }

  /**
   * reset change state of all units to false
   */
  public void resetToUnchanged() {
    for (Unit u : units().values()) {
      u.setOrdersChanged(false);
    }
  }

  /**
   * returns a clone of the game data (using CRWriter/CRParser trick encapsulated in Loader)
   * 
   * @throws CloneNotSupportedException DOCUMENT-ME
   */
  @Override
  public Object clone() throws CloneNotSupportedException {
    // return new Loader().cloneGameData(this);
    return clone(new CoordinateID(0, 0));
  }

  /**
   * returns a clone of the game data (using CRWriter/CRParser trick encapsulated in Loader) and at
   * the same time translates the origin two <code>newOrigin</code>
   * 
   * @throws CloneNotSupportedException DOCUMENT-ME
   */
  public Object clone(CoordinateID newOrigin) throws CloneNotSupportedException {
    // if (newOrigin.x == 0 && newOrigin.y == 0) {
    // GameData.log.info("no need to clone - same origin");
    // return this.clone();
    // }
    if (MemoryManagment.isFreeMemory(estimateSize() * 3)) {
      GameData.log.info("cloning in memory");
      GameData clonedData = new Loader().cloneGameDataInMemory(this, newOrigin);
      if (clonedData == null || clonedData.outOfMemory) {
        GameData.log.info("cloning externally after failed memory-clone-attempt");
        clonedData = new Loader().cloneGameData(this, newOrigin);
      }
      return clonedData;
    }
    GameData.log.info("cloning externally");
    return new Loader().cloneGameData(this, newOrigin);

  }

  /**
   * Returns an estimate of the memory (in bytes) needed to store this game data. This can be a very
   * rough estimate!
   * 
   * @return An estimate of the memory needed to store this game data
   */
  public abstract long estimateSize();

  /**
   * Provides the encapsulating of game specific stuff
   */
  public GameSpecificStuff getGameSpecificStuff() {
    return rules.getGameSpecificStuff();
  }

  /** Post processes the game data (if necessary) once */
  private boolean postProcessed = false;

  private Region activeRegion;

  /**
   * This method can be called after loading or merging a report to avoid double messages and to set
   * some game specific stuff.
   */
  public void postProcess() {
    if (postProcessed)
      return;
    GameData.log.info("start GameData postProcess");

    // enforce locale to be non-null
    postProcessLocale();

    // attach Regions to Islands
    MagellanFactory.postProcess(this);

    // remove double messages
    postProcessMessages();

    // do game specific post processing
    getGameSpecificStuff().postProcess(this);

    // adding Default Translations to the translations
    postProcessDefaultTranslations();

    // remove double potions
    postProcessPotions();

    postProcessed = true;

    GameData.log.info("finished GameData postProcess");
  }

  /**
   * adding Default Translations to the translations
   */
  private void postProcessDefaultTranslations() {
    // Skilltypes
    for (Iterator<SkillType> iter = rules.getSkillTypeIterator(); iter.hasNext();) {
      SkillType skillType = iter.next();
      String key = "skill." + skillType.getID().toString();
      if (!translations().contains(key)) {
        // we have to add
        String translated = Resources.getRuleItemTranslation(key);
        addTranslation(key, translated, TranslationType.sourceMagellan);
      }
    }
  }

  /**
   * scans the regions for missing regions, for regions with regionType "The Void" or "Leere" These
   * Regions are not created in the world on the server, but we are so near, that we should have
   * some information about it. So we add these Regions with the special RegionType "Leere"
   */
  public void postProcessTheVoid() {
    List<Region> newRegions = new ArrayList<Region>();
    for (CoordinateID actRegionID : regions().keySet()) {
      Region actRegion = regions().get(actRegionID);
      boolean shouldHaveAllNeighbours = false;
      if (actRegion.getVisibility().compareTo(Region.Visibility.TRAVEL) >= 0) {
        shouldHaveAllNeighbours = true;
      }
      if (shouldHaveAllNeighbours) {
        CoordinateID center = actRegion.getCoordinate();

        int radius = 1;
        for (int dx = -radius; dx <= radius; dx++) {
          for (int dy = (-radius + Math.abs(dx)) - ((dx > 0) ? dx : 0); dy <= ((radius - Math
              .abs(dx)) - ((dx < 0) ? dx : 0)); dy++) {
            CoordinateID c = new CoordinateID(0, 0, center.z);
            c.x = center.x + dx;
            c.y = center.y + dy;

            Region neighbour = regions().get(c);

            if (neighbour == null) {
              // Missing Neighbor
              Region r = MagellanFactory.createRegion(c, this);
              RegionType type = RegionType.unknown;
              r.setType(type);
              r.setName(Resources.get("completedata.region.thevoid.name"));
              r.setDescription(Resources.get("completedata.region.thevoid.beschr"));
              newRegions.add(r);
              addTranslation("Leere", Resources.get("completedata.region.thevoid.name"),
                  TranslationType.sourceMagellan);
            }
          }
        }
      }
    }
    if (newRegions.size() > 0) {
      for (Region actRegion : newRegions) {
        if (!regions().containsKey(actRegion.getID())) {
          addRegion(actRegion);
        }
      }
    }
  }

  /**
   * Adds the order locale of Magellan if locale is null. This should prevent some NPE with the
   * sideeffect to store a locale in a locale-less game data object.
   */
  private void postProcessLocale() {
    if (getLocale() == null) {
      setLocale(Locales.getOrderLocale());
    }
  }

  /**
   * This function post processes the message blocks to remove duplicate messages. In former times
   * this has been done while loading the game data but this had a negative time tradeoff (O(n^2)).
   * This functions needs about O(n log n).
   */
  private void postProcessMessages() {
    // faction.messages
    for (Faction o : factions().values()) {
      postProcessMessages(o.getMessages());
    }

    // region.messages
    for (Region o : regions().values()) {
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
  private void postProcessPotions() {
    if (potions() == null || potions().size() == 0)
      // nothing to do
      return;
    // just for info:
    int count_before = potions().size();
    // The final Map of the potions
    HashMap<String, Potion> potions = new HashMap<String, Potion>();
    // To sort the Potions after ID we use a simple List
    // and fill it with our Potions
    List<Potion> sortedPotions = new LinkedList<Potion>(potions().values());
    // use normal name and ID comparator
    Comparator<Unique> idCmp = IDComparator.DEFAULT;
    Collections.sort(sortedPotions, new NameComparator(idCmp));

    // fill our new PotionMap with the PotionNames as Keys
    for (Potion p : sortedPotions) {
      // some info, if replacing
      if (potions.containsKey(p.getName().toLowerCase())) {
        Potion oldPotion = potions.get(p.getName().toLowerCase());
        GameData.log.info("removing Potion " + oldPotion.getName() + "(ID: "
            + oldPotion.getID().toString() + ")");
      }
      potions.put(p.getName().toLowerCase(), p);
    }
    // we have a clean PotionMap now -> set the real Potion Map
    // erease all potions
    potions().clear();
    // fill again from our PotionMap
    for (Potion p : potions.values()) {
      potions().put(p.getID(), p);
    }
    // ready
    // some info?
    int count_after = potions().size();
    if (count_before != count_after) {
      GameData.log.info("postProcessPotions: changing number of potions from " + count_before
          + " to " + count_after);
    }

  }

  /**
   * Postprocess a given list of messages. To remove duplicate messages we put all messages in an
   * ordered hashtable and put them back into the messages collection.
   */
  private void postProcessMessages(Collection<Message> messages) {
    if (messages == null)
      return;

    Map<Message, Message> ht = new OrderedHashtable<Message, Message>();

    for (Message msg : messages) {
      if (ht.put(msg, msg) != null) {
        if (1 == 2) {
          GameData.log.warn("Duplicate message \"" + msg.getText() + "\" found, removing it.");
        }
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
    List<CoordinateID> delRegionID = new ArrayList<CoordinateID>();
    for (CoordinateID actRegionID : regions().keySet()) {
      Region actRegion = regions().get(actRegionID);
      if (actRegion.getRegionType().equals(rules.getRegionType("Leere"))) {
        delRegionID.add(actRegionID);
      }
    }
    if (delRegionID.size() > 0) {
      for (CoordinateID actID : delRegionID) {
        regions().remove(actID);
      }
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
   * Sets the mapping for astral to real space.
   * 
   * @param c the real space <code>CoordianteID</code> <x,y,0> which is the center of the astral
   *          space region with CoordinateID <0,0,1>.
   */
  // public abstract void setAstralMapping(CoordinateID c);
  /**
   * Returns the relation of two map layers.
   * 
   * @return the <code>CoordinateID</code> of the toLevel region which is accessable by the
   *         fromLevel region with CoordinateID <0, 0, fromLevel>.
   */
  public LevelRelation getLevelRelation(int fromLevel, int toLevel) {
    Map<Integer, LevelRelation> relations = levelRelations.get(toLevel);
    if (relations == null) {
      relations = new HashMap<Integer, LevelRelation>();
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
   * @param data
   * @param level
   * @return Mapped Coordinate
   */
  public CoordinateID getRelatedCoordinate(CoordinateID c, int level) {
    LevelRelation lr = getLevelRelation(c.z, level);
    if (lr == null)
      return null;
    return lr.getRelatedCoordinate(c);
  }

  public CoordinateID getRelatedCoordinateUI(CoordinateID c, int level) {
    CoordinateID result = getRelatedCoordinate(c, level);
    if (result == null) {
      LevelRelation lr = getLevelRelation(level, c.z);
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
      layerMap = new HashMap<Integer, CoordinateID>();
      coordinateTranslations.put(otherFaction, layerMap);
    }
    layerMap.put(usedTranslation.z, usedTranslation);
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

  public String getGameName() {
    return gameName;
  }

  public void setGameName(String newName) {
    gameName = newName;
  }

  public void setFileType(FileType filetype) {
    this.filetype = filetype;
  }

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

}
