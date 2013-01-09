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
 * SimpleStringReplacer.java
 *
 * Created on 19. Mai 2002, 12:13
 */
package magellan.library.utils.replacers;

/**
 * Returns a certain string.
 * 
 * @author Andreas
 * @version 1.0
 */
public class SimpleStringReplacer implements Replacer {
  protected String string;

  /**
   * Creates new SimpleStringReplacer
   */
  public SimpleStringReplacer(String string) {
    this.string = string;
  }

  /**
   * Returns the string defined in the constructor.
   */
  public Object getReplacement(Object o) {
    return string;
  }

  /**
   * @see magellan.library.utils.replacers.Replacer#getDescription()
   */
  public String getDescription() {
    return "simple string";
  }
}
