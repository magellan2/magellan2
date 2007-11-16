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

import java.util.Locale;

import magellan.library.utils.logging.Logger;


/**
 * Helper class for centrally managing different locales. This class provides the locales
 * statically. Optionally, you can specify a Properties object from which this class determines
 * which Locale to use. If the Locales are changed this is also recorded in the Properties object.
 */
public class Locales {
    private final static Logger log = Logger.getInstance(Locales.class);
    
	private static Locale guiLocale = null;
	private static Locale orderLocale = null;

	/**
	 * Sets the locale for the user interface. If Locales was initialized with a Properties object
	 * earlier, the new Locale is stored in it.
	 *
	 * 
	 */
	public static void setGUILocale(Locale l) {
		if (l==null)
			guiLocale=Locale.getDefault();
		else {
			Locale.setDefault(l);
			guiLocale = l;
		}
	}

	/**
	 * Returns the locale applicable for the user interface.
	 *
	 * 
	 *
	 */
	public static Locale getGUILocale() throws IllegalStateException {
		if(guiLocale == null) {
			log.warn("Locales.getGUILocale: Locales is not initialized, falling back to default locale");
			return Locale.getDefault();
		}

		return guiLocale;
	}

	/**
	 * Sets the locale for the unit orders. If Locales was initialized with a Properties object
	 * earlier, the new Locale is stored in it.
	 *
	 * 
	 */
	public static void setOrderLocale(Locale l) {
		if (l == null){
			orderLocale=Locale.GERMAN;
		}else
			orderLocale = l;
	}

	/**
	 * Returns the locale applicable for the unit orders.
	 *
	 * 
	 *
	 * @throws IllegalStateException when the method is invoked and neither the init() nor the
	 * 		   setGUILocale() methodes were invoked earlier with valid arguments.
	 */
	public static Locale getOrderLocale() throws IllegalStateException {
		if(orderLocale == null) {
			log.warn("Locales.getOrderLocale: Locales is not initialized, falling back to GERMAN locale");
			setOrderLocale(Locale.GERMAN);
			return Locale.GERMAN;
		}

		return orderLocale;
	}
}
