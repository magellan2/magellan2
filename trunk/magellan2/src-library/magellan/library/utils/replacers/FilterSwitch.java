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
import magellan.library.utils.filters.UnitFilter;

/**
 * DOCUMENT ME!
 * 
 * @author Andreas
 * @version 1.0
 */
public class FilterSwitch implements ParameterReplacer, BranchReplacer, EnvironmentDependent {
  protected Object branch;
  protected ReplacerEnvironment env;
  protected UnitFilter myFilter;

  /**
   * Creates new Template
   */
  public FilterSwitch() {
  }

  /**
   * Sets the (only) parameter.
   * 
   * @see magellan.library.utils.replacers.ParameterReplacer#setParameter(int, java.lang.Object)
   */
  public void setParameter(int index, Object obj) {
    createFilter(obj);
  }

  /**
   * Returns 1.
   */
  public int getParameterCount() {
    return 1;
  }

  /**
   * @see magellan.library.utils.replacers.BranchReplacer#getBranchSign(int)
   */
  public String getBranchSign(int index) {
    return Replacer.END;
  }

  /**
   * @see magellan.library.utils.replacers.BranchReplacer#setBranch(int, java.lang.Object)
   */
  public void setBranch(int index, Object obj) {
    branch = obj;
  }

  /**
   * Returns 1.
   */
  public int getBranchCount() {
    return 1;
  }

  /**
   * Applies the filter to the argument.
   * 
   * @see magellan.library.utils.replacers.Replacer#getReplacement(java.lang.Object)
   */
  public Object getReplacement(Object argument) {
    if (branch != null) {
      if (branch instanceof Replacer) {
        Replacer r = (Replacer) branch;

        if ((myFilter != null) && (env != null)
            && (env.getPart(ReplacerEnvironment.UNITSELECTION_PART) != null)) {
          UnitSelection us = (UnitSelection) env.getPart(ReplacerEnvironment.UNITSELECTION_PART);
          us.addFilter(myFilter);

          Object obj = r.getReplacement(argument);
          us.removeFilter(myFilter);

          return obj;
        }

        return r.getReplacement(argument);
      }

      return Replacer.EMPTY;
    }

    return Replacer.EMPTY;
  }

  /**
   * DOCUMENT-ME
   * 
   * @see magellan.library.utils.replacers.EnvironmentDependent#setEnvironment(magellan.library.utils.replacers.ReplacerEnvironment)
   */
  public void setEnvironment(ReplacerEnvironment env) {
    this.env = env;
  }

  /**
   * @see magellan.library.utils.replacers.Replacer#getDescription()
   */
  public String getDescription() {
    return Resources.get("util.replacers.filterswitch.description");
  }

  protected void createFilter(Object predicate) {
    myFilter = new MyFilterClass(predicate);
  }

  protected class MyFilterClass extends UnitFilter {
    int always = 0;
    protected Replacer rep = null;

    /**
     * Creates a new MyFilterClass object.
     * 
     * @param predicate A replacer used for deciding acceptance of units.
     */
    public MyFilterClass(Object predicate) {
      if (predicate instanceof Replacer) {
        rep = (Replacer) predicate;
      } else {
        if (predicate.toString().equals(Replacer.TRUE)) {
          always = 1;
        } else {
          always = 2;
        }
      }
    }

    /**
     * Returns <code>true</code> iff the predicate replacer returns true for the unit.
     */
    @Override
    public boolean acceptUnit(Unit u) {
      if (always != 0)
        return always == 1;

      try {
        String s = rep.getReplacement(u).toString();

        return s.equals(Replacer.TRUE);
      } catch (Exception exc) {
      }

      return false;
    }
  }
}
