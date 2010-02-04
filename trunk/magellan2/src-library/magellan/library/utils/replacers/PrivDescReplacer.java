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

package magellan.library.utils.replacers;

import magellan.library.Unit;
import magellan.library.utils.Resources;

/**
 * Returns the private description of a unit
 * 
 * @author Andreas
 * @version 1.0
 */
public class PrivDescReplacer extends AbstractUnitReplacer {

  /**
   * @see magellan.library.utils.replacers.AbstractUnitReplacer#getUnitReplacement(magellan.library.Unit)
   */
  @Override
  public Object getUnitReplacement(Unit r) {
    return r.getPrivDesc();
  }

  /**
   * @see magellan.library.utils.replacers.Replacer#getDescription()
   */
  public String getDescription() {
    return Resources.get("util.replacers.privdescreplacer.description");
  }

}
