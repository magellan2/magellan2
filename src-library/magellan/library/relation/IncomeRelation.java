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

import magellan.library.Region;
import magellan.library.Unit;

/**
 * A relation indicating earning money from a unit container (region) based on WORK, (STEAL) (BUY)
 * TAX, ENTERTAIN (SELL)
 */
public class IncomeRelation extends UnitContainerRelation implements LongOrderRelation {
  /** The money earned */
  public final int amount;

  /**
   * Creates a new IncomeRelation object.
   * 
   * @param s The source unit
   * @param r The target region
   * @param amount The amount of money earned
   * @param line The line in the source's orders
   */
  public IncomeRelation(Unit s, Region r, int amount, int line) {
    super(s, r, line);
    this.amount = amount;
  }
}
