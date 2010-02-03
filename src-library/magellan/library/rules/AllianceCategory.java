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


import java.util.Collection;
import java.util.HashSet;

import magellan.library.StringID;

/**
 * This class contains an alliance category. 
 *
 * @author $Author: $
 * @version $Revision: 271 $
 */
public class AllianceCategory extends ObjectType {
	private int bitMask = -1;

	// TODO: This *may* be a normal Category. Emulate the basic features
	private AllianceCategory parent = null;
	private Collection<AllianceCategory> children = null;
	
	public AllianceCategory getParent() {
		return parent;
	}

	public void setParent(AllianceCategory p) {
		if(parent != p) {
			if (parent != null) {
				parent.removeChild(this);
			}
			
			parent = p;
			
			if(p != null) {
				parent.addChild(this);
			}
		}
	}
	public boolean hasChildren() {
		return (children != null) && (children.size() > 0);
	}

	public Collection<AllianceCategory> getChildren() {
		if(children == null) {
			children = new HashSet<AllianceCategory>();
		}

		return children;
	}

	public void addChild(AllianceCategory ic) {
		getChildren().add(ic);
	}

	protected void removeChild(AllianceCategory ic) {
		if(hasChildren()) {
			getChildren().remove(ic);
		}
	}


	/**
	 * Creates a new AllianceCategory object.
	 *
	 * 
	 */
	public AllianceCategory(StringID id) {
		super(id);
	}

	/**
	 * copy constructor
	 *
	 * 
	 */
	public AllianceCategory(AllianceCategory orig) {
		super(orig.getID());
		bitMask = orig.bitMask;
	}

  /**
   * Sets the bitmask of this category. Each category should have a unique bit. Alliances use 
   * combinations of bits as their bitmask.
   */
	public void setBitMask(int mask) {
		this.bitMask = mask;
	}

	/**
	 * Returns the bitmask of this category. Each category should have a unique bit. Alliances use 
	 * combinations of bits as their bitmask.
	 */
	public int getBitMask() {
		return this.bitMask;
	}

	/**
	 * Compares this category to another one according to the bitmask values.
	 * 
	 * @see magellan.library.impl.MagellanIdentifiableImpl#compareTo(java.lang.Object)
	 */
	@Override
  public int compareTo(Object o) {
		int anotherBitMask = ((AllianceCategory) o).bitMask;

		return (bitMask < anotherBitMask) ? (-1) : ((bitMask == anotherBitMask) ? 0 : 1);
	}

	/**
	 * @see magellan.library.impl.MagellanNamedImpl#toString()
	 */
	@Override
  public String toString() {
		return "AllianceCategory[name=" + getName() + ", bitMask=" + bitMask + "]";
	}

  /**
   * Returns the id uniquely identifying this object.
   */
  @Override
  public StringID getID() {
    return (StringID) id;
  }

}
