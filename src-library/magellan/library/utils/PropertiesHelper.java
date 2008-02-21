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

import java.awt.Rectangle;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import magellan.library.utils.logging.Logger;



/**
 * This class logically accesses values in a given Properties object
 */
public class PropertiesHelper {
	private static final Logger log = Logger.getInstance(PropertiesHelper.class);

	/**
	 * Extracts properties by given prefix. If there exists a key called prefix.count this is used
	 * as order
	 */
	public static List<String> getList(Properties p, String prefix) {
		List<String> ret = new LinkedList<String>();
		String count = p.getProperty(prefix + ".count");

		if(count == null) {
            boolean hasMore = true;
            for(int i=0; hasMore; i++) {
                String prop = p.getProperty(prefix + "." + i);

                if(prop == null) {
                    prop = p.getProperty(prefix + i);
                }
                
                if(prop != null) {
                    ret.add(prop);
                } else {
                    hasMore = false;
                }
            }
		} else {
			for(int i = 0, max = new Integer(count).intValue(); i < max; i++) {
				String prop = p.getProperty(prefix + "." + i);

				if(prop != null) {
					ret.add(prop);
				}
			}
		}

		return ret;
	}

  /**
	 * a) remove old properties
	 * b) set prefix.count value
	 * c) set prefix.0 .. prefix.n values
   */
	public static void setList(Properties p, String prefix, Collection list) {
		// a) remove old properties
		for(Iterator iter = getPrefixedList(p, prefix).iterator(); iter.hasNext();) {
			p.remove(iter.next());
		}

		// b) set prefix.count value
		p.setProperty(prefix + ".count", Integer.toString(list.size()));

		// c) set prefix.0 .. prefix.n values
		int i = 0;

		for(Iterator iter = list.iterator(); iter.hasNext(); i++) {
			Object value = iter.next();
			p.setProperty(prefix + "." + i, value.toString());
		}
	}

	/**
	 * Delivers a list of all keys having the prefix <tt>prefix</tt>
	 */
	public static List<String> getPrefixedList(Properties p, String prefix) {
		List<String> ret = new LinkedList<String>();

		for(Enumeration e = p.propertyNames(); e.hasMoreElements();) {
			String key = (String)e.nextElement();

			if(key.startsWith(prefix)) {
				ret.add(p.getProperty(key));
			}
		}

		return ret;
	}

  /**
   * Searches the property with the given key and if it exists, it tries
   * to convert it into a boolean and returns that value. If something goes
   * wrong or the key couldn't be found the default value def is returned.
   */
	public static boolean getBoolean(Properties p, String key, boolean def) {
		String val = p.getProperty(key);

		if(val != null) {
			return Boolean.valueOf(val).booleanValue();
		}

		return def;
	}

	/**
	 * Searches the property with the given key and if it exists, it tries
   * to convert it into a float and returns that value. If something goes
   * wrong or the key couldn't be found the default value def is returned.
	 */
	public static float getFloat(Properties p, String key, float def) {
		String val = p.getProperty(key);

		if(val != null) {
			try {
				return Float.valueOf(val).floatValue();
			} catch(NumberFormatException nfe) {
			}
		}

		return def;
	}
	
	/**
	 * Loads a rectangle from the settings using the given key. If <code>r</code> is null, a new
	 * object is created. Else the result is stored in <code>r</code>. In either case, the
	 * resulting rectangle is returned.
	 * 
	 * @return The loaded rectangle or null if an error occurs.
	 */
	public static Rectangle loadRect(Properties settings, Rectangle r, String key) {
		if(r == null) {
			r = new Rectangle();
		}

		try {
			r.x = Integer.parseInt(settings.getProperty(key + ".x"));
			r.y = Integer.parseInt(settings.getProperty(key + ".y"));
			r.width = Integer.parseInt(settings.getProperty(key + ".width"));
			r.height = Integer.parseInt(settings.getProperty(key + ".height"));
		} catch(Exception exc) {
			log.warn("Bad rectangle: "+key);
			log.debug("", exc);
			return null;
		}
		return r;
	}

	/**
	 * Saves the rectangle r with property-key key to the settings. The rectangle is stored as
	 * key.x, key.y, key.width, key.height.
	 */
	public static void saveRectangle(Properties settings, Rectangle r, String key) {
		settings.setProperty(key + ".x", String.valueOf(r.x));
		settings.setProperty(key + ".y", String.valueOf(r.y));
		settings.setProperty(key + ".width", String.valueOf(r.width));
		settings.setProperty(key + ".height", String.valueOf(r.height));
	}


}
