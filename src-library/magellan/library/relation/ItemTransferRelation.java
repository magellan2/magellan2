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
import magellan.library.rules.ItemType;

/**
 * A relation indicating that a unit transfers a certain amount of an item to another unit.
 */
public class ItemTransferRelation extends TransferRelation {
  /** The transferred item. */
  public ItemType itemType;

  /**
   * Creates a new ItemTransferRelation object without warning.
   */
  /**
   * @param source
   * @param target
   * @param amount
   * @param type
   * @param line
   */
  public ItemTransferRelation(Unit source, Unit target, int amount, ItemType type, int line) {
    this(source, source, target, amount, type, line);
  }

  /**
   * Creates a new ItemTransferRelation object.
   * 
   * @param source The source unit
   * @param target The target unit
   * @param amount The amount to transfer
   * @param type The item to transfer
   * @param line The line in the source's orders
   * @param warning
   */
  public ItemTransferRelation(Unit source, Unit target, int amount, ItemType type, int line,
      boolean warning) {
    this(source, source, target, amount, type, line);
  }

  /**
   * Creates a new ItemTransferRelation object.
   * 
   * @param origin The origin unit
   * @param source The source unit
   * @param target The target unit
   * @param amount The amount to transfer
   * @param type The item to transfer
   * @param line The line in the source's orders
   */
  public ItemTransferRelation(Unit origin, Unit source, Unit target, int amount, ItemType type,
      int line) {
    super(origin, source, target, amount, line);
    if (type == null)
      throw new NullPointerException();
    itemType = type;
  }

  /**
   * @see magellan.library.relation.TransferRelation#toString()
   */
  @Override
  public String toString() {
    return super.toString() + "@ITEMTYPE=" + itemType;
  }
}
