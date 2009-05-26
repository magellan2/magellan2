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
 * A Replacer that has branches (e.g. like an if-statment)
 *
 * @author Andreas
 * @version 1.0
 */
public interface BranchReplacer extends Replacer {
	/**
	 * Returns the number of branches of this replacer
	 * 
	 */
	public int getBranchCount();

	/**
	 * Returns the marker for the <code>index</code>-th branch. 
	 * 
	 * magellan.library.utils.replacers.Replacer#NEXT_BRANCH or 
	 * magellan.library.utils.replacers.Replacer#END.
	 * 
	 */
	public String getBranchSign(int index);

	/**
	 * Set the <code>index</code>-th branch to <code>obj</code>.
	 * 
	 */
	public void setBranch(int index, Object obj);
}
