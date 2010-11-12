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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import magellan.library.Building;
import magellan.library.Faction;
import magellan.library.Region;
import magellan.library.Rules;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.rules.Race;
import magellan.library.utils.Locales;
import magellan.library.utils.Resources;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 305 $
 */
public class EresseaOrderChanger implements OrderChanger {

  public static final String eresseaOrderChangedMarker = ";changed by Magellan";

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
    Collection<String> orders = new ArrayList<String>();
    orders.addAll(unit.getOrders());

    // remove hide (but not hide faction) order
    for (Iterator<String> iter = orders.iterator(); iter.hasNext();) {
      String order = iter.next();

      if (order.startsWith(Resources.getOrderTranslation(EresseaConstants.O_HIDE))
          && (order.indexOf(Resources.getOrderTranslation(EresseaConstants.O_FACTION)) == -1)) {
        boolean raceFound = false;

        for (Iterator<Race> it2 = getRules().getRaceIterator(); it2.hasNext();) {
          Race race = it2.next();

          if (order.indexOf(race.getName()) > 0) {
            raceFound = true;

            break;
          }
        }

        if (!raceFound) {
          iter.remove();
        }
      }
    }

    orders.add(createHideOrder(level));
    unit.setOrders(orders);
  }

  protected String createHideOrder(String level) {
    return Resources.getOrderTranslation(EresseaConstants.O_HIDE) + " " + level;
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
      str += Resources.getOrderTranslation(EresseaConstants.O_COMBAT_FRONT);

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

    orders.add("// " + Resources.getOrderTranslation(EresseaConstants.O_NUMBER) + " "
        + Resources.getOrderTranslation(EresseaConstants.O_UNIT) + " " + unit.getID());
    orders.add("// " + Resources.getOrderTranslation(EresseaConstants.O_NAME) + " "
        + Resources.getOrderTranslation(EresseaConstants.O_UNIT) + " \"" + unit.getName() + "\"");

    if (unit.getDescription() != null) {
      orders.add("// " + Resources.getOrderTranslation(EresseaConstants.O_DESCRIBE) + " "
          + Resources.getOrderTranslation(EresseaConstants.O_UNIT) + " \"" + unit.getDescription()
          + "\"");
    }

    if (!unit.isHideFaction()) {
      orders.add("// " + Resources.getOrderTranslation(EresseaConstants.O_HIDE) + " "
          + Resources.getOrderTranslation(EresseaConstants.O_FACTION) + " "
          + Resources.getOrderTranslation(EresseaConstants.O_NOT));
    }

    if (unit.getShip() != null) {
      orders.add("// " + Resources.getOrderTranslation(EresseaConstants.O_NUMBER) + " "
          + Resources.getOrderTranslation(EresseaConstants.O_SHIP) + " "
          + unit.getShip().getID().toString());
      orders.add("// " + Resources.getOrderTranslation(EresseaConstants.O_NAME) + " "
          + Resources.getOrderTranslation(EresseaConstants.O_SHIP) + " \""
          + unit.getShip().getName() + "\"");

      if (unit.getShip().getDescription() != null) {
        orders.add("// " + Resources.getOrderTranslation(EresseaConstants.O_DESCRIBE) + " "
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
    Collection<String> longOrders =
        toLowerCase(getLongOrders(u.getFaction().getLocale()), u.getFaction().getLocale());
    LinkedList<String> newOrders = new LinkedList<String>();
    for (String order : u.getOrders()) {
      boolean add = true;
      for (String longOrder : longOrders) {
        if (order.toLowerCase(u.getFaction().getLocale()).startsWith(longOrder)) {
          add = false;
          break;
        }
      }
      if (add) {
        newOrders.add(order);
      } else {
        newOrders.add("; " + order);
      }
    }
    u.setOrders(newOrders, false);
  }

  private List<String> toLowerCase(List<String> orders, Locale locale) {
    ArrayList<String> result = new ArrayList<String>();
    for (String order : orders) {
      result.add(order.toLowerCase(locale));
    }
    return result;
  }

  /**
   * @see magellan.library.gamebinding.OrderChanger#isLongOrder(java.lang.String)
   */
  public boolean isLongOrder(String order) {
    /*
     * Wenn eine Order mit einem Eintrag aus LongOrdersTranslated beginnt, aber nicht mit einem aus
     * LongButShort, genau dann ist es eine long Order
     */
    String rOrder = order;
    if (rOrder.startsWith("@")) {
      rOrder = rOrder.substring(1);
    }
    if (rOrder.startsWith(";"))
      return false;
    if (rOrder.startsWith("//"))
      return false;
    boolean isInLongorder = false;
    for (String s : getLongOrdersTranslated()) {
      if (order.toLowerCase().startsWith(s.toLowerCase())) {
        isInLongorder = true;
        break;
      }
    }
    if (!isInLongorder)
      return false;

    // Abgleich mit "NegativListe"
    boolean isInLongButShortOrders = false;
    for (String s : getLongButShortOrdersTranslated()) {
      if (order.toLowerCase().startsWith(s.toLowerCase())) {
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
   * @see magellan.library.gamebinding.OrderChanger#areCompatibleLongOrders(java.util.Collection)
   */
  public int areCompatibleLongOrders(Collection<String> orders) {
    if (orders.size() <= 1)
      return -1;

    Locale locale = Locales.getOrderLocale();

    CountMap<String> map = new CountMap<String>();

    // count frequency of orders
    Collection<String> longOrders = toLowerCase(getLongOrdersTranslated(), locale);
    for (String order : orders) {
      if (isLongOrder(order)) {
        for (String longOrder : longOrders) {
          if (order.toLowerCase().startsWith(longOrder)) {
            map.increase(longOrder);
            break;
          }
        }
      }
    }

    if (map.containsKey(Resources.getOrderTranslation(EresseaConstants.O_FOLLOW, locale))) {
      map.remove(Resources.getOrderTranslation(EresseaConstants.O_FOLLOW, locale));
    }

    String buy;
    String sell;
    if (map.containsKey(buy = Resources.getOrderTranslation(EresseaConstants.O_BUY, locale))
        | map.containsKey(sell = Resources.getOrderTranslation(EresseaConstants.O_SELL, locale))) {
      // only KAUFE and VERKAUFE and only one KAUFE are allowed
      if (map.size() > 2 || !(map.containsKey(buy) && map.containsKey(sell)))
        return findFirst(orders, buy, sell);
      else { // size <= 2 and map does contain only buy and sell
        if (map.get(buy) > 1)
          return findFirst(orders, buy);
        return -1;
      }
    }

    String zaubere;
    if (map.containsKey(zaubere = Resources.getOrderTranslation(EresseaConstants.O_CAST, locale))) {
      // ZAUBERE is compatible with itself only
      for (String candidate : map.keySet())
        if (!candidate.equalsIgnoreCase(zaubere))
          return findFirst(orders, candidate);
      return -1;
    }

    if (map.size() == 0)
      return -1;
    else if (map.size() == 1)
      if (map.values().iterator().next() == 1)
        return -1;
      else
        return findNth(orders, 2, map.keySet().iterator().next());
    else { // map size > 1
      int n = 0, line = 0;
      for (String order : orders) {
        if (isLongOrder(order)) {
          n++;
        }
        if (n >= 2)
          return line;
        line++;
      }
      return 0; // should never happen
    }

  }

  private int findFirst(Collection<String> orders, String... orderTranslation) {
    return findNth(orders, 1, orderTranslation);
  }

  private int findNth(Collection<String> orders, int n, String... orderTranslation) {
    int line = 0;
    int found = 0;
    for (String order : orders) {
      for (String candidate : orderTranslation)
        if (order.toLowerCase().startsWith(candidate)) {
          found++;
          if (found == n)
            return line;
        }
      line++;
    }
    return -1;
  }

  public class CountMap<T> extends HashMap<T, Integer> {
    public int increase(T key, int delta) {
      Integer value = get(key);
      if (value == null) {
        put(key, delta);
        return 1;
      } else {
        put(key, value + delta);
        return value + delta;
      }
    }

    public int increase(T key) {
      return increase(key, 1);
    }

    public int decrease(T key) {
      return increase(key, -1);
    }
  }

}
