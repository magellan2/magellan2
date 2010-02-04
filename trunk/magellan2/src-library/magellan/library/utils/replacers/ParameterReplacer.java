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
 * ParameterReplacer.java
 *
 * Created on 20. Mai 2002, 14:11
 */
package magellan.library.utils.replacers;

/**
 * A replacer that accepts parameters
 * 
 * @author Andreas
 * @version 1.0
 */
public interface ParameterReplacer extends Replacer {
  /**
   * Returns the number of parameters.
   */
  public int getParameterCount();

  /**
   * Sets the <code>index</code>-th parameter to obj
   */
  public void setParameter(int index, Object obj);
}
