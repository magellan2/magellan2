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

package magellan.library.relation;

import magellan.library.Unit;
import magellan.library.rules.Race;

/**
 * A relation indicating that a unit recruits a certain amount of peasants.
 */
public class RecruitmentRelation extends PersonTransferRelation {
	/* pavkovic 2003.02.17: made RecruitmentRelation an inverse PersonTransferRelation! */
	/**
	 * Creates a new RecruitmentRelation object.
	 *
	 * @param t The target unit
	 * @param a The amount to transfer
	 * @param line The line in the source's orders
	 */
	public RecruitmentRelation(Unit t, int a, int line) {
	  this(t, a, t.getRace(), line);
	}

  /**
   * Creates a new RecruitmentRelation object.
   *
   * @param t The target unit
   * @param a The amount to transfer
   * @param line The line in the source's orders
   * @param race The race that is recruited
   */
  public RecruitmentRelation(Unit t, int a, Race race, int line) {
    super(t.getRegion().getZeroUnit(), t, a, race, line);

    // super(t, t.getRegion().getZeroUnit(), -amount, t.realRace != null ? t.realRace : t.race);
    // ...but we need to remember that the target unit is the originator of this
    // relation...
    this.origin = t;
  }
}
