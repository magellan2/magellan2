// class magellan.client.swing.tree.DefaultNodeWrapper
// created on Nov 11, 2010
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
package magellan.client.swing.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A default implementation of a node wrapper for convenience.
 * 
 * @author stm
 */
public abstract class DefaultNodeWrapper implements CellObject, SupportsEmphasizing {

  private int warning = 0;
  private List<SupportsEmphasizing> subordinatedElements;

  /**
   * @see magellan.client.swing.tree.CellObject#setWarningLevel(int)
   */
  public int setWarningLevel(int level) {
    propertiesChanged();
    int res = warning;
    warning = level;
    return res;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#getWarningLevel()
   */
  public int getWarningLevel() {
    return warning;
  }

  /**
   * @see magellan.client.swing.tree.SupportsEmphasizing#getSubordinatedElements()
   */
  public List<SupportsEmphasizing> getSubordinatedElements() {
    if (subordinatedElements != null)
      return subordinatedElements;
    else
      return Collections.emptyList();
  }

  /**
   * @see magellan.client.swing.tree.SupportsEmphasizing#addSubordinatedElement(magellan.client.swing.tree.SupportsEmphasizing)
   */
  public void addSubordinatedElement(SupportsEmphasizing newObject) {
    if (subordinatedElements == null) {
      subordinatedElements = new ArrayList<SupportsEmphasizing>(1);
    }
    subordinatedElements.add(newObject);
  }

  /**
   * @see magellan.client.swing.tree.SupportsEmphasizing#emphasized()
   */
  public boolean emphasized() {
    if (subordinatedElements == null)
      return false;

    for (SupportsEmphasizing se : subordinatedElements) {
      if (se.emphasized())
        return true;
    }

    return false;
  }

}
