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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import magellan.client.swing.context.ContextFactory;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 259 $
 */
public class SimpleNodeWrapper implements CellObject, SupportsClipboard, Changeable,
    SupportsEmphasizing {
  protected static final List<String> defaultIcon = Collections.singletonList("simpledefault");
  private List<SupportsEmphasizing> subordinatedElements = null;
  protected List<String> icons;
  protected List returnIcons;
  protected String text;
  protected Object object;
  protected String clipboardValue = null;
  protected DetailsNodeWrapperDrawPolicy adapter;
  protected boolean showIcons = true;
  protected ContextFactory contextFactory = null;
  protected Object contextArgument = null;
  protected int amount = -1;

  /**
   * Creates new SimpleNodeWrapper
   */
  public SimpleNodeWrapper(Object obj, Object icons, String clipboardValue) {
    this(obj, obj == null ? "" : obj.toString(), icons, clipboardValue);
  }

  public SimpleNodeWrapper(Object obj, String text, Object icons) {
    this(obj, text, icons, null);
  }

  /**
   * Creates new SimpleNodeWrapper
   */
  public SimpleNodeWrapper(Object obj, String text, Object icons, String clipboardValue) {
    this.object = obj;
    this.text = text;
    this.icons = null;

    if (icons != null) {
      if (icons instanceof Collection) {
        this.icons = new ArrayList<String>((Collection<String>) icons);
      } else if (icons instanceof Map) {
        Map m = (Map) icons;

        this.icons = new ArrayList<String>(m.size());

        for (Iterator iter = m.values().iterator(); iter.hasNext();) {
          this.icons.add(iter.next().toString());
        }
      } else {
        this.icons = Collections.singletonList(icons.toString());
      }
    }

    this.clipboardValue = clipboardValue;
  }

  /**
   * Creates a new SimpleNodeWrapper object.
   */
  public SimpleNodeWrapper(Object obj, Object icons) {
    this(obj, icons, null);
  }

  /**
   * Controls whether the tree cell renderer should display this item more
   * noticeably than other nodes.
   */
  public boolean emphasized() {
    if (subordinatedElements != null) {
      for (Iterator<SupportsEmphasizing> iter = subordinatedElements.iterator(); iter.hasNext();) {
        SupportsEmphasizing se = iter.next();

        if (se.emphasized()) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * DOCUMENT-ME
   */
  public List<SupportsEmphasizing> getSubordinatedElements() {
    if (subordinatedElements == null) {
      subordinatedElements = new LinkedList<SupportsEmphasizing>();
    }

    return subordinatedElements;
  }

  /**
   * DOCUMENT-ME
   */
  public boolean isShowingIcons() {
    if (adapter != null) {
      return adapter.properties[0];
    }

    return showIcons;
  }

  /**
   * DOCUMENT-ME
   */
  public void setShowIcons(boolean b) {
    showIcons = b;
  }

  /**
   * DOCUMENT-ME
   */
  public List getIconNames() {
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
   * DOCUMENT-ME
   */
  public void propertiesChanged() {
    returnIcons = null;
  }

  /**
   * DOCUMENT-ME
   */
  public void setAmount(int i) {
    this.amount = i;
  }

  /**
   * DOCUMENT-ME
   */
  public int getAmount() {
    return this.amount;
  }

  /**
   * DOCUMENT-ME
   */
  @Override
  public String toString() {
    if (amount == -1) {
      return text;
    } else {
      return text + ": " + amount;
    }
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
    if (clipboardValue == null) {
      return toString();
    } else {
      return clipboardValue;
    }
  }

  protected NodeWrapperDrawPolicy createSimpleDrawPolicy(Properties settings, String prefix) {
    return new DetailsNodeWrapperDrawPolicy(1, null, settings, prefix, new String[][] { {
        "simple.showIcon", "true" } }, new String[] { "icons.text" }, 0, "tree.simplenodewrapper.");
  }

  /**
   * protected String getString(String key) { return
   * Resources.get("tree.simplenodewrapper.", key); }
   */

  /**
   * DOCUMENT-ME
   */
  public ContextFactory getContextFactory() {
    return contextFactory;
  }

  /**
   * DOCUMENT-ME
   */
  public Object getArgument() {
    return contextArgument;
  }

  /**
   * DOCUMENT-ME
   */
  public int getChangeModes() {
    return Changeable.CONTEXT_MENU;
  }

  /**
   * DOCUMENT-ME
   */
  public void setContextFactory(ContextFactory contextFactory) {
    this.contextFactory = contextFactory;
  }

  /**
   * DOCUMENT-ME
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
}
