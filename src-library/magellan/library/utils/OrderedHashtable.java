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
 * do not provide any garuantee for the ordering of the  values if not otherwise mentioned.
 * Further note that in order to maintain the order of the values additional space and time
 * overhead is introduced to the standard Hashtable implementation. This object should be
 * threadsafe, every value changing method  is synchronized.
 */
public class OrderedHashtable<K,V> extends Hashtable<K,V> {
	private List<K> keyList;

	/**
	 * Constructs a new, empty ordered hashtable with a default capacity and load factor, which is
	 * 0.75.
	 */
	public OrderedHashtable() {
		this(6);
	}

	/**
	 * Constructs a new, empty ordered hashtable with the specified initial capacity and default
	 * load factor, which is 0.75.
	 *
	 * @param initialCapacity the initial capacity of the hashtable.
	 */
	public OrderedHashtable(int initialCapacity) {
		this(initialCapacity, 0.75f);
	}

	/**
	 * Constructs a new, empty hashtable with the specified initial  capacity and the specified
	 * load factor.
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
	 * with a capacity of twice the number of entries in the given Map or 11 (whichever is
	 * greater), and a default load factor, which is 0.75. If <tt>t</tt> is an instance of
	 * <tt>OrderedHashtable</tt> the new Hashtable contains its values in the same order as
	 * <tt>t</tt>. Creating a new Hashtable with this constructor is very expensive.
	 *
	 * 
	 */
	public OrderedHashtable(Map t) {
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
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public Set<K> keySet() {
		return new KeySet();
	}

	private class KeySet extends AbstractSet<K> {
		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		@Override
    public Iterator<K> iterator() {
			return new OHIterator(OrderedHashtable.KEYS);
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		@Override
    public int size() {
			return OrderedHashtable.this.size();
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 *
		 * 
		 */
		@Override
    public boolean contains(Object o) {
			return containsKey(o);
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 *
		 * 
		 */
		@Override
    public boolean remove(Object o) {
			return OrderedHashtable.this.remove(o) != null;
		}

		/**
		 * DOCUMENT-ME
		 */
		@Override
    public void clear() {
			OrderedHashtable.this.clear();
		}
	}

	/**
	 * Creates a shallow copy of this hashtable. All the structure of the hashtable itself is
	 * copied, but the keys and values are  not cloned. This is a very expensive operation.
	 *
	 * @return an ordered clone of the ordered hashtable.
	 */
	@Override
  public synchronized Object clone() {
		return new OrderedHashtable(this);
	}

	/**
	 * Returns an ordered enumeration of the values in this hashtable. Use the Enumeration methods
	 * on the returned object to fetch the elements sequentially.
	 *
	 * @return an enumeration of the values in this hashtable.
	 */
	@Override
  public synchronized Enumeration elements() {
		return new OHIterator(OrderedHashtable.VALUES);
	}

	/**
	 * Returns a List view of the entries contained in this Hashtable. Each element in this
	 * collection is a Map.Entry.  The List is not backed by the Hashtable. The entries are
	 * returned in the order they were inserted into the hashtable. This is a very expensive
	 * operation.
	 *
	 * @return an ordered list view of the mappings contained in this map.
	 *
	 * @throws NullPointerException DOCUMENT-ME
	 */
	public synchronized List<Map.Entry<K,V>> entryList() {
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
		Map.Entry<K, V> alist[] = new Map.Entry[entrySet().size()];

		for(Iterator<Map.Entry<K,V>> iter = entrySet().iterator(); iter.hasNext();) {
			Map.Entry<K,V> entry = iter.next();
			OHEntry<K,V> newE = new OHEntry<K,V>(entry);
			alist[keyList.indexOf(newE.key)] = newE;
		}

		// check consistency
		for(int i = 0; i < alist.length; i++) {
			if(alist[i] == null) {
				throw new NullPointerException();
			}
		}

		return Arrays.asList(alist);
	}

	/**
	 * Maps the specified <code>key</code> to the specified  <code>value</code> in this hashtable.
	 * Neither the key nor the  value can be <code>null</code>.
	 * 
	 * <p>
	 * The value can be retrieved by calling the <code>get</code> method  with a key that is equal
	 * to the original key. Calls to this method determine the order of the values.
	 * </p>
	 *
	 * @param key the hashtable key.
	 * @param value the value.
	 *
	 * @return the previous value of the specified key in this hashtable, or <code>null</code> if
	 * 		   it did not have one.
	 */
	@Override
  public synchronized V put(K key, V value) {
		V old = super.put(key, value);

		if(old == null) {
			// keep track of newly added objects
			keyList.add(key);
		}

		return old;
	}

	/**
	 * Copies all of the mappings from the specified Map to this Hashtable These mappings will
	 * replace any mappings that this Hashtable had for any of the keys currently in the specified
	 * Map. This method maintains the order of entries as they are returned by
	 * <tt>t.entrySet()</tt>.or <tt>t.entryList()</tt> if t is an instance of
	 * <tt>OrderedHashtable</tt>.
	 *
	 * @param t Mappings to be stored in this map.
	 */
	@Override
  public synchronized void putAll(Map t) {
		Iterator<Map.Entry<K,V>> iter = null;

		if(t instanceof OrderedHashtable) {
			iter = ((OrderedHashtable) t).entryList().iterator();
		} else {
			iter = t.entrySet().iterator();
		}

		while(iter.hasNext()) {
			Map.Entry<K,V> e = iter.next();
			this.put(e.getKey(), e.getValue());
		}
	}

	/**
	 * Removes the key (and its corresponding value) from this  hashtable. This method does nothing
	 * if the key is not in the hashtable. This is very expensive operation.
	 *
	 * @param key the key that needs to be removed.
	 *
	 * @return the value to which the key had been mapped in this hashtable, or <code>null</code>
	 * 		   if the key did not have a mapping.
	 */
	@Override
  public synchronized V remove(Object key) {
		if(keyList.remove(key)) {
			return super.remove(key);
		} else {
			return null;
		}
	}

	/* a view on this hashtable */
	private transient SoftReference<Collection<V> > values = null;

	/**
	 * Returns a Collection view of the values contained in this Hashtable. The Collection does not
	 * support element removal or addition. The Collection returns the Hashtable's values in the
	 * order they were added to it with the put() method.
	 *
	 * @return an ordered collection view of the values contained in this map.
	 */
	@Override
  public Collection<V> values() {
		if(values == null || values.get()==null) {
			values = new SoftReference<Collection<V>>(Collections.synchronizedCollection(new ValueCollection()));
		}
		
		return values.get();
	}

	private class ValueCollection extends AbstractCollection<V> {
		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		@Override
    public Iterator<V> iterator() {
			return new OHIterator(OrderedHashtable.VALUES);
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		@Override
    public int size() {
			return OrderedHashtable.this.size();
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 *
		 * 
		 */
		@Override
    public boolean contains(Object o) {
			return containsValue(o);
		}

		/**
		 * DOCUMENT-ME
		 */
		@Override
    public void clear() {
			OrderedHashtable.this.clear();
		}
	}

	private static final int KEYS = 0;
	private static final int VALUES = 1;

	private class OHIterator implements Iterator, Enumeration {
		Iterator<K> base;
		K last = null;
		int mode = 0;

		/**
		 * Creates a new OHIterator object.
		 *
		 * 
		 */
		public OHIterator(int mode) {
			// we use the keys iterator as base for value iterator
			base = OrderedHashtable.this.keyList.iterator();
			this.mode = mode;
		}

		// Enumeration methods
		public boolean hasMoreElements() {
			return hasNext();
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public Object nextElement() {
			return next();
		}

		// Iterator methods
		public boolean hasNext() {
			return base.hasNext();
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public Object next() {
			last = base.next();

			return (mode == OrderedHashtable.KEYS) ? last : OrderedHashtable.this.get(last);
		}

		/**
		 * DOCUMENT-ME
		 *
		 * @throws IllegalStateException DOCUMENT-ME
		 */
		public void remove() {
			if(last == null) {
				throw new IllegalStateException();
			}

			OrderedHashtable.this.remove(last);
			last = null;
		}
	}

	private static class OHEntry<K,V> implements Map.Entry {
		private Object key;
		private Object value;

		/**
		 * Creates a new OHEntry object.
		 *
		 * 
		 *
		 * @throws NullPointerException DOCUMENT-ME
		 */
		public OHEntry(Map.Entry<K,V> entry) {
			key = entry.getKey();
			value = entry.getValue();

			if((key == null) || (value == null)) {
				throw new NullPointerException();
			}
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 *
		 * 
		 */
		@Override
    public boolean equals(Object o) {
			try {
				OHEntry e2 = (OHEntry) o;
				
				return ((key == null) ? (e2.key == null) : key.equals(e2.key)) &&
					((value == null) ? (e2.value == null) : value.equals(e2.value));
			} catch(ClassCastException e) {
				return false;
			}
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public Object getKey() {
			return key;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public Object getValue() {
			return value;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		@Override
    public int hashCode() {
			return ((key == null) ? 0 : key.hashCode()) ^ ((value == null) ? 0 : value.hashCode());
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 *
		 * 
		 *
		 * @throws NullPointerException DOCUMENT-ME
		 */
		public Object setValue(Object value) {
			if(value == null) {
				throw new NullPointerException();
			}

			Object oldValue = this.value;
			this.value = value;

			return oldValue;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 *
		 * @throws NullPointerException DOCUMENT-ME
		 */
		public void setKey(Object key) {
			if(key == null) {
				throw new NullPointerException();
			}

			this.key = key;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		@Override
    public String toString() {
			return key + "=" + value;
		}
	}
}
