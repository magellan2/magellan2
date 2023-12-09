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
 * A Zero Pattern implementation of the <kbd>GameData</kbd> supporting all of the attributes defined
 * there. No maps are defined as <kbd>null</kbd>.
 *
 * @see magellan.library.GameData
 */
public class MissingData extends CompleteData {
  /**
   * Creates a new MissingData object.
   */
  public MissingData() {
    super(new EmptyRules(), "void");
  }
}
