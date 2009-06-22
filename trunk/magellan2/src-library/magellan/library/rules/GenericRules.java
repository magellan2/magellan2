/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.rules;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import magellan.library.ID;
import magellan.library.Named;
import magellan.library.Rules;
import magellan.library.StringID;
import magellan.library.gamebinding.GameSpecificStuff;
import magellan.library.gamebinding.GameSpecificStuffProvider;
import magellan.library.impl.MagellanSpellImpl;
import magellan.library.utils.OrderedHashtable;
import magellan.library.utils.Umlaut;
import magellan.library.utils.logging.Logger;

/**
 * A class implementing Eressea specific rules. Primarily, this class collects all the well-known
 * object-types which in turn provide information about their properties as they are defined in the
 * rules of Eressea. In fact, there is nothing eressea specific in Rules anymore, so this is the
 * generic rules object.
 */
public class GenericRules implements Rules {
  private static final Logger log = Logger.getInstance(GenericRules.class);

  private Map<Class<? extends ObjectType>, Map<String, ? extends ObjectType>> metaMap =
      new HashMap<Class<? extends ObjectType>, Map<String, ? extends ObjectType>>();
  private Map<Class<? extends ObjectType>, Map<String, ? extends ObjectType>> namesMetaMap =
      new HashMap<Class<? extends ObjectType>, Map<String, ? extends ObjectType>>();

// // Map consisting of Race, RegionType, ShipType, BuildingType, CastleType
// private Map<String, UnitContainerType> mapUnitContainerType =
// new OrderedHashtable<String, UnitContainerType>();
// private Map<String, UnitContainerType> mapUnitContainerTypeNames =
// new OrderedHashtable<String, UnitContainerType>();
//
// // Map consisting of ItemType
// private Map<String, ItemType> mapItemType = new OrderedHashtable<String, ItemType>();
// private Map<String, ItemType> mapItemTypeNames = new OrderedHashtable<String, ItemType>();
//
// // Map consisting of AllianceCategory
// private Map<String, AllianceCategory> mapAllianceCategory = new OrderedHashtable<String,
  // AllianceCategory>();
// private Map<String, AllianceCategory> mapAllianceCategoryNames =
// new OrderedHashtable<String, AllianceCategory>();
//
// // Map consisting of OptionCategory
// private Map<String, OptionCategory> mapOptionCategory = new OrderedHashtable<String,
  // OptionCategory>();
// private Map<String, OptionCategory> mapOptionCategoryNames =
// new OrderedHashtable<String, OptionCategory>();
//
// // Map consisting of ItemCategory
// private Map<String, ItemCategory> mapItemCategory = new OrderedHashtable<String, ItemCategory>();
// private Map<String, ItemCategory> mapItemCategoryNames = new OrderedHashtable<String,
  // ItemCategory>();
//
// // Map consisting of SkillCategory
// private Map<String, SkillCategory> mapSkillCategory = new OrderedHashtable<String,
  // SkillCategory>();
// private Map<String, SkillCategory> mapSkillCategoryNames =
// new OrderedHashtable<String, SkillCategory>();
//
// // Map consisting of SkillType
// private Map<String, SkillType> mapSkillType = new OrderedHashtable<String, SkillType>();
// private Map<String, SkillType> mapSkillTypeNames = new OrderedHashtable<String, SkillType>();

  // Class with gamespecific things...
  private GameSpecificStuff gameSpecificStuff;

  private String orderFileStartingString = "ERESSEA";

  // this is the only point where metaMap is changed, so unchecked cast is safe
  @SuppressWarnings("unchecked")
  protected <T extends ObjectType> Map<String, T> getMap(Class<T> class1) {
    Map<String, T> result = (Map<String, T>) metaMap.get(class1);
    if (result == null) {
      result = new OrderedHashtable<String, T>();
      metaMap.put(class1, result);
    }
    return result;
  }

  // this is the only point where metaMap is changed, so unchecked cast is safe
  @SuppressWarnings("unchecked")
  protected <T extends ObjectType> Map<String, T> getNamesMap(Class<T> class1) {
    Map<String, T> result = (Map<String, T>) namesMetaMap.get(class1);
    if (result == null) {
      result = new OrderedHashtable<String, T>();
      namesMetaMap.put(class1, result);
    }
    return result;
  }

  protected <T extends ObjectType> T getObjectType(Class<T> class1, ID id, boolean add) {
    Map<String, T> map = getMap(class1);
    Map<String, T> mapNames = getNamesMap(class1);
    T objectType = getObjectType(map, mapNames, id.toString());

    if ((objectType == null) && add) {
      try {
        addObject(objectType = class1.getConstructor(ID.class).newInstance(id), map, mapNames);// new
        // T(id),
        // mapT,
        // mapTNames);
      } catch (Exception e) {
        log.error("class has no constructor C(ID)");
        throw new RuntimeException(e);
      }
      objectType.setName(id.toString());
    }

    return objectType;
  }

  /**
   * @see magellan.library.Rules#getRegionTypeIterator()
   * @deprecated use {@link #getRegionTypes()}
   */
  public Iterator<RegionType> getRegionTypeIterator() {
    return getMap(RegionType.class).values().iterator();
  }

  /**
   * @see magellan.library.Rules#getRegionType(magellan.library.ID)
   */
  public RegionType getRegionType(ID id) {
    return getRegionType(id, false);
  }

  /**
   * @see magellan.library.Rules#getRegionType(magellan.library.ID, boolean)
   */
  public RegionType getRegionType(ID id, boolean add) {
    return getObjectType(RegionType.class, id, add);
  }

  /**
   * @see magellan.library.Rules#getRegionType(java.lang.String)
   */
  public RegionType getRegionType(String id) {
    return getRegionType(id, false);
  }

  /**
   * @see magellan.library.Rules#getRegionType(java.lang.String, boolean)
   */
  public RegionType getRegionType(String id, boolean add) {
    if ((id == null) || id.equals("")) {
      return null;
    }

    return getRegionType(StringID.create(id), add);
  }

  /**
   * All RegionTypes in one Collection
   */
  public Collection<RegionType> getRegionTypes() {
    return getMap(RegionType.class).values();
  }

  /**
   * @see magellan.library.Rules#getShipTypeIterator()
   * @deprecated use {@link #getShipTypes()}
   */
  public Iterator<ShipType> getShipTypeIterator() {
    return getShipTypes().iterator();
  }

  protected Collection<ShipType> getShipTypes() {
    return getMap(ShipType.class).values();
  }

  /**
   * @see magellan.library.Rules#getShipType(magellan.library.ID)
   */
  public ShipType getShipType(ID id) {
    return getShipType(id, false);
  }

  /**
   * @see magellan.library.Rules#getShipType(magellan.library.ID, boolean)
   */
  public ShipType getShipType(ID id, boolean add) {
    return getObjectType(ShipType.class, id, add);
  }

  /**
   * @see magellan.library.Rules#getShipType(java.lang.String)
   */
  public ShipType getShipType(String id) {
    return getShipType(id, false);
  }

  /**
   * @see magellan.library.Rules#getShipType(java.lang.String, boolean)
   */
  public ShipType getShipType(String id, boolean add) {
    if ((id == null) || id.equals("")) {
      return null;
    }

    return getShipType(StringID.create(id), add);
  }

  /**
   * @see magellan.library.Rules#getBuildingTypeIterator()
   */
  public Iterator<BuildingType> getBuildingTypeIterator() {
    return getBuildingTypes().iterator();
  }

  protected Collection<BuildingType> getBuildingTypes() {
    return getMap(BuildingType.class).values();
  }

  /**
   * @see magellan.library.Rules#getBuildingType(magellan.library.ID)
   */
  public BuildingType getBuildingType(ID id) {
    return getBuildingType(id, false);
  }

  /**
   * @see magellan.library.Rules#getBuildingType(magellan.library.ID, boolean)
   */
  public BuildingType getBuildingType(ID id, boolean add) {
    return getObjectType(BuildingType.class, id, add);
  }

  /**
   * @see magellan.library.Rules#getBuildingType(java.lang.String)
   */
  public BuildingType getBuildingType(String id) {
    return getBuildingType(id, false);
  }

  /**
   * @see magellan.library.Rules#getBuildingType(java.lang.String, boolean)
   */
  public BuildingType getBuildingType(String id, boolean add) {
    if ((id == null) || id.equals("")) {
      return null;
    }

    return getBuildingType(StringID.create(id), add);
  }

  /**
   * @see magellan.library.Rules#getCastleTypeIterator()
   * @deprecated you may use {@link #getCastleTypes()}
   */
  public Iterator<CastleType> getCastleTypeIterator() {
    return getCastleTypes().iterator();
  }

  protected Collection<CastleType> getCastleTypes() {
    return getMap(CastleType.class).values();
  }

  /**
   * @see magellan.library.Rules#getCastleType(magellan.library.ID)
   */
  public CastleType getCastleType(ID id) {
    return getCastleType(id, false);
  }

  /**
   * @see magellan.library.Rules#getCastleType(magellan.library.ID, boolean)
   */
  public CastleType getCastleType(ID id, boolean add) {
    return getObjectType(CastleType.class, id, add);
  }

  /**
   * @see magellan.library.Rules#getCastleType(java.lang.String)
   */
  public CastleType getCastleType(String id) {
    return getCastleType(id, false);
  }

  /**
   * @see magellan.library.Rules#getCastleType(java.lang.String, boolean)
   */
  public CastleType getCastleType(String id, boolean add) {
    if ((id == null) || id.equals("")) {
      return null;
    }

    return getCastleType(StringID.create(id), add);
  }

  /**
   * @see magellan.library.Rules#getRaceIterator()
   * @deprecated you may use getRaces()
   */
  public Iterator<Race> getRaceIterator() {
    return getRaces().iterator();
  }

  protected Collection<Race> getRaces() {
    return getMap(Race.class).values();
  }

  /**
   * @see magellan.library.Rules#getRace(magellan.library.ID)
   */
  public Race getRace(ID id) {
    return getRace(id, false);
  }

  /**
   * @see magellan.library.Rules#getRace(magellan.library.ID, boolean)
   */
  public Race getRace(ID id, boolean add) {
    return getObjectType(Race.class, id, add);
  }

  /**
   * @see magellan.library.Rules#getRace(java.lang.String)
   */
  public Race getRace(String id) {
    return getRace(id, false);
  }

  /**
   * @see magellan.library.Rules#getRace(java.lang.String, boolean)
   */
  public Race getRace(String id, boolean add) {
    if ((id == null) || id.equals("")) {
      return null;
    }

    return getRace(StringID.create(id), add);
  }

  /**
   * @see magellan.library.Rules#getItemTypeIterator()
   * @deprecated you may use getItemTypes()
   */
  public Iterator<ItemType> getItemTypeIterator() {
    return getItemTypes().iterator();
  }

  protected Collection<ItemType> getItemTypes() {
    return getMap(ItemType.class).values();
  }

  /**
   * @see magellan.library.Rules#getItemType(magellan.library.ID)
   */
  public ItemType getItemType(ID id) {
    return getItemType(id, false);
  }

  /**
   * @see magellan.library.Rules#getItemType(magellan.library.ID, boolean)
   */
  public ItemType getItemType(ID id, boolean add) {
    return getObjectType(ItemType.class, id, add);
  }

  /**
   * @see magellan.library.Rules#getItemType(java.lang.String)
   */
  public ItemType getItemType(String id) {
    return getItemType(id, false);
  }

  /**
   * @see magellan.library.Rules#getItemType(java.lang.String, boolean)
   */
  public ItemType getItemType(String id, boolean add) {
    if ((id == null) || id.equals("")) {
      return null;
    }

    return getItemType(StringID.create(id), add);
  }

  /**
   * @see magellan.library.Rules#getAllianceCategoryIterator()
   * @deprecated you may use {@link #getAllianceCategories()}
   */
  public Iterator<AllianceCategory> getAllianceCategoryIterator() {
    return getAllianceCategories().iterator();
  }

  protected Collection<AllianceCategory> getAllianceCategories() {
    return getMap(AllianceCategory.class).values();
  }

  /**
   * @see magellan.library.Rules#getAllianceCategory(magellan.library.ID)
   */
  public AllianceCategory getAllianceCategory(ID id) {
    return getAllianceCategory(id, false);
  }

  /**
   * @see magellan.library.Rules#getAllianceCategory(magellan.library.ID, boolean)
   */
  public AllianceCategory getAllianceCategory(ID id, boolean add) {
    return getObjectType(AllianceCategory.class, id, add);
  }

  /**
   * @see magellan.library.Rules#getAllianceCategory(java.lang.String)
   */
  public AllianceCategory getAllianceCategory(String id) {
    return getAllianceCategory(id, false);
  }

  /**
   * @see magellan.library.Rules#getAllianceCategory(java.lang.String, boolean)
   */
  public AllianceCategory getAllianceCategory(String id, boolean add) {
    if ((id == null) || id.equals("")) {
      return null;
    }

    return getAllianceCategory(StringID.create(id), add);
  }

  /**
   * @see magellan.library.Rules#getOptionCategoryIterator()
   * @deprecated you may use getOptionCategories()
   */
  public Iterator<OptionCategory> getOptionCategoryIterator() {
    return getOptionCategories().iterator();
  }

  protected Collection<OptionCategory> getOptionCategories() {
    return getMap(OptionCategory.class).values();
  }

  /**
   * @see magellan.library.Rules#getOptionCategory(magellan.library.ID)
   */
  public OptionCategory getOptionCategory(ID id) {
    return getOptionCategory(id, false);
  }

  /**
   * @see magellan.library.Rules#getOptionCategory(magellan.library.ID, boolean)
   */
  public OptionCategory getOptionCategory(ID id, boolean add) {
    return getObjectType(OptionCategory.class, id, add);
  }

  /**
   * @see magellan.library.Rules#getOptionCategory(java.lang.String)
   */
  public OptionCategory getOptionCategory(String id) {
    return getOptionCategory(id, false);
  }

  /**
   * @see magellan.library.Rules#getOptionCategory(java.lang.String, boolean)
   */
  public OptionCategory getOptionCategory(String id, boolean add) {
    if ((id == null) || id.equals("")) {
      return null;
    }

    return getOptionCategory(StringID.create(id), add);
  }

  /**
   * @see magellan.library.Rules#getSkillCategoryIterator()
   * @deprecated you may use {@link #getSkillCategories()}
   */
  public Iterator<SkillCategory> getSkillCategoryIterator() {
    return getSkillCategories().iterator();
  }

  protected Collection<SkillCategory> getSkillCategories() {
    return getMap(SkillCategory.class).values();
  }

  /**
   * @see magellan.library.Rules#getSkillCategory(magellan.library.ID)
   */
  public SkillCategory getSkillCategory(ID id) {
    return getSkillCategory(id, false);
  }

  /**
   * @see magellan.library.Rules#getSkillCategory(magellan.library.ID, boolean)
   */
  public SkillCategory getSkillCategory(ID id, boolean add) {
    return getObjectType(SkillCategory.class, id, add);
  }

  /**
   * @see magellan.library.Rules#getSkillCategory(java.lang.String)
   */
  public SkillCategory getSkillCategory(String id) {
    return getSkillCategory(id, false);
  }

  /**
   * @see magellan.library.Rules#getSkillCategory(java.lang.String, boolean)
   */
  public SkillCategory getSkillCategory(String id, boolean add) {
    if ((id == null) || id.equals("")) {
      return null;
    }

    return getSkillCategory(StringID.create(id), add);
  }

  /**
   * @see magellan.library.Rules#getItemCategoryIterator()
   * @deprecated use getItemCategories
   */
  public Iterator<ItemCategory> getItemCategoryIterator() {
    return getItemCategories().iterator();
  }

  protected Collection<ItemCategory> getItemCategories() {
    return getMap(ItemCategory.class).values();
  }

  /**
   * @see magellan.library.Rules#getItemCategory(magellan.library.ID)
   */
  public ItemCategory getItemCategory(ID id) {
    return getItemCategory(id, false);
  }

  /**
   * @see magellan.library.Rules#getItemCategory(magellan.library.ID, boolean)
   */
  public ItemCategory getItemCategory(ID id, boolean add) {
    return getObjectType(ItemCategory.class, id, add);
  }

  /**
   * @see magellan.library.Rules#getItemCategory(java.lang.String)
   */
  public ItemCategory getItemCategory(String id) {
    return getItemCategory(id, false);
  }

  /**
   * @see magellan.library.Rules#getItemCategory(java.lang.String, boolean)
   */
  public ItemCategory getItemCategory(String id, boolean add) {
    if ((id == null) || id.equals("")) {
      return null;
    }

    return getItemCategory(StringID.create(id), add);
  }

  /**
   * @see magellan.library.Rules#getSkillTypeIterator()
   * @deprecated u may use {@link #getSkillTypes()}
   */
  public Iterator<SkillType> getSkillTypeIterator() {
    return getSkillTypes().iterator();
  }

  protected Collection<SkillType> getSkillTypes() {
    return getMap(SkillType.class).values();
  }

  /**
   * Shorthand for <code>getSkillType(id, false)</code>.
   * 
   * @see magellan.library.Rules#getSkillType(magellan.library.ID)
   */
  public SkillType getSkillType(ID id) {
    return getSkillType(id, false);
  }

  /**
   * Returns the skill type with given id. If there is no such skill type and
   * <code>add == true</code>, a new skill type is added and returns. Otherwise, <code>null</code>
   * is returned.
   * 
   * @see magellan.library.Rules#getSkillType(magellan.library.ID, boolean)
   */
  public SkillType getSkillType(ID id, boolean add) {
    return getObjectType(SkillType.class, id, add);
  }

  /**
   * Shorthand for <code>getSkillType(id, false)</code>.
   */
  public SkillType getSkillType(String id) {
    return getSkillType(id, false);
  }

  /**
   * Returns the skill type with given id. If there is no such skill type and
   * <code>add == true</code>, a new skill type is added and returns. Otherwise, <code>null</code>
   * is returned.
   */
  public SkillType getSkillType(String id, boolean add) {
    if ((id == null) || id.equals("")) {
      return null;
    }

    return getSkillType(StringID.create(id), add);
  }
  
  public MagellanSpellImpl getSpell(String id){
    return getSpell(id, false);
  }
  
  
  public MagellanSpellImpl getSpell(String id, boolean add){
    if ((id == null) || id.equals("")) {
      return null;
    }
    
    return getSpell(StringID.create(id), add);
  }

  private MagellanSpellImpl getSpell(StringID id, boolean add) {
    return getObjectType(MagellanSpellImpl.class, id, add);
  }

  public Collection<MagellanSpellImpl> getSpells(){
    return getMap(MagellanSpellImpl.class).values();
  }
  
  /**
   * @see magellan.library.Rules#changeName(java.lang.String, java.lang.String)
   */
  public ObjectType changeName(String from, String to) {
    return changeName(StringID.create(from), to);
  }

  /**
   * Changes the name of an object identified by the specified id. This method serves as a
   * convenience as it relieves the implementor of the arduous task of determining the kind of
   * object type (ItemType, SkillType etc.) and accessing the corresponding data structures. It also
   * ensures that the object is also accessible by calling the getXXX methods with the new name.
   */
  protected ObjectType changeName(ID id, String name) {
    ObjectType ot = null;

    ot = changeName(id, name, getMap(RegionType.class), getNamesMap(RegionType.class));
    if(ot != null) {
      return ot;
    }

    ot = changeName(id, name, getMap(ShipType.class), getNamesMap(ShipType.class));
    if(ot != null) {
      return ot;
    }

    ot = changeName(id, name, getMap(BuildingType.class), getNamesMap(BuildingType.class));
    if(ot != null) {
      return ot;
    }

    ot = changeName(id, name, getMap(CastleType.class), getNamesMap(CastleType.class));
    if(ot != null) {
      return ot;
    }

    ot = changeName(id, name, getMap(Race.class), getNamesMap(Race.class));
    if(ot != null) {
      return ot;
    }

    ot = changeName(id, name, getMap(ItemType.class), getNamesMap(ItemType.class));

    if(ot != null) {
      return ot;
    }

    // pavkovic 2004.03.17: Don't change the name of alliance and option category
    //    ot = changeName(id, name, getMap(AllianceCategory.class), getNamesMap(AllianceCategory.class));
    //    if(ot != null) {
    //      return ot;
    //    }
    //    ot = changeName(id, name, getMap(OptionCategory.class), getNamesMap(OptionCategory.class));
    //    if(ot != null) {
    //      return ot;
    //    }
    ot = changeName(id, name, getMap(ItemCategory.class), getNamesMap(ItemCategory.class));

    if(ot != null) {
      return ot;
    }

    ot = changeName(id, name, getMap(SkillCategory.class), getNamesMap(SkillCategory.class));

    if(ot != null) {
      return ot;
    }

    ot = changeName(id, name, getMap(SkillType.class), getNamesMap(SkillType.class));

    if(ot != null) {
      return ot;
    }

    ot = changeName(id, name, getMap(MagellanSpellImpl.class), getNamesMap(MagellanSpellImpl.class));

    if(ot != null) {
      return ot;
    }

    return null;
  }

  // this was
  // protected static ObjectType changeName(ID id, String name, Map<String,ObjectType>
  // mapObjectType, Map<String,ObjectType> mapObjectTypeNames) {
  protected static <T extends ObjectType> T changeName(ID id, String name,
      Map<String, T> mapObjectType, Map<String, T> mapObjectTypeNames) {
    String key = Umlaut.normalize(id.toString());
    T ot = mapObjectType.get(key);

    if (ot != null) {
      mapObjectTypeNames.remove(Umlaut.normalize(ot.getName()));
      ot.setName(name);
      addObject(ot, mapObjectType, mapObjectTypeNames);
    }

    return null;
  }

  /**
   * this should also work: protected static <T extends ObjectType> T addObject(T o, Map<String,?
   * super T> mapObjectType, Map<String,? super T> mapObjectTypeNames) { however, it give an error
   * druing build.xml, but not in eclipse:
   * 
   * <pre>
	* [javac] /export/home/i11pc226/steffen/workspace/Magellan2/src-library/magellan/library/rules/GenericRules.java:714: <T>addObject(T,java.util.Map<java.lang.String,? super T>,java.util.Map<java.lang.String,? super T>) in magellan.library.rules.GenericRules cannot be applied to (T,java.util.Map<java.lang.String,T>,java.util.Map<java.lang.String,T>)
  * [javac]       addObject(ot, mapObjectType, mapObjectTypeNames);
	* </pre>
   */

  /**
   * Adds the specified object to the specified map by id and by name.
   */
  protected static <T extends ObjectType> void addObject(T o, Map<String, T> mapObjectType,
      Map<String, T> mapObjectTypeNames) {
// private static ObjectType addObject(ObjectType o, Map<String,ObjectType> mapObjectType,
    // Map<String,ObjectType> mapObjectTypeNames) {
    if (GenericRules.log.isDebugEnabled()) {
      GenericRules.log.debug("GenericRules.addObject(" + o.getClass().toString() + "," + o.getID()
          + ")");
    }

    mapObjectType.put(Umlaut.normalize(o.getID().toString()), o);

    if (o.getName() != null) {
      mapObjectTypeNames.put(Umlaut.normalize(o.getName()), o);
    }

  }

  /**
   * Tries to retrieve an object type form the specified map by its name. If the name is not used as
   * a key in the map but an object with the specified name exists, the object is put into the map
   * with the name as its key for speeding up future look-ups.
   */
  protected static <T extends Named> T getObjectType(Map<String, T> objects, Map<String, T> names,
      String name) {
    String normName = Umlaut.normalize(name);

    if (names.containsKey(normName)) {
      return names.get(normName);
    } else {
      for (T ot : objects.values()) {
        if (Umlaut.normalize(ot.getName()).equals(normName)) {
          names.put(normName, ot);
          return ot;
        }
        if (Umlaut.normalize(ot.getID().toString()).equals(normName)) {
          names.put(normName, ot);
          return ot;
        }
      }
    }

    // pavkovic 2004.03.08: for now also return object with id
    // return null;
    return objects.get(StringID.create(normName));
  }

  private String gameSpecificStuffClassName;

  /**
   * Sets the name of the class for getGameSpecificStuff()
   * 
   * @see magellan.library.Rules#setGameSpecificStuffClassName(java.lang.String)
   */
  public void setGameSpecificStuffClassName(String className) {
    gameSpecificStuffClassName = className;
  }

  /**
   * Returns the GameSpecificStuff object for the name specified by setGameSpecificClassName.
   * 
   * @see magellan.library.Rules#getGameSpecificStuff()
   */
  public GameSpecificStuff getGameSpecificStuff() {
    if (gameSpecificStuff == null) {
      gameSpecificStuff =
          new GameSpecificStuffProvider().getGameSpecificStuff(gameSpecificStuffClassName);
    }

    return gameSpecificStuff;
  }

  /**
   * @see magellan.library.Rules#getOrderfileStartingString()
   */
  public String getOrderfileStartingString() {
    return orderFileStartingString;
  }

  /**
   * @see magellan.library.Rules#setOrderfileStartingString(java.lang.String)
   */
  public void setOrderfileStartingString(String startingString) {
    orderFileStartingString = startingString;
  }

}
