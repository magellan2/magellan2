// class magellan.library.tasks.OrderSyntaxInspector
// created on 02.03.2008
//
// Copyright 2003-2008 by magellan project team
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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
package magellan.library.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import magellan.library.GameData;
import magellan.library.Order;
import magellan.library.Orders;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.relation.UnitRelation;
import magellan.library.tasks.Problem.Severity;
import magellan.library.utils.Resources;
import magellan.library.utils.Utils;

/**
 * This inspectors checks all syntax.
 * 
 * @author Thoralf Rickert
 * @version 1.0, 02.03.2008
 */
public class OrderSyntaxInspector extends AbstractInspector {

  public enum OrderSyntaxProblemTypes {
    NO_ORDERS, PARSE_ERROR, PARSE_WARNING, LONGORDERS, NO_LONG_ORDER;

    public ProblemType type;

    OrderSyntaxProblemTypes() {
      String name = name().toLowerCase();
      type = ProblemType.create("tasks.ordersyntaxinspector", name);
    }

    public ProblemType getType() {
      return type;
    }
  }

  public enum OrderSemanticsProblemTypes {
    SEMANTIC_ERROR, SEMANTIC_WARNING, GIVE_ERROR, GIVE_WARNING, GIVE_UNKNOWN_TARGET,
    GIVE_UNKNOWN_TARGET_SPECIAL;

    public ProblemType type;

    OrderSemanticsProblemTypes() {
      String name = name().toLowerCase();
      type = ProblemType.create("tasks.ordersemanticsinspector", name);
    }

    ProblemType getType() {
      return type;
    }
  }

  private Collection<ProblemType> types;

  protected OrderSyntaxInspector(GameData data) {
    super(data);
  }

  @Override
  public void setGameData(GameData gameData) {
    super.setGameData(gameData);
  }

  /**
   * Returns an instance of OrderSyntaxInspector.
   * 
   * @return The singleton instance of OrderSyntaxInspector
   */
  public static OrderSyntaxInspector getInstance(GameData data) {
    return new OrderSyntaxInspector(data);
  }

  @Override
  public List<Problem> listProblems(Region r) {
    List<Problem> errors = new ArrayList<Problem>();
    if (r == getData().getRegions().iterator().next()) {
      for (Problem p : getData().getErrors()) {
        errors.add(p);
      }
    }

    // Unit zero = r.getZeroUnit();
    // if (zero != null) {
    // for (UnitRelation rel : zero.getRelations(UnitRelation.class))
    // if (rel.problem != null && rel.problem.getSeverity() == severity) {
    // if (rel.line > 0) {
    // Order order = rel.origin.getOrders2().get(rel.line - 1);
    // if (order == null || (order.getProblem() == null && order.isValid())) {
    // errors.add(ProblemFactory.createProblem(rel.problem.getSeverity(), rel.problem
    // .getType(), zero.getRegion(), rel.origin, rel.origin.getFaction(), rel.origin,
    // this, rel.problem.getMessage(), rel.line));
    // }
    // }
    // }
    // }

    return errors;
  }

  /**
   * @see AbstractInspector#findProblems(magellan.library.Unit)
   */
  @Override
  public List<Problem> findProblems(Unit unit) {

    Orders orders = unit.getOrders2();
    List<Problem> errors = new ArrayList<Problem>();

    // be careful with the order parser. Some orders may be correct but will not get
    // an OK from the parser: ZAUBERE und Benutze Trank ...
    // so I change that from error to warning

    // OrderParser parser = getParser();

    int line = 0;
    boolean longOrder = false;
    for (Order order : orders) {
      line++;

      if (order.getProblem() != null) {
        Problem problem = order.getProblem();
        String message = problem.getMessage();
        if (message == null) {
          message = getWarningMessage(OrderSyntaxProblemTypes.PARSE_WARNING, order);
        }
        errors.add(ProblemFactory.createProblem(problem.getSeverity(), problem.getType(), unit
            .getRegion(), unit, unit.getFaction(), unit, this, message, line));
        // order.getProblem());
      } else if (!order.isValid()) {
        errors.add(ProblemFactory.createProblem(Severity.WARNING,
            OrderSyntaxProblemTypes.PARSE_WARNING.getType(), unit, this, getWarningMessage(
                OrderSyntaxProblemTypes.PARSE_WARNING, order), line));
      }

      longOrder |= order.isLong() && !orders.isToken(order, 0, EresseaConstants.OC_ATTACK);
    }

    if ((Utils.isEmpty(orders) || orders.size() == 0)) {
      // no orders...that could be a problem.
      if (!magellan.library.utils.Units.isPrivilegedAndNoSpy(unit))
        // okay, that isn't our unit... forget it
        return Collections.emptyList();
      else {
        errors.add(ProblemFactory.createProblem(Severity.ERROR, OrderSyntaxProblemTypes.NO_ORDERS
            .getType(), unit, this));
      }
    } else if (!longOrder) {
      errors.add(ProblemFactory.createProblem(Severity.ERROR, OrderSyntaxProblemTypes.NO_LONG_ORDER
          .getType(), unit, this));
    } else {
      line = getData().getGameSpecificStuff().getOrderChanger().areCompatibleLongOrders(orders);
      if (0 <= line) {
        errors.add(ProblemFactory.createProblem(Severity.ERROR, OrderSyntaxProblemTypes.LONGORDERS
            .getType(), unit, this, getWarningMessage(OrderSyntaxProblemTypes.LONGORDERS, orders
                .get(line)), line + 1));
      }
    }

    for (UnitRelation rel : unit.getRelations(UnitRelation.class))
      if (rel.problem != null && rel.origin == unit) {
        if (rel.line > 0) {
          Order order = rel.origin.getOrders2().get(rel.line - 1);
          if (order == null || (order.getProblem() == null && order.isValid())) {
            errors.add(ProblemFactory.createProblem(rel.problem.getSeverity(), rel.problem
                .getType(), unit.getRegion(), unit, unit.getFaction(), unit, this, rel.problem
                    .getMessage(), rel.line));
          }
        } else {
          errors.add(ProblemFactory.createProblem(rel.problem.getSeverity(), rel.problem
              .getType(), unit.getRegion(), unit, unit.getFaction(), unit, this, rel.problem
                  .getMessage(), rel.line));
        }
      }

    return errors;
  }

  private String getWarningMessage(OrderSyntaxProblemTypes parseWarning, Order order) {
    return Resources.get("tasks.ordersyntaxinspector." + parseWarning.name().toLowerCase()
        + ".message", new Object[] { order });
  }

  public Collection<ProblemType> getTypes() {
    if (types == null) {
      types = new LinkedList<ProblemType>();
      for (OrderSyntaxProblemTypes t : OrderSyntaxProblemTypes.values()) {
        types.add(t.getType());
      }
      for (OrderSemanticsProblemTypes t : OrderSemanticsProblemTypes.values()) {
        types.add(t.getType());
      }
    }
    return types;
  }
}
