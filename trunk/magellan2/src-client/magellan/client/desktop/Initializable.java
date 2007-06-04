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

package magellan.client.desktop;

/**
 * This interface is used to allow certain desktop configurations to have different component
 * configurations. This is implemented through config strings. These strings are stored in the
 * magellan_desktop.ini/magellan.ini in the desktop configuration data. A component is initialized
 * with the initComponent(String) method. This is done each time the desktop configuration(e.g.
 * layouts, split sets etc.) changes. Before this call the current configuration will be retrieved
 * through the getComponentConfiguration() (if this is not the very first init) and stored.
 *
 * @author Andreas
 * @version
 */
public interface Initializable {
	/**
	 * Initializes the component according to the given string.
	 */
	public void initComponent(String configuration);

	/**
	 * Returns the current configuration of this component. This may use any characters except ","
	 * and ";".
	 *
	 * 
	 */
	public String getComponentConfiguration();
}
