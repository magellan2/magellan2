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

import magellan.library.Ship;
import magellan.library.Unit;

/**
 * A relation indicating that a captain transfers a certain amount of ships to another ship.
 */
public class ShipTransferRelation extends TransferRelation {
  /** the transferred ship / fleet */
  public Ship ship;
  private Ship targetShip;

  /**
   * Creates a new object.
   *
   * @param source The source unit
   * @param target The target unit
   * @param amount The amount to transfer
   * @param ship The transferred ship/fleet
   * @param line The line in the source's orders
   */
  public ShipTransferRelation(Unit source, Unit target, int amount, Ship ship, Ship targetShip,
      int line) {
    this(source, source, target, amount, ship, targetShip, line);
  }

  /**
   * Creates a new object.
   *
   * @param origin The origin unit
   * @param source The source unit
   * @param target The target unit
   * @param amount The amount to transfer
   * @param ship The transferred ship/fleet
   * @param line The line in the source's orders
   */
  public ShipTransferRelation(Unit origin, Unit source, Unit target, int amount, Ship ship,
      Ship targetShip,
      int line) {
    super(origin, source, target, amount, line);
    if (ship == null || targetShip == null)
      throw new NullPointerException();
    this.ship = ship;
    this.targetShip = targetShip;
  }

  /**
   * @see magellan.library.relation.TransferRelation#toString()
   */
  @Override
  public String toString() {
    return super.toString() + " SHIP";
  }

  @Override
  public void add() {
    super.add();
    ship.addRelation(this);
    targetShip.addRelation(this);
  }
}
