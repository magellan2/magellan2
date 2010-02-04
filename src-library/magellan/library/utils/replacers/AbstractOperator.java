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

/**
 * A replacer that defines a operation with the following definition elements treated as numbers. To
 * support complex formulae it is necessary to use Polish Notation that is Postfix-Notation.
 * 
 * @author Andreas
 * @version 1.0
 */
public abstract class AbstractOperator extends AbstractParameterReplacer implements
    EnvironmentDependent {
  protected Object numbers[];
  protected Float evolved = null;
  protected static final Float ZERO = new Float(0);
  protected ReplacerEnvironment environment;

  /**
   * Creates an AbstractOperator with the specified number of parameters.
   * 
   * @param params
   */
  protected AbstractOperator(int params) {
    super(params);
    numbers = new Object[params];
  }

  /**
   * @see magellan.library.utils.replacers.AbstractParameterReplacer#setParameter(int,
   *      java.lang.Object)
   */
  @Override
  public void setParameter(int param, Object obj) {
    // try to compute the operation to save time
    evolved = null;
    super.setParameter(param, obj);

    if (param == (numbers.length - 1)) { // all parameters set

      try {
        evolved = (Float) getReplacement(null);
      } catch (RuntimeException exc) {
      }
    }
  }

  /**
   * Computes the result from the parameters in numbers and returns the result.
   */
  public abstract Object compute(Object numbers[]);

  /**
   * Converts all parameters and then returns the result from {@link #compute(Object[])}.
   * 
   * @see magellan.library.utils.replacers.Replacer#getReplacement(java.lang.Object)
   */
  public Object getReplacement(Object argument) {
    if (evolved != null)
      return evolved;

    boolean nullAsZero =
        ((OperationMode) environment.getPart(ReplacerEnvironment.OPERATION_PART))
            .isNullEqualsZero();

    for (int i = 0; i < numbers.length; i++) {
      Object param = getParameter(i, argument);

      if (param == null) {
        if (!nullAsZero)
          return null;

        param = AbstractOperator.ZERO;
      }

      if (!(param instanceof Number)) {
        try {
          Float fl = Float.valueOf(param.toString().replace(".", ""));
          numbers[i] = fl;
        } catch (NumberFormatException exc) {
          return null;
        }
      } else {
        numbers[i] = param;
      }
    }

    // now all parameters are Number objects
    return compute(numbers);
  }

  /**
   * @see magellan.library.utils.replacers.EnvironmentDependent#setEnvironment(magellan.library.utils.replacers.ReplacerEnvironment)
   */
  public void setEnvironment(ReplacerEnvironment env) {
    environment = env;
  }
}
