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

import magellan.library.StringID;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 203 $
 */
public abstract class UnitContainerType extends ObjectType {
  /**
   * Creates a new UnitContainerType object.
   */
  public UnitContainerType(StringID id) {
    super(id);
  }

  /**
   * Returns the id uniquely identifying this object.
   */
  @Override
  public StringID getID() {
    return (StringID) id;
  }

}
