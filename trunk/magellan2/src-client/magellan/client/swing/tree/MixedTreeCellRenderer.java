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

import java.awt.Component;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version 1.0
 */
public class MixedTreeCellRenderer implements TreeCellRenderer {
	protected Map<Class<?>,TreeCellRenderer> renderers;
	protected TreeCellRenderer defaultRenderer;

	/**
	 * Creates new Template
	 *
	 * 
	 */
	public MixedTreeCellRenderer(TreeCellRenderer defaultRenderer) {
		this.defaultRenderer = defaultRenderer;
		renderers = new HashMap<Class<?>, TreeCellRenderer>();
	}

	/**
	 * 
	 */
	public void putRenderer(Object o, TreeCellRenderer r) {
		if(o instanceof Class<?>) {
			renderers.put((Class<?>)o, r);
		} else {
			renderers.put(o.getClass(), r);
		}
	}

  /**
   * 
   */
	protected TreeCellRenderer findRenderer(Class<?> c) {
		if(renderers.containsKey(c)) {
			return renderers.get(c);
		}

		Iterator<Class<?>> it = renderers.keySet().iterator();

		while(it.hasNext()) {
			Class<?> o = it.next();

			if(o.isAssignableFrom(c)) {
				return renderers.get(o);
			}
		}

		return defaultRenderer;
	}

	/**
	 * 
	 */
	public Component getTreeCellRendererComponent(JTree jTree,
														   Object obj, boolean selected,
														   boolean expanded, boolean leaf,
														   int row, boolean hasFocus) {
		Object o = obj;

		if(obj instanceof DefaultMutableTreeNode) {
			o = ((DefaultMutableTreeNode) obj).getUserObject();
		}

		TreeCellRenderer tcr = findRenderer(o.getClass());

		if(tcr != null) {
      return tcr.getTreeCellRendererComponent(jTree, obj, selected, expanded, leaf, row, hasFocus);
		}

		return null;
	}
}
