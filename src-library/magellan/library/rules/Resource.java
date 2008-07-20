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

package magellan.library.rules;

/**
 * A Resource reflects a needed resource for  e.g. building roads (stones, building) e.g. a
 * building (stones, wood, iron, silver e.g. recruiting (silver, potion?)
 */
public class Resource {
	private int amount = 1;
	private ObjectType type = null;

	/**
	 * Creates a new Resource object.
	 */
	public Resource() {
	}

	/**
	 * Creates a new Resource object.
	 *
	 * 
	 */
	public Resource(int amount) {
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
	public void setAmount(int amount) {
		this.amount = amount;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public ObjectType getObjectType() {
		return type;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setObjectType(ObjectType type) {
		this.type = type;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public String toString() {
		return "Resource: " + amount + " " + type;
	}
}
