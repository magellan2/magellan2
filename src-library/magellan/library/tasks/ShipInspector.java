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
import magellan.library.relation.ControlRelation;
import magellan.library.relation.UnitRelation;
import magellan.library.rules.RegionType;
import magellan.library.tasks.Problem.Severity;
import magellan.library.utils.Direction;
import magellan.library.utils.Regions;
import magellan.library.utils.Resources;
import magellan.library.utils.Units;

/**
 * This class inspects ships and checks for overload, missing crew and bad routes.
 */
public class ShipInspector extends AbstractInspector {

  /** The singleton instance of the ShipInspector */
  // public static final ShipInspector INSPECTOR = new ShipInspector();

  enum ShipProblemTypes {
    EMPTY, NOCREW, NONEXTREGION, NOOCEAN, WRONGSHORE, WRONGSHOREHARBOUR, SHIPWRECK, OVERLOADED;

    private ProblemType type;

    ShipProblemTypes() {
      String name = name().toLowerCase();
      String message = Resources.get("tasks.shipinspector." + name + ".message");
      String typeName = Resources.get("tasks.shipinspector." + name + ".name", false);
      if (typeName == null) {
        typeName = message;
      }
      String description = Resources.get("tasks.shipinspector." + name + ".description", false);
      String group = Resources.get("tasks.shipinspector." + name + ".group", false);
      type = new ProblemType(typeName, group, description, message);
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
   * @see magellan.library.tasks.AbstractInspector#reviewRegion(magellan.library.Region, Severity)
   */
  @Override
  public List<Problem> reviewRegion(Region r, Severity severity) {
    // we notify errors only
    if (severity != Severity.ERROR)
      return Collections.emptyList();

    // fail fast if prerequisites are not fulfilled
    if ((r == null) || (r.units() == null) || r.units().isEmpty())
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
    int nominalShipSize = s.getShipType().getMaxSize();

    // here we have all problems checked also for ships without a captain
    boolean empty = false;
    // also if not ready yet, there should be someone to take care..
    if (s.modifiedUnits().isEmpty()) {
      empty = true;
      problems.add(ProblemFactory.createProblem(Severity.ERROR, ShipProblemTypes.EMPTY.getType(),
          s, this));
    }

    if (s.getSize() != nominalShipSize)
      // ship will be built, so we don´t go through the other checks
      return problems;

    Unit owner = s.getOwnerUnit();
    // the problem also belongs to the faction of the new owner...
    Unit newOwner = null;
    if (owner != null) {
      for (UnitRelation u : owner.getRelations(ControlRelation.class)) {
        if (u instanceof ControlRelation) {
          ControlRelation ctr = (ControlRelation) u;
          if (u.source == owner) {
            newOwner = ctr.target;
          }
        }
      }
    }

    if ((!empty && ((owner != null && Units.isPrivilegedAndNoSpy(owner)) || (newOwner != null && Units
        .isPrivilegedAndNoSpy(newOwner))))
        && (Units.getCaptainSkillAmount(s) < s.getShipType().getCaptainSkillLevel() || Units
            .getSailingSkillAmount(s) < s.getShipType().getSailorSkillLevel())) {
      problems.add(ProblemFactory.createProblem(Severity.WARNING,
          ShipProblemTypes.NOCREW.getType(), s, this));
    }

    // moving ships are taken care of while checking units...
    // problems.addAll(reviewMovingShip(s));
    return problems;
  }

  private List<Problem> reviewMovingShip(Ship ship) {
    if (checkIgnoreUnitContainer(ship))
      return Collections.emptyList();

    List<Problem> problems = new ArrayList<Problem>();
    if (ship.getModifiedOwnerUnit() == null)
      return problems;

    List<CoordinateID> modifiedMovement = ship.getModifiedOwnerUnit().getModifiedMovement();

    if (modifiedMovement.isEmpty())
      return problems;

    Iterator<CoordinateID> movementIterator = modifiedMovement.iterator();
    // skip origin
    movementIterator.next();
    if (!movementIterator.hasNext()) {
      // this happens when we have some kind of movement and an startRegion
      // but no next region
      // example: ROUTE PAUSE NO

      // FIXME That doesnt work anymore because iterator returns always next movement.
      problems.add(ProblemFactory.createProblem(Severity.ERROR, ShipProblemTypes.NONEXTREGION
          .getType(), ship, this));
      return problems;
    }
    CoordinateID nextRegionCoord = movementIterator.next();
    Region nextRegion = getData().getRegion(nextRegionCoord);

    // actually we have to check - is this really a ship (caravans are also marked as ships but
    // travel on land)
    boolean isShip = getGameSpecificStuff().getGameSpecificRules().isShip(ship);

    Rules rules = getData().rules;
    RegionType ozean = rules.getRegionType("Ozean");

    // TODO: We should consider harbors, too. But this is difficult because we don't know if
    // harbor owner is allied with ship owner etc. We better leave it up to the user to decide...
    if (ship.getShoreId() != -1 && isShip) {
      if (nextRegion != null && !nextRegion.getRegionType().equals(ozean)) {
        problems.add(ProblemFactory.createProblem(Severity.ERROR, ShipProblemTypes.NOOCEAN
            .getType(), ship, this));
        return problems;
      }
      // If ship is shored, it can only move deviate by one from the shore direction and only
      // move to an ocean region
      Direction d = Regions.getDirectionObjectsOfCoordinates(modifiedMovement).get(0);
      if (d.getDifference(ship.getShoreId()) > 1 || d.getDifference(ship.getShoreId()) < -1) {
        if (!hasHarbourInRegion(ship.getRegion())) {
          problems.add(ProblemFactory.createProblem(Severity.ERROR, ShipProblemTypes.WRONGSHORE
              .getType(), ship, this));
        } else {
          // harbour in Region -> just warn, no error
          problems.add(ProblemFactory.createProblem(Severity.WARNING,
              ShipProblemTypes.WRONGSHOREHARBOUR.getType(), ship, this));
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
    for (Region lastRegion = null; movementIterator.hasNext()
        && (lastRegion == null || lastRegion != nextRegion); lastRegion = nextRegion, nextRegion =
        getData().getRegion(movementIterator.next())) {
      // check if next region is unknown or ship cannot land in next region and there is no harbor
      // we have to check game specific stuff, because in Allanon a longboat can
      // land everywhere, too
      if (isShip
          && (nextRegion == null || !(getGameSpecificStuff().getGameSpecificRules()
              .canLandInRegion(ship, nextRegion)))) {
        if (nextRegion == null || !hasHarbourInRegion(nextRegion)) {
          problems.add(ProblemFactory.createProblem(Severity.ERROR, ShipProblemTypes.SHIPWRECK
              .getType(), ship, this));
          return problems;
        }
      }
    }

    // overload
    if (ship.getModifiedLoad() > (ship.getMaxCapacity())) {
      problems.add(ProblemFactory.createProblem(Severity.ERROR, ShipProblemTypes.OVERLOADED
          .getType(), ship, this));
    } else if (ship.getShipType().getMaxPersons() > 0
        && ship.getModifiedPersonLoad() > (ship.getMaxPersons() * 1000)) {
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
  private boolean hasHarbourInRegion(Region nextRegion) {
    if (nextRegion.buildings() != null && nextRegion.buildings().size() > 0) {
      // check all buildings
      // i have no reference to the rules, do I?
      // so we have to check for Harbour by BuildingTypeName
      for (Building b : nextRegion.buildings()) {
        if (b.getBuildingType().getName().equalsIgnoreCase("Hafen")) {
          // lets check size
          if (b.getSize() >= b.getBuildingType().getMaxSize())
            // ok...there is an harbour
            return true;
        }
      }
    }
    return false;
  }

  /**
   * Reviews the region for ships with problems.
   * 
   * @see magellan.library.tasks.AbstractInspector#reviewRegion(magellan.library.Region, Severity)
   */
  @Override
  public List<Problem> reviewUnit(Unit u, Severity severity) {
    // we notify errors only
    if (severity != Severity.ERROR)
      return Collections.emptyList();
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
