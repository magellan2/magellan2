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
 * ReplacerFactory.java
 *
 * Created on 20. Mai 2002, 14:09
 */
package magellan.library.utils.replacers;

import java.util.Set;

/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version 1.0
 */
public interface ReplacerFactory {
	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Set<String> getReplacers();

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public boolean isReplacer(String name);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public Replacer createReplacer(String name);
}
