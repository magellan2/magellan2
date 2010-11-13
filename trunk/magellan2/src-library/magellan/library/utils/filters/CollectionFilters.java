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

/**
 * A tool box of methods to filter collections by class.
 * 
 * @author stm
 */
public class CollectionFilters {

  /**
   * Casts any collection to a generic collection. USE WITH EXTREME CARE! Use it only if you are
   * absolutely sure that the oldCollection contains only elements of Type T. The result is backed
   * by oldCollection. This means that any changes to oldCollection affect the collection returned
   * and vice versa.
   * 
   * @param <T> The target collection's element type
   * @param oldCollection
   * @return Effectively returns oldCollection
   */
  @SuppressWarnings("unchecked")
  public static <T> Collection<T> uncheckedCast(
      @SuppressWarnings("rawtypes") Collection oldCollection, Class<T> type) {
    return oldCollection;
  }

  /**
   * Casts any collection to a generic collection. If not every element of oldCollection can be cast
   * to type, an Exception is thrown. The result is backed by oldCollection. This means that any
   * changes to oldCollection affect the collection returned and vice versa.
   * 
   * @param <T> The target collection's element type
   * @param oldCollection
   * @param type The values are filtered by this type; effectively the same as T
   * @return Effectively returns oldCollection
   * @throws ClassCastException if any of the elements of oldCollection is not an instance of type
   */
  @SuppressWarnings("unchecked")
  public static <T> Collection<T> checkedCast(
      @SuppressWarnings("rawtypes") Collection oldCollection, Class<T> type)
      throws ClassCastException {
    for (Object o : oldCollection) {
      if (!type.isInstance(o))
        throw new ClassCastException(o + " is not of type " + type);
    }
    return oldCollection;
  }

  /**
   * Creates a new set, adds to it all elements of oldCollection which are of the specified type,
   * and returns this set.
   * 
   * @param <T> The target collection's element type
   * @param oldCollection
   * @param type The values are filtered by this type; effectively the same as T
   * @return A collection with the objects of the desired type
   */
  public static <T> Collection<T> filter(Collection<?> oldCollection, Class<T> type) {
    Collection<T> result = new HashSet<T>();
    for (Object o : oldCollection) {
      if (type.isInstance(o)) {
        result.add(type.cast(o));
      }
    }
    return result;
  }

  /**
   * Creates a new set, adds to it all elements of oldCollection which are of the specified type,
   * and returns this set.
   * 
   * @param <T> The target collection's element type
   * @param oldCollection
   * @param type The values are filtered by this type; effectively the same as T
   * @return A collection with the objects of the desired type
   */
  public static <T> Collection<T> filter(Object[] oldCollection, Class<T> type) {
    Collection<T> result = new HashSet<T>();
    for (Object o : oldCollection) {
      if (type.isInstance(o)) {
        result.add(type.cast(o));
      }
    }
    return result;
  }

  /**
   * Adds to newCollection all elements of oldCollection which are of the specified type.
   * 
   * @param <T> The target collection's element type
   * @param oldCollection
   * @param newCollection
   * @param type The values are filtered by this type; effectively the same as T
   */
  public static <T> void filter(Collection<?> oldCollection, Collection<T> newCollection,
      Class<T> type) {
    for (Object o : oldCollection) {
      if (type.isInstance(o)) {
        newCollection.add(type.cast(o));
      }
    }
  }

  /**
   * Returns an iterator that iterates only over values of map of type class1.
   * 
   * @param <T> The target collection's element type
   * @param type The values are filtered by this type; effectively the same as T
   * @param map
   * @return an iterator that iterates only over values of map of type class1.
   */
  public static <T> Iterator<T> getValueIterator(Class<T> type, Map<?, ?> map) {
    if (map != null)
      return CollectionFilters.getIterator(type, map.values());
    else
      return new ClassIterator<T>(type, Collections.emptyList().iterator());
  }

  /**
   * Returns an iterator that iterates only over keys of map of type class1.
   * 
   * @param <T> The target collection's element type
   * @param type The values are filtered by this type; effectively the same as T
   * @param map
   * @return an iterator that iterates only over keys of map of type class1.
   */
  public static <T> Iterator<T> getKeyIterator(Class<T> type, Map<?, ?> map) {
    if (map != null)
      return CollectionFilters.getIterator(type, map.keySet());
    else
      return new ClassIterator<T>(type, Collections.emptyList().iterator());
  }

  /**
   * Returns an iterator that iterates only over elements of coll of type class1.
   * 
   * @param <T> The target collection's element type
   * @param type The values are filtered by this type; effectively the same as T
   * @param coll
   * @return an iterator that iterates only over entries of the collection of type class1.
   */
  public static <T> Iterator<T> getIterator(Class<T> type, Collection<?> coll) {
    if (coll != null)
      return new ClassIterator<T>(type, Collections.unmodifiableCollection(coll).iterator());
    else
      return new ClassIterator<T>(type, Collections.emptyList().iterator());
  }

  /**
   * An iterator implementation to iterate over Map of objects and return only object instances of
   * the given Class.
   * 
   * @param <T> The target collection's element type
   * @author stm
   */
  private static class ClassIterator<T> implements Iterator<T> {
    private Class<T> givenClass;
    private Iterator<?> givenIterator;
    private T currentObject;

    /**
     * Creates a new ClassIterator object.
     * 
     * @throws NullPointerException if on of the arguments is <code>null</code>
     */
    public ClassIterator(Class<T> c, Iterator<?> i) {
      if (c == null)
        throw new NullPointerException();

      if (i == null)
        throw new NullPointerException();

      givenClass = c;
      givenIterator = i;
    }

    /**
     * Returns true if the collection contains more elements of the class
     * 
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
      possiblyMoveToNext();

      return currentObject != null;
    }

    /**
     * Returns the next element of the class in the collection.
     * 
     * @see java.util.Iterator#next()
     * @throws NoSuchElementException If there's no more such element
     */
    public T next() {
      possiblyMoveToNext();

      if (currentObject == null)
        throw new NoSuchElementException();

      T ret = currentObject;
      currentObject = null;

      return ret;
    }

    private void possiblyMoveToNext() {
      if (currentObject != null)
        return;

      try {
        Object newObject = null;
        currentObject = null;

        while (givenIterator.hasNext() && (newObject == null)) {
          newObject = givenIterator.next();

          if (!givenClass.isInstance(newObject)) {
            newObject = null;
          } else {
            currentObject = givenClass.cast(newObject);
          }
        }

      } catch (NoSuchElementException e) {
        // currentObject = null;
      }
    }

    /**
     * @see java.util.Iterator#remove()
     */
    public void remove() {
      givenIterator.remove();
    }
  }

  /**
   * Returns an (unmodifiable) collection that contains all elements that are instances of
   * <code>class1</code> in <code>values</code>.
   * 
   * @param <T> The target collection's element type
   * @param type The values are filtered by this type; effectively the same as T
   * @param values
   * @return A collection of type T elements
   * @see Class#isInstance(Object)
   */
  public static <T> Collection<T> getCollection(final Class<T> type, final Collection<?> values) {
    return new AbstractCollection<T>() {

      @Override
      public Iterator<T> iterator() {
        return new ClassIterator<T>(type, values.iterator());
      }

      @Override
      public int size() {
        int size = 0;
        for (Object o : values) {
          if (type.isInstance(o)) {
            size++;
          }
        }
        return size;
      }
    };
  }

}
