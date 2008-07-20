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
	 * 
	 * 
	 *
	 * @throws IllegalArgumentException DOCUMENT-ME
	 */
	public Item(ItemType type, int amount) {
		if(type != null) {
			this.type = type;
			this.amount = amount;
		} else {
			throw new IllegalArgumentException("Item.Item(): specified item type is null!");
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setAmount(int amount) {
		this.amount = amount;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getAmount() {
		return amount;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public String toString() {
		return type.toString();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public ItemType getItemType() {
		return type;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * 
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
  
  public String getOrderName() {
    return type.getOrderName();
  }
}
