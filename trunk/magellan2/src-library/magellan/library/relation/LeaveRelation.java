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

package magellan.library.relation;

import magellan.library.Unit;
import magellan.library.UnitContainer;

/**
 * A relation indicating that a unit leaves a unit container.
 */
public class LeaveRelation extends UnitContainerRelation {
  private boolean implicit;

  /**
   * Creates a new LeaveRelation object.
   * 
   * @param s The leaving unit
   * @param t The left container
   * @param line The order line that caused this
   */
  public LeaveRelation(Unit s, UnitContainer t, int line) {
    super(s, t, line);
  }

  /**
   * Creates a new LeaveRelation object.
   * 
   * @param s The leaving unit
   * @param t The left container
   * @param line The order line that caused this
   * @param implicit indicates that another command (like ENTER or MOVE) caused this
   * @param warning warning flag
   */
  public LeaveRelation(Unit s, UnitContainer t, int line, boolean implicit, boolean warning) {
    super(s, t, line, warning);
    this.implicit = implicit;
  }

  /**
   * Returns true if this relation is not explicitly caused by a command.
   */
  public boolean isImplicit() {
    return implicit;
  }

}
