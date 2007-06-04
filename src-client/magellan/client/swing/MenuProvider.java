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

package magellan.client.swing;

import javax.swing.JMenu;

/**
 * Simple interface that shows that the implementing class provides a menu.
 *
 * @author Andreas
 */
public interface MenuProvider {
	// the menu itself
	public JMenu getMenu();

	// should return the internal name of the menu it should be inserted under
	// a value of null means a top level menu
	public String getSuperMenu();

	// should return the title of the super menu
	public String getSuperMenuTitle();
}
