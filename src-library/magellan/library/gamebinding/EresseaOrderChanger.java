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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import magellan.library.Building;
import magellan.library.Faction;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.rules.Race;
import magellan.library.utils.Resources;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 305 $
 */
public class EresseaOrderChanger implements OrderChanger {
  private static final EresseaOrderChanger singleton = new EresseaOrderChanger();
  
	protected EresseaOrderChanger() {
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static EresseaOrderChanger getSingleton() {
		return EresseaOrderChanger.singleton;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 */
	public void addNamingOrder(Unit unit, String name) {
		String order = createNamingOrder(name);
		unit.addOrder(order, true, 2);
	}

	private String createNamingOrder(String name) {
		return Resources.getOrderTranslation(EresseaConstants.O_NAME) + " " +
    Resources.getOrderTranslation(EresseaConstants.O_UNIT) + " \"" + name + "\"";
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 * 
	 */
	public void addNamingOrder(Unit unit, UnitContainer uc, String name) {
		String order = createNamingOrder(uc, name);
		unit.addOrder(order, true, 2);
	}

	private String createNamingOrder(UnitContainer uc, String name) {
		String order = null;

		if(uc instanceof Building) {
			order = Resources.getOrderTranslation(EresseaConstants.O_CASTLE);
		} else if(uc instanceof Ship) {
			order = Resources.getOrderTranslation(EresseaConstants.O_SHIP);
		} else if(uc instanceof Region) {
			order = Resources.getOrderTranslation(EresseaConstants.O_REGION);
		} else if(uc instanceof Faction) {
			order = Resources.getOrderTranslation(EresseaConstants.O_FACTION);
		}

		return Resources.getOrderTranslation(EresseaConstants.O_NAME) + " " + order + " \"" + name + "\"";
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 * 
	 */
	public void addDescribeUnitContainerOrder(Unit unit, UnitContainer uc, String descr) {
		String suborder = createDescribeUnitContainerOrder(uc);
		String order = suborder + " \"" + descr + "\"";
		unit.addOrder(order, true, (suborder.indexOf(" ") >= 0) ? 2 : 1);
	}

	private String createDescribeUnitContainerOrder(UnitContainer uc) {
		String order = null;

		if(uc instanceof Building) {
			order = Resources.getOrderTranslation(EresseaConstants.O_DESCRIBE) + " " +
      Resources.getOrderTranslation(EresseaConstants.O_CASTLE);
		} else if(uc instanceof Ship) {
			order = Resources.getOrderTranslation(EresseaConstants.O_DESCRIBE) + " " +
      Resources.getOrderTranslation(EresseaConstants.O_SHIP);
		} else if(uc instanceof Region) {
			order = Resources.getOrderTranslation(EresseaConstants.O_DESCRIBE) + " " +
      Resources.getOrderTranslation(EresseaConstants.O_REGION);
		} else if(uc instanceof Faction) {
			order = Resources.getOrderTranslation(EresseaConstants.O_BANNER);
		}

		return order;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 */
	public void addDescribeUnitPrivateOrder(Unit unit, String descr) {
		String order = createDescribeUnitPrivateOrder(descr);
		unit.addOrder(order, true, 2);
	}

	private String createDescribeUnitPrivateOrder(String descr) {
		return Resources.getOrderTranslation(EresseaConstants.O_DESCRIBE) + " " +
    Resources.getOrderTranslation(EresseaConstants.O_PRIVATE) + " \"" + descr + "\"";
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 */
	public void addDescribeUnitOrder(Unit unit, String descr) {
		String order = createDescribeUnitOrder(descr);
		unit.addOrder(order, true, 2);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public String createDescribeUnitOrder(String descr) {
		return Resources.getOrderTranslation(EresseaConstants.O_DESCRIBE) + " " +
    Resources.getOrderTranslation(EresseaConstants.O_UNIT) + " \"" + descr + "\"";
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 */
	public void addHideOrder(Unit unit, String level) {
		Collection<String> orders = new ArrayList<String>();
		orders.addAll(unit.getOrders());

		// remove hide (but not hide faction) order
		for(Iterator<String> iter = orders.iterator(); iter.hasNext();) {
			String order = iter.next();

			if(order.startsWith(Resources.getOrderTranslation(EresseaConstants.O_HIDE)) &&
				   (order.indexOf(Resources.getOrderTranslation(EresseaConstants.O_FACTION)) == -1)) {
				boolean raceFound = false;

				for(Iterator it2 = unit.getRegion().getData().rules.getRaceIterator();
						it2.hasNext();) {
					Race race = (Race) it2.next();

					if(order.indexOf(race.getName()) > 0) {
						raceFound = true;

						break;
					}
				}

				if(!raceFound) {
					iter.remove();
				}
			}
		}

		orders.add(createHideOrder(level));
		unit.setOrders(orders);
	}

	private String createHideOrder(String level) {
		return Resources.getOrderTranslation(EresseaConstants.O_HIDE) + " " + level;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 */
	public void addCombatOrder(Unit unit, int newState) {
		String order = getCombatOrder(unit, newState);
		unit.addOrder(order, true, 1);
	}

	private String getCombatOrder(Unit unit, int newState) {
		String str = Resources.getOrderTranslation(EresseaConstants.O_COMBAT) + " ";

		switch(newState) {
		case 0:
			str += Resources.getOrderTranslation(EresseaConstants.O_AGGRESSIVE);

			break;

		case 1:
			str += Resources.getOrderTranslation(EresseaConstants.O_FRONT);

			break;

		case 2:
			str += Resources.getOrderTranslation(EresseaConstants.O_REAR);

			break;

		case 3:
			str += Resources.getOrderTranslation(EresseaConstants.O_DEFENSIVE);

			break;

		case 4:
			str += Resources.getOrderTranslation(EresseaConstants.O_NOT);

			break;

		case 5:
			str += Resources.getOrderTranslation(EresseaConstants.O_FLEE);

			break;

		default:
			break;
		}

		return str;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 */
	public void addRecruitOrder(Unit unit, int i) {
		String order = Resources.getOrderTranslation(EresseaConstants.O_RECRUIT) + " " +
					   String.valueOf(i);
		unit.addOrders(order);
	}

	/**
	 * Adds camouflage orders, for hiding all that could identify the unit and remembering the old values in comments.
	 *  
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

		if(!unit.isHideFaction()) {
			orders.add("// " + Resources.getOrderTranslation(EresseaConstants.O_HIDE) + " " +
          Resources.getOrderTranslation(EresseaConstants.O_FACTION) + " " +
          Resources.getOrderTranslation(EresseaConstants.O_NOT));
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
}
