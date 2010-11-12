/*
 *  Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe,
 *                          Stefan Goetz, Sebastian Pappert,
 *                          Klaas Prause, Enno Rehling,
 *                          Sebastian Tusk, Ulrich Kuester,
 *                          Ilja Pavkovic
 *
 * This file is part of the Eressea Java Code Base, see the
 * file LICENSING for the licensing information applying to
 * this file.
 *
 */

package magellan.client.swing.tree;

import java.awt.Image;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.UIManager;

import magellan.library.Group;

/**
 * A node that represents a Group.
 * 
 * @author Andreas, Ulrich Küster
 */
public class GroupNodeWrapper extends DefaultNodeWrapper implements CellObject2, SupportsClipboard {
  protected Group group;
  protected List<GraphicsElement> GE;
  protected static Icon icon;
  private int amount = -1;

  /**
   * Creates new GroupNodeWrapper
   */
  public GroupNodeWrapper(Group g) {
    group = g;

    if (GroupNodeWrapper.icon == null) {
      GroupNodeWrapper.icon = UIManager.getIcon("Tree.closedIcon");
    }

    if (g != null) {
      setAmount(g.units().size());
    }
  }

  /**
   * @return the corresponding group
   */
  public Group getGroup() {
    return group;
  }

  /**
   * @see magellan.client.swing.tree.CellObject2#getGraphicsElements()
   */
  public List<GraphicsElement> getGraphicsElements() {
    if (GE == null) {
      GraphicsElement ge = new GroupGraphicsElement(toString(), GroupNodeWrapper.icon, null, null);
      Tag2Element.start(group);
      Tag2Element.apply(ge);
      ge.setType(GraphicsElement.MAIN);

      GE = Collections.singletonList(ge);
    }

    return GE;
  }

  /**
   * @see magellan.client.swing.tree.CellObject2#getLabelPosition()
   */
  public int getLabelPosition() {
    return GE.size() - 1;
  }

  private class GroupGraphicsElement extends GraphicsElement {
    /**
     * Creates a new GroupGraphicsElement object.
     */
    public GroupGraphicsElement(Object object, Icon icon, Image image, String imageName) {
      super(object, icon, image, imageName);
    }

    /**
     * @see magellan.client.swing.tree.GraphicsElement#isEmphasized()
     */
    @Override
    public boolean isEmphasized() {
      return emphasized();
    }
  }

  /**
   * Never reversed
   * 
   * @see magellan.client.swing.tree.CellObject2#reverseOrder()
   */
  public boolean reverseOrder() {
    return false;
  }

  /**
   * Changes the amount (of persons)
   * 
   * @param i
   */
  public void setAmount(int i) {
    amount = i;
  }

  /**
   * @return the amount (of persons)
   */
  public int getAmount() {
    return amount;
  }

  /**
   * @return "Group name: amount"
   */
  @Override
  public String toString() {
    if (amount == -1)
      return group.toString();
    else
      return group.toString() + ": " + amount;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#getIconNames()
   */
  public List<String> getIconNames() {
    return null;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#propertiesChanged()
   */
  public void propertiesChanged() {
    // no changeable properties
  }

  /**
   * @see magellan.client.swing.tree.SupportsClipboard#getClipboardValue()
   */
  public String getClipboardValue() {
    if (group != null)
      return group.getName();
    else
      return toString();
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

}
