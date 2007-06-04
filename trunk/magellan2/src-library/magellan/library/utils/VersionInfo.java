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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import magellan.library.utils.logging.Logger;


/**
 * A class for retrieving versioning information about Magellan.
 */
public class VersionInfo {
	private static final Logger log = Logger.getInstance(VersionInfo.class);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static String getVersion() {
		try {
			ResourceBundle bundle = new PropertyResourceBundle(new FileInputStream("etc/VERSION"));

			return bundle.getString("VERSION");
		} catch(IOException e) {
		} catch(MissingResourceException e) {
		}

		return null;
	}
}
