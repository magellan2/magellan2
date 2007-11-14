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

package magellan.library.utils.replacers;

/**
 * Base interface for Replacer architecture.
 *
 * @author Andreas
 * @version 1.0
 */
public interface Replacer {
	/** DOCUMENT-ME */
	public static final String BLANK = "";

	/** DOCUMENT-ME */
	public static final String TRUE = "true";

	/** DOCUMENT-ME */
	public static final String FALSE = "false";

	/** DOCUMENT-ME */
	public static final String CLEAR = "clear";

	/** DOCUMENT-ME */
	public static final String NEXT_BRANCH = "else";

	/** DOCUMENT-ME */
	public static final String END = "end";

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public Object getReplacement(Object o);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getDescription();
}
