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

  public String getTokenLocalized(Locale orderLocale, Object arg) throws RulesException;

  public String getOrder(Locale orderLocale, StringID orderId);

  public String getOrder(Locale orderLocale, StringID orderId, Object[] args);

  public String getOrder(StringID orderId, Locale orderLocale) throws RulesException;

  public String getOrder(StringID orderId, Locale orderLocale, Object[] args) throws RulesException;

  public int extractTempUnits(GameData gdata, int tempSortIndex, Locale locale, Unit unit);

  public Collection<? extends Order> getTempOrders(boolean writeUnitTagsAsVorlageComment, Unit unit);

}
