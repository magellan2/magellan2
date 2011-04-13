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

/*
 * ItemCategory.java
 *
 * Created on 9. März 2002, 20:39
 */
package magellan.library.rules;

import magellan.library.StringID;

/**
 * A category is a group that an item can belong to, e.g. "weapons", "luxury goods" etc.
 * 
 * @author Andreas
 * @version 1.0
 */
public class ItemCategory extends Category {
  /**
   * Creates new ItemCategory
   */
  public ItemCategory(StringID id) {
    super(id);
  }

  /**
   * Creates a new ItemCategory object.
   */
  public ItemCategory(StringID id, Category parent) {
    super(id, parent);
  }

  /**
   * Returns <code>true</code> if o is an ItemType and (transitively) belongs to this category.
   * 
   * @returns <code>true</code> if the top level ancestor of the item type o is a descendant of
   *          this, <code>false</code> otherwise, including if o is not an ItemType or
   *          <code>null</code>.
   */
  @Override
  public boolean isInstance(Object o) {
    if (o instanceof ItemType) {
      ItemType it = (ItemType) o;

      if (it.getCategory() != null)
        return it.getCategory().isDescendant(this);
    }

    return false;
  }

  /**
   * Returns the id uniquely identifying this object.
   */
  @Override
  public StringID getID() {
    return (StringID) id;
  }

}
