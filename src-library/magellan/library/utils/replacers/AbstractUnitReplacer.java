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

import magellan.library.Unit;

/**
 * DOCUMENT ME!
 * 
 * @author unknown
 * @version 1.0
 */
public abstract class AbstractUnitReplacer implements Replacer {
  /**
   * DOCUMENT-ME
   */
  public Object getReplacement(Object r) {
    if (r instanceof Unit)
      return getUnitReplacement((Unit) r);

    return null;
  }

  /**
   * DOCUMENT-ME
   */
  public abstract Object getUnitReplacement(Unit r);

  /**
   * DOCUMENT-ME
   */
  /*
   * public String getDescription() { return
   * magellan.library.utils.Translations.getTranslation(this, "description"); }
   */
}
