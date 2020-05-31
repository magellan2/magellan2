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

package magellan.library.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import magellan.library.EntityID;
import magellan.library.GameData;
import magellan.library.HasRegion;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.gamebinding.GameSpecificStuff;
import magellan.library.gamebinding.MovementEvaluator;
import magellan.library.relation.ShipTransferRelation;
import magellan.library.rules.ShipType;
import magellan.library.utils.Cache;
import magellan.library.utils.Resources;
import magellan.library.utils.Units;
import magellan.library.utils.logging.Logger;

/**
 * A class for representing a ship.
 *
 * @author $Author: $
 * @version $Revision: 389 $
 */
public class MagellanShipImpl extends MagellanUnitContainerImpl implements Ship, HasRegion {
  private static final Logger log = Logger.getInstance(MagellanShipImpl.class);

  /** The shore the ship is lying. */
  protected int shoreId = -1; // 0
  // =
  // northwest,
  // 1
  // =
  // northeast,
  // etc.
  // -1 = every direction

  /**
   * The size of this ship. While the ship is being built, size &lt;= getType().getMaxSize() is
   * true. After the ship is finished, size equals getType().getMaxSize().
   */
  protected int size = -1;

  /** The ratio to which degree this ship is damaged. Values range from 0 to 100. */
  protected int damageRatio = 0;

  /**
   * The weight of the units and items on this ship in GE.
   *
   * @deprecated replaced by cargo
   */
  @Deprecated
  protected int deprecatedLoad = -1;

  /**
   * The maximum payload of this ship in GE. 0 &lt;= capacity &lt;= getType().getCapacity() if the
   * ship is damaged.
   *
   * @deprecated replaced by capacity
   */
  @Deprecated
  protected int deprecatedCapacity = -1;

  /** the weight of the units and items on this ship in silver */
  protected int cargo = -1;

  /**
   * The maximum payload of this ship in silver. 0 &lt;= capacity &lt;= getType().getCapacity() if
   * the ship is damaged.
   */
  protected int capacity = -1;

  /** The maximum capacity for persons &lt;=getType().getMaxPersons() */
  protected int maxPersons = -1;

  /** number of ships in a fleet (Eressea since CR - Version 67 (ca 12/2019) */
  protected int amount = 1;

  /**
   * Creates a new Ship object.
   *
   * @param id
   * @param data
   */
  public MagellanShipImpl(EntityID id, GameData data) {
    super(id, data);
  }

  /** The region this ship is in. */
  private Region region = null;

  private int speed = -1;

  private List<Ship> ships;
  private HashSet<EntityID> shipIds;
  private Ship parent;

  /**
   * Sets the region this ship is in and notifies region about it.
   *
   * @param region
   */
  public void setRegion(Region region) {
    if (this.region != null) {
      this.region.removeShip(this);
    }

    this.region = region;

    if (this.region != null) {
      this.region.addShip(this);
    }
  }

  /**
   * Returns the region this ship is in.
   *
   * @return The region the ship is in, possibly null
   */
  public Region getRegion() {
    return region;
  }

  /**
   * The type of this ship.
   *
   * @return The type of this ship
   */
  public ShipType getShipType() {
    return (ShipType) getType();
  }

  /**
   * Returns the maximum capacity with respect to damages of the ship in silver.
   *
   * @return Returns the maximum capacity with respect to damages of the ship in silver
   */
  public int getMaxCapacity() {
    if (capacity != -1)
      return capacity;
    return (deprecatedCapacity != -1) ? deprecatedCapacity * 100 : getMaxCapacity(getShipType()
        .getCapacity() * 100 * getAmount());
  }

  /**
   * Returns the expected / projected maximum capacity with respect to damages of the ship in
   * silver.
   *
   * @return Returns the expected / projected maximum capacity with respect to damages of the ship
   *         in silver
   */
  public int getModifiedMaxCapacity() {
    return (getMaxCapacity(getShipType().getCapacity() * 100 * getModifiedAmount()));
  }

  /**
   * Returns the maximum capacity with respect to damages of the ship in GE if the undamaged
   * capacity was <code>maxCapacity</code>.
   *
   * @param maxCapacity The capacity is calculated relative to this capacity
   * @return The max damaged capacity
   */
  private int getMaxCapacity(int maxCapacity) {
    // (int)(maxCapacity*(100-damageRatio)/100)
    return new BigDecimal(maxCapacity).multiply(new BigDecimal(100 - damageRatio)).divide(
        new BigDecimal(100), BigDecimal.ROUND_DOWN).intValue();
  }

  /**
   * Returns the cargo load of this ship.
   *
   * @return Returns the cargo load of this ship
   */
  public int getCargo() {
    if (cargo != -1)
      return cargo;
    if (deprecatedLoad != -1)
      return deprecatedLoad * 100;
    else
      return -1;
  }

  /**
   * Calculates the weight of all units of this ship. This is usually less precise than
   * {@link #getCargo()}.
   *
   * @return The load of the ship
   */
  public int getLoad() {
    int modLoad = 0;
    // subtract all units initially on the ship with their initial weight
    for (final Unit u : units()) {
      modLoad += getGameSpecificStuff().getMovementEvaluator().getWeight(u);
      // if persons and cargo are counted separately (E3), remove persons' weight here
      if (getShipType().getMaxPersons() > 0) {
        modLoad -= u.getPersons() * u.getRace().getWeight() * 100;
      }
    }
    return modLoad;
  }

  /**
   * Returns the (modified) weight of all (modified) units of this ship . The method does some delta
   * calculation to be more precise. The initial load is subtracted by the initial weight of the
   * initial units and added by the modified weight of the modified units.
   *
   * @return The modified load of the ship TODO: move to {@link MovementEvaluator}
   */
  public int getModifiedLoad() {
    // we do a delta calculation
    // therefore start with the cargo given in the report
    int modLoad = getCargo();

    if (modLoad < 0) {
      modLoad = 0;
    } else {
      modLoad -= getLoad();
    }

    // now we generally should have modLoad zero or near zero.
    // the difference to zero is the weight that we don't see or know, i.e.,
    // silver from factions where we don't have a report or
    // items/races where we don't know the weight

    // add now the current calculated weight of the units
    for (final Unit u : modifiedUnits()) {
      modLoad += getGameSpecificStuff().getMovementEvaluator().getModifiedWeight(u);
      // if persons and cargo are counted separately (E3), remove persons' weight here
      if (getShipType().getMaxPersons() > 0) {
        modLoad -= u.getModifiedPersons() * u.getRace().getWeight() * 100;
      }
    }
    return modLoad;
  }

  private GameSpecificStuff getGameSpecificStuff() {
    return data.getGameSpecificStuff();
  }

  /**
   * This is a helper function for showing inner object state.
   *
   * @return A debug message
   */
  public String toDebugString() {
    return "SHIP[" + "shoreId=" + shoreId + "," + "size=" + size + "," + "damageRation="
        + damageRatio + "," + "deprecatedLoad=" + deprecatedLoad + "," + "deprecatedCapacity="
        + deprecatedCapacity + "]";
  }

  /**
   * A string representation of this ship.
   *
   * @return A string representation of this ship
   */
  @Override
  public String toString() {
    return toString(true);
  }

  /**
   * Returns the string representation of this ship. If <code>printExtended</code> is true, type,
   * damage and remaing capacity are shown, too.
   *
   * @param printExtended Whether to return a more detailed description
   * @return A string representation of this ship
   */
  public String toString(boolean printExtended) {
    final StringBuffer sb = new StringBuffer();

    // we could use getModifiedName here but it seems a bit obtrusive (and hard to handle tree
    // updates)
    String myName = getName();
    if (myName == null) {
      myName = getType().toString() + " " + getID();
    }

    sb.append(myName).append(" (").append(getID().toString()).append(")");

    if (printExtended) {
      sb.append(", ");
      if (amount > 1) {
        sb.append(amount + "x ");
      }
      if (getModifiedAmount() != amount) {
        sb.append("(" + getModifiedAmount() + "x) ");
      }
      sb.append(getType());

      final int nominalShipSize = Units.getNominalSize(this);
      final int modifiedNominalShipSize = getShipType().getMaxSize() * getModifiedAmount();

      if (size != nominalShipSize) {
        sb.append(" (").append(size).append("/").append(nominalShipSize).append(")");
      }

      if (getModifiedSize() != modifiedNominalShipSize && (getModifiedSize() != size
          || modifiedNominalShipSize != nominalShipSize)) {
        sb.append(" (-> ").append(getModifiedSize()).append("/").append(modifiedNominalShipSize)
            .append(")");
      }

      if (damageRatio != 0) {
        sb.append(", ").append(damageRatio).append(Resources.get("ship.damage"));
      }
    }

    if (MagellanShipImpl.log.isDebugEnabled()) {
      MagellanShipImpl.log.debug("Ship.toString: " + sb.toString());
    }

    return sb.toString();
  }

  /**
   * Returns the value of capacity.
   *
   * @return Returns capacity.
   */
  public int getCapacity() {
    return capacity;
  }

  /**
   * Sets the value of capacity.
   *
   * @param capacity The value for capacity.
   */
  public void setCapacity(int capacity) {
    this.capacity = capacity;
  }

  /**
   * Returns the value of damageRatio.
   *
   * @return Returns damageRatio.
   */
  public int getDamageRatio() {
    return damageRatio;
  }

  /**
   * Sets the value of damageRatio.
   *
   * @param damageRatio The value for damageRatio.
   */
  public void setDamageRatio(int damageRatio) {
    this.damageRatio = damageRatio;
  }

  /**
   * Returns the value of deprecatedCapacity.
   *
   * @return Returns deprecatedCapacity.
   */
  public int getDeprecatedCapacity() {
    return deprecatedCapacity;
  }

  /**
   * Sets the value of deprecatedCapacity.
   *
   * @param deprecatedCapacity The value for deprecatedCapacity.
   */
  public void setDeprecatedCapacity(int deprecatedCapacity) {
    this.deprecatedCapacity = deprecatedCapacity;
  }

  /**
   * Returns the value of deprecatedLoad.
   *
   * @return Returns deprecatedLoad.
   */
  public int getDeprecatedLoad() {
    return deprecatedLoad;
  }

  /**
   * Sets the value of deprecatedLoad.
   *
   * @param deprecatedLoad The value for deprecatedLoad.
   */
  public void setDeprecatedLoad(int deprecatedLoad) {
    this.deprecatedLoad = deprecatedLoad;
  }

  /**
   * Returns the value of shoreId.
   *
   * @return Returns shoreId.
   */
  public int getShoreId() {
    return shoreId;
  }

  /**
   * Sets the value of shoreId.
   *
   * @param shoreId The value for shoreId.
   */
  public void setShoreId(int shoreId) {
    this.shoreId = shoreId;
  }

  /**
   * Returns the value of size.
   *
   * @return Returns size.
   */
  public int getSize() {
    return size;
  }

  /**
   * Sets the value of size.
   *
   * @param size The value for size.
   */
  public void setSize(int size) {
    this.size = size;
  }

  /**
   * Sets the value of cargo.
   *
   * @param cargo The value for cargo.
   */
  public void setCargo(int cargo) {
    this.cargo = cargo;
  }

  /**
   * @see magellan.library.Ship#setMaxPersons(int)
   */
  public void setMaxPersons(int persons) {
    maxPersons = persons;
  }

  /**
   * @see magellan.library.Ship#getMaxPersons()
   */
  public int getMaxPersons() {
    /* max persons are not modified by damage */
    /* return (maxPersons != -1) ? maxPersons : getMaxCapacity(getShipType().getMaxPersons()); */
    return (maxPersons != -1) ? maxPersons : getShipType().getMaxPersons();
  }

  /**
   * @see magellan.library.Ship#setAmount(int)
   */
  public void setAmount(int _amount) {
    amount = _amount;
  }

  /**
   * @see magellan.library.Ship#getAmount()
   */
  public int getAmount() {
    return amount;
  }

  /**
   * @see magellan.library.Ship#getModifiedPersonLoad() TODO: move to {@link MovementEvaluator}
   */
  public int getModifiedPersonLoad() {
    int modInmates = 0;
    for (final Unit u : modifiedUnits()) {
      modInmates += u.getModifiedPersons() * u.getRace().getWeight() * 100;
    }
    return modInmates;
  }

  /**
   * @see magellan.library.Ship#getPersonLoad()
   */
  public int getPersonLoad() {
    int inmates = 0;
    for (final Unit u : units()) {
      inmates += u.getPersons() * u.getRace().getWeight() * 100;
    }
    return inmates;
  }

  /**
   * Returns the id uniquely identifying this object.
   */
  @Override
  public EntityID getID() {
    return (EntityID) super.getID();
  }

  /**
   * @see magellan.library.Ship#getSpeed()
   */
  public int getSpeed() {
    return speed;
  }

  /**
   * @see magellan.library.Ship#setSpeed(int)
   */
  public void setSpeed(int newSpeed) {
    speed = newSpeed;
  }

  /**
   * Returns the number of Ships in this fleet as it would be after the orders of this and other
   * units have been processed since it may be modified by transfer orders.
   */
  public int getModifiedAmount() {
    Cache cache1 = getCache();

    if (cache1.modifiedAmount == -1) {
      cache1.modifiedAmount = getAmount();
      for (ShipTransferRelation tr : getShipTransferRelations()) {
        if (equals(tr.ship)) {
          cache1.modifiedAmount -= tr.amount;
        } else {
          cache1.modifiedAmount += tr.amount;
        }
      }
    }

    return cache1.modifiedAmount;
  }

  /**
   * Returns the size of this convoy as it would be after the orders of this and other units have
   * been processed since it may be modified by transfer orders.
   */
  public int getModifiedSize() {
    Cache cache1 = getCache();
    if (cache1.modifiedSize == -1) {
      cache1.modifiedSize = getSize();
      for (ShipTransferRelation tr : getShipTransferRelations()) {
        int sizeAmount = tr.amount * tr.ship.getSize() / tr.ship.getAmount();
        if (equals(tr.ship)) {
          cache1.modifiedSize -= sizeAmount;
        } else {
          cache1.modifiedSize += sizeAmount;
        }
      }
    }
    return cache1.modifiedSize;
  }

  /**
   * Returns a collection of the ship transfer relations associated with the owner of this
   * ship/fleet
   *
   * @return a collection of ShipTransferRelation objects.
   */
  public List<ShipTransferRelation> getShipTransferRelations() {
    return getRelations(ShipTransferRelation.class);
  }

  public List<Ship> getTempShips() {
    if (ships == null)
      return Collections.emptyList();
    return Collections.unmodifiableList(ships);
  }

  public Ship createTempShip() {
    if (getParent() != null)
      return getParent().createTempShip();
    if (ships == null) {
      ships = new ArrayList<Ship>(3);
      shipIds = new HashSet<EntityID>(3);
    }

    EntityID targetId = EntityID.createEntityID(-getID().intValue(), data.base);
    for (int id = targetId.intValue(); shipIds.contains(targetId); --id) {
      targetId = EntityID.createEntityID(id, data.base);
    }

    MagellanShipImpl tempShip = new MagellanShipImpl(targetId, data);
    tempShip.setParent(this);

    tempShip.setName("TEMP SHIP " + targetId);
    tempShip.setType(getShipType());
    tempShip.setShoreId(getShoreId());
    tempShip.setMaxPersons(getMaxPersons());
    tempShip.setSpeed(getSpeed());
    tempShip.setDamageRatio(getDamageRatio());

    tempShip.setCapacity(0);
    tempShip.setCargo(0);
    tempShip.setSize(0);
    tempShip.setAmount(0);

    // data.addShip(tempShip);
    // tempShip.setRegion(ship.getRegion());
    tempShip.region = getRegion();

    ships.add(tempShip);
    shipIds.add(tempShip.getID());
    return tempShip;
  }

  private void setParent(MagellanShipImpl parent) {
    if (parent.getParent() != null)
      throw new RuntimeException("Temp ship with temp ships " + parent);
    this.parent = parent;
  }

  private Ship getParent() {
    return parent;
  }

  @Override
  public void clearRelations() {
    super.clearRelations();
    if (ships != null) {
      ships.clear();
      shipIds.clear();
    }
  }
}
