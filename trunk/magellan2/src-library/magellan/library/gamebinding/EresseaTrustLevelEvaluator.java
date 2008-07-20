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

package magellan.library.gamebinding;

import magellan.library.Alliance;

/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class EresseaTrustLevelEvaluator {
	private EresseaTrustLevelEvaluator() {
	}

	private static final EresseaTrustLevelEvaluator singleton = new EresseaTrustLevelEvaluator();

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static EresseaTrustLevelEvaluator getSingleton() {
		return EresseaTrustLevelEvaluator.singleton;
	}

	/**
	 * A method to convert an alliance into a trustlevel. This method should be uses when Magellan
	 * calculates trust levels on its own.
	 *
	 * 
	 *
	 * 
	 *
	 * @deprecated
	 */
	@Deprecated
  public int getTrustLevel(Alliance alliance) {
		return alliance.getTrustLevel();
	}
}
