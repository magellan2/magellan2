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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import magellan.client.swing.context.ContextFactory;
import magellan.library.Unit;
import magellan.library.relation.UnitRelation;

/**
 * A node that represents a unit relation and is wrapped around another node.
 * 
 * @author stm
 */
public class UnitRelationNodeWrapper extends DefaultNodeWrapper implements SupportsClipboard, Changeable {

  private UnitRelation relation;
  private CellObject innerNode;
  private ContextFactory contextFactory;
  private Unit owner;
  private String addIcon;

  /**
   * Creates a node representing a relation to another unit.
   * 
   * @param owner
   * @param rel
   * @param innerNode Another node that this node wraps around
   */
  public UnitRelationNodeWrapper(Unit owner, UnitRelation rel, CellObject innerNode) {
    relation = rel;
    this.innerNode = innerNode;
    this.owner = owner;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#emphasized()
   */
  public boolean emphasized() {
    return innerNode.emphasized();
  }

  /**
   * @see magellan.client.swing.tree.CellObject#getIconNames()
   */
  public Collection<String> getIconNames() {
    if (addIcon != null) {
      Collection<String> res = new ArrayList<String>(innerNode.getIconNames().size() + 1);
      res = new ArrayList<String>(innerNode.getIconNames().size());
      res.add(addIcon);
      return res;
    } else
      return innerNode.getIconNames();
  }

  /**
   * @see magellan.client.swing.tree.CellObject#init(java.util.Properties,
   *      magellan.client.swing.tree.NodeWrapperDrawPolicy)
   */
  public NodeWrapperDrawPolicy init(Properties settings, NodeWrapperDrawPolicy adapter) {
    return null;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#init(java.util.Properties, java.lang.String,
   *      magellan.client.swing.tree.NodeWrapperDrawPolicy)
   */
  public NodeWrapperDrawPolicy init(Properties settings, String prefix,
      NodeWrapperDrawPolicy adapter) {
    return null;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#propertiesChanged()
   */
  public void propertiesChanged() {
    innerNode.propertiesChanged();
  }

  /**
   * @see magellan.client.swing.tree.SupportsClipboard#getClipboardValue()
   */
  public String getClipboardValue() {
    return innerNode.toString();
  }

  /**
   * @see magellan.client.swing.tree.Changeable#getArgument()
   */
  public Object getArgument() {
    return relation;
  }

  /**
   * @see magellan.client.swing.tree.Changeable#getChangeModes()
   */
  public int getChangeModes() {
    return Changeable.CONTEXT_MENU;
  }

  /**
   * @see magellan.client.swing.tree.Changeable#getContextFactory()
   */
  public ContextFactory getContextFactory() {
    return contextFactory;
  }

  public void setContextFactory(ContextFactory factory) {
    contextFactory = factory;
  }

  /**
   * Returns the inner nodes text.
   */
  @Override
  public String toString() {
    return innerNode.toString();
  }

  /**
   * @return the inner node
   */
  public CellObject getInnerNode() {
    return innerNode;
  }

  /**
   * Returns the value of owner.
   * 
   * @return Returns owner.
   */
  public Unit getOwner() {
    return owner;
  }

  /**
   * Returns the value of addIcon.
   * 
   * @return Returns addIcon.
   */
  public String getAdditionalIcons() {
    return addIcon;
  }

  /**
   * Sets the value of addIcon.
   * 
   * @param addIcon The value for addIcon.
   */
  public void addAdditionalIcon(String addIcon) {
    this.addIcon = addIcon;
  }

}
