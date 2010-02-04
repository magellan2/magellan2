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

/*
 * TimeStampedEvent.java
 *
 * Created on 18. Februar 2002, 13:52
 */
package magellan.library.event;

import java.util.EventObject;

/**
 * Event which remembers its creation time.
 * 
 * @author Andreas
 * @version 1.0
 */
public abstract class TimeStampedEvent extends EventObject {
  private long timestamp;

  /**
   * Creates new TimeStampedEvent
   */
  public TimeStampedEvent(Object source) {
    super(source);
    timestamp = System.currentTimeMillis();
  }

  /**
   * Returns the time when the event was created.
   */
  public long getTimestamp() {
    return timestamp;
  }
}
