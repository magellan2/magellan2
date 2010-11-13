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
import magellan.library.Unit;
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

  /** The singleton instance of the OrderSyntaxInspector */
  // public static final OrderSyntaxInspector INSPECTOR = new OrderSyntaxInspector();

  enum OrderSyntaxProblemTypes {
    NO_ORDERS, PARSE_ERROR, PARSE_WARNING, LONGORDERS, NO_LONG_ORDER;

    private ProblemType type;

    OrderSyntaxProblemTypes() {
      String name = name().toLowerCase();
      String message = Resources.get("tasks.ordersyntaxinspector." + name + ".message");
      String typeName = Resources.get("tasks.ordersyntaxinspector." + name + ".name", false);
      if (typeName == null) {
        typeName = message;
      }
      String description =
          Resources.get("tasks.ordersyntaxinspector." + name + ".description", false);
      String group = Resources.get("tasks.ordersyntaxinspector." + name + ".group", false);
      type = new ProblemType(typeName, group, description, message);
    }

    ProblemType getType() {
      return type;
    }
  }

  private Collection<ProblemType> types;

  protected OrderSyntaxInspector(GameData data) {
    super(data);
    // parser = getGameSpecificStuff().getOrderParser(getData());
  }

  @Override
  public void setGameData(GameData gameData) {
    super.setGameData(gameData);
    // parser = getGameSpecificStuff().getOrderParser(getData());
  }

  /**
   * Returns an instance of OrderSyntaxInspector.
   * 
   * @return The singleton instance of OrderSyntaxInspector
   */
  public static OrderSyntaxInspector getInstance(GameData data) {
    return new OrderSyntaxInspector(data);
  }

  /**
   * @see magellan.library.tasks.AbstractInspector#reviewUnit(magellan.library.Unit, Severity)
   */
  @Override
  public List<Problem> reviewUnit(Unit unit, Severity severity) {

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

      if (severity == Severity.WARNING)
        if (order.getWarning() != null) {
          errors.add(ProblemFactory.createProblem(Severity.WARNING,
              OrderSyntaxProblemTypes.PARSE_WARNING.getType(), unit, this, order.getWarning()
                  + ": " + order.toString(), line));
        } else if (!order.isValid()) {
          errors.add(ProblemFactory.createProblem(Severity.WARNING,
              OrderSyntaxProblemTypes.PARSE_WARNING.getType(), unit, this, getWarningMessage(
                  OrderSyntaxProblemTypes.PARSE_WARNING, order), line));
        }

      longOrder |= order.isLong();
    }

    if (severity == Severity.ERROR) {
      if ((Utils.isEmpty(orders) || orders.size() == 0)) {
        // no orders...that could be a problem.
        if (!magellan.library.utils.Units.isPrivilegedAndNoSpy(unit))
          // okay, that isn't our unit... forget it
          return Collections.emptyList();
        else {
          errors.add(ProblemFactory.createProblem(Severity.ERROR, OrderSyntaxProblemTypes.NO_ORDERS
              .getType(), unit, this));
          // }
        }
      } else if (!longOrder) {
        errors.add(ProblemFactory.createProblem(Severity.ERROR,
            OrderSyntaxProblemTypes.NO_LONG_ORDER.getType(), unit, this));
      } else {
        line = getData().getGameSpecificStuff().getOrderChanger().areCompatibleLongOrders(orders);
        if (0 <= line) {
          errors.add(ProblemFactory.createProblem(Severity.ERROR,
              OrderSyntaxProblemTypes.LONGORDERS.getType(), unit, this, getWarningMessage(
                  OrderSyntaxProblemTypes.LONGORDERS, orders.get(line)), line + 1));
        }
      }
    }

    if (severity == Severity.WARNING) {
      for (UnitRelation rel : unit.getRelations(UnitRelation.class))
        if (rel.warning) {
          if (rel.line <= 0) {
            Order order = rel.origin.getOrders2().get(rel.line - 1);
            if (order == null || (order.getWarning() == null && order.isValid())) {
              errors.add(ProblemFactory.createProblem(Severity.WARNING,
                  OrderSyntaxProblemTypes.PARSE_WARNING.getType(), rel.origin, this, rel.getClass()
                      .toString(), rel.line));
            }
          }
        }
    }

    return errors;
  }

  // private OrderParser getParser() {
  // return parser;
  // }

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
    }
    return types;
  }
}
