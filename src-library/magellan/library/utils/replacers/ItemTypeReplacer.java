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

package magellan.library.utils.replacers;

import java.util.Collection;
import java.util.Iterator;

import magellan.library.Item;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.rules.ItemType;
import magellan.library.utils.Resources;

/**
 * Replaces a item name string with the number of that item in the given region.
 * 
 * @author unknown
 * @version 1.0
 */
public class ItemTypeReplacer extends AbstractParameterReplacer implements EnvironmentDependent {
  private static final Integer ZERO = new Integer(0);
  protected ReplacerEnvironment environment;

  /**
   * Creates a new ItemTypeReplacer object.
   */
  public ItemTypeReplacer() {
    super(1);
  }

  /**
   * Searches the units in the region given as argument for the item given as parameter. Sensitive
   * to the environment (for example unit filters).
   * 
   * @see magellan.library.utils.replacers.Replacer#getReplacement(java.lang.Object)
   */
  public Object getReplacement(Object region) {
    if (region instanceof Region) {
      String items = getParameter(0, region).toString();
      int count = 0;
      Collection<Unit> c =
          ((UnitSelection) environment.getPart(ReplacerEnvironment.UNITSELECTION_PART))
              .getUnits((Region) region);

      if (c == null)
        return null;

      Iterator<Unit> unitIt = c.iterator();

      while (unitIt.hasNext()) {
        Unit u = unitIt.next();
        Iterator<Item> itemIt = u.getItems().iterator();

        while (itemIt.hasNext()) {
          Item i = itemIt.next();
          ItemType ity = i.getItemType();

          if (ity.getName().equalsIgnoreCase(items)
              || ity.getID().toString().equalsIgnoreCase(items)) {
            count += i.getAmount();

            break;
          }
        }
      }

      if (count != 0)
        return new Integer(count);

      return ItemTypeReplacer.ZERO;
    }

    return null;
  }

  /**
   * @see magellan.library.utils.replacers.EnvironmentDependent#setEnvironment(magellan.library.utils.replacers.ReplacerEnvironment)
   */
  public void setEnvironment(ReplacerEnvironment env) {
    environment = env;
  }

  /**
   * @see magellan.library.utils.replacers.Replacer#getDescription()
   */
  @Override
  public String getDescription() {
    return Resources.get("util.replacers.itemtypereplacer.description") + "\n\n"
        + super.getDescription();
  }
}
