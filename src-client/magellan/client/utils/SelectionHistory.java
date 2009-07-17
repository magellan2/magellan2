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
import magellan.client.event.TempUnitEvent;
import magellan.client.event.TempUnitListener;
import magellan.library.utils.Bucket;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class SelectionHistory {
	private static EventHook eventHook = new EventHook();
	private static Bucket<Object> history = new Bucket<Object>(10);
	private static Collection<Object> ignoredSources = new HashSet<Object>();
	private static List<ChangeListener> listeners = new ArrayList<ChangeListener>();

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static SelectionListener getSelectionEventHook() {
		return SelectionHistory.eventHook;
	}

  public static TempUnitListener getTempUnitEventHook() {
    return SelectionHistory.eventHook;
  }

	/**
	 * Adds active object to history.
	 *
	 * @param e
	 */
	public static void selectionChanged(SelectionEvent e) {
		if((e.getActiveObject() != null) && !SelectionHistory.ignoredSources.contains(e.getSource())){
			SelectionHistory.history.add(e.getActiveObject());
			SelectionHistory.informListeners();
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static void addListener(ChangeListener l) {
		SelectionHistory.listeners.add(l);
	}

	private static void informListeners() {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					Iterator it = SelectionHistory.listeners.iterator();

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
		return SelectionHistory.history;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static void ignoreSource(Object o) {
		SelectionHistory.ignoredSources.add(o);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static void unignoreSource(Object o) {
		SelectionHistory.ignoredSources.remove(o);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static void setMaxSize(int i) {
		SelectionHistory.history.setMaxSize(i);
	}

	/**
	 * DOCUMENT-ME
	 */
	public static void clear() {
		SelectionHistory.history.clear();
	}

	private static class EventHook implements SelectionListener, TempUnitListener {
		/**
		 * @see magellan.client.event.SelectionListener#selectionChanged(magellan.client.event.SelectionEvent)
		 */
		public void selectionChanged(SelectionEvent e) {
			SelectionHistory.selectionChanged(e);
		}

    /**
     * @see magellan.client.event.TempUnitListener#tempUnitCreated(magellan.client.event.TempUnitEvent)
     */
    public void tempUnitCreated(TempUnitEvent e) {
      
    }

    /**
     * @see magellan.client.event.TempUnitListener#tempUnitDeleting(magellan.client.event.TempUnitEvent)
     */
    public void tempUnitDeleting(TempUnitEvent e) {
      SelectionHistory.tempUnitDeleting(e);
    }
	}

  /**
   * @param e
   */
  public static void tempUnitDeleting(TempUnitEvent e) {
    SelectionHistory.getHistory().remove(e.getTempUnit());
    
  }
}
