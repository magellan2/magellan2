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

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A LRU-like organized collection, i.e. calls to the add() method always insert elements at the
 * front of the collection. If an already existing element is added to the bucket it is moved to
 * the front of the bucket. If a maximum size is set for the bucket it does not grow beyond this
 * limit by dropping elements at the end of the bucket.
 * 
 * @param <E> The content type
 */
public class Bucket<E> extends AbstractCollection<E> {
  private int maxSize = -1;
  private List<E> data = new LinkedList<E>();

  // private int hashCode = 76;

  /**
   * Creates a Bucket object with unlimited maximum size. I.e. this bucket will never drop elements
   * by itself.
   */
  public Bucket() {
    this(-1);
  }

  /**
   * Creates a Bucket object with the specified maximum size.
   * 
   * @param maxSize maximum size of the bucket.
   */
  public Bucket(int maxSize) {
    this.maxSize = maxSize;
  }

  /**
   * Add an object to the front of the bucket. If the number of elements in the bucket exceeds the
   * bucket's maximum size an element at the end of the bucket is dropped. If object o is already
   * contained in the bucket, it is moved to the front of the bucket.
   * 
   * @param o object to add to the bucket.
   */
  @Override
  public boolean add(E o) {
    // don't do anything in this pathologic case
    if (getMaxSize() == 0)
      return false;

    int index = data.indexOf(o);

    // remove the object from the list, if it is already there
    // and not the first element
    if (index > 0) {
      data.remove(index);
      // hashCode = (hashCode - o.hashCode())/7;
    }

    // add item if it is not already the first element
    if (index != 0) {
      data.add(0, o);
      // hashCode = hashCode * 7 + o.hashCode();
    }

    // enforce size limitation
    if (getMaxSize() > 0) {
      while (data.size() > getMaxSize()) {
        // E old =
        data.remove(data.size() - 1);
        // hashCode = (hashCode - old.hashCode())/7;
      }
    }

    return (index != 0);
  }

  /**
   * Empty bucket.
   */
  @Override
  public void clear() {
    data.clear();
    // hashCode = 76;
  }

  /**
   * Check if an object is inside the bucket.
   * 
   * @param o object to look for.
   * @return true if found, false if not.
   */
  @Override
  public boolean contains(Object o) {
    return data.contains(o);
  }

  /**
   * Check if a list of objects is inside the bucket.
   * 
   * @param c collection of objects to look for.
   * @return true if all were found, false if not.
   */
  @Override
  public boolean containsAll(Collection<?> c) {
    return data.containsAll(c);
  }

  /**
   * Check if a given object is the same as the actual bucket. This is a semi-deep version
   * 
   * @param o object to be compared with.
   * @return true if it is the same, false if not.
   */
  @Override
  public boolean equals(Object o) {
    if (o == this)
      return true;

    if (o instanceof Bucket) {
      Bucket<?> b = (Bucket<?>) o;

      if ((this.getMaxSize() == b.getMaxSize()) && (this.size() == b.size())) {
        Iterator<E> i1 = this.iterator();
        Iterator<?> i2 = b.iterator();

        while (i1.hasNext() && i2.hasNext()) {
          if (!i1.next().equals(i2.next()))
            return false;
        }
        return true;
      }
    }
    return false;
  }

  /**
   * This implementation is pretty naive...
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int count = 0;
    int hashCode = 76;
    for (E o : data) {
      hashCode = hashCode * 31 + o.hashCode();
      if (count++ > 3)
        return hashCode;
    }
    return hashCode;
  }

  /**
   * Check if bucket is empty.
   * 
   * @return true if empty, false if not.
   */
  @Override
  public boolean isEmpty() {
    return data.isEmpty();
  }

  /**
   * Get an interator for the bucket content.
   * 
   * @return an iterator for the objects inside the bucket.
   */
  @Override
  public Iterator<E> iterator() {
    return data.iterator();
  }

  /**
   * Remove a given object from the bucket.
   * 
   * @param o object to remove from the bucket.
   * @return true if removal was successful, false if not.
   */
  @Override
  public boolean remove(Object o) {
    // hashCode = (hashCode - o.hashCode())/7;
    return data.remove(o);
  }

  /**
   * Remove a list of items from the bucket.
   * 
   * @param c collection of all the items to remove.
   * @return true if removal was successful, false if not.
   */
  @Override
  public boolean removeAll(Collection<?> c) {
    // for (Object o : c)
    // hashCode = (hashCode - o.hashCode())/7;
    return data.removeAll(c);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param c collection of the items to retain.
   * @return true if retaining was successfull, false if not.
   */
  @Override
  public boolean retainAll(Collection<?> c) {
    boolean val = data.retainAll(c);
    // hashCode = 76;
    // for (E o : data)
    // hashCode = hashCode*7 + o.hashCode();
    return val;
  }

  /**
   * Get the number of items inside the bucket.
   * 
   * @return the number of objects inside the bucket.
   */
  @Override
  public int size() {
    return data.size();
  }

  /**
   * Get array of all items inside the bucket.
   * 
   * @return array of items inside the bucket.
   */
  @Override
  public Object[] toArray() {
    return data.toArray();
  }

  /**
   * @see AbstractCollection#toArray(Object[])
   */
  @Override
  public <T> T[] toArray(T a[]) {
    return data.toArray(a);
  }

  /**
   * Returns the maximum size of this bucket that it is not allowed to outgrow.
   * 
   * @return number of items the bucket may contain.
   */
  public int getMaxSize() {
    return this.maxSize;
  }

  /**
   * Sets the specified maximum size for this bucket. If there are more elements in the bucket than
   * maxSize, elements at the end of the bucket are dropped until the size of the bucket equals
   * maxSize.
   * 
   * @param maxSize number of items the bucket may contain.
   */
  public void setMaxSize(int maxSize) {
    while (data.size() > maxSize) {
      // E o =
      data.remove(data.size() - 1);
      // hashCode = (hashCode - o.hashCode())/7;
    }

    this.maxSize = maxSize;
  }

  /**
   * Create a string representation of the bucket.
   * 
   * @return string representation of the bucket.
   */
  @Override
  public String toString() {
    return data.toString();
  }

  /**
   * Get a specific item from the bucket.
   * 
   * @param index number of a specific item in the bucket.
   * @return the element at the specified index.
   */
  public E get(int index) {
    return data.get(index);
  }
}
