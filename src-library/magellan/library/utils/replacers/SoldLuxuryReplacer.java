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
 * SoldLuxuryReplacer.java
 *
 * Created on 29. Dezember 2001, 16:12
 */
package magellan.library.utils.replacers;

import java.util.Iterator;

import magellan.library.ID;
import magellan.library.LuxuryPrice;
import magellan.library.Region;
import magellan.library.StringID;
import magellan.library.utils.Resources;

/**
 * Returns name of available luxury
 * 
 * @author Andreas
 * @version 1.0
 */
public class SoldLuxuryReplacer extends AbstractRegionReplacer {
  protected int mode;

  /**
   * Creates new SoldLuxuryReplacer
   * 
   * @param mode if 0: return name, if 1: return first letter, if 2: return first two letters, if 3:
   *          return price
   */
  public SoldLuxuryReplacer(int mode) {
    this.mode = mode;
  }

  /**
   * Returns name or value of available luxury.
   * 
   * @see magellan.library.utils.replacers.AbstractRegionReplacer#getRegionReplacement(magellan.library.Region)
   */
  @Override
  public Object getRegionReplacement(Region r) {
    if (r.getPrices() != null) {
      Iterator<StringID> it = r.getPrices().keySet().iterator();

      while (it.hasNext()) {
        ID id = it.next();
        LuxuryPrice lp = r.getPrices().get(id);

        if (lp.getPrice() < 0) {
          switch (mode) {
          case 0:
            return id.toString();

          case 1:
            return new String(lp.getItemType().getName().toCharArray(), 0, 1);

          case 2:
            return new String(lp.getItemType().getName().toCharArray(), 0, 2);

          case 3:
            return Integer.valueOf(-lp.getPrice());
          }
        }
      }
    }

    return null;
  }

  /**
   * @see magellan.library.utils.replacers.Replacer#getDescription()
   */
  public String getDescription() {
    return Resources.get("util.replacers.soldluxuryreplacer.description." + mode);
  }

}
