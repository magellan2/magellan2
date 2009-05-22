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

import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import magellan.library.rules.MessageType;
import magellan.library.utils.OrderedHashtable;
import magellan.library.utils.Translations;

/**
 * An implementation of the <tt>GameData</tt> supporting all of the attributes defined there. No
 * maps are defined as <tt>null</tt>.
 *
 * @see magellan.library.GameData
 */
public class CompleteData extends GameData {
	protected Map<CoordinateID,Region> regions = new OrderedHashtable<CoordinateID, Region>();
	protected Map<ID,Unit> units = new Hashtable<ID, Unit>();
	protected Map<ID,TempUnit> tempUnits = new Hashtable<ID, TempUnit>();
	protected Map<ID,Faction> factions = new OrderedHashtable<ID, Faction>();
	protected Map<ID,Ship> ships = new OrderedHashtable<ID, Ship>();
	protected Map<ID,Building> buildings = new OrderedHashtable<ID, Building>();
	protected Map<ID,Island> islands = new OrderedHashtable<ID, Island>();
	protected Map<ID,MessageType> msgTypes = new OrderedHashtable<ID, MessageType>();
	protected Map<ID,Spell> spells = new OrderedHashtable<ID, Spell>();
	protected Map<ID,Potion> potions = new OrderedHashtable<ID, Potion>();
	protected Map<ID,HotSpot> hotSpots = new OrderedHashtable<ID, HotSpot>();
	protected Translations translations = new Translations();
	protected Locale locale = null;
	protected Map<CoordinateID,Region> selectedRegions = new TreeMap<CoordinateID, Region>();
/*
  protected CoordinateID astralMapping = null;
  private boolean astralMappingImpossible = false;
*/
	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public Map<ID,Island> islands() {
		return islands;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public Map<CoordinateID,Region> regions() {
		return regions;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public Map<ID,Unit> units() {
		return units;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public Map<ID,TempUnit> tempUnits() {
		return tempUnits;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public Map<ID,Faction> factions() {
		return factions;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public Map<ID,Ship> ships() {
		return ships;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public Map<ID,Building> buildings() {
		return buildings;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public Map<ID,MessageType> msgTypes() {
		return msgTypes;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public Map<ID, Spell> spells() {
		return spells;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public Map<ID,Potion> potions() {
		return potions;
	}


	/**
	 * Returns a collection of the coordinates of selected regions.
	 *
	 */
	@Override
  public Map<CoordinateID,Region> getSelectedRegionCoordinates() {
		return selectedRegions;
	}

	/**
	 * set a collection of selected regions.
	 *
	 * @param regions the map of coordinates of selected regions
	 */
	@Override
  public void setSelectedRegionCoordinates(Map<CoordinateID, Region> regions) {
		selectedRegions = new TreeMap<CoordinateID, Region>();
		if(regions != null) {
			selectedRegions.putAll(regions);
		}
	}


	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public Map<ID,HotSpot> hotSpots() {
		return hotSpots;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public Translations translations() {
		return translations;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public void setLocale(Locale l) {
		this.locale = l;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public Locale getLocale() {
		return locale;
	}

	/**
	 * Creates a new CompleteData object.
	 *
	 * 
	 */
	public CompleteData(Rules rules) {
		super(rules);
	}

	/**
	 * Creates a new CompleteData object.
	 *
	 * 
	 * 
	 */
	public CompleteData(Rules rules, String name) {
		super(rules, name);
	}
	
  /**
   * Sets the mapping for astral to real space.
   * 
   * @param c the real space <code>CoordianteID</code> <x,y,0> which is the center of the 
   * astral space region with CoordinateID <0,0,1>.
   */
/*  
  public void setAstralMapping(CoordinateID c) {
    this.astralMapping = c;
  }
*/  
  /**
   * Returns the mapping for astral to real space.
   * 
   * @return the <code>CoordinateID</code> of the real space region which is the center
   * of the astral space region with CoordinateID <0,0,1>.
   */
/*
  public CoordinateID getAstralMapping() {
    if (this.getGameSpecificStuff() instanceof AllanonSpecificStuff) {
      // Allanon doesn't provide an astral space
      return null;
    }
    if (!(this.getGameSpecificStuff() instanceof EresseaSpecificStuff)) { 
      return null;
    }
    if (this.astralMappingImpossible) { 
      return null;
    }
    if (this.astralMapping == null) {
      EresseaMapMergeEvaluator mme = (EresseaMapMergeEvaluator) this.getGameSpecificStuff().getMapMergeEvaluator();
      this.astralMapping = mme.getAstral2RealMapping(this);
      this.astralMappingImpossible = this.astralMapping == null;
    }
    return this.astralMapping;
  }
*/  
  /**
   * @see magellan.library.GameData#estimateSize()
   */
  @Override
  public long estimateSize() {
    return  regions().size()*1000+units.size()*1000;
  }

}