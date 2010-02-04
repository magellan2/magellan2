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

/*
 * ExtendedPreferencesDialog.java
 *
 * Created on 23. April 2002, 16:54
 */
package magellan.client.swing.preferences;

import java.util.Collection;

/**
 * A class that has sub preference dialogs. This is for layout purposes in the preferences dialog.
 * It marks a pref adapter that has several sub dialogs that should be displayed as sub elements in
 * the options tree. All big preference adapters should use this interface.
 * 
 * @author Andreas
 * @version 1.0
 */
public interface ExtendedPreferencesAdapter extends PreferencesAdapter {
  /**
   * Returns a list of preferences adapters that should be displayed in the given order.
   */
  public Collection<PreferencesAdapter> getChildren();
}
