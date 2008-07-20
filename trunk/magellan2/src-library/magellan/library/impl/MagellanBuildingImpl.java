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

import magellan.library.Building;
import magellan.library.GameData;
import magellan.library.HasRegion;
import magellan.library.ID;
import magellan.library.Region;
import magellan.library.rules.BuildingType;

/**
 * A <code>Building</code> found in Atlantis reports.
 *
 * @author $Author: $
 * @version $Revision: 299 $
 */
public class MagellanBuildingImpl extends MagellanUnitContainerImpl implements Building,HasRegion {
	/** Size of the building. */
	private int size = 0;

	/** Costs for the building. Could depend on size, so don't put it into the UnitContType. */
	private int cost = 0;
	
	private String trueBuildingType = null; 

	/**
	 * Creates the Object for a building.
	 */
	public MagellanBuildingImpl(ID id, GameData data) {
		super(id, data);
	}

	/**
	 * Returns the size of the building
	 *
	 * @return the size of the building.
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Sets the size of the building
	 *
	 * @param iSize set the size of the building.
	 */
	public void setSize(int iSize) {
		size = iSize;
	}

	/**
	 * Returns the costs for this building.
	 *
	 * @return the cost of the building.
	 */
	public int getCost() {
		return cost;
	}

	/**
	 * Sets the costs for this building.
	 *
	 * @param iCost set the cost for the building.
	 */
	public void setCost(int iCost) {
		cost = iCost;
	}

	/** The region this building is in. */
	private Region region = null;

	/**
	 * Sets the region this building is in. If this building already has a region set, this method
	 * takes care of removing it from that region.
	 *
	 * @param region the region to the the building into.
	 */
	public void setRegion(Region region) {
		// remove the building from a prior location
		if(this.region != null) {
			this.region.removeBuilding(this);
		}

		// set the new region and add the building
		this.region = region;

		if(this.region != null) {
			this.region.addBuilding(this);
		}
	}

	/**
	 * Returns the <code>BuildingType</code> of this building.
	 *
	 * @return the <code>BuildingType</code> of this building
	 */
	public BuildingType getBuildingType() {
		return (BuildingType) getType();
	}

	/**
	 * Get the region where this building is located.
	 *
	 * @return the region the building is in.
	 */
	public Region getRegion() {
		return region;
	}
	
	/**
	 * Sets the trueBuildingType which es not realy a type but
	 * just a String
	 * only occurance now "Traumschlößchen",wahrerTyp
	 * Fiete 20060910
	 * 
	 * @param trueBuildingType  as string
	 */
	
	public void setTrueBuildingType(String trueBuildingType){
		this.trueBuildingType = trueBuildingType;
	}
	/**
	 * Gets the trueBuildingType which es not realy a type but
	 * just a String
	 * only occurance now "Traumschlößchen",wahrerTyp
	 * Fiete 20060910
	 * 
	 * 
	 * @return String = trueBuildingType
	 */
	public String getTrueBuildingType() {
		return this.trueBuildingType;
	}
	
	/**
	 * Returns a String representation of the Building object.
	 *
	 * @return the Building object as string.
	 */
	@Override
  public String toString() {
		// Fiete 20060910
		// added support for wahrer Typ
		if (this.trueBuildingType==null){
			return getName() + " (" + id + "), " + this.getType() + " (" + this.getSize() + ")";
		} else {
			return this.trueBuildingType + ": " + getName() + " (" + id + "), " + this.getType() + " (" + this.getSize() + ")";
		}
	}
}
