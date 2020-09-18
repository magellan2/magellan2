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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import magellan.library.CoordinateID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Item;
import magellan.library.Order;
import magellan.library.Orders;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.MovementOrder;
import magellan.library.relation.MaintenanceRelation;
import magellan.library.rules.ItemType;
import magellan.library.tasks.Problem.Severity;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * Checks land movement for overload or too many horses.
 */
public class MaintenanceInspector extends AbstractInspector {

  public enum MaintenanceProblemTypes {
    UNITSTARVING, BUILDINGMAINTENANCE, LEARNCOSTS, UNKNOWNDESTINATION;

    public ProblemType type;

    MaintenanceProblemTypes() {
      String name = name().toLowerCase();
      type = ProblemType.create("tasks.maintenanceinspector", name);
    }

    ProblemType getType() {
      return type;
    }
  }

  /**
   * Returns an instance this inspector.
   * 
   * @return The singleton instance of MaintenanceInspector
   */
  public static MaintenanceInspector getInstance(GameData data) {
    return new MaintenanceInspector(data);
  }

  private Collection<ProblemType> types;
  private Map<Unit, Region> movements = new HashMap<Unit, Region>();

  protected MaintenanceInspector(GameData data) {
    super(data);

  }

  @Override
  public List<Problem> listProblems(Region r) {
    if (r.units().size() == 0)
      return Collections.emptyList();

    ItemType silverType = getData().getRules().getItemType(EresseaConstants.I_USILVER);

    /** return value */
    List<Problem> problems = new ArrayList<Problem>();
    /** List of regions whose maintenance could have changed */
    HashSet<Region> regions = new HashSet<Region>();
    /** List of factions whose maintenance could have changed */
    HashSet<Faction> factions = new HashSet<Faction>();

    regions.add(r);
    for (Unit u : r.units()) {
      // building upkeep
      for (MaintenanceRelation rel : u.getRelations(MaintenanceRelation.class)) {
        if (rel.problem != null) {
          problems.add(ProblemFactory.createProblem(rel.problem.getSeverity(), rel.problem
              .getType(), r, u, u.getFaction(), u, this, rel.problem.getMessage(), rel.line));
        }
      }

      // unit upkeep preparation
      int movementOrderLine = getMovementOrderLine(u);
      CoordinateID destination = u.getNewRegion();
      Region destRegion = destination == null ? null : getData().getRegion(destination);

      factions.add(u.getFaction());
      if (movements.containsKey(u)) {
        // add old region, it has changed, too
        regions.add(movements.get(u));
      }

      if (destRegion == null) {
        // there is no region at destination
        if (u.getItem(silverType) == null
            || u.getItem(silverType).getAmount() < u.getRace().getMaintenance()) {
          problems.add(ProblemFactory.createProblem(Severity.WARNING,
              MaintenanceProblemTypes.UNKNOWNDESTINATION.getType(), r, u, u.getFaction(), u, this,
              Resources.get("tasks.maintenanceinspector.unknowndestination.message", destination),
              movementOrderLine));
        }
        movements.remove(u);
      } else {
        regions.add(destRegion);
        movements.put(u, destRegion);
      }

      // if (u.getNewRegion() != null) {
      // u.getNewRegion().removeMaintenance(u);
      // }
      // if (destination != null && destination != u.getRegion()) {
      // destination.addMaintenance(u);
      // }
    }

    for (Region r2 : regions) {
      if (r2 == null) {
        continue;
      }
      for (Faction f : factions) {
        if (f == null) {
          continue;
        }
        int has = 0, needs = 0;
        Unit lastUnit = null;
        for (Unit u : r2.units()) {
          if (f == u.getFaction() && r2.getCoordinate().equals(u.getNewRegion())) {
            Item item = u.getModifiedItem(silverType);
            int silver = 0;
            if (item != null) {
              silver = item.getAmount();
            }
            has += silver;
            needs += u.getRace().getMaintenance() * u.getModifiedPersons();
            if (silver <= u.getRace().getMaintenance() * u.getModifiedPersons()) {
              lastUnit = u;
            }
          }
        }
        for (Unit u : r2.getMaintained()) {
          if (f == u.getFaction()) {
            if (!r2.getCoordinate().equals(u.getNewRegion())) {
              Logger.getInstance(this.getClass()).info("hmmm....");
            }
            Item item = u.getModifiedItem(silverType);
            int silver = 0;
            if (item != null) {
              silver = item.getAmount();
            }
            has += silver;
            needs += u.getRace().getMaintenance() * u.getModifiedPersons();
            if (silver <= u.getRace().getMaintenance() * u.getModifiedPersons()) {
              lastUnit = u;
            }
          }
        }
        if (has < needs) {
          SimpleProblem problem =
              ProblemFactory.createProblem(Severity.WARNING, MaintenanceProblemTypes.UNITSTARVING
                  .getType(), r, null, f, lastUnit, this, Resources.get(
                      "tasks.maintenanceinspector.unitstarving.message", r2), -1);
          if (!checkIgnoreUnit(lastUnit, problem)) {
            // problems.add(ProblemFactory.createProblem(Severity.WARNING,
            // MaintenanceProblemTypes.UNITSTARVING.getType(), lastUnit, this, -1));
            problems.add(problem);
          }
        }
      }
    }
    return problems;
  }

  private int getMovementOrderLine(Unit u) {
    int movementOrderLine = -1;
    int line = 0;
    Orders orders = u.getOrders2();
    for (Order order : orders) {
      line++;
      if (order.getProblem() == null && order instanceof MovementOrder) {
        movementOrderLine = line;
      }
    }
    return movementOrderLine;
  }

  public Collection<ProblemType> getTypes() {
    if (types == null) {
      types = new LinkedList<ProblemType>();
      for (MaintenanceProblemTypes t : MaintenanceProblemTypes.values()) {
        types.add(t.getType());
      }
    }
    return types;
  }

  /**
   * @see magellan.library.tasks.AbstractInspector#setGameData(magellan.library.GameData)
   */
  @Override
  public void setGameData(GameData gameData) {
    super.setGameData(gameData);
    movements.clear();
  }

  @Override
  public Unit suppress(Problem p) {
    ((Unit) p.getObject()).addOrderAt(0, getSuppressUnitComment(p));
    return (Unit) p.getObject();
  }
}
