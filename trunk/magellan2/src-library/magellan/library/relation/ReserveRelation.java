/*
 *  Copyright (C) 2000-2006 Roger Butenuth, Andreas Gampe,
 *                          Stefan Goetz, Sebastian Pappert,
 *                          Klaas Prause, Enno Rehling,
 *                          Sebastian Tusk, Ulrich Kuester,
 *                          Ilja Pavkovic, Steffen Mecke
 *
 * This file is part of the Eressea Java Code Base, see the
 * file LICENSING for the licensing information applying to
 * this file.
 *
 */
package magellan.library.relation;

import magellan.library.Unit;
import magellan.library.gamebinding.ReserveOrder;
import magellan.library.rules.ItemType;

/**
 * This relation holds information about a unit reserving some item.
 * 
 * @author stm
 */
public class ReserveRelation extends UnitRelation {

  /**
   * The type of item being reserved
   */
  public ItemType itemType;
  /**
   * The amount that is actually reserved. Usually min(item.amount, simpleAmount *
   * (each?unit.getPersons():1))
   */
  public int amount;

  /**
   * Constructs a ReserveRelation (with warning parameter).
   * 
   * @param source The reserving unit
   * @param amount The amount. {@link ReserveOrder#ALL} for "ALLES"
   * @param itemType The item (type)
   * @param line The line number in the unit's orders
   * @param warning true iff a warning should be displayed
   */
  public ReserveRelation(Unit source, int amount, ItemType itemType, int line, boolean warning) {
    this(source, source, amount, itemType, line, warning);
  }

  /**
   * Constructs a ReserveRelation (with warning parameter).
   * 
   * @param origin The reserving unit
   * @param source The reserving unit
   * @param amount The amount. {@link ReserveOrder#ALL} for "ALLES"
   * @param itemID The item (type)
   * @param line The line number in the unit's orders
   * @param warning true iff a warning should be displayed
   */
  public ReserveRelation(Unit origin, Unit source, int amount, ItemType type, int line,
      boolean warning) {
    super(origin, source, line, warning);
    if (type == null)
      throw new NullPointerException();
    this.amount = amount;
    itemType = type;
  }

  /*
   * (non-Javadoc)
   * @see com.eressea.relation.UnitRelation#toString()
   */
  @Override
  public String toString() {
    return super.toString() + "@ITEMTYPE=" + itemType + "@AMOUNT=" + amount;
  }

}
