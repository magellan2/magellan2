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

import java.util.Collection;
import java.util.Properties;

import magellan.client.extern.MagellanPlugIn;
import magellan.client.preferences.ResourcePreferences;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.swing.preferences.PreferencesFactory;


/**
 * DOCUMENT ME!
 *
 * @author SirBacon
 */
public class ResourceSettingsFactory implements PreferencesFactory {
	Properties settings;
  private Collection<MagellanPlugIn> plugins;

	/**
	 * Creates a new instance of EresseaClass
	 *
	 * 
	 */
	public ResourceSettingsFactory(Collection<MagellanPlugIn> plugins, Properties settings) {
		this.settings = settings;
		this.plugins = plugins;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public PreferencesAdapter createPreferencesAdapter() {
		return new ResourcePreferences(plugins, settings);
	}
}
