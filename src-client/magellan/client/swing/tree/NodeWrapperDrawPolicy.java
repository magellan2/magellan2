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

import magellan.client.swing.preferences.PreferencesFactory;

/**
 * A PreferencesFactory for node wrappers.
 * 
 * @author SirBacon
 */
public interface NodeWrapperDrawPolicy extends PreferencesFactory {
  /**
   * Adds a cell object to the preferences factory in order to display options for it in the
   * preferences dialog.
   */
  public void addCellObject(CellObject co);

  /**
   * Returns a title for this factory.
   */
  public String getTitle();
}
