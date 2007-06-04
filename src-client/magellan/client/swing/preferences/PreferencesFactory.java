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

/**
 * An interface for preferences purposes. To always keep track of program changes each time the
 * options are opened new preferences adapters should be built. To make this more modular this
 * interface should be used. The class building the preferences dialog (at the moment the menu
 * action) should know what to do.
 *
 * @author Andreas
 * @version
 */
public interface PreferencesFactory {
	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public PreferencesAdapter createPreferencesAdapter();
}
