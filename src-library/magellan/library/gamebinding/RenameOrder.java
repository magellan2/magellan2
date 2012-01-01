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

  public enum RenameObject {
    /** unknown type */
    T_UNKNOWN,
    /** marks a BENENNE EINHEIT order */
    T_UNIT,
    /** marks a BENENNE BURG order */
    T_BUILDING,
    /** marks a BENENNE SCHIFF order */
    T_SHIP,
    /** marks a BENENNE PARTEI order */
    T_FACTION,
    /** marks a BENENNE REGION order */
    T_REGION,
    /** marks a BENENNE ALLIANZ order */
    T_ALLIANCE,
    /** unknown type -- description */
    T_DESCRIBE_UNKNOWN,
    /** marks a BESCHREIBE EINHEIT order */
    T_DESCRIBE_UNIT,
    /** marks a BESCHREIBE BURG order */
    T_DESCRIBE_BUILDING,
    /** marks a BESCHREIBE SCHIFF order */
    T_DESCRIBE_SHIP,
    /** marks a BESCHREIBE PARTEI order */
    T_DESCRIBE_FACTION,
    /** marks a BESCHREIBE REGION order */
    T_DESCRIBE_REGION,
    /** does not exist, currently */
    T_DESCRIBE_ALLIANCE;
  }

  /**
   * The order type (Unit, Faction, etc.)
   */
  public RenameObject type;

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
  public RenameOrder(List<OrderToken> tokens, String text, RenameObject type, EntityID target,
      String name) {
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
    case T_DESCRIBE_UNIT:
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
    case T_DESCRIBE_BUILDING:
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
    case T_DESCRIBE_SHIP:
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
    case T_DESCRIBE_FACTION:
      if (target != null) {
        named = unit.getData().getFaction(target);
      } else {
        named = unit.getFaction();
      }
      break;
    case T_REGION:
    case T_DESCRIBE_REGION:
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

    case T_UNKNOWN:
      setWarning(unit, line, Resources.get("order.name.warning.namewhat"));
      break;
    case T_DESCRIBE_UNKNOWN:
      setWarning(unit, line, Resources.get("order.name.warning.describewhat"));
      break;
    case T_ALLIANCE:
      break;
    case T_DESCRIBE_ALLIANCE:
      break;
    }
    if (named != null && type.ordinal() < RenameObject.T_DESCRIBE_UNKNOWN.ordinal()) {
      UnitRelation rel = new RenameNamedRelation(unit, named, name, line);
      rel.add();
    }
  }

}
