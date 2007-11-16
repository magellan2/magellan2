// class magellan.client.swing.MagellanFocusTraversalPolicy
// created on 11.08.2007
//
// Copyright 2003-2007 by magellan project team
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
package magellan.client.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.util.Vector;

/**
 * This is a small utility for a focus traversal policy based on
 * a list of components. The next focus element is based on the
 * current index of the corresponding element in the list.
 *
 * @author Thoralf Rickert
 * @version 1.0, 11.08.2007
 */
public class MagellanFocusTraversalPolicy extends FocusTraversalPolicy {
  private Vector<Component> order;
  
  public MagellanFocusTraversalPolicy(Vector<Component> components) {
    this.order = components;
  }
  
  /**
   * @see java.awt.FocusTraversalPolicy#getComponentAfter(java.awt.Container, java.awt.Component)
   */
  @Override
  public Component getComponentAfter(Container container, Component aComponent) {
    int idx = (order.indexOf(aComponent) + 1) % order.size();
    return order.get(idx);
  }

  /**
   * @see java.awt.FocusTraversalPolicy#getComponentBefore(java.awt.Container, java.awt.Component)
   */
  @Override
  public Component getComponentBefore(Container container, Component aComponent) {
    int idx = order.indexOf(aComponent) - 1;
    if (idx < 0) {
        idx = order.size() - 1;
    }
    return order.get(idx);
  }

  /**
   * @see java.awt.FocusTraversalPolicy#getDefaultComponent(java.awt.Container)
   */
  @Override
  public Component getDefaultComponent(Container container) {
    return order.get(0);
  }

  /**
   * @see java.awt.FocusTraversalPolicy#getFirstComponent(java.awt.Container)
   */
  @Override
  public Component getFirstComponent(Container container) {
    return order.get(0);
  }

  /**
   * @see java.awt.FocusTraversalPolicy#getLastComponent(java.awt.Container)
   */
  @Override
  public Component getLastComponent(Container container) {
    return order.lastElement();
  }
  
}
