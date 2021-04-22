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

package magellan.library.rules;

import magellan.library.ID;
import magellan.library.StringID;

/**
 * This class contains info about a special faction.
 * 
 * @author stm
 */
public class FactionType extends ObjectType {

  private boolean monster;
  private int factionId;

  /**
   * @param id
   */
  public FactionType(ID id) {
    super(id);
  }

  /**
   * @see magellan.library.impl.MagellanNamedImpl#toString()
   */
  @Override
  public String toString() {
    return "Faction[" + getID() + "]";
  }

  /**
   * Returns the id uniquely identifying this object.
   * 
   * @see magellan.library.impl.MagellanIdentifiableImpl#getID()
   */
  @Override
  public StringID getID() {
    return (StringID) id;
  }

  /**
   * Sets the monster property.
   */
  public void setMonster(boolean val) {
    monster = val;
  }

  /**
   * Returns the mosnter property.
   */
  public boolean isMonster() {
    return monster;
  }

  public int getNumber() {
    return factionId;
  }

  public void setNumber(int factionId) {
    this.factionId = factionId;
  }

}
