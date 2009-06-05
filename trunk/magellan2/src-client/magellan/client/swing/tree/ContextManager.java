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
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import magellan.client.event.EventDispatcher;
import magellan.client.swing.context.ContextFactory;
import magellan.library.GameData;
import magellan.library.utils.logging.Logger;


/**
 * A context management class for trees.
 *
 * @author Andreas
 * @version 1.0
 */
public class ContextManager extends MouseAdapter {
	private static final Logger log = Logger.getInstance(ContextManager.class);
	private Collection<ContextListener> listeners = null;
	private JTree source;
    private EventDispatcher dispatcher;
	private Collection<?> selection = null;
	private GameData data = null;
	private ContextFactory failFactory = null;
	private Object failArgument = null;
	private Map<Object,ContextFactory> simpleObjects = null;

	/**
	 * Creates new ContextManager
	 *
	 * 
	 */
	public ContextManager(JTree source, EventDispatcher dispatcher) {
		this.source = source;
        this.dispatcher = dispatcher;
		source.addMouseListener(this);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setGameData(GameData data) {
		this.data = data;
	}

	/**
	 * Notifies this object of a selection change.
	 * 
	 * @param selection
	 */
	public void setSelection(Collection<?> selection) {
		this.selection = selection;
	}

  /**
   * Sets a context factory which is used in case no factory can be found based
   * on the selected element. 
   * 
	 * @param failArgument The argument which shall be passed to the factory
	 * @param failFactory The fall-back factory
	 */
	public void setFailFallback(Object failArgument, ContextFactory failFactory) {
		this.failArgument = failArgument;
		this.failFactory = failFactory;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setSimpleObjects(Map<Object,ContextFactory> simpleObjects) {
		this.simpleObjects = simpleObjects;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 */
	public void putSimpleObject(Object o, ContextFactory factory) {
		if(simpleObjects == null) {
			simpleObjects = new HashMap<Object, ContextFactory>();
		}

		simpleObjects.put(o, factory);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void removeSimpleObject(Object o) {
		if(simpleObjects == null) {
			return;
		}

		simpleObjects.remove(o);

		if(simpleObjects.size() == 0) {
			simpleObjects = null;
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void addContextListener(ContextListener cl) {
		if(listeners == null) {
			listeners = new LinkedList<ContextListener>();
		}

		listeners.add(cl);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void removeContextListener(ContextListener cl) {
		if(listeners == null) {
			return;
		}

		listeners.remove(cl);

		if(listeners.size() == 0) {
			listeners = null;
		}
	}

	/**
	 * Handles right-click actions by selecting an appropriate context menu.
	 * 
	 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
  public void mouseClicked(MouseEvent e) {
		if((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
			TreePath path = source.getPathForLocation(e.getX(), e.getY());
			DefaultMutableTreeNode node = null;
			boolean found = false;

			if(path != null) {
				Object last = path.getLastPathComponent();

				if(last instanceof DefaultMutableTreeNode) {
					node = (DefaultMutableTreeNode) last;

					Object user = node.getUserObject();

					if((user != null) && (user instanceof Changeable)) {
						Changeable change = (Changeable) user;

						if(((change.getChangeModes() & Changeable.CONTEXT_MENU) != 0) &&
							   (change.getContextFactory() != null)) {
							JPopupMenu menu = change.getContextFactory().createContextMenu(dispatcher, data, change.getArgument(), selection,node);

							if(menu != null) {
								showMenu(menu, source, e.getX(), e.getY());
								found = true;
							}
						}
					}

					if(!found) {
						found = checkSimpleObjects(e, node, user);
					}
				}
			}

			if(!found) {
				if(failFactory != null) {
					JPopupMenu menu = null;

					if(failArgument != null) {
						menu = failFactory.createContextMenu(dispatcher, data, failArgument, selection, node);
					} else {
						if(node != null) {
							menu = failFactory.createContextMenu(dispatcher, data, node.getUserObject(),
																 selection, node);
						}

						if(menu == null) {
							menu = failFactory.createContextMenu(dispatcher, data, node, selection, node);
						}
					}

					if(menu != null) {
						showMenu(menu, source, e.getX(), e.getY());
					} else {
						fireContextFailed(source, e);
					}
				} else {
					fireContextFailed(source, e);
				}
			}
		}
	}

	protected boolean checkSimpleObjects(MouseEvent e, DefaultMutableTreeNode node, Object user) {
		try {
			if(simpleObjects != null) {
				if(simpleObjects.containsKey(user.getClass())) {
					ContextFactory factory = simpleObjects.get(user.getClass());
					JPopupMenu menu = factory.createContextMenu(dispatcher, data, user, selection, node);

					if(menu != null) {
						showMenu(menu, source, e.getX(), e.getY());

						return true;
					}
				}

				if(simpleObjects.containsKey(node.getClass())) {
					ContextFactory factory = simpleObjects.get(node.getClass());
					JPopupMenu menu = factory.createContextMenu(dispatcher, data, node, selection, node);

					if(menu != null) {
						showMenu(menu, source, e.getX(), e.getY());

						return true;
					}
				}
			}
		} catch(Exception exc) {
			ContextManager.log.error(exc);
		}

		return false;
	}

	protected void fireContextFailed(Component src, MouseEvent e) {
		if(listeners != null) {
			Iterator it = listeners.iterator();

			while(it.hasNext()) {
				((ContextListener) it.next()).contextFailed(src, e);
			}
		}
	}

	/**
	 * Shows the given menu at position x,y. If there is not enough space on the screen, the menu
	 * not shown down and right from (x,y) but left or up or both of that point.
	 *
	 * 
	 * 
	 * 
	 * 
	 */
	public static void showMenu(JPopupMenu menu, Component c, int x, int y) {
		Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		Point showAtPoint = new Point(x, y);
		javax.swing.SwingUtilities.convertPointToScreen(showAtPoint, c);

		int width = (int) menu.getPreferredSize().getWidth();
		int height = (int) menu.getPreferredSize().getHeight();

		if((screen.width - showAtPoint.x) < width) {
			showAtPoint.x -= width;
		}

		if((screen.height - showAtPoint.y) < height) {
			showAtPoint.y -= height;
		}

		javax.swing.SwingUtilities.convertPointFromScreen(showAtPoint, c);
		menu.show(c, showAtPoint.x, showAtPoint.y);
	}

	/**
	 * @return the selection
	 */
	public Collection<?> getSelection() {
		return selection;
	}
}
