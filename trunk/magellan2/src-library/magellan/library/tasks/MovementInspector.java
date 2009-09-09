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
import java.util.StringTokenizer;

import magellan.library.CoordinateID;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.tasks.Problem.Severity;
import magellan.library.utils.Regions;
import magellan.library.utils.Resources;

/**
 * Checks land movement for overload or too many horses.
 */
public class MovementInspector extends AbstractInspector {
  /** The singleton instance of this Inspector */
  public static final MovementInspector INSPECTOR = new MovementInspector();

  enum MovementProblemTypes {
    FOOTOVERLOADED, HORSEOVERLOADED, TOOMANYHORSESFOOT, TOOMANYHORSESRIDE, UNITFOLLOWSSELF;

    private ProblemType type;

    MovementProblemTypes() {
      String name = this.name().toLowerCase();
      String message = Resources.get("tasks.movementinspector." + name + ".message");
      String typeName = Resources.get("tasks.movementinspector." + name + ".name", false);
      if (typeName == null)
        typeName = message;
      String description = Resources.get("tasks.movementinspector." + name + ".description", false);
      String group = Resources.get("tasks.movementinspector." + name + ".group", false);
      type = new ProblemType(typeName, group, description, message, getInstance());
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
  public static MovementInspector getInstance() {
    return MovementInspector.INSPECTOR;
  }

  private Collection<ProblemType> types;

  protected MovementInspector() {
  }

  /**
   * Checks the specified movement for overload and too many horses.
   * 
   * @see magellan.library.tasks.AbstractInspector#reviewUnit(magellan.library.Unit, int)
   */
  @Override
  public List<Problem> reviewUnit(Unit u, Severity severity) {
    if ((u == null) || u.ordersAreNull()) {
      return Collections.emptyList();
    }

    // we only warn
    if (severity != Severity.WARNING) {
      return Collections.emptyList();
    }

    List<Problem> problems = new ArrayList<Problem>();

    if (!u.getModifiedMovement().isEmpty() || hasMovementOrder(u)) {
      // only test for foot/horse movement if unit is not owner of a modified ship
      if ((u.getModifiedShip() == null) || !u.equals(u.getModifiedShip().getOwnerUnit())) {
        problems.addAll(reviewUnitOnFoot(u));
        if (u.getModifiedMovement().size() > 0) {

          int count = 0;
          boolean road = true;
          CoordinateID lastID = null;
          for (CoordinateID coordinate : u.getModifiedMovement()) {
            Region currentRegion = u.getRegion().getData().getRegion(coordinate);
            if (lastID == null) {
              lastID = coordinate;
            } else {
              if (coordinate.equals(lastID) || count > 2) // found PAUSE
                break;
              Region lastRegion = u.getRegion().getData().getRegion(lastID);
              if (currentRegion == null || lastRegion == null
                  || !Regions.isCompleteRoadConnection(lastRegion, currentRegion)) // ;!roadTo(lastRegion,
                                                                                   // currentRegion))
                road = false;

              lastID = coordinate;
              count++;
            }
          }
          if ((count == 2 && !road) || count > 2) {
            problems.addAll(reviewUnitOnHorse(u));
          }
        }
      }
    }

    int line = 0;
    for (String order : u.getOrders()) {
      line++;
      try {
        if (order.trim().startsWith(Resources.getOrderTranslation(EresseaConstants.O_FOLLOW))) {
          StringTokenizer st = new StringTokenizer(order.trim());
          st.nextToken();
          if (Resources.getOrderTranslation(EresseaConstants.O_UNIT).equals(st.nextToken())) {
            if (UnitID.createUnitID(st.nextToken(), u.getRegion().getData().base).equals(u.getID())) {
              problems.add(ProblemFactory.createProblem(Severity.ERROR,
                  MovementProblemTypes.UNITFOLLOWSSELF.getType(), u, line));
            }
          }
        }
      } catch (Exception e) {
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
    if (problems.isEmpty()) {
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
        if (order.trim().startsWith(Resources.getOrderTranslation(EresseaConstants.O_FOLLOW))) {
          StringTokenizer st = new StringTokenizer(order.trim());
          st.nextToken();
          if (Resources.getOrderTranslation(EresseaConstants.O_UNIT).equals(st.nextToken())) {
            return true;
          }
        }
      } catch (Exception e) {

      }
    }
    return false;
  }

  private List<Problem> reviewUnitOnFoot(Unit u) {
    int maxOnFoot = u.getPayloadOnFoot();

    if (maxOnFoot == Unit.CAP_UNSKILLED) {
      return Collections.singletonList((Problem) (ProblemFactory.createProblem(Severity.WARNING,
          MovementProblemTypes.TOOMANYHORSESFOOT.getType(), u)));
    }

    int modLoad = u.getModifiedLoad();

    if ((maxOnFoot - modLoad) < 0) {
      return Collections.singletonList((Problem) (ProblemFactory.createProblem(Severity.WARNING,
          MovementProblemTypes.FOOTOVERLOADED.getType(), u)));
    }

    return Collections.emptyList();
  }

  private List<Problem> reviewUnitOnHorse(Unit u) {
    int maxOnHorse = u.getPayloadOnHorse();

    if (maxOnHorse == Unit.CAP_UNSKILLED) {
      return Collections.singletonList((Problem) (ProblemFactory.createProblem(Severity.WARNING,
          MovementProblemTypes.TOOMANYHORSESRIDE.getType(), u)));
    }

    if (maxOnHorse != Unit.CAP_NO_HORSES) {
      int modLoad = u.getModifiedLoad();

      if ((maxOnHorse - modLoad) < 0) {
        return Collections.singletonList((Problem) (ProblemFactory.createProblem(Severity.WARNING,
            MovementProblemTypes.HORSEOVERLOADED.getType(), u)));
      }
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
