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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import magellan.library.ID;
import magellan.library.utils.logging.Logger;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 171 $
 * 
 * @deprecated (stm) Nobody seems to really need this class. A simple Map<String, String> would do.
 */
@Deprecated
public class ExternalTagMap extends HashMap<ID,TagMap> {
	private static final Logger log = Logger.getInstance(ExternalTagMap.class);
	private static final String METHOD_NAME = "getID";
	
	protected ID getID(Object o) {
		if(o instanceof ID) {
			return (ID) o;
		}

		Class<? extends Object> c = o.getClass();

		try {
			Method m = c.getMethod(ExternalTagMap.METHOD_NAME, (Class<?>)null);

			if(m != null) {
				try {
					Object o2 = m.invoke(o, (Object[])null);

					if(o2 instanceof ID) {
						return (ID) o2;
					}
				} catch(Exception inner) {
				}
			}
		} catch(NoSuchMethodException nsme) {
			ExternalTagMap.log.error("Error trying to get ID: " + o);
		}

		return null;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 * 
	 *
	 * 
	 */
	public String putTag(Object o, String tag, String value) {
		ID id = getID(o);

		if(id == null) {
			return null;
		}

		if(!containsKey(id)) {
			put(id, new TagMap());
		}

		TagMap m = get(id);

		return m.put(tag, value);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public String getTag(Object o, String tag) {
		ID id = getID(o);

		if(id == null) {
			return null;
		}

		TagMap m = get(id);

		if(m == null) {
			return null;
		}

		return m.get(tag);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public boolean containsTag(Object o, String tag) {
		ID id = getID(o);

		if(id == null) {
			return false;
		}

		TagMap m = get(id);

		if(m == null) {
			return false;
		}

		return m.containsKey(tag);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public String removeTag(Object o, String tag) {
		ID id = getID(o);

		if(id == null) {
			return null;
		}

		TagMap m = get(id);

		if(m == null) {
			return null;
		}

		return m.remove(tag);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public Map<String,String> getTagMap(Object o, boolean create) {
		ID id = getID(o);

		if(id == null) {
			return null;
		}

		TagMap m = get(id);

		if((m == null) && create) {
			m = new TagMap();
			put(id, m);
		}

		return m;
	}
}
