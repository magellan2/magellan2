package magellan.library.gamebinding;

// class magellan.library.gamebinding.AtlantisOrderChanger
// created on Apr 16, 2013
//
// Copyright 2003-2013 by magellan project team
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
// package magellan.library.gamebinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import magellan.library.GameData;
import magellan.library.Item;
import magellan.library.Order;
import magellan.library.Orders;
import magellan.library.Rules;
import magellan.library.StringID;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.UnitID;
import magellan.library.completion.OrderParser;
import magellan.library.impl.MagellanUnitImpl;
import magellan.library.rules.ItemType;
import magellan.library.rules.OrderType;
import magellan.library.utils.logging.Logger;

public class AtlantisOrderChanger implements OrderChanger {
  private static final Logger log = Logger.getInstance(AtlantisOrderChanger.class);

  private static final Object[] EMPTY = new Object[0];

  private Rules rules;

  protected AtlantisOrderChanger(Rules rules) {
    this.rules = rules;
  }

  public Rules getRules() {
    return rules;
  }

  public Order createOrder(Unit unit, String order) {
    return unit.createOrder(order);
  }

  public void addCombatOrder(Unit unit, int newstate) {
    // HIGHTODO Automatisch generierte Methode implementieren
  }

  public void addDescribeUnitContainerOrder(Unit unit, UnitContainer uc, String descr) {
    // HIGHTODO Automatisch generierte Methode implementieren
  }

  public void addDescribeUnitOrder(Unit unit, String descr) {
    // HIGHTODO Automatisch generierte Methode implementieren
  }

  public void addDescribeUnitPrivateOrder(Unit unit, String descr) {
    // HIGHTODO Automatisch generierte Methode implementieren
  }

  public void addHideOrder(Unit unit, String level) {
    // HIGHTODO Automatisch generierte Methode implementieren
  }

  public void addGroupOrder(Unit unit, String name) {
    // HIGHTODO Automatisch generierte Methode implementieren
  }

  public void addNamingOrder(Unit unit, String name) {
    // HIGHTODO Automatisch generierte Methode implementieren
  }

  public void addNamingOrder(Unit unit, UnitContainer uc, String name) {
    // HIGHTODO Automatisch generierte Methode implementieren
  }

  public void addRecruitOrder(Unit u, int amount) {
    String order =
        getOrderTranslation(AtlantisConstants.OC_RECRUIT, u) + " " + String.valueOf(amount);
    u.addOrder(order);
  }

  public void addGiveOrder(Unit source, Unit target, int amount, StringID item, String comment) {
    Locale locale = source.getLocale();
    String sItem = "";
    String tmpOrders = null;
    Item sourceItem = new Item(new ItemType(StringID.create("unknown")), 1);
    if (item != null) {
      if (item.equals(EresseaConstants.I_MEN)) {
        tmpOrders = getOrder(locale, AtlantisConstants.OC_TRANSFER, target.getID(), locale);
      } else {
        ItemType itemType = getRules().getItemType(item);
        if (itemType == null) {
          tmpOrders = "; unknown item " + item;
        } else {
          sItem = itemType.getOrderName();
          sourceItem = source.getItem(itemType);
        }
      }
    } else {
      tmpOrders = "; give all not supported";
    }

    if (tmpOrders != null) {
      log.error(tmpOrders);
    } else {
      if (item.equals(AtlantisConstants.I_USILVER)) {
        tmpOrders =
            getOrder(locale, AtlantisConstants.OC_PAY, target.getID(), (amount < 0 ? target
                .getPersons()
                * amount : (amount == OrderChanger.ALL ? (sourceItem == null ? 0 : sourceItem
                .getAmount()) : amount)), (comment != null ? ("; " + comment) : ""));
      } else {
        tmpOrders =
            getOrder(locale, AtlantisConstants.OC_GIVE, target.getID(), (amount < 0 ? target
                .getPersons()
                * amount : (amount == OrderChanger.ALL ? (sourceItem == null ? 0 : sourceItem
                .getAmount()) : amount)), sItem, (comment != null ? ("; " + comment) : ""));
      }
    }
    source.addOrder(tmpOrders);
  }

  public void addMultipleHideOrder(Unit u) {
    // HIGHTODO Automatisch generierte Methode implementieren
  }

  public void disableLongOrders(Unit u) {
    // HIGHTODO Automatisch generierte Methode implementieren
  }

  public boolean isLongOrder(String order) {
    // HIGHTODO Automatisch generierte Methode implementieren
    return false;
  }

  public boolean isLongOrder(Order order) {
    // HIGHTODO Automatisch generierte Methode implementieren
    return false;
  }

  public int areCompatibleLongOrders(Orders orders) {
    // HIGHTODO Automatisch generierte Methode implementieren
    return orders.size() > 1 ? 1 : -1;
  }

  /**
   * Returns the order with the given id, localized for the unit's order locale.
   * 
   * @see OrderType#getName(Locale)
   */
  protected String getOrderTranslation(StringID id, Unit unit) {
    return getOrder(unit.getLocale(), id);
  }

  /**
   * @see magellan.library.gamebinding.OrderChanger#getTokenLocalized(java.util.Locale,
   *      java.lang.Object)
   */
  public String getTokenLocalized(Locale orderLocale, Object arg) throws RulesException {
    if (arg instanceof StringID)
      return getOrder1((StringID) arg, orderLocale);
    else if (arg instanceof UnitID)
      if (((UnitID) arg).intValue() < 0)
        return getTemp(orderLocale) + "  " + arg.toString();
      else
        return arg.toString();
    else
      return arg.toString();
  }

  protected String getTemp(Locale locale) {
    return getOrder(locale, AtlantisConstants.OC_NEW);
  }

  public String getOrder(Locale orderLocale, StringID orderId) {
    try {
      return getOrder(orderId, orderLocale, EMPTY);
    } catch (RulesException e) {
      return orderId.toString();
    }
  }

  public String getOrder(Locale orderLocale, StringID orderId, Object... args) {
    try {
      return getOrder(orderId, orderLocale, args);
    } catch (RulesException e) {
      return orderId.toString();
    }
  }

  public String getOrder(StringID orderId, Locale orderLocale) throws RulesException {
    return getOrder(orderId, orderLocale, EMPTY);
  }

  public String getOrder(StringID orderId, Locale orderLocale, Object... args)
      throws RulesException {
    StringBuilder order = new StringBuilder();
    order.append(getOrder1(orderId, orderLocale));
    for (Object arg : args) {
      order.append(" ").append(getTokenLocalized(orderLocale, arg));
    }
    return order.toString();
  }

  private String getOrder1(StringID orderId, Locale orderLocale) throws RulesException {
    OrderType order = getRules().getOrder(orderId);
    if (order == null)
      throw new RulesException("unknown order " + orderId);
    String name = order.getName(orderLocale);
    if (name == null)
      throw new RulesException("no translation for " + orderId + " into " + orderLocale);
    return name;
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
      TempUnit tempUnit = null;

      boolean foundTemp = false;
      for (Order order : unit.getOrders2()) {
        if (isTempUnitOrder(order, unit.getOrders2(), unit)) {
          foundTemp = true;
          break;
        }
      }
      if (foundTemp) {
        ArrayList<Order> neworders = new ArrayList<Order>(unit.getOrders2().size());
        orderList.clear();
        Orders ordersObject = unit.getOrders2();
        for (Order line : ordersObject) {
          if (tempUnit == null) {
            if (isTempUnitOrder(line, ordersObject, unit)) {
              try {
                final int base = (unit.getID()).getRadix();
                final UnitID orderTempID = UnitID.createUnitID(line.getToken(1).getText(), base);

                if (unit.getRegion() == null || unit.getRegion().getUnit(orderTempID) == null) {
                  tempUnit = unit.createTemp(gdata, orderTempID);
                  tempUnit.setSortIndex(++tempSortIndex);
                  if (line.size() > 4) {
                    tempUnit.addOrders(Collections.singleton(getOrderTranslation(
                        AtlantisConstants.OC_NAME, unit)
                        + " "
                        + getOrderTranslation(AtlantisConstants.OC_UNIT, unit)
                        + " "
                        + line.getToken(3).getText()), false);
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
              neworders.add(line);
            }
          } else {
            if (ordersObject.isToken(line, 0, AtlantisConstants.OC_END)) {
              tempUnit = null;
            } else {
              scanTempOrder(tempUnit, line);
            }
          }
        }
      }
    }

    return tempSortIndex;
  }

  private final boolean isTempUnitOrder(Order line, Orders ordersObject, Unit unit) {
    if (line.getProblem() == null && !line.isEmpty()
        && ordersObject.isToken(line, 0, AtlantisConstants.OC_FORM))
      return true;
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
  public Collection<? extends Order> getTempOrders(boolean writeUnitTagsAsVorlageComment, Unit unit) {
    final OrderParser parser = getRules().getGameSpecificStuff().getOrderParser(unit.getData());
    final List<Order> cmds = new LinkedList<Order>();
    final Locale locale = unit.getLocale();

    for (TempUnit u : unit.tempUnits()) {
      cmds.add(parser.parse(getOrder(locale, AtlantisConstants.OC_MAKE, u.getID()), locale));

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

      cmds.add(parser.parse(getOrder(locale, AtlantisConstants.OC_END), locale));
    }

    return cmds;
  }
}
