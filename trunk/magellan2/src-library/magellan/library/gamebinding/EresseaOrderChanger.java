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
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import magellan.library.Building;
import magellan.library.Faction;
import magellan.library.Order;
import magellan.library.Orders;
import magellan.library.Region;
import magellan.library.Rules;
import magellan.library.Ship;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.rules.ItemType;
import magellan.library.rules.Race;
import magellan.library.utils.Locales;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * OrderChanger class for the game Eressea.
 */
public class EresseaOrderChanger implements OrderChanger {
  public static final String eresseaOrderChangedMarker = ";changed by Magellan";

  protected static final String PCOMMENTSTART = EresseaConstants.O_PCOMMENT + " ";

  private static final Logger log = Logger.getInstance(EresseaOrderChanger.class);

  private Rules rules;

  protected EresseaOrderChanger(Rules rules) {
    this.rules = rules;
  }

  public Rules getRules() {
    return rules;
  }

  /**
   * @see magellan.library.gamebinding.OrderChanger#addNamingOrder(magellan.library.Unit,
   *      java.lang.String)
   */
  public void addNamingOrder(Unit unit, String name) {
    String order = createNamingOrder(name);
    unit.addOrder(order, true, 2);
  }

  protected String createNamingOrder(String name) {
    return Resources.getOrderTranslation(EresseaConstants.O_NAME) + " "
        + Resources.getOrderTranslation(EresseaConstants.O_UNIT) + " \"" + name + "\"";
  }

  /**
   * @see magellan.library.gamebinding.OrderChanger#addNamingOrder(magellan.library.Unit,
   *      magellan.library.UnitContainer, java.lang.String)
   */
  public void addNamingOrder(Unit unit, UnitContainer uc, String name) {
    String order = createNamingOrder(uc, name);
    unit.addOrder(order, true, 2);
  }

  protected String createNamingOrder(UnitContainer uc, String name) {
    String order = null;

    if (uc instanceof Building) {
      order = Resources.getOrderTranslation(EresseaConstants.O_CASTLE);
    } else if (uc instanceof Ship) {
      order = Resources.getOrderTranslation(EresseaConstants.O_SHIP);
    } else if (uc instanceof Region) {
      order = Resources.getOrderTranslation(EresseaConstants.O_REGION);
    } else if (uc instanceof Faction) {
      order = Resources.getOrderTranslation(EresseaConstants.O_FACTION);
    }

    return Resources.getOrderTranslation(EresseaConstants.O_NAME) + " " + order + " \"" + name
        + "\"";
  }

  /**
   * @see magellan.library.gamebinding.OrderChanger#addDescribeUnitContainerOrder(magellan.library.Unit,
   *      magellan.library.UnitContainer, java.lang.String)
   */
  public void addDescribeUnitContainerOrder(Unit unit, UnitContainer uc, String descr) {
    String suborder = createDescribeUnitContainerOrder(uc);
    String order = suborder + " \"" + descr + "\"";
    unit.addOrder(order, true, (suborder.indexOf(" ") >= 0) ? 2 : 1);
  }

  protected String createDescribeUnitContainerOrder(UnitContainer uc) {
    String order = null;

    if (uc instanceof Building) {
      order =
          Resources.getOrderTranslation(EresseaConstants.O_DESCRIBE) + " "
              + Resources.getOrderTranslation(EresseaConstants.O_CASTLE);
    } else if (uc instanceof Ship) {
      order =
          Resources.getOrderTranslation(EresseaConstants.O_DESCRIBE) + " "
              + Resources.getOrderTranslation(EresseaConstants.O_SHIP);
    } else if (uc instanceof Region) {
      order =
          Resources.getOrderTranslation(EresseaConstants.O_DESCRIBE) + " "
              + Resources.getOrderTranslation(EresseaConstants.O_REGION);
    } else if (uc instanceof Faction) {
      order = Resources.getOrderTranslation(EresseaConstants.O_BANNER);
    }

    return order;
  }

  /**
   * @see magellan.library.gamebinding.OrderChanger#addDescribeUnitPrivateOrder(magellan.library.Unit,
   *      java.lang.String)
   */
  public void addDescribeUnitPrivateOrder(Unit unit, String descr) {
    String order = createDescribeUnitPrivateOrder(descr);
    unit.addOrder(order, true, 2);
  }

  protected String createDescribeUnitPrivateOrder(String descr) {
    return Resources.getOrderTranslation(EresseaConstants.O_DESCRIBE) + " "
        + Resources.getOrderTranslation(EresseaConstants.O_PRIVATE) + " \"" + descr + "\"";
  }

  /**
   * @see magellan.library.gamebinding.OrderChanger#addDescribeUnitOrder(magellan.library.Unit,
   *      java.lang.String)
   */
  public void addDescribeUnitOrder(Unit unit, String descr) {
    String order = createDescribeUnitOrder(descr);
    unit.addOrder(order, true, 2);
  }

  protected String createDescribeUnitOrder(String descr) {
    return Resources.getOrderTranslation(EresseaConstants.O_DESCRIBE) + " "
        + Resources.getOrderTranslation(EresseaConstants.O_UNIT) + " \"" + descr + "\"";
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

      if (orders.isToken(order, 0, EresseaConstants.O_HIDE))
        if (orders.isToken(order, 1, EresseaConstants.O_FACTION)) {
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
    return createOrder(unit, Resources.getOrderTranslation(EresseaConstants.O_HIDE) + " " + level);
  }

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
    String str = Resources.getOrderTranslation(EresseaConstants.O_COMBAT) + " ";

    switch (newState) {
    case 0:
      str += Resources.getOrderTranslation(EresseaConstants.O_COMBAT_AGGRESSIVE);

      break;

    case 1:
      // KÄMPFE VORNE is deprecated
      str += PCOMMENTSTART + Resources.getOrderTranslation(EresseaConstants.O_COMBAT_FRONT);

      break;

    case 2:
      str += Resources.getOrderTranslation(EresseaConstants.O_COMBAT_REAR);

      break;

    case 3:
      str += Resources.getOrderTranslation(EresseaConstants.O_COMBAT_DEFENSIVE);

      break;

    case 4:
      str += Resources.getOrderTranslation(EresseaConstants.O_COMBAT_NOT);

      break;

    case 5:
      str += Resources.getOrderTranslation(EresseaConstants.O_COMBAT_FLEE);

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
    String order =
        Resources.getOrderTranslation(EresseaConstants.O_RECRUIT) + " " + String.valueOf(i);
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
    orders.add(Resources.getOrderTranslation(EresseaConstants.O_NUMBER) + " "
        + Resources.getOrderTranslation(EresseaConstants.O_UNIT) + " ");
    orders.add(Resources.getOrderTranslation(EresseaConstants.O_NAME) + " "
        + Resources.getOrderTranslation(EresseaConstants.O_UNIT) + " \"\"");
    orders.add(Resources.getOrderTranslation(EresseaConstants.O_DESCRIBE) + " "
        + Resources.getOrderTranslation(EresseaConstants.O_UNIT) + " \"\"");
    orders.add(Resources.getOrderTranslation(EresseaConstants.O_HIDE) + " "
        + Resources.getOrderTranslation(EresseaConstants.O_FACTION));

    if (unit.getShip() != null) {
      orders.add(Resources.getOrderTranslation(EresseaConstants.O_NUMBER) + " "
          + Resources.getOrderTranslation(EresseaConstants.O_SHIP));
      orders.add(Resources.getOrderTranslation(EresseaConstants.O_NAME) + " "
          + Resources.getOrderTranslation(EresseaConstants.O_SHIP) + " \"\"");
      orders.add(Resources.getOrderTranslation(EresseaConstants.O_DESCRIBE) + " "
          + Resources.getOrderTranslation(EresseaConstants.O_SHIP) + " \"\"");
    }

    orders.add(PCOMMENTSTART + Resources.getOrderTranslation(EresseaConstants.O_NUMBER) + " "
        + Resources.getOrderTranslation(EresseaConstants.O_UNIT) + " " + unit.getID());
    orders.add(PCOMMENTSTART + Resources.getOrderTranslation(EresseaConstants.O_NAME) + " "
        + Resources.getOrderTranslation(EresseaConstants.O_UNIT) + " \"" + unit.getName() + "\"");

    if (unit.getDescription() != null) {
      orders.add(PCOMMENTSTART + Resources.getOrderTranslation(EresseaConstants.O_DESCRIBE) + " "
          + Resources.getOrderTranslation(EresseaConstants.O_UNIT) + " \"" + unit.getDescription()
          + "\"");
    }

    if (!unit.isHideFaction()) {
      orders.add(PCOMMENTSTART + Resources.getOrderTranslation(EresseaConstants.O_HIDE) + " "
          + Resources.getOrderTranslation(EresseaConstants.O_FACTION) + " "
          + Resources.getOrderTranslation(EresseaConstants.O_NOT));
    }

    if (unit.getShip() != null) {
      orders.add(PCOMMENTSTART + Resources.getOrderTranslation(EresseaConstants.O_NUMBER) + " "
          + Resources.getOrderTranslation(EresseaConstants.O_SHIP) + " "
          + unit.getShip().getID().toString());
      orders.add(PCOMMENTSTART + Resources.getOrderTranslation(EresseaConstants.O_NAME) + " "
          + Resources.getOrderTranslation(EresseaConstants.O_SHIP) + " \""
          + unit.getShip().getName() + "\"");

      if (unit.getShip().getDescription() != null) {
        orders.add(PCOMMENTSTART + Resources.getOrderTranslation(EresseaConstants.O_DESCRIBE) + " "
            + Resources.getOrderTranslation(EresseaConstants.O_SHIP) + " \""
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
      for (String longOrder : getLongOrderTokens()) {
        if (u.getOrders2().isToken(order, 0, longOrder)) {
          u.replaceOrder(i, createOrder(u, "; " + order.getText()), false);
        }
      }
      ++i;
    }
    u.refreshRelations();
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

    // Abgleich mit "NegativListe"
    boolean isInLongButShortOrders = false;
    for (String s : getLongButShortOrdersTranslated()) {
      if (rOrder.toLowerCase().startsWith(s.toLowerCase())) {
        isInLongButShortOrders = true;
        break;
      }
    }
    if (isInLongButShortOrders)
      return false;
    return true;
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

  /**
   * List of orders in the default locale.
   */
  protected ArrayList<String> getLongButShortOrdersTranslated() {
    return getLongButShortOrders(null);
  }

  // implementation must permit null key!
  private Map<Locale, ArrayList<String>> longButShortOrders =
      new HashMap<Locale, ArrayList<String>>();

  /**
   * List of orders in the selected locale, which could be identified as long, but in the listed
   * form are short orders.. make temp = short (in this list) make sword = long (not in this list)
   */
  protected ArrayList<String> getLongButShortOrders(Locale locale) {
    if (locale == null) {
      locale = Locales.getOrderLocale();
    }
    ArrayList<String> orders = longButShortOrders.get(locale);
    if (orders == null) {
      orders = translateOrders(getLongButShortOrderTokens(), locale);
      longButShortOrders.put(locale, orders);
    }
    return orders;
  }

  private ArrayList<String> longOrderTokens = null;

  /**
   * list of long orders in Eressea. <br />
   * ARBEITE, ATTACKIERE, BEKLAUE, BELAGERE, FAHRE, FOLGE, FORSCHE, KAUFE, LEHRE, LERNE, MACHE
   * (Ausnahme: MACHE TEMP), NACH, PFLANZE, PIRATERIE, ROUTE, SABOTIERE SCHIFF, SPIONIERE, TREIBE,
   * UNTERHALTE, VERKAUFE, ZAUBERE, ZÜCHTE.
   * 
   * @return
   */
  protected ArrayList<String> getLongOrderTokens() {
    if (longOrderTokens == null) {
      longOrderTokens = new ArrayList<String>();
      longOrderTokens.add(EresseaConstants.O_WORK);
      longOrderTokens.add(EresseaConstants.O_ATTACK);
      longOrderTokens.add(EresseaConstants.O_STEAL);
      longOrderTokens.add(EresseaConstants.O_SIEGE);
      longOrderTokens.add(EresseaConstants.O_RIDE);
      longOrderTokens.add(EresseaConstants.O_FOLLOW);
      longOrderTokens.add(EresseaConstants.O_RESEARCH);
      longOrderTokens.add(EresseaConstants.O_BUY);
      longOrderTokens.add(EresseaConstants.O_TEACH);
      longOrderTokens.add(EresseaConstants.O_LEARN);
      longOrderTokens.add(EresseaConstants.O_MAKE);
      longOrderTokens.add(EresseaConstants.O_MOVE);
      longOrderTokens.add(EresseaConstants.O_PLANT);
      longOrderTokens.add(EresseaConstants.O_PIRACY);
      longOrderTokens.add(EresseaConstants.O_ROUTE);
      longOrderTokens.add(EresseaConstants.O_SABOTAGE);
      longOrderTokens.add(EresseaConstants.O_SPY);
      longOrderTokens.add(EresseaConstants.O_TAX);
      longOrderTokens.add(EresseaConstants.O_ENTERTAIN);
      longOrderTokens.add(EresseaConstants.O_SELL);
      longOrderTokens.add(EresseaConstants.O_CAST);
      longOrderTokens.add(EresseaConstants.O_GROW);
    }
    return longOrderTokens;
  }

  private ArrayList<String> longButShortOrderTokens = null;

  /**
   * list of orders, which could be identified as long, but in the listed form are short orders..
   * make temp = short (in this list) make sword = long (not in this list)
   */
  protected ArrayList<String> getLongButShortOrderTokens() {
    if (longButShortOrderTokens == null) {
      longButShortOrderTokens = new ArrayList<String>();
      longButShortOrderTokens.add(EresseaConstants.O_MAKE + " " + EresseaConstants.O_TEMP);
    }
    return longButShortOrderTokens;
  }

  private ArrayList<String> translateOrders(ArrayList<String> orders, Locale locale) {
    ArrayList<String> result = new ArrayList<String>();
    for (String order : orders) {
      StringBuilder translation = new StringBuilder();
      for (StringTokenizer tokenizer = new StringTokenizer(order); tokenizer.hasMoreTokens();) {
        if (translation.length() != 0) {
          translation.append(" ");
        }
        translation.append(Resources.getOrderTranslation(tokenizer.nextToken(), locale));
      }
      result.add(translation.toString());
    }
    return result;
  }

  /**
   * @see magellan.library.gamebinding.OrderChanger#areCompatibleLongOrders(Orders)
   */
  public int areCompatibleLongOrders(Orders orders) {
    if (orders.size() <= 1)
      return -1;

    CountMap<String> map = new CountMap<String>();

    // count frequency of orders
    Collection<String> longOrders = getLongOrderTokens();
    int line = 0;
    for (Order order : orders) {
      if (isLongOrder(order)) {
        for (String longOrder : longOrders) {
          if (orders.isToken(order, 0, longOrder)
              && !orders.isToken(order, 0, EresseaConstants.O_ATTACK)) {
            map.increase(longOrder, line);
            break;
          }
        }
      }
      line++;
    }

    String follow;
    if (map.containsKey(follow = EresseaConstants.O_FOLLOW)) {
      // ignore FOLGE EINHEIT
      for (Iterator<Integer> it = map.get(follow).iterator(); it.hasNext();) {
        Integer occ = it.next();
        if (orders.isToken(orders.get(occ.intValue()), 1, EresseaConstants.O_UNIT)) {
          it.remove();
          break;
        }
      }
      if (map.get(follow).size() == 0) {
        map.remove(follow);
      }
    }

    String sell;
    String buy;
    if (map.containsKey(buy = EresseaConstants.O_BUY)
        | map.containsKey(sell = EresseaConstants.O_SELL)) {
      // only KAUFE and VERKAUFE and only one KAUFE are allowed
      if (map.size() > 2 || (map.size() == 2 && !(map.containsKey(buy) && map.containsKey(sell)))) {
        // there is another order except buy and sell
        boolean firstIsBuySell =
            map.keySet().iterator().next().equals(EresseaConstants.O_BUY)
                || map.keySet().iterator().next().equals(EresseaConstants.O_SELL);
        for (String order : map.keySet()) {
          if (order.equals(EresseaConstants.O_BUY) || order.equals(EresseaConstants.O_SELL)) {
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

    if (map.containsKey(EresseaConstants.O_CAST)) {
      // multiple ZAUBERE allowed
      if (map.size() == 1)
        return -1;

      // combination with other long orders not allowed
      boolean firstIsCast = map.keySet().iterator().next().equals(EresseaConstants.O_CAST);
      boolean first = true;
      for (String order : map.keySet()) {
        if (order.equals(EresseaConstants.O_CAST)) {
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
      String first = map.keySet().iterator().next();
      for (String order : map.keySet()) {
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
    String sItem = "";
    String tmpOrders = null;
    // FIXME methods like this should throw RulesExceptions if anything goes wrong...
    if (item != null) {
      ItemType itemType = getRules().getItemType(item);
      if (itemType == null)
        if (item.equals(EresseaConstants.I_MEN)) {
          sItem = " " + Resources.getOrderTranslation(EresseaConstants.O_MEN);
        } else {
          tmpOrders = "; unknown item " + item;
        }
      else {
        sItem = " " + itemType.getOrderName();
      }
    } else {
      if (amount != OrderChanger.ALL) {
        tmpOrders = "; illegal amount and no item " + amount;
      }
      sItem = "";
    }
    if (tmpOrders != null) {
      log.error(tmpOrders);
    } else {
      tmpOrders =
          Resources.getOrderTranslation(EresseaConstants.O_GIVE)
              + " "
              + target.getID().toString(true, source.getLocale())
              + (amount < 0 ? (" " + Resources.getOrderTranslation(EresseaConstants.O_EACH) + " ")
                  : " ")
              + (amount == OrderChanger.ALL ? Resources.getOrderTranslation(EresseaConstants.O_ALL)
                  : Math.abs(amount)) + sItem + (comment != null ? ("; " + comment) : "");
    }
    source.addOrder(tmpOrders);
  }

}
