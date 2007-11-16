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

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A class implementing an enumeration through an iterator.
 */
public class IteratorEnumeration<E> implements Enumeration<E> {
	private Iterator<E> iter = null;

	/**
	 * Creates a new IteratorEnumeration providing an enumeration interface to the specified
	 * iterator.
	 *
	 * @param iterator the Iterator to enumerate. If iterator is null the hasMoreElements() method
	 * 		  always returns false and the nextElement() method always throws an
	 * 		  NoSuchElementException.
	 */
	public IteratorEnumeration(Iterator<E> iterator) {
		this.iter = iterator;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean hasMoreElements() {
		return (iter == null) ? false : iter.hasNext();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * @throws NoSuchElementException DOCUMENT-ME
	 */
	public E nextElement() throws NoSuchElementException {
		if(iter == null) {
			throw new NoSuchElementException("Empty enumeration");
		}

		return iter.next();
	}
}
