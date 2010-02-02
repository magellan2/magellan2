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

package magellan.library.gamebinding.e3a;

import magellan.library.Item;
import magellan.library.Rules;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.EresseaMovementEvaluator;



/**
 *
 * @author $Author: $
 * @version $Revision: 396 $
 */
public class E3AMovementEvaluator extends EresseaMovementEvaluator {
  protected E3AMovementEvaluator(Rules rules) {
    super(rules);
	}

  protected int getHorses(Unit unit) {
    int horses = 0;
    
    Item i = unit.getModifiedItem(getRules().getItemType(EresseaConstants.I_UHORSE, true));
    if(i != null) {
      horses += i.getAmount();
    }

    i = unit.getModifiedItem(getRules().getItemType(E3AConstants.I_STREITROSS, true));
    if(i != null) {
      horses += i.getAmount();
    }
    return horses;
  }

}
