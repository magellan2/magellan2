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

package magellan.library.gamebinding.e3a;

import magellan.library.gamebinding.EresseaPostProcessor;



/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 345 $
 */
public class E3APostProcessor extends EresseaPostProcessor {
  private static final E3APostProcessor singleton = new E3APostProcessor();

  protected E3APostProcessor() {
	}
  

	/**
	 * 
	 */
	public static E3APostProcessor getSingleton() {
		return E3APostProcessor.singleton;
	}

}
