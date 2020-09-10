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
 * Records selection events.
 */
public class SelectionHistory {
  /**
   * Encapsulates a Selectionevent
   */
  public static class SelectionEntry {
    /** The Selection that represents this entry. */
    public SelectionEvent event;

    protected SelectionEntry(SelectionEvent e) {
      event = e;
    }

    /**
     * Two Entries are equal if they have the same context, but not necessarily the same source.
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
      if (obj instanceof SelectionEntry)
        return event.equals(((SelectionEntry) obj).event);

      return false;
    }

    @Override
    public String toString() {
      if (event.getContexts().size() > 1)
        return event.getActiveObject() + "+" + (event.getContexts().size() - 1);
      else
        return event.getActiveObject().toString();
    }

    @Override
    public int hashCode() {
      return event.hashCode();
    }
  }

  /**
   * Number of entries in the history list.
   */
  public static final int HISTORY_SIZE = 42;

  private static EventHook eventHook = new EventHook();
  private static Bucket<SelectionEntry> history = new Bucket<SelectionEntry>(HISTORY_SIZE);
  private static Collection<Object> ignoredSources = new HashSet<Object>();
  private static List<ChangeListener> listeners = new ArrayList<ChangeListener>();
  private static SelectionEvent lastEvent;

  /**
   * Returns the SelectionListener of this history.
   */
  public static SelectionListener getSelectionEventHook() {
    return SelectionHistory.eventHook;
  }

  /**
   * Returns the TempUnitListener of this history.
   */
  public static TempUnitListener getTempUnitEventHook() {
    return SelectionHistory.eventHook;
  }

  /**
   * Adds active object to history if it is not from an ignored source.
   * 
   * @param e
   */
  public static void selectionChanged(SelectionEvent e) {
    // ignore empty events and events from ignored sources
    if (e.getSelectionType() == SelectionEvent.ST_DEFAULT) {
      if ((e.getActiveObject() != null) && !SelectionHistory.ignoredSources.contains(e.getSource())) {
        // do not change list if the last event is repeated
        if (!e.equals(SelectionHistory.lastEvent)) {
          // cut of head of list up to selected entry
          SelectionEntry newEntry = new SelectionEntry(e);
          SelectionEntry lastEntry = new SelectionEntry(SelectionHistory.lastEvent);
          for (Iterator<SelectionEntry> it = SelectionHistory.history.iterator(); it.hasNext();) {
            if (it.next().equals(lastEntry)) {
              break;
            } else {
              it.remove();
            }
          }

          // insert new entry
          SelectionHistory.history.add(newEntry);
          SelectionHistory.informListeners();
          SelectionHistory.lastEvent = e;
        }
      }
    }
  }

  /**
   * Registers another listener.
   */
  public static void addListener(ChangeListener l) {
    SelectionHistory.listeners.add(l);
  }

  /**
   * Asynchronously notifies all ChangeListeners
   */
  private static void informListeners() {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        Iterator<ChangeListener> it = SelectionHistory.listeners.iterator();

        while (it.hasNext()) {
          (it.next()).stateChanged(null);
        }
      }
    });
  }

  /**
   * Returns the history of events
   */
  public static Collection<SelectionEntry> getHistory() {
    return SelectionHistory.history;
  }

  /**
   * Returns the history of events
   */
  public static SelectionEntry getHistory(int index) {
    return SelectionHistory.history.get(index);
  }

  /**
   * Events from sources registered here will be ignored by
   * {@link #selectionChanged(SelectionEvent)}.
   * 
   * @see #selectionChanged(SelectionEvent)
   */
  public static void ignoreSource(Object source) {
    SelectionHistory.ignoredSources.add(source);
  }

  /**
   * Events from this source will no longer be ignored.
   * 
   * @see #ignoreSource(Object)
   */
  public static void unignoreSource(Object source) {
    SelectionHistory.ignoredSources.remove(source);
  }

  /**
   * Sets the maximum number of events that the history holds.
   */
  public static void setMaxSize(int i) {
    SelectionHistory.history.setMaxSize(i);
  }

  /**
   * Empties the history.
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
      // no change necessary
    }

    /**
     * @see magellan.client.event.TempUnitListener#tempUnitDeleting(magellan.client.event.TempUnitEvent)
     */
    public void tempUnitDeleting(TempUnitEvent e) {
      SelectionHistory.tempUnitDeleting(e);
    }
  }

  /**
   * Removes entries corresponding to the temp unit from the history
   * 
   * @param e
   */
  public static void tempUnitDeleting(TempUnitEvent e) {
    for (Iterator<SelectionEntry> it = SelectionHistory.getHistory().iterator(); it.hasNext();) {
      SelectionEvent se = it.next().event;
      if (se.getActiveObject().equals(e.getTempUnit())) {
        it.remove();
      }
    }
  }

}
