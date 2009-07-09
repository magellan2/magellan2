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

package magellan.library;

import magellan.library.rules.Race;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 256 $
 */
public interface TempUnit extends Unit {
  /**
   * Assigns this temp unit a parent unit.
   */
  public void setParent(Unit u);

  /**
   * Returns the parent of this temp unit. If this is not a temp unit, null is
   * returned.
   */
  public Unit getParent();
  
  /**
   * Returns a string representation of this temporary unit.
   */

  public String toString(boolean withName);

  
  /**
   * If the temp unit has a different race than the parent unit, set it here.
   */
  public void setTempRace(Race r);

}
