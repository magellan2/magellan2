// class magellan.library.utils.CollectionFactory
// created on Nov 26, 2010
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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Creates collections. Used to easily change collection implementations.
 * 
 * @author stm
 * @version 1.0, Nov 26, 2010
 */
public class CollectionFactory {

  public static <K, V> Map<K, V> createMap(int i, float f) {
    return new LinkedHashMap<K, V>(i, f);
  }

  public static <K, V> Map<K, V> createMap(int i) {
    return new LinkedHashMap<K, V>(i);
  }

  public static <K, V> Map<K, V> createMap() {
    return new LinkedHashMap<K, V>();
  }

  public static <K, V> Map<K, V> createOrderedMap(int i, float f) {
    return new LinkedHashMap<K, V>(i, f);
  }

  public static <K, V> Map<K, V> createOrderedMap(int i) {
    return new LinkedHashMap<K, V>(i);
  }

  public static <K, V> Map<K, V> createOrderedMap() {
    return new LinkedHashMap<K, V>();
  }

  public static <K, V> Map<K, V> createSyncMap(int i, float f) {
    return Collections.synchronizedMap(new LinkedHashMap<K, V>(i, f));
  }

  public static <K, V> Map<K, V> createSyncMap(int i) {
    return Collections.synchronizedMap(new LinkedHashMap<K, V>(i));
  }

  public static <K, V> Map<K, V> createSyncMap() {
    return Collections.synchronizedMap(new LinkedHashMap<K, V>());
  }

  public static <K, V> Map<K, V> createSyncOrderedMap(int i, float f) {
    return Collections.synchronizedMap(new LinkedHashMap<K, V>(i, f));
  }

  public static <K, V> Map<K, V> createSyncOrderedMap(int i) {
    return Collections.synchronizedMap(new LinkedHashMap<K, V>(i));
  }

  public static <K, V> Map<K, V> createSyncOrderedMap() {
    return Collections.synchronizedMap(new LinkedHashMap<K, V>());
  }

  public static <K, V> Map<K, V> createSyncOrderedMap(Map<K, V> map) {
    return Collections.synchronizedMap(new LinkedHashMap<K, V>(map));
  }

}
