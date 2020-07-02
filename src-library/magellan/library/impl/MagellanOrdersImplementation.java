package magellan.library.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import magellan.library.Order;
import magellan.library.Orders;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.OrderType;
import magellan.library.utils.Locales;
import magellan.library.utils.OrderToken;

/**
 * A class for handling orders in the Unit object.
 */
public class MagellanOrdersImplementation implements Orders {
  private List<Order> orders = null;

  private Unit unit;

  /**
   * Creates a new Orders object.
   */
  public MagellanOrdersImplementation(Unit u) {
    unit = u;
    orders = new ArrayList<Order>();
  }

  /**
   * Creates a new Orders object. The orders <strong>are backed by <code>list</code></strong>, i.e.
   * changes in the list also change this orders object.
   *
   * @param list
   */
  public MagellanOrdersImplementation(Unit u, List<Order> list) {
    unit = u;
    orders = list;
  }

  /**
   * @see magellan.library.Orders#removeOrder(magellan.library.Order, int)
   */
  public boolean removeOrder(Order order, int length) {
    if (order.isEmpty())
      return false;

    // parse order until there are enough match tokens
    int tokenCounter = 0;
    final Collection<OrderToken> matchTokens = new LinkedList<OrderToken>();
    Iterator<OrderToken> ct = order.getTokens().iterator();
    OrderToken t = ct.next();

    while ((t.ttype != OrderToken.TT_EOC) && (tokenCounter++ < length)) {
      matchTokens.add(t);
      t = ct.next();
    }

    // order does not contain enough match tokens, abort
    if (matchTokens.size() < length)
      return false;

    boolean result = false;

    // if replace, delete matching orders first
    boolean tempBlock = false;

    // cycle through this unit's orders
    for (final ListIterator<Order> cmds = listIterator(); cmds.hasNext();) {
      final Order cmd = cmds.next();
      ct = cmd.getTokens().iterator();
      t = ct.next();

      // skip empty orders and comments
      if ((OrderToken.TT_EOC == t.ttype) || (OrderToken.TT_COMMENT == t.ttype)) {
        continue;
      }

      // FIXME game specific!
      if (false == tempBlock) {
        if (equalsToken(t, EresseaConstants.OC_MAKE)) {
          t = ct.next();

          if (OrderToken.TT_EOC == t.ttype) {
            continue;
          } else if (equalsToken(t, EresseaConstants.OC_TEMP)) {
            tempBlock = true;

            continue;
          }
        } else {
          // compare the current unit order and tokens of the one to add
          boolean removeOrder = true;

          for (final Iterator<OrderToken> iter = matchTokens.iterator(); iter.hasNext()
              && (t.ttype != OrderToken.TT_EOC);) {
            final OrderToken matchToken = iter.next();

            if (!(t.equalsToken(matchToken.getText()) || matchToken.equalsToken(t.getText()))) {
              removeOrder = false;

              break;
            }

            t = ct.next();
          }

          if (removeOrder) {
            cmds.remove();
            result = true;
          }

          continue;
        }
      } else {
        if (equalsToken(t, EresseaConstants.OC_END)) {
          tempBlock = false;

          continue;
        }
      }
    }

    return result;

  }

  protected boolean equalsToken(OrderToken token, StringID order) {
    List<String> translations = getOrderTranslations(order);
    for (String translation : translations)
      if (token.equalsToken(translation))
        return true;
    return false;
  }

  protected boolean equalsCompletedToken(OrderToken token, StringID order) {
    List<String> translations = getOrderTranslations(order);
    for (String translation : translations)
      if (token.equalsCompletedToken(translation))
        return true;
    return false;
  }

  protected List<String> getOrderTranslations(StringID orderId) {
    OrderType order = unit.getData().getRules().getOrder(orderId);
    if (order != null) {
      List<String> names =
          order.getNames(unit.getLocale() != null ? unit.getLocale() : Locales.getOrderLocale());
      if (names != null)
        return names;
    }
    return Collections.singletonList(orderId.toString());

    // return unit.getData().getGameSpecificStuff().getOrderChanger().getOrder(unit.getLocale(),
    // orderId).toString();
  }

  /**
   * Returns an unmodifiable copy of this object.
   */
  public Orders getView() {
    return new MagellanOrdersImplementation(unit, Collections.unmodifiableList(orders));
  }

  /**
   * @see magellan.library.Orders#getUnit()
   */
  public Unit getUnit() {
    return unit;
  }

  // public int getBase() {
  // return unit.getData().base;
  // }

  // public EntityID getEntityID(Order order, int pos) {
  // return EntityID.createEntityID(order.getToken(pos).getText(), getBase());
  // }
  //
  // public UnitID getUnitID(Order order, int pos) {
  // return UnitID.createUnitID(order.getToken(pos).getText(), getBase());
  // }

  /**
   * @see magellan.library.Orders#getNumber(Order, int)
   */
  public int getNumber(Order order, int pos) {
    if (order.getToken(pos).ttype == OrderToken.TT_NUMBER)
      return Integer.parseInt(order.getToken(pos).getText());
    else
      throw new NumberFormatException("not a number token");
  }

  /**
   * @see magellan.library.Orders#isToken(magellan.library.Order, int, StringID)
   */
  public boolean isToken(Order order, int i, StringID token) {
    return equalsCompletedToken(order.getToken(i), token);
  }

  /**
   * @return true if there are no orders
   * @see java.util.List#isEmpty()
   */
  public boolean isEmpty() {
    return orders.isEmpty();
  }

  /**
   * @see java.util.List#contains(java.lang.Object)
   */
  public boolean contains(Object o) {
    return orders.contains(o);
  }

  /**
   * @see java.util.List#iterator()
   */
  public Iterator<Order> iterator() {
    return new OrdersIterator(orders.listIterator());
  }

  /**
   * @see java.util.List#toArray()
   */
  public Object[] toArray() {
    return orders.toArray();
  }

  /**
   * @see java.util.List#toArray(Object[])
   */
  public <T> T[] toArray(T[] a) {
    return orders.toArray(a);
  }

  /**
   * @see java.util.List#add(java.lang.Object)
   */
  public boolean add(Order e) {
    if (orders.add(e))
      return true;
    else
      return false;
  }

  /**
   * @see java.util.List#remove(java.lang.Object)
   */
  public boolean remove(Object o) {
    if (orders.remove(o))
      return true;
    else
      return false;
  }

  /**
   * @param c
   * @see java.util.List#containsAll(java.util.Collection)
   */
  public boolean containsAll(Collection<?> c) {
    return orders.containsAll(c);
  }

  /**
   * @param c
   * @see java.util.List#addAll(java.util.Collection)
   */
  public boolean addAll(Collection<? extends Order> c) {
    if (orders.addAll(c))
      return true;
    else
      return false;
  }

  /**
   * @param index
   * @param c
   * @see java.util.List#addAll(int, java.util.Collection)
   */
  public boolean addAll(int index, Collection<? extends Order> c) {
    if (orders.addAll(index, c))
      return true;
    else
      return false;
  }

  /**
   * @param c
   * @see java.util.List#removeAll(java.util.Collection)
   */
  public boolean removeAll(Collection<?> c) {
    if (orders.removeAll(c))
      return true;
    else
      return false;
  }

  /**
   * @param c
   * @see java.util.List#retainAll(java.util.Collection)
   */
  public boolean retainAll(Collection<?> c) {
    if (orders.retainAll(c))
      return true;
    else
      return false;
  }

  /**
   * @see java.util.List#clear()
   */
  public void clear() {
    orders.clear();
  }

  /**
   * @param o
   * @see java.util.List#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {

    return (o instanceof Orders) && unit.equals(((Orders) o).getUnit()) && orders.equals(o);
  }

  /**
   * @see java.util.List#hashCode()
   */
  @Override
  public int hashCode() {
    return orders.hashCode() * 31 + unit.hashCode();
  }

  /**
   * @param index
   * @see java.util.List#get(int)
   */
  public Order get(int index) {
    return orders.get(index);
  }

  /**
   * @param index
   * @param element
   * @see java.util.List#set(int, java.lang.Object)
   */
  public Order set(int index, Order element) {
    return orders.set(index, element);
  }

  /**
   * @param index
   * @param element
   * @see java.util.List#add(int, java.lang.Object)
   */
  public void add(int index, Order element) {
    orders.add(index, element);
  }

  /**
   * @param index
   * @see java.util.List#remove(int)
   */
  public Order remove(int index) {
    return orders.remove(index);
  }

  /**
   * @param o
   * @see java.util.List#indexOf(java.lang.Object)
   */
  public int indexOf(Object o) {
    return orders.indexOf(o);
  }

  /**
   * @param o
   * @see java.util.List#lastIndexOf(java.lang.Object)
   */
  public int lastIndexOf(Object o) {
    return orders.lastIndexOf(o);
  }

  /**
   * @see java.util.List#listIterator()
   */
  public ListIterator<Order> listIterator() {
    return new OrdersIterator(orders.listIterator());
  }

  /**
   * @param index
   * @see java.util.List#listIterator(int)
   */
  public ListIterator<Order> listIterator(int index) {
    return new OrdersIterator(orders.listIterator(index));
  }

  /**
   * @throws UnsupportedOperationException
   */
  public List<Order> subList(int fromIndex, int toIndex) {
    throw new UnsupportedOperationException();
  }

  /**
   * @see java.util.List#size()
   */
  public int size() {
    return orders.size();
  }

  /**
   * A ListIterator for Orders.
   */
  public final class OrdersIterator implements ListIterator<Order> {

    private ListIterator<Order> delegate;

    public OrdersIterator(ListIterator<Order> iterator) {
      delegate = iterator;
    }

    /**
     * @see java.util.ListIterator#hasNext()
     */
    public boolean hasNext() {
      return delegate.hasNext();
    }

    /**
     * @see java.util.ListIterator#next()
     */
    public Order next() {
      return delegate.next();
    }

    /**
     * @see java.util.ListIterator#hasPrevious()
     */
    public boolean hasPrevious() {
      return delegate.hasPrevious();
    }

    /**
     * @see java.util.ListIterator#previous()
     */
    public Order previous() {
      return delegate.previous();
    }

    /**
     * @see java.util.ListIterator#nextIndex()
     */
    public int nextIndex() {
      return delegate.nextIndex();
    }

    /**
     * @see java.util.ListIterator#previousIndex()
     */
    public int previousIndex() {
      return delegate.previousIndex();
    }

    /**
     * @see java.util.ListIterator#remove()
     */
    public void remove() {
      delegate.remove();
    }

    /**
     * @param e
     * @see java.util.ListIterator#set(java.lang.Object)
     */
    public void set(Order e) {
      delegate.set(e);
    }

    /**
     * @param e
     * @see java.util.ListIterator#add(java.lang.Object)
     */
    public void add(Order e) {
      delegate.add(e);
    }

  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("@Unit " + unit + ":\n");
    for (Order order : orders) {
      result.append(order.toString()).append("\n");
    }
    return result.toString();
  }

}