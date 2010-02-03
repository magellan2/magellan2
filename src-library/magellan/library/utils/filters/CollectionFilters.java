// class magellan.library.utils.filters.CollectionFilters
// created on May 29, 2009
//
// Copyright 2003-2009 by magellan project team
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
package magellan.library.utils.filters;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class CollectionFilters {

  /**
   * Casts any collection to a generic collection. USE WITH EXTREME CARE! Use it only if you are
   * absolutely sure that the oldCollection contains only elements of Type T. The result is backed
   * by oldCollection. This means that any changes to oldCollection affect the collection returned
   * and vice versa.
   * 
   * @param <T>
   * @param oldCollection
   * @return Effectively returns oldCollection
   */
  @SuppressWarnings("unchecked")
  public static <T> Collection<T> uncheckedCast(Collection oldCollection, Class<T> type) {
    return oldCollection;
  }

  /**
   * Casts any collection to a generic collection. If not every element of oldCollection can be cast
   * to type, an Exception is thrown. The result is backed by oldCollection. This means that any
   * changes to oldCollection affect the collection returned and vice versa.
   * 
   * @param <T>
   * @param oldCollection
   * @return Effectively returns oldCollection
   * @throws ClassCastException
   */
  @SuppressWarnings("unchecked")
  public static <T> Collection<T> checkedCast(Collection oldCollection, Class<T> type)
      throws ClassCastException {
    for (Object o : oldCollection) {
      if (!type.isInstance(o))
        throw new ClassCastException(o + " is not of type " + type);
    }
    return oldCollection;
  }

  /**
   * Creates a new list, adds to it all elements of oldCollection which are of the specified type,
   * and returns this list.
   * 
   * @param <T>
   * @param oldCollection
   * @param type
   * @return A collection with the objects of the desired type
   */
  public static <T> Collection<T> filter(Collection<?> oldCollection, Class<T> type) {
    Collection<T> result = new HashSet<T>();
    for (Object o : oldCollection) {
      if (type.isInstance(o))
        result.add(type.cast(o));
    }
    return result;
  }

  /**
   * Creates a new list, adds to it all elements of oldCollection which are of the specified type,
   * and returns this list.
   * 
   * @param <T>
   * @param oldCollection
   * @param type
   * @return A collection with the objects of the desired type
   */
  public static <T> Collection<T> filter(Object[] oldCollection, Class<T> type) {
    Collection<T> result = new HashSet<T>();
    for (Object o : oldCollection) {
      if (type.isInstance(o))
        result.add(type.cast(o));
    }
    return result;
  }

  /**
   * Adds to newList all elements of oldCollection which are of the specified type.
   * 
   * @param <T>
   * @param oldCollection
   * @param newCollection
   * @param type
   */
  public static <T> void filter(Collection<?> oldCollection, Collection<T> newCollection, Class<T> type) {
    for (Object o : oldCollection) {
      if (type.isInstance(o))
        newCollection.add(type.cast(o));
    }
  }

  /**
   * Returns an iterator that iterates only over values of map of type class1.
   * 
   * @param <T>
   * @param class1
   * @param map
   * @return
   */
  public static <T> Iterator<T> getValueIterator(Class<T> class1, Map<?, ?> map) {
    if (map != null) {
      return getIterator(class1, map.values());
    } else {
      return new ClassIterator<T>(class1, Collections.emptyList().iterator());
    }
  }

  /**
   * Returns an iterator that iterates only over keys of map of type class1.
   * 
   * @param <T>
   * @param class1
   * @param map
   * @return
   */
  public static <T> Iterator<T> getKeyIterator(Class<T> class1, Map<?, ?> map) {
    if (map != null) {
      return getIterator(class1, map.keySet());
    } else {
      return new ClassIterator<T>(class1, Collections.emptyList().iterator());
    }
  }

  /**
   * Returns an iterator that iterates only over elements of coll of type class1.
   * 
   * @param <T>
   * @param class1
   * @param coll
   * @return
   */
  public static <T> Iterator<T> getIterator(Class<T> class1, Collection<?> coll) {
    if (coll != null) {
      return new ClassIterator<T>(class1, Collections.unmodifiableCollection(coll).iterator());
    } else {
      return new ClassIterator<T>(class1, Collections.emptyList().iterator());
    }
  }

  /**
   * An iterator implementation to iterate over Map of objects and return only object instances of
   * the given Class.
   */
  private static class ClassIterator<T> implements Iterator<T> {
    private Class<T> givenClass;
    private Iterator<?> givenIterator;
    private T currentObject;

    /**
     * Creates a new ClassIterator object.
     * 
     * @throws NullPointerException DOCUMENT-ME
     */
    public ClassIterator(Class<T> c, Iterator<?> i) {
      if (c == null) {
        throw new NullPointerException();
      }

      if (i == null) {
        throw new NullPointerException();
      }

      givenClass = c;
      givenIterator = i;
    }

    /**
     * DOCUMENT-ME
     */
    public boolean hasNext() {
      possiblyMoveToNext();

      return currentObject != null;
    }

    /**
     * DOCUMENT-ME
     * 
     * @throws NoSuchElementException DOCUMENT-ME
     */
    public T next() {
      possiblyMoveToNext();

      if (currentObject == null) {
        throw new NoSuchElementException();
      }

      T ret = currentObject;
      currentObject = null;

      return ret;
    }

    private void possiblyMoveToNext() {
      if (currentObject != null) {
        return;
      }

      try {
        Object newObject = null;
        currentObject = null;

        while (givenIterator.hasNext() && (newObject == null)) {
          newObject = givenIterator.next();

          if (!givenClass.isInstance(newObject)) {
            newObject = null;
          } else
            currentObject = givenClass.cast(newObject);
        }

      } catch (NoSuchElementException e) {
      }
    }

    /**
     * DOCUMENT-ME
     */
    public void remove() {
      givenIterator.remove();
    }
  }

  /**
   * Returns an (unmodifiable) collection that contains all elements that are instances of
   * <code>class1</code> in <code>values</code>.
   */
  public static <T> Collection<T> getCollection(final Class<T> class1, final Collection<?> values) {
    return new AbstractCollection<T>() {

      @Override
      public Iterator<T> iterator() {
        return new ClassIterator<T>(class1, values.iterator());
      }

      @Override
      public int size() {
        int size = 0;
        for (Object o : values) {
          if (class1.isInstance(o))
            size++;
        }
        return size;
      }
    };
  }

}
