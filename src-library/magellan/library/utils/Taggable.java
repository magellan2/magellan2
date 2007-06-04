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
 * Taggable.java
 *
 * Created on 6. Juni 2002, 18:44
 */
package magellan.library.utils;

import java.util.Map;

/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public interface Taggable {
	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean hasTags();

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public boolean containsTag(String tag);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public String putTag(String tag, String value);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public String getTag(String tag);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public String removeTag(String tag);

	/**
	 * DOCUMENT-ME
	 */
	public void deleteAllTags();

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Map getTagMap();
}
