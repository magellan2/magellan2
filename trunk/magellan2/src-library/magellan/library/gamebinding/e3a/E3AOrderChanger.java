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
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.EresseaOrderChanger;
import magellan.library.gamebinding.SimpleOrderFactory;

/**
 * OrderChanger class for the game Eressea -- The Third Age.
 */
public class E3AOrderChanger extends EresseaOrderChanger {

  protected E3AOrderChanger(Rules rules, SimpleOrderFactory factory) {
    super(rules, factory);
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
    String order = getOrderTranslation(EresseaConstants.OC_RECRUIT, unit) + " " + String.valueOf(i);
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

  private ArrayList<StringID> longOrderTokens;

  /**
   * Returns list of long order tokens in E3.
   *
   * @see magellan.library.gamebinding.EresseaOrderChanger#getLongOrderTokens()
   */
  @Override
  protected ArrayList<StringID> getLongOrderTokens() {
    if (longOrderTokens == null) {
      longOrderTokens = new ArrayList<StringID>();
      longOrderTokens.add(EresseaConstants.OC_WORK);
      longOrderTokens.add(EresseaConstants.OC_ATTACK);
      longOrderTokens.add(EresseaConstants.OC_STEAL);
      longOrderTokens.add(EresseaConstants.OC_SIEGE);
      longOrderTokens.add(EresseaConstants.OC_RIDE);
      longOrderTokens.add(EresseaConstants.OC_FOLLOW);
      longOrderTokens.add(EresseaConstants.OC_RESEARCH);
      longOrderTokens.add(EresseaConstants.OC_BUY);
      longOrderTokens.add(EresseaConstants.OC_TEACH);
      longOrderTokens.add(EresseaConstants.OC_LEARN);
      longOrderTokens.add(EresseaConstants.OC_MAKE);
      longOrderTokens.add(EresseaConstants.OC_MOVE);
      longOrderTokens.add(EresseaConstants.OC_PLANT);
      longOrderTokens.add(EresseaConstants.OC_ROUTE);
      longOrderTokens.add(EresseaConstants.OC_SABOTAGE);
      longOrderTokens.add(EresseaConstants.OC_SPY);
      longOrderTokens.add(EresseaConstants.OC_TAX);
      longOrderTokens.add(EresseaConstants.OC_ENTERTAIN);
      longOrderTokens.add(EresseaConstants.OC_SELL);
      longOrderTokens.add(EresseaConstants.OC_CAST);
      longOrderTokens.add(EresseaConstants.OC_GROW);
    }
    return longOrderTokens;
  }

}
