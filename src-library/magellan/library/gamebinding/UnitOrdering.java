// class magellan.library.gamebinding.UnitOrdering
// created on Jan 28, 2020
//
// Copyright 2003-2020 by magellan project team
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import magellan.library.Order;
import magellan.library.Unit;

/**
 * Holds a collection of orders, returns them in order of order priorities and unit ordering and
 * handles re-orderings due to change of unit ordering.
 *
 * @author stm
 * @version 1.0, Jan 29, 2020
 */
public class UnitOrdering {

  protected static final class OrderInfo implements Comparable<OrderInfo> {
    public Order order;
    public UnitInfo unit;
    public int line;
    public int priority;

    public OrderInfo(Order order, int priority, UnitInfo unit, int line) {
      this.order = order;
      this.priority = priority;
      this.unit = unit;
      this.line = line;
    }

    @Override
    public String toString() {
      return order.toString();
    }

    public int compareTo(OrderInfo o) {
      return priority - o.priority;
    }
  }

  protected static final class UnitInfo {
    public UnitInfo(Unit unit, int order) {
      this.unit = unit;
      ordering = order;
    }

    @Override
    public String toString() {
      return unit + ": " + ordering;
    }

    public Unit unit;
    public int ordering;
  }

  private OrderInfo[] orders;
  private Map<Unit, UnitInfo> unitMap;
  private UnitInfo[] unitArray;

  private int filled = 0;
  private int executed = 0;

  private Comparator<OrderInfo> orderComparator = new Comparator<OrderInfo>() {

    public int compare(OrderInfo o1, OrderInfo o2) {
      int comp = Integer.compare(o1.priority, o2.priority);
      if (comp != 0)
        return comp;
      return Integer.compare(o1.unit.ordering, o2.unit.ordering);
    }
  };

  /**
   * Reserves space for count orders and deletes all orders and units.
   *
   * @param count
   */
  public void reset(int count) {
    orders = new OrderInfo[count];
    unitMap = new HashMap<Unit, UnitOrdering.UnitInfo>(count);
    unitArray = new UnitInfo[count];
    filled = 0;
    executed = 0;
  }

  /**
   * Adds a new order. The returned ordering (without calling sort first) is according to insertion
   * order.
   *
   * @param o
   * @param priority
   * @param u
   * @param line
   */
  public void add(Order o, int priority, Unit u, int line) {
    if (filled >= orders.length)
      throw new IndexOutOfBoundsException(filled);

    UnitInfo uInfo = unitMap.get(u);
    if (uInfo == null) {
      uInfo = new UnitInfo(u, 0);
      unitArray[unitMap.size()] = uInfo;
      unitMap.put(u, uInfo);
    }

    orders[filled++] = new OrderInfo(o, priority, uInfo, line);
  }

  /**
   * Returns true if not all orders have been consumed.
   */
  public boolean hasNext() {
    return executed < filled;
  }

  /**
   * Advances to the next order.
   */
  public void consume() {
    executed++;
  }

  /**
   * Resets to the first order.
   */
  public void reset() {
    executed = 0;
  }

  /**
   * Returns priority of current order.
   */
  public int getPriority() {
    return orders[executed].priority;
  }

  /**
   * Returns current order.
   */
  public Order getOrder() {
    return orders[executed].order;
  }

  /**
   * Returns unit of current order.
   */
  public Unit getUnit() {
    return orders[executed].unit.unit;
  }

  /**
   * Returns line of current order.
   */
  public int getLine() {
    return orders[executed].line;
  }

  /**
   * Simulates server behavior, of unit re-ordering after unit order changes, namely for ENTER
   * orders: Re-sorts the orders with priority greater than the current priority according to the
   * unit order after removing unit and re-inserting unit behind afterUnit.
   *
   * @param unit
   * @param afterUnit
   */
  public void insert(Unit unit, Unit afterUnit) {
    if (!unitMap.containsKey(unit) || !unitMap.containsKey(afterUnit))
      throw new RuntimeException();
    UnitInfo insertedValue = null;
    for (int i = 0; i < unitMap.size(); ++i) {
      if (unitArray[i].unit == unit) {
        insertedValue = unitArray[i];
      }
      if (i + 1 >= unitMap.size())
        throw new RuntimeException();
      if (insertedValue != null) {
        unitArray[i] = unitArray[i + 1];
        unitArray[i].ordering = i;
      }
      if (unitArray[i].unit == afterUnit) {
        if (insertedValue != null) {
          unitArray[i + 1] = insertedValue;
          unitArray[i + 1].ordering = i + 1;
        }
        break;
      }
    }
    int shiftBegin = executed + 1;
    for (; shiftBegin < orders.length; ++shiftBegin) {
      if (orders[shiftBegin].priority != orders[executed].priority) {
        break;
      }
    }
    if (shiftBegin < orders.length) {
      Arrays.sort(orders, shiftBegin, filled, orderComparator);
    }
  }

  /**
   * Sorts the orders according to order priority first, order of units (as returned by units2's
   * iterator) second.
   *
   * @param units2
   */
  public void sort(Collection<Unit> units2) {

    int ordering = 0;

    unitArray = Arrays.copyOf(unitArray, units2.size());

    for (Unit u : units2) {
      UnitInfo uInfo = unitMap.get(u);
      if (uInfo == null) {
        uInfo = new UnitInfo(u, ordering++);
        unitArray[unitMap.size()] = uInfo;
        unitMap.put(u, uInfo);
      } else {
        uInfo.ordering = ordering++;
      }
    }

    Arrays.sort(unitArray, 0, unitMap.size(), new Comparator<UnitInfo>() {

      public int compare(UnitInfo u1, UnitInfo u2) {
        return Integer.compare(u1.ordering, u2.ordering);
      }
    });
    Arrays.sort(orders, 0, filled, orderComparator);
  }

}
