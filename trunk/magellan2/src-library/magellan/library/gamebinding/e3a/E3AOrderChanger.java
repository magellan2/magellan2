/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.gamebinding.e3a;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import magellan.library.Rules;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.EresseaOrderChanger;

/**
 * OrderChanger class for the game Eressea -- The Third Age.
 */
public class E3AOrderChanger extends EresseaOrderChanger {

  protected E3AOrderChanger(Rules rules) {
    super(rules);
  }

  /**
   * E2K9 has no concept of stealth. So this changes nothing.
   */
  @Override
  public void addHideOrder(Unit unit, String level) {

  }

  /**
   * FIXME (stm) must honor primary and secondary races
   */
  @Override
  public void addRecruitOrder(Unit unit, int i) {
    String order = getOrderTranslation(EresseaConstants.O_RECRUIT, unit) + " " + String.valueOf(i);
    unit.addOrder(order);
  }

  /**
   * Just change number and description.
   * 
   * @param unit The affected unit.
   */
  @Override
  public void addMultipleHideOrder(Unit unit) {
    List<String> orders = new LinkedList<String>();
    orders.add(getOrderTranslation(EresseaConstants.O_NUMBER, unit) + " "
        + getOrderTranslation(EresseaConstants.O_UNIT, unit) + " ");
    orders.add(getOrderTranslation(EresseaConstants.O_NAME, unit) + " "
        + getOrderTranslation(EresseaConstants.O_UNIT, unit) + " \"\"");
    orders.add(getOrderTranslation(EresseaConstants.O_DESCRIBE, unit) + " "
        + getOrderTranslation(EresseaConstants.O_UNIT, unit) + " \"\"");
    orders.add(getOrderTranslation(EresseaConstants.O_HIDE, unit) + " "
        + getOrderTranslation(EresseaConstants.O_FACTION, unit));

    if (unit.getShip() != null) {
      orders.add(getOrderTranslation(EresseaConstants.O_NUMBER, unit) + " "
          + getOrderTranslation(EresseaConstants.O_SHIP, unit));
      orders.add(getOrderTranslation(EresseaConstants.O_NAME, unit) + " "
          + getOrderTranslation(EresseaConstants.O_SHIP, unit) + " \"\"");
      orders.add(getOrderTranslation(EresseaConstants.O_DESCRIBE, unit) + " "
          + getOrderTranslation(EresseaConstants.O_SHIP, unit) + " \"\"");
    }

    orders.add(PCOMMENTSTART + getOrderTranslation(EresseaConstants.O_NUMBER, unit) + " "
        + getOrderTranslation(EresseaConstants.O_UNIT, unit) + " " + unit.getID());
    orders.add(PCOMMENTSTART + getOrderTranslation(EresseaConstants.O_NAME, unit) + " "
        + getOrderTranslation(EresseaConstants.O_UNIT, unit) + " \"" + unit.getName() + "\"");

    if (unit.getDescription() != null) {
      orders.add(PCOMMENTSTART + getOrderTranslation(EresseaConstants.O_DESCRIBE, unit) + " "
          + getOrderTranslation(EresseaConstants.O_UNIT, unit) + " \"" + unit.getDescription()
          + "\"");
    }

    if (unit.getShip() != null) {
      orders.add(PCOMMENTSTART + getOrderTranslation(EresseaConstants.O_NUMBER, unit) + " "
          + getOrderTranslation(EresseaConstants.O_SHIP, unit) + " "
          + unit.getShip().getID().toString());
      orders.add(PCOMMENTSTART + getOrderTranslation(EresseaConstants.O_NAME, unit) + " "
          + getOrderTranslation(EresseaConstants.O_SHIP, unit) + " \"" + unit.getShip().getName()
          + "\"");

      if (unit.getShip().getDescription() != null) {
        orders.add(PCOMMENTSTART + getOrderTranslation(EresseaConstants.O_DESCRIBE, unit) + " "
            + getOrderTranslation(EresseaConstants.O_SHIP, unit) + " \""
            + unit.getShip().getDescription() + "\"");
      }
    }

    unit.addOrders(orders);
  }

  private ArrayList<String> longOrderTokens;

  /**
   * Returns list of long order tokens in E3.
   * 
   * @see magellan.library.gamebinding.EresseaOrderChanger#getLongOrderTokens()
   */
  @Override
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
   * @see magellan.library.gamebinding.EresseaOrderChanger#getLongButShortOrderTokens()
   */
  @Override
  protected ArrayList<String> getLongButShortOrderTokens() {
    if (longButShortOrderTokens == null) {
      longButShortOrderTokens = new ArrayList<String>();
      longButShortOrderTokens.add(EresseaConstants.O_MAKE + " " + EresseaConstants.O_TEMP);
    }
    return longButShortOrderTokens;
  }

}
