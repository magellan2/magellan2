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

import magellan.library.ID;
import magellan.library.StringID;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 203 $
 */
public class RegionType extends UnitContainerType {
	/** A static instance of the unknown region type */
	public static RegionType unknown = new RegionType(StringID.create("unbekannt"));
	private int inhabitants = -1;

  private boolean isOcean = false;

  private boolean isAstralVisible = false;
  
  /**
	 * Creates a new RegionType object.
	 *
	 * 
	 */
	public RegionType(ID id) {
		super(id);
	}

	/**
	 * DOCUMENT-ME
	 *
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
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getInhabitants() {
		return inhabitants;
	}

	/**
	 * DOCUMENT ME!
	 */
	public int getRoadStones() {
		for(Iterator iter = resources.iterator(); iter.hasNext();) {
			Resource r = (Resource) iter.next();

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

	private List<Resource> resources = new LinkedList<Resource>();

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void addRoadResource(Resource r) {
		resources.add(r);
	}

	/**
	 * Gets a List of needed Resources for road building
	 *
	 * 
	 */
	public List getRoadResources() {
		return Collections.unmodifiableList(resources);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean isOcean() {
		return isOcean;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setIsOcean(boolean isOcean) {
		this.isOcean = isOcean;
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
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String toString() {
		String s = getName();

		if(s == null) {
			s = id.toString();
		}

		return s;
	}
}
