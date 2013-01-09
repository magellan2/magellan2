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

import magellan.library.Region;
import magellan.library.utils.Resources;

/**
 * Returns a method value of an object of class {@link Region}.
 * 
 * @author unknown
 * @version 1.0
 */
public class RegionMethodReplacer extends RegionFieldReplacer {

  /**
   * Creates a new RegionMethodReplacer object.
   * 
   * @param method A method name of Region
   * @param mode Defines what is returned for negative values.
   * @throws RuntimeException if the given field is not accessible
   */
  public RegionMethodReplacer(String method, int mode) {
    super();

    try {
      setMethod(Class.forName("magellan.library.impl.MagellanRegionImpl").getMethod(method));
    } catch (Exception exc) {
      throw new RuntimeException("Error retrieving region method " + method);
    }

    setMode(mode);
  }

  /**
   * @see magellan.library.utils.replacers.RegionFieldReplacer#getDescription()
   */
  @Override
  public String getDescription() {
    return Resources.get("util.replacers.regionmethodreplacer."
        + (method != null ? method.getName() : "") + ".description");
  }
}
