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

import magellan.library.utils.logging.Logger;

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
   * @param defaults the defaults.
   */
  public OrderedOutputProperties(Properties defaults) {
    super(defaults);
  }

  /**
   * DOCUMENT-ME
   * 
   * @see java.util.Hashtable#keys()
   */
  @Override
  public synchronized Enumeration<Object> keys() {
    List<Object> l = new LinkedList<Object>();
    l.addAll(keySet());
    Collections.sort(l, new ObjectComparator<Object>());

    return new IteratorEnumeration(l.iterator());
  }

  private static class IteratorEnumeration implements Enumeration<Object> {
    protected Iterator<?> iterator;

    /**
     * Creates a new IteratorEnumeration object.
     */
    public IteratorEnumeration(Iterator<?> it) {
      iterator = it;
    }

    /**
     * @see java.util.Enumeration#hasMoreElements()
     */
    public boolean hasMoreElements() {
      return iterator.hasNext();
    }

    /**
     * @see java.util.Enumeration#nextElement()
     */
    public Object nextElement() {
      return iterator.next();
    }
  }

  private static class ObjectComparator<T extends Object> implements Comparator<T> {

    ObjectComparator() {

    }

    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    // The class cast exception is caught...
    @SuppressWarnings("unchecked")
    public int compare(Object o1, Object o2) {
      if (o1 == null)
        return Integer.MAX_VALUE;
      if (o2 == null)
        return Integer.MIN_VALUE;
      if (o1 instanceof Comparable && o2 instanceof Comparable) {
        Comparable c1 = (Comparable) o1;
        Comparable c2 = (Comparable) o2;
        try {
          return c1.compareTo(c2);
        } catch (ClassCastException e) {
          Logger.getInstance(OrderedOutputProperties.class).warn(e);
        }
      }
      return o1.toString().compareTo(o2.toString());
    }

  }
}
