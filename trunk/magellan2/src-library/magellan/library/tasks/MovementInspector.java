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
import magellan.library.Orders;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.MovementEvaluator;
import magellan.library.relation.LeaveRelation;
import magellan.library.relation.MovementRelation;
import magellan.library.tasks.Problem.Severity;
import magellan.library.tasks.ShipInspector.ShipProblemTypes;
import magellan.library.utils.Regions;

/**
 * Checks land movement for overload or too many horses.
 */
public class MovementInspector extends AbstractInspector {
  /** The singleton instance of this Inspector */
  // public static final MovementInspector INSPECTOR = new MovementInspector(data);

  @SuppressWarnings("javadoc")
  public enum MovementProblemTypes {
    FOOTOVERLOADED, HORSEOVERLOADED, TOOMANYHORSESFOOT, TOOMANYHORSESRIDE, UNITFOLLOWSSELF,
    MOVE_INVALID, UNKNOWNREGION, MOVEMENTTOOLONG, ROUTETOOLONG, OWNERLEAVES;

    private ProblemType type;

    MovementProblemTypes() {
      String name = name().toLowerCase();
      type = ProblemType.create("tasks.movementinspector", name);
    }

    public ProblemType getType() {
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
    if (severity != Severity.WARNING && severity != Severity.ERROR)
      return Collections.emptyList();

    List<Problem> problems = new ArrayList<Problem>();

    Orders orders = u.getOrders2();
    List<MovementRelation> rels = u.getRelations(MovementRelation.class);
    if (rels.size() == 0)
      return Collections.emptyList();
    MovementRelation mRel = rels.iterator().next();

    List<CoordinateID> movement = mRel.getMovement();

    if (severity == Severity.ERROR && movement.size() > 1
        && movement.get(0).equals(movement.get(1))) {
      // this happens when we have some kind of movement and an startRegion
      // but no next region
      // example: ROUTE PAUSE NO
      problems.add(ProblemFactory.createProblem(Severity.ERROR, ShipProblemTypes.NONEXTREGION
          .getType(), u, this, mRel.line));
    }

    if (severity == Severity.WARNING && mRel.getTransporter() == u) {
      if (mRel.unknown) {
        problems.add(ProblemFactory.createProblem(Severity.WARNING,
            MovementProblemTypes.UNKNOWNREGION.getType(), u, this, mRel.line));
      }
      if (mRel.getFutureMovement().size() > 1) {
        if (orders.isToken(orders.get(mRel.line - 1), 0, EresseaConstants.O_MOVE)) {
          problems.add(ProblemFactory.createProblem(Severity.WARNING,
              MovementProblemTypes.MOVEMENTTOOLONG.getType(), u, this, mRel.line));
        } else {
          if (!mRel.getFutureMovement().get(1).equals(mRel.getFutureMovement().get(0))) {
            // future does not start with pause
            problems.add(ProblemFactory.createProblem(Severity.WARNING,
                MovementProblemTypes.ROUTETOOLONG.getType(), u, this, mRel.line));
          }
        }
      }

      // only test for foot/horse movement if unit is not owner of a modified ship
      if ((u.getModifiedShip() == null) || !u.equals(u.getModifiedShip().getModifiedOwnerUnit())) {
        problems.addAll(reviewUnitOnFoot(u, mRel.line));
        if (movement.size() > 0) {
          int count = 0;
          boolean road = true;
          CoordinateID lastID = null;
          for (CoordinateID coordinate : movement) {
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
            problems.addAll(reviewUnitOnHorse(u, mRel.line));
          }
        }
      }
    }

    if (severity == Severity.WARNING && u.getBuilding() != null) {
      Unit newOwner = u.getBuilding().getModifiedOwnerUnit();
      if (u.equals(newOwner) || newOwner == null) {
        SimpleProblem problem = null;
        for (LeaveRelation rel : u.getRelations(LeaveRelation.class)) {
          if (rel.isImplicit()) {
            problem =
                ProblemFactory.createProblem(Severity.WARNING, MovementProblemTypes.OWNERLEAVES
                    .getType(), u, this, mRel.line);
          } else {
            problem = null;
            break;
          }
        }
        if (problem != null) {
          problems.add(problem);
        }
      }
    }

    if (problems.isEmpty())
      return Collections.emptyList();
    else
      return problems;
  }

  private List<Problem> reviewUnitOnFoot(Unit u, int line) {
    int maxOnFoot = getGameSpecificStuff().getMovementEvaluator().getPayloadOnFoot(u);

    if (maxOnFoot == MovementEvaluator.CAP_UNSKILLED)
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

    if (maxOnHorse == MovementEvaluator.CAP_UNSKILLED)
      return Collections.singletonList((Problem) (ProblemFactory.createProblem(Severity.WARNING,
          MovementProblemTypes.TOOMANYHORSESRIDE.getType(), u, this, line)));

    if (maxOnHorse != MovementEvaluator.CAP_NO_HORSES) {
      int modLoad = getGameSpecificStuff().getMovementEvaluator().getModifiedLoad(u);

      if ((maxOnHorse - modLoad) < 0)
        return Collections.singletonList((Problem) (ProblemFactory.createProblem(Severity.WARNING,
            MovementProblemTypes.HORSEOVERLOADED.getType(), u, this, line)));
    }

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
