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

import java.util.ArrayList;
import java.util.List;

import magellan.library.GameData;
import magellan.library.Item;
import magellan.library.Ship;
import magellan.library.StringID;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.UnitID;
import magellan.library.ZeroUnit;
import magellan.library.gamebinding.EresseaRelationFactory.EresseaExecutionState;
import magellan.library.relation.ControlRelation;
import magellan.library.relation.EnterRelation;
import magellan.library.relation.InterUnitRelation;
import magellan.library.relation.ItemTransferRelation;
import magellan.library.relation.LeaveRelation;
import magellan.library.relation.PersonTransferRelation;
import magellan.library.relation.ReserveRelation;
import magellan.library.relation.ShipTransferRelation;
import magellan.library.relation.UnitRelation;
import magellan.library.relation.UnitTransferRelation;
import magellan.library.rules.ItemCategory;
import magellan.library.rules.ItemType;
import magellan.library.tasks.OrderSyntaxInspector;
import magellan.library.tasks.Problem.Severity;
import magellan.library.tasks.ProblemFactory;
import magellan.library.tasks.ProblemType;
import magellan.library.utils.Locales;
import magellan.library.utils.OrderToken;
import magellan.library.utils.Resources;

/**
 * An order with one or more units as arguments.
 *
 * @author stm
 */
public class GiveOrder extends UnitArgumentOrder {

  public StringID type;
  protected boolean each;
  public int amount;
  public ItemType itemType;
  protected boolean all;

  /**
   * @param tokens
   * @param text
   * @param type
   */
  public GiveOrder(List<OrderToken> tokens, String text, UnitID target, StringID type) {
    super(tokens, text, target);
    this.type = type;
  }

  /**
   * Returns the value of type.
   *
   * @return Returns type.
   */
  public StringID getType() {
    return type;
  }

  /**
   * Returns the value of each.
   *
   * @return Returns each.
   */
  public boolean isEach() {
    return each;
  }

  /**
   * Returns the value of amount.
   *
   * @return Returns amount.
   */
  public int getAmount() {
    return amount;
  }

  /**
   * Returns the value of itemType.
   *
   * @return Returns itemType.
   */
  public ItemType getItemType() {
    return itemType;
  }

  /**
   * Returns the value of all.
   *
   * @return Returns all.
   */
  public boolean isAll() {
    return all;
  }

  /**
   * Problem types from give orders.
   */
  public enum GiveProblemTypes {
    /** target unit unknown */
    UNKNOWN_UNIT;

    protected ProblemType type;

    GiveProblemTypes() {
      String name = name().toLowerCase();
      type = ProblemType.create("tasks.ordersemanticsinspector", name);
    }

    /**
     * Returns the problem type.
     */
    public ProblemType getType() {
      return type;
    }
  }

  @Override
  public void execute(ExecutionState state, GameData data, Unit unit, int line) {
    // GIB (0|<enr>) (ALLES|KRÄUTER|KOMMANDO|EINHEIT)
    // GIB (0|<enr>) (([JE] <amount>)|ALLES)(SILBER|PERSONEN|<gegenstand>)))
    // GIB (0|<enr>) <amount> SCHIFF
    if (!isValid())
      return;

    if (unit instanceof TempUnit && type != EresseaConstants.OC_CONTROL) {
      setError(unit, line, Resources.get("order.give.warning.temp"));
      return;
    }

    Unit tUnit = getTargetUnit(data, unit, line, true);

    Unit zeroOrTarget;
    if (tUnit == null) {
      zeroOrTarget = unit.getRegion().getZeroUnit();
      if (type == EresseaConstants.OC_CONTROL || type == EresseaConstants.OC_UNIT) {
        setProblem(ProblemFactory.createProblem(Severity.WARNING,
            OrderSyntaxInspector.OrderSemanticsProblemTypes.GIVE_UNKNOWN_TARGET_SPECIAL.type, unit,
            null, Resources.get("order.all.warning.unknowntarget", target), line));
      } else {
        String targetID;
        try {
          targetID =
              unit.getData().getGameSpecificStuff().getOrderChanger().getTokenLocalized(
                  unit.getLocale(), target);
        } catch (RulesException e) {
          targetID = EresseaConstants.O_TEMP + target;
        }
        setProblem(ProblemFactory.createProblem(Severity.WARNING,
            OrderSyntaxInspector.OrderSemanticsProblemTypes.GIVE_UNKNOWN_TARGET.type, unit, null,
            Resources.get("order.all.warning.unknowntarget", targetID), line));
      }
    } else {
      zeroOrTarget = tUnit;
    }

    if (tUnit == null || tUnit.getRegion() == unit.getRegion()) {
      EresseaExecutionState eState = (EresseaExecutionState) state;

      if (!unit.equals(tUnit)) {
        if (type == EresseaConstants.OC_CONTROL) {
          if (target.intValue() == 0) {
            setError(unit, line, Resources.get("order.all.warning.zeronotallowed"));
          } else {
            UnitRelation rel = new ControlRelation(unit, zeroOrTarget, line);
            UnitContainer uc = unit.getModifiedUnitContainer();
            if (uc == null || uc.getModifiedOwnerUnit() != unit) {
              // better warn too often than too rarely
              rel.setWarning(Resources.get("order.give.warning.nocommand"),
                  OrderSyntaxInspector.OrderSemanticsProblemTypes.GIVE_WARNING.type);
            } else if (zeroOrTarget.getModifiedUnitContainer() == uc) {
              uc.setModifiedOwnerUnit(zeroOrTarget);
            } else {
              setError(unit, line, Resources.get("order.give.warning.unitnotin", unit, uc));
            }
            rel.add();
          }
        } else if (type == EresseaConstants.OC_UNIT) {
          if (target.intValue() == 0) {
            setWarning(unit, line, Resources.get("order.all.warning.zeronotallowed"));
          } else {
            UnitRelation rel = new UnitTransferRelation(unit, zeroOrTarget, unit.getRace(), line);
            rel.add();
          }
        } else if (type == EresseaConstants.OC_MEN) {
          PersonTransferRelation rel =
              new PersonTransferRelation(unit, zeroOrTarget, -1, unit.getRace(), line);

          if (all) {
            rel.amount = unit.getModifiedPersons();
          } else {
            // if not, only transfer the minimum amount the unit has
            rel.amount = Math.min(unit.getModifiedPersons(), amount);
          }

          rel.add();
        } else if (type == EresseaConstants.OC_SHIP) {

          Ship ship = unit.getModifiedShip(), targetShip = zeroOrTarget.getModifiedShip();
          if (ship == null || ship.getModifiedOwnerUnit() != unit) {
            setWarning(unit, line, Resources.get("order.give.warning.ship.noship", unit));
          } else {
            if (ship.getModifiedAmount() < amount) {
              setWarning(unit, line, Resources.get("order.give.warning.ship.toomany", unit));
              amount = ship.getModifiedAmount();
            }
            if (amount <= 0) {
              setWarning(unit, line, Resources.get("order.give.warning.ship.amount0"));
            } else {
              if (ship.getEffects() != null && !ship.getEffects().isEmpty()) {
                setWarning(unit, line, Resources.get("order.give.warning.ship.spell", ship));
              }
              if (targetShip != null && targetShip.getEffects() != null && !targetShip.getEffects()
                  .isEmpty()) {
                setWarning(unit, line, Resources.get("order.give.warning.ship.spell", ship));
              }
              if (ship.getShipType().getRange() < 3) {
                setWarning(unit, line, Resources.get("order.give.warning.ship.boat", ship));
              }

              if (zeroOrTarget == unit.getRegion().getZeroUnit()) {
                targetShip = ship.createTempShip();
                if (amount == ship.getModifiedAmount() && !unit.getRegion().getRegionType()
                    .isLand()) {
                  setWarning(unit, line, Resources.get("order.give.warning.ship.lastship"));
                }
              } else if (!zeroOrTarget.getFaction().equals(unit
                  .getFaction())) {
                setError(unit, line, Resources.get("order.give.warning.invalidunit", target));
              } else if (zeroOrTarget.getModifiedShip() == null) {
                // target enters new ship
                targetShip = ship.createTempShip();
                eState.enter(zeroOrTarget, targetShip);
                // EnterRelation enter = new EnterRelation(zeroOrTarget, targetShip, line);
                // enter.add();
              } else if (targetShip == ship) {
                // unit on our ship
                targetShip = ship.createTempShip();
                eState.enter(zeroOrTarget, targetShip);

                EnterRelation enter = new EnterRelation(zeroOrTarget, targetShip, line);
                enter.add();
              } else if (targetShip == null || targetShip.getModifiedOwnerUnit() != zeroOrTarget) {
                setError(unit, line, Resources.get("order.give.warning.ship.noship", zeroOrTarget));
                targetShip = null;
              }

              if (targetShip != null) {
                if (!targetShip.getShipType().equals(ship.getShipType())) {
                  setError(unit, line, Resources.get("order.give.warning.shiptype", targetShip));
                } else {
                  boolean shoreWarning = false;
                  if (ship.getShoreId() != targetShip.getShoreId() && ship.getShoreId() >= 0) {
                    shoreWarning = true;
                    setWarning(unit, line, Resources.get("order.give.warning.ship.wrongshore",
                        getTranslation(ship.getShoreId(), data),
                        getTranslation(targetShip.getShoreId(), data)));
                  }
                  if (!shoreWarning || targetShip.getShoreId() >= 0) {

                    ShipTransferRelation rel =
                        new ShipTransferRelation(unit, zeroOrTarget, -1, ship, targetShip, line);
                    rel.amount = Math.min(ship.getModifiedAmount(), amount);
                    rel.add();
                    InterUnitRelation iur = new InterUnitRelation(unit, zeroOrTarget, line);
                    iur.add();
                    if (ship.getModifiedAmount() <= 0 && zeroOrTarget != unit.getRegion()
                        .getZeroUnit()) {
                      // last ship, transfer units
                      ArrayList<Unit> left = new ArrayList<Unit>(ship.modifiedUnits());
                      for (Unit u : left) {
                        eState.leave(u, ship);
                        eState.enter(u, targetShip);
                        LeaveRelation leave = new LeaveRelation(u, ship, line, true);
                        EnterRelation enter = new EnterRelation(u, targetShip, line);
                        leave.add();
                        enter.add();
                      }
                      left.clear();
                    }
                  }
                }
              }
            }
          }
        } else if (type == EresseaConstants.OC_HERBS) {
          // create relations for all herbs the unit carries
          ItemCategory herbCategory = data.getRules().getItemCategory(StringID.create(("HERBS")));

          if ((herbCategory != null)) {
            for (ItemType i : eState.getHerbTypes()) {
              if (unit.getItem(i) != null || unit.getModifiedItem(i) != null) {
                // List<UnitRelation> relations = eState.giveItem(unit, tUnit, true, 0, i, line,
                // this);
                // for (UnitRelation rel : relations) {
                // rel.add();
                // }
                List<UnitRelation> relations =
                    eState.acquireItem(unit, i, 0, true, false, false, line, this);
                for (UnitRelation rel : relations) {
                  if (rel instanceof ReserveRelation) {
                    UnitRelation ownRelation =
                        new ItemTransferRelation(unit, zeroOrTarget,
                            ((ReserveRelation) rel).amount, i, line, false);
                    if (!(zeroOrTarget instanceof ZeroUnit)) {
                      ReserveRelation targetRelation =
                          new ReserveRelation(unit, zeroOrTarget, ((ReserveRelation) rel).amount,
                              i, line);
                      targetRelation.add();
                    }
                    ownRelation.add();
                  } else {
                    rel.add();
                  }
                }
              }
            }
          }

        } else if (type == EresseaConstants.OC_GIVE) {
          if (itemType != null) {
            if (EresseaConstants.I_UPEASANT.equals(itemType.getID())) {
              setWarning(unit, line, Resources.get("order.give.warning.invaliditem", itemType));
            }

            int requiredAmount =
                all ? 0 : (each ? zeroOrTarget.getModifiedPersons() * amount : amount);
            List<UnitRelation> relations =
                eState.acquireItem(unit, itemType, requiredAmount, all, false, false, line, this);
            for (UnitRelation rel : relations) {
              if (rel instanceof ReserveRelation) {
                UnitRelation ownRelation =
                    new ItemTransferRelation(unit, zeroOrTarget, ((ReserveRelation) rel).amount,
                        itemType, line, false);
                if (((ReserveRelation) rel).problem != null) {
                  ownRelation.setWarning(Resources.get("order.give.warning.insufficient", itemType
                      .toString()),
                      OrderSyntaxInspector.OrderSemanticsProblemTypes.GIVE_WARNING.type);
                }
                ownRelation.add();
                if (!(zeroOrTarget instanceof ZeroUnit)) {
                  ReserveRelation targetRelation =
                      new ReserveRelation(unit, zeroOrTarget, ((ReserveRelation) rel).amount,
                          itemType, line);
                  targetRelation.add();
                }
              } else {
                rel.add();
              }
            }
          } else {
            // in this case the order looks like:
            // GIVE <unit id> ALLES<EOC>
            if (all) {
              for (Item i : unit.getModifiedItems()) {
                // List<UnitRelation> relations =
                // eState.giveItem(unit, tUnit, all, 0, i.getItemType(), line, this);
                // for (UnitRelation rel : relations) {
                // rel.add();
                // }
                List<UnitRelation> relations =
                    eState.acquireItem(unit, i.getItemType(), 0, all, false, false, line, this);
                for (UnitRelation rel : relations) {
                  if (rel instanceof ReserveRelation) {
                    UnitRelation ownRelation =
                        new ItemTransferRelation(unit, zeroOrTarget,
                            ((ReserveRelation) rel).amount, i.getItemType(), line, false);
                    ownRelation.add();
                    if (!(zeroOrTarget instanceof ZeroUnit)) {
                      ReserveRelation targetRelation =
                          new ReserveRelation(unit, zeroOrTarget, ((ReserveRelation) rel).amount, i
                              .getItemType(), line);
                      targetRelation.add();
                    }

                  } else {
                    rel.add();
                  }
                }
              }
            }
          }
        } else {
          setWarning(unit, line, Resources.get("order.give.warning.unknowntype"));
        }
      } else {
        setWarning(unit, line, Resources.get("order.give.warning.reflexive"));
      }
    } else {
      setWarning(unit, line, Resources.get("order.give.warning.invalidunit", target));
    }
  }

  private String getTranslation(int shore, GameData data) {
    return data.getGameSpecificStuff().getOrderChanger().getOrderO(
        Locales.getOrderLocale(), data.getMapMetric().toDirection(shore).getId())
        .getText();
  }

  /**
   * @see magellan.library.gamebinding.SimpleOrder#setWarning(magellan.library.Unit, int,
   *      java.lang.String)
   */
  @Override
  protected void setWarning(Unit unit, int line, String string) {
    setProblem(ProblemFactory
        .createProblem(Severity.WARNING,
            OrderSyntaxInspector.OrderSemanticsProblemTypes.GIVE_WARNING.type, unit, null, string,
            line));
  }

  /**
   * @see magellan.library.gamebinding.SimpleOrder#setError(magellan.library.Unit, int,
   *      java.lang.String)
   */
  @Override
  protected void setError(Unit unit, int line, String string) {
    setProblem(ProblemFactory.createProblem(Severity.ERROR,
        OrderSyntaxInspector.OrderSemanticsProblemTypes.GIVE_ERROR.type, unit, null, string, line));
  }

}
