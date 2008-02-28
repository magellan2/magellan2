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
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import magellan.library.ID;
import magellan.library.Rules;
import magellan.library.StringID;
import magellan.library.gamebinding.GameSpecificStuff;
import magellan.library.gamebinding.GameSpecificStuffProvider;
import magellan.library.utils.OrderedHashtable;
import magellan.library.utils.Umlaut;
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

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Iterator<RegionType> getRegionTypeIterator() {
		return getIterator(RegionType.class, mapUnitContainerType);
	}

	/**
	 * DOCUMENT-ME
	 */
	public RegionType getRegionType(ID id) {
		return getRegionType(id, false);
	}

	/**
	 * DOCUMENT-ME
	 *
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
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public RegionType getRegionType(String id) {
		return getRegionType(id, false);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public RegionType getRegionType(String id, boolean add) {
		if((id == null) || id.equals("")) {
			return null;
		}

		return getRegionType(StringID.create(id), add);
	}

  /**
   * All RegionTypes in one Collection
   * @return
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
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Iterator getShipTypeIterator() {
		return getIterator(ShipType.class, mapUnitContainerType);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public ShipType getShipType(ID id) {
		return getShipType(id, false);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
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
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public ShipType getShipType(String id) {
		return getShipType(id, false);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public ShipType getShipType(String id, boolean add) {
		if((id == null) || id.equals("")) {
			return null;
		}

		return getShipType(StringID.create(id), add);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Iterator<BuildingType> getBuildingTypeIterator() {
		return getIterator(BuildingType.class, mapUnitContainerType);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public BuildingType getBuildingType(ID id) {
		return getBuildingType(id, false);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
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
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public BuildingType getBuildingType(String id) {
		return getBuildingType(id, false);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public BuildingType getBuildingType(String id, boolean add) {
		if((id == null) || id.equals("")) {
			return null;
		}

		return getBuildingType(StringID.create(id), add);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Iterator getCastleTypeIterator() {
		return getIterator(CastleType.class, mapUnitContainerType);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public CastleType getCastleType(ID id) {
		return getCastleType(id, false);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
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
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public CastleType getCastleType(String id) {
		return getCastleType(id, false);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public CastleType getCastleType(String id, boolean add) {
		if((id == null) || id.equals("")) {
			return null;
		}

		return getCastleType(StringID.create(id), add);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Iterator getRaceIterator() {
		return getIterator(Race.class, mapUnitContainerType);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public Race getRace(ID id) {
		return getRace(id, false);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
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
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public Race getRace(String id) {
		return getRace(id, false);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public Race getRace(String id, boolean add) {
		if((id == null) || id.equals("")) {
			return null;
		}

		return getRace(StringID.create(id), add);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Iterator<ItemType> getItemTypeIterator() {
		return getIterator(ItemType.class, mapItemType);
	}

	/**
	 * 
	 */
	public ItemType getItemType(ID id) {
		return getItemType(id, false);
	}

	/**
   * 
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
	 * 
	 */
	public ItemType getItemType(String id) {
		return getItemType(id, false);
	}

	/**
	 * 
	 */
	public ItemType getItemType(String id, boolean add) {
		if((id == null) || id.equals("")) {
			return null;
		}

		return getItemType(StringID.create(id), add);
	}

	/**
   * 
	 */
	public Iterator getAllianceCategoryIterator() {
		return getIterator(AllianceCategory.class, mapAllianceCategory);
	}

	/**
	 * 
	 */
	public AllianceCategory getAllianceCategory(ID id) {
		return getAllianceCategory(id, false);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
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
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public AllianceCategory getAllianceCategory(String id) {
		return getAllianceCategory(id, false);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public AllianceCategory getAllianceCategory(String id, boolean add) {
		if((id == null) || id.equals("")) {
			return null;
		}

		return getAllianceCategory(StringID.create(id), add);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Iterator getOptionCategoryIterator() {
		return getIterator(OptionCategory.class, mapOptionCategory);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public OptionCategory getOptionCategory(ID id) {
		return getOptionCategory(id, false);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
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
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public OptionCategory getOptionCategory(String id) {
		return getOptionCategory(id, false);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public OptionCategory getOptionCategory(String id, boolean add) {
		if((id == null) || id.equals("")) {
			return null;
		}

		return getOptionCategory(StringID.create(id), add);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Iterator getSkillCategoryIterator() {
		return getIterator(SkillCategory.class, mapSkillCategory);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public SkillCategory getSkillCategory(ID id) {
		return getSkillCategory(id, false);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
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
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public SkillCategory getSkillCategory(String id) {
		return getSkillCategory(id, false);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public SkillCategory getSkillCategory(String id, boolean add) {
		if((id == null) || id.equals("")) {
			return null;
		}

		return getSkillCategory(StringID.create(id), add);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Iterator getItemCategoryIterator() {
		return getIterator(ItemCategory.class, mapItemCategory);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public ItemCategory getItemCategory(ID id) {
		return getItemCategory(id, false);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
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
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public ItemCategory getItemCategory(String id) {
		return getItemCategory(id, false);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public ItemCategory getItemCategory(String id, boolean add) {
		if((id == null) || id.equals("")) {
			return null;
		}

		return getItemCategory(StringID.create(id), add);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Iterator getSkillTypeIterator() {
		return getIterator(SkillType.class, mapSkillType);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public SkillType getSkillType(ID id) {
		return getSkillType(id, false);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
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
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public SkillType getSkillType(String id) {
		return getSkillType(id, false);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public SkillType getSkillType(String id, boolean add) {
		if((id == null) || id.equals("")) {
			return null;
		}

		return getSkillType(StringID.create(id), add);
	}

	private Iterator getIterator(Class c, Map m) {
    if (m != null && m.values() != null)
      return new ClassIterator(c,Collections.unmodifiableCollection(m.values()).iterator());
    else
      return new ClassIterator(c,Collections.emptyList().iterator());
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
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
	 *
	 * 
	 * 
	 *
	 * 
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

	protected ObjectType changeName(ID id, String name, Map<String,ObjectType> mapObjectType, Map<String,ObjectType> mapObjectTypeNames) {
    String key = Umlaut.normalize(id.toString());
		ObjectType ot = (ObjectType) mapObjectType.get(key);

		if(ot != null) {
			mapObjectTypeNames.remove(Umlaut.normalize(ot.getName()));
			ot.setName(name);
			addObject(ot, mapObjectType, mapObjectTypeNames);
		}

		return null;
	}

	/**
	 * Adds the specified object to the specified map by id and by name.
	 *
	 * 
	 * 
	 * 
	 *
	 * 
	 */
	private ObjectType addObject(ObjectType o, Map<String,ObjectType> mapObjectType, Map<String,ObjectType> mapObjectTypeNames) {
		if(log.isDebugEnabled()) {
			log.debug("GenericRules.addObject(" + o.getClass().toString() + "," + o.getID() + ")");
		}

		//mapObjectType.put(o.getID().toString(), o);
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
	private ObjectType getObjectType(Map<String,ObjectType> objects, Map<String, ObjectType> names, String name) {
		String normName = Umlaut.normalize(name);

		if(names.containsKey(normName)) {
			return names.get(normName);
		} else {
			for(Iterator<ObjectType> iter = objects.values().iterator(); iter.hasNext();) {
				ObjectType ot = iter.next();

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
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setGameSpecificStuffClassName(String className) {
		gameSpecificStuffClassName = className;
	}

	private GameSpecificStuff gameSpecificStuff;

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public GameSpecificStuff getGameSpecificStuff() {
		if(gameSpecificStuff == null) {
			gameSpecificStuff = new GameSpecificStuffProvider().getGameSpecificStuff(gameSpecificStuffClassName);
		}

		return gameSpecificStuff;
	}

	/**
	 * An iterator implementation to iterate a Map of objects and return only  returns object
	 * instances of the given Class.
	 */
	private static class ClassIterator implements Iterator {
		private Class givenClass;
		private Iterator givenIterator;
		private Object currentObject;

		/**
		 * Creates a new ClassIterator object.
		 *
		 * 
		 * 
		 *
		 * @throws NullPointerException DOCUMENT-ME
		 */
		public ClassIterator(Class c, Iterator i) {
			if(c == null) {
				throw new NullPointerException();
			}

			if(i == null) {
				throw new NullPointerException();
			}

			givenClass = c;
			givenIterator = i;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public boolean hasNext() {
			possiblyMoveToNext();

			return currentObject != null;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 *
		 * @throws NoSuchElementException DOCUMENT-ME
		 */
		public Object next() {
			possiblyMoveToNext();

			if(currentObject == null) {
				throw new NoSuchElementException();
			}

			Object ret = currentObject;
			currentObject = null;

			return ret;
		}

		private void possiblyMoveToNext() {
			if(currentObject != null) {
				return;
			}

			try {
				Object newObject = null;

				while(givenIterator.hasNext() && (newObject == null)) {
					newObject = givenIterator.next();

					if(!givenClass.isInstance(newObject)) {
						newObject = null;
					}
				}

				currentObject = newObject;
			} catch(NoSuchElementException e) {
			}
		}

		/**
		 * DOCUMENT-ME
		 */
		public void remove() {
			givenIterator.remove();
		}
	}
}
