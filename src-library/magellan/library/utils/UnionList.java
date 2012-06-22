// class magellan.library.rules.CompoundCollection
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

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

/**
 * A list which acts as the union of two collections. Unmodifiable.
 */
public class UnionList<T> extends AbstractList<T> {

  private List<? extends T>[] collections;
  private int size;

  /**
   * Creates a new list that behaves like a concatenation of the given collections. Changes to the
   * given collections are reflected in the union collection.
   * 
   * @param collections
   */
  @SuppressWarnings("unchecked")
  public UnionList(List<? extends T>... collections) {
    if (collections == null)
      throw new NullPointerException();
    this.collections = new List[collections.length];
    for (int i = 0; i < collections.length; ++i) {
      this.collections[i] = collections[i];
      size += collections[i].size();
    }

  }

  /**
   * @see java.util.AbstractCollection#iterator()
   */
  @Override
  public Iterator<T> iterator() {
    return new CompoundIterator<T>(collections);
  }

  /**
   * @see java.util.AbstractCollection#size()
   */
  @Override
  public int size() {
    return size;
  }

  /**
   * @see java.util.AbstractList#get(int)
   */
  @Override
  public T get(int index) {
    for (int i = 0; i < collections.length; ++i) {
      if (index < collections[i].size())
        return collections[i].get(index);
      index -= collections[i].size();
    }
    throw new IndexOutOfBoundsException();
  }

}