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

/*
 * ItemNodeWrapper.java
 *
 * Created on 16. August 2001, 16:26
 */
package magellan.client.swing.tree;

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import magellan.library.Faction;
import magellan.library.Item;
import magellan.library.Unit;
import magellan.library.utils.Resources;
import magellan.library.utils.StringFactory;
import magellan.library.utils.Units;

/**
 * DOCUMENT ME!
 * 
 * @author Andreas
 * @version 1.0
 */
public class ItemNodeWrapper implements CellObject, SupportsClipboard {
  // Achtung: Das modifizierte Item!
  protected Item modItem;
  protected Unit unit;
  protected String text;

  protected boolean warning = false;

  protected int unmodifiedAmount;

  // protected ItemNodeWrapperPreferencesAdapter adapter=null;
  protected boolean showRegionItemAmount = false;
  protected DetailsNodeWrapperDrawPolicy adapter;
  protected static final java.text.NumberFormat weightNumberFormat = java.text.NumberFormat
      .getNumberInstance();

  /**
   * Creates new ItemNodeWrapper
   */
  public ItemNodeWrapper(Item item) {
    this(null, item, -1);
  }

  /**
   * Creates new ItemNodeWrapper
   */
  public ItemNodeWrapper(Unit unit, Item modItem, int unmodifiedAmount) {
    this.unit = unit;
    this.modItem = modItem;
    this.unmodifiedAmount = unmodifiedAmount;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#emphasized()
   */
  public boolean emphasized() {
    return false;
  }

  /**
   * @return true if the item amount of all units in the region should be displayed.
   */
  public boolean isShowingRegionItemAmount() {
    if (adapter != null)
      return adapter.properties[0];

    return showRegionItemAmount;
  }

  /**
   * Determines if the item amount of all units in the region should be displayed.
   */
  public void setShowRegionItemAmount(boolean b) {
    adapter = null;
    showRegionItemAmount = b;
    propertiesChanged();
  }

  /**
   * sets the warning flag for this node
   * 
   * @param b the new value of the warning flag
   * @return the old value of the warning flag
   */
  public boolean setWarningFlag(boolean b) {
    boolean res = warning;
    warning = b;
    text = null;
    return res;
  }

  // pavkovic 2003.10.01: prevent multiple Lists to be generated for nearly static code
  private static Map<String, List<String>> iconNamesLists = new Hashtable<String, List<String>>();

  /**
   * @return the modified item stored in this node
   */
  public Item getItem() {
    return modItem;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#getIconNames()
   */
  public List<String> getIconNames() {
    String key = modItem.getItemType().getIconName();
    List<String> iconNames = ItemNodeWrapper.iconNamesLists.get(key);

    if (iconNames == null) {
      iconNames = Collections.singletonList(StringFactory.getFactory().intern("items/" + key));
      ItemNodeWrapper.iconNamesLists.put(key, iconNames);
    }

    return iconNames;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#propertiesChanged()
   */
  public void propertiesChanged() {
    text = null;
  }

  /**
   * produces the string describing an item that a unit (or the like) has. The string is:
   * "<amount>[(!!!)] of <regionamount> <itemname>: <weight> GE " for items, the unit already has or
   * "<amount> (<modamount>[,!!!]) of <regionamount> <itemname>: <weight> (<modweight>) GE [(!!!)]"
   * for new items. (!!!) is added if the warning flag is set.
   * 
   * @return the string representation of this item node.
   */
  @Override
  public String toString() {
    if (text == null) {
      boolean showRegion = isShowingRegionItemAmount();

      // do not show region amounts if faction is not privileged
      // TODO: make this configurable
      if ((unit == null) || (unit.getFaction().getTrustLevel() < Faction.TL_PRIVILEGED)) {
        showRegion = false;
      }

      Item item = null;

      if (unit != null) {
        item = unit.getItem(modItem.getItemType());

        if (item == null) {
          item = new Item(modItem.getItemType(), 0);
        }
      }

      StringBuffer nodeText = new StringBuffer();

      if (item == null) {

        // special if unmodifiedAmount is known
        if (unmodifiedAmount > -1 && unmodifiedAmount != modItem.getAmount()) {
          nodeText.append(unmodifiedAmount).append(" (").append(modItem.getAmount()).append(")")
              .append(' ');
        } else {
          nodeText.append(modItem.getAmount()).append(' ');
        }

        if (warning) {
          nodeText.append(" (!!!) ");
        }

        if (showRegion) {
          Item ri = Units.getContainerPrivilegedUnitItem(unit.getRegion(), modItem.getItemType());

          if (ri != null) {
            nodeText.append(Resources.get("tree.itemnodewrapper.node.of")).append(' ').append(
                ri.getAmount()).append(' ');
          }
        }

        nodeText.append(modItem.getName());

        if (modItem.getItemType().getWeight() > 0) {
          float weight =
              (((int) (modItem.getItemType().getWeight() * 100)) * modItem.getAmount()) / 100.0f;
          nodeText.append(": ");
          if (unmodifiedAmount > -1 && unmodifiedAmount != modItem.getAmount()) {
            float unmodifiedWeight =
                (((int) (modItem.getItemType().getWeight() * 100)) * unmodifiedAmount) / 100.0f;
            nodeText.append(ItemNodeWrapper.weightNumberFormat.format(new Float(unmodifiedWeight)));
            nodeText.append(" (").append(
                ItemNodeWrapper.weightNumberFormat.format(new Float(weight))).append(")");
          } else {
            nodeText.append(ItemNodeWrapper.weightNumberFormat.format(new Float(weight)));
          }
          nodeText.append(" " + Resources.get("tree.itemnodewrapper.node.weightunits"));
        }
      } else {
        nodeText.append(item.getAmount()).append(" ");

        if (modItem.getAmount() != item.getAmount()) {
          nodeText.append("(").append(modItem.getAmount());
          if (warning) {
            nodeText.append("!!!) ");
          } else {
            nodeText.append(") ");
          }

        } else {
          if (warning) {
            nodeText.append("(!!!) ");
          }
        }

        if (showRegion) {
          Item ri = Units.getContainerPrivilegedUnitItem(unit.getRegion(), modItem.getItemType());

          if (ri != null) {
            nodeText.append(Resources.get("tree.itemnodewrapper.node.of")).append(' ').append(
                ri.getAmount()).append(' ');
          }
        }

        nodeText.append(modItem.getName());

        if (modItem.getItemType().getWeight() > 0) {
          if (item.getItemType().getWeight() > 0) {
            float weight =
                (((int) (item.getItemType().getWeight() * 100)) * item.getAmount()) / 100.0f;
            nodeText.append(": ").append(
                ItemNodeWrapper.weightNumberFormat.format(new Float(weight)));

            if (modItem.getAmount() != item.getAmount()) {
              float modWeight =
                  (((int) (modItem.getItemType().getWeight() * 100)) * modItem.getAmount()) / 100.0f;
              nodeText.append(" (").append(
                  ItemNodeWrapper.weightNumberFormat.format(new Float(modWeight))).append(")");
            }
            nodeText.append(" " + Resources.get("tree.itemnodewrapper.node.weightunits"));
          }
        }
      }

      text = nodeText.toString();
    }

    return text;
  }

  protected NodeWrapperDrawPolicy createItemDrawPolicy(Properties settings, String prefix) {
    return new DetailsNodeWrapperDrawPolicy(1, null, settings, prefix, new String[][] { {
        "units.showRegionItemAmount", "true" } }, new String[] { "prefs.region.text" }, 0,
        "tree.itemnodewrapper.");
  }

  /**
   * @see magellan.client.swing.tree.SupportsClipboard#getClipboardValue()
   */
  public String getClipboardValue() {
    if (modItem != null)
      return modItem.getName();
    else
      return toString();
  }

  /**
   * @see magellan.client.swing.tree.CellObject#init(java.util.Properties,
   *      magellan.client.swing.tree.NodeWrapperDrawPolicy)
   */
  public NodeWrapperDrawPolicy init(Properties settings, NodeWrapperDrawPolicy adapter) {
    return init(settings, "ItemNodeWrapper", adapter);
  }

  /**
   * @see magellan.client.swing.tree.CellObject#init(java.util.Properties, java.lang.String,
   *      magellan.client.swing.tree.NodeWrapperDrawPolicy)
   */
  public NodeWrapperDrawPolicy init(Properties settings, String prefix,
      NodeWrapperDrawPolicy adapter) {
    if (adapter == null) {
      adapter = createItemDrawPolicy(settings, prefix);
    }

    adapter.addCellObject(this);
    this.adapter = (DetailsNodeWrapperDrawPolicy) adapter;

    return adapter;
  }
}
