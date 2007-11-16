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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import magellan.library.Island;
import magellan.library.Region;
import magellan.library.Unit;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class IslandNodeWrapper implements CellObject, SupportsClipboard {
	private Island island = null;

	// a static list (will never change) its value over all instances of IslandNodeWrapper
	private static List<String> iconNames = Collections.singletonList("insel");

	/**
	 * Creates a new IslandNodeWrapper object.
	 *
	 * 
	 */
	public IslandNodeWrapper(Island island) {
		this.island = island;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Island getIsland() {
		return island;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String toString() {
		return island.getName();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public List getIconNames() {
		return iconNames;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean emphasized() {
		for(Iterator regionIter = island.regions().iterator(); regionIter.hasNext();) {
			Iterator it = ((Region) regionIter.next()).units().iterator();

			if(it != null) {
				while(it.hasNext()) {
					Unit u = (Unit) it.next();

					if(u.getFaction().isPrivileged()) {
						if(!u.isOrdersConfirmed()) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	/**
	 * DOCUMENT-ME
	 */
	public void propertiesChanged() {
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getClipboardValue() {
		return (island != null) ? island.getName() : toString();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public NodeWrapperDrawPolicy init(Properties settings, NodeWrapperDrawPolicy adapter) {
		return null;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 * 
	 *
	 * 
	 */
	public NodeWrapperDrawPolicy init(Properties settings, String prefix,
									  NodeWrapperDrawPolicy adapter) {
		return null;
	}
}
