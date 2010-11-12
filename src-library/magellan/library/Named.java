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

/**
 * An interface granting access to the name of an object.
 */
public interface Named extends Identifiable {
  /**
   * Returns the name of this object.
   */
  public String getName();

  /**
   * Sets the name of this object.
   */
  public void setName(String name);

  /**
   * Returns the name after orders.
   * 
   * @return The new name
   */
  public String getModifiedName();

}
