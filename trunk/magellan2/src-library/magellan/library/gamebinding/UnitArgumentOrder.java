// class magellan.library.gamebinding.ReserveOrder
// created on Aug 12, 2010
//
// Copyright 2003-2010 by magellan project team
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc., 
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
package magellan.library.gamebinding;

import java.util.List;

import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.utils.OrderToken;
import magellan.library.utils.Resources;

/**
 * An order with one or more units as arguments.
 * 
 * @author stm
 */
public class UnitArgumentOrder extends SimpleOrder {

  protected UnitID target;

  /**
   * @param tokens
   * @param text
   */
  public UnitArgumentOrder(List<OrderToken> tokens, String text, UnitID target) {
    super(tokens, text);
    this.target = target;
  }

  /**
   * Returns the value of unit.
   * 
   * @return Returns unit.
   */
  public UnitID getUnit() {
    return target;
  }

  /**
   * Returns the target unit. Returns <code>null</code> if <code>regionOnly==true</code> and the
   * target unit is not in the same region as unit.
   * 
   * @param data
   * @param unit
   * @param sameRegionOnly
   */
  protected Unit getTargetUnit(GameData data, Unit unit, int line, boolean sameRegionOnly,
      boolean zeroAllowed) {
    if (target == null)
      return null;
    if (!zeroAllowed && target.intValue() == 0) {
      setWarning(unit, line, Resources.get("order.all.warning.zeronotallowed"));
    }

    Unit tUnit = data.getUnit(target);
    if (tUnit == null) {
      tUnit = data.getTempUnit(target);
    }
    if (tUnit != null && (!sameRegionOnly || tUnit.getRegion() == unit.getRegion()))
      return tUnit;
    else
      return null;
  }

}
