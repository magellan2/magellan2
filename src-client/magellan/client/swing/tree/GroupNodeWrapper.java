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

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.UIManager;

import magellan.library.Group;


/**
 * DOCUMENT ME!
 *
 * @author Andreas, Ulrich K�ster
 */
public class GroupNodeWrapper implements CellObject2, SupportsClipboard, SupportsEmphasizing {
	protected Group group;
	protected List<GraphicsElement> GE;
	protected static Icon icon;
	private int amount = -1;
	private List<SupportsEmphasizing> subordinatedElements = new ArrayList<SupportsEmphasizing>();

	/**
	 * Creates new GroupNodeWrapper
	 *
	 * 
	 */
	public GroupNodeWrapper(Group g) {
		group = g;

		if(icon == null) {
			icon = UIManager.getIcon("Tree.closedIcon");
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Group getGroup() {
		return group;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public List<GraphicsElement> getGraphicsElements() {
		if(GE == null) {
			GraphicsElement ge = new GroupGraphicsElement(group, icon, null, null);
			Tag2Element.start(group);
			Tag2Element.apply(ge);
			ge.setType(GraphicsElement.MAIN);

			GE = Collections.singletonList(ge);
		}

		return GE;
	}

	private class GroupGraphicsElement extends GraphicsElement {
		/**
		 * Creates a new GroupGraphicsElement object.
		 *
		 * 
		 * 
		 * 
		 * 
		 */
		public GroupGraphicsElement(Object object, Icon icon, Image image, String imageName) {
			super(object, icon, image, imageName);
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public boolean isEmphasized() {
			return emphasized();
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public List<SupportsEmphasizing> getSubordinatedElements() {
		return subordinatedElements;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean reverseOrder() {
		return false;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setAmount(int i) {
		this.amount = i;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getAmount() {
		return this.amount;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String toString() {
		if(this.amount == -1) {
			return group.toString();
		} else {
			return group.toString() + ": " + amount;
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean emphasized() {
		for(Iterator iter = subordinatedElements.iterator(); iter.hasNext();) {
			SupportsEmphasizing se = (SupportsEmphasizing) iter.next();

			if(se.emphasized()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public List getIconNames() {
		return null;
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
		if(group != null) {
			return group.getName();
		} else {
			return toString();
		}
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
