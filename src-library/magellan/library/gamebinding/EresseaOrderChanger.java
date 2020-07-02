/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.gamebinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import magellan.library.Building;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Order;
import magellan.library.Orders;
import magellan.library.Region;
import magellan.library.Rules;
import magellan.library.Ship;
import magellan.library.StringID;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.UnitID;
import magellan.library.completion.OrderParser;
import magellan.library.impl.MagellanUnitImpl;
import magellan.library.rules.ItemType;
import magellan.library.rules.Race;
import magellan.library.utils.Locales;
import magellan.library.utils.MagellanFactory;
import magellan.library.utils.logging.Logger;

/**
 * OrderChanger class for the game Eressea.
 */
public class EresseaOrderChanger implements OrderChanger {
  private static final Logger log = Logger.getInstance(EresseaOrderChanger.class);

  protected static final String PCOMMENTSTART = EresseaConstants.O_PCOMMENT + " ";
  protected static final String COMMENTSTART = EresseaConstants.O_COMMENT + " ";

  private Rules rules;

  private OrderTranslationHelper helper;

  protected EresseaOrderChanger(Rules rules, SimpleOrderFactory factory) {
    this.rules = rules;
    helper = new OrderTranslationHelper(rules, factory, EresseaConstants.OC_TEMP);
  }

  protected Rules getRules() {
    return rules;
  }

  /**
   * @see magellan.library.gamebinding.OrderChanger#addNamingOrder(magellan.library.Unit,
   *      java.lang.String)
   */
  public void addNamingOrder(Unit unit, String name) {
    String order = createNamingOrder(name, unit);
    unit.addOrder(order, true, 2);
  }

  protected String createNamingOrder(String name, Unit unit) {
    return getOrderTranslation(EresseaConstants.OC_NAME, unit) + " "
        + getOrderTranslation(EresseaConstants.OC_UNIT, unit) + " \"" + name + "\"";
  }

  /**
   * @see magellan.library.gamebinding.OrderChanger#addNamingOrder(magellan.library.Unit,
   *      magellan.library.UnitContainer, java.lang.String)
   */
  public void addNamingOrder(Unit unit, UnitContainer uc, String name) {
    String order = createNamingOrder(uc, name, unit);
    unit.addOrder(order, true, 2);
  }

  protected String createNamingOrder(UnitContainer uc, String name, Unit unit) {
    Object order = null;

    if (uc instanceof Building) {
      order = EresseaConstants.OC_CASTLE;
    } else if (uc instanceof Ship) {
      order = EresseaConstants.OC_SHIP;
    } else if (uc instanceof Region) {
      order = EresseaConstants.OC_REGION;
    } else if (uc instanceof Faction) {
      order = EresseaConstants.OC_PARAMETER_FACTION;
    }

    return getOrderO(
        unit.getLocale(),
        EresseaConstants.OC_NAME,
        order == null ? new Object[] { ("\"" + name + "\"") } : new Object[] { order,
            ("\"" + name + "\"") }).toString();
  }

  /**
   * @see magellan.library.gamebinding.OrderChanger#addDescribeUnitContainerOrder(magellan.library.Unit,
   *      magellan.library.UnitContainer, java.lang.String)
   */
  public void addDescribeUnitContainerOrder(Unit unit, UnitContainer uc, String descr) {
    String order = createDescribeUnitContainerOrder(uc, unit, descr);
    unit.addOrder(order, true, 2); // FIXME should be 1 for BANNER
  }

  protected String
      createDescribeUnitContainerOrder(UnitContainer uc, Unit unit, String description) {
    StringBuilder order = new StringBuilder();

    if (uc instanceof Building) {
      order.append(getOrderTranslation(EresseaConstants.OC_DESCRIBE, unit)).append(" ").append(
          getOrderTranslation(EresseaConstants.OC_CASTLE, unit));
    } else if (uc instanceof Ship) {
      order.append(getOrderTranslation(EresseaConstants.OC_DESCRIBE, unit)).append(" ").append(
          getOrderTranslation(EresseaConstants.OC_SHIP, unit));
    } else if (uc instanceof Region) {
      order.append(getOrderTranslation(EresseaConstants.OC_DESCRIBE, unit)).append(" ").append(
          getOrderTranslation(EresseaConstants.OC_REGION, unit));
    } else if (uc instanceof Faction) {
      order.append(getOrderTranslation(EresseaConstants.OC_BANNER, unit));
    } else
      return null;

    order.append(" \"").append(description).append("\"");

    return order.toString();
  }

  /**
   * @see magellan.library.gamebinding.OrderChanger#addDescribeUnitPrivateOrder(magellan.library.Unit,
   *      java.lang.String)
   */
  public void addDescribeUnitPrivateOrder(Unit unit, String descr) {
    String order = createDescribeUnitPrivateOrder(descr, unit);
    unit.addOrder(order, true, 2);
  }

  protected String createDescribeUnitPrivateOrder(String descr, Unit unit) {
    return getOrderTranslation(EresseaConstants.OC_DESCRIBE, unit) + " "
        + getOrderTranslation(EresseaConstants.OC_PRIVATE, unit) + " \"" + descr + "\"";
  }

  /**
   * @see magellan.library.gamebinding.OrderChanger#addDescribeUnitOrder(magellan.library.Unit,
   *      java.lang.String)
   */
  public void addDescribeUnitOrder(Unit unit, String descr) {
    String order = createDescribeUnitOrder(descr, unit);
    unit.addOrder(order, true, 2);
  }

  protected String createDescribeUnitOrder(String descr, Unit unit) {
    return getOrderTranslation(EresseaConstants.OC_DESCRIBE, unit) + " "
        + getOrderTranslation(EresseaConstants.OC_UNIT, unit) + " \"" + descr + "\"";
  }

  /**
   * @see magellan.library.gamebinding.OrderChanger#addHideOrder(magellan.library.Unit,
   *      java.lang.String)
   */
  public void addHideOrder(Unit unit, String level) {
    Orders orders = unit.getOrders2();
    List<Order> ordersCopy = new ArrayList<Order>();
    ordersCopy.addAll(orders);

    // remove hide (but not hide faction/race) order
    for (Iterator<Order> iter = ordersCopy.iterator(); iter.hasNext();) {
      Order order = iter.next();

      if (orders.isToken(order, 0, EresseaConstants.OC_HIDE))
        if (orders.isToken(order, 1, EresseaConstants.OC_PARAMETER_FACTION)) {
          continue;
        } else {
          boolean raceFound = false;

          for (Iterator<Race> it2 = getRules().getRaceIterator(); it2.hasNext();) {
            Race race = it2.next();
            if (order.getToken(1).getText().equals(race.getName())) {
              raceFound = true;
              break;
            }
          }

          if (!raceFound) {
            iter.remove();
          }
        }
    }

    ordersCopy.add(createHideOrder(unit, level));
    unit.setOrders2(ordersCopy);
  }

  protected Order createHideOrder(Unit unit, String level) {
    return createOrder(unit, getOrderTranslation(EresseaConstants.OC_HIDE, unit) + " " + level);
  }

  /**
   * @see magellan.library.gamebinding.OrderChanger#createOrder(magellan.library.Unit,
   *      java.lang.String)
   */
  public Order createOrder(Unit unit, String string) {
    return unit.createOrder(string);
  }

  /**
   * @see magellan.library.gamebinding.OrderChanger#addCombatOrder(magellan.library.Unit, int)
   */
  public void addCombatOrder(Unit unit, int newState) {
    String order = getCombatOrder(unit, newState);
    unit.addOrder(order, true, 1);
  }

  protected String getCombatOrder(Unit unit, int newState) {
    String str = getOrderTranslation(EresseaConstants.OC_COMBAT, unit) + " ";

    switch (newState) {
    case 0:
      str += getOrderTranslation(EresseaConstants.OC_COMBAT_AGGRESSIVE, unit);

      break;

    case 1:
      // KÄMPFE VORNE is deprecated
      str += COMMENTSTART + getOrderTranslation(EresseaConstants.OC_COMBAT_FRONT, unit);

      break;

    case 2:
      str += getOrderTranslation(EresseaConstants.OC_COMBAT_REAR, unit);

      break;

    case 3:
      str += getOrderTranslation(EresseaConstants.OC_COMBAT_DEFENSIVE, unit);

      break;

    case 4:
      str += getOrderTranslation(EresseaConstants.OC_COMBAT_NOT, unit);

      break;

    case 5:
      str += getOrderTranslation(EresseaConstants.OC_COMBAT_FLEE, unit);

      break;

    default:
      break;
    }

    return str;
  }

  /**
   * @see magellan.library.gamebinding.OrderChanger#addRecruitOrder(magellan.library.Unit, int)
   */
  public void addRecruitOrder(Unit unit, int i) {
    String order = getOrderTranslation(EresseaConstants.OC_RECRUIT, unit) + " " + String.valueOf(i);
    unit.addOrder(order);
  }

  /**
   * Adds camouflage orders, for hiding all that could identify the unit and remembering the old
   * values in comments.
   *
   * @param unit The affected unit.
   */
  public void addMultipleHideOrder(Unit unit) {
    List<String> orders = new LinkedList<String>();
    orders.add(getOrderTranslation(EresseaConstants.OC_NUMBER, unit) + " "
        + getOrderTranslation(EresseaConstants.OC_UNIT, unit) + " ");
    orders.add(getOrderTranslation(EresseaConstants.OC_NAME, unit) + " "
        + getOrderTranslation(EresseaConstants.OC_UNIT, unit) + " \"\"");
    orders.add(getOrderTranslation(EresseaConstants.OC_DESCRIBE, unit) + " "
        + getOrderTranslation(EresseaConstants.OC_UNIT, unit) + " \"\"");
    orders.add(getOrderTranslation(EresseaConstants.OC_HIDE, unit) + " "
        + getOrderTranslation(EresseaConstants.OC_PARAMETER_FACTION, unit));

    if (unit.getShip() != null) {
      orders.add(getOrderTranslation(EresseaConstants.OC_NUMBER, unit) + " "
          + getOrderTranslation(EresseaConstants.OC_SHIP, unit));
      orders.add(getOrderTranslation(EresseaConstants.OC_NAME, unit) + " "
          + getOrderTranslation(EresseaConstants.OC_SHIP, unit) + " \"\"");
      orders.add(getOrderTranslation(EresseaConstants.OC_DESCRIBE, unit) + " "
          + getOrderTranslation(EresseaConstants.OC_SHIP, unit) + " \"\"");
    }

    orders.add(PCOMMENTSTART + getOrderTranslation(EresseaConstants.OC_NUMBER, unit) + " "
        + getOrderTranslation(EresseaConstants.OC_UNIT, unit) + " " + unit.getID());
    orders.add(PCOMMENTSTART + getOrderTranslation(EresseaConstants.OC_NAME, unit) + " "
        + getOrderTranslation(EresseaConstants.OC_UNIT, unit) + " \"" + unit.getName() + "\"");

    if (unit.getDescription() != null) {
      orders.add(PCOMMENTSTART + getOrderTranslation(EresseaConstants.OC_DESCRIBE, unit) + " "
          + getOrderTranslation(EresseaConstants.OC_UNIT, unit) + " \"" + unit.getDescription()
          + "\"");
    }

    if (!unit.isHideFaction()) {
      orders.add(PCOMMENTSTART + getOrderTranslation(EresseaConstants.OC_HIDE, unit) + " "
          + getOrderTranslation(EresseaConstants.OC_PARAMETER_FACTION, unit) + " "
          + getOrderTranslation(EresseaConstants.OC_NOT, unit));
    }

    if (unit.getShip() != null) {
      orders.add(PCOMMENTSTART + getOrderTranslation(EresseaConstants.OC_NUMBER, unit) + " "
          + getOrderTranslation(EresseaConstants.OC_SHIP, unit) + " "
          + unit.getShip().getID().toString());
      orders.add(PCOMMENTSTART + getOrderTranslation(EresseaConstants.OC_NAME, unit) + " "
          + getOrderTranslation(EresseaConstants.OC_SHIP, unit) + " \"" + unit.getShip().getName()
          + "\"");

      if (unit.getShip().getDescription() != null) {
        orders.add(PCOMMENTSTART + getOrderTranslation(EresseaConstants.OC_DESCRIBE, unit) + " "
            + getOrderTranslation(EresseaConstants.OC_SHIP, unit) + " \""
            + unit.getShip().getDescription() + "\"");
      }
    }

    unit.addOrders(orders);
  }

  /**
   * @see magellan.library.gamebinding.OrderChanger#disableLongOrders(magellan.library.Unit)
   */
  public void disableLongOrders(Unit u) {
    int i = 0;
    for (Order order : u.getOrders2()) {
      for (StringID longOrder : getLongOrderTokens()) {
        if (u.getOrders2().isToken(order, 0, longOrder)) {
          u.replaceOrder(i, createOrder(u, "; " + order.getText()), false);
        }
      }
      ++i;
    }
  }

  public boolean isLongOrder(Order order) {
    return order.isLong();
  }

  /**
   * @see magellan.library.gamebinding.OrderChanger#isLongOrder(java.lang.String)
   * @deprecated The results of this method are not very accurate
   */
  @Deprecated
  public boolean isLongOrder(String order) {
    // getRules().getGameSpecificStuff().getOrderParser(DummyData(getRules())).parse(order,
    // Locales.getOrderLocale());
    /*
     * Wenn eine Order mit einem Eintrag aus LongOrdersTranslated beginnt, aber nicht mit einem aus
     * LongButShort, genau dann ist es eine long Order
     */
    String rOrder = order;
    if (rOrder.startsWith(EresseaConstants.O_COMMENT))
      return false;
    if (rOrder.startsWith(EresseaConstants.O_PERSISTENT)) {
      rOrder = rOrder.substring(1);
    }
    if (rOrder.startsWith(EresseaConstants.O_PCOMMENT))
      return false;
    boolean isInLongorder = false;
    for (String s : getLongOrdersTranslated()) {
      if (rOrder.toLowerCase().startsWith(s.toLowerCase())) {
        isInLongorder = true;
        break;
      }
    }
    if (!isInLongorder)
      return false;

    return !isLongButShort(rOrder, Locales.getOrderLocale());
  }

  /**
   * Returns if this is an order that is generally a long order, but short in this special case.
   * 
   * @param rOrder
   * @param orderLocale
   * 
   * @deprecated should use {@link #isLongOrder(Order)}
   */
  @Deprecated
  protected boolean isLongButShort(String rOrder, Locale orderLocale) {
    return rOrder.startsWith(getOrderO(Locales.getOrderLocale(), EresseaConstants.OC_MAKE,
        new Object[] { EresseaConstants.OC_TEMP }).getText());
  }

  /**
   * List of long orders in the default locale.
   *
   * @deprecated Use {@link #getLongOrdersTranslated()}.
   */
  @Deprecated
  protected ArrayList<String> getLongOrders() {
    return getLongOrders(null);
  }

  /**
   * List of long orders in the default locale.
   */
  protected ArrayList<String> getLongOrdersTranslated() {
    return getLongOrders(null);
  }

  // implementation must permit null key!
  private Map<Locale, ArrayList<String>> longOrders = new HashMap<Locale, ArrayList<String>>();

  /**
   * List of long orders in the selected locale.
   */
  protected ArrayList<String> getLongOrders(Locale locale) {
    if (locale == null) {
      locale = Locales.getOrderLocale();
    }
    ArrayList<String> orders = longOrders.get(locale);
    if (orders == null) {
      orders = translateOrders(getLongOrderTokens(), locale);
      longOrders.put(locale, orders);
    }
    return orders;
  }

  private ArrayList<StringID> longOrderTokens = null;

  /**
   * list of long orders in Eressea. <br />
   * ARBEITE, ATTACKIERE, BEKLAUE, BELAGERE, FAHRE, FOLGE, FORSCHE, KAUFE, LEHRE, LERNE, MACHE
   * (Ausnahme: MACHE TEMP), NACH, PFLANZE, PIRATERIE, ROUTE, SABOTIERE SCHIFF, SPIONIERE, TREIBE,
   * UNTERHALTE, VERKAUFE, ZAUBERE, ZÜCHTE, ZERSTÖRE.
   */
  protected ArrayList<StringID> getLongOrderTokens() {
    if (longOrderTokens == null) {
      longOrderTokens = new ArrayList<StringID>();
      longOrderTokens.add(EresseaConstants.OC_WORK);
      longOrderTokens.add(EresseaConstants.OC_ATTACK);
      longOrderTokens.add(EresseaConstants.OC_STEAL);
      longOrderTokens.add(EresseaConstants.OC_SIEGE);
      longOrderTokens.add(EresseaConstants.OC_RIDE);
      // longOrderTokens.add(EresseaConstants.OC_FOLLOW);
      longOrderTokens.add(EresseaConstants.OC_RESEARCH);
      longOrderTokens.add(EresseaConstants.OC_BUY);
      longOrderTokens.add(EresseaConstants.OC_TEACH);
      longOrderTokens.add(EresseaConstants.OC_LEARN);
      longOrderTokens.add(EresseaConstants.OC_MAKE);
      longOrderTokens.add(EresseaConstants.OC_MOVE);
      longOrderTokens.add(EresseaConstants.OC_PLANT);
      longOrderTokens.add(EresseaConstants.OC_PIRACY);
      longOrderTokens.add(EresseaConstants.OC_ROUTE);
      longOrderTokens.add(EresseaConstants.OC_SABOTAGE);
      longOrderTokens.add(EresseaConstants.OC_SPY);
      longOrderTokens.add(EresseaConstants.OC_TAX);
      longOrderTokens.add(EresseaConstants.OC_ENTERTAIN);
      longOrderTokens.add(EresseaConstants.OC_SELL);
      longOrderTokens.add(EresseaConstants.OC_CAST);
      longOrderTokens.add(EresseaConstants.OC_GROW);
      longOrderTokens.add(EresseaConstants.OC_DESTROY);
    }
    return longOrderTokens;
  }

  private ArrayList<String> translateOrders(ArrayList<StringID> orders, Locale locale) {
    ArrayList<String> result = new ArrayList<String>();
    for (StringID order : orders) {
      result.add(getOrderO(locale, order).getText());
    }
    return result;
  }

  /**
   * @see magellan.library.gamebinding.OrderChanger#areCompatibleLongOrders(Orders)
   */
  public int areCompatibleLongOrders(Orders orders) {
    if (orders.size() <= 1)
      return -1;

    CountMap<StringID> map = new CountMap<StringID>();

    // count frequency of orders
    int line = 0;
    for (Order order : orders) {
      if (isLongOrder(order)) {
        for (StringID longOrder : getLongOrderTokens()) {
          if (orders.isToken(order, 0, longOrder)
              && !orders.isToken(order, 0, EresseaConstants.OC_ATTACK)) {
            map.increase(longOrder, line);
            break;
          }
        }
      }
      line++;
    }

    StringID sell;
    StringID buy;
    if (map.containsKey(buy = EresseaConstants.OC_BUY)
        | map.containsKey(sell = EresseaConstants.OC_SELL)) {
      // only KAUFE and VERKAUFE and only one KAUFE are allowed
      if (map.size() > 2 || (map.size() == 2 && !(map.containsKey(buy) && map.containsKey(sell)))) {
        // there is another order except buy and sell
        boolean firstIsBuySell =
            map.keySet().iterator().next().equals(EresseaConstants.OC_BUY)
                || map.keySet().iterator().next().equals(EresseaConstants.OC_SELL);
        for (StringID order : map.keySet()) {
          if (order.equals(EresseaConstants.OC_BUY) || order.equals(EresseaConstants.OC_SELL)) {
            if (!firstIsBuySell)
              return map.get(order).get(0);
          } else {
            if (firstIsBuySell)
              return map.get(order).get(0);
          }
        }
        log.warn("unexpected case in areCompatibleLongOrders");
        return 0; // should not occur
      } else { // size <= 2 and map does contain only buy and sell
        if (map.containsKey(buy) && map.get(buy).size() > 1)
          return map.get(buy).get(1).intValue();
        return -1;
      }
    }

    if (map.containsKey(EresseaConstants.OC_CAST)) {
      // multiple ZAUBERE allowed
      if (map.size() == 1)
        return -1;

      // combination with other long orders not allowed
      boolean firstIsCast = map.keySet().iterator().next().equals(EresseaConstants.OC_CAST);
      boolean first = true;
      for (StringID order : map.keySet()) {
        if (order.equals(EresseaConstants.OC_CAST)) {
          if (!firstIsCast)
            return map.get(order).get(0);
        } else {
          if (firstIsCast || !first)
            return map.get(order).get(0);
        }
        first = false;
      }
      log.warn("unexpected case in areCompatibleLongOrders");
      return 0; // should not occur
    }

    if (map.size() == 0)
      return -1; // no long order
    else if (map.size() == 1)
      if (map.values().iterator().next().size() == 1)
        return -1; // exactly one long order
      else
        return map.values().iterator().next().get(1).intValue(); // multiple long orders of one type
    else { // map size > 1, more than one lone order
      StringID first = map.keySet().iterator().next();
      for (StringID order : map.keySet()) {
        if (!order.equals(first))
          return map.get(order).get(0);
      }
      log.warn("unexpected case in areCompatibleLongOrders");
      return 0; // should not occur
    }
  }

  /**
   * A map that counts occurrences of keys
   */
  public static class CountMap<T> extends LinkedHashMap<T, List<Integer>> {
    /**
     * Increase the value of key by delta. The value is assumed 0 if it's not present in the map.
     */
    @SuppressWarnings("boxing")
    public int increase(T key, int line) {
      List<Integer> value = get(key);
      if (value == null) {
        value = new ArrayList<Integer>(3);
      }
      value.add(line);
      put(key, value);
      return value.size();
    }
  }

  /**
   * @see magellan.library.gamebinding.OrderChanger#addGiveOrder(Unit, Unit, int, StringID, String)
   */
  public void addGiveOrder(Unit source, Unit target, int amount, StringID item, String comment) {
    List<Object> arguments = new LinkedList<Object>();

    arguments.add(target.getID());
    if (amount < 0) {
      arguments.add(EresseaConstants.OC_EACH);
    }
    if (amount == OrderChanger.ALL) {
      arguments.add(EresseaConstants.OC_ALL);
    } else {
      arguments.add(Math.abs(amount));
    }

    Order addedOrder = null;
    // FIXME methods like this should throw RulesExceptions if anything goes wrong...
    if (item != null) {
      ItemType itemType = getRules().getItemType(item);
      if (itemType == null)
        if (item.equals(EresseaConstants.I_MEN)) {
          arguments.add(EresseaConstants.OC_MEN);
        } else {
          addedOrder = source.createOrder("; unknown item " + item);
        }
      else {
        arguments.add(itemType.getOrderName());
      }
    } else {
      if (amount != OrderChanger.ALL) {
        addedOrder = source.createOrder("; illegal amount and no item " + amount);
      }
      // no further argument
    }
    if (addedOrder != null) {
      log.error(addedOrder);
    } else {
      arguments.add(comment != null ? (EresseaConstants.O_COMMENT + " " + comment) : "");

      addedOrder = getOrderO(source.getLocale(), EresseaConstants.OC_GIVE, arguments.toArray());
    }

    source.addOrder(addedOrder, false);
  }

  /**
   * @see magellan.library.gamebinding.OrderChanger#addGroupOrder(magellan.library.Unit,
   *      java.lang.String)
   */
  public void addGroupOrder(Unit unit, String name) {
    Order group;
    if (name != null && name.trim().length() > 0) {
      group =
          getOrderO(unit.getLocale(), EresseaConstants.OC_GROUP,
              new Object[] { ("\"" + name + "\"") });
    } else {
      group = getOrderO(unit.getLocale(), EresseaConstants.OC_GROUP);
    }
    // FIXME replace?
    unit.addOrder(group, true);
  }

  private List<Order> orderList = new ArrayList<Order>(100);

  /**
   * Scans this unit's orders for temp units to create. It constructs them as TempUnit objects and
   * removes the corresponding orders from this unit.
   *
   * @param tempSortIndex an index for sorting units (required to reconstruct the original order in
   *          the report) which is incremented with each new temp unit.
   * @param locale the locale to parse the orders with.
   * @return the new sort index. <tt>return value</tt> - sortIndex is the number of temp units read
   *         from this unit's orders.
   */
  public int extractTempUnits(GameData gdata, int tempSortIndex, Locale locale, Unit unit) {
    if (!unit.ordersAreNull()) {
      if (locale == null) {
        if (Locales.getOrderLocale() != null) {
          extractTempUnits(gdata, tempSortIndex, Locales.getOrderLocale(), unit);
        } else
          return tempSortIndex;
      }

      TempUnit tempUnit = null;

      boolean foundTemp = false;
      for (Order order : unit.getOrders2()) {
        if (isTempUnitOrder(order, unit.getOrders2(), locale)) {
          foundTemp = true;
          break;
        }
      }
      if (foundTemp) {
        ArrayList<Order> newOrders = new ArrayList<Order>(unit.getOrders2().size());
        orderList.clear();
        Orders ordersObject = unit.getOrders2();
        for (Order line : ordersObject) {
          if (tempUnit == null) {
            if (isTempUnitOrder(line, ordersObject, locale)) {
              try {
                final int base = (unit.getID()).getRadix();
                final UnitID orderTempID = UnitID.createUnitID(line.getToken(1).getText(), base);

                if (unit.getRegion() == null || unit.getRegion().getUnit(orderTempID) == null) {
                  tempUnit = unit.createTemp(gdata, orderTempID);
                  tempUnit.setSortIndex(++tempSortIndex);
                  if (line.size() > 4) {
                    addNamingOrder(tempUnit, line.getToken(3).getText());
                  }
                } else {
                  log.warn("region " + unit.getRegion()
                      + " already contains a temp unit with the id " + orderTempID
                      + ". This temp unit remains in the orders of its parent "
                      + "unit instead of being created as a unit in its own right.");
                }
              } catch (final NumberFormatException e) {
                // temp unit invalid -- don't create it
              }
            } else {
              newOrders.add(line);
            }
          } else {
            if (ordersObject.isToken(line, 0, EresseaConstants.OC_END)) {
              tempUnit = null;
            } else {
              scanTempOrder(tempUnit, line);
            }
          }
        }
        unit.setOrders2(newOrders);
      }
    }

    return tempSortIndex;
  }

  private final boolean isTempUnitOrder(Order line, Orders ordersObject, Locale locale) {
    if (line.getProblem() == null && !line.isEmpty()
        && ordersObject.isToken(line, 0, EresseaConstants.OC_MAKE)) {
      line.getToken(1);
      if (line.getToken(1).getText().toLowerCase().startsWith(
          getOrderO(locale, EresseaConstants.OC_TEMP).getText().toLowerCase()))
        return true;
    }
    return false;
  }

  private void scanTempOrder(TempUnit tempUnit, Order line) {
    boolean scanned = false;
    if (MagellanUnitImpl.CONFIRMEDTEMPCOMMENT.equals(line.toString())) {
      tempUnit.setOrdersConfirmed(true);
      scanned = true;
    }
    if (!scanned && !line.isEmpty() && line.getText().startsWith(MagellanUnitImpl.TAG_PREFIX_TEMP)) {
      String tag = null;
      String value = null;
      final StringTokenizer st = new StringTokenizer(line.getText());
      if (st.hasMoreTokens()) {
        // ignore TAG_PREFIX_TEMP
        st.nextToken();
      }
      if (st.hasMoreTokens()) {
        tag = st.nextToken();
      }
      if (st.hasMoreTokens()) {
        value = st.nextToken().replace('~', ' ');
      }
      if (tag != null && value != null) {
        tempUnit.putTag(tag, value);
        scanned = true;
      }
    }
    if (!scanned) {
      tempUnit.addOrder(line, false);
    }
  }

  /**
   * Returns the orders necessary to issue the creation of all the child temp units of this unit.
   */
  public Collection<? extends Order>
      getTempOrders(boolean writeUnitTagsAsVorlageComment, Unit unit) {
    final OrderParser parser = unit.getData().getOrderParser();
    final List<Order> cmds = new LinkedList<Order>();
    final Locale locale = unit.getLocale();

    for (TempUnit u : unit.tempUnits()) {
      cmds.add(getOrderO(locale, EresseaConstants.OC_MAKE, new Object[] { u.getID() }));

      cmds.addAll(u.getCompleteOrders(writeUnitTagsAsVorlageComment));

      if (u.isOrdersConfirmed()) {
        cmds.add(parser.parse(MagellanUnitImpl.CONFIRMEDTEMPCOMMENT, locale));
      }

      if (u.hasTags()) {
        final Map<String, String> tempUnitTags = u.getTagMap();
        for (String tag : u.getTagMap().keySet()) {
          final String value = tempUnitTags.get(tag);
          cmds.add(parser.parse(MagellanUnitImpl.TAG_PREFIX_TEMP + tag + " "
              + value.replace(' ', '~'), locale));
        }
      }

      cmds.add(getOrderO(locale, EresseaConstants.OC_END));
    }

    return cmds;
  }

  protected Order getOrderTranslation(StringID orderId, Unit unit) {
    return helper.getOrderTranslation(orderId, unit);
  }

  public String getTokenLocalized(Locale orderLocale, Object arg) throws RulesException {
    return helper.getTokenLocalized(orderLocale, arg);
  }

  public Order getOrderO(Locale orderLocale, StringID orderId) {
    return helper.getOrder(orderLocale, orderId);
  }

  public Order getOrderO(Locale orderLocale, StringID orderId, Object[] args) {
    return helper.getOrder(orderLocale, orderId, args);
  }

  public Order getOrderO(StringID orderId, Locale orderLocale) throws RulesException {
    return helper.getOrder(orderId, orderLocale);
  }

  public Order getOrderO(StringID orderId, Locale orderLocale, Object[] args) throws RulesException {
    return helper.getOrder(orderId, orderLocale, args);
  }

  @Deprecated
  public String getOrder(Locale orderLocale, StringID orderId) {
    return getOrderO(orderLocale, orderId).getText();
  }

  @Deprecated
  public String getOrder(Locale orderLocale, StringID orderId, Object[] args) {
    return getOrderO(orderLocale, orderId, args).getText();
  }

  @Deprecated
  public String getOrder(StringID orderId, Locale orderLocale) throws RulesException {
    return getOrderO(orderId, orderLocale).getText();
  }

  @Deprecated
  public String getOrder(StringID orderId, Locale orderLocale, Object[] args) throws RulesException {
    return getOrderO(orderId, orderLocale, args).getText();
  }

  /**
   * implements {@link OrderChanger#setLongOrders(Unit, List, boolean)}
   */
  public void setLongOrders(Unit unit, List<String> orders, boolean replace) {
    // add orders to captain
    if (replace) {
      unit.setOrders(orders);
    } else {
      List<Integer> toDisable = new ArrayList<Integer>();

      GameData data = unit.getData();
      int id = 1;
      for (id = 1; id < Integer.MAX_VALUE; ++id)
        if (data.getUnit(UnitID.createUnitID(id, data.base)) == null) {
          break;
        }
      Unit dummyUnit = MagellanFactory.createUnit(UnitID.createUnitID(id, data.base), data);

      Orders oldOrders = unit.getOrders2();
      dummyUnit.setOrders(orders);
      if (areCompatibleLongOrders(dummyUnit.getOrders2()) < 0) {
        for (int i = 0; i < oldOrders.size(); ++i) {
          Order checkedOrder = oldOrders.get(i);
          orders.add(checkedOrder.toString());
          dummyUnit.setOrders(orders);
          if (areCompatibleLongOrders(dummyUnit.getOrders2()) >= 0) {
            toDisable.add(i);
          }
          orders.remove(orders.size() - 1);
        }
      }
      for (Integer i : toDisable) {
        unit.replaceOrder(i, createOrder(unit, EresseaConstants.O_COMMENT + " "
            + oldOrders.get(i)));
      }

      for (ListIterator<String> iter = orders.listIterator(); iter.hasNext();) {
        unit.addOrder(iter.next());
      }
    }
  }

}
