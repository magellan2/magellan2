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

package magellan.client.utils;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.KeyStroke;

import magellan.client.desktop.DesktopEnvironment;
import magellan.client.desktop.ShortcutListener;
import magellan.client.event.EventDispatcher;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.utils.Resources;


/**
 * Manages setting an getting of Bookmarks. 
 * - CTRL + F2 : place a bookmark on the current activeObject or removes it if already bookmarked 
 * - F2 : go to next bookmark
 * - Shift + F2 : go to prior bookmark
 *
 * @author Ulrich Küster 
 */
public class BookmarkManager implements ShortcutListener, SelectionListener, GameDataListener {
	private EventDispatcher dispatcher;
	private List<KeyStroke> shortCuts = new LinkedList<KeyStroke>();
	private Object activeObject = null;

	// the list containing the bookmarked objects
	private List<Object> bookmarks = new Vector<Object>();

	// the number of the current bookmark
	private int activeBookmark = 0;
	private BookmarkDock dialog;

	/**
	 * Creates a new BookmarkManager object.
	 */
	public BookmarkManager(EventDispatcher dispatcher) {
		this.dispatcher = dispatcher;
		dispatcher.addSelectionListener(this);

		shortCuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		shortCuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_F2, InputEvent.SHIFT_MASK));
		shortCuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_F2, InputEvent.CTRL_MASK));
		DesktopEnvironment.registerShortcutListener(this);
		
		this.dialog = BookmarkDock.getInstance();
		this.dialog.init(this,dispatcher);
	}

	/**
	 * 
	 */
	public Iterator<KeyStroke> getShortCuts() {
		return shortCuts.iterator();
	}

	/**
	 * 
	 */
	public void shortCut(KeyStroke shortCut) {
		if(shortCut.equals(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0))) {
			jumpForward();
		} else if(shortCut.equals(KeyStroke.getKeyStroke(KeyEvent.VK_F2, InputEvent.SHIFT_MASK))) {
			jumpBackward();
		} else if(shortCut.equals(KeyStroke.getKeyStroke(KeyEvent.VK_F2, InputEvent.CTRL_MASK))) {
			toggleBookmark(activeObject);
		}
	}

	/**
	 * Bookmarks the given Object o, if it has not already been bookmarked. In this case o is
	 * deleted from the bookmark list.
	 */
	public void toggleBookmark(Object o) {
		if(o != null) {
			if(bookmarks.contains(o)) {
				bookmarks.remove(o);
			} else {
				if(activeBookmark < bookmarks.size()) {
					activeBookmark++;
				}

				bookmarks.add(activeBookmark, o);
			}

			if(dialog != null) {
				dialog.updateData();
			}
		}
	}

	/**
	 * DOCUMENT-ME
	 */
	public void toggleBookmark() {
		toggleBookmark(activeObject);
	}

	/**
	 * Differs from toggleBookmark in that way that it guarantees, that o is in the bookmark list
	 * after the call
	 */
	public void addBookmark(Object o) {
		if(bookmarks.contains(o)) {
			bookmarks.remove(o);
		}

		bookmarks.add(o);

		if(dialog != null) {
			dialog.updateData();
		}
	}

	/**
	 * DOCUMENT-ME
	 */
	public void clearBookmarks() {
		bookmarks.clear();
		activeBookmark = 0;

		if(dialog != null) {
			dialog.updateData();
		}
	}

	/**
	 * Jumps to the next bookmark by firing the corresponding selectionevent.
	 */
	public void jumpForward() {
		if(bookmarks.size() > 0) {
			if((activeBookmark < 0) || (activeBookmark >= bookmarks.size())) {
				// safety check
				activeBookmark = 0;
			}

			Object o = bookmarks.get(activeBookmark);

			if(o.equals(activeObject) && (bookmarks.size() > 1)) {
				activeBookmark++;
				jumpForward();
			} else if(!o.equals(activeObject)) {
				activeObject = o;
				dispatcher.fire(new SelectionEvent(this, null, o));

				if(dialog != null) {
					dialog.setSelectedObject(o);
				}
			}
		}
	}

	/**
	 * Jumps to the prior bookmark by firing the corresponding selectionevent.
	 */
	public void jumpBackward() {
		if(bookmarks.size() > 0) {
			if((activeBookmark < 0) || (activeBookmark >= bookmarks.size())) {
				activeBookmark = (bookmarks.size() - 1);
			}

			Object o = bookmarks.get(activeBookmark);

			if(o.equals(activeObject) && (bookmarks.size() > 1)) {
				activeBookmark--;
				jumpBackward();
			} else if(!o.equals(activeObject)) {
				activeObject = o;
				dispatcher.fire(new SelectionEvent(this, null, o));

				if(dialog != null) {
					dialog.setSelectedObject(o);
				}
			}
		}
	}

	/**
	 * 
	 */
	public List<Object> getBookmarks() {
		return bookmarks;
	}

	/**
	 * 
	 */
	public void selectionChanged(SelectionEvent se) {
		if(!se.getSource().equals(this) && (se.getActiveObject() != null)) {
			activeObject = se.getActiveObject();
		}
	}

	/**
	 * 
	 */
	public void gameDataChanged(GameDataEvent ge) {
		activeObject = null;
		bookmarks.clear();
		activeBookmark = 0;

		if(dialog != null) {
			dialog.updateData();
		}
	}

	/**
	 * 
	 */
	public java.lang.String getShortcutDescription(KeyStroke obj) {
		int index = shortCuts.indexOf(obj);

		return Resources.get("util.bookmarkmanager.shortcuts.description." + String.valueOf(index));
	}

	/**
	 * 
	 */
	public java.lang.String getListenerDescription() {
		return Resources.get("util.bookmarkmanager.shortcuts.title");
	}
	
	/**
	 * 
	 */
	public Object getActiveObject() {
	  return activeObject;
	}

  /**
   * Returns the value of activeBookmark.
   * 
   * @return Returns activeBookmark.
   */
  public int getActiveBookmark() {
    return activeBookmark;
  }

  /**
   * Sets the value of activeBookmark.
   *
   * @param activeBookmark The value for activeBookmark.
   */
  public void setActiveBookmark(int activeBookmark) {
    this.activeBookmark = activeBookmark;
  }

  /**
   * Returns the value of dialog.
   * 
   * @return Returns dialog.
   */
  public BookmarkDock getDialog() {
    return dialog;
  }

  /**
   * Sets the value of dialog.
   *
   * @param dialog The value for dialog.
   */
  public void setDialog(BookmarkDock dialog) {
    this.dialog = dialog;
  }

  /**
   * Sets the value of activeObject.
   *
   * @param activeObject The value for activeObject.
   */
  public void setActiveObject(Object activeObject) {
    this.activeObject = activeObject;
  }

  /**
   * Sets the value of bookmarks.
   *
   * @param bookmarks The value for bookmarks.
   */
  public void setBookmarks(List<Object> bookmarks) {
    this.bookmarks = bookmarks;
  }
	
	
}
