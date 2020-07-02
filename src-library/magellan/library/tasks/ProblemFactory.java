// class magellan.library.tasks.ProblemFactory
// created on Jun 9, 2009
//
// Copyright 2003-2009 by magellan project team
//
// Author : $Author: stm$
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
package magellan.library.tasks;

import magellan.library.Battle;
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.HasRegion;
import magellan.library.Message;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.UnitID;
import magellan.library.impl.MagellanMessageImpl;
import magellan.library.tasks.Problem.Severity;
import magellan.library.utils.Resources;

/**
 * Creates problems for the TaskTable.
 */
public class ProblemFactory {

  /**
   * Creates a problem with all parameters given;
   *
   * @param severity
   * @param type One of {@link Problem.Severity#INFORMATION}, {@link Problem.Severity#WARNING},
   *          {@link Problem.Severity#ERROR}
   * @param region A region where the problem occurs
   * @param owner The unit responsible for this problem or <code>null</code>. If
   *          <code>line &ge; 0 </code>, it refers to an order of this unit.
   * @param faction The faction this problem belongs to or <code>null</code>
   * @param object The object that this problem criticizes
   * @param inspector The Inspector that reported this problem
   * @param message The message text of the problem
   * @param line The line number in the orders of owner where the problem occurred or -1 if no such
   *          order can be identified. The first line is line 1!
   */
  public static SimpleProblem createProblem(Severity severity, ProblemType type, Region region,
      Unit owner, Faction faction, Object object, Inspector inspector, String message, int line) {
    return new SimpleProblem(severity, type, region, owner, faction, object, inspector, message,
        line);
  }

  /**
   * Creates a problem. Tries to deduce region and faction from the Unit, and the message from the
   * ProblemType.
   *
   * @param severity
   * @param type
   * @param unit The unit responsible for this problem or <code>null</code>. If
   *          <code>line &ge; 0 </code>, it refers to an order of this unit.
   * @param inspector
   * @param line The line number in the orders of owner where the problem occurred or -1 if no such
   *          order can be identified. The first line is line 1!
   */
  public static SimpleProblem createProblem(Severity severity, ProblemType type, Unit unit,
      Inspector inspector, int line) {
    return new SimpleProblem(severity, type, unit.getRegion(), unit, unit.getFaction(), unit,
        inspector, type.getMessage(), line);
  }

  /**
   * Creates a problem without line. Tries to deduce region and faction from the Unit, and the
   * message from the ProblemType.
   *
   * @param severity
   * @param type
   * @param unit The unit responsible for this problem or <code>null</code>. If
   *          <code>line &ge; 0 </code>, it refers to an order of this unit.
   * @param inspector
   */
  public static SimpleProblem createProblem(Severity severity, ProblemType type, Unit unit,
      Inspector inspector) {
    return new SimpleProblem(severity, type, unit.getRegion(), unit, unit.getFaction(), unit,
        inspector, type.getMessage(), -1);
  }

  /**
   * Creates a problem. Tries to deduce unit, region and faction from the UnitContainer, and the
   * message from the ProblemType.
   *
   * @param severity
   * @param type
   * @param container
   * @param inspector
   * @param line The line number in the orders of owner where the problem occured or -1 if no such
   *          order can be identified. The first line is line 1!
   */
  public static SimpleProblem createProblem(Severity severity, ProblemType type,
      UnitContainer container, Inspector inspector, int line) {
    return new SimpleProblem(severity, type, container.getOwner() == null ? null : container
        .getOwner().getRegion(), container.getOwner(), container.getOwner() == null ? null
            : container.getOwner().getFaction(), container, inspector, type.getMessage(), line);
  }

  /**
   * Creates a problem without line. Tries to deduce unit, region and faction from the
   * UnitContainer, and the message from the ProblemType.
   *
   * @param severity
   * @param type
   * @param container
   * @param inspector
   */
  public static SimpleProblem createProblem(Severity severity, ProblemType type,
      UnitContainer container, Inspector inspector) {
    Region r = null;
    if (container instanceof HasRegion) {
      r = ((HasRegion) container).getRegion();
    } else {
      r = container.getOwner() == null ? null : container.getOwner().getRegion();
    }

    return new SimpleProblem(severity, type, r, container.getOwner(), container.getOwner() == null
        ? null : container.getOwner().getFaction(), container, inspector, type.getMessage(), -1);
  }

  /**
   * Creates a problem. Tries to deduce region and faction from the Unit, but uses the given
   * message.
   *
   * @param severity
   * @param type
   * @param unit The unit responsible for this problem or <code>null</code>. If
   *          <code>line &ge; 0 </code>, it refers to an order of this unit.
   * @param inspector
   * @param message
   * @param line The line number in the orders of owner where the problem occured or -1 if no such
   *          order can be identified. The first line is line 1!
   */
  public static SimpleProblem createProblem(Severity severity, ProblemType type, Unit unit,
      Inspector inspector, String message, int line) {
    return new SimpleProblem(severity, type, unit.getRegion(), unit, unit.getFaction(), unit,
        inspector, message, line);
  }

  /**
   * Creates a dummy problem without object, inspector, or line.
   *
   * @param severity
   * @param type
   */
  public static Problem createProblem(Severity severity, ProblemType type) {
    return new SimpleProblem(severity, type, null, null, null, null, null, null, -1);
  }

  public static Problem createProblem(GameData data, Battle b, Faction faction, Region region,
      Unit owner, Object object, MessageInspector inspector) {
    return new BattleProblem(b, faction, region, owner, object, inspector);
  }

  public static Problem createProblem(GameData data, Message m, Faction faction, Region region,
      Unit owner, Object object, MessageInspector inspector) {

    // try to guess missing arguments by evaluating attributes
    if (m.getAttributes() != null) {
      if (owner == null) {
        String attribute = m.getAttributes().get("unit");
        if (attribute != null) {
          UnitID id;
          try {
            id = UnitID.createUnitID(attribute, 10, data.base);
          } catch (NumberFormatException e) {
            id = null;
          }
          if (id != null) {
            owner = data.getUnit(id);
          }
        }
      }
      if (object == null) {
        String attribute = m.getAttributes().get("target");
        if (attribute != null) {
          UnitID id;
          try {
            id = UnitID.createUnitID(attribute, 10, data.base);
          } catch (NumberFormatException e) {
            id = null;
          }
          if (id != null) {
            object = data.getUnit(id);
          }
        }
      }
      if (object == null) {
        String attribute = m.getAttributes().get("mage");
        if (attribute != null) {
          UnitID id;
          try {
            id = UnitID.createUnitID(attribute, 10, data.base);
          } catch (NumberFormatException e) {
            id = null;
          }
          if (id != null) {
            object = data.getUnit(id);
          }
        }
      }

      if (region == null) {
        String attribute = m.getAttributes().get("region");
        if (attribute != null) {
          CoordinateID coord = CoordinateID.parse(attribute, ",");
          if (coord == null) {
            coord = CoordinateID.parse(attribute, " ");
          }
          if (coord != null) {
            region = data.getRegion(coord);
          }
        }
      }
      if (region == null) {
        String attribute = m.getAttributes().get("end");
        if (attribute != null && m.getAttributes().get("start") != null) {
          CoordinateID coord = CoordinateID.parse(attribute, ",");
          if (coord == null) {
            coord = CoordinateID.parse(attribute, " ");
          }
          if (coord != null) {
            region = data.getRegion(coord);
          }
        }
      }
      if (object == null) {
        String attribute = m.getAttributes().get("ship");
        if (attribute != null) {
          EntityID id;
          try {
            id = EntityID.createEntityID(attribute, 10, data.base);
          } catch (NumberFormatException e) {
            id = null;
          }
          if (id != null) {
            object = data.getShip(id);
          }
        }
      }
      if (object == null) {
        String attribute = m.getAttributes().get("building");
        if (attribute != null) {
          EntityID id;
          try {
            id = EntityID.createEntityID(attribute, 10, data.base);
          } catch (NumberFormatException e) {
            id = null;
          }
          if (id != null) {
            object = data.getBuilding(id);
          }
        }
      }

      if (region == null && owner != null) {
        region = owner.getRegion();
      }
      if (region == null && object instanceof HasRegion) {
        region = ((HasRegion) object).getRegion();
      }
    }
    if (object == null) {
      object = owner != null ? owner : region != null ? region : faction != null ? faction : null;
    }
    return new MessageProblem(m, faction, region, owner, object, inspector);
  }

  /**
   * Problem implementation tailored for {@link Message}s
   *
   * @author stm
   * @version 1.0, Nov 22, 2010
   */
  protected static abstract class MessageOrBattleProblem implements Problem {

    protected Faction faction;
    protected Region region;
    protected Unit unit;
    protected MessageInspector inspector;
    protected Object object;

    public MessageOrBattleProblem(Faction faction, Region region, Unit unit, Object object,
        MessageInspector inspector) {
      super();
      this.faction = faction;
      this.region = region;
      this.unit = unit;
      this.object = object;
      this.inspector = inspector;
    }

    /**
     * @see magellan.library.tasks.Problem#getSeverity()
     */
    public Severity getSeverity() {
      return Severity.INFORMATION;
    }

    /**
     * @see magellan.library.tasks.Problem#getInspector()
     */
    public Inspector getInspector() {
      return inspector;
    }

    /**
     * @see magellan.library.tasks.Problem#getLine()
     */
    public int getLine() {
      return -1;
    }

    /**
     * @see magellan.library.tasks.Problem#getObject()
     */
    public Object getObject() {
      return object;
    }

    /**
     * @see magellan.library.tasks.Problem#getFaction()
     */
    public Faction getFaction() {
      return faction;
    }

    /**
     * @see magellan.library.tasks.Problem#getRegion()
     */
    public Region getRegion() {
      return region;
    }

    /**
     * @see magellan.library.tasks.Problem#getOwner()
     */
    public Unit getOwner() {
      return unit;
    }

    /**
     * @see magellan.library.tasks.Problem#addSuppressComment()
     */
    public Unit addSuppressComment() {
      if (getInspector() != null)
        return getInspector().suppress(this);
      return null;
    }

  }

  /**
   * Problem implementation tailored for {@link Message}s
   *
   * @author stm
   * @version 1.0, Nov 22, 2010
   */
  public static class MessageProblem extends MessageOrBattleProblem {

    private Message message;

    public MessageProblem(Message message, Faction faction, Region region, Unit unit,
        Object object, MessageInspector inspector) {
      super(faction, region, unit, object, inspector);
      this.message = message;
    }

    /**
     * @see magellan.library.tasks.Problem#getType()
     */
    public ProblemType getType() {
      return inspector.getProblemType(message);
    }

    /**
     * @see magellan.library.tasks.Problem#getMessage()
     */
    public String getMessage() {
      return message.getText();
    }

    /**
     * @see magellan.library.tasks.Problem#addSuppressComment()
     */
    @Override
    public Unit addSuppressComment() {
      if (getInspector() != null)
        return getInspector().suppress(this);
      return null;
    }

    @Override
    public String toString() {
      return message.getText();
    }

    public Message getReportMessage() {
      return message;
    }
  }

  /**
   * Problem implementation tailored for {@link Message}s
   *
   * @author stm
   * @version 1.0, Nov 22, 2010
   */
  public static class BattleProblem extends MessageOrBattleProblem {

    private Battle battle;
    private Message message;

    public BattleProblem(Battle battle, Faction faction, Region region, Unit unit, Object object,
        MessageInspector inspector) {
      super(faction, region, unit, object, inspector);
      this.battle = battle;
      message =
          new MagellanMessageImpl(Message.ambiguousID, Resources.get(
              "tasks.messageinspector.battle.message", region), MessageInspector.BATTLE, null);
    }

    /**
     * @see magellan.library.tasks.Problem#getType()
     */
    public ProblemType getType() {
      return inspector.getProblemType(battle);
    }

    /**
     * @see magellan.library.tasks.Problem#getMessage()
     */
    public String getMessage() {
      return message.getText();
    }

    /**
     * @see magellan.library.tasks.Problem#addSuppressComment()
     */
    @Override
    public Unit addSuppressComment() {
      if (getInspector() != null)
        return getInspector().suppress(this);
      return null;
    }

    @Override
    public String toString() {
      return message.getText();
    }

    public Message getReportMessage() {
      for (Message m : battle.messages())
        return m;
      return message;
    }

  }

}
