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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.completion.OrderParser;
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
  public static final OrderSyntaxInspector INSPECTOR = new OrderSyntaxInspector();

  enum OrderSyntaxProblemTypes {
    NO_ORDERS, PARSE_ERROR, PARSE_WARNING;

    private ProblemType type;

    OrderSyntaxProblemTypes() {
      String name = this.name().toLowerCase();
      String message = Resources.get("tasks.ordersyntaxinspector." + name + ".message");
      String typeName = Resources.get("tasks.ordersyntaxinspector." + name + ".name", false);
      if (typeName == null)
        typeName = message;
      String description =
          Resources.get("tasks.ordersyntaxinspector." + name + ".description", false);
      type = new ProblemType(typeName, description, message, getInstance());
    }

    ProblemType getType() {
      return type;
    }
  }

  private Collection<ProblemType> types;

  protected OrderSyntaxInspector() {
  }

  /**
   * Returns an instance of OrderSyntaxInspector.
   * 
   * @return The singleton instance of OrderSyntaxInspector
   */
  public static OrderSyntaxInspector getInstance() {
    return OrderSyntaxInspector.INSPECTOR;
  }

  /**
   * @see magellan.library.tasks.AbstractInspector#reviewUnit(magellan.library.Unit, int)
   */
  @Override
  public List<Problem> reviewUnit(Unit unit, Severity severity) {

    Collection<String> orders = unit.getOrders();
    List<Problem> errors = new ArrayList<Problem>();

    if ((Utils.isEmpty(orders) || orders.size() == 0) && severity == Severity.ERROR) {
      // no orders...that could be a problem.
      if (!magellan.library.utils.Units.isPrivilegedAndNoSpy(unit)) {
        // okay, that isn't our unit... forget it
        return Collections.emptyList();
      } else {
        errors.add(new AbstractProblem(Severity.ERROR, OrderSyntaxProblemTypes.NO_ORDERS.getType(),
            unit));
      }

    }

    // be careful with the order parser. Some orders may be correct but will not get
    // an OK from the parser: ZAUBERE und Benutze Trank ...
    // so I change that from error to warning

    if (severity == Severity.WARNING) {
      GameData data = unit.getRegion().getData();
      OrderParser parser = data.getGameSpecificStuff().getOrderParser(data);

      Integer line = 0;
      for (String order : orders) {
        line++;
        StringReader reader = new StringReader(order);
        boolean ok = parser.read(reader);
        if (!ok) {
          errors.add(new AbstractProblem(Severity.WARNING, OrderSyntaxProblemTypes.PARSE_WARNING
              .getType(), unit, getWarningMessage(order, line), line));
        }
      }
    }
    return errors;
  }

  private String getWarningMessage(String order, Integer line) {
    return Resources.get("tasks.ordersyntaxinspector.parse_warning.message", new Object[] { order,
        line });
  }

  private String getErrorMessage(String order, Integer line) {
    return Resources.get("tasks.ordersyntaxinspector.parse_error.message", new Object[] { order,
        line });
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
