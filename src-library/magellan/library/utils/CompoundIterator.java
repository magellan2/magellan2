// class magellan.library.rules.CompoundIterator
// created on Jun 21, 2012
//
// Copyright 2003-2012 by magellan project team
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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc., 
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
package magellan.library.utils;

import java.util.Collection;
import java.util.Iterator;

/**
 * An iterator that iterates over the union of two collections. Unmodifiable.
 */
public class CompoundIterator<T> implements Iterator<T> {

  private Iterator<T>[] iterators;
  private int currentIterator;

  /**
   * @param collections
   */
  @SuppressWarnings("unchecked")
  public CompoundIterator(Collection<? extends T>... collections) {
    iterators = new Iterator[collections.length];
    for (int i = 0; i < collections.length; ++i) {
      this.iterators[i] = (Iterator<T>) collections[i].iterator();
    }
  }

  public boolean hasNext() {
    if (iterators[currentIterator].hasNext())
      return true;

    while (++currentIterator < iterators.length) {
      if (iterators[currentIterator].hasNext())
        return true;
    }
    currentIterator--;
    return false;
  }

  public T next() {
    if (iterators[currentIterator].hasNext())
      return iterators[currentIterator].next();

    while (++currentIterator < iterators.length) {
      if (iterators[currentIterator].hasNext())
        return iterators[currentIterator].next();
    }
    currentIterator--;
    return iterators[currentIterator].next();
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

}