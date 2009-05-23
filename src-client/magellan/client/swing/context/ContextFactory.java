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

package magellan.client.swing.context;

import java.util.Collection;

import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

import magellan.client.event.EventDispatcher;
import magellan.library.GameData;


/**
 * Interface for automated context menu creation.
 *
 * @author Andreas
 * @version 1.0
 */
public interface ContextFactory {
	/**
	 * Creates an appropriate context menu
	 * 
	 * @param dispatcher The responsible event dispatcher.
	 * @param data The data
	 * @param argument The object for which the context menu should be created
	 * @param selectedObjects Additional selected objects
	 * @param node
	 * @return
	 */
	public JPopupMenu createContextMenu(EventDispatcher dispatcher, GameData data, Object argument,
      Collection<?> selectedObjects, DefaultMutableTreeNode node);
}
