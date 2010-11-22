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

import magellan.library.utils.logging.Logger;

/**
 * DOCUMENT ME!
 * 
 * @author Andreas
 * @version 1.0
 */
public class ReplacerSystem {
  protected ReplacerEnvironment environment;
  protected Replacer base;

  /**
   * DOCUMENT-ME
   */
  public ReplacerEnvironment getEnvironment() {
    if (environment == null) {
      environment = new ReplacerEnvironment();
    }

    return environment;
  }

  /**
   * DOCUMENT-ME
   */
  public Replacer getBase() {
    return base;
  }

  protected void setBase(Replacer replacer) {
    base = replacer;
  }

  /**
   * DOCUMENT-ME
   */
  public synchronized Object getReplacement(Object obj) {
    if (environment != null) {
      environment.reset();
    }

    Object ret = null;

    try {
      ret = base.getReplacement(obj);
    } catch (Exception exc) {
      Logger.getInstance(this.getClass()).fine("", exc);
    }

    if (environment != null) {
      environment.reset();
    }

    return ret;
  }
}
