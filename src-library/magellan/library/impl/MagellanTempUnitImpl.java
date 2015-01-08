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

import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.RulesException;
import magellan.library.rules.Race;

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
  public MagellanTempUnitImpl(UnitID id, MagellanUnitImpl parent) {
    super(id, parent.getData());
    this.parent = parent;

    // pavkovic 2003.12.04: TempUnits have empty orders by default
    this.setOrders(Collections.singleton(""), false);
    this.clearOrders();
  }

  /**
   * Assigns this temp unit a parent unit.
   */
  public void setParent(Unit u) {
    parent = u;
  }

  /**
   * Returns the parent of this temp unit. If this is not a temp unit, null is returned.
   */
  public Unit getParent() {
    return parent;
  }

  /**
   * Returns a string representation of this temporary unit.
   */

  @Override
  public String toString(boolean withName) {
    if (withName)
      return super.toString(withName);
    else {
      try {
        return getData().getGameSpecificStuff().getOrderChanger().getTokenLocalized(getLocale(),
            getID());
      } catch (RulesException e) {
        return EresseaConstants.O_TEMP + getID();
      }
    }
  }

  public void setTempRace(Race r) {
    tempRace = r;
  }

  @Override
  public Race getRace() {
    if (tempRace == null)
      return super.getRace();
    else
      return tempRace;
  }

  /**
   * Returns the id uniquely identifying this object.
   */
  @Override
  public UnitID getID() {
    return super.getID();
  }

}
