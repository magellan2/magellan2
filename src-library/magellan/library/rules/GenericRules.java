/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.rules;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.ID;
import magellan.library.IntegerID;
import magellan.library.Named;
import magellan.library.Rules;
import magellan.library.StringID;
import magellan.library.UnitID;
import magellan.library.gamebinding.GameSpecificStuff;
import magellan.library.gamebinding.GameSpecificStuffProvider;
import magellan.library.impl.MagellanSpellImpl;
import magellan.library.utils.CollectionFactory;
import magellan.library.utils.Umlaut;
import magellan.library.utils.UnionCollection;
import magellan.library.utils.filters.CollectionFilters;
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

  // Class with gamespecific things...
  private GameSpecificStuff gameSpecificStuff;

  private String orderFileStartingString = "ERESSEA";

  // this is the only point where metaMap is changed, so unchecked cast is safe
  @SuppressWarnings("unchecked")
  protected <T extends ObjectType> Map<String, T> getMap(Class<T> class1) {
    Map<String, T> result = (Map<String, T>) metaMap.get(class1);
    if (result == null) {
      result = CollectionFactory.<String, T> createSyncOrderedMap();
      metaMap.put(class1, result);
    }
    return result;
  }

  // this is the only point where metaMap is changed, so unchecked cast is safe
  @SuppressWarnings("unchecked")
  protected <T extends ObjectType> Map<String, T> getNamesMap(Class<T> class1) {
    Map<String, T> result = (Map<String, T>) namesMetaMap.get(class1);
    if (result == null) {
      result = CollectionFactory.<String, T> createSyncOrderedMap();
      namesMetaMap.put(class1, result);
    }
    return result;
  }

  protected <T extends ObjectType> T getObjectType(Class<T> class1, ID id, boolean add) {
    Map<String, T> map = getMap(class1);
    Map<String, T> mapNames = getNamesMap(class1);
    T objectType = GenericRules.getObjectType(map, mapNames, id.toString());

    if ((objectType == null) && add) {
      try {
        Constructor<T> constructor = null;
        for (Class<?> idclass : new Class<?>[] { ID.class, StringID.class, CoordinateID.class,
            IntegerID.class, EntityID.class, UnitID.class }) {
          try {
            constructor = class1.getConstructor(idclass);
            try {
              if (!constructor.trySetAccessible())
                return null;
            } catch (NoSuchMethodError e) {
              // must be pre java 9, this is fine
            }
          } catch (Exception e) {
            //
          }
          if (constructor != null) {
            break;
          }
        }
        if (constructor == null)
          throw new RuntimeException("no constructor found");

        GenericRules.addObjectType(objectType = constructor.newInstance(id), map, mapNames);
        // new T(id), mapT, mapTNames);
      } catch (Exception e) {
        GenericRules.log.error("class has no constructor C(ID)", e);
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
  @Deprecated
  public Iterator<RegionType> getRegionTypeIterator() {
    return getMap(RegionType.class).values().iterator();
  }

  /**
   * @see magellan.library.Rules#getRegionType(magellan.library.StringID)
   */
  public RegionType getRegionType(StringID id) {
    return getRegionType(id, false);
  }

  /**
   * @see magellan.library.Rules#getRegionType(magellan.library.StringID, boolean)
   */
  public RegionType getRegionType(StringID id, boolean add) {
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
    if ((id == null) || id.equals(""))
      return null;

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
  @Deprecated
  public Iterator<ShipType> getShipTypeIterator() {
    return getShipTypes().iterator();
  }

  public Collection<ShipType> getShipTypes() {
    return getMap(ShipType.class).values();
  }

  /**
   * @see magellan.library.Rules#getShipType(magellan.library.StringID)
   */
  public ShipType getShipType(StringID id) {
    return getShipType(id, false);
  }

  /**
   * @see magellan.library.Rules#getShipType(magellan.library.StringID, boolean)
   */
  public ShipType getShipType(StringID id, boolean add) {
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
    if ((id == null) || id.equals(""))
      return null;

    return getShipType(StringID.create(id), add);
  }

  /**
   * @see magellan.library.Rules#getBuildingTypeIterator()
   */
  public Iterator<BuildingType> getBuildingTypeIterator() {
    return getBuildingTypes().iterator();
  }

  public Collection<BuildingType> getBuildingTypes() {
    return new UnionCollection<BuildingType>(getMap(CastleType.class).values(), getMap(
        BuildingType.class).values());
    // return getMap(BuildingType.class).values();
  }

  /**
   * @see magellan.library.Rules#getBuildingType(magellan.library.StringID)
   */
  public BuildingType getBuildingType(StringID id) {
    return getBuildingType(id, false);
  }

  /**
   * @see magellan.library.Rules#getBuildingType(magellan.library.StringID, boolean)
   */
  public BuildingType getBuildingType(StringID id, boolean add) {
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
    if ((id == null) || id.equals(""))
      return null;

    return getBuildingType(StringID.create(id), add);
  }

  /**
   * @see magellan.library.Rules#getCastleTypeIterator()
   */
  public Iterator<CastleType> getCastleTypeIterator() {
    return CollectionFilters.getIterator(CastleType.class, getBuildingTypes());
  }

  public Collection<CastleType> getCastleTypes() {
    return CollectionFilters.getCollection(CastleType.class, getBuildingTypes());
  }

  /**
   * @see magellan.library.Rules#getCastleType(magellan.library.StringID)
   */
  public CastleType getCastleType(StringID id) {
    return getCastleType(id, false);
  }

  /**
   * @see magellan.library.Rules#getCastleType(magellan.library.StringID, boolean)
   */
  public CastleType getCastleType(StringID id, boolean add) {
    BuildingType t = getBuildingType(id, false);
    if (t == null && add) {
      t = new CastleType(id);
      GenericRules.addObjectType(t, getMap(BuildingType.class), getNamesMap(BuildingType.class));
    }
    if (t instanceof CastleType)
      return (CastleType) t;
    else
      return null;
    // return getObjectType(CastleType.class, id, add);
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
    if ((id == null) || id.equals(""))
      return null;

    return getCastleType(StringID.create(id), add);
  }

  /**
   * @see magellan.library.Rules#getRaceIterator()
   * @deprecated you may use getRaces()
   */
  @Deprecated
  public Iterator<Race> getRaceIterator() {
    return getRaces().iterator();
  }

  public Collection<Race> getRaces() {
    return getMap(Race.class).values();
  }

  /**
   * @see magellan.library.Rules#getRace(magellan.library.StringID)
   */
  public Race getRace(StringID id) {
    return getRace(id, false);
  }

  /**
   * @see magellan.library.Rules#getRace(magellan.library.StringID, boolean)
   */
  public Race getRace(StringID id, boolean add) {
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
    if ((id == null) || id.equals(""))
      return null;

    return getRace(StringID.create(id), add);
  }

  /**
   * @see magellan.library.Rules#getItemTypeIterator()
   * @deprecated you may use getItemTypes()
   */
  @Deprecated
  public Iterator<ItemType> getItemTypeIterator() {
    return getItemTypes().iterator();
  }

  public Collection<ItemType> getItemTypes() {
    return getMap(ItemType.class).values();
  }

  /**
   * @see magellan.library.Rules#getItemType(magellan.library.StringID)
   */
  public ItemType getItemType(StringID id) {
    return getItemType(id, false);
  }

  /**
   * @see magellan.library.Rules#getItemType(magellan.library.StringID, boolean)
   */
  public ItemType getItemType(StringID id, boolean add) {
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
    if ((id == null) || id.equals(""))
      return null;

    return getItemType(StringID.create(id), add);
  }

  /**
   * @see magellan.library.Rules#getAllianceCategoryIterator()
   * @deprecated you may use {@link #getAllianceCategories()}
   */
  @Deprecated
  public Iterator<AllianceCategory> getAllianceCategoryIterator() {
    return getAllianceCategories().iterator();
  }

  public Collection<AllianceCategory> getAllianceCategories() {
    return getMap(AllianceCategory.class).values();
  }

  /**
   * @see magellan.library.Rules#getAllianceCategory(magellan.library.StringID)
   */
  public AllianceCategory getAllianceCategory(StringID id) {
    return getAllianceCategory(id, false);
  }

  /**
   * @see magellan.library.Rules#getAllianceCategory(magellan.library.StringID, boolean)
   */
  public AllianceCategory getAllianceCategory(StringID id, boolean add) {
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
    if ((id == null) || id.equals(""))
      return null;

    return getAllianceCategory(StringID.create(id), add);
  }

  /**
   * @see magellan.library.Rules#getOptionCategoryIterator()
   * @deprecated you may use getOptionCategories()
   */
  @Deprecated
  public Iterator<OptionCategory> getOptionCategoryIterator() {
    return getOptionCategories().iterator();
  }

  public Collection<OptionCategory> getOptionCategories() {
    return getMap(OptionCategory.class).values();
  }

  /**
   * @see magellan.library.Rules#getOptionCategory(magellan.library.StringID)
   */
  public OptionCategory getOptionCategory(StringID id) {
    return getOptionCategory(id, false);
  }

  /**
   * @see magellan.library.Rules#getOptionCategory(magellan.library.StringID, boolean)
   */
  public OptionCategory getOptionCategory(StringID id, boolean add) {
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
    if ((id == null) || id.equals(""))
      return null;

    return getOptionCategory(StringID.create(id), add);
  }

  /**
   * @see magellan.library.Rules#getSkillCategoryIterator()
   * @deprecated you may use {@link #getSkillCategories()}
   */
  @Deprecated
  public Iterator<SkillCategory> getSkillCategoryIterator() {
    return getSkillCategories().iterator();
  }

  public Collection<SkillCategory> getSkillCategories() {
    return getMap(SkillCategory.class).values();
  }

  /**
   * @see magellan.library.Rules#getSkillCategory(magellan.library.StringID)
   */
  public SkillCategory getSkillCategory(StringID id) {
    return getSkillCategory(id, false);
  }

  /**
   * @see magellan.library.Rules#getSkillCategory(magellan.library.StringID, boolean)
   */
  public SkillCategory getSkillCategory(StringID id, boolean add) {
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
    if ((id == null) || id.equals(""))
      return null;

    return getSkillCategory(StringID.create(id), add);
  }

  /**
   * @see magellan.library.Rules#getItemCategoryIterator()
   * @deprecated use getItemCategories
   */
  @Deprecated
  public Iterator<ItemCategory> getItemCategoryIterator() {
    return getItemCategories().iterator();
  }

  public Collection<ItemCategory> getItemCategories() {
    return getMap(ItemCategory.class).values();
  }

  /**
   * @see magellan.library.Rules#getItemCategory(magellan.library.StringID)
   */
  public ItemCategory getItemCategory(StringID id) {
    return getItemCategory(id, false);
  }

  /**
   * @see magellan.library.Rules#getItemCategory(magellan.library.StringID, boolean)
   */
  public ItemCategory getItemCategory(StringID id, boolean add) {
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
    if ((id == null) || id.equals(""))
      return null;

    return getItemCategory(StringID.create(id), add);
  }

  /**
   * @see magellan.library.Rules#getSkillTypeIterator()
   * @deprecated u may use {@link #getSkillTypes()}
   */
  @Deprecated
  public Iterator<SkillType> getSkillTypeIterator() {
    return getSkillTypes().iterator();
  }

  public Collection<SkillType> getSkillTypes() {
    return getMap(SkillType.class).values();
  }

  /**
   * Shorthand for <code>getSkillType(id, false)</code>.
   *
   * @see magellan.library.Rules#getSkillType(magellan.library.StringID)
   */
  public SkillType getSkillType(StringID id) {
    return getSkillType(id, false);
  }

  /**
   * Returns the skill type with given id. If there is no such skill type and
   * <code>add == true</code>, a new skill type is added and returns. Otherwise, <code>null</code> is
   * returned.
   *
   * @see magellan.library.Rules#getSkillType(magellan.library.StringID, boolean)
   */
  public SkillType getSkillType(StringID id, boolean add) {
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
   * <code>add == true</code>, a new skill type is added and returns. Otherwise, <code>null</code> is
   * returned.
   */
  public SkillType getSkillType(String id, boolean add) {
    if ((id == null) || id.equals(""))
      return null;

    return getSkillType(StringID.create(id), add);
  }

  /**
   * Returns a spell for the given id, or <code>null</code>.
   */
  public MagellanSpellImpl getSpell(String id) {
    return getSpell(id, false);
  }

  /**
   * Returns a spell for the given id, or <code>null</code>. Tries to add a spell if
   * <code>add == true</code>
   */
  public MagellanSpellImpl getSpell(String id, boolean add) {
    if ((id == null) || id.equals(""))
      return null;

    return getSpell(StringID.create(id), add);
  }

  /**
   * Returns a spell for the given id, or <code>null</code>. Tries to add a spell if
   * <code>add == true</code>
   */
  private MagellanSpellImpl getSpell(StringID id, boolean add) {
    return getObjectType(MagellanSpellImpl.class, id, add);
  }

  /**
   * Returns all spells.
   */
  public Collection<MagellanSpellImpl> getSpells() {
    return getMap(MagellanSpellImpl.class).values();
  }

  /**
   * @see magellan.library.Rules#getOrder(java.lang.String)
   */
  public OrderType getOrder(String id) {
    return getObjectType(OrderType.class, StringID.create(id), false);
  }

  /**
   * @see magellan.library.Rules#getOrder(magellan.library.StringID)
   */
  public OrderType getOrder(StringID id) {
    return getObjectType(OrderType.class, id, false);
  }

  /**
   * @see magellan.library.Rules#getOrder(magellan.library.StringID, boolean)
   */
  public OrderType getOrder(StringID id, boolean add) {
    return getObjectType(OrderType.class, id, add);
  }

  /**
   * Returns all orders.
   */
  public Collection<OrderType> getOrders() {
    return getMap(OrderType.class).values();
  }

  /**
   * @see magellan.library.Rules#getFaction(magellan.library.StringID)
   */
  public FactionType getFaction(StringID id) {
    return getObjectType(FactionType.class, id, false);
  }

  /**
   * @see magellan.library.Rules#getFaction(magellan.library.StringID, boolean)
   */
  public FactionType getFaction(StringID id, boolean add) {
    return getObjectType(FactionType.class, id, add);
  }

  /**
   * Returns all factionss.
   */
  public Collection<FactionType> getFactions() {
    return getMap(FactionType.class).values();
  }

  /**
   * @see magellan.library.Rules#changeName(java.lang.String, java.lang.String)
   */
  public ObjectType changeName(String from, String to) {
    return changeName(StringID.create(from), to);
  }

  /**
   * Changes the name of an object identified by the specified id. This method serves as a convenience
   * as it relieves the implementor of the arduous task of determining the kind of object type
   * (ItemType, SkillType etc.) and accessing the corresponding data structures. It also ensures that
   * the object is also accessible by calling the getXXX methods with the new name.
   */
  protected ObjectType changeName(ID id, String name) {
    ObjectType ot = null;

    ot = GenericRules.changeName(id, name, getMap(RegionType.class), getNamesMap(RegionType.class));
    if (ot != null)
      return ot;

    ot = GenericRules.changeName(id, name, getMap(ShipType.class), getNamesMap(ShipType.class));
    if (ot != null)
      return ot;

    ot =
        GenericRules.changeName(id, name, getMap(BuildingType.class),
            getNamesMap(BuildingType.class));
    if (ot != null)
      return ot;

    ot = GenericRules.changeName(id, name, getMap(CastleType.class), getNamesMap(CastleType.class));
    if (ot != null)
      return ot;

    ot = GenericRules.changeName(id, name, getMap(Race.class), getNamesMap(Race.class));
    if (ot != null)
      return ot;

    ot = GenericRules.changeName(id, name, getMap(ItemType.class), getNamesMap(ItemType.class));

    if (ot != null)
      return ot;

    // pavkovic 2004.03.17: Don't change the name of alliance and option category
    // ot = changeName(id, name, getMap(AllianceCategory.class),
    // getNamesMap(AllianceCategory.class));
    // if(ot != null) {
    // return ot;
    // }
    // ot = changeName(id, name, getMap(OptionCategory.class), getNamesMap(OptionCategory.class));
    // if(ot != null) {
    // return ot;
    // }
    ot =
        GenericRules.changeName(id, name, getMap(ItemCategory.class),
            getNamesMap(ItemCategory.class));

    if (ot != null)
      return ot;

    ot =
        GenericRules.changeName(id, name, getMap(SkillCategory.class),
            getNamesMap(SkillCategory.class));

    if (ot != null)
      return ot;

    ot = GenericRules.changeName(id, name, getMap(SkillType.class), getNamesMap(SkillType.class));

    if (ot != null)
      return ot;

    ot =
        GenericRules.changeName(id, name, getMap(MagellanSpellImpl.class),
            getNamesMap(MagellanSpellImpl.class));

    if (ot != null)
      return ot;

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
      GenericRules.addObjectType(ot, mapObjectType, mapObjectTypeNames);
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
  protected static <T extends ObjectType> void addObjectType(T o, Map<String, T> mapObjectType,
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
   * Tries to retrieve an object type form the specified map by its name. If the name is not used as a
   * key in the map but an object with the specified name exists, the object is put into the map with
   * the name as its key for speeding up future look-ups.
   */
  protected static <T extends Named> T getObjectType(Map<String, T> objects, Map<String, T> names,
      String name) {
    String normName = Umlaut.normalize(name);

    if (names.containsKey(normName))
      return names.get(normName);
    else {
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
    return objects.get(normName);
  }

  private String gameSpecificStuffClassName;

  private String gameName;

  /**
   * Sets the name of the class for getGameSpecificStuff()
   *
   * @throws IOException If rules cannot be read
   * @see magellan.library.Rules#setGameSpecificStuffClassName(java.lang.String)
   */
  public void setGameSpecificStuffClassName(String className) throws IOException {
    gameSpecificStuffClassName = className;
  }

  /**
   * Returns the GameSpecificStuff object for the name specified by setGameSpecificClassName.
   *
   * @see magellan.library.Rules#getGameSpecificStuff()
   */
  public GameSpecificStuff getGameSpecificStuff() {
    if (gameSpecificStuff == null) {
      if (gameSpecificStuffClassName == null) {
        gameSpecificStuff = new GameSpecificStuffProvider().getGameSpecificStuff();
      } else {
        gameSpecificStuff =
            new GameSpecificStuffProvider().getGameSpecificStuff(gameSpecificStuffClassName,
                this);
      }
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

  @SuppressWarnings("unused")
  private void dummy() {
    // for debugging purposes only.
    // As this class uses reflection, I added this method to be able to trace constructor calls in
    // call hierarchy
    new AllianceCategory(StringID.create("foo"));
    new BuildingType(StringID.create("foo"));
    new CastleType(StringID.create("foo"));
    new ItemCategory(StringID.create("foo"));
    new ItemType(StringID.create("foo"));
    new OptionCategory(StringID.create("foo"));
    new Race(StringID.create("foo"));
    new RegionType(StringID.create("foo"));
    new ShipType(StringID.create("foo"));
    new SkillCategory(StringID.create("foo"));
    new SkillType(StringID.create("foo"));
    new MagellanSpellImpl(StringID.create("foo"), null);
  }

  public void setGameName(String name) {
    gameName = name;
  }

  public String getGameName() {
    return gameName;
  }

}
