/*
 *  Copyright (C) 2000-2006 Roger Butenuth, Andreas Gampe,
 *                          Stefan Goetz, Sebastian Pappert,
 *                          Klaas Prause, Enno Rehling,
 *                          Sebastian Tusk, Ulrich Kuester,
 *                          Ilja Pavkovic, Steffen Mecke
 *
 * This file is part of the Eressea Java Code Base, see the
 * file LICENSING for the licensing information applying to
 * this file.
 *
 */
package magellan.library.relation;

import magellan.library.Unit;
import magellan.library.rules.ItemType;


/**
 * This relation holds information about a unit reserving some item.
 * 
 * @author stm
 *
 */
public class ReserveRelation extends UnitRelation {

	/**
	 * The type of item being reserved
	 */
	public ItemType itemType;
	/**
	 * The amount being reserved.
	 */
	public int amount;
	
	/**
	 * Constructs a ReserveRelation (with warning parameter).
	 * 
	 * @param s The reserving unit
	 * @param a The amount
	 * @param i The item (type)
	 * @param line The line number in the unit's orders
	 * @param w true iff a warning should be displayed
	 */
	public ReserveRelation(Unit s, int a, ItemType i, int line, boolean w) {
		super(s, line, w);
		this.itemType = i;
		this.amount = a;
	}

	/* (non-Javadoc)
	 * @see com.eressea.relation.UnitRelation#toString()
	 */
	public String toString() {
		return super.toString() + "@ITEMTYPE=" + itemType + "@AMOUNT="+amount;
	}

}
