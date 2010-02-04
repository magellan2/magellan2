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
import java.util.Collection;

import magellan.library.Unit;
import magellan.library.event.TimeStampedEvent;

/**
 * An event indicating that the order confirmation status of one or more units has changed.
 * 
 * @see OrderConfirmListener
 * @see EventDispatcher
 */
public class OrderConfirmEvent extends TimeStampedEvent {
  private Collection<Unit> units;
  private boolean changedToUnConfirmed = false;

  /**
   * Constructs a new order confirmation event.
   * 
   * @param source the object issuing the event.
   * @param units the units which order confirmation status was modified.
   */
  public OrderConfirmEvent(Object source, Collection<Unit> units) {
    super(source);
    // we need to copy the list in case the original collection gets modified before the event is
    // handled by all listeners...
    this.units = units == null ? null : new ArrayList<Unit>(units);
    changedToUnConfirmed = calcChangedToUnConfirmed();
  }

  /**
   * Returns the units affected by the event.
   */
  public Collection<Unit> getUnits() {
    return units;
  }

  /**
   * BUG in JTree. UI calculates the bounding not correct if text is not bold when init
   * Overviewpanel - tree calls updateUI if one or more units had changed the order confirm to yes
   * this is here calculated
   * 
   * @return true, if one or more units are confirmed, else false
   * @author Fiete
   */
  private boolean calcChangedToUnConfirmed() {
    if (units == null)
      return false;
    for (Unit u : units) {
      if (!u.isOrdersConfirmed())
        return true;
    }
    return false;
  }

  /**
   * @return <code>true</code> if one of the units is unconfirmed
   * @deprecated (stm) does this belong here? Not needed any more (was used by EMapOverviewPanel).
   */
  @Deprecated
  public boolean changedToUnConfirmed() {
    return changedToUnConfirmed;
  }

}
