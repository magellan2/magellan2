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
public class CastleType extends BuildingType implements Comparable {
	private int minSize;
	private int wage = -1;
	private int tax = -1;

	/**
	 * Creates a new CastleType object.
	 *
	 * 
	 */
	public CastleType(ID id) {
		super(id);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setPeasantWage(int w) {
		wage = w;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getPeasantWage() {
		return wage;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setTradeTax(int t) {
		tax = t;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getTradeTax() {
		return tax;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setMinSize(int s) {
		this.minSize = s;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getMinSize() {
		return minSize;
	}

	/**
	 * Imposes a natural ordering on CastleType objects according to their minimum size attribute.
	 * If obj is an instance of class BuildingType the return value reflects the natural ordering
	 * of the ids of this object and obj.
	 *
	 * 
	 *
	 * 
	 */
	@Override
  public int compareTo(Object obj) {
		if(obj instanceof CastleType) {
			CastleType c = (CastleType) obj;

			if(this.minSize < c.minSize) {
				return -1;
			}

			if(this.minSize > c.minSize) {
				return 1;
			}

			return 0;
		} else {
			return super.compareTo(obj);
		}
	}
}
