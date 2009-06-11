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

import magellan.library.gamebinding.EresseaMovementEvaluator;



/**
 *
 * @author $Author: $
 * @version $Revision: 396 $
 */
public class E3AMovementEvaluator extends EresseaMovementEvaluator {
  private static final E3AMovementEvaluator singleton = new E3AMovementEvaluator();

  protected E3AMovementEvaluator() {
	}


	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static E3AMovementEvaluator getSingleton() {
		return E3AMovementEvaluator.singleton;
	}

}
