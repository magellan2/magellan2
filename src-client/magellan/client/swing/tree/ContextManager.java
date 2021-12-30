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
import magellan.client.event.SelectionEvent;
import magellan.client.swing.context.ContextFactory;
import magellan.library.GameData;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.utils.logging.Logger;

/**
 * A context management class for trees.
 * 
 * @author Andreas
 * @version 1.0
 */
public class ContextManager extends MouseAdapter implements GameDataListener {
  private static final Logger log = Logger.getInstance(ContextManager.class);
  private Collection<ContextListener> listeners = null;
  private JTree source;
  private EventDispatcher dispatcher;
  private SelectionEvent selection = null;
  private GameData data = null;
  private ContextFactory failFactory = null;
  private Object failArgument = null;
  private Map<Object, ContextFactory> simpleObjects;

  /**
   * Creates new ContextManager
   */
  public ContextManager(JTree source, EventDispatcher dispatcher) {
    this.source = source;
    this.dispatcher = dispatcher;
    // FIXME should use source.setComponentPopupMenu(popup);
    source.addMouseListener(this);
    dispatcher.addGameDataListener(this);
  }

  /**
   * Notifies this object of a selection change.
   * 
   * @param selection
   */
  public void setSelection(SelectionEvent selection) {
    this.selection = selection;
  }

  /**
   * Sets a context factory which is used in case no factory can be found based on the selected
   * element.
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
   */
  public void setSimpleObjects(Map<Object, ContextFactory> simpleObjects) {
    this.simpleObjects = simpleObjects;
  }

  /**
   * DOCUMENT-ME
   */
  public void putSimpleObject(Object o, ContextFactory factory) {
    if (simpleObjects == null) {
      simpleObjects = new HashMap<Object, ContextFactory>();
    }

    simpleObjects.put(o, factory);
  }

  /**
   * DOCUMENT-ME
   */
  public void removeSimpleObject(Object o) {
    if (simpleObjects == null)
      return;

    simpleObjects.remove(o);

    if (simpleObjects.size() == 0) {
      simpleObjects = null;
    }
  }

  /**
   * Registers a listener that will be notified if context menu creation fails.
   * 
   * @param cl A new listener
   */
  public void addContextListener(ContextListener cl) {
    if (listeners == null) {
      listeners = new LinkedList<ContextListener>();
    }

    listeners.add(cl);
  }

  /**
   * Removes this listener
   * 
   * @param cl This listeners will no longer be notified.
   */
  public void removeContextListener(ContextListener cl) {
    if (listeners == null)
      return;

    listeners.remove(cl);

    if (listeners.size() == 0) {
      listeners = null;
    }
  }

  /**
   * Handles right-click actions by selecting an appropriate context menu. There are three ways to
   * create a context menu: If the node's userObject is a {@link Changeable}, the Changeable's
   * context factory is used. Else one of the factories registered by
   * {@link #putSimpleObject(Object, ContextFactory)} is selected. If neither of those find a
   * suitable factory, the fallback factory set in {@link #setFailFallback(Object, ContextFactory)}
   * is used.
   * 
   * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseClicked(MouseEvent e) {
    maybeShowPopup(e);
  }

  @Override
  public void mousePressed(MouseEvent e) {
    maybeShowPopup(e);
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    maybeShowPopup(e);
  }

  private void maybeShowPopup(MouseEvent e) {
    if (e.isPopupTrigger()) {
      TreePath path = source.getPathForLocation(e.getX(), e.getY());
      DefaultMutableTreeNode node = null;
      boolean found = false;

      if (path != null) {
        Object last = path.getLastPathComponent();

        if (last instanceof DefaultMutableTreeNode) {
          node = (DefaultMutableTreeNode) last;

          Object user = node.getUserObject();

          if ((user != null) && (user instanceof Changeable)) {
            Changeable change = (Changeable) user;

            if (((change.getChangeModes() & Changeable.CONTEXT_MENU) != 0)
                && (change.getContextFactory() != null)) {
              JPopupMenu menu =
                  change.getContextFactory().createContextMenu(dispatcher, data,
                      change.getArgument(), selection, node);

              if (menu != null) {
                ContextManager.showMenu(menu, source, e.getX(), e.getY());
                found = true;
              }
            }
          }

          if (!found) {
            found = checkSimpleObjects(e, node, user);
          }
        }
      }

      if (!found) {
        if (failFactory != null) {
          JPopupMenu menu = null;

          if (failArgument != null) {
            menu = failFactory.createContextMenu(dispatcher, data, failArgument, selection, node);
          } else {
            if (node != null) {
              menu =
                  failFactory.createContextMenu(dispatcher, data, node.getUserObject(), selection,
                      node);
            }

            if (menu == null) {
              menu = failFactory.createContextMenu(dispatcher, data, node, selection, node);
            }
          }

          if (menu != null) {
            ContextManager.showMenu(menu, source, e.getX(), e.getY());
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
      if (simpleObjects != null) {
        if (simpleObjects.containsKey(user.getClass())) {
          ContextFactory factory = simpleObjects.get(user.getClass());
          JPopupMenu menu = factory.createContextMenu(dispatcher, data, user, selection, node);

          if (menu != null) {
            ContextManager.showMenu(menu, source, e.getX(), e.getY());

            return true;
          }
        }

        if (simpleObjects.containsKey(node.getClass())) {
          ContextFactory factory = simpleObjects.get(node.getClass());
          JPopupMenu menu = factory.createContextMenu(dispatcher, data, node, selection, node);

          if (menu != null) {
            ContextManager.showMenu(menu, source, e.getX(), e.getY());

            return true;
          }
        }
      }
    } catch (Exception exc) {
      ContextManager.log.error(exc);
    }

    return false;
  }

  protected void fireContextFailed(Component src, MouseEvent e) {
    if (listeners != null) {
      Iterator<ContextListener> it = listeners.iterator();

      while (it.hasNext()) {
        (it.next()).contextFailed(src, e);
      }
    }
  }

  /**
   * Shows the given menu at position x,y. If there is not enough space on the screen, the menu not
   * shown down and right from (x,y) but left or up or both of that point.
   */
  public static void showMenu(JPopupMenu menu, Component c, int x, int y) {
    Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
    Point showAtPoint = new Point(x, y);
    javax.swing.SwingUtilities.convertPointToScreen(showAtPoint, c);

    int width = (int) menu.getPreferredSize().getWidth();
    int height = (int) menu.getPreferredSize().getHeight();

    if ((screen.width - showAtPoint.x) < width) {
      showAtPoint.x -= width;
    }

    if ((screen.height - showAtPoint.y) < height) {
      showAtPoint.y -= height;
    }

    javax.swing.SwingUtilities.convertPointFromScreen(showAtPoint, c);
    menu.show(c, showAtPoint.x, showAtPoint.y);
  }

  /**
   * @return the selection
   */
  public SelectionEvent getSelection() {
    return selection;
  }

  public void gameDataChanged(GameDataEvent e) {
    data = e.getGameData();
  }
}
