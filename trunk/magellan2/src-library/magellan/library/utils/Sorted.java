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

package magellan.library.utils;

/**
 * An interface for all objects that are sorted.
 */
public interface Sorted {
	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setSortIndex(int index);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getSortIndex();
}
