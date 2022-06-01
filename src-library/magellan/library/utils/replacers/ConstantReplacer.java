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
 * Evaluates to the newline character.
 * 
 * @author Andreas
 * @version 1.0
 */
public class ConstantReplacer implements Replacer {

  private String constant;
  private String description;

  /**
   * Creates a new RegionMethodReplacer object.
   * 
   * @param method A method name of Region
   * @param mode Defines what is returned for negative values.
   * @throws RuntimeException if the given field is not accessible
   */
  public ConstantReplacer(String constant, String description) {
    this.constant = constant;
    this.description = description;
  }
  /**
   * Evaluates to the newline character.
   */
  public Object getReplacement(Object o) {
    return constant;
  }

  /**
   * @see magellan.library.utils.replacers.Replacer#getDescription()
   */
  public String getDescription() {
    return description;
  }

}
