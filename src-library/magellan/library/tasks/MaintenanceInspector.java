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
import magellan.library.relation.RecruitmentRelation;
import magellan.library.relation.UnitRelation;
import magellan.library.rules.ItemType;
import magellan.library.tasks.Problem.Severity;
import magellan.library.utils.Resources;

/**
 * Checks land movement for overload or too many horses.
 */
public class MaintenanceInspector extends AbstractInspector {

  public enum MaintenanceProblemTypes {
    UNITSTARVING, BUILDINGMAINTENANCE, LEARNCOSTS, UNKNOWNDESTINATION, MISSINGREQUIREMENT;

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
  private ItemType silverType;

  protected MaintenanceInspector(GameData data) {
    super(data);

  }

  @Override
  public List<Problem> listProblems(Region r) {
    if (r.units().size() == 0)
      return Collections.emptyList();

    silverType = getData().getRules().getItemType(EresseaConstants.I_USILVER);
    if (silverType == null)
      return Collections.singletonList(ProblemFactory.createProblem(Severity.ERROR, ProblemType.create(
          "tasks.maintenanceinspector", "nosilver"), null, this, "silver type not found", 0));

    /** return value */
    List<Problem> problems = new ArrayList<Problem>();
    /** List of regions whose maintenance could have changed */
    HashSet<Region> regions = new HashSet<Region>();
    /** List of factions whose maintenance could have changed */
    HashSet<Faction> factions = new HashSet<Faction>();

    regions.add(r);
    for (Unit u : r.units()) {
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
    }

    for (Region r2 : regions) {
      if (r2 == null) {
        continue;
      }
      for (Faction f : factions) {
        if (f == null) {
          continue;
        }
        int diff = 0;
        Unit lastUnit = null;

        for (Unit u : r2.units()) {
          if (f == u.getFaction() && r2.getCoordinate().equals(u.getNewRegion())) {
            diff += checkUnitMaintenance(problems, u, r);
            lastUnit = u;
          }
        }
        for (Unit u : r2.getMaintained()) {
          if (f == u.getFaction() && r2.getCoordinate().equals(u.getNewRegion())) {
            diff += checkUnitMaintenance(problems, u, r);
            lastUnit = u;
          }
        }
        if (diff < 0 && lastUnit != null) {
          Region r3 = getData().getRegion(lastUnit.getNewRegion());
          SimpleProblem problem =
              ProblemFactory.createProblem(Severity.WARNING, MaintenanceProblemTypes.UNITSTARVING
                  .getType(), r3, null, f, lastUnit, this, Resources.get(
                      "tasks.maintenanceinspector.unitstarving.message", r3), -1);
          if (!checkIgnoreUnit(lastUnit, problem)) {
            problems.add(problem);
          }
        }
      }
    }
    return problems;
  }

  private int checkUnitMaintenance(List<Problem> problems, Unit u, Region r) {
    int has = getSilver(u);
    int needs = getMaintenance(u, has);
    // problem added by getMaintenance
    // if (has < needs && u.getRegion() == r) {
    // SimpleProblem problem =
    // ProblemFactory.createProblem(Severity.WARNING,
    // MaintenanceProblemTypes.MISSINGREQUIREMENT.getType(),
    // r, u, u.getFaction(), u, this,
    // Resources.get("tasks.maintenanceinspector.missingrequirement.message", u), -1);
    // if (!checkIgnoreUnit(u, problem)) {
    // problems.add(problem);
    // }
    // }
    return has - needs - u.getRace().getMaintenance() * u.getModifiedPersons();
  }

  private int getSilver(Unit u) {
    Item item = u.getModifiedItem(silverType);
    if (item != null)
      return item.getAmount();
    return 0;
  }

  private int getMaintenance(Unit u, int has) {
    int costs = 0;
    for (UnitRelation rel : u.getRelations()) {
      int cost = apply(rel);
      costs += cost;
      if (cost > 0 && costs > has) {
        rel.setWarning(Resources.get("tasks.maintenanceinspector.missingrequirement.message", u),
            MaintenanceProblemTypes.MISSINGREQUIREMENT.getType());
      }
    }

    return costs;
  }

  private int apply(UnitRelation rel) {
    if (rel instanceof RecruitmentRelation)
      return apply((RecruitmentRelation) rel);
    if (rel instanceof MaintenanceRelation)
      return apply((MaintenanceRelation) rel);
    return 0;
  }

  private int apply(RecruitmentRelation rel) {
    return rel.costs;
  }

  private int apply(MaintenanceRelation rel) {
    if (silverType.equals(rel.itemType))
      return rel.getCosts();
    return 0;
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
