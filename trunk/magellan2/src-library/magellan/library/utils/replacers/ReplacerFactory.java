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
 * ReplacerFactory.java
 *
 * Created on 20. Mai 2002, 14:09
 */
package magellan.library.utils.replacers;

import java.util.Set;

/**
 * Provides replacer instances.
 * 
 * @author Andreas
 * @version 1.0
 */
public interface ReplacerFactory {
  /**
   * Get names of all available replacers.
   */
  public Set<String> getReplacers();

  /**
   * Checks if a replacer exists.
   */
  public boolean isReplacer(String name);

  /**
   * Return an instance for the given name.
   */
  public Replacer createReplacer(String name);
}
