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
public class UnitListNodeWrapper implements CellObject, SupportsClipboard {
  // identifies that this UnitListNodeWrapper contains a list of units that are
  // some other unit's students

  protected static final List<String> defaultIcon = Collections.singletonList("simpledefault");

  /** DOCUMENT-ME */
  public static final int STUDENT_LIST = 1;
  private int type = 0;
  protected Collection<Unit> units = null;
  protected String text = null;
  protected String clipboardValue = null;

  protected Collection<String> icons;
  protected Collection<String> returnIcons;
  protected DetailsNodeWrapperDrawPolicy adapter;

  /**
   * Creates new UnitListNodeWrapper
   */
  public UnitListNodeWrapper(String text, String clipboardValue, Collection<Unit> units, int type) {
    this(text, clipboardValue, units);
    this.type = type;
  }

  /**
   * Creates a new UnitListNodeWrapper object.
   */
  public UnitListNodeWrapper(String text, String clipboardValue, Collection<Unit> units) {
    this.text = text;
    this.units = units;
    this.clipboardValue = clipboardValue;
  }

  /**
   * DOCUMENT-ME
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
   * DOCUMENT-ME
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
   * DOCUMENT-ME
   */
  public int getType() {
    return type;
  }

  /**
   * DOCUMENT-ME
   */
  public Collection<Unit> getUnits() {
    return units;
  }

  /**
   * DOCUMENT-ME
   */
  @Override
  public String toString() {
    return text;
  }

  /**
   * DOCUMENT-ME
   */
  public String getClipboardValue() {
    if (clipboardValue == null)
      return toString();
    else
      return clipboardValue;
  }

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
   * Controls whether the tree cell renderer should display this item more noticeably than other
   * nodes.
   */
  public boolean emphasized() {
    return false;
  }

  /**
   * DOCUMENT-ME
   */
  public NodeWrapperDrawPolicy init(Properties settings, NodeWrapperDrawPolicy adapter) {
    return init(settings, "UnitListNodeWrapper", adapter);
  }

  /**
   * DOCUMENT-ME
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
        "simple.showIcon", "true" } }, new String[] { "icons.text" }, 0, "tree.unitnodewrapper.");
  }

  /**
   * DOCUMENT-ME
   */
  public void propertiesChanged() {
    returnIcons = null;
  }

}
