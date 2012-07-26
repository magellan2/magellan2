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

import magellan.library.Region;

/**
 * A RelationFactory creates relations of a unit to other game objects according to the unit's
 * orders. This interface is implemented by game specific classes.
 * 
 * @author $Author: $
 * @version $Revision: 389 $
 */
public interface RelationFactory {
  /** The highest order priority. */
  public static final int P_FIRST = 0;

  /** The lowest order priority. */
  public static final int P_LAST = Integer.MAX_VALUE - 2;

  public void createRelations(Region region);

}
