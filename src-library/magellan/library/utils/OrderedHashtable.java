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

import java.lang.ref.SoftReference;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Hashtable implementation maintaining the values in the order they were inserted. Note that the
 * order is only maintained on the put() and get() functions and the ordered values can only be
 * accessed through the values() function. All other methods that operate on other Maps or on keys
 * do not provide any guarantee for the ordering of the values if not otherwise mentioned. Further
 * note that in order to maintain the order of the values additional space and time overhead is
 * introduced to the standard Hashtable implementation. This object should be threadsafe, every
 * value changing method is synchronized.
 */
public class OrderedHashtable<K, V> extends Hashtable<K, V> {
  private List<K> keyList;

  /**
   * Constructs a new, empty ordered hashtable with a default capacity and load factor, which is
   * 0.75.
   */
  public OrderedHashtable() {
    this(6);
  }

  /**
   * Constructs a new, empty ordered hashtable with the specified initial capacity and default load
   * factor, which is 0.75.
   * 
   * @param initialCapacity the initial capacity of the hashtable.
   */
  public OrderedHashtable(int initialCapacity) {
    this(initialCapacity, 0.75f);
  }

  /**
   * Constructs a new, empty hashtable with the specified initial capacity and the specified load
   * factor.
   * 
   * @param initialCapacity the initial capacity of the hashtable.
   * @param loadFactor the load factor of the hashtable.
   */
  public OrderedHashtable(int initialCapacity, float loadFactor) {
    super(initialCapacity, loadFactor);
    keyList = new ArrayList<K>(initialCapacity);
  }

  /**
   * Constructs a new hashtable with the same mappings as the given Map. The hashtable is created
   * with a capacity of twice the number of entries in the given Map or 11 (whichever is greater),
   * and a default load factor, which is 0.75. If <kbd>t</kbd> is an instance of
   * <kbd>OrderedHashtable</kbd> the new Hashtable contains its values in the same order as <kbd>t</kbd>
   * . Creating a new Hashtable with this constructor is very expensive.
   */
  public OrderedHashtable(Map<? extends K, ? extends V> t) {
    this(Math.max(2 * t.size(), 11), 0.75f);
    this.putAll(t);
  }

  /**
   * Clears this hashtable so that it contains neither keys nor values.
   */
  @Override
  public synchronized void clear() {
    super.clear();
    keyList.clear();
  }

  /**
   * Returns a view on the set of keys which is backed by the Hashtable
   * 
   * @see java.util.Hashtable#keySet()
   */
  @Override
  public Set<K> keySet() {
    return new KeySet();
  }

  private class KeySet extends AbstractSet<K> {
    /**
     * Returns an iterator over the keys.
     */
    @Override
    public Iterator<K> iterator() {
      return new OHKeyIterator();
    }

    /**
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size() {
      return OrderedHashtable.this.size();
    }

    /**
     * @see java.util.AbstractCollection#contains(java.lang.Object)
     */
    @Override
    public boolean contains(Object o) {
      return containsKey(o);
    }

    /**
     * Removes o from the Hashtable
     * 
     * @see java.util.AbstractCollection#remove(java.lang.Object)
     */
    @Override
    public boolean remove(Object o) {
      return OrderedHashtable.this.remove(o) != null;
    }

    /**
     * Removes all elements from the Hashtable
     * 
     * @see java.util.AbstractCollection#clear()
     */
    @Override
    public void clear() {
      OrderedHashtable.this.clear();
    }
  }

  /**
   * Creates a shallow copy of this hashtable. All the structure of the hashtable itself is copied,
   * but the keys and values are not cloned. This is a very expensive operation.
   * 
   * @return an ordered clone of the ordered hashtable.
   */
  @Override
  public synchronized Object clone() {
    @SuppressWarnings("unchecked")
    OrderedHashtable<K, V> clone = (OrderedHashtable<K, V>) super.clone();
    clone.clear();
    clone.putAll(this);

    return clone;
  }

  /**
   * Returns an ordered enumeration of the values in this hashtable. Use the Enumeration methods on
   * the returned object to fetch the elements sequentially.
   * 
   * @return an enumeration of the values in this hashtable.
   */
  @Override
  public synchronized Enumeration<V> elements() {
    return new OHValueIterator();
  }

  /**
   * Returns a List view of the entries contained in this Hashtable. Each element in this collection
   * is a Map.Entry. The List is not backed by the Hashtable. The entries are returned in the order
   * they were inserted into the hashtable. This is a very expensive operation.
   * 
   * @return an ordered list view of the mappings contained in this map.
   * @throws NullPointerException DOCUMENT-ME
   */
  public synchronized List<Map.Entry<K, V>> entryList() {
    // 2002.04.18 pavkovic: this code seems to be quite ugly, but
    // Hashtable caches the hashCode() value of the key object. This
    // is fine for normal purposes. But there are some strange objects
    // like com.eressea.util.Coordinate which change their hashCode()
    // value on runtime! (this contradicts the general contract of
    // Object.hashCode() and Object.equals()). By doing so the key in
    // the keyList unpredictably can or cannot be found in the hashtable.
    // To circumvent this problem we build an array by using the
    // hashtable entries and find the position of its key in the keylist.
    // As I said before, this is a very expensive operation.
    //
    // unchecked cast is safe, because we insert only entries of entryList, which is bounded
    // correctly!
    @SuppressWarnings("unchecked")
    Map.Entry<K, V> alist[] = new Map.Entry[entrySet().size()];

    for (Map.Entry<K, V> entry : entrySet()) {
      OHEntry<K, V> newE = new OHEntry<K, V>(entry);
      alist[keyList.indexOf(newE.key)] = newE;
    }

    // check consistency
    // huh? this can never cause an exception...
    // for (int i = 0; i < alist.length; i++) {
    // if (alist[i] == null) {
    // throw new NullPointerException();
    // }
    // }

    return Arrays.asList(alist);
  }

  /**
   * Maps the specified <code>key</code> to the specified <code>value</code> in this hashtable.
   * Neither the key nor the value can be <code>null</code>.
   * <p>
   * The value can be retrieved by calling the <code>get</code> method with a key that is equal to
   * the original key. Calls to this method determine the order of the values.
   * </p>
   * 
   * @param key the hashtable key.
   * @param value the value.
   * @return the previous value of the specified key in this hashtable, or <code>null</code> if it
   *         did not have one.
   */
  @Override
  public synchronized V put(K key, V value) {
    V old = super.put(key, value);

    if (old == null) {
      // keep track of newly added objects
      keyList.add(key);
    }

    return old;
  }

  /* a view on this hashtable */
  private transient SoftReference<Collection<V>> values = null;

  /**
   * Copies all of the mappings from the specified Map to this Hashtable. These mappings will
   * replace any mappings that this Hashtable had for any of the keys currently in the specified
   * Map. This method maintains the order of entries as they are returned by <kbd>t.entrySet()</kbd>
   * .or <kbd>t.entryList()</kbd> if t is an instance of <kbd>OrderedHashtable</kbd>.
   * 
   * @param t Mappings to be stored in this map.
   * @see java.util.Hashtable#putAll(java.util.Map)
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public synchronized void putAll(Map<? extends K, ? extends V> t) {
    Iterator<? extends Map.Entry<? extends K, ? extends V>> iter = null;

    if (t instanceof OrderedHashtable<?, ?>) {
      // unchecked cast okay here, because t is correctly bounded
      iter = ((OrderedHashtable) t).entryList().iterator();
    } else {
      iter = t.entrySet().iterator();
    }

    while (iter.hasNext()) {
      Map.Entry<? extends K, ? extends V> e = iter.next();
      this.put(e.getKey(), e.getValue());
    }
  }

  /**
   * Removes the key (and its corresponding value) from this hashtable. This method does nothing if
   * the key is not in the hashtable. This is very expensive operation.
   * 
   * @param key the key that needs to be removed.
   * @return the value to which the key had been mapped in this hashtable, or <code>null</code> if
   *         the key did not have a mapping.
   */
  @Override
  public synchronized V remove(Object key) {
    if (keyList.remove(key))
      return super.remove(key);
    else
      return null;
  }

  /**
   * Returns a Collection view of the values contained in this Hashtable. The Collection does not
   * support element removal or addition. The Collection returns the Hashtable's values in the order
   * they were added to it with the put() method.
   * 
   * @return an ordered collection view of the values contained in this map.
   */
  @Override
  public Collection<V> values() {
    if (values == null || values.get() == null) {
      values =
          new SoftReference<Collection<V>>(Collections
              .synchronizedCollection(new ValueCollection()));
    }

    return values.get();
  }

  private class ValueCollection extends AbstractCollection<V> {
    /**
     * Returns an iterator over the values of the map.
     * 
     * @see java.util.AbstractCollection#iterator()
     */
    @Override
    public Iterator<V> iterator() {
      return new OHValueIterator();
    }

    /**
     * Returns the map's size.
     * 
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size() {
      return OrderedHashtable.this.size();
    }

    /**
     * Returns true iff the map contains o as value.
     * 
     * @see java.util.AbstractCollection#contains(java.lang.Object)
     */
    @Override
    public boolean contains(Object o) {
      return containsValue(o);
    }

    /**
     * Clears the map.
     */
    @Override
    public void clear() {
      OrderedHashtable.this.clear();
    }
  }

  private class OHValueIterator implements Iterator<V>, Enumeration<V> {

    OHKeyIterator keyIterator = new OHKeyIterator();

    public OHValueIterator() {

    }

    public boolean hasNext() {
      return keyIterator.hasNext();
    }

    public V next() {
      return get(keyIterator.next());
    }

    public void remove() {
      keyIterator.remove();
    }

    public boolean hasMoreElements() {
      return keyIterator.hasMoreElements();
    }

    public V nextElement() {
      return get(keyIterator.nextElement());
    }

  }

  private class OHKeyIterator implements Iterator<K>, Enumeration<K> {
    Iterator<K> base;
    K last = null;

    /**
     * Creates a new OHIterator object.
     */
    public OHKeyIterator() {
      // we use the keys iterator as base for value iterator
      base = OrderedHashtable.this.keyList.iterator();
    }

    // Enumeration methods
    /**
     * @see java.util.Enumeration#hasMoreElements()
     */
    public boolean hasMoreElements() {
      return hasNext();
    }

    /**
     * @see java.util.Enumeration#nextElement()
     */
    public K nextElement() {
      return next();
    }

    // Iterator methods
    /**
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
      return base.hasNext();
    }

    /**
     * @see java.util.Iterator#next()
     */
    public K next() {
      last = base.next();

      return last;
    }

    /**
     * @see java.util.Iterator#remove()
     * @throws IllegalStateException if the <kbd>next</kbd> method has not yet been called, or the
     *           <kbd>remove</kbd> method has already been called after the last call to the
     *           <kbd>next</kbd> method.
     */
    public void remove() {
      if (last == null)
        throw new IllegalStateException();

      OrderedHashtable.this.remove(last);
      last = null;
    }
  }

  private static class OHEntry<K, V> implements Map.Entry<K, V> {
    private K key;
    private V value;

    /**
     * Creates a new OHEntry object.
     * 
     * @throws NullPointerException if key or value of the specified entry are <code>null</code>.
     */
    public OHEntry(Map.Entry<K, V> entry) {
      key = entry.getKey();
      value = entry.getValue();

      if ((key == null) || (value == null))
        throw new NullPointerException();
    }

    /**
     * Returns <code>true</code> if o is an OHEntry and its key and value match this entry.
     */
    @Override
    public boolean equals(Object o) {
      if (o == null)
        return false;
      try {
        // if this throws a ClassCastException, the entries are not equal...
        @SuppressWarnings({ "unchecked", "rawtypes" })
        OHEntry<K, V> e2 = (OHEntry) o;

        return ((key == null) ? (e2.key == null) : key.equals(e2.key))
            && ((value == null) ? (e2.value == null) : value.equals(e2.value));
      } catch (ClassCastException e) {
        return false;
      }
    }

    /**
     * @see java.util.Map.Entry#getKey()
     */
    public K getKey() {
      return key;
    }

    /**
     * @see java.util.Map.Entry#getValue()
     */
    public V getValue() {
      return value;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      return ((key == null) ? 0 : key.hashCode()) ^ ((value == null) ? 0 : value.hashCode());
    }

    /**
     * @see java.util.Map.Entry#setValue(java.lang.Object)
     * @throws NullPointerException if the specified value is <code>null</code>.
     */
    public V setValue(V value) {
      if (value == null)
        throw new NullPointerException();

      V oldValue = this.value;
      this.value = value;

      return oldValue;
    }

    @Override
    public String toString() {
      return key + "=" + value;
    }
  }
}
