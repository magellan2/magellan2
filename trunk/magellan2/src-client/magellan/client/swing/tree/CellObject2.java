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

import java.util.List;

/**
 * A CellObject that supports a more flexible icon order.
 */
public interface CellObject2 extends CellObject {

  /**
   * Returns a list of graphics elements in the order in which they should appear in the tree node.
   * This should generally be: Fixed icons first, then the rest of the icons and then the label for
   * normal order, and fixed icons first, then the label, then the rest of the icons for reversed
   * order.
   */
  List<GraphicsElement> getGraphicsElements();

  /**
   * Signals if the order of the label and icons should be reversed.
   */
  boolean reverseOrder();

  /**
   * Returns the position of the label in the list of graphics elements.
   */
  int getLabelPosition();
}
