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
 * ParameterReplacer.java
 *
 * Created on 20. Mai 2002, 14:11
 */
package magellan.library.utils.replacers;

/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version 1.0
 */
public interface ParameterReplacer extends Replacer {
	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getParameterCount();

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 */
	public void setParameter(int index, Object obj);
}
