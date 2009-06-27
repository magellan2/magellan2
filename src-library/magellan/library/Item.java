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

import magellan.library.rules.ItemType;

/**
 * A class representing an item in Eressea. Items are qualified by their type and a certain amount.
 * Mark that the item class is quite 'bare', i.e. its name and identifiability are not enforced by
 * sub-classing the respective interfaces.
 */
public class Item {
	private ItemType type;
	private int amount;

	/**
	 * Creates a new item of the specified type and with the specified amount.
	 *
	 * @throws NullPointerException if <code>type==null</code>
	 */
	public Item(ItemType type, int amount) {
		if(type != null) {
			this.type = type;
			this.amount = amount;
		} else {
			throw new NullPointerException("Item.Item(): specified item type is null!");
		}
	}

	/**
	 * Sets the amount. 
	 */
	public void setAmount(int amount) {
		this.amount = amount;
	}

	/**
	 * Returns the amount, i.e. the number of entities this item represents. 
	 */
	public int getAmount() {
		return amount;
	}

	/**
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
  public String toString() {
		return type.toString();
	}

	/**
	 * Returns the type of this item.
	 */
	public ItemType getItemType() {
		return type;
	}

	/**
   * Returns the type of this item.
	 *
	 * @deprecated Use getItemType() instead
	 */
	@Deprecated
  public ItemType getType() {
		return getItemType();
	}

	/**
	 * This method is a shortcut for calling this.getType().getName()
	 *
	 */
	public String getName() {
		return type.getName();
	}
  
  /**
   * Returns the name, quoted if required.
   */
  public String getOrderName() {
    return type.getOrderName();
  }
}
