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

package magellan.library.gamebinding.e3a;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import magellan.library.Rules;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.EresseaOrderChanger;
import magellan.library.utils.Resources;


/**
 *
 * @author $Author: $
 * @version $Revision: 305 $
 */
public class E3AOrderChanger extends EresseaOrderChanger {
  
	protected E3AOrderChanger(Rules rules) {
	  super(rules);
	}


	/**
	 * E2K9 has no concept of stealth. So this changes nothing. 
	 * 
	 */
	public void addHideOrder(Unit unit, String level) {
	
	}

	/**
	 * FIXME (stm) must honour primary and secondary races
	 */
	public void addRecruitOrder(Unit unit, int i) {
		String order = Resources.getOrderTranslation(EresseaConstants.O_RECRUIT) + " " +
					   String.valueOf(i);
		unit.addOrders(order);
	}

	/**
   * Just change number and description.
	 *
	 * @param unit The affected unit.
	 */
	public void addMultipleHideOrder(Unit unit) {
		List<String> orders = new LinkedList<String>();
		orders.add(Resources.getOrderTranslation(EresseaConstants.O_NUMBER) + " " +
        Resources.getOrderTranslation(EresseaConstants.O_UNIT) + " ");
		orders.add(Resources.getOrderTranslation(EresseaConstants.O_NAME) + " " +
        Resources.getOrderTranslation(EresseaConstants.O_UNIT) + " \"\"");
		orders.add(Resources.getOrderTranslation(EresseaConstants.O_DESCRIBE) + " " +
        Resources.getOrderTranslation(EresseaConstants.O_UNIT) + " \"\"");
		orders.add(Resources.getOrderTranslation(EresseaConstants.O_HIDE) + " " +
        Resources.getOrderTranslation(EresseaConstants.O_FACTION));

		if(unit.getShip() != null) {
			orders.add(Resources.getOrderTranslation(EresseaConstants.O_NUMBER) + " " +
          Resources.getOrderTranslation(EresseaConstants.O_SHIP));
			orders.add(Resources.getOrderTranslation(EresseaConstants.O_NAME) + " " +
          Resources.getOrderTranslation(EresseaConstants.O_SHIP) + " \"\"");
			orders.add(Resources.getOrderTranslation(EresseaConstants.O_DESCRIBE) + " " +
          Resources.getOrderTranslation(EresseaConstants.O_SHIP) + " \"\"");
		}

		orders.add("// " + Resources.getOrderTranslation(EresseaConstants.O_NUMBER) + " " +
        Resources.getOrderTranslation(EresseaConstants.O_UNIT) + " " + unit.getID());
		orders.add("// " + Resources.getOrderTranslation(EresseaConstants.O_NAME) + " " +
        Resources.getOrderTranslation(EresseaConstants.O_UNIT) + " \"" +
				   unit.getName() + "\"");

		if(unit.getDescription() != null) {
			orders.add("// " + Resources.getOrderTranslation(EresseaConstants.O_DESCRIBE) + " " +
          Resources.getOrderTranslation(EresseaConstants.O_UNIT) + " \"" +
					   unit.getDescription() + "\"");
		}

		if(unit.getShip() != null) {
			orders.add("// " + Resources.getOrderTranslation(EresseaConstants.O_NUMBER) + " " +
          Resources.getOrderTranslation(EresseaConstants.O_SHIP) + " " +
					   unit.getShip().getID().toString());
			orders.add("// " + Resources.getOrderTranslation(EresseaConstants.O_NAME) + " " +
          Resources.getOrderTranslation(EresseaConstants.O_SHIP) + " \"" +
					   unit.getShip().getName() + "\"");

			if(unit.getShip().getDescription() != null) {
				orders.add("// " + Resources.getOrderTranslation(EresseaConstants.O_DESCRIBE) +
						   " " + Resources.getOrderTranslation(EresseaConstants.O_SHIP) + " \"" +
						   unit.getShip().getDescription() + "\"");
			}
		}

		unit.addOrders(orders);
	}
	
	/**
	 * FIXME not implemented
	 * 
   * @see magellan.library.gamebinding.OrderChanger#disableLongOrders(magellan.library.Unit)
   */
  public void disableLongOrders(Unit u) {
//    LinkedList<String> newOrders = new LinkedList<String>();
    LinkedList<String> oldOrders = new LinkedList<String>();
    if (u.getOrders()!=null && u.getOrders().size()>0){
      oldOrders.addAll(u.getOrders());
    }
    if (oldOrders.size()>0){
//      for (String s:oldOrders){
//           
//      }
    }
  }

  private ArrayList<String> longOrders = null;
  
  /**
   * list of long orders in Eressea
   * @return
   */
  protected ArrayList<String> getLongOrders(){
    if (this.longOrders==null){
      this.longOrders = new ArrayList<String>();
      this.longOrders.add(EresseaConstants.O_WORK);
      this.longOrders.add(EresseaConstants.O_ATTACK);
      this.longOrders.add(EresseaConstants.O_STEAL);
      this.longOrders.add(EresseaConstants.O_SIEGE);
      this.longOrders.add(EresseaConstants.O_RIDE);
      this.longOrders.add(EresseaConstants.O_FOLLOW);
      this.longOrders.add(EresseaConstants.O_RESEARCH);
      this.longOrders.add(EresseaConstants.O_BUY);
      this.longOrders.add(EresseaConstants.O_TEACH);
      this.longOrders.add(EresseaConstants.O_LEARN);
      this.longOrders.add(EresseaConstants.O_MAKE);
      this.longOrders.add(EresseaConstants.O_MOVE);
      this.longOrders.add(EresseaConstants.O_PLANT);
      this.longOrders.add(EresseaConstants.O_ROUTE);
      this.longOrders.add(EresseaConstants.O_SABOTAGE);
      this.longOrders.add(EresseaConstants.O_SPY);
      this.longOrders.add(EresseaConstants.O_TAX);
      this.longOrders.add(EresseaConstants.O_ENTERTAIN);
      this.longOrders.add(EresseaConstants.O_SELL);
      this.longOrders.add(EresseaConstants.O_CAST);
      this.longOrders.add(EresseaConstants.O_GROW);
    }
    return this.longOrders;
  }
	
  private ArrayList<String> longButShortOrders = null;
  
  /**
   * list of orders, which could be identified as long, but in
   * the listed form are short orders..
   * make temp = short (in this list)
   * make sword = long (not in this list)
   * @return
   */
  protected ArrayList<String> getLongButShortOrders(){
    if (this.longButShortOrders==null){
      this.longButShortOrders = new ArrayList<String>();
      this.longButShortOrders.add(EresseaConstants.O_MAKE + " " + EresseaConstants.O_TEMP);
    }
    return this.longButShortOrders;
  }
  
  
  
}
