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

/*
 * UnitCountReplacer.java
 *
 * Created on 29. Dezember 2001, 15:47
 */
package magellan.library.utils.replacers;

import java.util.Collection;
import java.util.Iterator;

import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.utils.Resources;

/**
 * Counts persons of all units.
 * 
 * @author Andreas
 * @version 1.0
 */
public class UnitCountReplacer extends AbstractRegionReplacer implements EnvironmentDependent {
  private static final Integer ZERO = Integer.valueOf(0);
  protected ReplacerEnvironment environment;
  protected boolean countPersons;

  /**
   * Creates a new UnitCountReplacer object.
   */
  public UnitCountReplacer() {
    this(true);
  }

  /**
   * Creates a new UnitCountReplacer object.
   */
  public UnitCountReplacer(boolean countPersons) {
    this.countPersons = countPersons;
  }

  /**
   * Returns the sum of persons of all filtered units.
   * 
   * @return The sum of persons of filtered units as Integer, or <code>null</code> if no unit
   * @see magellan.library.utils.replacers.AbstractRegionReplacer#getRegionReplacement(magellan.library.Region)
   */
  @Override
  public Object getRegionReplacement(Region r) {
    Collection<Unit> c =
        ((UnitSelection) environment.getPart(ReplacerEnvironment.UNITSELECTION_PART)).getUnits(r);

    if (c != null) {
      int count = 0;

      if (countPersons) {
        Iterator<Unit> it = c.iterator();

        while (it.hasNext()) {
          count += (it.next()).getPersons();
        }
      } else {
        count = c.size();
      }

      if (count > 0)
        return Integer.valueOf(count);

      return UnitCountReplacer.ZERO;
    }

    return null;
  }

  /**
   * @see magellan.library.utils.replacers.EnvironmentDependent#setEnvironment(magellan.library.utils.replacers.ReplacerEnvironment)
   */
  public void setEnvironment(ReplacerEnvironment env) {
    environment = env;
  }

  /**
   * @see magellan.library.utils.replacers.Replacer#getDescription()
   */
  public String getDescription() {
    return Resources.get("util.replacers.unitcountreplacer.description." + countPersons);
  }
}
