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
 * Base interface for Replacer architecture.
 * <p>
 * A Replacer's getReplacement method takes an object and replaces it by something else. Replacers
 * with several arguments can by implemented by using a {@link BranchReplacer}.
 * </p>
 * <p>
 * Our replacers are usually created in {@link ReplacerFactory} and therefore have only default
 * constructors.
 * </p>
 * 
 * @author Andreas
 * @version 1.0
 */
public interface Replacer {
  /** An empty String. */
  public static final String EMPTY = "";

  /** The <code>true</code> keyword. */
  public static final String TRUE = "true";

  /** The <code>false</code> keyword. */
  public static final String FALSE = "false";

  /** The "clear" keyword */
  public static final String CLEAR = "clear";

  /** The "else" keyword. */
  public static final String NEXT_BRANCH = "else";

  /** The end marker. */
  public static final String END = "end";

  /**
   * Apply this replacer to the argument.
   */
  public Object getReplacement(Object argument);

  /**
   * Returns a description for the user of what this replacer does.
   */
  public String getDescription();
}
