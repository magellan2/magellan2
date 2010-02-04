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
 * An subtraction operator.
 * 
 * @author Andreas
 * @version 1.0
 */
public class SubtractionOperator extends AbstractOperator {
  /**
   * Creates a new SubtractionOperator object.
   */
  public SubtractionOperator() {
    super(2);
  }

  /**
   * Subtracts the second number given as argument from the first one.
   */
  @Override
  public Object compute(Object numbers[]) {
    return new Float(((Number) numbers[0]).floatValue() - ((Number) numbers[1]).floatValue());
  }

  /**
   * @see magellan.library.utils.replacers.Replacer#getDescription()
   */
  @Override
  public String getDescription() {
    return Resources.get("util.replacers.subtractionoperator.description") + "\n\n"
        + super.getDescription();
  }
}
