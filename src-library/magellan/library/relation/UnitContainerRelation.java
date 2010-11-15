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
import magellan.library.UnitContainer;

/**
 * A (possibly abstract) relation indicating that the source unit interacts with the target unit
 * container.
 */
public class UnitContainerRelation extends UnitRelation {
  /** The container affected by this relation. */
  public final UnitContainer target;

  /**
   * Creates a new UnitContainerRelation object.
   * 
   * @param s The source unit
   * @param t The target unit
   * @param line The line in the source's orders
   */
  public UnitContainerRelation(Unit s, UnitContainer t, int line) {
    super(s, line);
    target = t;
  }

  /*
   * (non-Javadoc)
   * @see com.eressea.relation.UnitRelation#toString()
   */
  @Override
  public String toString() {
    return super.toString() + "@TARGET=" + target;
  }

  /**
   * Attaches an order to all report objects it is relevant to.
   */
  @Override
  public void add() {
    super.add();
    if (target != source && target != origin) {
      target.addRelation(this);
    }
  }

}
