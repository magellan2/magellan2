// class magellan.library.Addeable
// created on 12.04.2009
//
// Copyright 2003-2009 by magellan project team
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
package magellan.library;

import java.util.List;

/**
 * This interface marks an object as addeable. Addeable objects have "attributes", i.e. Name/Value
 * pairs.
 * 
 * @author Thoralf Rickert
 * @version 1.0, 12.04.2009
 */
public interface Addeable {
  /**
   * Tags this object with the given key/value pair.
   */
  public void addAttribute(String key, String value);

  /**
   * Returns all keys that have been added to this object.
   * 
   * @return a list of keys that have been added to this object.
   */
  public List<String> getAttributeKeys();

  /**
   * Returns the value for the given key.
   * 
   * @return the value for the given key or <code>null</code> if no value has been added for the
   *         key.
   */
  public String getAttribute(String key);

  /**
   * Returns <code>true</code> if a value has been added for the given key.
   * 
   * @return <code>true</code> if a value has been added for the given key.
   */
  public boolean containsAttribute(String key);

  /**
   * Returns the number of keys that have been added.
   */
  public int getAttributeSize();
}
