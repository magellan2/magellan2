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
   * Creates a new TransferRelation object.
   * 
   * @param s The source unit
   * @param t The target unit
   * @param a The amount to transfer.
   * @param line The line in the source's orders
   */
  public TransferRelation(Unit s, Unit t, int a, int line) {
    super(s, t, line);
    amount = a;
  }

  /**
   * Creates a new TransferRelation object.
   * 
   * @param s The source unit
   * @param t The target unit
   * @param a The amount to transfer.
   * @param line The line in the source's orders
   * @param w <code>true</code> iff this relation causes a warning
   */
  public TransferRelation(Unit s, Unit t, int a, int line, boolean w) {
    super(s, t, line, w);
    amount = a;
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
