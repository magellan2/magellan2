// class magellan.client.utils.BookmarkDock
// created on 19.07.2008
//
// Copyright 2003-2008 by magellan project team
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc., 
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
package magellan.client.utils;

import java.awt.BorderLayout;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import magellan.client.event.EventDispatcher;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;

/**
 * This is the old bookmark tool - now as a dock. A little dialog showing the bookmarks
 * 
 * @author Ulrich Küster
 * @author Thoralf Rickert
 * @version 1.0, 19.07.2008
 */
public class BookmarkDock extends JPanel implements SelectionListener {
  public static final String IDENTIFIER = "BOOKMARKS";
  private static BookmarkDock _INSTANCE = null;
  private JList list;
  private BookmarkManager manager = null;

  /**
   * Creates a new BookmarkDialog object.
   */
  protected BookmarkDock() {
  }

  /**
   * Returns the single instance of this dock.
   */
  public static BookmarkDock getInstance() {
    if (BookmarkDock._INSTANCE == null) {
      BookmarkDock._INSTANCE = new BookmarkDock();
    }
    return BookmarkDock._INSTANCE;
  }

  public void init(final BookmarkManager manager, final EventDispatcher dispatcher) {
    this.manager = manager;
    dispatcher.addSelectionListener(this);
    setLayout(new BorderLayout());

    list = new JList();
    updateData();

    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
          Object selectedValue = list.getSelectedValue();

          if ((selectedValue != null) && (selectedValue != manager.getActiveObject())) {
            dispatcher.fire(SelectionEvent.create(this, selectedValue, SelectionEvent.ST_DEFAULT));
          }
        }
      }
    });

    list.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_F2) {
          if (e.getModifiers() == InputEvent.CTRL_MASK) {
            manager.toggleBookmark();
          } else if (e.getModifiers() == 0) {
            manager.jumpForward();
          } else if (e.getModifiers() == InputEvent.SHIFT_MASK) {
            manager.jumpBackward();
          }
        } else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
          manager.toggleBookmark();
        }
      }
    });
    add(new JScrollPane(list), BorderLayout.CENTER);
  }

  /**
   * Rebuilds the whole list from the manager.
   */
  public void updateData() {
    DefaultListModel model = new DefaultListModel();

    for (Iterator<?> iter = manager.getBookmarks().listIterator(); iter.hasNext();) {
      model.addElement(iter.next());
    }

    list.setModel(model);
    list.setSelectedValue(manager.getActiveObject(), true);
    list.revalidate();
    list.repaint();
  }

  /**
   * Select an entry in the list.
   */
  public void setSelectedObject(Object o) {
    list.setSelectedValue(o, true);
    list.repaint();
  }

  /**
   * Selects (in the list) the entry corresponding to the selection.
   * 
   * @see magellan.client.event.SelectionListener#selectionChanged(magellan.client.event.SelectionEvent)
   */
  public void selectionChanged(SelectionEvent se) {
    if (!se.isSingleSelection()) {
      list.clearSelection();
    }

    Object o = se.getActiveObject();

    if (o != null) {
      if (manager.getBookmarks().contains(o)) {
        list.setSelectedValue(o, true);
        manager.setActiveBookmark(((DefaultListModel) list.getModel()).indexOf(o));
      } else {
        list.clearSelection();
      }
    }
  }

  /**
   * Does nothing
   */
  public void quit() {
    // settings.put("BookmarkManager.DialogWidth", String.valueOf(this.getWidth()));
    // settings.put("BookmarkManager.DialogHeight", String.valueOf(this.getHeight()));
    // settings.put("BookmarkManager.DialogXPos", String.valueOf(this.getX()));
    // settings.put("BookmarkManager.DialogYPos", String.valueOf(this.getY()));
  }

}
