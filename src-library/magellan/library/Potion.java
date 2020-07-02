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

import java.util.Collection;

/**
 * Container class for a potion based on its representation in a cr version &ge; 42.
 */
public interface Potion extends Described {
  /**
   * Sets the level of this Potion.
   */
  public void setLevel(int level);

  /**
   * Returns the level of this Potion.
   */
  public int getLevel();

  /**
   * Returns the ingredients required for this potion. The elements are instances of class Item.
   */
  public Collection<Item> ingredients();

  /**
   * Returns a specific ingredient of this potion.
   * 
   * @param key the item type id of the ingredient to be returned.
   */
  public Item getIngredient(ID key);

  /**
   * Puts a new element into the list of ingredients required to brew this potion.
   */
  public Item addIngredient(Item i);

  /**
   * Removes an item from the list of ingredients required to brew this potion.
   * 
   * @param key the id of the item's item type to be removed.
   */
  public Item removeIngredient(ID key);

  /**
   * Removes all ingredients of this potion.
   */
  public void clearIngredients();

  /**
   * @see magellan.library.Identifiable#getID()
   */
  public IntegerID getID();
}
