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
import magellan.library.utils.OrderedHashtable;
import magellan.library.utils.Sorted;
import magellan.library.utils.TagMap;
import magellan.library.utils.Taggable;
import magellan.library.utils.logging.Logger;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 389 $
 */
public abstract class MagellanUnitContainerImpl extends MagellanRelatedImpl implements UnitContainer, Sorted, Taggable {
	private static final Logger log = Logger.getInstance(MagellanUnitContainerImpl.class);
	private UnitContainerType type = null;
	private Unit owner = null;

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

	/**
	 * The cache object containing cached information that may be not related enough to be
	 * encapsulated as a function and is time consuming to gather.
	 */
	protected Cache cache = null;

	/**
	 * The items carried by this unitcontainer. The keys are the IDs of the item's type, the values are the
	 * Item objects themselves.
	 */
	protected Map<ID,Item> items = null;

	/**
	 * A map storing all unknown tags for all UnitContainer objects. Keys are IDs of these objects,
	 * values are Maps(should be TagMaps).
	 */
	private TagMap externalMap = null;

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

  public Unit getOwner() {
    return owner;
  }
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
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * @throws IllegalArgumentException DOCUMENT-ME
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
			return (Unit) units.get(key);
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
			Unit u = (Unit) units.remove(key);

			if(units.isEmpty()) {
				units = null;
			}

			return u;
		} else {
			return null;
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Collection<Unit> modifiedUnits() {
		if((cache == null) || (cache.modifiedContainerUnits == null)) {
			refreshModifiedUnits();
		}

		if((cache != null) && (cache.modifiedContainerUnits != null)) {
      if (cache.modifiedContainerUnits.values() != null) {
        return Collections.unmodifiableCollection(cache.modifiedContainerUnits.values());
      } else {
        return Collections.emptyList();
      }
		} else {
			return Collections.emptyList();
		}
	}

    protected Collection<UnitRelation> getRelations() {
        if(cache == null) {
            cache = new Cache();
        }
        if(cache.relations == null) {
            cache.relations = new ArrayList<UnitRelation>();
        }
        return cache.relations;
    }
	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public Unit getModifiedUnit(ID key) {
		if((cache == null) || (cache.modifiedContainerUnits == null)) {
			refreshModifiedUnits();
		}

		if(cache.modifiedContainerUnits == null) {
			return null;
		}

		return (Unit) cache.modifiedContainerUnits.get(key);
	}

	private void refreshModifiedUnits() {
		if(cache == null) {
			cache = new Cache();
		}

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
					log.info("UnitContainer.refreshModifiedUnits(): unit container " + this +
							 " has a relation associated that does not point to it!");
				}
			} else {
				log.info("UnitContainer.refreshModifiedUnits(): unit container " + this +
						 " contains a relation that is not a UnitContainerRelation object!");
			}
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String toString() {
		return getName() + " (" + id + "), " + type;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setOwnerUnit(Unit unit) {
		this.owner = unit;
	}

	/**
	 * Returns the unit owning this UnitContainer. If this UnitContainer is an instance of class
	 * Ship or Building the normal owning unit is returned (or null, if there is none). In case of
	 * a Region, the OwnerUnit of the largest castle is returned. In case of a Faction, null is
	 * returned.
	 *
	 * 
	 */
	public Unit getOwnerUnit() {
		if((owner == null) && this instanceof Region) {
			Unit foundOwner = null;
			int bSize = 0;

			for(Iterator<Building> iter = ((Region) this).buildings().iterator(); iter.hasNext();) {
				Building b = iter.next();

				if(b.getType() instanceof CastleType) {
					if(b.getSize() > bSize) {
						bSize = b.getSize();
						foundOwner = b.getOwnerUnit();
					}
				}
			}

			if(foundOwner != null) {
				owner = foundOwner;
			}
		}

		return owner;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 */
	public void addRelation(UnitRelation rel) {
		if(cache == null) {
			cache = new Cache();
		}

		if(cache.relations == null) {
			cache.relations = new LinkedList<UnitRelation>();
		}

		cache.relations.add(rel);

        invalidateCache();

	}

    private void invalidateCache() {
        if(cache != null) {
            cache.modifiedName = null;
            cache.modifiedContainerUnits = null;
        }
    }
    
    /**
     * @see magellan.library.Named#getModifiedName()
     */
    public String getModifiedName() {
        if(cache == null) {
            cache = new Cache();
        } 
        if(cache.modifiedName == null) {
            cache.modifiedName = super.getModifiedName();
        }
        return cache.modifiedName != null ? cache.modifiedName : getName(); 
    }

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public UnitRelation removeRelation(UnitRelation rel) {
		UnitRelation r = null;

		if((cache != null) && (cache.relations != null)) {
			if(cache.relations.remove(rel)) {
				r = rel;
                invalidateCache();
			}
		}

		return r;
	}

	/** EXTERNAL TAG METHODS
	 * 
	 * DOCUMENT-ME
	 * 
	 * @see magellan.library.utils.Taggable#deleteAllTags()
	 */
	public void deleteAllTags() {
		externalMap = null;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public String putTag(String tag, String value) {
		if(externalMap == null) {
			externalMap = new TagMap();
		}

		return (String) externalMap.put(tag, value);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public String getTag(String tag) {
		if(externalMap == null) {
			return null;
		}

		return (String) externalMap.get(tag);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public String removeTag(String tag) {
		if(externalMap == null) {
			return null;
		}

		return (String) externalMap.remove(tag);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public boolean containsTag(String tag) {
		return (externalMap != null) && externalMap.containsKey(tag);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Map<String,String> getTagMap() {
		if(externalMap == null) {
			externalMap = new TagMap();
		}

		return externalMap;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean hasTags() {
		return (externalMap != null) && !externalMap.isEmpty();
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
   * Returns the value of cache.
   * 
   * @return Returns cache.
   */
  public Cache getCache() {
    return cache;
  }

  /**
   * Sets the value of cache.
   *
   * @param cache The value for cache.
   */
  public void setCache(Cache cache) {
    this.cache = cache;
  }
}
