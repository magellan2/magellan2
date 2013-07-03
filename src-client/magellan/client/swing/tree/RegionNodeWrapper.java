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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.Icon;

import magellan.library.Region;
import magellan.library.utils.StringFactory;

/**
 * A wrapper for regions.
 * 
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class RegionNodeWrapper extends DefaultNodeWrapper implements CellObject2, SupportsClipboard {
  private Region region = null;
  private List<GraphicsElement> graphicElements = null;
  private int amount = Integer.MIN_VALUE;

  /**
   * Creates a new RegionNodeWrapper object.
   */
  public RegionNodeWrapper(Region r) {
    this(r, Integer.MIN_VALUE);
  }

  /**
   * Creates a new RegionNodeWrapper object with amount (of persons). {@link Integer#MIN_VALUE}
   * disables amount display
   */
  public RegionNodeWrapper(Region r, int amount) {
    region = r;
    this.amount = amount;
  }

  /**
   * @return the corresponding region
   */
  public Region getRegion() {
    return region;
  }

  /**
   * Sets the amount (of persons)
   * 
   * @param amount {@link Integer#MIN_VALUE} disables amount display
   */
  public void setAmount(int amount) {
    this.amount = amount;
  }

  /**
   * @return the amount (of persons)
   */
  public int getAmount() {
    return amount;
  }

  /**
   * @return "region name: amount"
   */
  @Override
  public String toString() {
    return (amount > Integer.MIN_VALUE) ? (region.toString() + ": " + amount) : region.toString();
  }

  // pavkovic 2003.10.01: prevent multiple Lists to be generated for nearly static code
  private static Map<Object, List<String>> iconNamesLists = new Hashtable<Object, List<String>>();

  /**
   * @see magellan.client.swing.tree.CellObject#getIconNames()
   */
  public List<String> getIconNames() {
    Object key = region.getType().getID();
    List<String> iconNames = RegionNodeWrapper.iconNamesLists.get(key);

    if (iconNames == null) {
      // in this situation init the region
      region.refreshUnitRelations();

      iconNames = Collections.singletonList(StringFactory.getFactory().intern(key.toString()));
      RegionNodeWrapper.iconNamesLists.put(key, iconNames);
    }

    return iconNames;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#propertiesChanged()
   */
  public void propertiesChanged() {
    // no changeable properties
  }

  /**
   * @see magellan.client.swing.tree.CellObject2#getGraphicsElements()
   */
  public List<GraphicsElement> getGraphicsElements() {
    if (graphicElements == null) {
      // FIXME (stm) do this in background thread!
      // in this situation init the region
      region.refreshUnitRelations(); // true

      GraphicsElement ge =
          new RegionGraphicsElement(toString(), null, null, region.getType().getIcon());
      ge.setTooltip(region.getType().getName());
      ge.setType(GraphicsElement.MAIN);

      graphicElements = Collections.singletonList(ge);
    }

    return graphicElements;
  }

  /**
   * @see magellan.client.swing.tree.CellObject2#getLabelPosition()
   */
  public int getLabelPosition() {
    return graphicElements.size() - 1;
  }

  /**
   * Never reversed
   * 
   * @see magellan.client.swing.tree.CellObject2#reverseOrder()
   */
  public boolean reverseOrder() {
    return false;
  }

  protected class RegionGraphicsElement extends GraphicsElement {
    /**
     * Creates a new RegionGraphicsElement object.
     */
    public RegionGraphicsElement(Object o, Icon i, Image im, String imageName) {
      super(o, i, im, imageName);
      setType(GraphicsElement.MAIN);
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
   * @see magellan.client.swing.tree.SupportsClipboard#getClipboardValue()
   */
  public String getClipboardValue() {
    if (region != null)
      return region.toString();
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
