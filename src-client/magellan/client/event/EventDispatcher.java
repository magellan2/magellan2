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

package magellan.client.event;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;

import magellan.client.MagellanContext;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.utils.guiwrapper.EventDispatcherInterface;
import magellan.library.utils.logging.Logger;

/**
 * A class forwarding events from event sources to listeners.
 */
public class EventDispatcher implements EventDispatcherInterface {
  private static final Logger log = Logger.getInstance(EventDispatcher.class);
  private List<Object> listeners[];
  private boolean notifierIsAliveOnList[];
  private boolean notifierIsAlive = false;
  private boolean stopNotification = false;
  private int eventsFired = 0;
  private int eventsDispatched = 0;
  private int lastPriority = Integer.MAX_VALUE;
  private static final int GAMEDATA = 0;
  private static final int SELECTION = 1;
  private static final int UNITORDERS = 2;
  private static final int TEMPUNIT = 3;
  private static final int ORDERCONFIRM = 4;

  private static final int PRIORITIES[] = { 0, 4, 1, 1, 1 };
  private EQueue queue;

  private static final int infoMilliSeks = 5000;
  
  /**
   * TODO DOCUMENT ME!
   */
  public EventDispatcher() {
    listeners = new List[EventDispatcher.PRIORITIES.length];
    notifierIsAliveOnList = new boolean[EventDispatcher.PRIORITIES.length];

    for (int i = 0; i < EventDispatcher.PRIORITIES.length; i++) {
      listeners[i] = new ArrayList<Object>();
      notifierIsAliveOnList[i] = false;
    }

    queue = new EQueue();

    Thread t = new Thread(new EManager());

    // t.setPriority(Thread.MIN_PRIORITY);
    t.start();
  }

  /**
   * Returns the Magellan Context.
   * 
   * TR: This method was deprecated. But I don't know what to do 
   * if I need the magellan context.
   */
  public MagellanContext getMagellanContext() {
    return context;
  }

  private MagellanContext context;

  /**
   * Sets the Magellan Context.
   */
  public void setMagellanContext(MagellanContext context) {
    this.context = context;
  }

  /**
   * Clones the List (and remove WeakReference objects with null target)
   */
  private List<Object> cloneList(List<Object> list) {
    return new ArrayList<Object>(list);
  }

  private void addListener(int pos, Object obj) {
    if (notifierIsAliveOnList[pos]) {
      // clone list before changing
      listeners[pos] = cloneList(listeners[pos]);
      EventDispatcher.log.error("The following exception shall be reported to bugzilla!", new Exception());
    }

    listeners[pos].add(obj);
//    if (listeners[pos].size()%10==0)
//      log.info(pos + " "+listeners[pos].size());
  }

  private void addPriorityListener(int pos, Object obj) {
    if (notifierIsAlive) {
      // clone list before changing
      listeners[pos] = cloneList(listeners[pos]);
      EventDispatcher.log.debug("The following exception shall be reported to bugzilla!", new Exception());
    }

    listeners[pos].add(0, obj);
  }

  private boolean removeListener(int pos, Object l) {
    if (notifierIsAlive) {
      // clone list before changing
      listeners[pos] = cloneList(listeners[pos]);
//      EventDispatcher.log.debug("The following exception shall be reported to bugzilla!", new Exception());
    }
//    if (listeners[pos].size()%10==0)
//      log.info(pos + " "+listeners[pos].size());

    return listeners[pos].remove(l);
  }

  /**
   * Adds a listener for selection events.
   * 
   * @param l
   *          the listener to add.
   * @see SelectionEvent
   */
  public void addSelectionListener(SelectionListener l) {
    addListener(EventDispatcher.SELECTION, l);
  }

  /**
   * Adds the given selection listener to the front of all registered listeners.
   * Warning: The order will change if another listener is added with priority.
   */
  public void addPrioritySelectionListener(SelectionListener l) {
    addPriorityListener(EventDispatcher.SELECTION, l);
  }

  /**
   * Removes the specified listener for selection events.
   * 
   * @param l
   *          the listener to remove.
   * @return true if this list contained the specified element.
   * @see SelectionEvent
   */
  public boolean removeSelectionListener(SelectionListener l) {
    return removeListener(EventDispatcher.SELECTION, l);
  }

  /**
   * Adds a listener for game data events.
   * 
   * @param l
   *          the listener to add.
   * @see GameDataEvent
   */
  public void addGameDataListener(GameDataListener l) {
    addListener(EventDispatcher.GAMEDATA, l);
  }

  /**
   * Adds the given game-data listener to the front of all registered listeners.
   * Warning: The order will change if another listener is added with priority.
   */
  public void addPriorityGameDataListener(GameDataListener l) {
    addPriorityListener(EventDispatcher.GAMEDATA, l);
  }

  /**
   * Removes the specified listener for game data events.
   * 
   * @param l
   *          the listener to remove.
   * @return true if this list contained the specified element.
   * @see GameDataEvent
   */
  public boolean removeGameDataListener(GameDataListener l) {
    return removeListener(EventDispatcher.GAMEDATA, l);
  }

  /**
   * Adds a listener for temp unit events.
   * 
   * @param l
   *          the listener to add.
   * @see TempUnitEvent
   */
  public void addTempUnitListener(TempUnitListener l) {
    addListener(EventDispatcher.TEMPUNIT, l);
  }

  /**
   * Adds the given temp-unit listener to the front of all registered listeners.
   * Warning: The order will change if another listener is added with priority.
   */
  public void addPriorityTempUnitListener(TempUnitListener l) {
    addPriorityListener(EventDispatcher.TEMPUNIT, l);
  }

  /**
   * Removes the specified listener for temp unit events.
   * 
   * @param l
   *          the listener to remove.
   * @return true if this list contained the specified element.
   * @see TempUnitEvent
   */
  public boolean removeTempUnitListener(TempUnitListener l) {
    return removeListener(EventDispatcher.TEMPUNIT, l);
  }

  /**
   * Adds a listener for unit orders events.
   * 
   * @param l
   *          the listener to add.
   * @see UnitOrdersEvent
   */
  public void addUnitOrdersListener(UnitOrdersListener l) {
    addListener(EventDispatcher.UNITORDERS, l);
  }

  /**
   * Adds the given unit-orders listener to the front of all registered
   * listeners. Warning: The order will change if another listener is added with
   * priority.
   */
  public void addPriorityUnitOrdersListener(UnitOrdersListener l) {
    addPriorityListener(EventDispatcher.UNITORDERS, l);
  }

  /**
   * Removes the specified listener for unit order events.
   * 
   * @param l
   *          the listener to remove.
   * @return true if this list contained the specified element.
   * @see UnitOrdersEvent
   */
  public boolean removeUnitOrdersListener(UnitOrdersListener l) {
    return removeListener(EventDispatcher.UNITORDERS, l);
  }

  /**
   * Removes the specified listener from all event queues
   * 
   * @param o
   *          the listener to remove.
   * @return true if one of the list contained the specified element.
   */
  public boolean removeAllListeners(Object o) {
    boolean result = false;

    if (o instanceof GameDataListener) {
      if (removeGameDataListener((GameDataListener) o)) {
        if (EventDispatcher.log.isDebugEnabled()) {
          EventDispatcher.log.debug("EventDispatcher.removeAllListeners: stale GameDataListener entry for " + o.getClass());
        }

        result = true;
      }
    }

    if (o instanceof TempUnitListener) {
      if (removeTempUnitListener((TempUnitListener) o)) {
        if (EventDispatcher.log.isDebugEnabled()) {
          EventDispatcher.log.debug("EventDispatcher.removeAllListeners: stale TempUnitListener entry for " + o.getClass());
        }

        result = true;
      }
    }

    if (o instanceof UnitOrdersListener) {
      if (removeUnitOrdersListener((UnitOrdersListener) o)) {
        if (EventDispatcher.log.isDebugEnabled()) {
          EventDispatcher.log.debug("EventDispatcher.removeAllListeners: stale UnitOrdersListener entry for " + o.getClass());
        }

        result = true;
      }
    }

    if (o instanceof SelectionListener) {
      if (removeSelectionListener((SelectionListener) o)) {
        if (EventDispatcher.log.isDebugEnabled()) {
          EventDispatcher.log.debug("EventDispatcher.removeAllListeners: stale SelectionListener entry for " + o.getClass());
        }

        result = true;
      }
    }

    if (o instanceof OrderConfirmListener) {
      if (removeOrderConfirmListener((OrderConfirmListener) o)) {
        if (EventDispatcher.log.isDebugEnabled()) {
          EventDispatcher.log.debug("EventDispatcher.removeAllListeners: stale OrderConfirmListener entry for " + o.getClass());
        }

        result = true;
      }
    }

    return result;
  }

  /**
   * Adds a listener for order confirm events.
   * 
   * @param l
   *          the listener to add.
   * @see OrderConfirmEvent
   */
  public void addOrderConfirmListener(OrderConfirmListener l) {
    addListener(EventDispatcher.ORDERCONFIRM, l);
  }

  /**
   * Adds the given order-confirm listener to the front of all registered
   * listeners. Warning: The order will change if another listener is added with
   * priority.
   */
  public void addPriorityOrderConfirmListener(OrderConfirmListener l) {
    addPriorityListener(EventDispatcher.ORDERCONFIRM, l);
  }

  /**
   * Removes the specified listener for order confirm events.
   * 
   * @param l
   *          the listener to remove.
   * @return true if this list contained the specified element.
   * @see OrderConfirmEvent
   */
  public boolean removeOrderConfirmListener(OrderConfirmListener l) {
    return removeListener(EventDispatcher.ORDERCONFIRM, l);
  }

  /**
   * Forwards an event to all registered listeners for this event type.
   * <p>
   * If synchronous is false, the forwarding is done asynchronously in a
   * separate dispatcher thread. If the fire method is called before the
   * dispatcher thread has finished the previous request, it is stopped and
   * starts forwarding the new event.
   * </p>
   */
  public void fire(EventObject e, boolean synchronous) {
    if (EventDispatcher.log.isDebugEnabled()) {
      EventDispatcher.log.debug("EventDispatcher(" + e + "," + synchronous + "): fired event ", new Exception());
    }

    if (synchronous) {
      new Notifier(e).run();
    } else {
      queue.push(e);
    }
  }

  /**
   * Asynchronously forwards an event to all registered listeners for this event
   * type.
   */
  public void fire(EventObject e) {
    fire(e, false);
  }

  /**
   * Returns the number of events that were passed to this dispatcher for
   * forwarding.
   */
  public int getEventsFired() {
    return eventsFired;
  }

  /**
   * Returns the number of events that were actually forwarded to event
   * listeners.
   */
  public int getEventsDispatched() {
    return eventsDispatched;
  }

  private class EManager implements Runnable {
    /**
     * DOCUMENT-ME
     */
    public void run() {
      while (true) {
        try {
          EventObject o = queue.waitFor();

          long start = 0;

          int prio = getPriority(o);

          eventsFired++;

          // if some notifier has already been started and it should stop we
          // have to wait for it to die
          // of course the waiting time should be bounded

          /*
           * Note: I think it's better to use a priority system. Some events
           * should not be interrupted like GameDataEvents since data integrity
           * would be lost. Andreas
           */
          if (notifierIsAlive) {
            if (prio < lastPriority) { // interrupt the old notifier
              stopNotification = true;
              start = System.currentTimeMillis();

              while (stopNotification && ((System.currentTimeMillis() - start) < 2000)) {
                Thread.yield();
              }
            } else { // wait for the notifier

              do {
                Thread.yield();
              } while (notifierIsAlive);
            }
          }

          stopNotification = false;

          lastPriority = prio;
          SwingUtilities.invokeLater(new Notifier(o));
        } catch (InterruptedException ie) {
        }
      }
    }
  }

  private class EQueue {
    private List<EventObject> objects = new LinkedList<EventObject>();
    private boolean block = false;

    /**
     * DOCUMENT-ME
     */
    public synchronized EventObject poll() {
      if (block) {
        try {
          this.wait();
        } catch (InterruptedException ie) {
        }
      }

      return objects.remove(0);
    }

    /**
     * DOCUMENT-ME
     * 
     * @throws InterruptedException
     *           DOCUMENT-ME
     */
    public synchronized EventObject waitFor() throws InterruptedException {
      if (block) {
        this.wait();
      }

      if (objects.size() == 0) {
        this.wait();
      }

      return poll();
    }

    /**
     * DOCUMENT-ME
     */
    public synchronized void push(EventObject o) {
      int index = 0;

      if (objects.size() > 0) {
        int prioNew = getPriority(o);
        block = true;

        while (index < objects.size()) {
          EventObject obj = objects.get(index);
          int prioOld = getPriority(obj);

          if (prioOld > prioNew) {
            do {
              objects.remove(index);
            } while (index < objects.size());
          } else {
            index++;
          }
        }

        block = false;
      }

      objects.add(index, o);
      this.notifyAll();
    }
  }

  protected int getPriority(EventObject e) {
    int prio = -1;

    if (e instanceof GameDataEvent) {
      prio = EventDispatcher.PRIORITIES[EventDispatcher.GAMEDATA];
    } else if (e instanceof SelectionEvent) {
      prio = EventDispatcher.PRIORITIES[EventDispatcher.SELECTION];
    } else if (e instanceof UnitOrdersEvent) {
      prio = EventDispatcher.PRIORITIES[EventDispatcher.UNITORDERS];
    } else if (e instanceof TempUnitEvent) {
      prio = EventDispatcher.PRIORITIES[EventDispatcher.TEMPUNIT];
    } else if (e instanceof OrderConfirmEvent) {
      prio = EventDispatcher.PRIORITIES[EventDispatcher.ORDERCONFIRM];
    }

    return prio;
  }

  private class Notifier implements Runnable {
    private EventObject event = null;

    /**
     * Creates a new Notifier object.
     */
    public Notifier(EventObject e) {
      this.event = e;
    }

    /**
     * DOCUMENT-ME
     */
    public void run() {
      if (EventDispatcher.log.isDebugEnabled()) {
        EventDispatcher.log.debug("EventDispatcher.Notifier.run called ", new Exception());
      }
      long timeWatchStart = 0;
      long timeWatchEnd = 0;
      notifierIsAlive = true;
      
      // the for loops are duplicated for each event type to
      // avoid a lot of expensive class casts and instanceof
      // operations
      if (event instanceof SelectionEvent) {
        SelectionEvent e = (SelectionEvent) event;
        
        notifierIsAliveOnList[EventDispatcher.SELECTION] = true;

        for (Iterator iter = listeners[EventDispatcher.SELECTION].iterator(); iter.hasNext() && !EventDispatcher.this.stopNotification;) {
          // Object o = ((WeakReference) iter.next()).get();
          Object o = iter.next();

          if (o != null) {
            eventsDispatched++;
            try {
              timeWatchStart = System.currentTimeMillis();
              ((SelectionListener) o).selectionChanged(e);
              timeWatchEnd = System.currentTimeMillis();
              if ((timeWatchEnd-timeWatchStart) > EventDispatcher.infoMilliSeks){
                EventDispatcher.log.info("Notify took " + (timeWatchEnd-timeWatchStart) + "ms for SELECTION-notify from " + event.getSource().getClass().getName() + " in " + ((SelectionListener) o).getClass().getName());
              }
            } catch (Exception ex) {
              EventDispatcher.log.error("An Exception occured in the EventDispatcher", ex);
            }
          }

          if (EventDispatcher.this.stopNotification) {
            EventDispatcher.this.stopNotification = false;
          }
        }
        notifierIsAliveOnList[EventDispatcher.SELECTION] = false;
      } else if (event instanceof OrderConfirmEvent) {
        OrderConfirmEvent e = (OrderConfirmEvent) event;

        notifierIsAliveOnList[EventDispatcher.ORDERCONFIRM] = true;

        for (Iterator iter = listeners[EventDispatcher.ORDERCONFIRM].iterator(); iter.hasNext() && !EventDispatcher.this.stopNotification;) {
          // Object o = ((WeakReference) iter.next()).get();
          Object o = iter.next();

          if (o != null) {
            eventsDispatched++;
            try {
              ((OrderConfirmListener) o).orderConfirmationChanged(e);
            } catch (Exception ex) {
              EventDispatcher.log.error("An Exception occured in the EventDispatcher", ex);
            }
          }

          if (EventDispatcher.this.stopNotification) {
            EventDispatcher.this.stopNotification = false;
          }
        }

        notifierIsAliveOnList[EventDispatcher.ORDERCONFIRM] = false;
      } else if (event instanceof UnitOrdersEvent) {
        UnitOrdersEvent e = (UnitOrdersEvent) event;

        notifierIsAliveOnList[EventDispatcher.UNITORDERS] = true;

        for (Iterator iter = listeners[EventDispatcher.UNITORDERS].iterator(); iter.hasNext() && !EventDispatcher.this.stopNotification;) {
          // Object o = ((WeakReference) iter.next()).get();
          Object o = iter.next();

          if (o != null) {
            eventsDispatched++;
            try {
              ((UnitOrdersListener) o).unitOrdersChanged(e);
            } catch (Exception ex) {
              EventDispatcher.log.error("An Exception occured in the EventDispatcher", ex);
            }

          }

          if (EventDispatcher.this.stopNotification) {
            EventDispatcher.this.stopNotification = false;
          }
        }

        notifierIsAliveOnList[EventDispatcher.UNITORDERS] = false;
      } else if (event instanceof TempUnitEvent) {
        TempUnitEvent e = (TempUnitEvent) event;

        notifierIsAliveOnList[EventDispatcher.TEMPUNIT] = true;

        for (Iterator iter = listeners[EventDispatcher.TEMPUNIT].iterator(); iter.hasNext() && !EventDispatcher.this.stopNotification;) {
          // Object o = ((WeakReference) iter.next()).get();
          Object o = iter.next();

          if (o != null) {
            eventsDispatched++;

            TempUnitListener l = (TempUnitListener) o;

            try {
              if (e.getType() == TempUnitEvent.CREATED) {
                l.tempUnitCreated(e);
              } else if (e.getType() == TempUnitEvent.DELETING) {
                l.tempUnitDeleting(e);
              }
            } catch (Exception ex) {
              EventDispatcher.log.error("An Exception occured in the EventDispatcher", ex);
            }

          }

          if (EventDispatcher.this.stopNotification) {
            EventDispatcher.this.stopNotification = false;
          }
        }

        notifierIsAliveOnList[EventDispatcher.TEMPUNIT] = false;
      } else if (event instanceof GameDataEvent) {
        GameDataEvent e = (GameDataEvent) event;

        notifierIsAliveOnList[EventDispatcher.GAMEDATA] = true;
        
        for (Iterator iter = listeners[EventDispatcher.GAMEDATA].iterator(); iter.hasNext() && !EventDispatcher.this.stopNotification;) {
          // Object o = ((WeakReference) iter.next()).get();
          Object o = iter.next();

          if (o != null) {
            eventsDispatched++;
            try {
              timeWatchStart = System.currentTimeMillis();
              ((GameDataListener) o).gameDataChanged(e);
              timeWatchEnd = System.currentTimeMillis();
              if ((timeWatchEnd-timeWatchStart) > EventDispatcher.infoMilliSeks){
                EventDispatcher.log.info("Notify took " + (timeWatchEnd-timeWatchStart) + "ms for GAMEDATA-notify from " + event.getSource().getClass().getName() + " in " + ((GameDataListener) o).getClass().getName());
              }
            } catch (Exception ex) {
              EventDispatcher.log.error("An Exception occured in the EventDispatcher", ex);
            }
          }

          if (EventDispatcher.this.stopNotification) {
            EventDispatcher.this.stopNotification = false;
          }
        }
        notifierIsAliveOnList[EventDispatcher.GAMEDATA] = false;
      }

      // 2002.03.04 pavkovic: get rid of evil Event, seems that Notifier will
      // not
      // be removed correctly
      event = null;
      notifierIsAlive = false;
      lastPriority = Integer.MAX_VALUE;
    }
  }
}
