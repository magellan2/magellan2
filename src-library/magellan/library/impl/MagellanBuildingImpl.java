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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import magellan.library.Building;
import magellan.library.EntityID;
import magellan.library.GameData;
import magellan.library.HasRegion;
import magellan.library.Region;
import magellan.library.UnitID;
import magellan.library.rules.BuildingType;

/**
 * A <code>Building</code> found in Atlantis reports.
 * 
 * @author $Author: $
 * @version $Revision: 299 $
 */
public class MagellanBuildingImpl extends MagellanUnitContainerImpl implements Building, HasRegion {
  /** Size of the building. */
  private int size = 0;

  /** Costs for the building. Could depend on size, so don't put it into the UnitContType. */
  private int cost = 0;

  private String trueBuildingType = null;

  /**
   * Creates the Object for a building.
   */
  public MagellanBuildingImpl(EntityID id, GameData data) {
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

  private int besiegers;

  private Set<UnitID> besiegerUnits;

  /**
   * Sets the region this building is in. If this building already has a region set, this method
   * takes care of removing it from that region.
   * 
   * @param region the region to the the building into.
   */
  public void setRegion(Region region) {
    // remove the building from a prior location
    if (this.region != null) {
      this.region.removeBuilding(this);
    }

    // set the new region and add the building
    this.region = region;

    if (this.region != null) {
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
   * Sets the trueBuildingType which is not really a type but just a String only occurrence now
   * "Traumschlößchen",wahrerTyp Fiete 20060910
   * 
   * @param trueBuildingType as string
   */

  public void setTrueBuildingType(String trueBuildingType) {
    this.trueBuildingType = trueBuildingType;
  }

  /**
   * Gets the trueBuildingType which es not realy a type but just a String only occurrence now
   * "Traumschlößchen",wahrerTyp Fiete 20060910
   * 
   * @return String = trueBuildingType
   */
  public String getTrueBuildingType() {
    return trueBuildingType;
  }

  /**
   * Returns a String representation of the Building object.
   * 
   * @return the Building object as string.
   */
  @Override
  public String toString() {
    // we could use getModifiedName here but it seems a bit obtrusive (and hard to handle tree updates)
    String text = "";
    if (trueBuildingType != null) {
      text += trueBuildingType + " ";
    }
    text += getName() + " (" + id + "), " + getType() + ": ";

    int uc = personCount();
    int muc = modifiedPersonCount();
    text += uc;
    if (uc != muc) {
      text += " (" + modifiedPersonCount() + ")";
    }
    text += " / " + getSize();
    return text;
  }

  /**
   * Returns the id uniquely identifying this object.
   */
  @Override
  public EntityID getID() {
    return (EntityID) super.getID();
  }

  public int getBesiegers() {
    return besiegers;
  }

  public void setBesiegers(int number) {
    besiegers = number;
  }

  public void addBesiegerUnit(UnitID besieger) {
    if (besiegerUnits == null) {
      besiegerUnits = new HashSet<UnitID>();
    }
    besiegerUnits.add(besieger);
  }

  public void setBesiegerUnits(Collection<UnitID> besiegers) {
    if (besiegers == null) {
      besiegerUnits = null;
    } else {
      besiegerUnits.clear();
      besiegerUnits.addAll(besiegers);
    }
  }

  public Collection<UnitID> getBesiegerUnits() {
    if (besiegerUnits == null)
      return null;
    return Collections.unmodifiableCollection(besiegerUnits);
  }
}
