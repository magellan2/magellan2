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

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;


/**
 * DOCUMENT ME!
 *
 * @author Andreas Gampe
 * @version 1.0
 */
public class OrderedOutputProperties extends Properties {
	/**
	 * Creates new OrderedOutputProperties
	 */
	public OrderedOutputProperties() {
	}

	/**
	 * Creates a new OrderedOutputProperties object.
	 *
	 * 
	 */
	public OrderedOutputProperties(Properties def) {
		super(def);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public Enumeration<Object> keys() {
		List<Object> l = new LinkedList<Object>();
		l.addAll(keySet());
		Collections.sort(l, new ObjectComparator());

		return new IteratorEnumeration(l.iterator());
	}

	private class IteratorEnumeration implements Enumeration<Object> {
		protected Iterator iterator;

		/**
		 * Creates a new IteratorEnumeration object.
		 *
		 * 
		 */
		public IteratorEnumeration(Iterator it) {
			iterator = it;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public boolean hasMoreElements() {
			return iterator.hasNext();
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public Object nextElement() {
			return iterator.next();
		}
	}
	
	private class ObjectComparator implements Comparator<Object> {

    public int compare(Object o1, Object o2) {
      if (o1 == null) {
        return Integer.MAX_VALUE;
      }
      if (o2 == null) {
        return Integer.MIN_VALUE;
      }
      if (o1 instanceof Comparable && o2 instanceof Comparable) {
        Comparable c1 = (Comparable)o1;
        Comparable c2 = (Comparable)o2;
        return c1.compareTo(c2);
      } else {
        return o1.toString().compareTo(o2.toString());
      }
    }
	  
	}
}
