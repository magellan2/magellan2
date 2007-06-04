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

import magellan.library.ID;

/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 203 $
 */
public class ShipType extends UnitContainerType {
	private int maxSize = -1;
	private int buildLevel = -1;
	private int range = -1;
	private int capacity = -1;
	private int captainLevel = -1;
	private int sailorLevel = -1;

	/**
	 * Creates a new ShipType object.
	 *
	 * 
	 */
	public ShipType(ID id) {
		super(id);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setMaxSize(int s) {
		maxSize = s;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getMaxSize() {
		return maxSize;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setBuildLevel(int l) {
		buildLevel = l;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getBuildLevel() {
		return buildLevel;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setRange(int r) {
		range = r;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getRange() {
		return range;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setCapacity(int c) {
		capacity = c;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getCapacity() {
		return capacity;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setCaptainSkillLevel(int l) {
		captainLevel = l;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getCaptainSkillLevel() {
		return captainLevel;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setSailorSkillLevel(int l) {
		sailorLevel = l;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getSailorSkillLevel() {
		return sailorLevel;
	}
}
