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

import magellan.library.Building;
import magellan.library.CoordinateID;
import magellan.library.Region;
import magellan.library.Rules;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.rules.RegionType;
import magellan.library.utils.Direction;
import magellan.library.utils.Regions;
import magellan.library.utils.Resources;


/**
 * This class inspects ships and checks for overload, missing crew and bad routes.
 * 
 */
public class ShipInspector extends AbstractInspector implements Inspector {

	/** The singleton instance of the ShipInspector */
	public static final ShipInspector INSPECTOR = new ShipInspector();

	/**
	 * Returns an instance of ShipInspector.
	 * 
	 * @return The singleton instance of ShipInspector
	 */
	public static ShipInspector getInstance() {
		return ShipInspector.INSPECTOR;
	}

	protected ShipInspector() {
	}

	/**
	 * Reviews the region for ships with problems. 
	 * 
	 * @see magellan.library.tasks.AbstractInspector#reviewRegion(magellan.library.Region, int)
	 */
	@Override
  public List<Problem> reviewRegion(Region r, int type) {
		// we notify errors only
		if (type != Problem.ERROR) {
			return Collections.emptyList();
		}

		// fail fast if prerequisites are not fulfilled
		if ((r == null) || (r.units() == null) || r.units().isEmpty()) {
			return Collections.emptyList();
		}

		// this inspector is only interested in ships
		if ((r.ships() == null) || r.ships().isEmpty()) {
			return Collections.emptyList();
		}

		List<Problem> problems = reviewShips(r);

		if (problems.isEmpty()) {
			return Collections.emptyList();
		} else {
			return problems;
		}
	}

	private List<Problem> reviewShips(Region r) {
		List<Problem> problems = new ArrayList<Problem>(2);

		for (Iterator<Ship> iter = r.ships().iterator(); iter.hasNext();) {
			Ship s = iter.next();
			problems.addAll(reviewShip(s));
		}

		return problems;
	}

	private List<Problem> reviewShip(Ship s) {
		List<Problem> problems = new ArrayList<Problem>();
		int nominalShipSize = s.getShipType().getMaxSize();
		
		// here we have all problems checked also for ships without a captn
		
    // also if not ready yet, there should be someone to take care..
    if (s.modifiedUnits().isEmpty()) {
      problems.add(new CriticizedError(s.getRegion(), s, this,
          Resources.get("tasks.shipinspector.error.nocrew.description")));
    }
    
		if (s.getSize() != nominalShipSize) {
			// ship will be built, so dont review ship
      // wrong! also while building someone should be on the ship!
			// return Collections.emptyList();
      // true is, tht we don�t go through the other checks
      return problems;
		}


		

		// problems.addAll(reviewMovingShip(s));
		// moving ships are taken care of while checking units... 
		return problems;
	}

	private List<Problem> reviewMovingShip(Ship ship) {
		List<Problem> problems = new ArrayList<Problem>();
		if (ship.getOwnerUnit() == null) {
			return problems;
		}

		List<CoordinateID> modifiedMovement = ship.getOwnerUnit().getModifiedMovement();

		if (modifiedMovement.isEmpty()) {
			return problems;
		}

		Iterator<CoordinateID> movementIterator = modifiedMovement.iterator();
		movementIterator.next();
		if (!movementIterator.hasNext()){
		  // this happens when we have some kind of movement and an startRegion
		  // but no next region
		  // example: ROUTE PAUSE NO
		  
		  // TODO That doesnt work anymore because iterator returns always next movement.
		  problems.add(new CriticizedError(ship.getRegion(), ship, this,
          Resources.get("tasks.shipinspector.error.nonextregion.description")));
      return problems;
		}
		CoordinateID nextRegionCoord = movementIterator.next();
		Region nextRegion = ship.getRegion().getData().getRegion(nextRegionCoord);
		
		// actually we have to check - is this really a ship (karawane's are also marked as ships but travel on land)
		boolean isShip = ship.getData().getGameSpecificStuff().getGameSpecificRules().isShip(ship);
		
		Rules rules = ship.getData().rules;
		RegionType ozean = rules.getRegionType("Ozean");

		// TODO: We should consider harbors, too. But this is difficult because we don't know if
		// harbor owner is allied with ship owner etc. We better leave it up to the user to decide...
		if (ship.getShoreId() != -1 && isShip) {
		  if (nextRegion!=null && !nextRegion.getRegionType().equals(ozean)) {
        problems.add(new CriticizedError(ship.getRegion(), ship, this,
            Resources.get("tasks.shipinspector.error.noocean.description")));
        return problems;
      }
			// If ship is shored, it can only move deviate by one from the shore direction and only
			// move to an ocean region
			Direction d = Regions.getDirectionObjectsOfCoordinates(modifiedMovement).get(0);
			if (Math.abs(ship.getShoreId() - d.getDir()) > 1 && Math.abs(ship.getShoreId() - d.getDir()) < 5) {
				if (!this.hasHarbourInRegion(ship.getRegion())){
          problems.add(new CriticizedError(ship.getRegion(), ship, this,
						Resources.get("tasks.shipinspector.error.wrongshore.description")));
        } else {
          // harbour in Region -> just warn, no error
          problems.add(new CriticizedWarning(ship.getRegion(), ship, this,
              Resources.get("tasks.shipinspector.error.wrongshore.harbour.description")));
        }
				return problems;
			}
			
			if (movementIterator.hasNext()) {
				nextRegionCoord = movementIterator.next();
				nextRegion = ship.getRegion().getData().getRegion(nextRegionCoord);
			} else {
        nextRegion = null;
      }
		}

		while (nextRegion != null) {
			// if ship is not a boat and on ocean , it can only move to ocean, plain or forest
      // FF 20071119: don�t forget Harbors
		  
		  // we have to check game specific stuff, because in Allanon a longboat can land everywhere too
		  // for that we should game specific rules 
		  if (isShip && !(ship.getData().getGameSpecificStuff().getGameSpecificRules().canLandInRegion(ship,nextRegion))) {
        if (!this.hasHarbourInRegion(nextRegion)){
          problems.add(new CriticizedError(ship.getRegion(), ship, this, Resources.get("tasks.shipinspector.error.shipwreck.description")));
          return problems;
        }
		  }
		  
			if (movementIterator.hasNext()) {
				nextRegionCoord = movementIterator.next();
				nextRegion = ship.getRegion().getData().getRegion(nextRegionCoord);
			} else {
        nextRegion = null;
      }
		}

		// overload
		if (ship.getModifiedLoad() > (ship.getMaxCapacity())) {
      problems.add(new CriticizedError(ship.getRegion(), ship, this,
          Resources.get("tasks.shipinspector.error.overloaded.description")));
    }
		
		
		return problems;
	}
  
  /**
   * checks, if an harbor is in the region and its size == maxSize
   * !checks no alliance status
   * @param nextRegion
   * @return
   */
  private boolean hasHarbourInRegion(Region nextRegion){
    if (nextRegion.buildings()!=null && nextRegion.buildings().size()>0){
      // check all buildings
      // i have no reference to the rules, do I?
      // so we have to check for Harbour by BuildingTypeName
      for (Iterator<Building> iter = nextRegion.buildings().iterator();iter.hasNext();){
        Building b = iter.next();
        if (b.getBuildingType().getName().equalsIgnoreCase("Hafen")){
          // lets check size
          if (b.getSize()>=b.getBuildingType().getMaxSize()){
            // ok...there is an harbour
            return true;
          }
        }
      }
    }
    return false;
  }
  
  
  /**
   * Reviews the region for ships with problems. 
   * 
   * @see magellan.library.tasks.AbstractInspector#reviewRegion(magellan.library.Region, int)
   */
  @Override
  public List<Problem> reviewUnit(Unit u, int type) {
    // we notify errors only
    if (type != Problem.ERROR) {
      return Collections.emptyList();
    }
    // we check for captns of ships
    UnitContainer UC = u.getModifiedUnitContainer();
    if (UC==null){
      return Collections.emptyList();
    }
    if (!(UC instanceof Ship)) {
      return Collections.emptyList();
    }
    if (UC.getOwnerUnit()==null || !UC.getOwner().equals(u)){
      return Collections.emptyList();
    }
    
    return reviewMovingShip((Ship)UC);
  }
  
  
  /**
   * @see magellan.library.tasks.Inspector#getSuppressComment()
   */
  public String getSuppressComment(){
    return null;
  }

}