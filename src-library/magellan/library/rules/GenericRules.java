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

package magellan.library.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import magellan.library.ID;
import magellan.library.Named;
import magellan.library.Rules;
import magellan.library.StringID;
import magellan.library.gamebinding.GameSpecificStuff;
import magellan.library.gamebinding.GameSpecificStuffProvider;
import magellan.library.utils.OrderedHashtable;
import magellan.library.utils.Umlaut;
import magellan.library.utils.filters.CollectionFilters;
import magellan.library.utils.logging.Logger;


/**
 * A class implementing Eressea specific rules. Primarily, this class collects all the well-known
 * object-types which in turn provide information about their properties as they are defined in
 * the rules of Eressea. In fact, there is nothing eressea specific in Rules anymore, so this is
 * the generic rules object.
 */
public class GenericRules implements Rules {
	private static final Logger log = Logger.getInstance(GenericRules.class);

	// Map consisting of Race, RegionType, ShipType, BuildingType, CastleType
	private Map<String,ObjectType> mapUnitContainerType = new OrderedHashtable<String, ObjectType>();
	private Map<String,ObjectType> mapUnitContainerTypeNames = new OrderedHashtable<String, ObjectType>();

	// Map consisting of ItemType
	private Map<String,ObjectType> mapItemType = new OrderedHashtable<String, ObjectType>();
	private Map<String,ObjectType> mapItemTypeNames = new OrderedHashtable<String, ObjectType>();

	// Map consisting of AllianceCategory
	private Map<String,ObjectType> mapAllianceCategory = new OrderedHashtable<String, ObjectType>();
	private Map<String,ObjectType> mapAllianceCategoryNames = new OrderedHashtable<String, ObjectType>();

	// Map consisting of OptionCategory
	private Map<String,ObjectType> mapOptionCategory = new OrderedHashtable<String, ObjectType>();
	private Map<String,ObjectType> mapOptionCategoryNames = new OrderedHashtable<String, ObjectType>();

	// Map consisting of ItemCategory
	private Map<String,ObjectType> mapItemCategory = new OrderedHashtable<String, ObjectType>();
	private Map<String,ObjectType> mapItemCategoryNames = new OrderedHashtable<String, ObjectType>();

	// Map consisting of SkillCategory
	private Map<String,ObjectType> mapSkillCategory = new OrderedHashtable<String, ObjectType>();
	private Map<String,ObjectType> mapSkillCategoryNames = new OrderedHashtable<String, ObjectType>();

	// Map consisting of SkillType
	private Map<String,ObjectType> mapSkillType = new OrderedHashtable<String, ObjectType>();
	private Map<String,ObjectType> mapSkillTypeNames = new OrderedHashtable<String, ObjectType>();
	
	// Class with gamespecific things...
	private GameSpecificStuff gameSpecificStuff;
	
	private String orderFileStartingString = "ERESSEA";

	/**
	 * @see magellan.library.Rules#getRegionTypeIterator()
	 */
	public Iterator<RegionType> getRegionTypeIterator() {
		return CollectionFilters.getValueIterator(RegionType.class, mapUnitContainerType);
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
		Object uct = getObjectType(mapUnitContainerType, mapUnitContainerTypeNames, id.toString());

		if((uct != null) && !(uct instanceof RegionType)) {
			return null;
		}

		RegionType r = (RegionType) uct;

		if((r == null) && add) {
			r = (RegionType) addObject(new RegionType(id), mapUnitContainerType,
									   mapUnitContainerTypeNames);
			r.setName(id.toString());
		}

		return r;
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
		if((id == null) || id.equals("")) {
			return null;
		}

		return getRegionType(StringID.create(id), add);
	}

  /**
   * All RegionTypes in one Collection
   */
  public Collection<RegionType> getRegionTypes(){
    if (this.mapUnitContainerType!=null && this.mapUnitContainerType.size()>0){
      ArrayList<RegionType> erg = new ArrayList<RegionType>();
      for (Iterator iter = this.getRegionTypeIterator();iter.hasNext();){
        RegionType actRegionType = (RegionType)iter.next();
        erg.add(actRegionType);
      }
      return erg;
    } else {
      return null;
    }
  }
  
	/**
	 * @see magellan.library.Rules#getShipTypeIterator()
	 */
	public Iterator<ShipType> getShipTypeIterator() {
		return CollectionFilters.getValueIterator(ShipType.class, mapUnitContainerType);
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
		Object uct = getObjectType(mapUnitContainerType, mapUnitContainerTypeNames, id.toString());

		if((uct != null) && !(uct instanceof ShipType)) {
			return null;
		}

		ShipType r = (ShipType) uct;

		if((r == null) && add) {
			r = (ShipType) addObject(new ShipType(id), mapUnitContainerType,
									 mapUnitContainerTypeNames);
			r.setName(id.toString());
		}

		return r;
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
		if((id == null) || id.equals("")) {
			return null;
		}

		return getShipType(StringID.create(id), add);
	}

	/**
	 * @see magellan.library.Rules#getBuildingTypeIterator()
	 */
	public Iterator<BuildingType> getBuildingTypeIterator() {
		return CollectionFilters.getValueIterator(BuildingType.class, mapUnitContainerType);
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
		Object uct = getObjectType(mapUnitContainerType, mapUnitContainerTypeNames, id.toString());

		if((uct != null) && !(uct instanceof BuildingType)) {
			return null;
		}

		BuildingType r = (BuildingType) uct;

		if((r == null) && add) {
			r = (BuildingType) addObject(new BuildingType(id), mapUnitContainerType,
										 mapUnitContainerTypeNames);
			r.setName(id.toString());
		}

		return r;
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
		if((id == null) || id.equals("")) {
			return null;
		}

		return getBuildingType(StringID.create(id), add);
	}

	/**
	 * @see magellan.library.Rules#getCastleTypeIterator()
	 */
	public Iterator<CastleType> getCastleTypeIterator() {
		return CollectionFilters.getValueIterator(CastleType.class, mapUnitContainerType);
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
		Object uct = getObjectType(mapUnitContainerType, mapUnitContainerTypeNames, id.toString());

		if((uct != null) && !(uct instanceof CastleType)) {
			return null;
		}

		CastleType r = (CastleType) uct;

		if((r == null) && add) {
			r = (CastleType) addObject(new CastleType(id), mapUnitContainerType,
									   mapUnitContainerTypeNames);
			r.setName(id.toString());
		}

		return r;
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
		if((id == null) || id.equals("")) {
			return null;
		}

		return getCastleType(StringID.create(id), add);
	}

	/**
	 * @see magellan.library.Rules#getRaceIterator()
	 */
	public Iterator<Race> getRaceIterator() {
		return CollectionFilters.getValueIterator(Race.class, mapUnitContainerType);
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
		ObjectType uct = getObjectType(mapUnitContainerType, mapUnitContainerTypeNames, id.toString());

		if((uct != null) && !(uct instanceof Race)) {
			return null;
		}

		Race r = (Race) uct;

		if((r == null) && add) {
			r = (Race) addObject(new Race(id), mapUnitContainerType, mapUnitContainerTypeNames);
			r.setName(id.toString());
		}

		return r;
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
		if((id == null) || id.equals("")) {
			return null;
		}

		return getRace(StringID.create(id), add);
	}

	/**
	 * @see magellan.library.Rules#getItemTypeIterator()
	 */
	public Iterator<ItemType> getItemTypeIterator() {
		return CollectionFilters.getValueIterator(ItemType.class, mapItemType);
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
		ItemType r = (ItemType) getObjectType(mapItemType, mapItemTypeNames, id.toString());

		if((r == null) && add) {
			r = (ItemType) addObject(new ItemType(id), mapItemType, mapItemTypeNames);
			r.setName(id.toString());
		}

		return r;
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
		if((id == null) || id.equals("")) {
			return null;
		}

		return getItemType(StringID.create(id), add);
	}

	/**
	 * @see magellan.library.Rules#getAllianceCategoryIterator()
	 */
	public Iterator<AllianceCategory> getAllianceCategoryIterator() {
		return CollectionFilters.getValueIterator(AllianceCategory.class, mapAllianceCategory);
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
		AllianceCategory r = (AllianceCategory) getObjectType(mapAllianceCategory,
															  mapAllianceCategoryNames,
															  id.toString());

		if((r == null) && add) {
			r = (AllianceCategory) addObject(new AllianceCategory(id), mapAllianceCategory,
											 mapAllianceCategoryNames);
			r.setName(id.toString());
		}

		return r;
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
		if((id == null) || id.equals("")) {
			return null;
		}

		return getAllianceCategory(StringID.create(id), add);
	}

	/**
	 * @see magellan.library.Rules#getOptionCategoryIterator()
	 */
	public Iterator<OptionCategory> getOptionCategoryIterator() {
		return CollectionFilters.getValueIterator(OptionCategory.class, mapOptionCategory);
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
		OptionCategory r = (OptionCategory) getObjectType(mapOptionCategory,
														  mapOptionCategoryNames, id.toString());

		if((r == null) && add) {
			r = (OptionCategory) addObject(new OptionCategory(id), mapOptionCategory,
										   mapOptionCategoryNames);
			r.setName(id.toString());
		}

		return r;
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
		if((id == null) || id.equals("")) {
			return null;
		}

		return getOptionCategory(StringID.create(id), add);
	}

	/**
	 * @see magellan.library.Rules#getSkillCategoryIterator()
	 */
	public Iterator<SkillCategory> getSkillCategoryIterator() {
		return CollectionFilters.getValueIterator(SkillCategory.class, mapSkillCategory);
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
		SkillCategory r = (SkillCategory) getObjectType(mapSkillCategory, mapSkillCategoryNames,
														id.toString());

		if((r == null) && add) {
			r = (SkillCategory) addObject(new SkillCategory(id), mapSkillCategory,
										  mapSkillCategoryNames);
			r.setName(id.toString());
		}

		return r;
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
		if((id == null) || id.equals("")) {
			return null;
		}

		return getSkillCategory(StringID.create(id), add);
	}

	/**
	 * @see magellan.library.Rules#getItemCategoryIterator()
	 */
	public Iterator<ItemCategory> getItemCategoryIterator() {
	  return CollectionFilters.getValueIterator(ItemCategory.class, mapItemCategory);
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
		ItemCategory r = (ItemCategory) getObjectType(mapItemCategory, mapItemCategoryNames,
													  id.toString());

		if((r == null) && add) {
			r = (ItemCategory) addObject(new ItemCategory(id), mapItemCategory, mapItemCategoryNames);
			r.setName(id.toString());
		}

		return r;
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
		if((id == null) || id.equals("")) {
			return null;
		}

		return getItemCategory(StringID.create(id), add);
	}

	/**
	 * @see magellan.library.Rules#getSkillTypeIterator()
	 */
	public Iterator<SkillType> getSkillTypeIterator() {
		return CollectionFilters.getValueIterator(SkillType.class, mapSkillType);
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
	 * Returns the skill type with given id. If there is no such skill type and <code>add == true</code>, 
	 * a new skill type is added and returns. Otherwise, <code>null</code> is returned.
	 *
	 * @see magellan.library.Rules#getSkillType(magellan.library.ID, boolean)
	 */
	public SkillType getSkillType(ID id, boolean add) {
		SkillType r = (SkillType) getObjectType(mapSkillType, mapSkillTypeNames, id.toString());

		if((r == null) && add) {
			r = (SkillType) addObject(new SkillType(id), mapSkillType, mapSkillTypeNames);
			r.setName(id.toString());
		}

		return r;
	}

	/**
   * Shorthand for <code>getSkillType(id, false)</code>. 
	 */
	public SkillType getSkillType(String id) {
		return getSkillType(id, false);
	}

	/**
   * Returns the skill type with given id. If there is no such skill type and <code>add == true</code>, 
   * a new skill type is added and returns. Otherwise, <code>null</code> is returned.
	 */
	public SkillType getSkillType(String id, boolean add) {
		if((id == null) || id.equals("")) {
			return null;
		}

		return getSkillType(StringID.create(id), add);
	}

	/**
	 * 
	 * @see magellan.library.Rules#changeName(java.lang.String, java.lang.String)
	 */
	public ObjectType changeName(String from, String to) {
		return changeName(StringID.create(from), to);
	}

	/**
	 * Changes the name of an object identified by the specified id.  This method serves as a
	 * convenience as it relieves the implementor of the arduous task of determining the kind of
	 * object type (ItemType, SkillType etc.) and accessing the corresponding data structures. It
	 * also ensures that the object is also accessible by calling the getXXX methods with the new
	 * name.
	 */
	private ObjectType changeName(ID id, String name) {
		ObjectType ot = null;

		ot = changeName(id, name, mapUnitContainerType, mapUnitContainerTypeNames);

		if(ot != null) {
			return ot;
		}

		ot = changeName(id, name, mapItemType, mapItemTypeNames);

		if(ot != null) {
			return ot;
		}

		// pavkovic 2004.03.17: Don't change the name of alliance and option category
		// 		ot = changeName(id, name, mapAllianceCategory, mapAllianceCategoryNames);
		// 		if(ot != null) {
		// 			return ot;
		// 		}
		// 		ot = changeName(id, name, mapOptionCategory, mapOptionCategoryNames);
		// 		if(ot != null) {
		// 			return ot;
		// 		}
		ot = changeName(id, name, mapItemCategory, mapItemCategoryNames);

		if(ot != null) {
			return ot;
		}

		ot = changeName(id, name, mapSkillCategory, mapSkillCategoryNames);

		if(ot != null) {
			return ot;
		}

		ot = changeName(id, name, mapSkillType, mapSkillTypeNames);

		if(ot != null) {
			return ot;
		}

		return null;
	}

  // this should do the trick here, but for some reason this led to an error when executing build.xml:
	//	protected static <T extends ObjectType> T changeName(ID id, String name, Map<String,T> mapObjectType, Map<String,T> mapObjectTypeNames) {
	protected static ObjectType changeName(ID id, String name, Map<String,ObjectType> mapObjectType, Map<String,ObjectType> mapObjectTypeNames) {
    String key = Umlaut.normalize(id.toString());
    ObjectType ot = mapObjectType.get(key);

		if(ot != null) {
			mapObjectTypeNames.remove(Umlaut.normalize(ot.getName()));
			ot.setName(name);
			addObject(ot, mapObjectType, mapObjectTypeNames);
		}

		return null;
	}

	// this should do the trick here, but for some reason this led to an error when executing build.xml:
	//	private static <T extends ObjectType> T addObject(T o, Map<String,? super T> mapObjectType, Map<String,? super T> mapObjectTypeNames) {
	/**
	 * Adds the specified object to the specified map by id and by name.
	 */
	private static ObjectType addObject(ObjectType o, Map<String,ObjectType> mapObjectType, Map<String,ObjectType> mapObjectTypeNames) {
		if(GenericRules.log.isDebugEnabled()) {
			GenericRules.log.debug("GenericRules.addObject(" + o.getClass().toString() + "," + o.getID() + ")");
		}

    mapObjectType.put(Umlaut.normalize(o.getID().toString()), o);

		if(o.getName() != null) {
			mapObjectTypeNames.put(Umlaut.normalize(o.getName()), o);
		}

		return o;
	}

	/**
	 * Tries to retrieve an object type form the specified map by its name. If the name is not used
	 * as a key in the map but an object with the specified name exists, the object is put into
	 * the map with the name as its key for speeding up future look-ups.
	 */
	private static <T extends Named> T getObjectType(Map<String,T> objects, Map<String, T> names, String name) {
		String normName = Umlaut.normalize(name);

		if(names.containsKey(normName)) {
			return names.get(normName);
		} else {
			for(T ot : objects.values()) {
				if(Umlaut.normalize(ot.getName()).equals(normName)) {
					names.put(normName, ot);
					return ot;
				}
        if(Umlaut.normalize(ot.getID().toString()).equals(normName)) {
          names.put(normName, ot);
          return ot;
        }
			}
		}

		// pavkovic 2004.03.08: for now also return object with id
		//return null;
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
		if(gameSpecificStuff == null) {
			gameSpecificStuff = new GameSpecificStuffProvider().getGameSpecificStuff(gameSpecificStuffClassName);
		}

		return gameSpecificStuff;
	}

	
	/**
	 * 
	 * @see magellan.library.Rules#getOrderfileStartingString()
	 */
	public String getOrderfileStartingString(){
	    return orderFileStartingString;
	}
	
	/**
	 * 
	 * @see magellan.library.Rules#setOrderfileStartingString(java.lang.String)
	 */
	public void setOrderfileStartingString(String startingString){
	  orderFileStartingString = startingString;
	}
	
	
}
