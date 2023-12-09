package magellan.library.gamebinding.atlantis;

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

import magellan.library.Building;
import magellan.library.GameData;
import magellan.library.Item;
import magellan.library.Order;
import magellan.library.Orders;
import magellan.library.Rules;
import magellan.library.Ship;
import magellan.library.StringID;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.UnitID;
import magellan.library.completion.OrderParser;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.OrderChanger;
import magellan.library.gamebinding.OrderTranslationHelper;
import magellan.library.gamebinding.RulesException;
import magellan.library.gamebinding.SimpleOrderFactory;
import magellan.library.impl.MagellanUnitImpl;
import magellan.library.rules.ItemType;
import magellan.library.utils.Locales;
import magellan.library.utils.logging.Logger;

/**
 * Order changer for Atlantis game.
 */
public class AtlantisOrderChanger implements OrderChanger {
  private static final Logger log = Logger.getInstance(AtlantisOrderChanger.class);

  private Rules rules;

  private GameData dummyData;

  private OrderTranslationHelper helper;

  protected AtlantisOrderChanger(Rules rules, SimpleOrderFactory factory) {
    this.rules = rules;
    helper = new OrderTranslationHelper(rules, factory, AtlantisConstants.OC_NEW);
  }

  /**
   * Return rules.
   */
  public Rules getRules() {
    return rules;
  }

  public Order createOrder(Unit unit, String order) {
    return unit.createOrder(order);
  }

  public void addCombatOrder(Unit unit, int newstate) {
    if (newstate > 0) {
      unit.addOrder(getOrderO(unit.getLocale(), AtlantisConstants.OC_BEHIND,
          new Object[] { newstate - 1 }).getText());
    }
  }

  public void addDescribeUnitContainerOrder(Unit unit, UnitContainer uc, String descr) {
    if (uc instanceof Ship) {
      unit.addOrder(getOrderO(unit.getLocale(), AtlantisConstants.OC_DISPLAY,
          new Object[] { AtlantisConstants.OC_SHIP, "\"" + descr + "\"" }).getText());
    } else if (uc instanceof Building) {
      unit.addOrder(getOrderO(unit.getLocale(), AtlantisConstants.OC_DISPLAY,
          new Object[] { AtlantisConstants.OC_BUILDING, "\"" + descr + "\"" }).getText());
    } else {
      unit.addOrder(AtlantisConstants.O_COMMENT + "can't describe " + uc);
    }
  }

  public void addDescribeUnitOrder(Unit unit, String descr) {
    unit.addOrder(getOrderO(unit.getLocale(), AtlantisConstants.OC_DISPLAY,
        new Object[] { AtlantisConstants.OC_UNIT, "\"" + descr + "\"" }).getText());
  }

  public void addDescribeUnitPrivateOrder(Unit unit, String descr) {
    // impossible
  }

  public void addHideOrder(Unit unit, String level) {
    // impossible
  }

  public void addGroupOrder(Unit unit, String name) {
    // impossible
  }

  public void addNamingOrder(Unit unit, String name) {
    unit.addOrder(getOrderO(unit.getLocale(), AtlantisConstants.OC_NAME,
        new Object[] { AtlantisConstants.OC_UNIT, "\"" + name + "\"" }).getText());
  }

  /**
   * @see magellan.library.gamebinding.OrderChanger#addNamingOrder(magellan.library.Unit,
   *      magellan.library.UnitContainer, java.lang.String)
   */
  public void addNamingOrder(Unit unit, UnitContainer uc, String name) {
    if (uc instanceof Ship) {
      unit.addOrder(getOrderO(unit.getLocale(), AtlantisConstants.OC_NAME,
          new Object[] { AtlantisConstants.OC_SHIP, "\"" + name + "\"" }).getText());
    } else if (uc instanceof Building) {
      unit.addOrder(getOrderO(unit.getLocale(), AtlantisConstants.OC_NAME,
          new Object[] { AtlantisConstants.OC_BUILDING, "\"" + name + "\"" }).getText());
    } else {
      unit.addOrder(AtlantisConstants.O_COMMENT + "can't name " + uc);
    }
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
      if (!item.equals(EresseaConstants.I_MEN)) {
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

    if (tmpOrders != null || item == null) {
      log.error(tmpOrders);
    } else {
      if (item.equals(EresseaConstants.I_MEN)) {
        tmpOrders =
            getOrderO(locale, AtlantisConstants.OC_TRANSFER,
                new Object[] { target.getID(), amount }).getText();
      } else if (item.equals(AtlantisConstants.I_USILVER)) {
        tmpOrders =
            getOrderO(
                locale,
                AtlantisConstants.OC_PAY,
                new Object[] {
                    target.getID(),
                    (amount < 0 ? target.getPersons() * amount : (amount == OrderChanger.ALL
                        ? (sourceItem == null ? 0 : sourceItem.getAmount()) : amount)),
                    (comment != null ? ("; " + comment) : "") }).getText();
      } else {
        tmpOrders =
            getOrderO(
                locale,
                AtlantisConstants.OC_GIVE,
                new Object[] {
                    target.getID(),
                    (amount < 0 ? target.getPersons() * amount : (amount == OrderChanger.ALL
                        ? (sourceItem == null ? 0 : sourceItem.getAmount()) : amount)), sItem,
                    (comment != null ? ("; " + comment) : "") }).getText();
      }
    }
    source.addOrder(tmpOrders);
  }

  public void addMultipleHideOrder(Unit u) {
    u.addOrder(getOrderO(u.getLocale(), AtlantisConstants.OC_NAME,
        new Object[] { AtlantisConstants.OC_UNIT, "Unit" }).getText());
    u.addOrder(getOrderO(u.getLocale(), AtlantisConstants.OC_DISPLAY,
        new Object[] { AtlantisConstants.OC_UNIT, "\"\"" }).getText());
  }

  public void disableLongOrders(Unit u) {
    Collection<String> newOrders = new ArrayList<String>(u.getOrders2().size());
    boolean changed = false;
    for (Order order : u.getOrders2()) {
      if (order.isLong()) {
        changed = true;
        newOrders.add(AtlantisConstants.O_COMMENT + order.getText());
      } else {
        newOrders.add(order.getText());
      }
    }
    if (changed) {
      u.setOrders(newOrders);
    }
  }

  public boolean isLongOrder(String order) {
    return getDummyData().getOrderParser().parse(order, Locales.getOrderLocale()).isLong();
  }

  protected GameData getDummyData() {
    if (dummyData == null) {
      dummyData =
          getRules().getGameSpecificStuff().createGameData(
              getRules().getGameSpecificStuff().getName());
    }
    return dummyData;
  }

  public boolean isLongOrder(Order order) {
    return order.isLong();
  }

  public int areCompatibleLongOrders(Orders orders) {
    int i = 0, l = 0;
    for (Order order : orders) {
      if (order.isLong()) {
        l++;
      }
      if (l > 1)
        return i;
      i++;
    }
    return -1;
  }

  private List<Order> orderList = new ArrayList<Order>(100);

  /**
   * Scans this unit's orders for temp units to create. It constructs them as TempUnit objects and
   * removes the corresponding orders from this unit.
   *
   * @param tempSortIndex an index for sorting units (required to reconstruct the original order in
   *          the report) which is incremented with each new temp unit.
   * @param locale the locale to parse the orders with.
   * @return the new sort index. <kbd>return value</kbd> - sortIndex is the number of temp units read
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
        ArrayList<Order> newOrders = new ArrayList<Order>(unit.getOrders2().size());
        orderList.clear();
        Orders ordersObject = unit.getOrders2();
        for (Order line : ordersObject) {
          if (tempUnit == null) {
            if (isTempUnitOrder(line, ordersObject, unit)) {
              try {
                final int base = (unit.getID()).getRadix();
                UnitID orderTempID = UnitID.createUnitID(line.getToken(1).getText(), base);
                orderTempID = UnitID.createUnitID(-orderTempID.intValue(), base);

                if (unit.getRegion() == null || unit.getRegion().getUnit(orderTempID) == null) {
                  tempUnit = unit.createTemp(gdata, orderTempID);
                  tempUnit.setSortIndex(++tempSortIndex);
                  if (line.size() > 4) {
                    tempUnit.addOrders(Collections.singleton(helper.getOrderTranslation(
                        AtlantisConstants.OC_NAME, unit)
                        + " "
                        + helper.getOrderTranslation(AtlantisConstants.OC_UNIT, unit)
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
              newOrders.add(line);
            }
          } else {
            if (ordersObject.isToken(line, 0, AtlantisConstants.OC_END)) {
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
  public Collection<? extends Order>
      getTempOrders(boolean writeUnitTagsAsVorlageComment, Unit unit) {
    final OrderParser parser = unit.getData().getOrderParser();
    final List<Order> cmds = new LinkedList<Order>();
    final Locale locale = unit.getLocale();

    for (TempUnit u : unit.tempUnits()) {
      cmds.add(getOrderO(locale, AtlantisConstants.OC_FORM, new Object[] { -u.getID().intValue() }));

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

      cmds.add(getOrderO(locale, AtlantisConstants.OC_END));
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
  public void setLongOrders(Unit u, List<String> orders, boolean replace) {
    if (replace) {
      u.setOrders(orders);
    } else {
      u.addOrders(orders);
    }
  }
}
