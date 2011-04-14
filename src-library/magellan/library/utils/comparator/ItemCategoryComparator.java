// class magellan.client.swing.tree.CategoryComparator
// created on Apr 14, 2011
//
// Copyright 2003-2011 by magellan project team
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
package magellan.library.utils.comparator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import magellan.library.Item;

/**
 * Compares items by their category, items of the same category by type (i.e., by name).
 * 
 * @author stm
 */
public class ItemCategoryComparator implements Comparator<Item> {
  protected static ItemCategoryComparator _instance = new ItemCategoryComparator();

  /**
   * @return An instance of this comparator.
   */
  public static Comparator<? super Item> getInstance() {
    return _instance;
  }

  final List<Integer> catIndices1 = new ArrayList<Integer>();
  final List<Integer> catIndices2 = new ArrayList<Integer>();

  public int compare(Item o1, Item o2) {
    synchronized (catIndices1) {
      if (o1.getItemType().getCategory() != null)
        if (o2.getItemType().getCategory() != null) {
          int compare = o1.getItemType().getCategory().compareTo(o2.getItemType().getCategory());
          if (compare != 0)
            return compare;
        } else
          return -1;
      else if (o2.getItemType().getCategory() != null)
        return 1;
      return o1.getItemType().compareTo(o2.getItemType());
    }
  }
}
