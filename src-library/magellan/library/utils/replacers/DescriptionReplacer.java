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
 * TagReplacer.java
 *
 * Created on 6. Juni 2002, 18:41
 */
package magellan.library.utils.replacers;

import magellan.library.Described;
import magellan.library.utils.Resources;

/**
 * Replacer returning the description of Described objects.
 * 
 * @author Andreas
 * @version 1.0
 */
public class DescriptionReplacer implements Replacer {
  protected boolean mode;

  /**
   * Creates a new DescriptionReplacer object.
   */
  public DescriptionReplacer() {
    this(true);
  }

  /**
   * Creates new DescriptionReplacer
   * 
   * @param mode {@link #getReplacement(Object)} returns {@link Replacer#EMPTY} for objects that are
   *          not descendents of {@link Described} if this is true, otherwise <code>null</code>.
   */
  public DescriptionReplacer(boolean mode) {
    this.mode = mode;
  }

  /**
   * Returns the description.
   * 
   * @return the description. If the argument does no inherit from {@link Described}, returns
   *         {@link Replacer#EMPTY} or null, depending on the mode.
   * @see magellan.library.utils.replacers.Replacer#getReplacement(java.lang.Object)
   */
  public Object getReplacement(Object o) {
    if (o instanceof Described)
      return ((Described) o).getDescription();

    if (mode)
      return Replacer.EMPTY;

    return null;
  }

  /**
   * @see magellan.library.utils.replacers.Replacer#getDescription()
   */
  public String getDescription() {
    return Resources.get("util.replacers.descriptionreplacer.description");
  }
}
