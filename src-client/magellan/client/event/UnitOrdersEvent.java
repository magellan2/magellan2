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

import magellan.library.Unit;
import magellan.library.event.TimeStampedEvent;

/**
 * An event indicating that the orders of a certain unit were modified.
 * 
 * @see UnitOrdersListener
 * @see EventDispatcher
 */
public class UnitOrdersEvent extends TimeStampedEvent {
  private Unit unit;

  // private boolean changing;

  /**
   * Creates an event for a change of a single unit.
   * 
   * @param source
   * @param unit
   */
  public UnitOrdersEvent(Object source, Unit unit) {
    super(source);
    this.unit = unit;

    // this(source, unit, false);
  }

  // /**
  // * Creates an event object that signals start or end of a batch of changes.
  // *
  // * @param source the object that originated the event.
  // * @param changing <code>true</code> if this starts a batch of changes, <code>false</code> if it
  // * ends a batch of changes.
  // */
  // public UnitOrdersEvent(Object source, boolean changing) {
  // this(source, null, changing);
  // }

  // /**
  // * Creates an event object.
  // *
  // * @param source the object that originated the event.
  // * @param unit the unit which orders changed.
  // */
  // public UnitOrdersEvent(Object source, Unit unit, boolean changing) {
  // super(source);
  // this.unit = unit;
  // this.changing = changing;
  // }

  /**
   * Returns the unit which orders changed.
   */
  public Unit getUnit() {
    return unit;
  }

  // /**
  // * Returns the value of changing.
  // *
  // * @return Returns changing.
  // */
  // public boolean isChanging() {
  // return changing;
  // }
  //
  // /**
  // * Sets the value of changing.
  // *
  // * @param changing The value for changing.
  // */
  // public void setChanging(boolean changing) {
  // this.changing = changing;
  // }

  @Override
  public String toString() {
    return super.toString() + "[u=" + unit /* + ",changing=" + changing */+ "]";
  }

}
