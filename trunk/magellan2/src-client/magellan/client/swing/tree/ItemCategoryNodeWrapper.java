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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import magellan.library.rules.ItemCategory;

/**
 * DOCUMENT ME!
 * 
 * @author Ulrich Küster
 */
public class ItemCategoryNodeWrapper implements CellObject {
  private int amount = -1;
  private int unmodifiedAmount = -1;
  private ItemCategory cat = null;
  private String setCatName = null;
  protected List<String> icons;
  protected List<String> returnIcons;

  protected DetailsNodeWrapperDrawPolicy adapter;

  /**
   * Creates a new ItemCategoryNodeWrapper object (with number of items).
   */
  public ItemCategoryNodeWrapper(ItemCategory category, int amount) {
    this.amount = amount;
    cat = category;
  }

  /**
   * Creates a new ItemCategoryNodeWrapper object with explicit name.
   */
  public ItemCategoryNodeWrapper(ItemCategory category, int amount, String _catName) {
    this.amount = amount;
    cat = category;
    setCatName = _catName;
  }

  /**
   * @param i
   */
  public void setAmount(int i) {
    amount = i;
  }

  /**
   * @return The corresponding category.
   */
  public ItemCategory getItemCategory() {
    return cat;
  }

  /**
   * @return the category name and amount (and unmodified amount)
   */
  @Override
  public String toString() {
    if (amount == -1) {
      if (setCatName == null)
        return cat.toString();
      else
        return setCatName;
    } else {
      String amountInfo = "";
      if (unmodifiedAmount == -1 || unmodifiedAmount == amount) {
        amountInfo = ": " + amount;
      } else {
        amountInfo = ": " + unmodifiedAmount + " (" + amount + ")";
      }
      if (setCatName == null)
        return cat.toString() + amountInfo;
      else
        return setCatName + amountInfo;
    }
  }

  /**
   * @see magellan.client.swing.tree.CellObject#init(java.util.Properties,
   *      magellan.client.swing.tree.NodeWrapperDrawPolicy)
   */
  public NodeWrapperDrawPolicy init(Properties settings, NodeWrapperDrawPolicy adapter) {
    return init(settings, "SimpleNodeWrapper", adapter);
  }

  /**
   * @see magellan.client.swing.tree.CellObject#init(java.util.Properties, java.lang.String,
   *      magellan.client.swing.tree.NodeWrapperDrawPolicy)
   */
  public NodeWrapperDrawPolicy init(Properties settings, String prefix,
      NodeWrapperDrawPolicy adapter) {
    if (adapter == null) {
      adapter = createSimpleDrawPolicy(settings, prefix);
    }

    adapter.addCellObject(this);
    this.adapter = (DetailsNodeWrapperDrawPolicy) adapter;

    return adapter;
  }

  /**
   * DOCUMENT-ME
   */
  protected NodeWrapperDrawPolicy createSimpleDrawPolicy(Properties settings, String prefix) {
    return new DetailsNodeWrapperDrawPolicy(1, null, settings, prefix, new String[][] { {
        "simple.showIcon", "true" } }, new String[] { "icons.text" }, 0,
        "tree.itemcategorynodewrapper.");
  }

  /**
   * @param icons
   */
  public void setIcons(Collection<String> icons) {
    this.icons = null;
    if (icons != null) {
      this.icons = new ArrayList<String>(icons);
    }
  }

  /**
   * @param icons
   */
  public void setIcons(Map<?, ?> icons) {
    this.icons = null;
    if (icons != null) {
      this.icons = new ArrayList<String>(icons.size());

      for (Object name : icons.values()) {
        this.icons.add(name.toString());
      }
    }
  }

  /**
   * @param icons
   */
  public void setIcons(Object icons) {
    this.icons = null;
    this.icons = Collections.singletonList(icons.toString());
  }

  /**
   * @see magellan.client.swing.tree.CellObject#emphasized()
   */
  public boolean emphasized() {
    return false;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#propertiesChanged()
   */
  public void propertiesChanged() {
    returnIcons = null;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#getIconNames()
   */
  public List<String> getIconNames() {
    if (returnIcons == null) {

      returnIcons = icons;

    }

    return returnIcons;
  }

  /**
   * Returns the value of unmodifiedAmount.
   * 
   * @return Returns unmodifiedAmount.
   */
  public int getUnmodifiedAmount() {
    return unmodifiedAmount;
  }

  /**
   * Sets the value of unmodifiedAmount.
   * 
   * @param unmodifiedAmount The value for unmodifiedAmount.
   */
  public void setUnmodifiedAmount(int unmodifiedAmount) {
    this.unmodifiedAmount = unmodifiedAmount;
  }

}
