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
import java.util.LinkedList;
import java.util.List;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Order;
import magellan.library.Orders;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.FollowUnitOrder;
import magellan.library.gamebinding.MovementOrder;
import magellan.library.tasks.Problem.Severity;
import magellan.library.tasks.ShipInspector.ShipProblemTypes;
import magellan.library.utils.Regions;

/**
 * Checks land movement for overload or too many horses.
 */
public class MovementInspector extends AbstractInspector {
  /** The singleton instance of this Inspector */
  // public static final MovementInspector INSPECTOR = new MovementInspector(data);

  enum MovementProblemTypes {
    FOOTOVERLOADED, HORSEOVERLOADED, TOOMANYHORSESFOOT, TOOMANYHORSESRIDE, UNITFOLLOWSSELF;

    private ProblemType type;

    MovementProblemTypes() {
      String name = name().toLowerCase();
      type = ProblemType.create("tasks.movementinspector", name);
    }

    ProblemType getType() {
      return type;
    }
  }

  /**
   * Returns an instance this inspector.
   * 
   * @return The singleton instance of MovementInspector
   */
  public static MovementInspector getInstance(GameData data) {
    return new MovementInspector(data);
  }

  private Collection<ProblemType> types;

  protected MovementInspector(GameData data) {
    super(data);
  }

  /**
   * Checks the specified movement for overload and too many horses.
   * 
   * @see magellan.library.tasks.AbstractInspector#reviewUnit(magellan.library.Unit, Severity)
   */
  @Override
  public List<Problem> reviewUnit(Unit u, Severity severity) {
    if ((u == null) || u.ordersAreNull())
      return Collections.emptyList();

    // we only warn
    if (severity != Severity.WARNING)
      return Collections.emptyList();

    List<Problem> problems = new ArrayList<Problem>();

    int movementOrderLine = -1;
    int line = 0;
    Orders orders = u.getOrders2();
    for (Order order : orders) {
      line++;
      try {
        if (order.isValid() && order instanceof FollowUnitOrder) {
          movementOrderLine = line;
          if (orders.isToken(order, 1, EresseaConstants.O_UNIT)) {
            if (UnitID.createUnitID(order.getToken(2).getText(), getData().base).equals(u.getID())) {
              problems.add(ProblemFactory.createProblem(Severity.ERROR,
                  MovementProblemTypes.UNITFOLLOWSSELF.getType(), u, this, line));
            }
          }
        } else if (order.isValid() && order instanceof MovementOrder) {
          movementOrderLine = line;
        }
      } catch (Exception e) {
      }
    }

    if (!u.getModifiedMovement().isEmpty() || movementOrderLine > 0) {

      if (u.getModifiedMovement().size() > 1
          && u.getModifiedMovement().get(0).equals(u.getModifiedMovement().get(1))) {
        // this happens when we have some kind of movement and an startRegion
        // but no next region
        // example: ROUTE PAUSE NO
        problems.add(ProblemFactory.createProblem(Severity.ERROR, ShipProblemTypes.NONEXTREGION
            .getType(), u, this, movementOrderLine));
      }

      // only test for foot/horse movement if unit is not owner of a modified ship
      if ((u.getModifiedShip() == null) || !u.equals(u.getModifiedShip().getModifiedOwnerUnit())) {
        problems.addAll(reviewUnitOnFoot(u, movementOrderLine));
        if (u.getModifiedMovement().size() > 0) {

          int count = 0;
          boolean road = true;
          CoordinateID lastID = null;
          for (CoordinateID coordinate : u.getModifiedMovement()) {
            Region currentRegion = getData().getRegion(coordinate);
            if (lastID == null) {
              lastID = coordinate;
            } else {
              if (coordinate.equals(lastID) || count > 2) {
                break;
              }
              Region lastRegion = getData().getRegion(lastID);
              if (currentRegion == null || lastRegion == null
                  || !Regions.isCompleteRoadConnection(lastRegion, currentRegion)) {
                // currentRegion))
                road = false;
              }

              lastID = coordinate;
              count++;
            }
          }
          if ((count == 2 && !road) || count > 2) {
            problems.addAll(reviewUnitOnHorse(u, movementOrderLine));
          }
        }
      }
    }

    // TODO: check for movement length
    // TODO: check for roads

    // Reminder: Regions.isCompleteRoadConnection (Fiete) (nur für 2 benachbarte Regionen)

    /*
     * switch(u.getRadius()) { case 0: problems.add(new CriticizedWarning(u, this,
     * "Cannot move, radius is on "+u.getRadius()+"!")); case 1: problems.add(new
     * CriticizedWarning(u, this, "Cannot ride, radius is on "+u.getRadius()+"!")); default: ; }
     */
    if (problems.isEmpty())
      return Collections.emptyList();
    else
      return problems;
  }

  private boolean hasMovementOrder(Unit u) {
    Orders orders = u.getOrders2();
    for (Order order : orders) {
      if (order.isEmpty() || !order.isValid()) {
        continue;
      }
      if (orders.isToken(order, 0, EresseaConstants.O_MOVE)
          || orders.isToken(order, 0, EresseaConstants.O_ROUTE))
        return true;
      try {
        if (orders.isToken(order, 0, EresseaConstants.O_FOLLOW)) {
          if (orders.isToken(order, 1, EresseaConstants.O_UNIT))
            return true;
        }
      } catch (Exception e) {

      }
    }
    return false;
  }

  private List<Problem> reviewUnitOnFoot(Unit u, int line) {
    int maxOnFoot = getGameSpecificStuff().getMovementEvaluator().getPayloadOnFoot(u);

    if (maxOnFoot == Unit.CAP_UNSKILLED)
      return Collections.singletonList((Problem) (ProblemFactory.createProblem(Severity.WARNING,
          MovementProblemTypes.TOOMANYHORSESFOOT.getType(), u, this, line)));

    int modLoad = getGameSpecificStuff().getMovementEvaluator().getModifiedLoad(u);

    if ((maxOnFoot - modLoad) < 0)
      return Collections.singletonList((Problem) (ProblemFactory.createProblem(Severity.WARNING,
          MovementProblemTypes.FOOTOVERLOADED.getType(), u, this, line)));

    return Collections.emptyList();
  }

  private List<Problem> reviewUnitOnHorse(Unit u, int line) {
    int maxOnHorse = getGameSpecificStuff().getMovementEvaluator().getPayloadOnHorse(u);

    if (maxOnHorse == Unit.CAP_UNSKILLED)
      return Collections.singletonList((Problem) (ProblemFactory.createProblem(Severity.WARNING,
          MovementProblemTypes.TOOMANYHORSESRIDE.getType(), u, this, line)));

    if (maxOnHorse != Unit.CAP_NO_HORSES) {
      int modLoad = getGameSpecificStuff().getMovementEvaluator().getModifiedLoad(u);

      if ((maxOnHorse - modLoad) < 0)
        return Collections.singletonList((Problem) (ProblemFactory.createProblem(Severity.WARNING,
            MovementProblemTypes.HORSEOVERLOADED.getType(), u, this, line)));
    }
    // FIXME if unit has no horses, we should report an error

    return Collections.emptyList();
  }

  public Collection<ProblemType> getTypes() {
    if (types == null) {
      types = new LinkedList<ProblemType>();
      for (MovementProblemTypes t : MovementProblemTypes.values()) {
        types.add(t.getType());
      }
    }
    return types;
  }
}
