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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import magellan.library.Item;
import magellan.library.Potion;

/**
 * Displays a potion.
 * 
 * @author $Author: $
 * @version $Revision: 259 $
 */
public class PotionNodeWrapper extends DefaultNodeWrapper implements CellObject2, SupportsClipboard {
  private Potion potion = null;
  private String name = null;
  private String postfix = null;
  /*
   * We want Icons besides PotionNodes
   */
  protected List<String> icon;

  private List<GraphicsElement> graphicElements = null;

  /**
   * Creates a new PotionNodeWrapper object.
   */
  public PotionNodeWrapper(Potion p) {
    this(p, null);
  }

  /**
   * Creates a new PotionNodeWrapper object.
   */
  public PotionNodeWrapper(Potion p, String postfix) {
    this(p, p.getName(), postfix);
  }

  /**
   * Creates a new PotionNodeWrapper object.
   * 
   * @param p
   * @param name
   * @param postfix
   */
  public PotionNodeWrapper(Potion p, String name, String postfix) {
    potion = p;
    this.name = name;
    this.postfix = postfix;
  }

  /**
   * @return The corresponding potion
   */
  public Potion getPotion() {
    return potion;
  }

  /**
   * @return potion name + postfix
   */
  @Override
  public String toString() {
    return postfix == null ? name : (name + postfix);
  }

  /**
   * @see magellan.client.swing.tree.CellObject#getIconNames()
   */
  public List<String> getIconNames() {
    if (icon == null) {
      icon = new ArrayList<String>(1);

      if (potion != null) {
        icon.add("items/" + potion.getName());
      }
    }

    return icon;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#emphasized()
   */
  @Override
  public boolean emphasized() {
    return false;
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
    if (potion != null)
      return potion.getName();
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

  /**
   * @see magellan.client.swing.tree.CellObject2#getGraphicsElements()
   */
  public List<GraphicsElement> getGraphicsElements() {
    if (graphicElements == null) {
      GraphicsElement ge = new GraphicsElement(toString(), null, null, "items/" + potion.getName());
      StringBuilder tip = new StringBuilder();
      for (Item ingredient : potion.ingredients()) {
        if (tip.length() > 0) {
          tip.append(", ");
        }
        tip.append(ingredient.getName()).append(" ").append(ingredient.getAmount());
      }
      ge.setTooltip(tip.toString());

      graphicElements = Collections.singletonList(ge);
    }

    return graphicElements;
  }

  public boolean reverseOrder() {
    return false;
  }

  public int getLabelPosition() {
    return graphicElements.size() - 1;
  }
}
