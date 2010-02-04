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

import magellan.library.rules.GenericRules;

/**
 * A Zero Pattern implementation of the <tt>GameData</tt> supporting all of the attributes defined
 * there. No maps are defined as <tt>null</tt>.
 * 
 * @see magellan.library.GameData
 */
public class MissingData extends CompleteData {
  /**
   * Creates a new MissingData object.
   */
  public MissingData() {
    super(new GenericRules(), "void");
  }
}
