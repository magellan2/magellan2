// class magellan.library.gamebinding.OrderTranslationHelper
// created on Jan 10, 2015
//
// Copyright 2003-2015 by magellan project team
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import magellan.library.Order;
import magellan.library.Rules;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.rules.OrderType;
import magellan.library.utils.Locales;
import magellan.library.utils.OrderToken;
import magellan.library.utils.logging.Logger;

/**
 * Provides some common functions for creating translations
 *
 * @author stm
 */
public class OrderTranslationHelper {
  private static final Object[] EMPTY = new Object[0];
  private Rules rules;
  private SimpleOrderFactory orderFactory;
  private StringID tempOrderId;

  public OrderTranslationHelper(Rules rules, SimpleOrderFactory factory, StringID tempId) {
    this.rules = rules;
    orderFactory = factory;
    tempOrderId = tempId;
  }

  /**
   * Returns the order with the given id, localized for the unit's order locale.
   *
   * @see OrderType#getName(Locale)
   */
  public Order getOrderTranslation(StringID id, Unit unit) {
    return getOrder(unit.getLocale(), id);
  }

  /**
   * @see magellan.library.gamebinding.OrderChanger#getTokenLocalized(java.util.Locale,
   *      java.lang.Object)
   */
  public String getTokenLocalized(Locale orderLocale, Object arg) throws RulesException {
    if (arg instanceof StringID)
      return getOneTokenOrder((StringID) arg, orderLocale);
    else if (arg instanceof UnitID)
      if (((UnitID) arg).intValue() < 0)
        return getTemp(orderLocale) + " " + arg.toString();
      else
        return arg.toString();
    else
      return arg.toString();
  }

  /**
   * @see magellan.library.gamebinding.OrderChanger#getOrderO(java.util.Locale,
   *      magellan.library.StringID)
   */
  public Order getOrder(Locale orderLocale, StringID orderId) {
    try {
      return getOrder(orderId, orderLocale, EMPTY);
    } catch (RulesException e) {
      return orderFactory.getInstance(orderId.toString());
    }
  }

  /**
   * @see magellan.library.gamebinding.OrderChanger#getOrderO(java.util.Locale,
   *      magellan.library.StringID, java.lang.Object[])
   */
  public Order getOrder(Locale orderLocale, StringID orderId, Object[] args) {
    try {
      return getOrderImpl(orderId, orderLocale, true, args);
    } catch (RulesException e) {
      throw new RuntimeException("this cannot happen");
    }
  }

  /**
   * @see magellan.library.gamebinding.OrderChanger#getOrderO(magellan.library.StringID,
   *      java.util.Locale)
   */
  public Order getOrder(StringID orderId, Locale orderLocale) throws RulesException {
    return getOrder(orderId, orderLocale, EMPTY);
  }

  /**
   * @see magellan.library.gamebinding.OrderChanger#getOrderO(magellan.library.StringID,
   *      java.util.Locale, java.lang.Object[])
   */
  public Order getOrder(StringID orderId, Locale orderLocale, Object[] args) throws RulesException {
    return getOrderImpl(orderId, orderLocale, false, args);
  }

  protected Order getOrderImpl(StringID orderId, Locale orderLocale, boolean fix, Object[] args)
      throws RulesException {
    String text;
    try {
      text = getOneTokenOrder(orderId, orderLocale);
    } catch (RulesException e2) {
      if (fix) {
        text = orderId.toString();
      } else
        throw e2;
    }
    if (args.length > 0) {
      StringBuilder order = new StringBuilder(text);
      List<OrderToken> tokens = new ArrayList<OrderToken>();
      tokens.add(new OrderToken(text));
      for (Object arg : args) {
        String tok;
        try {
          tok = getTokenLocalized(orderLocale, arg);
        } catch (RulesException e2) {
          if (fix) {
            tok = arg.toString();
          } else
            throw e2;
        }
        if (tok.length() > 0) {
          order.append(" ").append(tok);
          tokens.add(new OrderToken(tok));
        }
      }
      return orderFactory.getInstance(tokens, order.toString());
    } else
      return orderFactory.getInstance(text);
  }

  private String getOneTokenOrder(StringID orderId, Locale orderLocale) throws RulesException {
    OrderType order = getRules().getOrder(orderId);
    if (order == null)
      throw new RulesException("unknown order " + orderId);
    if (orderLocale == null) {
      orderLocale = Locales.getOrderLocale();
      Logger.getInstance(this.getClass()).fine("locale null", new RuntimeException());
    }
    String name = order.getName(orderLocale);
    if (name == null)
      throw new RulesException("no translation for " + orderId + " into " + orderLocale);
    return name;
  }

  private Rules getRules() {
    return rules;
  }

  protected String getTemp(Locale locale) {
    return getOrder(locale, tempOrderId).getText();
  }

}
