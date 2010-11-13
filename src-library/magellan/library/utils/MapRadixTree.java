// class magellan.library.utils.MapRadixTree
// created on Aug 3, 2010
//
// Copyright 2003-2010 by magellan project team
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

import java.util.Map;

import ds.tree.RadixTree;

/**
 * A radix tree that implements the {@link Map} interface.
 * 
 * @author stm
 */
public interface MapRadixTree<T> extends RadixTree<T>, Map<String, T> {

  /**
   * Returns a map containing all entries whose keys are have the specified key as a prefix. The
   * returned result is <i>not</i> backed by this maps. Changes made to it are independent of this
   * map.
   * 
   * @param key
   * @param recordLimit The limit for the results. The result map contains at most this many
   *          entries.
   */
  public Map<String, T> searchPrefixMap(String key, int recordLimit);

}
