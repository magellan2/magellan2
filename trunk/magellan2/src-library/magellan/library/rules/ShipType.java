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
 * Members of this class contain information about a type of ship.
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
	 */
	public ShipType(ID id) {
		super(id);
	}

	/**
	 * Sets the maximum size. 
	 */
	public void setMaxSize(int s) {
		maxSize = s;
	}

  /**
   * Returns the maximum size of the ship (which would usually be the number of
   * wood needed to construct it).
   * 
   */
	public int getMaxSize() {
		return maxSize;
	}

	/**
	 * Sets the skill level needed to build one unit of this ship. 
	 * 
	 */
	public void setBuildLevel(int l) {
		buildLevel = l;
	}

	/**
   * Returns the skill level needed to build one unit of this ship. 
	 * 
	 */
	public int getBuildLevel() {
		return buildLevel;
	}

	/**
	 * Sets the regular range of this ship type.
	 * 
	 */
	public void setRange(int r) {
		range = r;
	}

	/**
   * Returns the regular range of this ship type (before any modifiers).
	 *
	 * 
	 */
	public int getRange() {
		return range;
	}

	/**
	 * Sets the maximum capacity (in GE).
	 * 
	 */
	public void setCapacity(int c) {
		capacity = c;
	}

	/**
   * Returns the maximum capacity (in GE).
	 * 
	 */
	public int getCapacity() {
		return capacity;
	}

	/**
	 * Set the skill level required to command the ship.
	 * 
	 */
	public void setCaptainSkillLevel(int l) {
		captainLevel = l;
	}

	/**
	 * Returns the skill level required to command the ship.
	 * 
	 */
	public int getCaptainSkillLevel() {
		return captainLevel;
	}

	/**
	 * Sets the number of skill levels to sail the ship.
	 * 
	 */
	public void setSailorSkillLevel(int l) {
		sailorLevel = l;
	}

	/**
   * Returns the number of skill levels to sail the ship.
	 * 
	 */
	public int getSailorSkillLevel() {
		return sailorLevel;
	}
}
