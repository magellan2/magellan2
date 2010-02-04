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
 * AbstractParameterReplacer.java
 *
 * Created on 29. Dezember 2001, 16:17
 */
package magellan.library.utils.replacers;

import magellan.library.utils.Resources;

/**
 * A default implementation of ParameterReplacer.
 * 
 * @author Andreas
 * @version 1.0
 */
public abstract class AbstractParameterReplacer implements ParameterReplacer {
  protected Object parameters[];

  /**
   * Creates a new AbstractParameterReplacer
   * 
   * @param parameters The number of parameters
   */
  protected AbstractParameterReplacer(int parameters) {
    this.parameters = new Object[parameters];
  }

  /**
   * @see magellan.library.utils.replacers.ParameterReplacer#getParameterCount()
   */
  public int getParameterCount() {
    return parameters.length;
  }

  /**
   * @see magellan.library.utils.replacers.ParameterReplacer#setParameter(int, java.lang.Object)
   */
  public void setParameter(int param, java.lang.Object obj) {
    parameters[param] = obj;
  }

  protected Object getParameter(int index, Object o) {
    if (parameters[index] != null) {
      if (parameters[index] instanceof Replacer)
        return ((Replacer) parameters[index]).getReplacement(o);
    }

    return parameters[index];
  }

  /**
   * Returns a string describing the number of parameters.
   * 
   * @see magellan.library.utils.replacers.Replacer#getDescription()
   */
  public String getDescription() {
    return Resources.get("util.replacers.abstractparameter.description",
        new Object[] { getParameterCount() });
  }

}
