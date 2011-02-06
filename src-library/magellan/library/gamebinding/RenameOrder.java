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

import magellan.library.Building;
import magellan.library.EntityID;
import magellan.library.GameData;
import magellan.library.Named;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.relation.RenameNamedRelation;
import magellan.library.relation.UnitRelation;
import magellan.library.utils.OrderToken;
import magellan.library.utils.Resources;

/**
 * An order with one or more units as arguments.
 * 
 * @author stm
 */
public class RenameOrder extends SimpleOrder {

  /** unknown type */
  public static final int T_UNKNOWN = 0;
  /** marks a BENENNE EINHEIT order */
  public static final int T_UNIT = 1;
  /** marks a BENENNE BURG order */
  public static final int T_BUILDING = 2;
  /** marks a BENENNE SCHIFF order */
  public static final int T_SHIP = 3;
  /** marks a BENENNE PARTEI order */
  public static final int T_FACTION = 4;
  /** marks a BENENNE REGION order */
  public static final int T_REGION = 5;

  /**
   * The order type (Unit, Faction, etc.)
   */
  public int type;

  /**
   * A target != <code>null</code> indicates BENNENE FREMDE... order
   */
  public EntityID target;
  /**
   * The new name
   */
  public String name;

  /**
   * @param tokens
   * @param text
   * @param name
   */
  public RenameOrder(List<OrderToken> tokens, String text, int type, EntityID target, String name) {
    super(tokens, text);
    this.type = type;
    this.target = target;
    this.name = name;
  }

  /**
   * Returns the value of target.
   * 
   * @return Returns target.
   */
  public EntityID getTarget() {
    return target;
  }

  /**
   * Returns the value of name.
   * 
   * @return Returns name.
   */
  public String getName() {
    return name;
  }

  @Override
  public void execute(ExecutionState state, GameData data, Unit unit, int line) {
    if (unit == null || !isValid())
      return;

    Named named = null;
    switch (type) {
    case T_UNIT:
      if (target != null) {
        Unit u = unit.getData().getUnit(target);
        if (u != null) {
          named = u.getRegion() == unit.getRegion() ? u : null;
        }
      } else {
        named = unit;
      }
      break;
    case T_BUILDING:
      if (target != null) {
        Building b = unit.getData().getBuilding(target);
        if (b != null) {
          named = b.getRegion() == unit.getRegion() ? b : null;
        }
      } else {
        if (unit.getBuilding() != null && unit.getBuilding().getOwnerUnit() != unit) {
          setWarning(unit, line, Resources.get("order.name.warning.owner"));
        } else {
          named = unit.getBuilding();
        }

      }
      break;
    case T_SHIP:
      if (target != null) {
        Ship s = unit.getData().getShip(target);
        if (s != null) {
          named = s.getRegion() == unit.getRegion() ? s : null;
        }
      } else {
        if (unit.getShip() != null && unit.getShip().getOwnerUnit() != unit) {
          setWarning(unit, line, Resources.get("order.name.warning.owner"));
        } else {
          named = unit.getShip();
        }
      }
      break;
    case T_FACTION:
      if (target != null) {
        named = unit.getData().getFaction(target);
      } else {
        named = unit.getFaction();
      }
      break;
    case T_REGION:
      if (target != null) {
        magellan.library.utils.logging.Logger.getInstance(this.getClass()).warn(
            "BENENNE FREMDE REGION geht nicht");
      } else {
        if (unit.getRegion().getOwnerUnit() != unit) {
          setWarning(unit, line, Resources.get("order.name.warning.owner"));
        } else {
          named = unit.getRegion();
        }
      }
      break;

    default:
      break;
    }
    if (named != null) {
      UnitRelation rel = new RenameNamedRelation(unit, named, name, line);
      rel.add();
    } else if (getProblem() == null) {
      setWarning(unit, line, Resources.get("order.name.warning.namewhat"));
    }
  }

}
