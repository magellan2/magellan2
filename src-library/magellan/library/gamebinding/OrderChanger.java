/*
 *  Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe,
 *                          Stefan Goetz, Sebastian Pappert,
 *                          Klaas Prause, Enno Rehling,
 *                          Sebastian Tusk, Ulrich Kuester,
 *                          Ilja Pavkovic
 *
 * This file is part of the Eressea Java Code Base, see the
 * file LICENSING for the licensing information applying to
 * this file.
 *
 */

package magellan.library.gamebinding;

import java.util.Collection;
import java.util.Locale;

import magellan.library.GameData;
import magellan.library.Order;
import magellan.library.Orders;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.UnitContainer;

/**
 * This class has methods to change unit orders for various purposes, usually related to higher
 * order user operations.
 * 
 * @author $Author: $
 * @version $Revision: 305 $
 */
public interface OrderChanger {

  /**
   * Turns line of text into an Order object.
   */
  public Order createOrder(Unit unit, String order);

  /**
   * Adds a KÄMPFE order. TODO: state are defined... somewhere
   */
  public void addCombatOrder(Unit unit, int newstate);

  /**
   * Adds a command line "DESCRIBE uc \"descr\"" ("BESCHREIBE uc \"descr\"") , e.g. "DESCRIBE SHIP
   * \"A wonderful small boat.\"" ("BESCHREIBE SCHIFF \"Ein wundervolles kleines Boot.\"") to the
   * given unit. See EMapDetailsPanel.
   */
  public void addDescribeUnitContainerOrder(Unit unit, UnitContainer uc, String descr);

  /**
   * Adds a command line "DESCRIBE UNIT \"descr\"" ("BESCHREIBE EINHEIT \"descr\"") , e.g.
   * "DESCRIBE UNIT \"A wonderful sailor.\"" ("BESCHREIBE EINHEIT \"Ein wundervoller Segler.\"") to
   * the given unit. See EMapDetailsPanel.
   */
  public void addDescribeUnitOrder(Unit unit, String descr);

  /**
   * Adds a command line "DESCRIBE PRIVATE \"descr\"" ("BESCHREIBE PRIVAT \"descr\"") , e.g.
   * "DESCRIBE PRIVATE \"My spy!.\"" ("BESCHREIBE PRIVAT \"Mein Spion!\"") to the given unit. See
   * EMapDetailsPanel.
   */
  public void addDescribeUnitPrivateOrder(Unit unit, String descr);

  /**
   * Adds a command line "HIDE newstate" ("TARNE newstate") , e.g. "HIDE 3" ("TARNE 3") to the given
   * unit. See EMapDetailsPanel.
   */
  public void addHideOrder(Unit unit, String level);

  /**
   * Adds a command line "GROUP \"name\"" ("GRUPPE \"name\"") , e.g. "GROUP \"Magellan.\""
   * ("GRUPPE \"Magellan.\"") to the given unit. See EMapDetailsPanel.
   */
  public void addGroupOrder(Unit unit, String name);

  /**
   * Adds a command line "NAME UNIT \"name\"" ("BENENNE EINHEIT \"name\"") , e.g. "NAME UNIT
   * \"Magellan.\"" ("BENENNE EINHEIT \"Magellan.\"") to the given unit. See EMapDetailsPanel.
   */
  public void addNamingOrder(Unit unit, String name);

  /**
   * Adds a command line "NAME uc \"name\"" ("BENENNE uc \"name\"") , e.g. "NAME SHIP \"Santa
   * Barbara.\"" ("BENENNE SCHIFF \"Santa Barbara.\"") to the given unit. See EMapDetailsPanel.
   */
  public void addNamingOrder(Unit unit, UnitContainer uc, String name);

  /**
   * Adds a REKRUTIERE amount order.
   */
  public void addRecruitOrder(Unit u, int amount);

  /** Code for ALL in commands like GIVE xyz ALL item, used in addGiveOrder. */
  public static final int ALL = Integer.MAX_VALUE;

  /**
   * Adds a GIVE target amount item order.
   * 
   * @param amount use ALL for all, negative amount for each
   */
  public void addGiveOrder(Unit source, Unit target, int amount, StringID item, String comment);

  // for UnitContextMenu
  /**
   * Adds command lines for hiding all that could identify this unit, like name, number, description
   * etc.
   * 
   * @param u The affected unit.
   */
  public void addMultipleHideOrder(Unit u);

  /**
   * searches the orders of the unit for long orders and comments them out
   * 
   * @param u
   */
  public void disableLongOrders(Unit u);

  /**
   * checks, if the given order is a long order
   * 
   * @param order
   * @return true if the given order is a long order
   */
  public boolean isLongOrder(String order);

  /**
   * checks, if the given order is a long order
   * 
   * @param order
   * @return true if the given order is a long order
   */
  public boolean isLongOrder(Order order);

  /**
   * Returns true if the orders in the collection are legal to have at the same time for one unit.
   * 
   * @param orders
   * @return The first offending order
   */
  public int areCompatibleLongOrders(Orders orders);

  /**
   * Returns localized string for given locale and argument.
   * 
   * @param orderLocale
   * @param arg can be an order ID, a unit ID or anything else
   * @throws RulesException if argument has no translation
   */
  public String getTokenLocalized(Locale orderLocale, Object arg) throws RulesException;

  /**
   * Returns localized order for given order id
   * 
   * @param orderId an order ID, usually one of the EresseaConstants.OC_... constants.
   */
  public String getOrder(Locale orderLocale, StringID orderId);

  /**
   * Returns a localized order for given order id concatenated (separated by spaces) with the
   * argument translations.
   * 
   * @param orderId an order ID, usually one of the EresseaConstants.OC_... constants.
   * @param args Additional arguments
   * @return The order
   */
  public String getOrder(Locale orderLocale, StringID orderId, Object[] args);

  /**
   * Returns localized order for given order id
   * 
   * @param orderId an order ID, usually one of the EresseaConstants.OC_... constants.
   * @throws RulesException if argument has no translation
   */
  public String getOrder(StringID orderId, Locale orderLocale) throws RulesException;

  /**
   * Returns a localized order for given order id concatenated (separated by spaces) with the
   * argument translations.
   * 
   * @param orderId an order ID, usually one of the EresseaConstants.OC_... constants.
   * @param args Additional arguments
   * @return The order
   * @throws RulesException if argument has no translation
   */
  public String getOrder(StringID orderId, Locale orderLocale, Object[] args) throws RulesException;

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
  public int extractTempUnits(GameData gdata, int tempSortIndex, Locale locale, Unit unit);

  /**
   * Returns the orders necessary to issue the creation of all the child temp units of this unit.
   */
  public Collection<? extends Order> getTempOrders(boolean writeUnitTagsAsVorlageComment, Unit unit);

}
