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

package magellan.library;

import java.util.List;

import magellan.library.rules.ShipType;

/**
 * A class for representing a ship.
 *
 * @author $Author: $
 * @version $Revision: 389 $
 */
public interface Ship extends UnitContainer, HasRegion, Selectable {

  /**
   * Sets the region this ship is in and notifies region about it.
   *
   * @param region
   */
  public void setRegion(Region region);

  /**
   * Returns the region this ship is in.
   *
   * @return The region the ship is in, possibly null
   */
  public Region getRegion();

  /**
   * The type of this ship.
   *
   * @return The type of this ship
   */
  public ShipType getShipType();

  /**
   * Returns the maximum capacity with respect to damages of the ship in silver.
   *
   * @return Returns the maximum capacity with respect to damages of the ship in silver
   */
  public int getMaxCapacity();

  /**
   * Returns the projected maximum capacity with respect to damages of the ship in silver.
   *
   * @return Returns the projected maximum capacity with respect to damages of the ship in silver
   */
  public int getModifiedMaxCapacity();

  /**
   * Returns the cargo load of this ship.
   *
   * @return Returns the cargo load of this ship or -1 if unknown
   */
  public int getCargo();

  /**
   * Returns the weight of all units of this ship in silver. This is usually less precise than
   * {@link #getCargo()}.
   *
   * @return The weight of the units on the ship
   */
  public int getLoad();

  /**
   * Returns the weight of all units of this ship in silver based on the modified units.
   *
   * @return The modified weight of the modified units on the ship
   */
  public int getModifiedLoad();

  /**
   * A string representation of this ship.
   *
   * @return A string representation of this ship
   */
  public String toString();

  /**
   * Returns the string representation of this ship. If <code>printExtended</code> is true, type,
   * damage and remaing capacity are shown, too.
   *
   * @param printExtended Whether to return a more detailed description
   * @return A strig representation of this ship
   */
  public String toString(boolean printExtended);

  /**
   * Returns the value of capacity.
   *
   * @return Returns capacity.
   */
  public int getCapacity();

  /**
   * Sets the value of capacity.
   *
   * @param capacity The value for capacity.
   */
  public void setCapacity(int capacity);

  /**
   * Returns the value of damageRatio.
   *
   * @return Returns damageRatio.
   */
  public int getDamageRatio();

  /**
   * Sets the value of damageRatio.
   *
   * @param damageRatio The value for damageRatio.
   */
  public void setDamageRatio(int damageRatio);

  /**
   * Returns the value of deprecatedCapacity.
   *
   * @return Returns deprecatedCapacity.
   */
  public int getDeprecatedCapacity();

  /**
   * Sets the value of deprecatedCapacity.
   *
   * @param deprecatedCapacity The value for deprecatedCapacity.
   */
  public void setDeprecatedCapacity(int deprecatedCapacity);

  /**
   * Returns the value of deprecatedLoad.
   *
   * @return Returns deprecatedLoad.
   */
  public int getDeprecatedLoad();

  /**
   * Sets the value of deprecatedLoad.
   *
   * @param deprecatedLoad The value for deprecatedLoad.
   */
  public void setDeprecatedLoad(int deprecatedLoad);

  /**
   * Returns the value of shoreId.
   *
   * @return Returns shoreId.
   */
  public int getShoreId();

  /**
   * Sets the value of shoreId.
   *
   * @param shoreId The value for shoreId.
   */
  public void setShoreId(int shoreId);

  /**
   * Returns the value of size.
   *
   * @return Returns size.
   */
  public int getSize();

  /**
   * Sets the value of size.
   *
   * @param size The value for size.
   */
  public void setSize(int size);

  /**
   * Sets the value of cargo.
   *
   * @param cargo The value for cargo.
   */
  public void setCargo(int cargo);

  /**
   * Returns the maximum number of persons allowed on board.
   */
  public int getMaxPersons();

  /**
   * Sets the maximum number of persons (default: -1).
   *
   * @see ShipType#getMaxPersons()
   * @param persons
   */
  public void setMaxPersons(int persons);

  /**
   * Returns the current weight of the persons currently on board.
   */
  public int getPersonLoad();

  /**
   * Returns the projected weight of the projected peresons on board.
   */
  public int getModifiedPersonLoad();

  /**
   * Returns the id uniquely identifying this object.
   */
  public EntityID getID();

  /**
   * Set the ship's current speed
   */
  public void setSpeed(int newSpeed);

  /**
   * Returns the ship's current speed.
   *
   * @return The current speed, -1 if undefined.
   */
  public int getSpeed();

  /**
   * Set the amount of Ships in a Fleet
   */
  public void setAmount(int _amount);

  /**
   * @return The amount of ships in a fleet, or 1
   */
  public int getAmount();

  /**
   * @return The projected amount of ships in a fleet, or 1
   */
  public int getModifiedAmount();

  /**
   * @return The projected size of ships in a fleet
   */
  public int getModifiedSize();

  /**
   * @return The temporary ships that depend on this ship.
   */
  public List<Ship> getTempShips();

  /**
   * @return A new ship that depends on this ship.
   */
  public Ship createTempShip();

}
