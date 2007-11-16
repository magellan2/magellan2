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

import magellan.client.swing.context.ContextFactory;

/**
 * An interface signaling a context manager that this element can be edited. At this time only
 * context menus are supported.
 *
 * @author Andreas
 * @version 1.0
 */
public interface Changeable {
	/** DOCUMENT-ME */
	public static final int CONTEXT_MENU = 1;

	/** DOCUMENT-ME */
	public static final int CELL_EDITOR = 2;

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getChangeModes();

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public ContextFactory getContextFactory();

	//public CellEditor getCellEditor();
	public Object getArgument();
}
