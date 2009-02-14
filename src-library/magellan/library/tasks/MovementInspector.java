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

package magellan.library.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import magellan.library.CoordinateID;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.utils.Resources;


/**
 * Checks land movement for overload or too many horses.
 * 
 */
public class MovementInspector extends AbstractInspector implements Inspector {
	/** The singleton instance of this Inspector */
	public static final MovementInspector INSPECTOR = new MovementInspector();

	/**
	 * Returns an instance this inspector.
	 * 
	 * @return The singleton instance of MovementInspector
	 */
	public static MovementInspector getInstance() {
		return MovementInspector.INSPECTOR;
	}

	protected MovementInspector() {
	}

	/**
	 * Checks the specified movement for overload and too many horses.
	 * 
	 * @see magellan.library.tasks.AbstractInspector#reviewUnit(magellan.library.Unit, int)
	 */
	@Override
  public List<Problem> reviewUnit(Unit u, int type) {
		if ((u == null) || u.ordersAreNull()) {
			return Collections.emptyList();
		}

		// we only warn
		if (type != Problem.WARNING) {
			return Collections.emptyList();
		}

		List<Problem> problems = new ArrayList<Problem>();

		if (!u.getModifiedMovement().isEmpty() || hasMovementOrder(u)) {
			// only test for foot/horse movement if unit is not owner of a modified ship
			if ((u.getModifiedShip() == null) || !u.equals(u.getModifiedShip().getOwnerUnit())) {
				problems.addAll(reviewUnitOnFoot(u));
				if (u.getModifiedMovement().size()>0){
				  Iterator<CoordinateID> it = u.getModifiedMovement().iterator();
				  CoordinateID last = it.next();
				  for (int count = 1; count <3 && it.hasNext(); ++count){
				    CoordinateID current = it.next();
				    if (current.equals(last)) break;
				    if (count > 1) {
		          problems.addAll(reviewUnitOnHorse(u));
		          break;
				    }
				    last = current;
				  }
        }
			}
		}

		for (String order : u.getOrders()) {
		  try {
		    if (order.trim().startsWith(Resources.getOrderTranslation(EresseaConstants.O_FOLLOW))){
		      StringTokenizer st = new StringTokenizer(order.trim());
		      st.nextToken();
		      if (Resources.getOrderTranslation(EresseaConstants.O_UNIT).equals(st.nextToken())){
		        if  (UnitID.createUnitID(st.nextToken(), u.getRegion().getData().base).equals(u.getID())){
		          problems.add(new CriticizedError(u, u, this, Resources.get("tasks.movementinspector.error.unitfollowsself.description")));
		        }
		      }
		    }
		  } catch (Exception e){
		  }
		}
    // TODO: check for movement length
		// TODO: check for roads

		/*
		switch(u.getRadius()) {
		case 0:
		    problems.add(new CriticizedWarning(u, this, "Cannot move, radius is on "+u.getRadius()+"!"));
		case 1:
		    problems.add(new CriticizedWarning(u, this, "Cannot ride, radius is on "+u.getRadius()+"!"));
		default:
		    ;
		}
		*/
		if(problems.isEmpty()) {
			return Collections.emptyList();
		} else {
			return problems;
		}
	}

	private boolean hasMovementOrder(Unit u) {
	  for (String order : u.getOrders()) {
	    if (order.trim().startsWith(Resources.getOrderTranslation(EresseaConstants.O_MOVE))
	        || order.trim().startsWith(Resources.getOrderTranslation(EresseaConstants.O_ROUTE)))
	      return true;
	    try {
	      if (order.trim().startsWith(Resources.getOrderTranslation(EresseaConstants.O_FOLLOW))){
	        StringTokenizer st = new StringTokenizer(order.trim());
	        st.nextToken();
	        if (Resources.getOrderTranslation(EresseaConstants.O_UNIT).equals(st.nextToken())){
	          return true;
	        }
	      }
	    } catch (Exception e){

	    }
    }
    return false;
  }

  private List<Problem> reviewUnitOnFoot(Unit u) {
		int maxOnFoot = u.getPayloadOnFoot();

		if (maxOnFoot == Unit.CAP_UNSKILLED) {
			return Collections.singletonList((Problem)(new CriticizedWarning(u, u, this, Resources.get("tasks.movementinspector.error.toomanyhorsesfoot.description"))));
		}

		int modLoad = u.getModifiedLoad();

		if ((maxOnFoot - modLoad) < 0) {
			return Collections.singletonList((Problem)(new CriticizedWarning(u, u, this, Resources.get("tasks.movementinspector.error.footoverloaded.description"))));
		}

		return Collections.emptyList();
	}

	private List<Problem> reviewUnitOnHorse(Unit u) {
		int maxOnHorse = u.getPayloadOnHorse();

		if (maxOnHorse == Unit.CAP_UNSKILLED) {
			return Collections.singletonList((Problem)(new CriticizedWarning(u, u, this,
          Resources.get("tasks.movementinspector.error.toomanyhorsesride.description"))));
		}

		if (maxOnHorse != Unit.CAP_NO_HORSES) {
			int modLoad = u.getModifiedLoad();

			if ((maxOnHorse - modLoad) < 0) {
				return Collections.singletonList((Problem)(new CriticizedWarning(u, u, this,
            Resources.get("tasks.movementinspector.error.horseoverloaded.description"))));
			}
		}
		// FIXME if unit has no horses, we should report an error

		return Collections.emptyList();
	}
}
