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

package magellan.client.utils;

import java.util.List;

import magellan.client.preferences.IconPreferences;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.client.swing.tree.NodeWrapperFactory;

/**
 * DOCUMENT ME!
 * 
 * @author SirBacon
 */
public class IconAdapterFactory implements PreferencesFactory {
  List<NodeWrapperFactory> nodeWrapperFactories;

  /**
   * Creates a new instance of EresseaClass
   */
  public IconAdapterFactory(List<NodeWrapperFactory> nw) {
    nodeWrapperFactories = nw;
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesFactory#createPreferencesAdapter()
   */
  public PreferencesAdapter createPreferencesAdapter() {
    return new IconPreferences(nodeWrapperFactories);
  }
}
