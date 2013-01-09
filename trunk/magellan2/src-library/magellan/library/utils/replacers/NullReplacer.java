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
 * NotReplacer.java
 *
 * Created on 21. Mai 2002, 17:24
 */
package magellan.library.utils.replacers;

import magellan.library.utils.Resources;

/**
 * Returns true iff the argument is null.
 * 
 * @author Andreas
 * @version 1.0
 */
public class NullReplacer extends AbstractParameterReplacer {

  /**
   * Creates new NotReplacer
   */
  public NullReplacer() {
    super(1);
  }

  /**
   * @see magellan.library.utils.replacers.Replacer#getReplacement(java.lang.Object)
   */
  public Object getReplacement(Object o) {
    Object obj = getParameter(0, o);

    if (obj == null)
      return Replacer.TRUE;

    return Replacer.FALSE;
  }

  /**
   * @see magellan.library.utils.replacers.AbstractParameterReplacer#getDescription()
   */
  @Override
  public String getDescription() {
    return Resources.get("util.replacers.null.description") + "\n\n" + super.getDescription();
  }
}
