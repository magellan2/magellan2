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
 * This class contains an abstract order.
 * 
 * @author stm
 */
public class OrderType extends ObjectType {

  String syntax;
  boolean internal;
  boolean active;

  /**
   * @param id
   */
  public OrderType(ID id) {
    super(id);
  }

  /**
   * Returns the value of syntax.
   * 
   * @return Returns syntax.
   */
  public String getSyntax() {
    return syntax;
  }

  /**
   * Sets the value of syntax.
   * 
   * @param syntax The value for syntax.
   */
  public void setSyntax(String syntax) {
    this.syntax = syntax;
  }

  /**
   * Returns the value of internal.
   * 
   * @return Returns internal.
   */
  public boolean isInternal() {
    return internal;
  }

  /**
   * Sets the value of internal.
   * 
   * @param internal The value for internal.
   */
  public void setInternal(boolean internal) {
    this.internal = internal;
  }

  /**
   * Returns the value of active.
   * 
   * @return Returns active.
   */
  public boolean isActive() {
    return active;
  }

  /**
   * Sets the value of active.
   * 
   * @param active The value for active.
   */
  public void setActive(boolean active) {
    this.active = active;
  }

  /**
   * @see magellan.library.impl.MagellanNamedImpl#toString()
   */
  @Override
  public String toString() {
    return "Order[" + getID() + "," + getSyntax();
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

}
