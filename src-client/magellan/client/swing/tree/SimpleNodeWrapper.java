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
import java.util.Properties;

import magellan.client.swing.context.ContextFactory;

/**
 * A SimpleNodeWrapper wraps some object, displays a text and a set of icons and has a clipboard
 * value.
 * 
 * @author $Author: $
 * @version $Revision: 259 $
 */
public class SimpleNodeWrapper extends DefaultNodeWrapper implements SupportsClipboard, Changeable {
  protected static final List<String> defaultIcon = Collections.singletonList("simpledefault");
  protected List<String> icons;
  protected List<String> returnIcons;
  protected String text;
  protected Object object;
  protected String clipboardValue = null;
  protected DetailsNodeWrapperDrawPolicy adapter;
  protected boolean showIcons = true;
  protected ContextFactory contextFactory = null;
  protected Object contextArgument = null;
  protected int amount = -1;

  // /**
  // * Creates new SimpleNodeWrapper
  // */
  // public SimpleNodeWrapper(Object obj, String icon, String clipboardValue) {
  // this(obj, obj == null ? "" : obj.toString(), icon, clipboardValue);
  // }
  //
  // /**
  // * Creates new SimpleNodeWrapper
  // */
  // public SimpleNodeWrapper(Object obj, Collection<String> icons, String clipboardValue) {
  // this(obj, obj == null ? "" : obj.toString(), icons, clipboardValue);
  // }

  /**
   * @param obj
   * @param text
   * @param icon may be <code>null</code>
   */
  public SimpleNodeWrapper(Object obj, String text, String icon) {
    this(obj, text, icon, null);
  }

  /**
   * @param obj
   * @param text
   * @param icons may be <code>null</code>
   */
  public SimpleNodeWrapper(Object obj, String text, Collection<String> icons) {
    this(obj, text, icons, null);
  }

  /**
   * Creates new SimpleNodeWrapper.
   * 
   * @param obj
   * @param text
   * @param icon may be <code>null</code>
   * @param clipboardValue
   */
  public SimpleNodeWrapper(Object obj, String text, String icon, String clipboardValue) {
    this(obj, text, icon == null ? null : Collections.singletonList(icon), clipboardValue);
  }

  /**
   * Creates new SimpleNodeWrapper. If <code>icon == null</code>, a default icon is displayed. If
   * <code>clipboardValue == null</code>, the text is returned as clipboard value.
   * 
   * @param obj
   * @param text
   * @param icons may be <code>null</code> meaning no icons
   * @param clipboardValue may be <code>null</code>
   */
  public SimpleNodeWrapper(Object obj, String text, Collection<String> icons, String clipboardValue) {
    object = obj;
    this.text = text;
    this.icons = icons == null ? null : new ArrayList<String>(icons);
    this.clipboardValue = clipboardValue;
  }

  /**
   * Creates a new SimpleNodeWrapper object.
   */
  public SimpleNodeWrapper(Object obj, Collection<String> icons) {
    this(obj, obj == null ? "" : obj.toString(), icons, null);
  }

  /**
   * Creates a new SimpleNodeWrapper object.
   */
  public SimpleNodeWrapper(Object obj, String icon) {
    this(obj, obj == null ? "" : obj.toString(), icon == null ? null : Collections
        .singletonList(icon), null);
  }

  /**
   * DOCUMENT-ME
   */
  public boolean isShowingIcons() {
    if (adapter != null)
      return adapter.properties[0];

    return showIcons;
  }

  /**
   * DOCUMENT-ME
   */
  public void setShowIcons(boolean b) {
    showIcons = b;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#getIconNames()
   */
  public List<String> getIconNames() {
    if (returnIcons == null) {
      if (!isShowingIcons() || (icons == null)) {
        returnIcons = SimpleNodeWrapper.defaultIcon;
      } else {
        returnIcons = icons;
      }
    }

    return returnIcons;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#propertiesChanged()
   */
  public void propertiesChanged() {
    returnIcons = null;
  }

  /**
   * @param i
   */
  public void setAmount(int i) {
    amount = i;
  }

  /**
   * DOCUMENT-ME
   */
  public int getAmount() {
    return amount;
  }

  /**
   * DOCUMENT-ME
   */
  @Override
  public String toString() {
    if (amount == -1)
      return text;
    else
      return text + ": " + amount;
  }

  /**
   * DOCUMENT-ME
   */
  // public Object getText() {
  // return text;
  // }
  public Object getObject() {
    return object;
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

  protected NodeWrapperDrawPolicy createSimpleDrawPolicy(Properties settings, String prefix) {
    return new DetailsNodeWrapperDrawPolicy(1, null, settings, prefix, new String[][] { {
        "simple.showIcon", "true" } }, new String[] { "icons.text" }, 0, "tree.simplenodewrapper.");
  }

  /**
   * @see magellan.client.swing.tree.Changeable#getContextFactory()
   */
  public ContextFactory getContextFactory() {
    return contextFactory;
  }

  /**
   * @see magellan.client.swing.tree.Changeable#getArgument()
   */
  public Object getArgument() {
    return contextArgument;
  }

  /**
   * Returns {@link Changeable#CONTEXT_MENU}
   * 
   * @see magellan.client.swing.tree.Changeable#getChangeModes()
   */
  public int getChangeModes() {
    return Changeable.CONTEXT_MENU;
  }

  /**
   * Sets the context factory for this node.
   * 
   * @see magellan.client.swing.tree.Changeable
   */
  public void setContextFactory(ContextFactory contextFactory) {
    this.contextFactory = contextFactory;
  }

  /**
   * Sets the argument for the {@link ContextFactory}.
   * 
   * @see magellan.client.swing.tree.Changeable
   */
  public void setArgument(Object argument) {
    contextArgument = argument;
  }

  /**
   * DOCUMENT-ME
   */
  public NodeWrapperDrawPolicy init(Properties settings, NodeWrapperDrawPolicy adapter) {
    return init(settings, "SimpleNodeWrapper", adapter);
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

  private int warning = 0;

  /**
   * @see magellan.client.swing.tree.CellObject#setWarningLevel(int)
   */
  @Override
  public int setWarningLevel(int level) {
    propertiesChanged();
    int res = warning;
    warning = level;
    return res;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#getWarningLevel()
   */
  @Override
  public int getWarningLevel() {
    return warning;
  }

}
