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
import java.util.Map;

import magellan.library.Alliance;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Group;
import magellan.library.ID;
import magellan.library.Unit;
import magellan.library.utils.ExternalTagMap;
import magellan.library.utils.OrderedHashtable;


/**
 * A class representing a group of units within a faction.
 */
public class MagellanGroupImpl extends MagellanNamedImpl implements Group {
	private Faction faction = null;
	private Map<ID,Alliance> allies = new OrderedHashtable<ID,Alliance>();
	// TODO: this does not seem to be needed.
	// private GameData data = null;
	private static ExternalTagMap externalMap = null; // Map for external tags

	/**
	 * Create a new <tt>Group</tt> object.
	 *
	 * @param id the id of this group.
	 * @param data the game data this group belongs to.
	 */
	public MagellanGroupImpl(ID id, GameData data) {
		this(id, data, null, null);
	}

	/**
	 * Create a new <tt>Group</tt> object.
	 *
	 * @param id the id of this group.
	 * @param data the game data this group belongs to.
	 * @param name the name of this group.
	 */
	public MagellanGroupImpl(ID id, GameData data, String name) {
		this(id, data, name, null);
	}

	/**
	 * Create a new <tt>Group</tt> object.
	 *
	 * @param id the id of this group.
	 * @param data the game data this group belongs to.
	 * @param name the name of this group.
	 * @param faction the faction this group belongs to.
	 */
	public MagellanGroupImpl(ID id, GameData data, String name, Faction faction) {
		super(id);
//		this.data = data;
		this.setName(name);
		this.faction = faction;
	}

	/**
	 * Set the faction this group belongs to.
	 *
	 * 
	 */
	public void setFaction(Faction f) {
		this.faction = f;
	}

	/**
	 * Get the faction this group belongs to.
	 *
	 * 
	 */
	public Faction getFaction() {
		return faction;
	}

	/**
	 * The alliances specific to this group. The map returned by this function contains <tt>ID</tt>
	 * objects as keys with the id of the faction that alliance references. The values are
	 * instances of class <tt>Alliance</tt>. The return value is never null.
	 *
	 * 
	 */
	public Map<ID,Alliance> allies() {
		return allies;
	}

	/** A group dependent prefix to be prepended to this faction's race name. */
	private String raceNamePrefix = null;

	/**
	 * Sets the group dependent prefix for the race name.
	 *
	 * 
	 */
	public void setRaceNamePrefix(String prefix) {
		this.raceNamePrefix = prefix;
	}

	/**
	 * Returns the group dependent prefix for the race name.
	 *
	 * 
	 */
	public String getRaceNamePrefix() {
		return this.raceNamePrefix;
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
	 *
	 * 
	 */
	public Collection<Unit> units() {
		if(units == null) {
			return new ArrayList<Unit>();
		}

		if(unitCollection == null) {
      if (units != null && units.values() != null) unitCollection = Collections.unmodifiableCollection(units.values());
		}

		return unitCollection;
	}

	/**
	 * Retrieve a unit in this container by id.
	 *
	 * 
	 *
	 * 
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
			units = new Hashtable<ID, Unit>();

			/* enforce the creation of a new collection view */
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
	 * Returns a String representation of this group object.
	 *
	 * 
	 */
	public String toString() {
		// pavkovic 2004.01.04: for a Group id is more a technical connection so we dont
		// want to see it.
		//return name + " (" + id + ")";
		return getName();
	}

	// EXTERNAL TAG METHODS
	/**
	 * TODO DOCUMENT ME!
	 * 
	 * @param tag
	 * @param value
	 * 
	 */
	public String putTag(String tag, String value) {
		if(externalMap == null) {
			externalMap = new ExternalTagMap();
		}

		return externalMap.putTag(this.getID(), tag, value);
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

		return externalMap.getTag(this.getID(), tag);
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

		return externalMap.removeTag(this.getID(), tag);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public boolean containsTag(String tag) {
		if(externalMap == null) {
			return false;
		}

		return externalMap.containsTag(this.getID(), tag);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Map<String,String> getTagMap() {
		if(externalMap == null) {
			externalMap = new ExternalTagMap();
		}

		return externalMap.getTagMap(this.getID(), true);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean hasTags() {
		if(externalMap == null) {
			return false;
		}

		return externalMap.getTagMap(this.getID(), false) != null;
	}

  /**
   * @see magellan.library.Group#setAllies(java.util.Map)
   */
  public void setAllies(Map<ID, Alliance> allies) {
    this.allies = allies;
  }
}
