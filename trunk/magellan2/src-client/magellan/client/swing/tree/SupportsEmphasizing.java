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

package magellan.client.swing.tree;

import java.util.List;

/**
 * An interface to be implemented by wrapper classes. Helps to create useful implementations of the
 * emphasized method that is already defined in CellObject. The List that can be retrieved via
 * getSubordinatedElements is supposed to contain those wrappers that are directly under the node
 * that contains this wrapper. This List should be used to determine the return value of
 * emphasized().
 *
 * @author Ulrich K�ster
 */
public interface SupportsEmphasizing {
	/**
	 * Retrieve the subordinate elements of this wrapper. They are supposed to implement
	 * SupportsEmphasizing too. This way we can ask them and check whether all subordinated
	 * elements are not emphasized.
	 *
	 * 
	 */
	public List<SupportsEmphasizing> getSubordinatedElements();

	/**
	 * Tells whether this element is emphasized or not.
	 *
	 * 
	 */
	public boolean emphasized();
}
