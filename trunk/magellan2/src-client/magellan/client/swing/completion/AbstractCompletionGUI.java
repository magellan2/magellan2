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

package magellan.client.swing.completion;

/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public abstract class AbstractCompletionGUI implements CompletionGUI {
	/**
	 * Returns the title of this GUI.
	 *
	 * 
	 */
	public String toString() {
		return getTitle();
	}

	/**
	 * Returns the name of this CompletionGUI. This implementation returns the content of
	 * "gui.title" key in the current Resource Bundle.
	 *
	 * ... gui.title
	 */
	public abstract String getTitle();
}
