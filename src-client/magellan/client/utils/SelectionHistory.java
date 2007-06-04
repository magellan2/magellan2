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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.event.ChangeListener;

import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.library.utils.Bucket;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class SelectionHistory {
	private static SelectionListener eventHook = new EventHook();
	private static Bucket<Object> history = new Bucket<Object>(10);
	private static Collection<Object> ignoredSources = new HashSet<Object>();
	private static List<ChangeListener> listeners = new ArrayList<ChangeListener>();

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static SelectionListener getEventHook() {
		return eventHook;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static void selectionChanged(SelectionEvent e) {
		if((e.getActiveObject() != null) && !ignoredSources.contains(e.getSource())) {
			history.add(e.getActiveObject());
			informListeners();
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static void addListener(ChangeListener l) {
		listeners.add(l);
	}

	private static void informListeners() {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					Iterator it = listeners.iterator();

					while(it.hasNext()) {
						((ChangeListener) it.next()).stateChanged(null);
					}
				}
			});
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static Collection getHistory() {
		return history;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static void ignoreSource(Object o) {
		ignoredSources.add(o);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static void unignoreSource(Object o) {
		ignoredSources.remove(o);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static void setMaxSize(int i) {
		history.setMaxSize(i);
	}

	/**
	 * DOCUMENT-ME
	 */
	public static void clear() {
		history.clear();
	}

	private static class EventHook implements SelectionListener {
		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public void selectionChanged(SelectionEvent e) {
			SelectionHistory.selectionChanged(e);
		}
	}
}
