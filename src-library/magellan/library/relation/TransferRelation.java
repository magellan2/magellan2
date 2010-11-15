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

/**
 * A (possibly abstract) relation indicating that the source unit transfers a certain amount of some
 * objects to the target unit.
 */
public class TransferRelation extends InterUnitRelation {
  /**
   * The amount to transfer. This has to be a non-negative value based on the rest amount of the
   * transfered object
   */
  public int amount;

  /**
   * Creates a new TransferRelation object without warning.
   * 
   * @param source The source unit
   * @param target The target unit
   * @param amount The amount to transfer.
   * @param line The line in the source's orders
   */
  public TransferRelation(Unit source, Unit target, int amount, int line) {
    this(source, source, target, amount, line);
  }

  /**
   * Creates a new TransferRelation object.
   * 
   * @param origin The origin unit
   * @param source The source unit
   * @param target The target unit
   * @param amount The amount to transfer.
   * @param line The line in the source's orders
   */
  public TransferRelation(Unit origin, Unit source, Unit target, int amount, int line) {
    super(origin, source, target, line);
    this.amount = amount;
  }

  /*
   * (non-Javadoc)
   * @see com.eressea.relation.InterUnitRelation#toString()
   */
  @Override
  public String toString() {
    return super.toString() + "@AMOUNT=" + amount;
  }
}
