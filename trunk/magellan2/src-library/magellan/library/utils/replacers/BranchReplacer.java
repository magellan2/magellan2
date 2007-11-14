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

/*
 * BranchReplacer.java
 *
 * Created on 19. Mai 2002, 12:02
 */
package magellan.library.utils.replacers;

/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version 1.0
 */
public interface BranchReplacer extends Replacer {
	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getBranchCount();

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public String getBranchSign(int index);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 */
	public void setBranch(int index, Object obj);
}
