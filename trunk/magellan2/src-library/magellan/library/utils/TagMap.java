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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class TagMap implements Map<String,String> {
	protected class Tag {
		/** DOCUMENT-ME */
		public String key;

		/** DOCUMENT-ME */
		public String value;

		/**
		 * Creates a new Tag object.
		 */
		public Tag() {
		}

		/**
		 * Creates a new Tag object.
		 *
		 * 
		 * 
		 */
		public Tag(String k, String v) {
			key = k;
			value = v;
		}
	}

	protected Tag tags[] = null;

	/**
	 * DOCUMENT-ME
	 */
	public void clear() {
		tags = null;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public boolean containsKey(Object obj) {
		if(!(obj instanceof String) || (tags == null)) {
			return false;
		}

		String key = (String) obj;

		for(int i = 0; i < tags.length; i++) {
			if(tags[i].key.equals(key)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public boolean containsValue(Object obj) {
		if(!(obj instanceof String) || (tags == null)) {
			return false;
		}

		String value = (String) obj;

		for(int i = 0; i < tags.length; i++) {
			if(tags[i].value.equals(value)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Set<Map.Entry<String,String>> entrySet() {
		return null;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public boolean equals(Object obj) {
		return false;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public String get(Object obj) {
		if((obj == null) || !(obj instanceof String) || (tags == null)) {
			return null;
		}

		String key = (String) obj;

		for(int i = 0; i < tags.length; i++) {
			if(tags[i].key.equals(key)) {
				return tags[i].value;
			}
		}

		return null;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int hashCode() {
		if(tags == null) {
			return super.hashCode();
		}

		int j = 0;

		for(int i = 0; i < tags.length; i++) {
			j += tags[i].key.hashCode();
		}

		return j;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean isEmpty() {
		return tags == null;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Set<String> keySet() {
		Set<String> s = new HashSet<String>();

		if(tags != null) {
			for(int i = 0; i < tags.length; i++) {
				s.add(tags[i].key);
			}
		}

		return s;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public String put(String key, String value) {
		if((key == null) || !((key instanceof String) && (value instanceof String))) {
			return null;
		}

		if(containsKey(key)) {
			for(int i = 0; i < tags.length; i++) {
				if(tags[i].key.equals(key)) {
					String old = tags[i].value;
					tags[i].value = (String) value;

					return old;
				}
			}
		} else {
			int curSize = 0;

			if(tags != null) {
				curSize = tags.length;
			}

			Tag temp[] = new Tag[curSize + 1];

			for(int i = 0; i < curSize; i++) {
				temp[i + 1] = tags[i];
			}

			temp[0] = new Tag((String) key, (String) value);
			tags = temp;
		}

		return null;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
   * @see Map#putAll(Map)
	 */
  public void putAll(Map<? extends String, ? extends String> map) {
		if(map.size() > 0) {
			Set<? extends String> s = map.keySet();
			Iterator<? extends String> it = s.iterator();

			while(it.hasNext()) {
				String key = it.next();
				put(key, map.get(key));
			}
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public String remove(Object obj) {
		if((obj == null) || !containsKey(obj)) {
			return null;
		}

		if(tags.length == 1) {
			String old = tags[0].value;
			tags = null;

			return old;
		}

		Tag temp[] = new Tag[tags.length - 1];
		int j = 0;
		String old = null;

		for(int i = 0; i < tags.length; i++) {
			if(!tags[i].key.equals(obj)) {
				temp[j] = tags[i];
				j++;
			} else {
				old = tags[i].value;
			}
		}

		tags = temp;

		return old;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int size() {
		if(tags == null) {
			return 0;
		}

		return tags.length;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Collection<String> values() {
		int s = 0;

		if(tags != null) {
			s = tags.length;
		}

		List<String> l = new ArrayList<String>(s);

		if(tags != null) {
			for(int i = 0; i < tags.length; i++) {
				l.add(tags[i].value);
			}
		}

		return l;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public String getTag(String tag) {
		return (String) get(tag);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public String putTag(String tag, String value) {
		return (String) put(tag, value);
	}
}
