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
  /** Recruitment costs in silver */
  public int costs;
  public UnitRelation reserveRelation;

  /* pavkovic 2003.02.17: made RecruitmentRelation an inverse PersonTransferRelation! */
  /**
   * Creates a new RecruitmentRelation object.
   * 
   * @param unit The recruiting unit
   * @param amount The amount to transfer
   * @param cost The costs in silver
   * @param line The line in the source's orders
   */
  public RecruitmentRelation(Unit unit, int amount, int cost, int line) {
    this(unit, amount, cost, unit.getRace(), line);
  }

  /**
   * Creates a new RecruitmentRelation object.
   * 
   * @param unit The recruiting unit
   * @param amount The amount to transfer
   * @param cost The costs in silver
   * @param line The line in the source's orders
   * @param race The race that is recruited
   */
  public RecruitmentRelation(Unit unit, int amount, int cost, Race race, int line) {
    super(unit.getRegion().getZeroUnit(), unit, amount, race, line);
    costs = cost;

    // super(t, t.getRegion().getZeroUnit(), -amount, t.realRace != null ? t.realRace : t.race);
    // ...but we need to remember that the target unit is the originator of this
    // relation...
    origin = unit;
  }

  @Override
  public void add() {
    origin.addRelation(this);
    source.addRelation(this);
  }

  // public void setReserve(UnitRelation rel) {
  // reserveRelation = rel;
  // }
}
