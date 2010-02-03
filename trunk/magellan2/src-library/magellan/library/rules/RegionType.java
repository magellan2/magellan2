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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import magellan.library.StringID;


/**
 * Stores rule relevant information about a region type.
 *
 */
public class RegionType extends UnitContainerType {
	/** A static instance of the unknown region type */
	public static RegionType unknown= new RegionType(StringID.create("unbekannt"));
	private int inhabitants = -1;

  private boolean isOcean = false;
  private boolean isLand = false;

  private boolean isAstralVisible = false;

  private int peasantWage = 11;

  private List<Resource> resources = new LinkedList<Resource>();

  /**
	 * Creates a new RegionType object.
	 *
	 * 
	 */
	public RegionType(StringID id) {
		super(id);
	}

	/**
	 * Sets the maximum number of peasant workers.
	 * 
	 */
	public void setInhabitants(int i) {
		inhabitants = i;
	}

	/**
	 * helper method for xml reader
	 *
	 * 
	 */
	public void setInhabitants(String i) {
		setInhabitants(Integer.parseInt(i));
	}

	/**
	 * Returns the number of max inhabitants in this region type
	 */
	public int getInhabitants() {
		return inhabitants;
	}

	/**
	 * DOCUMENT ME!
	 */
	public int getRoadStones() {
		for(Iterator<Resource> iter = resources.iterator(); iter.hasNext();) {
			Resource r = iter.next();

			if(r.getObjectType() instanceof ItemType) {
				return r.getAmount();
			}
		}

		return -1;
	}

	/**
	 * DOCUMENT ME!
	 */
	public BuildingType getRoadSupportBuilding() {
		for(Iterator<Resource> iter = resources.iterator(); iter.hasNext();) {
			Resource r = iter.next();

			if(r.getObjectType() instanceof BuildingType) {
				return (BuildingType) r.getObjectType();
			}
		}

		return null;
	}

	/**
	 * Adds a resource need for road building.
	 *
	 * 
	 */
	public void addRoadResource(Resource r) {
		resources.add(r);
	}

	/**
	 * Gets a List of needed Resources for road building.
	 *
	 * 
	 */
	public List<Resource> getRoadResources() {
		return Collections.unmodifiableList(resources);
	}

	/**
	 * Returns true if the unit is an ocean region.
	 */
	public boolean isOcean() {
		return isOcean;
	}

	/**
	 * Sets the ocean property. 
	 */
	public void setIsOcean(boolean isOcean) {
		this.isOcean = isOcean;
	}

  /**
   * Returns true if the unit is a land region (note that this is not just !isOcean()).
   */
  public boolean isLand() {
    return isLand;
  }

  /**
   * Sets the land property.
   */
  public void setLand(boolean land) {
    isLand = land;
  }

  /** 
   * Returns <code>true</code> if this RegionType is visible from the astral space.
   * 
   * @return <code>true</code> if this RegionType is visible from the astral space
   */
  public boolean isAstralVisible() {
    return isAstralVisible;
  }

  /**
   * If argument is true, this RegionType is marked as being visible from the astral space.
   * 
   * @param isAstralVisible
   */
  public void setAstralVisible(boolean isAstralVisible) {
    this.isAstralVisible = isAstralVisible;
  }

  /**
   * Returns the peasantWage value for this region type. Defaults to 11.
   * 
   * @return Returns peasantWage.
   */
  public int getPeasantWage() {
    return peasantWage;
  }

  /**
   * Sets the peasantWage value for this region type.
   *
   * @param peasantWage The value for peasantWage.
   */
  public void setPeasantWage(int peasantWage) {
    this.peasantWage = peasantWage;
  }

	/**
	 * @see magellan.library.impl.MagellanNamedImpl#toString()
	 */
	@Override
  public String toString() {
		String s = getName();

		if(s == null) {
			s = id.toString();
		}

		return s;
	}

  /**
   * Returns the id uniquely identifying this object.
   */
  @Override
  public StringID getID() {
    return (StringID) id;
  }

}
