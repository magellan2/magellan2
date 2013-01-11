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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import magellan.library.Unit;

/**
 * DOCUMENT ME!
 * 
 * @author Ulrich Küster A simple nodewrapper wrapping a list of units allowing acces to them via
 *         getUnits().
 */
public class UnitListNodeWrapper extends DefaultNodeWrapper implements SupportsClipboard {
  // identifies that this UnitListNodeWrapper contains a list of units that are
  // some other unit's students

  protected static final List<String> defaultIcon = Collections.singletonList("simpledefault");

  /** The student list type */
  public static final int STUDENT_LIST = 1;

  protected Collection<Unit> units = null;
  protected String text = null;
  protected String clipboardValue = null;

  protected Collection<String> icons;
  protected Collection<String> returnIcons;
  protected DetailsNodeWrapperDrawPolicy adapter;

  /**
   * Creates a new UnitListNodeWrapper object.
   */
  public UnitListNodeWrapper(String text, String clipboardValue, Collection<Unit> units) {
    this.text = text;
    this.units = units;
    this.clipboardValue = clipboardValue;
  }

  /**
   * Creates a new UnitListNodeWrapper object.
   * 
   * @param text
   * @param clipboardValue
   * @param units
   * @param icon
   */
  public UnitListNodeWrapper(String text, String clipboardValue, Collection<Unit> units, String icon) {
    this(text, clipboardValue, units, icon == null ? null : Collections.singleton(icon));
  }

  /**
   * Creates a new UnitListNodeWrapper object.
   * 
   * @param text
   * @param clipboardValue
   * @param units
   * @param icons
   */
  public UnitListNodeWrapper(String text, String clipboardValue, Collection<Unit> units,
      Collection<String> icons) {
    this(text, clipboardValue, units);
    this.icons = icons;

  }

  /**
   * @return the unit list
   */
  public Collection<Unit> getUnits() {
    return units;
  }

  /**
   * @return The (explicitly set) text.
   */
  @Override
  public String toString() {
    return text;
  }

  /**
   * @see magellan.client.swing.tree.SupportsClipboard#getClipboardValue()
   */
  public String getClipboardValue() {
    if (clipboardValue == null)
      return toString();
    else
      return clipboardValue;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#getIconNames()
   */
  public Collection<String> getIconNames() {
    if (returnIcons == null) {
      if ((icons == null)) {
        returnIcons = UnitListNodeWrapper.defaultIcon;
      } else {
        returnIcons = icons;
      }
    }

    return returnIcons;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#emphasized()
   */
  @Override
  public boolean emphasized() {
    return false;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#init(java.util.Properties,
   *      magellan.client.swing.tree.NodeWrapperDrawPolicy)
   */
  public NodeWrapperDrawPolicy init(Properties settings, NodeWrapperDrawPolicy anAdapter) {
    return init(settings, "UnitListNodeWrapper", anAdapter);
  }

  /**
   * @see magellan.client.swing.tree.CellObject#init(java.util.Properties, java.lang.String,
   *      magellan.client.swing.tree.NodeWrapperDrawPolicy)
   */
  public NodeWrapperDrawPolicy init(Properties settings, String prefix,
      NodeWrapperDrawPolicy anAdapter) {
    if (anAdapter == null) {
      anAdapter = createSimpleDrawPolicy(settings, prefix);
    }

    anAdapter.addCellObject(this);
    adapter = (DetailsNodeWrapperDrawPolicy) anAdapter;

    return anAdapter;
  }

  /**
   * @param settings
   * @param prefix
   */
  protected NodeWrapperDrawPolicy createSimpleDrawPolicy(Properties settings, String prefix) {
    return new DetailsNodeWrapperDrawPolicy(1, null, settings, prefix, new String[][] { {
        "simple.showIcon", "true" } }, new String[] { "icons.text" }, 0, "tree.unitnodewrapper.");
  }

  /**
   * @see magellan.client.swing.tree.CellObject#propertiesChanged()
   */
  public void propertiesChanged() {
    returnIcons = null;
  }

}
