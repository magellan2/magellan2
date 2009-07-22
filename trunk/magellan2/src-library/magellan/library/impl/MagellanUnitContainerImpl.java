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

package magellan.library.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import magellan.library.Building;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.Item;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.relation.EnterRelation;
import magellan.library.relation.LeaveRelation;
import magellan.library.relation.UnitContainerRelation;
import magellan.library.relation.UnitRelation;
import magellan.library.rules.CastleType;
import magellan.library.rules.UnitContainerType;
import magellan.library.utils.Cache;
import magellan.library.utils.CacheHandler;
import magellan.library.utils.OrderedHashtable;
import magellan.library.utils.Sorted;
import magellan.library.utils.TagMap;
import magellan.library.utils.Taggable;
import magellan.library.utils.guiwrapper.CacheableOrderEditor;
import magellan.library.utils.logging.Logger;


/**
 * The implementation of UnitContainer of the Magellan client.
 *
 * @author $Author: $
 * @version $Revision: 389 $
 */
public abstract class MagellanUnitContainerImpl extends MagellanRelatedImpl implements UnitContainer, Sorted, Taggable {
	private static final Logger log = Logger.getInstance(MagellanUnitContainerImpl.class);
	private UnitContainerType type = null;
	private Unit owner = null;
  private Unit ownerUnit = null;


	/**
	 * A list containing <tt>String</tt> objects, specifying  effects on this
	 * <tt>UnitContainer</tt> object.
	 */
	protected List<String> effects = null;

	// TODO hm, could be private, too, just to prevent it to be null
	// but that probably consumes a lot of memory

	/** Comments modifiable by the user. The comments are represented as String objects. */
	protected List<String> comments = null;

	/** The game data this unit capsule refers to. */
	protected GameData data = null;


	// (stm 09-06-08) had to get rid of the soft reference again as it leads to problems with 
	// updates of unit relations.
//	/**
//	 * The cache object containing cached information that may be not related enough to be
//	 * encapsulated as a function and is time consuming to gather.
//	 */
//	protected SoftReference<Cache> cacheReference = null;

	protected Cache cache;

	/**
	 * The items carried by this unitcontainer. The keys are the IDs of the item's type, the values are the
	 * Item objects themselves.
	 */
	protected Map<ID,Item> items = null;

	/**
	 * A map storing all unknown tags for all UnitContainer objects. Keys are IDs of these objects,
	 * values are Maps(should be TagMaps).
	 */
	private Map<String, String> tagMap = null;

	/**
	 * Creates a new UnitContainer object.
	 *
	 * 
	 * 
	 */
	public MagellanUnitContainerImpl(ID id, GameData data) {
		super(id);
		this.data = data;
	}

  /**
   * @see magellan.library.UnitContainer#getOwner()
   */
  public Unit getOwner() {
    return owner;
  }
  /**
   * @see magellan.library.UnitContainer#setOwner(magellan.library.Unit)
   */
  public void setOwner(Unit owner) {
    this.owner = owner;
  }

	/**
	 * Adds an item to the unitcontainer. If the unitcontainer already has an item of the same type, the item is
	 * overwritten with the specified item object.
	 *
	 * 
	 *
	 * @return the specified item i.
	 */
	public Item addItem(Item i) {
		if(items == null) {
			items = new OrderedHashtable<ID, Item>();
		}

		items.put(i.getItemType().getID(), i);

		return i;
	}

	/**
	 * Returns all the items this unit possesses.
	 *
	 * @return a collection of Item objects.
	 */
	public Collection<Item> getItems() {
    if (this.items != null && this.items.values() != null) {
      return Collections.unmodifiableCollection(this.items.values());
    } else {
      return Collections.emptyList();
    }
	}

	/**
	 * @see magellan.library.UnitContainer#setType(magellan.library.rules.UnitContainerType)
	 */
	public void setType(UnitContainerType t) {
		if(t != null) {
			this.type = t;
		} else {
			throw new IllegalArgumentException("UnitContainer.setType(): invalid type specified!");
		}
	}

	/**
	 * Returns the associated GameData
	 *
	 * 
	 */
	public GameData getData() {
		return data;
	}

	/**
	 * returns the type of the UnitContainer
	 *
	 * 
	 */
	public UnitContainerType getType() {
		return type;
	}

	// units are sorted in unit containers with this index
	private int sortIndex = -1;

	/**
	 * Sets an index indicating how instances of class are sorted in the report.
	 *
	 * 
	 */
	public void setSortIndex(int index) {
		this.sortIndex = index;
	}

	/**
	 * Returns an index indicating how instances of class are sorted in the report.
	 *
	 * 
	 */
	public int getSortIndex() {
		return sortIndex;
	}

	/** All units that are in this container. */
	private Map<ID,Unit> units = null;

	/** Provides a collection view of the unit map. */
	private Collection<Unit> unitCollection = null;

	/**
	 * Returns an unmodifiable collection of all the units in this container.
	 */
	public Collection<Unit> units() {
		// note that there is a consistency problem here. If units is
		// null now we create an empty collection, but if units are
		// added later we have to create a new collection object
		// see addUnit()
		if(units == null) {
			return Collections.emptyList();
		}

		if(unitCollection == null) {
      if (units != null && units.values() != null) {
        unitCollection = Collections.unmodifiableCollection(units.values());
      } else {
			  unitCollection = Collections.emptyList();
      }
		}

		return unitCollection;
	}

	/**
	 * Retrieve a unit in this container by id.
	 */
	public Unit getUnit(ID key) {
		if(units != null) {
			return units.get(key);
		} else {
			return null;
		}
	}

	/**
	 * Adds a unit to this container. This method should only be invoked by Unit.setXXX() methods.
	 *
	 * 
	 */
	public void addUnit(Unit u) {
		if(units == null) {
			units = new OrderedHashtable<ID, Unit>();

			// enforce the creation of a new collection view:
			unitCollection = null;
		}

		units.put(u.getID(), u);
	}

	/**
	 * Removes a unit from this container. This method should only be invoked by Unit.setXXX()
	 * methods.
	 *
	 * 
	 *
	 * 
	 */
	public Unit removeUnit(ID key) {
		if(units != null) {
			Unit u = units.remove(key);

			if(units.isEmpty()) {
				units = null;
			}

			return u;
		} else {
			return null;
		}
	}

	/**
	 * @see magellan.library.UnitContainer#modifiedUnits()
	 */
	public Collection<Unit> modifiedUnits() {
		if(!hasCache() || (getCache().modifiedContainerUnits == null)) {
			refreshModifiedUnits();
		}

		if(hasCache() && (getCache().modifiedContainerUnits != null)) {
      if (getCache().modifiedContainerUnits.values() != null) {
        return Collections.unmodifiableCollection(getCache().modifiedContainerUnits.values());
      } else {
        return Collections.emptyList();
      }
		} else {
			return Collections.emptyList();
		}
	}


	@Override
	protected Collection<UnitRelation> getRelations() {
	  if(getCache().relations == null) {
	    getCache().relations = new ArrayList<UnitRelation>();
	  }
	  return getCache().relations;
	}

	/**
	 *  @see magellan.library.UnitContainer#getModifiedUnit(magellan.library.ID)
	 */  
	public Unit getModifiedUnit(ID key) {
	  if(!hasCache() || (getCache().modifiedContainerUnits == null)) {
	    refreshModifiedUnits();
	  }

	  if(getCache().modifiedContainerUnits == null) {
	    return null;
	  }

	  return getCache().modifiedContainerUnits.get(key);
	}

	private void refreshModifiedUnits() {
	  Cache cache = getCache();

		// be careful when clearing modifiedContainerUnits, it could
		// be the normal units
		if(cache.modifiedContainerUnits == units) {
			cache.modifiedContainerUnits = null;
		}

		if(cache.modifiedContainerUnits != null) {
			cache.modifiedContainerUnits.clear();
		}

		// if this unit container does not have relations the
		// modified units equal the normal units
		if(cache.relations == null) {
			if(cache.modifiedContainerUnits != units) {
				if(cache.modifiedContainerUnits != null) {
					cache.modifiedContainerUnits.clear();
				}

				cache.modifiedContainerUnits = units;
			}

			return;
		}

		if(cache.modifiedContainerUnits == null) {
			cache.modifiedContainerUnits = new Hashtable<ID, Unit>();
		}

		if(units != null) {
			cache.modifiedContainerUnits.putAll(units);
		}

		for(Iterator iter = cache.relations.iterator(); iter.hasNext();) {
			UnitRelation rel = (UnitRelation) iter.next();

			if(rel instanceof UnitContainerRelation) {
				UnitContainerRelation ucr = (UnitContainerRelation) rel;

				if(this.equals(ucr.target)) {
					if(ucr instanceof EnterRelation) {
						cache.modifiedContainerUnits.put(ucr.source.getID(), ucr.source);
					} else if(ucr instanceof LeaveRelation) {
						cache.modifiedContainerUnits.remove(ucr.source.getID());
					}
				} else {
					MagellanUnitContainerImpl.log.info("UnitContainer.refreshModifiedUnits(): unit container " + this +
							 " has a relation associated that does not point to it!");
				}
			} else {
				MagellanUnitContainerImpl.log.info("UnitContainer.refreshModifiedUnits(): unit container " + this +
						 " contains a relation that is not a UnitContainerRelation object!");
			}
		}
	}

	/**
	 * @see magellan.library.impl.MagellanNamedImpl#toString()
	 */
	@Override
  public String toString() {
		return getName() + " (" + id + "), " + type;
	}

	/**
	 * @see magellan.library.UnitContainer#setOwnerUnit(magellan.library.Unit)
	 */
	public void setOwnerUnit(Unit unit) {
		this.ownerUnit = unit;
	}

	/**
	 * Returns the unit owning this UnitContainer. If this UnitContainer is an instance of class
	 * Ship or Building the normal owning unit is returned (or null, if there is none). In case of
	 * a Region, the OwnerUnit of the largest castle is returned. In case of a Faction, null is
	 * returned.
	 * 
	 * @see magellan.library.UnitContainer#getOwnerUnit()
	 */
	public Unit getOwnerUnit() {
		if((owner == null) && this instanceof Region) {
			int bSize = 0;

			for(Iterator<Building> iter = ((Region) this).buildings().iterator(); iter.hasNext();) {
				Building b = iter.next();

				if(b.getType() instanceof CastleType) {
					if(b.getSize() > bSize) {
						bSize = b.getSize();
						ownerUnit = b.getOwnerUnit();
					}
				}
			}
		}

		return owner!=null?owner:ownerUnit;
	}

	/**
	 * @see magellan.library.impl.MagellanRelatedImpl#addRelation(magellan.library.relation.UnitRelation)
	 */
	@Override
  public void addRelation(UnitRelation rel) {
	  Cache cache = getCache();
	  
		if(cache.relations == null) {
			cache.relations = new LinkedList<UnitRelation>();
		}

		cache.relations.add(rel);

		invalidateCache();

	}

	private void invalidateCache() {
	  if (hasCache()){
	    getCache().modifiedName = null;
	    getCache().modifiedContainerUnits = null;
	  }
	}
    
    /**
     * @see magellan.library.Named#getModifiedName()
     */
    @Override
    public String getModifiedName() {
        if(getCache().modifiedName == null) {
          getCache().modifiedName = super.getModifiedName();
        }
        return getCache().modifiedName != null ? getCache().modifiedName : getName(); 
    }

	/**
	 * @see magellan.library.impl.MagellanRelatedImpl#removeRelation(magellan.library.relation.UnitRelation)
	 */
	@Override
  public UnitRelation removeRelation(UnitRelation rel) {
		UnitRelation r = null;

		if(hasCache() && (getCache().relations != null)) {
			if(getCache().relations.remove(rel)) {
				r = rel;
                invalidateCache();
			}
		}

		return r;
	}

  /**
   * @see magellan.library.utils.Taggable#deleteAllTags()
   */
  public void deleteAllTags() {
    tagMap = null;
  }

  /**
   * @see magellan.library.utils.Taggable#putTag(java.lang.String, java.lang.String)
   */
  public String putTag(String tag, String value) {
    if(tagMap == null) {
      tagMap = new HashMap<String, String>();
    }

    return tagMap.put(tag, value);
  }

  /**
   * @see magellan.library.utils.Taggable#getTag(java.lang.String)
   */
  public String getTag(String tag) {
    if(tagMap == null) {
      return null;
    }

    return tagMap.get(tag);
  }

  /**
   * @see magellan.library.utils.Taggable#removeTag(java.lang.String)
   */
  public String removeTag(String tag) {
    if(tagMap == null) {
      return null;
    }

    return tagMap.remove(tag);
  }

  /**
   * @see magellan.library.utils.Taggable#containsTag(java.lang.String)
   */
  public boolean containsTag(String tag) {
    if(tagMap == null) {
      return false;
    }

    return tagMap.containsKey(tag);
  }

  /**
   * @see magellan.library.utils.Taggable#getTagMap()
   */
  public Map<String,String> getTagMap() {
    if(tagMap == null) {
      tagMap = new TagMap();
    }

    return Collections.unmodifiableMap(tagMap);
  }

  /**
   * @see magellan.library.utils.Taggable#hasTags()
   */
  public boolean hasTags() {
    return (tagMap != null) && !tagMap.isEmpty();
  }


  /**
   * Returns the value of comments.
   * 
   * @return Returns comments.
   */
  public List<String> getComments() {
    return comments;
  }

  /**
   * Sets the value of comments.
   *
   * @param comments The value for comments.
   */
  public void setComments(List<String> comments) {
    this.comments = comments;
  }

  /**
   * Returns the value of effects.
   * 
   * @return Returns effects.
   */
  public List<String> getEffects() {
    return effects;
  }

  /**
   * Sets the value of effects.
   *
   * @param effects The value for effects.
   */
  public void setEffects(List<String> effects) {
    this.effects = effects;
  }


  /**
   * @see magellan.library.HasCache#hasCache()
   */
  public boolean hasCache(){
//    return cacheReference!=null && cacheReference.get()!=null;
    return cache!=null;
  }
  
  /**
   * Returns the value of cache.
   * 
   * @return Returns cache.
   */
  public Cache getCache() {
//    Cache c;
//    if (cacheReference!=null && (c = cacheReference.get())!=null)
//      return c;
//    else{
//      c = new Cache();
//      cacheReference = new SoftReference<Cache>(c);
//      return c;
//    }
    if (cache==null)
      cache = new Cache();
    return cache;
  }

  /**
   * Sets the value of cache.
   *
   * @param cache The value for cache.
   */
  public void setCache(Cache cache) {
//    cacheReference = new SoftReference<Cache>(cache);
    this.cache = cache;
  }

  /**
   * @see magellan.library.Unit#clearCache()
   */
  public void clearCache(){
//    if (cacheReference==null)
//      return;
//    Cache c = cacheReference.get();
//    if (c!=null)
//      c.clear();
//    cacheReference.clear();
//    cacheReference = null;
    if (cache==null)
      return;
    cache.clear();
    cache=null;
  }
  

  /**
   * @see magellan.library.HasCache#addCacheHandler(magellan.library.utils.CacheHandler)
   */
  public void addCacheHandler(CacheHandler handler) {
    getCache().addHandler(handler);
  }

  /**
   * Always returns <code>null</code>. UnitContainers do not have order editors.
   * 
   * @return
   */
  public CacheableOrderEditor getOrderEditor(){
    Cache cache = getCache();
    if(hasCache() && cache.orderEditor != null) {
      return cache.orderEditor;
    } else {
      return null;
    }
  }

  public void setOrderEditor(CacheableOrderEditor editor){
    getCache().orderEditor=editor;
  }

}