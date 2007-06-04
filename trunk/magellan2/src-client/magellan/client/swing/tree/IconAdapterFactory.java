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

import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.swing.preferences.PreferencesFactory;


/**
 * DOCUMENT ME!
 *
 * @author SirBacon
 */
public class IconAdapterFactory implements PreferencesFactory {
	List<NodeWrapperFactory> nodeWrapperFactories;

	/**
	 * Creates a new instance of EresseaClass
	 *
	 * 
	 */
	public IconAdapterFactory(List<NodeWrapperFactory> nw) {
		nodeWrapperFactories = nw;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public PreferencesAdapter createPreferencesAdapter() {
		return new IconAdapter(nodeWrapperFactories);
	}
}
