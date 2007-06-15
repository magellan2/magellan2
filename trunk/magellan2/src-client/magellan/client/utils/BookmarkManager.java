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

import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import magellan.client.desktop.DesktopEnvironment;
import magellan.client.desktop.ShortcutListener;
import magellan.client.event.EventDispatcher;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.utils.Resources;


/**
 * DOCUMENT ME!
 *
 * @author Ulrich Küster Manages setting an getting of Bookmarks. - CTRL + F2 : place a bookmark on
 * 		   the current activeObject or removes it if already bookmarked - F2 : go to next bookmark
 * 		   - Shift + F2 : go to prior bookmark
 */
public class BookmarkManager implements ShortcutListener, SelectionListener, GameDataListener {
	private EventDispatcher dispatcher;
	private List<KeyStroke> shortCuts = new LinkedList<KeyStroke>();
	private Object activeObject = null;

	// the list containing the bookmarked objects
	private Vector<Object> bookmarks = new Vector<Object>();

	// the number of the current bookmark
	private int activeBookmark = 0;
	private Properties settings;
	private BookmarkDialog dialog;

	/**
	 * Creates a new BookmarkManager object.
	 *
	 * 
	 * 
	 */
	public BookmarkManager(EventDispatcher dispatcher, Properties settings) {
		this.dispatcher = dispatcher;
		this.settings = settings;
		dispatcher.addSelectionListener(this);

		shortCuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		shortCuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_F2, KeyEvent.SHIFT_MASK));
		shortCuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_F2, KeyEvent.CTRL_MASK));
		DesktopEnvironment.registerShortcutListener(this);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Iterator<KeyStroke> getShortCuts() {
		return shortCuts.iterator();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void shortCut(KeyStroke shortCut) {
		if(shortCut.equals(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0))) {
			jumpForward();
		} else if(shortCut.equals(KeyStroke.getKeyStroke(KeyEvent.VK_F2, KeyEvent.SHIFT_MASK))) {
			jumpBackward();
		} else if(shortCut.equals(KeyStroke.getKeyStroke(KeyEvent.VK_F2, KeyEvent.CTRL_MASK))) {
			toggleBookmark(activeObject);
		}
	}

	/**
	 * Bookmarks the given Object o, if it has not already been bookmarked. In this case o is
	 * deleted from the bookmark list.
	 *
	 * 
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
	 *
	 * 
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
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public List getBookmarks() {
		return bookmarks;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void selectionChanged(SelectionEvent se) {
		if(!se.getSource().equals(this) && (se.getActiveObject() != null)) {
			activeObject = se.getActiveObject();
		}
	}

	/**
	 * DOCUMENT-ME
	 *
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
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public java.lang.String getShortcutDescription(java.lang.Object obj) {
		int index = shortCuts.indexOf(obj);

		return Resources.get("magellan.util.bookmarkmanager.shortcuts.description." + String.valueOf(index));
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public java.lang.String getListenerDescription() {
		return Resources.get("magellan.util.bookmarkmanager.shortcuts.title");
	}

	/**
	 * displays a dialog with the bookmarks
	 *
	 * 
	 */
	public void showDialog(Frame owner) {
		if(dialog != null) {
			dialog.quit();
		}

		dialog = new BookmarkDialog(owner);
		dialog.show();
	}

	/**
	 * A little dialog showing the bookmarks
	 */
	private class BookmarkDialog extends JDialog implements SelectionListener {
		private JList list;

		/**
		 * Creates a new BookmarkDialog object.
		 *
		 * 
		 */
		public BookmarkDialog(Frame owner) {
			super(owner, Resources.get("magellan.util.bookmarkmanager.bookmarkdialog.caption"), false);
			dispatcher.addSelectionListener(this);

			this.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						quit();
					}
				});

			int width = Integer.parseInt(settings.getProperty("BookmarkManager.DialogWidth", "300"));
			int height = Integer.parseInt(settings.getProperty("BookmarkManager.DialogHeight", "500"));
			setSize(width, height);

			int xPos = Integer.parseInt(settings.getProperty("BookmarkManager.DialogXPos", "400"));
			int yPos = Integer.parseInt(settings.getProperty("BookmarkManager.DialogYPos", "200"));
			setLocation(xPos, yPos);
			list = new JList();
			updateData();
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						if(!e.getValueIsAdjusting()) {
							Object selectedValue = list.getSelectedValue();

							if((selectedValue != null) && (selectedValue != activeObject)) {
								dispatcher.fire(new SelectionEvent(this, null, selectedValue));
							}
						}
					}
				});
			list.addKeyListener(new KeyAdapter() {
					public void keyReleased(KeyEvent e) {
						if(e.getKeyCode() == KeyEvent.VK_F2) {
							if(e.getModifiers() == KeyEvent.CTRL_MASK) {
								BookmarkManager.this.toggleBookmark();
							} else if(e.getModifiers() == 0) {
								BookmarkManager.this.jumpForward();
							} else if(e.getModifiers() == KeyEvent.SHIFT_MASK) {
								BookmarkManager.this.jumpBackward();
							}
						} else if(e.getKeyCode() == KeyEvent.VK_DELETE) {
							BookmarkManager.this.toggleBookmark();
						}
					}
				});
			getContentPane().add(new JScrollPane(list));
		}

		/**
		 * DOCUMENT-ME
		 */
		public void updateData() {
			DefaultListModel model = new DefaultListModel();

			for(Iterator iter = bookmarks.listIterator(); iter.hasNext();) {
				model.addElement(iter.next());
			}

			list.setModel(model);
			list.setSelectedValue(activeObject, true);
			list.revalidate();
			list.repaint();
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public void setSelectedObject(Object o) {
			list.setSelectedValue(o, true);
			list.repaint();
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public void selectionChanged(SelectionEvent se) {
			Object o = se.getActiveObject();

			if(o != null) {
				if(bookmarks.contains(o)) {
					list.setSelectedValue(o, true);
					activeBookmark = ((DefaultListModel) list.getModel()).indexOf(o);
				} else {
					list.clearSelection();
				}
			}
		}

		/**
		 * DOCUMENT-ME
		 */
		public void quit() {
			settings.put("BookmarkManager.DialogWidth", String.valueOf(this.getWidth()));
			settings.put("BookmarkManager.DialogHeight", String.valueOf(this.getHeight()));
			settings.put("BookmarkManager.DialogXPos", String.valueOf(this.getX()));
			settings.put("BookmarkManager.DialogYPos", String.valueOf(this.getY()));
			dispose();
		}
	}

	// pavkovic 2003.01.28: this is a Map of the default Translations mapped to this class
	// it is called by reflection (we could force the implementation of an interface,
	// this way it is more flexible.)
	// Pls use this mechanism, so the translation files can be created automagically
	// by inspecting all classes.
	private static Map<String,String> defaultTranslations;

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static synchronized Map<String,String> getDefaultTranslations() {
		if(defaultTranslations == null) {
			defaultTranslations = new Hashtable<String,String>();
			defaultTranslations.put("shortcuts.title", "Bookmarks");
			defaultTranslations.put("shortcuts.description.2", "Set/reset bookmark on active object");
			defaultTranslations.put("shortcuts.description.1", "Last bookmark");
			defaultTranslations.put("shortcuts.description.0", "Next bookmark");
			defaultTranslations.put("bookmarkdialog.caption", "Bookmarks");
		}

		return defaultTranslations;
	}
}
