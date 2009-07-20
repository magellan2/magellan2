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

package magellan.library;



/**
 * A ZeroUnit mimics behaviour of a unit called "0".
 *
 * @author $Author: $
 * @version $Revision: 171 $
 */
public interface ZeroUnit extends Unit {
  // id should be a UnitID, though, but it is in fact an IntegerID
  //	/**
//	 * This is the unit associated with the id 0. Used for UnitRelations for commands like "give
//	 * 0..."
//	 */
//	public static final ID ZERO_ID = UnitID.create(0);

	/**
	 * Sets the region of this unit
	 *
	 * @param r the region of this unit
	 */
	public void setRegion(Region r);

	/**
	 * Returns the amount of recruitable persons
	 *
	 * @return amount of recruitable persons
	 */
	public int getPersons();

	/**
	 * Returns the amount of recruitable persons - recruited persons
	 *
	 * @return amount of recruitable persons - recruited persons
	 */
	public int getModifiedPersons();

  public int getGivenPersons();
  
	/**
	 * Returns a string representation of this temporary unit.
	 *
	 * @return a string representation of this temporary unit
	 */
	public String toString();
}
