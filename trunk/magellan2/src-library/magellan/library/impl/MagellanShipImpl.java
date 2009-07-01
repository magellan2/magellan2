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

import magellan.library.GameData;
import magellan.library.HasRegion;
import magellan.library.ID;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.rules.ShipType;
import magellan.library.utils.logging.Logger;


/**
 * A class for representing a ship.
 *
 * @author $Author: $
 * @version $Revision: 389 $
 */
public class MagellanShipImpl extends MagellanUnitContainerImpl implements Ship,HasRegion {
	private static final Logger log = Logger.getInstance(MagellanShipImpl.class);

	/** The shore the ship is lying. */
	protected int shoreId = -1; // 0 = northwest, 1 = northeast, etc.
							 // -1 = every direction

	/**
	 * The size of this ship. While the ship is being built, size &lt;= getType().getMaxSize() is
	 * true. After the ship is finished,  size equals getType().getMaxSize().
	 */
  protected int size = -1;

	/** The ratio to which degree this ship is damaged. Values range from 0 to 100. */
  protected int damageRatio = 0;

	/** The weight of the units and items on this ship in GE. 
	 * @deprecated replaced by cargo
	 */
  @Deprecated
  protected int deprecatedLoad = -1;

	/**
	 * The maximum payload of this ship in GE. 0 &lt;= capacity &lt;= getType().getCapacity() if
	 * the ship is damaged.
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

	/**
	 * Creates a new Ship object.
	 *
	 * @param id 
	 * @param data 
	 */
	public MagellanShipImpl(ID id, GameData data) {
		super(id, data);
	}

	/** The region this ship is in. */
	private Region region = null;

	/**
	 * Sets the region this ship is in and notifies region about it.
	 *
	 * @param region 
	 */
	public void setRegion(Region region) {
		if(this.region != null) {
			this.region.removeShip(this);
		}

		this.region = region;

		if(this.region != null) {
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
	 * Returns the maximum capacity with respect to  damages of the ship in silver.
	 *
	 * @return Returns the maximum capacity with respect to  damages of the ship in silver
	 */
	@SuppressWarnings("deprecation")
  public int getMaxCapacity() {
		if(capacity != -1) {
			return capacity;
		}
		return (deprecatedCapacity != -1) ? deprecatedCapacity*100 : getMaxCapacity(getShipType().getCapacity()*100);
	}

	/**
	 * Returns the maximimum capacity with respect to damages of the ship in GE if the undamaged
	 * capacity was <code>maxCapacity</code>.
	 * 
	 * @param maxCapacity The capacity is calculated relative to this capacity 
	 * 
	 * @return The max damaged capacity
	 */
	private int getMaxCapacity(int maxCapacity) {
	  // (int)(maxCapacity*(100-damageRatio)/100)
		return new BigDecimal(maxCapacity).multiply(new BigDecimal(100 - damageRatio))
										  .divide(new BigDecimal(100), BigDecimal.ROUND_DOWN)
										  .intValue();
	}

	/**
	 * Returns the cargo load of this ship.
	 * 
	 * @return Returns the cargo load of this ship
	 */
	public int getCargo() {
		if(cargo != -1) {
      return cargo;
    }
		if (deprecatedLoad!=-1) {
      return deprecatedLoad*100;
    } else {
      return -1;
    }
	}
	
	/**
	 * Returns the weight of all units of this ship. The method does some
   * delta calculation to be more precise. The initial load is subtracted 
   * by the initial weight of the initial units and added by the modified
   * weight of the modified units. 
	 *
	 * @return The modified load of the ship
	 */
	public int getModifiedLoad() {
    // we do a delta calculation
    // therefore start with the cargo given in the report
		int modLoad = getCargo();

		if (modLoad<0) {
		  modLoad=0;
		} else {
		  // subtract all units initially on the ship with their initial weight
		  for(Unit u : units()) {
		    modLoad -= u.getWeight();
		    // if persons and cargo are counted separately (E3A), remove person's weight here
		    if (getShipType().getMaxPersons()>0)
		      modLoad += u.getPersons()*u.getRace().getWeight();
		  }
		}

		// now we generally should have modLoad zero or near zero.
    // the difference to zero is the weight that we don't see or know
    // (i.e.) silver from factions where we don't have a report or
    // items/races where we don't know the weight 
    
    // add now the current calculated weight of the units
    for(Unit u : modifiedUnits()) {
			modLoad += u.getModifiedWeight();
      // if persons and cargo are counted separately (E3A), remove person's weight here
      if (getShipType().getMaxPersons()>0)
        modLoad -= u.getPersons()*u.getRace().getWeight();
		}
		return modLoad;
	}

	/**
	 * This is a helper function for showing inner object state.
	 * 
	 * @return A debug message
	 */
	public String toDebugString() {
		return "SHIP[" + "shoreId=" + shoreId + "," + "size=" + size + "," + "damageRation=" +
			   damageRatio + "," + "deprecatedLoad=" + deprecatedLoad + "," + "deprecatedCapacity=" + deprecatedCapacity + "]";
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
	 * Returns the string representation of this ship. If <code>printExtended</code> is true,
	 * type, damage and remaing capacity are shown, too.
	 * 
	 * @param printExtended
	 *            Whether to return a more detailed description
	 * 
	 * @return A strig representation of this ship
	 */
	public String toString(boolean printExtended) {
		StringBuffer sb = new StringBuffer();

		sb.append(getName()).append(" (").append(this.getID().toString()).append(")");

		if(printExtended) {
			sb.append(", ").append(getType());

			int nominalShipSize = getShipType().getMaxSize();

			if(size != nominalShipSize) {
				sb.append(" (").append(size).append("/").append(nominalShipSize).append(")");
			}

			if(damageRatio != 0) {
        // TODO localize
				sb.append(", ").append(damageRatio).append("% Beschädigung");
			}
		}

		if(MagellanShipImpl.log.isDebugEnabled()) {
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
}
