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
import magellan.library.utils.Resources;

/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public class PrivDescReplacer extends AbstractUnitReplacer {
	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public Object getUnitReplacement(Unit r) {
		return r.getPrivDesc();
	}
  

  public String getDescription() {
    return Resources.get("magellan.util.replacers.privdescreplacer.description");
  }  

}
