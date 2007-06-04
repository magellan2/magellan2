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

import java.awt.event.FocusListener;
import java.awt.event.KeyListener;

import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;

import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.Unit;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public interface OrderEditorList extends magellan.client.event.SelectionListener {
	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public JTextComponent getCurrentEditor();

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Unit getCurrentUnit();

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void addExternalKeyListener(KeyListener k);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void removeExternalKeyListener(KeyListener k);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void addExternalCaretListener(CaretListener k);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void removeExternalCaretListener(CaretListener k);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void addExternalFocusListener(FocusListener k);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void removeExternalFocusListener(FocusListener k);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public PreferencesAdapter getPreferencesAdapter();
}
