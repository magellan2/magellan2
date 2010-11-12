// class magellan.client.swing.tree.RelationNodeWrapper
// created on Jun 13, 2009
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
package magellan.client.swing.tree;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import magellan.client.swing.context.ContextFactory;
import magellan.library.Unit;
import magellan.library.relation.UnitRelation;

// FIXME doesn't work well
// FIXME explain CellObject2
public class UnitRelationNodeWrapper2 extends UnitRelationNodeWrapper implements CellObject2 {

  private UnitRelation relation;
  private CellObject2 innerNode;
  private ContextFactory contextFactory;

  public UnitRelationNodeWrapper2(Unit owner, UnitRelation rel, CellObject2 innerNode) {
    super(owner, rel, null);
    this.innerNode = innerNode;
    relation = rel;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#emphasized()
   */
  @Override
  public boolean emphasized() {
    return innerNode.emphasized();
  }

  /**
   * @see magellan.client.swing.tree.CellObject#getIconNames()
   */
  @Override
  public Collection<String> getIconNames() {
    return super.getIconNames();
  }

  /**
   * @see magellan.client.swing.tree.CellObject#init(java.util.Properties,
   *      magellan.client.swing.tree.NodeWrapperDrawPolicy)
   */
  @Override
  public NodeWrapperDrawPolicy init(Properties settings, NodeWrapperDrawPolicy adapter) {
    return null;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#init(java.util.Properties, java.lang.String,
   *      magellan.client.swing.tree.NodeWrapperDrawPolicy)
   */
  @Override
  public NodeWrapperDrawPolicy init(Properties settings, String prefix,
      NodeWrapperDrawPolicy adapter) {
    return null;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#propertiesChanged()
   */
  @Override
  public void propertiesChanged() {
    innerNode.propertiesChanged();
  }

  /**
   * @see magellan.client.swing.tree.SupportsClipboard#getClipboardValue()
   */
  @Override
  public String getClipboardValue() {
    return innerNode.toString();
  }

  /**
   * @see magellan.client.swing.tree.Changeable#getArgument()
   */
  @Override
  public Object getArgument() {
    return relation;
  }

  /**
   * @see magellan.client.swing.tree.Changeable#getChangeModes()
   */
  @Override
  public int getChangeModes() {
    return Changeable.CONTEXT_MENU;
  }

  /**
   * @see magellan.client.swing.tree.Changeable#getContextFactory()
   */
  @Override
  public ContextFactory getContextFactory() {
    return contextFactory;
  }

  @Override
  public void setContextFactory(ContextFactory factory) {
    contextFactory = factory;
  }

  /**
   * Returns the text.
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return innerNode.toString();
  }

  public List<GraphicsElement> getGraphicsElements() {
    return innerNode.getGraphicsElements();
  }

  /**
   * @see magellan.client.swing.tree.CellObject2#getLabelPosition()
   */
  public int getLabelPosition() {
    return innerNode.getLabelPosition();
  }

  public boolean reverseOrder() {
    return innerNode.reverseOrder();
  }

  @Override
  public CellObject getInnerNode() {
    return innerNode;
  }

}
