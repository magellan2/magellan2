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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import magellan.library.ID;
import magellan.library.StringID;
import magellan.library.utils.CollectionFactory;
import magellan.library.utils.logging.Logger;

/**
 * This class contains an abstract order.
 * 
 * @author stm
 */
public class OrderType extends ObjectType {

  String syntax;
  boolean internal;
  boolean active;
  private Map<String, List<String>> names = CollectionFactory.createMap(2, .8f);

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
    // return getName(Locales.getOrderLocale());
    return "Order[" + getID() + "," + getSyntax() + "]";
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

  public void addName(Locale loc, String name) {
    List<String> list = names.get(loc.getLanguage());
    if (list == null) {
      list = new ArrayList<String>(1);
      names.put(loc.getLanguage(), list);
    }
    list.add(name);
  }

  /**
   * Returns all registered localized order names.
   * 
   * @param loc
   * @return The list of names or <code>null</code> if no such name has been added.
   */
  public List<String> getNames(Locale loc) {
    return names.get(loc.getLanguage());
  }

  /**
   * Returns a localized order name.
   * 
   * @param loc
   * @return The name or <code>null</code> if no such name has been added.
   */
  public String getName(Locale loc) {
    if (loc == null) {
      Logger.getInstance(this.getClass()).error("loc null", new RuntimeException());
      return null;
    }

    List<String> list = names.get(loc.getLanguage());
    if (list != null)
      return list.get(0);
    else
      return null;
  }

}
