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

import magellan.library.utils.Resources;

/**
 * Returns a substring of a given string. §substr§start§end§string§
 * 
 * @author Andreas
 * @version 1.0
 */
public class SubstrOperator extends AbstractParameterReplacer {

  /**
   * @see magellan.library.utils.replacers.AbstractParameterSwitch#getDescription()
   */
  @Override
  public String getDescription() {
    return Resources.get("util.replacers.substrreplacer.description");
  }

  /**
   * 
   */
  public SubstrOperator() {
    super(3);
  }

  /**
   * Computes the result from the parameters in numbers and returns the result.
   */
  public Object compute(int start, int end, String str) {
    try {
      if (start < 0) {
        start = str.length() + start + 1;
      }
      if (end < 0) {
        end = str.length() + end + 1;
      }
      return str.substring(start, end);
    } catch (RuntimeException e) {
      return null;
    }
  }

  /**
   * Converts all parameters and then returns the result from {@link #compute(int, int, String)}.
   * 
   * @see magellan.library.utils.replacers.Replacer#getReplacement(java.lang.Object)
   */
  public Object getReplacement(Object argument) {
    Integer start = getNumberParameter(argument, 0);
    Integer end = getNumberParameter(argument, 1);
    Object p = getParameter(2, argument);
    if (p == null)
      return null;
    return compute(start, end, String.valueOf(p));
  }

  private Integer getNumberParameter(Object argument, int i) {
    Object param = getParameter(i, argument);

    if (param instanceof Number)
      return ((Number) param).intValue();
    else {
      try {
        return Integer.valueOf(param.toString());
      } catch (NumberFormatException exc) {
        return null;
      }
    }
  }

}
