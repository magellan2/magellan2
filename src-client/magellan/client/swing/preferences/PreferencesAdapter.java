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

package magellan.client.swing.preferences;

import java.awt.Component;

/**
 * This interface represents a component that contains preferences for one part of magellan.
 * 
 * @author $Author: $
 * @version $Revision: 269 $
 */
public interface PreferencesAdapter {

  int PREFERRED_WIDTH = 500;

  /**
   * This function is called for initializing the preferences. It is recommended to use this method
   * to re-initialize the values that you set within the constructor because it is possible that
   * there is multithread problem (see for example bug #156 in mantis).
   */
  public void initPreferences();

  /**
   * This function is called for applying the preferences. After calling this method, the
   * Preferences should reflect the changes made in the adapter. But not before.
   */
  public void applyPreferences();

  /**
   * This function delivers the gui for the preferences adapter.
   */
  public Component getComponent();

  /**
   * This function delivers the visible name of the preferences adapter.
   */
  public String getTitle();
}
