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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public class MixedTreeCellRenderer implements TreeCellRenderer {
	protected Map<Class,TreeCellRenderer> renderers;
	protected TreeCellRenderer def;

	/**
	 * Creates new Template
	 *
	 * 
	 */
	public MixedTreeCellRenderer(TreeCellRenderer def) {
		this.def = def;
		renderers = new HashMap<Class, TreeCellRenderer>();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 */
	public void putRenderer(Object o, TreeCellRenderer r) {
		if(o instanceof Class) {
			renderers.put((Class)o, r);
		} else {
			renderers.put(o.getClass(), r);
		}
	}

	protected TreeCellRenderer findRenderer(Class c) {
		if(renderers.containsKey(c)) {
			return (TreeCellRenderer) renderers.get(c);
		}

		Iterator<Class> it = renderers.keySet().iterator();

		while(it.hasNext()) {
			Class o = it.next();

			if(o.isAssignableFrom(c)) {
				return (TreeCellRenderer) renderers.get(o);
			}
		}

		return def;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 *
	 * 
	 */
	public java.awt.Component getTreeCellRendererComponent(javax.swing.JTree jTree,
														   java.lang.Object obj, boolean param,
														   boolean param3, boolean param4,
														   int param5, boolean param6) {
		Object o = obj;

		if(obj instanceof DefaultMutableTreeNode) {
			o = ((DefaultMutableTreeNode) obj).getUserObject();
		}

		TreeCellRenderer tcr = findRenderer(o.getClass());

		if(tcr != null) {
			return tcr.getTreeCellRendererComponent(jTree, obj, param, param3, param4, param5,
													param6);
		}

		return null;
	}
}
