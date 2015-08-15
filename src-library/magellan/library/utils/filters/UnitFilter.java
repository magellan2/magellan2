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

package magellan.library.utils.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import magellan.library.Unit;
import magellan.library.utils.Resources;

/**
 * The base class for filtering units. Designed after FileFilter and similar interfaces, but as an
 * abstract class to have a short-cut for Collections implemented. ,
 *
 * @author Andreas
 * @version 1.0
 */
public abstract class UnitFilter {
  /**
   * Returns <code>true</code> fall accepted units.
   */
  public abstract boolean acceptUnit(Unit u);

  /**
   * Returns a collection of accepted units in <code>col</code>. The original list is not modified.
   */
  public Collection<Unit> acceptUnits(Collection<Unit> col) {
    return acceptUnits(col, false);
  }

  /**
   * Filters the given collection. The result is a collection of all accepted units in
   * <code>col</code>. If <code>useThis</code> is <code>true</code>, the collection itself is
   * returnd (and modified). Otherwise, a new list is returned.
   */
  public Collection<Unit> acceptUnits(Collection<Unit> col, boolean useThis) {
    Collection<Unit> col2 = null;

    if (useThis) {
      col2 = col;
      Iterator<Unit> it = col2.iterator();

      while (it.hasNext()) {
        if (!acceptUnit(it.next())) {
          it.remove();
        }
      }
    } else {
      col2 = new ArrayList<Unit>();
      for (Unit u : col) {
        if (acceptUnit(u)) {
          col2.add(u);
        }
      }
    }

    return col2;
  }

  /**
   * Returns a name for the filter.
   */
  public String getName() {
    String ret = Resources.get("unitfilter." + getClass().getName());

    if (ret == null) {
      ret = "UnitFilter";
    }

    return ret;
  }
}
