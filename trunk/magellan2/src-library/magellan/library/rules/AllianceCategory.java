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

import magellan.library.ID;



/**
 * DOCUMENT-ME
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
	public AllianceCategory(ID id) {
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
	 *
	 * 
	 */
	@Override
  public int compareTo(Object o) {
		int anotherBitMask = ((AllianceCategory) o).bitMask;

		return (bitMask < anotherBitMask) ? (-1) : ((bitMask == anotherBitMask) ? 0 : 1);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public String toString() {
		return "AllianceCategory[name=" + getName() + ", bitMask=" + bitMask + "]";
	}
}
