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

package magellan.library.impl;

import java.util.Collections;

import magellan.library.ID;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.Race;
import magellan.library.utils.Resources;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 256 $
 */
public class MagellanTempUnitImpl extends MagellanUnitImpl implements TempUnit {
  /** If this is a temp unit the parent is the unit that created this temp unit. */
  private Unit parent = null;
  private Race tempRace;

  /**
   * Creates a new TempUnit object.
   */
  public MagellanTempUnitImpl(ID id, Unit parent) {
    super(id);
    this.parent = parent;

    // pavkovic 2003.12.04: TempUnits have empty orders by default
    this.setOrders(Collections.singleton(""), false);
    this.clearOrders();
  }

  /**
   * Assigns this temp unit a parent unit.
   */
  public void setParent(Unit u) {
    this.parent = u;
  }

  /**
   * Returns the parent of this temp unit. If this is not a temp unit, null is
   * returned.
   */
  public Unit getParent() {
    return parent;
  }

  /**
   * Returns a string representation of this temporary unit.
   */

  @Override
  public String toString(boolean withName) {
    if (withName) {
      return super.toString(withName);
    } else {
      String temp = Resources.getOrderTranslation(EresseaConstants.O_TEMP);
      return temp + " " + id.toString();
    }
  }
  
  public void setTempRace(Race r){
    this.tempRace = r;
  }
  
  @Override
  public Race getRace() {
    if (tempRace == null)
      return super.getRace();
    else
      return tempRace;
  }
}
