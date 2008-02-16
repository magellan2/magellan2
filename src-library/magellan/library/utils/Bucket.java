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
 * front of the collection. If an already exisiting element is added to the bucket it is moved to
 * the front of the bucket. If a maximum size is set for the bucket it does not grow beyond this
 * limit by dropping elements at the end of the bucket.
 */
public class Bucket<E> extends AbstractCollection<E> {
	private int maxSize = -1;
	private List<E> data = new LinkedList<E>();

	/**
	 * Creates a Bucket object with unlimited maximum size. I.e. this bucket will never drop
	 * elements by itself.
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
	 * Add an object to the front of the bucket. If the number of elements in the bucket exceeds
	 * the bucket's maximum size an element at the end of the bucket is dropped. If object o is
	 * already contained in the bucket, it is moved to the front of the bucket.
	 *
	 * @param o object to add to the bucket.
	 *
	 * 
	 */
	public boolean add(E o) {
		// don't do anything in this pathologic case
		if(getMaxSize() == 0) {
			return false;
		}

		int index = data.indexOf(o);

		// remove the object from the list, if it is already there
		// and not the first element
		if(index > 0) {
			data.remove(index);
		}

		// add item if it is not already the first element
		if(index != 0) {
			data.add(0, o);
		}

		// enforce size limitation
		if(getMaxSize() > 0) {
			while(data.size() > getMaxSize()) {
				data.remove(data.size() - 1);
			}
		}

		return (index != 0);
	}

	/**
	 * Empty bucket.
	 */
	public void clear() {
		data.clear();
	}

	/**
	 * Check if an object is inside the bucket.
	 *
	 * @param o object to look for.
	 *
	 * @return true if found, false if not.
	 */
	public boolean contains(Object o) {
		return data.contains(o);
	}

	/**
	 * Check if a list of objects is inside the bucket.
	 *
	 * @param c collection of objects to look for.
	 *
	 * @return true if all were found, false if not.
	 */
	public boolean containsAll(Collection<?> c) {
		return data.containsAll(c);
	}

	/**
	 * Check if a given object is the same as the actual bucket.
	 *
	 * @param o object to be compared with.
	 *
	 * @return true if it is the same, false if not.
	 */
	public boolean equals(Object o) {
		if(o == this) {
			return true;
		}
		
		try {
			Bucket b = (Bucket) o;
			
			if((this.getMaxSize() == b.getMaxSize()) && (this.size() == b.size())) {
				Iterator i1 = this.iterator();
				Iterator i2 = b.iterator();
				
				while(i1.hasNext() && i2.hasNext()) {
					if(!i1.next().equals(i2.next())) {
						return false;
					}
				}
				return true;
			}
		} catch(ClassCastException e) {
		}
		return false;
	}

	/**
	 * Check if bucket is empty.
	 *
	 * @return true if empty, false if not.
	 */
	public boolean isEmpty() {
		return data.isEmpty();
	}

	/**
	 * Get an interator for the bucket content.
	 *
	 * @return an iterator for the objects inside the bucket.
	 */
	public Iterator<E> iterator() {
		return data.iterator();
	}

	/**
	 * Remove a given object from the bucket.
	 *
	 * @param o object to remove from the bucket.
	 *
	 * @return true if removal was successful, false if not.
	 */
	public boolean remove(Object o) {
		return data.remove(o);
	}

	/**
	 * Remove a list of items from the bucket.
	 *
	 * @param c collection of all the items to remove.
	 *
	 * @return true if removal was successful, false if not.
	 */
	public boolean removeAll(Collection c) {
		return data.removeAll(c);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param c collection of the items to retain.
	 *
	 * @return true if retaining was successfull, false if not.
	 */
	public boolean retainAll(Collection c) {
		return data.retainAll(c);
	}

	/**
	 * Get the number of items inside the bucket.
	 *
	 * @return the number of objects inside the bucket.
	 */
	public int size() {
		return data.size();
	}

	/**
	 * Get array of all items inside the bucket.
	 *
	 * @return array of items inside the bucket.
	 */
	public Object[] toArray() {
		return data.toArray();
	}

	/**
	 * TODO: don't know... yet. to be commented.
	 *
	 * 
	 *
	 * 
	 */
	public Object[] toArray(Object a[]) {
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
	 * Sets the specified maximum size for this bucket. If there are more elements in the bucket
	 * than maxSize, elements at the end of the bucket are dropped until the size of the bucket
	 * equals maxSize.
	 *
	 * @param maxSize number of items the bucket may contain.
	 */
	public void setMaxSize(int maxSize) {
		while(data.size() > maxSize) {
			data.remove(data.size() - 1);
		}

		this.maxSize = maxSize;
	}

	/**
	 * Create a string representation of the bucket.
	 *
	 * @return string representation of the bucket.
	 */
	public String toString() {
		return data.toString();
	}

	/**
	 * Get a specific item from the bucket.
	 *
	 * @param index number of a specific item in the bucket.
	 *
	 * @return the element at the specified index.
	 */
	public E get(int index) {
		return data.get(index);
	}
}
