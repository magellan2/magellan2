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
 * A relation indicating that a unit transfers a certain amount of persons to another unit.
 */
public class PersonTransferRelation extends TransferRelation {
  /** The source unit's race */
  public final Race race;

  /**
   * Creates a new PersonTransferRelation object.
   * 
   * @param source The source unit
   * @param target The target unit
   * @param amount The amount to transfer
   * @param race The race of the source Unit
   * @param line The line in the source's orders
   */
  public PersonTransferRelation(Unit source, Unit target, int amount, Race race, int line) {
    super(source, target, amount, line);
    this.race = race;
  }

  /*
   * (non-Javadoc)
   * @see com.eressea.relation.TransferRelation#toString()
   */
  @Override
  public String toString() {
    return super.toString() + "@RACE=" + race;
  }

}
