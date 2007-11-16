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

import java.util.Iterator;

import javax.swing.KeyStroke;

/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version 1.0
 */
public interface ShortcutListener {
	/**
	 * Should return all short cuts this class want to be informed. The elements have to be of type
	 * javax.swing.KeyStroke
	 *
	 * @return Iterator over all short
	 */
	public Iterator<KeyStroke> getShortCuts();

	/**
	 * This method is called when a shortcut from getShortCuts() is recognized.
	 */
	public void shortCut(KeyStroke shortcut);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getListenerDescription();

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public String getShortcutDescription(Object stroke);
}
