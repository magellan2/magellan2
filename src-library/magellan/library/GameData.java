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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import magellan.library.utils.logging.Logger;
import magellan.library.utils.mapping.LevelRelation;

/**
 * This is the central class for collecting all the data representing one
 * computer report.
 * <p>
 * The maps units, regions and so on are declared as abstract methods and the
 * getX and addX provide access to them. This allows for subclasses that
 * implicitly represent only a certain part of the game data by declaring
 * certain maps as <tt>null</tt> and returning <tt>null</tt> on the
 * corresponding getX() methods. This concept has so far not been applied and
 * you usually operate on the <tt>CompleteData</tt> subclass.
 * </p>
 */
public abstract class GameData implements Cloneable {
  private static final Logger log = Logger.getInstance(GameData.class);

  /** Game specific and usually fixed data (like races etc.). */
  public final Rules rules;

  /** The name of the game. */
  public String gameName;

  /** encoding */
  protected String encoding = FileType.DEFAULT_ENCODING.toString();

  private EntityID ownerFaction;

  private Map<EntityID, Map<Integer, CoordinateID>> coordinateTranslations = new HashMap<EntityID, Map<Integer,CoordinateID>>();

  private Map<Integer, Map<Integer, LevelRelation>> levelRelations = new HashMap<Integer, Map<Integer,LevelRelation>>();

  /**
   * The current TempUnit-ID. This means, if a new TempUnit is created, it's
   * suggested ID is usually curTempID and if this suggestion is accepted by the
   * user (which means, that a TempUnit with this id was created) curTempID is
   * increased. A value of -1 indicates, that the variable is uninitialized and
   * a value of 0 that the old system shall be used (which means, that the
   * suggested temp id shall be calculated out of the id of the parent unit of
   * the tempunit).
   */
  protected int curTempID = -1;

  /**
   * This method sets the current temp id with respect to the possible max value
   * of the current base. The value also has to be >= -1
   * 
   * @param newTempID
   *          temp id
   */
  public void setCurTempID(int newTempID) {
    curTempID = Math.max(-1, Math.min(newTempID, IDBaseConverter.getMaxId(this.base)));
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
   * The current file attached to the game data. If it is null, the save as
   * dialog shall be opened.
   */
  private FileType filetype = null;

  /**
   * The 'round' this game data belongs to. Note that this imposes a restriction
   * on how fine-grained date information can be applied to game data or certain
   * parts of it. This will probably have to be changed the one or the other
   * way.
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
   * A collection of all units. The keys are <tt>Integer</tt> objects containg
   * the unit's ids. The values consist of objects of class <tt>Unit</tt>.
   * TEMP units are not included, they are only stored in the unit collection of
   * their parents and their regions and in the tempUnits map.
   * 
   * @return returns the units map
   */
  public abstract Map<ID, Unit> units();

  /**
   * A collection of tempUnits. The keys are <tt>Integer</tt> objects containg
   * the unit's ids. The values consist of objects of class <tt>TempUnit</tt>.
   * 
   * @return returns the tempunits map
   */
  public abstract Map<ID, TempUnit> tempUnits();

  /**
   * All regions in this game data. The keys are <tt>Coordinate</tt> objects
   * containg the id of each region. The values consist of objects of class
   * <tt>Region</tt>.
   * 
   * @return returns the regions map
   */
  public abstract Map<CoordinateID, Region> regions();

  /**
   * All factions in this game data. The keys are <tt>Integer</tt> objects
   * containg the id of each faction. The values consist of objects of class
   * <tt>Faction</tt>. One of these factions can be referenced by the
   * ownerFaction attribute.
   * 
   * @return returns the factions map
   */
  public abstract Map<ID, Faction> factions();

  /**
   * All buildings in this game data. The keys are <tt>Integer</tt> objects
   * containg the id of each building. The values consist of objects of class
   * <tt>Building</tt>.
   * 
   * @return returns the buildings map
   */
  public abstract Map<ID, Building> buildings();

  /**
   * All ships in this game data. The keys are <tt>Integer</tt> objects
   * containg the id of each ship. The values consist of objects of class
   * <tt>Ship</tt>.
   * 
   * @return returns the ships map
   */
  public abstract Map<ID, Ship> ships();

  /**
   * All message types in this game data. The keys are <tt>Integer</tt>
   * objects containg the id of each message type. The values consist of
   * <tt>MessageType</tt> objects.
   * 
   * @return returns the messageType map
   */
  public abstract Map<ID, MessageType> msgTypes();

  /**
   * All magic spells in this game data. The keys are <tt>Integer</tt> objects
   * containg the id of each spell. The values consist of objects of class
   * <tt>Spell</tt>.
   * 
   * @return returns the spells map
   */
  public abstract Map<ID, Spell> spells();

  /**
   * All potions in this game data. The keys are <tt>Integer</tt> objects
   * containg the id of each potion. The values consist of objects of class
   * <tt>Potion</tt>.
   * 
   * @return returns the potions map
   */
  public abstract Map<ID, Potion> potions();

  /**
   * All islands in this game data. The keys are <tt>Integer</tt> objects
   * containing the id of each island. The values consist of objects of class
   * <tt>Island</tt>.
   * 
   * @return returns the islands map
   */
  public abstract Map<ID, Island> islands();

  /**
   * All HotSpots existing for this game data. Hot spots are used to quickly
   * access regions of interest on the map. The keys are Integer representations
   * of the hot spot id, the values are Coordinate objects.
   */
  public abstract Map<ID, HotSpot> hotSpots();

  /**
   * Represents the table of translations from the report.
   */
  public abstract Translations translations();

  /**
   * is set to true, if while proceeding some functions (e.g. CRParse) and we
   * are running out of memory... data may be corrupted or empty then
   */
  public boolean outOfMemory = false;
  
  /**
   * sortIndex is used to keep objects from CRParser to CRWriter 
   * in an order.
   * maxSortIndex is set after CRParse and Used for creation of new
   * Objects (e.g. MapEdit Plugin) and increased.
   */
  private int maxSortIndex = 0;
  

  /**
   * Creates a new GameData object.
   */
  public GameData(Rules _rules) {
    this(_rules, "default");
  }

  /**
   * Creates a new GameData object.
   * 
   * @throws NullPointerException
   *           DOCUMENT-ME
   */
  public GameData(Rules _rules, String _name) {
    if (_rules == null) {
      throw new NullPointerException();
    }

    rules = _rules;
    gameName = _name;
  }

  /**
   * Retrieve a building from buildings() by id.
   * 
   * @param id
   *          the id of the building to be retrieved.
   * @return an instance of class <tt>Building</tt> or <tt>null</tt> if
   *         there is no building with the specified id or if buildings() is
   *         <tt>null</tt>.
   */
  public Building getBuilding(ID id) {
    return (buildings() == null) ? null : (Building) buildings().get(id);
  }

  /**
   * Retrieve a ship from ships() by id.
   * 
   * @param id
   *          the id of the ship to be retrieved.
   * @return an instance of class <tt>Ship</tt> or <tt>null</tt> if there is
   *         no ship with the specified id or if ships() is <tt>null</tt>.
   */
  public Ship getShip(ID id) {
    return (ships() == null) ? null : (Ship) ships().get(id);
  }

  /**
   * Retrieve a faction from factions() by id.
   * 
   * @param id
   *          the id of the faction to be retrieved.
   * @return an instance of class <tt>Faction</tt> or <tt>null</tt> if there
   *         is no faction with the specified id or if factions() is
   *         <tt>null</tt>.
   */
  public Faction getFaction(ID id) {
    return (factions() == null) ? null : factions().get(id);
  }

  /**
   * Retrieve a unit from units() by id.
   * 
   * @param id
   *          the id of the unit to be retrieved.
   * @return an instance of class <tt>Unit</tt> or <tt>null</tt> if there is
   *         no unit with the specified id or if units() is <tt>null</tt>.
   */
  public Unit getUnit(ID id) {
    return (units() == null) ? null : (Unit) units().get(id);
  }

  /**
   * Retrieve a region from regions() by id.
   * 
   * @param c
   *          region coordinate
   * @return an instance of class <tt>Region</tt> or <tt>null</tt> if there
   *         is no region with the specified coordinates or if regions() is
   *         <tt>null</tt>.
   */
  public Region getRegion(CoordinateID c) {
    CoordinateID id = new CoordinateID(c);
    return (regions() == null) ? null : (Region) regions().get(id);
  }

  /**
   * Retrieve a message type from msgTypes() by id.
   * 
   * @param id
   *          the id of the message type to be retrieved.
   * @return an instance of class <tt>MessageType</tt> or <tt>null</tt> if
   *         there is no message type with the specified id or if msgTypes() is
   *         <tt>null</tt>.
   */
  public MessageType getMsgType(ID id) {
    return (msgTypes() == null) ? null : (MessageType) msgTypes().get(id);
  }

  /**
   * Retrieve a spell from spells() by id.
   * 
   * @param id
   *          the id of the spell to be retrieved.
   * @return an instance of class <tt>Spell</tt> or <tt>null</tt> if there
   *         is no spell with the specified id or if spells() is <tt>null</tt>.
   */
  public Spell getSpell(ID id) {
    return (spells() == null) ? null : (Spell) spells().get(id);
  }

  /**
   * Retrieve a spell from spells() by Name. used for orderReader / completer
   * 
   * @param spellName
   *          the name of the spell to be retrieved.
   * @return an instance of class <tt>Spell</tt> or <tt>null</tt> if there
   *         is no spell with the specified id or if spells() is <tt>null</tt>.
   */
  public Spell getSpell(String spellName) {
    if (spells() == null || spells().size() == 0) {
      return null;
    }
    for (Iterator iter = spells().values().iterator(); iter.hasNext();) {
      Spell spell = (Spell) iter.next();
      if (spell.getName().equalsIgnoreCase(spellName)) {
        return spell;
      }
    }
    return null;
  }

  /**
   * Retrieve a potion from potions() by id.
   * 
   * @param id
   *          the id of the potion to be retrieved.
   * @return an instance of class <tt>Potion</tt> or <tt>null</tt> if there
   *         is no potion with the specified id or if potions() is <tt>null</tt>.
   */
  public Potion getPotion(ID id) {
    return (potions() == null) ? null : (Potion) potions().get(id);
  }

  /**
   * Retrieve a island from islands() by id.
   * 
   * @param id
   *          the id of the island to be retrieved.
   * @return an instance of class <tt>Island</tt> or <tt>null</tt> if there
   *         is no island with the specified id or if islands() is <tt>null</tt>.
   */
  public Island getIsland(ID id) {
    return (islands() == null) ? null : (Island) islands().get(id);
  }

  /**
   * Add a faction to the specified game data. If factions() is <tt>null</tt>,
   * this method has no effect.
   * 
   * @param f
   *          the faction to be added.
   */
  public void addFaction(Faction f) {
    if (factions() != null) {
      factions().put(f.getID(), f);
    }
  }

  /**
   * Add a unit to the specified game data. If units() is <tt>null</tt>, this
   * method has no effect.
   * 
   * @param u
   *          the unit to be added.
   */
  public void addUnit(Unit u) {
    if (units() != null) {
      units().put(u.getID(), u);
    }
  }

  /**
   * Add a region to the specified game data. If regions() is <tt>null</tt>,
   * this method has no effect.
   * 
   * @param r
   *          the region to be added.
   */
  public void addRegion(Region r) {
    if (regions() != null) {
      regions().put((CoordinateID) r.getID(), r);
    }
  }

  /**
   * Add a ship to the specified game data. If ships() is <tt>null</tt>, this
   * method has no effect.
   * 
   * @param s
   *          the ship to be added.
   */
  public void addShip(Ship s) {
    if (ships() != null) {
      ships().put(s.getID(), s);
    }
  }

  /**
   * Add a building to the specified game data. If buildings() is <tt>null</tt>,
   * this method has no effect.
   * 
   * @param b
   *          the building to be added.
   */
  public void addBuilding(Building b) {
    if (buildings() != null) {
      buildings().put(b.getID(), b);
    }
  }

  /**
   * Add a message type to the specified game data. If msgTypes() is
   * <tt>null</tt>, this method has no effect.
   * 
   * @param type
   *          the message type to be added.
   */
  public void addMsgType(MessageType type) {
    if (msgTypes() != null) {
      msgTypes().put(type.getID(), type);
    }
  }

  /**
   * Add a spell to the specified game data. If spells() is <tt>null</tt>,
   * this method has no effect.
   * 
   * @param s
   *          the spells to be added.
   */
  public void addSpell(Spell s) {
    if (spells() != null) {
      spells().put(s.getID(), s);
    }
  }

  /**
   * Add a pption to the specified game data. If potions() is <tt>null</tt>,
   * this method has no effect.
   * 
   * @param p
   *          the potion to be added.
   */
  public void addPotion(Potion p) {
    if (potions() != null) {
      potions().put(p.getID(), p);
    }
  }

  /**
   * Add an island to the specified game data. If islands() is <tt>null</tt>,
   * this method has no effect.
   * 
   * @param i
   *          the island to be added.
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
   * @param regions
   *          the Map of coordinates of selected regions
   */
  public abstract void setSelectedRegionCoordinates(Map<CoordinateID, Region> regions);

  /**
   * Add or set a hot spot to the specified game data. If hotSpots() is
   * <tt>null</tt>, this method has no effect.
   * 
   * @param h
   *          the hot spot to be added.
   */
  public void setHotSpot(HotSpot h) {
    if (hotSpots() != null) {
      hotSpots().put(h.getID(), h);
    }
  }

  /**
   * Retrieve a hot spot from hotSpots() by its id.
   * 
   * @param id
   *          the id of the hot spot to be retrieved.
   * @return an instance of class <tt>HotSpot</tt> or <tt>null</tt> if there
   *         is no hot spot with the specified id or if hotSpots() is
   *         <tt>null</tt>.
   */
  public HotSpot getHotSpot(ID id) {
    return (hotSpots() == null) ? null : (HotSpot) hotSpots().get(id);
  }

  /**
   * Remove a hot spot from hotSpots() by its id.
   * 
   * @param id
   *          the id of the hot spot to be removed.
   */
  public void removeHotSpot(ID id) {
    if (hotSpots() != null) {
      hotSpots().remove(id);
    }
  }

  /**
   * Puts a translation into the translation table.
   * 
   * @param from
   *          a language independent key.
   * @param to
   *          the language dependent translation of key.
   */
  public void addTranslation(String from, String to, int source) {
    
    this.translations().addTranslation(from, to, source);
    if (rules != null) {
      // dynamically add translation key to rules to access object by name
      rules.changeName(from, to);
    }
    
    
    /*
    if (translations() != null) {
      translations().put(from, to);

      if (rules != null) {
        // dynamically add translation key to rules to access object by name
        rules.changeName(from, to);
      }
    }
    */
  }

  /**
   * Retrieve a translation from translations().
   * 
   * @param key
   *          the key of the translation to be retrieved.
   * @return an instance of class <tt>String</tt>. If no translation could be
   *         found, the name of the object is returned.
   */
  public String getTranslation(Named key) {
    return key == null ? null : getTranslation(key.getName());
  }

  /**
   * Retrieve a translation from translations().
   * 
   * @param key
   *          the key of the translation to be retrieved.
   * @return an instance of class <tt>String</tt>. If no translation could be
   *         found, the key is returned.
   */
  public String getTranslation(String key) {
    
    return this.translations().getTranslation(key);
    /*
    String retVal = (key == null || translations() == null) ? null : (String) translations().get(key);

    return retVal != null ? retVal : key;
    */
  }

  /**
   * Set a date, or a 'round', for this game data.
   * 
   * @param d
   *          the new date.
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
   * The base (radix) in which ids are interpreted. The default value is 10.
   * Note that all internal and cr representation is always decimal.
   */
  public int base = 10;

  /**
   * Indicates whether in this report skill points are to be expected or whether
   * they are meaningful, respecitively.
   */
  public boolean noSkillPoints = false;


  /**
   * Sets the valid locale for this report. Currently, this is only used to
   * remember this setting and write it back into the cr.
   */
  public abstract void setLocale(Locale l);

  /**
   * Returns the locale of this report. Currently, this is only used to remember
   * this setting and write it back into the cr.
   */
  public abstract Locale getLocale();

  /**
   * Merges the specified dataset with this dataset.
   * 
   * @param gd1 the first game data object for merging
   * @param gd2 the second game data object for merging
   * @return the new merged game data object
   * @throws IllegalArgumentException
   *           if first and second game data object are from different game
   *           types.
   */
  public static GameData merge(GameData gd1, GameData gd2) {
    // make sure, the game types are the same.
    if (!gd1.getGameName().equalsIgnoreCase(gd2.getGameName())) {
      throw new IllegalArgumentException("GameData.merge(): Can't merge different game types. (" + gd1.getGameName() + " via " + gd2.getGameName() + ")");
    }

    // make sure that a date object is available
    if (gd1.getDate() == null) {
      gd1.setDate(new EresseaDate(0));
    }

    if (gd2.getDate() == null) {
      gd2.setDate(new EresseaDate(0));
    }

    if (gd1.getDate().compareTo(gd2.getDate()) > 0) {
      return mergeIt(gd2, gd1);
    } else {
      return mergeIt(gd1, gd2);
    }
  }

  /**
   * Merges the two game data containers yielding a third one. By convention,
   * olderGD must not be newer than newerGD. The resulting game data container
   * inherits the rules and name from <b>newerGD</b>.
   * 
   * @param olderGD
   *          A GameData object, must be the older one of the two
   * @param newerGD
   *          The newer GameData object.
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

    // DATE
    EresseaDate date = new EresseaDate(newerGD.getDate().getDate());
    date.setEpoch(((EresseaDate) newerGD.getDate()).getEpoch());
    resultGD.setDate(date);
    
    // new report - new timestamp
    resultGD.setTimestamp(System.currentTimeMillis() / 1000);
    
    // verify the encodings of the two reports
    String oldEncoding = olderGD.getEncoding();
    String newEncoding = newerGD.getEncoding();
    
    log.info("Old Encoding: "+oldEncoding);
    log.info("New Encoding: "+newEncoding);
    
    if (oldEncoding != null && newEncoding != null) {
      if (oldEncoding.equalsIgnoreCase(newEncoding)) {
        // do nothing
        log.debug("Do nothing");
        resultGD.setEncoding(oldEncoding);
      } else if (oldEncoding.equalsIgnoreCase(Encoding.UTF8.toString()) || newEncoding.equalsIgnoreCase(Encoding.UTF8.toString())) {
        // if one of the reports has UTF-8 Encoding, we use it always.
        log.info("Set UTF-8 because one report match");
        resultGD.setEncoding(Encoding.UTF8.toString());
      } else {
        // okay, we have differnt encodings, but none of them is UTF-8 - what now?
        log.info("Encoding does not match ("+oldEncoding+" vs. "+newEncoding+"), using new encoding");
        resultGD.setEncoding(newEncoding);
      }
    } else {
      // okay, this should never happen (no encoding in the reports)
      // so, we set the default encoding
      log.info("Set UTF-8 as default");
      resultGD.setEncoding(Encoding.UTF8.toString());
    }
    
    log.info("Result: "+resultGD.getEncoding());
    

    boolean sameRound = olderGD.getDate().equals(newerGD.getDate());

    // MAIL TO, MAIL SUBJECT
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

    // BASE
    if (newerGD.base != 0) {
      resultGD.base = newerGD.base;
    } else {
      resultGD.base = olderGD.base;
    }

    /**
     * Tracking an Bug warn, if we do not have 36 with eressea or vinyambar and
     * set it to 36
     */
    String actGameName = newerGD.getGameName().toLowerCase();
    if ((actGameName.indexOf("eressea") > -1 || actGameName.indexOf("vinyambar") > -1) && (newerGD.base != 36)) {
      // this should not happen
      log.warn("BASE ERROR !! merged report could have not base36 !! Changed to base36.");
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

    // LOCALE
    if (sameRound) {
      // if the added report is from the same round it is the newer
      // one, but we would stay then with the current locale
      if (olderGD.getLocale() != null) {
        resultGD.setLocale(olderGD.getLocale());
      } else {
        resultGD.setLocale(newerGD.getLocale());
      }
    } else {
      // if we dont have the same round then we use the newer local
      // as we must have a chance to change the local by adding a report
      // but we don't want that if we add an older report.
      if (newerGD.getLocale() != null) {
        resultGD.setLocale(newerGD.getLocale());
      } else {
        resultGD.setLocale(olderGD.getLocale());
      }      
    }     

    // TRANSLATIONS
    // simple objects, created and merged in one step
    // for safety we should merge the translation directly 
    // after setting the local of the result report  
    if (resultGD.translations() != null) {
      if (olderGD.getLocale().equals(resultGD.getLocale())) {
        resultGD.translations().addAll(olderGD.translations(), resultGD.rules);
        /*
        for (Iterator iter = olderGD.translations().keySet().iterator(); iter.hasNext();) {
          String key = (String) iter.next();
          String translation = (String) olderGD.translations().get(key);
          resultGD.addTranslation(key, translation);
        }
        */
      } else {
        resultGD.translations().clear();
      }
      if (newerGD.getLocale().equals(resultGD.getLocale())) {
        resultGD.translations().addAll(newerGD.translations(), resultGD.rules);
        /*
        for (Iterator iter = newerGD.translations().keySet().iterator(); iter.hasNext();) {
          String key = (String) iter.next();
          String translation = (String) newerGD.translations().get(key);
          resultGD.addTranslation(key, translation);
        }
        */
      }
    }

    // MESSAGETYPES
    // simple objects, created and merged in one step
    // locale has to be considered. 
    if (olderGD.msgTypes() != null) {
      for (Iterator<MessageType> iter = olderGD.msgTypes().values().iterator(); iter.hasNext();) {
        MessageType mt = iter.next();
        MessageType newMT = null;

        try {
          newMT = new MessageType((ID) mt.getID().clone());
        } catch (CloneNotSupportedException e) {
          log.error(e);
        }

        MessageType.merge(olderGD, mt, resultGD, newMT);
        resultGD.addMsgType(newMT);
      }
    }

    if (newerGD.msgTypes() != null) {
      for (Iterator<MessageType> iter = newerGD.msgTypes().values().iterator(); iter.hasNext();) {
        MessageType mt = iter.next();
        MessageType newMT = resultGD.getMsgType(mt.getID());

        if (newMT == null) {
          try {
            newMT = new MessageType((ID) mt.getID().clone());
          } catch (CloneNotSupportedException e) {
            log.error(e);
          }
        }

        MessageType.merge(newerGD, mt, resultGD, newMT);
        resultGD.addMsgType(newMT);
      }
    }

    // SPELLS
    // simple objects, created and merged in one step
    if (olderGD.spells() != null) {
      for (Iterator<Spell> iter = olderGD.spells().values().iterator(); iter.hasNext();) {
        Spell spell = iter.next();
        Spell newSpell = null;

        try {
          newSpell = MagellanFactory.createSpell((ID) spell.getID().clone(),resultGD);
        } catch (CloneNotSupportedException e) {
          log.error(e);
        }

        MagellanFactory.mergeSpell(olderGD, spell, resultGD, newSpell);
        resultGD.addSpell(newSpell);
      }
    }

    if (newerGD.spells() != null) {
      for (Iterator<Spell> iter = newerGD.spells().values().iterator(); iter.hasNext();) {
        Spell spell = iter.next();
        Spell newSpell = resultGD.getSpell(spell.getID());

        if (newSpell == null) {
          try {
            newSpell = MagellanFactory.createSpell((ID) spell.getID().clone(),resultGD);
          } catch (CloneNotSupportedException e) {
            log.error(e);
          }
        }

        MagellanFactory.mergeSpell(newerGD, spell, resultGD, newSpell);
        resultGD.addSpell(newSpell);
      }
    }

    // POTIONS
    // simple objects, created and merged in one step
    if (olderGD.potions() != null) {
      for (Iterator<Potion> iter = olderGD.potions().values().iterator(); iter.hasNext();) {
        Potion potion = iter.next();
        Potion newPotion = null;

        try {
          newPotion = MagellanFactory.createPotion((ID) potion.getID().clone());
        } catch (CloneNotSupportedException e) {
          log.error(e);
        }

        MagellanFactory.mergePotion(olderGD, potion, resultGD, newPotion);
        resultGD.addPotion(newPotion);
      }
    }

    if (newerGD.potions() != null) {
      for (Iterator<Potion> iter = newerGD.potions().values().iterator(); iter.hasNext();) {
        Potion potion = iter.next();
        Potion newPotion = resultGD.getPotion(potion.getID());

        if (newPotion == null) {
          try {
            newPotion = MagellanFactory.createPotion((ID) potion.getID().clone());
          } catch (CloneNotSupportedException e) {
            log.error(e);
          }
        }

        MagellanFactory.mergePotion(newerGD, potion, resultGD, newPotion);
        resultGD.addPotion(newPotion);
      }
    }

    if (olderGD.getOwnerFaction()!=null){
      resultGD.setOwnerFaction(olderGD.getOwnerFaction());
    }
    // never change owner faction
//  else
//    resultGD.setOwnerFaction(newerGD.getOwnerFaction());

    for (EntityID factionID : olderGD.coordinateTranslations.keySet()){
      for (Integer layer : olderGD.coordinateTranslations.get(factionID).keySet()){
        CoordinateID oldTranslation = olderGD.getCoordinateTranslation(factionID, layer);
        if (oldTranslation!=null)
          resultGD.setCoordinateTranslation(factionID, oldTranslation);
      }
    }
    for (EntityID factionID : newerGD.coordinateTranslations.keySet()){
      for (Integer layer : newerGD.coordinateTranslations.get(factionID).keySet()){
        CoordinateID oldTranslation = olderGD.getCoordinateTranslation(factionID, layer);
        CoordinateID newTranslation = newerGD.getCoordinateTranslation(factionID, layer);
        if (oldTranslation!=null && newTranslation!=null && !oldTranslation.equals(newTranslation)){
          log.warn("coordinate translations do not match "+factionID+","+layer+":"+oldTranslation+"!="+newTranslation);
          resultGD.setCoordinateTranslation(factionID, oldTranslation);
        } else {
          if (oldTranslation!=null)
            resultGD.setCoordinateTranslation(factionID, oldTranslation);
          else if (newTranslation!=null)
            resultGD.setCoordinateTranslation(factionID, newTranslation);
          else
            log.warn("unexpected case");
        }
      }
    }
   
    // FACTIONS
    if (olderGD.factions() != null) {
      for (Iterator<Faction> iter = olderGD.factions().values().iterator(); iter.hasNext();) {
        Faction f = iter.next();

        try {
          resultGD.addFaction(MagellanFactory.createFaction((ID) f.getID().clone(), resultGD));
        } catch (CloneNotSupportedException e) {
          log.error(e);
        }
      }
    }

    if (newerGD.factions() != null) {
      for (Iterator<Faction> iter = newerGD.factions().values().iterator(); iter.hasNext();) {
        Faction f = iter.next();

        if (resultGD.getFaction(f.getID()) == null) {
          try {
            resultGD.addFaction(MagellanFactory.createFaction((ID) f.getID().clone(), resultGD));
          } catch (CloneNotSupportedException e) {
            log.error(e);
          }
        }
      }
    }

    // REGIONS
    // this just adds all the regions to newGD. No content yet.
    if (olderGD.regions() != null) {
      for (Iterator<Region> iter = olderGD.regions().values().iterator(); iter.hasNext();) {
        Region r = iter.next();

        try {
          resultGD.addRegion(MagellanFactory.createRegion((CoordinateID) r.getID().clone(), resultGD));
        } catch (CloneNotSupportedException e) {
          log.error(e);
        }
      }
    }

    if (newerGD.regions() != null) {
      for (Iterator<Region> iter = newerGD.regions().values().iterator(); iter.hasNext();) {
        Region r = iter.next();

        if (resultGD.getRegion((CoordinateID) r.getID()) == null) {
          try {
            resultGD.addRegion(MagellanFactory.createRegion((CoordinateID) r.getID().clone(), resultGD));
          } catch (CloneNotSupportedException e) {
            log.error(e);
          }
        }
      }
    }

    // ISLANDS
    if (olderGD.islands() != null) {
      for (Iterator<Island> iter = olderGD.islands().values().iterator(); iter.hasNext();) {
        Island i = iter.next();

        try {
          resultGD.addIsland(MagellanFactory.createIsland((ID) i.getID().clone(), resultGD));
        } catch (CloneNotSupportedException e) {
          log.error(e);
        }
      }
    }

    if (newerGD.islands() != null) {
      for (Iterator<Island> iter = newerGD.islands().values().iterator(); iter.hasNext();) {
        Island i = iter.next();

        if (olderGD.getIsland(i.getID()) == null) {
          try {
            resultGD.addIsland(MagellanFactory.createIsland((ID) i.getID().clone(), resultGD));
          } catch (CloneNotSupportedException e) {
            log.error(e);
          }
        }
      }
    }

    // HOTSPOTS
    if (olderGD.hotSpots() != null) {
      for (Iterator<HotSpot> iter = olderGD.hotSpots().values().iterator(); iter.hasNext();) {
        HotSpot h = iter.next();

        try {
          resultGD.setHotSpot(MagellanFactory.createHotSpot((ID) h.getID().clone()));
        } catch (CloneNotSupportedException e) {
          log.error(e);
        }
      }
    }

    if (newerGD.hotSpots() != null) {
      for (Iterator<HotSpot> iter = newerGD.hotSpots().values().iterator(); iter.hasNext();) {
        HotSpot h = iter.next();

        if (resultGD.getHotSpot(h.getID()) == null) {
          try {
            resultGD.setHotSpot(MagellanFactory.createHotSpot((ID) h.getID().clone()));
          } catch (CloneNotSupportedException e) {
            log.error(e);
          }
        }
      }
    }

    // BUILDINGS
    if (newerGD.buildings() != null) {
      for (Iterator<Building> iter = newerGD.buildings().values().iterator(); iter.hasNext();) {
        Building b = iter.next();

        try {
          resultGD.addBuilding(MagellanFactory.createBuilding((ID) b.getID().clone(), resultGD));
        } catch (CloneNotSupportedException e) {
          log.error(e);
        }
      }
    }

    if (olderGD.buildings() != null) {
      // buildings are persistent.
      // Accept old buildings not occuring in the new report
      // only if there are no units in that region
      for (Iterator<Building> iter = olderGD.buildings().values().iterator(); iter.hasNext();) {
        Building b = iter.next();
        Building curBuilding = newerGD.getBuilding(b.getID());

        if (curBuilding == null) {
          // check if the building disappeared because we do
          // not know the region anymore or if it was
          // destroyed
          // FIXME(pavkovic): shouldn't it be Region curRegion = b.getRegion();
          // ?
          // try to identify Bug #285,#287, here NPE
          Region curRegion = null;
          if (b.getRegion()!=null) {
            if (b.getRegion().getID()!=null){
              curRegion = newerGD.getRegion((CoordinateID) b.getRegion().getID());
            } else {
              log.errorOnce("Region without ID!");
            }
          } else {
            log.errorOnce("Ship without Region!");
          }

          if ((curRegion == null) || curRegion.units().isEmpty()) {
            try {
              resultGD.addBuilding(MagellanFactory.createBuilding((ID) b.getID().clone(), resultGD));
            } catch (CloneNotSupportedException e) {
              log.error(e);
            }
          } else {
            // we just don't see this region anymore so
            // keep the building
          }
        } else {
          // the building occurs in gd2 so we already
          // included its current version in newGD
        }
      }
    }

    // SHIPS
    if (sameRound && (olderGD.ships() != null)) {
      for (Iterator<Ship> iter = olderGD.ships().values().iterator(); iter.hasNext();) {
        Ship s = iter.next();

        try {
          resultGD.addShip(MagellanFactory.createShip((ID) s.getID().clone(), resultGD));
        } catch (CloneNotSupportedException e) {
          log.error(e);
        }
      }
    }

    if (newerGD.ships() != null) {
      for (Iterator<Ship> iter = newerGD.ships().values().iterator(); iter.hasNext();) {
        Ship s = iter.next();

        if (resultGD.getShip(s.getID()) == null) {
          try {
            resultGD.addShip(MagellanFactory.createShip((ID) s.getID().clone(), resultGD));
          } catch (CloneNotSupportedException e) {
            log.error(e);
          }
        }
      }
    }

    // UNITS

    /*
     * Note: To gather the information needed for level changes, report one is
     * always treated. But in the case of unequal dates only units that are also
     * in the second report are added to the new one and temp units are ignored.
     * IDs are used for comparism.
     */
    if (olderGD.units() != null) {
      for (Iterator<Unit> iter = olderGD.units().values().iterator(); iter.hasNext();) {
        Unit u = iter.next();

        if (sameRound || (newerGD.getUnit(u.getID()) != null)) {
          // TODO (stm): Isn't that nonsense? Doesn't it suffice to add the
          // units of the new report
          // if they are not from the same round?
          try {
            resultGD.addUnit(MagellanFactory.createUnit((ID) u.getID().clone()));
          } catch (CloneNotSupportedException e) {
            log.error(e);
          }
        }
      }
    }

    if (newerGD.units() != null) {
      for (Iterator<Unit> iter = newerGD.units().values().iterator(); iter.hasNext();) {
        Unit u = iter.next();

        if (resultGD.getUnit(u.getID()) == null) {
          try {
            resultGD.addUnit(MagellanFactory.createUnit((ID) u.getID().clone()));
          } catch (CloneNotSupportedException e) {
            log.error(e);
          }
        }
      }
    }

    // MERGE FACTIONS
    if (olderGD.factions() != null) {
      for (Iterator<Faction> iter = olderGD.factions().values().iterator(); iter.hasNext();) {
        Faction curFaction = iter.next();
        Faction newFaction = resultGD.getFaction(curFaction.getID());

        // first pass
        MagellanFactory.mergeFaction(olderGD, curFaction, resultGD, newFaction);
      }
    }

    // MERGE REGIONS
    if (olderGD.regions() != null) {
      for (Iterator<Region> iter = olderGD.regions().values().iterator(); iter.hasNext();) {
        Region curRegion = iter.next();
        Region newRegion = resultGD.getRegion((CoordinateID) curRegion.getID());

        // first pass
        MagellanFactory.mergeRegion(olderGD, curRegion, resultGD, newRegion, sameRound);
      }
    }

    // MERGE ISLANDS
    if (olderGD.islands() != null) {
      for (Iterator<Island> iter = olderGD.islands().values().iterator(); iter.hasNext();) {
        Island curIsland = iter.next();
        Island newIsland = resultGD.getIsland(curIsland.getID());

        // first pass
        MagellanFactory.mergeIsland(olderGD, curIsland, resultGD, newIsland);
      }
    }

    // MERGE HOTSPOTS
    if (olderGD.hotSpots() != null) {
      for (Iterator<HotSpot> iter = olderGD.hotSpots().values().iterator(); iter.hasNext();) {
        HotSpot curHotSpot = iter.next();
        HotSpot newHotSpot = resultGD.getHotSpot(curHotSpot.getID());
        // first pass
        MagellanFactory.mergeHotSpot(olderGD, curHotSpot, resultGD, newHotSpot);
      }
    }

    // MERGE BUILDINGS
    if (olderGD.buildings() != null) {
      for (Iterator<Building> iter = olderGD.buildings().values().iterator(); iter.hasNext();) {
        Building curBuilding = iter.next();
        Building newBuilding = resultGD.getBuilding(curBuilding.getID());

        if (newBuilding != null) {
          // first pass
          MagellanFactory.mergeBuilding(olderGD, curBuilding, resultGD, newBuilding);
        }
      }
    }

    // MERGE SHIPS
    if ((olderGD.ships() != null)) {
      for (Iterator<Ship> iter = olderGD.ships().values().iterator(); iter.hasNext();) {
        Ship curShip = iter.next();
        Ship newShip = resultGD.getShip(curShip.getID());

        // only merge ships from the "older" game data if they are from the same
        // round
        if (sameRound)
          // first pass
          MagellanFactory.mergeShip(olderGD, curShip, resultGD, newShip);
        else
          // TODO (stm 2007-02-19) this is a workaround, we need a nicer
          // solution
          MagellanFactory.mergeComments(curShip, newShip);
      }
    }

    // MERGE FACTIONS, SECOND PASS
    // must be done before merging units to keep group information
    if (newerGD.factions() != null) {
      for (Iterator<Faction> iter = newerGD.factions().values().iterator(); iter.hasNext();) {
        Faction curFaction = iter.next();
        Faction newFaction = resultGD.getFaction(curFaction.getID());

        // second pass
        MagellanFactory.mergeFaction(newerGD, curFaction, resultGD, newFaction);
      }
    }

    // MERGE UNITS

    /*
     * Note: To gather level change informations all units are used. If the
     * dates are equal, a fully merge is done, if not, only the skills are
     * retrieved.
     */
    for (Iterator<Unit> it = resultGD.units().values().iterator(); it.hasNext();) {
      Unit resultUnit = it.next();

      // find the second first since we may need the temp id
      Unit newerUnit = newerGD.findUnit(resultUnit.getID(), null, null);

      // find a temp ID to gather information out of the temp unit
      ID tempID = null;
      Region newRegion = null;

      if ((newerUnit != null) && !sameRound) { // only use temp ID if reports
                                              // have different date
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
          MagellanFactory.mergeUnit(olderGD, olderUnit, resultGD, resultUnit, sameRound);
        } else { // only copy the skills to get change-level base
          if ((newerUnit.getSkills() != null) || (olderUnit.getFaction().isPrivileged())) {
            MagellanFactory.copySkills(olderUnit, resultUnit);
          }
        }
      }

      // second merge step
      if (newerUnit != null) {
        MagellanFactory.mergeUnit(newerGD, newerUnit, resultGD, resultUnit, sameRound);
      }
    }

    // MERGE REGIONS, SECOND PASS
    if (resultGD.regions() != null) {
      for (Iterator<Region> iter = resultGD.regions().values().iterator(); iter.hasNext();) {
          Region newRegion = iter.next();
          Region curRegion = newerGD.getRegion((CoordinateID) newRegion.getID());
          if (curRegion!=null){
            // second pass
            MagellanFactory.mergeRegion(newerGD, curRegion, resultGD, newRegion, true);
          } else {
            newRegion.setVisibility(0);
          }
      }
    }

    // MERGE ISLANDS, SECOND PASS
    if (newerGD.islands() != null) {
      for (Iterator iter = newerGD.islands().values().iterator(); iter.hasNext();) {
        Island curIsland = (Island) iter.next();
        Island newIsland = resultGD.getIsland(curIsland.getID());

        // second pass
        MagellanFactory.mergeIsland(newerGD, curIsland, resultGD, newIsland);
      }
    }

    // MERGE HOTSPOTS, SECOND PASS
    if (newerGD.hotSpots() != null) {
      for (Iterator iter = newerGD.hotSpots().values().iterator(); iter.hasNext();) {
        HotSpot curHotSpot = (HotSpot) iter.next();
        HotSpot newHotSpot = resultGD.getHotSpot(curHotSpot.getID());
        // second pass
        MagellanFactory.mergeHotSpot(newerGD, curHotSpot, resultGD, newHotSpot);
      }
    }

    // MERGE BUILDINGS, SECOND PASS
    if (newerGD.buildings() != null) {
      for (Iterator iter = newerGD.buildings().values().iterator(); iter.hasNext();) {
        Building curBuilding = (Building) iter.next();
        Building newBuilding = resultGD.getBuilding(curBuilding.getID());

        if (newBuilding != null) {
          // second pass
          MagellanFactory.mergeBuilding(newerGD, curBuilding, resultGD, newBuilding);
        }
      }
    }

    // MERGE SHIPS, SECOND PASS
    if (newerGD.ships() != null) {
      for (Iterator iter = newerGD.ships().values().iterator(); iter.hasNext();) {
        Ship curShip = (Ship) iter.next();
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
        Iterator it = units().values().iterator();

        while (it.hasNext()) {
          Unit u = (Unit) it.next();
          Unit u2 = u.getTempUnit(tempID);

          if (u2 != null) {
            return u2;
          }
        }
      } else {
        Map m = Regions.getAllNeighbours(regions(), newRegion.getID(), 3, null);

        if (m != null) {
          Iterator it = m.values().iterator();

          while (it.hasNext()) {
            Region r = (Region) it.next();
            Unit u2 = r.getUnit(tempID);

            if (u2 != null) {
              return u2;
            }
          }
        }
      }
    }

    // standard search
    return getUnit(id);
  }

  /**
   * This function checks if the game data have been manipulated somehow (merge
   * will lead to a filetype null).
   * TODO (stm) nobody uses this
   */
  public boolean gameDataChanged(GameData g) {
    if (g.getFileType() == null) {
      return true;
    }

    for (Iterator iter = g.units().values().iterator(); iter.hasNext();) {
      Unit u = (Unit) iter.next();

      if (u.ordersHaveChanged()) {
        return true;
      }
    }

    return false;
  }

  /**
   * reset change state of all units to false
   */
  public void resetToUnchanged() {
    for (Iterator iter = units().values().iterator(); iter.hasNext();) {
      Unit u = (Unit) iter.next();
      u.setOrdersChanged(false);
    }
  }

  /**
   * returns a clone of the game data (using CRWriter/CRParser trick
   * encapsulated in Loader)
   * 
   * @throws CloneNotSupportedException
   *           DOCUMENT-ME
   */
  public Object clone() throws CloneNotSupportedException {
    return new Loader().cloneGameData(this);
  }

  /**
   * returns a clone of the game data (using CRWriter/CRParser trick
   * encapsulated in Loader) and at the same time translates the origin two
   * <code>newOrigin</code>
   * 
   * @throws CloneNotSupportedException
   *           DOCUMENT-ME
   */
  public Object clone(CoordinateID newOrigin) throws CloneNotSupportedException {
    if (newOrigin.x == 0 && newOrigin.y == 0)
      return this.clone();
    else{
      if (MemoryManagment.isFreeMemory(this.estimateSize()*3)){
        log.info("cloning in memory");
        GameData clonedData = new Loader().cloneGameDataInMemory(this, newOrigin);
        if (clonedData==null || clonedData.outOfMemory){
          log.info("cloning externally");
          clonedData = new Loader().cloneGameData(this, newOrigin);
        }
        return clonedData;
      }else {
        log.info("cloning externally");
        return new Loader().cloneGameData(this, newOrigin);
      }
    }
  }

  /**
   * Returns an estimate of the memory (in bytes) needed to store this game
   * data. This can be a very rough estimate!
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

  /**
   * This method can be called after loading or merging a report to avoid
   * double messages and to set some game specific stuff.
   */
  public void postProcess() {
    if (postProcessed) {
      return;
    }
    log.info("start GameData postProcess");
    
    
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
    
    postProcessed = true;
    
    log.info("finished GameData postProcess");
  }

  /**
   * adding Default Translations to the translations
   */
  private void postProcessDefaultTranslations(){
    // Skilltypes
    for (Iterator<SkillType> iter = this.rules.getSkillTypeIterator();iter.hasNext();){
      SkillType skillType = (SkillType)iter.next();
      String key = "skill."+skillType.getID().toString();
      if (!this.translations().contains(key)){
        // we have to add
        String translated = Resources.getRuleItemTranslation(key);
        this.addTranslation(key, translated, TranslationType.sourceMagellan);
      }
    }
  }
  
  
  
  /**
   * scans the regions for missing regions, for regions with regionType "The
   * Void" or "Leere" These Regions are not created in the world on the server,
   * but we are so near, that we should have some information about it. So we
   * add these Regions with the special RegionType "Leere"
   */
  public void postProcessTheVoid() {
    List<Region> newRegions = new ArrayList<Region>();
    for (Iterator iter = this.regions().keySet().iterator(); iter.hasNext();) {
      CoordinateID actRegionID = (CoordinateID) iter.next();
      Region actRegion = (Region) regions().get(actRegionID);
      boolean shouldHaveAllNeighbours = false;
      if (actRegion.getVisibility() != null && (actRegion.getVisibility().equalsIgnoreCase("travel")
      // || actRegion.getVisibility().equalsIgnoreCase("neighbour")
      )) {
        shouldHaveAllNeighbours = true;
      } else {
        // if we have a unit in the region?
        if (actRegion.units() != null && actRegion.units().size() > 0) {
          shouldHaveAllNeighbours = true;
        }
      }
      if (shouldHaveAllNeighbours) {
        CoordinateID center = actRegion.getCoordinate();

        int radius = 1;
        for (int dx = -radius; dx <= radius; dx++) {
          for (int dy = (-radius + Math.abs(dx)) - ((dx > 0) ? dx : 0); dy <= ((radius - Math.abs(dx)) - ((dx < 0) ? dx : 0)); dy++) {
            CoordinateID c = new CoordinateID(0, 0, center.z);
            c.x = center.x + dx;
            c.y = center.y + dy;

            Region neighbour = (Region) regions().get(c);

            if (neighbour == null) {
              // Missing Neighbor
              Region r = MagellanFactory.createRegion(c, this);
              RegionType type = this.rules.getRegionType(StringID.create("Leere"), true);
              r.setType(type);
              r.setName(Resources.get("completedata.region.thevoid.name"));
              r.setDescription(Resources.get("completedata.region.thevoid..beschr"));
              newRegions.add(r);
              this.addTranslation("Leere", Resources.get("completedata.region.thevoid.name"),TranslationType.sourceMagellan);
            }
          }
        }
      }
    }
    if (newRegions.size() > 0) {
      for (Iterator iter = newRegions.iterator(); iter.hasNext();) {
        Region actRegion = (Region) iter.next();
        if (!this.regions().containsKey(actRegion.getID())) {
          this.addRegion(actRegion);
        }
      }
    }
  }

  /**
   * Adds the order locale of Magellan if locale is null. This should prevent
   * some NPE with the sideeffect to store a locale in a locale-less game data
   * object.
   */
  private void postProcessLocale() {
    if (getLocale() == null) {
      setLocale(Locales.getOrderLocale());
    }
  }

  /**
   * This function post processes the message blocks to remove duplicate
   * messages. In former times this has been done while loading the game data
   * but this had a negative time tradeoff (O(n^2)). This functions needs about
   * O(n log n).
   */
  private void postProcessMessages() {
    // faction.messages
    for (Iterator<Faction> iter = factions().values().iterator(); iter.hasNext();) {
      Faction o = iter.next();
      postProcessMessages(o.getMessages());
    }

    // region.messages
    for (Iterator<Region> iter = regions().values().iterator(); iter.hasNext();) {
      Region o = iter.next();
      postProcessMessages(o.getMessages());
    }
  }

  /**
   * Postprocess a given list of messages. To remove duplicate messages we put
   * all messages in an ordered hashtable and put them back into the messages
   * collection.
   */
  private void postProcessMessages(Collection<Message> messages) {
    if (messages == null) {
      return;
    }

    Map<Message, Message> ht = new OrderedHashtable<Message, Message>();

    for (Iterator<Message> iter = messages.iterator(); iter.hasNext();) {
      Message msg = iter.next();

      if (ht.put(msg, msg) != null) {
        if (1 == 2) {
          log.warn("Duplicate message \"" + msg.getText() + "\" found, removing it.");
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
    for (Iterator iter = this.regions().keySet().iterator(); iter.hasNext();) {
      CoordinateID actRegionID = (CoordinateID) iter.next();
      Region actRegion = (Region) regions().get(actRegionID);
      if (actRegion.getRegionType().equals(this.rules.getRegionType("Leere"))) {
        delRegionID.add(actRegionID);
      }
    }
    if (delRegionID.size() > 0) {
      for (Iterator<CoordinateID> iter = delRegionID.iterator(); iter.hasNext();) {
        CoordinateID actID = iter.next();
        this.regions().remove(actID);
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
    for (Region r : regions().values()) {
      if (r.isActive()) return r;
    }
    
    return null;
  }
  
  /**
   * Sets the mapping for astral to real space.
   * 
   * @param c the real space <code>CoordianteID</code> <x,y,0> which is the center of the 
   * astral space region with CoordinateID <0,0,1>.
   */
//  public abstract void setAstralMapping(CoordinateID c);
  
  /**
   * Returns the relation of two map layers.
   * 
   * @return the <code>CoordinateID</code> of the toLevel region which is accessable
   * by the fromLevel region with CoordinateID <0, 0, fromLevel>.
   */
  public LevelRelation getLevelRelation(int fromLevel, int toLevel) {
    Map<Integer, LevelRelation> relations = levelRelations.get(toLevel);
    if (relations == null) {
      relations = new HashMap<Integer, LevelRelation>();
      levelRelations.put(toLevel, relations);
    }      
    LevelRelation lr = relations.get(fromLevel);
    if (lr == null) {
      MapMergeEvaluator mh = this.getGameSpecificStuff().getMapMergeEvaluator();
      lr = mh.getLevelRelation(this, fromLevel, toLevel); 
      relations.put(fromLevel, lr);
    }
    return lr;
  }

  /**
   * This method should wrap the mapping information former contained in 
   * magellan.client.swing.MapperPanel.setLevel(int)
   * @param data
   * @param level
   * @return Mapped Coordinate
   */
  public CoordinateID getRelatedCoordinate(CoordinateID c, int level) {
    LevelRelation lr = getLevelRelation(c.z, level);
    if (lr == null) return null;
    return lr.getRelatedCoordinate(c);
  }
  
  public CoordinateID getRelatedCoordinateUI(CoordinateID c, int level) {
    CoordinateID result = getRelatedCoordinate(c, level);
    if (result == null) {
      LevelRelation lr = getLevelRelation(level, c.z);
      if (lr == null) return null;
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
   * Returns a coordinate translation of <code>otherFaction</code> this
   * report. This is the coordinate vector that has to be added to the origin
   * (with z-coordinate <code>layer</code>) of this report to get the origin
   * of <code>otherFaction</code>. In other words, you have to subtract this
   * translation from <code>otherFaction</code>'s coordinates in layer
   * <code>layer</code> to get coordinates of this report. The coordinate
   * translation of the owner faction is <i>not</i> always (0, 0, layer).
   * 
   * @param otherFaction
   * @param layer
   * @return The coordinate translation of <code>otherFaction</code> to the
   *         owner faction. <code>null</code> if the translation is unknown 
   */
  public CoordinateID getCoordinateTranslation(EntityID otherFaction, int layer) {
    Map<Integer, CoordinateID> layerMap = getCoordinateTranslationMap(otherFaction);
    if (layerMap == null)
      return null;
    else
      return layerMap.get(layer);
  }

  /**
   * Returns the immutable map of all known coordinate translations of
   * <code>otherFaction</code>. This is a mapping of layers to coordinateIDs.
   * This is the coordinate vector that has to be added to the origin (with
   * z-coordinate <code>layer</code>) of this report to get the origin of
   * <code>otherFaction</code>. In other words, you have to subtract this
   * translation from <code>otherFaction</code>'s coordinates in layer
   * <code>layer</code> to get coordinates of this report. The coordinate
   * translation of the owner faction is <i>not</i> always (0, 0, layer).
   * 
   * @return The map of coordinate translations of faction
   *         <code>otherFaction</code> or <code>null</code> if unknown
   * @see #getCoordinateTranslation(EntityID, int)
   * 
   */
  public Map<Integer, CoordinateID> getCoordinateTranslationMap(EntityID otherFaction) {
    if (coordinateTranslations.get(otherFaction)==null)
      return null;
    return Collections.unmodifiableMap(coordinateTranslations.get(otherFaction));
  }
  
  /**
   * Sets the coordinate translation of <code>otherFaction</code> to the owner
   * faction. This is a mapping of layers to coordinateIDs.
   * This is the coordinate vector that has to be added to the origin (with
   * z-coordinate <code>layer</code>) of this report to get the origin of
   * <code>otherFaction</code>. In other words, you have to subtract this
   * translation from <code>otherFaction</code>'s coordinates in layer
   * <code>layer</code> to get coordinates of this report. The coordinate
   * translation of the owner faction is <i>not</i> always (0, 0, layer).
   * 
   * @param otherFaction
   * @param usedTranslation
   * 
   */
  public void setCoordinateTranslation(EntityID otherFaction, CoordinateID usedTranslation) {
    Map<Integer, CoordinateID> layerMap = coordinateTranslations.get(otherFaction);
    if (layerMap==null){
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
    if (this.ownerFaction!=null){
      // TODO (stm) we could translate the translations if we have a 
      // translation from old owner faction to ownerFaction. For now, we simply forget 
      // all translations...
      log.warn("owner faction changed");
    }
    this.ownerFaction = ownerFaction;
  }
  
  public String getGameName(){
    return gameName;
  }

  public void setGameName(String newName){
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
