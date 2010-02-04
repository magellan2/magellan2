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

package magellan.library.gamebinding;

import java.util.List;

import magellan.library.Unit;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 171 $
 */
public interface TempUnitFactory {
  /**
   * Returns the orders necessary to issue the creation of all the child temp units of this unit.
   */
  public List<?> getTempOrders(Unit unit);

  /**
   * DOCUMENT-ME
   */
  public List<?> extractTempUnits(Unit unit);
}
