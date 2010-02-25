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
 * A class representing the price of a luxury good as they are offered in any region.
 */
public class LuxuryPrice {
  private final int price;
  private ItemType itemType;

  /**
   * Creates a new LuxuryPrice object with the specified luxury good and price.
   */
  public LuxuryPrice(ItemType itemType, int price) {
    this.price = price;
    this.itemType = itemType;
  }

  /**
   * The price of the item type.
   */
  public int getPrice() {
    return price;
  }

  /**
   * The item type the price is for.
   */
  public ItemType getItemType() {
    return itemType;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj instanceof LuxuryPrice)
      return price == ((LuxuryPrice) obj).price && itemType == ((LuxuryPrice) obj).itemType;
    return false;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return (17 + price << 5) << 5 + itemType.hashCode();
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return itemType.toString() + ":" + price;
  }
}
