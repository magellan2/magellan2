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
import magellan.client.swing.tree.UnitNodeWrapper;
import magellan.library.GameData;
import magellan.library.Unit;


/**
 * Context Factory for unit contexts.
 *
 * @author Andreas
 * @version 1.0
 */
public class UnitContextFactory implements ContextFactory {
	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 * 
	 * 
	 *
	 * 
	 */
	public JPopupMenu createContextMenu(EventDispatcher dispatcher,
            GameData data, Object argument,
            Collection<Unit> selectedObjects,
            DefaultMutableTreeNode node) {
		if(argument instanceof Unit) {
			return new UnitContextMenu((Unit) argument, selectedObjects,
									   dispatcher, data);
		} else if(argument instanceof UnitNodeWrapper) {
			return new UnitContextMenu(((UnitNodeWrapper) argument).getUnit(), selectedObjects,
									   dispatcher, data);
		}

		return null;
	}
}
