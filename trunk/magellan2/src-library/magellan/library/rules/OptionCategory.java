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

import magellan.library.StringID;

/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 203 $
 */
public class OptionCategory extends ObjectType {
	private int bitMask = 0;
	private boolean isActive = false;
	private boolean isOrder = false;

	/**
	 * Creates a new OptionCategory object.
	 *
	 * 
	 */
	public OptionCategory(StringID id) {
		super(id);
	}

	/**
	 * copy constructor
	 *
	 * 
	 */
	public OptionCategory(OptionCategory orig) {
		super(orig.getID());
		bitMask = orig.bitMask;
		isActive = orig.isActive;
		isOrder = orig.isOrder;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setBitMask(int mask) {
		this.bitMask = mask;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getBitMask() {
		return this.bitMask;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setActive(boolean bool) {
		this.isActive = bool;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean isActive() {
		return this.isActive;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setOrder(boolean bool) {
		this.isOrder = bool;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean isOrder() {
		return this.isOrder;
	}

  /**
   * Returns the id uniquely identifying this object.
   */
  @Override
  public StringID getID() {
    return (StringID) id;
  }
}
