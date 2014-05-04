/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.gamebinding.e4;

import magellan.library.gamebinding.e3a.E3APostProcessor;

/**
 * 
 */
public class E4PostProcessor extends E3APostProcessor {
  private static final E4PostProcessor singleton = new E4PostProcessor();

  protected E4PostProcessor() {
    super();
  }

  /**
   * Returns an instance.
   */
  public static E4PostProcessor getSingleton() {
    return E4PostProcessor.singleton;
  }
}
