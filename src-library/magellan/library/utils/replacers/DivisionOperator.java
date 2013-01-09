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
 * An division operator.
 * <p>
 * A Divide By Zero returns <i>null</i>.
 * </p>
 * 
 * @author Andreas
 * @version 1.0
 */
public class DivisionOperator extends AbstractOperator {
  /**
   * Creates a new DivisionOperator object.
   */
  public DivisionOperator() {
    super(2);
  }

  /**
   * Divides the first argument number by the second.
   */
  @Override
  public Object compute(Object numbers[]) {
    if (((Number) numbers[1]).floatValue() == 0)
      return null;

    return Float.valueOf(((Number) numbers[0]).floatValue() / ((Number) numbers[1]).floatValue());
  }

  /**
   * @see magellan.library.utils.replacers.Replacer#getDescription()
   */
  @Override
  public String getDescription() {
    return Resources.get("util.replacers.divisionoperator.description") + "\n\n"
        + super.getDescription();
  }
}
