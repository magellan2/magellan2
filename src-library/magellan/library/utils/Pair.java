// class magellan.library.utils.Pair
// created on 26.04.2007
//
// Copyright 2003-2007 by magellan project team
//
// Author : $Author: thoralf $
// $Id: Pair.java 35 2007-05-28 10:24:05Z thoralf $
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

/**
 * This class is a container for a pair of objects.
 * 
 * @author Thoralf Rickert
 * @version 1.0, 26.04.2007
 */
public class Pair<K, V> {
  private K key;
  private V value;

  public Pair() {
  }

  public Pair(K key, V value) {
    this.key = key;
    this.value = value;
  }

  /**
   * Returns the value of key.
   * 
   * @return Returns key.
   */
  public K getKey() {
    return key;
  }

  /**
   * Sets the value of key.
   * 
   * @param key The value for key.
   */
  public void setKey(K key) {
    this.key = key;
  }

  /**
   * Returns the value of value.
   * 
   * @return Returns value.
   */
  public V getValue() {
    return value;
  }

  /**
   * Sets the value of value.
   * 
   * @param value The value for value.
   */
  public void setValue(V value) {
    this.value = value;
  }

}
