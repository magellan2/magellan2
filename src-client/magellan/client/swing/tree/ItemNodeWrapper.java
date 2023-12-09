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

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import magellan.client.EMapDetailsPanel.ShowItems;
import magellan.library.Item;
import magellan.library.Unit;
import magellan.library.utils.Resources;
import magellan.library.utils.StringFactory;
import magellan.library.utils.Units;

/**
 * A wrapper for items. Created on 16. August 2001, 16:26
 *
 * @author Andreas
 * @version 1.0
 */
public class ItemNodeWrapper extends DefaultNodeWrapper implements SupportsClipboard {
  // Achtung: Das modifizierte Item!
  protected Units.StatItem modItem;
  protected Unit unit;
  protected String text;

  protected long unmodifiedAmount;

  // protected ItemNodeWrapperPreferencesAdapter adapter=null;
  protected ShowItems showRegionItemAmount = ShowItems.SHOW_PRIVILEGED_FACTIONS;
  protected DetailsNodeWrapperDrawPolicy adapter;
  protected static final java.text.NumberFormat weightNumberFormat = java.text.NumberFormat
      .getNumberInstance();

  /**
   * Creates new ItemNodeWrapper
   */
  public ItemNodeWrapper(Units.StatItem item) {
    this(null, item, -1);
  }

  /**
   * Creates new ItemNodeWrapper
   */
  public ItemNodeWrapper(Unit unit, Units.StatItem modItem, long unmodifiedAmount) {
    this.unit = unit;
    this.modItem = modItem;
    this.unmodifiedAmount = unmodifiedAmount;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#emphasized()
   */
  @Override
  public boolean emphasized() {
    return false;
  }

  /**
   * @return true if the item amount of all units in the region should be displayed.
   */
  protected ShowItems isShowingRegionItemAmount() {
    if (adapter != null) {
      ShowItems result = ShowItems.SHOW_NONE;
      if (adapter.properties[0]) {
        result = ShowItems.SHOW_MY_FACTION;
      }
      if (adapter.properties.length > 1 && adapter.properties[1]) {
        result = ShowItems.SHOW_PRIVILEGED_FACTIONS;
      }
      if (adapter.properties.length > 2 && adapter.properties[2]) {
        result = ShowItems.SHOW_ALL_FACTIONS;
      }
      return result;
    }

    return showRegionItemAmount;
  }

  /**
   * Determines if the item amount of all units in the region should be displayed.
   */
  protected void setShowRegionItemAmount(ShowItems newValue) {
    adapter = null;
    showRegionItemAmount = newValue;
    propertiesChanged();
  }

  // pavkovic 2003.10.01: prevent multiple Lists to be generated for nearly static code
  private static Map<String, List<String>> iconNamesLists = new Hashtable<String, List<String>>();

  /**
   * @return the modified item stored in this node
   */
  public Units.StatItem getItem() {
    return modItem;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#getIconNames()
   */
  public List<String> getIconNames() {
    String key = modItem.getItemType().getIcon();
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
   * "&lt;amount&gt;[(!!!)] of &lt;regionamount&gt; &lt;itemname&gt;: &lt;weight&gt; GE " for items, the unit already
   * has or
   * "&lt;amount&gt; (&lt;modamount&gt;[,!!!]) of &lt;regionamount&gt; &lt;itemname&gt;: &lt;weight&gt;
   * (&lt;modweight&gt;) GE [(!!!)]"
   * for new items. (!!!) is added if the warning flag is set.
   *
   * @return the string representation of this item node.
   */
  @Override
  public String toString() {
    if (text == null) {
      ShowItems showRegion = isShowingRegionItemAmount();

      // do not show region amounts if faction is not privileged
      // (stm-2011-12-30 who wants this? deactivated:
      // || (unit.getFaction().getTrustLevel() < Faction.TL_PRIVILEGED)
      if ((unit == null)) {
        showRegion = ShowItems.SHOW_NONE;
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

        if (getWarningLevel() >= CellObject.L_WARNING) {
          nodeText.append(" (!!!) ");
        }

        Units.StatItem ri = null;
        if (showRegion == ShowItems.SHOW_PRIVILEGED_FACTIONS) {
          ri = Units.getContainerPrivilegedUnitItem(unit.getRegion(), modItem.getItemType());
        } else if (showRegion == ShowItems.SHOW_ALL_FACTIONS) {
          ri = Units.getContainerAllUnitItem(unit.getRegion(), modItem.getItemType());
        } else if (showRegion == ShowItems.SHOW_MY_FACTION) {
          ri = Units.getContainerFactionUnitItem(unit.getRegion(), unit, modItem.getItemType());
        }
        if (ri != null) {
          nodeText.append(Resources.get("tree.itemnodewrapper.node.of")).append(' ').append(
              ri.getAmount()).append(' ');
        }

        nodeText.append(modItem.getName());

        if (modItem.getItemType().getWeight() > 0) {
          float weight =
              (((long) (modItem.getItemType().getWeight() * 100)) * modItem.getAmount()) / 100.0f;
          nodeText.append(": ");
          if (unmodifiedAmount > -1 && unmodifiedAmount != modItem.getAmount()) {
            float unmodifiedWeight =
                (((long) (modItem.getItemType().getWeight() * 100)) * unmodifiedAmount) / 100.0f;
            nodeText.append(ItemNodeWrapper.weightNumberFormat.format(Float
                .valueOf(unmodifiedWeight)));
            nodeText.append(" (").append(
                ItemNodeWrapper.weightNumberFormat.format(Float.valueOf(weight))).append(")");
          } else {
            nodeText.append(ItemNodeWrapper.weightNumberFormat.format(Float.valueOf(weight)));
          }
          nodeText.append(" " + Resources.get("tree.itemnodewrapper.node.weightunits"));
        }
      } else {
        nodeText.append(item.getAmount()).append(" ");

        if (modItem.getAmount() != item.getAmount()) {
          nodeText.append("(").append(modItem.getAmount());
          if (getWarningLevel() >= CellObject.L_WARNING) {
            nodeText.append("!!!) ");
          } else {
            nodeText.append(") ");
          }

        } else {
          if (getWarningLevel() >= CellObject.L_WARNING) {
            nodeText.append("(!!!) ");
          }
        }

        Units.StatItem ri = null;
        if (showRegion == ShowItems.SHOW_PRIVILEGED_FACTIONS) {
          ri = Units.getContainerPrivilegedUnitItem(unit.getRegion(), modItem.getItemType());
        } else if (showRegion == ShowItems.SHOW_ALL_FACTIONS) {
          ri = Units.getContainerAllUnitItem(unit.getRegion(), modItem.getItemType());
        } else if (showRegion == ShowItems.SHOW_MY_FACTION) {
          ri = Units.getContainerFactionUnitItem(unit.getRegion(), unit, modItem.getItemType());
        }
        if (ri != null) {
          nodeText.append(Resources.get("tree.itemnodewrapper.node.of")).append(' ').append(
              ri.getAmount()).append(' ');
        }

        nodeText.append(modItem.getName());

        if (modItem.getItemType().getWeight() > 0) {
          if (item.getItemType().getWeight() > 0) {
            float weight =
                (((long) (item.getItemType().getWeight() * 100)) * item.getAmount()) / 100.0f;
            nodeText.append(": ").append(
                ItemNodeWrapper.weightNumberFormat.format(Float.valueOf(weight)));

            if (modItem.getAmount() != item.getAmount()) {
              float modWeight =
                  (((long) (modItem.getItemType().getWeight() * 100)) * modItem.getAmount()) / 100.0f;
              nodeText.append(" (").append(
                  ItemNodeWrapper.weightNumberFormat.format(Float.valueOf(modWeight))).append(")");
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
    // return new DetailsNodeWrapperDrawPolicy(1, null, settings, prefix, new String[][] { {
    // "units.showRegionItemAmount", "true" } }, new String[] { "prefs.region.text" }, 0,
    // "tree.itemnodewrapper.");
    return new DetailsNodeWrapperDrawPolicy(3, null, settings, prefix, new String[][] {
        { "units.showMyTotalAmount", "false" }, { "units.showPrivilegedTotalAmount", "true" },
        { "units.showAllTotalAmount", "false" } }, new String[] { "prefs.total.my.text",
            "prefs.total.privileged.text", "prefs.total.all.text" }, 0, "tree.itemnodewrapper.");
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
  public NodeWrapperDrawPolicy init(Properties settings, NodeWrapperDrawPolicy anAdapter) {
    return init(settings, "ItemNodeWrapper", anAdapter);
  }

  /**
   * @see magellan.client.swing.tree.CellObject#init(java.util.Properties, java.lang.String,
   *      magellan.client.swing.tree.NodeWrapperDrawPolicy)
   */
  public NodeWrapperDrawPolicy init(Properties settings, String prefix,
      NodeWrapperDrawPolicy anAdapter) {
    if (anAdapter == null) {
      anAdapter = createItemDrawPolicy(settings, prefix);
    }

    anAdapter.addCellObject(this);
    adapter = (DetailsNodeWrapperDrawPolicy) anAdapter;

    return anAdapter;
  }

}
