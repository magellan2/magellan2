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
import java.util.Properties;

/**
 * An interface serving as an abstraction layer between a tree cell renderer and the user object to
 * render. Implementations of this interface mainly can decide what information of the user object
 * they want to encode as icons.
 * 
 * @author Sebastian
 * @version 1.0
 */
public interface CellObject {
  /** Message level. */
  public static final int L_MESSAGE = 1;
  /** Warning level. */
  public static final int L_WARNING = 2;
  /** Error level. */
  public static final int L_ERROR = 3;

  /**
   * Creates a preferences adapter factory.
   * 
   * @param settings
   * @param adapter
   * @return A preferences adapter factory. If <code>adapter!=null</code> it should be returned. If
   *         this object has no changeable preferences, <code>null</code> should be returned.
   */
  NodeWrapperDrawPolicy init(Properties settings, NodeWrapperDrawPolicy adapter);

  /**
   * Creates a preferences adapter factory.
   * 
   * @param settings
   * @param prefix Prefix for properties keys
   * @param adapter The cell object should be appended to this adapter. If this is <code>null</code>
   *          a new adapter should be returned.
   * @return A preferences adapter factory. If <code>adapter!=null</code> it should be returned. If
   *         this object has no changeable preferences, <code>null</code> should be returned.
   */
  NodeWrapperDrawPolicy init(Properties settings, String prefix, NodeWrapperDrawPolicy adapter);

  /**
   * Notifies this object that the draw policy may have changed. So it may have to recalculate its
   * appearance.
   */
  void propertiesChanged();

  /**
   * Returns a list of String objects that denote the file name (without the extension) of the icons
   * to be displayed by the tree cell renderer. A return value of <code>null</code> is valid to
   * indicate that no icons shall be displayed.
   * 
   * @return list of icon names or null if no icons shall be displayed.
   */
  Collection<String> getIconNames();

  /**
   * Controls whether the tree cell renderer should display this item more noticeably than other
   * nodes.
   * 
   * @return true if this item shall be displayed emphasized, false otherwise
   */
  boolean emphasized();

  /**
   * Sets the warning level for this node
   * 
   * @param level the new warning level
   * @return the old warning level
   */
  public int setWarningLevel(int level);

  /**
   * Returns the warning level for this node.
   * 
   * @return the warning level for this node
   */
  public int getWarningLevel();

  /**
   * This enforces the toString method
   * 
   * @return String representation of this CellObject
   */
  String toString();
}
