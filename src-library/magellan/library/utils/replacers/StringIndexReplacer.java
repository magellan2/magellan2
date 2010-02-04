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
 * A Switch that compares the following to elements by their String replacement. Possible replacers
 * are evaluated by forwarding the Switch object and iterator. If these two are not evaluatable(list
 * too short) or only on of them is <i>null</i> the Switch stays active. <i>Note that if both are
 * null the switch is inactive!</i>
 * 
 * @author Andreas
 * @version 1.0
 */
public class StringIndexReplacer extends AbstractParameterSwitch {
  /**
   * If the String comparism should be done with regarding to the case this property is
   * <i>false</i>, else <i>true</i>.
   */
  protected boolean ignoreCase = false;

  /**
   * Constructs a default String Compare Switch that is case-sensitive.
   */
  public StringIndexReplacer() {
    this(false);
  }

  /**
   * Constructs a String Compare Switch with the given sensibility for case.
   */
  public StringIndexReplacer(boolean iCase) {
    super(2);
    ignoreCase = iCase;
  }

  /**
   * Checks the following two elements and evaluates their replacements. They are treated as Strings
   * through <i>toString()</i> and compared for equality.
   */
  @Override
  public boolean isSwitchingObject(Object o) {
    Object o1 = getParameter(0, o);
    Object o2 = getParameter(1, o);

    if ((o1 != null) && (o2 != null)) {
      int i = -1;

      if (ignoreCase) {
        i = o1.toString().toUpperCase().indexOf(o2.toString().toUpperCase());
      } else {
        i = o1.toString().indexOf(o2.toString());
      }

      return i >= 0;
    }

    return false;
  }

  /**
   * @see magellan.library.utils.replacers.AbstractParameterSwitch#getDescription()
   */
  @Override
  public String getDescription() {
    return Resources.get("util.replacers.stringindexreplacer.description." + ignoreCase) + "\n\n"
        + super.getDescription();
  }
}
