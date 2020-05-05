/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import magellan.library.Building;
import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Rules;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.MapMetric;
import magellan.library.rules.RegionType;
import magellan.library.tasks.Problem.Severity;
import magellan.library.utils.Direction;
import magellan.library.utils.Regions;
import magellan.library.utils.Units;

/**
 * This class inspects ships and checks for overload, missing crew and bad routes.
 */
public class ShipInspector extends AbstractInspector {

  enum ShipProblemTypes {
    EMPTY, EMPTY_FUSSY, CAPTAIN_FACTION, NOCAPTAIN, NOCONVOYCAPTAINS, NOCREW, NONEXTREGION, NOOCEAN,
    WRONGSHORE, WRONGSHOREHARBOUR, WRONGSHOREHARBOUR_INFO, SHIPWRECK, OVERLOADED;

    private ProblemType type;

    ShipProblemTypes() {
      String name = name().toLowerCase();
      type = ProblemType.create("tasks.shipinspector", name);
    }

    ProblemType getType() {
      return type;
    }
  }

  /**
   * Returns an instance of ShipInspector.
   *
   * @return The singleton instance of ShipInspector
   */
  public static ShipInspector getInstance(GameData data) {
    return new ShipInspector(data);
  }

  private Collection<ProblemType> types;

  protected ShipInspector(GameData data) {
    super(data);
  }

  /**
   * Reviews the region for ships with problems.
   *
   * @see AbstractInspector#listProblems(magellan.library.Region)
   */
  @Override
  public List<Problem> listProblems(Region r) {
    // fail fast if prerequisites are not fulfilled
    if (r == null)
      return Collections.emptyList();

    // this inspector is only interested in ships
    if ((r.ships() == null) || r.ships().isEmpty())
      return Collections.emptyList();

    List<Problem> problems = reviewShips(r);

    if (problems.isEmpty())
      return Collections.emptyList();
    else
      return problems;
  }

  private List<Problem> reviewShips(Region r) {
    List<Problem> problems = new ArrayList<Problem>(2);

    for (Ship s : r.ships()) {
      problems.addAll(reviewShip(s));
    }

    return problems;
  }

  private List<Problem> reviewShip(Ship s) {
    if (checkIgnoreUnitContainer(s))
      return Collections.emptyList();

    List<Problem> problems = new ArrayList<Problem>();
    int nominalShipSize = s.getShipType().getMaxSize() * s.getAmount();

    if (s.getModifiedAmount() == 0)
      return problems;

    // here we have all problems checked also for ships without a captain
    boolean empty = false;
    // also if not ready yet, there should be someone to take care..
    if (s.modifiedUnits().isEmpty()) {
      empty = true;
      if (s.getRegion().units().isEmpty()) {
        problems.add(ProblemFactory.createProblem(Severity.WARNING, ShipProblemTypes.EMPTY_FUSSY
            .getType(), s, this));
      } else {
        problems.add(ProblemFactory.createProblem(Severity.WARNING, ShipProblemTypes.EMPTY
            .getType(), s, this));
      }
    }

    if (s.getSize() != nominalShipSize)
      // ship will be built, so we don´t go through the other checks
      return problems;

    Unit newOwner = s.getModifiedOwnerUnit();

    if (!empty) {
      if (newOwner == null || Units.getCaptainSkillLevel(s) < s.getShipType()
          .getCaptainSkillLevel()) {
        problems.add(ProblemFactory.createProblem(Severity.WARNING, ShipProblemTypes.NOCAPTAIN
            .getType(), s, this));
      } else if (s.getModifiedAmount() > 1 && newOwner.getModifiedPersons() < s
          .getModifiedAmount()) {
        problems.add(ProblemFactory.createProblem(Severity.WARNING,
            ShipProblemTypes.NOCONVOYCAPTAINS.getType(), s, this));
      } else if (!Units.isPrivilegedAndNoSpy(newOwner)) {
        problems.add(ProblemFactory.createProblem(Severity.WARNING, ShipProblemTypes.CAPTAIN_FACTION
            .getType(), s, this));
      } else if (Units.getSailingSkillAmount(s) < s.getShipType().getSailorSkillLevel()) {
        problems.add(ProblemFactory.createProblem(Severity.WARNING, ShipProblemTypes.NOCREW
            .getType(), s, this));
      }
    }

    // moving ships are taken care of while checking units...
    // problems.addAll(reviewMovingShip(s));
    return problems;
  }

  private List<Problem> reviewMovingShip(Ship ship) {
    if (checkIgnoreUnitContainer(ship))
      return Collections.emptyList();

    if (intercept(null, null, null, ship))
      return Collections.emptyList();

    List<Problem> problems = new ArrayList<Problem>();
    Unit captain = ship.getModifiedOwnerUnit();
    if (captain == null || ship.getModifiedAmount() == 0)
      return problems;

    List<CoordinateID> modifiedMovement = captain.getModifiedMovement();

    if (modifiedMovement.isEmpty())
      return problems;

    Iterator<CoordinateID> movementIterator = modifiedMovement.iterator();
    // skip origin
    movementIterator.next();
    CoordinateID nextRegionCoord = movementIterator.next();
    Region nextRegion = getData().getRegion(nextRegionCoord);

    // actually we have to check - is this really a ship (caravans are also marked as ships but
    // travel on land)
    boolean isShip = getGameSpecificStuff().getGameSpecificRules().isShip(ship);

    Rules rules = getData().getRules();
    RegionType ozean = rules.getRegionType("Ozean");

    // We should consider harbors, too. But this is difficult because we don't know if
    // harbor owner is allied with ship owner etc. We better leave it up to the user to decide...
    if (ship.getShoreId() != -1 && isShip) {
      if (nextRegion != null && !nextRegion.getRegionType().equals(ozean)) {
        problems.add(ProblemFactory.createProblem(Severity.ERROR, ShipProblemTypes.NOOCEAN
            .getType(), ship, this));
        return problems;
      }
      // If ship is shored, it can only move deviate by one from the shore direction and only
      // move to an ocean region
      Direction d = Regions.getDirectionObjectsOfCoordinates(getData(), modifiedMovement).get(0);
      MapMetric mapMetric = getData().getGameSpecificStuff().getMapMetric();
      if (mapMetric.getDifference(d, mapMetric.toDirection(ship.getShoreId())) > 1 || mapMetric
          .getDifference(d, mapMetric.toDirection(ship.getShoreId())) < -1) {
        Unit owner = hasHarbourInRegion(ship.getRegion());
        if (owner == null) {
          problems.add(ProblemFactory.createProblem(Severity.ERROR, ShipProblemTypes.WRONGSHORE
              .getType(), ship, this));
        } else {
          if (Units.isAllied(captain.getFaction(), owner.getFaction(), EresseaConstants.A_GUARD)) {
            // harbour in Region -> just warn, no error
            problems.add(ProblemFactory.createProblem(Severity.INFORMATION,
                ShipProblemTypes.WRONGSHOREHARBOUR_INFO.getType(), ship, this));
          } else {
            // harbour in Region -> just warn, no error
            problems.add(ProblemFactory.createProblem(Severity.WARNING,
                ShipProblemTypes.WRONGSHOREHARBOUR.getType(), ship, this));
          }
        }
        return problems;
      }

      if (movementIterator.hasNext()) {
        nextRegionCoord = movementIterator.next();
        nextRegion = getData().getRegion(nextRegionCoord);
      } else {
        nextRegion = null;
      }
    }

    // loop until end of movement order or until first PAUSE
    for (Region lastRegion = null; movementIterator.hasNext() && (lastRegion == null
        || lastRegion != nextRegion); lastRegion = nextRegion, nextRegion = getData().getRegion(
            movementIterator.next())) {
      // check if next region is unknown or ship cannot land in next region and there is no harbor
      // we have to check game specific stuff, because in Allanon a longboat can
      // land everywhere, too
      if (isShip && (nextRegion == null || !(getGameSpecificStuff().getGameSpecificRules()
          .canLandInRegion(ship, nextRegion)))) {
        if (nextRegion == null || hasHarbourInRegion(nextRegion) == null || nextRegion
            .getRegionType().equals(RegionType.theVoid)) {
          problems.add(ProblemFactory.createProblem(Severity.ERROR, ShipProblemTypes.SHIPWRECK
              .getType(), ship, this));
          return problems;
        }
      }
    }

    // overload
    if (ship.getModifiedLoad() > (ship.getModifiedMaxCapacity())) {
      problems.add(ProblemFactory.createProblem(Severity.ERROR, ShipProblemTypes.OVERLOADED
          .getType(), ship, this));
    } else if (ship.getShipType().getMaxPersons() > 0 && ship.getModifiedPersonLoad() > (ship
        .getMaxPersons() * 1000)) {
      // persons overload
      problems.add(ProblemFactory.createProblem(Severity.ERROR, ShipProblemTypes.OVERLOADED
          .getType(), ship, this));
    }

    return problems;
  }

  /**
   * checks, if an harbor is in the region and its size == maxSize !checks no alliance status
   *
   * @param nextRegion
   * @return
   */
  private Unit hasHarbourInRegion(Region nextRegion) {
    if (nextRegion.buildings() != null && nextRegion.buildings().size() > 0) {
      // check all buildings
      // i have no reference to the rules, do I?
      // so we have to check for Harbour by BuildingTypeName
      for (Building b : nextRegion.buildings()) {
        if (b.getBuildingType().getName().equalsIgnoreCase("Hafen")) {
          // lets check size
          if (b.getSize() >= b.getBuildingType().getMaxSize())
            // ok...there is an harbour
            return b.getOwnerUnit();
        }
      }
    }
    return null;
  }

  /**
   * Reviews the region for ships with problems.
   *
   * @see AbstractInspector#listProblems(magellan.library.Region)
   */
  @Override
  public List<Problem> findProblems(Unit u) {
    // we check for captns of ships
    UnitContainer uc = u.getModifiedUnitContainer();
    if (uc == null)
      return Collections.emptyList();
    if (!(uc instanceof Ship))
      return Collections.emptyList();
    if (uc.getModifiedOwnerUnit() == null || !uc.getModifiedOwnerUnit().equals(u))
      return Collections.emptyList();

    return reviewMovingShip((Ship) uc);
  }

  public Collection<ProblemType> getTypes() {
    if (types == null) {
      types = new LinkedList<ProblemType>();
      for (ShipProblemTypes t : ShipProblemTypes.values()) {
        types.add(t.getType());
      }
    }
    return types;
  }
}
