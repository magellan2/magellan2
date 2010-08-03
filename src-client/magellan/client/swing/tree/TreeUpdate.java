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

/**
 * An interface for notifiying a GUI element that it should update its tree.
 * 
 * @author Andreas
 * @version 1.0
 */
public interface TreeUpdate {
  /**
   * Called when the tree should be updated.
   * 
   * @param src The object that was responsible for this event.
   */
  public void updateTree(Object src);
}
